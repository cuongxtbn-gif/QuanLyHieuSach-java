package org.example;

import javafx.beans.Observable;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Giỏ hàng và lịch sử đơn theo từng tài khoản, lưu ra đĩa để còn dữ liệu sau khi đăng xuất / mở lại app.
 */
public final class CustomerAccountStore {

    private static final Path DATA_DIR = Path.of(System.getProperty("user.home"), ".quanlyhieusach");
    private static final Path DATA_FILE = DATA_DIR.resolve("customer-store.ser");
    private static final Path TEMP_FILE = DATA_DIR.resolve("customer-store.ser.tmp");

    private static final Map<String, ObservableList<CartController.CartItem>> carts = new ConcurrentHashMap<>();
    private static final Map<String, ObservableList<CartController.Order>> orders = new ConcurrentHashMap<>();
    private static final ObservableList<DiscountCoupon> coupons = FXCollections.observableArrayList();

    /** Bản sao dữ liệu đã đọc từ file; dùng khi user chưa mở giỏ/đơn trong phiên này. */
    private static RootDocument rootDocument = loadFromDisk();

    static {
        hydrateCoupons();
    }

    private CustomerAccountStore() {}

    public static ObservableList<CartController.CartItem> getCart(String username) {
        if (username == null || username.isBlank()) {
            return FXCollections.observableArrayList();
        }
        final String user = username.trim();
        return carts.computeIfAbsent(user, CustomerAccountStore::createHydratedCart);
    }

    public static ObservableList<CartController.Order> getOrders(String username) {
        if (username == null || username.isBlank()) {
            return FXCollections.observableArrayList();
        }
        final String user = username.trim();
        return orders.computeIfAbsent(user, CustomerAccountStore::createHydratedOrders);
    }

    public static ObservableList<CartController.Order> getAllOrders() {
        if (rootDocument != null && rootDocument.users != null) {
            for (String user : rootDocument.users.keySet()) {
                getOrders(user);
            }
        }
        ObservableList<CartController.Order> all = FXCollections.observableArrayList();
        for (ObservableList<CartController.Order> eachUserOrders : orders.values()) {
            all.addAll(eachUserOrders);
        }
        return all;
    }

    public static boolean updateOrderStatus(String orderId, String username, String newStatus) {
        if (username != null && !username.isBlank()) {
            ObservableList<CartController.Order> userOrders = getOrders(username);
            for (CartController.Order order : userOrders) {
                if (order.orderIdProperty().get().equals(orderId)) {
                    order.statusProperty().set(newStatus);
                    persist();
                    return true;
                }
            }
        }

        for (ObservableList<CartController.Order> userOrders : orders.values()) {
            for (CartController.Order order : userOrders) {
                if (order.orderIdProperty().get().equals(orderId)) {
                    order.statusProperty().set(newStatus);
                    persist();
                    return true;
                }
            }
        }
        return false;
    }

    public static ObservableList<DiscountCoupon> getCoupons() {
        return coupons;
    }

    public static void saveNow() {
        persist();
    }

    private static ObservableList<CartController.CartItem> createHydratedCart(String user) {
        ObservableList<CartController.CartItem> list = FXCollections.observableArrayList(
                item -> new Observable[]{
                        item.bookIdProperty(),
                        item.productNameProperty(),
                        item.priceProperty(),
                        item.quantityProperty(),
                        item.selectedProperty()
                });
        UserSnapshot snap = snapshotFor(user);
        if (snap != null) {
            for (CartLineDto line : snap.cart) {
                String id = line.bookId;
                if ((id == null || id.isBlank()) && line.productName != null) {
                    // fallback tương thích dữ liệu cũ: tìm id theo tên
                    for (Sach s : BookCatalog.getAllBooks()) {
                        if (line.productName.equalsIgnoreCase(s.getTenSach())) {
                            id = s.getId();
                            break;
                        }
                    }
                }
                CartController.CartItem it = new CartController.CartItem(id, line.productName, line.price, line.quantity);
                it.setSelected(line.selected);
                list.add(it);
            }
        }
        list.addListener((ListChangeListener<CartController.CartItem>) c -> persist());
        return list;
    }

