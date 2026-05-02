package org.example;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AdminMaGiamGiaController {
    @FXML private Label lblTotalCoupons;
    @FXML private Label lblActiveCoupons;
    @FXML private Label lblCouponDetail;

    @FXML private TableView<CouponRow> tblCoupons;
    @FXML private TableColumn<CouponRow, String> colCode;
    @FXML private TableColumn<CouponRow, String> colDescription;
    @FXML private TableColumn<CouponRow, String> colPercent;
    @FXML private TableColumn<CouponRow, String> colTargetUsers;
    @FXML private TableColumn<CouponRow, String> colConditions;
    @FXML private TableColumn<CouponRow, String> colActive;

    @FXML private TextField txtCode;
    @FXML private TextField txtDescription;
    @FXML private TextField txtPercent;
    @FXML private TextField txtTargetUsers;
    @FXML private TextArea txtConditions;
    @FXML private CheckBox chkActive;

    private final ObservableList<CouponRow> coupons = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        seedData();
        tblCoupons.setItems(coupons);
        tblCoupons.setPlaceholder(new Label("Chua co ma giam gia nao."));
        tblCoupons.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblCoupons.refresh();
        tblCoupons.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> fillForm(newItem));
        updateStats();
        lblCouponDetail.setText("Chon ma giam gia de xem chi tiet.");
    }

    @FXML
    private void handleAddCoupon() {
        CouponRow row = buildCouponFromForm();
        if (row == null) return;

        if (findByCode(row.codeProperty().get()) != null) {
            showAlert(Alert.AlertType.WARNING, "Ma giam gia da ton tai.");
            return;
        }
        coupons.add(row);
        updateStats();
        clearForm();
        showAlert(Alert.AlertType.INFORMATION, "Da them ma giam gia moi.");
    }

    @FXML
    private void handleUpdateCoupon() {
        CouponRow selected = tblCoupons.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Hay chon ma can sua.");
            return;
        }
        CouponRow input = buildCouponFromForm();
        if (input == null) return;

        CouponRow duplicate = findByCode(input.codeProperty().get());
        if (duplicate != null && duplicate != selected) {
            showAlert(Alert.AlertType.WARNING, "Ma moi bi trung voi ma da co.");
            return;
        }

        selected.codeProperty().set(input.codeProperty().get());
        selected.descriptionProperty().set(input.descriptionProperty().get());
        selected.discountPercentProperty().set(input.discountPercentProperty().get());
        selected.targetUsersProperty().set(input.targetUsersProperty().get());
        selected.conditionsProperty().set(input.conditionsProperty().get());
        selected.activeProperty().set(input.activeProperty().get());

        tblCoupons.refresh();
        updateStats();
        fillForm(selected);
        showAlert(Alert.AlertType.INFORMATION, "Da cap nhat ma giam gia.");
    }

    @FXML
    private void handleDeleteCoupon() {
        CouponRow selected = tblCoupons.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Hay chon ma can xoa.");
            return;
        }
        coupons.remove(selected);
        updateStats();
        clearForm();
        showAlert(Alert.AlertType.INFORMATION, "Da xoa ma giam gia.");
    }

    private void setupColumns() {
        colCode.setCellValueFactory(cell -> cell.getValue().codeProperty());
        colDescription.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        colPercent.setCellValueFactory(cell -> new SimpleStringProperty(
                String.format("%.0f%%", cell.getValue().discountPercentProperty().get())
        ));
        colTargetUsers.setCellValueFactory(cell -> cell.getValue().targetUsersProperty());
        colConditions.setCellValueFactory(cell -> cell.getValue().conditionsProperty());
        colActive.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().activeProperty().get() ? "Dang hoat dong" : "Tam dung"));

        applyColumnCellStyle(colCode);
        applyColumnCellStyle(colDescription);
        applyColumnCellStyle(colPercent);
        applyColumnCellStyle(colTargetUsers);
        applyColumnCellStyle(colConditions);
        applyColumnCellStyle(colActive);
    }

    private void seedData() {
        if (!coupons.isEmpty()) return;
        coupons.add(new CouponRow("WELCOME10", "Giam 10% cho khach moi", 10, "Khach moi", "Don tu 150000d", true));
        coupons.add(new CouponRow("VIP15", "Uu dai cho thanh vien VIP", 15, "Thanh vien VIP", "Don tu 300000d", true));
        coupons.add(new CouponRow("ALL5", "Giam nhe cho moi tai khoan", 5, "Tat ca", "Don tu 50000d", true));
        coupons.add(new CouponRow("BIG25", "Khuyen mai don gia tri cao", 25, "Tat ca", "Don tu 700000d", false));
    }

    private CouponRow buildCouponFromForm() {
        String code = txtCode.getText() == null ? "" : txtCode.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Ma giam gia khong duoc de trong.");
            return null;
        }
        double percent;
        try {
            percent = Double.parseDouble(txtPercent.getText().trim());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Phan tram giam phai la so hop le.");
            return null;
        }
        if (percent <= 0 || percent > 100) {
            showAlert(Alert.AlertType.ERROR, "Phan tram giam phai trong khoang 0 - 100.");
            return null;
        }

        return new CouponRow(
                code,
                txtDescription.getText().trim(),
                percent,
                txtTargetUsers.getText().trim(),
                txtConditions.getText().trim(),
                chkActive.isSelected()
        );
    }

    private CouponRow findByCode(String code) {
        for (CouponRow row : coupons) {
            if (row.codeProperty().get().equalsIgnoreCase(code)) return row;
        }
        return null;
    }

    private void fillForm(CouponRow row) {
        if (row == null) return;
        txtCode.setText(row.codeProperty().get());
        txtDescription.setText(row.descriptionProperty().get());
        txtPercent.setText(String.valueOf(row.discountPercentProperty().get()));
        txtTargetUsers.setText(row.targetUsersProperty().get());
        txtConditions.setText(row.conditionsProperty().get());
        chkActive.setSelected(row.activeProperty().get());

        lblCouponDetail.setText(
                "Ma: " + row.codeProperty().get()
                        + "\nGiam: " + row.discountPercentProperty().get() + "%"
                        + "\nDoi tuong: " + row.targetUsersProperty().get()
                        + "\nDieu kien: " + row.conditionsProperty().get()
                        + "\nTrang thai: " + (row.activeProperty().get() ? "Dang hoat dong" : "Tam dung")
        );
    }

    private void clearForm() {
        txtCode.clear();
        txtDescription.clear();
        txtPercent.clear();
        txtTargetUsers.clear();
        txtConditions.clear();
        chkActive.setSelected(true);
        lblCouponDetail.setText("Chon ma giam gia de xem chi tiet.");
        tblCoupons.getSelectionModel().clearSelection();
    }

    private void updateStats() {
        long activeCount = coupons.stream().filter(c -> c.activeProperty().get()).count();
        lblTotalCoupons.setText(String.valueOf(coupons.size()));
        lblActiveCoupons.setText(String.valueOf(activeCount));
    }

    private void showAlert(Alert.AlertType type, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void applyColumnCellStyle(TableColumn<CouponRow, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #1f2937; -fx-font-size: 13px;");
                }
            }
        });
    }

    public static class CouponRow {
        private final StringProperty code = new SimpleStringProperty();
        private final StringProperty description = new SimpleStringProperty();
        private final DoubleProperty discountPercent = new SimpleDoubleProperty();
        private final StringProperty targetUsers = new SimpleStringProperty();
        private final StringProperty conditions = new SimpleStringProperty();
        private final BooleanProperty active = new SimpleBooleanProperty();

        public CouponRow(String code, String description, double discountPercent, String targetUsers, String conditions, boolean active) {
            this.code.set(code);
            this.description.set(description);
            this.discountPercent.set(discountPercent);
            this.targetUsers.set(targetUsers);
            this.conditions.set(conditions);
            this.active.set(active);
        }

        public StringProperty codeProperty() { return code; }
        public StringProperty descriptionProperty() { return description; }
        public DoubleProperty discountPercentProperty() { return discountPercent; }
        public StringProperty targetUsersProperty() { return targetUsers; }
        public StringProperty conditionsProperty() { return conditions; }
        public BooleanProperty activeProperty() { return active; }
    }
}
