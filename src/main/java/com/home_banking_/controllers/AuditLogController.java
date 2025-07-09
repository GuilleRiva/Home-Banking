package com.home_banking_.controllers;

import com.home_banking_.dto.ResponseDto.AuditLogResponseDto;
import com.home_banking_.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Audit logs", description = "Operations for registering and retrieving audit log events")
@RestController
@RequestMapping("/api/auditLog")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;


    @Operation(
            summary = "Register a new audit log",
            description = "This method will register a new action audit log"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "registered event successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AuditLogResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> registerAuditLog(@RequestBody @Valid AuditLogResponseDto dto){

        auditLogService.registerEvent(dto.getId(), dto.getType(), dto.getAction(), dto.getIpOrigin());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @Operation(
            summary = "Retrieve audit logs by user ID",
            description = "Returns a list of all audit log entries associated with a specified user."
    )
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully",
            content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = AuditLogResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "logs by user not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponseDto>> getLogsByUser(
            @Parameter(name = "userId", description = "unique identifier of the user", required = true)
            @PathVariable Long userId){

        List<AuditLogResponseDto> logs = auditLogService.getLogsByUser(userId);

        return ResponseEntity.ok(logs);
    }


    @Operation(
            summary = "Retrieves logs by type",
            description = "Returns a list containing all logs by type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "logs by type found",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AuditLogResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "logs by type not found")
    })
    @GetMapping("/type/{type}")
    public ResponseEntity<List<AuditLogResponseDto>> getLogsByType(
            @Parameter(name = "type", description = "Type of logs", required = true)
            @PathVariable String type){

        List<AuditLogResponseDto> logs = auditLogService.getLogsByType(type);

        return ResponseEntity.ok(logs);
    }

}
