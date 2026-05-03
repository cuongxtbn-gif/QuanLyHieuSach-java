package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Định dạng mỗi dòng trong chuỗi {@code purchasedItems}:
 * {@code [bookId]Tên sách x3 - 123,456đ} (có id) hoặc tương thích cũ {@code Tên sách x3 - 123,456đ} (không id).
 * Các dòng nối bằng {@code " | "}.
 */
public final class OrderStockUtil {

    private OrderStockUtil() {}

    public static String formatOrderLine(String bookId, String bookName, int qty, String formattedMoney) {
        String name = bookName == null ? "" : bookName.trim();
        String money = formattedMoney == null ? "" : formattedMoney;
        String core = name + " x" + qty + " - " + money;
        if (bookId != null && !bookId.isBlank()) {
            return "[" + bookId.trim() + "]" + core;
        }
        return core;
    }

    /**
     * Kiểm tra có đủ tồn kho và parse được dòng đơn hay không (chưa trừ kho).
     */
    public static Optional<String> validateStockForOrder(String purchasedItemsRaw) {
        List<ParsedLine> lines = parsePurchasedItems(purchasedItemsRaw);
        if (lines.isEmpty()) {
            return Optional.of("Đơn hàng không có dòng sách hợp lệ để trừ kho.");
        }
        for (ParsedLine line : lines) {
            Optional<Sach> sachOpt = resolveBook(line);
            if (sachOpt.isEmpty()) {
                return Optional.of("Không tìm thấy sách trong danh mục: " + line.nameForMessage());
            }
            Sach s = sachOpt.get();
            if (s.getTonKho() < line.qty) {
                return Optional.of("«" + s.getTenSach() + "» chỉ còn " + s.getTonKho()
                        + " cuốn, đơn cần " + line.qty + " cuốn.");
            }
        }
        return Optional.empty();
    }

    /** Trừ kho sau khi đã {@link #validateStockForOrder(String)} và đã lưu trạng thái đơn. */
    public static void applyDeductStockForOrder(String purchasedItemsRaw) {
        List<ParsedLine> lines = parsePurchasedItems(purchasedItemsRaw);
        for (ParsedLine line : lines) {
            resolveBook(line).ifPresent(s -> s.setTonKho(s.getTonKho() - line.qty));
        }
    }

    private static List<ParsedLine> parsePurchasedItems(String raw) {
        List<ParsedLine> out = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return out;
        }
        for (String token : raw.split("\\|")) {
            String t = token.trim();
            if (t.isEmpty()) {
                continue;
            }
            String bookId = null;
            if (t.startsWith("[")) {
                int close = t.indexOf(']');
                if (close > 1) {
                    bookId = t.substring(1, close).trim();
                    t = t.substring(close + 1).trim();
                }
            }
            int sep = t.lastIndexOf(" x");
            if (sep < 0) {
                continue;
            }
            String namePart = t.substring(0, sep).trim();
            String afterX = t.substring(sep + 2).trim();
            int dashIdx = afterX.indexOf(" - ");
            if (dashIdx < 0) {
                continue;
            }
            try {
                int qty = Integer.parseInt(afterX.substring(0, dashIdx).trim());
                if (qty <= 0) {
                    continue;
                }
                out.add(new ParsedLine(bookId, namePart, qty));
            } catch (NumberFormatException ignored) {
                // bỏ qua dòng lỗi
            }
        }
        return out;
    }

    private static Optional<Sach> resolveBook(ParsedLine line) {
        if (line.bookId != null && !line.bookId.isBlank()) {
            Optional<Sach> byId = BookCatalog.findById(line.bookId);
            if (byId.isPresent()) {
                return byId;
            }
        }
        if (line.bookName == null || line.bookName.isBlank()) {
            return Optional.empty();
        }
        for (Sach s : BookCatalog.getAllBooks()) {
            if (line.bookName.equalsIgnoreCase(s.getTenSach())) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    private static final class ParsedLine {
        final String bookId;
        final String bookName;
        final int qty;

        ParsedLine(String bookId, String bookName, int qty) {
            this.bookId = bookId;
            this.bookName = bookName;
            this.qty = qty;
        }

        String nameForMessage() {
            if (bookName != null && !bookName.isBlank()) {
                return bookName;
            }
            if (bookId != null && !bookId.isBlank()) {
                return "mã " + bookId;
            }
            return "(không tên)";
        }
    }
}
