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
import java.util.*;

public class SupplierController {

    @FXML private Label lblTotalSuppliers, lblTotalAmount;
    @FXML private TextField txtSearch;
    @FXML private TableView<Supplier> tableSuppliers;
    @FXML private TableColumn<Supplier, String> colId, colName, colPhone, colEmail, colTotal, colStatus, colActions;

    @FXML private StackPane modalSupplierForm;
    @FXML private Label lblModalFormTitle;
    @FXML private TextField txtModalId, txtModalName, txtModalPhone, txtModalEmail, txtModalStatus;

    @FXML private StackPane modalInvoiceList, modalInvoiceDetail;
    @FXML private Label lblModalInvoiceTitle, lblDetailTitle, lblDetailTotal;
    @FXML private TableView<Invoice> tableInvoices;
    @FXML private TableColumn<Invoice, String> colInvId, colInvDate, colInvCount, colInvTotal, colInvAction;
    @FXML private TableView<InvoiceItem> tableInvoiceDetail;
    @FXML private TableColumn<InvoiceItem, String> colDetId, colDetTitle, colDetPrice, colDetQty, colDetTotal;

    @FXML private StackPane modalAddInvoice;
    @FXML private DatePicker dpInvoiceDate;
    @FXML private ComboBox<BookOption> cbBookSelect;
    @FXML private TextField txtBookQty;
    @FXML private Label lblCartTotal;
    @FXML private TableView<InvoiceItem> tableCart;
    @FXML private TableColumn<InvoiceItem, String> colCartTitle, colCartPrice, colCartQty, colCartTotal, colCartAction;

    private ObservableList<Supplier> supplierList = FXCollections.observableArrayList();
    private ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();
    private ObservableList<InvoiceItem> cartList = FXCollections.observableArrayList();
    private List<Book> bookDatabase = new ArrayList<>();
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
        initBookDatabase();
        initSuppliersAndInvoices();
        setupTableColumns();

