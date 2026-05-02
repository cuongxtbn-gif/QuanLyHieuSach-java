package org.example;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartController {

    @FXML private StackPane rootPane;
    @FXML private Hyperlink linkLogin; // Nút hiển thị tên hoặc "Đăng nhập"
    @FXML private TextField searchField;

    // Giỏ hàng
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, Boolean> colSelect;
    @FXML private TableColumn<CartItem, String> colProduct;
    @FXML private TableColumn<CartItem, Double> colPrice;
    @FXML private TableColumn<CartItem, Integer> colQuantity;
    @FXML private TableColumn<CartItem, Double> colTotal;
    @FXML private TableColumn<CartItem, Void> colAction;

    // Thanh toán & Voucher
    @FXML private HBox voucherContainer;
    @FXML private Button btnApplyVoucher;
    @FXML private TextField txtCouponCode;
    @FXML private CheckBox checkSelectAllBottom;
    @FXML private Label lblTotalPrice;
    @FXML private Button btnShowCheckout;

    // Lịch sử đơn hàng
    @FXML private TableView<Order> trackingTable;
    @FXML private TableColumn<Order, String> colOrderId;
    @FXML private TableColumn<Order, String> colOrderDate;
    @FXML private TableColumn<Order, String> colOrderMonth;
    @FXML private TableColumn<Order, String> colOrderTime;
    @FXML private TableColumn<Order, Double> colOrderTotal;
    @FXML private TableColumn<Order, String> colStatus;

    // Modal Đặt Hàng
    @FXML private VBox modalOverlay;
    @FXML private Button btnCloseModal;
    @FXML private Label lblModalTotal;
    @FXML private TextField txtName, txtPhone;
    @FXML private TextArea txtAddress;
    @FXML private RadioButton radioCOD, radioQR;
    @FXML private VBox qrArea;
    @FXML private Button btnConfirmOrder;

    // ================= BIẾN TRẠNG THÁI =================
    private ObservableList<CartItem> cartList = FXCollections.observableArrayList();
    private ObservableList<Order> orderList = FXCollections.observableArrayList();
    private boolean isVoucherApplied = false;
    private DiscountCoupon appliedCoupon;
    private double currentTotalToPay = 0;
    private final DecimalFormat formatter = new DecimalFormat("#,###đ");

    @FXML
    public void initialize() {
        // Tích hợp UserSession để hiển thị tên nếu đã đăng nhập
        if (UserSession.isLogged()) {
            if(linkLogin != null) linkLogin.setText("👤 " + UserSession.getUsername());
            cartList = CustomerAccountStore.getCart(UserSession.getUsername());
            orderList = CustomerAccountStore.getOrders(UserSession.getUsername());
        }

        setupCartTable();
        setupTrackingTable();
        setupEvents();
        calculateTotal();
    }

    // ================= CÁC HÀM CHUYỂN TRANG =================
    @FXML
    public void quayVeTrangChu() {
        chuyenTrang("/hello-view.fxml");
    }

    @FXML
    public void moTrangDanhSachSach() {
        chuyenTrang("/danh-muc-sach.fxml");
    }

    @FXML
    public void handleLoginClick() {
        if (!UserSession.isLogged()) {
            chuyenTrang("/dang-nhap.fxml");
        } else {
            // Xử lý đăng xuất nếu cần (giống DanhMucSachController)
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận đăng xuất");
            alert.setHeaderText(null);
            alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

            java.util.Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                UserSession.clear();
                chuyenTrang("/dang-nhap.fxml");
            }
        }
    }

    private void chuyenTrang(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            rootPane.getScene().setRoot(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ================= CẤU HÌNH BẢNG GIỎ HÀNG =================
    private void setupCartTable() {
        colProduct.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());

        colPrice.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        colPrice.setCellFactory(column -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : formatter.format(price));
            }
        });

        colSelect.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colSelect.setCellFactory(column -> new TableCell<CartItem, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(e -> {
                    getTableView().getItems().get(getIndex()).setSelected(checkBox.isSelected());
                    calculateTotal();
                    checkSelectAllBottom.setSelected(isAllSelected());
                });
            }
            @Override
            protected void updateItem(Boolean selected, boolean empty) {
                super.updateItem(selected, empty);
                if (empty) { setGraphic(null); }
                else { checkBox.setSelected(selected); setGraphic(checkBox); setAlignment(Pos.CENTER); }
            }
        });

        colQuantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        colQuantity.setCellFactory(column -> new TableCell<CartItem, Integer>() {
            private final Button btnMinus = new Button("-");
            private final Button btnPlus = new Button("+");
            private final Label lblQty = new Label();
            private final HBox pane = new HBox(5, btnMinus, lblQty, btnPlus);
            {
                pane.setAlignment(Pos.CENTER);
                btnMinus.setOnAction(e -> updateQty(-1));
                btnPlus.setOnAction(e -> updateQty(1));
            }
            private void updateQty(int change) {
                CartItem item = getTableView().getItems().get(getIndex());
                int newQty = item.getQuantity() + change;
                if (newQty > 0) {
                    item.setQuantity(newQty);
                    getTableView().refresh();
                    calculateTotal();
                }
            }
            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty) { setGraphic(null); }
                else { lblQty.setText(String.valueOf(qty)); setGraphic(pane); }
            }
        });

        colTotal.setCellValueFactory(cellData -> cellData.getValue().totalPriceProperty().asObject());
        colTotal.setCellFactory(column -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty) { setText(null); }
                else { setText(formatter.format(total)); setStyle("-fx-text-fill: #e11d48; -fx-font-weight: bold;"); }
            }
        });

        colAction.setCellFactory(column -> new TableCell<CartItem, Void>() {
            private final Button btnDelete = new Button("Xóa");
            {
                btnDelete.setStyle("-fx-background-color: #fca5a5; -fx-cursor: hand;");
                btnDelete.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cartList.remove(item);
                    calculateTotal();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); }
                else { setGraphic(btnDelete); setAlignment(Pos.CENTER); }
            }
        });

        cartTable.setItems(cartList);
    }

    // ================= CẤU HÌNH BẢNG THEO DÕI ĐƠN HÀNG =================
    private void setupTrackingTable() {
        colOrderId.setCellValueFactory(cellData -> cellData.getValue().orderIdProperty());
        colOrderDate.setCellValueFactory(cellData -> cellData.getValue().orderDateProperty());
        colOrderMonth.setCellValueFactory(cellData -> cellData.getValue().orderMonthProperty());
        colOrderTime.setCellValueFactory(cellData -> cellData.getValue().orderTimeProperty());

        colOrderTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());
        colOrderTotal.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty) { setText(null); }
                else { setText(formatter.format(price)); setStyle("-fx-text-fill: #e11d48; -fx-font-weight: bold;"); }
            }
        });
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        trackingTable.setItems(orderList);
    }

    // ================= XỬ LÝ SỰ KIỆN =================
    private void setupEvents() {
        ToggleGroup paymentGroup = new ToggleGroup();
        radioCOD.setToggleGroup(paymentGroup);
        radioQR.setToggleGroup(paymentGroup);

        paymentGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            qrArea.setVisible(radioQR.isSelected());
            qrArea.setManaged(radioQR.isSelected());
        });

        btnApplyVoucher.setOnAction(e -> {
            if (!UserSession.isLogged()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng đăng nhập để dùng voucher!");
                return;
            }
            if (getSelectedItemsCount() == 0) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn sản phẩm trước khi áp dụng voucher!");
                return;
            }
            String code = txtCouponCode == null ? "" : txtCouponCode.getText().trim();
            if (code.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập mã giảm giá.");
                return;
            }

            DiscountCoupon found = findCoupon(code);
            if (found == null || !found.activeProperty().get()) {
                showAlert(Alert.AlertType.ERROR, "Không hợp lệ", "Mã giảm giá không tồn tại hoặc đã ngừng hoạt động.");
                return;
            }
            if (!isCouponEligible(found)) {
                showAlert(Alert.AlertType.WARNING, "Không đủ điều kiện", "Tài khoản hoặc điều kiện đơn hàng chưa phù hợp mã này.");
                return;
            }

            appliedCoupon = found;
            isVoucherApplied = true;
            btnApplyVoucher.setText("Đã áp dụng (" + found.discountPercentProperty().get() + "%)");
            btnApplyVoucher.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
            calculateTotal();
        });

        checkSelectAllBottom.setOnAction(e -> {
            boolean isChecked = checkSelectAllBottom.isSelected();
            cartList.forEach(item -> item.setSelected(isChecked));
            cartTable.refresh();
            calculateTotal();
        });

        btnShowCheckout.setOnAction(e -> {
            if (!UserSession.isLogged()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bạn cần đăng nhập để tiến hành đặt hàng!");
                chuyenTrang("/dang-nhap.fxml");
                return;
            }
            if (getSelectedItemsCount() == 0) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn ít nhất 1 sản phẩm!");
                return;
            }
            lblModalTotal.setText("Tổng tiền cần thanh toán: " + formatter.format(currentTotalToPay));
            modalOverlay.setVisible(true);
        });

        btnCloseModal.setOnAction(e -> modalOverlay.setVisible(false));
        btnConfirmOrder.setOnAction(e -> processCheckout());

        if (searchField != null) {
            searchField.setOnAction(e -> {
                CatalogSearchBridge.setPendingQuery(searchField.getText());
                moTrangDanhSachSach();
            });
        }
    }

    private void calculateTotal() {
        double total = 0;
        int count = 0;

        for (CartItem item : cartList) {
            if (item.isSelected()) {
                total += item.getTotalPrice();
                count++;
            }
        }

        if (count == 0 && isVoucherApplied) {
            isVoucherApplied = false;
            appliedCoupon = null;
            btnApplyVoucher.setText("Áp dụng");
            btnApplyVoucher.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        }

        if (isVoucherApplied && total > 0) {
            double percent = appliedCoupon == null ? 0 : appliedCoupon.discountPercentProperty().get();
            double discount = total * (percent / 100.0);
            currentTotalToPay = total - discount;
            lblTotalPrice.setText(String.format("Tổng thanh toán (%d sản phẩm): %s (Đã giảm %.1f%%)", count, formatter.format(currentTotalToPay), percent));
        } else {
            currentTotalToPay = total;
            lblTotalPrice.setText(String.format("Tổng thanh toán (%d sản phẩm): %s", count, formatter.format(total)));
        }
    }

    private void processCheckout() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ Tên, Số điện thoại và Địa chỉ!");
            return;
        }

        String user = UserSession.getUsername();
        String randomStr = Long.toHexString(Double.doubleToLongBits(Math.random())).substring(0, 4).toUpperCase();
        ObservableList<Order> userOrders = CustomerAccountStore.getOrders(user);
        String orderId = "#ORD-" + (userOrders.size() + 1) + "-" + randomStr;
        LocalDateTime placedAt = LocalDateTime.now();
        String purchasedItems = buildPurchasedItemsSummary();

        userOrders.add(0, new Order(orderId, user, purchasedItems, placedAt, currentTotalToPay, "Chờ xác nhận"));

        List<CartItem> itemsToRemove = new ArrayList<>();
        for (CartItem item : cartList) {
            if (item.isSelected()) itemsToRemove.add(item);
        }
        cartList.removeAll(itemsToRemove);

        if (isVoucherApplied) {
            isVoucherApplied = false;
            appliedCoupon = null;
            if (txtCouponCode != null) {
                txtCouponCode.clear();
            }
            btnApplyVoucher.setText("Áp dụng");
            btnApplyVoucher.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        }
        checkSelectAllBottom.setSelected(false);
        calculateTotal();

        modalOverlay.setVisible(false);
        txtName.clear(); txtPhone.clear(); txtAddress.clear();

        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đặt hàng thành công! Đơn hàng sẽ được chuyển đến Admin.");
    }

    private boolean isAllSelected() {
        if (cartList.isEmpty()) return false;
        return cartList.stream().allMatch(CartItem::isSelected);
    }

    private int getSelectedItemsCount() {
        return (int) cartList.stream().filter(CartItem::isSelected).count();
    }

    private String buildPurchasedItemsSummary() {
        List<String> lines = new ArrayList<>();
        for (CartItem item : cartList) {
            if (item.isSelected()) {
                lines.add(item.productNameProperty().get() + " x" + item.getQuantity() + " - " + formatter.format(item.getTotalPrice()));
            }
        }
        return String.join(" | ", lines);
    }

    private DiscountCoupon findCoupon(String code) {
        for (DiscountCoupon coupon : CustomerAccountStore.getCoupons()) {
            if (coupon.codeProperty().get().equalsIgnoreCase(code)) {
                return coupon;
            }
        }
        return null;
    }

    private boolean isCouponEligible(DiscountCoupon coupon) {
        String target = safeLower(coupon.targetUsersProperty().get());
        String username = safeLower(UserSession.getUsername());
        if (target.contains("mới")) {
            if (!CustomerAccountStore.getOrders(UserSession.getUsername()).isEmpty()) {
                return false;
            }
        } else if (target.contains("vip")) {
            if (!username.contains("vip")) {
                return false;
            }
        }

        double selectedTotal = 0;
        for (CartItem item : cartList) {
            if (item.isSelected()) {
                selectedTotal += item.getTotalPrice();
            }
        }
        double minOrder = parseMinOrder(coupon.conditionsProperty().get());
        return selectedTotal >= minOrder;
    }

    private double parseMinOrder(String conditions) {
        if (conditions == null) return 0;
        String lower = conditions.toLowerCase(Locale.ROOT);
        if (!lower.contains("đơn từ")) return 0;
        String digits = conditions.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String safeLower(String input) {
        return input == null ? "" : input.toLowerCase(Locale.ROOT);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ================= CÁC LỚP MÔ HÌNH DỮ LIỆU (Đã bổ sung) =================

    public static class CartItem {
        private final StringProperty productName;
        private final DoubleProperty price;
        private final IntegerProperty quantity;
        private final BooleanProperty selected;

        public CartItem(String productName, double price, int quantity) {
            this.productName = new SimpleStringProperty(productName);
            this.price = new SimpleDoubleProperty(price);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.selected = new SimpleBooleanProperty(false);
        }

        public StringProperty productNameProperty() { return productName; }
        public DoubleProperty priceProperty() { return price; }
        public IntegerProperty quantityProperty() { return quantity; }
        public BooleanProperty selectedProperty() { return selected; }

        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int quantity) { this.quantity.set(quantity); }

        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean selected) { this.selected.set(selected); }

        public ReadOnlyDoubleProperty totalPriceProperty() {
            return new SimpleDoubleProperty(price.get() * quantity.get());
        }
        public double getTotalPrice() { return price.get() * quantity.get(); }
    }

    public static class Order {
        private final StringProperty orderId;
        private final StringProperty username;
        private final StringProperty purchasedItems;
        private final StringProperty orderDate;
        private final StringProperty orderMonth;
        private final StringProperty orderTime;
        private final DoubleProperty total;
        private final StringProperty status;

        public Order(String orderId, String username, String purchasedItems, LocalDateTime placedAt, double total, String status) {
            this.orderId = new SimpleStringProperty(orderId);
            this.username = new SimpleStringProperty(username);
            this.purchasedItems = new SimpleStringProperty(purchasedItems);
            DateTimeFormatter day = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter month = DateTimeFormatter.ofPattern("MM/yyyy");
            DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm:ss");
            this.orderDate = new SimpleStringProperty(placedAt.format(day));
            this.orderMonth = new SimpleStringProperty(placedAt.format(month));
            this.orderTime = new SimpleStringProperty(placedAt.format(time));
            this.total = new SimpleDoubleProperty(total);
            this.status = new SimpleStringProperty(status);
        }

        /** Khôi phục từ file lưu trữ (đăng nhập lại). */
        public Order(String orderId, String username, String purchasedItems, String orderDate, String orderMonth, String orderTime, double total, String status) {
            this.orderId = new SimpleStringProperty(orderId);
            this.username = new SimpleStringProperty(username);
            this.purchasedItems = new SimpleStringProperty(purchasedItems);
            this.orderDate = new SimpleStringProperty(orderDate);
            this.orderMonth = new SimpleStringProperty(orderMonth);
            this.orderTime = new SimpleStringProperty(orderTime);
            this.total = new SimpleDoubleProperty(total);
            this.status = new SimpleStringProperty(status);
        }

        public StringProperty orderIdProperty() { return orderId; }
        public StringProperty usernameProperty() { return username; }
        public StringProperty purchasedItemsProperty() { return purchasedItems; }
        public StringProperty orderDateProperty() { return orderDate; }
        public StringProperty orderMonthProperty() { return orderMonth; }
        public StringProperty orderTimeProperty() { return orderTime; }
        public DoubleProperty totalProperty() { return total; }
        public StringProperty statusProperty() { return status; }
    }
}