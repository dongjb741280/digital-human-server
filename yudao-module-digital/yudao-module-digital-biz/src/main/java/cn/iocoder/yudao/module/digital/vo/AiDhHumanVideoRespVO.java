package cn.iocoder.yudao.module.digital.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiDhHumanVideoRespVO {


    private String id;
    private String videoName ;
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
    private String drivingType;
    private String voiceContent;
    private String videoStatus;
    private String videoSave;
    private String oprTime;
    private String  FirstFrame;
    private String frameName;
    private String videoFinalName;

    private String videoUrl;
}
