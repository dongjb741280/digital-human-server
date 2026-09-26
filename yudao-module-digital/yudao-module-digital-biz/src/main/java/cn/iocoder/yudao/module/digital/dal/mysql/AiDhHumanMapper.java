package cn.iocoder.yudao.module.digital.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.digital.dal.AiDhHumanDO;
import cn.iocoder.yudao.module.digital.vo.AiDhHumanQueryVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiDhHumanMapper extends BaseMapperX<AiDhHumanDO> {


    /** 
    * @Description: 按照id查询
    * @Param: [id]
    * @return: cn.iocoder.yudao.module.digital.dal.AiDhHumanDO
    * @Author: huangyx
    * @Date: 2024/7/13
    */
    default AiDhHumanDO selectById(String id) {
        return selectOne("id", id);
    }


    /** 
    * @Description: 数字人信息查询
    * @Param: [reqVO]
    * @return: cn.iocoder.yudao.framework.common.pojo.PageResult<cn.iocoder.yudao.module.digital.dal.AiDhHumanDO>
    * @Author: huangyx
    * @Date: 2024/7/13
    */
    default PageResult<AiDhHumanDO> selectPage(AiDhHumanQueryVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiDhHumanDO>()
                .eqIfPresent(AiDhHumanDO::getOprStaff, reqVO.getOprStaff())
                .eqIfPresent(AiDhHumanDO::getHumanShare, reqVO.getHumanShare())
                .inIfPresent(AiDhHumanDO::getHumanStatus, reqVO.getStatus() != null ? "4" : "2","3","4")
                .orderByDesc(AiDhHumanDO::getOprTime));
    }
}
