package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.*;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.feignclient.UserServiceClient;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MinioClient minioClient;

    @Autowired
    UserServiceClient userServiceClient;

    @Autowired
    MediaFileService currentProxy;

    @Value("${minio.bucket.files}")
    String bucket_mediafiles;

    @Value("${minio.bucket.videofiles}")
    String bucket_video;

    @Override
    public MediaFiles getFileById(String mediaId) {
        return mediaFilesMapper.selectById(mediaId);
    }

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        LambdaQueryWrapper<MediaFiles> queryWrapper;
        if(companyId != null){
            queryWrapper = new LambdaQueryWrapper<MediaFiles>().eq(MediaFiles::getCompanyId, companyId).orderByDesc(MediaFiles::getCreateDate);
        }else {
            queryWrapper = new LambdaQueryWrapper<MediaFiles>().orderByDesc(MediaFiles::getCreateDate);
        }

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, (int)total, pageParams.getPageNo(), pageParams.getPageSize());

    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date()).replace("-", "/") + "/";
    }

    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        ContentInfo match = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (match != null) {
            mimeType = match.getMimeType();
        }

        return mimeType;
    }

    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder().bucket(bucket).object(objectName).filename(localFilePath).contentType(mimeType).build();
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件到minio成功,bucket{},objectName:{}", bucket, objectName);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错,bucket{},objectName:{},错误信息:{}", bucket, objectName, e.getMessage());
            XueChengPlusException.cast("上传文件到minio失败");
        }
        return true;
    }

    public boolean queryFileIsExist(String filePath, String bucket){
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(filePath).build();
        try {
            minioClient.getObject(getObjectArgs);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto paramsDto, String localFilePath, String objectName) {
        UploadFileResultDto resultDto = new UploadFileResultDto();

        String fileMd5 = getFileMd5(new File(localFilePath));
        String filename = paramsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);

        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        //数据库中存在该文件信息
        if(mediaFiles != null){
            //判断文件是否失效，如果失效则存入minio
            String filePath = mediaFiles.getFilePath();
            boolean isExist = queryFileIsExist(filePath, bucket_mediafiles);
            if(!isExist){
                //不存在，说明数据库中保存的路径失效，重新写入minio
                currentProxy.addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediafiles, filePath);
            }
            BeanUtils.copyProperties(mediaFiles, resultDto);
            return resultDto;
        }

        String defaultFolderPath = getDefaultFolderPath();
        if(StringUtils.isEmpty(objectName)){
            objectName = defaultFolderPath + fileMd5 + extension;
        }

        currentProxy.addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediafiles, objectName);
        //保存文件信息
        mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, paramsDto, bucket_mediafiles, objectName);
        if (mediaFiles == null) {
            XueChengPlusException.cast("文件上传后保存信息失败");
        }

        BeanUtils.copyProperties(mediaFiles, resultDto);
        return resultDto;
    }

    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            mediaFiles.setChangeDate(LocalDateTime.now());
            mediaFiles.setCompanyName(userServiceClient.queryCompanyNameById(String.valueOf(companyId)));
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败,{}", mediaFiles.toString());
                XueChengPlusException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());

            addWaitingTask(mediaFiles);
        }
        return mediaFiles;

    }

    public void addWaitingTask(MediaFiles mediaFiles){

        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);

        if("video/x-msvideo".equals(mimeType)){
            MediaProcess mediaProcess = new MediaProcess();

            BeanUtils.copyProperties(mediaFiles, mediaProcess);

            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);

            mediaProcessMapper.insert(mediaProcess);
        }
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {

        //先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);

        if (mediaFiles != null) {
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(filePath).build();
            try {
                minioClient.getObject(getObjectArgs);
                return RestResponse.success(true);
            } catch (Exception e) {
                //文件不存在
                return RestResponse.success(false);
            }
        }
        //文件不存在
        return RestResponse.success(false);
    }

    //检查桶里分块是否存在
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {

        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath + chunkIndex)
                .build();
        try {
            minioClient.getObject(getObjectArgs);
            return RestResponse.success(true);
        } catch (Exception e) {
            //文件不存在
            return RestResponse.success(false);
        }
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {

        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5) + chunk;

        //获取mimeType
        String mimeType = getMimeType(null);

        //上传分块
        addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_video, chunkFileFolderPath);

        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {

        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String objectName = getFilePathByMd5(fileMd5, extension);

        List<ComposeSource> sourceList = new ArrayList<>();

        for (int i = 0; i < chunkTotal; i++) {
            sourceList.add(ComposeSource.builder().bucket(bucket_video).object(chunkFileFolderPath + i).build());
        }

        ComposeObjectArgs testbucket = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(objectName)
                .sources(sourceList)
                .build();
        try {
            minioClient.composeObject(testbucket);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错， bucket{}, objectName:{}, 错误信息{}",bucket_video, objectName, e.getMessage());
            return RestResponse.validfail(false, "合并文件异常");
        }

        //校验
        File file = downloadFileFromMinIO(bucket_video, objectName);

        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            String mergeFile_md5 = DigestUtils.md5Hex(fileInputStream);
            if(!fileMd5.equals(mergeFile_md5)){
                return RestResponse.validfail(false, "文件校验失败");
            }
        }catch (Exception e){
            return RestResponse.validfail(false, "文件校验失败");
        }

        //文件信息入库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if(mediaFiles == null){
            return RestResponse.validfail(false, "文件入库失败");
        }

        //清理分块文件
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }

    @Override
    public ResultResponse<CompareWithLastYear> compareWithLastYear() {

        LambdaQueryWrapper<MediaFiles> wrapper = new LambdaQueryWrapper<MediaFiles>().orderByDesc(MediaFiles::getCreateDate);

        List<MediaFiles> mediaFiles = mediaFilesMapper.selectList(wrapper);

        Map<Integer, Integer> map = new HashMap<>();

        int year = LocalDateTime.now().getYear();
        map.put(year, 0);
        map.put(year - 1, 0);

        for (MediaFiles mediaFile : mediaFiles) {
            int y = mediaFile.getCreateDate().getYear();
            if(map.containsKey(y)){
                map.put(y, map.get(y) + 1);
            }else {
                break;
            }
        }

        CompareWithLastYear compareWithLastYear = new CompareWithLastYear();
        compareWithLastYear.setDate(year + "年");

        Integer curNum = map.get(year);
        Integer lastNum = map.get(year - 1);
        compareWithLastYear.setFlag(curNum >= lastNum);

        compareWithLastYear.setRate((curNum - lastNum) / (lastNum + 0.000001) * 100);

        return ResultResponse.success(200, compareWithLastYear);
    }

    @Override
    public String testtest(Long companyId) {

        String id = String.valueOf(companyId);
        String s = userServiceClient.queryCompanyNameById(id);
        return s;
    }

    @Override
    @Transactional
    public ResultResponse<?> deleteFileById(String fileId) {

        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if(mediaFiles == null){
            XueChengPlusException.cast("文件不存在");
        }
        int i = mediaFilesMapper.deleteById(fileId);
        if(i <= 0){
            XueChengPlusException.cast("删除失败，请重试");
        }
        return ResultResponse.success(200, null);
    }

    @Override
    public ResultResponse<MediaFiles> selectFileById(String fileId) {
        return ResultResponse.success(200, mediaFilesMapper.selectById(fileId));
    }

    @Override
    public ResultResponse<MediaFiles> auditFileById(AuditMediaFileDto auditMediaFileDto) {

        MediaFiles mediaFiles = mediaFilesMapper.selectById(auditMediaFileDto.getId());
        if(mediaFiles == null){
            XueChengPlusException.cast("文件不存在");
        }
        String auditStatus = auditMediaFileDto.getAuditStatus();
        if(auditStatus == null || "".equals(auditStatus)){
            XueChengPlusException.cast("审核结果不能为空");
        }
        if(!("002003".equals(auditStatus) || "002001".equals(auditStatus) || "002002".equals(auditStatus))){
            XueChengPlusException.cast("审核状态错误");
        }
        mediaFiles.setAuditStatus(auditStatus);
        mediaFiles.setAuditMind(auditMediaFileDto.getAuditMind());

        int i = mediaFilesMapper.updateById(mediaFiles);
        if(i <= 0){
            XueChengPlusException.cast("审核失败");
        }
        return ResultResponse.success(200, mediaFiles);
    }

    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("video").objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清除分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清除分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }
}
