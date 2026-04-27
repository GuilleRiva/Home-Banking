package com.home_banking_.service.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class RequestHashServiceImpl implements RequestHashService{

    private final ObjectMapper objectMapper;

    @Override
    public String generateHash(Object request) {

        try {
            String json = objectMapper.writeValueAsString(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(json.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

        return hexString.toString();

    } catch (JsonProcessingException  e) {
            throw new RuntimeException("Error generating request hash");
        } catch (NoSuchAlgorithmException  e) {
            throw new RuntimeException("SHA-256 algorithm not available");
        }
    }
}
