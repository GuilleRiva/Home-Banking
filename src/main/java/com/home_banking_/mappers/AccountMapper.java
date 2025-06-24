package com.home_banking_.mappers;

import com.home_banking_.dto.RequestDto.AccountRequestDto;
import com.home_banking_.dto.ResponseDto.AccountResponseDto;
import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.TypeAccount;
import com.home_banking_.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    //Mapper entity - Dto response
    @Mapping(target = "typeAccount", source = "typeAccount", qualifiedByName = "enumToString")
    @Mapping(source = "statusAccount", target = "statusAccount", qualifiedByName = "enumToString")
    AccountResponseDto toDto(Account account);


    //Mapper DTO to entry -> entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(source = "typeAccount", target = "typeAccount", qualifiedByName = "stringToTypeAccount")
    @Mapping(source = "statusAccount", target = "statusAccount", qualifiedByName = "stringToStatusAccount")
    Account toEntity(AccountRequestDto dto);

    //convert Enum -> String
    @Named("enumToString")
    static String enumToString(Enum<?> e){
        return e != null ? e.name() : null;
    }

    //convert String -> Enum TypeAccount
    @Named("stringToTypeAccount")
    static TypeAccount stringToTypeAccount(String value){
        return value != null ? TypeAccount.valueOf(value) : null;
    }


    //convert String -> Enum StatusAccount
    @Named("stringToStatusAccount")
    static StatusAccount stringToStatusAccount(String value){
        return value != null ? StatusAccount.valueOf(value) : null ;
    }
}
