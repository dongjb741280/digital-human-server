package cn.iocoder.yudao.module.digital.dal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fhs.core.trans.vo.TransPojo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * @program: ai_digital
 * @description: 数字人表用户 DO
 * @author: huangyx
 * @create: 2024-07-13 14:38
 **/
@TableName(value = "tb_ai_dh_human", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDhHumanDO implements Serializable, TransPojo {

    /**
     * ID
     */
    @TableId
    @TableField(value = "id")
    private String id;
    /**
     * 形象名称
     */
    @TableField(value = "human_name")
    private String humanName;

    /**
     * 文件名称
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 图像名称
     */
    @TableField(value = "image_name")
    private String imageName;
    /**
     * 原始视频存放地址
     *
     */
    @TableField(value = "human_org_url")
    private String humanOrgUrl;
    /**
     * 形象是否分享加入公共库 1：是，0：否
     */
    @TableField(value = "human_share")
    private String humanShare;
    /**
     * 背景替换 0：保留 1：去除背景
     */
    @TableField(value = "human_bg")
    private String humanBg;
    /**
     * 封面，可以去视频第一帧
     */
    @TableField(value = "first_frame")
    private String firstFrame;

    /**
     * 新生成视频存放路径
     */
    @TableField(value = "human_generate_url")
    private String humanGenerateUrl;
    /**
     * 制作结果 1：未执行 2：执行中 3：执行失败 4：执行成功
     */
    @TableField(value = "human_status")
    private String humanStatus;

    /**
     * 操作时间
     */
    @TableField(value = "opr_time")
    private LocalDateTime oprTime;

    /**
     * 操作工号
     */
    @TableField(value = "opr_staff")
    private String oprStaff;


}
