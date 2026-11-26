error id: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/controller/UploadController.java:_empty_/UploadService#
file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/controller/UploadController.java
empty definition using pc, found symbol in pc: _empty_/UploadService#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 705
uri: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/controller/UploadController.java
text:
```scala
package com.example.microSaas.upload.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.microSaas.upload.dto.UploadResponse;
import com.example.microSaas.upload.service.UploadService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1")
public class UploadController {

    private final Uploa@@dService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(
        value = "/uploads",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public UploadResponse upload(
        @RequestParam("challengeId") @NotBlank String challengeId,
        @RequestParam("files") List<MultipartFile> files,
        @RequestParam("datasetNames") List<String> datasetNames
    ) throws Exception {

        return uploadService.upload(
            challengeId,
            files,
            datasetNames
        );
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/UploadService#