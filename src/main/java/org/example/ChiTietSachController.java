package org.example;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
public class ChiTietSachController {

    @FXML private BorderPane chiTietRoot;
    @FXML private Hyperlink linkAccount;

    @FXML private ImageView imgBiaSach;
    @FXML private Label lblTenSach;
    @FXML private Label lblTacGia;
    @FXML private Label lblGiaBan;
    @FXML private Label lblDuongDan;
    @FXML private Label lblNhaXuatBan;
    @FXML private Label lblSoTrang;
    @FXML private Label lblTheLoaiDetail;
    @FXML private Label lblTonKhoDetail;
    @FXML private Label lblMoTa;

    @FXML private TextField txtSoLuong;
    @FXML private Button btnThemGio;
    @FXML private Button btnMuaNgay;

    @FXML private VBox vboxQuickCheckout;
    @FXML private TextField txtShipName;
    @FXML private TextField txtShipPhone;
    @FXML private TextArea txtShipAddress;
    @FXML private VBox vboxOrderPlaced;
    @FXML private Label lblOrderReceipt;

    private Sach sachChon;
    private final DecimalFormat moneyFmt = new DecimalFormat("#,###đ");

    @FXML
    public void initialize() {
        if (UserSession.isLogged()) {
            linkAccount.setText("👤 " + UserSession.getUsername());
        }
    }

    public void setSachData(Sach sach) {
        if (sach == null) return;
        this.sachChon = sach;

        lblTenSach.setText(sach.getTenSach());
        lblDuongDan.setText(sach.getTenSach());
        lblTacGia.setText(sach.getTacGia());
        lblGiaBan.setText(String.format("%,.0f đ", sach.getGiaBan()));

        lblNhaXuatBan.setText(sach.getNhaXuatBan());
        lblSoTrang.setText(sach.getSoTrang() + " trang");
        lblTheLoaiDetail.setText(sach.getTheLoai());
        if (lblTonKhoDetail != null) {
            lblTonKhoDetail.setText(sach.getTonKho() > 0 ? (sach.getTonKho() + " cuốn") : "Đã hết hàng");
            if (sach.getTonKho() <= 0) {
                lblTonKhoDetail.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            }
        }

        lblMoTa.setText(sach.getMoTa());
        lblMoTa.setWrapText(true);

        if (txtSoLuong != null) {
            txtSoLuong.setText("1");
        }
        capNhatTrangThaiTonKho();
        anFormDatHangVaBienLai();

        try {
            imgBiaSach.setImage(new Image(getClass().getResourceAsStream(sach.getHinhAnh())));
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh: " + sach.getHinhAnh());
        }
    }

    private void capNhatTrangThaiTonKho() {
        boolean outOfStock = sachChon != null && sachChon.getTonKho() <= 0;
        if (txtSoLuong != null) {
            txtSoLuong.setDisable(outOfStock);
        }
        if (btnThemGio != null) {
            btnThemGio.setDisable(outOfStock);
            if (outOfStock) btnThemGio.setText("Đã hết hàng");
        }
        if (btnMuaNgay != null) {
            btnMuaNgay.setDisable(outOfStock);
            if (outOfStock) btnMuaNgay.setText("Đã hết hàng");
        }
    }

    private void anFormDatHangVaBienLai() {
        if (vboxQuickCheckout != null) {
            vboxQuickCheckout.setVisible(false);
            vboxQuickCheckout.setManaged(false);
        }
        if (vboxOrderPlaced != null) {
            vboxOrderPlaced.setVisible(false);
            vboxOrderPlaced.setManaged(false);
        }
        if (txtShipName != null) txtShipName.clear();
        if (txtShipPhone != null) txtShipPhone.clear();
        if (txtShipAddress != null) txtShipAddress.clear();
        if (lblOrderReceipt != null) lblOrderReceipt.setText("");
    }

