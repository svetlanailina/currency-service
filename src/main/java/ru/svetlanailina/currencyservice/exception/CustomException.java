package ru.svetlanailina.currencyservice.exception;

public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    //Для проверки уникальности: логин, телефон и email не должны быть заняты
    public CustomException(String message) {
        super(message);
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
