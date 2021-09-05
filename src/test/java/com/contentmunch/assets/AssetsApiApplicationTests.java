package com.contentmunch.assets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;

@SpringBootTest
class AssetsApiApplicationTests {

    @Test
    void contextLoads() throws MalformedURLException {
        String downloadUrl = "https://drive.google.com/uc?export=download&id=1-4jpNE5CpSWDvecsXUwkQXDHH7kSSnJb";
        String downloadUrl1 = "https://drive.google.com/uc?id=1-4jpNE5CpSWDvecsXUwkQXDHH7kSSnJb&export=download";

        URL url = new URL(downloadUrl.replaceAll(" ", "%20"));
        var parameters = UriComponentsBuilder.fromUriString(downloadUrl).build().getQueryParams();
        System.out.println(parameters.getFirst("id"));
        //return stream(ImageIO.getReaderFormatNames()).anyMatch(format -> format.equals(FilenameUtils.getExtension(url)));
    }

}
