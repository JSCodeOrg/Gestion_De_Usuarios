package com.JSCode.GestionUsuarios.controllers;

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
import com.JSCode.GestionUsuarios.dto.Response;
import com.JSCode.GestionUsuarios.dto.DeactivationRequest;
import com.JSCode.GestionUsuarios.dto.ProfileImageDTO;
import com.JSCode.GestionUsuarios.dto.Password.RecoverPassword;
import com.JSCode.GestionUsuarios.dto.register.EditDataDTO;
import com.JSCode.GestionUsuarios.dto.register.UserRegisterDto;
import com.JSCode.GestionUsuarios.dto.users.AddressDTO;
import com.JSCode.GestionUsuarios.dto.users.DeliveryDataDTO;
import com.JSCode.GestionUsuarios.dto.users.DeliveryEntregasData;
import com.JSCode.GestionUsuarios.dto.WorkerRegisterDto;
import com.JSCode.GestionUsuarios.dto.UserDataDTO;
import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.services.UserService;
import com.JSCode.GestionUsuarios.services.Email.RecoverEmail;
import com.JSCode.GestionUsuarios.utils.VerificationCodeGenerator;
import com.JSCode.GestionUsuarios.utils.VerificationStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import com.JSCode.GestionUsuarios.dto.Auth.RecoveryCodeDto;
import com.JSCode.GestionUsuarios.dto.VerificationEditionRequest;
import com.JSCode.GestionUsuarios.dto.Auth.RecoverResponse;
import com.JSCode.GestionUsuarios.dto.Password.NewPasswordDto;
import com.JSCode.GestionUsuarios.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/users")

