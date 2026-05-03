package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
public class HelloController {

    @FXML
    private Hyperlink linkAccount;

    @FXML
    private TextField txtTimKiem;

    @FXML
    private FlowPane flowSachMoi;

    @FXML
    private FlowPane flowSachBanChay;

    private javafx.scene.layout.VBox taoCardSach(Sach sach) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(10);
        card.setStyle("-fx-alignment: CENTER; -fx-padding: 15; -fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(200);

        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
        try {
            imgView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(sach.getHinhAnh())));
        } catch (Exception e) {
            System.out.println("Lỗi ảnh: " + sach.getHinhAnh());
        }
        imgView.setFitWidth(150);
        imgView.setFitHeight(200);
        imgView.setCursor(javafx.scene.Cursor.HAND);
        imgView.setOnMouseClicked(e -> moTrangChiTiet(sach));

        javafx.scene.control.Label lblTen = new javafx.scene.control.Label(sach.getTenSach());
        lblTen.setStyle("-fx-font-weight: bold; -fx-text-alignment: CENTER;");
        lblTen.setWrapText(true);

        javafx.scene.control.Label lblGia = new javafx.scene.control.Label(String.format("%,.0f đ", sach.getGiaBan()));
        lblGia.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        javafx.scene.control.Button btnXem = new javafx.scene.control.Button("Xem ngay");
        btnXem.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-cursor: hand;");
        btnXem.setOnAction(e -> moTrangChiTiet(sach));

        card.getChildren().addAll(imgView, lblTen, lblGia, btnXem);
        return card;
    }

    private void dienFlowTuIds(FlowPane flow, List<String> ids) {
        if (flow == null) return;
        flow.getChildren().clear();
        for (String id : ids) {
            BookCatalog.findById(id).filter(s -> !s.isDeleted()).ifPresent(s -> flow.getChildren().add(taoCardSach(s)));
        }
    }

    @FXML
    public void handleCustomerLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            UserSession.clear();
            quayVeDangNhap();
        }
    }

    @FXML
    public void initialize() {
        if (UserSession.isLogged()) {
            linkAccount.setText("👤 " + UserSession.getUsername());
        }

        List<String> idsMoi = Arrays.asList(
                "ba-tuoc-monte-cristo", "giet-con-chim-nhai", "hai-so-phan", "khong-gia-dinh",
                "dac-nhan-tam", "nha-gia-kim", "phia-sau-nghi-can-x", "rung-nauy"
        );
        List<String> idsBanChay = Arrays.asList(
                "bo-gia", "nhung-nguoi-khon-kho", "phia-tay-khong-co-gi-la", "tuoi-tre-dang-gia-bao-nhieu"
        );
        dienFlowTuIds(flowSachMoi, idsMoi);
        dienFlowTuIds(flowSachBanChay, idsBanChay);
    }

    @FXML
    public void timKiemVaMoDanhMuc() {
        moTrangDanhSachSach();
    }

    @FXML
    public void moTrangDanhSachSach() {
        if (txtTimKiem != null) {
            CatalogSearchBridge.setPendingQuery(txtTimKiem.getText());
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/danh-muc-sach.fxml"));
            linkAccount.getScene().setRoot(loader.load());

            Stage stage = (Stage) linkAccount.getScene().getWindow();
            stage.setTitle("Danh Mục Sách - BOOKSTORE");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void quayVeDangNhap() {
        try {
            Stage stage = (Stage) linkAccount.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dang-nhap.fxml"));
            Scene scene = new Scene(loader.load());

            stage.setScene(scene);
            stage.setTitle("Đăng nhập - BOOKSTORE");

            stage.setMaximized(false);
            stage.sizeToScene();
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

            linkAccount.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void moTrangGioHang() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gio-hang.fxml"));
            linkAccount.getScene().setRoot(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
