package org.example;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminDonHangController {
    @FXML private Label lblPendingCount;
    @FXML private Label lblApprovedCount;
    @FXML private TableView<PendingOrderRow> tblPendingOrders;
    @FXML private TableView<CartController.Order> tblApprovedOrders;
    @FXML private TableColumn<PendingOrderRow, Boolean> colPendingCheck;
    @FXML private TableColumn<PendingOrderRow, String> colPendingId;
    @FXML private TableColumn<PendingOrderRow, String> colPendingDate;
    @FXML private TableColumn<PendingOrderRow, String> colPendingCustomer;
    @FXML private TableColumn<PendingOrderRow, Double> colPendingTotal;
    @FXML private TableColumn<PendingOrderRow, String> colPendingStatus;
    @FXML private TableColumn<CartController.Order, String> colApprovedId;
    @FXML private TableColumn<CartController.Order, String> colApprovedDate;
    @FXML private TableColumn<CartController.Order, String> colApprovedCustomer;
    @FXML private TableColumn<CartController.Order, Double> colApprovedTotal;
    @FXML private TableColumn<CartController.Order, String> colApprovedStatus;
    @FXML private Label lblDetailOrderId;
    @FXML private Label lblDetailCustomer;
    @FXML private Label lblDetailDate;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailItems;

    private final DecimalFormat formatter = new DecimalFormat("#,###đ");
    private final ObservableList<CartController.Order> allOrders = FXCollections.observableArrayList();
    private final ObservableList<PendingOrderRow> pendingRows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        allOrders.setAll(CustomerAccountStore.getAllOrders());
        refreshTables();

        tblPendingOrders.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldRow, newRow) -> showOrderDetail(newRow == null ? null : newRow.getOrder()));
        tblApprovedOrders.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldOrder, newOrder) -> showOrderDetail(newOrder));
    }

    @FXML
    private void handleApproveOrder() {
        List<PendingOrderRow> selectedRows = pendingRows.stream()
                .filter(PendingOrderRow::isSelected)
                .collect(Collectors.toList());
        if (selectedRows.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Bạn chưa tích chọn đơn hàng nào để duyệt.");
            return;
        }

        int success = 0;
        List<String> failures = new ArrayList<>();
        for (PendingOrderRow row : selectedRows) {
            CartController.Order order = row.getOrder();
            Optional<String> stockErr = OrderStockUtil.validateStockForOrder(order.purchasedItemsProperty().get());
            if (stockErr.isPresent()) {
                failures.add(order.orderIdProperty().get() + ": " + stockErr.get());
                continue;
            }
            boolean ok = CustomerAccountStore.updateOrderStatus(
                    order.orderIdProperty().get(),
                    order.usernameProperty().get(),
                    "Đã duyệt"
            );
            if (!ok) {
                failures.add(order.orderIdProperty().get() + ": không cập nhật được trạng thái.");
                continue;
            }
            order.statusProperty().set("Đã duyệt");
            OrderStockUtil.applyDeductStockForOrder(order.purchasedItemsProperty().get());
            success++;
        }

        refreshTables();
        if (success > 0 && failures.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Đã duyệt thành công " + success + " đơn hàng.");
        } else if (success > 0) {
            showAlert(Alert.AlertType.WARNING,
                    "Đã duyệt " + success + " đơn.\n\nKhông duyệt được:\n" + String.join("\n", failures));
        } else if (!failures.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Không duyệt được đơn nào:\n" + String.join("\n", failures));
        } else {
            showAlert(Alert.AlertType.ERROR, "Không thể duyệt các đơn đã chọn.");
        }
    }

    @FXML
    private void handleCancelOrder() {
        List<PendingOrderRow> selectedRows = pendingRows.stream()
                .filter(PendingOrderRow::isSelected)
                .collect(Collectors.toList());
        if (selectedRows.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Bạn chưa tích chọn đơn hàng nào để hủy.");
            return;
        }

        int success = 0;
        for (PendingOrderRow row : selectedRows) {
            CartController.Order order = row.getOrder();
            boolean ok = CustomerAccountStore.updateOrderStatus(
                    order.orderIdProperty().get(),
                    order.usernameProperty().get(),
                    "Đã hủy"
            );
            if (ok) {
                order.statusProperty().set("Đã hủy");
                success++;
            }
        }

        refreshTables();
        if (success > 0) {
            showAlert(Alert.AlertType.INFORMATION, "Đã hủy " + success + " đơn hàng.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Không thể hủy các đơn đã chọn.");
        }
    }

    @FXML
    private void handleSelectAllPending() {
        pendingRows.forEach(row -> row.setSelected(true));
        tblPendingOrders.refresh();
    }

    @FXML
    private void handleClearPendingSelection() {
        pendingRows.forEach(row -> row.setSelected(false));
        tblPendingOrders.refresh();
    }

    private void setupColumns() {
        colPendingCheck.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        colPendingCheck.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(e -> {
                    PendingOrderRow row = getTableRow() == null ? null : (PendingOrderRow) getTableRow().getItem();
                    if (row != null) {
                        row.setSelected(checkBox.isSelected());
                    }
                });
            }

            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(Boolean.TRUE.equals(value));
                    setGraphic(checkBox);
                }
            }
        });
        colPendingId.setCellValueFactory(cell -> cell.getValue().getOrder().orderIdProperty());
        colPendingDate.setCellValueFactory(cell -> cell.getValue().getOrder().orderDateProperty());
        colPendingCustomer.setCellValueFactory(cell -> cell.getValue().getOrder().usernameProperty());
        colPendingTotal.setCellValueFactory(cell -> cell.getValue().getOrder().totalProperty().asObject());
        colPendingStatus.setCellValueFactory(cell -> cell.getValue().getOrder().statusProperty());

        colApprovedId.setCellValueFactory(cell -> cell.getValue().orderIdProperty());
        colApprovedDate.setCellValueFactory(cell -> cell.getValue().orderDateProperty());
        colApprovedCustomer.setCellValueFactory(cell -> cell.getValue().usernameProperty());
        colApprovedTotal.setCellValueFactory(cell -> cell.getValue().totalProperty().asObject());
        colApprovedStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());

        colPendingTotal.setCellFactory(column -> pendingMoneyCell());
        colApprovedTotal.setCellFactory(column -> moneyCell());
    }

    private TableCell<PendingOrderRow, Double> pendingMoneyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : formatter.format(value));
            }
        };
    }

    private TableCell<CartController.Order, Double> moneyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : formatter.format(value));
            }
        };
    }

    private void refreshTables() {
        pendingRows.clear();
        ObservableList<CartController.Order> approved = FXCollections.observableArrayList();

        for (CartController.Order order : allOrders) {
            String status = order.statusProperty().get();
            if ("Chờ xác nhận".equals(status)) {
                pendingRows.add(new PendingOrderRow(order));
            } else if ("Đã duyệt".equals(status)) {
                approved.add(order);
            }
        }

        tblPendingOrders.setItems(pendingRows);
        tblApprovedOrders.setItems(approved);
        lblPendingCount.setText(String.valueOf(pendingRows.size()));
        lblApprovedCount.setText(String.valueOf(approved.size()));
    }

    private void showOrderDetail(CartController.Order order) {
        if (order == null) {
            return;
        }
        lblDetailOrderId.setText(order.orderIdProperty().get());
        String customer = order.usernameProperty().get();
        lblDetailCustomer.setText((customer == null || customer.isBlank()) ? "Khách chưa định danh" : customer);
        lblDetailDate.setText(order.orderDateProperty().get() + " " + order.orderTimeProperty().get());
        lblDetailStatus.setText(order.statusProperty().get());
        String purchased = order.purchasedItemsProperty().get();
        lblDetailItems.setText((purchased == null || purchased.isBlank()) ? "Không có dữ liệu mặt hàng." : purchased.replace(" | ", "\n"));
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class PendingOrderRow {
        private final CartController.Order order;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);

        public PendingOrderRow(CartController.Order order) {
            this.order = order;
        }

        public CartController.Order getOrder() {
            return order;
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean value) {
            selected.set(value);
        }
    }
}
