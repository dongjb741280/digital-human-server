package cn.iocoder.yudao.module.digital.service;

import com.alibaba.fastjson.JSONObject;


/**
 * Project:AiDhPptService
 * Author:lilj
 * Date:2024/7/22
 * Description:
 */
public interface AiDhPptService {

    JSONObject getppt() throws Exception;

    public JSONObject updateppt(JSONObject jsonObject) throws Exception;

    JSONObject generateBody(JSONObject jsonObject) throws Exception;

    JSONObject generatePpt(JSONObject jsonObject) throws Exception;

    JSONObject generatePptMaster(JSONObject jsonObject) throws Exception;

    JSONObject submitPptMaster(JSONObject jsonObject) throws Exception;

    JSONObject getPptMasterStatus(String jobId) throws Exception;

    JSONObject savePptEdit(JSONObject jsonObject) throws Exception;

    JSONObject regeneratePpt(JSONObject jsonObject) throws Exception;

    JSONObject generateOutline(JSONObject jsonObject) throws Exception;

    JSONObject pilgrimage(JSONObject jsonObject) throws Exception;

    public JSONObject updatePptNote(JSONObject jsonObject) throws Exception;
}
