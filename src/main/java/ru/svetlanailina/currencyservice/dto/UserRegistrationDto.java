package ru.svetlanailina.currencyservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserRegistrationDto {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String fullName;
    private LocalDate dateCreated;
    private LocalDate birthDate;
    private Double initialBalance;
}
