package com.example.GestionUsuarios.services;

import com.JSCode.GestionUsuarios.dto.ProfileImageDTO;
import com.JSCode.GestionUsuarios.dto.UserDataDTO;
import com.JSCode.GestionUsuarios.dto.register.EditDataDTO;
import com.JSCode.GestionUsuarios.dto.register.UserRegisterDto;
import com.JSCode.GestionUsuarios.dto.Auth.RecoverResponse;
import com.JSCode.GestionUsuarios.dto.WorkerRegisterDto;
import com.JSCode.GestionUsuarios.exceptions.BadRequestException;
import com.JSCode.GestionUsuarios.exceptions.ConflictException;
import com.JSCode.GestionUsuarios.exceptions.NotFoundException;
import com.JSCode.GestionUsuarios.models.*;
import com.JSCode.GestionUsuarios.repositories.*;
import com.JSCode.GestionUsuarios.security.JwtUtil;
import com.JSCode.GestionUsuarios.services.*;
import com.JSCode.GestionUsuarios.services.Email.EmailService;
import com.JSCode.GestionUsuarios.services.Email.RecoverEmail;
import com.JSCode.GestionUsuarios.services.Email.checkEmailService;
import com.JSCode.GestionUsuarios.utils.VerificationStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import javax.mail.MessagingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository userRepository;
    private PersonRepository personRepository;
    private RolesRepository rolesRepository;
    private UserPerRoleRepository userPerRoleRepository;
    private UserRecoveryCodeRepository recoveryCodeRepository;
    private PasswordEncoder passwordEncoder;
    private checkEmailService checkEmailService;
    private RecoverEmail recoverEmail;
    private EmailService emailService;
    private JwtUtil jwtUtil;
    private ImageStorageService imageService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        personRepository = mock(PersonRepository.class);
        rolesRepository = mock(RolesRepository.class);
        userPerRoleRepository = mock(UserPerRoleRepository.class);
        recoveryCodeRepository = mock(UserRecoveryCodeRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        checkEmailService = mock(checkEmailService.class);
        recoverEmail = mock(RecoverEmail.class);
        emailService = mock(EmailService.class);
        jwtUtil = mock(JwtUtil.class);
        imageService = mock(ImageStorageService.class);

        userService = new UserService();
        injectFields();
    }

    private void injectFields() {
        try {
            for (var field : userService.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                switch (field.getName()) {
                    case "userRepository" -> field.set(userService, userRepository);
                    case "personRepository" -> field.set(userService, personRepository);
                    case "rolesRepository" -> field.set(userService, rolesRepository);
                    case "userPerRoleRepository" -> field.set(userService, userPerRoleRepository);
                    case "passwordEncoder" -> field.set(userService, passwordEncoder);
                    case "checkEmailService" -> field.set(userService, checkEmailService);
                    case "recoverEmail" -> field.set(userService, recoverEmail);
                    case "emailService" -> field.set(userService, emailService);
                    case "jwtUtil" -> field.set(userService, jwtUtil);
                    case "recoveryCodeRepository" -> field.set(userService, recoveryCodeRepository);
                    case "imageService" -> field.set(userService, imageService);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void userExistsById_returnsTrue() {
        when(userRepository.existsById(1L)).thenReturn(true);
        assertTrue(userService.userExistsById(1L));
    }

    @Test
    void userExistsByMail_returnsFalse() {
        when(userRepository.existsByMail("mail@test.com")).thenReturn(false);
        assertFalse(userService.userExistsByMail("mail@test.com"));
    }

    @Test
    void deactivationRequest_setsDeletedAt() {
        User user = new User();
        user.setDeleted_at(null);
        when(userRepository.findByMail("test@mail.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        User result = userService.DeactivationRequest("test@mail.com");
        assertNotNull(result.getDeleted_at());
    }

    @Test
    void verifyUser_setsVerifiedTrue() {
        User user = new User();
        user.setVerified(false);
        when(jwtUtil.extractUsername("token123")).thenReturn("email@test.com");
        when(userRepository.findByMail("email@test.com")).thenReturn(Optional.of(user));
        VerificationStatus status = userService.verifyUser("token123");
        assertEquals(VerificationStatus.VERIFIED_SUCCESS, status);
    }

    @Test
    void updatePassword_successfulUpdate() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPassword1!"))
            .thenReturn("hashed");
        boolean result = userService.updatePassword(1L, "NewPassword1!");
        assertTrue(result);
    }

    @Test
    void getUserAddress_returnsCorrectAddress() {
        User user = new User();
        Person person = new Person();
        person.setDireccion("Cra 1 #2-3");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(personRepository.findByUser(user)).thenReturn(Optional.of(person));
        String result = userService.getUserAddress(1L);
        assertEquals("Cra 1 #2-3", result);
    }

    @Test
    void saveVerificationCode_createsNewCode() {
        User user = new User();
        user.setMail("user@mail.com");
        when(userRepository.findByMail("user@mail.com")).thenReturn(Optional.of(user));
        when(recoveryCodeRepository.findByUser(user)).thenReturn(Optional.empty());
        userService.saveVerificationCode("user@mail.com", "123456");
        verify(recoveryCodeRepository).save(any());
    }

    @Test
    void verifyUserEdit_successfulVerification() {
        User user = new User();
        user.setPassword("encoded");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);
        User result = userService.verifyUserEdit(1L, "raw");
        assertEquals(user, result);
    }

    @Test
    void generateRecoveryToken_returnsToken() {
        User user = new User();
        user.setId(99L);
        when(userRepository.findByMail("mail@mail.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateRecoveryToken(99L)).thenReturn("token");
        String result = userService.generateRecoveryToken("mail@mail.com");
        assertEquals("token", result);
    }

    @Test
    void createWorker_createsUserWithRoleAndSendsMail() throws Exception {
        WorkerRegisterDto dto = new WorkerRegisterDto();
        dto.setEmail("worker@mail.com");
        dto.setRole_id(1L);

        when(userRepository.findByMail(dto.getEmail())).thenReturn(Optional.empty());
        when(rolesRepository.findById(1L)).thenReturn(Optional.of(new Roles()));

        userService.createWorker(dto);
        verify(userRepository).save(any());
        verify(recoverEmail).sendLoginData(any(), any());
    }

    @Test
    void getUserData_returnsUserDataDTO() {
        User user = new User();
        user.setId(1L);
        user.setMail("mail@mail.com");

        Person person = new Person();
        person.setNombre("nombre");
        person.setApellido("apellido");
        person.setDocument("doc");
        person.setTelefono("tel");
        person.setDireccion("dir");
        person.setProfileImageUrl("url");

        when(jwtUtil.extractUsername("token")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(personRepository.findByUser(user)).thenReturn(Optional.of(person));

        UserDataDTO dto = userService.getUserData("token");
        assertEquals("mail@mail.com", dto.getEmail());
    }

    @Test
    void updateProfileImage_updatesSuccessfully() {
        MultipartFile file = mock(MultipartFile.class);
        User user = new User();
        Person person = new Person();

        when(jwtUtil.extractUsername("token")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(personRepository.findByUser(user)).thenReturn(Optional.of(person));
        when(imageService.uploadImageToImgBB(file)).thenReturn("newImageUrl");

        ProfileImageDTO result = userService.updateProfileImage(file, "token");
        assertEquals("newImageUrl", result.getImageUrl());
    }

    @Test
    void checkRecoveryCode_validCodeReturnsResponse() {
        User user = new User();
        user.setId(1L);
        UserRecoveryCode code = new UserRecoveryCode();
        code.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        code.setCode("hashed");

        when(userRepository.findByMail("mail@mail.com")).thenReturn(Optional.of(user));
        when(recoveryCodeRepository.findByUser(user)).thenReturn(Optional.of(code));
        when(passwordEncoder.matches("123456", "hashed")).thenReturn(true);
        when(jwtUtil.generateRecoveryToken(1L)).thenReturn("rec-token");

        RecoverResponse response = userService.checkRecoveryCode("mail@mail.com", "123456");
        assertEquals("mail@mail.com", response.getMail());
        assertEquals("rec-token", response.getToken());
    }
}
