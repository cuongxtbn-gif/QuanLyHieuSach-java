package org.example;

// CÁC DÒNG IMPORT ĐÃ ĐƯỢC BỔ SUNG ĐẦY ĐỦ
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
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

                // Khi đăng xuất thì thu nhỏ lại và đưa ra giữa màn hình là chuẩn nhất
                stage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Đoạn code xử lý chuyển trang sang Nhà Cung Cấp
    @FXML
    public void moTrangNhaCungCap(ActionEvent event) {
        try {
            // Tải file giao diện nha-cung-cap.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nha-cung-cap.fxml"));
            Parent root = loader.load();

            // Lấy ra cái cửa sổ (Stage) hiện tại đang hiển thị
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // ĐÃ SỬA: Chỉ thay đổi lõi giao diện (root) để giữ nguyên kích thước cửa sổ hiện tại (Fullscreen)
            stage.getScene().setRoot(root);
            stage.setTitle("Hệ thống Quản trị - Nhà Phân Phối");

            // Xóa bỏ dòng tạo Scene mới (1000, 700) và dòng stage.centerOnScreen() ở đây

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}