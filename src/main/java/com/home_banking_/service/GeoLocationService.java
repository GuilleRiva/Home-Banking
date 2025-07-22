package com.home_banking_.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GeoLocationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getLocationFromIP (String ip){

        String url = "http://ip-api.com/json/" + ip;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        try {
            if ("success".equalsIgnoreCase((String) response.getBody().get("status"))) {
                String city = (String) response.getBody().get("city");
                String country = (String) response.getBody().get("country");
                return city + "," + country;
            }
        }catch (Exception e) {

        }
        return "Unknown Location";

    }
}
