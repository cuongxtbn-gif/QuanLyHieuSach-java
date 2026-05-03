package org.example;

public class Sach {
    private String id, tenSach, hinhAnh, tacGia, theLoai, nhaXuatBan, moTa;
    private double giaBan;
    private int soTrang;
    private int tonKho;
    /** Đánh dấu xóa mềm — không hiển thị cho khách hàng. */
    private boolean deleted;

    // PHẢI CÓ ĐỦ 9 THAM SỐ TRONG NÀY
    public Sach(String id, String tenSach, double giaBan, String hinhAnh, String tacGia, String theLoai, String nhaXuatBan, int soTrang, String moTa) {
        this(id, tenSach, giaBan, hinhAnh, tacGia, theLoai, nhaXuatBan, soTrang, moTa, 10);
    }

    public Sach(String id, String tenSach, double giaBan, String hinhAnh, String tacGia, String theLoai, String nhaXuatBan, int soTrang, String moTa, int tonKho) {
        this.id = id;
        this.tenSach = tenSach;
        this.giaBan = giaBan;
        this.hinhAnh = hinhAnh;
        this.tacGia = tacGia;
        this.theLoai = theLoai;
        this.nhaXuatBan = nhaXuatBan;
        this.soTrang = soTrang;
        this.moTa = moTa;
        this.tonKho = Math.max(0, tonKho);
        this.deleted = false;
    }

    // Getters (Để trang Chi tiết lấy được dữ liệu)
    public String getId() { return id; }
    public String getTenSach() { return tenSach; }
    public double getGiaBan() { return giaBan; }
    public String getHinhAnh() { return hinhAnh; }
    public String getTacGia() { return tacGia; }
    public String getTheLoai() { return theLoai; }
    public String getNhaXuatBan() { return nhaXuatBan; }
    public int getSoTrang() { return soTrang; }
    public String getMoTa() { return moTa; }
    public int getTonKho() { return tonKho; }
    public void setTonKho(int tonKho) { this.tonKho = Math.max(0, tonKho); }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public void setTenSach(String tenSach) { this.tenSach = tenSach; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    public void setTacGia(String tacGia) { this.tacGia = tacGia; }
    public void setTheLoai(String theLoai) { this.theLoai = theLoai; }
    public void setNhaXuatBan(String nhaXuatBan) { this.nhaXuatBan = nhaXuatBan; }
    public void setSoTrang(int soTrang) { this.soTrang = Math.max(1, soTrang); }
    public void setMoTa(String moTa) { this.moTa = moTa; }
}