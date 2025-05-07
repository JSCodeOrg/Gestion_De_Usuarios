package com.JSCode.GestionUsuarios.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.JSCode.GestionUsuarios.dto.EditData;
import com.JSCode.GestionUsuarios.dto.WorkerRegisterDto;
import com.JSCode.GestionUsuarios.exceptions.BadRequestException;
import com.JSCode.GestionUsuarios.exceptions.ConflictException;
import com.JSCode.GestionUsuarios.exceptions.NotFoundException;
import com.JSCode.GestionUsuarios.models.Person;
import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.models.UserPerRole;
import com.JSCode.GestionUsuarios.models.UserRecoveryCode;
import com.JSCode.GestionUsuarios.repositories.UserPerRoleRepository;
import com.JSCode.GestionUsuarios.repositories.UserRecoveryCodeRepository;
import com.JSCode.GestionUsuarios.repositories.PersonRepository;
import com.JSCode.GestionUsuarios.repositories.RolesRepository;
import com.JSCode.GestionUsuarios.repositories.UserRepository;
import com.JSCode.GestionUsuarios.security.JwtUtil;
import com.JSCode.GestionUsuarios.services.email.EmailService;
import com.JSCode.GestionUsuarios.services.email.RecoverEmail;
import com.JSCode.GestionUsuarios.services.email.checkEmailService;
import com.JSCode.GestionUsuarios.dto.Auth.RecoverResponse;
import com.JSCode.GestionUsuarios.dto.register.UserRegisterDto;
import com.JSCode.GestionUsuarios.utils.PasswordGenerator;
import com.JSCode.GestionUsuarios.utils.VerificationCodeGenerator;
import com.JSCode.GestionUsuarios.utils.VerificationStatus;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private UserPerRoleRepository userPerRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private checkEmailService checkEmailService;

    @Autowired
    private RecoverEmail recoverEmail;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeGenerator codeGenerator;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRecoveryCodeRepository recoveryCodeRepository;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$");

    private void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BadRequestException(
                    "La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial.");
        }
    }

    public User registerUser(UserRegisterDto data) {

        if (userRepository.existsByMail(data.getMail())) {
            throw new ConflictException("El email ya está registrado");
        }
        if (personRepository.existsByDocument(data.getDocument())) {
            throw new ConflictException("El documento ya está registrado");
        }

        if (!checkEmailService.isValidEmail(data.getMail())) {
            throw new BadRequestException("El email no es válido");
        }

        validatePassword(data.getPassword());

        String verificationToken = jwtUtil.generateVerificationToken(data.getMail());
        
        try {
            emailService.sendVerificationEmail(data.getMail(), verificationToken);

        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo de verificacion");
        }

        User user = new User();
        user.setMail(data.getMail());
        user.setPassword(passwordEncoder.encode(data.getPassword()));
        user.setVerified(false);
        user.setFirstLogin(true);
        user = userRepository.save(user);

        Person person = new Person();
        person.setUser(user);
        person.setDocument(data.getDocument());
        person.setNombre(data.getNombre());
        person.setApellido(data.getApellido());
        person.setDireccion(data.getDireccion());
        person.setTelefono(data.getTelefono());
        person.setProfileImageUrl(null);
        person = personRepository.save(person);

        UserPerRole userPerRole = new UserPerRole();
        userPerRole.setUser(user);
        userPerRole.setRole(rolesRepository.findByName("usuario")
                .orElseThrow(() -> new NotFoundException("No se encontró el Rol Usuario")));
        userPerRoleRepository.save(userPerRole);

        return user;

    }

    public VerificationStatus verifyUser(String userToken) {
//todo: cambiar email por userId
        String email = jwtUtil.extractUsername(userToken);

        User user = this.userRepository.findByMail(email).orElseThrow(()-> new NotFoundException("Usuario no encontrado"));

        if(user.getVerified()){
            return VerificationStatus.ALREADY_VERIFIED;
        }

        user.setVerified(true);
        this.userRepository.save(user);

        return VerificationStatus.VERIFIED_SUCCESS;
    }

    public User DeactivationRequest(String email) {
        User user = userRepository.findByMail(email).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        user.setDeleted_at(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    public boolean userExistsById(Long userId) {
        return userRepository.existsById(userId);
    }

    public boolean userExistsByMail(String userMail) {
        return userRepository.existsByMail(userMail);
    }

    public void updateUserData(Long userId, EditData editData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Person person = personRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Información personal no encontrada"));

        if (editData.getNombre() != null) {
            person.setNombre(editData.getNombre());
        }
        if (editData.getApellido() != null) {
            person.setApellido(editData.getApellido());
        }
        if (editData.getDocument() != null) {

            if (personRepository.existsByDocumentAndUserNot(editData.getDocument(), user)) {
                throw new ConflictException("El documento ya está registrado por otro usuario");
            }
            person.setDocument(editData.getDocument());
        }
        if (editData.getTelefono() != null) {
            person.setTelefono(editData.getTelefono());
        }
        if (editData.getDireccion() != null) {
            person.setDireccion(editData.getDireccion());
        }

        personRepository.save(person);
    }

    public void saveVerificationCode(String mail, String verificationCode) {
        User user = userRepository.findByMail(mail).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Optional<UserRecoveryCode> existingCode = recoveryCodeRepository.findByUser(user);

        if (existingCode.isPresent()) {
            UserRecoveryCode recoveryCode = existingCode.get();
            recoveryCode.setCode(passwordEncoder.encode(verificationCode));
            recoveryCode.setExpiresAt(LocalDateTime.now().plusMinutes(60));
            recoveryCodeRepository.save(recoveryCode);
        } else {
            UserRecoveryCode recoveryCode = new UserRecoveryCode();
            recoveryCode.setEmail(mail);
            recoveryCode.setCode(passwordEncoder.encode(verificationCode));
            recoveryCode.setUser(user);
            recoveryCode.setExpiresAt(LocalDateTime.now().plusMinutes(60));
            recoveryCodeRepository.save(recoveryCode);
        }
    }

    public User verifyUserEdit(Long id, String password) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Contraseña incorrecta");
        }
        return user;
    }

    public RecoverResponse checkRecoveryCode(String mail, String code) {
        User user = userRepository.findByMail(mail).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        recoveryCodeRepository.findByUser(user).ifPresent(recoveryCode -> {
            if (recoveryCode.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("El código de recuperación ha expirado");
            }

            if (!passwordEncoder.matches(code, recoveryCode.getCode())) {
                throw new BadRequestException("Código de recuperación incorrecto");
            }
        });

        String recoveryToken = jwtUtil.generateRecoveryToken(user.getId());

        return new RecoverResponse(mail, recoveryToken);
    }

    public String generateRecoveryToken(String email){

        User user = this.userRepository.findByMail(email).orElseThrow(()-> new NotFoundException("Usuario no encontrado"));

        Long userId = user.getId();

        String token = jwtUtil.generateRecoveryToken(userId);

        return token;

    }

    public String createWorker(WorkerRegisterDto workerData) {


        return "Hola";

        /* 

        
        if (userRepository.findByMail(email).isPresent()) {
            throw new ConflictException("El email ya está registrado");
        }

        User user = new User();
        user.setMail(email);
        String provitionalpassword = PasswordGenerator.generatePassword();
        user.setPassword(passwordEncoder.encode(provitionalpassword));
        user.setFirstLogin(true);
        user.setVerified(true);

        userRepository.save(user);

        Person person = new Person();
        person.setUser(user);
        person.setDocument(UUID.randomUUID().toString());
        person.setNombre("Nombre");
        person.setApellido("Apellido");
        person.setDireccion("Direccion");
        person.setTelefono("telefono");
        person.setProfileImageUrl(null);
        personRepository.save(person);

        UserPerRole userPerRole = new UserPerRole();
        userPerRole.setUser(user);
        userPerRole.setRole(rolesRepository.findById(role_id)
                .orElseThrow(() -> new NotFoundException("No se encontró el Rol Usuario")));
        userPerRoleRepository.save(userPerRole);

        try {
            recoverEmail.sendLoginData(email, provitionalpassword);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo de recuperación", e);
        }

        return user;

        */
    }

    public boolean updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return true;

        }
    
}
