package cn.iocoder.yudao.module.digital.util;


import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;

/**
 * @program: ai_digital
 * @description:
 * @author: huangyx
 * @create: 2024-07-21 11:53
 **/

public class AbilityShareClient {
    private static Charset getDefaultCharset() {
        return Charset.forName("utf-8");
    }

    /**
     * get请求
     *
     * @param url
     * @param header
     * @return
     */
    public static String doGet(String url, Map<String, String> header) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).GET();
        buildHeader(header, builder);
        return execute(builder, getDefaultCharset());
    }

    /**
     * post请求
     *
     * @param url
     * @param header
     * @param body
     * @return
     */
    public static String doPost(String url, Map<String, String> header, String body) {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body, getDefaultCharset());
        Duration durationSeconds = Duration.ofSeconds(30);//秒
        HttpRequest.Builder builder = HttpRequest.newBuilder().timeout(durationSeconds).uri(URI.create(url)).POST(bodyPublisher);
        buildHeader(header, builder);
        return execute(builder, getDefaultCharset());
    }
    /**
     * post请求
     *
     * @param url
     * @param header
     * @param body
     * @return
     */
    public static String doPostPPt(String url, Map<String, String> header, String body) {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body, getDefaultCharset());
        Duration durationSeconds = Duration.ofSeconds(300);//秒
        HttpRequest.Builder builder = HttpRequest.newBuilder().timeout(durationSeconds).uri(URI.create(url)).POST(bodyPublisher);
        buildHeader(header, builder);
        return execute(builder, getDefaultCharset());
    }
    /**
     * post请求（长超时，ppt-master 生成是分钟级）
     *
     * @param url
     * @param header
     * @param body
     * @return
     */
    public static String doPostPPtLong(String url, Map<String, String> header, String body) {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body, getDefaultCharset());
        Duration durationSeconds = Duration.ofSeconds(900);//秒
        HttpRequest.Builder builder = HttpRequest.newBuilder().timeout(durationSeconds).uri(URI.create(url)).POST(bodyPublisher);
        buildHeader(header, builder);
        return execute(builder, getDefaultCharset());
    }
    /**
     * form表单
     *
     * @param url
     * @param header
     * @param pd
     * @return
     */
    public static String doPostForm(String url, Map<String, String> header, Map<String, String> pd, Charset charset) {
        if (charset == null) {
            charset = getDefaultCharset();
        }
        StringBuilder sb = new StringBuilder();
        if (pd != null) {
            pd.forEach((k, v) -> {
                sb.append(k);
                sb.append("=");
                sb.append(v);
                sb.append("&");
            });
        }
        sb.append("tmp=tmp");
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(sb.toString(), charset);
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).POST(bodyPublisher);
        buildHeader(header, builder);
        builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
        return execute(builder, charset);
    }


    private static void buildHeader(Map<String, String> header, HttpRequest.Builder builder) {
        if (header != null && header.size() > 0) {
            for (String key : header.keySet()) {
                builder.setHeader(key, header.get(key));
            }
        }
        builder.setHeader("Content-Type", "application/json");
    }

    /**
     * 针对某些https请求，忽略证书验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    private static SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        //使用 loadTrustMaterial() 方法实现一个信任策略，信任所有证书
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            // 信任所有
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        }).build();
        return sslContext;
    }

    private static String execute(HttpRequest.Builder builder, Charset charset) {
        HttpRequest request = builder.build();
        try {
            HttpClient client = HttpClient.newBuilder().sslContext(getSSLContext()).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(charset));
            String result = null;
            if (response.statusCode() == 200) {
                result = response.body();
            } else {

            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