        FilteredList<Supplier> filteredData = new FilteredList<>(supplierList, b -> true);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(supplier -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                String unaccentedFilter = removeAccents(lowerCaseFilter);
                String unaccentedName = removeAccents(supplier.getName().toLowerCase());
                String unaccentedId = removeAccents(supplier.getId().toLowerCase());

                return supplier.getName().toLowerCase().contains(lowerCaseFilter) ||
                        supplier.getId().toLowerCase().contains(lowerCaseFilter) ||
                        unaccentedName.contains(unaccentedFilter) ||
                        unaccentedId.contains(unaccentedFilter);
            });
            updateStats(filteredData);
        });
        tableSuppliers.setItems(filteredData);
        updateStats(filteredData);
        tableCart.setItems(cartList);
    }

    private void initBookDatabase() {
        bookDatabase.add(new Book("ba-tuoc-monte-cristo", "Bá tước Monte Cristo", 185000, "Văn học"));
        bookDatabase.add(new Book("giet-con-chim-nhai", "Giết con chim nhại", 210000, "Văn học"));
        bookDatabase.add(new Book("hai-so-phan", "Hai số phận", 320000, "Văn học"));
        bookDatabase.add(new Book("khong-gia-dinh", "Không gia đình", 450000, "Văn học"));
        bookDatabase.add(new Book("phia-sau-nghi-can-x", "Phía sau nghi can X", 230000, "Trinh thám"));
        bookDatabase.add(new Book("rung-nauy", "Rừng Nauy", 240000, "Văn học"));
        bookDatabase.add(new Book("bo-gia", "Bố Già", 105000, "Văn học"));
        bookDatabase.add(new Book("nhung-nguoi-khon-kho", "Những người khốn khổ", 285000, "Văn học"));
        bookDatabase.add(new Book("phia-tay-khong-co-gi-la", "Phía tây không có gì lạ", 50000, "Văn học"));
        bookDatabase.add(new Book("tuoi-tre-dang-gia-bao-nhieu", "Tuổi trẻ đáng giá bao nhiêu", 80000, "Kỹ năng"));
        bookDatabase.add(new Book("cay-cam-ngot-cua-toi", "Cây Cam Ngọt Của Tôi", 81000, "Văn học"));
        bookDatabase.add(new Book("khong-diet-khong-sinh-dung-so-hai", "Không Diệt Không Sinh Đừng Sợ Hãi", 82500, "Kỹ năng"));
        bookDatabase.add(new Book("khi-hoi-tho-hoa-thinh-khong", "Khi Hơi Thở Hóa Thinh Không", 81750, "Văn học"));
        bookDatabase.add(new Book("dan-ong-sao-hoa-dan-ba-sao-kim", "Đàn Ông Sao Hỏa Đàn Bà Sao Kim", 122000, "Kỹ năng"));
        bookDatabase.add(new Book("muon-kiep-nhan-sinh", "Muôn Kiếp Nhân Sinh", 117600, "Văn học"));
        bookDatabase.add(new Book("tieng-han-tong-hop-so-cap-1", "Tiếng Hàn Tổng Hợp - Sơ Cấp 1", 148500, "Ngoại ngữ"));
        bookDatabase.add(new Book("dac-nhan-tam", "Đắc Nhân Tâm", 56000, "Kỹ năng"));
        bookDatabase.add(new Book("day-con-lam-giau-01", "Dạy Con Làm Giàu 01", 62000, "Kinh tế"));
        bookDatabase.add(new Book("hieu-ve-trai-tim", "Hiểu Về Trái Tim", 119000, "Kỹ năng"));
        bookDatabase.add(new Book("ngon-ngu-co-the", "Ngôn Ngữ Cơ Thể", 158000, "Kỹ năng"));
        bookDatabase.add(new Book("cam-on-nguoi-lon", "Cảm Ơn Người Lớn", 84700, "Văn học"));
        bookDatabase.add(new Book("thay-doi-cuoc-song-voi-nhan-so-hoc", "Thay Đổi Cuộc Sống Với Nhân Số Học", 173600, "Kỹ năng"));
        bookDatabase.add(new Book("khoi-nghiep-ban-le", "Khởi Nghiệp Bán Lẻ", 125000, "Kinh tế"));
        bookDatabase.add(new Book("muon-kiep-nhan-sinh-tap-2", "Muôn Kiếp Nhân Sinh - Tập 2", 187600, "Văn học"));
        bookDatabase.add(new Book("lam-ban-voi-bau-troi", "Làm Bạn Với Bầu Trời", 169400, "Văn học"));
        bookDatabase.add(new Book("tam-ly-hoc-toi-pham", "Tâm Lý Học Tội Phạm", 94000, "Tâm lý"));
        bookDatabase.add(new Book("thien-tai-ben-trai-ke-dien-ben-phai", "Thiên Tài Bên Trái, Kẻ Điên Bên Phải", 116000, "Tâm lý"));
        bookDatabase.add(new Book("tam-ly-hoc-ve-tien", "Tâm Lý Học Về Tiền", 141500, "Tâm lý"));
        bookDatabase.add(new Book("cay-chuoi-non-di-giay-xanh", "Cây Chuối Non Đi Giày Xanh", 84700, "Văn học"));
        bookDatabase.add(new Book("tu-hoc-tieng-trung-cho-nguoi-moi-bat-dau", "Tự Học Tiếng Trung Cho Người Mới Bắt Đầu", 85500, "Ngoại ngữ"));
        bookDatabase.add(new Book("ong-tram-tuoi-treo-qua-cua-so-va-bien-mat", "Ông Trăm Tuổi Trèo Qua Cửa Sổ Và Biến Mất", 146300, "Văn học"));
        bookDatabase.add(new Book("nguoi-dua-dieu", "Người Đua Diều", 146300, "Văn học"));
        bookDatabase.add(new Book("think-and-grow-rich", "Think And Grow Rich", 77000, "Kinh tế"));
        bookDatabase.add(new Book("nguoi-giau-co-nhat-thanh-babylon", "Người Giàu Có Nhất Thành Babylon", 74000, "Kinh tế"));
        bookDatabase.add(new Book("lam-giau-tu-chung-khoan", "Làm Giàu Từ Chứng Khoán", 700000, "Kinh tế"));
        bookDatabase.add(new Book("ghi-chep-phap-y-nhung-cai-chet-bi-an", "Ghi Chép Pháp Y", 112500, "Trinh thám"));
        bookDatabase.add(new Book("kheo-an-noi-se-co-duoc-thien-ha", "Khéo Ăn Nói Sẽ Có Được Thiên Hạ", 118000, "Tâm lý"));
        bookDatabase.add(new Book("nha-gia-kim", "Nhà giả kim", 100000, "Văn học"));
        bookDatabase.add(new Book("90-tre-thong-minh-nho-tro-chuyen", "90% Trẻ Thông Minh Nhờ Trò Chuyện", 33000, "Kỹ năng"));
        bookDatabase.add(new Book("bi-mat-cua-phan-thien-an", "Bí Mật Của Phan Thiên Ân", 69000, "Kỹ năng"));
        bookDatabase.add(new Book("co-hai-con-meo-ngoi-ben-cua-so", "Có Hai Con Mèo Ngồi Bên Cửa Sổ", 77000, "Văn học"));
        bookDatabase.add(new Book("hanh-tinh-cua-mot-ke-nghi-nhieu", "Hành Tinh Của Một Kẻ Nghĩ Nhiều", 56000, "Tâm lý"));
        bookDatabase.add(new Book("tu-dien-tieng-em", "Từ Điển Tiếng 'Em'", 55000, "Văn học"));
        bookDatabase.add(new Book("di-tim-le-song", "Đi Tìm Lẽ Sống", 62000, "Tâm lý"));
        bookDatabase.add(new Book("cho-toi-xin-mot-ve-di-tuoi-tho", "Cho Tôi Xin Một Vé Đi Tuổi Thơ", 69300, "Văn học"));
        bookDatabase.add(new Book("bien-moi-thu-thanh-tien", "Biến Mọi Thứ Thành Tiền", 109000, "Kinh tế"));
        bookDatabase.add(new Book("combo-sach-english", "Combo sách English", 85500, "Ngoại ngữ"));
        bookDatabase.add(new Book("lam-giau-tu-chung-khoan-bo-2-cuon", "Làm Giàu Từ Chứng Khoán", 700000, "Kinh tế"));
        bookDatabase.add(new Book("khong-so-cham-chi-so-dung", "Không Sợ Chậm Chỉ Sợ Dừng", 84000, "Kỹ năng"));
        bookDatabase.add(new Book("vi-than-cua-nhung-quyet-dinh", "Vị Thần Của Những Quyết Định", 51000, "Kỹ năng"));
        bookDatabase.add(new Book("tho-bay-mau-va-nhung-nguoi-nghi-no-la-ban", "Thỏ Bảy Màu", 79000, "Văn học"));
        bookDatabase.add(new Book("muon-kiep-nhan-sinh-tap-3", "Muôn Kiếp Nhân Sinh 3", 152600, "Văn học"));
        bookDatabase.add(new Book("to-binh-yen-ve-hanh-phuc", "Tô Bình Yên Vẽ Hạnh Phúc", 70000, "Kỹ năng"));
        bookDatabase.add(new Book("thuat-thao-tung", "Thuật Thao Túng", 89600, "Kỹ năng"));
        bookDatabase.add(new Book("khong-phai-soi-nhung-cung-dung-la-cuu", "Không Phải Sói...", 92000, "Văn học"));
        bookDatabase.add(new Book("noi-chuyen-la-ban-nang-giu-mieng-la-tu-duong", "Nói Chuyện Là Bản Năng", 123000, "Kỹ năng"));
    }

    private void initSuppliersAndInvoices() {
        supplierList.add(new Supplier("NCC-001", "Nhà sách Fahasa", "1900 636467", "info@fahasa.com", "Đang hợp tác"));
        supplierList.add(new Supplier("NCC-002", "Công ty Nhã Nam", "0243 514 6876", "nhanam@nhanam.vn", "Đang hợp tác"));
        supplierList.add(new Supplier("NCC-003", "Alphabooks", "0243 722 6234", "info@alphabooks.vn", "Đang hợp tác"));
        supplierList.add(new Supplier("NCC-004", "Tiki Trading", "1900 6035", "hotro@tiki.vn", "Đang hợp tác"));

        Random rand = new Random();
        for (Supplier ncc : supplierList) {
            List<Book> filteredBooks = new ArrayList<>();
            for (Book b : bookDatabase) {
                if (ncc.getId().equals("NCC-001") && b.getCategory().equals("Văn học")) filteredBooks.add(b);
                else if (ncc.getId().equals("NCC-002") && b.getCategory().equals("Kỹ năng")) filteredBooks.add(b);
                else if (ncc.getId().equals("NCC-003") && b.getCategory().equals("Kinh tế")) filteredBooks.add(b);
                else if (ncc.getId().equals("NCC-004") && Arrays.asList("Tâm lý", "Trinh thám", "Ngoại ngữ").contains(b.getCategory())) filteredBooks.add(b);
            }

            if (!filteredBooks.isEmpty()) {
                double totalHD = 0;
                List<InvoiceItem> items = new ArrayList<>();
                for (Book s : filteredBooks) {
                    int sl = rand.nextInt(51) + 10;
                    double giaNhap = s.getPrice() * 0.65;
                    double thanhTien = giaNhap * sl;
                    totalHD += thanhTien;
                    items.add(new InvoiceItem(s.getId(), s.getTitle(), giaNhap, sl, thanhTien));
                }
                ncc.addTotal(totalHD);
                invoiceList.add(new Invoice("HD-" + (rand.nextInt(9000)+1000), ncc.getId(), LocalDate.now().toString(), totalHD, items));
            }
        }
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

    private void updateStats(List<Supplier> list) {
        lblTotalSuppliers.setText(String.valueOf(list.size()));
        double sum = list.stream().mapToDouble(Supplier::getTotalPaid).sum();
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

    private void showEditSupplierModal(Supplier s) {
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
            supplierList.add(new Supplier(inputId, inputName, txtModalPhone.getText(), txtModalEmail.getText(), txtModalStatus.getText()));
        } else {
            for (Supplier s : supplierList) {
                if (s.getId().equals(editingSupplierId)) {
                    s.setName(inputName); s.setPhone(txtModalPhone.getText()); s.setEmail(txtModalEmail.getText()); s.setStatus(txtModalStatus.getText());
                    break;
                }
            }
        }
        tableSuppliers.refresh();
        closeModals();
    }

    private void deleteSupplier(Supplier s) {
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

    private void showInvoices(Supplier s) {
        currentViewSupplierId = s.getId();
        lblModalInvoiceTitle.setText("Hóa Đơn - " + s.getName());
        ObservableList<Invoice> filtered = FXCollections.observableArrayList();
        for (Invoice inv : invoiceList) if (inv.getSupplierId().equals(s.getId())) filtered.add(inv);
        tableInvoices.setItems(filtered);
        modalInvoiceList.setVisible(true);
    }

    private void showInvoiceDetail(Invoice inv) {
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
        for(Book b : bookDatabase) {
            if(currentViewSupplierId.equals("NCC-001") && b.getCategory().equals("Văn học")) options.add(new BookOption(b));
            else if(currentViewSupplierId.equals("NCC-002") && b.getCategory().equals("Kỹ năng")) options.add(new BookOption(b));
            else if(currentViewSupplierId.equals("NCC-003") && b.getCategory().equals("Kinh tế")) options.add(new BookOption(b));
            else if(currentViewSupplierId.equals("NCC-004") && Arrays.asList("Tâm lý", "Trinh thám", "Ngoại ngữ").contains(b.getCategory())) options.add(new BookOption(b));
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
        for(InvoiceItem it : cartList) {
            if(it.getBookId().equals(opt.book.getId())) {
                it.setQuantity(it.getQuantity() + qty);
                it.setTotal(it.getQuantity() * it.getPrice());
                found = true; break;
            }
        }
        if(!found) {
            cartList.add(new InvoiceItem(opt.book.getId(), opt.book.getTitle(), opt.importPrice, qty, opt.importPrice * qty));
        }
        tableCart.refresh();
        updateCartTotal();
    }

    private void updateCartTotal() {
        double total = cartList.stream().mapToDouble(InvoiceItem::getTotal).sum();
        lblCartTotal.setText(formatter.format(total));
    }

    @FXML public void saveNewInvoice() {
        if(cartList.isEmpty()) { showAlert("Cảnh báo", "Vui lòng chọn ít nhất 1 cuốn sách để nhập!"); return; }

        double total = cartList.stream().mapToDouble(InvoiceItem::getTotal).sum();
        String randomId = "HD-" + (new Random().nextInt(9000) + 1000);
        String date = dpInvoiceDate.getValue() != null ? dpInvoiceDate.getValue().toString() : LocalDate.now().toString();

        List<InvoiceItem> savedItems = new ArrayList<>(cartList);
        Invoice newInv = new Invoice(randomId, currentViewSupplierId, date, total, savedItems);
        invoiceList.add(newInv);

        for(Supplier s : supplierList) {
            if(s.getId().equals(currentViewSupplierId)) { s.addTotal(total); break; }
        }

        tableSuppliers.refresh();
        updateStats(supplierList);
        showAlert("Thành công", "Nhập kho thành công!\nĐã tạo Hóa Đơn: " + randomId);

        closeAddInvoiceModal();
        for(Supplier s : supplierList) {
            if(s.getId().equals(currentViewSupplierId)) { showInvoices(s); break; }
        }
    }

    // =========================================================
    // CÁC LỚP DATA MODEL (SÁCH, NHÀ CUNG CẤP, HÓA ĐƠN)
    // =========================================================

    public static class Book {
        private String id, title, category; private double price;
        public Book(String id, String t, double p, String c) { this.id=id; this.title=t; this.price=p; this.category=c; }
        public String getId() { return id; } public String getTitle() { return title; }
        public double getPrice() { return price; } public String getCategory() { return category; }
    }

    public static class BookOption {
        public Book book; public double importPrice;
        public BookOption(Book b) { this.book = b; this.importPrice = b.getPrice() * 0.65; }
        @Override public String toString() {
            DecimalFormat df = new DecimalFormat("###,###,### đ");
            return book.getTitle() + " (Bán: " + df.format(book.getPrice()) + " ➡️ Nhập: " + df.format(importPrice) + ")";
        }
    }

    public static class Supplier {
        private String id, name, phone, email, status; private double totalPaid;
        public Supplier(String i, String n, String p, String e, String s) { id=i; name=n; phone=p; email=e; status=s; totalPaid=0;}
        public String getId() { return id; } public String getName() { return name; }
        public String getPhone() { return phone; } public String getEmail() { return email; }
        public String getStatus() { return status; } public double getTotalPaid() { return totalPaid; }
        public void setName(String n) { name=n; } public void setPhone(String p) { phone=p; }
        public void setEmail(String e) { email=e; } public void setStatus(String s) { status=s; }
        public void addTotal(double amt) { totalPaid += amt; }
    }

    public static class Invoice {
        private String id, supplierId, date; private double amount; private List<InvoiceItem> items;
        public Invoice(String i, String s, String d, double a, List<InvoiceItem> it) { id=i; supplierId=s; date=d; amount=a; items=it;}
        public String getId() { return id; } public String getSupplierId() { return supplierId; }
        public String getDate() { return date; } public double getAmount() { return amount; }
        public List<InvoiceItem> getItems() { return items; }
    }

    public static class InvoiceItem {
        private String bookId, title; private double price, total; private int quantity;
        public InvoiceItem(String b, String t, double p, int q, double to) { bookId=b; title=t; price=p; quantity=q; total=to;}
        public String getBookId() { return bookId; } public String getTitle() { return title; }
        public double getPrice() { return price; } public int getQuantity() { return quantity; }
        public double getTotal() { return total; }
        public void setQuantity(int q) { this.quantity = q; }
        public void setTotal(double t) { this.total = t; }
    }
}