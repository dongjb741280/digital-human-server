package cn.iocoder.yudao.module.digital.dal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class AiDhVideoLayerTrackRspDO {


    private int id;
    private String sceneId;

    private String sceneType;

    private String layerName;

    private String layerType;

    private String layerTypeId;

    private String layerLock;

    private Integer layerOrder;

    private String width;

    private String height;

    private String boxx;

    private String boxy;

    private String boxw;

    private String boxh;

    private String isShow;

    private String transparency;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;

    private boolean deleted;

    private Long tenantId;

    private String src;

}