    private static ObservableList<CartController.Order> createHydratedOrders(String user) {
        ObservableList<CartController.Order> list = FXCollections.observableArrayList();
        UserSnapshot snap = snapshotFor(user);
        if (snap != null) {
            for (OrderLineDto line : snap.orders) {
                list.add(new CartController.Order(
                        line.orderId,
                        line.username,
                        line.purchasedItems,
                        line.orderDate,
                        line.orderMonth,
                        line.orderTime,
                        line.total,
                        line.status
                ));
            }
        }
        list.addListener((ListChangeListener<CartController.Order>) c -> persist());
        return list;
    }

    private static UserSnapshot snapshotFor(String user) {
        if (rootDocument == null || rootDocument.users == null) {
            return null;
        }
        return rootDocument.users.get(user);
    }

    private static synchronized void persist() {
        try {
            RootDocument out = new RootDocument();
            Set<String> allUsers = new HashSet<>();
            if (rootDocument != null && rootDocument.users != null) {
                allUsers.addAll(rootDocument.users.keySet());
            }
            allUsers.addAll(carts.keySet());
            allUsers.addAll(orders.keySet());

            for (String u : allUsers) {
                UserSnapshot blob = new UserSnapshot();
                if (carts.containsKey(u)) {
                    for (CartController.CartItem item : carts.get(u)) {
                        blob.cart.add(new CartLineDto(
                                item.bookIdProperty() == null ? null : item.bookIdProperty().get(),
                                item.productNameProperty().get(),
                                item.priceProperty().get(),
                                item.getQuantity(),
                                item.isSelected()
                        ));
                    }
                } else if (rootDocument != null && rootDocument.users.containsKey(u)) {
                    blob.cart.addAll(rootDocument.users.get(u).cart);
                }

                if (orders.containsKey(u)) {
                    for (CartController.Order o : orders.get(u)) {
                        blob.orders.add(new OrderLineDto(
                                o.orderIdProperty().get(),
                                o.usernameProperty().get(),
                                o.purchasedItemsProperty().get(),
                                o.orderDateProperty().get(),
                                o.orderMonthProperty().get(),
                                o.orderTimeProperty().get(),
                                o.totalProperty().get(),
                                o.statusProperty().get()
                        ));
                    }
                } else if (rootDocument != null && rootDocument.users.containsKey(u)) {
                    blob.orders.addAll(rootDocument.users.get(u).orders);
                }

                out.users.put(u, blob);
            }

            for (DiscountCoupon coupon : coupons) {
                out.coupons.add(new CouponDto(
                        coupon.codeProperty().get(),
                        coupon.descriptionProperty().get(),
                        coupon.discountPercentProperty().get(),
                        coupon.targetUsersProperty().get(),
                        coupon.conditionsProperty().get(),
                        coupon.activeProperty().get()
                ));
            }

            rootDocument = out;
            writeAtomic(out);
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

    private static void writeAtomic(RootDocument doc) throws IOException {
        Files.createDirectories(DATA_DIR);
        try (OutputStream out = Files.newOutputStream(TEMP_FILE);
             ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(doc);
        }
        try {
            Files.move(TEMP_FILE, DATA_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {
            Files.move(TEMP_FILE, DATA_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // --- Serializable DTOs ---

    private static final class RootDocument implements Serializable {
        private static final long serialVersionUID = 1L;
        final Map<String, UserSnapshot> users = new HashMap<>();
        final List<CouponDto> coupons = new ArrayList<>();
    }

    private static final class UserSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        final List<CartLineDto> cart = new ArrayList<>();
        final List<OrderLineDto> orders = new ArrayList<>();
    }

    private static final class CartLineDto implements Serializable {
        private static final long serialVersionUID = 1L;
        final String bookId;
        final String productName;
        final double price;
        final int quantity;
        final boolean selected;

        CartLineDto(String bookId, String productName, double price, int quantity, boolean selected) {
            this.bookId = bookId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.selected = selected;
        }
    }

    private static final class OrderLineDto implements Serializable {
        private static final long serialVersionUID = 1L;
        final String orderId;
        final String username;
        final String purchasedItems;
        final String orderDate;
        final String orderMonth;
        final String orderTime;
        final double total;
        final String status;

        OrderLineDto(String orderId, String username, String purchasedItems, String orderDate, String orderMonth, String orderTime, double total, String status) {
            this.orderId = orderId;
            this.username = username;
            this.purchasedItems = purchasedItems;
            this.orderDate = orderDate;
            this.orderMonth = orderMonth;
            this.orderTime = orderTime;
            this.total = total;
            this.status = status;
        }
    }

    private static final class CouponDto implements Serializable {
        private static final long serialVersionUID = 1L;
        final String code;
        final String description;
        final double discountPercent;
        final String targetUsers;
        final String conditions;
        final boolean active;

        CouponDto(String code, String description, double discountPercent, String targetUsers, String conditions, boolean active) {
            this.code = code;
            this.description = description;
            this.discountPercent = discountPercent;
            this.targetUsers = targetUsers;
            this.conditions = conditions;
            this.active = active;
        }
    }

    private static void hydrateCoupons() {
        coupons.clear();
        if (rootDocument != null && rootDocument.coupons != null) {
            for (CouponDto dto : rootDocument.coupons) {
                coupons.add(new DiscountCoupon(
                        dto.code,
                        dto.description,
                        dto.discountPercent,
                        dto.targetUsers,
                        dto.conditions,
                        dto.active
                ));
            }
        }

        boolean changed = false;
        if (coupons.isEmpty()) {
            coupons.add(new DiscountCoupon(
                    "WELCOME10",
                    "Giảm 10% cho khách hàng mới",
                    10,
                    "Khách hàng mới",
                    "Đơn từ 150,000đ",
                    true
            ));
            coupons.add(new DiscountCoupon(
                    "VIP15",
                    "Ưu đãi 15% dành cho thành viên VIP",
                    15,
                    "Thành viên VIP",
                    "Đơn từ 300,000đ",
                    true
            ));
            changed = true;
        }

        changed |= ensureSampleCoupon("ALL5", "Giảm 5% cho mọi tài khoản", 5, "Tất cả", "Đơn từ 50,000đ", true);
        changed |= ensureSampleCoupon("SAVE12", "Giảm 12% khi mua đơn trung bình", 12, "Tất cả", "Đơn từ 200,000đ", true);
        changed |= ensureSampleCoupon("BIG25", "Giảm mạnh cho đơn lớn", 25, "Tất cả", "Đơn từ 700,000đ", true);
        changed |= ensureSampleCoupon("STUDENT8", "Ưu đãi học sinh sinh viên", 8, "Tất cả", "Đơn từ 100,000đ", true);
        changed |= ensureSampleCoupon("INACTIVE15", "Mã thử trạng thái tắt", 15, "Tất cả", "Đơn từ 120,000đ", false);

        if (changed) {
            persist();
        }

        coupons.addListener((ListChangeListener<DiscountCoupon>) c -> persist());
    }

    private static boolean ensureSampleCoupon(
            String code,
            String description,
            double discountPercent,
            String targetUsers,
            String conditions,
            boolean active
    ) {
        for (DiscountCoupon coupon : coupons) {
            if (coupon.codeProperty().get().equalsIgnoreCase(code)) {
                return false;
            }
        }
        coupons.add(new DiscountCoupon(code, description, discountPercent, targetUsers, conditions, active));
        return true;
    }
}
