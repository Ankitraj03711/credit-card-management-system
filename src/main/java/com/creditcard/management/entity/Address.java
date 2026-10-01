package com.ccms.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID addressId;

    @Column( nullable = false, length = 20)
    private String buildingNumber;

    @Column(nullable = false, length = 30)
    private String town;

    @Column(nullable = false, length = 10)
    private String pinCode;

    @Column(nullable = false, length = 30)
    private String district;

    @Column(nullable = false, length = 30)
    private String state;

    @Column(nullable = false, length = 20)
    private String country;

    @ManyToOne
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customer;

    public Address() {
    }

    public Address(String buildingNumber, String town, String pinCode,
                   String district, String state, String country,
                   Customer customer) {
        this.buildingNumber = buildingNumber;
        this.town = town;
        this.pinCode = pinCode;
        this.district = district;
        this.state = state;
        this.country = country;
        this.customer = customer;
    }

    public UUID getAddressId() {
        return addressId;
    }

    public void setAddressId(UUID addressId) {
        this.addressId = addressId;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}