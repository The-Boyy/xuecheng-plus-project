package com.xuecheng.media.api;

import com.baomidou.mybatisplus.extension.api.R;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.media.model.dto.*;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {

//    public static void main(String[] args) throws IOException {
//
//        File min = File.createTempFile("min", ".temp");
//        System.out.println(min.getAbsolutePath());
//    }

    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            XueChengPlusException.cast("用户未登录");
        }
        Long companyId = Long.parseLong(user.getCompanyId());
        return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);
    }

//    @PreAuthorize("hasAnyAuthority('media_admin')")
    @PostMapping("/files/all")
    @PreAuthorize("hasAnyAuthority('media_admin')")
    public PageResult<MediaFiles> mediaList(PageParams pageParams, @RequestBody(required = false) QueryMediaParamsDto queryMediaParamsDto){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            XueChengPlusException.cast("用户未登录");
        }
        return mediaFileService.queryMediaFiels(null, pageParams, queryMediaParamsDto);
    }

    @ApiOperation("上传图片")
    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile file, @RequestParam(value = "objectName", required = false) String objectName) throws Exception {

        SecurityUtil.XcUser user = SecurityUtil.getUser();

        if(user == null){
            XueChengPlusException.cast("用户未登录");
        }

        Long companyId = Long.valueOf(user.getCompanyId());
        String username = user.getUsername();

        UploadFileParamsDto dto = new UploadFileParamsDto();
        dto.setFilename(file.getOriginalFilename());
        dto.setFileSize(file.getSize());
        //图片资源
        dto.setFileType("001001");
        dto.setTags("图片资源");
        dto.setUsername(username);

        File tempFile = File.createTempFile("minio", ".temp");
        //transferTo:将内存文件存入磁盘中
        file.transferTo(tempFile);

        String absolutePath = tempFile.getAbsolutePath();

        return mediaFileService.uploadFile(companyId, dto, absolutePath, objectName);
    }

    @GetMapping("/file/compareWithLastYear")
    public ResultResponse<CompareWithLastYear> compareWithLastYear(){
        return mediaFileService.compareWithLastYear();
    }

    @GetMapping("/test")
    public String testtest(){
        SecurityUtil.XcUser user = SecurityUtil.getUser();

        if(user == null){
            XueChengPlusException.cast("用户未登录");
        }
        Long companyId = Long.valueOf(user.getCompanyId());
        return mediaFileService.testtest(companyId);
    }

    @DeleteMapping("/deleteFileById")
    public ResultResponse<?> deleteFileById(String fileId){

        return mediaFileService.deleteFileById(fileId);
    }

    @GetMapping("/selectFileById")
    public ResultResponse<MediaFiles> selectFileById(String fileId){
        return mediaFileService.selectFileById(fileId);
    }

    @PostMapping("/auditFileById")
    public ResultResponse<MediaFiles> auditFileById(@RequestBody AuditMediaFileDto auditMediaFileDto){
        return mediaFileService.auditFileById(auditMediaFileDto);
    }
}