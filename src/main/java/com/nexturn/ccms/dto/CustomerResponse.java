package com.nexturn.ccms.dto;

import com.nexturn.ccms.enums.CustomerStatus;

import java.math.BigDecimal;

public class CustomerResponse {

    private Integer customerId;
    private String name;
    private String phone;
    private BigDecimal annualIncome;
    private String email;
    private CustomerStatus customerStatus;

    public CustomerResponse() {
    }

    public CustomerResponse(
            Integer customerId,
            String name,
            String phone,
            BigDecimal annualIncome,
            String email,
            CustomerStatus customerStatus) {

        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.annualIncome = annualIncome;
        this.email = email;
        this.customerStatus = customerStatus;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

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

    public void setAnnualIncome(
            BigDecimal annualIncome) {

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