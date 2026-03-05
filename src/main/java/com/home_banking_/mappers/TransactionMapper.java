package com.home_banking_.mappers;

import com.home_banking_.dto.request.TransactionRequestDto;
import com.home_banking_.dto.response.TransactionResponseDto;
import com.home_banking_.enums.StatusTransaction;
import com.home_banking_.enums.TransactionOperationType;
import com.home_banking_.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TransactionMapper {


    @Mapping(target = "typeTransaction", source = "typeTransaction", qualifiedByName = "typeTransactionToString")
    @Mapping(target = "statusTransaction", source = "statusTransaction", qualifiedByName = "statusTransactionToString")
    @Mapping(target = "originAccountId", source = "accountOrigin.id")
    @Mapping(target = "destinationAccountId",source = "accountDestiny.id")
    TransactionResponseDto toDto(Transaction tx);


    @Named("typeTransactionToString")
    static String typeTransactionToString(TransactionOperationType value){
        return value != null ? value.name() : null;
    }


    @Named("statusTransactionToString")
    static String statusTransactionToString(StatusTransaction value) {
        return value != null ? value.name() : null;
    }
}
