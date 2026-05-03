package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Single source of truth for the book catalog (title, author, category search).
 * Toàn bộ danh mục được lưu vào {@code ~/.quanlyhieusach/book-catalog.ser} để giữ
 * tồn kho và chỉnh sửa admin sau khi tắt app.
 */
public final class BookCatalog {

    private static final Path DATA_DIR = Path.of(System.getProperty("user.home"), ".quanlyhieusach");
    private static final Path DATA_FILE = DATA_DIR.resolve("book-catalog.ser");
    private static final Path TEMP_FILE = DATA_DIR.resolve("book-catalog.ser.tmp");

    private static final List<Sach> allBooks = new ArrayList<>();
    private static boolean initialized = false;

    private BookCatalog() {}

    public static synchronized List<Sach> getAllBooks() {
        if (!initialized) {
            allBooks.clear();
            List<Sach> loaded = loadFromDisk();
            if (loaded != null) {
                allBooks.addAll(loaded);
            } else {
                allBooks.addAll(buildCatalog());
            }
            initialized = true;
        }
        // Trả view read-only, nhưng backing list vẫn mutable qua addBook/removeBook.
        return Collections.unmodifiableList(allBooks);
    }

    /** Ghi danh sách sách hiện tại ra đĩa (sau khi sửa trực tiếp đối tượng {@link Sach}, trừ/ cộng kho, v.v.). */
    public static synchronized void persistNow() {
        if (!initialized) {
            return;
        }
        persist();
    }

    public static Optional<Sach> findById(String id) {
        if (id == null) return Optional.empty();
        return getAllBooks().stream().filter(s -> id.equals(s.getId())).findFirst();
    }

    /**
     * Add a new book into catalog at runtime.
     * @return true if added, false if id already exists
     */
    public static synchronized boolean addBook(Sach sach) {
        if (sach == null) return false;
        String id = sach.getId();
        if (id == null || id.isBlank()) return false;
        // Ensure catalog initialized
        getAllBooks();
        if (findById(id).isPresent()) return false;
        if (allBooks.add(sach)) {
            persist();
            return true;
        }
        return false;
    }

    /** Xóa hẳn khỏi danh mục (dùng sau khi xóa vĩnh viễn trong admin). */
    public static synchronized boolean removeBook(Sach sach) {
        if (sach == null) return false;
        getAllBooks();
        String id = sach.getId();
        if (id == null || id.isBlank()) return false;
        boolean removed = allBooks.removeIf(s -> id.equals(s.getId()));
        if (removed) {
            persist();
        }
        return removed;
    }

