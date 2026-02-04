package fr.cdrochon.multimedias.evaluationdescripteurs.classes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class PrecisionRappelPRCurveService {
    
    @Value("${resnet.VT_files}")
    private String vtFilesPath;
    
    @Value("${resnet.VT_description}")
    private String vtDescriptionPath;
    
    @Value("${images.list.output}")
    private String base10000files;
    
    @Value("${resnet.resnet18}")
    private String resnet18;
    
    @Async
    public CompletableFuture<Map<String, Object>> generatePrecisionRecallCurve() {
        try {
            List<String> baseFiles = readLines(base10000files);
            List<String> classes = readLines(vtDescriptionPath);
            
            List<List<String>> vtFiles = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(vtFilesPath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    vtFiles.add(Arrays.asList(line.trim().split("\\s+")));
                }
            }
            
            Map<String, String> gt = new HashMap<>();
            Map<String, List<String>> classToFiles = new HashMap<>();
            for (int i = 0; i < classes.size(); i++) {
                String className = classes.get(i);
                List<String> files = vtFiles.get(i);
                classToFiles.put(className, new ArrayList<>(files));
                for (String file : files) {
                    gt.put(file, className);
                }
            }
            
            double[][] descriptors = loadDescriptorMatrix(resnet18);
            Map<String, Object> result = computePRCurve(descriptors, baseFiles, gt, classToFiles);
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.failedFuture(e);
        }
    }
    
    private List<String> readLines(String filename) throws IOException {
        return Files.lines(Paths.get(filename))
                    .filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.toList());
    }
    
    private double[][] loadDescriptorMatrix(String filename) throws IOException {
        List<double[]> matrix = new ArrayList<>();
        for (String line : readLines(filename)) {
            double[] row = Arrays.stream(line.trim().split("\\s+")).mapToDouble(Double::parseDouble).toArray();
            matrix.add(row);
        }
        return matrix.toArray(new double[0][]);
    }
    
    private Map<String, Object> computePRCurve(double[][] descriptors, List<String> baseFiles,
                                               Map<String, String> gt,
                                               Map<String, List<String>> classToFiles) {
        int numImages = descriptors.length;
        List<String> queryFiles = new ArrayList<>(gt.keySet());
        
        int M = classToFiles.values().iterator().next().size();
        int d = descriptors[0].length;
        double[][] prValues = new double[queryFiles.size()][M];
        
        Map<String, Integer> fileToIndex = new HashMap<>();
        for (int i = 0; i < baseFiles.size(); i++) {
            fileToIndex.put(baseFiles.get(i), i);
        }
        
        int qIdx = 0;
        for (String qFile : queryFiles) {
            if (!fileToIndex.containsKey(qFile)) continue;
            int qIndex = fileToIndex.get(qFile);
            double[] qDescriptor = descriptors[qIndex];
            
            double[] distances = new double[numImages];
            for (int i = 0; i < numImages; i++) {
                distances[i] = 0.0;
                for (int j = 0; j < d; j++) {
                    double diff = descriptors[i][j] - qDescriptor[j];
                    distances[i] += diff * diff;
                }
                distances[i] = Math.sqrt(distances[i]);
            }
            
            Integer[] sortedIndices = new Integer[numImages];
            for (int i = 0; i < numImages; i++) sortedIndices[i] = i;
            Arrays.sort(sortedIndices, Comparator.comparingDouble(i -> distances[i]));
            
            String qClass = gt.get(qFile);
            boolean[] relevance = new boolean[numImages];
            for (int i = 0; i < numImages; i++) {
                String fname = baseFiles.get(sortedIndices[i]);
                relevance[i] = qClass.equals(gt.get(fname));
            }
            
            double[] cumRelevance = new double[numImages];
            double sum = 0.0;
            for (int i = 0; i < numImages; i++) {
                if (relevance[i]) sum += 1.0;
                cumRelevance[i] = sum;
            }
            
            double[] precision = new double[numImages];
            double[] recall = new double[numImages];
            for (int i = 0; i < numImages; i++) {
                precision[i] = cumRelevance[i] / (i + 1);
                recall[i] = cumRelevance[i] / M;
            }
            
            for (int k = 1; k <= M; k++) {
                double milestone = k / (double) M;
                List<Double> matchingPrecisions = new ArrayList<>();
                for (int i = 0; i < numImages; i++) {
                    if (Math.abs(recall[i] - milestone) < 1e-6) {
                        matchingPrecisions.add(precision[i]);
                    }
                }
                prValues[qIdx][k - 1] = matchingPrecisions.isEmpty() ? 0.0 :
                        matchingPrecisions.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            }
            qIdx++;
        }
        
        double[] avgPrecision = new double[M];
        for (int k = 0; k < M; k++) {
            double sum = 0.0;
            for (int i = 0; i < qIdx; i++) sum += prValues[i][k];
            avgPrecision[k] = sum / qIdx;
        }
        
        double[] recallMilestones = new double[M];
        for (int i = 0; i < M; i++) recallMilestones[i] = (i + 1) / (double) M;
        
        Map<String, Object> result = new HashMap<>();
        result.put("recall", recallMilestones);
        result.put("precision", avgPrecision);
        return result;
    }
}