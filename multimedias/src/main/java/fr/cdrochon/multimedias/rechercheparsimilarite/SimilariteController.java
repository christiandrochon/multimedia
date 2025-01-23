package fr.cdrochon.multimedias.rechercheparsimilarite;

import fr.cdrochon.multimedias.rechercheparsimilarite.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RestController
public class SimilariteController {
    
    @Value("${images.directory}")
    private String imagesDirectory;
    
    @Autowired
    private QBE qbeService;
    @Autowired
    private SimilariteTextureAvecFFTSupplementaire textureServicSimilariteTextureAvecFFTSupplementaire;
    @Autowired
    private SimilariteTextureSansFFTSupplementaire textureServiceSansCalculSupplementaire;
    @Autowired
    private SimilariteForme similariteFormeService;
    @Autowired
    private SimilariteCouleurTexture similariteCouleurTextureService;
    @Autowired
    private SimilariteGlobale similariteGlobaleService;
    
    private String selectedImageName;
    
    /**
     * Endpoint pour obtenir une image aléatoire
     *
     * @return l'image aléatoire
     *
     * @throws IOException en cas d'erreur de lecture de l'image
     */
    @GetMapping("/similaritecouleur")
    public ResponseEntity<String> getRandomImage() throws IOException {
        File dir = new File(imagesDirectory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if(files == null || files.length == 0) {
            return ResponseEntity.notFound().build();
        }
        File randomFile = files[new Random().nextInt(files.length)];
        String imagePath = "/images/" + randomFile.getName();  // Renvoie le nom de l'image aléatoire pour l'affichage de l'image aleatoire
        
        selectedImageName = randomFile.getName(); // stocke le nom d'image pour la methode de recherche
        
        return ResponseEntity.ok(imagePath);
    }
    
    /**
     * Endpoint pour obtenir des images similaires
     *
     * @param k le nombre d'images similaires à retourner
     * @return la liste des images similaires
     */
    @GetMapping("/recherchesimilaire")
    public ResponseEntity<List<String>> getSimilarImages(@RequestParam int k) {
        if(selectedImageName == null) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        
        // Utiliser le nom de l'image sélectionnée pour la recherche
        List<String> similarImages = qbeService.findSimilarImages(selectedImageName, k);
        return ResponseEntity.ok(similarImages);
    }
    
    /**
     * Endpoint pour obtenir une image aléatoire
     *
     * @return l'image aléatoire
     *
     * @throws IOException en cas d'erreur de lecture de l'image
     */
    @GetMapping("/similaritetexture")
    public ResponseEntity<String> getRandomTextureImage() throws IOException {
        File dir = new File(imagesDirectory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if(files == null || files.length == 0) {
            return ResponseEntity.notFound().build();
        }
        File randomFile = files[new Random().nextInt(files.length)];
        String imagePath = "/images/" + randomFile.getName(); // Renvoie le nom de l'image aléatoire pour l'affichage
        
        selectedImageName = randomFile.getName(); // Stocke le nom de l'image pour la recherche
        
        return ResponseEntity.ok(imagePath);
    }
    
    /**
     * Endpoint pour obtenir des images similaires en termes de texture
     *
     * @param k le nombre d'images similaires à retourner
     * @return la liste des images similaires
     */
    @GetMapping("/recherchesimilairetexture")
    public ResponseEntity<List<String>> getSimilarTextureImages(@RequestParam String queryImage, @RequestParam int k) {
        if(queryImage == null || queryImage.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        
        // Trouver le nom de l'image depuis son URL
        String imageName = queryImage.substring(queryImage.lastIndexOf("/") + 1);
        
        // Appeler le service pour trouver des images similaires
        List<String> similarImages = textureServiceSansCalculSupplementaire.findSimilarTextures(imageName, k);
        return ResponseEntity.ok(similarImages);
    }
    
    //    @GetMapping("/recherchesimilairetexture")
    //    public ResponseEntity<List<String>> getSimilarTextureImages(@RequestParam int k) {
    //        if(selectedImageName == null) {
    //            return ResponseEntity.badRequest().body(Collections.emptyList());
    //        }
    //
    //        // Utiliser le nom de l'image sélectionnée pour la recherche
    //        List<String> similarImages = textureService.findSimilarTextures(selectedImageName, k);
    //        return ResponseEntity.ok(similarImages);
    //    }
    
    /**
     * Endpoint pour obtenir une image aléatoire
     *
     * @return l'image aléatoire
     *
     * @throws IOException en cas d'erreur de lecture de l'image
     */
    @GetMapping("/similaritecouleurtexture")
    public ResponseEntity<String> getRandomColorTextureImage() throws IOException {
        File dir = new File(imagesDirectory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if(files == null || files.length == 0) {
            return ResponseEntity.notFound().build();
        }
        File randomFile = files[new Random().nextInt(files.length)];
        String imagePath = "/images/" + randomFile.getName();  // Renvoie le nom de l'image aléatoire pour l'affichage de l'image aleatoire
        
        selectedImageName = randomFile.getName(); // stocke le nom d'image pour la methode de recherche
        
        return ResponseEntity.ok(imagePath);
    }
    
    /**
     * Endpoint pour obtenir une image aléatoire
     *
     * @param k le nombre d'images similaires à retourner
     * @return la liste des images similaires
     */
    @GetMapping("/recherchesimilairecouleurstextures")
    public ResponseEntity<List<String>> getImagesSimilarColorsTextures(@RequestParam int k) {
        if(selectedImageName == null) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        
        // Use the SimilariteGlobale service to find similar images
        List<String> similarImages = similariteCouleurTextureService.findSimilarImages(selectedImageName, k);
        return ResponseEntity.ok(similarImages);
    }
    
    @GetMapping("/similariteforme")
    public ResponseEntity<String> getRandomFormeImage() throws IOException {
        File dir = new File(imagesDirectory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if(files == null || files.length == 0) {
            return ResponseEntity.notFound().build();
        }
        File randomFile = files[new Random().nextInt(files.length)];
        String imagePath = "/images/" + randomFile.getName();  // Renvoie le nom de l'image aléatoire pour l'affichage de l'image aleatoire
        
        selectedImageName = randomFile.getName(); // stocke le nom d'image pour la methode de recherche
        
        return ResponseEntity.ok(imagePath);
    }
    
    @GetMapping("/recherchesimilaireforme")
    public ResponseEntity<List<String>> getImagesSimilarShapes(@RequestParam int k) {
        if(selectedImageName == null) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        
        // Use the SimilariteForme service to find similar images
        List<String> similarImages = similariteFormeService.findSimilarShapes(selectedImageName, k);
        return ResponseEntity.ok(similarImages);
    }
    
    @GetMapping("/similariteglobale")
    public ResponseEntity<String> getRandomGlobalImage() throws IOException {
        File dir = new File(imagesDirectory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if(files == null || files.length == 0) {
            return ResponseEntity.notFound().build();
        }
        File randomFile = files[new Random().nextInt(files.length)];
        String imagePath = "/images/" + randomFile.getName();  // Renvoie le nom de l'image aléatoire pour l'affichage de l'image aleatoire
        
        selectedImageName = randomFile.getName(); // stocke le nom d'image pour la methode de recherche
        
        return ResponseEntity.ok(imagePath);
    }
    
    @GetMapping("/recherchesimilaireglobale")
    public ResponseEntity<List<String>> getImagesSimilarGlobal(@RequestParam int k) {
        if(selectedImageName == null) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        
        // Use the SimilariteGlobale service to find similar images
        List<String> similarImages = similariteGlobaleService.findSimilarImages(selectedImageName, k);
        return ResponseEntity.ok(similarImages);
    }
    
}
