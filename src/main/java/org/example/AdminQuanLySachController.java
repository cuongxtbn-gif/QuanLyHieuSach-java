package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.Optional;

public class AdminQuanLySachController {

    @FXML private Label lblTieuDeBang;
    @FXML private Button btnThemSach;
    @FXML private TextField txtTimKiem;
    @FXML private ComboBox<String> cbTheLoai;
    @FXML private ToggleButton btnThungRac;

    @FXML private TableView<Sach> tableSach;
    @FXML private TableColumn<Sach, String> colBiaSach;
    @FXML private TableColumn<Sach, String> colTenSach;
    @FXML private TableColumn<Sach, String> colTacGia;
    @FXML private TableColumn<Sach, String> colTheLoai;
    @FXML private TableColumn<Sach, Double> colGiaBan;
    @FXML private TableColumn<Sach, Integer> colTonKho;
    @FXML private TableColumn<Sach, Void> colHanhDong;

    @FXML private Pagination pagination;

    // Dữ liệu gốc
    private ObservableList<Sach> masterData = FXCollections.observableArrayList();
    private FilteredList<Sach> filteredData;
    private final int ROWS_PER_PAGE = 10;
    private final DecimalFormat moneyFmt = new DecimalFormat("#,### đ");

    @FXML
    public void initialize() {
        // Nạp thể loại vào ComboBox
        cbTheLoai.getItems().addAll("Tất cả", "Văn học", "Kỹ năng", "Kinh tế", "Trinh thám", "Tâm lý", "Ngoại ngữ");
        cbTheLoai.getSelectionModel().selectFirst();

        setupColumns();
        loadDuLieuTuKho();
        setupFiltersAndPagination();

        // Lắng nghe nút Thùng rác
        btnThungRac.selectedProperty().addListener((obs, oldVal, isTrashMode) -> {
            chuyenCheDoThungRac(isTrashMode);
            updateFilter();
        });
    }

    private void loadDuLieuTuKho() {
        // TODO: Lấy dữ liệu thực tế. Ở đây tôi giả sử bạn lấy từ BookCatalog
        masterData.clear();
        masterData.addAll(BookCatalog.getAllBooks()); // Thay bằng hàm lấy danh sách sách của bạn

        filteredData = new FilteredList<>(masterData, p -> !isDeleted(p));
        updateTrashCount();
    }

    private void setupFiltersAndPagination() {
        // Lắng nghe ô tìm kiếm và combobox để lọc realtime
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        cbTheLoai.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());

