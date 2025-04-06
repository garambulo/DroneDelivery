package com.hitachi.assessment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DroneStateException extends RuntimeException {
    public DroneStateException(String message) {
        super(message);
    }
}
