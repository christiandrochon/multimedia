package fr.cdrochon.multimedias.generationhistogrammesvisuels;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class HistogrammeCouleurController {
    
    @Value("${images.directory}")
    private String imagesDirectory;
    
    /**
     * Endpoint pour calculer l'histogramme de couleur d'une image
     *
     * @param numBins Nombre de bins pour l'histogramme
     * @return Réponse JSON contenant les histogrammes et l'URL de l'image
     *
     * @throws IOException en cas d'erreur de lecture de l'image
     */
    @GetMapping("/histogramme-couleur")
    public Map<String, Object> computeRGBAndAverageHistogram(@RequestParam int numBins) throws IOException {
        // Sélectionner une image aléatoire
        File imageFile = selectRandomImage(imagesDirectory);
        BufferedImage image = ImageIO.read(imageFile);
        
        // Calculer les histogrammes
        Map<String, Object> rgbHistograms = computeRGBHistogram(image, numBins);
        
        // Préparer la réponse JSON
        Map<String, Object> response = new HashMap<>();
        response.put("redHistogram", rgbHistograms.get("redHistogram"));
        response.put("greenHistogram", rgbHistograms.get("greenHistogram"));
        response.put("blueHistogram", rgbHistograms.get("blueHistogram"));
        response.put("redAverages", rgbHistograms.get("redAverages"));
        response.put("greenAverages", rgbHistograms.get("greenAverages"));
        response.put("blueAverages", rgbHistograms.get("blueAverages"));
        response.put("averageHistogram", rgbHistograms.get("averageHistogram"));
        response.put("imageUrl", "http://localhost:8087/images/" + imageFile.getName());
        
        // Log de la réponse pour débogage
        System.out.println("Response JSON: " + response);
        
        return response;
    }
    
    /**
     * Fonction pour calculer les histogrammes de couleur RVB d'une image
     *
     * @param image   Image
     * @param numBins Nombre de bins pour chaque canal de couleur
     * @return Histogrammes RVB
     */
    private Map<String, Object> computeRGBHistogram(BufferedImage image, int numBins) {
        // Initialisation des histogrammes
        int[] redHistogram = new int[numBins];
        int[] greenHistogram = new int[numBins];
        int[] blueHistogram = new int[numBins];
        
        int[] redSum = new int[numBins];   // Init le tab des entiers pour la somme des valeurs Rouge pour calculer la moyenne
        int[] greenSum = new int[numBins]; // Init le tableau des sommes des valeurs Vert pour calculer la moyenne
        int[] blueSum = new int[numBins];  // Init le tableau des entiers pour futur calcul de la somme des valeurs Bleu pour calculer la moyenne
        int[] pixelCounts = new int[numBins]; // Init tab du count des pixels dans chaque bin pour la moyenne (non demandé)
        int[] averageHistogram = new int[numBins]; // init tab qui va stocker l'histogramme moyen, avec les 3 couleurs
        int binSize = 256 / numBins; //Calcul de la taille de chaque intervalle
        
        // recupere la dimension d'une image
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Double boucle imbriquée sur tous les pixels (la complexité est O(n^2) avec n = width * height)
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                // recuperation des composantes de couleur
                int rgb = image.getRGB(x, y); // recupere la valeur RGB du pixel (x,y)
                int red = (rgb >> 16) & 0xff; // Isole la composant rouge (avec un décalage de 16 bits vers la droite)
                int green = (rgb >> 8) & 0xff; //isole la composante verte (8-16 bits)
                int blue = rgb & 0xff; // isole la composante bleue (0-8 bits)
                
                // Calcul des indices d'intervalle pour chaque composante de couleur
                // pour chaque couleur, on divise le niveau de couleur par la taille de l'intervalle + on s'assure que la valeur
                // obtnue ne depasse pas le nb d'intervalle -1
                int redBinIndex = Math.min(red / binSize, numBins - 1);
                int greenBinIndex = Math.min(green / binSize, numBins - 1);
                int blueBinIndex = Math.min(blue / binSize, numBins - 1);
                
                // Mise à jour des histogrammes en incrementant le nombre de pixels dans chaque bin
                redHistogram[redBinIndex]++;
                greenHistogram[greenBinIndex]++;
                blueHistogram[blueBinIndex]++;
                
                // Mise à jour des sommes des niveaux de couleur et le nb de pixels dans chaque bin (non demandé)
                redSum[redBinIndex] += red;
                greenSum[greenBinIndex] += green;
                blueSum[blueBinIndex] += blue;
                pixelCounts[redBinIndex]++; // Compte total de pixels pour ce bin
            }
        }
        
        // Calculer les moyennes pour chaque intervalle de couleur (non demandé)
        int[] redAverages = new int[numBins];
        int[] greenAverages = new int[numBins];
        int[] blueAverages = new int[numBins];
        
        //  inti les tabs de stockage des moyennes de niveaux de couleur pour chaque intervalle (non demandé)
        // cad que pour chaque intervalle, si le bin contient des pixels, on calule la moyenne des valeurs de rouge,
        // vert et bleu par la somme du nb de pixels
        for(int i = 0; i < numBins; i++) {
            redAverages[i] = pixelCounts[i] > 0 ? redSum[i] / pixelCounts[i] : 0;
            greenAverages[i] = pixelCounts[i] > 0 ? greenSum[i] / pixelCounts[i] : 0;
            blueAverages[i] = pixelCounts[i] > 0 ? blueSum[i] / pixelCounts[i] : 0;
            
            // calcul de l'histogramme moyen en prenant la moiyenne des histo RGB
            averageHistogram[i] = (redHistogram[i] + greenHistogram[i] + blueHistogram[i]) / 3;
        }
        
        // Préparation de la réponse json avec les histogrammes et les moyennes
        // on stocke dans la Map les histigrammes des 3 couleurs, la miyenne des niveaux de couleur par intervalle et l'histo moyen
        Map<String, Object> histograms = new HashMap<>();
        // Obligatoire
        histograms.put("redHistogram", redHistogram);
        histograms.put("greenHistogram", greenHistogram);
        histograms.put("blueHistogram", blueHistogram);
        
        // Non demandé
        histograms.put("redAverages", redAverages);
        histograms.put("greenAverages", greenAverages);
        histograms.put("blueAverages", blueAverages);
        histograms.put("averageHistogram", averageHistogram);
        
        return histograms;
    }
    
    
    //    /**
    //     * Fonction pour calculer les histogrammes de couleur RVB d'une image
    //     *
    //     * @param image   Image
    //     * @param numBins Nombre de bins pour chaque canal de couleur
    //     * @return Histogrammes RVB
    //     */
    //    private Map<String, int[]> computeRGBHistogram(BufferedImage image, int numBins) {
    //        int[] redHistogram = new int[numBins];
    //        int[] greenHistogram = new int[numBins];
    //        int[] blueHistogram = new int[numBins];
    //        int binSize = 256 / numBins;
    //
    //        int width = image.getWidth();
    //        int height = image.getHeight();
    //
    //        // Parcourir tous les pixels
    //        for(int y = 0; y < height; y++) {
    //            for(int x = 0; x < width; x++) {
    //                int rgb = image.getRGB(x, y);
    //                int red = (rgb >> 16) & 0xff;
    //                int green = (rgb >> 8) & 0xff;
    //                int blue = rgb & 0xff;
    //
    //                //eviter les valeurs hors des limites
    //                int redBinIndex = Math.min(red / binSize, numBins - 1);
    //                int greenBinIndex = Math.min(green / binSize, numBins - 1);
    //                int blueBinIndex = Math.min(blue / binSize, numBins - 1);
    //
    //                redHistogram[redBinIndex]++;
    //                greenHistogram[greenBinIndex]++;
    //                blueHistogram[blueBinIndex]++;
    //            }
    //        }
    //
    //        Map<String, int[]> histograms = new HashMap<>();
    //        histograms.put("red", redHistogram);
    //        histograms.put("green", greenHistogram);
    //        histograms.put("blue", blueHistogram);
    //
    //        return histograms;
    //    }
    
    /**
     * Fonction qui sélectionne une image aléatoirement dans la bdd
     *
     * @param directory Répertoire contenant les images
     * @return Image aléatoire
     */
    private File selectRandomImage(String directory) {
        File dir = new File(directory);
        File[] imageFiles = dir.listFiles((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        
        if(imageFiles == null || imageFiles.length == 0) {
            throw new RuntimeException("Aucune image trouvée dans le répertoire : " + directory);
        }
        
        Random random = new Random();
        return imageFiles[random.nextInt(imageFiles.length)];
    }
}
