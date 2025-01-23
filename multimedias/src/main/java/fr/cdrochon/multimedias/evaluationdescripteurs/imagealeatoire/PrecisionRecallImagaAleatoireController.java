package fr.cdrochon.multimedias.evaluationdescripteurs.imagealeatoire;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping
public class PrecisionRecallImagaAleatoireController {
    
    @Autowired
    private PrecisionRecallImageAleatoireService precisionRecallService1;
    
    /**
     * Récupère une image aléatoire et les images similaires
     *
     * @return ResponseEntity<Map < String, Object>> réponse HTTP
     *
     * @throws IOException si une erreur d'entrée/sortie se produit
     */
    @GetMapping("/evaluation")
    public String getRandomImage() throws IOException {
        return precisionRecallService1.getRandomImageWithVT();
    }
    
    /**
     * Calcule la précision et le rappel moyen
     *
     * @param queryImage image requête
     * @param topK       nombre d'images similaires à considérer
     * @return précision et rappel moyen
     */
    @GetMapping("/precision-recall")
    public CompletableFuture<Map<String, Object>> getPrecisionRecallForImageAsync(
            @RequestParam String queryImage,
            @RequestParam(defaultValue = "50") int topK) {
        return precisionRecallService1.calculatePrecisionRecallForImageAsync(queryImage, topK);
    }
    
}

