package com.pinshengsheng.order.client;

import com.pinshengsheng.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// 订单服务通过这个客户端调用商品服务的库存接口
@Component
public class StockClient {

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public StockClient(
            RestTemplate restTemplate,
            @Value("${spring.product-service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    public boolean lockStock(Long skuId, Integer quantity) {
        return operateStock(skuId, quantity, "lock");
    }

    public boolean releaseStock(Long skuId, Integer quantity) {
        return operateStock(skuId, quantity, "release");
    }

    public boolean confirmStock(Long skuId, Integer quantity) {
        return operateStock(skuId, quantity, "confirm");
    }

    private boolean operateStock(Long skuId, Integer quantity, String operation) {
        try {
            ApiResponse response = restTemplate.postForObject(
                    productServiceUrl + "/api/stock/sku/" + skuId + "/" + operation,
                    Map.of("quantity", quantity),
                    ApiResponse.class
            );
            return response != null && Integer.valueOf(200).equals(response.getCode());
        } catch (RestClientException exception) {
            return false;
        }
    }
}
