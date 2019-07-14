package com.navigation.utils

import com.navigation.enums.NotificationType
import javafx.application.Platform
import javafx.geometry.Pos
import org.controlsfx.control.Notifications
import org.controlsfx.dialog.ExceptionDialog

class Message{
    /**
     *
     * 显示通知
     *
     */
    static void showNotification(String content, Pos pos, NotificationType type) {
        Platform.runLater{
            def n = Notifications.create();
            n.text(content);
            n.position(pos);
            if (type == NotificationType.CONFIRM) {
                n.showConfirm();
            }
            if (type == NotificationType.ERROR) {
                n.showError();
            }
            if (type == NotificationType.WARNING) {
                n.showWarning();
            }
            if (type == NotificationType.INFO) {
                n.showInformation();
            }
        }
    }

/**
 * 弹出对话框显示异常信息
 * @param title 异常信息简要描述
 * @param t 完整的异常信息
 */
    static void showError(String title,Throwable t) {
        Platform.runLater{
            def dialog = new ExceptionDialog(t)
            dialog.setTitle(title)
            dialog.showAndWait()
        }
    }

}
