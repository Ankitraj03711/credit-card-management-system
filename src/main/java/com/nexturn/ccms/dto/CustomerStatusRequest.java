package com.nexturn.ccms.dto;

import com.nexturn.ccms.enums.CustomerStatus;

public class CustomerStatusRequest {

    private CustomerStatus status;

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }
}