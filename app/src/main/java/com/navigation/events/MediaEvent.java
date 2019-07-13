package com.navigation.events;

import com.navigation.controller.MainController;
import com.navigation.enums.NotificationType;
import io.vertx.core.json.JsonObject;
import javafx.geometry.Pos;
import uk.co.caprica.vlcj.media.*;
import uk.co.caprica.vlcj.player.base.*;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import static com.navigation.utils.Message.showNotification;
import static com.navigation.config.Constant.*;

public class MediaEvent {
    private final MainController context;
    private final EmbeddedMediaPlayer player;

    public MediaEvent(MainController context, EmbeddedMediaPlayer player) {
        this.context = context;
        this.player = player;
        registerEvents();
    }

    private void registerEvents() {
        player.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                if (mediaPlayer.status().state() == State.PLAYING) {
                    context.updateProgress(newTime);
                }
            }
        });
        player.events().addMediaEventListener(new MediaEventListener() {
            @Override
            public void mediaMetaChanged(Media media, Meta metaType) {
                System.out.println("change");
            }

            @Override
            public void mediaSubItemAdded(Media media, MediaRef newChild) {

                System.out.println("subItem");
            }

            @Override
            public void mediaDurationChanged(Media media, long newDuration) {
                context.updateProgress(newDuration);
            }

            @Override
            public void mediaParsedChanged(Media media, MediaParsedStatus newStatus) {
                System.out.println("parsed");
            }

            @Override
            public void mediaFreed(Media media, MediaRef mediaFreed) {
                System.out.println("freed");
            }

            @Override
            public void mediaStateChanged(Media media, State newState) {
                //加载中
                if (newState == State.BUFFERING) {

                }
                //播放结束
                if (newState == State.ENDED) {
                    var param = new JsonObject();
                    param.put(ACTION, NEXT);
                    context.getVertx().eventBus().send("media_player", param);
                }
                //错误
                if (newState == State.ERROR) {
                    showNotification("播放出错", Pos.CENTER, NotificationType.ERROR);
                }
                //停止
                if (newState == State.STOPPED) {

                }
                //暂停
                if (newState == State.PAUSED) {

                }
                //正在播放
                if (newState == State.PLAYING) {
                    context.initProgress(media.info().duration());
                    context.updatePlayIcon(true);
                }
            }

            @Override
            public void mediaSubItemTreeAdded(Media media, MediaRef item) {
                System.out.println("tree");
            }

            @Override
            public void mediaThumbnailGenerated(Media media, Picture picture) {
                System.out.println(picture.width());
            }
        });
    }

    /**
     * 字符转码
     *
     * @param api
     * @param meta
     * @return
     */
    private String getMeta(MetaApi api, Meta meta) {
        return api.get(meta);
    }
}
