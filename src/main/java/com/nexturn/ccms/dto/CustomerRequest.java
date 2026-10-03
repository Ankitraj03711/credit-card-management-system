package com.nexturn.ccms.dto;

import com.nexturn.ccms.enums.CustomerStatus;

import java.math.BigDecimal;

public class CustomerRequest {

    private String name;
    private String phone;
    private BigDecimal annualIncome;
    private String email;
    private CustomerStatus customerStatus;

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

    public BigDecimal getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(BigDecimal annualIncome) {
        this.annualIncome = annualIncome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CustomerStatus getCustomerStatus() {
        return customerStatus;
    }

    public void setCustomerStatus(
            CustomerStatus customerStatus) {

        this.customerStatus = customerStatus;
    }
}