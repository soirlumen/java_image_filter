package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

/**
 * class na vykreslení, pohyb a zoom panelu s nahrátým obrázkem
 */
public class ImagePanel extends JPanel {
    private Image image;
    private double scale = 1.0;  // nové – aktuální zoom
    private int offsetX = 0;
    private int offsetY = 0;
    private int lastX;
    private int lastY;
    private boolean dragging = false;

    public ImagePanel() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragging = true;
                lastX = e.getX();
                lastY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!dragging) return;
                int dx = e.getX() - lastX;
                int dy = e.getY() - lastY;
                offsetX += dx;
                offsetY += dy;
                lastX = e.getX();
                lastY = e.getY();
                repaint();
            }
        });
    }

    public void setImage(Image img) {
        this.image = img;
        scale = 1.0;       // reset zoom při novém obrázku
        offsetX = 0;
        offsetY = 0;
        repaint();
    }

    public void updateImage(Image img) {
        this.image = img;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) {
            g.setColor(Color.WHITE);
            g.drawString("Otevři obrázek", 20, 50);
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();

        int panelW = getWidth();
        int panelH = getHeight();
        int imgW = image.getWidth(null);
        int imgH = image.getHeight(null);

        g2d.translate(panelW / 2.0, panelH / 2.0);

        g2d.translate(offsetX, offsetY);

        g2d.scale(scale, scale);

        g2d.drawImage(image, -imgW / 2, -imgH / 2, this);

        g2d.dispose();
    }

    public void resetImagePos() {
        offsetX = 0;
        offsetY = 0;
        scale = 1.0;
        repaint();
    }
    public void setScale(double newScale) {
        scale = Math.max(0.1, Math.min(10.0, newScale));
        repaint();
    }

    public BufferedImage getBufferedImage() {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();
        return bimage;
    }
}