package cn.iocoder.yudao.module.digital.service;

import cn.iocoder.yudao.module.digital.dal.SftpConfigDO;
import cn.iocoder.yudao.module.digital.vo.AiDhHumanSaveVO;

public interface SftpConfigService {

    SftpConfigDO selectBySftpSystem(SftpConfigDO sftpConfigDO);
}
