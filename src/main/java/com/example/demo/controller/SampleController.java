package com.example.demo.controller;

import com.example.demo.dto.SampleDTO;
import com.example.demo.util.LocalUploader;
import com.example.demo.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sample")
@Log4j2
@RequiredArgsConstructor
public class SampleController {

    private final LocalUploader localUploader;

    private final S3Uploader s3Uploader;

    @PostMapping("/upload")
    public ResponseEntity<List<String>> upload(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length <= 0) {
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }

            List<String> uploadedFilePaths = new ArrayList<>();

            for (MultipartFile file : files) {
                uploadedFilePaths.addAll(localUploader.uploadLocal(file));
            }
            log.info("--------------------");
            log.info(uploadedFilePaths);

            List<String> s3Paths = uploadedFilePaths.stream()
                    .map(fileName -> s3Uploader.upload(fileName))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(s3Paths);
        } catch (Exception e) {
            log.error("Exception during file upload: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
