package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class AdminController {

    @FXML
    private StackPane contentArea; // Đây là cái hộp trống bên phải

    @FXML
    public void initialize() {
        // Mặc định mở trang thống kê đầu tiên khi vừa vào Admin
        moTrangThongKe();
    }

    // Hàm dùng chung để nhúng file FXML khác vào ô bên phải
    private void loadSubPage(String fxmlFile) {
        if (contentArea == null) {
            return;
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi không tải được trang: " + fxmlFile);
        }
    }

    @FXML
    public void moTrangThongKe() { loadSubPage("/admin-thong-ke.fxml"); }

    @FXML
    public void moTrangQuanLySach() { loadSubPage("/admin-quan-ly-sach.fxml"); }

    @FXML
    public void moTrangDonHang() { loadSubPage("/admin-don-hang.fxml"); }

    @FXML
    public void moTrangMaGiamGia() { loadSubPage("/admin-ma-giam-gia.fxml"); }

    @FXML
    public void moTrangNhaCungCap() { loadSubPage("/admin-nha-cung-cap.fxml"); }

    @FXML
    public void dangXuat(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống Quản trị?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Stage stage;
                if (contentArea != null && contentArea.getScene() != null) {
                    stage = (Stage) contentArea.getScene().getWindow();
                } else {
                    stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                }
                Parent root = FXMLLoader.load(getClass().getResource("/dang-nhap.fxml"));
                stage.setScene(new Scene(root));
                stage.setTitle("Đăng nhập - BOOKSTORE");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void goToVoucher(ActionEvent event) {
        try {
            // Load thẳng file FXML của trang mã giảm giá
            Parent voucherRoot = FXMLLoader.load(getClass().getResource("/admin-ma-giam-gia.fxml"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(voucherRoot));
            stage.setTitle("Quản lý Mã giảm giá - BOOKSTORE");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setContentText("Không thể mở file fxml: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        dangXuat(event);
    }
}