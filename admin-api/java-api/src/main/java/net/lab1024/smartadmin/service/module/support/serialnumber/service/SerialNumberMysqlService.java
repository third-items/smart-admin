package net.lab1024.smartadmin.service.module.support.serialnumber.service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.smartadmin.service.common.exception.BusinessException;
import net.lab1024.smartadmin.service.module.support.serialnumber.domain.SerialNumberEntity;
import net.lab1024.smartadmin.service.module.support.serialnumber.domain.SerialNumberGenerateResultBO;
import net.lab1024.smartadmin.service.module.support.serialnumber.domain.SerialNumberInfoBO;
import net.lab1024.smartadmin.service.module.support.serialnumber.domain.SerialNumberLastGenerateBO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zhuoda
 * @Date 2021-11-10
 */

@Slf4j
public class SerialNumberMysqlService extends SerialNumberBaseService {

    @Override
    @Transactional(rollbackFor = Throwable.class)
    List<String> generateSerialNumberList(SerialNumberInfoBO serialNumberInfo, int count) {
        // // 获取上次的生成结果
        SerialNumberEntity serialNumberEntity = serialNumberDao.selectForUpdate(serialNumberInfo.getSerialNumberId());
        if (serialNumberEntity == null) {
            throw new BusinessException("cannot found SerialNumberId 数据库不存在:" + serialNumberInfo.getSerialNumberId());
        }
        SerialNumberLastGenerateBO lastGenerateBO = SerialNumberLastGenerateBO
                .builder()
                .lastNumber(serialNumberEntity.getLastNumber())
                .lastTime(serialNumberEntity.getLastTime())
                .serialNumberId(serialNumberEntity.getSerialNumberId())
                .build();

        // 生成
        SerialNumberGenerateResultBO serialNumberGenerateResult = super.loopNumberList(lastGenerateBO, serialNumberInfo, count);

        // 将生成信息保存的内存和数据库
        lastGenerateBO.setLastNumber(serialNumberGenerateResult.getLastNumber());
        lastGenerateBO.setLastTime(serialNumberGenerateResult.getLastTime());
        serialNumberDao.updateLastNumberAndTime(serialNumberInfo.getSerialNumberId(),
                serialNumberGenerateResult.getLastNumber(),
                serialNumberGenerateResult.getLastTime());

        // 把生成过程保存到数据库里
        super.saveRecord(serialNumberGenerateResult);

        return formatNumberList(serialNumberGenerateResult, serialNumberInfo);
    }
}
