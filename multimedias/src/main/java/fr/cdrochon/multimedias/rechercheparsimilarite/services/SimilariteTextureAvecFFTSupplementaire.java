package fr.cdrochon.multimedias.rechercheparsimilarite.services;

import org.jtransforms.fft.DoubleFFT_1D;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimilariteTextureAvecFFTSupplementaire {
    
    @Value("${histogram.texture.output}")
    private String textureDescriptorFile;  // Fichier contenant les descripteurs de texture
    
    @Value("${images.list.file}")
    private String imagesListFile;  // Fichier listant les noms des images
    
    /**
     * Trouve les images les plus similaires en terme de texture à une image de requête.
     *
     * @param queryImageName Nom de l'image de requête
     * @param k              Nombre d'images similaires à retourner
     * @return Liste des chemins des images similaires
     */
    public List<String> findSimilarTextures(String queryImageName, int k) {
        try {
            // Charger les noms des images
            List<String> imageNames = Files.readAllLines(Paths.get(imagesListFile));
            List<List<Float>> textureDescriptors = loadTextureDescriptors();
            
            // Vérifier la correspondance entre le nombre d'images et de descripteurs
            if(textureDescriptors.size() != imageNames.size()) {
                System.err.println("INcoherence entre le nombre d'images (" + imageNames.size() +
                                           ") et le nombre de descripteurs (" + textureDescriptors.size() + ").");
                return new ArrayList<>();
            }
            
            // Trouver l'index de l'image requête
            int queryImageIndex = imageNames.indexOf(queryImageName);
            if(queryImageIndex == -1) {
                System.err.println("Image de requête introuvable dans la liste d'images.");
                return new ArrayList<>();
            }
            
            List<Float> queryDescriptor = textureDescriptors.get(queryImageIndex);
            
            // Calculer les distances euclidiennes entre la requête et chaque image
            List<ImageDistance> distances = new ArrayList<>();
            for(int i = 0; i < textureDescriptors.size(); i++) {
                if(i != queryImageIndex) {  // Ignorer l'image requête elle-même
                    double distance = calculateEuclideanDistance(queryDescriptor, textureDescriptors.get(i));
                    distances.add(new ImageDistance(imageNames.get(i), distance));
                }
            }
            
            // Trier les distances par ordre croissant et retourner les k premières
            return distances.stream()
                            .sorted(Comparator.comparingDouble(ImageDistance::getDistance))
                            .limit(k)
                            .map(imageDistance -> "/images/" + imageDistance.getImageName())
                            .collect(Collectors.toList());
        } catch(IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Charge les descripteurs de texture depuis le fichier et applique la FFT.
     *
     * @return Liste de descripteurs après application de la FFT (chaque descripteur est une liste de valeurs flottantes)
     *
     * @throws IOException en cas de problème de lecture du fichier
     */
    private List<List<Float>> loadTextureDescriptors() throws IOException {
        List<List<Float>> descriptors = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(textureDescriptorFile));
        
        for(String line : lines) {
            String[] parts = line.trim().split("\\s+");
            List<Float> descriptor = new ArrayList<>();
            for(int i = 1; i < parts.length; i++) { // Ignorer le premier élément (nom de l'image)
                descriptor.add(Float.parseFloat(parts[i]));
            }
            // Appliquer la FFT sur le descripteur
            List<Float> fftDescriptor = applyFFT(descriptor);
            descriptors.add(fftDescriptor);
        }
        return descriptors;
    }
    
    
    /**
     * Applique la transformée de Fourier rapide (FFT) sur un descripteur.
     *
     * @param descriptor Descripteur de texture
     * @return Descripteur après application de la FFT
     */
    private List<Float> applyFFT(List<Float> descriptor) {
        if(descriptor == null || descriptor.isEmpty()) {
            throw new IllegalArgumentException("Le descripteur est vide ou nul.");
        }
        
        int n = descriptor.size();
        if(n <= 1) {
            throw new IllegalArgumentException("Le descripteur doit contenir au moins 2 valeurs pour la FFT.");
        }
        
        System.out.println("Taille du descripteur : " + n);
        
        double[] data = new double[n * 2]; // JTransforms nécessite un tableau pour les données complexes
        for(int i = 0; i < n; i++) {
            data[2 * i] = descriptor.get(i); // Partie réelle
            data[2 * i + 1] = 0;             // Partie imaginaire (0 pour des données réelles)
        }
        
        // Appliquer la FFT
        try {
            //            DoubleFFT_2D fft = new DoubleFFT_2D(1, n); //Tab de 2 dimension alorsd qu'il faut une seule dimension pour un vecteur
            DoubleFFT_1D fft = new DoubleFFT_1D(n);
            fft.complexForward(data);
        } catch(Exception e) {
            System.err.println("Erreur lors de l'application de la FFT : " + e.getMessage());
            e.printStackTrace();
            throw e; // Propager l'exception pour un diagnostic dans les logs
        }
        
        // Extraire la magnitude des coefficients FFT
        List<Float> fftDescriptor = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            double real = data[2 * i];
            double imag = data[2 * i + 1];
            float magnitude = (float) Math.sqrt(real * real + imag * imag);
            fftDescriptor.add(magnitude);
        }
        
        return fftDescriptor;
    }
    
    /**
     * Calcule la distance euclidienne entre deux descripteurs.
     *
     * @param descriptor1 Descripteur de la première image
     * @param descriptor2 Descripteur de la deuxième image
     * @return Distance euclidienne entre les deux descripteurs
     */
    private double calculateEuclideanDistance(List<Float> descriptor1, List<Float> descriptor2) {
        double sum = 0.0;
        for(int i = 0; i < descriptor1.size(); i++) {
            double diff = descriptor1.get(i) - descriptor2.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
    
    /**
     * Classe interne pour associer le nom d'une image à sa distance
     */
    private static class ImageDistance {
        
        private final String imageName;
        private final double distance;
        
        public ImageDistance(String imageName, double distance) {
            this.imageName = imageName;
            this.distance = distance;
        }
        
        public String getImageName() {
            return imageName;
        }
        
        public double getDistance() {
            return distance;
        }
    }
}

