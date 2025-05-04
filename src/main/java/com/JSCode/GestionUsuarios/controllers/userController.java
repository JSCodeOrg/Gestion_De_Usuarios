package com.JSCode.GestionUsuarios.controllers;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.JSCode.GestionUsuarios.dto.ApiResponse;
import com.JSCode.GestionUsuarios.dto.DeactivationRequest;
import com.JSCode.GestionUsuarios.dto.Password.RecoverPassword;
import com.JSCode.GestionUsuarios.dto.register.UserRegisterDto;
import com.JSCode.GestionUsuarios.dto.WorkerRegisterDto;
import com.JSCode.GestionUsuarios.dto.Password.RecoverPassword;
import com.JSCode.GestionUsuarios.dto.EditData;
import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.services.AuthService;
import com.JSCode.GestionUsuarios.services.UserService;
import com.JSCode.GestionUsuarios.services.email.RecoverEmail;
import com.JSCode.GestionUsuarios.utils.VerificationCodeGenerator;
import com.JSCode.GestionUsuarios.utils.VerificationStatus;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import com.JSCode.GestionUsuarios.dto.Auth.RecoveryCodeDto;
import com.JSCode.GestionUsuarios.dto.Auth.VerificationRequest;
import com.JSCode.GestionUsuarios.dto.VerificationEditionRequest;
import com.JSCode.GestionUsuarios.dto.Auth.CheckLogin;
import com.JSCode.GestionUsuarios.dto.Auth.RecoverResponse;
import com.JSCode.GestionUsuarios.dto.Password.NewPasswordDto;
import com.JSCode.GestionUsuarios.security.JwtUtil;

@RestController
@RequestMapping("/users")

public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RecoverEmail recoverEmail;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody UserRegisterDto data) {
        User user = userService.registerUser(data);
        return ResponseEntity.ok(
                new ApiResponse<>("Registro exitoso", user, false, 200));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyUser(@RequestParam("token") String token) {
        VerificationStatus isVerified = userService.verifyUser(token);
        if (isVerified == VerificationStatus.ALREADY_VERIFIED) {
            return ResponseEntity.ok(
                    new ApiResponse<>("La cuenta ya había sido verificada", null, false, 200));
        } else {
            return ResponseEntity.ok(
                    new ApiResponse<>("Cuenta activada correctamente", null, true, 200));
        }
    }

    @PostMapping("/deactivate")
    public ResponseEntity<ApiResponse<String>> requestAccountDeactivation(@RequestBody DeactivationRequest request) {
        userService.DeactivationRequest(request.getMail());
        return ResponseEntity.ok(new ApiResponse<>("Usuario desactivado exitosamente", null, false, 200));
    }

    @PostMapping("/recoverpassword")
    public ResponseEntity<ApiResponse<Void>> emailExists(@RequestBody RecoverPassword request) {
        boolean existUser = userService.userExistsByMail(request.getMail());

        if (existUser) {
            try {
                String verificationCode = VerificationCodeGenerator.generateVerificationCode();
                recoverEmail.sendRecoverEmail(request.getMail(), verificationCode);
                userService.saveVerificationCode(request.getMail(), verificationCode);
                return ResponseEntity.ok(
                        new ApiResponse<>(
                                "Email verificado correctamente. Se han enviado las instrucciones a tu correo.", null,
                                false, 200));
            } catch (MessagingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new ApiResponse<>("Error al enviar el correo de recuperación", null, true, 500));
            }
        } else {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Usuario no registrado", null, true, 400));
        }
    }

    @PutMapping("/createnewpassword")
    public ResponseEntity<ApiResponse<Void>> createNewPassword(@RequestBody NewPasswordDto newPasswordData,
            @CookieValue("recover_token") String recoveryToken, HttpServletResponse response) {

        if (newPasswordData.getNewPassword() == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Se requiere la nueva contraseña", null, true, 400));
        }
        if (recoveryToken == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Token de recuperación no encontrado", null, true, 400));
        }

        boolean tokenVerify = jwtUtil.isTokenValid(recoveryToken);
        if (!tokenVerify) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Token inválido o expirado", null, true, 403));
        }

        Long userId;
        try {
            userId = Long.parseLong(jwtUtil.extractUsername(recoveryToken));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Token inválido o manipulado", null, true, 403));
        }

        if (!userService.userExistsById(userId)) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Usuario no registrado", null, true, 400));
        }

        boolean passwordChanged = userService.updatePassword(userId,
                newPasswordData.getNewPassword());

        if (!passwordChanged) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Error al cambiar la contraseña", null, true, 400));
        }
        Cookie cookie = new Cookie("recover_token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok(
                new ApiResponse<>("Contraseña actualizada correctamente", null, false, 200));
    }

    @PostMapping("/checkrecoverycode")
    public ResponseEntity<ApiResponse<RecoverResponse>> checkRecoveryCode(@RequestBody RecoveryCodeDto userRecoveryData,
            HttpServletResponse response) {
        if (userRecoveryData.getMail() == null || userRecoveryData.getCode() == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Se requiere mail y codigo de verificacion", null, true, 400));
        }

        String generateRecoveryToken = this.userService.generateRecoveryToken(userRecoveryData.getMail());

        Cookie cookie = new Cookie("recover_token", generateRecoveryToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(1 * 30 * 60);
        response.addCookie(cookie);

        RecoverResponse isValid = userService.checkRecoveryCode(userRecoveryData.getMail(), userRecoveryData.getCode());
        if (isValid.getMail() != null && isValid.getRecoveryToken() != null) {
            return ResponseEntity.ok(
                    new ApiResponse<>("Codigo validado", isValid, false, 200));
        } else {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Codigo de verificacion incorrecto", null, true, 400));
        }
    }

    @PostMapping("/edition")
    public ResponseEntity<ApiResponse<Void>> editUserData(@RequestBody EditData request) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Se requiere user_id", null, true, 400));
        }

        userService.updateUserData(request.getUserId(), request);

        return ResponseEntity.ok(
                new ApiResponse<>("Datos actualizados correctamente", null, false, 200));
    }

    @PostMapping("/verifyedition")
    public ResponseEntity<ApiResponse<User>> verifyEdition(@RequestBody VerificationEditionRequest request) {
        if (request.getId() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Se requiere user_id y password", null, true, 400));
        }
        userService.verifyUserEdit(request.getId(), request.getPassword());
        return ResponseEntity.ok(
                new ApiResponse<>("Usuario verificado correctamente", false, 200));
    }

    @PostMapping("/createuser")
    public ResponseEntity<ApiResponse<User>> createWorker(@RequestBody WorkerRegisterDto workerData) {
        if (workerData.getEmail() == null || workerData.getRole_id() == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Se requiere email y rol", null, true, 400));
        }
        userService.createWorker(workerData.getEmail(), workerData.getRole_id());
        return ResponseEntity.ok(
                new ApiResponse<>("Usuario creado correctamente", null, false, 200));
    }

}
