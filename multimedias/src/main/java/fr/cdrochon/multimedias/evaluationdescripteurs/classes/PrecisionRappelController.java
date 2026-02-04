package fr.cdrochon.multimedias.evaluationdescripteurs.classes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private PRCurveCBIR prCurveCBIR;
//    @Autowired
//    private PrecisionRappelAsyncWrapper asyncWrapper;
    
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
    
//    @GetMapping("/PR-curve")
//    public CompletableFuture<Map<String, Object>> getPRCurve() {
//        return asyncWrapper.getPrecisionRecallCurveAsync();
//    }
    
    @GetMapping("/PR-curve")
    public CompletableFuture<Map<String, Object>> getPRCurve() throws IOException {
        return prCurveCBIR.computePRCurveAsync();
    }
//    @GetMapping("/PR-curve")
//    public Map<String, Object> getPRCurve() throws IOException {
//        Map<String, Object> gtData = generatePRCurve.loadGroundTruth();
//
//        List<String> baseFiles = (List<String>) gtData.get("baseFiles");
//        Map<String, String> gt = (Map<String, String>) gtData.get("gt");
//        Map<String, List<String>> classToFiles = (Map<String, List<String>>) gtData.get("classToFiles");
//
//        double[][] descriptors = generatePRCurve.loadDefaultDescriptorMatrix();
//
//        return generatePRCurve.computePRCurve(descriptors, baseFiles, gt, classToFiles);
//    }

}
