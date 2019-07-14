package com.navigation.model

import com.jfoenix.controls.JFXButton
import com.navigation.base.BaseModel
import com.navigation.views.MainView
import javafx.geometry.Pos
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox

import static com.navigation.utils.ResourceBundle.loadImageResource

class PlayListItem extends BaseModel {
    //歌曲名称
    String song
    //歌曲id
    String id
    //作者
    String singer
    //专辑
    String album
    //专辑图片
    String albumPic
    //来源
    String source

    HBox actionBox = new HBox()

    private MainView context;
    private final JFXButton play = new JFXButton()
    private final JFXButton download = new JFXButton()

    PlayListItem(MainView context) {
        actionBox.alignment = Pos.CENTER
        actionBox.getChildren().addAll(play, download)
        this.context = context
        play.setTooltip(new Tooltip("点击播放"))
        download.setTooltip(new Tooltip("点击下载"))
        play.setGraphic(new ImageView(loadImageResource("images/play_list.png", 30, 30, true)))
        download.setGraphic(new ImageView(loadImageResource("images/download.png", 30, 30, true)))
        play.setOnAction { e ->
            context.play(this)
        }
        download.setOnAction { e ->
            context.download(id)
        }
    }
}
