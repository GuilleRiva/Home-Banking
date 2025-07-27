package com.home_banking_.controllers;

import com.home_banking_.dto.ResponseDto.CardResponseDto;
import com.home_banking_.enums.TypeCard;
import com.home_banking_.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            summary = "Retrieve cards by account ID",
            description = "Returns a list cards associated with the specified bank account."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "card by account found successfully",
            content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = CardResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "cards by account not found")
    })
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED')")
    public ResponseEntity<List<CardResponseDto>> getCardByAccount(@PathVariable Long accountId){
        log.info("GET /api/cards/account/{} - Consultying cards associated with the account", accountId);

        List<CardResponseDto> cards = cardService.getCardByAccount(accountId);
        log.info("Total cards found for account ID {}: {}", accountId, cards.size());
        return ResponseEntity.ok(cards);

    }




    @Operation(
            summary = "Create a new card",
            description = "Creates a new card and returns its data. The card is associated with a specific account."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card successfully created",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> createCard(
            @RequestBody Long accountId, TypeCard typeCard, String mark){
        log.info("POST /api/cards - Creating card  for ID account: {} | Type: {} | Mark: {}",
                accountId, typeCard, mark);

        CardResponseDto created = cardService.createCard(accountId, typeCard, mark);
        log.info("Card created successfully for account ID: {}", accountId);

        return new ResponseEntity<>(created, HttpStatus.CREATED);

    }




    @Operation(
            summary = "Delete a card",
            description = "Deletes a card permanently based on the provided card ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(
            @Parameter(name = "cardId", description = "Unique identifier of the card", required = true)
            @PathVariable Long cardId){

        log.info("DELETE /api/cards/{} - Deleting card", cardId);

        cardService.deleteCard(cardId);

        log.info("Card successfully removed. ID: {}", cardId);
        return ResponseEntity.noContent().build();
    }



    @Operation(
            summary = "Cancel a card",
            description = "Sets the status of the card to 'CANCELLED' based on the provided card ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "400", description = "Invalid card ID")
    })
    @PutMapping("/{cardId}/cancel")
    public ResponseEntity<Void>cancelCard(
            @Parameter(name = "cardId", description = "Unique identifier of the card to be cancelled", required = true)
            @PathVariable Long cardId){

        log.info("PUT /api/cards/{}/cancel - Cancelling card", cardId);

        cardService.cancelCard(cardId);
        log.info("Card successfully canceled");

        return ResponseEntity.noContent().build();
    }
}
