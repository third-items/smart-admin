package net.lab1024.smartadmin.module.support.file.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.smartadmin.common.domain.PageResultDTO;
import net.lab1024.smartadmin.common.domain.ResponseDTO;
import net.lab1024.smartadmin.module.support.file.FileDao;
import net.lab1024.smartadmin.module.support.file.constant.FileModuleTypeEnum;
import net.lab1024.smartadmin.module.support.file.constant.FileResponseCodeConst;
import net.lab1024.smartadmin.module.support.file.constant.FileServiceTypeEnum;
import net.lab1024.smartadmin.module.support.file.domain.dto.FileAddDTO;
import net.lab1024.smartadmin.module.support.file.domain.dto.FileDTO;
import net.lab1024.smartadmin.module.support.file.domain.dto.FileQueryDTO;
import net.lab1024.smartadmin.module.support.file.domain.entity.FileEntity;
import net.lab1024.smartadmin.module.support.file.domain.vo.FileVO;
import net.lab1024.smartadmin.module.support.file.domain.vo.UploadVO;
import net.lab1024.smartadmin.module.system.login.domain.RequestTokenBO;
import net.lab1024.smartadmin.util.SmartBaseEnumUtil;
import net.lab1024.smartadmin.util.SmartBeanUtil;
import net.lab1024.smartadmin.util.SmartPageUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [  ]
 *
 * @author yandanyang
 * @version 1.0
 * @company 1024lab.net
 * @copyright (c) 2019 1024lab.netInc. All rights reserved.
 * @date
 * @since JDK1.8
 */
@Service
public class FileService {

    @Autowired
    private FileDao fileDao;

    @Autowired
    private java.util.Map<String, IFileService> fileServiceMap;

    /**
     * ????????????????????????
     *
     * @param typeEnum
     * @return
     */
    private IFileService getFileService(FileServiceTypeEnum typeEnum) {
        /**
         * ??????????????????
         */
        String serviceName = typeEnum.getServiceName();
        IFileService fileService = fileServiceMap.get(serviceName);
        if (null == fileService) {
            throw new RuntimeException("?????????????????????????????????" + serviceName);
        }
        return fileService;
    }

    /**
     * ??????????????????
     *
     * @param file
     * @param typeEnum   ???????????????????????????
     * @param moduleType ???????????????
     * @return
     */
    public ResponseDTO<UploadVO> fileUpload(MultipartFile file, FileServiceTypeEnum typeEnum, Integer moduleType) {
        FileModuleTypeEnum moduleTypeEnum = SmartBaseEnumUtil.getEnumByValue(moduleType, FileModuleTypeEnum.class);
        if (null == moduleTypeEnum) {
            return ResponseDTO.wrap(FileResponseCodeConst.FILE_MODULE_ERROR);
        }
        // ??????????????????
        IFileService fileService = this.getFileService(typeEnum);
        ResponseDTO<UploadVO> response = fileService.fileUpload(file, moduleTypeEnum.getPath());
        return response;
    }

    /**
     * ???????????????????????? ????????????URL
     *
     * @param path
     * @return
     */
    public ResponseDTO<String> getFileUrl(String path, FileServiceTypeEnum typeEnum) {
        IFileService fileService = this.getFileService(typeEnum);
        return fileService.getFileUrl(path);
    }

    /**
     * ????????????
     *
     * @param fileDTOList
     */
    public void insertFileBatch(List<FileDTO> fileDTOList) {
        fileDao.insertFileBatch(fileDTOList);
    }

    /**
     * ??????module ??????????????????
     *
     * @param moduleId
     * @return
     */
    public void deleteFilesByModuleId(String moduleId) {
        fileDao.deleteFilesByModuleId(moduleId);
    }

    /**
     * ??????module ??????????????????
     *
     * @param moduleId
     * @return
     */
    public List<FileVO> listFilesByModuleId(String moduleId) {
        return fileDao.listFilesByModuleId(moduleId);
    }

    /**
     * @param filesStr ??????????????????id?????????
     * @return
     */
    public List<FileVO> getFileDTOList(String filesStr) {
        if (StringUtils.isEmpty(filesStr)) {
            return Lists.newArrayList();
        }
        String[] fileIds = filesStr.split(",");
        List<Long> fileIdList = Arrays.asList(fileIds).stream().map(e -> Long.valueOf(e)).collect(Collectors.toList());
        List<FileVO> files = fileDao.listFilesByFileIds(fileIdList);
        return files;
    }

    /**
     * ????????????????????????
     *
     * @param queryDTO
     * @return
     */
    public ResponseDTO<PageResultDTO<FileVO>> queryListByPage(FileQueryDTO queryDTO) {
        Page page = SmartPageUtil.convert2QueryPage(queryDTO);
        List<FileVO> fileList = fileDao.queryListByPage(page, queryDTO);
        if (CollectionUtils.isNotEmpty(fileList)) {
            fileList.forEach(e -> {
                // ????????????????????? ???????????????????????? ?????? url
                FileServiceTypeEnum serviceTypeEnum = SmartBaseEnumUtil.getEnumByValue(e.getFileLocationType(), FileServiceTypeEnum.class);
                IFileService fileService = this.getFileService(serviceTypeEnum);
                e.setFileUrl(fileService.getFileUrl(e.getFilePath()).getData());
            });
        }
        PageResultDTO<FileVO> pageResultDTO = SmartPageUtil.convert2PageResult(page, fileList);
        return ResponseDTO.succData(pageResultDTO);
    }

    /**
     * ??????id ????????????
     *
     * @param id
     * @param request
     * @return
     */
    public ResponseEntity<byte[]> downLoadById(Long id, HttpServletRequest request) {
        FileEntity entity = fileDao.selectById(id);
        if (null == entity) {
            throw new RuntimeException("?????????????????????");
        }

        // ????????????????????? ???????????????????????? ?????? url
        FileServiceTypeEnum serviceTypeEnum = SmartBaseEnumUtil.getEnumByValue(entity.getFileLocationType(), FileServiceTypeEnum.class);
        IFileService fileService = this.getFileService(serviceTypeEnum);
        ResponseEntity<byte[]> stream = fileService.fileDownload(entity.getFilePath(), entity.getFileName(), request);
        return stream;
    }

    /**
     * ??????????????????????????????
     * @param addDTO
     * @return
     */
    public ResponseDTO<String> saveFile(FileAddDTO addDTO, RequestTokenBO requestToken) {
        FileEntity entity = SmartBeanUtil.copy(addDTO,FileEntity.class);
        entity.setCreaterUser(requestToken.getRequestUserId());
        entity.setCreateTime(new Date());
        fileDao.insert(entity);
        return ResponseDTO.succ();
    }
}
