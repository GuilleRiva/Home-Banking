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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IPAddressServiceImpl implements IPAddressService {

    private final IPAddressRepository ipAddressRepository;
    private final UsersRepository usersRepository;
    private final IPAddressMapper ipAddressMapper;

    public IPAddressServiceImpl(IPAddressRepository ipAddressRepository, UsersRepository usersRepository, IPAddressMapper ipAddressMapper) {
        this.ipAddressRepository = ipAddressRepository;
        this.usersRepository = usersRepository;
        this.ipAddressMapper = ipAddressMapper;
    }


    @Override
    public IPAddressResponseDto registerIP(IPAddressRequestDto dto) {
        Users users= usersRepository.findById(Long.valueOf(dto.getUserId()))
                .orElseThrow(()-> new ResourceNotFoundException("user not found"));

        IPAddress ipAddress = ipAddressMapper.toEntity(dto);
        ipAddress.setDirectionIP(dto.getDirectionIP());
        ipAddress.setRegistrationDate(LocalDateTime.now());
        ipAddress.setUsers(users);
        ipAddressRepository.save(ipAddress);

        return ipAddressMapper.toDTO(ipAddress);
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
    public List<IPAddressResponseDto> getIPsByUser(Long userId) {
        return ipAddressRepository.findByUsers(userId)
                .stream()
                .map(ipAddressMapper::toDTO)
                .toList();
    }




    @Override
    public void deleteIP(Long ipId) {
        IPAddress ip = ipAddressRepository.findById(ipId)
                .orElseThrow(()-> new ResourceNotFoundException("IP not found"));
        ipAddressRepository.delete(ip);

    }
}
