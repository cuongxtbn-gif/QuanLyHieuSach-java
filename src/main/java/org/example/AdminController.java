package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class AdminController {

    @FXML
    private BorderPane adminRoot; // Cần ID này để lấy Stage khi chuyển cảnh

    @FXML
    private TableView<?> adminOrderTable;

    @FXML
    public void handleLogout() {
        // Hiện thông báo xác nhận đúng như trên Web của bạn
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống Quản trị?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Tải lại trang đăng nhập
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dang-nhap.fxml"));
                Scene loginScene = new Scene(loader.load());

                Stage stage = (Stage) adminRoot.getScene().getWindow();
                stage.setScene(loginScene);
                stage.setTitle("Đăng nhập - BOOKSTORE");
                stage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}