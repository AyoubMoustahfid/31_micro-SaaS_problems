error id: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/scanner/ClamAvScanner.java:_empty_/LoggerFactory#getLogger#
file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/scanner/ClamAvScanner.java
empty definition using pc, found symbol in pc: _empty_/LoggerFactory#getLogger#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 601
uri: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/scanner/ClamAvScanner.java
text:
```scala
package com.example.microSaas.upload.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClamAvScanner implements AntivirusScanner {

    private static final Logger logger = LoggerFactory.ge@@tLogger(ClamAvScanner.class);
    private static final int MAX_DIAGNOSTIC_LENGTH = 4096;

    private final String executable;
    private final String databaseDirectory;
    private final long timeoutSeconds;

    public ClamAvScanner(
        @Value("${app.antivirus.executable}") String executable,
        @Value("${app.antivirus.database-directory:}") String databaseDirectory,
        @Value("${app.antivirus.timeout-seconds:60}")
        long timeoutSeconds
    ) {
        this.executable = executable;
        this.databaseDirectory = databaseDirectory;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public ScanResult scan(Path file) {

        Process process = null;
        Thread outputReader = null;
        AtomicReference<String> output = new AtomicReference<>("");
        AtomicReference<IOException> outputReadFailure = new AtomicReference<>();

        try {
            var command = new ArrayList<String>();
            command.add(executable);
            command.add("--no-summary");
            if (!databaseDirectory.isBlank()) {
                command.add("--database=" + databaseDirectory);
            }
            command.add(file.toAbsolutePath().toString());
            process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            Process runningProcess = process;
            outputReader = Thread.ofVirtual().start(() ->
                captureOutput(runningProcess, output, outputReadFailure)
            );

            boolean completed = process.waitFor(
                timeoutSeconds,
                TimeUnit.SECONDS
            );

            if (!completed) {
                process.destroyForcibly();
                process.waitFor();
                outputReader.join();
                logger.warn(
                    "ClamAV scan timed out after {} seconds for {}. Output: {}",
                    timeoutSeconds,
                    file,
                    output.get()
                );
                return ScanResult.ERROR;
            }

            outputReader.join();
            IOException readFailure = outputReadFailure.get();
            if (readFailure != null) {
                logger.error(
                    "Could not read ClamAV output for {}",
                    file,
                    readFailure
                );
                return ScanResult.ERROR;
            }

            return switch (process.exitValue()) {
                case 0 -> ScanResult.CLEAN;
                case 1 -> ScanResult.INFECTED;
                default -> {
                    logger.error(
                        "ClamAV returned exit code {} for {}. Output: {}",
                        process.exitValue(),
                        file,
                        output.get()
                    );
                    yield ScanResult.ERROR;
                }
            };

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroyForcibly();
            }
            logger.warn("ClamAV scan was interrupted", exception);
            return ScanResult.ERROR;
        } catch (java.io.IOException | SecurityException exception) {
            if (process != null) {
                process.destroyForcibly();
            }
            logger.error("Could not run ClamAV executable '{}'", executable, exception);
            return ScanResult.ERROR;
        }
    }

    private void captureOutput(
        Process process,
        AtomicReference<String> output,
        AtomicReference<IOException> readFailure
    ) {
        StringBuilder captured = new StringBuilder();
        byte[] buffer = new byte[1024];
        try (InputStream stream = process.getInputStream()) {
            int count;
            while ((count = stream.read(buffer)) != -1) {
                int remaining = MAX_DIAGNOSTIC_LENGTH - captured.length();
                if (remaining > 0) {
                    captured.append(new String(
                        buffer,
                        0,
                        Math.min(count, remaining),
                        StandardCharsets.UTF_8
                    ));
                }
            }
        } catch (IOException exception) {
            readFailure.set(exception);
        } finally {
            output.set(captured.toString().trim());
        }
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/LoggerFactory#getLogger#