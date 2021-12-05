import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.util.*;

public class ImageManipulator extends Application implements ImageManipulatorInterface{
    private Stage stage = null;
    private Scene scene = null;
    private Group root = null;
    private double width = 640,
                   height = 480;
    /**
     * Load the specified PPM image file.
     * The image file must be in the PPM P3 format
     *
     * @param filename
     * @return WritableImage
     * @throws FileNotFoundException
     * @link http://netpbm.sourceforge.net/doc/ppm.html
     * <p>
     * Don't forget to add a load button to the application!
     */
    @Override
    public WritableImage loadImage(String filename) throws FileNotFoundException {
        File imageFile = new File(filename);
        Integer width, height, colorSpace;
        try(Scanner imageScanner = new Scanner(imageFile)) {
            if (imageScanner.next() != "P3") {
                throw new IllegalArgumentException();
            }
            while(width == null || height == null || colorSpace == null) {
                String next = imageScanner.next();
                Scanner nextScanner = new Scanner(next);
                if(next.charAt(0) == '#') {
                    imageScanner.nextLine();
                    break;
                }
                if(width == null) {
                    width = nextScanner.nextInt();
                } else if (height == null) {
                    height = nextScanner.nextInt();
                } else {
                    colorSpace = nextScanner.nextInt();
                }
            }
            WritableImage image = new WritableImage(width,height);
            try {
                for(int i = 0; i < height; i++) {
                    for(int j = 0; j < width; j++) {
                        int red = imageScanner.nextInt()/colorSpace;
                        int green = imageScanner.nextInt()/colorSpace;
                        int blue = imageScanner.nextInt()/colorSpace;
                        image.getPixelWriter().setColor(j,i,Color.rgb(red,green,blue));
                    }
                }
            } catch (InputMismatchException exception) {
                imageScanner.nextLine();
            } catch (NoSuchElementException exception) {
            }
            return image;
        } catch (FileNotFoundException error) {
            throw error;
        }
    }

    /**
     * Save the specified image to a PPM file.
     * The image file must be in the PPM P3 format
     *
     * @param filename
     * @param image
     * @throws FileNotFoundException
     * @link http://netpbm.sourceforge.net/doc/ppm.html
     * <p>
     * Don't forget to add a save button to the application!
     */
    @Override
    public void saveImage(String filename, WritableImage image) throws FileNotFoundException {

    }

    /**
     * Invert an image by subtracting each RGB component from its max value
     * <p>
     * For example:
     * rbg( 255, 255, 255 ) -- invert --> rbg( 0, 0, 0 )
     * rbg( 0, 0, 0 ) -- invert --> rbg( 255, 255, 255 )
     * rbg( 255, 110, 63 ) -- invert --> rbg( 0, 145, 192 )
     * rbg( 0, 145, 192 ) -- invert --> rbg( 255, 110, 63 )
     *
     * @param image - the image to be inverted, do not modify!
     * @return a new inverted image
     */
    @Override
    public WritableImage invertImage(WritableImage image) {
        Color[][] pixels = new Color[(int)image.getHeight()][(int)image.getWidth()];
        for(int i = 0; i < pixels.length; i++) {
            for(int j = 0; j < pixels[1].length; j++) {
                pixels[i][j] = image.getPixelReader().getColor(i,j);
                int red = (int)(255-(pixels[i][j].getRed()*255));
                int green = (int)(255-(pixels[i][j].getGreen()*255));
                int blue = (int)(255-(pixels[i][j].getBlue()*255));
                image.getPixelWriter().setColor(i,j,Color.rgb(red,green,blue));
            }
        }
        return image;
    }

    /**
     * Convert an image to grayscale using the following formula:
     * intensity = 0.2989*red + 0.5870*green + 0.1140*blue
     * new rgb( intensity, intensity, intensity );
     * <p>
     * For example:
     * rbg( 0, 255, 255 ) -- grayify --> rbg( 178, 178, 178 )
     * rbg( 255, 0, 255 ) -- grayify --> rbg( 105, 105, 105 )
     * rbg( 255, 255, 0 ) -- grayify --> rbg( 225, 225, 225 )
     * rbg( 21, 11, 11 ) -- grayify --> rbg( 13, 13, 13 )
     *
     * @param image - the image to be converted to grayscale, do not modify!
     * @return a new image that displays in shades of gray
     */
    @Override
    public WritableImage grayifyImage(WritableImage image) {
        Color[][] pixels = new Color[(int)image.getHeight()][(int)image.getWidth()];
        for(int i = 0; i < pixels.length; i++) {
            for(int j = 0; j < pixels[1].length; j++) {
                pixels[i][j] = image.getPixelReader().getColor(i,j);
                int red = (int)(255-(pixels[i][j].getRed()*0.2989));
                int green = (int)(255-(pixels[i][j].getGreen()*0.5870));
                int blue = (int)(255-(pixels[i][j].getBlue()*0.1140));
                image.getPixelWriter().setColor(i,j,Color.rgb(red,green,blue));
            }
        }
        return image;
    }

