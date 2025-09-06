package com.javarush.jira.bugtracking.attachment;

import com.javarush.jira.common.error.IllegalRequestDataException;
import com.javarush.jira.common.error.NotFoundException;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class FileUtil {
    private static final String ATTACHMENT_PATH = "./attachments/%s/";

    public static void upload(MultipartFile multipartFile, String directoryPath, String fileName) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalRequestDataException("Select a file to upload.");
        }
        if (directoryPath == null || directoryPath.isBlank()) {
            throw new IllegalRequestDataException("Target directory must be provided.");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalRequestDataException("Filename must be provided.");
        }

        Path baseDir = Paths.get(directoryPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalRequestDataException("Cannot create directory: " + baseDir);
        }

        Path target = baseDir.resolve(fileName).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IllegalRequestDataException("Illegal file path.");
        }

        try (var in = multipartFile.getInputStream()) {
            Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalRequestDataException("Failed to upload file " + multipartFile.getOriginalFilename());
        }
    }

    public static Resource download(String fileLink) {
        Path path = Paths.get(fileLink);
        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalRequestDataException("Failed to download file " + resource.getFilename());
            }
        } catch (MalformedURLException ex) {
            throw new NotFoundException("File" + fileLink + " not found");
        }
    }

    public static void delete(String fileLink) {
        Path path = Paths.get(fileLink);
        try {
            Files.delete(path);
        } catch (IOException ex) {
            throw new IllegalRequestDataException("File" + fileLink + " deletion failed.");
        }
    }

    public static String getPath(String titleType) {
        return String.format(ATTACHMENT_PATH, titleType.toLowerCase());
    }
}
