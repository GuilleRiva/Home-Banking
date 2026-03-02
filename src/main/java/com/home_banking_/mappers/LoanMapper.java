package com.home_banking_.mappers;

import com.home_banking_.dto.request.LoanSimulationRequestDto;
import com.home_banking_.dto.response.LoanResponseDto;
import com.home_banking_.enums.LoanStatus;
import com.home_banking_.model.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "statusLoan", source = "statusLoan", qualifiedByName = "statusLoanToString")
    LoanResponseDto toDto(Loan loan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "totalToPay", ignore = true)
    @Mapping(target = "installmentsAmount", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    Loan toEntity(LoanSimulationRequestDto dto);


    @Named("statusLoanToString")
    static String statusLoanToString(LoanStatus value){
        return value != null ? value.name() : null;
    }

    @Named("stringToStatusLoan")
    static LoanStatus stringToStatusLoan(String value){
        return value != null ? LoanStatus.valueOf(value) : null;
    }


}
