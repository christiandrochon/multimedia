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
public class SimilariteGlobale {
    
    @Value("${histogram.texture.output}")
    private String textureDescriptorFile;
    
    @Value("${histogram.rgb666.output}")
    private String colorDescriptorFile;
    
    @Value("${histogram.forme.output}")
    private String formeDescriptorFile;
    
    @Value("${images.list.file}")
    private String imagesListFile;
    
    // Pondérations pour chaque type de descripteur
    private static final double COLOR_WEIGHT = 0.4;
    private static final double TEXTURE_WEIGHT = 0.2;
    private static final double FORME_WEIGHT = 0.4;
    
    /**
     * Trouve les images les plus similaires en termes de couleur, texture et forme.
     *
     * @param queryImageName Nom de l'image de requête
     * @param k              Nombre d'images similaires à retourner
     * @return Liste des chemins des images similaires
     */
    public List<String> findSimilarImages(String queryImageName, int k) {
        try {
            List<String> imageNames = Files.readAllLines(Paths.get(imagesListFile));
            int queryImageIndex = imageNames.indexOf(queryImageName);
            
            if (queryImageIndex == -1) {
                System.err.println("Image de requête introuvable dans la liste d'images.");
                return new ArrayList<>();
            }
            
            // Charger les descripteurs pour chaque type
            List<List<Float>> textureDescriptors = loadDescriptors(textureDescriptorFile);
            List<List<Float>> colorDescriptors = loadDescriptors(colorDescriptorFile);
            List<List<Float>> formeDescriptors = loadDescriptors(formeDescriptorFile);
            
            // Obtenir les descripteurs de l'image requête
            List<Float> queryTextureDescriptor = textureDescriptors.get(queryImageIndex);
            List<Float> queryColorDescriptor = colorDescriptors.get(queryImageIndex);
            List<Float> queryFormeDescriptor = formeDescriptors.get(queryImageIndex);
            
            // Calculer les distances combinées
            List<ImageDistance> distances = new ArrayList<>();
            for (int i = 0; i < imageNames.size(); i++) {
                if (i != queryImageIndex) {
                    double textureDistance = calculateEuclideanDistance(queryTextureDescriptor, textureDescriptors.get(i));
                    double colorDistance = calculateEuclideanDistance(queryColorDescriptor, colorDescriptors.get(i));
                    double formeDistance = calculateEuclideanDistance(queryFormeDescriptor, formeDescriptors.get(i));
                    
                    // Distance combinée avec pondération
                    double combinedDistance = COLOR_WEIGHT * colorDistance
                            + TEXTURE_WEIGHT * textureDistance
                            + FORME_WEIGHT * formeDistance;
                    
                    distances.add(new ImageDistance(imageNames.get(i), combinedDistance));
                }
            }
            
            // Trier les distances et retourner les k plus proches
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
     * Charge les descripteurs depuis un fichier donné.
     *
     * @param descriptorFile Chemin du fichier descripteur
     * @return Liste de descripteurs (chaque descripteur est une liste de valeurs flottantes)
     * @throws IOException En cas de problème de lecture du fichier
     */
    private List<List<Float>> loadDescriptors(String descriptorFile) throws IOException {
        List<List<Float>> descriptors = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(descriptorFile));
        
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
