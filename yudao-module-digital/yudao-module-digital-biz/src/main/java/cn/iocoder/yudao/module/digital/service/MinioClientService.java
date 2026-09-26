package cn.iocoder.yudao.module.digital.service;

import cn.iocoder.yudao.module.digital.framework.file.config.DigitalAbilityConfig;
import groovy.util.logging.Slf4j;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
@Slf4j
public class MinioClientService {

    @Resource
    private DigitalAbilityConfig digitalAbilityConfig;

    private MinioClient client;

    private String endpoint;

    private String accessKey;

    private String secretKey;

    public void initMinioClient() throws Exception {

         endpoint = digitalAbilityConfig.getMinioEndpoint();
         accessKey = digitalAbilityConfig.getMinioAccessKey();
         secretKey = digitalAbilityConfig.getMinioAccessSecret();

/*        endpoint = "http://10.19.28.109:9001";
        accessKey = "hC90YAR5YQDxEXBCRzCE";
        secretKey = "qfS1iy2vGKar63fcqyxRCQ3mbh9gUdPUQvsPlDnD";*/

         client = MinioClient.builder()
                .endpoint(endpoint) // Endpoint URL
                .credentials(accessKey, secretKey) // 认证密钥
                .build();
    }

    public String minioUpload(byte[] content, String path, String type) throws Exception {

        String bucket = digitalAbilityConfig.getMinioBucket();
        // 拼接返回路径
        // 使用putObject上传文件。
        client.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(path)
                .stream(new ByteArrayInputStream(content), content.length, -1)
                .contentType(contentType(type))
                .build());



        return endpoint + "/" + bucket + "/" + path;
    }

    public String minioUploadStream(InputStream content, String path, String type) throws Exception {
        String bucket = digitalAbilityConfig.getMinioBucket();
        byte[] bytes = readAllBytes(content);
        client.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(path)
                .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                .contentType(contentType(type))
                .build());
        return endpoint + "/" + bucket + "/" + path;
    }

    private byte[] readAllBytes(InputStream in) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        return baos.toByteArray();
    }

    public byte[] minioDownload(String path) throws Exception {
        String bucket = digitalAbilityConfig.getMinioBucket();
        try (GetObjectResponse response = client.getObject(
                GetObjectArgs.builder().bucket(bucket).object(path).build())) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = response.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
            return baos.toByteArray();
        }
    }

    public void minioDelete(String path) throws Exception {
        String bucket = digitalAbilityConfig.getMinioBucket();
        client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(path).build());
    }

    private String contentType(String type) {
        if (type == null) {
            return "application/octet-stream";
        }
        switch (type.toLowerCase()) {
            case "mp4":
                return "video/mp4";
            case "wav":
                return "audio/wav";
            case "mp3":
                return "audio/mpeg";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 将 SFTP 绝对路径转换为 MinIO 相对对象键。
     * 例如 /home/puaiuc/sftpFile/xxx/image -> xxx/image
     */
    public static String toObjectKey(String path) {
        if (path == null) {
            return null;
        }
        String key = path;
        // 去掉完整 URL 前缀（http://host:port/bucket/）
        key = key.replaceFirst("^https?://[^/]+/([^/]+/)?", "");
        // 去掉 SFTP 路径前缀
        key = key.replaceFirst("^.*?(sftpFile/|aifs01/)", "");
        // 去掉桶名前缀 aidigital/
        key = key.replaceFirst("^aidigital/", "");
        key = key.replaceFirst("^/+", "");
        return key;
    }

    public static String extension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int i = fileName.lastIndexOf('.');
        return i < 0 ? null : fileName.substring(i + 1);
    }

}
