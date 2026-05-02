package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Locale;

public class MaGiamGiaController {

    @FXML private Label lblActiveCount;
    @FXML private Label lblExpiredCount;

    @FXML private TableView<DiscountCoupon> tblActiveVouchers;
    @FXML private TableColumn<DiscountCoupon, String> colActiveCode;
    @FXML private TableColumn<DiscountCoupon, Number> colActivePercent;
    @FXML private TableColumn<DiscountCoupon, Number> colActiveQty;
    @FXML private TableColumn<DiscountCoupon, String> colActiveTarget;
    @FXML private TableColumn<DiscountCoupon, String> colActiveCondition;

    @FXML private TableView<DiscountCoupon> tblExpiredVouchers;
    @FXML private TableColumn<DiscountCoupon, String> colExpiredCode;
    @FXML private TableColumn<DiscountCoupon, Number> colExpiredPercent;
    @FXML private TableColumn<DiscountCoupon, Number> colExpiredQty;
    @FXML private TableColumn<DiscountCoupon, String> colExpiredTarget;
    @FXML private TableColumn<DiscountCoupon, String> colExpiredCondition;

    @FXML private TextField txtCode;
    @FXML private TextField txtDescription;
    @FXML private TextField txtDiscountPercent;
    @FXML private TextField txtTargetUsers;
    @FXML private TextField txtConditions;
    @FXML private TextField txtQuantity;
    @FXML private CheckBox chkActive;

    private final ObservableList<DiscountCoupon> activeCoupons = FXCollections.observableArrayList();
    private final ObservableList<DiscountCoupon> expiredCoupons = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTables();
        refreshData();
    }

    @FXML
    private void handleAddVoucher() {
        String code = readText(txtCode).toUpperCase(Locale.ROOT);
        String desc = readText(txtDescription);
        String target = readText(txtTargetUsers);
        String cond = readText(txtConditions);

        double percent;
        int quantity;
        try {
            percent = Double.parseDouble(readText(txtDiscountPercent));
            quantity = Integer.parseInt(readText(txtQuantity));
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", "Phần trăm giảm và số lượng phải là số.");
            return;
        }

        if (code.isBlank() || percent <= 0 || quantity < 0) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Mã, phần trăm giảm và số lượng cần hợp lệ.");
            return;
        }

        boolean active = chkActive.isSelected() && quantity > 0;
        DiscountCoupon coupon = new DiscountCoupon(code, desc, percent, target, cond, quantity, active);
        if (!CustomerAccountStore.addCoupon(coupon)) {
            showAlert(Alert.AlertType.ERROR, "Trùng mã", "Mã giảm giá đã tồn tại.");
            return;
        }

        clearForm();
        refreshData();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm mã giảm giá mới.");
    }

    @FXML
    private void handleExpireSelected() {
        DiscountCoupon selected = tblActiveVouchers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn voucher", "Vui lòng chọn voucher đang hoạt động để chuyển.");
            return;
        }
        selected.activeProperty().set(false);
        CustomerAccountStore.saveNow();
        refreshData();
    }

    @FXML
    private void handleReactivateSelected() {
        DiscountCoupon selected = tblExpiredVouchers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn voucher", "Vui lòng chọn voucher đã hết hạn để kích hoạt.");
            return;
        }
        if (selected.quantityProperty().get() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Không thể kích hoạt", "Voucher hết hạn do đã hết số lượng, hãy tạo mã mới.");
            return;
        }
        selected.activeProperty().set(true);
        CustomerAccountStore.saveNow();
        refreshData();
    }

    private void setupTables() {
        colActiveCode.setCellValueFactory(cell -> cell.getValue().codeProperty());
        colActivePercent.setCellValueFactory(cell -> cell.getValue().discountPercentProperty());
        colActiveQty.setCellValueFactory(cell -> cell.getValue().quantityProperty());
        colActiveTarget.setCellValueFactory(cell -> cell.getValue().targetUsersProperty());
        colActiveCondition.setCellValueFactory(cell -> cell.getValue().conditionsProperty());

        colExpiredCode.setCellValueFactory(cell -> cell.getValue().codeProperty());
        colExpiredPercent.setCellValueFactory(cell -> cell.getValue().discountPercentProperty());
        colExpiredQty.setCellValueFactory(cell -> cell.getValue().quantityProperty());
        colExpiredTarget.setCellValueFactory(cell -> cell.getValue().targetUsersProperty());
        colExpiredCondition.setCellValueFactory(cell -> cell.getValue().conditionsProperty());

        tblActiveVouchers.setItems(activeCoupons);
        tblExpiredVouchers.setItems(expiredCoupons);
    }

    private void refreshData() {
        activeCoupons.clear();
        expiredCoupons.clear();
        for (DiscountCoupon coupon : CustomerAccountStore.getCoupons()) {
            boolean isActive = coupon.activeProperty().get() && coupon.quantityProperty().get() > 0;
            if (isActive) {
                activeCoupons.add(coupon);
            } else {
                expiredCoupons.add(coupon);
            }
        }
        lblActiveCount.setText(String.valueOf(activeCoupons.size()));
        lblExpiredCount.setText(String.valueOf(expiredCoupons.size()));
    }

    private String readText(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }

    private void clearForm() {
        txtCode.clear();
        txtDescription.clear();
        txtDiscountPercent.clear();
        txtTargetUsers.clear();
        txtConditions.clear();
        txtQuantity.clear();
        chkActive.setSelected(true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}