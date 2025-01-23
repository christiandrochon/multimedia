package fr.cdrochon.multimedias.config;

import fr.cdrochon.multimedias.generationdescripteurs.gris.DescripteurGrey256_1f;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@ComponentScan(basePackages = "fr.cdrochon.multimedias.generationdescripteurs")
public class HistogramConsoleApp {
    
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(HistogramConsoleApp.class);
        DescripteurGrey256_1f descripteur = context.getBean(DescripteurGrey256_1f.class);
        
        try {
            descripteur.process();
            float[] histogram = descripteur.computeHistogram();
            for(float value : histogram) {
                System.out.print(value + " ");
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            context.close();
        }
    }
}