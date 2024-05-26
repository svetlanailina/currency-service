package ru.svetlanailina.currencyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "Request for transferring funds")
@Data
public class TransferRequestDto {

    @NotNull
    private Long toUserId;

    @NotNull
    @Min(0)
    private Double amount;
}

