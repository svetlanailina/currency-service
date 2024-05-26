package ru.svetlanailina.currencyservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "User entity representing a user in the system")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @Column(unique = true, nullable = false)
    @Schema(description = "Username of the user", example = "john_doe")
    private String username;

    @Column(nullable = false)
    @Schema(description = "Password of the user")
    private String password;

    @Column(unique = true, nullable = false)
    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @Column(unique = true, nullable = false)
    @Schema(description = "Phone number of the user", example = "+1234567890")
    private String phone;

    @Column(nullable = false)
    @Schema(description = "Full name of the user", example = "John Doe")
    private String fullName;

    @Column(nullable = false, updatable = false)
    @Schema(description = "Birthdate of the user", example = "1990-01-01")
    private LocalDate birthDate;

    @Column(nullable = false)
    @Schema(description = "Date when the user was created", example = "2023-05-24T18:25:43.511Z")
    private LocalDateTime dateCreated;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Schema(description = "Bank account associated with the user")
    private BankAccount bankAccount;

    // автоматически устанавливает значения полей перед сохранением сущности
    @PrePersist
    protected void onCreate() {
        this.dateCreated = LocalDateTime.now();
    }
}
