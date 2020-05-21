package src.resource;

import java.io.File;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ImageWindowController {

    @FXML
    private MenuItem openMenuItem;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private TabPane imageTabPane;

    @FXML
    private Label sizeLabel;

    @FXML
    private Label zoomLabel;

    @FXML
    private Label positionLabel;

    @FXML
    private Label colorLabel;

    @FXML
    private Rectangle colorBlock;

    @FXML
    private Label rightStatusLabel;

    @FXML
    private ProgressBar progressBar;

    public void addImage(File file) {
        Canvas imageView = new Canvas("file:" + file.getAbsolutePath());
        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);

        imageView.setOnScroll(e -> {
            int deltaY = (int) e.getDeltaY();
            imageView.zoomFactor += deltaY * Canvas.ZOOM_RATE;
            imageView.zoomFactor = Math.max(0.01, Math.min(20.0, imageView.zoomFactor));
            // System.out.println(deltaY + " " + imageView.zoomFactor);
            imageView.setScaleX(imageView.zoomFactor);
            imageView.setScaleY(imageView.zoomFactor);
            this.zoomLabel.setText(String.valueOf((int) (imageView.zoomFactor * 100)) + "% ");
        });

        imageView.setOnMouseMoved(e -> {
            int xPos = (int) (e.getX() / imageView.zoomFactor);
            int yPos = (int) (e.getY() / imageView.zoomFactor);
            this.positionLabel.setText("(" + xPos + ", " + yPos + ") ");

            Color color = imageView.getImage().getPixelReader().getColor(xPos, yPos);
            this.colorLabel.setText("(" + (int) (color.getRed() * 256) + ", " + (int) (color.getGreen() * 256) + ", "
                    + (int) (color.getBlue() * 256) + ") ");
            this.colorBlock.setFill(color);
        });

        imageView.setOnMouseDragged(e -> {
            imageView.setTranslateX(imageView.getTranslateX() + e.getX());
            imageView.setTranslateY(imageView.getTranslateY() + e.getY());
            System.out.println("e.getX() x:" + (int) e.getX() + " y:" + (int) e.getY() + " imageView.getLayoutX() x:"
                    + imageView.getLayoutX() + " y:" + imageView.getLayoutY() + " imageView.getX() x:"
                    + imageView.getX() + " y:" + imageView.getY() + " imageView.getTranslateX() x:"
                    + imageView.getTranslateX() + " y:" + imageView.getTranslateY());
        });

        imageView.setOnMouseExited(e -> {
            this.positionLabel.setText("(-, -)");
            this.colorLabel.setText("(-, -, -)");
            this.colorBlock.setFill(Color.WHITE);
        });

        ScrollPane scrollPane = new ScrollPane(imageView);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Tab tab = new Tab(file.getName(), new VBox(scrollPane));

        tab.setOnSelectionChanged(e -> {
            this.sizeLabel.setText(
                    (int) (imageView.getImage().getWidth()) + " x " + (int) (imageView.getImage().getHeight()));
        });

        this.imageTabPane.getTabs().add(tab);
    }

    public void closeStage() {
        this.imageTabPane.getTabs().clear();
    }
}

class Canvas extends ImageView {
    static final double ZOOM_RATE = 0.01;

    public double zoomFactor = 1.0;

    public Canvas(String filePath) {
        super(filePath);
    }
}