package cn.iocoder.yudao.module.digital.service.impl;

import cn.iocoder.yudao.module.digital.dal.SftpConfigDO;
import cn.iocoder.yudao.module.digital.dal.mysql.SftpConfigMapper;
import cn.iocoder.yudao.module.digital.service.SftpConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @program: ai_digital
 * @description: ftp配置信息
 * @author: huangyx
 * @create: 2024-07-13 16:27
 **/
@Service("sftpConfigService")
@Slf4j
public class SftpConfigServiceImpl implements SftpConfigService {

    @Resource
    private SftpConfigMapper sftpConfigMapper;

    @Override
    public SftpConfigDO selectBySftpSystem(SftpConfigDO sftpConfigDO) {
        return sftpConfigMapper.selectOne(SftpConfigDO::getSftpSystem,sftpConfigDO.getSftpSystem());
    }
}
