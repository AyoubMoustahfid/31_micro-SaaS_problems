error id: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/controller/UploadController.java:_empty_/MediaType#MULTIPART_FORM_DATA_VALUE#
file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/controller/UploadController.java
empty definition using pc, found symbol in pc: _empty_/MediaType#MULTIPART_FORM_DATA_VALUE#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 566
uri: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/controller/UploadController.java
text:
```scala
package com.example.microSaas.upload.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.microSaas.upload.service.UploadService;

@RestController 
@RequestMapping("/api/v1")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(
        value = "/uploads",
        consumes = MediaType.@@MULTIPART_FORM_DATA_VALUE
    )


}


```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/MediaType#MULTIPART_FORM_DATA_VALUE#