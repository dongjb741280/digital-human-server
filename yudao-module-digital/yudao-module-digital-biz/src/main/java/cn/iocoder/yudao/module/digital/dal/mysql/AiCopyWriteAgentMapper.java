package cn.iocoder.yudao.module.digital.dal.mysql;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.digital.dal.AiCopyWriteAgentDO;
import cn.iocoder.yudao.module.digital.vo.CopywritingReqVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiCopyWriteAgentMapper extends BaseMapperX<AiCopyWriteAgentDO> {

    /**
     * 通过 oprStaff 和 copywriteShare 查询 AiCopyWriteAgentDO 表，返回所有匹配的记录列表
     *
     * @param copywritingReqVO 字段值
     * @return 匹配的记录列表
     */
    default PageResult<AiCopyWriteAgentDO> selectByOprStaffAndCopywriteShare(CopywritingReqVO copywritingReqVO) {
        LambdaQueryWrapper<AiCopyWriteAgentDO> queryWrapper = new LambdaQueryWrapper<>();

        if (copywritingReqVO.getOprStaff() != null) {
            queryWrapper.eq(AiCopyWriteAgentDO::getOprStaff, copywritingReqVO.getOprStaff());
        }

        if (StringUtils.isNotBlank(copywritingReqVO.getCopywriteName())) {
            queryWrapper.like(AiCopyWriteAgentDO::getCopywriteName, copywritingReqVO.getCopywriteName().trim());
        }

        if (StringUtils.isNotBlank(copywritingReqVO.getCopywriteTitle())) {
            queryWrapper.like(AiCopyWriteAgentDO::getCopywriteTitle, copywritingReqVO.getCopywriteTitle().trim());
        }
        if(StringUtils.isNotBlank(copywritingReqVO.getCopywriterStatus())){
            queryWrapper.eq(AiCopyWriteAgentDO::getCopywriteStatus, copywritingReqVO.getCopywriterStatus());
        }

        queryWrapper.orderByDesc(AiCopyWriteAgentDO::getOprTime);
        return this.selectPage(copywritingReqVO, queryWrapper);
//        return this.selectPage(copywritingReqVO, new LambdaQueryWrapper<AiCopyWriteAgentDO>()
//                .eq(AiCopyWriteAgentDO::getOprStaff, copywritingReqVO.getOprStaff())
//                // 增加模糊查询，注意这里使用了 trim() 函数去除前后空格，防止空格影响查询结果
//                .like(StringUtils.isNotBlank(copywritingReqVO.getCopywriteName()),
//                        AiCopyWriteAgentDO::getCopywriteName,
//                        copywritingReqVO.getCopywriteName().trim())
//                .like(StringUtils.isNotBlank(copywritingReqVO.getCopywriteTitle()),
//                        AiCopyWriteAgentDO::getCopywriteTitle,
//                        copywritingReqVO.getCopywriteTitle().trim()));

    }

    default PageResult<AiCopyWriteAgentDO> selectByCopywriteShare(CopywritingReqVO copywritingReqVO) {

        LambdaQueryWrapper<AiCopyWriteAgentDO> queryWrapper = new LambdaQueryWrapper<>();

        if (copywritingReqVO.getPublicLibType() != null) {
            queryWrapper.eq(AiCopyWriteAgentDO::getCopywriteShare, copywritingReqVO.getPublicLibType());
        }

        if (StringUtils.isNotBlank(copywritingReqVO.getCopywriteName())) {
            queryWrapper.like(AiCopyWriteAgentDO::getCopywriteName, copywritingReqVO.getCopywriteName().trim());
        }

        if (StringUtils.isNotBlank(copywritingReqVO.getCopywriteTitle())) {
            queryWrapper.like(AiCopyWriteAgentDO::getCopywriteTitle, copywritingReqVO.getCopywriteTitle().trim());
        }
        queryWrapper.orderByDesc(AiCopyWriteAgentDO::getOprTime);
        return this.selectPage(copywritingReqVO, queryWrapper);
    }

    /**
     * 通过 id 和 oprStaff 删除文案
     * @param id
     * @param oprStaff
     */
    default void deleteByIdAndOprStaff(String id, String oprStaff) {
        this.delete(new LambdaQueryWrapper<AiCopyWriteAgentDO>()
                .eq(AiCopyWriteAgentDO::getId, id));
//                .eq(AiCopyWriteAgentDO::getOprStaff, oprStaff));
    }

}
