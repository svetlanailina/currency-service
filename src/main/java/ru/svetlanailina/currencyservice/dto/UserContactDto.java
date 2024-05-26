package ru.svetlanailina.currencyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

/*
Проверка, что пользователь не может изменить другие данные,
 кроме контактной информации, осуществляется на уровне DTO и сервиса.
 Мы используем специальный DTO (UserContactDto),
 который содержит только те поля,
  которые можно изменить (email и phone).
  Таким образом, контроллер принимает только этот DTO
  при обновлении контактной информации,
  и другие поля пользователя остаются недоступными для изменения.
 */

@Schema(description = "Request for user's contact information")
@Data
public class UserContactDto {

    @Email
    private String email;
    private String phone;
}
