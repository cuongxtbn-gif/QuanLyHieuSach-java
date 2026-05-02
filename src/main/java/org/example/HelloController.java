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

    @FXML private javafx.scene.layout.FlowPane flowSach;

    private javafx.scene.layout.VBox taoCardSach(Sach sach) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(10);
        card.setStyle("-fx-alignment: CENTER; -fx-padding: 15; -fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(200);

        // 1. Ảnh sách
        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
        try {
            imgView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(sach.getHinhAnh())));
        } catch (Exception e) { System.out.println("Lỗi ảnh: " + sach.getHinhAnh()); }
        imgView.setFitWidth(150);
        imgView.setFitHeight(200);
        imgView.setCursor(javafx.scene.Cursor.HAND);

        // SỰ KIỆN: Click vào ảnh là xem chi tiết
        imgView.setOnMouseClicked(e -> moTrangChiTiet(sach));

        // 2. Tên sách
        javafx.scene.control.Label lblTen = new javafx.scene.control.Label(sach.getTenSach());
        lblTen.setStyle("-fx-font-weight: bold; -fx-text-alignment: CENTER;");
        lblTen.setWrapText(true);

        // 3. Giá bán
        javafx.scene.control.Label lblGia = new javafx.scene.control.Label(String.format("%,.0f đ", sach.getGiaBan()));
        lblGia.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        // 4. Nút Xem ngay
        javafx.scene.control.Button btnXem = new javafx.scene.control.Button("Xem ngay");
        btnXem.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-cursor: hand;");

        // SỰ KIỆN: Click vào nút cũng xem chi tiết
        btnXem.setOnAction(e -> moTrangChiTiet(sach));

        card.getChildren().addAll(imgView, lblTen, lblGia, btnXem);
        return card;
    }

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
        // 1. Hiển thị tên user trên thanh menu
        if (UserSession.isLogged()) {
            linkAccount.setText("👤 " + UserSession.getUsername());
        }

        // 2. Kiểm tra xem flowSach có tồn tại không rồi mới đổ dữ liệu
        if (flowSach != null) {
            flowSach.getChildren().clear();

            // ĐÂY LÀ CÁCH VIẾT ĐÚNG: Tạo biến s1 rồi mới ném vào flowSach
            Sach s1 = new Sach(
                    "ba-tuoc-monte-cristo",
                    "Bá tước Monte Cristo",
                    185000.0,
                    "/assets/images/ba-tuoc-monte-cristo.jpg",
                    "Alexandre Dumas",
                    "Văn học",
                    "NXB Văn Học",
                    800,
                    "Câu chuyện về sự phản bội và trả thù..."
            );

            // Gọi hàm tạo card và thêm vào giao diện
            flowSach.getChildren().add(taoCardSach(s1));
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
    private void moTrangChiTiet(Sach sach) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chi-tiet-sach.fxml"));
            javafx.scene.Parent root = loader.load();

            ChiTietSachController controller = loader.getController();
            controller.setSachData(sach);

            // Dùng setRoot để chuyển trang mượt mà
            linkAccount.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}