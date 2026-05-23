package com.alfonso.flexcommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Column(length = 1000)
    private String description;

    @NotBlank(message = "La categoria es obligatoria")
    private String category;

    private String sku;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    private BigDecimal price;

    @NotNull(message = "El IVA es obligatorio")
    @DecimalMin(value = "0.00", message = "El IVA no puede ser negativo")
    private BigDecimal ivaPercent = new BigDecimal("21.00");

    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stock = 10;

    private String imageUrl;

    private boolean active = true;

    public Product() {
    }

    public Product(String name, String description, String category, BigDecimal price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.active = true;
        this.stock = 10;
        this.ivaPercent = new BigDecimal("21.00");
    }

    public Product(String name, String description, String category, BigDecimal price, BigDecimal ivaPercent, int stock, String imageUrl) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.ivaPercent = ivaPercent;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.active = true;
    }

    public BigDecimal getPriceWithoutIva() {
        if (price == null || ivaPercent == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal divisor = BigDecimal.ONE.add(ivaPercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        return price.divide(divisor, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getIvaAmount() {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        return price.subtract(getPriceWithoutIva()).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isAvailable() {
        return active && stock > 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getIvaPercent() {
        return ivaPercent;
    }

    public void setIvaPercent(BigDecimal ivaPercent) {
        this.ivaPercent = ivaPercent;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
