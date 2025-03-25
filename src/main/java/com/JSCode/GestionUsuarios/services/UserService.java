package com.JSCode.GestionUsuarios.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.JSCode.GestionUsuarios.dto.UserRegisterDto;
import com.JSCode.GestionUsuarios.models.Person;
import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.models.UserPerRole;
import com.JSCode.GestionUsuarios.repositories.UserPerRoleRepository;
import com.JSCode.GestionUsuarios.repositories.PersonRepository;
import com.JSCode.GestionUsuarios.repositories.RolesRepository;
import com.JSCode.GestionUsuarios.repositories.UserRepository;

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
    

    public User registerUser(UserRegisterDto data){
        if(userRepository.existsByMail(data.getMail())) {
            throw new RuntimeException("Email already exists");
        }
        if(personRepository.existsByDocument(data.getDocument())) {
            throw new RuntimeException("Document already exists");
        }

        if(!checkEmailService.isValidEmail(data.getMail())){
            throw new RuntimeException("Invalid Email");
        }

        User user = new User();
            user.setMail(data.getMail());
            user.setPassword(passwordEncoder.encode(data.getPassword()));
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
            userPerRole.setRole(rolesRepository.findByName("usuario").orElseThrow(() -> new RuntimeException("No se encontró el Rol Usuario")));
            userPerRoleRepository.save(userPerRole);

            return user;
    }
}
