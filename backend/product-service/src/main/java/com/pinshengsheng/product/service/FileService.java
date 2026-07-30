package com.pinshengsheng.product.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    // 上传商品图片，成功后返回图片访问地址
    String uploadImage(MultipartFile file);

    // 删除 MinIO 中的商品图片
    void deleteImage(String imageUrl);
}
