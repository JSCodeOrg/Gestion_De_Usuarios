package com.JSCode.GestionUsuarios.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.JSCode.GestionUsuarios.exception.BadRequestException;
import com.JSCode.GestionUsuarios.exception.ConflictException;
import com.JSCode.GestionUsuarios.exception.NotFoundException;
import com.JSCode.GestionUsuarios.model.Person;
import com.JSCode.GestionUsuarios.model.User;
import com.JSCode.GestionUsuarios.model.UserPerRole;
import com.JSCode.GestionUsuarios.model.UserRecoveryCode;
import com.JSCode.GestionUsuarios.repository.PersonRepository;
import com.JSCode.GestionUsuarios.repository.RolesRepository;
import com.JSCode.GestionUsuarios.repository.UserPerRoleRepository;
import com.JSCode.GestionUsuarios.repository.UserRecoveryCodeRepository;
import com.JSCode.GestionUsuarios.repository.UserRepository;
import com.JSCode.GestionUsuarios.security.JwtUtil;
import com.JSCode.GestionUsuarios.service.email.EmailService;
import com.JSCode.GestionUsuarios.service.email.RecoverEmail;
import com.JSCode.GestionUsuarios.service.email.checkEmailService;
import com.JSCode.GestionUsuarios.dto.auth.RecoverResponse;
import com.JSCode.GestionUsuarios.dto.register.EditDataDTO;
import com.JSCode.GestionUsuarios.dto.register.UserRegisterDto;
import com.JSCode.GestionUsuarios.dto.register.WorkerRegisterDto;
import com.JSCode.GestionUsuarios.dto.users.AddressDTO;
import com.JSCode.GestionUsuarios.dto.users.DeliveryDataDTO;
import com.JSCode.GestionUsuarios.dto.users.DeliveryEntregasData;
import com.JSCode.GestionUsuarios.dto.users.ProfileImageDTO;
import com.JSCode.GestionUsuarios.dto.users.UserDataDTO;
import com.JSCode.GestionUsuarios.utils.PasswordGenerator;
import com.JSCode.GestionUsuarios.utils.VerificationCodeGenerator;
import com.JSCode.GestionUsuarios.utils.VerificationStatus;

