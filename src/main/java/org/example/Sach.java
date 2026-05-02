package org.example;

public class Sach {
    private String id;
    private String tenSach;
    private double giaBan;
    private String hinhAnh;
    private String tacGia;
    private String theLoai;

    public Sach(String id, String tenSach, double giaBan, String hinhAnh, String tacGia, String theLoai) {
        this.id = id;
        this.tenSach = tenSach;
        this.giaBan = giaBan;
        this.hinhAnh = hinhAnh;
        this.tacGia = tacGia;
        this.theLoai = theLoai;
    }

    public String getTenSach() { return tenSach; }
    public double getGiaBan() { return giaBan; }
    public String getHinhAnh() { return hinhAnh; }
}