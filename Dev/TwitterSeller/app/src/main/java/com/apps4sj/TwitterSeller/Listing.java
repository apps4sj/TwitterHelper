package com.apps4sj.TwitterSeller;

public class Listing {

    private String id;
    private String productName;
    private String productPrice;
    private String datePosted;

    public Listing(String id, String productName, String productPrice, String datePosted) {
        setId(id);
        setProductName(productName);
        setProductPrice(productPrice);
        setDatePosted(datePosted);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public void setDatePosted(String datePosted) {
        this.datePosted = datePosted;
    }

    public String getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public String toString() {
        return getDatePosted() + " " + getProductName() + " for $" + getProductPrice() + "  ID:" + getId();
    }
}
