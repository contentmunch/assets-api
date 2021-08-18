package com.contentmunch.assets.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

@Slf4j
public final class LocalFileUtils {
    private static final String DOWNLOADS = "/Users/asikpradhan/Downloads/";

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

    public static File from(String url) throws IOException {

        BufferedImage img = ImageIO.read(new URL(url.replaceAll(" ", "%20")));
        File file = new File("downloaded.jpg");
        ImageIO.write(img, "jpg", file);
        return file;
    }

    public static URL urlFrom(String url) throws MalformedURLException {
        return new URL(url.replaceAll(" ", "%20"));
    }

    public static String directory(String directoryName) {
        String pathName = DOWNLOADS + directoryName;
        File directory = new File(pathName);
        if (!directory.exists()) {
            directory.mkdir();
        }
        return pathName;
    }

    public static void saveImageFile(String url, String directoryName, String name) throws IOException {
        if (!isImageFile(url)) {
            throw new UnsupportedEncodingException("Url: " + url + " is not an image file");
        }
        String pathName = directory(directoryName) + "/" + name;
        log.debug("Saving file {}", pathName);
        FileUtils.copyURLToFile(urlFrom(url), new File(pathName));
    }

    public static void deleteUrlFile() {
        File file = new File("downloaded.jpg");
        file.delete();
    }

    public static String stripExtension(String fileName) {
        return fileName != null ? fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName : null;
    }

    public static boolean isImageFile(String url) {
        return stream(ImageIO.getReaderFormatNames()).anyMatch(format -> format.equals(FilenameUtils.getExtension(url)));
    }
}