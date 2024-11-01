import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.IntStream;

public class ImageCutter
{

    public ImageCutter() throws IOException {
        Path cropsPath = Paths.get("./crops");
        if (!Files.exists(cropsPath) || !Files.isDirectory(cropsPath)) Files.createDirectory(cropsPath);

    }
    public BufferedImage[] cut(BufferedImage imageToCut, BufferedImage iconToCut, int[] points, int q1X, int q1Y, int q2Y)
    {
        q1X = transform(q1X, iconToCut.getWidth(), imageToCut.getWidth());
        q1Y = transform(q1Y, iconToCut.getHeight(), imageToCut.getHeight());
        q2Y = transform(q2Y, iconToCut.getHeight(), imageToCut.getHeight()); // maybe FIXME
        int secPoint = transform(points[0], iconToCut.getWidth(), imageToCut.getWidth());
        BufferedImage[] images = new BufferedImage[points.length];
        int diff = secPoint-q1X;
        for (int a = 0; a < points.length; a++)
        {
            System.out.println(q1X+a*diff);
            System.out.println(q1Y);
            System.out.println(diff);
            System.out.println(q2Y-q1Y);
            System.out.println();

            try{
                images[a] = imageToCut.getSubimage(q1X+a*diff,q1Y,diff,q2Y-q1Y);
            }
            catch (RasterFormatException e)
            {
                images[a] = imageToCut.getSubimage(q1X+a*diff,q1Y,Math.min(imageToCut.getWidth()-q1X-a*diff, diff),Math.min(imageToCut.getHeight()-q1Y,q2Y-q1Y));
            }
        }
        return images;
    }

    private int transform(int simulatedPoint, int simulationStretch, int actualStretch)
    { // this is not to be used to often bcs floating point division, we want a seamless transition between photos, pixel-wise
        return (int)((double)(simulatedPoint)*actualStretch/simulationStretch);
    }

    public void save(BufferedImage[] image, String name) throws IOException {
        Path imagesPath = Paths.get("./crops", name);
        if (!Files.exists(imagesPath) || !Files.isDirectory(imagesPath)) Files.createDirectory(imagesPath);
        IntStream.range(0, image.length).forEach(i->{
            save(image[i],imagesPath,name.substring(0, name.lastIndexOf('.'))+"_"+(i+1)+".png");
        });
    }
    public void save(BufferedImage image, Path path, String name)
            // format is defaulted to png for its no data loss and no compression
    {
        File outputFile = new File(String.valueOf(path), name);
        try {
            ImageIO.write(image, "png", outputFile);
            System.out.println("Image saved successfully at " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to save the image " + outputFile.getAbsolutePath());
        }
    }


}
