package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Nhà cung cấp và hóa đơn nhập kho: một nguồn dữ liệu cho mọi lần mở trang,
 * lưu ra đĩa để không bị tạo lại ngẫu nhiên mỗi lần vào menu.
 */
public final class SupplierInvoiceStore {

    private static final Path DATA_DIR = Path.of(System.getProperty("user.home"), ".quanlyhieusach");
    private static final Path DATA_FILE = DATA_DIR.resolve("supplier-invoices.ser");
    private static final Path TEMP_FILE = DATA_DIR.resolve("supplier-invoices.ser.tmp");

    private static final long SEED_DEMO = 12_345L;

    private static ObservableList<Supplier> suppliers;
    private static ObservableList<Invoice> invoices;
    private static volatile boolean ready;

    private SupplierInvoiceStore() {}

    public static ObservableList<Supplier> getSuppliers() {
        ensureReady();
        return suppliers;
    }

    public static ObservableList<Invoice> getInvoices() {
        ensureReady();
        return invoices;
    }

    public static void persistNow() {
        if (!ready) {
            return;
        }
        persist();
    }

    public static String nextInvoiceId() {
        ensureReady();
        int max = 1000;
        for (Invoice inv : invoices) {
            String id = inv.getId();
            if (id != null && id.startsWith("HD-")) {
                try {
                    int n = Integer.parseInt(id.substring(3));
                    max = Math.max(max, n);
                } catch (NumberFormatException ignored) {
                    // ignore
                }
            }
        }
        return "HD-" + (max + 1);
    }

    private static synchronized void ensureReady() {
        if (ready) {
            return;
        }
        RootDocument doc = loadFromDisk();
        if (doc.suppliers.isEmpty()) {
            seedInitialData(doc);
            writeRoot(doc);
        }
        suppliers = FXCollections.observableArrayList(doc.suppliers);
        invoices = FXCollections.observableArrayList(doc.invoices);
        suppliers.addListener((ListChangeListener<Supplier>) c -> persist());
        invoices.addListener((ListChangeListener<Invoice>) c -> persist());
        ready = true;
    }

    private static void seedInitialData(RootDocument doc) {
        doc.suppliers.add(new Supplier("NCC-001", "Nhà sách Fahasa", "1900 636467", "info@fahasa.com", "Đang hợp tác"));
        doc.suppliers.add(new Supplier("NCC-002", "Công ty Nhã Nam", "0243 514 6876", "nhanam@nhanam.vn", "Đang hợp tác"));
        doc.suppliers.add(new Supplier("NCC-003", "Alphabooks", "0243 722 6234", "info@alphabooks.vn", "Đang hợp tác"));
        doc.suppliers.add(new Supplier("NCC-004", "Tiki Trading", "1900 6035", "hotro@tiki.vn", "Đang hợp tác"));

        Random rand = new Random(SEED_DEMO);
        int invSeq = 1000;
        for (Supplier ncc : doc.suppliers) {
            List<Sach> filtered = new ArrayList<>();
            for (Sach b : BookCatalog.getAllBooks()) {
                if (b.isDeleted()) {
                    continue;
                }
                String cat = b.getTheLoai();
                if ("NCC-001".equals(ncc.getId()) && "Văn học".equals(cat)) {
                    filtered.add(b);
                } else if ("NCC-002".equals(ncc.getId()) && "Kỹ năng".equals(cat)) {
                    filtered.add(b);
                } else if ("NCC-003".equals(ncc.getId()) && "Kinh tế".equals(cat)) {
                    filtered.add(b);
                } else if ("NCC-004".equals(ncc.getId())
                        && Arrays.asList("Tâm lý", "Trinh thám", "Ngoại ngữ").contains(cat)) {
                    filtered.add(b);
                }
            }
            if (!filtered.isEmpty()) {
                double totalHD = 0;
                List<InvoiceItem> items = new ArrayList<>();
                for (Sach s : filtered) {
                    int sl = rand.nextInt(51) + 10;
                    double giaNhap = s.getGiaBan() * 0.65;
                    double thanhTien = giaNhap * sl;
                    totalHD += thanhTien;
                    items.add(new InvoiceItem(s.getId(), s.getTenSach(), giaNhap, sl, thanhTien));
                }
                ncc.addTotal(totalHD);
                invSeq++;
                doc.invoices.add(new Invoice("HD-" + invSeq, ncc.getId(), LocalDate.now().toString(), totalHD, items));
            }
        }
    }

    private static synchronized void persist() {
        if (!ready) {
            return;
        }
        RootDocument out = new RootDocument();
        out.suppliers.addAll(suppliers);
        out.invoices.addAll(invoices);
        writeRoot(out);
    }

    private static void writeRoot(RootDocument doc) {
        try {
            Files.createDirectories(DATA_DIR);
            try (OutputStream out = Files.newOutputStream(TEMP_FILE);
                 ObjectOutputStream oos = new ObjectOutputStream(out)) {
                oos.writeObject(doc);
            }
            Files.move(TEMP_FILE, DATA_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static RootDocument loadFromDisk() {
        if (!Files.isRegularFile(DATA_FILE)) {
            return new RootDocument();
        }
        try (InputStream in = Files.newInputStream(DATA_FILE);
             ObjectInputStream ois = new ObjectInputStream(in)) {
            Object o = ois.readObject();
            if (o instanceof RootDocument doc) {
                return doc;
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return new RootDocument();
    }

    // --- Serializable models ---

    static final class RootDocument implements Serializable {
        private static final long serialVersionUID = 1L;
        final ArrayList<Supplier> suppliers = new ArrayList<>();
        final ArrayList<Invoice> invoices = new ArrayList<>();
    }

    public static final class Supplier implements Serializable {
        private static final long serialVersionUID = 1L;
        private String id;
        private String name;
        private String phone;
        private String email;
        private String status;
        private double totalPaid;

        public Supplier(String i, String n, String p, String e, String s) {
            this.id = i;
            this.name = n;
            this.phone = p;
            this.email = e;
            this.status = s;
            this.totalPaid = 0;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }

        public String getStatus() {
            return status;
        }

        public double getTotalPaid() {
            return totalPaid;
        }

        public void setName(String n) {
            name = n;
        }

        public void setPhone(String p) {
            phone = p;
        }

        public void setEmail(String e) {
            email = e;
        }

        public void setStatus(String s) {
            status = s;
        }

        public void addTotal(double amt) {
            totalPaid += amt;
        }
    }

    public static final class Invoice implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String id;
        private final String supplierId;
        private final String date;
        private final double amount;
        private final ArrayList<InvoiceItem> items;

        public Invoice(String i, String s, String d, double a, List<InvoiceItem> it) {
            this.id = i;
            this.supplierId = s;
            this.date = d;
            this.amount = a;
            this.items = new ArrayList<>(it);
        }

        public String getId() {
            return id;
        }

        public String getSupplierId() {
            return supplierId;
        }

        public String getDate() {
            return date;
        }

        public double getAmount() {
            return amount;
        }

        public List<InvoiceItem> getItems() {
            return items;
        }
    }

    public static final class InvoiceItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String bookId;
        private String title;
        private double price;
        private double total;
        private int quantity;

        public InvoiceItem(String b, String t, double p, int q, double to) {
            this.bookId = b;
            this.title = t;
            this.price = p;
            this.quantity = q;
            this.total = to;
        }

        public String getBookId() {
            return bookId;
        }

        public String getTitle() {
            return title;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getTotal() {
            return total;
        }

        public void setQuantity(int q) {
            this.quantity = q;
        }

        public void setTotal(double t) {
            this.total = t;
        }
    }
}