    /**
     * Pixelates the image by dividing it into 5x5 regions, then assigning
     * all pixels in the region the same color as the central pixel.
     * <p>
     * For example:
     * [0,0,0] [0,0,0] [0,0,0] [0,0,0] [0,0,0]
     * [0,0,0] [5,5,5] [5,5,5] [5,5,5] [0,0,0]
     * [0,0,0] [5,5,5] [1,2,3] [5,5,5] [0,0,0]
     * [0,0,0] [5,5,5] [5,5,5] [5,5,5] [0,0,0]
     * [0,0,0] [0,0,0] [0,0,0] [0,0,0] [0,0,0]
     * <p>
     * is pixelated to
     * <p>
     * [1,2,3] [1,2,3] [1,2,3] [1,2,3] [1,2,3]
     * [1,2,3] [1,2,3] [1,2,3] [1,2,3] [1,2,3]
     * [1,2,3] [1,2,3] [1,2,3] [1,2,3] [1,2,3]
     * [1,2,3] [1,2,3] [1,2,3] [1,2,3] [1,2,3]
     * [1,2,3] [1,2,3] [1,2,3] [1,2,3] [1,2,3]
     *
     * @param image - the image to be converted to grayscale, do not modify!
     * @return a new image that displays in shades of gray
     */
    @Override
    public WritableImage pixelateImage(WritableImage image) {
        return null;
    }

    /**
     * Flips the image vertically.
     * <p>
     * For example:
     * [0,0,0] [0,0,0] [0,0,0] [0,0,0] [0,0,0]
     * [0,0,0] [5,5,5] [1,4,7] [5,5,5] [0,0,0]
     * [0,0,0] [1,2,3] [2,5,8] [1,2,3] [0,0,0]
     * [0,0,0] [4,4,4] [3,6,9] [4,4,4] [0,0,0]
     * [0,0,0] [0,0,0] [0,0,0] [0,0,0] [0,0,0]
     * <p>
     * is flipped to
     * <p>
     * [0,0,0] [0,0,0] [0,0,0] [0,0,0] [0,0,0]
     * [0,0,0] [4,4,4] [3,6,9] [4,4,4] [0,0,0]
     * [0,0,0] [1,2,3] [2,5,8] [1,2,3] [0,0,0]
     * [0,0,0] [5,5,5] [1,4,7] [5,5,5] [0,0,0]
     * [0,0,0] [0,0,0] [0,0,0] [0,0,0] [0,0,0]
     *
     * @param image - the image to be flipped, do not modify!
     * @return a new image that displays upside-down (but not rotated!)
     */
    @Override
    public WritableImage flipImage(WritableImage image) {
        Color[][] pixels = new Color[(int)image.getHeight()][(int)image.getWidth()];
        for(int i = 0; i < pixels.length; i++) {
            for(int j = 0; j < pixels[1].length; j++) {
                pixels[i][j] = image.getPixelReader().getColor(i,j);
            }
        }
        for(int i = 0; i < pixels.length/2; i++) {
            Color[] temp = pixels[i];
            pixels[i] = pixels[pixels.length-i];
            pixels[pixels.length-i] = temp;
        }
        for(int i = 0; i < pixels.length; i++) {
            for(int j = 0; j < pixels[1].length; j++) {
                int red = (int)(255-(pixels[i][j].getRed()*255));
                int green = (int)(255-(pixels[i][j].getGreen()*255));
                int blue = (int)(255-(pixels[i][j].getBlue()*255));
                image.getPixelWriter().setColor(i,j,Color.rgb(red,green,blue));
            }
        }
        return image;
    }

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages.
     * @throws Exception if something goes wrong
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        root = new Group( );
        scene = new Scene( root, width, height );

        List<Button> buttons = new ArrayList<>();
        buttons.add(new Button("Open"));
        buttons.add(new Button("Save"));
        buttons.add(new Button("Flip"));
        buttons.add(new Button("Invert"));
        buttons.add(new Button("Grayscale"));
        buttons.add(new Button("Pixelate"));

        for( int i = 0; i < buttons.size(); i++ )
            buttons.get(i).relocate((width/12)+(width/7)*i, height-50);

        root.getChildren().addAll(buttons);

        stage.setTitle("Image Manipulator-inator");
        stage.setScene(scene);
        stage.show();
    }
}
