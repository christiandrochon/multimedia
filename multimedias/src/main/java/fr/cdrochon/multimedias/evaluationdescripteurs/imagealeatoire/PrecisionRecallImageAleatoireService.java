package fr.cdrochon.multimedias.evaluationdescripteurs.imagealeatoire;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class PrecisionRecallImageAleatoireService {
    
    Logger logger = LoggerFactory.getLogger(PrecisionRecallImageAleatoireService.class);
    
    @Value("${resnet.VT_files}") //liste des images par classes VT
    private String vtFilesPath;
    
    @Value("${resnet.VT_description}") // nom des classes VT
    private String vtDescriptionPath;
    
    @Value("${images.list.output}") // liste des images de la base complete
    private String base10000files;
    
    @Value("${histogram.gray256.output}")
    private String histogramGray256;
    
    @Value("${histogram.gray64.output}")
    private String histogramGray64;
    
    @Value("${histogram.gray16.output}")
    private String histogramGray16;
    
    @Value("${histogram.rgb666.output}")
    private String histogramRGB666;
    
    /**
     * Methode asynchrone pour récuperer la classe verité-terrain de l'image aleatoire et ses descripteurs.
     * On utilise la méthode getRandomImageWithVT() pour récuperer une image aléatoire avec classe VT.
     * On charge les descripteurs de l'image aléatoire et on retourne la classe VT et les descripteurs.
     * On retourne un objet CompletableFuture contenant une map avec les clés "classeVT" et "descripteurs".
     * On appelle la methode de recherche par similarité pour récuperer les 8 images similaires les kppv.
     * On retourne les 8 images les kppv.
     * On retourne les points de précision et rappel pour l'image aléatoire.
     * On retourne un objet CompletableFuture contenant une map avec les clés "classeVT", "descripteurs", "nearestNeighbors", "Precision" et "Recall".
     *
     * @param queryImage nom de l'image requête
     * @param topK nombre d'images similaires à considérer
     * @return objet CompletableFuture contenant une map avec les clés "classeVT", "descripteurs", "nearestNeighbors", "Precision" et "Recall"
     */
    @Async
    public CompletableFuture<Map<String, Object>> calculatePrecisionRecallForImageAsync(String queryImage, int topK) {
        logger.info("Calcul asynchrone précision/rappel pour l'image : {}", queryImage);
        
        try {
            // Charger les noms des images dans la base
            List<String> allImageNames = Files.readAllLines(Paths.get(base10000files));
            logger.info("Nombre total d'images dans la base : {}", allImageNames.size());
            
            // Charger les fichiers VT_Files
            List<String> vtFilesLines = Files.readAllLines(Paths.get(vtFilesPath));
            if (vtFilesLines.isEmpty()) {
                throw new IllegalStateException("Le fichier VT_Files est vide.");
            }
            logger.info("Nombre total de classes dans VT_Files : {}", vtFilesLines.size());
            
            // Trouver la ligne contenant l'image requête
            int vtClassIndex = -1;
            for (int lineIndex = 0; lineIndex < vtFilesLines.size(); lineIndex++) {
                List<String> classImages = Arrays.asList(vtFilesLines.get(lineIndex).split("\\s+"));
                if (classImages.contains(queryImage)) {
                    vtClassIndex = lineIndex;
                    logger.info("Image requête '{}' trouvée sur la ligne : {}", queryImage, lineIndex);
                    break;
                }
            }
            
            // Si l'image requête n'est pas trouvée
            if (vtClassIndex == -1) {
                logger.warn("Image requête '{}' non trouvée dans le fichier VT_Files.", queryImage);
                return CompletableFuture.completedFuture(Map.of(
                        "Precision", new ArrayList<>(),
                        "Recall", new ArrayList<>(),
                        "nearestNeighbors", new ArrayList<>()
                                                               ));
            }
            
            // Récupérer toutes les images pertinentes de la classe VT
            List<String> relevantImages = Arrays.asList(vtFilesLines.get(vtClassIndex).split("\\s+"));
            logger.info("Images pertinentes pour la classe VT (ligne {}) : {}", vtClassIndex, relevantImages);
            
            // Charger les descripteurs RGB666
            Map<String, List<Double>> descriptorMap = loadDescriptors(histogramRGB666, allImageNames);
            if (!descriptorMap.containsKey(queryImage)) {
                throw new IllegalArgumentException("L'image requête n'est pas présente dans les descripteurs.");
            }
            
            // Calculer les similarités et trier les images par distance
            List<Double> queryDescriptor = descriptorMap.get(queryImage);
            List<String> sortedRetrievedImages = descriptorMap.entrySet().stream()
                                                              .filter(entry -> !entry.getKey().equals(queryImage)) // Exclure l'image elle-même
                                                              .sorted(Comparator.comparingDouble(entry -> calculateEuclideanDistance(queryDescriptor, entry.getValue())))
                                                              .map(Map.Entry::getKey)
                                                              .collect(Collectors.toList());
            
            // Récupérer les 8 images les plus proches voisins
            List<String> top8Neighbors = sortedRetrievedImages.subList(0, Math.min(8, sortedRetrievedImages.size()));
            logger.info("Top 8 plus proches voisins pour l'image {} : {}", queryImage, top8Neighbors);
            
            // Calculer précision et rappel
            List<Point> precisionPoints = new ArrayList<>();
            List<Point> recallPoints = new ArrayList<>();
            int kPlus = 0;
            
            for (int k = 1; k <= topK && k <= sortedRetrievedImages.size(); k++) {
                String retrievedImage = sortedRetrievedImages.get(k - 1);
                
                // Vérifier si l'image récupérée est pertinente
                if (relevantImages.contains(retrievedImage)) {
                    kPlus++;
                }
                
                double precision = (double) kPlus / k;
                double recall = (double) kPlus / relevantImages.size();
                
                precisionPoints.add(new Point(k, precision));
                recallPoints.add(new Point(k, recall));
            }
            
            // Résultats
            Map<String, Object> result = new HashMap<>();
            result.put("Precision", precisionPoints);
            result.put("Recall", recallPoints);
            result.put("nearestNeighbors", top8Neighbors); // Ajouter les voisins comme liste de chaînes
            
            // Écrire les résultats dans un fichier JSON
            String filePath = "precision_recall_" + queryImage + ".json";
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(Paths.get(filePath).toFile(), result);
            
            logger.info("Fichier JSON généré : {}", filePath);
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            logger.error("Erreur lors du calcul précision/rappel pour l'image {} : {}", queryImage, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Générer une image aléatoire avec classe VT uniquement
     * @return nom de l'image aléatoire
     * @throws IOException en cas d'erreur de lecture
     */
    public String getRandomImageWithVT() throws IOException {
        // Charger toutes les lignes du fichier VT_files
        List<String> vtFilesLines = Files.readAllLines(Paths.get(vtFilesPath));
        
        // Combiner toutes les images listées dans VT_files en une seule liste
        List<String> allVtImages = vtFilesLines.stream()
                                               .flatMap(line -> Arrays.stream(line.split("\\s+"))) // Découper chaque ligne en noms d'images
                                               .distinct() // Supprimer les doublons éventuels
                                               .collect(Collectors.toList());
        
        // Vérifier qu'il y a au moins une image dans la liste
        if (allVtImages.isEmpty()) {
            throw new IllegalStateException("Le fichier VT_files est vide ou aucune image n'appartient à une classe VT.");
        }
        
        // Sélectionner une image aléatoire parmi les images VT
        Random random = new Random();
        String randomImage = allVtImages.get(random.nextInt(allVtImages.size()));
        
        logger.info("Image aléatoire sélectionnée parmi les classes VT : {}", randomImage);
        
        return randomImage; // Retourner le nom de l'image sélectionnée
    }

    /**
     * Calculer la distance euclidienne entre deux vecteurs.
     */
    private double calculateEuclideanDistance(List<Double> desc1, List<Double> desc2) {
        double sum = 0.0;
        for (int i = 0; i < desc1.size(); i++) {
            double diff = desc1.get(i) - desc2.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
    
    /**
     * Charger les descripteurs pour les images de la base
     * @param histogramPath chemin du fichier de descripteurs
     * @param allImageNames noms de toutes les images
     * @return map des descripteurs
     * @throws IOException en cas d'erreur de lecture
     */
    private Map<String, List<Double>> loadDescriptors(String histogramPath, List<String> allImageNames) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(histogramPath));
        Map<String, List<Double>> descriptorMap = new HashMap<>();
        
        for (int i = 0; i < allImageNames.size(); i++) {
            String imageName = allImageNames.get(i);
            List<Double> descriptor = Arrays.stream(lines.get(i).split("\\s+"))
                                            .map(Double::parseDouble)
                                            .collect(Collectors.toList());
            descriptorMap.put(imageName, descriptor);
        }
        
        return descriptorMap;
    }
    
    /**
     * Classe pour représenter un point précision/rappel.
     */
    public static class Point {
        private final double recall;
        private final double precision;
        
        public Point(double recall, double precision) {
            this.recall = recall;
            this.precision = precision;
        }
        
        public double getRecall() {
            return recall;
        }
        
        public double getPrecision() {
            return precision;
        }
    }














//    /**
//     * Calcul principal des courbes précision/rappel.
//     */
//    public Map<String, List<Point>> calculatePrecisionRecall() throws IOException {
//        // Charger les classes VT
//        Map<String, List<String>> groundTruth = loadGroundTruth(vtFilesPath, vtDescriptionPath);
//
//        // Lire les noms des images dans la base
//        List<String> imageNames = Files.readAllLines(Paths.get(base10000files));
//
//        // Lire les descripteurs
//        List<String> gray256Descriptors = Files.readAllLines(Paths.get(histogramGray256));
//        List<String> gray64Descriptors = Files.readAllLines(Paths.get(histogramGray64));
//        List<String> gray16Descriptors = Files.readAllLines(Paths.get(histogramGray16));
//
//        // Préparer les résultats
//        Map<String, List<Point>> results = new LinkedHashMap<>();
//        results.put("Gray256", calculateForHistogram(gray256Descriptors, imageNames, groundTruth));
//        results.put("Gray64", calculateForHistogram(gray64Descriptors, imageNames, groundTruth));
//        results.put("Gray16", calculateForHistogram(gray16Descriptors, imageNames, groundTruth));
//
//        return results;
//    }
//
//    /**
//     * Charger la vérité terrain (classes VT).
//     */
//    private Map<String, List<String>> loadGroundTruth(String vtFilesPath, String vtDescriptionPath) throws IOException {
//        List<String> classNames = Files.readAllLines(Paths.get(vtDescriptionPath));
//        List<List<String>> classImages = Files.readAllLines(Paths.get(vtFilesPath)).stream()
//                                              .map(line -> Arrays.asList(line.split("\\s+")))
//                                              .collect(Collectors.toList());
//
//        Map<String, List<String>> groundTruth = new LinkedHashMap<>();
//        for (int i = 0; i < classNames.size(); i++) {
//            groundTruth.put(classNames.get(i), classImages.get(i));
//        }
//        return groundTruth;
//    }
//
//    /**
//     * Calculer les courbes précision/rappel pour un fichier de descripteurs.
//     */
//    private List<Point> calculateForHistogram(List<String> descriptors, List<String> imageNames, Map<String, List<String>> groundTruth) {
//        List<Point> precisionRecall = new ArrayList<>();
//
//        // Parcourir chaque classe VT
//        for (String className : groundTruth.keySet()) {
//            List<String> relevantImages = groundTruth.get(className);
//            precisionRecall.addAll(calculateForClass(relevantImages, imageNames, descriptors));
//        }
//
//        // Moyenne des résultats pour chaque classe
//        return averagePrecisionRecall(precisionRecall);
//    }
//
//    /**
//     * Calculer précision/rappel pour une classe.
//     */
//    private List<Point> calculateForClass(List<String> relevantImages, List<String> imageNames, List<String> descriptors) {
//        List<Point> points = new ArrayList<>();
//        int m = relevantImages.size(); // Nombre total d'images pertinentes dans la classe
//        int kPlus = 0;
//
//        for (int k = 1; k <= imageNames.size(); k++) {
//            String imageName = imageNames.get(k - 1);
//
//            if (relevantImages.contains(imageName)) {
//                kPlus++;
//            }
//
//            double precision = (double) kPlus / k;
//            double recall = (double) kPlus / m;
//
//            points.add(new Point(recall, precision));
//        }
//
//        return points;
//    }
//
//    /**
//     * Moyenne des courbes précision/rappel.
//     */
//    private List<Point> averagePrecisionRecall(List<Point> points) {
//        Map<Double, List<Double>> recallPrecisionMap = new TreeMap<>();
//        for (Point point : points) {
//            recallPrecisionMap.computeIfAbsent(point.getRecall(), r -> new ArrayList<>()).add(point.getPrecision());
//        }
//
//        List<Point> averagedPoints = new ArrayList<>();
//        for (Map.Entry<Double, List<Double>> entry : recallPrecisionMap.entrySet()) {
//            double recall = entry.getKey();
//            double precision = entry.getValue().stream().mapToDouble(p -> p).average().orElse(0.0);
//            averagedPoints.add(new Point(recall, precision));
//        }
//
//        return averagedPoints;
//    }
//
//    /**
//     * Classe pour représenter un point précision/rappel.
//     */
//    public static class Point {
//        private final double recall;
//        private final double precision;
//
//        public Point(double recall, double precision) {
//            this.recall = recall;
//            this.precision = precision;
//        }
//
//        public double getRecall() {
//            return recall;
//        }
//
//        public double getPrecision() {
//            return precision;
//        }
//    }
}
