package org.example;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminThongKeController {
    @FXML private Label lblDoanhThu;
    @FXML private Label lblDonCho;
    @FXML private Label lblDonDaDuyet;
    @FXML private Label lblTongDoanhThuTatCa;
    @FXML private TableView<CartController.Order> tblAllOrders;
    @FXML private TableColumn<CartController.Order, String> colOrderId;
    @FXML private TableColumn<CartController.Order, String> colOrderDate;
    @FXML private TableColumn<CartController.Order, String> colOrderCustomer;
    @FXML private TableColumn<CartController.Order, String> colOrderItems;
    @FXML private TableColumn<CartController.Order, Double> colOrderTotal;
    @FXML private TableColumn<CartController.Order, String> colOrderStatus;
    @FXML private BarChart<String, Number> chartRevenueByTypePerDay;

    private final DecimalFormat moneyFmt = new DecimalFormat("#,###đ");

    @FXML
    public void initialize() {
        ObservableList<CartController.Order> allOrders = CustomerAccountStore.getAllOrders();
        long pending = allOrders.stream()
                .filter(order -> "Chờ xác nhận".equals(order.statusProperty().get()))
                .count();
        long approved = allOrders.stream()
                .filter(order -> "Đã duyệt".equals(order.statusProperty().get()))
                .count();
        double revenue = allOrders.stream()
                .filter(order -> "Đã duyệt".equals(order.statusProperty().get()))
                .mapToDouble(order -> order.totalProperty().get())
                .sum();

        lblDoanhThu.setText(String.format("%,.0f đ", revenue));
        lblDonCho.setText(String.valueOf(pending));
        lblDonDaDuyet.setText(String.valueOf(approved));
        lblTongDoanhThuTatCa.setText("Tổng doanh thu đơn đã duyệt: " + moneyFmt.format(revenue));

        setupOrdersTable(allOrders);
        buildRevenueChart(allOrders);
    }

    private void setupOrdersTable(ObservableList<CartController.Order> allOrders) {
        colOrderId.setCellValueFactory(cell -> cell.getValue().orderIdProperty());
        colOrderDate.setCellValueFactory(cell -> cell.getValue().orderDateProperty());
        colOrderCustomer.setCellValueFactory(cell -> cell.getValue().usernameProperty());
        colOrderItems.setCellValueFactory(cell -> cell.getValue().purchasedItemsProperty());
        colOrderTotal.setCellValueFactory(cell -> cell.getValue().totalProperty().asObject());
        colOrderStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());
        colOrderTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : moneyFmt.format(item));
            }
        });

        tblAllOrders.setItems(allOrders);
    }

    private void buildRevenueChart(ObservableList<CartController.Order> allOrders) {
        chartRevenueByTypePerDay.getData().clear();
        Map<String, Map<String, Double>> revenueByTypeByDate = new LinkedHashMap<>();

        for (CartController.Order order : allOrders) {
            if (!"Đã duyệt".equals(order.statusProperty().get())) {
                continue;
            }
            String date = order.orderDateProperty().get();
            Map<String, Double> typeRevenue = revenueByTypeByDate.computeIfAbsent(date, d -> new LinkedHashMap<>());
            List<OrderBookLine> lines = parseOrderItems(order.purchasedItemsProperty().get());
            for (OrderBookLine line : lines) {
                String type = findBookType(line.bookName());
                double amount = line.lineTotal();
                typeRevenue.put(type, typeRevenue.getOrDefault(type, 0.0) + amount);
            }
        }

        Map<String, XYChart.Series<String, Number>> seriesByType = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Double>> byDate : revenueByTypeByDate.entrySet()) {
            String date = byDate.getKey();
            for (Map.Entry<String, Double> byType : byDate.getValue().entrySet()) {
                String type = byType.getKey();
                XYChart.Series<String, Number> series = seriesByType.computeIfAbsent(type, t -> {
                    XYChart.Series<String, Number> s = new XYChart.Series<>();
                    s.setName(t);
                    return s;
                });
                series.getData().add(new XYChart.Data<>(date, byType.getValue()));
            }
        }

        chartRevenueByTypePerDay.getData().addAll(seriesByType.values());
    }

    private List<OrderBookLine> parseOrderItems(String raw) {
        List<OrderBookLine> lines = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return lines;
        }

        String[] tokens = raw.split("\\|");
        for (String token : tokens) {
            String normalized = token.trim();
            if (normalized.isEmpty()) continue;

            String[] parts = normalized.split(" - ");
            if (parts.length < 2) continue;

            String nameAndQty = parts[0].trim();
            if (nameAndQty.startsWith("[")) {
                int closeBracket = nameAndQty.indexOf(']');
                if (closeBracket > 1) {
                    nameAndQty = nameAndQty.substring(closeBracket + 1).trim();
                }
            }
            String amountPart = parts[1].replaceAll("[^0-9]", "");
            double amount = amountPart.isEmpty() ? 0 : Double.parseDouble(amountPart);
            String bookName = nameAndQty.contains(" x") ? nameAndQty.substring(0, nameAndQty.lastIndexOf(" x")).trim() : nameAndQty;
            lines.add(new OrderBookLine(bookName, amount));
        }
        return lines;
    }

    private String findBookType(String bookName) {
        if (bookName == null || bookName.isBlank()) return "Khác";
        String lowered = bookName.toLowerCase(Locale.ROOT);
        for (Sach sach : BookCatalog.getAllBooks()) {
            if (sach.getTenSach().toLowerCase(Locale.ROOT).equals(lowered)) {
                return sach.getTheLoai();
            }
        }
        return "Khác";
    }

    private record OrderBookLine(String bookName, double lineTotal) {}
}
