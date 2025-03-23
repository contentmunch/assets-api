package com.contentmunch.assets;

import com.contentmunch.assets.external.GoogleDriveVideoService;
import com.contentmunch.assets.service.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AssetsApiApplicationTests {
    @Autowired
    private VideoService videoService;

    @Test
    void contextLoads() {
        System.out.println(videoService.findVideoBy("1x_sOP-aonB_MtIofiiyI2fBKen15YCGH","cover-video"));
    }

}
