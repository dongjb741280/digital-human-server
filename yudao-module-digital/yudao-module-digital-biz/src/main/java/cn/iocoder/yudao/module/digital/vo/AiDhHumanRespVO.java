package cn.iocoder.yudao.module.digital.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "数字人形象管理 - 数字人形象")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiDhHumanRespVO  {

    /**
     * ID
     */
    private String id;
    /**
     * 形象名称
     */
    private String humanName;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 图像存放地址
     *
     */
    private String humanImageUrl;

    /**
     * 图像名字
     *
     */
    private String humanImageName;


    /**
     * 视频播放地址
     *
     */
    private String humanViedoUrl;

    /**
     * 形象是否分享加入公共库 1：是，0：否
     */
    private String humanShare;

    /**
     * 背景替换 0：保留 1：去除背景
     */
    private String humanBg;

    /**
     * 原始视频存放地址
     *
     */
    private String humanOrgUrl;

    /**
     * 新生成视频存放路径
     */
    private String humanGenerateUrl;


    /**
     * 状态
     */
    private String humanStatus;
}
