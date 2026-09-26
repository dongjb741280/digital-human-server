//package cn.iocoder.yudao.module.digital;
//
//import cn.iocoder.yudao.framework.common.pojo.PageResult;
//import cn.iocoder.yudao.module.digital.dal.mysql.AiDhHumanMapper;
//import cn.iocoder.yudao.module.digital.service.AiDhHumanService;
//import cn.iocoder.yudao.module.digital.vo.AiDhHumanReqVO;
//import cn.iocoder.yudao.module.digital.vo.AiDhHumanRespVO;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import javax.annotation.Resource;
//
///**
// * @program: ai_digital
// * @description:
// * @author: huangyx
// * @create: 2024-07-13 18:51
// **/
//@SpringBootTest
//@Slf4j
//public class AiDhHumanServiceTest {
//    @Resource
//    private AiDhHumanService aiDhHumanService;
//
//    @Resource
//    private AiDhHumanMapper aiDhHumanMapper;
//
//    @Test
//    public void testCreateAiDhHuman(){
//        /*AiDhHumanSaveVO aiDhHumanSaveVO = new AiDhHumanSaveVO();
//        aiDhHumanSaveVO.setId("1722064446228663");
//        aiDhHumanSaveVO.setHumanName("数字人制作_1121");
//        aiDhHumanSaveVO.setHumanBg("1");
//        aiDhHumanSaveVO.setHumanShare("1");
//        try {
//            aiDhHumanService.createAiDhHuman(aiDhHumanSaveVO);
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }*/
//        AiDhHumanReqVO aiDhHumanReqVO = new AiDhHumanReqVO();
//        aiDhHumanReqVO.setPageNo(1);
//        aiDhHumanReqVO.setPageSize(10);
//        aiDhHumanReqVO.setQueryType("0");
//        PageResult<AiDhHumanRespVO> a = aiDhHumanService.getAiDhHumanPage(aiDhHumanReqVO,1L);
//
//    }
//
//}
