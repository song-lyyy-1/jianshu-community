package com.jianshu.system;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证模块系统测试
 * 使用 MockMvc 模拟完整 HTTP 请求流程，测试 AuthController + JwtAuthFilter + Service 的完整链路。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthSystemTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试登录成功：POST /api/auth/login 正确账号密码，返回 200 + token
     */
    @Test
    void testLoginSuccess() throws Exception {
        String body = "{\"username\":\"zhangsan\",\"password\":\"test123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("zhangsan"))
                .andExpect(jsonPath("$.data.user.nickname").value("张三"));
    }

    /**
     * 测试登录失败：密码错误，返回 code=400
     * 控制器捕获 RuntimeException 返回 Result.error(400, "密码错误")，HTTP 状态仍为 200
     */
    @Test
    void testLoginWrongPassword() throws Exception {
        String body = "{\"username\":\"zhangsan\",\"password\":\"wrongpassword\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("密码错误"));
    }

    /**
     * 测试登录参数校验失败：缺少用户名字段，返回 400（@Valid 触发的参数校验异常）
     */
    @Test
    void testLoginMissingUsername() throws Exception {
        String body = "{\"password\":\"test123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试注册成功：POST /api/auth/register 注册成功返回 200 + token
     */
    @Test
    void testRegisterSuccess() throws Exception {
        String body = "{\"username\":\"newuser\",\"password\":\"newpass123\",\"nickname\":\"新用户\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("newuser"))
                .andExpect(jsonPath("$.data.user.nickname").value("新用户"));
    }

    /**
     * 测试注册失败：重复用户名返回 code=400
     */
    @Test
    void testRegisterDuplicateUsername() throws Exception {
        String body = "{\"username\":\"zhangsan\",\"password\":\"test123\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    /**
     * 测试注册参数校验失败：密码太短（少于6位），返回 400
     */
    @Test
    void testRegisterPasswordTooShort() throws Exception {
        String body = "{\"username\":\"newuser\",\"password\":\"123\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试无 token 访问受保护 API：GET /api/user/profile 返回 401
     */
    @Test
    void testAccessProtectedApiWithoutToken() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 测试无效 token 访问受保护 API：返回 401
     */
    @Test
    void testAccessProtectedApiWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 测试无 token 访问公开 API：GET /api/article/list 返回 200
     */
    @Test
    void testAccessPublicApiWithoutToken() throws Exception {
        mockMvc.perform(get("/api/article/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 辅助方法：登录并返回 token
     */
    private String loginAndGetToken(String username, String password) throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        String response = result.getResponse().getContentAsString();
        return JsonPath.parse(response).read("$.data.token");
    }
}
