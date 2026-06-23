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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户模块系统测试
 * 使用 MockMvc 模拟完整 HTTP 请求流程，测试 UserController + JwtAuthFilter + Service 的完整链路。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserSystemTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试获取个人中心信息：登录后 GET /api/user/profile 返回用户信息和统计数据
     */
    @Test
    void testGetProfile() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");

        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.user.id").value(1))
                .andExpect(jsonPath("$.data.user.username").value("zhangsan"))
                .andExpect(jsonPath("$.data.user.nickname").value("张三"))
                .andExpect(jsonPath("$.data.stats.articleCount").value(1))
                .andExpect(jsonPath("$.data.stats.totalViews").value(256))
                .andExpect(jsonPath("$.data.stats.totalLikes").value(32))
                .andExpect(jsonPath("$.data.stats.totalFavorites").value(18));
    }

    /**
     * 测试无 token 获取个人中心：返回 401
     */
    @Test
    void testGetProfileNoAuth() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 测试获取统计数据：登录后 GET /api/user/stats 返回统计信息
     */
    @Test
    void testGetStats() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");

        mockMvc.perform(get("/api/user/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.articleCount").value(1))
                .andExpect(jsonPath("$.data.totalViews").value(256))
                .andExpect(jsonPath("$.data.totalLikes").value(32))
                .andExpect(jsonPath("$.data.totalFavorites").value(18));
    }

    /**
     * 测试更新用户信息：登录后 PUT /api/user/update 更新昵称和简介
     */
    @Test
    void testUpdateUser() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");
        String body = "{\"nickname\":\"新昵称\",\"bio\":\"新的个人简介\"}";

        mockMvc.perform(put("/api/user/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.bio").value("新的个人简介"));
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
