package cn.iocoder.yudao.module.digital.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommonMapper {

    public List<Map> getPptRecordDetail(Map<String,Object> params);


    public int updatePptRecordDetail(Map<String,Object> params);

    public int updatePptSlideContent(Map<String,Object> params);

    public int updatePptRecordUrl(Map<String,Object> params);

    public List<Map> getVideoExecLog (Map<String,Object> params);


    public int updateRecordVoiceHumanUrl(Map<String,Object> params);

    public int insertPptRecordDetail(Map<String,Object> params);

    public int insertVideoExecLog(Map<String,Object> params);

    public int updateRecordVideoImageUrl(Map<String,Object> params);

    public int updatemergeVideoUrl(Map<String,Object>  params);

    public int updatemergeAdjustVideoUrl(Map<String,Object> params);

    public int updateVideoUrl(Map<String,Object> params);

    public Map<String,Object> getAiDhHumanVideoDetail(Map<String,Object> params);

    public int updateMakeVideoStatus(Map<String,Object> params);

    public int updateVideoFirstFrame(Map<String,Object> params);

    public int updateRecordVoiceUrl(Map<String,Object> params);

    public Map<String,Object>  getFirstPptImage(Map<String,Object> params);

    public int  updateVoiceSplitHumanPath(Map<String,Object> params);

    public List<Map>  getvideoExecLogDetail(Map<String,Object> params);

    public int updateVideoExecLog(Map<String,Object> params);

    public List<HashMap> getAiImageAndVideoUrl();

    public int getExecHumanAddImageNum(Map<String,Object> params);

    public List<Map> getNotExecHumanAddImageNum(Map<String,Object> params);

    public int updatePptRecordUrlAndLength(Map<String,Object> params);

    public List<Map> getOrgChangeField();

    public List<Map> getCommonParam(Map<String,Object> params);

    public int deleteVideoExecLog(Map<String,Object> params);
}
