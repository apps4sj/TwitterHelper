package com.apps4sj.TwitterSeller;

public class Listing {

    private String id;
    private String productName;
    private String productDescription;
    private String email;
    private String location;
    private String phone;
    private String price;
    private String image1;
    private String image2;
    private String image3;
    private String datePosted;

    public Listing(String id, String productName, String productPrice, String datePosted) {
        setId(id);
        setProductName(productName);
        setPrice(productPrice);
        setDatePosted(datePosted);
    }

    public Listing(String id, String name, String desc, String em, String lo, String ph, String pr, String i1, String i2, String i3, String date) {
        setId(id);
        setProductName(name);
        setProductDescription(desc);
        setEmail(em);
        setLocation(lo);
        setPhone(ph);
        setPrice(pr);
        setImage1(i1);
        setImage2(i2);
        setImage3(i3);
        setDatePosted(date);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(String productPrice) {
        this.price = productPrice;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
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

    public String getProductDescription() {
        return productDescription;
    }

    public String getEmail() {
        return email;
    }

    public String getLocation() {
        return location;
    }

    public String getPhone() {
        return phone;
    }

    public String getPrice() {
        return price;
    }

    public String getImage1() {
        return image1;
    }

    public String getImage2() {
        return image2;
    }

    public String getImage3() {
        return image3;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public String toString() {
        return getDatePosted() + " " + getProductName() + " for $" + getPrice() + "  ID:" + getId();
    }
}
