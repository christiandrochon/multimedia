package fr.cdrochon.multimedias.generationdescripteurs;

import fr.cdrochon.multimedias.generationdescripteurs.forme.DescripteurForme;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DescripteurFormController {
    
    private DescripteurForme descripteurForme;
    
    public DescripteurFormController(DescripteurForme descripteurForme) {
        this.descripteurForme = descripteurForme;
    }
    
    @GetMapping("/genererforme")
    public String processFormeDescriptors() {
        try {
            descripteurForme.process();
            return "Descripteurs de forme générés avec succès.";
        } catch(Exception e) {
            return "Erreur lors de la génération des descripteurs de forme : " + e.getMessage();
        }
    }
}
