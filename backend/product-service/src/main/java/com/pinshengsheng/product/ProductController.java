package com.pinshengsheng.product;

import com.pinshengsheng.product.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProductController {
    @GetMapping("/api/product/{id}")
    public ApiResponse<Map<String, Object>> getProduct(
            @PathVariable("id") Integer id) {

        if (!Integer.valueOf(1).equals(id)) {
            return ApiResponse.fail(404, "商品不存在");
        }

        Map<String, Object> product = Map.of(
                "id", id,
                "name", "拼省省蓝牙耳机",
                "price", 199.00,
                "stock", 100
        );

        return ApiResponse.success(product);
    }
}
