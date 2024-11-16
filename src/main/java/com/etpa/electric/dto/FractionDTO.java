package com.etpa.electric.dto;

import com.etpa.electric.utils.Month;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class FractionDTO {

    private String id;
    @NotBlank(message = "Message cannot be blank")
    private String month;
    @NotBlank(message = "Profile cannot be blank")
    private String profile;
    @DecimalMin(value = "0.0")
    private BigDecimal fraction;

    @JsonCreator
    public FractionDTO(String month,
                       String profile,
                       BigDecimal fraction){
        this("-1", month, profile, fraction);
    }

    public FractionDTO(String id,
                       String month,
                       String profile,
                       BigDecimal fraction){
        this.id = id;
        this.month = month;
        this.profile = profile;
        this.fraction = fraction;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("profile")
    public String getProfile() {
        return profile;
    }

    @JsonProperty("month")
    public Month getMonth() {
        return Month.valueOf(month);
    }

    @JsonProperty("fraction")
    public BigDecimal getFraction() {
        return fraction;
    }

}
