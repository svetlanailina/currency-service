package ru.svetlanailina.currencyservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.svetlanailina.currencyservice.dto.TransferRequestDto;
import ru.svetlanailina.currencyservice.dto.UserContactDto;
import ru.svetlanailina.currencyservice.dto.UserRegistrationDto;
import ru.svetlanailina.currencyservice.entity.User;
import ru.svetlanailina.currencyservice.exception.GlobalExceptionHandler;
import ru.svetlanailina.currencyservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "User Controller", description = "API for user operations")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserService userService;

    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Custom Exception", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorDetails.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<User> register(@Valid @RequestBody UserRegistrationDto dto) {
        User user = userService.createUser(dto);
        logger.info("User registered successfully with username: {}", dto.getUsername());
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update user contact information", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact information updated successfully", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Custom Exception", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorDetails.class)))
    })
    @PutMapping("/{userId}/contact")
    public ResponseEntity<User> updateContact(@PathVariable Long userId, @Valid @RequestBody UserContactDto dto) {
        User user = userService.updateUserContact(userId, dto.getEmail(), dto.getPhone());
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Transfer funds to another user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds transferred successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Custom Exception", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorDetails.class)))
    })
    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> transferFunds(@Valid @RequestBody TransferRequestDto transferRequest) {
        userService.transferFunds(transferRequest.getToUserId(), transferRequest.getAmount());
        return ResponseEntity.ok().build();
    }

    // Метод поиска пользователей с фильтрацией и пагинацией
    @Operation(summary = "Search user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found successfully", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Custom Exception", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorDetails.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<User> users = userService.searchUsers(fullName, email, phone, birthDate, page, size);
        return ResponseEntity.ok(users);
    }
}
