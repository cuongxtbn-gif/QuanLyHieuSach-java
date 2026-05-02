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
    private boolean loadSubPage(String fxmlFile) {
        if (contentArea == null) {
            return false;
        }
        try {
            java.net.URL resource = getClass().getResource(fxmlFile);
            if (resource == null) {
                showError("Không tìm thấy file giao diện: " + fxmlFile);
                return false;
            }
            Parent root = FXMLLoader.load(resource);
            contentArea.getChildren().setAll(root);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi không tải được trang: " + fxmlFile + "\n" + e.getMessage());
            return false;
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
    private void goToOrderManagement(ActionEvent event) {
        if (!loadSubPage("/admin-don-hang.fxml")) {
            openAsStandalone(event, "/admin-don-hang.fxml", "Quản lý Đơn hàng - BOOKSTORE");
        }
    }

    @FXML
    private void goToDiscountManagement(ActionEvent event) {
        if (!loadSubPage("/admin-ma-giam-gia.fxml")) {
            openAsStandalone(event, "/admin-ma-giam-gia.fxml", "Quản lý Mã giảm giá - BOOKSTORE");
        }
    }

    @FXML
    private void goToVoucher(ActionEvent event) {
        goToDiscountManagement(event);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        dangXuat(event);
    }

    private void openAsStandalone(ActionEvent event, String fxmlFile, String title) {
        try {
            java.net.URL resource = getClass().getResource(fxmlFile);
            if (resource == null) {
                showError("Không tìm thấy file giao diện: " + fxmlFile);
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Không thể mở giao diện: " + fxmlFile + "\n" + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi điều hướng");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}