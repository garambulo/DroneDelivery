package com.hitachi.assessment.model;

public enum DroneModel {
    LIGHTWEIGHT(100),
    MIDDLEWEIGHT(250),
    CRUISERWEIGHT(500),
    HEAVYWEIGHT(1000);

    private final int weightLimit;

    DroneModel(int weightLimit) {
        this.weightLimit = weightLimit;
    }

    public int getWeightLimit() {
        return weightLimit;
    }
}
