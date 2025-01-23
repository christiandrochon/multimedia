package fr.cdrochon.multimedias.generationdescripteurs.gris;

import fr.cdrochon.multimedias.generationdescripteurs.AbstractIndexDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DescripteurGrey256_1f extends AbstractIndexDatabase {
    
    @Value("${histogram.gray256.output}")
    private String grayHistogramOutputFile;
    
    private static final int RED = 0, GREEN = 1, BLUE = 2;
    
    public void process() throws IOException {
        processDatabase();
    }
    
    /**
     * Méthode pour calculer l'histogramme en niveaux de gris avec 256 bins
     *
     * @return histogramme en niveaux de gris normalisé
     */
    @Override
    public float[] computeHistogram() {
        int[] histogram = new int[256];  // Histogramme avec 256 niveaux de gris
        int totalPixels = width * height;  // Nombre total de pixels
        
        // Parcourir tous les pixels de l'image pour calculer les niveaux de gris
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                // Calculer le niveau de gris pondéré en utilisant les valeurs RVB normalisées entre 0 et 1
                float grayLevel = 0.299f * pixels[x][y][RED] +
                        0.587f * pixels[x][y][GREEN] +
                        0.114f * pixels[x][y][BLUE];
                
                // Quantifier le niveau de gris en 256 bins (0 à 1)
                int binIndex = (int) (grayLevel * 255.0f);
                
                // Incrémenter le bin correspondant dans l'histogramme
                histogram[binIndex]++;
            }
        }
        
        // Normaliser l'histogramme en divisant chaque bin par le nombre total de pixels
        float[] normalizedHistogram = new float[256];
        for(int i = 0; i < 256; i++) {
            normalizedHistogram[i] = (float) histogram[i] / totalPixels;  // Valeur normalisée entre 0 et 1
        }
        
        return normalizedHistogram;
    }
    
    /**
     * Sauvegarde l'histogramme en niveaux de gris dans un fichier
     *
     * @return le fichier de sortie
     */
    @Override
    protected String getOutputFilePath() {
        return grayHistogramOutputFile;
    }
}
