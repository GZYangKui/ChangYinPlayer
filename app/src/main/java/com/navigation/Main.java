package com.navigation;


import com.navigation.utils.DBUtils;
import com.navigation.utils.Message;
import io.vertx.core.Vertx;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


import java.util.Objects;


public class Main extends Application {

    @Override
    public void init() throws Exception {
        final Vertx vertx = Vertx.vertx();
        vertx.fileSystem().readFile("config/config.json", rs -> {
            if (rs.succeeded()) {
                DBUtils.init(vertx, rs.result().toJsonObject().getJsonObject("dataSource"));
            } else {
                Message.showError("加载配置文件失败", rs.cause());
            }
        });
    }

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
