package com.example.creamsy;

public class CartItem {
    private String id; // Changed to String for UUID support
    private String productId; // Changed to String for UUID support
    private int quantity;
    private String createdAt;
    
    // These fields will be populated from joined product data
    private String productName;
    private double productPrice;
    private String productImageUri;

    public CartItem() {
    }

    public CartItem(String id, String productId, int quantity, String productName, double productPrice, String productImageUri) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productImageUri = productImageUri;
    }
    
    // Legacy constructor for backward compatibility with int IDs
    public CartItem(int id, int productId, int quantity, String productName, double productPrice, String productImageUri) {
        this.id = String.valueOf(id);
        this.productId = String.valueOf(productId);
        this.quantity = quantity;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productImageUri = productImageUri;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductImageUri() {
        return productImageUri;
    }

    public void setProductImageUri(String productImageUri) {
        this.productImageUri = productImageUri;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public double getTotalPrice() {
        return productPrice * quantity;
    }
}
