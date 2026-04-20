package com.leadelmarche.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SimplePdfService {
    public void writeLines(Path path, String title, List<String> lines) {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, buildPdfBytes(title, lines));
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'ecrire le PDF: " + path, ex);
        }
    }

    private byte[] buildPdfBytes(String title, List<String> lines) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        write(out, "%PDF-1.4\n");

        offsets.add(out.size());
        write(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets.add(out.size());
        write(out, "2 0 obj\n<< /Type /Pages /Count 1 /Kids [3 0 R] >>\nendobj\n");

        offsets.add(out.size());
        write(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");

        offsets.add(out.size());
        write(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        String stream = buildTextStream(title, lines);
        byte[] streamBytes = stream.getBytes(StandardCharsets.US_ASCII);
        offsets.add(out.size());
        write(out, "5 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
        out.write(streamBytes);
        write(out, "\nendstream\nendobj\n");

        int xrefPos = out.size();
        write(out, "xref\n0 6\n");
        write(out, "0000000000 65535 f \n");
        for (int i = 1; i <= 5; i++) {
            write(out, String.format("%010d 00000 n \n", offsets.get(i)));
        }
        write(out, "trailer\n<< /Size 6 /Root 1 0 R >>\n");
        write(out, "startxref\n" + xrefPos + "\n%%EOF");
        return out.toByteArray();
    }

    private String buildTextStream(String title, List<String> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("BT\n/F1 14 Tf\n40 800 Td\n(").append(escape(title)).append(") Tj\nET\n");

        int y = 775;
        int printed = 0;
        for (String line : lines) {
            if (y < 40 || printed >= 140) {
                break;
            }
            sb.append("BT\n/F1 10 Tf\n40 ").append(y).append(" Td\n(").append(escape(line)).append(") Tj\nET\n");
            y -= 12;
            printed++;
        }
        return sb.toString();
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private void write(ByteArrayOutputStream out, String content) throws IOException {
        out.write(content.getBytes(StandardCharsets.US_ASCII));
    }
}
