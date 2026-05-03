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
    private TextField txtRegName;

    @FXML
    private TextField txtRegEmail;

    @FXML
    private PasswordField txtRegPassword;

    @FXML
    public void handleLogin() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            hienThongBao(Alert.AlertType.ERROR, "Lỗi đăng nhập", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Đăng nhập quản trị: chỉ đúng khi tài khoản admin + mật khẩu 12345678
        if (user.equalsIgnoreCase("admin")) {
            if (!pass.equals("12345678")) {
                hienThongBao(Alert.AlertType.ERROR, "Sai mật khẩu", "Mật khẩu quản trị không đúng.");
                return;
            }
            hienThongBao(Alert.AlertType.INFORMATION, "Thành công", "Chào ADMIN!");
            chuyenTrang("/admin-layout.fxml", "Hệ thống Quản trị - BOOKSTORE", user);
            return;
        }

        // Khách hàng: phải đã đăng ký
        if (!RegisteredCustomerStore.isRegistered(user)) {
            hienThongBao(Alert.AlertType.ERROR, "Chưa có tài khoản",
                    "Email này chưa đăng ký. Vui lòng chuyển sang tab \"TẠO TÀI KHOẢN\" để đăng ký trước.");
            return;
        }
        if (!RegisteredCustomerStore.verify(user, pass)) {
            hienThongBao(Alert.AlertType.ERROR, "Sai mật khẩu", "Mật khẩu không đúng.");
            return;
        }

        String sessionKey = CustomerAccountStore.resolveCustomerKey(user);
        hienThongBao(Alert.AlertType.INFORMATION, "Thành công", "Chào khách hàng: " + sessionKey);
        UserSession.setUsername(sessionKey);
        chuyenTrang("/hello-view.fxml", "BOOKSTORE", sessionKey);
    }

    @FXML
    public void handleRegister() {
        String email = txtRegEmail != null ? txtRegEmail.getText().trim() : "";
        String pass = txtRegPassword != null ? txtRegPassword.getText() : "";

        if (email.isEmpty() || pass.isEmpty()) {
            hienThongBao(Alert.AlertType.ERROR, "Thiếu thông tin", "Vui lòng nhập email và mật khẩu.");
            return;
        }
        if (RegisteredCustomerStore.normalizeEmail(email).equals("admin")) {
            hienThongBao(Alert.AlertType.ERROR, "Không hợp lệ", "Không thể đăng ký với email này.");
            return;
        }
        if (!RegisteredCustomerStore.register(email, pass)) {
            hienThongBao(Alert.AlertType.ERROR, "Đăng ký thất bại", "Email đã được sử dụng. Hãy đăng nhập hoặc dùng email khác.");
            return;
        }
        hienThongBao(Alert.AlertType.INFORMATION, "Đăng ký thành công",
                "Bạn có thể chuyển sang tab ĐĂNG NHẬP và đăng nhập bằng email vừa tạo.");
        if (txtRegPassword != null) {
            txtRegPassword.clear();
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