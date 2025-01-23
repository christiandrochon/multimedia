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
public class PrecisionRappelGrisService {
    
    Logger logger = LoggerFactory.getLogger(PrecisionRappelGrisService.class);
    
    @Value("${resnet.VT_files}")
    private String vtFilesPath;
    
    @Value("${resnet.VT_description}")
    private String vtDescriptionPath;
    
    @Value("${images.list.output}")
    private String base10000files;
    
    @Value("${histogram.gray256.output}")
    private String histogramGray256;
    
    @Value("${histogram.gray64.output}")
    private String histogramGray64;
    
    @Value("${histogram.gray16.output}")
    private String histogramGray16;
    @Value("${pca.target.dimension.gris}")
    private int targetDimension;
    @Value("${precisionrappel.output.gris}")
    private String precisionRappelGrisOutput;
    
    @Async
    public CompletableFuture<String> generatePrecisionRecallForGris() {
        logger.info("Début du calcul des courbes précision/rappel pour les histogrammes de niveaux de gris.");
        try {
            // Étape 1 : Charger les classes VT
            logger.info("Chargement des classes VT depuis les fichiers : {}, {}", vtFilesPath, vtDescriptionPath);
            Map<String, List<String>> groundTruth = chargerClassesVT(vtFilesPath, vtDescriptionPath);
            
            logger.info("Chargement de la liste complète des images depuis : {}", base10000files);
            List<String> imageNames = Files.readAllLines(Paths.get(base10000files));
            logger.info("Nombre total d'images chargées : {}", imageNames.size());
            
            
            // Étape 2 : Calculer les courbes précision/rappel pour chaque descripteur
            // Calculer les courbes pour chaque descripteur
            Map<String, List<Point>> precisionRecallData = new LinkedHashMap<>();
            
            // Niveaux de gris
            precisionRecallData.put("Gray256", calculerPrecisionRappelPourClassesVT(histogramGray256, imageNames, groundTruth));
            // Couleurs
            precisionRecallData.put("Gray64", calculerPrecisionRappelPourClassesVT(histogramGray64, imageNames, groundTruth));
            // ResNet
            precisionRecallData.put("Gray16", calculerPrecisionRappelPourClassesVT(histogramGray16, imageNames, groundTruth));
            //            Map<String, List<Point>> precisionRecallData = new LinkedHashMap<>();
            //
            //            logger.info("Début du calcul des courbes pour Gray256.");
            //            List<Point> gray256Points = calculateForHistogram(histogramGray256, imageNames, groundTruth);
            //            precisionRecallData.put("Gray256", gray256Points);
            //            logger.info("Courbe pour Gray256 terminée avec {} points.", gray256Points.size());
            //
            //            logger.info("Début du calcul des courbes pour Gray64.");
            //            List<Point> gray64Points = calculateForHistogram(histogramGray64, imageNames, groundTruth);
            //            precisionRecallData.put("Gray64", gray64Points);
            //            logger.info("Courbe pour Gray64 terminée avec {} points.", gray64Points.size());
            //
            //            logger.info("Début du calcul des courbes pour Gray16.");
            //            List<Point> gray16Points = calculateForHistogram(histogramGray16, imageNames, groundTruth);
            //            precisionRecallData.put("Gray16", gray16Points);
            //            logger.info("Courbe pour Gray16 terminée avec {} points.", gray16Points.size());
            //
            //            logger.info("Contenu des courbes : {}", precisionRecallData);
            
            
            // Étape 3 : Générer le fichier JSON
            String jsonFilePath = precisionRappelGrisOutput;
            logger.info("Génération du fichier JSON à : {}", jsonFilePath);
            
            ObjectMapper mapper = new ObjectMapper();
            //            mapper.writeValue(Paths.get(jsonFilePath).toFile(), precisionRecallData);
            // pour ajouter les groupes à afficher sur rawgraphs
            List<Map<String, Object>> flatData = flattenJson(precisionRecallData);
            mapper.writeValue(Paths.get(jsonFilePath).toFile(), flatData);
            
            
            logger.info("Fichier JSON généré avec succès : {}", jsonFilePath);
            return CompletableFuture.completedFuture("Fichier JSON généré : " + jsonFilePath);
        } catch(Exception e) {
            logger.error("Erreur lors du calcul des courbes précision/rappel : {}", e.getMessage());
            return CompletableFuture.completedFuture("Erreur : " + e.getMessage());
        }
    }
    
