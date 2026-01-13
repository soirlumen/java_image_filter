package ui;

import image.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.awt.Dimension;

/**
 * třída pro vykreslení hlavního okna, image panelu a napojení filtrů na tlačítka
 */
public class MainFrame extends JFrame {
    // vykreslení obrázku v hlavním okně
    final ImagePanel imagePanel;

    //velikost ikony v baru
    final static int iconSize = 18;

    // historie obrázků
    private final HistoryManager history = new HistoryManager();

    //toolbar, progressbar & tlačítka
    JToolBar toolbar;
    JSlider slider;
    JButton btnUndo;
    JButton btnForward;
    JButton btnOpenPic;
    JButton btnReset;
    JProgressBar progressBar;
    JButton btnZoom;
    JButton btnDezoom;

    // vlaječky
    boolean isImage = false;

    //menu & tlačítka
    JMenuBar bar;
    JMenu file;
    JMenuItem open;
    JMenuItem savePic;
    JMenuItem close;
    JMenu filters;
    JMenuItem gaussianItem;
    JMenuItem edge;
    JMenuItem invertcolor;
    JMenuItem brightnessItem;
    JMenuItem grayscale;
    JMenuItem sharpen;
    JMenu transform;
    JMenuItem scaleItem;
    JMenuItem rotateL;
    JMenuItem rotateR;
    JMenuItem flipH;
    JMenuItem flipV;
    JMenu help;
    JMenuItem about;
    JMenuItem docs;
    int WINDOW_WIDTH = 800;
    int WINDOW_HEIGHT = 600;

    private final java.util.List<JMenuItem> menuItemsToDisable = new ArrayList<>();

