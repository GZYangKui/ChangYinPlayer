package com.navigation;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.RuntimeUtil;

import java.util.Objects;

import static uk.co.caprica.vlcj.binding.RuntimeUtil.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        final Parent root = FXMLLoader.load(ClassLoader.getSystemResource("fxml/main.fxml"));
        final Scene scene = new Scene(root);

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("images/icon.png"))));
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("畅音播放器");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
