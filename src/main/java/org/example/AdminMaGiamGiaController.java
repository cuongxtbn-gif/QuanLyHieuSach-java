package org.example;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AdminMaGiamGiaController {
    @FXML private TableView<DiscountCoupon> tblCoupons;
    @FXML private TableColumn<DiscountCoupon, String> colCode;
    @FXML private TableColumn<DiscountCoupon, String> colDescription;
    @FXML private TableColumn<DiscountCoupon, Number> colPercent;
    @FXML private TableColumn<DiscountCoupon, String> colTargetUsers;
    @FXML private TableColumn<DiscountCoupon, String> colConditions;
    @FXML private TableColumn<DiscountCoupon, Boolean> colActive;

    @FXML private TextField txtCode;
    @FXML private TextField txtDescription;
    @FXML private TextField txtPercent;
    @FXML private TextField txtTargetUsers;
    @FXML private TextArea txtConditions;
    @FXML private CheckBox chkActive;
    @FXML private Label lblCouponDetail;

    private ObservableList<DiscountCoupon> coupons;

    @FXML
    public void initialize() {
        coupons = CustomerAccountStore.getCoupons();

        colCode.setCellValueFactory(cell -> cell.getValue().codeProperty());
        colDescription.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        colPercent.setCellValueFactory(cell -> cell.getValue().discountPercentProperty());
        colTargetUsers.setCellValueFactory(cell -> cell.getValue().targetUsersProperty());
        colConditions.setCellValueFactory(cell -> cell.getValue().conditionsProperty());
        colActive.setCellValueFactory(cell -> cell.getValue().activeProperty());

        tblCoupons.setItems(coupons);
        tblCoupons.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> fillForm(newItem));
    }

    @FXML
    private void handleAddCoupon() {
        if (txtCode.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Mã giảm giá không được để trống.");
            return;
        }
        if (findByCode(txtCode.getText().trim()) != null) {
            showAlert(Alert.AlertType.WARNING, "Mã giảm giá đã tồn tại.");
            return;
        }

        DiscountCoupon coupon = buildCouponFromForm();
        if (coupon == null) return;
        coupons.add(coupon);
        clearForm();
    }

    @FXML
    private void handleUpdateCoupon() {
        DiscountCoupon selected = tblCoupons.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Hãy chọn mã cần sửa.");
            return;
        }
        DiscountCoupon updated = buildCouponFromForm();
        if (updated == null) return;

        selected.codeProperty().set(updated.codeProperty().get());
        selected.descriptionProperty().set(updated.descriptionProperty().get());
        selected.discountPercentProperty().set(updated.discountPercentProperty().get());
        selected.targetUsersProperty().set(updated.targetUsersProperty().get());
        selected.conditionsProperty().set(updated.conditionsProperty().get());
        selected.activeProperty().set(updated.activeProperty().get());
        tblCoupons.refresh();
        CustomerAccountStore.saveNow();
    }

    @FXML
    private void handleDeleteCoupon() {
        DiscountCoupon selected = tblCoupons.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Hãy chọn mã cần xóa.");
            return;
        }
        coupons.remove(selected);
        clearForm();
    }

    private DiscountCoupon buildCouponFromForm() {
        double percent;
        try {
            percent = Double.parseDouble(txtPercent.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Phần trăm giảm phải là số hợp lệ.");
            return null;
        }
        return new DiscountCoupon(
                txtCode.getText().trim().toUpperCase(),
                txtDescription.getText().trim(),
                percent,
                txtTargetUsers.getText().trim(),
                txtConditions.getText().trim(),
                chkActive.isSelected()
        );
    }

    private DiscountCoupon findByCode(String code) {
        for (DiscountCoupon coupon : coupons) {
            if (coupon.codeProperty().get().equalsIgnoreCase(code)) {
                return coupon;
            }
        }
        return null;
    }

    private void fillForm(DiscountCoupon coupon) {
        if (coupon == null) return;
        txtCode.setText(coupon.codeProperty().get());
        txtDescription.setText(coupon.descriptionProperty().get());
        txtPercent.setText(String.valueOf(coupon.discountPercentProperty().get()));
        txtTargetUsers.setText(coupon.targetUsersProperty().get());
        txtConditions.setText(coupon.conditionsProperty().get());
        chkActive.setSelected(coupon.activeProperty().get());
        lblCouponDetail.setText(
                "Mã: " + coupon.codeProperty().get()
                        + "\nGiảm: " + coupon.discountPercentProperty().get() + "%"
                        + "\nĐối tượng: " + coupon.targetUsersProperty().get()
                        + "\nĐiều kiện: " + coupon.conditionsProperty().get()
        );
    }

    private void clearForm() {
        txtCode.clear();
        txtDescription.clear();
        txtPercent.clear();
        txtTargetUsers.clear();
        txtConditions.clear();
        chkActive.setSelected(true);
        lblCouponDetail.setText("Chọn mã giảm giá để xem chi tiết.");
    }

    private void showAlert(Alert.AlertType type, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