    private boolean yeuCauDangNhap() {
        if (UserSession.isLogged()) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cần đăng nhập");
        alert.setHeaderText(null);
        alert.setContentText("Vui lòng đăng nhập để thêm vào giỏ hoặc đặt hàng.");
        alert.showAndWait();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dang-nhap.fxml"));
            chiTietRoot.getScene().setRoot(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int docSoLuong() {
        if (txtSoLuong == null) return 1;
        try {
            int q = Integer.parseInt(txtSoLuong.getText().trim());
            return Math.max(1, Math.min(q, 999));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @FXML
    public void themVaoGioHang() {
        if (sachChon == null) return;
        if (sachChon.getTonKho() <= 0) {
            Alert err = new Alert(Alert.AlertType.WARNING);
            err.setTitle("Hết hàng");
            err.setHeaderText(null);
            err.setContentText("Sản phẩm này hiện đã hết hàng.");
            err.showAndWait();
            return;
        }
        if (!yeuCauDangNhap()) return;

        int qty = docSoLuong();
        if (qty > sachChon.getTonKho()) qty = sachChon.getTonKho();
        String ten = sachChon.getTenSach();
        double gia = sachChon.getGiaBan();
        ObservableList<CartController.CartItem> cart = CustomerAccountStore.getCart(UserSession.getUsername());

        boolean merged = false;
        for (CartController.CartItem it : cart) {
            String itId = it.bookIdProperty() == null ? null : it.bookIdProperty().get();
            if ((itId != null && itId.equals(sachChon.getId())) || ten.equals(it.productNameProperty().get())) {
                it.setQuantity(it.getQuantity() + qty);
                merged = true;
                break;
            }
        }
        if (!merged) {
            cart.add(new CartController.CartItem(sachChon.getId(), ten, gia, qty));
        }

        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setTitle("Giỏ hàng");
        ok.setHeaderText(null);
        ok.setContentText("Đã thêm " + qty + " cuốn «" + ten + "» vào giỏ hàng.");
        ok.showAndWait();
    }

    @FXML
    public void onMuaNgay() {
        if (sachChon == null) return;
        if (sachChon.getTonKho() <= 0) {
            Alert err = new Alert(Alert.AlertType.WARNING);
            err.setTitle("Hết hàng");
            err.setHeaderText(null);
            err.setContentText("Sản phẩm này hiện đã hết hàng.");
            err.showAndWait();
            return;
        }
        if (!yeuCauDangNhap()) return;

        if (vboxOrderPlaced != null) {
            vboxOrderPlaced.setVisible(false);
            vboxOrderPlaced.setManaged(false);
        }
        if (vboxQuickCheckout != null) {
            vboxQuickCheckout.setVisible(true);
            vboxQuickCheckout.setManaged(true);
        }
        if (txtShipName != null) {
            txtShipName.requestFocus();
        }
    }

    @FXML
    public void huyMuaNgay() {
        anFormDatHangVaBienLai();
    }

    @FXML
    public void xacNhanMuaNgay() {
        if (sachChon == null) return;
        if (!UserSession.isLogged()) return;

        String name = txtShipName != null ? txtShipName.getText().trim() : "";
        String phone = txtShipPhone != null ? txtShipPhone.getText().trim() : "";
        String address = txtShipAddress != null ? txtShipAddress.getText().trim() : "";

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Thiếu thông tin");
            err.setHeaderText(null);
            err.setContentText("Vui lòng nhập đầy đủ Họ tên, Số điện thoại và Địa chỉ giao hàng.");
            err.showAndWait();
            return;
        }

        int qty = docSoLuong();
        if (sachChon.getTonKho() <= 0) {
            new Alert(Alert.AlertType.WARNING, "Sản phẩm đã hết hàng.").showAndWait();
            capNhatTrangThaiTonKho();
            return;
        }
        if (qty > sachChon.getTonKho()) {
            qty = sachChon.getTonKho();
        }
        double total = sachChon.getGiaBan() * qty;

        String user = UserSession.getUsername();
        ObservableList<CartController.Order> userOrders = CustomerAccountStore.getOrders(user);
        String randomStr = Long.toHexString(Double.doubleToLongBits(Math.random())).substring(0, 4).toUpperCase();
        String orderId = "#ORD-" + (userOrders.size() + 1) + "-" + randomStr;
        LocalDateTime placedAt = LocalDateTime.now();

        String purchasedItems = OrderStockUtil.formatOrderLine(
                sachChon.getId(), sachChon.getTenSach(), qty, moneyFmt.format(total));
        userOrders.add(0, new CartController.Order(orderId, user, purchasedItems, placedAt, total, "Chờ xác nhận"));
        if (lblTonKhoDetail != null) {
            lblTonKhoDetail.setText(sachChon.getTonKho() > 0 ? (sachChon.getTonKho() + " cuốn") : "Đã hết hàng");
        }
        capNhatTrangThaiTonKho();

        if (vboxQuickCheckout != null) {
            vboxQuickCheckout.setVisible(false);
            vboxQuickCheckout.setManaged(false);
        }
        if (lblOrderReceipt != null) {
            lblOrderReceipt.setText(
                    "Mã đơn: " + orderId + "\n"
                            + "Sách: " + sachChon.getTenSach() + " × " + qty + "\n"
                            + "Người nhận: " + name + "\n"
                            + "Điện thoại: " + phone + "\n"
                            + "Địa chỉ: " + address + "\n"
                            + "Ngày: " + placedAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + "   Tháng/Năm: " + placedAt.format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy"))
                            + "   Giờ: " + placedAt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "\n"
                            + "Tổng thanh toán: " + moneyFmt.format(total) + "\n"
                            + "Trạng thái: Chờ xác nhận"
            );
        }
        if (vboxOrderPlaced != null) {
            vboxOrderPlaced.setVisible(true);
            vboxOrderPlaced.setManaged(true);
        }

        Alert done = new Alert(Alert.AlertType.INFORMATION);
        done.setTitle("Đặt hàng");
        done.setHeaderText(null);
        done.setContentText("Đặt hàng thành công! Chi tiết đơn hàng hiển thị bên dưới.");
        done.showAndWait();
    }

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

    @FXML
    public void moTrangGioHang() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gio-hang.fxml"));
            chiTietRoot.getScene().setRoot(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
