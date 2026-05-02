package org.example;

public class UserSession {
    // Biến static giúp dữ liệu tồn tại xuyên suốt quá trình chạy App
    private static String currentUsername = null;

    public static void setUsername(String username) {
        if (username == null) {
            currentUsername = null;
            return;
        }
        String t = username.trim();
        currentUsername = t.isEmpty() ? null : t;
    }

    public static String getUsername() {
        return currentUsername;
    }

    public static void clear() {
        currentUsername = null;
    }

    public static boolean isLogged() {
        return currentUsername != null;
    }
}