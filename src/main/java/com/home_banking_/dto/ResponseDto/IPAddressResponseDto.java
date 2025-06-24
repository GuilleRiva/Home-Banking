package com.home_banking_.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class IPAddressResponseDto {

    private Long id;
    private String directionIP;
    private LocalDateTime registrationDate;
    private boolean suspicious;
}
