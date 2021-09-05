package com.contentmunch.assets.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

@Slf4j
public final class LocalFileUtils {

    public static File from(MultipartFile multipartFile) throws IOException {
        File filePath = new File(requireNonNull(multipartFile.getOriginalFilename()));
        try (OutputStream os = new FileOutputStream(filePath)) {
            os.write(multipartFile.getBytes());
        }
        return filePath;
    }

    public static void deleteTempFile(MultipartFile multipartFile) {
        File filePath = new File(requireNonNull(multipartFile.getOriginalFilename()));
        if (filePath.delete()) {
            log.debug(multipartFile.getOriginalFilename() + " deleted.");
        }
    }

    public static String stripExtension(String fileName) {
        return fileName != null ? fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName : null;
    }

    public static boolean isImageFile(String url) {
        return stream(ImageIO.getReaderFormatNames()).anyMatch(format -> format.equals(FilenameUtils.getExtension(url)));
    }
}
