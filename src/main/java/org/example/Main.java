package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        // Đã xóa dấu chấm phẩy thừa ở cuối dòng
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/dang-nhap.fxml"));

        // Tạo một cửa sổ (Scene) với kích thước rộng 1000px, cao 700px
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);

        // Đặt tiêu đề cho cửa sổ phần mềm
        stage.setTitle("Hệ Thống Nhà Sách Trực Tuyến");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(); // Lệnh kích hoạt giao diện JavaFX
    }
}