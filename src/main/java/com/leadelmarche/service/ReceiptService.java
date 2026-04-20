package com.leadelmarche.service;

import com.leadelmarche.domain.customer.Customer;
import com.leadelmarche.domain.sales.Receipt;
import com.leadelmarche.domain.sales.Sale;
import com.leadelmarche.domain.sales.SaleLine;
import com.leadelmarche.persistence.ReceiptRepository;
import com.leadelmarche.persistence.TextFileDatabase;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final TextFileDatabase textFileDatabase;

    public ReceiptService(ReceiptRepository receiptRepository, TextFileDatabase textFileDatabase) {
        this.receiptRepository = receiptRepository;
        this.textFileDatabase = textFileDatabase;
    }

    public Receipt buildReceipt(Sale sale, List<SaleLine> lines, Customer customer, PromotionComputation promotions) {
        Receipt receipt = new Receipt();
        receipt.setSaleId(sale.getId());
        receipt.setCustomerEmail(customer == null ? "" : customer.getEmail());
        receipt.setTextBody(buildTextBody(sale, lines, customer, promotions));
        receiptRepository.create(receipt);
        writeTicketFile(sale.getSaleNumber(), receipt.getTextBody());
        return receipt;
    }

    public String print(Receipt receipt) {
        receipt.setPrinted(true);
        receiptRepository.update(receipt);
        return writeEventFile("printed_tickets", receipt.getSaleId(), receipt.getTextBody());
    }

    public String email(Receipt receipt, String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email destinataire obligatoire");
        }
        receipt.setEmailed(true);
        receipt.setCustomerEmail(email.trim());
        receiptRepository.update(receipt);
        StringBuilder mail = new StringBuilder();
        mail.append("TO: ").append(email.trim()).append('\n');
        mail.append("SUBJECT: Ticket LeadelMarche ").append(receipt.getSaleId()).append('\n');
        mail.append('\n').append(receipt.getTextBody());
        return writeEventFile("outbox_mails", receipt.getSaleId(), mail.toString());
    }

    public Receipt findBySaleId(String saleId) {
        return receiptRepository.findBySaleId(saleId).orElseThrow(
            () -> new IllegalStateException("Ticket introuvable pour la vente: " + saleId)
        );
    }

    private String buildTextBody(Sale sale, List<SaleLine> lines, Customer customer, PromotionComputation promotions) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== LEADELMARCHE ===\n");
        sb.append("Ticket: ").append(sale.getSaleNumber()).append('\n');
        sb.append("Date: ").append(sale.getSoldAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append('\n');
        sb.append("Caissier: ").append(sale.getCashierBadge()).append('\n');
        sb.append("Client: ").append(customer == null ? "N/A" : customer.fullName()).append('\n');
        sb.append("--------------------------------------\n");
        for (SaleLine line : lines) {
            sb.append(line.getProductName()).append(" x").append(line.getQuantity()).append('\n');
            sb.append("  PU HT: ").append(line.getUnitPriceHT()).append("  TTC ligne: ").append(line.getLineTotal()).append('\n');
        }
        sb.append("--------------------------------------\n");
        sb.append("Sous-total HT: ").append(sale.getSubTotalHT()).append('\n');
        sb.append("TVA: ").append(sale.getTotalVat()).append('\n');
        sb.append("Remise: ").append(promotions.getImmediateDiscount()).append('\n');
        sb.append("TOTAL TTC: ").append(sale.getTotalTTC()).append('\n');
        if (promotions.getLoyaltyCredit().signum() > 0) {
            sb.append("Credite fidelite: ").append(promotions.getLoyaltyCredit()).append('\n');
        }
        if (!promotions.getAppliedPromotions().isEmpty()) {
            sb.append("Promotions: ").append(String.join(", ", promotions.getAppliedPromotions())).append('\n');
        }
        sb.append("==============================\n");
        return sb.toString();
    }

    private void writeTicketFile(String saleNumber, String text) {
        Path ticketsDir = textFileDatabase.getBasePath().resolve("tickets");
        Path ticketFile = ticketsDir.resolve(saleNumber + ".txt");
        try {
            Files.createDirectories(ticketsDir);
            Files.writeString(ticketFile, text, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to write ticket file: " + ticketFile, ex);
        }
    }

    private String writeEventFile(String folder, String saleId, String content) {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Path dir = textFileDatabase.getBasePath().resolve(folder);
        Path eventFile = dir.resolve(saleId + "_" + stamp + ".txt");
        try {
            Files.createDirectories(dir);
            Files.writeString(eventFile, content, StandardCharsets.UTF_8);
            return eventFile.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to write event file: " + eventFile, ex);
        }
    }
}
