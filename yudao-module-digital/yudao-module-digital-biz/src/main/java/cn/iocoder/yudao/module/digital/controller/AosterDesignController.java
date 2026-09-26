package cn.iocoder.yudao.module.digital.controller;


import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.digital.entity.TbAiDhAosterDesign;
import cn.iocoder.yudao.module.digital.entity.TbAiDhFontTemplate;
import cn.iocoder.yudao.module.digital.service.AosterDesignService;
import cn.iocoder.yudao.module.digital.vo.AiDhHumanVideoSaveVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@RestController
@RequestMapping("/digital-api/system/aosterDesign")
@Slf4j
public class AosterDesignController {

    @Resource
    private AosterDesignService aosterDesignService;

    //加载字体模板
    @PostMapping("/getFontTemplate")
    public CommonResult<List<TbAiDhFontTemplate>> getFontTemplate(@RequestBody TbAiDhFontTemplate tbAiDhFontTemplate) {
        try {
            List<TbAiDhFontTemplate> fontTemplate = aosterDesignService.getFontTemplate();
            return success(fontTemplate);
        } catch (Exception e) {
            e.printStackTrace();
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }


    //插入海报模板
    @PostMapping("/insertAosterDesign")
    public CommonResult insertAosterDesign(@RequestBody TbAiDhAosterDesign tbAiDhAosterDesign) {
        try {
            String staffId = getLoginUserId() + "";
            tbAiDhAosterDesign.setCreator(staffId);
            int i = aosterDesignService.insertAosterDesign(tbAiDhAosterDesign);
            if(i > 0 ){
                return success(null);
            }
            return error(GlobalErrorCodeConstants.UNKNOWN);
        } catch (Exception e) {
            e.printStackTrace();
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }

    //加载海报模板
    @PostMapping("/getAosterDesignList")
    public CommonResult<List<TbAiDhAosterDesign>> getAosterDesignList(@RequestBody TbAiDhAosterDesign tbAiDhAosterDesign) {
        try {
            List<TbAiDhAosterDesign> data = aosterDesignService.getAosterDesignList(tbAiDhAosterDesign);
            return success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }

    //删除海报模板
    @PostMapping("/deleteAosterDesignList")
    public CommonResult deleteAosterDesignList(@RequestBody TbAiDhAosterDesign tbAiDhAosterDesign) {
        try {
            int i = aosterDesignService.deleteTbAiDhAosterDesign(tbAiDhAosterDesign);
            if(i > 0 ){
                return success(null);
            }
            return error(GlobalErrorCodeConstants.UNKNOWN);
        } catch (Exception e) {
            e.printStackTrace();
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }

    //修改海报模板
    @PostMapping("/updateAosterDesignList")
    public CommonResult  updateAosterDesignList(@RequestBody TbAiDhAosterDesign tbAiDhAosterDesign) {
        try {
            int i = aosterDesignService.updateAosterDesignList(tbAiDhAosterDesign);
            if(i > 0 ){
                return success(null);
            }
            return error(GlobalErrorCodeConstants.UNKNOWN);
        } catch (Exception e) {
            e.printStackTrace();
            return error(GlobalErrorCodeConstants.UNKNOWN);
        }
    }

}
