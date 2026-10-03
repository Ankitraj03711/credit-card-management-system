package com.nexturn.ccms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table
public class IdGenerator {

    @Id
    private String name;

    @Column(nullable = false)
    private Long currentValue;

    public IdGenerator() {
		// TODO Auto-generated constructor stub
	}

	public IdGenerator(String name, Long currentValue) {
		this.name = name;
		this.currentValue = currentValue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(Long currentValue) {
		this.currentValue = currentValue;
	}
    
    
}