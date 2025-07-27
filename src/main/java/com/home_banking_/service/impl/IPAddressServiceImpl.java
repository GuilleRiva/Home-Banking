package com.home_banking_.service.impl;

import com.home_banking_.dto.RequestDto.IPAddressRequestDto;
import com.home_banking_.dto.ResponseDto.IPAddressResponseDto;
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
        log.info("IP record requested for userID: {} | IP: {}", dto.getId(), maskIP(dto.getDirectionIP()));

        Users users= usersRepository.findById(Long.valueOf(dto.getId()))
                .orElseThrow(()-> {
                            log.warn("User not found when registering IP. ID: {}", dto.getId());
                           return new ResourceNotFoundException("user not found");
                        });


        IPAddress ipAddress = ipAddressMapper.toEntity(dto);
        ipAddress.setDirectionIP(dto.getDirectionIP());
        ipAddress.setRegistrationDate(LocalDateTime.now());
        ipAddress.setUsers(users);
        ipAddressRepository.save(ipAddress);

        log.info("IP successfully registered for userID: {}", dto.getId());
        return ipAddressMapper.toDTO(ipAddress);
    }



    @Override
    public void makeAsSuspicious(Long ipId) {
        log.info("Making IP as suspicious. IP ID: {}", ipId);

        IPAddress ip = ipAddressRepository.findById(ipId)
                .orElseThrow(()-> {
                    log.warn("IP not found when trying to mark as suspicious. ID: {}", ipId);
                   return new ResourceNotFoundException("IP not found");
                        });

        ip.setSuspicious(true);
        ipAddressRepository.save(ip);

        log.info("IP successfully marked as suspicious. ID: {}", ipId);
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
        log.info("Obtaining IPs associated with the user ID: {}", userId);

        List<IPAddress> ips = ipAddressRepository.findByUsers_Id(userId);

        log.debug("Total IPs found for userID: {}",ips);
        return ips.stream()
                .map(ipAddressMapper::toDTO)
                .toList();
    }




    @Override
    public void deleteIP(Long ipId) {
        log.info("IP removal request. ID: {}", ipId);

        IPAddress ip = ipAddressRepository.findById(ipId)
                .orElseThrow(()-> {
                    log.warn("IP not found when trying to delete. ID: {}", ipId);
                    return new ResourceNotFoundException("IP not found");
                });

        ipAddressRepository.delete(ip);
        log.info("IP successfully removed. ID: {}", ipId);

    }



    private String maskIP(String ip) {
        if (ip == null || ip.isBlank()) return "xxx.xxx.xxx.xxx";
        int lastDot = ip.lastIndexOf('.');
        return lastDot != -1 ? ip.substring(0, lastDot) + "***" : "IP hidden";

    }
}
