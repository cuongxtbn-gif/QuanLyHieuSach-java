package org.example;

// CÁC DÒNG IMPORT ĐÃ ĐƯỢC BỔ SUNG ĐẦY ĐỦ
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
        try {
            URL resource = getClass().getResource(fxmlFile);
            String normalized = fxmlFile.startsWith("/") ? fxmlFile.substring(1) : fxmlFile;

            if (resource == null) {
                resource = Thread.currentThread().getContextClassLoader().getResource(normalized);
            }
            if (resource == null) {
                Path fromTarget = Path.of("target", "classes", normalized);
                if (Files.exists(fromTarget)) {
                    resource = fromTarget.toUri().toURL();
                }
            }
            if (resource == null) {
                Path fromSource = Path.of("src", "main", "resources", normalized);
                if (Files.exists(fromSource)) {
                    resource = fromSource.toUri().toURL();
                }
            }
            if (resource == null) {
                throw new IOException("Không tìm thấy file: " + fxmlFile + " (classpath + target/classes + src/main/resources)");
            }

            Parent root = FXMLLoader.load(resource);
            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            String reason = e.getMessage() == null ? "(không có chi tiết)" : e.getMessage();
            alert.setContentText("Không thể mở trang: " + fxmlFile + "\nLý do: " + reason);
            alert.showAndWait();
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
    public void moTrangNhaCungCap() { loadSubPage("/nha-cung-cap.fxml"); }

    @FXML
    public void dangXuat(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống Quản trị?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Stage stage = (Stage) contentArea.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("/dang-nhap.fxml"));
                stage.setScene(new Scene(root));
                stage.setTitle("Đăng nhập - BOOKSTORE");

                // Khi đăng xuất thì thu nhỏ lại và đưa ra giữa màn hình là chuẩn nhất
                stage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}