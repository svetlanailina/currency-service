package ru.svetlanailina.currencyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Schema(description = "Request for user's registration")
@Data
public class UserRegistrationDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phone;

    @NotBlank
    private String fullName;

    @NotNull
    private LocalDate birthDate;

    @NotNull
    private Double initialBalance;
}
