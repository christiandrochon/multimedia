//package fr.cdrochon.multimedias.evaluationdescripteurs.classes;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//
//@Service
//public class PrecisionRappelAsyncWrapper {
//
//    @Autowired
//    private PRCurveCBIR generatePRCurve;
//
//    @Async
//    public CompletableFuture<Map<String, Object>> getPrecisionRecallCurveAsync() {
//        try {
//            Map<String, Object> gtData = generatePRCurve.loadGroundTruth();
//            List<String> baseFiles = (List<String>) gtData.get("baseFiles");
//            Map<String, String> gt = (Map<String, String>) gtData.get("gt");
//            Map<String, List<String>> classToFiles = (Map<String, List<String>>) gtData.get("classToFiles");
//
//            double[][] descriptors = generatePRCurve.loadDefaultDescriptorMatrix();
//            Map<String, Object> result = generatePRCurve.computePRCurve(descriptors, baseFiles, gt, classToFiles);
//
//            return CompletableFuture.completedFuture(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return CompletableFuture.failedFuture(e);
//        }
//    }
//}
