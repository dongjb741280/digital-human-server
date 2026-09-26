package cn.iocoder.yudao.module.digital.service;

import java.util.List;
import java.util.Map;

public interface PptRecordDetailService {

    public List<Map> getPptRecordDetail(String mainPptId);


    public boolean updatePptRecordDetail(Map<String, Object> params);

    public List<Map> getPptRecordDetail(Map<String, Object> params);

    public boolean updatePptRecordUrlAndLength(Map<String, Object> params);

}
