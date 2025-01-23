package fr.cdrochon.multimedias.rechercheparsimilarite.services;

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
public class SimilariteForme {
    
    @Value("${histogram.forme.output}")
    private String formeDescriptorFile;  // Fichier contenant les descripteurs de forme
    
    @Value("${images.list.file}")
    private String imagesListFile;  // Fichier listant les noms des images
    
    /**
     * Trouve les images les plus similaires en termes de forme à une image requête.
     *
     * @param queryImageName Nom de l'image de requête
     * @param k              Nombre d'images similaires à retourner
     * @return Liste des chemins des images similaires
     */
    public List<String> findSimilarShapes(String queryImageName, int k) {
        try {
            // Charger les noms des images
            List<String> imageNames = Files.readAllLines(Paths.get(imagesListFile));
            List<List<Float>> formeDescriptors = loadFormeDescriptors();
            
            // Vérifier la correspondance entre le nombre d'images et de descripteurs
            if (formeDescriptors.size() != imageNames.size()) {
                System.err.println("Mismatch entre le nombre d'images (" + imageNames.size() +
                                           ") et le nombre de descripteurs (" + formeDescriptors.size() + ").");
                return new ArrayList<>();
            }
            
            // Trouver l'index de l'image requête
            int queryImageIndex = imageNames.indexOf(queryImageName);
            if (queryImageIndex == -1) {
                System.err.println("Image de requête introuvable dans la liste d'images.");
                return new ArrayList<>();
            }
            
            List<Float> queryDescriptor = formeDescriptors.get(queryImageIndex);
            
            // Calculer les distances euclidiennes entre la requête et chaque image
            List<ImageDistance> distances = new ArrayList<>();
            for (int i = 0; i < formeDescriptors.size(); i++) {
                if (i != queryImageIndex) {  // Ignorer l'image requête elle-même
                    double distance = calculateEuclideanDistance(queryDescriptor, formeDescriptors.get(i));
                    distances.add(new ImageDistance(imageNames.get(i), distance));
                }
            }
            
            // Trier les distances par ordre croissant et retourner les k premières
            return distances.stream()
                            .sorted(Comparator.comparingDouble(ImageDistance::getDistance))
                            .limit(k)
                            .map(imageDistance -> "/images/" + imageDistance.getImageName())
                            .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    
    /**
     * Charge les descripteurs de forme depuis le fichier.
     *
     * @return Liste de descripteurs (chaque descripteur est une liste de valeurs flottantes)
     * @throws IOException en cas de problème de lecture du fichier
     */
    private List<List<Float>> loadFormeDescriptors() throws IOException {
        List<List<Float>> descriptors = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(formeDescriptorFile));
        
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            List<Float> descriptor = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) { // Ignorer le premier élément (nom de l'image)
                descriptor.add(Float.parseFloat(parts[i]));
            }
            descriptors.add(descriptor);
        }
        return descriptors;
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
        for (int i = 0; i < descriptor1.size(); i++) {
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
