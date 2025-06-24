package com.home_banking_.mappers;

import com.home_banking_.dto.RequestDto.TransactionRequestDto;
import com.home_banking_.dto.ResponseDto.TransactionResponseDto;
import com.home_banking_.enums.MovementAccountType;
import com.home_banking_.enums.StatusTransaction;
import com.home_banking_.enums.TypeTransaction;
import com.home_banking_.model.Transaction;
import jdk.jfr.Name;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TransactionMapper {


    @Mapping(target = "movementAccount", source = "movementAccount", qualifiedByName = "enumToString")
    @Mapping(target = "typeTransaction", source = "typeTransaction", qualifiedByName = "enumToString")
    @Mapping(target = "statusTransaction", source = "statusTransaction", qualifiedByName = "enumToString")
    TransactionResponseDto toDto(Transaction dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    Transaction toEntity(TransactionRequestDto dto);


    @Named("enumToString")
    static String enumToString(Enum<?> e){
        return e != null ? e.name() : null;
    }


    @Name("enumToString")
    static MovementAccountType stringToMovementAccountType(String value){
        return value != null ? MovementAccountType.valueOf(value) : null;
    }


    @Named("enumToString")
    static TypeTransaction stringToTypeTransaction(String value){
        return value != null ? TypeTransaction.valueOf(value) : null;
    }

    @Named("enumToString")
    static StatusTransaction stringToStatusTransaction(String value){
        return value != null ? StatusTransaction.valueOf(value) : null ;
    }
}
