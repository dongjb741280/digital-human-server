package cn.iocoder.yudao.module.digital.service;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.digital.dal.AiDhHumanVideoDO;
import cn.iocoder.yudao.module.digital.vo.*;

import java.util.Map;


public interface AiDhHumanVideoService {

    /**
     * 保存数字人视频
     */
    public Map<String,Object> saveAiDhHumanVideo(AiDhHumanVideoSaveVO aiDhHumanVideoSaveVO)throws Exception;

    public int deleteAiDhHumanVideo(Map<String,Object> params);

    Map<String,Object>  getAiDhHumanVideoPage(AiDhHumanVideoReqVO pageReqVO);

    AiDhHumanVideoSaveVO selectById(String id);

    public int updateAiDhHumanVideo(AiDhHumanVideoSaveVO reqVO);

    public int updateRecordVoiceHuman(Map<String,Object> params);

    public int  updateRecordVideoImage(Map<String,Object> params);

    public Map<String,Object>  getFirstPptImageByCopywriteId(Map<String,Object> params);

    public void makePptVideoStart(String videoId,String batchNum,String modelId);


    public void updateMakeVideoStatus (String videoId,String videoStatus);
}
