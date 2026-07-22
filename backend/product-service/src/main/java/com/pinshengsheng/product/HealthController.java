package com.pinshengsheng.product;
import java.util.Map;

import com.pinshengsheng.product.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/product/health")
    public ApiResponse<Map<String,String>> health() {
        Map<String,String> data = Map.of(
                "service", "product-service",
                "status", "UP",
                "project", "pinshengsheng"
        );
        return ApiResponse.success(data);
    }
}
