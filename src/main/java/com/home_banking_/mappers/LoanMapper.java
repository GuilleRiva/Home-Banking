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

    @Mapping(target = "statusLoan", source = "loanStatus", qualifiedByName = "loanStatusToString")
    LoanResponseDto toDto(Loan loan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "totalToPay", ignore = true)
    @Mapping(target = "installmentsAmount", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    Loan toEntity(LoanSimulationRequestDto dto);


    @Named("loanStatusToString")
    static String LoanStatusToString(LoanStatus value){
        return value != null ? value.name() : null;
    }

    @Named("stringToLoanStatus")
    static LoanStatus stringToLoanStatus(String value){
        return value != null ? LoanStatus.valueOf(value) : null;
    }

}
