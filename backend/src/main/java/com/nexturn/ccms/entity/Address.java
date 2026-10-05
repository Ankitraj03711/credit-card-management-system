package com.nexturn.ccms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
        name = "address_id",
        nullable = false,
        updatable = false
    )
    private UUID addressId;

    @Column(
        name = "home_number",
        nullable = false,
        length = 20
    )
    private String homeNumber;

    @Column(
        nullable = false,
        length = 30
    )
    private String town;

    @Column(
        name = "pin_code",
        nullable = false,
        length = 10
    )
    private String pinCode;

    @Column(
        nullable = false,
        length = 30
    )
    private String district;

    @Column(
        nullable = false,
        length = 30
    )
    private String state;

    @Column(
        nullable = false,
        length = 20
    )
    private String country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "customer_id",
        nullable = false
    )
    private Customer customer;


    
    public Address() {
        // Required by JPA/Hibernate.
    }


    public UUID getAddressId() {
        return addressId;
    }

    public void setAddressId(UUID addressId) {
        this.addressId = addressId;
    }

    public String getHomeNumber() {
        return homeNumber;
    }

    public void setHomeNumber(String homeNumber) {
        this.homeNumber = homeNumber;
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