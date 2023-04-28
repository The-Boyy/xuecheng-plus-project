package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

public class BigFileTest {

    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("D:\\myMediaFile\\1.mp4");

        String chunkFilePath = "D:\\myMediaFile\\chunk\\";

        //5MB
        int chunkSize = 1024 * 1024 * 5;

        int chunkNum = (int)Math.ceil(sourceFile.length() * 1.0 / chunkSize);

        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");

        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);

            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");

            int len = -1;

            while ((len = raf_r.read(bytes)) != -1){
                raf_rw.write(bytes, 0, len);
                if(chunkFile.length() >= chunkSize){
                    break;
                }
            }

            raf_rw.close();
        }
        raf_r.close();
    }

    @Test
    public void testMerge() throws IOException {

        File sourceFile = new File("D:\\myMediaFile\\1.mp4");

        File chunkFolder = new File("D:\\myMediaFile\\chunk\\");

        File mergeFile = new File("D:\\myMediaFile\\1_merge.mp4");

        File[] files = chunkFolder.listFiles();

        assert files != null;
        Arrays.sort(files, Comparator.comparingInt((File a) -> Integer.parseInt(a.getName())));

        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");

        byte[] bytes = new byte[1024];

        for (File file : files) {
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;

            while ((len = raf_r.read(bytes)) != -1){
                raf_rw.write(bytes, 0, len);
            }

            raf_r.close();
        }

        raf_rw.close();

        //校验
        FileInputStream fileInputStream_merge = new FileInputStream(mergeFile);
        FileInputStream fileInputStream_source = new FileInputStream(sourceFile);
        String s = DigestUtils.md5Hex(fileInputStream_merge);
        String s1 = DigestUtils.md5Hex(fileInputStream_source);

        if(s.equals(s1)){
            System.out.println("合并成功");
        }
    }
}
