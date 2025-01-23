package fr.cdrochon.multimedias.generationdescripteurs.texture;

import fr.cdrochon.multimedias.generationdescripteurs.AbstractIndexDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.jtransforms.fft.DoubleFFT_2D;

import java.io.IOException;

@Component
public class DescripteurTexture extends AbstractIndexDatabase {
    
    @Value("${histogram.texture.output}")
    private String textureHistogramOutputFile;
    
    private static final int NUM_BANDS = 16; // Diviser les fréquences en 8 bandes
    private static final Logger logger = LoggerFactory.getLogger(DescripteurTexture.class);
    
    public void process() throws IOException {
        logger.info("Démarrage du calcul des descripteurs de texture...");
        processDatabase();
        logger.info("Calcul des descripteurs de texture terminé. Résultat enregistré dans : {}", textureHistogramOutputFile);
    }
    
    /**
     * Méthode pour calculer les descripteurs de texture via TFD
     *
     * @return histogramme de texture normalisé
     */
    @Override
    protected float[] computeHistogram() {
        logger.info("Étape 1 : Appliquer la Transformée de Fourier bidimensionnelle...");
        double[][] magnitudeSpectrum = computeFourierTransform();
//        logger.info("Transformée de Fourier calculée.");
        
        logger.info("Étape 2 : Calcul et normalisation de la répartition de l'énergie...");
        float[] normalizedHistogram = computeNormalizedFrequencyBands(magnitudeSpectrum, width, height);
//        logger.info("Histogramme de texture calculé avec succès.");
        return normalizedHistogram;
    }
    
    
    /**
     * Appliquer la Transformée de Fourier bidimensionnelle
     */
    private double[][] computeFourierTransform() {
        int width = this.width;
        int height = this.height;
        
        // Convertir les pixels en une matrice 2D
        double[][] pixelData = new double[height][width]; // Correct : hauteur x largeur
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelData[y][x] = pixels[x][y][0]; // Vérifiez si pixels est bien indexé [x][y]
            }
        }
        
        // Initialiser la FFT 2D
        DoubleFFT_2D fft2D = new DoubleFFT_2D(height, width);
        
        // Convertir la matrice en tableau 1D pour JTransforms
        double[] data = new double[height * width * 2]; // Tableau complexe (parties réelle + imaginaire)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = 2 * (y * width + x);
                data[index] = pixelData[y][x];  // Partie réelle
                data[index + 1] = 0.0;         // Partie imaginaire
            }
        }
        
        // Appliquer la FFT
        fft2D.complexForward(data);
        
        // Calculer le spectre de magnitude
        double[][] magnitudeSpectrum = new double[height][width]; // Correct : hauteur x largeur
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = 2 * (y * width + x);
                double real = data[index];
                double imag = data[index + 1];
                magnitudeSpectrum[y][x] = Math.sqrt(real * real + imag * imag); // Magnitude
            }
        }
        
        // recentrer le spectre apres son calcul
//        double[][] spectreRecentre = shiftSpectrum(magnitudeSpectrum, width, height);
        
        logger.debug("Transformée de Fourier avec FFT calculée avec succès.");
        return magnitudeSpectrum;
    }
    
    /**
     * Calculer la répartition de l'énergie dans des bandes de fréquences
     * @param spectrum spectre de magnitude
     * @param width largeur de l'image
     * @param height hauteur de l'image
     * @return répartition de l'énergie dans des bandes de fréquences
     */
    private float[] computeNormalizedFrequencyBands(double[][] spectrum, int width, int height) {
        logger.debug("Répartition des fréquences en bandes et normalisation...");
        
        // Vérifiez que les dimensions de spectrum correspondent
        if (spectrum.length != height || spectrum[0].length != width) { // Correction ici
            throw new IllegalArgumentException("Dimensions de spectrum incorrectes : attendu " + height + "x" + width +
                                                       ", mais reçu " + spectrum.length + "x" + spectrum[0].length);
        }
        
        double[] bands = new double[NUM_BANDS];
        double maxRadius = Math.sqrt(width * width + height * height) / 2;
        
        // Calcul des bandes
        for (int u = 0; u < width; u++) {
            for (int v = 0; v < height; v++) {
                double distance = Math.sqrt(u * u + v * v);
                int bandIndex = (int) (NUM_BANDS * distance / maxRadius);
                
                if (bandIndex >= NUM_BANDS) {
                    bandIndex = NUM_BANDS - 1;
                }
                
                bands[bandIndex] += spectrum[v][u]; // Correction : respecter l'ordre des indices
            }
        }
        
        // Normalisation des bandes
        double totalEnergy = 0.0;
        for (double band : bands) {
            totalEnergy += band;
        }
        logger.debug("Énergie totale calculée pour l'image : {}", totalEnergy);
        
        
        if (totalEnergy == 0.0) {
            logger.error("Énergie totale égale à zéro, impossible de normaliser les bandes.");
            throw new IllegalStateException("Énergie totale égale à zéro, vérifiez le contenu de spectrum.");
        } else if (totalEnergy < 10E-6) {
            logger.warn("Énergie totale très faible : {}. Vérifiez le contenu de l'image ou le spectre.", totalEnergy);
        }
        
        float[] normalizedBands = new float[NUM_BANDS];
        for (int i = 0; i < NUM_BANDS; i++) {
            normalizedBands[i] = (float) (bands[i] / totalEnergy);
        }
        
        logger.debug("Répartition et normalisation des bandes terminées.");
        return normalizedBands;
    }
    
    /**
     * Recentrer le spectre de magnitude
     * @param magnitudeSpectrum matrice de spectre de magnitude
     * @param width largeur de l'image
     * @param height hauteur de l'image
     * @return spectre de magnitude recentré
     */
    private double[][] shiftSpectrum(double[][] magnitudeSpectrum, int width, int height) {
        double[][] shifted = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int shiftedX = (x + width / 2) % width;
                int shiftedY = (y + height / 2) % height;
                shifted[shiftedY][shiftedX] = magnitudeSpectrum[y][x];
            }
        }
        return shifted;
    }
    
    /**
     * Sauvegarder l'histogramme de texture dans un fichier
     */
    @Override
    protected String getOutputFilePath() {
        return textureHistogramOutputFile;
    }
    
}
