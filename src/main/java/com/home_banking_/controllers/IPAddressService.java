package com.home_banking_.controllers;

import com.home_banking_.dto.ResponseDto.IPAddressResponseDto;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.model.IPAddress;
import com.home_banking_.model.Users;
import com.home_banking_.repository.UsersRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "IP address controller", description = "Operations related to suspicious IPs and user tracking")
@RestController
@RequestMapping("/api/IPAddress")
@RequiredArgsConstructor
public class IPAddressService {

    private final IPAddressService ipAddressService;
    private final UsersRepository usersRepository;



    @Operation(
            summary = "Register a new IP address.",
            description = "Register a new IP address in the system, associated with a specified user account."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "IP registered successfully.",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = IPAddressResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data.")
    })
    @PostMapping("/register")
    public ResponseEntity<IPAddressResponseDto>registerIP(@RequestParam Long userId,
                                                          @RequestParam String ip){

        IPAddressResponseDto registeredIP = ipAddressService.registerIP(userId, ip).getBody();
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredIP);
    }



    @Operation(
            summary = "Mark an IP address as suspicious",
            description = "Updates the IP address status to 'SUSPICIOUS' bases on the provided IP ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "IP address marked as suspicious successfully"),
            @ApiResponse(responseCode = "404", description = "IP address not found"),
            @ApiResponse(responseCode = "400", description = "Invalid IP ID")
    })
    @PutMapping("/{ipId}/suspicious")
    public ResponseEntity<Void> makeAsSuspicious(
            @Parameter(name = "IpId", description = "Unique identifier of the IP address", required = true)
            @PathVariable Long ipId){

        ipAddressService.makeAsSuspicious(ipId);
        return ResponseEntity.noContent().build();
    }




    @Operation(
            summary = "Check if an IP address is suspicious",
            description = "Returns true if the specified IP address is considered suspicious, false otherwise."
    )
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Suspicious status retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(type = "boolean", example = "true")))
    )
    @GetMapping("/check")
    public ResponseEntity<ResponseEntity<Boolean>> isSuspicious(@RequestParam String ip){
        return ResponseEntity.ok(ipAddressService.isSuspicious(ip).getBody());
    }




    @Operation(
            summary = "Get IP address by user ID",
            description = "Retrieves all IP addresses associated with the specified user ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "IP addresses retrieved successfully",
            content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = IPAddressResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<IPAddressResponseDto>> getIPsByUser(
            @Parameter(name = "userID", description = "Unique identifier of the user", required = true)
            @PathVariable Long userId){

        Users users = usersRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        List<IPAddressResponseDto> ipList = ipAddressService.getIPsByUser(users.getId()).getBody();
        return ResponseEntity.ok(ipList);

    }
}
