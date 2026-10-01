package com.nexturn.ccms.entity;

import com.nexturn.ccms.enums.CustomerStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @Column(updatable = false)
    private Integer customerId;

    @Column(nullable = false, length = 100)
    private String firstName;
    
    @Column(length = 100)
    private String middleName;
    
    @Column(length = 100)
    private String lastName;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false)
    private Double annualIncome;

    @OneToOne
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false, unique = true)
    private UserLogin userLogin;

    @Enumerated(EnumType.STRING)
    @Column( nullable = false)
    private CustomerStatus customerStatus;

    
}