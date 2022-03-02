package net.lab1024.smartadmin.service.module.support.serialnumber.service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.smartadmin.service.common.exception.BusinessException;
import net.lab1024.smartadmin.service.constant.RedisKeyConst;
import net.lab1024.smartadmin.service.module.support.redis.RedisService;
import net.lab1024.smartadmin.service.module.support.serialnumber.domain.SerialNumberEntity;
import net.lab1024.smartadmin.service.module.support.serialnumber.domain.SerialNumberGenerateResultBO;
import net.lab1024.smartadmin.service.module.support.serialnumber.domain.SerialNumberInfoBO;
import net.lab1024.smartadmin.service.module.support.serialnumber.domain.SerialNumberLastGenerateBO;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author zhuoda
 * @Date 2021-11-10
 */

@Slf4j
public class SerialNumberRedisService extends SerialNumberBaseService {

    private static final int MAX_GET_LOCK_COUNT = 5;

    private static final long SLEEP_MILLISECONDS = 200L;

    @Autowired
    private RedisService redisService;

    @PostConstruct
    void initLastGenerate() {
        List<SerialNumberEntity> serialNumberEntityList = serialNumberDao.selectList(null);
        if (serialNumberEntityList == null) {
            return;
        }

        //删除之前的
        redisService.delete(RedisKeyConst.Support.SERIAL_NUMBER_LAST_INFO);

        for (SerialNumberEntity serialNumberEntity : serialNumberEntityList) {
            SerialNumberLastGenerateBO lastGenerateBO = SerialNumberLastGenerateBO
                    .builder()
                    .serialNumberId(serialNumberEntity.getSerialNumberId())
                    .lastNumber(serialNumberEntity.getLastNumber())
                    .lastTime(serialNumberEntity.getLastTime())
                    .build();

            redisService.mset(RedisKeyConst.Support.SERIAL_NUMBER_LAST_INFO,
                    String.valueOf(serialNumberEntity.getSerialNumberId()),
                    lastGenerateBO
            );
        }
    }

    @Override
    List<String> generateSerialNumberList(SerialNumberInfoBO serialNumberInfo, int count) {
        SerialNumberGenerateResultBO serialNumberGenerateResult = null;
        String lockKey = RedisKeyConst.Support.SERIAL_NUMBER + serialNumberInfo.getSerialNumberId();
        try {
            boolean lock = false;
            for (int i = 0; i < MAX_GET_LOCK_COUNT; i++) {
                try {
                    lock = redisService.getLock(lockKey, 60 * 1000L);
                    if (lock) {
                        break;
                    }
                    Thread.sleep(SLEEP_MILLISECONDS);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (!lock) {
                throw new BusinessException("SerialNumber 尝试5次，未能生成单号");
            }
            // 获取上次的生成结果
            SerialNumberLastGenerateBO lastGenerateBO = (SerialNumberLastGenerateBO) redisService.mget(
                    RedisKeyConst.Support.SERIAL_NUMBER_LAST_INFO,
                    String.valueOf(serialNumberInfo.getSerialNumberId()));

            // 生成
            serialNumberGenerateResult = super.loopNumberList(lastGenerateBO, serialNumberInfo, count);

            // 将生成信息保存的内存和数据库
            lastGenerateBO.setLastNumber(serialNumberGenerateResult.getLastNumber());
            lastGenerateBO.setLastTime(serialNumberGenerateResult.getLastTime());
            serialNumberDao.updateLastNumberAndTime(serialNumberInfo.getSerialNumberId(),
                    serialNumberGenerateResult.getLastNumber(),
                    serialNumberGenerateResult.getLastTime());

            // 把生成过程保存到数据库里
            super.saveRecord(serialNumberGenerateResult);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            redisService.unLock(lockKey);
        }

        return formatNumberList(serialNumberGenerateResult, serialNumberInfo);
    }
}
