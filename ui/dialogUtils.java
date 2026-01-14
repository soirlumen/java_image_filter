package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import image.*;

/**
 * třída pro vykreslení dialogových okýnek
 * slouží pro získání parametrů filtru od uživatele
 */
public class dialogUtils {
    private final MainFrame parent;

    public dialogUtils(MainFrame par) {
        this.parent = par;
    }

    public BrightnessParams getBrightnessParams() {
        BufferedImage current;
        current = parent.imagePanel.getBufferedImage();
        if (current == null) {
            JOptionPane.showMessageDialog(parent, "Nejdřív otevři obrázek");
            return null;
        }

        JSlider slider = new JSlider(-100, 100, 0);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        JLabel valueLabel = new JLabel("0 %", SwingConstants.CENTER);

        slider.addChangeListener(e -> {
            int val = slider.getValue();
            valueLabel.setText(val + " %");
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.add(new JLabel("Úprava jasu (Brightness)", SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Brightness", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return null;

        int amount = slider.getValue();
        if (amount == 0) return null;

        return new BrightnessParams(amount);
    }

    public GaussianBlurParams getGaussianBlurParams() {
        BufferedImage current = parent.imagePanel.getBufferedImage();
        if (current == null) {
            JOptionPane.showMessageDialog(parent, "Nejdřív otevři obrázek, bráško!");
            return null;
        }

        //kernel slider-jen liché hodnoty slideru
        JSlider kernelSlider = new JSlider(3, 15, 5);
        kernelSlider.setMajorTickSpacing(2);
        kernelSlider.setMinorTickSpacing(2);
        kernelSlider.setPaintTicks(true);
        kernelSlider.setPaintLabels(true);
        kernelSlider.setSnapToTicks(true);

        // sigma slider
        JSlider sigmaSlider = new JSlider(50, 500, 140);
        sigmaSlider.setMajorTickSpacing(100);
        sigmaSlider.setMinorTickSpacing(50);
        sigmaSlider.setPaintTicks(true);
        sigmaSlider.setPaintLabels(false);

        JLabel kernelLabel = new JLabel("", SwingConstants.CENTER);
        JLabel sigmaLabel = new JLabel("", SwingConstants.CENTER);

        kernelSlider.addChangeListener(e -> {
            int k = kernelSlider.getValue();
            kernelLabel.setText("Velikost kernelu: " + k + "×" + k);
        });

        sigmaSlider.addChangeListener(e -> {
            double sigma = sigmaSlider.getValue() / 100.0;
            sigmaLabel.setText(String.format("Sigma: %.2f", sigma));
        });

        // inicializace textů
        kernelSlider.getChangeListeners()[0].stateChanged(null);
        sigmaSlider.getChangeListeners()[0].stateChanged(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Gaussian Blur", SwingConstants.CENTER));
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Velikost kernelu:"));
        panel.add(kernelSlider);
        panel.add(kernelLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(new JLabel("Intenzita rozostření (sigma):"));
        panel.add(sigmaSlider);
        panel.add(sigmaLabel);
        panel.add(Box.createVerticalStrut(10));

        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                "Gaussian Blur",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        int kernelSize = kernelSlider.getValue();
        double sigma = sigmaSlider.getValue() / 100.0;

        return new GaussianBlurParams(kernelSize, sigma);
    }

    public ScaleParams getScaleParams() {
        BufferedImage current = parent.imagePanel.getBufferedImage();
        if (current == null) {
            JOptionPane.showMessageDialog(parent, "Nejdřív otevři obrázek, bráško!");
            return null;
        }

        JSlider slider = new JSlider(10, 500, 100);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        JLabel label = new JLabel("100 %", SwingConstants.CENTER);
        JLabel previewLabel = new JLabel(String.format("Nová velikost: %d × %d px", current.getWidth(), current.getHeight()), SwingConstants.CENTER
        );

        slider.addChangeListener(e -> {
            int percent = slider.getValue();
            label.setText(percent + " %");

            int newW = (int) (current.getWidth() * percent / 100.0);
            int newH = (int) (current.getHeight() * percent / 100.0);
            previewLabel.setText(String.format("Nová velikost: %d × %d px", newW, newH));
        });

        //při otevření dialogu ihned aktualizuje listener slideru
        slider.getChangeListeners()[0].stateChanged(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Zvětšit / zmenšit obrázek", SwingConstants.CENTER));
        panel.add(Box.createVerticalStrut(15));
        panel.add(slider);
        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        panel.add(previewLabel);

        int result = JOptionPane.showConfirmDialog(
                parent, panel, "Změna velikosti", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return null;

        int percent = slider.getValue();
        if (percent == 100) return null;

        return new ScaleParams(percent);
    }

    public static ChooseGrayscale showGrayscaleDialog(Component parent) {
        ChooseGrayscale[] methods = {ChooseGrayscale.average, ChooseGrayscale.luminance_bt709, ChooseGrayscale.luma_bt601,};

        String[] labels = {
                "Mean value",
                "Luminance BT.709",
                "Luma BT.601",
        };

        JComboBox<String> combo = new JComboBox<>(labels);
        combo.setSelectedIndex(0);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(new JLabel("Vyber způsob převodu na šedou:"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(combo);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Grayscale", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return methods[combo.getSelectedIndex()];
        }
        return null;
    }
}