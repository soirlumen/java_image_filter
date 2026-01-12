package ui;

/**
 * Parametry pro Gaussian Blur
 * radius = poloměr kernelu (vždy liché)
 * sigma = míra rozostření vyšší-obrázek víc rozostřen
 */
public record GaussianBlurParams(int radius, double sigma) {}