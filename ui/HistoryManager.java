package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * pomocná třída uchovávající historii změn obrázku a iterovaní mezi změnami
 */
public class HistoryManager {
    private final List<BufferedImage> history = new ArrayList<>();
    private int index = -1;

    public void clear() {
        history.clear();
        index = -1;
    }

    /**
     * reset historie na jeden počáteční snímek-typicky po openImage
     * */
    public void reset(BufferedImage img) {
        clear();
        push(img);
    }

    /**
     * Přidá nový stav a zahodí "redo" větev.
     * */
    public void push(BufferedImage img) {
        if (img == null) return;

        // zahod redo (vše za indexem)
        while (history.size() > index + 1) {
            history.removeLast();
        }

        history.add(deepCopy(img));
        index++;
    }

    public boolean canUndo() {
        return index > 0;
    }

    public boolean canRedo() {
        return index >= 0 && index < history.size() - 1;
    }

    public BufferedImage undo() {
        if (!canUndo()) return null;
        index--;
        return deepCopy(history.get(index));
    }

    public BufferedImage redo() {
        if (!canRedo()) return null;
        index++;
        return deepCopy(history.get(index));
    }

    public BufferedImage current() {
        if (index < 0) return null;
        return deepCopy(history.get(index));
    }

    private BufferedImage deepCopy(BufferedImage img) {
        BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return copy;
    }
}