@Tag(name = "Usuarios", description = "Operaciones relacionadas con usuarios")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RecoverEmail recoverEmail;

    @Autowired
    private JwtUtil jwtUtil;

    // Registrar usuario

    @Operation(
    summary = "Registrar los usuarios",
    description = "Se encarga de crear los usuarios en las bases de datos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario registrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Response.class),
                examples = @ExampleObject(
                    name = "Registro exitoso",
                    summary = "Respuesta al registrar correctamente un usuario",
                    value = """
                    {
                    "message": "Registro exitoso",
                    "data": {
                        "id": 1,
                        "nombre": "Juan",
                        "apellido": "Pérez",
                        "mail": "juan.perez@example.com",
                        "document": "123456789",
                        "direccion": "Cra 45 #10-23, Bogotá",
                        "telefono": "+57 3201234567",
                        "profileImageUrl": "https://example.com/profile.jpg"
                    },
                    "error": false,
                    "status": 200
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error al intentar registrar el usuario",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Response.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Respuesta cuando ocurre un error inesperado",
                    value = """
                    {
                    "message": "Error al registrar el usuario. Detalles: violación de clave única",
                    "data": null,
                    "error": true,
                    "status": 500
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<Response<User>> register(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Datos necesarios para crear un nuevo usuario",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserRegisterDto.class),
            examples = @ExampleObject(
                name = "Entrada válida",
                summary = "Datos para registrar a un nuevo usuario",
                value = """
                {
                  "mail": "juan.perez@example.com",
                  "password": "Contrasena123!",
                  "document": "123456789",
                  "nombre": "Juan",
                  "apellido": "Pérez",
                  "direccion": "Cra 45 #10-23, Bogotá",
                  "telefono": "+57 3201234567",
                  "profileImageUrl": "https://example.com/profile.jpg"
                }
                """
            )
        )
    )
        @RequestBody UserRegisterDto data) {
        User user = userService.registerUser(data);
        return ResponseEntity.ok(
                new Response<>("Registro exitoso", user, false, 200));
    }

    // Verificar usuario

    
@Operation(
    summary = "Verificar usuario",
    description = "Verifica un usuario a través de un token enviado por correo electrónico"
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Usuario verificado exitosamente o ya estaba verificado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Response.class),
            examples = @ExampleObject(
                name = "Usuario verificado",
                summary = "Verificación correcta",
                value = """
                {
                  "message": "Cuenta activada correctamente",
                  "data": null,
                  "error": false,
                  "status": 200
                }
                """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Token inválido o expirado",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Response.class),
            examples = @ExampleObject(
                name = "Token inválido",
                summary = "Error de validación",
                value = """
                {
                  "message": "El token proporcionado es inválido o ha expirado",
                  "data": null,
                  "error": true,
                  "status": 400
                }
                """
            )
        )
    ),
    @ApiResponse(
        responseCode = "500",
        description = "Error interno del servidor",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Response.class),
            examples = @ExampleObject(
                name = "Error interno",
                summary = "Excepción inesperada",
                value = """
                {
                  "message": "Error al verificar el usuario",
                  "data": null,
                  "error": true,
                  "status": 500
                }
                """
            )
        )
    )
})
    @PostMapping("/verify")
    public ResponseEntity<Response<Void>> verifyUser(
        @Parameter(
            description = "Token de verificación enviado al correo del usuario",
            required = true,
            example = "abc123token"
        )
        @RequestParam("token") String token) {
        VerificationStatus isVerified = userService.verifyUser(token);
        if (isVerified == VerificationStatus.ALREADY_VERIFIED) {
            return ResponseEntity.ok(
                    new Response<>("La cuenta ya había sido verificada", null, false, 200));
        } else {
            return ResponseEntity.ok(
                    new Response<>("Cuenta activada correctamente", null, true, 200));
        }
    }

    // Desactivar cuenta

    @Operation(
    summary = "Solicitar desactivación de cuenta",
    description = "Desactiva la cuenta de un usuario dado su correo electrónico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cuenta desactivada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Response.class),
                examples = @ExampleObject(
                    name = "Desactivación exitosa",
                    summary = "Cuenta desactivada correctamente",
                    value = """
                    {
                    "message": "Usuario desactivado exitosamente",
                    "data": null,
                    "error": false,
                    "status": 200
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Solicitud inválida",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Response.class),
                examples = @ExampleObject(
                    name = "Correo inválido",
                    summary = "Formato de correo incorrecto",
                    value = """
                    {
                    "message": "El correo proporcionado no es válido",
                    "data": null,
                    "error": true,
                    "status": 400
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Response.class),
                examples = @ExampleObject(
                    name = "Error del servidor",
                    summary = "Excepción inesperada",
                    value = """
                    {
                    "message": "Error al procesar la solicitud de desactivación",
                    "data": null,
                    "error": true,
                    "status": 500
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/deactivate")
    public ResponseEntity<Response<String>> requestAccountDeactivation(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Correo del usuario que desea desactivar la cuenta",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = DeactivationRequest.class),
            examples = @ExampleObject(
                name = "Ejemplo de solicitud",
                value = """
                {
                  "mail": "usuario@example.com"
                }
                """
            )
        )
    )
        @RequestBody DeactivationRequest request) {
        userService.DeactivationRequest(request.getMail());
        return ResponseEntity.ok(new Response<>("Usuario desactivado exitosamente", null, false, 200));
    }

    // Recuperar contraseña

    @Operation(
    summary = "Recuperar contraseña",
    description = "Verifica si el correo existe y envía un código de recuperación si es válido"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Correo de recuperación enviado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Response.class),
                examples = @ExampleObject(
                    name = "Recuperación exitosa",
                    summary = "El usuario existe y se envió el email",
                    value = """
                    {
                    "message": "Email verificado correctamente. Se han enviado las instrucciones a tu correo.",
                    "data": null,
                    "error": false,
                    "status": 200
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "El correo no pertenece a ningún usuario registrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Response.class),
                examples = @ExampleObject(
                    name = "Usuario no registrado",
                    summary = "No se encontró el correo en la base de datos",
                    value = """
                    {
                    "message": "Usuario no registrado",
                    "data": null,
                    "error": true,
                    "status": 400
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Fallo al enviar el correo",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Response.class),
                examples = @ExampleObject(
                    name = "Error de envío",
                    summary = "Falló el envío del correo de recuperación",
                    value = """
                    {
                    "message": "Error al enviar el correo de recuperación",
                    "data": null,
                    "error": true,
                    "status": 500
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/recoverpassword")
    public ResponseEntity<Response<Void>> emailExists(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Correo del usuario que desea recuperar la contraseña",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = RecoverPassword.class),
            examples = @ExampleObject(
                name = "Solicitud válida",
                value = """
                {
                  "mail": "usuario@example.com"
                }
                """
            )
        )
    )
        
        @RequestBody RecoverPassword request) {
        boolean existUser = userService.userExistsByMail(request.getMail());

        if (existUser) {
            try {
                String verificationCode = VerificationCodeGenerator.generateVerificationCode();
                recoverEmail.sendRecoverEmail(request.getMail(), verificationCode);
                userService.saveVerificationCode(request.getMail(), verificationCode);
                return ResponseEntity.ok(
                        new Response<>(
                                "Email verificado correctamente. Se han enviado las instrucciones a tu correo.", null,
                                false, 200));
            } catch (MessagingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new Response<>("Error al enviar el correo de recuperación", null, true, 500));
            }
        } else {
            return ResponseEntity.badRequest().body(
                    new Response<>("Usuario no registrado", null, true, 400));
        }
    }

    // Crear nueva contraseña

    @Operation(
    summary = "Crear nueva contraseña",
    description = "Establece una nueva contraseña para el usuario si el token de recuperación es válido. El token es recibido como una cookie llamada `recover_token` y se elimina tras el cambio exitoso."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Contraseña cambiada exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Cambio exitoso",
                    value = """
                    {
                    "message": "Contraseña actualizada correctamente",
                    "data": null,
                    "error": false,
                    "status": 200
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Solicitud inválida (contraseña vacía, token ausente o usuario no encontrado)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Solicitud inválida",
                    value = """
                    {
                    "message": "Se requiere la nueva contraseña",
                    "data": null,
                    "error": true,
                    "status": 400
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Token inválido, manipulado o expirado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Token inválido",
                    value = """
                    {
                    "message": "Token inválido o expirado",
                    "data": null,
                    "error": true,
                    "status": 403
                    }
                    """
                )
            )
        )
    })
    @PutMapping("/createnewpassword")
    public ResponseEntity<Response<Void>> createNewPassword(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Nueva contraseña que el usuario desea establecer",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = NewPasswordDto.class),
            examples = @ExampleObject(
                name = "Ejemplo de nueva contraseña",
                value = """
                {
                  "newPassword": "NuevaPassword123!"
                }
                """
            )
        )
    )
        
        @RequestBody NewPasswordDto newPasswordData,
            @CookieValue("recover_token") String recoveryToken, HttpServletResponse response) {

        if (newPasswordData.getNewPassword() == null) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Se requiere la nueva contraseña", null, true, 400));
        }
        if (recoveryToken == null) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Token de recuperación no encontrado", null, true, 400));
        }

        boolean tokenVerify = jwtUtil.isTokenValid(recoveryToken);
        if (!tokenVerify) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Token inválido o expirado", null, true, 403));
        }

        Long userId;
        try {
            userId = Long.parseLong(jwtUtil.extractUsername(recoveryToken));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Token inválido o manipulado", null, true, 403));
        }

        if (!userService.userExistsById(userId)) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Usuario no registrado", null, true, 400));
        }

        boolean passwordChanged = userService.updatePassword(userId,
                newPasswordData.getNewPassword());

        if (!passwordChanged) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Error al cambiar la contraseña", null, true, 400));
        }
        Cookie cookie = new Cookie("recover_token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok(
                new Response<>("Contraseña actualizada correctamente", null, false, 200));
    }

    // Revisar codigo de recuperación

    @Operation(
    summary = "Validar código de recuperación",
    description = """
    Valida el código de recuperación enviado al correo del usuario.
    Si es correcto, genera un token de recuperación (`recover_token`) y lo almacena como cookie segura.
    """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Código validado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Response.class),
                examples = @ExampleObject(
                    name = "Validación exitosa",
                    summary = "Código correcto y cookie creada",
                    value = """
                    {
                    "message": "Codigo validado",
                    "data": {
                        "mail": "usuario@example.com",
                        "recoveryToken": "eyJhbGciOiJIUzI1NiJ9..."
                    },
                    "error": false,
                    "status": 200
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o código incorrecto",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error de validación",
                    summary = "Correo o código faltante o incorrecto",
                    value = """
                    {
                    "message": "Codigo de verificacion incorrecto",
                    "data": null,
                    "error": true,
                    "status": 400
                    }
                    """
                )
            )
        )
    })

    @PostMapping("/checkrecoverycode")
    public ResponseEntity<Response<RecoverResponse>> checkRecoveryCode(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Correo y código de verificación",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = RecoveryCodeDto.class),
            examples = @ExampleObject(
                name = "Solicitud válida",
                value = """
                {
                  "mail": "usuario@example.com",
                  "code": "123456"
                }
                """
            )
        )
    )
        
        @RequestBody RecoveryCodeDto userRecoveryData,
            HttpServletResponse response) {
        if (userRecoveryData.getMail() == null || userRecoveryData.getCode() == null) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Se requiere mail y codigo de verificacion", null, true, 400));
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
                    new Response<>("Codigo validado", isValid, false, 200));
        } else {
            return ResponseEntity.badRequest().body(
                    new Response<>("Codigo de verificacion incorrecto", null, true, 400));
        }
    }

    // Verificar edicion

    @Operation(
    summary = "Verificar datos para edición",
    description = "Verifica si la contraseña ingresada por el usuario coincide con la registrada para permitir la edición de sus datos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Verificación exitosa",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Usuario verificado",
                    value = """
                    {
                    "message": "Usuario verificado correctamente",
                    "data": null,
                    "error": false,
                    "status": 200
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Faltan campos requeridos o verificación fallida",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error de validación",
                    value = """
                    {
                    "message": "Se requiere user_id y password",
                    "data": null,
                    "error": true,
                    "status": 400
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/verifyedition")
    public ResponseEntity<Response<User>> verifyEdition(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Credenciales del usuario para verificar la edición",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = VerificationEditionRequest.class),
            examples = @ExampleObject(
                name = "Solicitud de verificación",
                value = """
                {
                  "id": 123,
                  "password": "miContrasena123"
                }
                """
            )
        )
    )
        
        @RequestBody VerificationEditionRequest request) {
        if (request.getId() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Se requiere user_id y password", null, true, 400));
        }
        userService.verifyUserEdit(request.getId(), request.getPassword());
        return ResponseEntity.ok(
                new Response<>("Usuario verificado correctamente", false, 200));
    }

    // Crear usuario desde el rol de administrador

    @Operation(
    summary = "Crear nuevo usuario trabajador",
    description = "Permite a un administrador crear un nuevo usuario con un rol específico. Requiere el rol `administrador`."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario creado correctamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Creación exitosa",
                    value = """
                    {
                    "message": "Usuario creado correctamente",
                    "data": null,
                    "error": false,
                    "status": 200
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Faltan datos requeridos",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Datos incompletos",
                    value = """
                    {
                    "message": "Se requiere email y rol",
                    "data": null,
                    "error": true,
                    "status": 400
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado. Solo usuarios con rol administrador pueden acceder a este endpoint."
        )
    })

    @PreAuthorize("hasRole('administrador')")
    @PostMapping("/createuser")
    public ResponseEntity<Response<User>> createWorker(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Datos del trabajador a crear",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = WorkerRegisterDto.class),
            examples = @ExampleObject(
                name = "Nuevo trabajador",
                value = """
                {
                  "email": "empleado@example.com",
                  "role_id": 2
                }
                """
            )
        )
    )
        
        @RequestBody WorkerRegisterDto workerData,
        @RequestHeader("Authorization") String authToken
        ) {

        if (workerData.getEmail() == null || workerData.getRole_id() == null) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Se requiere email y rol", null, true, 400));
        }

        userService.createWorker(workerData,authToken);

        return ResponseEntity.ok(
                new Response<>("Usuario creado correctamente", null, false, 200));
    }

    // Actulizar informacion del usuario

    @Operation(
    summary = "Actualizar información del usuario",
    description = "Permite al usuario autenticado actualizar su información personal. Se requiere un token válido en el header Authorization."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Datos actualizados correctamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Actualización exitosa",
                    value = """
                    {
                    "message": "Datos actualizados correctamente",
                    "data": {
                        "nombre": "Juan",
                        "apellido": "Pérez",
                        "direccion": "Calle 123 #45-67",
                        "telefono": "3001234567",
                        "documento": "1020304050",
                        "email": "juan.perez@example.com"
                    },
                    "error": false,
                    "status": 200
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos faltantes o token inválido",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error por datos faltantes",
                    value = """
                    {
                    "message": "Datos incompletos",
                    "data": null,
                    "error": true,
                    "status": 400
                    }
                    """
                )
            )
        )
    })

    @PutMapping("/updateinfo")
    public ResponseEntity<Response<EditDataDTO>> updateUserInfo(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Datos nuevos del usuario",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = EditDataDTO.class),
            examples = @ExampleObject(
                name = "Datos del usuario",
                value = """
                {
                  "nombre": "Juan",
                  "apellido": "Pérez",
                  "direccion": "Calle 123 #45-67",
                  "telefono": "3001234567",
                  "documento": "1020304050",
                  "email": "juan.perez@example.com"
                }
                """
            )
        )
    )
        
        @RequestBody EditDataDTO newData,
        @Parameter(
        description = "Token JWT del usuario en formato Bearer",
        required = true,
        example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
    )
            @RequestHeader("Authorization") String token) {
        if (newData.getNombre() == null || newData.getApellido() == null || newData.getDireccion() == null
                || newData.getTelefono() == null || newData.getDocumento() == null || newData.getEmail() == null) {

            return ResponseEntity.badRequest().body(
                    new Response<>("Se requiere nombre, apellido y mail", null, true, 400));
        }
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Token no proporcionado", null, true, 400));
        }
        if (!token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Token en formato incorrecto", null, true, 400));
        }

        if (!jwtUtil.isTokenValid(token.substring(7))) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Token inválido o expirado", null, true, 400));
        }

        EditDataDTO changeData = userService.updateUserData(newData, token.substring(7));

        return ResponseEntity.ok(
                new Response<>("Datos actualizados correctamente", changeData, false, 200));
    }

    // Obtener información del usuario

    @Operation(
    summary = "Obtener datos del usuario",
    description = "Devuelve la información del usuario autenticado. Requiere un token JWT válido en el header Authorization."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Datos obtenidos correctamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Datos del usuario",
                    value = """
                    {
                    "message": "Datos obtenidos correctamente",
                    "data": {
                        "nombre": "Juan",
                        "apellido": "Pérez",
                        "email": "juan.perez@example.com",
                        "telefono": "3001234567"
                    },
                    "error": false,
                    "status": 200
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Token faltante o inválido",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Token no válido",
                    value = """
                    {
                    "message": "Token no proporcionado o inválido",
                    "data": null,
                    "error": true,
                    "status": 400
                    }
                    """
                )
            )
        )
    })

    @GetMapping("/getuser")
    public ResponseEntity<Response<UserDataDTO>> getUserData(
        @Parameter(
        description = "Token JWT del usuario en formato Bearer",
        required = true,
        example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
    )
        
        @RequestHeader("Authorization") String authToken) {
        if (authToken == null || !authToken.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Token no proporcionado o inválido", null, true, 400));
        }
        String token = authToken.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Token inválido o expirado", null, true, 400));
        }
        UserDataDTO userData = userService.getUserData(token);

        return ResponseEntity.ok(
                new Response<>("Datos obtenidos correctamente", userData, false, 200));

    }

    // Actualizar foto del usuario

    @Operation(
    summary = "Actualizar imagen de perfil",
    description = "Permite a un usuario autenticado actualizar su imagen de perfil. Se requiere enviar el archivo como multipart/form-data junto con un token JWT válido."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Imagen actualizada correctamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Respuesta exitosa",
                    value = """
                    {
                    "message": "Imagen actualizada correctamente",
                    "data": {
                        "imageUrl": "https://cdn.example.com/profiles/juan.jpg"
                    },
                    "error": false,
                    "status": 200
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Token inválido o archivo faltante",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Token inválido",
                    value = """
                    {
                    "message": "Token no proporcionado o inválido",
                    "data": null,
                    "error": true,
                    "status": 400
                    }
                    """
                )
            )
        )
    })

    @PutMapping("/updateprofileimage")
    public ResponseEntity<Response<ProfileImageDTO>> updateUserImage(

        @Parameter(
        description = "Imagen nueva del perfil (formato .jpg, .png, etc.)",
        required = true
        )
        @RequestParam("newImage") MultipartFile file,

        @Parameter(
        description = "Token JWT del usuario en formato Bearer",
        required = true,
        example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
        )
        @RequestHeader("Authorization") String authToken) {

        if (authToken == null || !authToken.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Token no proporcionado o inválido", null, true, 400));
        }

        String token = authToken.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.badRequest().body(
                    new Response<>("Token inválido o expirado", null, true, 400));

        }

        ProfileImageDTO image = userService.updateProfileImage(file, token);

        return ResponseEntity.ok(new Response<>("Imagen actualizada correctamente", image, false, 0));
    }
    
    // Obtener direccion del usuario

        @Operation(
        summary = "Obtener dirección de un usuario",
        description = "Devuelve la dirección registrada de un usuario a partir de su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dirección obtenida correctamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Dirección ejemplo",
                    value = "\"Calle 123 #45-67, Medellín\""
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Usuario no encontrado\""
                )
            )
        )
    })

    @GetMapping("/getaddress/{id}")
    public ResponseEntity<AddressDTO> getuseraddress(
        @Parameter(
        description = "ID del usuario del cual se desea obtener la dirección",
        required = true,
        example = "5"
        )
        
        @PathVariable Long id){

        AddressDTO userAddress = userService.getUserAddress(id);

        return ResponseEntity.ok(userAddress);
    }

    // Actualizar informacion del repartidor

    @Operation(
    summary = "Actualizar información del repartidor",
    description = "Permite que un usuario con rol repartidor actualice su información personal, como nombre, dirección, teléfono, etc."
    )
    @ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Repartidor actualizado correctamente",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = String.class),
            examples = @ExampleObject(
                value = "Se ha actualizado satisfactoriamente el repartidor: {\"id\": 12, \"nombre\": \"Carlos\", \"apellido\": \"Gómez\", ... }"
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Solicitud inválida por datos incompletos o token faltante/incorrecto",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Response.class),
            examples = @ExampleObject(
                value = "{\n  \"message\": \"Se requiere nombre, apellido y demás datos.\",\n  \"data\": null,\n  \"error\": true,\n  \"status\": 400\n}"
            )
        )
    ),
    @ApiResponse(
        responseCode = "401",
        description = "Token JWT ausente o inválido",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Response.class),
            examples = @ExampleObject(
                value = "{\n  \"message\": \"Se requiere el token de usuario.\",\n  \"data\": null,\n  \"error\": true,\n  \"status\": 400\n}"
            )
        )
    ),
    @ApiResponse(
        responseCode = "500",
        description = "Error interno al intentar actualizar los datos",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(type = "string"),
            examples = @ExampleObject(
                value = "No se ha podido crear el repartidor."
            )
        )
    )
})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Datos del repartidor a actualizar",
        content = @Content(schema = @Schema(implementation = DeliveryDataDTO.class))
    )

    @PutMapping("/repartidor/actualizar")
    public ResponseEntity<?> registrarInfoRepartidor(
            @RequestBody DeliveryDataDTO deliveryData,
            @Parameter(description = "Token de autenticación del usuario", required = true)
            @RequestHeader("Authorization") String authToken) {

        try {
            if (deliveryData.getNombre() == null || deliveryData.getApellido() == null
                    || deliveryData.getDireccion() == null
                    || deliveryData.getTelefono() == null || deliveryData.getDocument() == null) {

                return ResponseEntity.badRequest().body(
                        new Response<>("Se requiere nombre, apellido y demás datos.", null, true, 400));
            }

            if (authToken.isBlank() || authToken.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new Response<>("Se requiere el token de usuario.", null, true, 400));
            }

            String token = authToken.substring(7);

            DeliveryEntregasData new_deliveryData = userService.updateDeliveryInfo(deliveryData, token);

            return ResponseEntity.ok("Se ha creado satisfactoriamente el repartidor:" + new_deliveryData);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("No se ha podido crear el repartidor." + e.getMessage());
        }
    }

}