    /**
     * Methode dstinées uniquement  à faire fonctionner l'afficher des courbes sur rawgraphs
     *
     * @param precisionRecallData les données de précision/rappel
     * @return les données de précision/rappel sous forme de liste de map
     */
    private List<Map<String, Object>> flattenJson(Map<String, List<Point>> precisionRecallData) {
        List<Map<String, Object>> flatData = new ArrayList<>();
        
        // Attribuer une couleur unique à chaque groupe
        Map<String, String> groupColors = Map.of(
                "Gray256", "#FF5733", // Rouge-orangé
                "Gray64", "#33FF57",  // Vert
                "Gray16", "#3357FF"   // Bleu
                                                );
        
        // Attribuer des noms lisibles pour chaque groupe
        Map<String, String> groupNames = Map.of(
                "Gray256", "Rouge-orangé (Grey256)",
                "Gray64", "Vert (Grey64)",
                "Gray16", "Bleu (Grey16)"
                                               );
        
        for(Map.Entry<String, List<Point>> entry : precisionRecallData.entrySet()) {
            String group = entry.getKey(); // Nom de la courbe (Gray256, Gray64, Gray16)
            String color = groupColors.getOrDefault(group, "#000000"); // Défaut : noir
            String groupName = groupNames.getOrDefault(group, group);
            
            for(Point point : entry.getValue()) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("recall", 100 - point.getRecall());
                dataPoint.put("precision", 100 * point.getPrecision());
                dataPoint.put("group", groupName); // Nom lisible pour légende
                dataPoint.put("color", color); // Ajout de la couleur
                flatData.add(dataPoint);
            }
        }
        
