package kdtree;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Main 
{
	public static void main(String[] args)
    {
        System.out.println("Entrer le nom de l'image à charger :");
        String filename = new Scanner(System.in).nextLine();
        
        try{
            File pathToFile = new File(filename);
            BufferedImage img = ImageIO.read(pathToFile);

            int imgHeight = img.getHeight();
            int imgWidth  = img.getWidth();
            BufferedImage res_img = new BufferedImage(imgWidth, imgHeight, img.getType());
            //BufferedImage id_img = new BufferedImage(imgWidth, imgHeight, img.getType());

/////////////////////////////////////////////////////////////////
            Point3i colour;
            ArrayList<Point3i> colPoints = new ArrayList<Point3i>();
            for (int y = 0; y < imgHeight; y++) {
                for (int x = 0; x < imgWidth; x++) {
                    int Color = img.getRGB(x,y);
                    int R = (Color >> 16) & 0xff;
                    int G = (Color >> 8) & 0xff;
                    int B = Color & 0xff;

                    int resR = R, resG = G, resB = B;
                    
                    colour = new Point3i(resR, resG, resB);
                    colPoints.add(colour);
                }
            }
            
            int max_depth = 4;//16 couleurs
            KdTree<Point3i> tree = new KdTree<Point3i>(3, new ArrayList<Point3i>(colPoints), max_depth);
            
            ArrayList<Point3i> palette_colors = new ArrayList<Point3i>();
            tree.getPointsFromLeaf(palette_colors);
            
            ArrayList<RefColor> tmp_palette = new ArrayList<RefColor>(1<<max_depth);
            int i = 0;
            for(Point3i p : palette_colors) {
                tmp_palette.add(new RefColor(p,i));
                ++i;
            }
            
            int v_id[] = new int[imgHeight*imgWidth];
            KdTree<RefColor> paletteTree = new KdTree<RefColor>(3, tmp_palette, Integer.MAX_VALUE);
            RefColor refCol;
            i = 0;
            for (Point3i col : colPoints) {
                refCol = new RefColor(col, -1);
                v_id[i] = paletteTree.getNN(refCol).getId();
                ++i;
            }
            
         // Save an image with the new colors
            for (int y = 0; y < imgHeight; y++) {
                for (int x = 0; x < imgWidth; x++) {

                    int id = v_id[y*imgWidth+x]; // Get id of new color for current pixel
                    Point3i color = palette_colors.get(id);  // Get color from id

                    // Save it in picture

                    int R = color.get(0);
                    int G = color.get(1);
                    int B = color.get(2);

                    int cRes = 0xff000000 | (R << 16)
                                          | (G << 8)
                                          | B;
                    res_img.setRGB(x,y,cRes);
                }
            }
            
         // Save the palette as an image (each color of the palette is represented by a block
         // of 8x8 pixels
         BufferedImage palette_img = new BufferedImage(palette_colors.size()*8, 8, img.getType());
         for (int j = 0; j < palette_colors.size(); j++) {
             int R = palette_colors.get(j).get(0);
             int G = palette_colors.get(j).get(1);
             int B = palette_colors.get(j).get(2);
             int cRes = 0xff000000 | (R << 16)
                                   | (G << 8)
                                   | B;

             for (int y = 0; y < 8; y++) {
                 for (int x = 0; x < 8; x++) {
                     palette_img.setRGB(x+j*8,y,cRes);
                  }
             }
         }
/////////////////////////////////////////////////////////////////

            //ImageIO.write(id_img, "jpg", new File("ResId.jpg"));
         	ImageIO.write(palette_img, "jpg", new File("Palette.jpg"));
            ImageIO.write(res_img, "jpg", new File("ResColor.jpg"));
/////////////////////////////////////////////////////////////////
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
