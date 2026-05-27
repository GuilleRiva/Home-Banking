package com.home_banking_.service.impl;

import com.home_banking_.dto.request.IPAddressRequestDto;
import com.home_banking_.dto.response.IPAddressResponseDto;
import com.home_banking_.exceptions.custom.BusinessException;
import com.home_banking_.exceptions.custom.ResourceNotFoundException;
import com.home_banking_.mappers.IPAddressMapper;
import com.home_banking_.model.IPAddress;
import com.home_banking_.model.Users;
import com.home_banking_.repository.IPAddressRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.IPAddressService;
import com.home_banking_.service.security.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class IPAddressServiceImpl implements IPAddressService {

    private final IPAddressRepository ipAddressRepository;
    private final UsersRepository usersRepository;
    private final CurrentUserService currentUserService;

    private final IPAddressMapper ipAddressMapper;

    public IPAddressServiceImpl(IPAddressRepository ipAddressRepository, UsersRepository usersRepository, CurrentUserService currentUserService, @Lazy IPAddressMapper ipAddressMapper) {
        this.ipAddressRepository = ipAddressRepository;
        this.usersRepository = usersRepository;
        this.currentUserService = currentUserService;
        this.ipAddressMapper = ipAddressMapper;
        
    }


    @Override
    @Transactional
    public IPAddressResponseDto registerIP(IPAddressRequestDto dto) {
        log.info("[REGISTER_IP_INIT] userId={} ip={}",
                dto.getUserId(),
                maskIp(dto.getIpAddress()));

        Users users= getUserByIdOrThrow(dto.getUserId());

        IPAddress ipAddress = buildIPAddress(dto,users);

        IPAddress savedIPAddress = ipAddressRepository.save(ipAddress);

        log.info("[REGISTER_IP_SUCCESS] userId={} ipAddressId={} ip={}",
                users.getId(),
                savedIPAddress.getId(),
                maskIp(savedIPAddress.getDirectionIP()));

        return ipAddressMapper.toDTO(savedIPAddress);
    }


    @Override
    @Transactional
    public void makeAsSuspicious(Long ipId) {
        log.info("[IP_MARK_SUSPICIOUS_INIT] ip={}",
                ipId);

        IPAddress ipAddress = getIpAddressByIdOrThrow(ipId);

        if (ipAddress.isSuspicious()) {
            log.info("[IP_MARK_SUSPICIOUS_SKIPPED] ipId={} reason=already_suspicious", ipId);
            return;
        }

        ipAddress.setSuspicious(true);
        ipAddressRepository.save(ipAddress);

        log.info("[IP_MARK_SUSPICIOUS_SUCCESS] ipId={} userId={}",
                ipAddress.getId(),
                ipAddress.getUsers() != null ? ipAddress.getUsers().getId() : null);
    }


    @Override
    @Transactional(readOnly = true)
    public boolean isSuspicious(String ip) {
        validateIp(ip);

        String maskedIp = maskIp(ip);

        boolean suspicious = ipAddressRepository.existsByDirectionIPAndSuspiciousTrue(ip);

        log.info("[IP_SUSPICIOUS_CHECK] ip={} suspicious={}",
                maskedIp, suspicious);

        return suspicious;
    }


    @Override
    @Transactional(readOnly = true)
    public List<IPAddressResponseDto> getIPsByUser(Long userId) {
        String requesterEmail = currentUserService.getCurrentUserEmail();

        log.info("[IP_FETCH_BY_USER_INIT] requesterEmail={} targetUserId={}",
                requesterEmail, userId);

        Users user = getUserByIdOrThrow(userId);

        List<IPAddressResponseDto> ips = ipAddressRepository.findByUsersId(user.getId())
                .stream()
                .map(ipAddressMapper::toDTO)
                .toList();

        log.info("[IP_FETCH_USER_SUCCESS] requesterEmail={} targetUserId={} ipCount={}",
                requesterEmail, user.getId(), ips.size());
        return ips;
    }



    private Users getUserByIdOrThrow(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
    }

    private IPAddress getIpAddressByIdOrThrow(Long ipId) {
        return ipAddressRepository.findById(ipId)
                .orElseThrow(()-> new ResourceNotFoundException("IP address not found"));
    }

    private IPAddress buildIPAddress(IPAddressRequestDto dto, Users user) {
        IPAddress ipAddress = ipAddressMapper.toEntity(dto);
        ipAddress.setDirectionIP(dto.getIpAddress());
        ipAddress.setRegistrationDate(LocalDateTime.now());
        ipAddress.setUsers(user);

        return ipAddress;
    }

    private void validateIp(String ip) {
        if (ip == null || ip.isBlank()) {
            throw new BusinessException("IP address is required");
        }
    }


    private String maskIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "unknown";
        }

        String[] parts = ip.split("\\.");

        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".***.***";
        }

        return "***";
    }
}
