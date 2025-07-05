package com.home_banking_.controllers;

import com.home_banking_.dto.ResponseDto.AuditLogResponseDto;
import com.home_banking_.model.AuditLog;
import com.home_banking_.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditLog")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;


    @PostMapping("/register")
    public ResponseEntity<Void> registerAuditLog(@RequestParam Long userId,
                                                 @RequestParam String message,
                                                 @RequestParam String typeEvent,
                                                 @RequestParam String type){

        auditLogService.registerEvent(userId, message, typeEvent, type);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponseDto>> getLogsByUser(@PathVariable Long userId){
        List<AuditLogResponseDto> logs = auditLogService.getLogsByUser(userId);
        return ResponseEntity.ok(logs);
    }


    @GetMapping("/type/{type}")
    public ResponseEntity<List<AuditLogResponseDto>> getLogsByType(@PathVariable String type){
        List<AuditLogResponseDto> logs = auditLogService.getLogsByType(type);
        return ResponseEntity.ok(logs);
    }

}
