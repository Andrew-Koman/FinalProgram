import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Andrew Koman
 * @author Ben Boelens
 * @author Ethan Schultz
 *
 * ASSIGNMNET: Final Project - Image Manipulator-inator
 * COURSE: CS 1122
 * LAB SECTION: L03
 *
 * DESCRIPTION:
 * A GUI that manipulates image files in the Portable Pixel Map (PPM) format
 * Modifications are: Flip Image, Invert Colors, Grayscale, and Pixelate
 *
 */
public class ImageManipulator extends Application implements ImageManipulatorInterface{
    private Stage primaryStage = null;
    private double width = 640,
                   height = 480;
    /**
     * Load the specified PPM image file.
     * The image file must be in the PPM P3 format
     *
     * @param filename File name to be loaded
     * @return loaded WritableImage from filename
     * @throws FileNotFoundException If filename is not found, throw exception
     * @link http://netpbm.sourceforge.net/doc/ppm.html
     * <p>
     * Don't forget to add a load button to the application!
     */
    @Override
    public WritableImage loadImage(String filename) throws FileNotFoundException {
        File imageFile = new File(filename);
        int width = -1, height = -1, colorSpace = -1;

        Scanner imageScanner = new Scanner(imageFile);
        if (!imageScanner.nextLine().equals("P3")) {
            infoPopup(new String[] {"File Error", "File is corrupt", "or not a PPM"});
            imageScanner.close();
            return null;
        }
        while(width == -1 || height == -1 || colorSpace == -1) {
            String next = imageScanner.nextLine();
            if(next.charAt(0) == '#')
                continue;

            Scanner lineScanner = new Scanner(next);
            if(width == -1 && height == -1) {
                width = lineScanner.nextInt();
                height = lineScanner.nextInt();
            } else {
                colorSpace = lineScanner.nextInt();
            }
        }
//        System.out.printf("Width: %d\tHeight: %d\tColor Space: %d%n", width, height, colorSpace);
        WritableImage image = new WritableImage(width,height);
        try {
            for(int i = 0; i < height; i++) {
                for(int j = 0; j < width; j++) {
                    int red = imageScanner.nextInt()*(255/colorSpace);
                    int green = imageScanner.nextInt()*(255/colorSpace);
                    int blue = imageScanner.nextInt()*(255/colorSpace);
                    image.getPixelWriter().setColor(j,i,Color.rgb(red,green,blue));
                }
            }
        } catch (InputMismatchException exception) {
            imageScanner.nextLine();
        }
        imageScanner.close();
        return image;
    }

