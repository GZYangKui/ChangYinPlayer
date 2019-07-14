package com.navigation.controller;


import com.jfoenix.controls.JFXSlider;
import com.navigation.base.BaseView;
import com.navigation.views.MainView;
import com.navigation.enums.NotificationType;
import com.navigation.events.MediaEvent;
import com.navigation.listener.DragListener;
import io.vertx.core.Vertx;
import javafx.application.Platform;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.State;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;

import static com.navigation.utils.Message.showNotification;


public class MainController implements Initializable {
    @FXML
    private HBox topActionBox;
    @FXML
    private HBox topActionBox1;
    @FXML
    private HBox topActionBox2;
    @FXML
    private HBox bottomBox;
    @FXML
    private HBox bottomBox1;
    @FXML
    private HBox bottomBox2;
    @FXML
    private HBox bottomBox3;
    //音乐播放进度
    @FXML
    private JFXSlider progress;
    //音量大小
    @FXML
    private JFXSlider volume;
    @FXML
    private VBox content;
    @FXML
    private BorderPane container;
    @FXML
    private Button lastSong;
    @FXML
    private Button nextSong;
    @FXML
    private Button play;
    @FXML
    private Label startTime;
    @FXML
    private Label endTime;
    @FXML
    private Button exit;

    private final MediaPlayerFactory mediaPlayerFactory;

    private final MediaEvent mediaEvent;

    //判断鼠标是否在拖动中
    private boolean isDrag = false;

    private Vertx vertx = Vertx.vertx();

    private final EmbeddedMediaPlayer mediaPlayer;

    //页面内容面板路由
    private final Stack<BaseView> router = new Stack<>();

    public MainController() {
        mediaPlayerFactory = new MediaPlayerFactory();
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        mediaPlayer.controls().setRepeat(true);
        mediaEvent = new MediaEvent(this, mediaPlayer);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initView();
    }

    private void initView() {
        topActionBox1.prefWidthProperty().bind(topActionBox.widthProperty().multiply(0.5));
        topActionBox2.prefWidthProperty().bind(topActionBox.widthProperty().multiply(0.5));
        bottomBox1.prefWidthProperty().bind(bottomBox.widthProperty().multiply(0.2));
        bottomBox2.prefWidthProperty().bind(bottomBox.widthProperty().multiply(0.6));
        bottomBox3.prefWidthProperty().bind(bottomBox.widthProperty().multiply(0.2));
        progress.prefWidthProperty().bind(bottomBox2.widthProperty().multiply(0.85));
        volume.prefWidthProperty().bind(bottomBox3.widthProperty().multiply(0.8));
        flushContent(new MainView(this));

        //监听音量改变
        volume.valueProperty().addListener((obs, newValue, oldValue) -> {
            mediaPlayer.audio().setVolume(newValue.intValue());
        });

        progress.addEventHandler(EventType.ROOT, e -> {
            if (e.getEventType().getName().equals("MOUSE_PRESSED")) {
                isDrag = true;
            }
            if (e.getEventType().getName().equals("MOUSE_RELEASED")) {
                isDrag = false;
            }
            if (e.getEventType().getName().equals("MOUSE_CLICKED")) {
                var percentage = new BigDecimal(progress.getValue() + "").divide(new BigDecimal(progress.getMax() + ""), 2, RoundingMode.UP).floatValue();
                mediaPlayer.controls().setPosition(percentage);
            }
        });

        exit.setOnAction(e -> this.exit());
        play.setOnAction(e -> {
            if (mediaPlayer.media().info().state() == State.PLAYING) {
                updatePlayIcon(false);
                mediaPlayer.controls().pause();
            }
            if (mediaPlayer.media().info().state() == State.PAUSED) {
                updatePlayIcon(true);
                mediaPlayer.controls().play();
            }
        });
        lastSong.setOnAction(e -> ((MainView) router.get(0)).control(-1));
        nextSong.setOnAction(e -> ((MainView) router.get(0)).control(1));
        topActionBox.setOnMouseEntered(e -> {
            if (topActionBox.getUserData() == null) {
                new DragListener((Stage) container.getScene().getWindow()).enableDrag(topActionBox);
                topActionBox.setUserData(true);
            }
        });
    }

    //更换内容面板
    private void flushContent(BaseView content) {

        var list = this.content.getChildren();

        if (!list.isEmpty()) {
            var temp = (BorderPane) list.get(0);
            if (content.getContainer() == temp) {
                return;
            }
            content.remove();
            temp.prefWidthProperty().unbind();
            temp.prefHeightProperty().unbind();
        }
        content.init();
        content.getContainer().prefWidthProperty().bind(this.content.widthProperty());
        content.getContainer().prefHeightProperty().bind(this.content.heightProperty());
        this.content.getChildren().add(content.getContainer());
        router.push(content);
    }

    public void play(String url) {
        if (url == null || url.trim().equals("")) {
            showNotification("播放失败", Pos.CENTER, NotificationType.ERROR);
            return;
        }
        CompletableFuture.runAsync(() -> {
            mediaPlayer.media().start(url);
        });
    }

    //更新播放图标
    public void updatePlayIcon(boolean status) {
        final Image image;
        if (status) {
            image = new Image(ClassLoader.getSystemResourceAsStream("images/play.png"), 25, 25, false, true);
        } else {
            image = new Image(ClassLoader.getSystemResourceAsStream("images/pause.png"), 25, 25, false, true);
        }
        Platform.runLater(() -> {
            play.setGraphic(new ImageView(image));
        });
    }

    /**
     * 更新进度条
     */
    public void updateProgress(long current) {

        if (!isDrag) {
            final String progress = formatTime(current);
            Platform.runLater(() -> {
                this.progress.setValue(current);
                startTime.setText(progress);
            });
            ((MainView) router.get(0)).updateProgress(Long.parseLong(progress.split(":")[0]) * 60 + Long.parseLong(progress.split(":")[1]));
        }

    }

    /**
     * 初始化进度条
     *
     * @param max 进度条最大值
     */
    public void initProgress(long max) {
        Platform.runLater(() -> {
            progress.setMax(max);
            progress.setValue(0);
            endTime.setText(formatTime(max));
        });
    }

    //获取vertx实例
    public Vertx getVertx() {
        return vertx;
    }

    /**
     * 格式化时间
     *
     * @return 返回 mm:ss
     */
    private String formatTime(long mill) {
        var second = mill % (60 * 1000) / 1000;
        var minutes = mill % (60 * 60 * 1000) / (60 * 1000);
        return (minutes > 9 ? minutes : "0" + minutes) + ":" + (second > 9 ? second : "0" + second);
    }

    //窗口最大化
    private @FXML
    void maximization() {
        final var window = container.getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).setMaximized(true);
        }
    }

    //窗口最小化
    private @FXML
    void minimize() {
        final var window = container.getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).setIconified(true);
        }
    }

    /**
     * 退出应用
     */
    private void exit() {
        vertx.close();
        mediaPlayer.release();
        Platform.exit();
        System.exit(0);
    }
}
