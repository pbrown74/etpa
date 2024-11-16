package com.etpa.electric.entity;

import com.etpa.electric.utils.Month;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * hibernate entity used to persist the Consumption
 */
@Entity
public class Consumption {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private String id;

    private String metreId;
    private String profile;
    private BigDecimal consumption;

    @Transient
    private transient Month month;
    @Column(name="month")
    private int monthCode;

    public String getId() {
        return id;
    }

    public String getMetreId() {
        return metreId;
    }

    public void setMetreId(String metreId) {
        this.metreId = metreId;
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

    public BigDecimal getConsumption() {
        return consumption;
    }

    public void setConsumption(BigDecimal consumption) {
        this.consumption = consumption;
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
