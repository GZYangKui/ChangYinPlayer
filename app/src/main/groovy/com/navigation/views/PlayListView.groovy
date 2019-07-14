package com.navigation.views

import com.jfoenix.controls.JFXMasonryPane
import com.navigation.base.BaseView
import com.navigation.component.PlayListItem
import com.navigation.controller.MainController
import io.vertx.core.json.JsonObject
import javafx.application.Platform
import javafx.scene.layout.VBox
import static com.navigation.config.Constant.*

import static com.navigation.service.HttpService.getCategoryList

class PlayListView extends BaseView {
    private MainView content
    private JFXMasonryPane masonryPane
    private List<JsonObject> list = new ArrayList<>()
    private boolean isInitList = false

    PlayListView(MainController context) {
        super(context, "fxml/play_list_view.fxml")
        initView()
    }

    def initView() {
        masonryPane = getContainer().lookup("#masonryPane")
        masonryPane.setHSpacing(20)
        masonryPane.setVSpacing(20)
    }

    def initList(JsonObject _tt) {
        def item = new PlayListItem(_tt.getString("coverImgUrl"), _tt.getString("name")).getItem()
        item.setUserData(_tt.getLong("id"))
        Platform.runLater {
            masonryPane.getChildren().add(item)
        }
        if (!isInitList) {
            updateList(list[0])
            isInitList = true
        }
        item.setOnMouseClicked {
            def source = it.source as VBox
            def result = list.stream().filter {
                it.getJsonObject("playlist").getLong("id") == source.userData
            }.findAny()
            if (result.present) {
                updateList(result.get())
            }

        }

    }

    private def updateList(JsonObject data) {
        def message = new JsonObject()
        message.put(ACTION, UPDATE_LIST)
        message.put(DATA, data)
        context.vertx.eventBus().send("media_player", message)

    }

    @Override
    def init() {
        context.vertx.fileSystem().readFile("config/config.json") {
            if (!it.succeeded()) {
                exception("加载配置信息失败", it.cause())
            }
            def temp = it.result().toJsonObject().getJsonArray("playList")
            temp.forEach { JsonObject _item ->
                getCategoryList(context.vertx, _item.getLong("id")).setHandler {
                    list.add(it.result())
                    initList(it.result().getJsonObject("playlist"))
                }
            }
        }
    }
}
