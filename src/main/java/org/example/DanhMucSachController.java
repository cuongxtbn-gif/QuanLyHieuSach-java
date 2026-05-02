package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DanhMucSachController {

    @FXML
    private BorderPane danhMucRoot;
    @FXML
    private Hyperlink linkAccount;
    @FXML
    private TilePane gridSach;
    @FXML
    private ComboBox<String> cbSapXep;
    @FXML
    private ComboBox<String> cbTheLoai;
    @FXML private Button btnTrang1;
    @FXML private Button btnTrang2;
    @FXML private Button btnTrang3;
    @FXML private Button btnTrang4;

    private List<Sach> tatCaSach = new ArrayList<>(); // Chứa toàn bộ 46 cuốn
    private int trangHienTai = 1;
    private int soSachMoiTrang = 12; // 12 cuốn/trang sẽ vừa đẹp cho 4 trang
    @FXML
    public void initialize() {
        // 1. Cài đặt dữ liệu cho các thanh lọc (ComboBox)
        cbSapXep.getItems().addAll("Mức độ phổ biến", "Giá: Cao -> Thấp", "Giá: Thấp -> Cao");
        cbTheLoai.getItems().addAll("Tất cả", "Văn học", "Trinh thám", "Kỹ năng", "Kinh tế", "Ngoại ngữ", "Tâm lý");
        // Hiển thị tên nếu đã đăng nhập
        if (UserSession.isLogged()) {
            linkAccount.setText("👤 " + UserSession.getUsername());
        } else {
            linkAccount.setText("Đăng nhập");
        }
        // 2. Làm sạch kho sách trước khi thêm mới (tránh bị nhân đôi dữ liệu nếu load lại)
        tatCaSach.clear();

        // 3. Đổ 46 cuốn sách vào kho dữ liệu tổng
        // DANH SÁCH 46 CUỐN SÁCH ĐẦY ĐỦ 9 THUỘC TÍNH
        tatCaSach.add(new Sach("ba-tuoc-monte-cristo", "Bá tước Monte Cristo", 185000, "/assets/images/ba-tuoc-monte-cristo.jpg", "Alexandre Dumas", "Văn học", "NXB Văn Học", 800, "Hành trình trả thù đầy kịch tính của Edmond Dantès sau nhiều năm bị oan sai."));
        tatCaSach.add(new Sach("giet-con-chim-nhai", "Giết con chim nhại", 210000, "/assets/images/giet-con-chim-nhai.jpg", "Harper Lee", "Văn học", "NXB Trẻ", 420, "Một cái nhìn sâu sắc về định kiến chủng tộc qua con mắt trẻ thơ."));
        tatCaSach.add(new Sach("hai-so-phan", "Hai số phận", 320000, "/assets/images/hai-so-phan.jpg", "Jeffrey Archer", "Văn học", "NXB Hội Nhà Văn", 760, "Câu chuyện về hai người đàn ông sinh ra cùng ngày nhưng ở hai hoàn cảnh trái ngược."));
        tatCaSach.add(new Sach("khong-gia-dinh", "Không gia đình", 450000, "/assets/images/khong-gia-dinh.jpg", "Hector Malot", "Văn học", "NXB Kim Đồng", 500, "Cuộc phiêu lưu đầy nghị lực của cậu bé Remi trên khắp nước Pháp."));
        tatCaSach.add(new Sach("phia-sau-nghi-can-x", "Phía sau nghi can X", 230000, "/assets/images/phia-sau-nghi-can-x.png", "Keigo Higashino", "Trinh thám", "NXB Hội Nhà Văn", 400, "Cuộc đấu trí nghẹt thở giữa một thiên tài toán học và một điều tra viên."));
        tatCaSach.add(new Sach("rung-nauy", "Rừng Nauy", 240000, "/assets/images/rung-nauy.png", "Haruki Murakami", "Văn học", "NXB Nhã Nam", 550, "Câu chuyện về nỗi cô đơn và tình yêu của những người trẻ tại Nhật Bản."));
        tatCaSach.add(new Sach("bo-gia", "Bố Già", 105000, "/assets/images/bo-gia.png", "Mario Puzo", "Văn học", "NXB Văn Học", 600, "Tác phẩm kinh điển về thế giới ngầm Mafia và những giá trị gia đình."));
        tatCaSach.add(new Sach("nhung-nguoi-khon-kho", "Những người khốn khổ", 285000, "/assets/images/nhung-nguoi-khon-kho.png", "Victor Hugo", "Văn học", "NXB Văn Học", 1200, "Bức tranh toàn cảnh về xã hội Pháp thế kỷ 19 qua cuộc đời Jean Valjean."));
        tatCaSach.add(new Sach("phia-tay-khong-co-gi-la", "Phía tây không có gì lạ", 50000, "/assets/images/phia-tay-khong-co-gi-la.png", "Erich Maria Remarque", "Văn học", "NXB Trẻ", 300, "Sự tàn khốc của chiến tranh qua góc nhìn của những người lính trẻ."));
        tatCaSach.add(new Sach("tuoi-tre-dang-gia-bao-nhieu", "Tuổi trẻ đáng giá bao nhiêu", 80000, "/assets/images/tuoi-tre-dang-gia-bao-nhieu.png", "Rosie Nguyễn", "Kỹ năng", "NXB Hội Nhà Văn", 280, "Những lời tâm sự chân thành giúp bạn trẻ tìm thấy hướng đi cho cuộc đời."));
        tatCaSach.add(new Sach("cay-cam-ngot-cua-toi", "Cây Cam Ngọt Của Tôi", 81000, "/assets/images/cay-cam-ngot-cua-toi.png", "José Mauro de Vasconcelos", "Văn học", "NXB Hội Nhà Văn", 300, "Câu chuyện cảm động về tình bạn giữa cậu bé nghèo Zezé và cây cam ngọt."));
        tatCaSach.add(new Sach("khong-diet-khong-sinh-dung-so-hai", "Không Diệt Không Sinh Đừng Sợ Hãi", 82500, "/assets/images/q2.png", "Thích Nhất Hạnh", "Kỹ năng", "NXB Hồng Đức", 250, "Những triết lý sâu sắc để tìm thấy sự bình an trong tâm hồn."));
        tatCaSach.add(new Sach("khi-hoi-tho-hoa-thinh-khong", "Khi Hơi Thở Hóa Thinh Không", 81750, "/assets/images/q3.png", "Paul Kalanithi", "Văn học", "NXB Lao Động", 240, "Lời tự sự của một bác sĩ phẫu thuật não đối mặt với bệnh ung thư."));
        tatCaSach.add(new Sach("dan-ong-sao-hoa-dan-ba-sao-kim", "Đàn Ông Sao Hỏa Đàn Bà Sao Kim", 122000, "/assets/images/q4.png", "John Gray", "Kỹ năng", "NXB Trẻ", 450, "Cẩm nang thấu hiểu sự khác biệt giữa hai giới trong tình yêu."));
        tatCaSach.add(new Sach("muon-kiep-nhan-sinh", "Muôn Kiếp Nhân Sinh", 117600, "/assets/images/q5.png", "Nguyên Phong", "Văn học", "NXB Tổng hợp TP.HCM", 400, "Hành trình khám phá về luật nhân quả và luân hồi qua nhiều kiếp sống."));
        tatCaSach.add(new Sach("tieng-han-tong-hop-so-cap-1", "Tiếng Hàn Tổng Hợp - Sơ Cấp 1", 148500, "/assets/images/q6.png", "Nhiều Tác Giả", "Ngoại ngữ", "NXB Nhân Dân", 350, "Giáo trình căn bản dành cho những người mới bắt đầu học tiếng Hàn."));
        tatCaSach.add(new Sach("dac-nhan-tam", "Đắc Nhân Tâm", 56000, "/assets/images/dac-nhan-tam.jpg", "Dale Carnegie", "Kỹ năng", "NXB Tổng hợp TP.HCM", 320, "Nghệ thuật thu phục lòng người và giao tiếp hiệu quả."));
        tatCaSach.add(new Sach("day-con-lam-giau-01", "Dạy Con Làm Giàu 01", 62000, "/assets/images/q8.png", "Robert T. Kiyosaki", "Kinh tế", "NXB Trẻ", 380, "Những bài học cơ bản về tư duy tài chính của người giàu."));
        tatCaSach.add(new Sach("hieu-ve-trai-tim", "Hiểu Về Trái Tim", 119000, "/assets/images/q9.png", "Thích Minh Niệm", "Kỹ năng", "NXB Tổng hợp TP.HCM", 480, "Cách nhìn nhận và chuyển hóa những nỗi khổ niềm đau."));
        tatCaSach.add(new Sach("ngon-ngu-co-the", "Ngôn Ngữ Cơ Thể", 158000, "/assets/images/q10.png", "Allan Pease", "Kỹ năng", "NXB Tổng hợp TP.HCM", 400, "Bí quyết đọc vị người khác qua những cử chỉ không lời."));
        tatCaSach.add(new Sach("cam-on-nguoi-lon", "Cảm Ơn Người Lớn", 84700, "/assets/images/q11.png", "Nguyễn Nhật Ánh", "Văn học", "NXB Trẻ", 320, "Một tấm vé đi tuổi thơ tiếp theo đầy thú vị cho cả trẻ em và người lớn."));
        tatCaSach.add(new Sach("thay-doi-cuoc-song-voi-nhan-so-hoc", "Thay Đổi Cuộc Sống Với Nhân Số Học", 173600, "/assets/images/q12.png", "Lê Đỗ Quỳnh Hương", "Kỹ năng", "NXB Tổng hợp TP.HCM", 350, "Khám phá bản thân và định hướng cuộc sống qua ngày sinh."));
        tatCaSach.add(new Sach("khoi-nghiep-ban-le", "Khởi Nghiệp Bán Lẻ", 125000, "/assets/images/q13.png", "Trần Thanh Phong", "Kinh tế", "NXB Văn Hóa Văn Nghệ", 300, "Cẩm nang dành cho người muốn bắt đầu kinh doanh cửa hàng."));
        tatCaSach.add(new Sach("muon-kiep-nhan-sinh-tap-2", "Muôn Kiếp Nhân Sinh - Tập 2", 187600, "/assets/images/q14.png", "Nguyên Phong", "Văn học", "NXB Tổng hợp TP.HCM", 500, "Tiếp tục khám phá bí mật vũ trụ và sự thức tỉnh nhân sinh."));
        tatCaSach.add(new Sach("lam-ban-voi-bau-troi", "Làm Bạn Với Bầu Trời", 169400, "/assets/images/q15.png", "Nguyễn Nhật Ánh", "Văn học", "NXB Trẻ", 250, "Câu chuyện về lòng nhân hậu và cái nhìn bao dung của một cậu bé."));
        tatCaSach.add(new Sach("tam-ly-hoc-toi-pham", "Tâm Lý Học Tội Phạm", 94000, "/assets/images/q17.png", "Stanton E. Samenow", "Tâm lý", "NXB Công An Nhân Dân", 420, "Phân tích diễn biến tâm lý và hành vi của các đối tượng phạm tội."));
        tatCaSach.add(new Sach("thien-tai-ben-trai-ke-dien-ben-phai", "Thiên Tài Bên Trái, Kẻ Điên Bên Phải", 116000, "/assets/images/q18.png", "Cao Minh", "Tâm lý", "NXB Thế Giới", 400, "Ghi chép về thế giới nội tâm kỳ lạ của những bệnh nhân tâm thần."));
        tatCaSach.add(new Sach("tam-ly-hoc-ve-tien", "Tâm Lý Học Về Tiền", 141500, "/assets/images/q20.png", "Morgan Housel", "Tâm lý", "NXB Trẻ", 350, "Giải mã cách con người suy nghĩ và hành động đối với tiền bạc."));
        tatCaSach.add(new Sach("cay-chuoi-non-di-giay-xanh", "Cây Chuối Non Đi Giày Xanh", 84700, "/assets/images/q21.png", "Nguyễn Nhật Ánh", "Văn học", "NXB Trẻ", 280, "Câu chuyện dễ thương về tình bạn và rung động đầu đời ở làng quê."));
        tatCaSach.add(new Sach("tu-hoc-tieng-trung-cho-nguoi-moi-bat-dau", "Tự Học Tiếng Trung Cho Người Mới Bắt Đầu", 85500, "/assets/images/q26.png", "MCBooks", "Ngoại ngữ", "NXB Hồng Đức", 320, "Sách tự học tiếng Trung hiệu quả với lộ trình rõ ràng."));
        tatCaSach.add(new Sach("ong-tram-tuoi-treo-qua-cua-so-va-bien-mat", "Ông Trăm Tuổi Trèo Qua Cửa Sổ Và Biến Mất", 146300, "/assets/images/q28.png", "Jonas Jonasson", "Văn học", "NXB Trẻ", 500, "Cuộc phiêu lưu hài hước và phi lý của một cụ già 100 tuổi."));
        tatCaSach.add(new Sach("nguoi-dua-dieu", "Người Đua Diều", 146300, "/assets/images/q29.png", "Khaled Hosseini", "Văn học", "NXB Nhã Nam", 450, "Câu chuyện đau xót về tình bạn, sự phản bội và chuộc lỗi tại Afghanistan."));
        tatCaSach.add(new Sach("think-and-grow-rich", "Think And Grow Rich", 77000, "/assets/images/q31.png", "Napoleon Hill", "Kinh tế", "NXB Tổng hợp TP.HCM", 380, "Bí quyết thành công và làm giàu dựa trên tư duy tích cực."));
        tatCaSach.add(new Sach("nguoi-giau-co-nhat-thanh-babylon", "Người Giàu Có Nhất Thành Babylon", 74000, "/assets/images/q32.png", "George S. Clason", "Kinh tế", "NXB Trẻ", 220, "Những quy tắc quản lý tài chính hiệu quả từ thời Babylon cổ đại."));
        tatCaSach.add(new Sach("lam-giau-tu-chung-khoan", "Làm Giàu Từ Chứng Khoán", 700000, "/assets/images/q34.png", "William J. O'Neil", "Kinh tế", "NXB Chiêu Dương", 650, "Phương pháp đầu tư CANSLIM kinh điển cho mọi nhà đầu tư."));
        tatCaSach.add(new Sach("ghi-chep-phap-y-nhung-cai-chet-bi-an", "Ghi Chép Pháp Y - Những Cái Chết Bí Ẩn", 112500, "/assets/images/q41.png", "Pháp y Tần Minh", "Trinh thám", "NXB Thanh Niên", 380, "Hồ sơ những vụ án bí ẩn dưới góc nhìn của một bác sĩ pháp y."));
        tatCaSach.add(new Sach("kheo-an-noi-se-co-duoc-thien-ha", "Khéo Ăn Nói Sẽ Có Được Thiên Hạ", 118000, "/assets/images/q45.png", "Trác Nhã", "Tâm lý", "NXB Văn Học", 350, "Kỹ năng giao tiếp khôn khéo để đạt được thành công trong cuộc sống."));
        tatCaSach.add(new Sach("nha-gia-kim", "Nhà giả kim", 100000, "/assets/images/nha-gia-kim.jpg", "Paulo Coelho", "Văn học", "NXB Hà Nội", 228, "Hành trình theo đuổi vận mệnh của cậu bé chăn cừu Santiago."));
        tatCaSach.add(new Sach("90-tre-thong-minh-nho-tro-chuyen", "90% Trẻ Thông Minh Nhờ Trò Chuyện", 33000, "/assets/images/q16.png", "Fukuda Takeshi", "Kỹ năng", "NXB Lao Động", 200, "Phương pháp giáo dục trẻ thông qua những cuộc trò chuyện hằng ngày."));
        tatCaSach.add(new Sach("bi-mat-cua-phan-thien-an", "Bí Mật Của Phan Thiên Ân", 69000, "/assets/images/q19.png", "Tiến sĩ Alan Phan", "Kỹ năng", "NXB Trẻ", 180, "Những bài học về làm giàu và triết lý sống của một doanh nhân Việt."));
        tatCaSach.add(new Sach("co-hai-con-meo-ngoi-ben-cua-so", "Có Hai Con Mèo Ngồi Bên Cửa Sổ", 77000, "/assets/images/q22.png", "Nguyễn Nhật Ánh", "Văn học", "NXB Trẻ", 260, "Một câu chuyện dễ thương về tình bạn giữa một con mèo và một con chuột."));
        tatCaSach.add(new Sach("hanh-tinh-cua-mot-ke-nghi-nhieu", "Hành Tinh Của Một Kẻ Nghĩ Nhiều", 56000, "/assets/images/q23.png", "Nguyễn Đoàn Minh Đức", "Tâm lý", "NXB Nhã Nam", 310, "Góc nhìn hài hước về những lo âu và suy nghĩ vẩn vơ của giới trẻ."));
        tatCaSach.add(new Sach("tu-dien-tieng-em", "Từ Điển Tiếng 'Em'", 55000, "/assets/images/q24.png", "Khotudien", "Văn học", "NXB Phụ Nữ", 220, "Những định nghĩa hài hước về các từ ngữ thường dùng của phái đẹp."));
        tatCaSach.add(new Sach("di-tim-le-song", "Đi Tìm Lẽ Sống", 62000, "/assets/images/q25.png", "Viktor E. Frankl", "Tâm lý", "NXB Trẻ", 240, "Tìm thấy ý nghĩa cuộc đời ngay cả trong những hoàn cảnh nghiệt ngã nhất."));
        tatCaSach.add(new Sach("cho-toi-xin-mot-ve-di-tuoi-tho", "Cho Tôi Xin Một Vé Đi Tuổi Thơ", 69300, "/assets/images/q27.png", "Nguyễn Nhật Ánh", "Văn học", "NXB Trẻ", 250, "Hồi ức trong sáng về những trò nghịch ngợm thời thơ bé."));
        tatCaSach.add(new Sach("bien-moi-thu-thanh-tien", "Biến Mọi Thứ Thành Tiền", 109000, "/assets/images/q30.png", "Tetsuya Ishida", "Kinh tế", "NXB Tổng hợp TP.HCM", 280, "Cách tối ưu hóa tài chính cá nhân và tư duy kiếm tiền hiện đại."));
        // 4. Lệnh kích hoạt: Vừa mở trang lên là vẽ luôn các cuốn sách của Trang số 1
        hienThiSachTheoTrang(1);
    }

    private VBox taoGiaoDienMotCuonSach(Sach sach) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(200);

        ImageView imgView = new ImageView();
        try {
            // Lấy ảnh từ thư mục resources
            InputStream stream = getClass().getResourceAsStream(sach.getHinhAnh());
            if (stream != null) {
                imgView.setImage(new Image(stream));
            }
        } catch (Exception e) {
            System.out.println("Không tải được ảnh: " + sach.getHinhAnh());
        }
        imgView.setFitWidth(140);
        imgView.setFitHeight(190);

        Label lblTen = new Label(sach.getTenSach());
        lblTen.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        lblTen.setWrapText(true);
        lblTen.setAlignment(Pos.CENTER);

        Label lblGia = new Label(String.format("%,.0f đ", sach.getGiaBan()));
        lblGia.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 16px;");

        Button btnMua = new Button("Mua ngay");
        btnMua.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-pref-width: 120px;");

        // Bấm vào nút Mua ngay hoặc bấm vào Ảnh đều mở trang Chi tiết
        btnMua.setOnAction(e -> moTrangChiTiet(sach));
        imgView.setOnMouseClicked(e -> moTrangChiTiet(sach));
        imgView.setStyle("-fx-cursor: hand;"); // Đổi con trỏ chuột thành hình bàn tay khi di qua ảnh

        card.getChildren().addAll(imgView, lblTen, lblGia, btnMua);
        return card;
    }

    // Hàm cắt danh sách giống hệt JS của bạn[cite: 1]
    private void hienThiSachTheoTrang(int trang) {
        trangHienTai = trang;
        gridSach.getChildren().clear(); // Dọn dẹp sách của trang cũ

        int batDau = (trang - 1) * soSachMoiTrang;
        int ketThuc = Math.min(batDau + soSachMoiTrang, tatCaSach.size());

        // Chỉ lấy đúng 12 cuốn để vẽ ra
        for (int i = batDau; i < ketThuc; i++) {
            gridSach.getChildren().add(taoGiaoDienMotCuonSach(tatCaSach.get(i)));
        }

        capNhatMauNutPhanTrang();
    }

    // Hàm tô màu nút màu cam cho trang đang chọn
    private void capNhatMauNutPhanTrang() {
        String mauThuong = "-fx-background-color: #ecf0f1; -fx-text-fill: black;";
        String mauCam = "-fx-background-color: #f39c12; -fx-text-fill: white;";

        if(btnTrang1 != null) btnTrang1.setStyle(trangHienTai == 1 ? mauCam : mauThuong);
        if(btnTrang2 != null) btnTrang2.setStyle(trangHienTai == 2 ? mauCam : mauThuong);
        if(btnTrang3 != null) btnTrang3.setStyle(trangHienTai == 3 ? mauCam : mauThuong);
        if(btnTrang4 != null) btnTrang4.setStyle(trangHienTai == 4 ? mauCam : mauThuong);
    }

    // Các nút chuyển trang trong FXML sẽ gọi vào đây
    @FXML public void chuyenTrang1() { hienThiSachTheoTrang(1); }
    @FXML public void chuyenTrang2() { hienThiSachTheoTrang(2); }
    @FXML public void chuyenTrang3() { hienThiSachTheoTrang(3); }
    @FXML public void chuyenTrang4() { hienThiSachTheoTrang(4); }

    @FXML
    public void chuyenTrangTiep() {
        int tongSoTrang = (int) Math.ceil((double) tatCaSach.size() / soSachMoiTrang);
        if (trangHienTai < tongSoTrang) {
            hienThiSachTheoTrang(trangHienTai + 1);
        }
    }
    @FXML
    public void quayVeTrangChu() {
        try {
            // Tương tự, lấy nội dung trang chủ...
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hello-view.fxml"));

            // ...bơm thẳng vào Scene hiện tại
            danhMucRoot.getScene().setRoot(loader.load());

            // Đổi tiêu đề
            Stage stage = (Stage) danhMucRoot.getScene().getWindow();
            stage.setTitle("BOOKSTORE - Mua sắm trực tuyến");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Hàm xử lý khi bấm vào chữ "Tài khoản / 👤 Tên" trên menu
    @FXML
    public void handleAccountAction() {
        if (UserSession.isLogged()) {
            // Nếu đã đăng nhập -> Hỏi xác nhận Đăng xuất
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận đăng xuất");
            alert.setHeaderText(null);
            alert.setContentText("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản '" + UserSession.getUsername() + "'?");

            java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                UserSession.clear(); // Xóa trí nhớ
                chuyenTrangSang("/dang-nhap.fxml", "Đăng nhập - BOOKSTORE");
            }
        } else {
            // Nếu chưa đăng nhập -> Chuyển sang trang Đăng nhập
            chuyenTrangSang("/dang-nhap.fxml", "Đăng nhập - BOOKSTORE");
        }
    }

    // Hàm chuyển trang giữ nguyên khung cửa sổ (để không bị nhảy lằng nhằng)
    private void chuyenTrangSang(String fxmlFile, String title) {
        try {
            Stage stage = (Stage) danhMucRoot.getScene().getWindow();
            double width = stage.getScene().getWidth();
            double height = stage.getScene().getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load(), width, height);

            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void moTrangChiTiet(Sach sach) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chi-tiet-sach.fxml"));
            javafx.scene.Parent root = loader.load();

            // Truyền dữ liệu cuốn sách đang được click sang cho Controller Chi Tiết
            ChiTietSachController controller = loader.getController();
            controller.setSachData(sach);

            // Bơm giao diện mới vào
            danhMucRoot.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}