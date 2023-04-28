package com.xuecheng.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;

import java.util.List;

@SpringBootTest
public class TeachplanServiceTests {

    @Autowired
    TeachplanService teachplanService;

    @Test
    public void testTeachplanService(){
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(117L);
    }
}
