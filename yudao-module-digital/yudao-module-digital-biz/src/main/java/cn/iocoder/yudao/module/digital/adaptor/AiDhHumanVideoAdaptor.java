package cn.iocoder.yudao.module.digital.adaptor;

import cn.iocoder.yudao.module.digital.dal.AiDhHumanDO;
import cn.iocoder.yudao.module.digital.dal.AiDhHumanVideoDO;
import cn.iocoder.yudao.module.digital.vo.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AiDhHumanVideoAdaptor {

    public static BiFunction<AiDhHumanVideoSaveVO, Map<String,String>, AiDhHumanVideoDO> saveCovDo =(info, paramMap )->
            Optional.ofNullable(info).map(it -> new AiDhHumanVideoDO()
                            .setId(paramMap.get("id"))
                            .setVideoName(info.getVideoName())
                            .setCopywriteId(info.getCopywriteId())
                            .setCopywritePptId( info.getCopywritePptId())
                            .setHumanId( info.getHumanId())
                            .setVoiceId( info.getVoiceId())
                            .setResolutionRatio( info.getResolutionRatio())
                            .setAspectRatio( info.getAspectRatio())
                            .setCharacterPosition( info.getCharacterPosition())
                            .setPptPosition( info.getPptPosition())
                            .setCaptionsPosition( info.getCaptionsPosition())
                            .setIsCaptions( info.getIsCaptions())
                            .setIsHuman( info.getIsHuman())
                            .setIsPpt( info.getIsPpt())
                            .setIsBg( info.getIsBg())
                            .setDrivingType( info.getDrivingType())
                            .setVoiceContent( info.getVoiceContent())
                            .setVideoUrl( info.getVideoUrl())
                            .setVideoStatus(paramMap.get("videoStatus"))
                            .setVideoSave( info.getVideoSave())
                            .setFirstFrame( info.getFirstFrame())
                            .setOprTime(paramMap.get("optTime"))
                            .setOprStaff(paramMap.get("staffId")))
                    .orElse(null);

    public static Function<AiDhHumanVideoReqVO,AiDhHumanVideoQueryVO> queryCovDoForAll =(reqVO)->
            Optional.ofNullable(reqVO).map(it -> new AiDhHumanVideoQueryVO()
                            .setOprStaff(reqVO.getStaffId())
                            .setPageNo(reqVO.getPageNo())
                            .setPageSize(reqVO.getPageSize()))
                    .orElse(null);


    public static Function<AiDhHumanVideoDO, AiDhHumanVideoRespVO> doCovAllRespVO = info ->
            Optional.ofNullable(info).map(it ->
                    new AiDhHumanVideoRespVO()
                            .setId(info.getId())
                            .setVideoName(info.getVideoName())
                            .setCopywriteId(info.getCopywriteId())
                            .setCopywritePptId( info.getCopywritePptId())
                            .setHumanId( info.getHumanId())
                            .setVoiceId( info.getVoiceId())
                            .setResolutionRatio( info.getResolutionRatio())
                            .setAspectRatio( info.getResolutionRatio())
                            .setCharacterPosition( info.getCharacterPosition())
                            .setPptPosition( info.getPptPosition())
                            .setCaptionsPosition( info.getCaptionsPosition())
                            .setIsCaptions( info.getIsCaptions())
                            .setIsHuman( info.getIsHuman())
                            .setIsPpt( info.getIsPpt())
                            .setIsBg( info.getIsBg())
                            .setDrivingType( info.getDrivingType())
                            .setVoiceContent( info.getVoiceContent())
                            .setVideoStatus( info.getVideoStatus())
                            .setVideoSave( info.getVideoSave())
                            .setFirstFrame( info.getFirstFrame())
                            .setOprTime(String.valueOf(LocalDateTime.now()).replace("T"," ")))
                    .orElse(null);


    public static Function<AiDhHumanVideoSaveVO,  AiDhHumanVideoDO> updateCovDo =(info)->
            Optional.ofNullable(info).map(it -> new AiDhHumanVideoDO()
                            .setVideoName(info.getVideoName())
                            )
                    .orElse(null);

}
