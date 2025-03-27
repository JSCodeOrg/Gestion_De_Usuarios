package com.JSCode.GestionUsuarios.models;
import java.sql.Date;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El correo no puede estar vacío")
    @Column(nullable = false, unique = true)
    private String mail;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Column(nullable = false)
    private String password;

    @Column(name = "verified")
    private Boolean verified = false;
    
    public boolean isVerified() {
        return verified;
    }
    
    @Column
    private LocalDateTime deleted_at;
}
