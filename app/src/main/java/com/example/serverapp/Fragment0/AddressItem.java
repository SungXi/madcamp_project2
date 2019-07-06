package com.example.serverapp.Fragment0;

public class AddressItem {
    private String name, number, email;
    private int imageID;

    public AddressItem(String name,
                       String number,
                       String email,
                       int imageID) {
        this.name = name;
        this.number = number;
        this.email = email;
        this.imageID = imageID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int id) {
        this.imageID = id;
    }
}
