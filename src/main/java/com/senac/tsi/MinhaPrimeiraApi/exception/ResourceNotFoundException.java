package com.senac.tsi.MinhaPrimeiraApi.exception;

/** Lançada quando um registro não existe (resulta em HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
