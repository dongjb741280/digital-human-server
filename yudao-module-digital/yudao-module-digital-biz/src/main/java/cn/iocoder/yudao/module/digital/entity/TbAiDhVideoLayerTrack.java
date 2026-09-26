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
 * ai数字人视频合成图层轨迹
 * </p>
 *
 * @author author
 * @since 2024-09-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_ai_dh_video_layer_track")
public class TbAiDhVideoLayerTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * 应用场景ID
     */
    private String sceneId;

    /**
     * 应用场景：1-数字人视频合成，2-数字人互动
     */
    private String sceneType;

    /**
     * 图层名称
     */
    private String layerName;

    /**
     * 图层类型：1-数字人，2-前景，3-PPT，4-对话Agent，5-互动画布容器
     */
    private String layerType;

    /**
     * 图层类型对应的id主键
     */
    private String layerTypeId;

    /**
     * 是否锁定：1-是，2-否
     */
    private String layerLock;

    /**
     * 图层顺序：1-N，数字大，图层越
     */
    private Integer layerOrder;

    /**
     * 图层原始宽度
     */
    private String width;

    /**
     * 图层原始高度
     */
    private String height;

    /**
     * 图层x轴坐标
     */
    private String boxX;

    /**
     * 图层y轴坐标
     */
    private String boxY;

    /**
     * 图层宽度
     */
    private String boxW;

    /**
     * 图层高度
     */
    private String boxH;

    /**
     * 是否显示：1-是，2-否
     */
    private String isShow;

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