    /**
     * Save the specified image to a PPM file.
     * The image file must be in the PPM P3 format
     *
     * @param filename Name of image file to be saved
     * @param image WritableImage to be saved
     * @throws FileNotFoundException If filename is not found, then throw exception
     * @link http://netpbm.sourceforge.net/doc/ppm.html
     * <p>
     * Don't forget to add a save button to the application!
     */
    @Override
    public void saveImage(String filename, WritableImage image) throws FileNotFoundException {
        File outFile = new File(filename);
        int width = (int)image.getWidth(), height = (int)image.getHeight(), colorSpace = 255;
        PrintWriter fileWriter = new PrintWriter( outFile );
        fileWriter.println("P3");
        fileWriter.println("# CREATOR: CS1122 ImageManipulator-inator");

        fileWriter.printf("%d %d%n", width, height);
        fileWriter.println(colorSpace);

        PixelReader pixelReader = image.getPixelReader();

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                Color pixelColor = pixelReader.getColor(j, i);
                int r = (int)(pixelColor.getRed()*colorSpace);
                int g = (int)(pixelColor.getGreen()*colorSpace);
                int b = (int)(pixelColor.getBlue()*colorSpace);

                fileWriter.printf("%d %d %d%n", r, g, b );
            }
        }
        fileWriter.close();
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
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage( (int)image.getWidth(), (int)image.getHeight() );
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color pixelColor = pixelReader.getColor(x, y);
                int red = 255-(int)(pixelColor.getRed()*255);
                int green = 255-(int)(pixelColor.getGreen()*255);
                int blue = 255-(int)(pixelColor.getBlue()*255);
                pixelWriter.setColor( x, y, Color.rgb( red, green, blue ) );
            }
        }
        return newImage;
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
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage( (int)image.getWidth(), (int)image.getHeight() );
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color pixelColor = pixelReader.getColor(x, y);
                int pixelIntensity = (int)(255*(pixelColor.getRed() * 0.2989
                                        + pixelColor.getGreen() * 0.5870
                                        + pixelColor.getBlue() * 0.1140));
                pixelWriter.setColor( x, y, Color.rgb(pixelIntensity, pixelIntensity, pixelIntensity) );
            }
        }
        return newImage;
    }

    /**
     * Pixelates the image by dividing it into 5x5 regions, then assigning
     * all pixels in the region the same color as the central pixel.
     * <p>
     * For example:
     * [0,0,0] [0,0,0] [0,0,0] [0,0,0] [0,0,0]     * [0,0,0] [0,0,0] [0,0,0] [0,0,0] [0,0,0]
     * [0,0,0] [5,5,5] [5,5,5] [5,5,5] [0,0,0]     * [0,0,0] [5,5,5] [5,5,5] [5,5,5] [0,0,0]
     * [0,0,0] [5,5,5] [1,2,3] [5,5,5] [0,0,0]     * [0,0,0] [5,5,5] [1,2,3] [5,5,5] [0,0,0]
     * [0,0,0] [5,5,5] [5,5,5] [5,5,5] [0,0,0]     * [0,0,0] [5,5,5] [5,5,5] [5,5,5] [0,0,0]
     * [0,0,0] [0,0,0] [0,0,0] [0,0,0] [0,0,0]     * [0,0,0] [0,0,0] [0,0,0] [0,0,0] [0,0,0]
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
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage( (int)image.getWidth(), (int)image.getHeight() );
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 2; y <= image.getHeight() - 2; y += 5) {
            for (int x = 2; x <= image.getWidth() - 2; x += 5) {
                Color pixelColor = pixelReader.getColor(x, y);
                for (int y2 = y - 2; y2 < y + 3; y2++) {
                    for (int x2 = x - 2; x2 < x + 3; x2++) {
                        try {   //Try and write pixels, even if they are out of the dimensions of the image
                            pixelWriter.setColor(x2, y2, pixelColor);
                        } catch (IndexOutOfBoundsException ignored){ }
                    }
                }
            }
        }
        return newImage;
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
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage( (int)image.getWidth(), (int)image.getHeight() );
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for( int y = 0; y < image.getHeight(); y++ ){
            for( int x = 0; x < image.getWidth(); x++ ){
                pixelWriter.setColor( x, y, pixelReader.getColor( x, (int)(image.getHeight() - 1 - y) ) );
            }
        }
        return newImage;
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
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        BorderPane root = new BorderPane();
        HBox buttonBox = new HBox();
        Scene scene = new Scene(root, width, height);

        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(480);

        //Create Buttons
        List<String> buttonNames = Arrays.asList("Open", "Save", "Flip", "Invert", "Grayscale", "Pixelate");
        Map<String, Button> buttons = new HashMap<>();
        for (String buttonName : buttonNames) {
            Button button = new Button(buttonName);
            buttons.put(buttonName, button);
            buttonBox.getChildren().add( button );
        }
        buttonBox.setAlignment(Pos.BASELINE_CENTER);
        buttonBox.setSpacing(20);
        buttonBox.setPadding( new Insets(10) );

        //Create Image Label
        Label imageLabel = new Label();
        ImageView view = new ImageView();

        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setFitHeight(height-100);


        imageLabel.setGraphic( view );
        root.setCenter( imageLabel );

        //Disable iamge modifier buttons because no image is loaded
        buttons.get("Save").setDisable(true);
        buttons.get("Flip").setDisable(true);
        buttons.get("Invert").setDisable(true);
        buttons.get("Grayscale").setDisable(true);
        buttons.get("Pixelate").setDisable(true);

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter fileFilter = new FileChooser.ExtensionFilter("Portable Pixel Map", "*.ppm");
        fileChooser.getExtensionFilters().add(fileFilter);
        fileChooser.setInitialDirectory(new File("./"));
        //Event handlers for buttons

        //On open, open open-dialog
        buttons.get("Open").setOnAction( (ActionEvent event ) -> {
            File file = fileChooser.showOpenDialog(ImageManipulator.this.primaryStage);
            if( file != null ) {
                try {
                    view.setImage( loadImage(file.getAbsolutePath()) );
                } catch( FileNotFoundException e ){
                    infoPopup( new String[] {"File not found.", file.getName() } );
                } finally {
                    if( view.getImage() != null ) {
                        //Set width and height
                        width = view.getImage().getWidth();
                        height = view.getImage().getHeight();
                        //Re-enable Image Modifiers once image is loaded
                        buttons.get("Save").setDisable(false);
                        buttons.get("Flip").setDisable(false);
                        buttons.get("Invert").setDisable(false);
                        buttons.get("Grayscale").setDisable(false);
                        buttons.get("Pixelate").setDisable(false);
                    }
                }
            }
        });

        //On save, open save-dialog
        buttons.get("Save").setOnAction( event -> {
            File file = fileChooser.showSaveDialog(this.primaryStage);
            if (file != null )
                try {
                    saveImage(file.getAbsolutePath(), (WritableImage) view.getImage());
                } catch ( FileNotFoundException e ){
                    infoPopup( new String[] {"File not found.", file.getName() } );
                } finally {
                    savePopup( file );
                }
        });

        //On flip, call flipImage
        buttons.get("Flip").setOnAction( event -> view.setImage( flipImage( (WritableImage) view.getImage() ) ));

        //On Invert, call invertImage
        buttons.get("Invert").setOnAction( event -> view.setImage( invertImage( (WritableImage) view.getImage() ) ));

        //On Grayscale, call grayifyImage
        buttons.get("Grayscale").setOnAction( event -> view.setImage( grayifyImage( (WritableImage) view.getImage() ) ));

        //On Pixelate, call pixelateImage
        buttons.get("Pixelate").setOnAction( event -> view.setImage( pixelateImage( (WritableImage) view.getImage() ) ));

        //On window height resize, scale image to new height
        primaryStage.heightProperty().addListener((observable, oldHeight, newHeight) -> {
            height = newHeight.doubleValue();
            view.setFitHeight(height-100);
        });

        primaryStage.widthProperty().addListener((observable, oldWidth, newWidth) -> {
            width = newWidth.doubleValue();
            view.setFitWidth(width-100);
        });

        root.setBottom(buttonBox);
        this.primaryStage.setTitle("Image Manipulator-inator");
        this.primaryStage.setScene(scene);
        this.primaryStage.show();
    }

    private void infoPopup( String[] messages ){
        Stage popup = new Stage();
        popup.setTitle(messages[0]);
        popup.initOwner(primaryStage);
        popup.setResizable(false);
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.BASELINE_CENTER);

        for( String message : messages ){
            Text text = new Text(message);
            text.setTextAlignment( TextAlignment.CENTER );
            vBox.getChildren().add(text);
        }

        Button okButton = new Button("OK");
        okButton.setOnAction( event -> popup.close());
        VBox okBox = new VBox();
        okBox.setAlignment(Pos.BASELINE_CENTER);
        okBox.setPadding( new Insets(5) );
        okBox.getChildren().add( okButton );

        vBox.getChildren().add(okBox);

        Scene dialogScene = new Scene(vBox, 200, 100);
        popup.setScene(dialogScene);
        popup.show();
    }

    private void savePopup( File file ){
        Stage popup = new Stage();
        popup.setTitle("File Saved");
        popup.initOwner(primaryStage);
        popup.setResizable(false);
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.BASELINE_CENTER);

        Text savedText = new Text("File Saved" );
        savedText.setTextAlignment(TextAlignment.CENTER);
        savedText.setStyle("-fx-font-size: 14; -fx-font-weight: bolder");

        Hyperlink fileLink = new Hyperlink(file.getName());
        fileLink.setAlignment(Pos.BASELINE_CENTER);
        fileLink.setTooltip( new Tooltip("Open File Location") );
        fileLink.setBorder( null );
        fileLink.setOnAction( event -> {
            try {
                Desktop.getDesktop().browse(file.getParentFile().toURI());
            } catch (IOException ignored) { }
        });

        Button okButton = new Button("OK");
        okButton.setOnAction( event -> popup.close());

        Button openButton = new Button("Open File");
        openButton.setOnAction( event -> {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ignored) {}
        });
        VBox okBox = new VBox();
        okBox.setAlignment(Pos.BASELINE_CENTER);
        okBox.setPadding( new Insets(5) );
        okBox.getChildren().add( okButton );

        VBox openBox = new VBox();
        openBox.setAlignment(Pos.BASELINE_CENTER);
        openBox.setPadding( new Insets(5) );
        openBox.getChildren().add( openButton );


        vBox.getChildren().addAll( savedText, fileLink, openBox, okBox );

        Scene dialogScene = new Scene(vBox, 200, 125);
        popup.setScene(dialogScene);
        popup.show();
    }
}
