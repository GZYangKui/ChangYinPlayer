package com.navigation.views


import com.jfoenix.controls.JFXListView
import com.navigation.base.BaseContent
import com.navigation.controller.MainController
import com.navigation.enums.NotificationType
import com.navigation.model.PlayListItem

import io.vertx.core.json.JsonObject
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.SingleSelectionModel
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

import static com.navigation.service.HttpService.getMusicLyric
import static com.navigation.service.HttpService.getMusicUrl
import static com.navigation.utils.Message.showNotification
import static com.navigation.utils.ResourceBundle.loadImageResource
import static com.navigation.config.Constant.*

class MainContent extends BaseContent {
    private TabPane tabPane
    private VBox rightInfo
    private TableView<PlayListItem> playingList
    private TableColumn song
    private TableColumn singer
    private TableColumn album
    private TableColumn action
    private javafx.collections.ObservableList<PlayListItem> list = FXCollections.observableArrayList()
    private ImageView albumPic
    private JFXListView<HBox> lyricList
    private static final String EVENT_ADDRESS = "media_player"
    //记录当前歌曲位置 便于做曲目切换控制
    private int currentIndex;
    private SearchComponent search
    private PlayListComponent playList

    MainContent(MainController context) {
        super(context, "fxml/main_content_view.fxml")
        search = new SearchComponent(context)
        playList = new PlayListComponent(context)
        initView()
    }
    //初始化视图内容
    def initView() {

        tabPane = getContainer().lookup("#tabPane")
        rightInfo = getContainer().lookup("#rightInfo")
        tabPane.prefWidthProperty().bind(getContainer().widthProperty())
        rightInfo.prefWidthProperty().bind(getContainer().widthProperty() * 0.3)
        playingList = getContainer().lookup("#playingList")
        albumPic = getContainer().lookup("#albumPic")
        lyricList = getContainer().lookup("#lyricList")

        tabPane.tabs.get(2).setContent(search.container)
        tabPane.tabs.get(1).setContent(playList.container)

        lyricList.prefHeightProperty().bind(getContainer().heightProperty())

        playingList.columns.eachWithIndex { TableColumn entry, int i ->
            if (i === 0) {
                song = entry
                song.prefWidthProperty().bind(playingList.widthProperty() * 0.377)
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
        playingList.setSortPolicy {
            return null
        }
    }

    def initPlayerList(JsonObject _item) {
        Platform.runLater {
            tabPane.selectionModel.select(tabPane.tabs[0])
        }
        list.clear()
        CompletableFuture.runAsync { _tt ->
            _item.getJsonObject("playlist").getJsonArray("tracks").forEach {
                def obj = it as JsonObject
                def item = new PlayListItem(this)
                item.song = obj.getString("name")
                item.id = obj.getValue("id").toString()
                item.singer = (obj.getJsonArray("ar")[0] as JsonObject).getString("name")
                item.album = obj.getJsonObject("al").getString("name")
                item.albumPic = obj.getJsonObject("al").getString("picUrl")
                Platform.runLater {
                    list.add(item)
                }

            }
        }
    }

    /**
     *
     * 播放歌曲
     *
     */
    def play(PlayListItem item) {
        //清空歌词列表
        Platform.runLater {
            lyricList.items.clear()
        }
        lyricList.userData = 0
        getMusicUrl(context.vertx, item.id, item.source).setHandler {
            if (it.result() == "err") {
                showNotification("获取歌曲url失败", Pos.CENTER, NotificationType.ERROR)
                return
            }
            context.play(it.result())
            updateAlbumPic(item.albumPic)
        }
        loadLyric(item)
        currentIndex = list.lastIndexOf(item)
    }

    /**
     *
     * 加载歌词
     *
     */
    def loadLyric(PlayListItem item) {
        getMusicLyric(context.getVertx(), item.id, item.source).setHandler {
            def lyric = it.result()
            if (lyric == null || lyric.trim() == "") {
                return
            }
            def temp = lyric.split("\n")
            temp.eachWithIndex { String entry, int i ->
                if (entry.trim() === "") {
                    return
                }
                def timeReg = "\\[\\d*:\\d*((\\.|\\:)\\d*)*\\]";
                def isContainer = entry.matches(timeReg);
                if (isContainer) {
                    return
                }
                def label = new Label()
                label.text = entry.replaceAll(timeReg, "")
                def _tt = entry.substring(0, entry.lastIndexOf(']') + 1)
                if (_tt.matches("\\[\\d{2,}\\:\\d{2,}\\.\\d{2,}\\]")) {
                    def time = _tt.replace('[', "").replace(']', "")
                    def minute = time.split(":")[0].toLong()
                    def second = time.split(":")[1].split("\\.")[0].toLong()
                    label.setTextFill(Color.rgb(225, 225, 225, 0.8))
                    def box = new HBox()
                    box.setUserData(minute * 60 + second)
                    box.setAlignment(Pos.CENTER)
                    box.children.add(label)
                    Platform.runLater {
                        lyricList.items.add(box)
                    }
                }

            }
        }
    }

    //更新专题图片
    def updateAlbumPic(String url) {
        CompletableFuture<Image> future = new CompletableFuture()
        future.completeAsync({
            return loadImageResource(url, 160, 200, true)
        }, Executors.newSingleThreadExecutor())
        future.whenComplete { r, t ->
            if (t != null) {
                updateAlbumPic("images/player_cover.png")
            } else {
                Platform.runLater {
                    albumPic.setImage(r)
                }
            }
        }

    }

    //更新进度信息
    def updateProgress(long second) {
        //歌词列表为空
        if (lyricList.items.size() == 0) {
            return
        }
        for (def i = 0; i < lyricList.items.size(); i++) {
            def item = lyricList.items[i]
            if (item.userData == second) {
                item.styleClass.add("current_lyric")
                Platform.runLater {
                    lyricList.scrollTo(item)
                }
                if (lyricList.userData != null) {
                    lyricList.items[lyricList.userData].styleClass.remove("current_lyric")
                }
                lyricList.userData = i
                return
            }
        }
    }
    /**
     * 跟换状态
     *
     * @param status 正值下一曲 负值上一曲
     */
    def control(int status) {
        if (status > 0) {
            if (currentIndex != list.size() - 1) {
                currentIndex += 1
            } else {
                currentIndex = 0
            }
        } else {
            if (currentIndex != 0) {
                currentIndex -= 1
            } else {
                currentIndex = list.size() - 1
            }
        }
        play(list[currentIndex])
    }


    /**
     *
     * 下载歌曲
     *
     */
    def download(String id) {

    }

    @Override
    def init() {
        context.vertx.eventBus().<JsonObject> consumer(EVENT_ADDRESS, {
            final def data = it.body().getJsonObject(DATA)
            final def action = it.body().getString(ACTION)
            if (action === NEXT) {
                control(1)
            }
            if (action === PAST) {
                control(-1)
            }
            if (action == PLAY) {
                def item = new PlayListItem(this)
                item.source = data.getString(SOURCE)
                item.id = data.getString(ID)
                item.album = data.getString(ALBUM)
                item.singer = data.getString(SINGER)
                item.song = data.getString(SONG)
                item.albumPic = data.getString("albumPic")
                Platform.runLater {
                    this.list.add(item)
                }
                play(item)
            }
            if (action == UPDATE_LIST) {
                initPlayerList(data)
            }
        })
        playList.init()
        search.init()
    }

    @Override
    def remove() {
        context.vertx.eventBus().unregisterCodec(EVENT_ADDRESS)
    }
}
