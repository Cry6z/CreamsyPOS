package com.example.creamsy;

public class Product {
    private int id;
    private String nama;
    private double harga;
    private int stok;
    private String fotoUri;

    // Constructor kosong
    public Product() {
    }

    // Constructor lengkap
    public Product(int id, String nama, double harga, int stok, String fotoUri) {
        this.id = id;
        this.nama = nama;
        this.harga = harga;
        this.stok = stok;
        this.fotoUri = fotoUri;
    }

    // Constructor tanpa id (untuk insert baru)
    public Product(String nama, double harga, int stok, String fotoUri) {
        this.nama = nama;
        this.harga = harga;
        this.stok = stok;
        this.fotoUri = fotoUri;
    }

    // Getter dan Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public String getFotoUri() {
        return fotoUri;
    }

    public void setFotoUri(String fotoUri) {
        this.fotoUri = fotoUri;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", nama='" + nama + '\'' +
                ", harga=" + harga +
                ", stok=" + stok +
                ", fotoUri='" + fotoUri + '\'' +
                '}';
    }
}
