package com.hitachi.assessment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DroneOverloadedException extends RuntimeException {
    public DroneOverloadedException(String message) {
        super(message);
    }
}
