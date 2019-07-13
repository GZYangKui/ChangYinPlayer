package com.navigation.component

import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

import static com.navigation.utils.ResourceBundle.loadImageResource

class PlayListItem {

    VBox container = new VBox()
    ImageView icon = new ImageView()
    Label label = new Label()

    PlayListItem(final String iconUrl, String title) {
        label.setText(title)
        container.setAlignment(Pos.CENTER)
        final CompletableFuture<Image> future = new CompletableFuture()
        icon.setImage(loadImageResource("images/player_cover.png", 100, 100, true))
        future.completeAsync({
            return loadImageResource(iconUrl, 100, 100, true)
        }, Executors.newSingleThreadExecutor())
        future.whenComplete { r, t ->
            Platform.runLater {
                icon.setImage(r)
            }
        }
        container.setPrefSize(100,150)
        container.children.addAll(icon, label)
    }

    VBox getItem() {
        return container
    }
}
