package com.home_banking_.mappers;

import com.home_banking_.dto.RequestDto.CardRequestDto;
import com.home_banking_.dto.ResponseDto.CardResponseDto;
import com.home_banking_.enums.StatusCard;
import com.home_banking_.enums.TypeCard;
import com.home_banking_.model.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(source = "statusCard", target = "statusCard", qualifiedByName = "statusCardToString")
    @Mapping(source = "typeCard", target = "typeCard", qualifiedByName = "typeCardToString")
    CardResponseDto toDTO(Card card);


    @Mapping(source = "typeCard", target = "typeCard", qualifiedByName = "stringToTypeCard")
    @Mapping(source = "statusCard", target = "statusCard", qualifiedByName = "stringToStatusCard")
    Card toEntity(CardRequestDto dto);


    @Named("statusCardToString")
    static String statusCardToString(StatusCard value){
        return  value != null ? value.name() : null;
    }


    @Named("typeCardToString")
    static String typeCardToString(TypeCard value){
        return value != null ? value.name() : null;
    }


    @Named("stringToStatusCard")
    static StatusCard stringToStatusCard(String value){
        return value !=  null ? StatusCard.valueOf(value) : null;
    }


    @Named("stringToTypeCard")
    static TypeCard stringToTypeCard(String value){
        return value != null ? TypeCard.valueOf(value) : null;
    }
}
