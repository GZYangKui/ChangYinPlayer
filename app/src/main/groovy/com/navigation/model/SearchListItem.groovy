package com.navigation.model

import com.jfoenix.controls.JFXButton
import com.navigation.base.BaseModel
import com.navigation.views.SearchComponent
import io.vertx.core.json.JsonObject
import javafx.geometry.Pos
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import static com.navigation.config.Constant.*
import static com.navigation.utils.ResourceBundle.loadImageResource

class SearchListItem extends BaseModel {
    //歌曲名称
    String song
    //歌曲id
    String id
    //作者
    String singer
    //专辑
    String album
    //来源
    String source
    //url id
    String urlId
    //图片id
    String picId
    //歌词id
    String lyricId


    HBox actionBox = new HBox()

    private final JFXButton play = new JFXButton()
    private final JFXButton download = new JFXButton()

    SearchListItem(SearchComponent context) {
        actionBox.alignment = Pos.CENTER
        actionBox.getChildren().addAll(play, download)
        play.setTooltip(new Tooltip("点击播放"))
        download.setTooltip(new Tooltip("点击下载"))
        play.setGraphic(new ImageView(loadImageResource("images/play_list.png", 30, 30, true)))
        download.setGraphic(new ImageView(loadImageResource("images/download.png", 30, 30, true)))
        play.setOnAction { e ->
            context.play(this.toJson())
        }
    }

    @Override
    JsonObject toJson() {
        return new JsonObject().put(ID, id)
                .put(SOURCE, source)
                .put(URL_ID, urlId)
                .put(PIC_ID, picId)
                .put(LYRIC_ID, lyricId)
                .put(SONG, song)
                .put(ALBUM, album)
                .put(SINGER, singer)

    }
}
