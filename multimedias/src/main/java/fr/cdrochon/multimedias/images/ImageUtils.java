package fr.cdrochon.multimedias.images;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageUtils {
    
    /**
     * Crée une image de damier de 400x400 pixels avec des cases de 200x200 pixels
     *
     * @param filePath Chemin du fichier image à créer
     * @throws Exception Erreur lors de la création de l'image
     */
    public static void createDamierImage(String filePath) throws Exception {
        int width = 400;
        int height = 400;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        
        for(int y = 0; y < height; y += 200) {
            for(int x = 0; x < width; x += 200) {
                g2d.setColor((x + y) % 400 == 0 ? Color.RED : Color.GREEN);
                g2d.fillRect(x, y, 200, 200);
            }
        }
        
        g2d.dispose();
        ImageIO.write(img, "png", new File(filePath));
    }
    
    /**
     * Échange les canaux de couleur d'une image
     *
     * @param inputPath  Chemin de l'image d'entrée
     * @param outputPath Chemin de l'image de sortie
     * @throws Exception Erreur lors de la lecture ou de l'écriture de l'image
     */
    public static void swapRGBtoGBR(String inputPath, String outputPath) throws Exception {
        BufferedImage img = ImageIO.read(new File(inputPath));
        int width = img.getWidth();
        int height = img.getHeight();
        
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gbr = (g << 16) | (b << 8) | r;
                img.setRGB(x, y, gbr);
            }
        }
        
        ImageIO.write(img, "png", new File(outputPath));
    }
}
