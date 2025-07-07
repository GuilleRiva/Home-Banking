package com.home_banking_.mappers;

import com.home_banking_.dto.RequestDto.PaymentRequestDto;
import com.home_banking_.dto.ResponseDto.PaymentResponseDto;
import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.enums.StatusPayment;
import com.home_banking_.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "serviceEntity", target = "serviceEntity", qualifiedByName = "serviceEntityToString")
    @Mapping(source = "statusPayment", target = "statusPayment", qualifiedByName = "statusPaymentToString")
    PaymentResponseDto toDto(Payment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(source = "statusPayment", target = "statusPayment", qualifiedByName = "stringToStatusPayment")
    @Mapping(source = "serviceEntity", target = "serviceEntity", qualifiedByName = "stringToServiceEntity")
    Payment toEntity(PaymentRequestDto dto);



    @Named("serviceEntityToString")
    static String serviceEntityToString(ServiceEntity value){
        return value != null ? value.name() : null;
    }


    @Named("statusPaymentToString")
    static String statusPaymentToString(StatusPayment value){
        return value != null ? value.name() : null;
    }


    @Named("stringToStatusPayment")
    static StatusPayment stringToStatusPayment(String value){
        return value != null ? StatusPayment.valueOf(value) : null;
    }


    @Named("stringToServiceEntity")
    static ServiceEntity stringToServiceEntity(String value){
        return value != null ? ServiceEntity.valueOf(value) : null;
    }


}
