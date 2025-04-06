package com.hitachi.assessment.dto;

import com.hitachi.assessment.model.DroneModel;
import com.hitachi.assessment.model.DroneState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DroneDTO {

    private Long id;

    @NotNull(message = "Serial number is required")
    @Size(min = 1, max = 100, message = "Serial number must be between 1 and 100 characters")
    private String serialNumber;

    @NotNull(message = "Model is required")
    private DroneModel model;

    @Min(value = 0, message = "Weight limit cannot be negative")
    @Max(value = 1000, message = "Weight limit cannot exceed 1000g")
    private Integer weightLimit;

    @Min(value = 0, message = "Battery capacity cannot be negative")
    @Max(value = 100, message = "Battery capacity cannot exceed 100%")
    private Integer batteryCapacity;

    private DroneState state;

    private Integer currentLoad;
}