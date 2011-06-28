package AP2DX.mapper;


// Imports for the interface
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.Thread;
import java.io.File;
import java.io.IOException;

/**
 * @author Maarten de Waard
 */
public class PictureViewer extends JPanel
{
    private static String pathToTheImage;
    private BufferedImage backgroundImage;
    public static int imageCounter = 0;
    private static JFrame frame;
    
    public static void main(String[] args)
    {
        frame = new JFrame("Map");
        PictureViewer viewer = new PictureViewer();
        frame.setBackground(java.awt.Color.white);

        frame.getContentPane().add(viewer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500,500);
        frame.setVisible(true);
        String pathToTheImage = "../../../../../c/dpslam/hmap";
        String image;
        BufferedImage bgImage;
        while(true)
        {
            try 
            {
                image = String.format("%s%02d%s", pathToTheImage, imageCounter, ".png");
                bgImage = ImageIO.read(new File(image));
                imageCounter++;
                viewer.updateImage(bgImage);
            } catch (IOException ex) 
            {
                try{Thread.sleep(1000);}catch(Exception e){}
            }
        }
    }

    public PictureViewer()
    {
        super();
    }

    public PictureViewer(String slamDir)
    {
        super();
        this.pathToTheImage = slamDir;
    }


    public void updateImage(BufferedImage backgroundImage)
    {
        this.backgroundImage = backgroundImage;
        frame.setSize(backgroundImage.getWidth(),backgroundImage.getHeight());
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);        
        if(backgroundImage != null)
            g.drawImage(this.backgroundImage, 0, 0, null);
    }

}
