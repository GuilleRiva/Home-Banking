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

    @Mapping(source = "statusCard", target = "statusCard", qualifiedByName = "enumToString")
    @Mapping(source = "typeCard", target = "typeCard", qualifiedByName = "enumToString")
    CardResponseDto toDTO(Card card);


    @Mapping(source = "typeCard", target = "typeCard", qualifiedByName = "stringToTypeCard")
    Card toEntity(CardRequestDto dto);


    @Named("enumToString")
    static String enumToString(Enum<?> e){
        return  e != null ? e.name() : null;
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
