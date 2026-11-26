package com.example.microSaas.upload.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ClamAvScannerTest {

    @Test
    void reportsErrorWhenScannerExecutableIsUnavailable() {
        ClamAvScanner scanner = new ClamAvScanner("clamav-command-not-installed", "", 1);

        assertEquals(AntivirusScanner.ScanResult.ERROR, scanner.scan(Path.of("upload.csv")));
    }
}
