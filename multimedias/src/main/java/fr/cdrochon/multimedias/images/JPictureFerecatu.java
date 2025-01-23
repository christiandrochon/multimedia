package fr.cdrochon.multimedias.images;

import java.awt.image.*;
import java.io.File;
import javax.imageio.ImageIO;


/**
 *
 * @author Marin Ferecatu
 */

public class JPictureFerecatu {
    static final int RGB_DEPTH = 3, depth = 3, RED = 0, GREEN = 1, BLUE = 2;
    String filename;
    boolean readError;
    int width, height, type;
    float[][][] pixels;
    
    public JPictureFerecatu() {
    }
    
    public JPictureFerecatu(String fileName) {
        readImageFile(fileName);
    }
    
    public JPictureFerecatu(int width, int height, int type, String filename, float[][][] pixels) {
        // deep constructor
        this.width = width;
        this.height = height;
        this.type = type;
        this.filename = filename;
        float[][][] pc = clonePixels(pixels);
        this.pixels = pc;
    }
    
    public JPictureFerecatu(int width, int height) {
        this.width = width;
        this.height = height;
        type = 5; // BufferedImage.TYPE_3BYTE_BGR
        pixels = new float[width][height][RGB_DEPTH];
    }
    
    double max3(double a, double b, double c) {
        return ((a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c));
    }
    
    double min3(double a, double b, double c) {
        return ((a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c));
    }
    
    float[][][] clonePixels(float[][][] pixels) {
        int x, y;
        float[][][] pc = new float[width][height][RGB_DEPTH]; // pixels copy
        for(x = 0; x < width; x++) for(y = 0; y < height; y++) {
            System.arraycopy(pixels[x][y], 0, pc[x][y], 0, RGB_DEPTH);
        }
        return pc;
    }
    
    void setPixels(double[][] v) {
        width = v.length;
        height = v[0].length;
        pixels = new float[width][height][RGB_DEPTH];
        for(int x = 0; x < width; x++) for(int y = 0; y < height; y++) {
            pixels[x][y][RED] = pixels[x][y][GREEN] = pixels[x][y][BLUE] = (float)v[x][y];
        }
    }
    
    void readImageFile(String fileName){
        try {
            BufferedImage img = ImageIO.read(new File(fileName));
            width = img.getWidth();
            height = img.getHeight();
            // type = img.getType();
            type = 5; // BufferedImage.TYPE_3BYTE_BGR
            pixels = new float[width][height][RGB_DEPTH];
            int[] rgbArray = img.getRGB(0, 0, width, height, null, 0, width);
            int pixel;
            for(int x = 0; x < width; ++x)
                for(int y = 0; y < height; ++y) {
                    pixel = rgbArray[y*width + x];
                    // int alpha = (pixel >> 24) & 0xff; // we do not use alpha
                    pixels[x][y][RED]   = (float)((pixel >> 16) & 0xff)/255.0f;
                    if(pixels[x][y][RED] == 1) pixels[x][y][RED] -= 1e-5f;
                    pixels[x][y][GREEN] = (float)((pixel >>  8) & 0xff)/255.0f;
                    if(pixels[x][y][GREEN] == 1) pixels[x][y][GREEN] -= 1e-5f;
                    pixels[x][y][BLUE]  = (float)((pixel      ) & 0xff)/255.0f;
                    if(pixels[x][y][BLUE] == 1) pixels[x][y][BLUE] -= 1e-5f;
                }
        } catch (Exception e) {
            e.printStackTrace();
            readError = true;
        }
    }
    
    void writeImageFile(String filename) {
        int[] rgbArray = new int[width*height];
        this.filename = filename;
        for(int x = 0; x < width; ++x)
            for(int y = 0; y < height; ++y) {
                rgbArray[y*width + x] = (((int)(pixels[x][y][RED]*255.0f) & 0xff)   << 16) |
                        (((int)(pixels[x][y][GREEN]*255.0f) & 0xff) <<  8) |
                        (((int)(pixels[x][y][BLUE]*255.0f) & 0xff)       );
            }
        BufferedImage img = new BufferedImage(width, height, type);
        img.setRGB(0, 0, width, height, rgbArray, 0, width);
        try {
            ImageIO.write(img, "jpg", new File(filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static void main(String[] args) {
        JPictureFerecatu p = new JPictureFerecatu("horse.jpg");
        
        for(int i = 0; i < p.width; ++i) {
            for(int j = 0; j < p.height/2; ++j) {
                p.pixels[i][j][RED] = 1 - p.pixels[i][j][RED];
                p.pixels[i][j][GREEN] = 1 - p.pixels[i][j][GREEN];
                p.pixels[i][j][BLUE] = 1 - p.pixels[i][j][BLUE];
            }
        }
        
        p.writeImageFile("horse1.jpg");
    }
}
