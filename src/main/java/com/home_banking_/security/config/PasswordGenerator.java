package com.home_banking_.security.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String[] passwords = {"cuervo", "fiesta", "piojos", "goat10", "patron"};

        for (String pwd : passwords){
            String hash = encoder.encode(pwd);
            System.out.println(pwd + "->" + hash);
        }
    }
}
