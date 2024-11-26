package com.etpa.electric.dto;

import com.etpa.electric.utils.Month;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * annotation based validation used to trap trivial errors early on before the DTO has reached the service layer
 * and the more complex validations are done.
 */
public class MetreReadingDTO implements Cloneable, Comparable<MetreReadingDTO> {

    private String id;
    @NotBlank(message = "MetreId cannot be blank")
    private String metreId;
    @NotBlank(message = "Profile cannot be blank")
    private String profile;
    @NotBlank(message = "Month cannot be blank")
    private String month;
    @DecimalMin(value = "0.0")
    private BigDecimal metreReading;

    /**
     * used to pass DTO from client, ID would be a path parameter not included in DTO
     * @param metreId
     * @param profile
     * @param month
     * @param metreReading
     */
    @JsonCreator
    public MetreReadingDTO( String metreId,
                            String profile,
                            String month,
                            BigDecimal metreReading){
        this("-1", metreId, profile, Month.valueOf(month), metreReading);
    }

    /**
     * Used from the code to instantiate a response DTO, therefore not a @JsonCreator
     * @param id
     * @param metreId
     * @param profile
     * @param month
     * @param metreReading
     */
    public MetreReadingDTO( String id,
                            String metreId,
                            String profile,
                            Month month,
                            BigDecimal metreReading){
        this.id = id;
        this.month = month.name();
        this.profile = profile;
        this.metreId = metreId;
        this.metreReading = metreReading;
    }

    public MetreReadingDTO(){
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("metre_id")
    public String getMetreId() {
        return metreId;
    }

    @JsonProperty("profile")
    public String getProfile() {
        return profile;
    }

    @JsonProperty("month")
    public Month getMonth() {
        return Month.valueOf(month);
    }

    @JsonProperty("metre_reading")
    public BigDecimal getMetreReading() {
        return metreReading;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMetreId(String metreId) {
        this.metreId = metreId;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setMetreReading(BigDecimal metreReading) {
        this.metreReading = metreReading;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int compareTo(MetreReadingDTO other) {
        return getMonth().getCode().compareTo(other.getMonth().getCode());
    }

}
