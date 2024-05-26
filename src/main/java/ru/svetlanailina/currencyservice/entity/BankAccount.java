package ru.svetlanailina.currencyservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "BankAccount entity representing a BankAccount in the system")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the bank account", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Initial balance of the bank account", example = "1000.0")
    private Double initialBalance;

    @Column(nullable = false)
    @Schema(description = "Current balance of the bank account", example = "1050.0")
    private Double balance;

    @Column(nullable = false)
    @Schema(description = "Maximum allowed balance of the bank account", example = "2070.0")
    private Double maxBalance;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    @Schema(description = "User associated with the bank account")
    private User user;

    @PrePersist
    public void prePersist() {
        if (maxBalance == null) {
            maxBalance = initialBalance * 2.07;
        }
    }
}
