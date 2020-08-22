package com.github.wensimin.ashioarae.controller;

import com.github.wensimin.ashioarae.service.enums.AshiType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("file")
public class FileController {
    @Value("${system.file.base-path}")
    private String fileBasePath;

    @PostMapping
    public String upload(@RequestParam("file") MultipartFile file,
                         Principal principal)
            throws IOException {
        String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse(".jpg");
        String name = "preHead" + fileName.substring(fileName.lastIndexOf("."));
        String filePath = fileBasePath + "/" + principal.getName() + "/" + name;
        File basePath = new File(fileBasePath + "/" + principal.getName());
        if (!basePath.exists()) {
            if (!basePath.mkdirs()) {
                throw new RuntimeException("创建目录失败");
            }
        }
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filePath)))) {
            stream.write(file.getBytes());
        }
        return principal.getName() + "/" + name;
    }

    @GetMapping("public/{username}/{fileName}")
    public ResponseEntity<?> getFile(@PathVariable String username, @PathVariable String fileName) {
        File file = new File(fileBasePath + "/" + username + "/" + fileName);
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getName() + "\"").body(new FileSystemResource(file));
    }
}
