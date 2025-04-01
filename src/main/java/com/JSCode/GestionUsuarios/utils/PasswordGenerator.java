package com.JSCode.GestionUsuarios.utils;

public class PasswordGenerator {

    private static final String Values = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int PASSWORD_LENGTH = 8; 
    
    public static String generatePassword(){
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for(int i = 0; i < PASSWORD_LENGTH; i++){
            int index = (int) (Math.random() * Values.length());
            char randomChar = Values.charAt(index);
            password.append(randomChar);
        }
        return password.toString();
    }

}
