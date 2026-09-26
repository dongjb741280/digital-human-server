package cn.iocoder.yudao.module.digital.service.copywriting;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.digital.dal.AiCopyWritePptRecordDO;
import cn.iocoder.yudao.module.digital.vo.*;

public interface CopywritingManagementService {


    /**
     * 查询文案列表
     *
     * @param copywritingReqVO
     * @return 文案列表
     */
    PageResult<CopywritingRespVO> copywritingListQuery(CopywritingReqVO copywritingReqVO);

    /**
     * 删除文案
     *
     * @param copywritingUpdateReqVO
     */
    void copywritingDelete(CopywritingUpdateReqVO copywritingUpdateReqVO);

    /**
     * 获取文案详情
     * @param copywritingReqVO
     * @return
     */
    CopywritingRespVO copywritingGetOne(CopywritingReqVO copywritingReqVO);

    /**
     * 更新文案
     * @param copywritingUpdateReqVO
     */
    void copywritingUpdate(CopywritingUpdateReqVO copywritingUpdateReqVO);

    /**
     * 获取文案 ppt 列表
     * @param copywritingReqVO
     * @return
     */
    PageResult<CopyWritePptRecordRespVO> copywritingListPPT(CopywritingReqVO copywritingReqVO);


    /**
     * 删除文案
     *
     * @param copywritingPptReqVO
     */
    void copywritingPPTDelete(CopywritingPptReqVO copywritingPptReqVO);

    /**
     * 上传 ppt
     */
    void copywritingUploadPPT(CopywritingPptReqVO copywritingPptReqVO);

    /**
     * 下载ppt
     */
    AiCopyWritePptRecordDO copywritingDownloadPPT(CopywritingPptReqVO copywritingPptReqVO);

    /**
     * 创建文案（一键生成PPT时落一条文案主表记录）
     *
     * @return 文案 id
     */
    String copywritingCreate(String copywriteName, String copywriteTitle, String copywriteContent, String oprStaff);

    /**
     * 创建 PPT 记录（系统生成），并更新文案主表的 mainPptId
     *
     * @return PPT 记录 id
     */
    String copywritingCreatePPT(String copywriteId, String pptUrl, String recordDesc, String oprStaff);
}
