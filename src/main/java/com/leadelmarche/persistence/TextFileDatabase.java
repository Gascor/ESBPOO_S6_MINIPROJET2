package com.leadelmarche.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TextFileDatabase {
    private final Path basePath;

    public TextFileDatabase(Path basePath) {
        this.basePath = basePath;
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize data directory: " + basePath, e);
        }
    }

    public Path getBasePath() {
        return basePath;
    }

    public synchronized List<String> readLines(String fileName) {
        Path path = basePath.resolve(fileName);
        ensureFile(path);
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read file: " + path, e);
        }
    }

    public synchronized void writeLines(String fileName, List<String> lines) {
        Path path = basePath.resolve(fileName);
        ensureFile(path);
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write file: " + path, e);
        }
    }

    public synchronized void appendLine(String fileName, String line) {
        List<String> lines = new ArrayList<>(readLines(fileName));
        lines.add(line);
        writeLines(fileName, lines);
    }

    private void ensureFile(Path path) {
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to prepare file: " + path, e);
        }
    }
}

