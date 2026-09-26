package cn.iocoder.yudao.module.digital.service.impl;

import cn.iocoder.yudao.module.digital.dal.SftpConfigDO;
import cn.iocoder.yudao.module.digital.dal.mysql.CommonMapper;
import cn.iocoder.yudao.module.digital.service.AiDhPptService;
import cn.iocoder.yudao.module.digital.service.PptRecordDetailService;
import cn.iocoder.yudao.module.digital.util.EncryptUtil;
import cn.iocoder.yudao.module.digital.util.SFtpOper;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.ChannelSftp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


@Service
@Slf4j
public class PptRecordDetailServiceImpl implements PptRecordDetailService {

    @Resource
    private CommonMapper commonMapper;

    @Resource
    private AiDhPptService aiDhPptService;

    @Value("${digital-ability.minioEndpoint:''}")
    private String minioEndpoint;

    @Value("${digital-ability.minioBucket:''}")
    private String minioBucket;

    @Override
    public List<Map> getPptRecordDetail(String mainPptId){
        Map<String,Object> params = new HashMap<>();
        params.put("mainPptId",mainPptId);
        List<Map> pptRecordDetailList = commonMapper.getPptRecordDetail(params);

        for(int i=0;i<pptRecordDetailList.size();i++) {
            Map<String, Object> data = pptRecordDetailList.get(i);
            String pptVoiceUrl = data.get("ppt_voice_url") + "";
            if(!"null".equals(pptVoiceUrl) && !"".equals(pptVoiceUrl)){
                pptVoiceUrl = minioEndpoint+"/"+minioBucket+"/" + pptVoiceUrl;
                data.put("ppt_voice_url",pptVoiceUrl);
            }
        }
        return pptRecordDetailList;
    }

    @Override
    public List<Map> getPptRecordDetail(Map<String, Object> params)
    {

        Map<String,Object> paramsReq = new HashMap<>();
        if(params.containsKey("pptNum")){
            paramsReq.put("pptNum",params.get("pptNum"));
        }
        if(params.containsKey("mainPptId")){
            paramsReq.put("mainPptId",params.get("mainPptId"));
        }
        log.info("params:"+paramsReq);
        List<Map> pptRecordDetailList = commonMapper.getPptRecordDetail(paramsReq);

        return pptRecordDetailList;

    }

    @Override
    public boolean updatePptRecordUrlAndLength(Map<String, Object> params) {
        int i = commonMapper.updatePptRecordUrlAndLength(params);
        if(i > 0 ){
            return true;
        }
        return false;
    }


    @Override
    public boolean updatePptRecordDetail(Map<String, Object> params){


        int upInt = commonMapper.updatePptRecordDetail(params);
        if(upInt>0){

            // 更新ppt_image_url
            JSONObject jsonObject = new JSONObject(params);
            try {
                aiDhPptService.updateppt(jsonObject);
            }catch (Exception e){
                log.error("updatePptRecordDetail error",e);
            }

            return true;
        }
        return false;

    }


    private String sftpCreateDirs(SftpConfigDO sftpConfigDO, String directory) {
        // MinIO 无目录概念，无需创建目录
        return null;
    }



}
