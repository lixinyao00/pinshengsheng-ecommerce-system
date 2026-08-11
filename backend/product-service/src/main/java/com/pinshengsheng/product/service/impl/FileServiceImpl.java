package com.pinshengsheng.product.service.impl;

import com.pinshengsheng.product.service.FileService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private final MinioClient minioClient;
    private final String publicEndpoint;
    private final String bucketName;

    public FileServiceImpl(
            MinioClient minioClient,
            @Value("${minio.public-endpoint}") String publicEndpoint,
            @Value("${minio.bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.publicEndpoint = publicEndpoint;
        this.bucketName = bucketName;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        validateImage(file);

        try {
            ensureBucketExists();

            String objectName = createObjectName(file);

            // 将浏览器提交的文件流直接写入 MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return buildFileUrl(objectName);
        } catch (Exception exception) {
            throw new IllegalStateException("图片上传失败", exception);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        String objectName = extractObjectName(imageUrl);

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("图片删除失败", exception);
        }
    }

    // 只接收 5MB 以内的图片文件
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择图片文件");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("图片大小不能超过 5MB");
        }

        // 不只依赖请求头的 MIME 类型，直接读取内容确认它确实是图片
        try (InputStream inputStream = file.getInputStream()) {
            if (ImageIO.read(inputStream) == null) {
                throw new IllegalArgumentException("只能上传图片文件");
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("图片文件读取失败");
        }
    }

    // Bucket 类似项目专用的顶层文件夹，第一次上传时自动创建
    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
            );
        }

        // 商品图片允许浏览器匿名读取，但上传和删除仍只能由后端凭证完成
        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(buildReadOnlyPolicy())
                        .build()
        );
    }

    // 用日期目录和 UUID 避免不同图片重名
    private String createObjectName(MultipartFile file) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String suffix = StringUtils.hasText(extension)
                ? "." + extension.toLowerCase()
                : "";

        String datePath = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return "product/" + datePath + "/" + UUID.randomUUID() + suffix;
    }

    private String buildFileUrl(String objectName) {
        String baseUrl = publicEndpoint.endsWith("/")
                ? publicEndpoint.substring(0, publicEndpoint.length() - 1)
                : publicEndpoint;

        return baseUrl + "/" + bucketName + "/" + objectName;
    }

    // 只允许删除当前项目 Bucket 中的图片，避免接口被用于删除其他对象
    private String extractObjectName(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            throw new IllegalArgumentException("图片地址不能为空");
        }

        String prefix = buildFileUrl("");
        if (!imageUrl.startsWith(prefix)) {
            throw new IllegalArgumentException("图片地址不属于当前存储空间");
        }

        String objectName = imageUrl.substring(prefix.length());
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("图片地址无效");
        }

        return objectName;
    }

    // 仅开放对象读取权限，不能匿名上传、删除或列出文件
    private String buildReadOnlyPolicy() {
        return """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": "*",
                      "Action": "s3:GetObject",
                      "Resource": "arn:aws:s3:::%s/*"
                    }
                  ]
                }
                """.formatted(bucketName);
    }
}
