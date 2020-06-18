package src.resource;

import src.operation.imageoperation.Filter;
import src.operation.imageoperation.Pen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;

public class ImageWindowController {

    @FXML
    private MenuItem openMenuItem;
    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private MenuItem undoMenuItem;
    @FXML
    private MenuItem redoMenuItem;
    @FXML
    private TabPane imageTabPane;
    @FXML
    private Button meanBlur;
    @FXML
    private Button gaussianBlur;
    @FXML
    private Button sharpen;
    @FXML
    private Button relief;
    @FXML
    private Button unsharpMasking;
    @FXML
    private Button negative;
    @FXML
    private Button grayScale;
    @FXML
    private Label sizeLabel;
    @FXML
    private Label zoomLabel;
    @FXML
    private ColorPicker penColorPicker;
    @FXML
    private Slider penSizeSlider;
    @FXML
    private TextField penSizeTextField;
    @FXML
    private Slider saturationSlider;
    @FXML
    private Slider brightnessSlider;
    @FXML
    private Slider hueSlider;
    @FXML
    private Slider contrastSlider;
    @FXML
    private Label positionLabel;
    @FXML
    private Label colorRGBLabel;
    @FXML
    private Label colorHSBLabel;
    @FXML
    private Rectangle colorBlock;
    @FXML
    private Label rightStatusLabel;
    @FXML
    private ProgressBar progressBar;

    @FXML
    void initialize() {
        penSizeTextField.textProperty().bindBidirectional(penSizeSlider.valueProperty(), new StringConverter<Number>() {
            @Override
            public String toString(Number t) {
                return String.format("%.0f", t);
            }

            @Override
            public Number fromString(String s) {
                return Integer.valueOf(s);
            }
        });
    }

    @FXML
    void saveMenuItemOnAction(ActionEvent event) {
        Tab currentTab = imageTabPane.getSelectionModel().getSelectedItem();
        Image currentImage = ((ImageView) ((ScrollPane) currentTab.getContent()).getContent()).getImage();
        File currentFile = new File(currentTab.getText());
        currentFile = new File(currentFile.getName().split("\\.")[0]);
        BufferedImage bImage = SwingFXUtils.fromFXImage(currentImage, null);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image File as");
        fileChooser.setInitialDirectory(new File("C:/"));
        fileChooser.setInitialFileName(currentFile.toString());

        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpg", "*.jpg"),
                new FileChooser.ExtensionFilter("png", "*.png"), new FileChooser.ExtensionFilter("bmp", "*.bmp"));

        File savePath = fileChooser.showSaveDialog(null);

        String fileExtension = fileChooser.selectedExtensionFilterProperty().get().getDescription();

