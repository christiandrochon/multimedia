package fr.cdrochon.multimedias.evaluationdescripteurs.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import smile.neighbor.KDTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public class PrecisionRappelTousDescripteursService {
    
    Logger logger = LoggerFactory.getLogger(PrecisionRappelTousDescripteursService.class);
    
    @Value("${resnet.VT_files}")
    private String vtFilesPath;
    
    @Value("${resnet.VT_description}")
    private String vtDescriptionPath;
    
    @Value("${images.list.output}")
    private String base10000files;
    
    @Value("${histogram.gray256.output}")
    private String histogramGray256;
    
    @Value("${histogram.rgb666.output}")
    private String histogramRGB666;
    
    @Value("${resnet.resnet18}")
    private String resnet18;
    
    @Value("${pca.target.dimension.tous}")
    private int targetDimension;
    @Value("${precisionrappel.output.tous}")
    private String precisionRappelTous;
    
    @Async
    public CompletableFuture<String> generatePrecisionRecallForAllDescripteurs() {
        logger.info("Début du calcul des courbes précision/rappel pour tous les descripteurs.");
        try {
            // Charger les classes VT
            logger.info("Chargement des classes VT depuis les fichiers : {}, {}", vtFilesPath, vtDescriptionPath);
            Map<String, List<String>> groundTruth = chargerClassesVT(vtFilesPath, vtDescriptionPath);
            
            logger.info("Chargement de la liste complète des images depuis : {}", base10000files);
            List<String> imageNames = Files.readAllLines(Paths.get(base10000files));
            logger.info("Nombre total d'images chargées : {}", imageNames.size());
            
            // Calculer les courbes pour chaque descripteur
            Map<String, List<Point>> precisionRecallData = new LinkedHashMap<>();
            
            // Niveaux de gris
            precisionRecallData.put("Gray256", calculerPrecisionRappelPourClassesVT(histogramGray256, imageNames, groundTruth));
            // Couleurs
            precisionRecallData.put("RGB666", calculerPrecisionRappelPourClassesVT(histogramRGB666, imageNames, groundTruth));
            // ResNet
            precisionRecallData.put("ResNet18", calculerPrecisionRappelPourClassesVT(resnet18, imageNames, groundTruth));
            
            // Générer le fichier JSON
            String jsonFilePath = precisionRappelTous;
            logger.info("Génération du fichier JSON à : {}", jsonFilePath);
            
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> flatData = flattenJson(precisionRecallData);
            mapper.writeValue(Paths.get(jsonFilePath).toFile(), flatData);
            
            logger.info("Fichier JSON généré avec succès : {}", jsonFilePath);
            return CompletableFuture.completedFuture("Fichier JSON généré : " + jsonFilePath);
        } catch(Exception e) {
            logger.error("Erreur lors du calcul des courbes précision/rappel : {}", e.getMessage());
            return CompletableFuture.completedFuture("Erreur : " + e.getMessage());
        }
    }
    
    // Méthode pour aplatir les données pour le fichier JSON
    private List<Map<String, Object>> flattenJson(Map<String, List<Point>> precisionRecallData) {
        
        /// Attribuer une couleur unique à chaque groupe
        Map<String, String> groupColors = Map.of(
                "RGB666", "#FF5733", // Rouge-orangé
                "ResNet18", "#33FF57",  // Vert
                "Gray256", "#3357FF"   // Bleu
                                                );
        
        // Attribuer des noms lisibles pour chaque groupe
        Map<String, String> groupNames = Map.of(
                "RGB666", "Rouge-orangé (RGB666)",
                "ResNet18", "Vert (ResNet18)",
                "Gray256", "Bleu (Grey256)"
                                               );
        
        List<Map<String, Object>> flatData = new ArrayList<>();
        for(Map.Entry<String, List<Point>> entry : precisionRecallData.entrySet()) {
            String group = entry.getKey(); // Nom du descripteur
            String color = groupColors.getOrDefault(group, "#000000"); // Défaut : noir
            String groupName = groupNames.getOrDefault(group, group);
            
            for(Point point : entry.getValue()) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("recall", 100 - point.getRecall()); //inverser l'axe des rappels
                dataPoint.put("precision", 100 * point.getPrecision());
                dataPoint.put("color", color);
                dataPoint.put("group", groupName);
                flatData.add(dataPoint);
            }
        }
        return flatData;
    }
    
    /**
     * Charger les classes VT à partir des fichiers de description et de fichiers
     *
     * @param vtFilesPath       Chemin du fichier VT
     * @param vtDescriptionPath Chemin de la description VT
     * @return Map des classes VT avec les images correspondantes
     *
     * @throws IOException En cas d'erreur lors de la lecture des fichiers
     */
    private Map<String, List<String>> chargerClassesVT(String vtFilesPath, String vtDescriptionPath) throws IOException {
        List<String> classNames = Files.readAllLines(Paths.get(vtDescriptionPath));
        List<List<String>> classImages = Files.readAllLines(Paths.get(vtFilesPath)).stream()
                                              .map(line -> Arrays.asList(line.split("\\s+")))
                                              .collect(Collectors.toList());
        
        Map<String, List<String>> classesVT = new LinkedHashMap<>();
        for(int i = 0; i < classNames.size(); i++) {
            classesVT.put(classNames.get(i), classImages.get(i));
        }
        return classesVT;
    }
    
    /**
     * Calculer les courbes précision/rappel pour un histogramme donné
     *
     * @param histogramPath Chemin du fichier d'histogrammes
     * @param imageNames    Liste des noms d'images
     * @param classesVT   Classes VT avec les images pertinentes
     * @return Liste des points de précision/rappel
     *
     * @throws IOException En cas d'erreur lors de la lecture des descripteurs
     */
    private List<Point> calculerPrecisionRappelPourClassesVT(String histogramPath, List<String> imageNames, Map<String, List<String>> classesVT) throws IOException {
        logger.info("Chargement des descripteurs depuis : {}", histogramPath);
        Map<String, List<Double>> descriptorMap = chargerDescripteurs(histogramPath, imageNames);
        KDTree<String> kdTree = buildKdTree(descriptorMap);
        
        List<Point> precisionRecallPoints = new ArrayList<>();
        for(String className : classesVT.keySet()) {
            List<String> relevantImages = classesVT.get(className);
            
//            // Vérifiez si toutes les images pertinentes ont des descripteurs
//            List<String> missingDescriptors = relevantImages.stream()
//                                                            .filter(image -> !descriptorMap.containsKey(image))
//                                                            .collect(Collectors.toList());
            
            // Pré-calculer les distances de toutes les images pertinentes entre elles
//            Map<String, Map<String, Double>> distanceMap = precomputeDistances(relevantImages, descriptorMap);
//            if(distanceMap.isEmpty()) { //distanceMap sont les 100 images de la classe VT
//                logger.error("Distance map vide pour la classe VT '{}'.", className);
//                continue; // Ignorez cette classe si la distance map est vide
//            }
            
           
            // Parcourir les images requêtes
//            for(String queryImage : relevantImages) {
//                // Trier les images de classe VT de l'image reference, trié par distance au queryImage
//                List<String> sortedRelevantImages = relevantImages.stream()
//                                                                  //                                                                  .filter(image -> !image
//                                                                  //                                                                  .equals(queryImage)) //
//                                                                  //                                                                  Exclure l'image requête
//                                                                  //                                                                  elle-même
//                                                                  .sorted(Comparator.comparingDouble(image -> distanceMap.get(queryImage).get(image)))
//                                                                  .limit(50) // Limiter à 50 images de la classe VT
//                                                                  .collect(Collectors.toList());
//
//                // Calcul précision/rappel pour les images triées
//                precisionRecallPoints.addAll(calculateForQuery(queryImage, sortedRelevantImages, kdTree, descriptorMap));
//            }
            
            List<String> reducedRelevantImages = relevantImages.subList(0, Math.min(100, relevantImages.size()));
            for(String queryImage : reducedRelevantImages) {
                precisionRecallPoints.addAll(calculerPrecisionRappelPourImageReference(queryImage, relevantImages, kdTree, descriptorMap));
            }
        }
        return consolidatePoints(precisionRecallPoints, 0.01);
    }
    
    /**
     * Calculer les points de précision/rappel pour les images de reference
     *
     * @param queryImage     Image de référence
     * @param relevantImages Images pertinentes
     * @param kdTree         Arbre KD optimisé pour la recherche des voisins les plus proches
     * @param descriptorMap  Map des descripteurs
     * @return Liste des points de précision/rappel
     */
    private List<Point> calculerPrecisionRappelPourImageReference(String queryImage, List<String> relevantImages, KDTree<String> kdTree, Map<String, List<Double>> descriptorMap) {
        
        List<Point> points = new ArrayList<>();
        double[] queryDescriptor = descriptorMap.get(queryImage).stream().mapToDouble(Double::doubleValue).toArray();
        List<String> sortedRetrievedImages = Arrays.stream(kdTree.knn(queryDescriptor, 10000))
                                                   .map(neighbor -> neighbor.value)
                                                   .collect(Collectors.toList());
        
        int kPlus = 0; // Nombre d'éléments pertinents récupérés
        int kppv = 10000; // Nombre d'images voisines de l'image requête
        for(int k = 1; k < Math.min(sortedRetrievedImages.size(), kppv); k++) {
            String retrievedImage = sortedRetrievedImages.get(k -1);
            if(relevantImages.contains(retrievedImage)) {
                kPlus++;
            }
            // Valeur de précision et de rappel entre 0 et 1
            // Calculer Précision et Rappel
            double precision = (double) kPlus / k; // |A ∩ C| / |A|
            double recall = (double) kPlus / relevantImages.size(); // |A ∩ C| / |C|
            // Ajouter le point précision/rappel
            points.add(new Point(recall * 100, precision * 100)); // Multiplier par 100 pour avoir un pourcentage
            
            //            // Calcul en pourcentage
            //            double recall = ((double) kPlus / relevantImages.size()) * 100;
            //            double precision = ((double) kPlus / (k + 1)) * 100;
            //            points.add(new Point(recall, precision));
        }
        
        // Trier les points par rappel croissant
        points.sort(Comparator.comparingDouble(Point::getRecall));
        
        return points;
    }
    
    /**
     * Méthode pour pré-calculer les distances entre toutes les images pertinentes pour une classe VT donnée. Sert à optimiser le calcul des précisions et
     * rappels pour chaque image requête. Il permet notamment de trier les images les plus pertinentes avant de boucler au lieu de recuperer les images dans
     * l'ordre de la base d'images
     *
     * @param images        liste des images pertinentes pour une classe VT donnée
     * @param descriptorMap map associant chaque image à son descripteur
     * @return map associant chaque image à une map de distances avec les autres images pertinentes
     */
    private Map<String, Map<String, Double>> precomputeDistances(List<String> images, Map<String, List<Double>> descriptorMap) {
        Map<String, Map<String, Double>> distanceMap = new HashMap<>();
        
        for(String image1 : images) {
            Map<String, Double> innerMap = new HashMap<>();
            List<Double> descriptor1 = descriptorMap.get(image1);
            
            for(String image2 : images) {
                if(!image1.equals(image2)) { // Pas besoin de calculer la distance pour soi-même
                    double distance = calculateEuclideanDistance(descriptor1, descriptorMap.get(image2));
                    innerMap.put(image2, distance);
                }
            }
            
            distanceMap.put(image1, innerMap);
        }
        
        return distanceMap;
    }
    
    /**
     * Calcule la distance euclidienne entre deux descripteurs, ce qui me permet de trier les images pertinentes par distance au lieu de les récupérer dans
     * l'ordre de la base d'images
     *
     * @param descriptor1 descripteur de la première image
     * @param descriptor2 descripteur de la deuxième image
     * @return distance euclidienne entre les deux descripteurs
     */
    private double calculateEuclideanDistance(List<Double> descriptor1, List<Double> descriptor2) {
        double sum = 0.0;
        for(int i = 0; i < descriptor1.size(); i++) {
            double diff = descriptor1.get(i) - descriptor2.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
    
    /**
     * Construire un arbre KD à partir des descripteurs normalisés. Cet arbre est optimisé pour la recherche des voisins les plus proches. Il fit passer la
     * complexité de la recherche de O(n) à O(log n).
     *
     * @param descriptorMap Map des descripteurs
     * @return Arbre KD optimisé
     */
    private KDTree<String> buildKdTree(Map<String, List<Double>> descriptorMap) {
        double[][] points = descriptorMap.values().stream()
                                         .map(descriptor -> descriptor.stream().mapToDouble(Double::doubleValue).toArray())
                                         .toArray(double[][]::new);
        String[] labels = descriptorMap.keySet().toArray(new String[0]);
        return new KDTree<>(points, labels);
    }

    /**
     * Consolider les points de précision/rappel en fonction de la granularité
     *
     * @param points      Liste des points de précision/rappel
     * @param granularity Granularité pour l'arrondi
     * @return Liste des points consolidés
     */
    private List<Point> consolidatePoints(List<Point> points, double granularity) {
        Map<Double, List<Double>> recallPrecisionMap = new TreeMap<>();
        
        for(Point point : points) {
            double roundedRecall = Math.round(point.getRecall() / granularity) * granularity;
            recallPrecisionMap.computeIfAbsent(roundedRecall, r -> new ArrayList<>()).add(point.getPrecision());
        }
        
        recallPrecisionMap.putIfAbsent(1.0, new ArrayList<>()); // Ajouter recall = 1.0 si absent
        
        //        List<Point> consolidatedPoints = new ArrayList<>();
        //        for(Map.Entry<Double, List<Double>> entry : recallPrecisionMap.entrySet()) {
        //            double recall = entry.getKey();
        //            double precision = entry.getValue().stream().mapToDouble(p -> p).average().orElse(0.0);
        //            consolidatedPoints.add(new Point(recall, precision));
        //        }
        List<Point> consolidatedPoints = recallPrecisionMap.entrySet().stream()
                                                           .map(entry -> {
                                                               double recall = entry.getKey();
                                                               double precision = entry.getValue().stream().mapToDouble(p -> p).average().orElse(0.0);
                                                               return new Point(recall, precision);
                                                           })
                                                           .collect(Collectors.toList());
        
        // Pas besoin d'inverser les données ici
        return consolidatedPoints;
    }

    /**
     * Charger les descripteurs à partir du fichier d'histogrammes. La methode permet de normaliser les longueurs des descripteurs en utilisant PCA.
     *
     * @param histogramPath Chemin du fichier d'histogrammes
     * @param imageNames    Liste des noms d'images
     * @return Map des descripteurs
     *
     * @throws IOException En cas d'erreur lors de la lecture du fichier
     */
    private Map<String, List<Double>> chargerDescripteurs(String histogramPath, List<String> imageNames) throws IOException {
        logger.info("Lecture des descripteurs à partir de : {}", histogramPath);
        
        int numDescriptors;
        logger.info("RESNET CONTIENT  : {}", histogramPath.contains("RENSET18"));
        if(histogramPath.contains("GREY_256")) {
            numDescriptors = 256;
        } else if(histogramPath.contains("6x6x6")) {
            numDescriptors = 216;
        } else if(histogramPath.contains("RESNET18")) {
            numDescriptors = 512;
        } else {
            numDescriptors = 0;
        }
        
        // Lire toutes les lignes du fichier
        List<String> lines = Files.readAllLines(Paths.get(histogramPath));
        logger.info("Nombre de lignes lues : {}", lines.size());
        
        Map<String, List<Double>> descriptorMap = new HashMap<>();
        for(int i = 0; i < imageNames.size(); i++) {
            String imageName = imageNames.get(i);
            if(i >= lines.size()) {
                logger.warn("Pas assez de lignes dans le fichier pour l'image : {}", imageName);
                continue;
            }
            
                        String line = lines.get(i).trim();
                        if(line.isEmpty()) {
                            logger.warn("Ligne vide rencontrée pour l'image : {}", imageName);
                            continue;
                        }
            
            String[] lineParts = lines.get(i).split("\\s+");
            lineParts = Arrays.stream(lineParts)
                              .filter(part -> !part.isEmpty())
                              .toArray(String[]::new);
            logger.info("Image : {}, Ligne brute : {}", imageName, Arrays.toString(lineParts));
            
            // Vérification : la ligne doit contenir exactement le bon nombre de descripteurs
            if(lineParts.length < numDescriptors) {
                logger.warn("Ligne {} ne contient pas suffisamment de descripteurs. Attendu : {}, Trouvé : {}", i, numDescriptors, lineParts.length);
                continue; // Ignorer les lignes incorrectes
            } else if(lineParts.length > numDescriptors) {
                logger.warn("Ligne {} contient trop de descripteurs. Attendu : {}, Trouvé : {}", i, numDescriptors, lineParts.length);
                lineParts = Arrays.copyOf(lineParts, numDescriptors); // Tronquer les descripteurs supplémentaires
            }
            
            // Convertir chaque descripteur en Double
            List<Double> descriptor = Arrays.stream(lineParts)
                                            .map(Double::parseDouble)
                                            .collect(Collectors.toList());
            
            // Vérifier ou corriger la somme des descripteurs s'ils ne font pas 1.0
            double sum = descriptor.stream().mapToDouble(Double::doubleValue).sum();
            if(sum < 0.99 || sum > 1.01) { // Tolérance ±0.01
                logger.warn("Descripteur anormal EN ENTREE détecté pour l'image {} : somme = {}", imageName, sum);
            }
            
            descriptorMap.put(imageName, descriptor);
        }
        logger.info("Descripteurs chargés avec succès : {} descripteurs.", descriptorMap.size());
        
        
        //PCA
        // Calculer la dimension maximale réelle
        int maxDimension = getMaxDimension(descriptorMap);
        logger.info("Dimension maximale réelle des descripteurs : {}", maxDimension);
        
        // Ajuster la dimension cible
        if(targetDimension > maxDimension) {
            logger.warn("Dimension cible ({}) dépasse la dimension maximale réelle ({}). Ajustement automatique.", targetDimension, maxDimension);
            targetDimension = maxDimension;
        }
        
        // Normaliser les longueurs des descripteurs. Ici, je lui envoie les 100 images VT pour qu'il normalise les descripteurs de ces images
        descriptorMap = DescriptorNormalizer.normalizeDescriptors(descriptorMap, targetDimension);
        
        //        // Vérification des descripteurs après normalisation s'ils sont toujours entre 0 et 1!! Pour verifier la normalisation!!!
        //        for(String image : descriptorMap.keySet()) {
        //            double sum = descriptorMap.get(image).stream().mapToDouble(Double::doubleValue).sum();
        //            if(sum > 1.01 || sum < 0.99) { // Tolérance pour les erreurs flottantes
        //                logger.warn("Descripteur anormal APRES NORMALISATION pour l'image {} : somme = {}", image, sum);
        //            }
        //        }
        
        
        return descriptorMap; //renvoi 10000 vecteurs de longeueur 181
    }
    
    /**
     * Calculer la dimension maximale des descripteurs
     *
     * @param descriptorMap Map des descripteurs
     * @return Dimension maximale
     */
    private int getMaxDimension(Map<String, List<Double>> descriptorMap) {
        return descriptorMap.values().stream()
                            .mapToInt(List::size)
                            .max()
                            .orElse(0); // Retourne 0 si la map est vide
    }
    
    
    /**
     * Classe interne pour stocker les points de précision/rappel
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
}
