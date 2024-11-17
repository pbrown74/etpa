package com.etpa.electric.dto;

import com.etpa.electric.utils.Month;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * the annotations used here would be useful if we had implement all the CRUD methods on the controller
 * at the entity level rather than at file level, then these annotations are used to do validation early
 * on, before the service layer is reached.
 */
public class ConsumptionDTO {

    @NotBlank(message = "Id cannot be blank")
    private String id;
    @NotBlank(message = "MetreId cannot be blank")
    private String metreId;
    @NotBlank(message = "Profile cannot be blank")
    private String profile;
    @NotBlank(message = "Month cannot be blank")
    private String month;
    @DecimalMin(value = "0.0")
    private BigDecimal consumption;

    @JsonCreator
    public ConsumptionDTO(String metreId,
                          String month,
                          String profile,
                          BigDecimal consumption){
        this("-1", metreId, month, profile, consumption);
    }

    public ConsumptionDTO(String id,
                          String metreId,
                          String month,
                          String profile,
                          BigDecimal consumption){
        this.id = id;
        this.metreId = metreId;
        this.month = month;
        this.profile = profile;
        this.consumption = consumption;
    }

    @JsonProperty("metre_id")
    public String getMetreId() {
        return metreId;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("month")
    public Month getMonth() {
        return Month.from(month);
    }

    @JsonProperty("consumption")
    public BigDecimal getConsumption() {
        return consumption;
    }

    @JsonProperty("profile")
    public String getProfile() {
        return profile;
    }

}
