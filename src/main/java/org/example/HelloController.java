package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class HelloController {

    @FXML
    private Hyperlink linkAccount; // Đây là nút hiển thị tên khách hàng

    @FXML
    public void handleCustomerLogout() {
        // Hiện bảng xác nhận giống bản Web
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            UserSession.clear(); // Xóa tên khách hàng khỏi bộ nhớ
            quayVeDangNhap();
        }
    }
    @FXML
    public void initialize() {
        if (UserSession.isLogged()) {
            linkAccount.setText("👤 " + UserSession.getUsername());
        } else {
            linkAccount.setText("Đăng nhập");
        }
    }
    @FXML
    public void moTrangDanhSachSach() {
        try {
            // Không tạo Scene mới nữa, chỉ lấy nội dung của trang Sách...
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/danh-muc-sach.fxml"));

            // ...và "bơm" thẳng vào cái Scene hiện tại
            linkAccount.getScene().setRoot(loader.load());

            // Đổi tiêu đề cửa sổ
            Stage stage = (Stage) linkAccount.getScene().getWindow();
            stage.setTitle("Danh Mục Sách - BOOKSTORE");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void quayVeDangNhap() {
        try {
            // Lấy cái khung cửa sổ (Stage) hiện tại
            Stage stage = (Stage) linkAccount.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dang-nhap.fxml"));
            Scene scene = new Scene(loader.load());

            stage.setScene(scene);
            stage.setTitle("Đăng nhập - BOOKSTORE");

            // 1. Tắt chế độ toàn màn hình đi
            stage.setMaximized(false);

            // 2. Ép cửa sổ tự động thu nhỏ lại cho vừa khít với Form Đăng nhập
            stage.sizeToScene();

            // 3. Đưa nó ra giữa màn hình
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}