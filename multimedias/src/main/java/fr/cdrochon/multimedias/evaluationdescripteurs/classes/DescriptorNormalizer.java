package fr.cdrochon.multimedias.evaluationdescripteurs.classes;

import org.apache.commons.math3.linear.*;

import java.util.*;
import java.util.stream.Collectors;

public class DescriptorNormalizer {
    
    /**
     * Normalise les descripteurs à une dimension cible en utilisant PCA.
     *
     * @param descriptorMap   Map associant les noms d'images à leurs descripteurs
     * @param targetDimension Dimension cible pour la réduction
     * @return Map normalisée associant les noms d'images aux descripteurs réduits
     */
    public static Map<String, List<Double>> normalizeDescriptors(Map<String, List<Double>> descriptorMap, int targetDimension) {
        // Étape 1 : Vérifier les dimensions et convertir en matrice
        double[][] descriptorMatrix = descriptorMap.values().stream()
                                                   .map(descriptor -> descriptor.stream().mapToDouble(Double::doubleValue).toArray())
                                                   .toArray(double[][]::new);
        
        int originalDimension = descriptorMatrix[0].length;
        if(targetDimension > originalDimension) {
            throw new IllegalArgumentException("La dimension cible dépasse la dimension originale des descripteurs.");
        }
        
        // Étape 2 : Centrage des données (soustraction de la moyenne colonne par colonne)
        RealMatrix dataMatrix = new Array2DRowRealMatrix(descriptorMatrix);
        double[] columnMeans = new double[dataMatrix.getColumnDimension()];
        for(int col = 0; col < columnMeans.length; col++) {
            columnMeans[col] = Arrays.stream(dataMatrix.getColumn(col)).average().orElse(0.0);
            for(int row = 0; row < dataMatrix.getRowDimension(); row++) {
                dataMatrix.addToEntry(row, col, -columnMeans[col]);
            }
        }
        
        // Étape 3 : Mise à l'échelle (normalisation colonne par colonne)
        for(int col = 0; col < dataMatrix.getColumnDimension(); col++) {
            double stdDev = Math.sqrt(Arrays.stream(dataMatrix.getColumn(col))
                                            .map(val -> Math.pow(val, 2))
                                            .average()
                                            .orElse(1.0));
            if(stdDev > 0) {
                for(int row = 0; row < dataMatrix.getRowDimension(); row++) {
                    dataMatrix.setEntry(row, col, dataMatrix.getEntry(row, col) / stdDev);
                }
            }
        }
        
        // Étape 4 : Réduction dimensionnelle avec PCA (SVD)
        SingularValueDecomposition svd = new SingularValueDecomposition(dataMatrix);
        double[] singularValues = svd.getSingularValues();
        double totalVariance = Arrays.stream(singularValues).sum();
        double cumulativeVariance = 0.0;
        int optimalDimensions = 0;
        
        // Trouver la dimension optimale pour 95 % de variance expliquée
        for(int i = 0; i < singularValues.length; i++) {
            cumulativeVariance += singularValues[i];
            if((cumulativeVariance / totalVariance) >= 0.95) {
                optimalDimensions = i + 1;
                break;
            }
        }
        
        // Ajuster la dimension cible si nécessaire
        if(targetDimension > optimalDimensions) {
            System.out.printf("Dimension cible ajustée de %d à %d pour atteindre 95%% de variance expliquée.%n", targetDimension, optimalDimensions);
            targetDimension = optimalDimensions;
        }
        
        // Réduire la dimension avec les vecteurs propres correspondants
        RealMatrix reducedMatrix = svd.getU().getSubMatrix(0, descriptorMatrix.length - 1, 0, targetDimension - 1);
        
        // Étape 5 : Mise à l'échelle globale après PCA
        for(int col = 0; col < reducedMatrix.getColumnDimension(); col++) {
            double min = Arrays.stream(reducedMatrix.getColumn(col)).min().orElse(0.0);
            double max = Arrays.stream(reducedMatrix.getColumn(col)).max().orElse(1.0);
            if(max != min) { // Éviter division par zéro
                for(int row = 0; row < reducedMatrix.getRowDimension(); row++) {
                    double scaledValue = (reducedMatrix.getEntry(row, col) - min) / (max - min);
                    reducedMatrix.setEntry(row, col, scaledValue);
                }
            } else {
                // Si toutes les valeurs sont égales, fixez-les à 0.5 pour éviter un impact disproportionné
                for(int row = 0; row < reducedMatrix.getRowDimension(); row++) {
                    reducedMatrix.setEntry(row, col, 0.5);
                }
            }
        }
        
        //        for (int col = 0; col < reducedMatrix.getColumnDimension(); col++) {
        //            double min = Arrays.stream(reducedMatrix.getColumn(col)).min().orElse(0.0);
        //            double max = Arrays.stream(reducedMatrix.getColumn(col)).max().orElse(1.0);
        //            for (int row = 0; row < reducedMatrix.getRowDimension(); row++) {
        //                double scaledValue = (reducedMatrix.getEntry(row, col) - min) / (max - min);
        //                reducedMatrix.setEntry(row, col, scaledValue);
        //            }
        //        }
        
        // Étape 6 : Reconstruire la Map avec les descripteurs réduits et normalisés
        Map<String, List<Double>> normalizedDescriptorMap = new LinkedHashMap<>();
        String[] keys = descriptorMap.keySet().toArray(new String[0]);
        
        // Assurez-vous que la somme est proche de 1
        for(int row = 0; row < reducedMatrix.getRowDimension(); row++) {
            double sum = Arrays.stream(reducedMatrix.getRow(row)).sum();
            if(sum > 0) { // Éviter la division par zéro
                for(int col = 0; col < reducedMatrix.getColumnDimension(); col++) {
                    reducedMatrix.setEntry(row, col, reducedMatrix.getEntry(row, col) / sum);
                }
            }
        }
        
        
        for(int i = 0; i < reducedMatrix.getRowDimension(); i++) {
            double[] reducedDescriptor = reducedMatrix.getRow(i);
            List<Double> normalizedDescriptor = Arrays.stream(reducedDescriptor)
                                                      .map(value -> Math.max(0, Math.min(1, value))) // Clip pour s'assurer que tout est entre 0 et 1
                                                      .boxed()
                                                      .collect(Collectors.toList());
            normalizedDescriptorMap.put(keys[i], normalizedDescriptor);
        }
        
        return normalizedDescriptorMap;
    }
}