    /**
     * konstruktor
     * nastaví výšku, šířku, inicializace imagepanelu
     * volání funkcí pro render menu, status baru a tool baru
     */
    public MainFrame() {
        super("Aplikace na úpravu obrázků");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        //ikona aplikace
        try {
            Image icon = ImageIO.read(Objects.requireNonNull(getClass().getResource("icons/ico.png")));
            setIconImage(icon);
        } catch (Exception e) {
            System.err.println("Ikona aplikace se nepodařila načíst: " + e.getMessage());
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        imagePanel = new ImagePanel();
        JScrollPane scrolex = new JScrollPane(imagePanel);
        scrolex.getHorizontalScrollBar().setUnitIncrement(16);
        scrolex.getVerticalScrollBar().setUnitIncrement(16);
        this.getContentPane().add(scrolex, BorderLayout.CENTER);
        createMenu();
        createToolbar();
        createStatusBar();
        addToEnable();
        setUIEnabled(false);
        updateUndoRedoButtons();
        setVisible(true);
        progressBar.setVisible(false);
    }

    /**
     * Render menu včetně přiřazení action-listenerů
     */
    private void createMenu() {
        bar = new JMenuBar();

        file = new JMenu("File");
        open = new JMenuItem("Open");
        open.addActionListener(e -> openImage());
        file.add(open);

        savePic = new JMenuItem("Save picture as...");
        savePic.addActionListener(e -> saveImageAs());
        file.add(savePic);

        close = new JMenuItem("Close app");
        close.addActionListener(e -> dispose());
        file.add(close);

        bar.add(file);
        filters = new JMenu("Filters");
        gaussianItem = new JMenuItem("Gaussian Blur...");
        gaussianItem.addActionListener(e -> {
            GaussianBlurParams params = new dialogUtils(this).getGaussianBlurParams();
            if (params == null) return;

            runBackground("Chyba při gaussian bluru", (current, progress) -> {
                double[][] kernel = ImageProcessor.kernelGaussGenerator(params.radius() * 2 + 1, params.sigma());
                return ImageProcessor.applyKernel(current, kernel, progress);
            });
        });
        filters.add(gaussianItem);

        edge = new JMenuItem("Edge!");
        edge.addActionListener(e -> runBackground("Chyba při detekci hran", ImageProcessor::detectEdges));
        filters.add(edge);

        invertcolor = new JMenuItem("Invert colors!");
        invertcolor.addActionListener(e ->
                applySimple(ImageProcessor::invertColors, true)
        );

        filters.add(invertcolor);
        brightnessItem = new JMenuItem("Brightness...");
        brightnessItem.addActionListener(e -> {
            BrightnessParams params = new dialogUtils(this).getBrightnessParams();
            if (params == null) return;

            applySimple(img -> ImageProcessor.brightness(img, params.amount()), true);
        });
        filters.add(brightnessItem);

        grayscale = new JMenuItem("Grayscale...");
        grayscale.addActionListener(e -> {
            ChooseGrayscale method = dialogUtils.showGrayscaleDialog(this);
            if (method != null) {
                applySimple(img -> ImageProcessor.toGrayscaleUpgrade(img, method), false);
            }
        });

        filters.add(grayscale);

        sharpen = new JMenuItem("Sharpen");
        sharpen.addActionListener(e ->
                runBackground("Chyba při zostření hran", ImageProcessor::sharpen));
        filters.add(sharpen);

        bar.add(filters);

        transform = new JMenu("Transform");

        scaleItem = new JMenuItem("Zvětšit / zmenšit...");
        scaleItem.addActionListener(e -> {
            ScaleParams params = new dialogUtils(this).getScaleParams();
            if (params == null) return;

            applySimple(img -> {
                int newWidth = (int) (img.getWidth() * params.percentage() / 100.0);
                int newHeight = (int) (img.getHeight() * params.percentage() / 100.0);
                return ImageProcessor.resize(img, newWidth, newHeight);
            }, true);
        });
        transform.add(scaleItem);
        rotateL = new JMenuItem("Rotate left!");
        rotateL.addActionListener(e -> applySimple(ImageProcessor::rotateLeft, false));
        transform.add(rotateL);

        rotateR = new JMenuItem("Rotate right!");
        rotateR.addActionListener(e -> applySimple(ImageProcessor::rotateRight, false));
        transform.add(rotateR);

        flipH = new JMenuItem("Flip horizontal");
        flipH.addActionListener(e -> applySimple(ImageProcessor::flipH, false));
        transform.add(flipH);

        flipV = new JMenuItem("Flip vertical");
        flipV.addActionListener(e -> applySimple(ImageProcessor::flipV, false));
        transform.add(flipV);

        bar.add(transform);

        help = new JMenu("Help");
        about = new JMenuItem("About");
        about.addActionListener(e -> {
            String message = "Aplikace na úpravu obrázků\n" + "Verze 1.0\n" + "Autor: Mirka Novotná\n" + "2025–2026";

            JOptionPane.showMessageDialog(this, message, "O aplikaci", JOptionPane.INFORMATION_MESSAGE);
        });
        help.add(about);
        docs = new JMenuItem("Documenation...");
        docs.addActionListener(e -> OpenDocs());
        help.add(docs);
        bar.add(help);
        setJMenuBar(bar);
    }

    /**
     * Itemy menu, které nemají být přístupné během výpočtu
     */
    private void addToEnable() {
        menuItemsToDisable.add(gaussianItem);
        menuItemsToDisable.add(edge);
        menuItemsToDisable.add(invertcolor);
        menuItemsToDisable.add(brightnessItem);
        menuItemsToDisable.add(grayscale);
        menuItemsToDisable.add(sharpen);
        menuItemsToDisable.add(rotateL);
        menuItemsToDisable.add(rotateR);
        menuItemsToDisable.add(flipH);
        menuItemsToDisable.add(flipV);
        menuItemsToDisable.add(scaleItem);
        menuItemsToDisable.add(savePic);
    }

    /**
     * render toolbar, který lze vzít a mít jako plovoucí okénko
     */
    private void createToolbar() {
        toolbar = new JToolBar();
        btnOpenPic = new JButton();
        btnUndo = new JButton();
        btnForward = new JButton();
        btnReset = new JButton();
        try {
            Image icoOpenf = ImageIO.read(Objects.requireNonNull(getClass().getResource("icons/open_folder.png")));
            Image smallIcoOpenf = icoOpenf.getScaledInstance(iconSize, iconSize, Image.SCALE_DEFAULT);
            Image icoReset = ImageIO.read(Objects.requireNonNull(getClass().getResource("icons/reset.png")));
            Image smallIcoreset = icoReset.getScaledInstance(iconSize, iconSize, Image.SCALE_DEFAULT);
            Image icoUndo = ImageIO.read(Objects.requireNonNull(getClass().getResource("icons/undo.png")));
            Image smallIcoUndo = icoUndo.getScaledInstance(iconSize, iconSize, Image.SCALE_DEFAULT);
            Image icoForward = ImageIO.read(Objects.requireNonNull(getClass().getResource("icons/forward.png")));
            Image smallIcoForward = icoForward.getScaledInstance(iconSize, iconSize, Image.SCALE_DEFAULT);
            btnUndo.setIcon(new ImageIcon(smallIcoUndo));
            btnUndo.setToolTipText("Zpět - vrátit poslední úpravu");
            btnForward.setIcon(new ImageIcon(smallIcoForward));
            btnForward.setToolTipText("Vpřed - zopakovat vrácenou úpravu");
            btnOpenPic.setIcon(new ImageIcon(smallIcoOpenf));
            btnOpenPic.setToolTipText("Otevřít obrázek...");
            btnReset.setIcon(new ImageIcon(smallIcoreset));
            btnReset.setToolTipText("Resetovat zobrazení - přiblížení na 100%");
        } catch (Exception e) {
            System.out.println(e);
        }
        btnOpenPic.addActionListener(e -> openImage());
        btnReset.addActionListener(e -> imagePanel.resetImagePos());

        toolbar.add(btnOpenPic);
        toolbar.add(btnReset);
        toolbar.addSeparator();
        toolbar.add(btnUndo);
        btnUndo.addActionListener(e -> {
            BufferedImage prev = history.undo();
            if (prev != null) {
                imagePanel.setImage(prev);
                slider.setValue(100);
                updateUndoRedoButtons();
            } else {
                JOptionPane.showMessageDialog(this, "Nic k navrácení");
            }
        });

        toolbar.add(btnForward);
        btnForward.addActionListener(e -> {
            BufferedImage next = history.redo();
            if (next != null) {
                imagePanel.setImage(next);
                slider.setValue(100);
                updateUndoRedoButtons();
            } else {
                JOptionPane.showMessageDialog(this, "Nic k opakování");
            }
        });
        add(toolbar, BorderLayout.NORTH);
    }

    /**
     * render status bar, což je jen label v dolní části hlavního okna
     * zobrazuje tlačítka a slider pro zvětšení/zmenšení zobrazení a progress bar složitých operací
     */
    private void createStatusBar() {
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        this.add(statusPanel, BorderLayout.SOUTH);
        statusPanel.setPreferredSize(new Dimension(this.getWidth(), iconSize + 10));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        JLabel statusLabel = new JLabel("status");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        btnZoom = new JButton();
        btnDezoom = new JButton();
        slider = new JSlider();
        slider.setMinimum(10);
        slider.setMaximum(300);
        slider.setValue(100);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        progressBar = new JProgressBar();
        progressBar.setValue(0);

        slider.setPreferredSize(new Dimension(300, 30));
        slider.setMaximumSize(new Dimension(500, 40));

        progressBar.setPreferredSize(new Dimension(200, 25));
        progressBar.setMaximumSize(new Dimension(300, 40));

        slider.addChangeListener(e -> {
            int value = slider.getValue();
            double newScale = value / 100.0;
            imagePanel.setScale(newScale);
        });
        try {
            Image icoZoomer = ImageIO.read(Objects.requireNonNull(getClass().getResource("icons/zoom.png")));
            Image smallicoZoomer = icoZoomer.getScaledInstance(iconSize, iconSize, Image.SCALE_DEFAULT);
            Image icoDezoomer = ImageIO.read(Objects.requireNonNull(getClass().getResource("icons/dezoom.png")));
            Image smallicoDezoomer = icoDezoomer.getScaledInstance(iconSize, iconSize, Image.SCALE_DEFAULT);
            btnZoom.setIcon(new ImageIcon(smallicoZoomer));
            btnDezoom.setIcon(new ImageIcon(smallicoDezoomer));
        } catch (Exception e) {
            System.out.println(e);
        }
        btnZoom.addActionListener(e -> {
            int step = 10;
            int newValue = slider.getValue() + step;
            slider.setValue(Math.min(newValue, slider.getMaximum()));
        });
        btnDezoom.addActionListener(e -> {
            int step = 10;
            int newValue = slider.getValue() - step;
            slider.setValue(Math.max(newValue, slider.getMinimum()));
        });
        statusPanel.add(Box.createHorizontalStrut(10)); // mezera zleva
        statusPanel.add(btnDezoom);
        statusPanel.add(Box.createHorizontalStrut(5));
        statusPanel.add(slider);
        statusPanel.add(Box.createHorizontalStrut(5));
        statusPanel.add(btnZoom);
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(progressBar);
        progressBar.setVisible(false);
        statusPanel.add(Box.createHorizontalStrut(10));
    }

    /**
     * metoda na nalezení adresy obrázku, který chceme upravit přes fileChooser
     */
    private void openImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                var img = ImageIO.read(chooser.getSelectedFile());
                if (img != null) {
                    imagePanel.setImage(img);
                    slider.setValue(100);
                    history.reset(imagePanel.getBufferedImage());
                    isImage = true;
                    setUIEnabled(true);
                    updateUndoRedoButtons();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * metoda na uložení obrázku do formátu .png
     */
    private void saveImageAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("*.png", "png"));
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                ImageIO.write(imagePanel.getBufferedImage(), "png", new File(file.getAbsolutePath() + ".png"));
            } catch (IOException ex) {
                System.out.println("Failed to save image!");
            }
        } else {
            System.out.println("No file choosen!");
        }
    }

    /**
     * metoda pro enable/disable všechna riziková tlačítka při operacích či pokud není otevřený žádný obrázek
     */
    private void setUIEnabled(boolean enabled) {
        for (JMenuItem item : menuItemsToDisable) {
            item.setEnabled(enabled);
        }
        btnOpenPic.setEnabled(true);
        btnReset.setEnabled(true);

        slider.setEnabled(enabled);
        btnZoom.setEnabled(enabled);
        btnDezoom.setEnabled(enabled);

        progressBar.setVisible(!enabled);
        updateUndoRedoButtons();
    }

    private void updateUndoRedoButtons() {
        btnUndo.setEnabled(history.canUndo());
        btnForward.setEnabled(history.canRedo());
    }

    /**
     * metoda na otevření dokumentace aplikace na webu
     */
    private void OpenDocs() {
        try {
            File docFile = new File("src/Documentation/index.html");
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(docFile.toURI());
            } else {
                JOptionPane.showMessageDialog(this, "Nelze otevřít prohlížeč...\n"
                        + "Dokumentace je na adrese:\n"
                        + docFile.getAbsolutePath(), "Pozor", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Nepodařilo se otevřít dokumentaci:\n" + ex.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * rozhraní pro pomalé operace s obrázkem,
     * které probíhají na pozadí (SwingWorker) a mohou hlásit průběh (progresspercents).
     */
    @FunctionalInterface
    private interface ImageTask {
        BufferedImage run(BufferedImage current, ProgressPercents progress) throws Exception;
    }

    private void runBackground(String errorTitle, ImageTask task) {
        BufferedImage current = imagePanel.getBufferedImage();
        if (current == null) return;

        setUIEnabled(false);
        progressBar.setValue(0);
        progressBar.setVisible(true);

        new SwingWorker<BufferedImage, Integer>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return task.run(current, percent -> publish(percent));
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    progressBar.setValue(chunks.getLast());
                }
            }

            @Override
            protected void done() {
                try {
                    BufferedImage result = get();
                    if (result != null) {
                        imagePanel.updateImage(result);
                        history.push(result);
                        imagePanel.resetImagePos();
                        slider.setValue(100);
                        updateUndoRedoButtons();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            errorTitle + ": " + ex.getMessage());
                } finally {
                    setUIEnabled(true);
                }
            }
        }.execute();
    }

    /**
     * rozhraní pro rychlé operace s obrázkem,
     * které se provádějí přímo na hlavním vlákně (bez progresspercents).
     */
    @FunctionalInterface
    private interface SimpleOp {
        BufferedImage apply(BufferedImage current);
    }

    private void applySimple(SimpleOp op, boolean resetView) {
        BufferedImage current = imagePanel.getBufferedImage();
        if (current == null) return;

        BufferedImage out = op.apply(current);
        if (out == null) return;

        imagePanel.updateImage(out);
        history.push(out);

        if (resetView) {
            imagePanel.resetImagePos();
            slider.setValue(100);
        }
        updateUndoRedoButtons();
    }
}