        if (savePath != null) {
            try {
                ImageIO.write(bImage, fileExtension, savePath);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void addImage(File file) {
        Canvas imageView = new Canvas("file:" + file.getAbsolutePath());
        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);

        imageView.getPen().fillProperty().bind(penColorPicker.valueProperty());
        imageView.getPen().radiusProperty().bind(penSizeSlider.valueProperty());
        imageView.getPen().setVisible(true);

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.saturationProperty().bind(saturationSlider.valueProperty());
        colorAdjust.brightnessProperty().bind(brightnessSlider.valueProperty());
        colorAdjust.hueProperty().bind(hueSlider.valueProperty());
        colorAdjust.contrastProperty().bind(contrastSlider.valueProperty());

        saturationSlider.valueProperty().addListener((v, ov, nv) -> imageView.setEffect(colorAdjust));
        brightnessSlider.valueProperty().addListener((v, ov, nv) -> imageView.setEffect(colorAdjust));
        hueSlider.valueProperty().addListener((v, ov, nv) -> imageView.setEffect(colorAdjust));
        contrastSlider.valueProperty().addListener((v, ov, nv) -> imageView.setEffect(colorAdjust));

        imageView.setOnScroll(e -> {
            int deltaY = (int) e.getDeltaY();
            imageView.zoomFactor += deltaY * Canvas.ZOOM_RATE;
            imageView.zoomFactor = Math.max(0.01, Math.min(20.0, imageView.zoomFactor));
            imageView.setScaleX(imageView.zoomFactor);
            imageView.setScaleY(imageView.zoomFactor);
            this.zoomLabel.setText(String.valueOf((int) (imageView.zoomFactor * 100.)) + "% ");
        });

        imageView.setOnMouseEntered(e -> {
            imageView.getScene().setCursor(Cursor.CROSSHAIR);
        });

        imageView.setOnMouseMoved(e -> {
            this.positionLabel.setText("(" + (int) e.getX() + ", " + (int) e.getY() + ") ");

            Color color = imageView.getImage().getPixelReader().getColor((int) e.getX(), (int) e.getY());
            this.colorRGBLabel.setText("(" + (int) (color.getRed() * 256) + ", " + (int) (color.getGreen() * 256) + ", "
                    + (int) (color.getBlue() * 256) + ") ");
            this.colorHSBLabel.setText(
                    "(" + String.format("%.3f", color.getHue()) + ", " + String.format("%.3f", color.getSaturation())
                            + ", " + String.format("%.3f", color.getBrightness()) + ") ");
            this.colorBlock.setFill(color);

            imageView.getPen().setPosition(e.getX(), e.getY());
        });

        imageView.setOnMousePressed(e -> {
            imageView.mouseX = e.getX();
            imageView.mouseY = e.getY();
            imageView.penX = (int) e.getX();
            imageView.penY = (int) e.getY();
        });

        imageView.setOnMouseDragged(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                imageView.getScene().setCursor(Cursor.DISAPPEAR);
                Circle penCircle = new Circle();

                penCircle.radiusProperty().bind(penSizeSlider.valueProperty());
                penCircle.fillProperty().bind(penColorPicker.valueProperty());
                penCircle.setStroke(Color.BLACK);
                penCircle.setCenterX(e.getX());
                penCircle.setCenterY(e.getY());

                imageView.paint((int) e.getX(), (int) e.getY(), (Color) imageView.getPen().getFill());
            }
            if (e.getButton().equals(MouseButton.SECONDARY)) {
                imageView.getScene().setCursor(Cursor.MOVE);
                imageView.setTranslateX(imageView.getTranslateX() + e.getX() - imageView.mouseX);
                imageView.setTranslateY(imageView.getTranslateY() + e.getY() - imageView.mouseY);
            }
        });

        imageView.setOnMouseExited(e -> {
            imageView.getScene().setCursor(Cursor.DEFAULT);
            this.positionLabel.setText("(-, -)");
            this.colorRGBLabel.setText("(-, -, -)");
            this.colorHSBLabel.setText("(-, -, -)");
            this.colorBlock.setFill(Color.WHITE);
        });

        ScrollPane scrollPane = new ScrollPane(imageView);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Tab tab = new Tab(file.getName(), scrollPane);

        tab.setOnSelectionChanged(e -> {
            this.sizeLabel.setText(
                    (int) (imageView.getImage().getWidth()) + " x " + (int) (imageView.getImage().getHeight()));
            saturationSlider.valueProperty().unbind();
            brightnessSlider.valueProperty().unbind();
            hueSlider.valueProperty().unbind();
            contrastSlider.valueProperty().unbind();

            imageView.setEffect(new ColorAdjust(0, 0, 0, 0));
            saturationSlider.setValue(((ColorAdjust) imageView.getEffect()).getSaturation());
            brightnessSlider.setValue(((ColorAdjust) imageView.getEffect()).getBrightness());
            hueSlider.setValue(((ColorAdjust) imageView.getEffect()).getHue());
            contrastSlider.setValue(((ColorAdjust) imageView.getEffect()).getContrast());
        });

