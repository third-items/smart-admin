package net.lab1024.smartadmin.service.module.support.heartbeat;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.lab1024.smartadmin.service.common.swagger.SwaggerTagConst;
import net.lab1024.smartadmin.service.common.domain.PageBaseDTO;
import net.lab1024.smartadmin.service.common.domain.PageResultDTO;
import net.lab1024.smartadmin.service.common.domain.ResponseDTO;
import net.lab1024.smartadmin.service.common.controller.SupportBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Api(tags = {SwaggerTagConst.Support.HEART_BEAT})
@RestController
public class HeartBeatController extends SupportBaseController {

    @Autowired
    private HeartBeatService heartBeatService;

    @PostMapping("/heartBeat/query")
    @ApiOperation("查询心跳记录 @author 卓大")
    public ResponseDTO<PageResultDTO<HeartBeatRecordVO>> query(@RequestBody @Valid PageBaseDTO pageBaseDTO) {
        return heartBeatService.pageQuery(pageBaseDTO);
    }

}