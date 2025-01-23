package fr.cdrochon.multimedias.generationdescripteurs;

import fr.cdrochon.multimedias.generationdescripteurs.couleur.DescripteurRVB222_1f;
import fr.cdrochon.multimedias.generationdescripteurs.couleur.DescripteurRVB444_1f;
import fr.cdrochon.multimedias.generationdescripteurs.couleur.DescripteurRVB666_1f;
import fr.cdrochon.multimedias.generationdescripteurs.gris.DescripteurGrey16_1f;
import fr.cdrochon.multimedias.generationdescripteurs.gris.DescripteurGrey256_1f;
import fr.cdrochon.multimedias.generationdescripteurs.gris.DescripteurGrey64_1f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DescripteurCouleurController {
    
    public static final Logger logger = LoggerFactory.getLogger(DescripteurCouleurController.class);
    
    @Autowired
    private DescripteurRVB222_1f descripteurRVB222_1f;
    
    @Autowired
    private DescripteurRVB666_1f descripteurRVB666_1f;
    
    @Autowired
    private DescripteurRVB444_1f descripteurRVB444_1f;
    
    @Autowired
    private DescripteurGrey256_1f descripteurGrey256_1f;
    
    @Autowired
    private DescripteurGrey64_1f descripteurGrey64_1f;
    
    @Autowired
    private DescripteurGrey16_1f descripteurGrey16_1f;
    
    // Endpoints pour exécuter chaque descripteur
    @GetMapping("/rvb666")
    public String processRVB666() {
        logger.info("Traitement du descripteur RVB 6x6x6");
        try {
            descripteurRVB666_1f.process();
            logger.info("Descripteur RVB 6x6x6 traité avec succès.");
            return "Descripteur RVB 6x6x6 traité avec succès.";
        } catch(Exception e) {
            logger.error("Erreur lors du traitement de RVB 6x6x6 : " + e.getMessage());
            return "Erreur lors du traitement de RVB 6x6x6 : " + e.getMessage();
        }
    }
    
    @GetMapping("/rvb444")
    public String processRVB444() {
        logger.info("Traitement du descripteur RVB 4x4x4");
        try {
            descripteurRVB444_1f.process();
            logger.info("Descripteur RVB 4x4x4 traité avec succès.");
            return "Descripteur RVB 4x4x4 traité avec succès.";
        } catch(Exception e) {
            logger.error("Erreur lors du traitement de RVB 4x4x4 : " + e.getMessage());
            return "Erreur lors du traitement de RVB 4x4x4 : " + e.getMessage();
        }
    }
    
    @GetMapping("/rvb222")
    public String processRVB222() {
        logger.info("Traitement du descripteur RVB 2x2x2");
        try {
            descripteurRVB222_1f.process();
            logger.info("Descripteur RVB 2x2x2 traité avec succès.");
            return "Descripteur RVB 2x2x2 traité avec succès.";
        } catch(Exception e) {
            logger.error("Erreur lors du traitement de RVB 2x2x2 : " + e.getMessage());
            return "Erreur lors du traitement de RVB 2x2x2 : " + e.getMessage();
        }
    }
    
    @GetMapping("/grey256")
    public String processGrey256() {
        logger.info("Traitement du descripteur niveau de gris 256");
        try {
            descripteurGrey256_1f.process();
            logger.info("Descripteur niveau de gris 256 traité avec succès.");
            return "Descripteur niveau de gris 256 traité avec succès.";
        } catch(Exception e) {
            logger.error("Erreur lors du traitement de gris 256 : " + e.getMessage());
            return "Erreur lors du traitement de gris 256 : " + e.getMessage();
        }
    }
    
    @GetMapping("/grey64")
    public String processGrey64() {
        logger.info("Traitement du descripteur niveau de gris 64");
        try {
            descripteurGrey64_1f.process();
            logger.info("Descripteur niveau de gris 64 traité avec succès.");
            return "Descripteur niveau de gris 64 traité avec succès.";
        } catch(Exception e) {
            logger.error("Erreur lors du traitement de gris 64 : " + e.getMessage());
            return "Erreur lors du traitement de gris 64 : " + e.getMessage();
        }
    }
    
    @GetMapping("/grey16")
    public String processGrey16() {
        logger.info("Traitement du descripteur niveau de gris 16");
        try {
            descripteurGrey16_1f.process();
            logger.info("Descripteur niveau de gris 16 traité avec succès.");
            return "Descripteur niveau de gris 16 traité avec succès.";
        } catch(Exception e) {
            logger.error("Erreur lors du traitement de gris 16 : " + e.getMessage());
            return "Erreur lors du traitement de gris 16 : " + e.getMessage();
        }
    }
    
}
