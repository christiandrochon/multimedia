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
public class QBE {
    
    @Value("${images.list.file}")
    private String imagesListFile;  // Fichier listant les images de la base
    
    @Value("${histogram.rgb666.output}")
    //    @Value("${histogram.gray256.output}")
    private String descriptorFile;  // Fichier contenant les descripteurs des images
    
    /**
     * Méthode pour trouver les images les plus similaires à une image de requête donnée par son nom de fichier.
     *
     * @param queryImageName Nom de l'image de requête (ex: "103000.jpg")
     * @param k              Nombre de résultats similaires désirés
     * @return Liste des chemins des images similaires
     */
    public List<String> findSimilarImages(String queryImageName, int k) {
        try {
            // Lire le fichier listant les images pour obtenir l’index de l’image requête
            List<String> imageNames = Files.readAllLines(Paths.get(imagesListFile));
            int queryImageIndex = imageNames.indexOf(queryImageName);
            
            if(queryImageIndex == -1) {
                System.err.println("Image de requête introuvable dans la liste d'images.");
                return new ArrayList<>();
            }
            
            // Récupérer les descripteurs de l'image requête
            List<List<Float>> descriptors = loadDescriptors();
            List<Float> queryDescriptor = descriptors.get(queryImageIndex);
            
            // Calculer les distances euclidiennes entre la requête et chaque image
            List<ImageDistance> distances = new ArrayList<>();
            for(int i = 0; i < descriptors.size(); i++) {
                if(i != queryImageIndex) {  // Ignorer l'image requête elle-même
                    double distance = calculateEuclideanDistance(queryDescriptor, descriptors.get(i));
                    distances.add(new ImageDistance(imageNames.get(i), distance));
                }
            }
            
            // Trier les images par similarité (distance croissante) et prendre les k premières
            return distances.stream()
                            .sorted(Comparator.comparingDouble(ImageDistance::getDistance))
                            .limit(k)
                            .map(imageDistance -> "/images/" + imageDistance.getImageName())
                            //                            .map(ImageDistance::getImageName)
                            .collect(Collectors.toList());
            
        } catch(IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Charge les descripteurs depuis le fichier.
     *
     * @return Liste de descripteurs (chaque descripteur est une liste de valeurs flottantes)
     *
     * @throws IOException en cas de problème de lecture du fichier
     */
    private List<List<Float>> loadDescriptors() throws IOException {
        List<List<Float>> descriptors = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(descriptorFile));
        
        for(String line : lines) {
            String[] parts = line.trim().split("\\s+");
            List<Float> descriptor = new ArrayList<>();
            for(String part : parts) {
                descriptor.add(Float.parseFloat(part));
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


