package cn.iocoder.yudao.module.digital.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * ai数字人互动配置表
 * </p>
 *
 * @author author
 * @since 2024-09-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_ai_interact_human")
public class TbAiInteractHuman implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * 对话互动名称
     */
    private String interactName;

    /**
     * 数字人ID
     */
    private String humanId;

    /**
     * 音色ID
     */
    private String voiceId;

    /**
     * 角色ID
     */
    private String agentId;

    /**
     * 背景图片ID
     */
    private String bgId;

    /**
     * 分辨率：对应码表id
     */
    private String resolutionRatio;

    /**
     * 画面比例：对应码表id
     */
    private String aspectRatio;

    /**
     * 发布状态：0-未发布、1-发布中、2-已发布、3-发布失败
     */
    private String releaseState;

    /**
     * 发布详情记录
     */
    private String releaseInfo;

    /**
     * 发布url
     */
    private String releaseUrl;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新者
     */
    private String updater;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 租户编号
     */
    private Long tenantId;


}