import jakarta.transaction.Transactional;

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
    private RepartidorClient repartidorClient;

    @Autowired
    private UserRecoveryCodeRepository recoveryCodeRepository;

    @Autowired
    private ImageStorageService imageService;

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
        user.setFirstLogin(false);
        user = userRepository.save(user);

        Person person = new Person();
        person.setUser(user);
        person.setDocument(data.getDocument());
        person.setNombre(data.getNombre());
        person.setApellido(data.getApellido());
        person.setDireccion(data.getDireccion());
        person.setTelefono(data.getTelefono());
        person.setProfileImageUrl(null);
        person.setCiudad(data.getCiudad());
        person = personRepository.save(person);

        UserPerRole userPerRole = new UserPerRole();
        userPerRole.setUser(user);
        userPerRole.setRole(rolesRepository.findByName("usuario")
                .orElseThrow(() -> new NotFoundException("No se encontró el Rol Usuario")));
        userPerRoleRepository.save(userPerRole);

        return user;

    }

    public VerificationStatus verifyUser(String userToken) {
        // todo: cambiar email por userId
        String email = jwtUtil.extractUsername(userToken);

        User user = this.userRepository.findByMail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (user.getVerified()) {
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

    @Transactional
    public EditDataDTO updateUserData(EditDataDTO editData, String token) {
        if (editData == null) {
            throw new BadRequestException("Los datos enviados son inválidos");
        }

        try {
            if (editData.getEmail() == null || editData.getEmail().isBlank()) {
                throw new BadRequestException("El email no puede ser nulo o vacío");
            }

            Optional<User> userWithEmail = userRepository.findByMail(editData.getEmail());

            if (userWithEmail.isPresent()) {
                Long user_id = userWithEmail.get().getId();

                Long requester_id_long;
                try {
                    requester_id_long = Long.parseLong(jwtUtil.extractUsername(token));
                } catch (NumberFormatException e) {
                    throw new BadRequestException("Token inválido: ID de usuario no numérico");
                }

                if (!user_id.equals(requester_id_long)) {
                    throw new ConflictException("El email ya se encuentra en uso");
                }
            }
            if (!checkEmailService.isValidEmail(editData.getEmail())) {
                throw new BadRequestException("El email no es válido");
            }

            Long userId = Long.parseLong(jwtUtil.extractUsername(token));
            User user = this.userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

            user.setMail(editData.getEmail());
            userRepository.save(user);

            Person person = personRepository.findByUser(user)
                    .orElseThrow(() -> new NotFoundException("Información personal no encontrada"));


            personRepository.findByDocument(editData.getDocument()).ifPresent(existing -> {
                if (!existing.getId().equals(person.getId())) {
                    throw new ConflictException("El documento ya está registrado por otro usuario");
                }
            });

            person.setNombre(editData.getNombre());
            person.setApellido(editData.getApellido());
            person.setDocument(editData.getDocument());
            person.setTelefono(editData.getTelefono());
            person.setDireccion(editData.getDireccion());
            personRepository.save(person);

            EditDataDTO editedData = new EditDataDTO();
            editedData.setNombre(person.getNombre());
            editedData.setApellido(person.getApellido());
            editedData.setDocument(person.getDocument());
            editedData.setEmail(user.getMail());
            editedData.setDireccion(person.getDireccion());
            editedData.setTelefono(person.getTelefono());

            return editedData;

        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar los datos del usuario: " + e.getMessage());
        }
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

    public String generateRecoveryToken(String email) {

        User user = this.userRepository.findByMail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Long userId = user.getId();

        String token = jwtUtil.generateRecoveryToken(userId);

        return token;

    }

    @Transactional
    public User createWorker(WorkerRegisterDto workerData, String authToken) {

        try {
            String email = workerData.getEmail();
            Long role_id = workerData.getRole_id();

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
                    .orElseThrow(() -> new NotFoundException("No se encontró el Rol provisto")));
            userPerRoleRepository.save(userPerRole);

            try {
                recoverEmail.sendLoginData(email, provitionalpassword);
            } catch (MessagingException e) {
                throw new RuntimeException("Error al enviar el correo de recuperación", e);
            }
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Ha ocurrido un error al crear el usuario." + e.getMessage());
        }
    }

    public boolean updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return true;

    }

    public UserDataDTO getUserData(String token) {

        Long userId = Long.parseLong(jwtUtil.extractUsername(token));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Person person = personRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Información personal no encontrada"));

        UserDataDTO userData = new UserDataDTO();
        userData.setFotoperfil(person.getProfileImageUrl());
        userData.setNombre(person.getNombre());
        userData.setApellido(person.getApellido());
        userData.setDocumento(person.getDocument());
        userData.setEmail(user.getMail());
        userData.setTelefono(person.getTelefono());
        userData.setDireccion(person.getDireccion());

        return userData;
    }

    public ProfileImageDTO updateProfileImage(MultipartFile file, String token) {
        String user_id = this.jwtUtil.extractUsername(token);

        Long user_id_long = Long.parseLong(user_id);

        User user = this.userRepository.findById(user_id_long)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Person person = this.personRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Información del usuario no encontrada"));
        String newImageUrl = this.imageService.uploadImageToImgBB(file);

        person.setProfileImageUrl(newImageUrl);

        personRepository.save(person);

        ProfileImageDTO newImage = new ProfileImageDTO();

        newImage.setImageUrl(newImageUrl);

        return newImage;

    }

    public AddressDTO getUserAddress(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado al usuario"));

        Person person = personRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado la información de esta persona"));

        String user_address = person.getDireccion();
        String ciudad = person.getCiudad();

        AddressDTO address = new AddressDTO();
        address.setCiudad(ciudad);
        address.setDireccion(user_address);

        return address;
    }

    @Transactional
    public DeliveryEntregasData updateDeliveryInfo(DeliveryDataDTO deliveryData, String authToken) {

        try {

            String user_id = jwtUtil.extractUsername(authToken);

            Long user_long = Long.parseLong(user_id);

            User user_found = userRepository.findById(user_long)
                    .orElseThrow(() -> new NotFoundException("No se ha encontrado un usuario asociado a este id"));

            personRepository.findByDocument(deliveryData.getDocument())
                    .ifPresent(existing -> {
                        throw new ConflictException("Ya existe esta persona en el sistema.");
                    });

            Person person_user = personRepository.findByUser(user_found)
                    .orElseThrow(() -> new NotFoundException("No se ha encontrado la data por defecto del usuario"));
            person_user.setNombre(deliveryData.getNombre());
            person_user.setApellido(deliveryData.getApellido());
            person_user.setDireccion(deliveryData.getDireccion());
            person_user.setDocument(deliveryData.getDocument());

            user_found.setPassword(passwordEncoder.encode(deliveryData.getPassword()));

            person_user.setTelefono(deliveryData.getTelefono());

            personRepository.save(person_user);

            DeliveryEntregasData deliveryInfo = new DeliveryEntregasData();
            deliveryInfo.setName(person_user.getNombre());
            deliveryInfo.setPhone(person_user.getTelefono());
            deliveryInfo.setUser_id(user_long);

            repartidorClient.sendDeliveryInfo(deliveryInfo, authToken);

            user_found.setFirstLogin(false);

            userRepository.save(user_found);

            return deliveryInfo;

        } catch (Exception e) {
            throw new RuntimeException("Error al crear el delivery" + e.getMessage());
        }
    }
}
