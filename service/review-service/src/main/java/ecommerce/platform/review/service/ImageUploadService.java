package ecommerce.platform.review.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class ImageUploadService {

    public String upload(MultipartFile file) {
        validateFile(file);

        // TODO: S3 연동
        // String key = "reviews/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        // s3Client.putObject(PutObjectRequest.builder()
        //         .bucket(bucketName)
        //         .key(key)
        //         .build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        // return "https://" + bucketName + ".s3.amazonaws.com/" + key;

        String randomUrl = "https://mock-s3.amazonaws.com/reviews/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        return randomUrl;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }
}