        this.imageTabPane.getTabs().add(tab);
    }

    @FXML
    void filterButtonOnAction(ActionEvent event) {
        Tab currentTab = imageTabPane.getSelectionModel().getSelectedItem();
        Canvas currentCanvas = (Canvas) ((ScrollPane) currentTab.getContent()).getContent();
        Image newImage = null;

        if (event.getSource() == meanBlur)
            newImage = Filter.computeFilter(currentCanvas.getImage(), Filter.MEAN_BLUR);
        else if (event.getSource() == gaussianBlur)
            newImage = Filter.computeFilter(currentCanvas.getImage(), Filter.GAUSSIAN_BLUR);
        else if (event.getSource() == sharpen)
            newImage = Filter.computeFilter(currentCanvas.getImage(), Filter.SHARPEN);
        else if (event.getSource() == relief)
            newImage = Filter.computeFilter(currentCanvas.getImage(), Filter.RELIEF);
        else if (event.getSource() == unsharpMasking)
            newImage = Filter.computeFilter(currentCanvas.getImage(), Filter.UNSHAPR_MASKING);
        else if (event.getSource() == negative)
            newImage = Filter.toNegative(currentCanvas.getImage());
        else if (event.getSource() == grayScale)
            newImage = Filter.toGrayScale(currentCanvas.getImage());

        while (currentCanvas.operationIter < currentCanvas.operations.size() - 1)
            currentCanvas.operations.remove(currentCanvas.operationIter + 1);

        currentCanvas.operations.add(newImage);
        currentCanvas.operationIter++;

        System.out.printf("add img, Operation.size() = %d, iter = %d\n", currentCanvas.operations.size(),
                currentCanvas.operationIter);
        currentCanvas.setImage(newImage);
    }

    @FXML
    void UndoMenuItemOnAction(ActionEvent event) {
        Tab currentTab = imageTabPane.getSelectionModel().getSelectedItem();
        Canvas currentCanvas = (Canvas) ((ScrollPane) currentTab.getContent()).getContent();

        if (currentCanvas.operationIter > 0)
            currentCanvas.setImage(currentCanvas.operations.get(--currentCanvas.operationIter));

        System.out.printf("undo, Operation.size() = %d, iter = %d\n", currentCanvas.operations.size(),
                currentCanvas.operationIter);
    }

    @FXML
    void RedoMenuItemOnAction(ActionEvent event) {
        Tab currentTab = imageTabPane.getSelectionModel().getSelectedItem();
        Canvas currentCanvas = (Canvas) ((ScrollPane) currentTab.getContent()).getContent();

        if (currentCanvas.operationIter < currentCanvas.operations.size() - 1)
            currentCanvas.setImage(currentCanvas.operations.get(++currentCanvas.operationIter));

        System.out.printf("redo, Operation.size() = %d, iter = %d\n", currentCanvas.operations.size(),
                currentCanvas.operationIter);
    }

    public void closeStage() {
        this.imageTabPane.getTabs().clear();
    }
}

class Canvas extends ImageView {
    static final double ZOOM_RATE = 0.01;

    public double mouseX, mouseY;
    public double zoomFactor = 1.0;

    private Pen pen = new Pen();
    public int penX, penY;

    public Image origImage;
    public Vector<Image> operations = new Vector<Image>();
    public int operationIter = 0;

    public Canvas(String filePath) {
        super(filePath);
        this.origImage = new Image(filePath);
        this.operations.add(this.origImage);
    }

    public Pen getPen() {
        return this.pen;
    }

    public void paint(int x, int y, Color color) {
        WritableImage writableImage = new WritableImage(getImage().getPixelReader(), (int) getImage().getWidth(),
                (int) getImage().getHeight());
        PixelWriter pw = writableImage.getPixelWriter();

        int radius = (int) this.pen.getRadius();
        for (int i = -radius; i < radius; i++)
            for (int j = -radius; j < radius; j++) {
                int paintX = x + i;
                int paintY = y + j;

                if (0 <= paintX && paintX < getImage().getWidth() && 0 <= paintY && paintY < getImage().getHeight()) {
                    if (Math.sqrt(i * i + j * j) < radius)
                        pw.setColor(paintX, paintY, color);
                }
            }

        setImage(writableImage);
    }
}
