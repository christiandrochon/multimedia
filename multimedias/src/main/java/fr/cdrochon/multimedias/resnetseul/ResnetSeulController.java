package fr.cdrochon.multimedias.resnetseul;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

@RestController
public class ResnetSeulController {
    
    private final ResnetSeulService resnetService;
    
    public ResnetSeulController(ResnetSeulService resnetService) {
        this.resnetService = resnetService;
    }
    
    // Récupérer les descripteurs de classes mais c'est l'user qui spécifie la classe de descripteur
    @GetMapping("/resnetclasses")
    public List<String> getClassDescriptors() {
        
        return resnetService.getClassDescriptors(); // Nom de méthode corrigé
    }
    
    // Ajoutez cette méthode pour obtenir une image aléatoire dans la classe
    @GetMapping("/resnetimages/random")
    public String getRandomImageForClass(@RequestParam("className") String className) {
        List<String> images = resnetService.getImagesForClass(className);
        if(images.isEmpty()) {
            return ""; // Retourner une chaîne vide si aucune image n'est trouvée
        }
        return images.get(new Random().nextInt(images.size())); // Retourner une image aléatoire
    }
    
    
    // Récupérer les images par similarité pour une classe donnée
    @GetMapping("/resnetimages")
    public List<String> getImagesForClassWithSimilarity(
            @RequestParam("className") String className,
            @RequestParam("queryImage") String queryImage) {
        return resnetService.getImagesForClassWithSimilarity(className, queryImage);
    }
}
