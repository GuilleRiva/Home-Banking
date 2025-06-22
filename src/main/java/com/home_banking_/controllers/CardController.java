package com.home_banking_.controllers;

import com.home_banking_.enums.TypeCard;
import com.home_banking_.model.Card;
import com.home_banking_.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;


    @GetMapping("/card/{accountId}")
    public ResponseEntity<List<Card>> getCardByAccount(Long accountId){
        List<Card> cards = cardService.getCardByAccount(accountId);
        return ResponseEntity.ok(cards);

    }


    @PostMapping
    public ResponseEntity<Card> createCard(@RequestBody Long accountId, TypeCard typeCard, String mark){
        Card created = cardService.createCard(accountId, typeCard, mark);

        return new ResponseEntity<>(created, HttpStatus.CREATED);

    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId){
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{cardId}/cancel")
    public ResponseEntity<Void>cancelCard(@PathVariable Long cardId){
        cardService.cancelCard(cardId);

        return ResponseEntity.noContent().build();
    }
}
