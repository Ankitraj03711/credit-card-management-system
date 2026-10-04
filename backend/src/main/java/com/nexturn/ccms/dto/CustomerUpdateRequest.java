package com.nexturn.ccms.dto;

import java.math.BigDecimal;

public class CustomerUpdateRequest {

    private String name;
    private String phone;
    private String address;
    private BigDecimal annualIncome;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(
            BigDecimal annualIncome) {

        this.annualIncome = annualIncome;
    }
}