package com.home_banking_.security.config;

import com.home_banking_.security.user.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        //1. Obtiene email y password del request
        String email = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();


        //2. Busca al usuario en la base de datos
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        //3. Verifica la contraseña de BCrypt
        if (!passwordEncoder.matches(rawPassword, userDetails.getPassword())){
            throw new BadCredentialsException("Invalid email or password");
        }


        //4. Si el válida, crea un token de autenticación
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
