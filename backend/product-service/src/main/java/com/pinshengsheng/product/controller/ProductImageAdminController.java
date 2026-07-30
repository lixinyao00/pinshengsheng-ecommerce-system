package com.pinshengsheng.product.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.dto.ProductImageSaveRequest;
import com.pinshengsheng.product.entity.ProductImage;
import com.pinshengsheng.product.service.FileService;
import com.pinshengsheng.product.service.ProductImageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 商品轮播图管理接口，图片文件本身仍由 FileService 保存到 MinIO
@RestController
@RequestMapping("/api/admin/product")
public class ProductImageAdminController {

    private final ProductImageService productImageService;
    private final FileService fileService;

    public ProductImageAdminController(
            ProductImageService productImageService,
            FileService fileService) {
        this.productImageService = productImageService;
        this.fileService = fileService;
    }

    @GetMapping("/{productId}/images")
    public ApiResponse<List<ProductImage>> getProductImages(@PathVariable Long productId) {
        return ApiResponse.success(productImageService.getProductImages(productId));
    }

    @PostMapping("/{productId}/images")
    public ApiResponse<ProductImage> addProductImage(
            @PathVariable Long productId,
            @RequestBody ProductImageSaveRequest request) {
        ProductImage productImage = productImageService.addProductImage(productId, request);
        return productImage == null
                ? ApiResponse.fail(400, "商品不存在或图片地址为空")
                : ApiResponse.success(productImage);
    }

    @DeleteMapping("/image/{imageId}")
    public ApiResponse<Void> deleteProductImage(@PathVariable Long imageId) {
        ProductImage productImage = productImageService.getProductImage(imageId);
        if (productImage == null) {
            return ApiResponse.fail(404, "轮播图不存在");
        }

        try {
            fileService.deleteImage(productImage.getImageUrl());
            return productImageService.deleteProductImage(imageId)
                    ? ApiResponse.success(null)
                    : ApiResponse.fail(404, "轮播图不存在");
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(400, exception.getMessage());
        } catch (IllegalStateException exception) {
            return ApiResponse.fail(500, exception.getMessage());
        }
    }
}
