package com.home_banking_.service.impl;

import com.home_banking_.dto.RequestDto.AuditLogRequestDto;
import com.home_banking_.dto.ResponseDto.AuditLogResponseDto;
import com.home_banking_.enums.AuditType;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.AuditLogMapper;
import com.home_banking_.model.AuditLog;
import com.home_banking_.model.Users;
import com.home_banking_.repository.AuditLogRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.AuditLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UsersRepository usersRepository;
    private final AuditLogMapper auditLogMapper;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, UsersRepository usersRepository, AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.usersRepository = usersRepository;
        this.auditLogMapper = auditLogMapper;
    }


    @Override
    public void registerEvent(Long userId, String message, String typeEvent, String type) {
        //Buscar el usuario que generó el evento
        Users users = usersRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

       //Convertir el tipo (String) a Enum
        AuditType auditType;
        try {
            auditType = AuditType.valueOf(type.toUpperCase());
        }catch (IllegalArgumentException e){
            throw new BusinessException("Invalid audit type : " + type);
        }

        //Crear el log manualmente (no usamos mapper por que no hay DTO)
        AuditLog log = new AuditLog();
        log.setUsers(users);
        log.setAction(typeEvent);
        log.setDescription(message);
        log.setDateTime(LocalDateTime.now());
        log.setIpOrigin("127.0.0.1"); //Puedo luego obtener esto desde el request HTTP

        //guardar en base de datos
        auditLogRepository.save(log);
    }

    @Override
    public List<AuditLogResponseDto> getLogsByUser(Long user_id) {
        List<AuditLog> logs = auditLogRepository.findByUsers_IdOrderByDateTimeDesc(user_id);
        return logs.stream()
                .map(auditLogMapper::toDTO)
                .toList();
    }



    @Override
    public List<AuditLogResponseDto> getLogsByType(String type) {
        AuditType auditType = AuditType.valueOf(type.toUpperCase());
        List<AuditLog> logs = auditLogRepository.findAllByOrderByDateTimeDesc(auditType);
        return  logs.stream()
                .map(auditLogMapper::toDTO)
                .toList();
    }
}
