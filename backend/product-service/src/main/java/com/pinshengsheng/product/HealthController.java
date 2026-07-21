package com.pinshengsheng.product;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/product/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "product-service",
                "status", "UP",
                "project", "pinshengsheng"
        );
    }
}
