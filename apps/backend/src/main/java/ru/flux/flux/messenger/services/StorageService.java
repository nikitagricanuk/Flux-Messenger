package ru.flux.flux.messenger.services;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class StorageService {
    private final MinioClient minio;
    private final String bucket;
    private final String publicUrl;

    public StorageService(
            @Value("${storage.minio.endpoint}") String endpoint,
            @Value("${storage.minio.public-url}") String publicUrl,
            @Value("${storage.minio.access-key}") String accessKey,
            @Value("${storage.minio.secret-key}") String secretKey,
            @Value("${storage.minio.bucket}") String bucket) {
        this.publicUrl = publicUrl.stripTrailing().replaceAll("/+$", "");
        this.bucket = bucket;
        this.minio = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @PostConstruct
    private void ensureBucket() throws Exception {
        if (!minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            minio.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucket)
                    .config("""
                            {"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"AWS":["*"]},"Action":["s3:GetObject"],"Resource":["arn:aws:s3:::%s/*"]}]}
                            """.formatted(bucket))
                    .build());
        }
    }

    public String upload(String objectName, InputStream stream, long size, String contentType) throws Exception {
        minio.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(stream, size, -1)
                .contentType(contentType)
                .build());
        return publicUrl + "/" + bucket + "/" + objectName;
    }
}