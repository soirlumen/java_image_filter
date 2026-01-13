# Java Image Processor CZ

Aplikace pro základní úpravy obrázků za pomocí knihoven Swing a BufferedImage.

## Filtry, transformace obrázku, další funkcionality
- Otevírání (PNG, JPG...) a ukládání obrázků (PNG)
- Úprava jasu
- Rozmazání (Gaussian)
- Detekce hran (Sobel)
- Zostření (Laplacian Sharpen)
- Invertování barev
- Převod barev na úrovně šedi (average / BT.709 luminance / BT601 luma)
- Škálování obrázku bez deformace poměru (bilineární interpolace)
- Historie operací (undo/redo)
- resetování pozice obrázku

## Požadavky na spuštění
- **Java 17 nebo novější**, jistě funguje na v25.0.1
- zkontrolovat verzi pomocí:
```cmd
java -version
```
- případně stáhnout z webu (např.: https://adoptium.net)  

## Jak spustit .jar soubor
1. Stáhnout `image_processing.jar` z repozitáře
2. Otevřiít příkazový řádek (cmd) a přejít do složky, kde je .jar
```cmd
cd cesta\k\souboru
```
3. spustit soubor pomocí příkazu
```cmd
java -jar image_processing.jar
```
4. (volitelné) Pokud je v počítači více java verzí, můžete spustit specifickou takto:
```cmd
"C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\java.exe" -jar image_processing.jar
# v powershellu
& "C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\java.exe" -jar image_processing.jar
```
# Java Image Processor EN
A simple desktop application for basic image editing using Java Swing and BufferedImage.

## Features / Filters & Transformations
- Open and save images (PNG, JPG...)
- Brightness adjustment
- Gaussian blur
- Edge detection (Sobel operator)
- Sharpening (Laplacian-based)
- Color inversion
- Grayscale conversion (average / BT.709 luminance / BT.601 luma)
- Image scaling with aspect ratio preserved (bilinear interpolation)
- Operation history (undo / redo)
- Reset image position / zoom

## Requirements
- **Java 17 or newer** (tested and works on Java 25.0.1)
- Check your Java version:
  ```cmd
  java -version
  ```
- If needed, download from: https://adoptium.net (Temurin builds recommended)

## How to Run the .jar File
1. Download image_processing.jar from this repository (or from Releases if available)
2. Open Command Prompt (cmd) and navigate to the folder containing the .jar:
 ```cmd
cd path\to\your\folder
```
3. Run the application
 ```cmd
java -jar image_processing.jar
 ```
4. (Optional) If you have multiple Java versions installed and need to use a specific one, for example:
 ```cmd
"C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\java.exe" -jar image_processing.jar
# in powershell
& "C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\java.exe" -jar image_processing.jar
 ```
