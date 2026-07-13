package com.home_banking_.controllers;

import com.home_banking_.dto.response.CardCreatedResponseDto;
import com.home_banking_.dto.response.CardResponseDto;
import com.home_banking_.service.CardService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Card controller", description = "User card management")
@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;



    @Operation(
            summary = "Create a card for the authenticated client",
            description = "Creates a card associated with an account owned by the authenticated client."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Card created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardCreatedResponseDto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request or business rule vibration"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CardCreatedResponseDto> createMyCard(
            @Valid @RequestBody CardCreatedResponseDto request){

        log.info("[POST_CREATE_MY_CARD] accountId={} typeCard={} brand={}",
                request.getAccountId(),
                request.getTypeCard(),
                request.getBrand());

        CardCreatedResponseDto response = cardService.createMyCard(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @Operation(
            summary = "Create a card for an account",
            description = "Allows an administrator or employee to create a card for an existing active account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Card created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardCreatedResponseDto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request or business rule violation"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYED')")
    public ResponseEntity<CardCreatedResponseDto> createCardForAccount(
            @Valid @RequestBody CardCreatedResponseDto request
    ) {
        log.info(
                "[POST_CREATE_CARD_FOR_ACCOUNT] accountId={} typeCard={} brand={}",
                request.getAccountId(),
                request.getTypeCard(),
                request.getBrand()
        );

        CardCreatedResponseDto response = cardService.createCardForAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @Operation(
            summary = "Retrieve all cards owned by the authenticated client",
            description = "Returns all cards associated with accounts owned by the authenticated client."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cards retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = CardResponseDto.class)
                            )
                    )
            )
    })
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CLIENT')")
    public ResponseEntity<List<CardResponseDto>> getMyCards() {
        log.info("[GET_MY_CARDS]");

        return ResponseEntity.ok(cardService.getMyCards());
    }



    @Operation(
            summary = "Retrieve authenticated client's cards by account",
            description = "Returns cards associated with an account owned by the authenticated client."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cards retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = CardResponseDto.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/me/accounts/{accountId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<CardResponseDto>> getMyCardsByAccount(
            @Parameter(
                    description = "Identifier of an account owned by the authenticated client",
                    required = true
            )
            @PathVariable Long accountId
    ) {
        log.info("[GET_MY_CARDS_BY_ACCOUNT] accountId={}", accountId);

        return ResponseEntity.ok(
                cardService.getMyCardsByAccount(accountId)
        );
    }


    @Operation(
            summary = "Retrieve cards by account",
            description = "Returns all cards associated with the specified account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cards retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = CardResponseDto.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/accounts/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYED', 'AUDITOR')")
    public ResponseEntity<List<CardResponseDto>> getCardsByAccount(
            @Parameter(
                    description = "Identifier of the account",
                    required = true
            )
            @PathVariable Long accountId
    ) {
        log.info("[GET_CARDS_BY_ACCOUNT] accountId={}", accountId);

        return ResponseEntity.ok(
                cardService.getCardsByAccount(accountId)
        );
    }

    @PatchMapping("/me/{cardId}/block")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> blockMyCard(
            @Parameter(description = "Identifier of the card", required = true)
            @PathVariable Long cardId
    ) {
        log.info("[PATCH_BLOCK_MY_CARD] cardId={}", cardId);

        cardService.blockMyCard(cardId);

        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Cancel a card owned by the authenticated client",
            description = "Changes a card owned by the authenticated client to CANCELLED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Card cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Card cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PatchMapping("/me/{cardId}/cancel")
    @PreAuthorize("hasAnyRole('CLIENT')")
    public ResponseEntity<Void> cancelMyCard(
            @Parameter(description = "Identifier of the card", required = true)
            @PathVariable Long cardId
    ) {
        log.info("[PATCH_CANCEL_MY_CARD] cardId={}", cardId);

        cardService.cancelMyCard(cardId);

        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Block a card",
            description = "Allows an administrator or employee to block an active card."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Card blocked successfully"),
            @ApiResponse(responseCode = "400", description = "Card cannot be blocked"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PatchMapping("/{cardId}/block")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYED')")
    public ResponseEntity<Void> blockCard(
            @Parameter(description = "Identifier of the card", required = true)
            @PathVariable Long cardId
    ){
        log.info("[PATCH_BLOCK_CARD] cardId={}", cardId);

        cardService.blockCard(cardId);

        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Cancel a card",
            description = "Allows an administrator or employee to cancel an active or blocked card."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Card cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Card cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PatchMapping("/{cardId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYED')")
    public ResponseEntity<Void>cancelCard(
            @Parameter(description = "Identifier of the card", required = true)
            @PathVariable Long cardId){

        log.info("[PATCH_CANCEL_CARD] cardId={}", cardId);

        cardService.cancelCard(cardId);

        return ResponseEntity.noContent().build();
    }
}
