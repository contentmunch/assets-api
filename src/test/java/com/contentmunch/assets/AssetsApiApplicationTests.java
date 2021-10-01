package com.contentmunch.assets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AssetsApiApplicationTests {

    @Test
    void contextLoads() {
        var folderId = "1234556";
        var name = "asik";
        System.out.println("'" + folderId + "' in parents and name='" + name + "'");
    }

}
