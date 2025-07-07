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


    @Mapping(target = "movementAccountType", source = "movementAccountType", qualifiedByName = "movementAccountTypeToString")
    @Mapping(target = "typeTransaction", source = "typeTransaction", qualifiedByName = "typeTransactionToString")
    @Mapping(target = "statusTransaction", source = "statusTransaction", qualifiedByName = "statusTransactionToString")
    TransactionResponseDto toDto(Transaction dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    Transaction toEntity(TransactionRequestDto dto);



    @Name("movementAccountTypeToString")
    static String movementAccountTypeToString(MovementAccountType e){
        return e != null ? e.name() : null;
    }


    @Named("typeTransactionToString")
    static String typeTransactionToString(TypeTransaction value){
        return value != null ? value.name() : null;
    }


    @Named("statusTransactionToString")
    static String statusTransactionToString(StatusTransaction value) {
        return value != null ? value.name() : null;
    }
}
