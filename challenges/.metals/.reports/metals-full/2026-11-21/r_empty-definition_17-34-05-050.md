error id: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/controller/UploadController.java:_empty_/GetFilesChallengeResponse#
file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/controller/UploadController.java
empty definition using pc, found symbol in pc: _empty_/GetFilesChallengeResponse#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 3072
uri: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/controller/UploadController.java
text:
```scala
package com.example.microSaas.upload.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.microSaas.platform.exception.ApiException;
import com.example.microSaas.upload.dto.DeleteChallengeUploadsResponse;
import com.example.microSaas.upload.dto.UploadResponse;
import com.example.microSaas.upload.dto.UploadedFileDataResponse;
import com.example.microSaas.upload.service.ChallengeUploadDeletionService;
import com.example.microSaas.upload.service.UploadService;
import com.example.microSaas.upload.service.UploadedFileDataService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1")
public class UploadController {

    private final UploadService uploadService;
    private final UploadedFileDataService uploadedFileDataService;
    private final ChallengeUploadDeletionService challengeUploadDeletionService;

    public UploadController(
        UploadService uploadService,
        UploadedFileDataService uploadedFileDataService,
        ChallengeUploadDeletionService challengeUploadDeletionService
    ) {
        this.uploadService = uploadService;
        this.uploadedFileDataService = uploadedFileDataService;
        this.challengeUploadDeletionService = challengeUploadDeletionService;
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

    @GetMapping("/uploads/{fileId}/data")
    public UploadedFileDataResponse getUploadedFileData(
        @PathVariable("fileId") String fileId,
        @RequestParam(name = "size", defaultValue = "100") int size
    ) {
        UUID parsedFileId;
        try {
            parsedFileId = UUID.fromString(fileId);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "fileId must be a valid UUID"
            );
        }

        return uploadedFileDataService.getData(parsedFileId, size);
    }

    @GetMapping("/challenges/{challengeId}/all")
    public GetFilesChall@@engeResponse getAllFilesChallengeById(
        @PathVariable("challengeId") String challengeId
    ) {
        return null;
    }

    @DeleteMapping("/challenges/{challengeId}/uploads")
    public DeleteChallengeUploadsResponse deleteChallengeUploads(
        @PathVariable("challengeId") String challengeId
    ) {
        return challengeUploadDeletionService.deleteChallengeUploads(
            challengeId
        );
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/GetFilesChallengeResponse#