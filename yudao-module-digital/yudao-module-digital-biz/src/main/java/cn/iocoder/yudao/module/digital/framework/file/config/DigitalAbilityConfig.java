/*
 * 文 件 名:  AgentAbilityConfig
 * 版    权:
 * 描    述:  <描述>
 * 修 改 人:  Young
 * 修改时间:  2024/1/15
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package cn.iocoder.yudao.module.digital.framework.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @program: ai_digital
 * @description: 数字人
 * @author: huangyx
 * @create: 2024-07-13 15:10
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "digital-ability")
public class DigitalAbilityConfig {
    /**
     * 调用视频背景替换和抠图相关python接口地址
     */
    private String abilityUrl;

    /**
     * 视频背景替换和抠图相关python接口地址
     */
    private Map<String, String> changeViedoAndImageUrl;

    /**
     * 调用ppt相关得python接口地址
     */
    private String abigetpptUrl;

    /**
     * ppt生成图片的python接口地址
     */
    private String ppttoimage;

    /**
     * minio相关配置
     */
    private Map<String, String> minioConfig;

    private String minioEndpoint;

    private String minioBucket;

    private String minioAccessKey;

    private String minioAccessSecret;

}
