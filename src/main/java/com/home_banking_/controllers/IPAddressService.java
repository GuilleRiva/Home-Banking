package com.home_banking_.controllers;

import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.model.IPAddress;
import com.home_banking_.model.Users;
import com.home_banking_.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/IPAddress")
@RequiredArgsConstructor
public class IPAddressService {

    private final IPAddressService ipAddressService;
    private final UsersRepository usersRepository;


    @PostMapping("/register")
    public ResponseEntity<IPAddress>registerIP(@RequestParam Long userId,
                                                @RequestParam String ip){

        IPAddress registeredIP = ipAddressService.registerIP(userId, ip).getBody();
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredIP);
    }


    @PutMapping("/{ipId}/suspicious")
    public ResponseEntity<Void> makeAsSuspicious(@PathVariable Long ipId){
        ipAddressService.makeAsSuspicious(ipId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/check")
    public ResponseEntity<ResponseEntity<Boolean>> isSuspicious(@RequestParam String ip){
        return ResponseEntity.ok(ipAddressService.isSuspicious(ip).getBody());
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<IPAddress>> getIPsByUser(@PathVariable Long userId){
        Users users = usersRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        List<IPAddress> ipList = ipAddressService.getIPsByUser(users.getId()).getBody();
        return ResponseEntity.ok(ipList);

    }
}
