package com.hitachi.assessment.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationDTO {

    private Long id;

    @NotNull(message = "Name is required")
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "Name can only contain letters, numbers, hyphen and underscore")
    private String name;

    @NotNull(message = "Weight is required")
    @Min(value = 1, message = "Weight must be at least 1g")
    private Integer weight;

    @NotNull(message = "Code is required")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Code can only contain uppercase letters, underscore and numbers")
    private String code;

    private String imageBase64;

    private MultipartFile imageFile;

    private Long droneId;
}