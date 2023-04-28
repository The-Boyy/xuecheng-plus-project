package com.xuecheng.media.api;

import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);

    }

    @ApiOperation("上传图片")
    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile file, @RequestParam(value = "objectName", required = false) String objectName) throws Exception {

        UploadFileParamsDto dto = new UploadFileParamsDto();
        dto.setFilename(file.getOriginalFilename());
        dto.setFileSize(file.getSize());
        //图片资源
        dto.setFileType("001001");
        dto.setTags("图片资源");

        File tempFile = File.createTempFile("minio", ".temp");
        //transferTo:将内存文件存入磁盘中
        file.transferTo(tempFile);
        Long companyId = 1232141425L;

        String absolutePath = tempFile.getAbsolutePath();

        return mediaFileService.uploadFile(companyId, dto, absolutePath, objectName);
    }


}