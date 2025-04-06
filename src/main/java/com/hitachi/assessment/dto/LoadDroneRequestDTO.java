package com.hitachi.assessment.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoadDroneRequestDTO {

    @NotNull(message = "Drone ID is required")
    private Long droneId;

    @NotEmpty(message = "At least one medication must be provided")
    private List<Long> medicationIds;
}