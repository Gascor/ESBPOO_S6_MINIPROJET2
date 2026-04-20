package com.leadelmarche.ui.mvc.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class MiniCalculatorDialog extends JDialog {
    private final JTextField display = new JTextField();

    private MiniCalculatorDialog(JFrame owner) {
        super(owner, "Calculatrice", false);
        setSize(320, 420);
        setLocationRelativeTo(owner);

        display.setEditable(false);
        display.setHorizontalAlignment(JTextField.RIGHT);

        JPanel keypad = new JPanel(new GridLayout(5, 4, 6, 6));
        addKey(keypad, "7");
        addKey(keypad, "8");
        addKey(keypad, "9");
        addKey(keypad, "/");
        addKey(keypad, "4");
        addKey(keypad, "5");
        addKey(keypad, "6");
        addKey(keypad, "*");
        addKey(keypad, "1");
        addKey(keypad, "2");
        addKey(keypad, "3");
        addKey(keypad, "-");
        addKey(keypad, "0");
        addKey(keypad, ".");
        addKey(keypad, "C");
        addKey(keypad, "+");
        addKey(keypad, "DEL");
        addKey(keypad, "(");
        addKey(keypad, ")");
        addKey(keypad, "=");

        setLayout(new BorderLayout(6, 6));
        add(display, BorderLayout.NORTH);
        add(keypad, BorderLayout.CENTER);
    }

    public static void open(JFrame owner) {
        new MiniCalculatorDialog(owner).setVisible(true);
    }

    private void addKey(JPanel keypad, String text) {
        JButton button = new JButton(text);
        button.addActionListener(e -> onKey(text));
        keypad.add(button);
    }

    private void onKey(String key) {
        if ("C".equals(key)) {
            display.setText("");
            return;
        }
        if ("DEL".equals(key)) {
            String current = display.getText();
            if (!current.isEmpty()) {
                display.setText(current.substring(0, current.length() - 1));
            }
            return;
        }
        if ("=".equals(key)) {
            String expr = display.getText().trim();
            if (expr.isEmpty()) {
                return;
            }
            BigDecimal result = evaluate(expr);
            display.setText(format(result));
            return;
        }
        display.setText(display.getText() + key);
    }

    private BigDecimal evaluate(String expression) {
        // Petit parser "maison": 2 piles (valeurs + operateurs) pour gerer priorites et parentheses.
        List<BigDecimal> values = new ArrayList<>();
        List<Character> operators = new ArrayList<>();
        String expr = expression.replace(" ", "");
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (c == '(') {
                operators.add(c);
                i++;
                continue;
            }
            if (c == ')') {
                while (!operators.isEmpty() && operators.get(operators.size() - 1) != '(') {
                    applyLast(values, operators);
                }
                if (operators.isEmpty() || operators.get(operators.size() - 1) != '(') {
                    throw new IllegalArgumentException("Expression invalide");
                }
                operators.remove(operators.size() - 1);
                i++;
                continue;
            }
            if (isOperator(c)) {
                // Gere le cas "-12" ou "(-3.5)" comme un nombre negatif et non une soustraction binaire.
                if (c == '-' && isUnaryMinus(expr, i)) {
                    int next = i + 1;
                    while (next < expr.length() && (Character.isDigit(expr.charAt(next)) || expr.charAt(next) == '.')) {
                        next++;
                    }
                    if (next == i + 1) {
                        throw new IllegalArgumentException("Nombre invalide");
                    }
                    values.add(new BigDecimal(expr.substring(i, next)));
                    i = next;
                    continue;
                }
                while (!operators.isEmpty() && precedence(operators.get(operators.size() - 1)) >= precedence(c)) {
                    applyLast(values, operators);
                }
                operators.add(c);
                i++;
                continue;
            }
            if (Character.isDigit(c) || c == '.') {
                int next = i;
                while (next < expr.length() && (Character.isDigit(expr.charAt(next)) || expr.charAt(next) == '.')) {
                    next++;
                }
                values.add(new BigDecimal(expr.substring(i, next)));
                i = next;
                continue;
            }
            throw new IllegalArgumentException("Caractere invalide: " + c);
        }
        while (!operators.isEmpty()) {
            applyLast(values, operators);
        }
        if (values.size() != 1) {
            throw new IllegalArgumentException("Expression invalide");
        }
        return values.get(0);
    }

    private boolean isUnaryMinus(String expr, int index) {
        if (index == 0) {
            return true;
        }
        char previous = expr.charAt(index - 1);
        return isOperator(previous) || previous == '(';
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private int precedence(char op) {
        if (op == '+' || op == '-') {
            return 1;
        }
        if (op == '*' || op == '/') {
            return 2;
        }
        return 0;
    }

    private void applyLast(List<BigDecimal> values, List<Character> operators) {
        if (operators.isEmpty() || values.size() < 2) {
            throw new IllegalArgumentException("Expression invalide");
        }
        // Applique toujours le dernier operateur pour respecter l'ordre de calcul construit plus haut.
        char op = operators.remove(operators.size() - 1);
        BigDecimal right = values.remove(values.size() - 1);
        BigDecimal left = values.remove(values.size() - 1);
        BigDecimal result;
        switch (op) {
            case '+' -> result = left.add(right);
            case '-' -> result = left.subtract(right);
            case '*' -> result = left.multiply(right);
            case '/' -> {
                if (right.signum() == 0) {
                    throw new IllegalArgumentException("Division par zero");
                }
                result = left.divide(right, 6, RoundingMode.HALF_UP);
            }
            default -> throw new IllegalArgumentException("Operateur invalide");
        }
        values.add(result);
    }

    private String format(BigDecimal value) {
        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() < 0) {
            normalized = normalized.setScale(0, RoundingMode.HALF_UP);
        }
        return normalized.toPlainString();
    }
}
