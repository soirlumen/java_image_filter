package image;

import java.awt.image.BufferedImage;

public class ImageProcessor {

    /**
     * Algoritmus pro úpravu jasu
     * @param img BufferedImage původního obrázku
     * @param perctg úroveň jasnosti, hodnoty mimo <-100,100> "ořízne"
     * @return BufferedImage původních rozměrů s přičtenou konstantou ke každému pixelu pro úpravu jasu
     */
    public static BufferedImage brightness(BufferedImage img, int perctg) {
        if (img == null) return null;

        int height = img.getHeight();
        int width = img.getWidth();
        // chci procenta z 255 protože pixel má hodnotu <0,255>, hodnoty přesahující <-100,100> % jsou oříznuty
        int amount = Math.max(-255, Math.min(255, perctg * 255 / 100));
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); //nachystáme nový obrázek

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF; //průhlednost neměníme, bitová operace - posune bitové číslo o 24 doprava a ořízne
                int r = Math.min(255, Math.max(0, ((rgb >> 16) & 0xFF) + amount)); //nechceme, aby číslo překročilo 255 ani podkročilo 0
                int g = Math.min(255, Math.max(0, ((rgb >> 8) & 0xFF) + amount));
                int b = Math.min(255, Math.max(0, (rgb & 0xFF) + amount));
                result.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b); // složení bitů zpět
            }
        }
        return result;
    }

    /**
     * algoritmus pro převedení obrázku na černobílý dle různých metod převodu
     * @param img BufferedImage původního obrázku
     * @param method enum pro vyběr metody převodu
     * @return BufferedImage původních rozměrů v grayscale
     */
    public static BufferedImage toGrayscaleUpgrade(BufferedImage img, ChooseGrayscale method) {
        if (img == null) return null;

        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int gray = method.togray(r, g, b);

                int newRgb = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
                result.setRGB(x, y, newRgb);
            }
        }
        return result;
    }

    /**
     * zjednodušená metoda pro grayscale používající average grayscale filter
     * @param img BufferedImage původního obrázku
     * @return BufferedImage původních rozměrů v grayscale
     */
    public static BufferedImage toGrayscaleUpgrade(BufferedImage img) {
        return toGrayscaleUpgrade(img, ChooseGrayscale.average);
    }

    /**
     * algoritmus pro detekci hran, používá Sobel kernel
     * s progress ukazatelem
     * @param img BufferedImage původního obrázku
     * @param progress progress ukazatel-pro pomalejší algoritmy
     * @return BufferedImage původních rozměrů na které byly aplikované kernely sobela
     */
    public static BufferedImage detectEdges(BufferedImage img, ProgressPercents progress) {
        if (img == null) return null;

        int width = img.getWidth();
        int height = img.getHeight();

        double[][] kernelX = { {-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1} };
        double[][] kernelY = { {-1,-2,-1}, { 0, 0, 0}, { 1, 2, 1} };

        BufferedImage gray = toGrayscaleUpgrade(img);
        if (progress != null) progress.update(10);

        // sobel x
        BufferedImage edgesX = applyKernel(gray, kernelX, percent -> {
            if (progress != null) progress.update(10 + (percent * 30 / 100));
        });

        // sobel y
        BufferedImage edgesY = applyKernel(gray, kernelY, percent -> {
             if (progress != null) progress.update(40 + (percent * 30 / 100));
         });

        BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgbX = edgesX.getRGB(x, y);
                int rgbY = edgesY.getRGB(x, y);

                int gx = (rgbX >> 16) & 0xFF;   // intenzita horizontálního gradientu
                int gy = (rgbY >> 16) & 0xFF;   // intenzita vertikálního gradientu

                // Pythagorova věta-velikost změny
                int g = (int) Math.sqrt(gx * gx+ gy * gy);
                g = Math.min(255, g);           // ořezání na max 255

                combined.setRGB(x, y, (255 << 24) | (g << 16) | (g << 8) | g);

                if (progress != null) {
                    int percent = 70 + (int) ((y + 1) * 30.0 / height);
                    progress.update(percent);
                }
            }
        }
        return combined;
    }

    /**
     * algoritmus pro rotaci obrázku doleva
     * @param img BufferedImage původního obrázku
     * @return BufferedImage obrácených rozměrů (w<->h) a orotovaných levotočivě:)
     */
    public static BufferedImage rotateLeft(BufferedImage img) {
        if (img == null) return null;

        int w = img.getWidth();
        int h = img.getHeight();

        BufferedImage out = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);

                int newX = y;
                int newY = w - x - 1;

                out.setRGB(newX, newY, rgb);
            }
        }
        return out;
    }

    /**
     * algoritmus pro rotaci obrázku doprava
     * @param img BufferedImage původního obrázku
     * @return BufferedImage obrácených rozměrů (w<->h) a orotovaných pravotočivě:)
     */
    public static BufferedImage rotateRight(BufferedImage img) {
        if (img == null) return null;

        int w = img.getWidth();
        int h = img.getHeight();

        BufferedImage result = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);

                int newX = h - y - 1;
                int newY = x;

                result.setRGB(newX, newY, rgb);
            }
        }
        return result;
    }

    /**
     * algoritmus pro převrácení obrázku podle osy Y
     * optimalizace: prochází jen půlku výšky a swapuje zároveň oba protilehlé pixely
     * @param img BufferedImage původního obrázku
     * @return BufferedImage obrácených rozměrů (w<->h) vertikálně převrácené
     */
    public static BufferedImage flipV(BufferedImage img) {
        if (img == null) return null;

        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h / 2; y++) { //stačí procházet jen půlku obrázku, jelikož rovnou prohazujeme zrcadlové pixely
            for (int x = 0; x < w; x++) {
                int toppix = img.getRGB(x, y);
                int botpix = img.getRGB(x, h - y - 1);

                result.setRGB(x, y, botpix);
                result.setRGB(x, h - y - 1, toppix);
            }
        }
        if (h % 2 == 1) {
            int middleY = h / 2;
            for (int x = 0; x < w; x++) {
                result.setRGB(x, middleY, img.getRGB(x, middleY)); //při lichém rozměru zkopíruje prostřední řádek
            }
        }
        return result;
    }

    /**
     * Algoritmus pro převrácení obrázku podle osy X
     * optimalizace: prochází jen půlku šířky a swapuje zároveň oba protilehlé pixely
     * @param img BufferedImage původního obrázku
     * @return BufferedImage původních rozměrů horizontálně převrácené
     */
    public static BufferedImage flipH(BufferedImage img) {
        if (img == null) return null;

        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w / 2; x++) { // jen do poloviny šířky
                int leftpix  = img.getRGB(x, y);
                int rightpix = img.getRGB(w - x - 1, y);

                result.setRGB(x, y, rightpix);
                result.setRGB(w - x - 1, y, leftpix);
            }
        }
        if (w % 2 == 1) {
            int middleX = w / 2;
            for (int y = 0; y < h; y++) {
                result.setRGB(middleX, y, img.getRGB(middleX, y));//při lichém rozměru zkopíruje prostřední
            }
        }
        return result;
    }

    /**
     * algoritmus pro normalizovaný Gaussův kernel pro rozmazání
     * @param size velikost kernelu-pouze lichá čísla
     * @param sigma standardní odchylka-doporučené rozmezí 0,3 až 5
     * @return normalizovaný čtvercový kernel-součet vah je 1
     * @throws IllegalArgumentException pokud size je sudé
     */
    public static double[][] kernelGaussGenerator(int size, double sigma) {
        if (size % 2 == 0) {
            throw new IllegalArgumentException("Kernel musí být lichý");
        }

        double[][] kernel = new double[size][size];
        int center = size / 2;
        double sum = 0.0;

        // Výpočet Gausse pro každý prvek
        for (int y = -center; y <= center; y++) {
            for (int x = -center; x <= center; x++) {
                double value = Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                kernel[y + center][x + center] = value;
                sum += value;
            }
        }
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                kernel[y][x] /= sum;
            }
        }
        return kernel;
    }

    /**
     * algoritmus aplikuje obecně(rozměrově) zadaný kernel na každý pixel obrázku
     * @param img BufferedImage původního obrázku
     * @param kernel matice-kernel, kterým se aplikuje na každý pixel
     * @param progres progress ukazatel-pro pomalejší algoritmy
     * @return BufferedImage původního obrázku projetého kernelem
     */
    public static BufferedImage applyKernel(BufferedImage img, double[][] kernel, ProgressPercents progres) {
        int kw = kernel[0].length;
        int kh = kernel.length;
        int kCenterX = kw / 2;
        int kCenterY = kh / 2;

        int w = img.getWidth();
        int h = img.getHeight();

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                double r = 0, g = 0, b = 0, a = 0;

                for (int ky = 0; ky < kh; ky++) {
                    for (int kx = 0; kx < kw; kx++) {

                        int px = Math.min(w - 1, Math.max(0, x + kx - kCenterX));
                        int py = Math.min(h - 1, Math.max(0, y + ky - kCenterY));

                        int rgb = img.getRGB(px, py);

                        double weight = kernel[ky][kx];
                        r += toLinear((rgb >>> 16) & 0xFF) * weight;
                        g += toLinear((rgb >>> 8) & 0xFF) * weight;
                        b += toLinear(rgb & 0xFF) * weight;
                    }
                }

                int ia = (img.getRGB(x, y) >>> 24) & 0xFF;
                int ir = toSRGB(r);
                int ig = toSRGB(g);
                int ib = toSRGB(b);

                int newRGB =
                        (ia << 24) | (ir << 16) | (ig << 8) | ib;

                result.setRGB(x, y, newRGB);
            }
            // aktualizace progressu po každém řádku
            if (progres != null) {
                int percent = (int) ((y + 1) * 100.0 / h);
                progres.update(percent);
            }
        }
        return result;
    }

    /**
     * algoritmus pro ostření hran s laplacian sharpen kernelem (centrální váha 5)
     * s progress ukazatelem
     * @param img BufferedImage původního obrázku
     * @param progres progress ukazatel-pro pomalejší algoritmy
     * @return BufferedImage původních rozměrů na které byly aplikované kernely laplacian sharpen
     */
    public static BufferedImage sharpen(BufferedImage img, ProgressPercents progres) {
        if (img == null) return null;
        double[][] kernel = {
                {0, -1, 0},
                {-1, 5, -1},
                {0, -1, 0}
        };
        return applyKernel(img, kernel, progres);
    }

    /**
     * algoritmus pro invertování barev
     * @param img BufferedImage původního obrázku
     * @return BufferedImage původních rozměrů invertovaných hodnot pixelů
     */
    public static BufferedImage invertColors(BufferedImage img) {
        int h = img.getHeight();
        int w = img.getWidth();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                int r = 255 - ((rgb >> 16) & 0xFF); //vezme jednotlivé hodnoty složek rgb a od 255 odečte tuto hodnotu
                int g = 255 - ((rgb >> 8) & 0xFF);
                int b = 255 - (rgb & 0xFF);
                result.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }

    /**
     * algoritmus pro zvětšení/zmenšení obrázku (bez deformace poměru) s bilineární interpolací
     * @param img BufferedImage původního obrázku
     * @param NEWWidth hodnota nové výšky obrázku
     * @param NEWHeight hodnota nové šířky obrázku
     * @return BufferedImage škálovaných rozměrů
     */
    public static BufferedImage resize(BufferedImage img, int NEWWidth, int NEWHeight) {
        if (img == null || NEWWidth <= 0 || NEWHeight <= 0) {
            return null;
        }
        BufferedImage resized = new BufferedImage(NEWWidth, NEWHeight, BufferedImage.TYPE_INT_ARGB);

        double scalex = (double) img.getWidth() / NEWWidth; // scalex = stará šířka / nová šířka
        double scaley = (double) img.getHeight() / NEWHeight;

        for (int y = 0; y < NEWHeight; y++) {
            for (int x = 0; x < NEWWidth; x++) {
                // Výpočet pozice v původním obrázku (s desetinným číslem)
                double srcX = x * scalex;
                double srcY = y * scaley;
                // Celá část + zlomková část
                int x1 = (int) srcX;
                int y1 = (int) srcY;
                double fx = srcX - x1;
                double fy = srcY - y1;

                // Ošetření okrajů, aby x2,y2 nevyšlo mimo obrázek
                int x2 = Math.min(x1 + 1, img.getWidth() - 1);
                int y2 = Math.min(y1 + 1, img.getHeight() - 1);

                // Získání 4 okolních pixelů
                int rgb00 = img.getRGB(x1, y1);
                int rgb10 = img.getRGB(x2, y1);
                int rgb01 = img.getRGB(x1, y2);
                int rgb11 = img.getRGB(x2, y2);

                // Interpolace pro každou složku a,r,g,b
                int a = interpolateColChannel(((rgb00 >> 24) & 0xFF), ((rgb10 >> 24) & 0xFF),
                        ((rgb01 >> 24) & 0xFF), ((rgb11 >> 24) & 0xFF), fx, fy);
                int r = interpolateColChannel(((rgb00 >> 16) & 0xFF), ((rgb10 >> 16) & 0xFF),
                        ((rgb01 >> 16) & 0xFF), ((rgb11 >> 16) & 0xFF), fx, fy);
                int g = interpolateColChannel(((rgb00 >> 8) & 0xFF), ((rgb10 >> 8) & 0xFF),
                        ((rgb01 >> 8) & 0xFF), ((rgb11 >> 8) & 0xFF), fx, fy);
                int b = interpolateColChannel((rgb00 & 0xFF), (rgb10 & 0xFF),
                        (rgb01 & 0xFF), (rgb11 & 0xFF), fx, fy);

                int newPixel = (a << 24) | (r << 16) | (g << 8) | b;
                resized.setRGB(x, y, newPixel);
            }
        }
        return resized;
    }

    /**
     * bilineární interpolace jedné barevné složky mezi čtyřmi sousedními pixely
     * pro algoritmus resise(...) pro plynulejší přechody
     */
    private static int interpolateColChannel(int c00, int c10, int c01, int c11, double fx, double fy) {
        double top = c00 * (1 - fx) + c10 * fx;
        double bottom = c01 * (1 - fx) + c11 * fx;
        return (int) (top * (1 - fy) + bottom * fy);
    }

    /**
     * převede barevnou složku do lineárního prostoru
     * Používá se před konvolucí, aby rozmazání/ostření vypadalo lépe
     */
    private static double toLinear(int c) {
        double v = c / 255.0;
        return Math.pow(v, 2.2);
    }

    /**
     * zpětný převod hodnoty z lineárního prostoru do srgb
     * osekne výsledek do rozsahu <0,255>
     */
    private static int toSRGB(double v) {
        v = Math.pow(v, 1.0 / 2.2);
        return (int) (Math.min(1.0, Math.max(0.0, v)) * 255);
    }
}
