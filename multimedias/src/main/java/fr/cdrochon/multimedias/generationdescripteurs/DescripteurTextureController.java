package fr.cdrochon.multimedias.generationdescripteurs;

import fr.cdrochon.multimedias.generationdescripteurs.texture.DescripteurTexture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DescripteurTextureController {
    
    @Autowired
    private DescripteurTexture descripteurTexture;
    
    @GetMapping("/generertexture")
    public String processTextureDescriptors() {
        try {
            descripteurTexture.process();
            return "Descripteurs de texture générés avec succès.";
        } catch (Exception e) {
            return "Erreur lors de la génération des descripteurs de texture : " + e.getMessage();
        }
    }
}
