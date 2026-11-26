package com.example.microSaas.upload.scanner;

import java.nio.file.Path;

public interface AntivirusScanner {

    ScanResult scan(Path file);

    enum ScanResult {
        CLEAN,
        INFECTED,
        ERROR
    }
}