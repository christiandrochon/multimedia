package fr.cdrochon.multimedias.generationdescripteurs.forme;

import fr.cdrochon.multimedias.generationdescripteurs.AbstractIndexDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Service
public class DescripteurForme extends AbstractIndexDatabase {
    
    @Value("${histogram.forme.output}")
    private String formeHistogramOutputFile;
    
    private static final int NUM_BINS = 16; // Diviser [0, 2π] en 16 intervalles
    private static final Logger logger = LoggerFactory.getLogger(DescripteurForme.class);
    
    /**
     * Processus principal pour générer les descripteurs de forme pour toutes les images.
     */
    public void process() throws IOException {
        logger.info("Démarrage du calcul des descripteurs de forme...");
        processDatabase(); // Appelle la logique de génération pour toutes les images
        logger.info("Calcul des descripteurs de forme terminé. Résultat enregistré dans : {}", formeHistogramOutputFile);
    }
    
    /**
     * Méthode pour calculer les descripteurs de forme via EOH (sans OpenCV).
     *
     * @return Histogramme de forme normalisé
     */
    @Override
    protected float[] computeHistogram() {
        logger.info("Étape 1 : Calcul des descripteurs de forme...");
        
        int width = this.width;
        int height = this.height;
        
        logger.info("Dimensions de l'image : {}x{}", width, height);
        
        // Étape 2 : Calcul des gradients
        double[][] gradX = computeGradientX();
        double[][] gradY = computeGradientY();
        
        // Étape 3 : Calcul et normalisation de l'histogramme des orientations
        return computeEOH(gradX, gradY);
    }
    
    /**
     * Calcul des gradients en X (Sobel simplifié).
     *
     * @return Matrice des gradients en X
     */
    private double[][] computeGradientX() {
        double[][] gradX = new double[width][height];
        
        // Appliquer un filtre Sobel simplifié en X
        for(int y = 1; y < height - 1; y++) {
            for(int x = 1; x < width - 1; x++) {
                gradX[x][y] = (
                        pixels[x + 1][y - 1][0] + 2 * pixels[x + 1][y][0] + pixels[x + 1][y + 1][0]
                                - pixels[x - 1][y - 1][0] - 2 * pixels[x - 1][y][0] - pixels[x - 1][y + 1][0]
                );
            }
        }
        return gradX;
    }
    
    /**
     * Calcul des gradients en Y (Sobel simplifié).
     *
     * @return Matrice des gradients en Y
     */
    private double[][] computeGradientY() {
        double[][] gradY = new double[width][height];
        
        // Appliquer un filtre Sobel simplifié en Y
        for(int y = 1; y < height - 1; y++) {
            for(int x = 1; x < width - 1; x++) {
                gradY[x][y] = (
                        pixels[x - 1][y + 1][0] + 2 * pixels[x][y + 1][0] + pixels[x + 1][y + 1][0]
                                - pixels[x - 1][y - 1][0] - 2 * pixels[x][y - 1][0] - pixels[x + 1][y - 1][0]
                );
            }
        }
        return gradY;
    }
    
    /**
     * Calculer l'histogramme des orientations des gradients.
     *
     * @param gradX Matrice des gradients en X
     * @param gradY Matrice des gradients en Y
     * @return Histogramme normalisé des orientations
     */
    private float[] computeEOH(double[][] gradX, double[][] gradY) {
        float[] histogram = new float[NUM_BINS];
        Arrays.fill(histogram, 0);
        
        for(int y = 1; y < height - 1; y++) {
            for(int x = 1; x < width - 1; x++) {
                double gx = gradX[x][y];
                double gy = gradY[x][y];
                
                // Calcul de l'angle du gradient
                double angle = Math.atan2(gy, gx);
                if(angle < 0) {
                    angle += 2 * Math.PI; // Normaliser l'angle entre [0, 2π]
                }
                
                // Quantification dans les intervalles de l'histogramme
                int bin = (int) (NUM_BINS * angle / (2 * Math.PI));
                histogram[bin]++;
            }
        }
        
        // Normaliser l'histogramme
        float total = 0;
        for(float value : histogram) {
            total += value;
        }
        
        if(total > 0) {
            for(int i = 0; i < NUM_BINS; i++) {
                histogram[i] /= total;
            }
        }
        
        return histogram;
    }
    
    /**
     * Fournir le chemin du fichier de sortie pour enregistrer les descripteurs.
     *
     * @return Chemin du fichier configuré dans application.properties
     */
    @Override
    protected String getOutputFilePath() {
        return formeHistogramOutputFile;
    }
}
