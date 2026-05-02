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

    /** Bản sao dữ liệu đã đọc từ file; dùng khi user chưa mở giỏ/đơn trong phiên này. */
    private static RootDocument rootDocument = loadFromDisk();

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

    private static ObservableList<CartController.CartItem> createHydratedCart(String user) {
        ObservableList<CartController.CartItem> list = FXCollections.observableArrayList(
                item -> new Observable[]{
                        item.productNameProperty(),
                        item.priceProperty(),
                        item.quantityProperty(),
                        item.selectedProperty()
                });
        UserSnapshot snap = snapshotFor(user);
        if (snap != null) {
            for (CartLineDto line : snap.cart) {
                CartController.CartItem it = new CartController.CartItem(line.productName, line.price, line.quantity);
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
    }

    private static final class UserSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        final List<CartLineDto> cart = new ArrayList<>();
        final List<OrderLineDto> orders = new ArrayList<>();
    }

    private static final class CartLineDto implements Serializable {
        private static final long serialVersionUID = 1L;
        final String productName;
        final double price;
        final int quantity;
        final boolean selected;

        CartLineDto(String productName, double price, int quantity, boolean selected) {
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.selected = selected;
        }
    }

    private static final class OrderLineDto implements Serializable {
        private static final long serialVersionUID = 1L;
        final String orderId;
        final String orderDate;
        final String orderMonth;
        final String orderTime;
        final double total;
        final String status;

        OrderLineDto(String orderId, String orderDate, String orderMonth, String orderTime, double total, String status) {
            this.orderId = orderId;
            this.orderDate = orderDate;
            this.orderMonth = orderMonth;
            this.orderTime = orderTime;
            this.total = total;
            this.status = status;
        }
    }
}
