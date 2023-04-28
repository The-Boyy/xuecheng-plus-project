//package com.xuecheng.media.service.jobhandler;
//
//import com.xuecheng.base.utils.Mp4VideoUtil;
//import com.xuecheng.media.model.po.MediaProcess;
//import com.xuecheng.media.service.MediaFileProcessService;
//import com.xuecheng.media.service.MediaFileService;
//import com.xxl.job.core.context.XxlJobHelper;
//import com.xxl.job.core.handler.annotation.XxlJob;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@Slf4j
//@Component
//public class VideoTask {
//
//    @Autowired
//    MediaFileProcessService mediaFileProcessService;
//
//    @Value("${videoprocess.ffmpegpath}")
//    private String ffmpegpath;
//
//    @Autowired
//    MediaFileService mediaFileService;
//
//    /**
//     * 2、分片广播任务
//     */
//    @XxlJob("videoJobHandler")
//    public void shardingJobHandler() throws Exception {
//
//        System.out.println("111111111111111111");
//
//        // 分片参数
//        int shardIndex = XxlJobHelper.getShardIndex();
//        int shardTotal = XxlJobHelper.getShardTotal();
//
//        //CPU核心数
//        int processors = Runtime.getRuntime().availableProcessors();
//
//        //查询待处理任务
//        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
//        //任务数量
//        int size = mediaProcessList.size();
//        //创建一个线程池
//        ExecutorService executorService = Executors.newFixedThreadPool(size);
//        CountDownLatch countDownLatch = new CountDownLatch(size);
//        mediaProcessList.forEach(mediaProcess -> {
//            executorService.execute(()->{
//                try {
//                    //开启任务
//                    Long taskId = mediaProcess.getId();
//                    String fileId = mediaProcess.getFileId();
//                    boolean b = mediaFileProcessService.startTask(taskId);
//                    if (!b) {
//                        log.debug("抢占任务失败，任务id:{}", taskId);
//                        return;
//                    }
//                    String bucket = mediaProcess.getBucket();
//                    String objectName = mediaProcess.getFilePath();
//                    //下载minio视频到本地
//                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
//                    if (file == null) {
//                        log.debug("下载视频失败");
//                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
//                        return;
//                    }
//
//                    //执行转码任务
//                    String video_path = file.getPath();
//                    String mp4_name = fileId + ".mp4";
//                    File mp4File;
//                    try {
//                        mp4File = File.createTempFile("minio", ".mp4");
//                    } catch (IOException e) {
//                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
//                        System.out.println("异常");
//                        return;
//                    }
//                    String mp4_path = mp4File.getAbsolutePath();
//
//                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, video_path, mp4_name, mp4_path);
//
//                    String result = videoUtil.generateMp4();
//
//                    if (!"success".equals(result)) {
//                        mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, null, "视频转码失败");
//                    }
//                    //上传到minio
//                    boolean b1 = mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), "video/mp4", bucket, objectName);
//                    if (!b1) {
//                        log.debug("上传失败");
//                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传失败    ");
//                        return;
//                    }
//                    String url = getFilePath(fileId, ".mp4");
//                    //保存任务处理结果
//                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, "保存成功");
//                }finally {
//                    countDownLatch.countDown();
//                }
//
//            });
//        });
//        countDownLatch.await();
//    }
//
//    private String getFilePath(String fileMd5,String fileExt){
//        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
//    }
//
//}
