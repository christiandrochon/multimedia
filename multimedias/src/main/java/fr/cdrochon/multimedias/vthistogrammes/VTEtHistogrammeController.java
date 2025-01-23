package fr.cdrochon.multimedias.vthistogrammes;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VTEtHistogrammeController {
    
    private final VTEtHistogrammeService resnetService;
    private final VTService vtService; // utilisation uniquement des classes VT. Non utilisé ici!
    
    public VTEtHistogrammeController(VTEtHistogrammeService resnetService, VTService vtService) {
        this.resnetService = resnetService;
        this.vtService = vtService;
    }
    

    /**
     * Récupérer une image aléatoire pour une classe donnée sans que l'user ait spécifié la classe
     *
     * @return Chemin de l'image aléatoire
     */
    @GetMapping("/VTclasses/similar")
    public String getRandomImage() {
        return resnetService.getRandomImage();
    }
    
    /**
     * Récupérer les images similaires pour une image donnée sans que l'user ait spécifié l'image
     *
     * @param queryImage Chemin de l'image de requête
     * @return Liste des chemins des images similaires
     */
    @GetMapping("/VTimages/similar")
    public List<String> getImagesWithSimilarityAndDescriptors(@RequestParam("queryImage") String queryImage) {
        return resnetService.getImagesForClassWithSimilarity(queryImage);
    }
}

