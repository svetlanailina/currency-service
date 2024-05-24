package ru.svetlanailina.currencyservice.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.svetlanailina.currencyservice.dto.UserContactDto;
import ru.svetlanailina.currencyservice.dto.UserRegistrationDto;
import ru.svetlanailina.currencyservice.entity.User;
import ru.svetlanailina.currencyservice.service.UserService;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<User> register(@Valid @RequestBody UserRegistrationDto dto) {
        User user = userService.createUser(
                dto.getUsername(),
                dto.getPassword(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getFullName(),
                dto.getBirthDate(),
                dto.getInitialBalance()
        );
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/contact")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<User> updateContact(@PathVariable Long userId, @Valid @RequestBody UserContactDto dto) {
        User user = userService.updateUserContact(userId, dto.getEmail(), dto.getPhone());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{fromUserId}/transfer/{toUserId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> transfer(@PathVariable Long fromUserId, @PathVariable Long toUserId, @RequestParam Double amount) {
        userService.transferFunds(fromUserId, toUserId, amount);
        return ResponseEntity.ok().build();
    }

    // Метод поиска пользователей с фильтрацией и пагинацией
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_USER')")
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
