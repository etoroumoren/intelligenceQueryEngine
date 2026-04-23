package com.apiPersistence.intelligenceQuery.exception;

public class UninterpretableQueryException extends RuntimeException{
    public UninterpretableQueryException() {
        super("Unable to interpret query");
    }
}
