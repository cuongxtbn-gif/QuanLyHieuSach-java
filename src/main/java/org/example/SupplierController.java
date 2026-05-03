package org.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.text.DecimalFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SupplierController {

    @FXML private Label lblTotalSuppliers, lblTotalAmount;
    @FXML private TextField txtSearch;
    @FXML private TableView<SupplierInvoiceStore.Supplier> tableSuppliers;
    @FXML private TableColumn<SupplierInvoiceStore.Supplier, String> colId, colName, colPhone, colEmail, colTotal, colStatus, colActions;

    @FXML private StackPane modalSupplierForm;
    @FXML private Label lblModalFormTitle;
    @FXML private TextField txtModalId, txtModalName, txtModalPhone, txtModalEmail, txtModalStatus;

    @FXML private StackPane modalInvoiceList, modalInvoiceDetail;
    @FXML private Label lblModalInvoiceTitle, lblDetailTitle, lblDetailTotal;
    @FXML private TableView<SupplierInvoiceStore.Invoice> tableInvoices;
    @FXML private TableColumn<SupplierInvoiceStore.Invoice, String> colInvId, colInvDate, colInvCount, colInvTotal, colInvAction;
    @FXML private TableView<SupplierInvoiceStore.InvoiceItem> tableInvoiceDetail;
    @FXML private TableColumn<SupplierInvoiceStore.InvoiceItem, String> colDetId, colDetTitle, colDetPrice, colDetQty, colDetTotal;

    @FXML private StackPane modalAddInvoice;
    @FXML private DatePicker dpInvoiceDate;
    @FXML private ComboBox<BookOption> cbBookSelect;
    @FXML private TextField txtBookQty;
    @FXML private Label lblCartTotal;
    @FXML private TableView<SupplierInvoiceStore.InvoiceItem> tableCart;
    @FXML private TableColumn<SupplierInvoiceStore.InvoiceItem, String> colCartTitle, colCartPrice, colCartQty, colCartTotal, colCartAction;

    private ObservableList<SupplierInvoiceStore.Supplier> supplierList;
    private ObservableList<SupplierInvoiceStore.Invoice> invoiceList;
    private ObservableList<SupplierInvoiceStore.InvoiceItem> cartList = FXCollections.observableArrayList();
    private DecimalFormat formatter = new DecimalFormat("###,###,### đ");

    private String editingSupplierId = null;
    private String currentViewSupplierId = null;

    private String removeAccents(String str) {
        if (str == null) return "";
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    @FXML
    public void initialize() {
        supplierList = SupplierInvoiceStore.getSuppliers();
        invoiceList = SupplierInvoiceStore.getInvoices();
        setupTableColumns();

        FilteredList<SupplierInvoiceStore.Supplier> filteredData = new FilteredList<>(supplierList, b -> true);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(supplier -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                String unaccentedFilter = removeAccents(lowerCaseFilter);
                String unaccentedName = removeAccents(supplier.getName().toLowerCase());
                String unaccentedId = removeAccents(supplier.getId().toLowerCase());

                return supplier.getName().toLowerCase().contains(lowerCaseFilter)
                        || supplier.getId().toLowerCase().contains(lowerCaseFilter)
                        || unaccentedName.contains(unaccentedFilter)
                        || unaccentedId.contains(unaccentedFilter);
            });
            updateStats(filteredData);
        });
        tableSuppliers.setItems(filteredData);
        updateStats(filteredData);
        tableCart.setItems(cartList);
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(formatter.format(c.getValue().getTotalPaid())));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("📑 HĐ");
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDelete = new Button("Xóa");
            private final HBox pane = new HBox(5, btnView, btnEdit, btnDelete);
            {
                btnView.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 11px;");
                btnEdit.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px;");
                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px;");
                btnView.setOnAction(e -> showInvoices(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(e -> showEditSupplierModal(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> deleteSupplier(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : pane);
            }
        });

        colInvId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colInvDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate()));
        colInvCount.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getItems().size() + " loại sách"));
        colInvTotal.setCellValueFactory(c -> new SimpleStringProperty(formatter.format(c.getValue().getAmount())));
        colInvAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDetail = new Button("👁️ Chi tiết");
            {
                btnDetail.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                btnDetail.setOnAction(e -> showInvoiceDetail(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : btnDetail);
            }
        });

        colDetId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookId()));
        colDetTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colDetPrice.setCellValueFactory(c -> new SimpleStringProperty(formatter.format(c.getValue().getPrice())));
        colDetQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        colDetTotal.setCellValueFactory(c -> new SimpleStringProperty(formatter.format(c.getValue().getTotal())));

        colCartTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colCartPrice.setCellValueFactory(c -> new SimpleStringProperty(formatter.format(c.getValue().getPrice())));
        colCartQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        colCartTotal.setCellValueFactory(c -> new SimpleStringProperty(formatter.format(c.getValue().getTotal())));
        colCartAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDel = new Button("X");
            {
                btnDel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
                btnDel.setOnAction(e -> {
                    cartList.remove(getTableView().getItems().get(getIndex()));
                    updateCartTotal();
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : btnDel);
            }
        });
    }

    private void updateStats(List<SupplierInvoiceStore.Supplier> list) {
        lblTotalSuppliers.setText(String.valueOf(list.size()));
        double sum = list.stream().mapToDouble(SupplierInvoiceStore.Supplier::getTotalPaid).sum();
        lblTotalAmount.setText(formatter.format(sum));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content);
        alert.setTitle(title); alert.setHeaderText(null); alert.show();
    }

    // =========================================================
    // HÀM XỬ LÝ GIAO DIỆN MODAL NHÀ CUNG CẤP
    // =========================================================

    @FXML public void closeModals() {
        modalSupplierForm.setVisible(false);
        modalInvoiceList.setVisible(false);
        modalInvoiceDetail.setVisible(false);
    }
    @FXML public void closeDetailModal() { modalInvoiceDetail.setVisible(false); }
    @FXML public void closeAddInvoiceModal() { modalAddInvoice.setVisible(false); }

    @FXML public void showAddSupplierModal() {
        editingSupplierId = null;
        lblModalFormTitle.setText("Thêm Nhà Phân Phối");
        txtModalId.setText(""); txtModalId.setDisable(false);
        txtModalName.setText(""); txtModalPhone.setText(""); txtModalEmail.setText("");
        txtModalStatus.setText("Đang hợp tác");
        modalSupplierForm.setVisible(true);
    }

    private void showEditSupplierModal(SupplierInvoiceStore.Supplier s) {
        editingSupplierId = s.getId();
        lblModalFormTitle.setText("Cập nhật Thông tin");
        txtModalId.setText(s.getId()); txtModalId.setDisable(true);
        txtModalName.setText(s.getName()); txtModalPhone.setText(s.getPhone()); txtModalEmail.setText(s.getEmail());
        txtModalStatus.setText(s.getStatus());
        modalSupplierForm.setVisible(true);
    }

    @FXML public void saveSupplier() {
        String inputId = txtModalId.getText().trim();
        String inputName = txtModalName.getText().trim();

        if(inputId.isEmpty() || inputName.isEmpty()) {
            showAlert("Lỗi", "Mã và Tên không được để trống!"); return;
        }

        if (editingSupplierId == null) {
            boolean exists = supplierList.stream().anyMatch(s -> s.getId().equalsIgnoreCase(inputId));
            if (exists) {
                showAlert("Lỗi", "Mã Nhà phân phối này đã tồn tại trong hệ thống!"); return;
            }
            supplierList.add(new SupplierInvoiceStore.Supplier(inputId, inputName, txtModalPhone.getText(), txtModalEmail.getText(), txtModalStatus.getText()));
        } else {
            for (SupplierInvoiceStore.Supplier s : supplierList) {
                if (s.getId().equals(editingSupplierId)) {
                    s.setName(inputName); s.setPhone(txtModalPhone.getText()); s.setEmail(txtModalEmail.getText()); s.setStatus(txtModalStatus.getText());
                    break;
                }
            }
        }
        tableSuppliers.refresh();
        SupplierInvoiceStore.persistNow();
        closeModals();
    }

    private void deleteSupplier(SupplierInvoiceStore.Supplier s) {
        if (s.getTotalPaid() > 0) {
            showAlert("Lỗi", "Không thể xóa đối tác đã có giao dịch. Hãy chuyển trạng thái sang Tạm dừng!"); return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "LƯU Ý: Chức năng này chỉ dành cho việc xóa khi thêm nhầm đối tác.\n\nBạn có chắc chắn muốn xóa vĩnh viễn \"" + s.getName() + "\" không?",
                ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText(null);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                supplierList.remove(s);
                updateStats(supplierList);
            }
        });
    }

    private void showInvoices(SupplierInvoiceStore.Supplier s) {
        currentViewSupplierId = s.getId();
        lblModalInvoiceTitle.setText("Hóa Đơn - " + s.getName());
        ObservableList<SupplierInvoiceStore.Invoice> filtered = FXCollections.observableArrayList();
        for (SupplierInvoiceStore.Invoice inv : invoiceList) {
            if (inv.getSupplierId().equals(s.getId())) {
                filtered.add(inv);
            }
        }
        tableInvoices.setItems(filtered);
        modalInvoiceList.setVisible(true);
    }

    private void showInvoiceDetail(SupplierInvoiceStore.Invoice inv) {
        lblDetailTitle.setText("Chi tiết HĐ: " + inv.getId());
        tableInvoiceDetail.setItems(FXCollections.observableArrayList(inv.getItems()));
        lblDetailTotal.setText("Tổng cộng: " + formatter.format(inv.getAmount()));
        modalInvoiceDetail.setVisible(true);
    }

    @FXML public void showAddInvoiceModal() {
        cartList.clear();
        updateCartTotal();
        dpInvoiceDate.setValue(LocalDate.now());
        txtBookQty.setText("10");

        ObservableList<BookOption> options = FXCollections.observableArrayList();
        // Yêu cầu: danh sách nhập kho phải có tất cả sách trong danh mục
        for (Sach s : BookCatalog.getAllBooks()) {
            if (s.isDeleted()) {
                continue;
            }
            options.add(new BookOption(s));
        }
        cbBookSelect.setItems(options);
        modalAddInvoice.setVisible(true);
    }

    @FXML public void addToCart() {
        BookOption opt = cbBookSelect.getValue();
        if(opt == null) { showAlert("Cảnh báo", "Vui lòng chọn sách!"); return; }
        int qty = 0;
        try { qty = Integer.parseInt(txtBookQty.getText()); } catch(Exception e) { showAlert("Lỗi", "Số lượng không hợp lệ!"); return; }
        if(qty <= 0) { showAlert("Lỗi", "Số lượng phải lớn hơn 0!"); return; }

        boolean found = false;
        for (SupplierInvoiceStore.InvoiceItem it : cartList) {
            if(it.getBookId().equals(opt.sach.getId())) {
                it.setQuantity(it.getQuantity() + qty);
                it.setTotal(it.getQuantity() * it.getPrice());
                found = true; break;
            }
        }
        if(!found) {
            cartList.add(new SupplierInvoiceStore.InvoiceItem(opt.sach.getId(), opt.sach.getTenSach(), opt.importPrice, qty, opt.importPrice * qty));
        }
        tableCart.refresh();
        updateCartTotal();
    }

    private void updateCartTotal() {
        double total = cartList.stream().mapToDouble(SupplierInvoiceStore.InvoiceItem::getTotal).sum();
        lblCartTotal.setText(formatter.format(total));
    }

    @FXML public void saveNewInvoice() {
        if(cartList.isEmpty()) { showAlert("Cảnh báo", "Vui lòng chọn ít nhất 1 cuốn sách để nhập!"); return; }

        double total = cartList.stream().mapToDouble(SupplierInvoiceStore.InvoiceItem::getTotal).sum();
        String date = dpInvoiceDate.getValue() != null ? dpInvoiceDate.getValue().toString() : LocalDate.now().toString();

        List<SupplierInvoiceStore.InvoiceItem> savedItems = new ArrayList<>(cartList);

        for (SupplierInvoiceStore.Supplier s : supplierList) {
            if (s.getId().equals(currentViewSupplierId)) {
                s.addTotal(total);
                break;
            }
        }

        String newId = SupplierInvoiceStore.nextInvoiceId();
        invoiceList.add(new SupplierInvoiceStore.Invoice(newId, currentViewSupplierId, date, total, savedItems));

        // Cộng tồn kho: mỗi item nhập về sẽ cộng thêm vào số lượng sẵn có
        for (SupplierInvoiceStore.InvoiceItem it : savedItems) {
            BookCatalog.findById(it.getBookId()).ifPresent(s -> s.setTonKho(s.getTonKho() + it.getQuantity()));
        }
        BookCatalog.persistNow();

        tableSuppliers.refresh();
        updateStats(supplierList);
        showAlert("Thành công", "Nhập kho thành công!\nĐã tạo Hóa Đơn: " + newId);

        closeAddInvoiceModal();
        for (SupplierInvoiceStore.Supplier s : supplierList) {
            if(s.getId().equals(currentViewSupplierId)) { showInvoices(s); break; }
        }
    }

    // =========================================================
    // LỚP PHỤ (COMBO SÁCH NHẬP KHO)
    // =========================================================

    public static class BookOption {
        public Sach sach; public double importPrice;
        public BookOption(Sach s) { this.sach = s; this.importPrice = s.getGiaBan() * 0.65; }
        @Override public String toString() {
            DecimalFormat df = new DecimalFormat("###,###,### đ");
            return sach.getTenSach() + " (Bán: " + df.format(sach.getGiaBan()) + " ➡️ Nhập: " + df.format(importPrice) + ")";
        }
    }
}