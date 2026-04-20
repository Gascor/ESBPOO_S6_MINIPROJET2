package com.leadelmarche.ui.mvc.controller;

import com.leadelmarche.service.MailOutboxService;
import com.leadelmarche.service.StatisticsService;
import com.leadelmarche.ui.mvc.view.StatsView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.JOptionPane;

public class StatsController {
    private final StatisticsService statisticsService;
    private final MailOutboxService mailOutboxService;
    private final StatsView view;
    // Garde la derniere version calculée pour l'envoyer telle quelle par mail.
    private String lastReportText = "";

    public StatsController(StatisticsService statisticsService, MailOutboxService mailOutboxService) {
        this.statisticsService = statisticsService;
        this.mailOutboxService = mailOutboxService;
        this.view = new StatsView();
        bindActions();
        refreshStatisticList(null);
        view.setComparisonFieldsEnabled(view.compareCheckBox().isSelected());
        computeStatistic();
    }

    public void show() {
        view.setVisible(true);
    }

    private void bindActions() {
        view.computeButton().addActionListener(e -> computeStatistic());
        view.compareCheckBox().addActionListener(
            e -> view.setComparisonFieldsEnabled(view.compareCheckBox().isSelected())
        );
        view.addCustomStatButton().addActionListener(e -> addCustomStatistic());
        view.emailReportButton().addActionListener(e -> emailCurrentReport());
        view.addAbsenceButton().addActionListener(e -> registerAbsence());
    }

