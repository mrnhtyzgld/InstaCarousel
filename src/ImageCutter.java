import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
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
    public BufferedImage[] cut(BufferedImage imageToCut, int[] points)
    {
        return new BufferedImage[]{imageToCut}; // TODO FİXME
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
