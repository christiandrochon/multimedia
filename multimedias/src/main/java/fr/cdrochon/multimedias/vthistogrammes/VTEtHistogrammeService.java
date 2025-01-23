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
public class VTEtHistogrammeService {
    
    @Value("${images.list.file}")
    private String imagesListFile;  // Fichier listant les images de la base
    
    @Value("${resnet.VT_description}")
    private String VTClassesDescripteur; // Chemin vers le fichier des classes (e.g., VT_description.txt)
    
    @Value("${resnet.VT_files}")
    private String VTVecteurFiles; // Chemin vers le fichier d'index des images (e.g., VT_files.txt)
    
    @Value("${histogram.rgb666.output}")
    private String histogramRgb666Output; // Chemin vers le fichier de sortie des descripteurs de couleur
    
    private List<List<Float>> descriptorsList;
    private Map<String, List<String>> classToImagesMap;
    private List<String> classDescriptors;
    
    /**
     * Initialisation du service pour charger les descripteurs de classes et les images associées à chaque classe.
     *
     * @throws IOException si une erreur de lecture du fichier se produit
     * @PostConstruct spécifie que la méthode doit être exécutée après l'injection de dépendances
     */
    @PostConstruct
    private void initializeService() throws IOException {
        // Charger les descripteurs de classes resnetClasses
        classDescriptors = Files.readAllLines(Paths.get(VTClassesDescripteur));
        
        // Charger les images depuis resnetFiles et créer une map des classes aux images
        List<String> imageLines = Files.readAllLines(Paths.get(VTVecteurFiles));
        classToImagesMap = new HashMap<>();
        
        for(int i = 0; i < classDescriptors.size(); i++) {
            String className = classDescriptors.get(i);
            if(i < imageLines.size()) {
                List<String> images = List.of(imageLines.get(i).split("\\s+"));
                classToImagesMap.put(className, images);
            }
        }
        //        // Charger les descripteurs des images dans descriptorsList
        //        descriptorsList = loadDescriptors();
    }
    
    /**
     * Méthode pour récupérer une image aléatoire
     *
     * @return Chemin de l'image aléatoire (parce que c'est le chemin que angular lit)
     */
    public String getRandomImage() {
        List<String> allImages = classToImagesMap.values()
                                                 .stream()
                                                 .flatMap(Collection::stream)
                                                 .collect(Collectors.toList());
        return allImages.get(new Random().nextInt(allImages.size()));
    }
    
    /**
     * Méthode pour trouver les images les plus similaires à une image de requête donnée par son nom de fichier. Utilise des descripteurs de couleur et des
     * classes vérite terrain resnet.
     *
     * @param queryImageName Nom de l'image de requête (ex: "103000.jpg")
     * @return Liste des chemins des images similaires
     */
    public List<String> getImagesForClassWithSimilarity(String queryImageName) {
        try {
            
            // Étape 1 : Charger le fichier des classes de descripteur (VT_files) et identifier la classe de l'image de requête
            List<String> descriptorLines = Files.readAllLines(Paths.get(VTVecteurFiles)); // VT_files.txt
            int descriptorClassIndex = -1;
            
            for(int i = 0; i < descriptorLines.size(); i++) {
                List<String> imagesInClass = Arrays.asList(descriptorLines.get(i).split("\\s+"));
                if(imagesInClass.contains(queryImageName)) {
                    descriptorClassIndex = i;
                    break;
                }
            }
            
            if(descriptorClassIndex == -1) {
                System.err.println("Image non trouvée dans les classes de descripteur : " + queryImageName);
                return Collections.emptyList();
            }
            
            
            // Étape 2 : Récupérer toutes les images de la même classe de descripteur
            List<String> imagesInSameClass = Arrays.asList(descriptorLines.get(descriptorClassIndex).split("\\s+"));
            
            
            // Étape 3 : Charger les descripteurs couleur pour toutes les images
            List<List<Float>> colorDescriptors = loadColorDescriptors(); // Chargez les vecteurs couleur pour chaque image
            List<String> allImageNames = Files.readAllLines(Paths.get(imagesListFile)); // imagesListFile contient la liste des noms d'images
            
            
            // Étape 4 : Trouver l’index et le descripteur de couleur de l'image de requête
            int queryIndex = allImageNames.indexOf(queryImageName);
            if(queryIndex == -1 || queryIndex >= colorDescriptors.size()) {
                System.err.println("Descripteur couleur introuvable pour l'image de requête.");
                return Collections.emptyList();
            }
            List<Float> queryDescriptor = colorDescriptors.get(queryIndex);
            
            
            // Étape 5 : Calculer les distances de similarité pour les images de la même classe et trier dynamiquement
            return imagesInSameClass.stream()
                                    .filter(image -> allImageNames.contains(image)) // Vérifie si l'image est dans la liste
                                    .sorted(Comparator.comparingDouble(image -> {
                                        int imageIndex = allImageNames.indexOf(image);
                                        if(imageIndex != -1 && imageIndex < colorDescriptors.size()) {
                                            return calculateEuclideanDistance(queryDescriptor, colorDescriptors.get(imageIndex));
                                        } else {
                                            return Double.MAX_VALUE;
                                        }
                                    }))
                                    .limit(8) // Limite à 10 résultats les plus proches
                                    .collect(Collectors.toList());
            
        } catch(IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * Charge les descripteurs de couleur depuis le fichier des histogrammes que l'on a généré.
     *
     * @return Liste de descripteurs (chaque descripteur est une liste de valeurs flottantes)
     *
     * @throws IOException en cas de problème de lecture du fichier
     */
    private List<List<Float>> loadColorDescriptors() throws IOException {
        return Files.lines(Paths.get(histogramRgb666Output))
                    .map(line -> Arrays.stream(line.split("\\s+"))
                                       .map(Float::parseFloat)
                                       .collect(Collectors.toList()))
                    .collect(Collectors.toList());
    }
    
    /**
     * Calcul de la distance euclidienne pour comparer les descripteurs couleur.
     *
     * @param descriptor1 Premier descripteur
     * @param descriptor2 Deuxième descripteur
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



