import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Main extends JPanel {
    public static void main(String[] args) {
        new Main();
    }

    int imageCount;
    public final int SCREEN_WIDTH, SCREEN_HEIGHT;
    public final int ICON_SIZE;
    BufferedImage[] images;
    BufferedImage[] imageIcons;
    int selectedX=0,selectedY=0;
    String state = "selection"; // selection or carousel
    int maxX,maxY;
    BufferedImage imageToCrop = null;
    BufferedImage iconToCrop = null;
    String[] imageNames;
    String currentName;

    int cropImX;
    int cropImY;
    int cropIcX;
    int cropIcY;

    int infoX; int infoY;

    char cropMod = 'q';
    boolean infoMode = true;
    int cropCount;

    int firstX,firstY,secX,secY;
    boolean areaDisable =true;
    boolean dragging =false;


    int currMouseX, currMouseY;

    int[] Xs;
    int q1Y, q1X, q2Y;

    public Main()
    {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.SCREEN_WIDTH = (int) screenSize.getWidth();
        this.SCREEN_HEIGHT = (int) screenSize.getHeight() - 70;
        ICON_SIZE =SCREEN_WIDTH/8; // TODO feature? this can be 200 flat instead if needed


        File[] files = findImages();
        imageCount = files.length;
        images = new BufferedImage[imageCount];
        imageIcons = new BufferedImage[imageCount];
        imageNames = new String[imageCount];

        try {
            for (int a = 0; a < imageCount; a++) {
                imageNames[a] = files[a].getName();
                images[a] = ImageIO.read(files[a]);
                imageIcons[a] = this.toBufferedImage(images[a].getScaledInstance(ICON_SIZE, ICON_SIZE, BufferedImage.SCALE_FAST));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        JFrame mainF = new JFrame();
        mainF.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainF.setExtendedState(Frame.MAXIMIZED_BOTH);
        mainF.setSize(500, 700);
        mainF.setLocationRelativeTo(null);
        mainF.add(this);
        mainF.setBackground(Color.DARK_GRAY);
        mainF.addKeyListener(new Keys());
        Mouse mouse = new Mouse();
        mainF.addMouseListener(mouse);
        mainF.addMouseMotionListener(mouse);
        this.setBackground(Color.DARK_GRAY);


        mainF.setVisible(true);


    }

    public File[] findImages()
    {
        String dirString = "./resources";

        // Regex deseni: .jpg, .jpeg, veya .png uzantılı dosyalar
        Pattern pattern = Pattern.compile(".*\\.(jpg|jpeg|png)$", Pattern.CASE_INSENSITIVE);

        File dir = new File(dirString);
        Path dirPath = Paths.get(dirString);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            try {
                Files.createDirectory(dirPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Dizindeki tüm uygun dosyaları listele
        File[] files = dir.listFiles((dir1, name) -> pattern.matcher(name).matches());

        if (files != null) {
            for (File file : files) {
                System.out.println(file.getName());
            }
        } else {
            System.out.println("Belirtilen dizin geçerli değil veya dizinde hiçbir dosya bulunamadı.");
        }

        return files;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (state.equals("selection")) {
            maxX = SCREEN_WIDTH / ICON_SIZE;
            maxY = SCREEN_HEIGHT / ICON_SIZE;
            for (int y = 0; y < maxY; y++) {
                for (int x = 0; x < maxX && y * maxX + x < imageIcons.length; x++) {
                    g.drawImage(imageIcons[y * maxX + x], x * ICON_SIZE, y * ICON_SIZE, this);
                }
            }
            printSelectedIcon(g, 7, Color.WHITE);
            printSelectedIcon(g, 5, Color.BLACK);

        } else {
            g.drawImage(iconToCrop, 0, 0, this);
            if (infoMode) printInfo(g, Color.WHITE);
            if (!areaDisable)
            {
                printSelectedArea(g, 6, Color.WHITE, Color.WHITE);
                printSelectedArea(g, 3, Color.RED, Color.GRAY);
            }
        }
    }

    private void printSelectedIcon(Graphics g, int boldness, Color color)
    {
        g.setColor(color);
        g.fillRect(selectedX * ICON_SIZE, selectedY * ICON_SIZE, ICON_SIZE, boldness);
        g.fillRect((selectedX + 1) * ICON_SIZE, selectedY * ICON_SIZE, boldness, ICON_SIZE + boldness);
        g.fillRect((selectedX) * ICON_SIZE, (selectedY + 1) * ICON_SIZE, ICON_SIZE + boldness, boldness);
        g.fillRect((selectedX) * ICON_SIZE, (selectedY) * ICON_SIZE, boldness, ICON_SIZE);

    }

    private void printSelectedArea (Graphics g,int boldness, Color color, Color color2)
    {
        g.setColor(color);
        q1X = Math.min(firstX, secX);
        q1Y = Math.min(firstY, secY);
        int q2X = Math.max(firstX, secX);
        q2Y = Math.max(firstY, secY);

        g.setColor(color2);
        g.fillRect(q1X, q1Y, q2X - q1X, boldness);
        g.fillRect(q2X, q1Y, boldness, q2Y - q1Y + boldness);
        g.fillRect(q1X, q2Y, q2X - q1X + boldness, boldness);
        g.fillRect(q1X, q1Y, boldness, q2Y - q1Y);

        int y = q2Y - q1Y;
        int x = q2X - q1X;
        if (y > 2 * boldness && x > 2 * boldness && ((cropMod == 'q' && x > y) || (cropMod == 'w' && x > 4.0 / 5 * y))) {
            if (cropMod == 'q') //1:1
            {
                Xs = new int[(int) ((double) (x) / (double) (y))];
                for (int a = 0; a < Xs.length; a++) {
                    Xs[a] = (int) ((a + 1) * y + q1X);
                }
            } else // 4:5
            {
                Xs = new int[(int) (5.0 * x / (4 * y))];
                for (int a = 0; a < Xs.length; a++) {
                    Xs[a] = (int) ((a + 1) * y * 4.0 / 5 + q1X);
                }

            }
            q2X=Xs[Xs.length-1];
            g.setColor(color);
            g.fillRect(q1X, q1Y, q2X - q1X, boldness);
            g.fillRect(q2X, q1Y, boldness, q2Y - q1Y + boldness);
            g.fillRect(q1X, q2Y, q2X - q1X + boldness, boldness);
            g.fillRect(q1X, q1Y, boldness, q2Y - q1Y);


            for (int a = 0; a < Xs.length; a++) {
                g.fillRect(Xs[a], q1Y, boldness, y);
            }

        }


    }


    private void printInfo(Graphics g, Color color)
    {
        g.setColor(color);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("INSTAGRAM CAROUSEL ARACI");
        strings.add("E'YE BASARSANIZ FOTO BÜYÜR");
        strings.add("");
        strings.add("Basılı tutarak kırpmak istediğiniz bölgeyi seçin");
        strings.add("./resources: girdi fotoğrafları");
        strings.add("./crops: kesilen fotoğraflar");
        strings.add("");
        strings.add("Enter: işlemi tamamla");
        strings.add("ESC: menü'ye dönüş");
        strings.add("Q: 1:1 ratio seçer (kare)");
        strings.add("W: 4/5 ratio seçer (dikey)");
        strings.add("şuanki mod: " + cropMod);
        strings.add("");
        strings.add("Nihat Emre Yüzügüldü github.com/mrnhtyzgld");

        for (int a = 0; a < strings.size(); a++) {
            g.drawString(strings.get(a), infoX, infoY + a * 30);
        }
    }

    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    private int getCurrImage()
    {
        return selectedX + selectedY * maxX;
    }

    class Keys implements KeyListener {

        @Override
        public void keyReleased(KeyEvent e) {
            if (state.equals("selection")) return;

            if (e.getKeyCode() == 27) // esc
            {
                state = "selection";iconToCrop=null;imageToCrop=null;
                areaDisable = true;
            }
            if (e.getKeyChar()=='q')
            {
                cropMod='q';
            }
            if (e.getKeyChar()=='w')
            {
                cropMod='w';
            }
            if (e.getKeyChar()>='0' &&e.getKeyChar()<='9') // numbers
            {
                cropCount=e.getKeyChar()-'0';
            }
            if (e.getKeyChar()=='r')
            {
                System.out.println("firstX " + firstX);
                System.out.println("firstY " + firstY);
                System.out.println("secX " + secX);
                System.out.println("secY " + secY);
                System.out.println("mouseX " + currMouseX);
                System.out.println("mouseY " + currMouseY);
                System.out.println("---"+Xs.length);
            }
            if (e.getKeyChar()=='e')
            {
                infoMode=!infoMode;
                areaDisable = true;

                findCoordinatesOfCropScreen();
            }

                repaint();
        }

        @Override
        public void keyTyped(KeyEvent e) {}
        @Override
        public void keyPressed(KeyEvent e)
        {
            if (state.equals("carousel") && ! areaDisable)
            {
                if (e.getKeyCode() == 10) { // enter
                    if (cropMod=='q'&&Math.abs(firstX-secX)>Math.abs(firstY-secY) || cropMod=='w'&&Math.abs(firstX-secX)>(int)(Math.abs(firstY-secY)*4.0/5)) {
                        ImageCutter ic;
                        try {
                            ic = new ImageCutter();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        try {
                            ic.save(ic.cut(imageToCrop, iconToCrop, Xs, q1X, q1Y, q2Y), currentName);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }

                repaint();
            };
            if (e.getKeyCode() == 38) { // w
                selectedY = Math.max(0, --selectedY);
            }
            if (e.getKeyCode() == 37) { // a
                selectedX = Math.max(0, --selectedX);
            }
            if (e.getKeyCode() == 40) { // s
                if (selectedY * maxX + selectedX + maxX < imageCount)
                    selectedY++;
            }
            if (e.getKeyCode() == 39) { // d
                if (selectedY * maxX + selectedX + 1 < imageCount && selectedX + 1 < maxX)
                    selectedX++;
            }

            if (e.getKeyCode() == 10) { // enter

                findCoordinatesOfCropScreen();

            }

            repaint();
        }
    }

    class Mouse implements MouseListener, MouseMotionListener
    {
        int xBuff = -8, yBuff=-32;
        @Override
        public void mouseClicked(MouseEvent e) {
            areaDisable=true;
        }
        @Override
        public void mousePressed(MouseEvent e) {
            dragging=true;
            firstX=Math.min(e.getX()+xBuff,cropIcX);
            firstY=Math.min(e.getY()+yBuff, cropIcY);
            repaint();
            if (state.equals("carousel")) areaDisable = false;
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            dragging = false;
            secX=Math.min(e.getX()+xBuff,cropIcX);
            secY=Math.min(e.getY()+yBuff, cropIcY);
            repaint();
        }
        @Override
        public void mouseEntered(MouseEvent e) {
        }
        @Override
        public void mouseExited(MouseEvent e) {
        }
        @Override
        public void mouseDragged(MouseEvent e) {
            currMouseX=Math.min(e.getX()+xBuff,cropIcX);
            currMouseY=Math.min(e.getY()+yBuff, cropIcY);
            secX=Math.min(e.getX()+xBuff,cropIcX);
            secY=Math.min(e.getY()+yBuff, cropIcY);
            repaint();
        }
        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }


    private void findCoordinatesOfCropScreen()
    {
        imageToCrop = images[getCurrImage()];
        currentName = imageNames[getCurrImage()];
        state = "carousel";
        int infoBuffer = 30;
        int anon = 300;
        if (!infoMode) anon = 0;
        if (SCREEN_HEIGHT/SCREEN_WIDTH<imageToCrop.getHeight()/imageToCrop.getWidth()) // h
        {
            iconToCrop = Main.toBufferedImage(imageToCrop.getScaledInstance(imageToCrop.getWidth() * SCREEN_HEIGHT / imageToCrop.getHeight(), (SCREEN_HEIGHT * imageToCrop.getHeight() / imageToCrop.getHeight()), BufferedImage.SCALE_FAST));
            infoX =iconToCrop.getWidth()+infoBuffer;
            infoY = infoBuffer;
        } else {
            iconToCrop = Main.toBufferedImage(imageToCrop.getScaledInstance(imageToCrop.getWidth() * (SCREEN_WIDTH-anon) / imageToCrop.getWidth(), ((SCREEN_WIDTH-anon) * imageToCrop.getHeight() / imageToCrop.getWidth()), BufferedImage.SCALE_FAST));
            //infoX = 0; // TODO wanted feature?
            //infoY = iconToCrop.getHeight()+infoBuffer;
            infoX = iconToCrop.getWidth()+infoBuffer;
            infoY = infoBuffer;
        }
        cropImX = imageToCrop.getWidth();
        cropImY = imageToCrop.getHeight();
        cropIcX = iconToCrop.getWidth();
        cropIcY = iconToCrop.getHeight();


    }
}
