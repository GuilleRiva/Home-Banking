package com.home_banking_.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardResponseDto {

    private Long id;
    private String number;
    private LocalDateTime expiration;
    private String typeCard;
    private String statusCard;

}
