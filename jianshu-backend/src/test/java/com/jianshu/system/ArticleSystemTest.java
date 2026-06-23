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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 文章模块系统测试
 * 使用 MockMvc 模拟完整 HTTP 请求流程，测试 ArticleController + JwtAuthFilter + Service 的完整链路。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ArticleSystemTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试获取文章列表：GET /api/article/list 返回 200，data 包含 records 和 total
     */
    @Test
    void testGetArticleList() throws Exception {
        mockMvc.perform(get("/api/article/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].title").isNotEmpty())
                .andExpect(jsonPath("$.data.records[0].author").exists());
    }

    /**
     * 测试带关键词搜索：搜索"三国"应只返回 1 篇文章
     */
    @Test
    void testGetArticleListWithKeyword() throws Exception {
        mockMvc.perform(get("/api/article/list").param("keyword", "三国"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("《三国演义》"));
    }

    /**
     * 测试获取文章详情：GET /api/article/detail/1 返回 200，含 liked/favorited 字段
     */
    @Test
    void testGetArticleDetail() throws Exception {
        mockMvc.perform(get("/api/article/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("《三国演义》"))
                .andExpect(jsonPath("$.data.liked").exists())
                .andExpect(jsonPath("$.data.favorited").exists())
                .andExpect(jsonPath("$.data.author.nickname").value("张三"));
    }

    /**
     * 测试获取不存在的文章详情：返回 code=404
     */
    @Test
    void testGetArticleDetailNotFound() throws Exception {
        mockMvc.perform(get("/api/article/detail/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("文章不存在"));
    }

    /**
     * 测试创建文章：先登录获取 token，POST /api/article/create 创建文章返回 200
     */
    @Test
    void testCreateArticle() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");
        String body = "{\"title\":\"测试文章\",\"content\":\"这是测试文章内容\",\"status\":1}";

        mockMvc.perform(post("/api/article/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    /**
     * 测试无 token 创建文章：返回 401
     */
    @Test
    void testCreateArticleNoAuth() throws Exception {
        String body = "{\"title\":\"测试文章\",\"content\":\"这是测试文章内容\",\"status\":1}";

        mockMvc.perform(post("/api/article/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 测试更新自己的文章：zhangsan 登录后更新 article 1（自己的文章）
     */
    @Test
    void testUpdateArticle() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");
        String body = "{\"title\":\"更新后的标题\",\"content\":\"更新后的内容\"}";

        mockMvc.perform(put("/api/article/update/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    /**
     * 测试更新别人的文章：lisi 登录后更新 article 1（zhangsan 的文章），返回 code=403
     */
    @Test
    void testUpdateArticleNoPermission() throws Exception {
        String token = loginAndGetToken("lisi", "test123");
        String body = "{\"title\":\"恶意修改\",\"content\":\"恶意修改内容\"}";

        mockMvc.perform(put("/api/article/update/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权限修改此文章"));
    }

    /**
     * 测试删除自己的文章：zhangsan 登录后删除 article 3（自己的草稿文章）
     */
    @Test
    void testDeleteArticle() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");

        mockMvc.perform(delete("/api/article/delete/3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试获取我的文章列表：zhangsan 登录后获取，应有 2 篇文章（1 已发布 + 1 草稿）
     */
    @Test
    void testGetMyArticles() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");

        mockMvc.perform(get("/api/article/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records").isArray());
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