        return flatData;
    }
    
    /**
     * Charger les classes VT à partir des fichiers VT_Files et VT_Description.
     *
     * @param vtFilesPath       chemin du fichier VT_Files
     * @param vtDescriptionPath chemin du fichier VT_Description
     * @return map associant chaque classe VT à ses images
     *
     * @throws IOException si une erreur d'entrée/sortie se produit
     */
    private Map<String, List<String>> chargerClassesVT(String vtFilesPath, String vtDescriptionPath) throws IOException {
        logger.info("Lecture des fichiers VT_Files et VT_Description.");
        List<String> classNames = Files.readAllLines(Paths.get(vtDescriptionPath));
        List<List<String>> classImages = Files.readAllLines(Paths.get(vtFilesPath)).stream()
                                              .map(line -> Arrays.asList(line.split("\\s+")))
                                              .collect(Collectors.toList());
        
        Map<String, List<String>> classesVT = new LinkedHashMap<>();
        for(int i = 0; i < classNames.size(); i++) {
            classesVT.put(classNames.get(i), classImages.get(i));
        }
        logger.info("Classes VT chargées avec succès : {} classes trouvées.", classNames.size());
        return classesVT;
    }
    
    /**
     * Calculer les courbes précision/rappel pour les descripteurs d'histogrammes de niveaux de gris.
     *
     * @param histogramPath chemin du fichier contenant les descripteurs
     * @param imageNames    liste des noms des images
     * @param classesVT     map associant chaque classe VT à ses images
     * @return liste des points précision/rappel calculés
     *
     * @throws IOException si une erreur d'entrée/sortie se produit
     */
    private List<Point> calculerPrecisionRappelPourClassesVT(String histogramPath, List<String> imageNames,
                                                             Map<String, List<String>> classesVT) throws IOException {
        logger.info("Chargement des descripteurs depuis : {}", histogramPath);
        Map<String, List<Double>> descriptorMap = chargerDescripteurs(histogramPath, imageNames);
        KDTree<String> kdTree = buildKdTree(descriptorMap);
        
        List<Point> precisionRecallPoints = new ArrayList<>();
        for(String className : classesVT.keySet()) {
            List<String> relevantImages = classesVT.get(className);
            
            // Vérifiez si toutes les images pertinentes ont des descripteurs
//            List<String> missingDescriptors = relevantImages.stream()
//                                                            .filter(image -> !descriptorMap.containsKey(image))
//                                                            .collect(Collectors.toList());
//
//            if(!missingDescriptors.isEmpty()) {
//                logger.warn("Classe VT '{}' : descripteurs manquants pour les images : {}", className, missingDescriptors);
//                continue; // Ignorez cette classe si des descripteurs sont manquants
//            }
            
            //            logger.info("Calcul des distances pour la classe VT '{}' avec {} images.", className, relevantImages.size());
//            Map<String, Map<String, Double>> distanceMap = precomputeDistances(relevantImages, descriptorMap);
//            if(distanceMap.isEmpty()) { //distanceMap sont les 100 images de la classe VT
//                logger.error("Distance map vide pour la classe VT '{}'.", className);
//                continue; // Ignorez cette classe si la distance map est vide
//            }
            
//            // Parcourir les images requêtes
//            for(String queryImage : relevantImages) {
//                List<String> sortedRelevantImages = relevantImages.stream()
//                                                                  .filter(image -> !image.equals(queryImage)) // Exclure l'image requête elle-même. Surtout
//                                                                  // NE PAS SUPPRIMER CETTE LIGNE!! Pourtant, il faudrait laisser 'image requete dans la
//                                                                  // liste des images pertinentes
//                                                                  .sorted(Comparator.comparingDouble(image -> distanceMap.get(queryImage).get(image)))
//                                                                  .limit(100) // Limiter à 100 images VT les plus proches
//                                                                  .collect(Collectors.toList());
//
//                // Calcul précision/rappel pour les images triées
//                precisionRecallPoints.addAll(calculerPrecisionRappelPourImageReference(queryImage, sortedRelevantImages, kdTree, descriptorMap));
//            }
            // les 100 images de la classe VT sont importées
                        List<String> reducedRelevantImages = relevantImages.subList(0, Math.min(100, relevantImages.size()));
                        for(String queryImage : reducedRelevantImages) {
                            precisionRecallPoints.addAll(calculerPrecisionRappelPourImageReference(queryImage, relevantImages, kdTree, descriptorMap));
                        }
        }
        return consolidatePoints(precisionRecallPoints, 0.01);
    }
    
    /**
     * Calcule la précision et le rappel pour une image requête donnée
     *
     * @param queryImage     image requête pour laquelle calculer la précision et le rappel. Cette imae appartient  à une classe VT. Elle change  à chaque
     *                       iteration de la boucle. Son descripteur est extrait à partir de descriptorMap et utilisé pour rechercher les kppv dans la base
     *                       d'images via le k-d tree.
     * @param relevantImages Cette liste contient toutes les images qui appartiennent à la même classe VT que queryImage. Elle est utilisée pour vérifier si les
     *                       k-NN retournés par le système (les résultats) sont pertinents, c'est-à-dire s'ils appartiennent à cette liste. Cette liste est
     *                       extraite à partir du fichier VT_Files, où chaque ligne représente une classe VT et contient les noms des images appartenant à cette
     *                       classe.
     * @param kdTree         k-d tree contient les descripteurs (vecteurs de caractéristiques) de toutes les images dans la base, organisés pour permettre une
     *                       recherche rapide. Le k-d tree est construit à partir de descriptorMap, où chaque descripteur d'image est un vecteur de
     *                       caractéristiques. Si le descripteur de queryImage est [0.5, 0.2, 0.7], le 𝑘 k-d tree renvoie les noms des 𝑘 k-images les plus
     *                       proches, comme [img3.jpg, img7.jpg, img8.jpg].
     * @param descriptorMap  associe chaque image de la base à son descripteur (vecteur de caractéristiques). Le descripteur est un vecteur de caractéristiques
     * @return liste des points précision/rappel calculés
     */
    private Collection<? extends Point> calculerPrecisionRappelPourImageReference(String queryImage, List<String> relevantImages, KDTree<String> kdTree,
                                                                                  Map<String, List<Double>> descriptorMap) {
        
        double[] queryDescriptor = descriptorMap.get(queryImage).stream().mapToDouble(Double::doubleValue).toArray();
        // un kdtree est dejà trié et optimisé pour cela! Ne pas l'ajouter car ca transforme les courbes!
        List<String> sortedRetrievedImages = Arrays.stream(kdTree.knn(queryDescriptor, 10000))
                                                   .map(neighbor -> neighbor.value)
                                                   .collect(Collectors.toList());
        // Vérification de la liste des voisins
        if(sortedRetrievedImages.isEmpty()) {
            logger.error("Aucun voisin trouvé pour l'image requête : {}", queryImage);
            return Collections.emptyList();
        }
        
        List<Point> points = new ArrayList<>();
        int kPlus = 0; // Nombre d'éléments pertinents récupérés
        int kppv = 10000; // Nombre d'images voisines de l'image requête
        int totalRelevant = relevantImages.size(); // Total des images pertinentes (|C|)
        for(int k = 1; k < sortedRetrievedImages.size(); k++) {
            String retrievedImage = sortedRetrievedImages.get(k);
            if(relevantImages.contains(retrievedImage)) {
                kPlus++;
            }
            // Valeur de précision et de rappel entre 0 et 1
            //            double recall = (double) kPlus / relevantImages.size();
            //            double precision = (double) kPlus / (k + 1);
            // Calcul en pourcentage
            double recall = (double) kPlus / relevantImages.size();
            double precision = (double) kPlus / (k + 1);
            
            points.add(new Point(recall * 100, precision * 100));
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
            // Vérifiez que l'image1 a un descripteur
            if(!descriptorMap.containsKey(image1)) {
                logger.error("Descripteur manquant pour l'image (image1) : {}", image1);
                continue; // Passez à l'image suivante
            }
            List<Double> descriptor1 = descriptorMap.get(image1);
            
            Map<String, Double> innerMap = new HashMap<>();
            for(String image2 : images) {
                // Ignorez les comparaisons avec soi-même
                if(image1.equals(image2)) {
                    continue;
                }
                // Vérifiez que l'image2 a un descripteur
                if(!descriptorMap.containsKey(image2)) {
                    logger.error("Descripteur manquant pour l'image (image2) : {}", image2);
                    continue; // Ignorez cette image
                }
                List<Double> descriptor2 = descriptorMap.get(image2);
                
                // Vérifiez que les descripteurs ne sont pas nuls
                if(descriptor1 == null || descriptor2 == null) {
                    logger.error("Descripteurs null détectés pour les images : {} ou {}", image1, image2);
                    continue; // Ignorez cette comparaison
                }
                
                // Calcul de la distance
                double distance = calculateEuclideanDistance(descriptor1, descriptor2);
                innerMap.put(image2, distance);
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
    
    private KDTree<String> buildKdTree(Map<String, List<Double>> descriptorMap) {
        double[][] points = descriptorMap.values().stream()
                                         .map(descriptor -> descriptor.stream().mapToDouble(Double::doubleValue).toArray())
                                         .toArray(double[][]::new);
        String[] labels = descriptorMap.keySet().toArray(new String[0]);
        return new KDTree<>(points, labels);
    }

    /**
     * Calculer les points de précision et de rappel pour les descripteurs d'histogrammes de niveaux de gris.
     *
     * @param points      liste des points de précision et de rappel
     * @param granularity granularité pour la consolidation des points
     * @return liste des points consolidés
     */
    private List<Point> consolidatePoints(List<Point> points, double granularity) {
        logger.info("Consolidation des points précision/rappel.");
        Map<Double, List<Double>> recallPrecisionMap = new TreeMap<>();
        
        for(Point point : points) {
            double roundedRecall = Math.round(point.getRecall() / granularity) * granularity;
            recallPrecisionMap.computeIfAbsent(roundedRecall, r -> new ArrayList<>()).add(point.getPrecision());
        }
        
        recallPrecisionMap.putIfAbsent(1.0, new ArrayList<>()); // Ajouter recall = 1.0 si absent
        // Reattribuer les valeurs de précision dans l'ordre inverse des valeurs de rappel
        //        List<Point> consolidatedPoints = new ArrayList<>();
        
        //        List<Point> consolidatedPoints = new ArrayList<>();
        List<Point> consolidatedPoints = new ArrayList<>();
        for(Map.Entry<Double, List<Double>> entry : recallPrecisionMap.entrySet()) {
            double recall = entry.getKey();
            double precision = entry.getValue().stream().mapToDouble(p -> p).average().orElse(0.0);
            consolidatedPoints.add(new Point(recall, precision));
        }
        
        logger.info("Points consolidés : {} points générés.", consolidatedPoints.size());
        return consolidatedPoints;
    }
    
    /**
     * Charger les descripteurs d'une image à partir du fichier spécifié.
     *
     * @param histogramPath chemin du fichier contenant les descripteurs
     * @param imageNames    liste des noms des images
     * @return map associant chaque image à ses descripteurs
     *
     * @throws IOException si une erreur d'entrée/sortie se produit
     */
    private Map<String, List<Double>> chargerDescripteurs(String histogramPath, List<String> imageNames) throws IOException {
        logger.info("Lecture des descripteurs à partir de : {}", histogramPath);
        
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
            
            //            String line = lines.get(i).trim();
            //            if(line.isEmpty()) {
            //                logger.warn("Ligne vide rencontrée pour l'image : {}", imageName);
            //                continue;
            //            }
            
            String[] lineParts = lines.get(i).split("\\s+");
            logger.info("Image : {}, Ligne brute : {}", imageName, Arrays.toString(lineParts));
            
            // Convertir chaque élément en Double
            List<Double> descriptor = Arrays.stream(lineParts)
                                            .map(Double::parseDouble)
                                            .collect(Collectors.toList());
            
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
        
        // Normaliser les longueurs des descripteurs
        descriptorMap = DescriptorNormalizer.normalizeDescriptors(descriptorMap, targetDimension);
        
        return descriptorMap;
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
     * Classe interne pour stocker les points de précision et de rappel
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
