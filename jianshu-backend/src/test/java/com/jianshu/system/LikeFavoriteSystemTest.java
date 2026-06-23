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
 * 点赞与收藏模块系统测试
 * 使用 MockMvc 模拟完整 HTTP 请求流程，测试 LikeController/FavoriteController + JwtAuthFilter + Service 的完整链路。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LikeFavoriteSystemTest {

    @Autowired
    private MockMvc mockMvc;

    // ===================== 点赞测试 =====================

    /**
     * 测试点赞：zhangsan 登录后点赞 article 1，返回 liked=true 和最新的 likeCount
     */
    @Test
    void testToggleLike() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");

        mockMvc.perform(post("/api/like/toggle/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").isNumber());
    }

    /**
     * 测试取消点赞：zhangsan 先点赞 article 1，再次调用取消点赞，返回 liked=false
     */
    @Test
    void testToggleLikeCancel() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");

        // 第一次点赞
        mockMvc.perform(post("/api/like/toggle/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true));

        // 再次调用取消点赞
        mockMvc.perform(post("/api/like/toggle/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.liked").value(false));
    }

    /**
     * 测试无 token 点赞：返回 401
     */
    @Test
    void testToggleLikeNoAuth() throws Exception {
        mockMvc.perform(post("/api/like/toggle/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 测试获取我点赞的文章列表：zhangsan 已点赞 红楼梦(article_id=2)
     */
    @Test
    void testGetMyLikedArticles() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");

        mockMvc.perform(get("/api/like/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("《红楼梦》"))
                .andExpect(jsonPath("$.data.records[0].author.nickname").value("李四"));
    }

    // ===================== 收藏测试 =====================

    /**
     * 测试收藏：zhangsan 登录后收藏 article 1，返回 favorited=true 和最新的 favoriteCount
     */
    @Test
    void testToggleFavorite() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");

        mockMvc.perform(post("/api/favorite/toggle/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.favorited").value(true))
                .andExpect(jsonPath("$.data.favoriteCount").isNumber());
    }

    /**
     * 测试取消收藏：zhangsan 先收藏 article 1，再次调用取消收藏，返回 favorited=false
     */
    @Test
    void testToggleFavoriteCancel() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");

        // 第一次收藏
        mockMvc.perform(post("/api/favorite/toggle/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited").value(true));

        // 再次调用取消收藏
        mockMvc.perform(post("/api/favorite/toggle/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.favorited").value(false));
    }

    /**
     * 测试获取我收藏的文章列表：zhangsan 已收藏 红楼梦(article_id=2)
     */
    @Test
    void testGetMyFavoriteArticles() throws Exception {
        String token = loginAndGetToken("zhangsan", "test123");

        mockMvc.perform(get("/api/favorite/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("《红楼梦》"))
                .andExpect(jsonPath("$.data.records[0].author.nickname").value("李四"));
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
