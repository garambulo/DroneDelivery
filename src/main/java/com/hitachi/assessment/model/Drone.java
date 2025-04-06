package com.hitachi.assessment.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "drones")
public class Drone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(unique = true)
    private String serialNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DroneModel model;

    @NotNull
    @Min(0)
    @Max(1000)
    private Integer weightLimit;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer batteryCapacity;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DroneState state;

    @OneToMany(mappedBy = "drone", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Medication> medications = new ArrayList<>();

    // Helper method to calculate current load weight
    public int getCurrentWeight() {
        return medications.stream()
                .mapToInt(Medication::getWeight)
                .sum();
    }

    // Helper method to check if drone can be loaded
    public boolean canBeLoaded() {
        return (state == DroneState.IDLE || state == DroneState.LOADING);
    }

    // Helper method to check if the drone can carry additional weight
    public boolean canCarryWeight(int additionalWeight) {
        return (getCurrentWeight() + additionalWeight) <= weightLimit;
    }
}