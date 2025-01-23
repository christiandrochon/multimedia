package fr.cdrochon.multimedias.rechercheparsimilarite.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimilariteCouleurTexture {
    
    @Value("${histogram.texture.output}")
    private String textureDescriptorFile;
    
    @Value("${histogram.rgb666.output}")
    //    @Value("${histogram.gray256.output}")
    private String colorDescriptorFile;
    
    @Value("${images.list.file}")
    private String imagesListFile;
    
    // ponderation entre les descripteurs de couleur et de texture
    private static final double COLOR_WEIGHT = 0.5;
    private static final double TEXTURE_WEIGHT = 0.5;
    
    public List<String> findSimilarImages(String queryImageName, int k) {
        try {
            List<String> imageNames = Files.readAllLines(Paths.get(imagesListFile));
            int queryImageIndex = imageNames.indexOf(queryImageName);
            
            if(queryImageIndex == -1) {
                System.err.println("Image de requête introuvable dans la liste d'images.");
                return new ArrayList<>();
            }
            
            List<List<Float>> textureDescriptors = loadDescriptors(textureDescriptorFile);
            List<List<Float>> colorDescriptors = loadDescriptors(colorDescriptorFile);
            
            List<Float> queryTextureDescriptor = textureDescriptors.get(queryImageIndex);
            List<Float> queryColorDescriptor = colorDescriptors.get(queryImageIndex);
            
            List<ImageDistance> distances = new ArrayList<>();
            for(int i = 0; i < imageNames.size(); i++) {
                if(i != queryImageIndex) {
                    double textureDistance = calculateEuclideanDistance(queryTextureDescriptor, textureDescriptors.get(i));
                    double colorDistance = calculateEuclideanDistance(queryColorDescriptor, colorDescriptors.get(i));
                    double combinedDistance = COLOR_WEIGHT * colorDistance + TEXTURE_WEIGHT * textureDistance;
                    distances.add(new ImageDistance(imageNames.get(i), combinedDistance));
                }
            }
            
            return distances.stream()
                            .sorted(Comparator.comparingDouble(ImageDistance::getDistance))
                            .limit(k)
                            .map(imageDistance -> "/images/" + imageDistance.getImageName())
                            .collect(Collectors.toList());
            
        } catch(IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    private List<List<Float>> loadDescriptors(String descriptorFile) throws IOException {
        List<List<Float>> descriptors = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(descriptorFile));
        
        for(String line : lines) {
            String[] parts = line.trim().split("\\s+");
            List<Float> descriptor = new ArrayList<>();
            for(int i = 1; i < parts.length; i++) {
                descriptor.add(Float.parseFloat(parts[i]));
            }
            descriptors.add(descriptor);
        }
        return descriptors;
    }
    
    private double calculateEuclideanDistance(List<Float> descriptor1, List<Float> descriptor2) {
        double sum = 0.0;
        for(int i = 0; i < descriptor1.size(); i++) {
            double diff = descriptor1.get(i) - descriptor2.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
    
    private static class ImageDistance {
        
        private final String imageName;
        private final double distance;
        
        public ImageDistance(String imageName, double distance) {
            this.imageName = imageName;
            this.distance = distance;
        }
        
        public String getImageName() {
            return imageName;
        }
        
        public double getDistance() {
            return distance;
        }
    }
}
