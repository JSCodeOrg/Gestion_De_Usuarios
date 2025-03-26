package com.JSCode.GestionUsuarios.services;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class VerificationCodeGenerator {
    public static String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    
}
