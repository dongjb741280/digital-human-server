package cn.iocoder.yudao.module.digital.service;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.digital.vo.*;


/**
 * @program: ai_digital
 * @description: 数字人
 * @author: huangyx
 * @create: 2024-07-13 14:38
 **/
public interface AiDhHumanService {

    /**
     * 创建数字人形象
     *
     * @param aiDhHumanSaveVO 用户信息
     */
    String createAiDhHuman(AiDhHumanSaveVO aiDhHumanSaveVO) throws Exception;

    AiDhHumanRespVO uploadViedo(AiDhHumanSaveVO aiDhHumanSaveVO, Long userId) throws Exception;

    /**
     * 获得数字人形象分页列表
     *
     * @param reqVO 分页条件
     * @return 分页列表
     */
    PageResult<AiDhHumanRespVO> getAiDhHumanPage(AiDhHumanReqVO reqVO, Long userId);


    String createAiDhHumanTest() throws Exception;


    AiDhHumanRespVO selectById(String id);

    void deleteAiDhHuman(String id);

    void updateAiDhHuman(AiDhHumanUpdateVO reqVO, Long userId);

    void callBackAiDhHuman(CallBackVo callBackVo);


}