    private void refreshStatisticList(String preferredStatId) {
        StatisticsService.StatisticOption current = (StatisticsService.StatisticOption) view.statisticCombo().getSelectedItem();
        String targetId = preferredStatId != null
            ? preferredStatId
            : (current == null ? null : current.getId());
        List<StatisticsService.StatisticOption> options = statisticsService.listAvailableStats();
        view.statisticCombo().removeAllItems();
        for (StatisticsService.StatisticOption option : options) {
            view.statisticCombo().addItem(option);
        }
        if (targetId != null) {
            for (int i = 0; i < view.statisticCombo().getItemCount(); i++) {
                StatisticsService.StatisticOption option = view.statisticCombo().getItemAt(i);
                if (option.getId().equals(targetId)) {
                    view.statisticCombo().setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void computeStatistic() {
        try {
            StatisticsService.StatisticOption selected =
                (StatisticsService.StatisticOption) view.statisticCombo().getSelectedItem();
            if (selected == null) {
                view.outputArea().setText("Aucune statistique disponible.");
                return;
            }
            LocalDate startA = parseDate(view.periodStartField().getText(), "Debut periode A");
            LocalDate endA = parseDate(view.periodEndField().getText(), "Fin periode A");
            String displayMode = view.displayModeCombo().getSelectedItem().toString();
            if (!view.compareCheckBox().isSelected()) {
                BigDecimal value = statisticsService.compute(selected.getId(), startA, endA);
                lastReportText = renderSingle(selected, startA, endA, value, displayMode);
                view.outputArea().setText(lastReportText);
                return;
            }
            LocalDate startB = parseDate(view.compareStartField().getText(), "Debut periode B");
            LocalDate endB = parseDate(view.compareEndField().getText(), "Fin periode B");
            StatisticsService.ComparisonResult result = statisticsService.compare(
                selected.getId(),
                startA,
                endA,
                startB,
                endB
            );
            lastReportText = renderComparison(result, startA, endA, startB, endB, displayMode);
            view.outputArea().setText(lastReportText);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCustomStatistic() {
        try {
            String name = view.customStatNameField().getText().trim();
            String productToken = view.customProductFilterField().getText().trim();
            StatisticsService.StatisticOption created = statisticsService.registerProductSalesStatistic(name, productToken);
            refreshStatisticList(created.getId());
            view.outputArea().append("\nNouvelle statistique ajoutee: " + created.getLabel() + '\n');
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void emailCurrentReport() {
        try {
            if (view.outputArea().getText() == null || view.outputArea().getText().isBlank()) {
                computeStatistic();
            }
            String to = view.reportEmailField().getText().trim();
            if (to.isBlank()) {
                throw new IllegalArgumentException("Email destinataire obligatoire");
            }
            StatisticsService.StatisticOption selected =
                (StatisticsService.StatisticOption) view.statisticCombo().getSelectedItem();
            String title = selected == null
                ? "Statistiques LeadelMarche"
                : "Statistique LeadelMarche - " + selected.getLabel();
            // Si l'utilisateur a retouché la zone resultat, on envoie exactement ce qu'il voit a l'ecran.
            String report = view.outputArea().getText().isBlank() ? lastReportText : view.outputArea().getText();
            String path = mailOutboxService.sendMail(to, title, report);
            JOptionPane.showMessageDialog(view, "Statistique envoyee (outbox): " + path, "Email", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerAbsence() {
        try {
            String badge = view.absenceBadgeField().getText().trim();
            LocalDate date = parseDate(view.absenceDateField().getText(), "Date absence");
            String type = view.absenceTypeCombo().getSelectedItem().toString();
            String note = view.absenceNoteField().getText().trim();
            statisticsService.recordAbsence(badge, date, type, note);
            view.outputArea().append("Absence enregistree pour " + badge + " le " + date + " (" + type + ")\n");
            computeStatistic();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String renderSingle(
        StatisticsService.StatisticOption option,
        LocalDate start,
        LocalDate end,
        BigDecimal value,
        String displayMode
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Statistique: ").append(option.getLabel()).append('\n');
        sb.append("Periode A: ").append(start).append(" -> ").append(end).append('\n');
        sb.append("Mode: ").append(displayMode).append("\n\n");
        if ("BARRES".equals(displayMode)) {
            BigDecimal max = value.max(BigDecimal.ONE);
            sb.append("A | ").append(buildBar(value, max)).append(" ").append(formatValue(value)).append(' ').append(option.getUnit()).append('\n');
            return sb.toString();
        }
        if ("CAMEMBERT".equals(displayMode)) {
            sb.append("A | ").append(buildPieSlice(BigDecimal.ONE)).append(" 100.0%\n");
            sb.append("Valeur: ").append(formatValue(value)).append(' ').append(option.getUnit()).append('\n');
            return sb.toString();
        }
        sb.append("Valeur: ").append(formatValue(value)).append(' ').append(option.getUnit()).append('\n');
        return sb.toString();
    }

    private String renderComparison(
        StatisticsService.ComparisonResult result,
        LocalDate startA,
        LocalDate endA,
        LocalDate startB,
        LocalDate endB,
        String displayMode
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Statistique: ").append(result.getOption().getLabel()).append('\n');
        sb.append("Periode A: ").append(startA).append(" -> ").append(endA).append('\n');
        sb.append("Periode B: ").append(startB).append(" -> ").append(endB).append('\n');
        sb.append("Mode: ").append(displayMode).append("\n\n");
        if ("BARRES".equals(displayMode)) {
            BigDecimal max = result.getPeriodA().max(result.getPeriodB()).max(BigDecimal.ONE);
            sb.append("A | ").append(buildBar(result.getPeriodA(), max)).append(" ").append(formatValue(result.getPeriodA())).append('\n');
            sb.append("B | ").append(buildBar(result.getPeriodB(), max)).append(" ").append(formatValue(result.getPeriodB())).append('\n');
        } else if ("CAMEMBERT".equals(displayMode)) {
            BigDecimal total = result.getPeriodA().add(result.getPeriodB());
            BigDecimal ratioA = total.signum() == 0
                ? BigDecimal.ZERO
                : result.getPeriodA().divide(total, 3, RoundingMode.HALF_UP);
            BigDecimal ratioB = total.signum() == 0
                ? BigDecimal.ZERO
                : result.getPeriodB().divide(total, 3, RoundingMode.HALF_UP);
            sb.append("A | ").append(buildPieSlice(ratioA)).append(" ").append(formatPercent(ratioA)).append("%\n");
            sb.append("B | ").append(buildPieSlice(ratioB)).append(" ").append(formatPercent(ratioB)).append("%\n");
        } else {
            sb.append("A: ").append(formatValue(result.getPeriodA())).append(' ').append(result.getOption().getUnit()).append('\n');
            sb.append("B: ").append(formatValue(result.getPeriodB())).append(' ').append(result.getOption().getUnit()).append('\n');
        }
        sb.append('\n');
        sb.append("Delta (A-B): ").append(formatValue(result.getDeltaAbsolute())).append(' ').append(result.getOption().getUnit()).append('\n');
        sb.append("Delta %: ").append(formatValue(result.getDeltaPercent())).append("%\n");
        return sb.toString();
    }

    private String buildBar(BigDecimal value, BigDecimal max) {
        int width = 28;
        if (max.signum() <= 0) {
            return "[]";
        }
        BigDecimal ratio = value.max(BigDecimal.ZERO).divide(max, 4, RoundingMode.HALF_UP);
        int fill = ratio.multiply(BigDecimal.valueOf(width)).intValue();
        if (fill < 0) {
            fill = 0;
        }
        if (fill > width) {
            fill = width;
        }
        return "[" + "#".repeat(fill) + " ".repeat(width - fill) + "]";
    }

    private String buildPieSlice(BigDecimal ratio) {
        int width = 20;
        int fill = ratio.max(BigDecimal.ZERO).multiply(BigDecimal.valueOf(width)).intValue();
        if (fill < 0) {
            fill = 0;
        }
        if (fill > width) {
            fill = width;
        }
        return "(" + "o".repeat(fill) + ".".repeat(width - fill) + ")";
    }

    private String formatValue(BigDecimal value) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        BigDecimal normalized = safe.stripTrailingZeros();
        return normalized.scale() < 0 ? normalized.setScale(0).toPlainString() : normalized.toPlainString();
    }

    private String formatPercent(BigDecimal ratio) {
        return ratio.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    private LocalDate parseDate(String raw, String fieldLabel) {
        try {
            LocalDate date = LocalDate.parse(raw.trim());
            return date;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldLabel + " invalide (attendu YYYY-MM-DD)");
        }
    }
}
