package com.etpa.electric.entity;

import com.etpa.electric.utils.Month;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Fraction {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private String id;

    private String profile;
    private BigDecimal fraction;

    @Transient
    private transient Month month;
    @Column(name="month")
    private int monthCode;

    public String getId() {
        return id;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public BigDecimal getFraction() {
        return fraction;
    }

    public void setFraction(BigDecimal fraction) {
        this.fraction = fraction;
    }

    /**
     * JPA hooks to map enums to and from integers
     */

    @PrePersist
    private void populateDBFields(){
        monthCode = month.getCode();
    }

    @PostLoad
    private void populateTransientFields(){
        month = Month.valueOf(monthCode);
    }

}
