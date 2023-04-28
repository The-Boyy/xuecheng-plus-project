package com.xuecheng.media;


import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://47.98.126.47:9000")
                    .credentials("admin", "admin123")
                    .build();

    @Test
    public void test_upload() throws Exception{

        ContentInfo match = ContentInfoUtil.findExtensionMatch(".jpg");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(match != null){
            mimeType = match.getMimeType();
        }

        UploadObjectArgs UploadObjectArgs = io.minio.UploadObjectArgs.builder()
                .bucket("testbucket")
                .filename("D:\\Java\\wa.jpg")
                .object("test/wa.jpg")
                .contentType(mimeType)
                .build();

        minioClient.uploadObject(UploadObjectArgs);
    }

    @Test
    public void test_delete(){
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket("testbucket").object("test/wa.jpg").build());
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    @Test
    public void test_getFile() throws Exception{
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("test/wa.jpg").build();
        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);

        FileOutputStream outputStream = new FileOutputStream(new File("D:\\java\\wa1.jpg"));
        IOUtils.copy(inputStream,outputStream);

        String source_md5 = DigestUtils.md5Hex(inputStream);
        String local_md5 = DigestUtils.md5Hex(Files.newInputStream(new File("D:\\java\\wa1.jpg").toPath()));

        if(source_md5.equals(local_md5)){
            System.out.println("okkkkkkkkkkk");
        }
    }


    //将分块文件上传到minio
    @Test
    public void uploadChunk() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        for (int i = 0; i < 6; i++) {
            UploadObjectArgs UploadObjectArgs = io.minio.UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .filename("D:\\myMediaFile\\chunk\\" + i)
                    .object("chunk/" + i)
                    .build();

            minioClient.uploadObject(UploadObjectArgs);

            System.out.println("上传分块" + i + "成功");
        }
    }

    //调用minio接口合并分块
    @Test
    public void testMergeWithMinio() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        List<ComposeSource> sourceList = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            sourceList.add(ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build());
        }

        ComposeObjectArgs testbucket = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge01.mp4")
                .sources(sourceList)
                .build();

        minioClient.composeObject(testbucket);
    }

    //批量清理分块文件

}
