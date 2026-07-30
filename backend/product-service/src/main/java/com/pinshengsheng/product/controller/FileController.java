package com.pinshengsheng.product.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // 前端使用 form-data 的 file 字段提交图片
    @PostMapping("/image")
    public ApiResponse<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileService.uploadImage(file);

            Map<String, String> data = new HashMap<>();
            data.put("url", imageUrl);

            return ApiResponse.success(data);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(400, exception.getMessage());
        } catch (IllegalStateException exception) {
            return ApiResponse.fail(500, exception.getMessage());
        }
    }
}