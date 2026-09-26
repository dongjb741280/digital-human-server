package cn.iocoder.yudao.module.digital.dal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_ai_dh_video_layer_track")
public class  AiDhVideoLayerTrackDO {

    @TableId(value = "id")
    @TableField(value = "id")
    private int id;
    @TableField(value = "scene_id")
    private String sceneId;

    @TableField(value = "scene_type")
    private String sceneType;

    @TableField(value = "layer_name")
    private String layerName;

    @TableField(value = "layer_type")
    private String layerType;

    @TableField(value = "layer_type_id")
    private String layerTypeId;

    @TableField(value = "layer_lock")
    private String layerLock;

    @TableField(value = "layer_order")
    private Integer layerOrder;

    @TableField(value = "width")
    private String width;

    @TableField(value = "height")
    private String height;

    @TableField(value = "box_x")
    private String boxx;

    @TableField(value = "box_y")
    private String boxy;

    @TableField(value = "box_w")
    private String boxw;

    @TableField(value = "box_h")
    private String boxh;

    @TableField(value = "is_show")
    private String isShow;

    @TableField(value = "transparency")
    private String transparency;

    @TableField(value = "creator")
    private String creator;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "updater")
    private String updater;

    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(value = "deleted")
    private boolean deleted;

    @TableField(value = "tenant_id")
    private Long tenantId;

}
