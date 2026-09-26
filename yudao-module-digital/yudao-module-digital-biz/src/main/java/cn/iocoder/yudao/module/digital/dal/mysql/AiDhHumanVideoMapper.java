package cn.iocoder.yudao.module.digital.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.digital.dal.AiDhHumanDO;
import cn.iocoder.yudao.module.digital.dal.AiDhHumanVideoDO;
import cn.iocoder.yudao.module.digital.entity.TbAiDhVoice;
import cn.iocoder.yudao.module.digital.vo.AiDhHumanQueryVO;
import cn.iocoder.yudao.module.digital.vo.AiDhHumanVideoQueryVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.poi.ss.formula.functions.T;

@Mapper
public interface AiDhHumanVideoMapper extends BaseMapper<AiDhHumanVideoDO>   {




}
