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

    @Mapping(source = "serviceEntity", target = "serviceEntity", qualifiedByName = "enumToString")
    @Mapping(source = "statusPayment", target = "statusPayment", qualifiedByName = "enumToString")
    PaymentResponseDto toDto(Payment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    Payment toEntity(PaymentRequestDto dto);



    @Named("enumToString")
    static String enumToString(Enum<?> e){
        return e != null ? e.name() : null;
    }


    @Named("enumToString")
    static StatusPayment stringToStatusPayment(String value){
        return value != null ? StatusPayment.valueOf(value) : null;
    }


    @Named("enumToString")
    static ServiceEntity stringToServiceEntity(String value){
        return value != null ? ServiceEntity.valueOf(value) : null ;
    }
}
