package fr.cdrochon.multimedias.vthistogrammes;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VTService {
    
//    @Value("${images.list.file}")
//    private String imagesListFile;  // Fichier listant les images de la base
    
    @Value("${resnet.VT_description}")
    private String VTDescription; // Chemin vers le fichier des classes (e.g., VT_description.txt)
    
    @Value("${resnet.VT_files}")
    private String VTFiles; // Chemin vers le fichier d'index des images (e.g., VT_files.txt)
    
    
    private List<String> classDescriptors;
    private Map<String, List<String>> classToImagesMap;
    private List<List<Float>> descriptorsList;
    
    /**
     * Initialiser le service en chargeant les descripteurs de classes et les images. Dans cette méthode, nous chargeons les descripteurs de classes depuis le
     * fichier resnetClasses, les images depuis le fichier resnetFiles et les descripteurs d'images. C'est l'user qui choisit la classe de description resnet
     *
     * @throws IOException si une erreur de lecture des fichiers se produit
     */
    @PostConstruct
    private void initializeService() throws IOException {
        // Charger les descripteurs de classes depuis resnetClasses
        classDescriptors = Files.readAllLines(Paths.get(VTDescription));
        
        // Charger les images depuis resnetFiles et créer une map des classes aux images
        List<String> imageLines = Files.readAllLines(Paths.get(VTFiles));
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
     * Charger les descripteurs d'images depuis le fichier resnetFiles et acceder à chaque descripteur via son index
     *
     * @return Liste des descripteurs d'images
     *
     * @throws IOException si une erreur de lecture du fichier se produit
     */
    private List<List<Float>> loadDescriptors() throws IOException {
        return Files.lines(Paths.get(VTFiles))
                    .map(line -> Arrays.stream(line.split("\\s+"))
                                       .filter(token -> token.matches("-?\\d+(\\.\\d+)?")) // Filter only numeric values
                                       .map(Float::parseFloat)
                                       .collect(Collectors.toList()))
                    .collect(Collectors.toList());
    }
    
    /**
     * Récupérer une image aléatoire pour une classe donnée
     *
     * @param className Nom de la classe de description resnet
     * @return Chemin de l'image aléatoire
     */
    public String getRandomImageForClass(String className) {
        List<String> images = classToImagesMap.getOrDefault(className, Collections.emptyList());
        if(images.isEmpty()) {
            return null; // Retourner null si aucune image n'est trouvée
        }
        Random random = new Random();
        return images.get(random.nextInt(images.size())); // Retourne une image aléatoire
    }
    
    /**
     * REcuperation d'une image aleatoire mais appartenant  à une classe de description resnet, puis comparaison par similarité en triant les images par ordre
     * de pertinence
     *
     * @param className      Nom de la classe de description resnet
     * @param queryImageName Nom de l'image de requête
     * @return Liste des chemins des images similaires
     */
    public List<String> getImagesForClassWithSimilarity(String className, String queryImageName) {
        List<String> images = classToImagesMap.getOrDefault(className, new ArrayList<>());
        
        try {
            // 1. FILTRAGE DES IMAGES
            // Charger le fichier VT_files et rechercher l'image de requête
            List<String> allImageLines = Files.readAllLines(Paths.get(VTFiles));
            
            int queryIndex = -1;
            
            // Parcourir chaque ligne du fichier des classes de descripteur pour trouver la classe et l’index de l'image de requête
            for(int i = 0; i < allImageLines.size(); i++) {
                List<String> lineImages = Arrays.asList(allImageLines.get(i).split("\\s+"));
                if(lineImages.contains(queryImageName)) {
                    queryIndex = i;
                    break;
                }
            }
            
            // Vérifier que l'image de requête a bien été trouvée
            if(queryIndex == -1) {
                System.err.println("Image de requête introuvable dans le fichier VT_files : " + queryImageName);
                return new ArrayList<>(); // Retourner une liste vide si l'image de requête est introuvable
            }
            
            // Charger les descripteurs de chaque classe
            List<List<Float>> descriptorsList = loadDescriptors();
            if(queryIndex >= descriptorsList.size()) {
                System.err.println("Index de classe hors limites pour le descripteur : " + queryIndex);
                return new ArrayList<>();
            }
            
            // Récupérer le descripteur de l'image de requête
            List<Float> queryDescriptor = descriptorsList.get(queryIndex);
            
            // 2. TRI PAR SIMILARITE
            // Trier les images par similarité (distance croissante) et prendre les k premières
            return images.stream()
                         .filter(image -> allImageLines.stream().anyMatch(line -> line.contains(image)))  // Filtrer les images présentes
                         .sorted(Comparator.comparingDouble(image -> {
                             int imageIdx = findImageIndexInFile(image, allImageLines);
                             return imageIdx == -1 ? Double.MAX_VALUE : calculateEuclideanDistance(queryDescriptor, descriptorsList.get(imageIdx));
                         }))
                         .collect(Collectors.toList());
            
        } catch(IOException e) {
            e.printStackTrace();
            return new ArrayList<>(); // En cas d'erreur de lecture du fichier
        }
    }
    
    /**
     * Trouver l'index de l'image dans le fichier VT_files
     *
     * @param imageName     Nom de l'image
     * @param allImageLines Lignes du fichier VT_files
     * @return Index de l'image dans le fichier VT_files
     */
    private int findImageIndexInFile(String imageName, List<String> allImageLines) {
        for(int i = 0; i < allImageLines.size(); i++) {
            List<String> lineImages = Arrays.asList(allImageLines.get(i).split("\\s+"));
            if(lineImages.contains(imageName)) {
                return i;
            }
        }
        return -1; // Retourner -1 si l'image n'est pas trouvée
    }
    
    /**
     * Calculer la distance euclidienne entre deux descripteurs
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
}
