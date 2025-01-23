package fr.cdrochon.multimedias.evaluationdescripteurs.classes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/precision-rappel")
public class PrecisionRappelController {
    
    @Autowired
    private PrecisionRappelGrisService precisionRappelGrisService;
    
    @Autowired
    private PrecisionRappelCouleurService precisionRappelCouleurService;
    
    @Autowired
    private PrecisionRappelTousDescripteursService precisionRappelTousDescripteursService;
    
    @GetMapping("/gris")
    public CompletableFuture<String> getPrecisionRappelGris() {
        return precisionRappelGrisService.generatePrecisionRecallForGris();
    }
    
    @GetMapping("/couleur")
    public CompletableFuture<String> getPrecisionRappelCouleur() {
        return precisionRappelCouleurService.generatePrecisionRecallForCouleur();
    }

    @GetMapping("/gris-couleur-resnet")
    public CompletableFuture<String> getPrecisionRappelGrisCouleurResnet() {
        return precisionRappelTousDescripteursService.generatePrecisionRecallForAllDescripteurs();
    }

}
