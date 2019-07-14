package com.navigation.utils


import javafx.scene.image.Image

class ResourceBundle {
    /**
     *
     * 加载图片资源文件
     *
     * @param url
     */
    static Image loadImageResource(String url, double width, double height, boolean smooth) {
        def image
        if (url.startsWith("https://") || url.startsWith("http://")) {
            image = new Image(url, width, height, false, true)
        } else {
            image = new Image(ClassLoader.getSystemResourceAsStream(url), width, height, false, smooth)
        }
        return image
    }

}
