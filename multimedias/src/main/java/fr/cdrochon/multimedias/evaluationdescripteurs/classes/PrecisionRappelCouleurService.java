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
public class PrecisionRappelCouleurService {
    
    Logger logger = LoggerFactory.getLogger(PrecisionRappelCouleurService.class);
    
    @Value("${resnet.VT_files}")
    private String vtFilesPath;
    
    @Value("${resnet.VT_description}")
    private String vtDescriptionPath;
    
    @Value("${images.list.output}")
    private String base10000files;
    
    @Value("${histogram.rgb666.output}")
    private String histogramRGB666;
    
    @Value("${histogram.rgb444.output}")
    private String histogramRGB444;
    
    @Value("${histogram.rgb222.output}")
    private String histogramRGB222;
    @Value("${pca.target.dimension.couleur}")
    private int targetDimension;
    @Value("${precisionrappel.output.couleur}")
    private String precisionRappelCouleur;
    
    @Async
    public CompletableFuture<String> generatePrecisionRecallForCouleur() {
        logger.info("Début du calcul des courbes précision/rappel pour les histogrammes de niveaux de couleur.");
        try {
            // Étape 1 : Charger les classes VT
            logger.info("Chargement des classes VT depuis les fichiers : {}, {}", vtFilesPath, vtDescriptionPath);
            Map<String, List<String>> classesVT = chargerClassesVT(vtFilesPath, vtDescriptionPath);
            
            logger.info("Chargement de la liste complète des images depuis : {}", base10000files);
            List<String> imageNames = Files.readAllLines(Paths.get(base10000files));
            logger.info("Nombre total d'images chargées : {}", imageNames.size());
            
            
            // Étape 2 : Calculer les courbes précision/rappel pour chaque descripteur
            Map<String, List<Point>> precisionRecallData = new LinkedHashMap<>();
            
            logger.info("Début du calcul des courbes pour histogramRGB666.");
            List<Point> RGB666Points = calculerPrecisionRappelPourClasseVT(histogramRGB666, imageNames, classesVT);
            precisionRecallData.put("RGB666", RGB666Points);
            logger.info("Courbe pour RGB666 terminée avec {} points.", RGB666Points.size());
            
            logger.info("Début du calcul des courbes pour histogramRGB444.");
            List<Point> RGB4444Points = calculerPrecisionRappelPourClasseVT(histogramRGB444, imageNames, classesVT);
            precisionRecallData.put("RGB444", RGB4444Points);
            logger.info("Courbe pour RGB444 terminée avec {} points.", RGB4444Points.size());
            
            logger.info("Début du calcul des courbes pour histogramRGB222.");
            List<Point> RGB222Points = calculerPrecisionRappelPourClasseVT(histogramRGB222, imageNames, classesVT);
            precisionRecallData.put("RGB222", RGB222Points);
            logger.info("Courbe pour RGB222 terminée avec {} points.", RGB222Points.size());
            
            logger.info("Contenu des courbes : {}", precisionRecallData);
            
            
            // Étape 3 : Générer le fichier JSON
            String jsonFilePath = precisionRappelCouleur;
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
     * Charger les classes VT à partir des fichiers VT_Files et VT_Description
     *
     * @param vtFilesPath       chemin du fichier VT_Files
     * @param vtDescriptionPath chemin du fichier VT_Description
     * @return map associant chaque classe VT à ses images
     *
     * @throws IOException si une erreur de lecture du fichier se produit
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
     * Charger les descripteurs de couleur d'une image à partir du fichier spécifié. Methode générique qui détecte le nombre de descripteurs contenu dans un
     * fichier précis, les fichiers de desscripteurss ne contenant pas tous le meme nombre descripteur pour une meme image
     *
     * @param histogramPath chemin du fichier contenant les descripteurs
     * @param imageNames    liste des noms des images
     * @param classesVT   map associant chaque classe VT à ses images
     * @return liste des points précision/rappel calculés
     *
     * @throws IOException si une erreur de lecture du fichier se produit
     */
    private List<Point> calculerPrecisionRappelPourClasseVT(String histogramPath, List<String> imageNames,
                                                            Map<String, List<String>> classesVT) throws IOException {
        
        logger.info("Chargement des descripteurs depuis : {}", histogramPath);
        Map<String, List<Double>> descriptorMap = chargerDescripteurs(histogramPath, imageNames);
        
        // Construire le k-d tree
        logger.info("Construction du k-d tree pour les descripteurs.");
        KDTree<String> kdTree = buildKdTree(descriptorMap);
        
        List<Point> precisionRecallPoints = new ArrayList<>();
        for(String className : classesVT.keySet()) {
            List<String> relevantImages = classesVT.get(className); // Images pertinentes pour la classe VT
            //            logger.info("Images pertinentes pour la classe VT '{}': {}", className, relevantImages);
            
//            // Vérifiez si toutes les images pertinentes ont des descripteurs
//            List<String> missingDescriptors = relevantImages.stream()
//                                                            .filter(image -> !descriptorMap.containsKey(image))
//                                                            .collect(Collectors.toList());
//
//            if(!missingDescriptors.isEmpty()) {
//                logger.warn("Classe VT '{}' : descripteurs manquants pour les images : {}", className, missingDescriptors);
//                continue; // Ignorez cette classe si des descripteurs sont manquants
//            }
//
//            //            logger.info("Calcul des distances pour la classe VT '{}' avec {} images.", className, relevantImages.size());
//            Map<String, Map<String, Double>> distanceMap = precomputeDistances(relevantImages, descriptorMap);
//            if(distanceMap.isEmpty()) { //distanceMap sont les 100 images de la classe VT
//                logger.error("Distance map vide pour la classe VT '{}'.", className);
//                continue; // Ignorez cette classe si la distance map est vide
//            }
//
//            // Parcourir les images requêtes
//            for(String queryImage : relevantImages) {
//                List<String> sortedRelevantImages = relevantImages.stream()
//                                                                  .filter(image -> !image.equals(queryImage)) // Exclure l'image requête elle-même. Surtout
//                                                                  // NE PAS SUPPRIMER CETTE LIGNE!! Pourtant, il faudrait laisser 'image requete dans la
//                                                                  // liste des images pertinentes
//                                                                  .sorted(Comparator.comparingDouble(image -> distanceMap.get(queryImage).get(image)))
//                                                                  .limit(50) // Limiter à 100 images VT les plus proches
//                                                                  .collect(Collectors.toList());
//
//                // Calcul précision/rappel pour les images triées
//                precisionRecallPoints.addAll(calculerPrecisionRappelPourImageReference(queryImage, sortedRelevantImages, kdTree, descriptorMap));
//            }
            
            List<String> reducedRelevantImages = relevantImages.subList(0, Math.min(100, relevantImages.size()));
            for(String queryImage : reducedRelevantImages) {
                precisionRecallPoints.addAll(calculerPrecisionRappelPourImageReference(queryImage, relevantImages, kdTree, descriptorMap));
            }
        }
        
        // Choix au niveau de la consolidation avec un nombre défini de points
        return consolidatePoints(precisionRecallPoints, 0.01); // Exemple : 100 points
        //        return moyennePointsPrecisionRappel(precisionRecallPoints, 0.01); // Granularité pour 100 points
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
    private List<Point> calculerPrecisionRappelPourImageReference(String queryImage, List<String> relevantImages, KDTree<String> kdTree,
                                                                  Map<String, List<Double>> descriptorMap) {
        //        logger.info("Calcul précision/rappel pour l'image requête : {}", queryImage);
        
        List<Point> points = new ArrayList<>();
        
        // Récupération du descripteur de l'image requête
        if(!descriptorMap.containsKey(queryImage)) {
            logger.error("ATTENTION §§§ Descripteur manquant pour l'image requête : {}", queryImage);
        }
        
        double[] queryDescriptor = descriptorMap.get(queryImage).stream().mapToDouble(Double::doubleValue).toArray();
        // Récupération de toutes les images de la base voisins triés par distance.
        // La dependance Smile knn(descriptor, n) est dejà optimisé pour renvoyer les kppv dans l'ordre!
        List<String> sortedRetrievedImages = Arrays.stream(kdTree.knn(queryDescriptor, 10000))
                                                   .map(neighbor -> neighbor.value)
                                                   .collect(Collectors.toList());
        
        // Vérification de la liste des voisins
        if(sortedRetrievedImages.isEmpty()) {
            logger.error("Aucun voisin trouvé pour l'image requête : {}", queryImage);
            return Collections.emptyList();
        }
        
        
        int kPlus = 0; // Nombre d'images pertinentes retrouvées (|A ∩ C|)
        int maxNeighbors = 10000; //certains niveaux de rappel élevés peuvent ne pas être atteints si les voisins pertinents apparaissent au-delà des 100
        // premiers voisins.
        int totalRelevant = relevantImages.size(); // Total des images pertinentes (|C|)
        
        for(int k = 1; k <= Math.min(sortedRetrievedImages.size(), maxNeighbors); k++) {
            String retrievedImage = sortedRetrievedImages.get(k - 1);
            
            if(relevantImages.contains(retrievedImage)) {
                kPlus++;
            }
            
            double precision = (double) kPlus / k; // |A ∩ C| / |A|
            double recall = (double) kPlus / totalRelevant; // |A ∩ C| / |C|
            
            points.add(new Point(recall * 100, precision * 100));
        }
        
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
    
    /**
     * Construit un k-d tree à partir des descripteurs des images de la base. Optimise considérablement la recherche des kppv pour chaque image requête.
     *
     * @param descriptorMap map associant chaque image à son descripteur
     * @return k-d tree contenant les descripteurs de toutes les images de la base
     */
    private KDTree<String> buildKdTree(Map<String, List<Double>> descriptorMap) {
        logger.info("Création du k-d tree avec {} descripteurs.", descriptorMap.size());
        double[][] points = descriptorMap.values().stream()
                                         .map(descriptor -> descriptor.stream().mapToDouble(Double::doubleValue).toArray())
                                         .toArray(double[][]::new);
        
        String[] labels = descriptorMap.keySet().toArray(new String[0]);
        
        return new KDTree<>(points, labels);
    }
    
    /**
     * Methode destinée uniquement  à l'affichage des courbes sur rawgraphs, et de quelques informations supplémentaires
     *
     * @param precisionRecallData les données de précision/rappel
     * @return les données de précision/rappel sous forme de liste de map
     */
    private List<Map<String, Object>> flattenJson(Map<String, List<Point>> precisionRecallData) {
        List<Map<String, Object>> flatData = new ArrayList<>();
        
        // Attribuer une couleur unique à chaque groupe
        Map<String, String> groupColors = Map.of(
                "RGB666", "#FF5733", // Rouge-orangé
                "RGB444", "#33FF57",  // Vert
                "RGB222", "#3357FF"   // Bleu
                                                );
        
        // Attribuer des noms lisibles pour chaque groupe
        Map<String, String> groupNames = Map.of(
                "RGB666", "Rouge-orangé (RGB666)",
                "RGB444", "Vert (RGB444)",
                "RGB222", "Bleu (RGB222)"
                                               );
        
        for(Map.Entry<String, List<Point>> entry : precisionRecallData.entrySet()) {
            String group = entry.getKey(); // Nom brut de la courbe (RGB666, RGB444, RGB222)
            String color = groupColors.getOrDefault(group, "#000000"); // Couleur par défaut : noir
            String groupName = groupNames.getOrDefault(group, group); // Nom lisible pour légende
            
            for(Point point : entry.getValue()) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("recall", 100 - point.getRecall());
                dataPoint.put("precision", 100 * point.getPrecision());
                dataPoint.put("color", color); // Couleur unique
                dataPoint.put("group", groupName); // Nom lisible pour légende
                flatData.add(dataPoint);
            }
            
            //            // Ajouter des "points fictifs" pour forcer les graduations sur l'axe des abscisses
            //            for (int i = 0; i <= 100; i += 10) {
            //                Map<String, Object> tickPoint = new HashMap<>();
            //                tickPoint.put("recall", i); // Valeur de recall régulière
            //                tickPoint.put("precision", 0); // Valeur neutre ou fictive
            //                tickPoint.put("color", color); // Couleur de la courbe (optionnel)
            //                tickPoint.put("group", groupName); // Associer au groupe correspondant
            //                flatData.add(tickPoint);
            //            }
        }
        
        return flatData;
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
        
        // Identifier le nombre de descripteurs par ligne en fonction du fichier (RGB_2x2x2, RGB_4x4x4, RGB_6x6x6)
        int numDescriptors;
        if(histogramPath.contains("2x2x2")) {
            numDescriptors = 8; // 2x2x2 = 8 descripteurs
        } else if(histogramPath.contains("4x4x4")) {
            numDescriptors = 64; // 4x4x4 = 64 descripteurs
        } else if(histogramPath.contains("6x6x6")) {
            numDescriptors = 216; // 6x6x6 = 216 descripteurs
        } else {
            logger.error("Nom du fichier inattendu : {}. Veuillez vérifier que le chemin inclut un descripteur valide (2x2x2, 4x4x4, 6x6x6)", histogramPath);
            throw new IllegalArgumentException("Impossible de déterminer le nombre de descripteurs à partir du chemin : " + histogramPath);
        }
        
        logger.info("Chargement des descripteurs pour {} descripteurs par ligne.", numDescriptors);
        
        // Lire les lignes du fichier
        List<String> lines = Files.readAllLines(Paths.get(histogramPath));
        Map<String, List<Double>> descriptorMap = new HashMap<>();
        
        for(int i = 0; i < imageNames.size(); i++) {
            String imageName = imageNames.get(i);
            String[] lineParts = lines.get(i).split("\\s+"); // Séparer la ligne par espaces
            
            // Vérification : la ligne doit contenir exactement le bon nombre de descripteurs
            if(lineParts.length < numDescriptors) {
                logger.warn("Ligne {} ne contient pas suffisamment de descripteurs. Attendu : {}, Trouvé : {}", i, numDescriptors, lineParts.length);
                continue; // Ignorer les lignes incorrectes
            }
            
            if(i >= lines.size()) {
                logger.error("Nombre de lignes dans le fichier des descripteurs ({}) est insuffisant pour les images listées.", lines.size());
                break; // Arrêtez le traitement si les lignes sont insuffisantes
            }
            
            // Lire les descripteurs
            List<Double> descriptor = Arrays.stream(lineParts)
                                            .map(Double::parseDouble)
                                            .collect(Collectors.toList());
            
            // Vérifiez la somme des descripteurs
            double sum = descriptor.stream().mapToDouble(Double::doubleValue).sum();
            if(sum < 0.99 || sum > 1.01) { // Tolérance ±0.01
                logger.warn("Descripteur anormal EN ENTREE détecté pour l'image {} : somme = {}", imageName, sum);
            }
            
            descriptorMap.put(imageName, descriptor);
//            logger.info("Image : {}, Descripteur (taille {}) : {}", imageName, descriptor.size(), descriptor);
        }
        
        
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
        
        
        // Vérification des descripteurs après normalisation s'ils sont toujours entre 0 et 1!! Pour verifier la normalisation!!!
        for(String image : descriptorMap.keySet()) {
            double sum = descriptorMap.get(image).stream().mapToDouble(Double::doubleValue).sum();
            if(sum > 1.01 || sum < 0.99) { // Tolérance pour les erreurs flottantes
                logger.warn("Descripteur anormal APRES NORMALISATION pour l'image {} : somme = {}", image, sum);
            }
        }
        
        if(descriptorMap.size() != imageNames.size()) {
            logger.error("Le nombre d'images ({}) ne correspond pas au nombre de descripteurs ({}) après normalisation.",
                         imageNames.size(),
                         descriptorMap.size());
        }
        
        logger.info("Descripteurs chargés avec succès : {} descripteurs.", descriptorMap.size());
        return descriptorMap;
    }
    
    
    /**
     * Calculer la dimension maximale des descripteurs pour la normalisation des dimensions cibles
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
     * Moyenne des points de précision et de rappel pour une granularité donnée.
     * Alternative à la consolidation pour obtenir un nombre fixe de points.
     *
     * @param points      liste des points de précision et de rappel
     * @param granularity granularité pour arrondir le rappel
     * @return liste des points moyens
     */
    private List<Point> moyennePointsPrecisionRappel(List<Point> points, double granularity) {
        // Map pour regrouper les précisions selon une valeur de rappel arrondie
        Map<Double, List<Double>> recallPrecisionMap = new TreeMap<>();
        
        for(Point point : points) {
            // Arrondir le rappel à la granularité définie
            double roundedRecall = Math.round(point.getRecall() / granularity) * granularity;
            
            // Ajouter la précision à la liste associée à ce rappel arrondi
            recallPrecisionMap.computeIfAbsent(roundedRecall, r -> new ArrayList<>()).add(point.getPrecision());
        }
        
        // Créer une liste de points moyenne
        List<Point> averagedPoints = recallPrecisionMap.entrySet().stream()
                                                       .map(entry -> {
                                                           double recall = entry.getKey(); // Rappel arrondi
                                                           double precision = entry.getValue()
                                                                                   .stream()
                                                                                   .mapToDouble(p -> p)
                                                                                   .average()
                                                                                   .orElse(0.0); // Moyenne des précisions
                                                           return new Point(recall, precision); // Nouveau point avec le rappel arrondi et la précision moyenne
                                                       })
                                                       .collect(Collectors.toList());
        
        return averagedPoints;
    }
    
    /**
     * Charger les descripteurs d'une image de la fin jusqu'au début de la ligne à partir du fichier spécifié. Methode générique qui détecte le nombre de
     * descripteurs à partir du nom du fichier.
     *
     * @param points      chemin du fichier contenant les descripteurs
     * @param granularity liste des noms des images
     * @return map associant chaque image à ses descripteurs
     */
    private List<Point> consolidatePoints(List<Point> points, double granularity) {
        Map<Double, List<Double>> recallPrecisionMap = new TreeMap<>();
        for(Point point : points) {
            double roundedRecall = Math.round(point.getRecall() / granularity) * granularity;
            recallPrecisionMap.computeIfAbsent(roundedRecall, r -> new ArrayList<>()).add(point.getPrecision());
        }
        
        List<Point> consolidatedPoints = recallPrecisionMap.entrySet().stream()
                                                           .map(entry -> {
                                                               double recall = entry.getKey();
                                                               double precision = entry.getValue().stream().mapToDouble(p -> p).average().orElse(0.0);
                                                               return new Point(recall, precision);
                                                           })
                                                           .collect(Collectors.toList());
        
        return consolidatedPoints;
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