        // Phân trang:
        // Không trả TableView vào Pagination (Node chỉ có 1 parent -> UI bị co, chỉ thấy 1 dòng).
        // Pagination chỉ điều khiển pageIndex, TableView vẫn nằm độc lập trong layout.
        pagination.setPageFactory(pageIndex -> {
            updateTableForPage(pageIndex);
            return new javafx.scene.layout.Pane();
        });
        updatePagination(true);
    }

    private void updateFilter() {
        boolean isTrashMode = btnThungRac.isSelected();
        String searchText = txtTimKiem.getText() == null ? "" : txtTimKiem.getText().toLowerCase();
        String category = cbTheLoai.getValue();

        filteredData.setPredicate(sach -> {
            // Lọc theo chế độ (Kho hay Thùng rác)
            if (isDeleted(sach) != isTrashMode) return false;

            // Lọc theo thể loại
            if (!"Tất cả".equals(category) && sach.getTheLoai() != null && !sach.getTheLoai().equals(category)) {
                return false;
            }

            // Lọc theo từ khóa (Tên hoặc Tác giả)
            if (searchText.isEmpty()) return true;
            String ten = sach.getTenSach() != null ? sach.getTenSach().toLowerCase() : "";
            String tacGia = sach.getTacGia() != null ? sach.getTacGia().toLowerCase() : "";

            return ten.contains(searchText) || tacGia.contains(searchText);
        });

        updatePagination(true);
    }

    private void updatePagination(boolean resetToFirstPage) {
        int pageCount = (int) Math.ceil((double) filteredData.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        if (resetToFirstPage) {
            pagination.setCurrentPageIndex(0);
            updateTableForPage(0);
        } else {
            int safeIndex = Math.min(Math.max(0, pagination.getCurrentPageIndex()), pagination.getPageCount() - 1);
            if (safeIndex != pagination.getCurrentPageIndex()) pagination.setCurrentPageIndex(safeIndex);
            updateTableForPage(safeIndex);
        }
    }

    // Cắt list dữ liệu để hiển thị theo trang (10 quyển / trang)
    private void updateTableForPage(int pageIndex) {
        if (filteredData == null || filteredData.isEmpty()) {
            tableSach.setItems(FXCollections.observableArrayList());
            return;
        }

        int fromIndex = pageIndex * ROWS_PER_PAGE;
        if (fromIndex >= filteredData.size()) {
            fromIndex = Math.max(0, (pagination.getPageCount() - 1) * ROWS_PER_PAGE);
        }
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredData.size());

        SortedList<Sach> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredData.subList(fromIndex, toIndex))
        );
        sortedData.comparatorProperty().bind(tableSach.comparatorProperty());
        tableSach.setItems(sortedData);
    }

    private void setupColumns() {
        // Gắn cột với thuộc tính của đối tượng Sach
        colTenSach.setCellValueFactory(new PropertyValueFactory<>("tenSach"));
        colTacGia.setCellValueFactory(new PropertyValueFactory<>("tacGia"));
        colTheLoai.setCellValueFactory(new PropertyValueFactory<>("theLoai"));

        // Cột Hình ảnh
        colBiaSach.setCellValueFactory(new PropertyValueFactory<>("hinhAnh")); // Giả sử lưu URL hoặc path
        colBiaSach.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(40);
                imageView.setFitHeight(55);
                imageView.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        // Lưu ý: Nếu url là link web thì để nguyên, nếu là path local thì có thể cần "file:"
                        imageView.setImage(new Image(url, true));
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null); // Lỗi load ảnh
                    }
                }
            }
        });

        // Cột Giá
        colGiaBan.setCellValueFactory(new PropertyValueFactory<>("giaBan"));
        colGiaBan.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else {
                    setText(moneyFmt.format(price));
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        // Cột Tồn kho
        colTonKho.setCellValueFactory(new PropertyValueFactory<>("tonKho")); // Sửa thành thuộc tính tồn kho của bạn
        colTonKho.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(stock > 0 ? stock + " cuốn" : "Hết hàng");
                    lbl.setStyle(stock > 0 ?
                            "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10;" :
                            "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10;");
                    setGraphic(lbl);
                }
            }
        });

        // Cột Hành động (Sửa / Xóa / Khôi phục)
        colHanhDong.setCellFactory(param -> new TableCell<>() {
            private final Button btn1 = new Button();
            private final Button btn2 = new Button();
            private final HBox pane = new HBox(5, btn1, btn2);
            { pane.setStyle("-fx-alignment: CENTER;"); }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                Sach sach = getTableView().getItems().get(getIndex());

                if (btnThungRac.isSelected()) {
                    // Chế độ thùng rác
                    btn1.setText("🔄 Khôi phục");
                    btn1.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                    btn1.setOnAction(e -> { khuyenPhucSach(sach); });

                    btn2.setText("🧨 Xóa hẳn");
                    btn2.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-cursor: hand;");
                    btn2.setOnAction(e -> { xoaVinhVien(sach); });
                } else {
                    // Chế độ kho sách
                    btn1.setText("✏️ Sửa");
                    btn1.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");
                    btn1.setOnAction(e -> { showBookDialog(sach); });

                    btn2.setText("🗑️ Xóa");
                    btn2.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                    btn2.setOnAction(e -> { xoaTamThoi(sach); });
                }
                setGraphic(pane);
            }
        });
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ SỰ KIỆN
    // ==========================================

    @FXML
    private void moFormThemSach() {
        showBookDialog(null);
    }

    private void chuyenCheDoThungRac(boolean isTrashMode) {
        if (isTrashMode) {
            lblTieuDeBang.setText("Thùng rác (Sách đã xóa)");
            lblTieuDeBang.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-border-color: #e74c3c; -fx-border-width: 0 0 0 4; -fx-padding: 0 0 0 10;");
            btnThungRac.setText("🔙 Quay lại Kho");
            btnThemSach.setVisible(false);
        } else {
            lblTieuDeBang.setText("Kho Sách Của Bạn");
            lblTieuDeBang.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-border-color: #3498db; -fx-border-width: 0 0 0 4; -fx-padding: 0 0 0 10;");
            btnThungRac.setText("🗑️ Thùng rác (" + demThungRac() + ")");
            btnThemSach.setVisible(true);
        }
        // Ép TableView vẽ lại cột Hành động để đổi nút
        tableSach.refresh();
    }

    private void xoaTamThoi(Sach sach) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Chuyển sách '" + sach.getTenSach() + "' vào thùng rác?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            setDeleted(sach, true);
            updateFilter();
            updateTrashCount();
        }
    }

    private void khuyenPhucSach(Sach sach) {
        setDeleted(sach, false);
        updateFilter();
        updateTrashCount();
    }

    private void xoaVinhVien(Sach sach) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Xóa vĩnh viễn sách này? Không thể khôi phục!", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            masterData.remove(sach);
            // TODO: Xóa trong Database thực tế
            updateFilter();
            updateTrashCount();
        }
    }

    private void updateTrashCount() {
        if (!btnThungRac.isSelected()) {
            btnThungRac.setText("🗑️ Thùng rác (" + demThungRac() + ")");
        }
    }

    private long demThungRac() {
        return masterData.stream().filter(this::isDeleted).count();
    }

    // Helper: Tránh lỗi compile nếu class Sach chưa có isDeleted
    private boolean isDeleted(Sach sach) {
        // Tùy thuộc vào class Sach của bạn. VD: return sach.isDeleted();
        return false;
    }
    private void setDeleted(Sach sach, boolean deleted) {
        // sach.setDeleted(deleted);
        // TODO: Update Database
    }

    // ==========================================
    // MODAL THÊM / SỬA SÁCH (Sử dụng Dialog của JavaFX)
    // ==========================================
    private void showBookDialog(Sach sachToEdit) {
        Dialog<Sach> dialog = new Dialog<>();
        dialog.setTitle(sachToEdit == null ? "Thêm Sách Mới" : "Chỉnh Sửa Sách");
        dialog.setHeaderText(null);

        // Nút Lưu và Hủy
        ButtonType btnTypeLuu = new ButtonType("💾 LƯU THÔNG TIN", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnTypeLuu, ButtonType.CANCEL);

        // Tạo Form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 20, 20));

        TextField txtTen = new TextField(); txtTen.setPromptText("Tên Sách");
        TextField txtTacGia = new TextField(); txtTacGia.setPromptText("Tác Giả");
        TextField txtGia = new TextField(); txtGia.setPromptText("Giá Bán (VNĐ)");
        ComboBox<String> cbLoai = new ComboBox<>(); cbLoai.getItems().addAll("Văn học", "Kỹ năng", "Kinh tế", "Trinh thám", "Tâm lý", "Ngoại ngữ");
        TextField txtNxb = new TextField(); txtNxb.setPromptText("Nhà Xuất Bản");
        TextField txtSoTrang = new TextField(); txtSoTrang.setPromptText("Số trang");
        TextArea txtMoTa = new TextArea(); txtMoTa.setPromptText("Mô tả"); txtMoTa.setPrefRowCount(3);
        TextField txtAnh = new TextField(); txtAnh.setPromptText("Đường dẫn ảnh (VD: /assets/images/ten-anh.png)");

        grid.add(new Label("Tên Sách (*):"), 0, 0); grid.add(txtTen, 1, 0, 3, 1);
        grid.add(new Label("Tác Giả (*):"), 0, 1); grid.add(txtTacGia, 1, 1, 3, 1);
        grid.add(new Label("Giá Bán (*):"), 0, 2); grid.add(txtGia, 1, 2);
        grid.add(new Label("Số trang (*):"), 2, 2); grid.add(txtSoTrang, 3, 2);
        grid.add(new Label("Thể Loại (*):"), 0, 3); grid.add(cbLoai, 1, 3, 3, 1);
        grid.add(new Label("Nhà XB (*):"), 0, 4); grid.add(txtNxb, 1, 4, 3, 1);
        grid.add(new Label("Ảnh bìa:"), 0, 5); grid.add(txtAnh, 1, 5, 3, 1);
        grid.add(new Label("Mô tả:"), 0, 6); grid.add(txtMoTa, 1, 6, 3, 1);

        // Đổ dữ liệu nếu là Edit
        if (sachToEdit != null) {
            txtTen.setText(sachToEdit.getTenSach());
            txtTacGia.setText(sachToEdit.getTacGia());
            txtGia.setText(String.valueOf(sachToEdit.getGiaBan()));
            cbLoai.setValue(sachToEdit.getTheLoai());
            txtNxb.setText(sachToEdit.getNhaXuatBan());
            txtSoTrang.setText(String.valueOf(sachToEdit.getSoTrang()));
            txtAnh.setText(sachToEdit.getHinhAnh());
            txtMoTa.setText(sachToEdit.getMoTa());
        } else {
            cbLoai.getSelectionModel().selectFirst();
            txtSoTrang.setText("100");
        }

        dialog.getDialogPane().setContent(grid);

        // Bắt sự kiện khi bấm Lưu
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnTypeLuu) {
                try {
                    String ten = txtTen.getText() == null ? "" : txtTen.getText().trim();
                    String tacGia = txtTacGia.getText() == null ? "" : txtTacGia.getText().trim();
                    String theLoai = cbLoai.getValue();
                    String nxb = txtNxb.getText() == null ? "" : txtNxb.getText().trim();
                    String img = txtAnh.getText() == null ? "" : txtAnh.getText().trim();
                    String moTa = txtMoTa.getText() == null ? "" : txtMoTa.getText().trim();

                    if (ten.isEmpty() || tacGia.isEmpty() || theLoai == null || theLoai.isBlank() || nxb.isEmpty()) {
                        new Alert(Alert.AlertType.ERROR, "Vui lòng nhập đủ Tên sách, Tác giả, Thể loại và Nhà xuất bản.").show();
                        return null;
                    }

                    double price = Double.parseDouble(txtGia.getText().trim());
                    int soTrang = Integer.parseInt(txtSoTrang.getText().trim());
                    if (price <= 0 || soTrang <= 0) {
                        new Alert(Alert.AlertType.ERROR, "Giá bán và Số trang phải > 0.").show();
                        return null;
                    }

                    // Nếu không nhập ảnh, dùng ảnh mặc định (tránh NPE load ảnh)
                    if (img.isEmpty()) {
                        img = "/assets/images/nha-gia-kim.jpg";
                    }

                    if (sachToEdit != null) {
                        // Hiện tại class Sach chưa có setter -> coi như màn này chỉ thêm mới.
                        new Alert(Alert.AlertType.INFORMATION, "Chức năng chỉnh sửa sẽ bổ sung sau. Hiện tại chỉ hỗ trợ thêm mới.").show();
                        return null;
                    }

                    String id = taoIdSachTuTen(ten);
                    // Tránh trùng ID: thêm hậu tố -2, -3...
                    if (BookCatalog.findById(id).isPresent()) {
                        int i = 2;
                        while (BookCatalog.findById(id + "-" + i).isPresent()) i++;
                        id = id + "-" + i;
                    }

                    return new Sach(id, ten, price, img, tacGia, theLoai, nxb, soTrang, moTa);
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Vui lòng nhập đúng định dạng số cho Giá bán và Số trang.").show();
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(sach -> {
            boolean added = BookCatalog.addBook(sach);
            if (!added) {
                new Alert(Alert.AlertType.ERROR, "Không thể thêm sách (trùng mã ID hoặc dữ liệu không hợp lệ).").show();
                return;
            }

            // Cập nhật ngay bảng quản lý sách (admin)
            // Sách mới thêm mặc định hết hàng, sẽ tăng lên khi nhập kho.
            sach.setTonKho(0);
            masterData.add(sach);
            updateFilter();
        });
    }

    private String taoIdSachTuTen(String ten) {
        // slugify đơn giản: bỏ dấu, chữ thường, thay khoảng trắng bằng '-'
        String s = Normalizer.normalize(ten, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .trim();
        s = s.replaceAll("[^a-z0-9\\s-]", "");
        s = s.replaceAll("\\s+", "-");
        s = s.replaceAll("-{2,}", "-");
        if (s.isEmpty()) s = "sach-moi";
        return s;
    }
}