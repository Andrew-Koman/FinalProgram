import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

public class ImageManipulator extends Application implements ImageManipulatorInterface{
    private Stage primaryStage = null;
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
        Integer width = null, height = null, colorSpace = null;
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
                double intensity = pixels[i][j].getRed()*.2989 + pixels[i][j].getBlue()*.1140 + pixels[i][j].getGreen()*.5870;
                image.getPixelWriter().setColor(i,j,Color.color(intensity,intensity,intensity));
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
        this.primaryStage = primaryStage;
        root = new Group( );
        scene = new Scene( root, width, height );

        List<String> buttonNames = Arrays.asList("Open", "Save", "Flip", "Invert", "Grayscale", "Pixelate");

        Map<String, Button> buttons = new HashMap<>();
        for( int i = 0; i < buttonNames.size(); i++ ) {
            Button button = new Button( buttonNames.get(i) );
            button.relocate((width / 12) + (width / 7) * i, height - 50);
            buttons.put( buttonNames.get(i), button );
        }
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter fileFilter = new FileChooser.ExtensionFilter("Portable Pixel Map", "*.ppm");
        fileChooser.getExtensionFilters().add(fileFilter);

        WritableImage image;
        buttons.get("Open").setOnAction( (ActionEvent event ) -> {
            File file = fileChooser.showOpenDialog(ImageManipulator.this.primaryStage);
            if( file != null ) {
                try {
                    loadImage(file.getAbsolutePath());
                } catch( FileNotFoundException e ){
                    fileNotFoundPopup( file.getName() );
                }
            }
        });

        buttons.get("Save").setOnAction( event -> {
            File file = fileChooser.showSaveDialog(this.primaryStage);
            if (file != null )
                try {
                    saveImage(file.getAbsolutePath(), null);
                } catch ( FileNotFoundException e ){
                    fileNotFoundPopup( file.getName() );
                }
        });

        root.getChildren().addAll(buttons.values());
        this.primaryStage.setTitle("Image Manipulator-inator");
        this.primaryStage.setScene(scene);
        this.primaryStage.show();
    }

    private void fileNotFoundPopup(String fileName){
        Stage dialog = new Stage();
        dialog.setTitle("File Not Found");
        dialog.initOwner(primaryStage);
        dialog.setResizable(false);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.BASELINE_CENTER);

        Label label = new Label("File Not Found.");
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);

        Label fileLabel = new Label(fileName);
        fileLabel.setTextAlignment(TextAlignment.CENTER);
        fileLabel.setAlignment(Pos.CENTER);

        Button okButton = new Button("OK");
        okButton.setOnAction( event -> {
            dialog.close();
        });

        vBox.getChildren().addAll(label, fileLabel, okButton);
        Scene dialogScene = new Scene(vBox, 200, 100);
        dialog.setScene(dialogScene);
        dialog.show();
    }
}
