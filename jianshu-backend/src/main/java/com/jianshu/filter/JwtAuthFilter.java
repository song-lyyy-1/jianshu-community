package com.jianshu.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianshu.dto.Result;
import com.jianshu.util.JwtUtil;
import com.jianshu.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/article/list",
            "/api/article/detail/*"
    );

    public JwtAuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // OPTIONS预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 白名单放行
        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 从Header取Authorization
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorizedResponse(response, "未提供认证令牌");
            return;
        }

        String token = authorization.substring(7);
        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            UserContext.setUserId(userId);
            UserContext.setUsername(username);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            writeUnauthorizedResponse(response, "认证令牌无效或已过期");
        } finally {
            UserContext.clear();
        }
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        // 手动添加 CORS 头，确保 Filter 层返回的 401 也能被前端跨域读取
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Max-Age", "3600");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}