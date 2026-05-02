package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;
import java.io.InputStream; // Quan trọng để hết lỗi dòng 42

public class ChiTietSachController {

    @FXML private BorderPane chiTietRoot;
    @FXML private Hyperlink linkAccount;

    // Các nhãn để hiển thị thông tin
    @FXML private Label lblĐuongDan;
    @FXML private ImageView imgBiaSach;
    @FXML private Label lblTenSach;
    @FXML private Label lblTacGia;
    @FXML private Label lblTheLoai;
    @FXML private Label lblGiaBan;

    @FXML
    public void initialize() {
        if (UserSession.isLogged()) {
            linkAccount.setText("👤 " + UserSession.getUsername());
        }
    }

    // HÀM ĐẶC BIỆT: Nhận dữ liệu sách và hiển thị lên màn hình

        @FXML private Label lblDuongDan;      // Đã đổi từ lblĐuongDan (bỏ dấu)[cite: 2]
        @FXML private Label lblNhaXuatBan;    // Cho bảng chi tiết[cite: 2]
        @FXML private Label lblSoTrang;       // Cho bảng chi tiết[cite: 2]
        @FXML private Label lblTheLoaiDetail; // Cho bảng chi tiết[cite: 2]
        @FXML private Label lblMoTa;          // Cho phần mô tả[cite: 2]

        public void setSachData(Sach sach) {
            if (sach == null) return;

            // Đổ dữ liệu vào các nhãn
            lblTenSach.setText(sach.getTenSach());
            lblDuongDan.setText(sach.getTenSach());
            lblTacGia.setText(sach.getTacGia());
            lblGiaBan.setText(String.format("%,.0f đ", sach.getGiaBan()));

            // Đổ dữ liệu vào BẢNG CHI TIẾT bên dưới[cite: 2]
            lblNhaXuatBan.setText(sach.getNhaXuatBan());
            lblSoTrang.setText(sach.getSoTrang() + " trang");
            lblTheLoaiDetail.setText(sach.getTheLoai());

            // Đổ dữ liệu vào MÔ TẢ[cite: 2]
            lblMoTa.setText(sach.getMoTa());
            lblMoTa.setWrapText(true);

            // Load ảnh bìa
            try {
                imgBiaSach.setImage(new Image(getClass().getResourceAsStream(sach.getHinhAnh())));
            } catch (Exception e) {
                System.out.println("Không tìm thấy ảnh: " + sach.getHinhAnh());
            }
        }

    // Các hàm điều hướng dùng setRoot (mượt mà, không giật màn hình)
    @FXML
    public void quayVeDanhSach() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/danh-muc-sach.fxml"));
            chiTietRoot.getScene().setRoot(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void quayVeTrangChu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hello-view.fxml"));
            chiTietRoot.getScene().setRoot(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}