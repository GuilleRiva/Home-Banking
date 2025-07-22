package com.home_banking_.security.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String[] passwords = {"123456", "adminpass", "cliente1", "usuarioABC"};

        for (String pwd : passwords){
            String hash = encoder.encode(pwd);
            System.out.println(pwd + "->" + hash);
        }
    }
}
