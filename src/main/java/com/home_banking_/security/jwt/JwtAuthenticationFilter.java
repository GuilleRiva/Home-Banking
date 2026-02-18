package com.home_banking_.security.jwt;

import com.home_banking_.security.token.TokenRepository;
import com.home_banking_.security.user.UserDetailsServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenRepository tokenRepo;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {


        // Endpoints públicos o que no requieren JWT
        String path = request.getRequestURI();

        if (path.startsWith("/api/auth/") ||
        path.startsWith("/v3/api-docs") ||
        path.startsWith("/swagger-ui") ||
        "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Leer extraer
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
                    return;
        }

        // Extraer JWT
        String jwt = authHeader.substring(authHeader.lastIndexOf(" ") + 1);

        System.out.println(">>> JWT PARA VALIDAR: [" + jwt + " ]");

        if (jwt.isEmpty()) {
            filterChain.doFilter(request,response);
            return;
        }


        try {
            // Extraer subject (username/email)
             String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Verificación criptografica + expiracion
                boolean cryptoValid = jwtService.isTokenValid(jwt);

                // validación de revocación/expiración en DB
                boolean dbValid = tokenRepo.existsByTokenAndExpiredFalseAndRevokedFalse(jwt);

                if (cryptoValid  && dbValid) {

                    //Cargar usuario
                    UserDetails ud = userDetailsService.loadUserByUsername(username);

                    //Authorities.
                    var auth = new UsernamePasswordAuthenticationToken(ud, null ,ud.getAuthorities());

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    log.info("JWT OK: principal={}, authorities={}", username, ud.getAuthorities());
                } else {
                    log.debug("JWT inválido : cryptoValid={}, dbValid={}, path={}",
                            cryptoValid, dbValid, path);
                }

            }

            //Continuar cadena
            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException e) {
            // Token mal formado / expirado / firma inválida
            log.debug("JWT exception : {}",  e.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");

            // No lancemos runtime; dejamos que EntryPoint responda 401
        }


    }
}
