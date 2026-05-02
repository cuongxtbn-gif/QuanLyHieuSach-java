package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;
import java.io.IOException;

public class DangNhapController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    public void handleLogin() {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            hienThongBao(Alert.AlertType.ERROR, "Lỗi đăng nhập", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (user.equalsIgnoreCase("admin") && pass.equals("12345678")) {
            hienThongBao(Alert.AlertType.INFORMATION, "Thành công", "Chào ADMIN!");
            chuyenTrang("/index-admin.fxml", "Hệ thống Quản trị - BOOKSTORE", user);
        } else {
            hienThongBao(Alert.AlertType.INFORMATION, "Thành công", "Chào khách hàng: " + user);
            UserSession.setUsername(user);
            chuyenTrang("/hello-view.fxml", "BOOKSTORE", user);
        }
    }

    private void chuyenTrang(String fxmlFile, String title, String username) {
        try {
            Stage stage = (Stage) txtUsername.getScene().getWindow();

            // Bỏ width, height ở đây đi, cứ để nó load tự nhiên
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());

            if (fxmlFile.equals("/hello-view.fxml")) {
                javafx.application.Platform.runLater(() -> {
                    Hyperlink link = (Hyperlink) scene.lookup("#linkAccount");
                    if (link != null) {
                        link.setText("👤 " + username);
                    }
                });
            }

            stage.setScene(scene);
            stage.setTitle(title);

            // TUYỆT CHIÊU: Phóng to toàn màn hình cho vừa khít với mọi máy tính
            stage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void hienThongBao(Alert.AlertType type, String tieuDe, String noiDung) {
        Alert alert = new Alert(type);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}