package com.leadelmarche.ui.mvc.view;

import com.leadelmarche.service.StatisticsService;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class StatsView extends JFrame {
    private final JComboBox<StatisticsService.StatisticOption> statisticCombo = new JComboBox<>();
    private final JComboBox<String> displayModeCombo = new JComboBox<>(new String[]{"TABLEAU", "BARRES", "CAMEMBERT"});
    private final JTextField periodStartField = new JTextField(LocalDate.now().minusDays(30).toString());
    private final JTextField periodEndField = new JTextField(LocalDate.now().toString());
    private final JCheckBox compareCheckBox = new JCheckBox("Comparer A et B");
    private final JTextField compareStartField = new JTextField(LocalDate.now().minusDays(60).toString());
    private final JTextField compareEndField = new JTextField(LocalDate.now().minusDays(31).toString());
    private final JButton computeButton = new JButton("Calculer");

    private final JComboBox<String> customStatTypeCombo = new JComboBox<>(new String[]{"PRODUIT", "PROMOTION"});
    private final JTextField customStatNameField = new JTextField();
    private final JTextField customFilterField = new JTextField();
    private final JButton addCustomStatButton = new JButton("Ajouter statistique");

    private final JTextField reportEmailField = new JTextField();
    private final JButton emailReportButton = new JButton("Envoyer par mail");
    private final JLabel resultHeaderLabel = new JLabel("Resultat: -");
    private final JTextArea outputArea = new JTextArea();
    private final StatsChartPanel chartPanel = new StatsChartPanel();

    public StatsView() {
        super("LeadelMarche - Statistiques");
        setSize(1200, 980);
        setLocationRelativeTo(null);
        Branding.applyWindowIcon(this);

        JPanel analysisPanel = new JPanel(new GridLayout(0, 4, 8, 8));
        analysisPanel.setBorder(BorderFactory.createTitledBorder("1) Analyse"));
        analysisPanel.add(new JLabel("Statistique"));
        analysisPanel.add(statisticCombo);
        analysisPanel.add(new JLabel("Affichage"));
        analysisPanel.add(displayModeCombo);
        analysisPanel.add(new JLabel("Periode A debut"));
        analysisPanel.add(periodStartField);
        analysisPanel.add(new JLabel("Periode A fin"));
        analysisPanel.add(periodEndField);
        analysisPanel.add(compareCheckBox);
        analysisPanel.add(computeButton);
        analysisPanel.add(new JLabel("Periode B debut"));
        analysisPanel.add(compareStartField);
        analysisPanel.add(new JLabel("Periode B fin"));
        analysisPanel.add(compareEndField);
        analysisPanel.add(new JLabel(""));
        analysisPanel.add(new JLabel(""));

        JPanel customStatPanel = new JPanel(new GridLayout(0, 4, 8, 8));
        customStatPanel.setBorder(BorderFactory.createTitledBorder("2) Enrichir la liste"));
        customStatPanel.add(new JLabel("Type"));
        customStatPanel.add(customStatTypeCombo);
        customStatPanel.add(new JLabel("Nom"));
        customStatPanel.add(customStatNameField);
        customStatPanel.add(new JLabel("Filtre (nom produit ou promo)"));
        customStatPanel.add(customFilterField);
        customStatPanel.add(new JLabel(""));
        customStatPanel.add(addCustomStatButton);

        JPanel sharePanel = new JPanel(new GridLayout(1, 4, 8, 8));
        sharePanel.setBorder(BorderFactory.createTitledBorder("3) Partage"));
        sharePanel.add(new JLabel("Email destinataire"));
        sharePanel.add(reportEmailField);
        sharePanel.add(emailReportButton);
        sharePanel.add(resultHeaderLabel);

        JPanel topPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        topPanel.add(analysisPanel);
        topPanel.add(customStatPanel);
        topPanel.add(sharePanel);

        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        chartPanel.setPreferredSize(new Dimension(420, 300));

        JPanel reportPanel = new JPanel(new BorderLayout());
        reportPanel.setBorder(BorderFactory.createTitledBorder("Rapport"));
        reportPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.setBorder(BorderFactory.createTitledBorder("Graphique"));
        graphPanel.add(chartPanel, BorderLayout.CENTER);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, reportPanel, graphPanel);
        centerSplit.setResizeWeight(0.62);
        centerSplit.setDividerLocation(720);

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.add(topPanel, BorderLayout.NORTH);
        content.add(centerSplit, BorderLayout.CENTER);

        add(
            Branding.createHeader(
                "Module Statistiques",
                "Aide - Statistiques",
                "1) Choisir une statistique et une periode A puis Calculer.\n"
                    + "2) Activer Comparer A et B pour soldes A vs A-1.\n"
                    + "3) Choisir l'affichage: tableau, barres, camembert.\n"
                    + "4) Ajouter une statistique personnalisee (produit/promotion).\n"
                    + "5) Envoyer le rapport par mail via l'outbox."
            ),
            BorderLayout.NORTH
        );
        add(content, BorderLayout.CENTER);
    }

    public JComboBox<StatisticsService.StatisticOption> statisticCombo() {
        return statisticCombo;
    }

    public JComboBox<String> displayModeCombo() {
        return displayModeCombo;
    }

    public JTextField periodStartField() {
        return periodStartField;
    }

    public JTextField periodEndField() {
        return periodEndField;
    }

    public JCheckBox compareCheckBox() {
        return compareCheckBox;
    }

    public JTextField compareStartField() {
        return compareStartField;
    }

    public JTextField compareEndField() {
        return compareEndField;
    }

    public JButton computeButton() {
        return computeButton;
    }

    public JComboBox<String> customStatTypeCombo() {
        return customStatTypeCombo;
    }

    public JTextField customStatNameField() {
        return customStatNameField;
    }

    public JTextField customFilterField() {
        return customFilterField;
    }

    public JButton addCustomStatButton() {
        return addCustomStatButton;
    }

    public JTextField reportEmailField() {
        return reportEmailField;
    }

    public JButton emailReportButton() {
        return emailReportButton;
    }

    public JTextArea outputArea() {
        return outputArea;
    }

    public void setComparisonFieldsEnabled(boolean enabled) {
        compareStartField.setEnabled(enabled);
        compareEndField.setEnabled(enabled);
    }

    public void setResultHeader(String text) {
        resultHeaderLabel.setText(text == null || text.isBlank() ? "Resultat: -" : text);
    }

    public void updateChart(String mode, String labelA, BigDecimal valueA, String labelB, BigDecimal valueB, String unit) {
        chartPanel.setData(mode, labelA, valueA, labelB, valueB, unit);
    }

    private static final class StatsChartPanel extends JPanel {
        private String mode = "TABLEAU";
        private String labelA = "A";
        private BigDecimal valueA = BigDecimal.ZERO;
        private String labelB = "";
        private BigDecimal valueB = null;
        private String unit = "";

        private void setData(String mode, String labelA, BigDecimal valueA, String labelB, BigDecimal valueB, String unit) {
            this.mode = mode == null ? "TABLEAU" : mode;
            this.labelA = labelA == null ? "A" : labelA;
            this.valueA = valueA == null ? BigDecimal.ZERO : valueA;
            this.labelB = labelB == null ? "" : labelB;
            this.valueB = valueB;
            this.unit = unit == null ? "" : unit;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int left = 50;
            int right = 20;
            int top = 30;
            int bottom = 50;
            int width = Math.max(1, getWidth() - left - right);
            int height = Math.max(1, getHeight() - top - bottom);

            g.setColor(new Color(245, 247, 250));
            g.fillRect(left, top, width, height);
            g.setColor(new Color(210, 216, 224));
            g.drawRect(left, top, width, height);

            if ("BARRES".equals(mode)) {
                drawBars(g, left, top, width, height);
            } else if ("CAMEMBERT".equals(mode)) {
                drawPie(g, left, top, width, height);
            } else {
                drawTableHint(g, left, top, width, height);
            }

            g.dispose();
        }

        private void drawBars(Graphics2D g, int left, int top, int width, int height) {
            BigDecimal b = valueB == null ? BigDecimal.ZERO : valueB;
            BigDecimal max = valueA.max(b).max(BigDecimal.ONE);
            int axisY = top + height - 20;
            int barMaxH = Math.max(10, height - 40);
            int barW = Math.max(24, width / 6);
            int gap = Math.max(30, width / 8);
            int xA = left + width / 3 - barW;
            int xB = left + width / 3 + gap;

            g.setColor(new Color(70, 70, 70));
            g.drawLine(left + 15, axisY, left + width - 15, axisY);

            int hA = valueA.max(BigDecimal.ZERO).multiply(BigDecimal.valueOf(barMaxH)).divide(max, 4, RoundingMode.HALF_UP).intValue();
            g.setColor(new Color(52, 129, 77));
            g.fillRect(xA, axisY - hA, barW, hA);
            g.setColor(new Color(45, 45, 45));
            g.drawString(labelA, xA, axisY + 15);
            g.drawString(valueA.stripTrailingZeros().toPlainString() + " " + unit, xA - 5, axisY - hA - 8);

            if (valueB != null) {
                int hB = b.max(BigDecimal.ZERO).multiply(BigDecimal.valueOf(barMaxH)).divide(max, 4, RoundingMode.HALF_UP).intValue();
                g.setColor(new Color(59, 107, 173));
                g.fillRect(xB, axisY - hB, barW, hB);
                g.setColor(new Color(45, 45, 45));
                g.drawString(labelB, xB, axisY + 15);
                g.drawString(b.stripTrailingZeros().toPlainString() + " " + unit, xB - 5, axisY - hB - 8);
            }
        }

        private void drawPie(Graphics2D g, int left, int top, int width, int height) {
            int size = Math.min(width - 30, height - 30);
            int x = left + (width - size) / 2;
            int y = top + (height - size) / 2;

            if (valueB == null) {
                g.setColor(new Color(52, 129, 77));
                g.fillArc(x, y, size, size, 0, 360);
                g.setColor(Color.WHITE);
                g.drawString("100%", x + size / 2 - 18, y + size / 2 + 4);
                g.setColor(new Color(45, 45, 45));
                g.drawString(labelA + ": " + valueA.stripTrailingZeros().toPlainString() + " " + unit, x, y + size + 18);
                return;
            }

            BigDecimal b = valueB.max(BigDecimal.ZERO);
            BigDecimal total = valueA.max(BigDecimal.ZERO).add(b);
            if (total.signum() == 0) {
                g.setColor(new Color(215, 220, 226));
                g.fillArc(x, y, size, size, 0, 360);
                g.setColor(new Color(45, 45, 45));
                g.drawString("Aucune valeur", x + size / 2 - 28, y + size / 2 + 4);
                return;
            }
            int angleA = valueA.max(BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(360))
                .divide(total, 0, RoundingMode.HALF_UP)
                .intValue();
            g.setColor(new Color(52, 129, 77));
            g.fillArc(x, y, size, size, 0, angleA);
            g.setColor(new Color(59, 107, 173));
            g.fillArc(x, y, size, size, angleA, 360 - angleA);
            g.setColor(new Color(45, 45, 45));
            g.setStroke(new BasicStroke(1.2f));
            g.drawOval(x, y, size, size);
            g.drawString(labelA + ": " + valueA.stripTrailingZeros().toPlainString() + " " + unit, x, y + size + 18);
            g.drawString(labelB + ": " + b.stripTrailingZeros().toPlainString() + " " + unit, x + size / 2, y + size + 18);
        }

        private void drawTableHint(Graphics2D g, int left, int top, int width, int height) {
            g.setColor(new Color(45, 45, 45));
            g.drawString("Mode tableau: details dans le rapport texte", left + 20, top + 30);
            g.drawString(labelA + " = " + valueA.stripTrailingZeros().toPlainString() + " " + unit, left + 20, top + 55);
            if (valueB != null) {
                g.drawString(labelB + " = " + valueB.stripTrailingZeros().toPlainString() + " " + unit, left + 20, top + 80);
            }
        }
    }
}
