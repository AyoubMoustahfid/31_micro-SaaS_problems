package com.example.microSaas.upload.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.microSaas.upload.dto.UploadResponse;
import com.example.microSaas.upload.service.ChallengeUploadDeletionService;
import com.example.microSaas.upload.service.UploadService;
import com.example.microSaas.upload.service.UploadedFileDataService;

class UploadControllerTest {

    private final UploadService uploadService = mock(UploadService.class);
    private final UploadController controller = new UploadController(
        uploadService,
        mock(UploadedFileDataService.class),
        mock(ChallengeUploadDeletionService.class)
    );

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void acceptsUploadAndReturnsStatusLocation() throws Exception {
        UUID uploadId = UUID.randomUUID();
        UploadResponse response = new UploadResponse(
            uploadId,
            "MS_01",
            "PROCESSING",
            List.of()
        );
        when(uploadService.upload(
            org.mockito.ArgumentMatchers.eq("MS_01"),
            org.mockito.ArgumentMatchers.anyList(),
            org.mockito.ArgumentMatchers.eq(List.of("vendor"))
        )).thenReturn(response);
        MockHttpServletRequest request = new MockHttpServletRequest(
            "POST",
            "/api/v1/uploads"
        );
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(request)
        );

        var result = controller.upload(
            "MS_01",
            List.of(new MockMultipartFile(
                "files",
                "vendors.csv",
                "text/csv",
                "data".getBytes()
            )),
            List.of("vendor")
        );

        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        assertEquals(
            "http://localhost:8080/api/v1/uploads/" + uploadId + "/status",
            result.getHeaders().getLocation().toString()
        );
        assertEquals(response, result.getBody());
    }
}
