package com.navigation.views

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXRadioButton
import com.jfoenix.controls.JFXTextField
import com.navigation.base.BaseView
import com.navigation.controller.MainController
import com.navigation.enums.NotificationType
import com.navigation.model.PlayListItem
import com.navigation.model.SearchListItem
import com.navigation.model.SearchModel
import com.navigation.utils.Message
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.geometry.Pos
import javafx.scene.control.ScrollBar
import javafx.scene.control.ScrollToEvent
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.ToggleGroup
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.VBox

import static com.navigation.config.Constant.ACTION
import static com.navigation.config.Constant.DATA
import static com.navigation.config.Constant.PLAY
import static com.navigation.config.Constant.SOURCE
import static com.navigation.service.HttpService.get

/**
 *
 * 搜索结果展示面板
 *
 */
class SearchView extends BaseView {
    private JFXTextField searchBox
    private JFXButton searchAction
    private ToggleGroup searchGroup
    private SearchModel searchParam
    private javafx.collections.ObservableList<SearchListItem> list = FXCollections.observableArrayList()
    private TableView<PlayListItem> playingList
    private TableColumn song
    private TableColumn singer
    private TableColumn album
    private TableColumn action
    private Vertx vertx

    SearchView(MainController context) {
        super(context, "fxml/search_view.fxml")
        initView()
        vertx = context.vertx
    }

    private def initView() {
        final VBox topAction = getContainer().lookup("#topBox")
        searchBox = topAction.lookup("#searchBox")
        searchAction = topAction.lookup("#searchAction")
        searchGroup = topAction.lookup("#searchGroup")
        searchGroup = (topAction.lookup("#netease") as JFXRadioButton).toggleGroup
        searchBox.prefWidthProperty().bind(topAction.widthProperty() * 0.6)
        playingList = getContainer().lookup("#playingList")
        playingList.columns.eachWithIndex { TableColumn entry, int i ->
            if (i === 0) {
                song = entry
                song.prefWidthProperty().bind(playingList.widthProperty() * 0.394)
            } else if (i === 1) {
                singer = entry
                singer.prefWidthProperty().bind(playingList.widthProperty() * 0.2)
            } else if (i == 2) {
                album = entry
                album.prefWidthProperty().bind(playingList.widthProperty() * 0.2)
            } else {
                action = entry
                action.prefWidthProperty().bind(playingList.widthProperty() * 0.2)
            }
        }
        song.cellValueFactory = new PropertyValueFactory("song")
        singer.cellValueFactory = new PropertyValueFactory("singer")
        album.cellValueFactory = new PropertyValueFactory("album")
        action.cellValueFactory = new PropertyValueFactory("actionBox")
        playingList.setItems(list)
        searchAction.setOnAction {
            if (searchBox.text.trim() == "") {
                Message.showNotification("搜索关键字不能为空", Pos.CENTER, NotificationType.INFO)
                return
            }
            def selected = searchGroup.selectedToggle
            if (selected == null) {
                Message.showNotification("请选择搜索源", Pos.CENTER, NotificationType.INFO)
                return
            }
            list.clear()
            searchParam = new SearchModel()
            searchParam.name = searchBox.text
            searchParam.source = selected.userData
            search()
        }
        playingList.setSortPolicy {
            return null
        }
        playingList.setOnMouseEntered() {
            if (playingList.userData == null) {
                playingList.userData = true
                final ScrollBar _scrollBar = playingList.lookup(".scroll-bar:vertical")
                _scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        if (newValue.doubleValue() >= _scrollBar.getMax()*0.8) {
                            search()
                        }
                    }
                })
            }
        }

    }

    private search() {
        if (searchParam.isEmpty) {
            return
        }
        def url = "http://www.gequdaquan.net/gqss/api.php"
        get(context.getVertx(), url, searchParam.toJson()).setHandler {
            if (!it.succeeded()) {
                exception("搜索失败", it.cause())
                return
            }
            def data = it.result().bodyAsJsonArray()
            if (data.size() == 0) {
                searchParam.isEmpty = true
                return
            }

            searchParam.page++

            data.forEach {
                def obj = it as JsonObject
                def item = new SearchListItem(this)
                item.song = obj.getString("name")
                item.id = formatTransform(obj, "id")
                item.singer = obj.getJsonArray("artist")[0]
                item.album = obj.getString("album")
                item.urlId = formatTransform(obj, "url_id")
                item.picId = formatTransform(obj, "pic_id")
                item.source = obj.getString("source")
                item.lyricId = formatTransform(obj, "lyric_id")
                Platform.runLater {
                    list.add(item)
                }
            }

        }
    }

    def play(JsonObject info) {
        def message = new JsonObject()
        message.put(ACTION, PLAY)
        message.put(DATA, info)
        def url = "http://www.gequdaquan.net/gqss/api.php"
        def param = new JsonObject()
        param.put("types", "pic")
        param.put("id", info.getString("picId"))
        param.put(SOURCE, info.getString(SOURCE))
        get(context.vertx, url, param).setHandler {
            if (it.succeeded()) {
                info.put("albumPic", it.result().bodyAsJsonObject().getString("url"))
                vertx.eventBus().send("media_player", message)
            }
        }
    }

    private def formatTransform(JsonObject obj, String field) {
        return obj.getValue(field).toString()
    }

}
