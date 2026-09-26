package cn.iocoder.yudao.module.digital.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.digital.dal.AiDhVideoLayerTrackDO;
import cn.iocoder.yudao.module.digital.dal.AiDhVideoLayerTrackRspDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "数字人视频管理 - 数字人视频保存")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AiDhHumanVideoSaveVO extends PageParam {

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

    private List<AiDhVideoLayerTrackDO> layerInfo;

    private List<AiDhVideoLayerTrackRspDO> layerRspInfo;



    @Schema(description = "视频文件", example = "yudao")
    private MultipartFile file;


}
