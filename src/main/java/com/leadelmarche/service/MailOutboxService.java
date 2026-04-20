package com.leadelmarche.service;

import com.leadelmarche.persistence.TextFileDatabase;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MailOutboxService {
    private final TextFileDatabase textFileDatabase;

    public MailOutboxService(TextFileDatabase textFileDatabase) {
        this.textFileDatabase = textFileDatabase;
    }

    public String sendMail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Destinataire email obligatoire");
        }
        String safeSubject = (subject == null || subject.isBlank()) ? "LeadelMarche" : subject.trim();
        String safeBody = body == null ? "" : body;
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Path outboxDir = textFileDatabase.getBasePath().resolve("outbox_mails");
        String safeTo = to.trim().replaceAll("[^a-zA-Z0-9@._-]", "_");
        Path mailFile = outboxDir.resolve("mail_" + safeTo + "_" + stamp + ".txt");
        StringBuilder content = new StringBuilder();
        content.append("TO: ").append(to.trim()).append('\n');
        content.append("SUBJECT: ").append(safeSubject).append('\n');
        content.append("DATE: ").append(LocalDateTime.now()).append("\n\n");
        content.append(safeBody);
        try {
            Files.createDirectories(outboxDir);
            Files.writeString(mailFile, content.toString(), StandardCharsets.UTF_8);
            return mailFile.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'ecrire le mail outbox: " + mailFile, ex);
        }
    }
}
