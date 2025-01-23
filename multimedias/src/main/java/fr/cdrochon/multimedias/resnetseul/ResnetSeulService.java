package fr.cdrochon.multimedias.resnetseul;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResnetSeulService {
    
    @Value("${images.list.file}")
    private String imagesListFile;  // Fichier listant les images de la base
    
    @Value("${resnet.VT_description}")
    private String resnetClasses; // Chemin vers le fichier des classes (e.g., VT_description.txt)
    
    @Value("${resnet.VT_files}")
    private String resnetFiles; // Chemin vers le fichier d'index des images (e.g., VT_files.txt)
    
    @Value("${resnet.resnet18}")
    private String resnet18; // Chemin vers le fichier des descripteurs ResNet18
    
    private List<String> classDescriptors;
    private Map<String, List<String>> classToImagesMap;
    private List<List<Float>> descriptorsList;
    
    
    /**
     * Méthode pour charger les classes et images depuis les fichiers PostContruct sert à appeler la méthode après l'initialisation du bean
     *
     * @throws IOException
     */
    @PostConstruct
    private void initializeService() {
        try {
            // Charger les descripteurs de classes depuis resnetClasses
            classDescriptors = Files.readAllLines(Paths.get(resnetClasses));
            
            // Charger les images depuis resnetFiles et créer une map des classes aux images
            List<String> imageLines = Files.readAllLines(Paths.get(resnetFiles));
            classToImagesMap = new HashMap<>();
            
            for(int i = 0; i < classDescriptors.size(); i++) {
                String className = classDescriptors.get(i);
                if(i < imageLines.size()) {
                    List<String> images = List.of(imageLines.get(i).split("\\s+"));
                    classToImagesMap.put(className, images);
                }
            }
            
            // Charger les descripteurs des images dans descriptorsList
            descriptorsList = loadDescriptors();
        } catch(IOException e) {
            System.err.println("Erreur lors du chargement des fichiers dans initializeService: " + e.getMessage());
            e.printStackTrace();
        } catch(Exception e) {
            System.err.println("Une erreur inattendue est survenue dans initializeService: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    /**
     * Récupérer les descripteurs de classes
     *
     * @return Liste des descripteurs de classes
     */
    public List<String> getClassDescriptors() {
        return classDescriptors;
    }
    
    /**
     * Récupérer les images pour une classe donnée
     *
     * @param className Nom de la classe
     * @return Liste des noms des images
     */
    public List<String> getImagesForClass(String className) {
        List<String> images = classToImagesMap.getOrDefault(className, new ArrayList<>());
        List<String> fullUrls = new ArrayList<>();
        
        // Envoi le nom de l'image
        for(String image : images) {
            fullUrls.add(image);
        }
        
        return fullUrls;
    }
    
    /**
     * Caluler la distance entre les descripteurs de l'image de requête et les descripteurs des images de la même classe
     *
     * @param className      Nom de la classe de descripteur resnet
     * @param queryImageName Nom de l'image de requête
     * @return Liste des noms des images les plus similaires
     */
    public List<String> getImagesForClassWithSimilarity(String className, String queryImageName) {
        try {
            // Charger la liste complète des images de la base
            List<String> allImages = Files.readAllLines(Paths.get(imagesListFile));
            int queryIndex = allImages.indexOf(queryImageName);
            
            if(queryIndex == -1) {
                System.err.println("Image de requête introuvable dans la liste d'images : " + queryImageName);
                return Collections.emptyList();
            }
            
            // Vérifier si la classe est valide et obtenir les images de cette classe
            List<String> relevantImages = classToImagesMap.getOrDefault(className, Collections.emptyList());
            if(relevantImages.isEmpty()) {
                System.err.println("Classe introuvable ou sans images : " + className);
                return Collections.emptyList();
            }
            
            // Charger les descripteurs de classe et récupérer le descripteur de l'image de requête
            List<List<Float>> descriptors = loadDescriptors();
            List<Float> queryDescriptor = descriptorsList.get(queryIndex);
            
            // Calculer les distances euclidiennes entre le descripteur de la requête et ceux des images de la même classe
            List<ResnetSeulService.ImageDistance> distances = relevantImages.stream()
                                                                            .filter(allImages::contains) // S'assurer que l'image est bien dans la
                                                                            // liste complète
                                                                            .map(image -> {
                                                                                           int imageIndex = allImages.indexOf(image);
                                                                                           if(imageIndex != -1 && imageIndex < descriptorsList.size()) {
                                                                                               double distance = calculateEuclideanDistance(queryDescriptor,
                                                                                                                                            descriptorsList.get(
                                                                                                                                                    imageIndex));
                                                                                               return new ImageDistance(image, distance);
                                                                                           } else {
                                                                                               return new ImageDistance(image,
                                                                                                                        Double.MAX_VALUE); // Distance max
                                                                                               // pour les images sans descripteur
                                                                                           }
                                                                                       })
                                                                            .sorted(Comparator.comparingDouble(ImageDistance::getDistance))
                                                                            .limit(8) // Limiter aux 8 résultats les plus proches
                                                                            .collect(Collectors.toList());
            
            return distances.stream().map(ImageDistance::getImageName).collect(Collectors.toList());
            
        } catch(IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * Calculer la distance euclidienne entre deux descripteurs
     *
     * @param descriptor1 Premier descripteur
     * @param descriptor2 Deuxième descripteur
     * @return Distance euclidienne entre les deux descripteurs
     */
    private double calculateEuclideanDistance(List<Float> descriptor1, List<Float> descriptor2) {
        if(descriptor1.size() != descriptor2.size()) {
            throw new IllegalArgumentException("Les descripteurs doivent avoir la même longueur");
        }
        
        double sum = 0.0;
        for(int i = 0; i < descriptor1.size(); i++) {
            double diff = descriptor1.get(i) - descriptor2.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
    
    
    /**
     * Charge les descripteurs depuis le fichier.
     *
     * @return Liste de descripteurs (chaque descripteur est une liste de valeurs flottantes)
     * @throws IOException en cas de problème de lecture du fichier
     */
    private List<List<Float>> loadDescriptors() throws IOException {
        List<List<Float>> descriptors = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(resnet18));
        
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

