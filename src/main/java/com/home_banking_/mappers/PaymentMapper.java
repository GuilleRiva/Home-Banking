package com.home_banking_.mappers;

import com.home_banking_.dto.request.PaymentRequestDto;
import com.home_banking_.dto.response.PaymentResponseDto;
import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponseDto toDto(Payment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "statusPayment",ignore = true)
    @Mapping(source = "serviceEntity", target = "serviceEntity", qualifiedByName = "stringToServiceEntity")
    Payment toEntity(PaymentRequestDto dto);


    @Named("stringToServiceEntity")
    static ServiceEntity stringToServiceEntity(String value){
        return value != null ? ServiceEntity.valueOf(value) : null;
    }


}
