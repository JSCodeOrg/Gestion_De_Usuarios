package com.JSCode.GestionUsuarios.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class UserRecoveryCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El correo no puede estar vacío")
    private String email;

    @NotBlank(message = "El codigo no puede estar vacío")
    private String code;

    @NotNull(message = "La fecha de expiración no puede estar vacía")
    private LocalDateTime expiresAt;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true) 
    private User user;

    
}
