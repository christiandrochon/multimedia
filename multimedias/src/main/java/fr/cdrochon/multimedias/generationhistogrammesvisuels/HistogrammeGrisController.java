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
public class HistogrammeGrisController {
    
    @Value("${images.directory}")
    private String imagesDirectory;
    
    /**
     * Endpoint pour calculer l'histogramme en niveaux de gris d'une image
     *
     * @param numBins Nombre de bins pour l'histogramme
     * @return Réponse JSON contenant l'histogramme et l'URL de l'image
     *
     * @throws IOException en cas d'erreur de lecture de l'image
     */
    @GetMapping("/histogramme-gris")
    public Map<String, Object> computeGrayLevelHistogram(@RequestParam int numBins) throws IOException {
        // Sélectionner une image aléatoire dans le répertoire
        File imageFile = selectRandomImage(imagesDirectory);
        BufferedImage image = ImageIO.read(imageFile);
        
        // Calculer l'histogramme en niveaux de gris
        int[] histogram = computeHistogram(image, numBins);
        
        // Préparer la réponse JSON
        Map<String, Object> response = new HashMap<>();
        response.put("histogram", histogram);
        response.put("imageUrl", "http://localhost:8087/images/" + imageFile.getName());  // URL relative de l'image
        
        return response;
    }
    
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
    
    /**
     * Fonction qui calcule l'histogramme d'une image en niveaux de gris
     *
     * @param image   Image
     * @param numBins Nombre de niveaux de gris
     * @return Histogramme
     */
    private int[] computeHistogram(BufferedImage image, int numBins) {
        int[] histogram = new int[numBins];
        int width = image.getWidth(); // récupère la largeur de l'image
        int height = image.getHeight(); // récupère la hauteur de l'image
        int binSize = 256 / numBins; // calcule la taille de chaque bin en divisant 256 par le nombre d'intervalles defini en parametre
        
        // Double boucle imbriquée pour parcourir chaque pixel de l'image avec x et y, coordonnées s'incrementant de 1 en 1. On utilise la taille et la
        // largeur de l'image pour définir les limites de la boucle
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                //recupere la valeur RGB du pixel (x,y)
                int rgb = image.getRGB(x, y);
                // recupere la valeur de gris du pixel
                // rgb >> 16 : decalage de 16 bits vers la droite pour obtenir la valeur du rouge
                // & 0xff : Applique un masque pour garder uniquement les 8 bits les plus à droite (c'est-à-dire la valeur de la composante rouge)
                int grayLevel = (rgb >> 16) & 0xff;
                // calcule l'indice du bin dans lequel le pixel doit etre place, tout en s'assurant que l'on ne depasse pas le nombre de bins
                int binIndex = Math.min(grayLevel / binSize, numBins - 1);
                // incremente le nombre de pixels dans le bin correspondant
                histogram[binIndex]++;
            }
        }
        
        return histogram;
    }
}
