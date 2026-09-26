package cn.iocoder.yudao.module.digital.service.impl;

import cn.iocoder.yudao.module.digital.dal.AiInteractAgentDo;
import cn.iocoder.yudao.module.digital.dal.CommparaDo;
import cn.iocoder.yudao.module.digital.dal.mysql.AiAgentMapper;
import cn.iocoder.yudao.module.digital.dal.mysql.TbSCommparaMapper;
import cn.iocoder.yudao.module.digital.service.AiCommparaService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("aiCommparaService")
public class AiCommparaServiceImpl implements AiCommparaService {
    @Resource
    private TbSCommparaMapper commparaMapper;

    @Override
    public List<CommparaDo> getPptRecordDetail(String paraCode) {
        QueryWrapper<CommparaDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("para_code",paraCode).eq("remove_tag","1");
        List<CommparaDo> list =  commparaMapper.selectList(queryWrapper);
        return list;
    }
}
