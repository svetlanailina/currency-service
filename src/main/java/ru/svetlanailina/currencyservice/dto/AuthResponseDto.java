package ru.svetlanailina.currencyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Response for user's token")
@Data
public class AuthResponseDto {

    private String token;

    public AuthResponseDto(String token) {
        this.token = token;
    }
}