    /**
     * Match query against title, author, or category (case-insensitive, contains).
     */
    public static List<Sach> filterByQuery(List<Sach> source, String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return new ArrayList<>(source);
        }
        String q = rawQuery.trim().toLowerCase(Locale.ROOT);
        return source.stream()
                .filter(s -> contains(s.getTenSach(), q)
                        || contains(s.getTacGia(), q)
                        || contains(s.getTheLoai(), q))
                .collect(Collectors.toList());
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }

    private static void persist() {
        try {
            ArrayList<BookRecord> records = new ArrayList<>(allBooks.size());
            for (Sach s : allBooks) {
                records.add(BookRecord.from(s));
            }
            Files.createDirectories(DATA_DIR);
            try (OutputStream out = Files.newOutputStream(TEMP_FILE);
                 ObjectOutputStream oos = new ObjectOutputStream(out)) {
                oos.writeObject(records);
            }
            try {
                Files.move(TEMP_FILE, DATA_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ignored) {
                Files.move(TEMP_FILE, DATA_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return danh sách đã lưu; {@code null} nếu chưa có file hoặc lỗi đọc (dùng danh mục mặc định trong code).
     */
    private static List<Sach> loadFromDisk() {
        if (!Files.isRegularFile(DATA_FILE)) {
            return null;
        }
        try (InputStream in = Files.newInputStream(DATA_FILE);
             ObjectInputStream ois = new ObjectInputStream(in)) {
            Object o = ois.readObject();
            if (o instanceof ArrayList<?> raw) {
                ArrayList<Sach> out = new ArrayList<>();
                for (Object item : raw) {
                    if (item instanceof BookRecord br) {
                        out.add(br.toSach());
                    }
                }
                return out;
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final class BookRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        String id;
        String tenSach;
        double giaBan;
        String hinhAnh;
        String tacGia;
        String theLoai;
        String nhaXuatBan;
        int soTrang;
        String moTa;
        int tonKho;
        boolean deleted;

        static BookRecord from(Sach s) {
            BookRecord r = new BookRecord();
            r.id = s.getId();
            r.tenSach = s.getTenSach();
            r.giaBan = s.getGiaBan();
            r.hinhAnh = s.getHinhAnh();
            r.tacGia = s.getTacGia();
            r.theLoai = s.getTheLoai();
            r.nhaXuatBan = s.getNhaXuatBan();
            r.soTrang = s.getSoTrang();
            r.moTa = s.getMoTa();
            r.tonKho = s.getTonKho();
            r.deleted = s.isDeleted();
            return r;
        }

        Sach toSach() {
            Sach s = new Sach(id, tenSach, giaBan, hinhAnh, tacGia, theLoai, nhaXuatBan, soTrang, moTa, tonKho);
            s.setDeleted(deleted);
            return s;
        }
    }

    private static List<Sach> buildCatalog() {
        List<Sach> list = new ArrayList<>();
        list.add(new Sach("ba-tuoc-monte-cristo", "Bá tước Monte Cristo", 185000, "/assets/images/ba-tuoc-monte-cristo.jpg", "Alexandre Dumas", "Văn học", "NXB Văn Học", 800, "Hành trình trả thù đầy kịch tính của Edmond Dantès sau nhiều năm bị oan sai."));
        list.add(new Sach("giet-con-chim-nhai", "Giết con chim nhại", 210000, "/assets/images/giet-con-chim-nhai.jpg", "Harper Lee", "Văn học", "NXB Trẻ", 420, "Một cái nhìn sâu sắc về định kiến chủng tộc qua con mắt trẻ thơ."));
        list.add(new Sach("hai-so-phan", "Hai số phận", 320000, "/assets/images/hai-so-phan.jpg", "Jeffrey Archer", "Văn học", "NXB Hội Nhà Văn", 760, "Câu chuyện về hai người đàn ông sinh ra cùng ngày nhưng ở hai hoàn cảnh trái ngược."));
        list.add(new Sach("khong-gia-dinh", "Không gia đình", 450000, "/assets/images/khong-gia-dinh.jpg", "Hector Malot", "Văn học", "NXB Kim Đồng", 500, "Cuộc phiêu lưu đầy nghị lực của cậu bé Remi trên khắp nước Pháp."));
        list.add(new Sach("phia-sau-nghi-can-x", "Phía sau nghi can X", 230000, "/assets/images/phia-sau-nghi-can-x.png", "Keigo Higashino", "Trinh thám", "NXB Hội Nhà Văn", 400, "Cuộc đấu trí nghẹt thở giữa một thiên tài toán học và một điều tra viên."));
        list.add(new Sach("rung-nauy", "Rừng Nauy", 240000, "/assets/images/rung-nauy.png", "Haruki Murakami", "Văn học", "NXB Nhã Nam", 550, "Câu chuyện về nỗi cô đơn và tình yêu của những người trẻ tại Nhật Bản."));
        list.add(new Sach("bo-gia", "Bố Già", 105000, "/assets/images/bo-gia.png", "Mario Puzo", "Văn học", "NXB Văn Học", 600, "Tác phẩm kinh điển về thế giới ngầm Mafia và những giá trị gia đình."));
        list.add(new Sach("nhung-nguoi-khon-kho", "Những người khốn khổ", 285000, "/assets/images/nhung-nguoi-khon-kho.png", "Victor Hugo", "Văn học", "NXB Văn Học", 1200, "Bức tranh toàn cảnh về xã hội Pháp thế kỷ 19 qua cuộc đời Jean Valjean."));
        list.add(new Sach("phia-tay-khong-co-gi-la", "Phía tây không có gì lạ", 50000, "/assets/images/phia-tay-khong-co-gi-la.png", "Erich Maria Remarque", "Văn học", "NXB Trẻ", 300, "Sự tàn khốc của chiến tranh qua góc nhìn của những người lính trẻ."));
        list.add(new Sach("tuoi-tre-dang-gia-bao-nhieu", "Tuổi trẻ đáng giá bao nhiêu", 80000, "/assets/images/tuoi-tre-dang-gia-bao-nhieu.png", "Rosie Nguyễn", "Kỹ năng", "NXB Hội Nhà Văn", 280, "Những lời tâm sự chân thành giúp bạn trẻ tìm thấy hướng đi cho cuộc đời."));
        list.add(new Sach("cay-cam-ngot-cua-toi", "Cây Cam Ngọt Của Tôi", 81000, "/assets/images/cay-cam-ngot-cua-toi.png", "José Mauro de Vasconcelos", "Văn học", "NXB Hội Nhà Văn", 300, "Câu chuyện cảm động về tình bạn giữa cậu bé nghèo Zezé và cây cam ngọt."));
        list.add(new Sach("khong-diet-khong-sinh-dung-so-hai", "Không Diệt Không Sinh Đừng Sợ Hãi", 82500, "/assets/images/q2.png", "Thích Nhất Hạnh", "Kỹ năng", "NXB Hồng Đức", 250, "Những triết lý sâu sắc để tìm thấy sự bình an trong tâm hồn."));
        list.add(new Sach("khi-hoi-tho-hoa-thinh-khong", "Khi Hơi Thở Hóa Thinh Không", 81750, "/assets/images/q3.png", "Paul Kalanithi", "Văn học", "NXB Lao Động", 240, "Lời tự sự của một bác sĩ phẫu thuật não đối mặt với bệnh ung thư."));
        list.add(new Sach("dan-ong-sao-hoa-dan-ba-sao-kim", "Đàn Ông Sao Hỏa Đàn Bà Sao Kim", 122000, "/assets/images/q4.png", "John Gray", "Kỹ năng", "NXB Trẻ", 450, "Cẩm nang thấu hiểu sự khác biệt giữa hai giới trong tình yêu."));
        list.add(new Sach("muon-kiep-nhan-sinh", "Muôn Kiếp Nhân Sinh", 117600, "/assets/images/q5.png", "Nguyên Phong", "Văn học", "NXB Tổng hợp TP.HCM", 400, "Hành trình khám phá về luật nhân quả và luân hồi qua nhiều kiếp sống."));
        list.add(new Sach("tieng-han-tong-hop-so-cap-1", "Tiếng Hàn Tổng Hợp - Sơ Cấp 1", 148500, "/assets/images/q6.png", "Nhiều Tác Giả", "Ngoại ngữ", "NXB Nhân Dân", 350, "Giáo trình căn bản dành cho những người mới bắt đầu học tiếng Hàn."));
        list.add(new Sach("dac-nhan-tam", "Đắc Nhân Tâm", 56000, "/assets/images/dac-nhan-tam.jpg", "Dale Carnegie", "Kỹ năng", "NXB Tổng hợp TP.HCM", 320, "Nghệ thuật thu phục lòng người và giao tiếp hiệu quả."));
        list.add(new Sach("day-con-lam-giau-01", "Dạy Con Làm Giàu 01", 62000, "/assets/images/q8.png", "Robert T. Kiyosaki", "Kinh tế", "NXB Trẻ", 380, "Những bài học cơ bản về tư duy tài chính của người giàu."));
        list.add(new Sach("hieu-ve-trai-tim", "Hiểu Về Trái Tim", 119000, "/assets/images/q9.png", "Thích Minh Niệm", "Kỹ năng", "NXB Tổng hợp TP.HCM", 480, "Cách nhìn nhận và chuyển hóa những nỗi khổ niềm đau."));
        list.add(new Sach("ngon-ngu-co-the", "Ngôn Ngữ Cơ Thể", 158000, "/assets/images/q10.png", "Allan Pease", "Kỹ năng", "NXB Tổng hợp TP.HCM", 400, "Bí quyết đọc vị người khác qua những cử chỉ không lời."));
        list.add(new Sach("cam-on-nguoi-lon", "Cảm Ơn Người Lớn", 84700, "/assets/images/q11.png", "Nguyễn Nhật Ánh", "Văn học", "NXB Trẻ", 320, "Một tấm vé đi tuổi thơ tiếp theo đầy thú vị cho cả trẻ em và người lớn."));
        list.add(new Sach("thay-doi-cuoc-song-voi-nhan-so-hoc", "Thay Đổi Cuộc Sống Với Nhân Số Học", 173600, "/assets/images/q12.png", "Lê Đỗ Quỳnh Hương", "Kỹ năng", "NXB Tổng hợp TP.HCM", 350, "Khám phá bản thân và định hướng cuộc sống qua ngày sinh."));
        list.add(new Sach("khoi-nghiep-ban-le", "Khởi Nghiệp Bán Lẻ", 125000, "/assets/images/q13.png", "Trần Thanh Phong", "Kinh tế", "NXB Văn Hóa Văn Nghệ", 300, "Cẩm nang dành cho người muốn bắt đầu kinh doanh cửa hàng."));
        list.add(new Sach("muon-kiep-nhan-sinh-tap-2", "Muôn Kiếp Nhân Sinh - Tập 2", 187600, "/assets/images/q14.png", "Nguyên Phong", "Văn học", "NXB Tổng hợp TP.HCM", 500, "Tiếp tục khám phá bí mật vũ trụ và sự thức tỉnh nhân sinh."));
        list.add(new Sach("lam-ban-voi-bau-troi", "Làm Bạn Với Bầu Trời", 169400, "/assets/images/q15.png", "Nguyễn Nhật Ánh", "Văn học", "NXB Trẻ", 250, "Câu chuyện về lòng nhân hậu và cái nhìn bao dung của một cậu bé."));
        list.add(new Sach("tam-ly-hoc-toi-pham", "Tâm Lý Học Tội Phạm", 94000, "/assets/images/q17.png", "Stanton E. Samenow", "Tâm lý", "NXB Công An Nhân Dân", 420, "Phân tích diễn biến tâm lý và hành vi của các đối tượng phạm tội."));
        list.add(new Sach("thien-tai-ben-trai-ke-dien-ben-phai", "Thiên Tài Bên Trái, Kẻ Điên Bên Phải", 116000, "/assets/images/q18.png", "Cao Minh", "Tâm lý", "NXB Thế Giới", 400, "Ghi chép về thế giới nội tâm kỳ lạ của những bệnh nhân tâm thần."));
        list.add(new Sach("tam-ly-hoc-ve-tien", "Tâm Lý Học Về Tiền", 141500, "/assets/images/q20.png", "Morgan Housel", "Tâm lý", "NXB Trẻ", 350, "Giải mã cách con người suy nghĩ và hành động đối với tiền bạc."));
        list.add(new Sach("cay-chuoi-non-di-giay-xanh", "Cây Chuối Non Đi Giày Xanh", 84700, "/assets/images/q21.png", "Nguyễn Nhật Ánh", "Văn học", "NXB Trẻ", 280, "Câu chuyện dễ thương về tình bạn và rung động đầu đời ở làng quê."));
        list.add(new Sach("tu-hoc-tieng-trung-cho-nguoi-moi-bat-dau", "Tự Học Tiếng Trung Cho Người Mới Bắt Đầu", 85500, "/assets/images/q26.png", "MCBooks", "Ngoại ngữ", "NXB Hồng Đức", 320, "Sách tự học tiếng Trung hiệu quả với lộ trình rõ ràng."));
        list.add(new Sach("ong-tram-tuoi-treo-qua-cua-so-va-bien-mat", "Ông Trăm Tuổi Trèo Qua Cửa Sổ Và Biến Mất", 146300, "/assets/images/q28.png", "Jonas Jonasson", "Văn học", "NXB Trẻ", 500, "Cuộc phiêu lưu hài hước và phi lý của một cụ già 100 tuổi."));
        list.add(new Sach("nguoi-dua-dieu", "Người Đua Diều", 146300, "/assets/images/q29.png", "Khaled Hosseini", "Văn học", "NXB Nhã Nam", 450, "Câu chuyện đau xót về tình bạn, sự phản bội và chuộc lỗi tại Afghanistan."));
        list.add(new Sach("think-and-grow-rich", "Think And Grow Rich", 77000, "/assets/images/q31.png", "Napoleon Hill", "Kinh tế", "NXB Tổng hợp TP.HCM", 380, "Bí quyết thành công và làm giàu dựa trên tư duy tích cực."));
        list.add(new Sach("nguoi-giau-co-nhat-thanh-babylon", "Người Giàu Có Nhất Thành Babylon", 74000, "/assets/images/q32.png", "George S. Clason", "Kinh tế", "NXB Trẻ", 220, "Những quy tắc quản lý tài chính hiệu quả từ thời Babylon cổ đại."));
        list.add(new Sach("lam-giau-tu-chung-khoan", "Làm Giàu Từ Chứng Khoán", 700000, "/assets/images/q34.png", "William J. O'Neil", "Kinh tế", "NXB Chiêu Dương", 650, "Phương pháp đầu tư CANSLIM kinh điển cho mọi nhà đầu tư."));
        list.add(new Sach("ghi-chep-phap-y-nhung-cai-chet-bi-an", "Ghi Chép Pháp Y - Những Cái Chết Bí Ẩn", 112500, "/assets/images/q41.png", "Pháp y Tần Minh", "Trinh thám", "NXB Thanh Niên", 380, "Hồ sơ những vụ án bí ẩn dưới góc nhìn của một bác sĩ pháp y."));
        list.add(new Sach("kheo-an-noi-se-co-duoc-thien-ha", "Khéo Ăn Nói Sẽ Có Được Thiên Hạ", 118000, "/assets/images/q45.png", "Trác Nhã", "Tâm lý", "NXB Văn Học", 350, "Kỹ năng giao tiếp khôn khéo để đạt được thành công trong cuộc sống."));
        list.add(new Sach("nha-gia-kim", "Nhà giả kim", 100000, "/assets/images/nha-gia-kim.jpg", "Paulo Coelho", "Văn học", "NXB Hà Nội", 228, "Hành trình theo đuổi vận mệnh của cậu bé chăn cừu Santiago."));
        list.add(new Sach("90-tre-thong-minh-nho-tro-chuyen", "90% Trẻ Thông Minh Nhờ Trò Chuyện", 33000, "/assets/images/q16.png", "Fukuda Takeshi", "Kỹ năng", "NXB Lao Động", 200, "Phương pháp giáo dục trẻ thông qua những cuộc trò chuyện hằng ngày."));
        list.add(new Sach("bi-mat-cua-phan-thien-an", "Bí Mật Của Phan Thiên Ân", 69000, "/assets/images/q19.png", "Tiến sĩ Alan Phan", "Kỹ năng", "NXB Trẻ", 180, "Những bài học về làm giàu và triết lý sống của một doanh nhân Việt."));
        list.add(new Sach("co-hai-con-meo-ngoi-ben-cua-so", "Có Hai Con Mèo Ngồi Bên Cửa Sổ", 77000, "/assets/images/q22.png", "Nguyễn Nhật Ánh", "Văn học", "NXB Trẻ", 260, "Một câu chuyện dễ thương về tình bạn giữa một con mèo và một con chuột."));
        list.add(new Sach("hanh-tinh-cua-mot-ke-nghi-nhieu", "Hành Tinh Của Một Kẻ Nghĩ Nhiều", 56000, "/assets/images/q23.png", "Nguyễn Đoàn Minh Đức", "Tâm lý", "NXB Nhã Nam", 310, "Góc nhìn hài hước về những lo âu và suy nghĩ vẩn vơ của giới trẻ."));
        list.add(new Sach("tu-dien-tieng-em", "Từ Điển Tiếng 'Em'", 55000, "/assets/images/q24.png", "Khotudien", "Văn học", "NXB Phụ Nữ", 220, "Những định nghĩa hài hước về các từ ngữ thường dùng của phái đẹp."));
        list.add(new Sach("di-tim-le-song", "Đi Tìm Lẽ Sống", 62000, "/assets/images/q25.png", "Viktor E. Frankl", "Tâm lý", "NXB Trẻ", 240, "Tìm thấy ý nghĩa cuộc đời ngay cả trong những hoàn cảnh nghiệt ngã nhất."));
        list.add(new Sach("cho-toi-xin-mot-ve-di-tuoi-tho", "Cho Tôi Xin Một Vé Đi Tuổi Thơ", 69300, "/assets/images/q27.png", "Nguyễn Nhật Ánh", "Văn học", "NXB Trẻ", 250, "Hồi ức trong sáng về những trò nghịch ngợm thời thơ bé."));
        list.add(new Sach("bien-moi-thu-thanh-tien", "Biến Mọi Thứ Thành Tiền", 109000, "/assets/images/q30.png", "Tetsuya Ishida", "Kinh tế", "NXB Tổng hợp TP.HCM", 280, "Cách tối ưu hóa tài chính cá nhân và tư duy kiếm tiền hiện đại."));
        return list;
    }
}
