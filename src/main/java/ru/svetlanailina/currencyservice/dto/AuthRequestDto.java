package ru.svetlanailina.currencyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "Request for user's authentication")
@Data
public class AuthRequestDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
