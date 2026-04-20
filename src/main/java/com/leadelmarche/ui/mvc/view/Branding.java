package com.leadelmarche.ui.mvc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class Branding {
    private static final String LOGO_PATH = "src/img/LeadelMarche.png";
    private static final int HEADER_LOGO_MAX_WIDTH = 260;
    private static final int HEADER_LOGO_MAX_HEIGHT = 104;
    private static final int WINDOW_ICON_SIZE = 16;
    private static final Image LOGO_IMAGE = loadLogoImage();

    private Branding() {
    }

    public static JPanel createHeader(String title) {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
            )
        );

        JLabel logoLabel = new JLabel();
        ImageIcon icon = createScaledIconPreserveAspect(HEADER_LOGO_MAX_WIDTH, HEADER_LOGO_MAX_HEIGHT);
        if (icon != null) {
            logoLabel.setIcon(icon);
        } else {
            logoLabel.setText("LM");
            logoLabel.setFont(logoLabel.getFont().deriveFont(Font.BOLD, 18f));
        }
        header.add(logoLabel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        header.add(titleLabel, BorderLayout.CENTER);
        return header;
    }

    public static void applyWindowIcon(JFrame frame) {
        if (frame == null || LOGO_IMAGE == null) {
            return;
        }
        ImageIcon icon = createScaledIconPreserveAspect(WINDOW_ICON_SIZE, WINDOW_ICON_SIZE);
        if (icon != null) {
            frame.setIconImage(icon.getImage());
        }
    }

    private static ImageIcon createScaledIconPreserveAspect(int maxWidth, int maxHeight) {
        if (LOGO_IMAGE == null) {
            return null;
        }
        int originalWidth = LOGO_IMAGE.getWidth(null);
        int originalHeight = LOGO_IMAGE.getHeight(null);
        if (originalWidth <= 0 || originalHeight <= 0 || maxWidth <= 0 || maxHeight <= 0) {
            return null;
        }
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double scale = Math.min(widthRatio, heightRatio);
        int scaledWidth = Math.max(1, (int) Math.round(originalWidth * scale));
        int scaledHeight = Math.max(1, (int) Math.round(originalHeight * scale));
        Image scaled = LOGO_IMAGE.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static Image loadLogoImage() {
        try {
            File file = new File(LOGO_PATH);
            if (!file.exists()) {
                return null;
            }
            return new ImageIcon(file.getAbsolutePath()).getImage();
        } catch (Exception ex) {
            return null;
        }
    }
}
