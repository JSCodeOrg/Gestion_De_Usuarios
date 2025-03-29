package com.JSCode.GestionUsuarios.services;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.JSCode.GestionUsuarios.dto.UserRegisterDto;
import com.JSCode.GestionUsuarios.exceptions.BadRequestException;
import com.JSCode.GestionUsuarios.exceptions.ConflictException;
import com.JSCode.GestionUsuarios.exceptions.NotFoundException;
import com.JSCode.GestionUsuarios.models.Person;
import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.models.UserPerRole;
import com.JSCode.GestionUsuarios.repositories.UserPerRoleRepository;
import com.JSCode.GestionUsuarios.repositories.PersonRepository;
import com.JSCode.GestionUsuarios.repositories.RolesRepository;
import com.JSCode.GestionUsuarios.repositories.UserRepository;
import com.JSCode.GestionUsuarios.services.Email.EmailService;
import com.JSCode.GestionUsuarios.services.Email.checkEmailService;

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
    private EmailService emailService;

    @Autowired
    private VerificationCodeGenerator codeGenerator;
    
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$"
    );

    private void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BadRequestException("La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial.");
        }
    }

    public User registerUser(UserRegisterDto data){

        if(userRepository.existsByMail(data.getMail())) {
            throw new ConflictException("El email ya está registrado");
        }
        if(personRepository.existsByDocument(data.getDocument())) {
            throw new ConflictException("El documento ya está registrado");
        }

        if(!checkEmailService.isValidEmail(data.getMail())){
            throw new BadRequestException("El email no es válido");
        }

        validatePassword(data.getPassword());

        String verificationCode = codeGenerator.generateVerificationCode();
        verificationCodes.put(data.getMail(), verificationCode);

        try{
            emailService.sendVerificationEmail(data.getMail(), verificationCode);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo de verificacion");
        }

        User user = new User();
            user.setMail(data.getMail());
            user.setPassword(passwordEncoder.encode(data.getPassword()));
            user.setVerified(false);
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
            userPerRole.setRole(rolesRepository.findByName("usuario").orElseThrow(() -> new NotFoundException("No se encontró el Rol Usuario")));
            userPerRoleRepository.save(userPerRole);

            return user;
    
    }

    public boolean verifyUser(String email, String code) {
        String savedCode = verificationCodes.get(email);
        if (savedCode != null && savedCode.equals(code)) {
            User user = userRepository.findByMail(email).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

            user.setVerified(true);
            userRepository.save(user);

            verificationCodes.remove(email);
            return true;
        }

        return false;
    }

    public User DeactivationRequest(String email){
        User user = userRepository.findByMail(email).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        user.setDeleted_at(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    
    public boolean emailExists(String email) {
        return userRepository.existsByMail(email);
    }
}
