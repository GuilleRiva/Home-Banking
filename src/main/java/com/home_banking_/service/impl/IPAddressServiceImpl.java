package com.home_banking_.service.impl;

import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.model.IPAddress;
import com.home_banking_.model.Users;
import com.home_banking_.repository.IPAddressRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.IPAddressService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IPAddressServiceImpl implements IPAddressService {

    private final IPAddressRepository ipAddressRepository;
    private final UsersRepository usersRepository;

    public IPAddressServiceImpl(IPAddressRepository ipAddressRepository, UsersRepository usersRepository) {
        this.ipAddressRepository = ipAddressRepository;
        this.usersRepository = usersRepository;
    }


    @Override
    public IPAddress registerIP(Long userId, String ip) {
        Users users= usersRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("user not found"));

        IPAddress ipAddress = new IPAddress();
        ipAddress.setUsers(users);
        ipAddress.setDirectionIP(ip);
        ipAddress.setSuspicious(false);
        ipAddress.setRegistrationDate(LocalDateTime.now());

        return ipAddressRepository.save(ipAddress);
    }


    @Override
    public void makeAsSuspicious(Long ipId) {
        IPAddress ip = ipAddressRepository.findById(ipId)
                .orElseThrow(()-> new ResourceNotFoundException("IP not found"));

        ip.setSuspicious(true);
        ipAddressRepository.save(ip);
    }


    @Override
    public boolean isSuspicious(String ip) {
        return ipAddressRepository.existsByDirectionIPAndSuspiciousTrue(ip);
    }


    @Override
    public List<IPAddress> getIPsByUser(Long userID) {
        return ipAddressRepository.findByUser(userID);
    }


    @Override
    public void deleteIP(Long ipId) {
        if (!ipAddressRepository.existsById(ipId)){
            throw new ResourceNotFoundException("IP not found");
        }

    }
}
