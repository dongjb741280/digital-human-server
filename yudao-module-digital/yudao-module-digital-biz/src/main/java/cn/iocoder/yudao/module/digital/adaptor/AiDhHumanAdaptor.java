package cn.iocoder.yudao.module.digital.adaptor;

import cn.iocoder.yudao.module.digital.dal.AiDhHumanDO;
import cn.iocoder.yudao.module.digital.vo.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @program: ai_digital
 * @description: 转换
 * @author: huangyx
 * @create: 2024-07-13 15:13
 **/

public class AiDhHumanAdaptor {



    public static BiFunction<AiDhHumanSaveVO, Map<String,String>, AiDhHumanDO> saveCovDo =(info, paramMap )->
            Optional.ofNullable(info).map(it -> new AiDhHumanDO()
                            .setId(paramMap.get("id"))
                            .setHumanName(info.getHumanName())
                            .setFileName(paramMap.get("fileName"))
                            .setImageName(paramMap.get("imageName"))
                            .setHumanOrgUrl(paramMap.get("sftpUploadViedoPath"))
                            .setFirstFrame(paramMap.get("sftpUploadImegaPath"))
                            .setHumanStatus("1")
                            .setOprTime(LocalDateTime.now())
                            .setOprStaff(paramMap.get("staffId")))
                    .orElse(null);


    public static BiFunction<String, AiDhHumanReqVO,AiDhHumanQueryVO> queryCovDoForAll =(staffid, reqVO )->
            Optional.ofNullable(reqVO).map(it -> new AiDhHumanQueryVO()
                            .setOprStaff(staffid)
                            .setStatus(reqVO.getStatus())
                            .setPageNo(reqVO.getPageNo())
                            .setPageSize(reqVO.getPageSize()))
                    .orElse(null);
    public static Function< AiDhHumanReqVO,AiDhHumanQueryVO> queryCovDoForHumanShare = reqVO ->
            Optional.ofNullable(reqVO).map(it -> new AiDhHumanQueryVO()
                               .setHumanShare(reqVO.getQueryType())
                            .setStatus(reqVO.getStatus())
                            .setPageNo(reqVO.getPageNo())
                            .setPageSize(reqVO.getPageSize()))
                    .orElse(null);
    public static Function<AiDhHumanDO, AiDhHumanRespVO> doCovRespVO = infoDo ->
            Optional.ofNullable(infoDo).map(it -> new AiDhHumanRespVO()
                            .setId(infoDo.getId())
                            .setHumanName(infoDo.getHumanName())
                            .setHumanImageUrl(infoDo.getFileName()))
                    .orElse(null);

    public static Function<AiDhHumanDO, AiDhHumanRespVO> doCovAllRespVO = infoDo ->
            Optional.ofNullable(infoDo).map(it -> new AiDhHumanRespVO()
                            .setId(infoDo.getId())
                            .setHumanShare(infoDo.getHumanShare())
                            .setFileName(infoDo.getFileName())
                            .setHumanBg(infoDo.getHumanBg())
                            .setHumanGenerateUrl(infoDo.getHumanGenerateUrl())
                            .setHumanImageUrl(infoDo.getFirstFrame())
                            .setHumanImageName(infoDo.getImageName())
                            .setHumanOrgUrl(infoDo.getHumanOrgUrl())
                            .setHumanViedoUrl("4".equals(infoDo.getHumanStatus()) ? (infoDo.getHumanBg().equals("0") ? infoDo.getHumanOrgUrl() : infoDo.getHumanGenerateUrl()) : infoDo.getHumanOrgUrl() )
                            .setHumanName(infoDo.getHumanName()))
                    .orElse(null);


    public static BiFunction<AiDhHumanUpdateVO, Map<String,String>, AiDhHumanDO> updateCovDo =(info, paramMap )->
            Optional.ofNullable(info).map(it -> new AiDhHumanDO()
                            /*.setId(paramMap.get("id"))*/
                            .setHumanName(info.getHumanName())
                            /*.setFileName(paramMap.get("fileName"))
                            .setHumanOrgUrl(paramMap.get("sftpUploadViedoPath"))*/
                            .setHumanShare(info.getHumanShare())
                            .setOprTime(LocalDateTime.now())
                            .setOprStaff(paramMap.get("staffId")))
                    .orElse(null);

    public static Function<String, AiDhHumanDO> updateStringCovDo =humanStatus->
            Optional.ofNullable(humanStatus).map(it -> new AiDhHumanDO()
                            .setHumanStatus(humanStatus)
                            .setOprTime(LocalDateTime.now()))
                    .orElse(null);


    public static BiFunction<AiDhHumanSaveVO, Map<String,String>, AiDhHumanDO> updatevo =(info, paramMap )->
            Optional.ofNullable(info).map(it -> new AiDhHumanDO()
                            .setId(info.getId())
                            .setHumanName(info.getHumanName())
                            .setHumanShare(info.getHumanShare())
                            .setHumanBg(info.getHumanBg())
                            .setHumanStatus("2")
                            .setHumanGenerateUrl(info.getHumanBg().equals("0") ? null : paramMap.get("sftpUploadGeneratePath"))
                            .setOprTime(LocalDateTime.now()))
                    .orElse(null);
}
