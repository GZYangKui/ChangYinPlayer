package com.navigation.base

import com.navigation.controller.MainController

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.layout.BorderPane

import static com.navigation.utils.Message.showError

class BaseView {
    protected final MainController context
    private final BorderPane container

    BaseView(MainController context, String viewPath) {
        this.context = context
        final Parent root = FXMLLoader.load(ClassLoader.getSystemResource(viewPath))
        container = root.lookup("#container") as BorderPane
    }
    //获取容器
    BorderPane getContainer() {
        return container
    }
    //内容面板添加到场景图中时调用
    def init() {
        //TODO OVERRIDE
    }
    //内容从场景图中移除时调用
    def remove() {
        //TODO OVERRIDE
    }
    //异常处理
    def exception(String title,Throwable t) {
        showError(title,t)
    }
}
