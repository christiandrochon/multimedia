package fr.cdrochon.multimedias.images;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class MultimediaController {
    
    @Value("${images.directory}")
    private String imagesDirectory;
    
    /**
     * Message de bienvenue
     *
     * @return Message de bienvenue
     */
    @GetMapping("/hello")
    public String sayHell() {
        return "Hello";
    }
    
    /**
     * Créer une image de damier dans le répertoire spécifié
     *
     * @return Message de succès ou d'erreur
     */
    @GetMapping("/createCheckerboard")
    public ResponseEntity<Map<String, String>> createDamier() {
        try {
            String checkerboardImagePath = imagesDirectory + "/checkerboard.png";
            ImageUtils.createDamierImage(checkerboardImagePath);
            
            Map<String, String> response = new HashMap<>();
            response.put("checkerboardImage", "/images/checkerboard.png");
            
            return ResponseEntity.ok(response);
        } catch(Exception e) {
            throw new RuntimeException("Error creating checkerboard image: " + e.getMessage());
        }
    }
    
    /**
     * Intervertir les canaux de couleur d'une image aléatoire dans le répertoire spécifié, de RGB vers GBR
     *
     * @return Message de succès ou d'erreur
     */
    //    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/swapRandomImage")
    public ResponseEntity<Map<String, String>> swapRandomImage() {
        try {
            // Chemin du répertoire contenant les images
            File dir = new File(imagesDirectory);
            
            // Vérification que le répertoire existe et est un répertoire valide
            if(!dir.exists() || !dir.isDirectory()) {
                throw new RuntimeException("Directory not found or not valid: " + imagesDirectory);
            }
            // Liste des fichiers dans le répertoire
            List<File> files = Arrays.stream(dir.listFiles())
                                     .filter(file -> !file.isDirectory() && isImageFile(file))
                                     .collect(Collectors.toList());
            
            // Vérifier s'il y a au moins une image
            if(files.isEmpty()) {
                throw new RuntimeException("No images found in directory.");
            }
            
            File randomImage = files.get(new Random().nextInt(files.size()));
            String randomImageUrl = "/images/" + randomImage.getName();
            
            // Assuming you have a method to swap the image and save it as swapped_image.png
            String swappedImageUrl = "/images/swapped_image.png";
            ImageUtils.swapRGBtoGBR(randomImage.getAbsolutePath(), imagesDirectory + "/swapped_image.png");
            
            Map<String, String> response = new HashMap<>();
            response.put("randomImage", randomImageUrl);
            response.put("swappedImage", swappedImageUrl);
            
            return ResponseEntity.ok(response);
        } catch(Exception e) {
            throw new RuntimeException("Error swapping image color channels: " + e.getMessage());
        }
    }
    
    
    /**
     * Récupérer une liste de 16 images aléatoires dans le répertoire spécifié
     *
     * @return Liste des chemins des images
     */
    @GetMapping("/images")
    public List<String> getImages() {
        File dir = new File(imagesDirectory);
        
        // Vérification que le répertoire existe et est un répertoire valide
        if(!dir.exists() || !dir.isDirectory()) {
            throw new RuntimeException("Directory not found or not valid: " + imagesDirectory);
        }
        
        // Liste des fichiers dans le répertoire
        List<File> files = Arrays.stream(dir.listFiles())
                                 .filter(file -> !file.isDirectory() && isImageFile(file))
                                 .collect(Collectors.toList());
        
        // Vérifier si on a assez d'images
        if(files.size() < 16) {
            throw new RuntimeException("Not enough images found in directory. Found " + files.size());
        }
        
        // Mélanger les fichiers et en sélectionner 16 aléatoirement
        Collections.shuffle(files);
        List<File> selectedFiles = files.subList(0, 16);
        
        // Retourner les chemins relatifs ou absolus des images sélectionnées
        return selectedFiles.stream()
                            .map(file -> "/images/" + file.getName())
                            .collect(Collectors.toList());
    }
    
    /**
     * Fonction utilitaire pour vérifier si le fichier est une image
     *
     * @param file Fichier à vérifier
     * @return Vrai si le fichier est une image, faux sinon
     */
    private boolean isImageFile(File file) {
        String[] extensions = {"jpg", "jpeg", "png", "gif", "bmp"};
        String fileName = file.getName().toLowerCase();
        return Arrays.stream(extensions).anyMatch(fileName::endsWith);
    }
}
