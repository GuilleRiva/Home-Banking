package com.home_banking_.mappers;

import com.home_banking_.dto.RequestDto.LoanRequestDto;
import com.home_banking_.dto.ResponseDto.LoanResponseDto;
import com.home_banking_.enums.StatusLoan;
import com.home_banking_.model.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "statusLoan", source = "statusLoan", qualifiedByName = "enumToString")
    LoanResponseDto toDto(Loan loan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "totalToPay", ignore = true)
    @Mapping(target = "amountQuota", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    Loan toEntity(LoanRequestDto dto);


    @Named("enumToString")
    static String enumToString(Enum<?> e){
        return e != null ? e.name() : null;
    }

    @Named("stringToStatusLoan")
    static StatusLoan stringToStatusLoan(String value){
        return value != null ? StatusLoan.valueOf(value) : null;
    }


}
