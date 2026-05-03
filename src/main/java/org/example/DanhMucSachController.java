package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DanhMucSachController {

    @FXML
    private BorderPane danhMucRoot;
    @FXML
    private Hyperlink linkAccount;
    @FXML
    private TilePane gridSach;
    @FXML
    private ComboBox<String> cbSapXep;
    @FXML
    private ComboBox<String> cbTheLoai;
    @FXML
    private TextField txtTimKiem;
    @FXML private Button btnTrang1;
    @FXML private Button btnTrang2;
    @FXML private Button btnTrang3;
    @FXML private Button btnTrang4;

    private List<Sach> sachSauLoc = new ArrayList<>();
    private int trangHienTai = 1;
    private int soSachMoiTrang = 12; // 12 cuốn/trang sẽ vừa đẹp cho 4 trang
    @FXML
    public void initialize() {
        cbSapXep.getItems().addAll("Mức độ phổ biến", "Giá: Cao -> Thấp", "Giá: Thấp -> Cao");
        cbTheLoai.getItems().addAll("Tất cả", "Văn học", "Trinh thám", "Kỹ năng", "Kinh tế", "Ngoại ngữ", "Tâm lý");
        cbSapXep.getSelectionModel().selectFirst();
        cbTheLoai.getSelectionModel().selectFirst();

        if (UserSession.isLogged()) {
            linkAccount.setText("👤 " + UserSession.getUsername());
        } else {
            linkAccount.setText("Đăng nhập");
        }

        if (txtTimKiem != null) {
            txtTimKiem.textProperty().addListener((obs, oldV, newV) -> apDungLocVaSapXep());
        }
        cbSapXep.setOnAction(e -> apDungLocVaSapXep());
        cbTheLoai.setOnAction(e -> apDungLocVaSapXep());

        String pending = CatalogSearchBridge.consumePendingQuery();
        if (txtTimKiem != null && pending != null && !pending.isEmpty()) {
            txtTimKiem.setText(pending);
        }

        apDungLocVaSapXep();
    }

    private void apDungLocVaSapXep() {
        List<Sach> working = new ArrayList<>(BookCatalog.getAllBooks());

        String theLoaiChon = cbTheLoai.getSelectionModel().getSelectedItem();
        if (theLoaiChon != null && !theLoaiChon.isBlank() && !"Tất cả".equals(theLoaiChon)) {
            working = working.stream()
                    .filter(s -> theLoaiChon.equals(s.getTheLoai()))
                    .collect(Collectors.toList());
        }

        String tuKhoa = txtTimKiem != null ? txtTimKiem.getText() : "";
        working = BookCatalog.filterByQuery(working, tuKhoa);

        String sapXep = cbSapXep.getSelectionModel().getSelectedItem();
        if ("Giá: Cao -> Thấp".equals(sapXep)) {
            working.sort(Comparator.comparingDouble(Sach::getGiaBan).reversed());
        } else if ("Giá: Thấp -> Cao".equals(sapXep)) {
            working.sort(Comparator.comparingDouble(Sach::getGiaBan));
        } else {
            working.sort(Comparator.comparingInt(Sach::getSoTrang).reversed());
        }

        sachSauLoc = working;
        hienThiSachTheoTrang(1);
    }

    private VBox taoGiaoDienMotCuonSach(Sach sach) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(200);

        ImageView imgView = new ImageView();
        try {
            // Lấy ảnh từ thư mục resources
            InputStream stream = getClass().getResourceAsStream(sach.getHinhAnh());
            if (stream != null) {
                imgView.setImage(new Image(stream));
            }
        } catch (Exception e) {
            System.out.println("Không tải được ảnh: " + sach.getHinhAnh());
        }
        imgView.setFitWidth(140);
        imgView.setFitHeight(190);

        Label lblTen = new Label(sach.getTenSach());
        lblTen.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        lblTen.setWrapText(true);
        lblTen.setAlignment(Pos.CENTER);

        Label lblGia = new Label(String.format("%,.0f đ", sach.getGiaBan()));
        lblGia.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 16px;");

        Button btnMua = new Button("Mua ngay");
        btnMua.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-pref-width: 120px;");

        // Bấm vào nút Mua ngay hoặc bấm vào Ảnh đều mở trang Chi tiết
        if (sach.getTonKho() <= 0) {
            btnMua.setText("Đã hết hàng");
            btnMua.setDisable(true);
            btnMua.setStyle("-fx-background-color: #cbd5e1; -fx-text-fill: #334155; -fx-cursor: default; -fx-pref-width: 120px;");
        } else {
            btnMua.setOnAction(e -> moTrangChiTiet(sach));
        }
        imgView.setOnMouseClicked(e -> moTrangChiTiet(sach));
        imgView.setStyle("-fx-cursor: hand;"); // Đổi con trỏ chuột thành hình bàn tay khi di qua ảnh

        card.getChildren().addAll(imgView, lblTen, lblGia, btnMua);
        return card;
    }

    // Hàm cắt danh sách giống hệt JS của bạn[cite: 1]
    private void hienThiSachTheoTrang(int trang) {
        gridSach.getChildren().clear();

        if (sachSauLoc.isEmpty()) {
            gridSach.getChildren().add(new Label("Không có sách phù hợp với bộ lọc hoặc từ khóa."));
            capNhatMauNutPhanTrang();
            return;
        }

        int tongSoTrang = (int) Math.ceil(sachSauLoc.size() / (double) soSachMoiTrang);
        if (trang < 1) {
            trang = 1;
        }
        if (trang > tongSoTrang) {
            trang = tongSoTrang;
        }
        trangHienTai = trang;

        int batDau = (trang - 1) * soSachMoiTrang;
        int ketThuc = Math.min(batDau + soSachMoiTrang, sachSauLoc.size());

        for (int i = batDau; i < ketThuc; i++) {
            gridSach.getChildren().add(taoGiaoDienMotCuonSach(sachSauLoc.get(i)));
        }

        capNhatMauNutPhanTrang();
    }

    // Hàm tô màu nút màu cam cho trang đang chọn
    private void capNhatMauNutPhanTrang() {
        String mauThuong = "-fx-background-color: #ecf0f1; -fx-text-fill: black;";
        String mauCam = "-fx-background-color: #f39c12; -fx-text-fill: white;";

        int tongSoTrang = sachSauLoc.isEmpty() ? 1
                : (int) Math.ceil(sachSauLoc.size() / (double) soSachMoiTrang);

        if (btnTrang1 != null) {
            btnTrang1.setDisable(tongSoTrang < 1);
            btnTrang1.setStyle(trangHienTai == 1 ? mauCam : mauThuong);
        }
        if (btnTrang2 != null) {
            btnTrang2.setDisable(tongSoTrang < 2);
            btnTrang2.setStyle(trangHienTai == 2 ? mauCam : mauThuong);
        }
        if (btnTrang3 != null) {
            btnTrang3.setDisable(tongSoTrang < 3);
            btnTrang3.setStyle(trangHienTai == 3 ? mauCam : mauThuong);
        }
        if (btnTrang4 != null) {
            btnTrang4.setDisable(tongSoTrang < 4);
            btnTrang4.setStyle(trangHienTai == 4 ? mauCam : mauThuong);
        }
    }

    // Các nút chuyển trang trong FXML sẽ gọi vào đây
    @FXML public void chuyenTrang1() { hienThiSachTheoTrang(1); }
    @FXML public void chuyenTrang2() { hienThiSachTheoTrang(2); }
    @FXML public void chuyenTrang3() { hienThiSachTheoTrang(3); }
    @FXML public void chuyenTrang4() { hienThiSachTheoTrang(4); }

    @FXML
    public void chuyenTrangTiep() {
        int tongSoTrang = sachSauLoc.isEmpty() ? 1
                : (int) Math.ceil(sachSauLoc.size() / (double) soSachMoiTrang);
        if (trangHienTai < tongSoTrang) {
            hienThiSachTheoTrang(trangHienTai + 1);
        }
    }
    @FXML
    public void quayVeTrangChu() {
        try {
            // Tương tự, lấy nội dung trang chủ...
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hello-view.fxml"));

            // ...bơm thẳng vào Scene hiện tại
            danhMucRoot.getScene().setRoot(loader.load());

            // Đổi tiêu đề
            Stage stage = (Stage) danhMucRoot.getScene().getWindow();
            stage.setTitle("BOOKSTORE - Mua sắm trực tuyến");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Hàm xử lý khi bấm vào chữ "Tài khoản / 👤 Tên" trên menu
    @FXML
    public void handleAccountAction() {
        if (UserSession.isLogged()) {
            // Nếu đã đăng nhập -> Hỏi xác nhận Đăng xuất
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận đăng xuất");
            alert.setHeaderText(null);
            alert.setContentText("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản '" + UserSession.getUsername() + "'?");

            java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                UserSession.clear(); // Xóa trí nhớ
                chuyenTrangSang("/dang-nhap.fxml", "Đăng nhập - BOOKSTORE");
            }
        } else {
            // Nếu chưa đăng nhập -> Chuyển sang trang Đăng nhập
            chuyenTrangSang("/dang-nhap.fxml", "Đăng nhập - BOOKSTORE");
        }
    }

    // Hàm chuyển trang giữ nguyên khung cửa sổ (để không bị nhảy lằng nhằng)
    private void chuyenTrangSang(String fxmlFile, String title) {
        try {
            Stage stage = (Stage) danhMucRoot.getScene().getWindow();
            double width = stage.getScene().getWidth();
            double height = stage.getScene().getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load(), width, height);

            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void moTrangChiTiet(Sach sach) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chi-tiet-sach.fxml"));
            javafx.scene.Parent root = loader.load();

            // Truyền dữ liệu cuốn sách đang được click sang cho Controller Chi Tiết
            ChiTietSachController controller = loader.getController();
            controller.setSachData(sach);

            // Bơm giao diện mới vào
            danhMucRoot.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void moTrangGioHang() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gio-hang.fxml"));
            danhMucRoot.getScene().setRoot(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}