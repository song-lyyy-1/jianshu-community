package com.jianshu.unit;

import com.jianshu.dto.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Result 单元测试
 * 测试统一返回结果 Result 的静态工厂方法。
 */
class ResultTest {

    /**
     * 测试 success(data) 返回 code=200, message="success", data=传入值
     */
    @Test
    void testSuccessWithData() {
        String data = "hello";
        Result<String> result = Result.success(data);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals(data, result.getData());
    }

    /**
     * 测试 success(message, data) 返回 code=200, message=传入message, data=传入值
     */
    @Test
    void testSuccessWithMessage() {
        String message = "操作成功";
        Integer data = 42;
        Result<Integer> result = Result.success(message, data);

        assertEquals(200, result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(data, result.getData());
    }

    /**
     * 测试 error(code, message) 返回指定 code 和 message, data=null
     */
    @Test
    void testError() {
        int code = 500;
        String message = "服务器错误";
        Result<Object> result = Result.error(code, message);

        assertEquals(code, result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
    }
}
