package com.hitachi.assessment.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "medications")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "Name can only contain letters, numbers, hyphen and underscore")
    private String name;

    @NotNull
    @Min(1)
    private Integer weight;

    @NotNull
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Code can only contain uppercase letters, underscore and numbers")
    private String code;

    @Lob
    @Column(name = "image_data", length = 1000)
    private byte[] image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drone_id")
    @ToString.Exclude
    private Drone drone;
}