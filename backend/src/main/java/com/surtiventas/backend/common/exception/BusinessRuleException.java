package com.surtiventas.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Raised when a request is well-formed but violates a business invariant,
 * e.g. an order state transition that isn't allowed from the current state.
 */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
