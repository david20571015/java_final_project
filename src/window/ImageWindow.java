package src.window;

import src.controller.ImageWindowController;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import javafx.stage.Stage;

public class ImageWindow extends Stage {
    public ImageWindowController imageWindowController;

    public ImageWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("..\\resource\\ImageWindow.fxml"));

        try {
            Scene scene = new Scene(loader.load());
            this.setScene(scene);
        } catch (IOException e) {
            System.out.println("ImageWindow.fxml exception");
        }

        this.imageWindowController = loader.getController();
        setOnCloseRequest(e -> this.imageWindowController.closeStage());
    }

    public ImageWindowController getController() {
        return this.imageWindowController;
    }
}