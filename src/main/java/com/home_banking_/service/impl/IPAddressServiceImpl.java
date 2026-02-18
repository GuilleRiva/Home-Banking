package com.home_banking_.service.impl;

import com.home_banking_.dto.request.IPAddressRequestDto;
import com.home_banking_.dto.response.IPAddressResponseDto;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.IPAddressMapper;
import com.home_banking_.model.IPAddress;
import com.home_banking_.model.Users;
import com.home_banking_.repository.IPAddressRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.IPAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class IPAddressServiceImpl implements IPAddressService {

    private final IPAddressRepository ipAddressRepository;
    private final UsersRepository usersRepository;

    private final IPAddressMapper ipAddressMapper;

    public IPAddressServiceImpl( IPAddressRepository ipAddressRepository, UsersRepository usersRepository,@Lazy IPAddressMapper ipAddressMapper) {
        this.ipAddressRepository = ipAddressRepository;
        this.usersRepository = usersRepository;
        this.ipAddressMapper = ipAddressMapper;
        
    }


    @Override
    public IPAddressResponseDto registerIP(IPAddressRequestDto dto) {

        Users users= usersRepository.findById(Long.valueOf(dto.getId()))
                .orElseThrow(()->  new ResourceNotFoundException(
                        "user not found"
                ));


        IPAddress ipAddress = ipAddressMapper.toEntity(dto);
        ipAddress.setDirectionIP(dto.getDirectionIP());
        ipAddress.setRegistrationDate(LocalDateTime.now());
        ipAddress.setUsers(users);
        ipAddressRepository.save(ipAddress);

        log.info("IP  registered for userID: {}", dto.getId());
        return ipAddressMapper.toDTO(ipAddress);
    }


    @Override
    public void makeAsSuspicious(Long ipId) {

        IPAddress ip = ipAddressRepository.findById(ipId)
                .orElseThrow(()->  new ResourceNotFoundException(
                        "IP not found"
                ));

        ip.setSuspicious(true);
        ipAddressRepository.save(ip);

        log.info("IP marked as suspicious. ID: {}", ipId);
    }



    @Override
    public boolean isSuspicious(String ip) {
        String masked = maskIP(ip);
        boolean result = ipAddressRepository.existsByDirectionIPAndSuspiciousTrue(ip);
        log.info("Suspicious IP verification. IP: {} | Result: {}", masked, result);
        return result;
    }


    @Override
    public List<IPAddressResponseDto> getIPsByUser(Long userId) {

        List<IPAddress> ips = ipAddressRepository.findByUsers_Id(userId);

        return ips.stream()
                .map(ipAddressMapper::toDTO)
                .toList();
    }


    @Override
    public void deleteIP(Long ipId) {

        IPAddress ip = ipAddressRepository.findById(ipId)
                .orElseThrow(()->  new ResourceNotFoundException(
                        "IP not found"
                ));

        ipAddressRepository.delete(ip);
        log.info("IP removed. ID: {}", ipId);
    }


    private String maskIP(String ip) {
        if (ip == null || ip.isBlank()) return "xxx.xxx.xxx.xxx";
        int lastDot = ip.lastIndexOf('.');
        return lastDot != -1 ? ip.substring(0, lastDot) + "***" : "IP hidden";

    }
}
