package fr.cdrochon.multimedias.generationdescripteurs.gris;

import fr.cdrochon.multimedias.generationdescripteurs.AbstractIndexDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DescripteurGrey16_1f extends AbstractIndexDatabase {
    
    @Value("${histogram.gray16.output}")
    private String grayHistogramOutputFile;
    
    private static final int RED = 0, GREEN = 1, BLUE = 2;
    
    public void process() throws IOException {
        processDatabase();
    }
    
    /**
     * Méthode pour calculer l'histogramme en niveaux de gris avec 16 bins
     * @return histogramme en niveaux de gris normalisé
     */
    @Override
    protected float[] computeHistogram() {
        int[] histogram = new int[16];  // Histogramme avec 16 niveaux de gris
        int totalPixels = width * height;  // Nombre total de pixels
        float binSize = 1.0f / 16;  // Taille des intervalles des bins dans l'intervalle [0, 1]
        
        // Parcourir tous les pixels de l'image pour calculer les niveaux de gris
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                // Calculer le niveau de gris pondéré en utilisant les valeurs RVB normalisées entre 0 et 1
                float grayLevel = 0.299f * pixels[x][y][RED] +
                        0.587f * pixels[x][y][GREEN] +
                        0.114f * pixels[x][y][BLUE];
                
                // Quantification sur 16 intervalles : chaque bin correspond à une partie de l'intervalle [0, 1]
                int binIndex = (int) (grayLevel / binSize);
                
                // Assurer que le bin indexé est valide (éviter les dépassements)
                if(binIndex >= 16) {
                    binIndex = 15;
                }
                
                // Incrémenter le bin correspondant dans l'histogramme
                histogram[binIndex]++;
            }
        }
        
        // Normaliser l'histogramme en divisant chaque bin par le nombre total de pixels
        float[] normalizedHistogram = new float[16];
        for(int i = 0; i < 16; i++) {
            normalizedHistogram[i] = (float) histogram[i] / totalPixels;  // Valeur normalisée entre 0 et 1
        }
        
        return normalizedHistogram;
    }
    
    /**
     * Sauvegarde l'histogramme en niveaux de gris dans un fichier
     * @return le fichier de sortie
     */
    @Override
    protected String getOutputFilePath() {
        return grayHistogramOutputFile;
    }
}
