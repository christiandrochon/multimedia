package fr.cdrochon.multimedias.generationdescripteurs;

import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public abstract class AbstractIndexDatabase {
    
    @Value("${images.list.file}")
    protected String imagesListFile;  // Chemin vers le fichier texte listant les images
    
    @Value("${images.directory}")
    protected String imagesDirectory;  // Chemin vers le répertoire contenant les images
    
    @Value("${images.list.output}")
    protected String imagesListOutputFile;  // Fichier de sortie pour la copie du fichier texte
    
    protected int width, height;
    protected float[][][] pixels;
    
    /**
     * Méthode abstraite pour calculer les descripteurs d'une image
     *
     * @return vecteurs descripteurs pour chaque image
     */
    protected abstract float[] computeHistogram();
    
    /**
     * Méthode abstraite pour obtenir le chemin du fichier de sortie
     *
     * @return le chemin du fichier de sortie
     */
    protected abstract String getOutputFilePath();
    
    /**
     * Méthode pour lire l'image à partir du fichier
     *
     * @param fileName le nom du fichier
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    protected void readImageFile(String fileName) throws IOException {
        BufferedImage img = ImageIO.read(new File(fileName));
        width = img.getWidth();
        height = img.getHeight();
        pixels = new float[width][height][3];  // Créer une matrice pour stocker les pixels RVB
        int[] rgbArray = img.getRGB(0, 0, width, height, null, 0, width);
        
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                int pixel = rgbArray[y * width + x];
                // Extraire les composantes RVB et les normaliser entre 0 et 1
                pixels[x][y][0] = (float) ((pixel >> 16) & 0xff) / 255.0f;
                pixels[x][y][1] = (float) ((pixel >> 8) & 0xff) / 255.0f;
                pixels[x][y][2] = (float) (pixel & 0xff) / 255.0f;
                
                // Correction pour éviter 1 exact
                if(pixels[x][y][0] == 1)
                    pixels[x][y][0] -= 1e-5f;
                if(pixels[x][y][1] == 1)
                    pixels[x][y][1] -= 1e-5f;
                if(pixels[x][y][2] == 1)
                    pixels[x][y][2] -= 1e-5f;
            }
        }
    }
    
    /**
     * Lecture de toutes les images de lBD et écriture de l'histogramme dans le fichier de sortie
     *
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    public void processDatabase() throws IOException {
        List<String> imageNames = Files.readAllLines(Paths.get(imagesListFile));
        try(FileWriter writer = new FileWriter(getOutputFilePath())) {
            for(String imageName : imageNames) {
                File imageFile = new File(imagesDirectory, imageName);
                if(imageFile.exists() && (imageFile.getName().endsWith(".jpg") || imageFile.getName().endsWith(".png"))) {
                    System.out.println("Traitement de l'image : " + imageName);
                    readImageFile(imageFile.getAbsolutePath());
                    float[] histogram = computeHistogram();
                    saveHistogram(writer, histogram);
                } else {
                    System.out.println("L'image " + imageName + " n'existe pas.");
                }
            }
        }
        if(!Files.exists(Paths.get(imagesListOutputFile))) {
            copyImagesListFile();
        }
        System.out.println("L'indexation des images est terminée.");
    }
    
    /**
     * Enregistrer un histogramme
     *
     * @param writer    le fichier de sortie
     * @param histogram l'histogramme à enregistrer
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    protected void saveHistogram(FileWriter writer, float[] histogram) throws IOException {
        for(float value : histogram) {
            writer.write(value + " ");
        }
        writer.write("\n");
    }
    
    /**
     * Copier le fichier de référence des images
     *
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    protected void copyImagesListFile() throws IOException {
        Files.copy(Paths.get(imagesListFile), Paths.get(imagesListOutputFile));
        System.out.println("Le fichier de référence des images a été copié.");
    }
}
