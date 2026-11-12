package com.senac.tsi.MinhaPrimeiraApi.exception;

import org.springframework.http.HttpStatus;

/** Lançada quando uma regra de negócio é violada. Por padrão resulta em HTTP 409. */
public class BusinessRuleException extends RuntimeException {

    private final HttpStatus status;

    public BusinessRuleException(String message) {
        this(HttpStatus.CONFLICT, message);
    }

    public BusinessRuleException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
