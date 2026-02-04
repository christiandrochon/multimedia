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
public class PRCurveCBIR {
    
    @Value("${resnet.VT_files}")
    private String vtFilesPath;
    
    @Value("${resnet.VT_description}")
    private String vtDescriptionPath;
    
    @Value("${images.list.output}")
    private String base10000files;
    
    @Value("${resnet.resnet18}")
    private String resnet18;
    
    @Async
    public CompletableFuture<Map<String, Object>> computePRCurveAsync() throws IOException {
        System.out.println("▶️ THREAD: " + Thread.currentThread().getName());
        
        List<String> baseFiles = readLines(base10000files);
        List<String> classes = readLines(vtDescriptionPath);
        List<List<String>> vtFiles = readVTFiles(vtFilesPath);
        
        Map<String, String> gt = new HashMap<>();
        Map<String, List<String>> classToFiles = new HashMap<>();
        for (int i = 0; i < classes.size(); i++) {
            String className = classes.get(i);
            List<String> files = vtFiles.get(i);
            classToFiles.put(className, files);
            for (String file : files) gt.put(file, className);
        }
        
        double[][] descriptors = loadDescriptors(resnet18);
        Map<String, Object> result = compute(baseFiles, descriptors, gt, classToFiles);
        return CompletableFuture.completedFuture(result);
        
    }
    
    private List<String> readLines(String path) throws IOException {
        return Files.readAllLines(Paths.get(path));
    }
    
    private List<List<String>> readVTFiles(String path) throws IOException {
        return Files.readAllLines(Paths.get(path)).stream()
                    .map(line -> Arrays.asList(line.trim().split("\\s+")))
                    .collect(Collectors.toList());
    }
    
    private double[][] loadDescriptors(String path) throws IOException {
        return Files.readAllLines(Paths.get(path)).stream()
                    .map(line -> Arrays.stream(line.trim().split("\\s+"))
                                       .mapToDouble(Double::parseDouble)
                                       .toArray())
                    .toArray(double[][]::new);
    }
    
    private Map<String, Object> compute(List<String> baseFiles, double[][] desc, Map<String, String> gt, Map<String, List<String>> classToFiles) {
        int N = desc.length;
        int D = desc[0].length;
        int M = classToFiles.values().iterator().next().size();
        double[][] pr = new double[gt.size()][M];
        
        Map<String, Integer> fileIndex = new HashMap<>();
        for (int i = 0; i < baseFiles.size(); i++) fileIndex.put(baseFiles.get(i), i);
        
        int q = 0;
        for (String file : gt.keySet()) {
            if (!fileIndex.containsKey(file)) continue;
            
            int idx = fileIndex.get(file);
            double[] qVec = desc[idx];
            
            double[] dists = new double[N];
            for (int i = 0; i < N; i++) {
                double sum = 0;
                for (int j = 0; j < D; j++) {
                    double diff = qVec[j] - desc[i][j];
                    sum += diff * diff;
                }
                dists[i] = Math.sqrt(sum);
            }
            
            Integer[] ranks = new Integer[N];
            for (int i = 0; i < N; i++) ranks[i] = i;
            Arrays.sort(ranks, Comparator.comparingDouble(i -> dists[i]));
            
            String cls = gt.get(file);
            boolean[] relevant = new boolean[N];
            for (int i = 0; i < N; i++) {
                String other = baseFiles.get(ranks[i]);
                relevant[i] = cls.equals(gt.getOrDefault(other, ""));
            }
            
            double[] cum = new double[N];
            cum[0] = relevant[0] ? 1 : 0;
            for (int i = 1; i < N; i++) cum[i] = cum[i - 1] + (relevant[i] ? 1 : 0);
            
            for (int k = 1; k <= M; k++) {
                double recall = k / (double) M;
                double bestP = 0.0;
                for (int i = 0; i < N; i++) {
                    double r = cum[i] / M;
                    if (Math.abs(r - recall) < 1e-6) bestP += cum[i] / (i + 1);
                }
                pr[q][k - 1] = bestP;
            }
            q++;
        }
        
        double[] avgPr = new double[M];
        for (int i = 0; i < M; i++) {
            double sum = 0;
            for (int j = 0; j < q; j++) sum += pr[j][i];
            avgPr[i] = sum / q;
        }
        
        double[] recall = new double[M];
        for (int i = 0; i < M; i++) recall[i] = (i + 1) / (double) M;
        
        Map<String, Object> out = new HashMap<>();
        out.put("recall", recall);
        out.put("precision", avgPr);
        return out;
    }
}

