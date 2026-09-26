package cn.iocoder.yudao.module.digital.service;

import cn.iocoder.yudao.module.digital.dal.CommparaDo;

import java.util.List;
import java.util.Map;

public interface AiCommparaService {
    List<CommparaDo> getPptRecordDetail(String paraCode);
}
