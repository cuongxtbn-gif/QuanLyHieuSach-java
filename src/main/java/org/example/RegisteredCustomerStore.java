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
import java.util.HashMap;
import java.util.Map;

/**
 * Email + mật khẩu khách đã đăng ký (lưu file để giữ giữa các lần chạy app).
 */
public final class RegisteredCustomerStore {

    private static final Path DATA_DIR = Path.of(System.getProperty("user.home"), ".quanlyhieusach");
    private static final Path DATA_FILE = DATA_DIR.resolve("registered-customers.ser");
    private static final Path TEMP_FILE = DATA_DIR.resolve("registered-customers.ser.tmp");

    private static Map<String, String> accounts = load();

    static {
        // Tài khoản thử nghiệm: coi như đã đăng ký, giữ nguyên khóa với lịch sử trong CustomerAccountStore
        ensureRegistered("cuongxtbn@gmail.com", "12345678");
    }

    private RegisteredCustomerStore() {}

    public static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    /** @return true nếu tạo mới thành công */
    public static boolean register(String email, String password) {
        String key = normalizeEmail(email);
        if (key.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        if (accounts.containsKey(key)) {
            return false;
        }
        accounts.put(key, password);
        persist();
        return true;
    }

    public static boolean isRegistered(String email) {
        return accounts.containsKey(normalizeEmail(email));
    }

    public static boolean verify(String email, String password) {
        String key = normalizeEmail(email);
        String stored = accounts.get(key);
        return stored != null && stored.equals(password);
    }

    /**
     * Đảm bảo email có trong danh sách (không ghi đè mật khẩu nếu đã tồn tại — để user đổi qua đăng ký/file).
     */
    private static void ensureRegistered(String email, String defaultPassword) {
        String key = normalizeEmail(email);
        if (!key.isEmpty() && !accounts.containsKey(key)) {
            accounts.put(key, defaultPassword);
            persist();
        }
    }

    private static synchronized void persist() {
        try {
            Files.createDirectories(DATA_DIR);
            try (OutputStream out = Files.newOutputStream(TEMP_FILE);
                 ObjectOutputStream oos = new ObjectOutputStream(out)) {
                oos.writeObject(new HashMap<>(accounts));
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

    @SuppressWarnings("unchecked")
    private static Map<String, String> load() {
        if (!Files.isRegularFile(DATA_FILE)) {
            return new HashMap<>();
        }
        try (InputStream in = Files.newInputStream(DATA_FILE);
             ObjectInputStream ois = new ObjectInputStream(in)) {
            Object o = ois.readObject();
            if (o instanceof Map<?, ?> map) {
                Map<String, String> out = new HashMap<>();
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    if (e.getKey() != null && e.getValue() != null) {
                        out.put(e.getKey().toString(), e.getValue().toString());
                    }
                }
                return out;
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
}
