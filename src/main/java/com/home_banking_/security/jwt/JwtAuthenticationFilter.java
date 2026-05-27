package com.home_banking_.security.jwt;

import com.home_banking_.security.token.TokenRepository;
import com.home_banking_.security.user.UserDetailsServiceImpl;
import com.home_banking_.service.impl.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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


        String path = request.getRequestURI();

        if (path.startsWith("/api/auth/") ||
        path.startsWith("/v3/api-docs") ||
        path.startsWith("/swagger-ui") ||
        "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }


        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
                    return;
        }


        String jwt = authHeader.substring(7);

        log.debug("JWT received: ...{}", jwt.length() > 6 ? jwt.substring(jwt.length()-6) : jwt);

        if (jwt.isEmpty()) {
            filterChain.doFilter(request,response);
            return;
        }


        try {

             String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                boolean cryptoValid = jwtService.isTokenValid(jwt);

                boolean dbValid = tokenRepo.existsByTokenAndExpiredFalseAndRevokedFalse(jwt);

                if (cryptoValid  && dbValid) {


                    UserDetails ud = userDetailsService.loadUserByUsername(username);


                    var auth = new UsernamePasswordAuthenticationToken(ud, null ,ud.getAuthorities());

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    log.info("JWT OK: principal={}, authorities={}", username, ud.getAuthorities());
                } else {
                    log.debug("JWT inválido : cryptoValid={}, dbValid={}, path={}",
                            cryptoValid, dbValid, path);
                }

            }

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException e) {

            log.debug("JWT exception : {}",  e.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");

        }


    }
}
