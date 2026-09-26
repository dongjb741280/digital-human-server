package cn.iocoder.yudao.module.digital.dal;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_ai_dh_video")
public class AiDhHumanVideoDO implements Serializable  {

    /**
     * ID
     */
    @TableId(value = "id")
    private String id;
    private String videoName;
    private String copywriteId;
    private String copywritePptId;
    private String humanId;
    private String voiceId;
    private String resolutionRatio;
    private String aspectRatio;
    private String characterPosition;
    private String pptPosition;
    private String captionsPosition;
    private String isCaptions;
    private String isHuman;
    private String isPpt;
    private String isBg;

    private String isAdjust;
    private String drivingType;
    private String voiceContent;
    private String firstFrame;
    private String videoUrl;
    private String videoStatus;
    private String videoSave;
    private String oprTime;
    private String oprStaff;
    private String frameName;
    private String videoFinalName;

}
