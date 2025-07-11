package com.JSCode.GestionUsuarios.controller;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.JSCode.GestionUsuarios.dto.password.RecoverPassword;
import com.JSCode.GestionUsuarios.dto.api.ApiResponse;
import com.JSCode.GestionUsuarios.dto.register.EditDataDTO;
import com.JSCode.GestionUsuarios.dto.register.UserRegisterDto;
import com.JSCode.GestionUsuarios.dto.register.WorkerRegisterDto;
import com.JSCode.GestionUsuarios.dto.users.AddressDTO;
import com.JSCode.GestionUsuarios.dto.users.DeactivationRequest;
import com.JSCode.GestionUsuarios.dto.users.DeliveryDataDTO;
import com.JSCode.GestionUsuarios.dto.users.DeliveryEntregasData;
import com.JSCode.GestionUsuarios.dto.users.ProfileImageDTO;
import com.JSCode.GestionUsuarios.dto.users.UserDataDTO;
import com.JSCode.GestionUsuarios.dto.users.UserIdDto;
import com.JSCode.GestionUsuarios.dto.users.VerificationEditionRequest;
import com.JSCode.GestionUsuarios.model.User;
import com.JSCode.GestionUsuarios.utils.VerificationCodeGenerator;
import com.JSCode.GestionUsuarios.utils.VerificationStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import com.JSCode.GestionUsuarios.dto.auth.RecoveryCodeDto;
import com.JSCode.GestionUsuarios.dto.auth.RecoverResponse;
import com.JSCode.GestionUsuarios.dto.password.NewPasswordDto;
import com.JSCode.GestionUsuarios.security.JwtUtil;
import com.JSCode.GestionUsuarios.service.UserService;
import com.JSCode.GestionUsuarios.service.email.RecoverEmail;

@RestController
@RequestMapping("/users")

public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RecoverEmail recoverEmail;

    @Autowired
    private JwtUtil jwtUtil;

    String tokenmessagealert = "Token inválido o expirado";
    String bearer = "Bearer ";

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
                    new ApiResponse<>(tokenmessagealert, null, true, 403));
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

    /*
     * 
     * @PostMapping("/edition")
     * public ResponseEntity<ApiResponse<Void>> editUserData(@RequestBody EditData
     * request) {
     * if (request.getUserId() == null) {
     * return ResponseEntity.badRequest().body(
     * new ApiResponse<>("Se requiere user_id", null, true, 400));
     * }
     * 
     * userService.updateUserData(request.getUserId(), request);
     * 
     * return ResponseEntity.ok(
     * new ApiResponse<>("Datos actualizados correctamente", null, false, 200));
     * }
     * 
     */

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

    @PreAuthorize("hasRole('administrador')")
    @PostMapping("/createuser")
    public ResponseEntity<ApiResponse<User>> createWorker(@RequestBody WorkerRegisterDto workerData,
            @RequestHeader("Authorization") String authToken) {

        if (workerData.getEmail() == null || workerData.getRole_id() == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Se requiere email y rol", null, true, 400));
        }

        userService.createWorker(workerData, authToken);

        return ResponseEntity.ok(
                new ApiResponse<>("Usuario creado correctamente", null, false, 200));
    }

    @PutMapping("/updateinfo")
    public ResponseEntity<ApiResponse<EditDataDTO>> updateUserInfo(
            @RequestBody EditDataDTO newData,
            @RequestHeader("Authorization") String token) {

        if (newData.getNombre() == null || newData.getApellido() == null || newData.getDireccion() == null
                || newData.getTelefono() == null || newData.getDocument() == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Se requiere nombre, apellido y demás datos.", null, true, 400));
        }

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Token no proporcionado", null, true, 400));
        }

        if (!token.startsWith(bearer)) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Token en formato incorrecto", null, true, 400));
        }

        String tokenClean = token.substring(7);
        if (!jwtUtil.isTokenValid(tokenClean)) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(tokenmessagealert, null, true, 400));
        }

        try {
            EditDataDTO changeData = userService.updateUserData(newData, tokenClean);
            return ResponseEntity.ok(
                    new ApiResponse<>("Datos actualizados correctamente", changeData, false, 200));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ApiResponse<>("Error inesperado: " + e.getMessage(), null, true, 500));
        }
    }

    @GetMapping("/getuser")
    public ResponseEntity<ApiResponse<UserDataDTO>> getUserData(@RequestHeader("Authorization") String authToken) {
        if (authToken == null || !authToken.startsWith(bearer)) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Token no proporcionado o inválido", null, true, 400));
        }
        String token = authToken.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(tokenmessagealert, null, true, 400));
        }
        UserDataDTO userData = userService.getUserData(token);

        return ResponseEntity.ok(
                new ApiResponse<>("Datos obtenidos correctamente", userData, false, 200));

    }

    @PutMapping("/updateprofileimage")
    public ResponseEntity<ApiResponse<ProfileImageDTO>> updateUserImage(@RequestParam("newImage") MultipartFile file,
            @RequestHeader("Authorization") String authToken) {

        if (authToken == null || !authToken.startsWith(bearer)) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Token no proporcionado o inválido", null, true, 400));
        }

        String token = authToken.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(tokenmessagealert, null, true, 400));

        }

        ProfileImageDTO image = userService.updateProfileImage(file, token);

        return ResponseEntity.ok(new ApiResponse<>("Imagen actualizada correctamente", image, false, 0));
    }

    @GetMapping("/getaddress/{id}")
    public ResponseEntity<AddressDTO> getuseraddress(@PathVariable Long id) {

        AddressDTO userAddress = userService.getUserAddress(id);

        return ResponseEntity.ok(userAddress);
    }

    @PutMapping("/repartidor/actualizar")
    public ResponseEntity<?> registrarInfoRepartidor(@RequestBody DeliveryDataDTO deliveryData,
            @RequestHeader("Authorization") String authToken) {

        try {
            if (deliveryData.getNombre() == null || deliveryData.getApellido() == null
                    || deliveryData.getDireccion() == null
                    || deliveryData.getTelefono() == null || deliveryData.getDocument() == null) {

                return ResponseEntity.badRequest().body(
                        new ApiResponse<>("Se requiere nombre, apellido y demás datos.", null, true, 400));
            }

            if (authToken.isBlank() || authToken.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>("Se requiere el token de usuario.", null, true, 400));
            }

            String token = authToken.substring(7);

            DeliveryEntregasData new_deliveryData = userService.updateDeliveryInfo(deliveryData, token);

            return ResponseEntity.ok("Se ha creado satisfactoriamente el repartidor:" + new_deliveryData);

        } catch (Exception e) {
            throw new RuntimeException("No se ha podido crear el repartidor." + e.getMessage());
        }
    }

    @PostMapping("/getusermail")
    public ResponseEntity<String> getUserMail(@RequestParam("userId") Long userId) {
        String userMail = userService.getUserMail(userId);
        if (userMail != null) {
            return ResponseEntity.ok(userMail);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
    }
}
