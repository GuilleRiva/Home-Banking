package com.home_banking_.mappers;

import com.home_banking_.dto.RequestDto.NotificationRequestDto;
import com.home_banking_.dto.ResponseDto.NotificationResponseDto;
import com.home_banking_.enums.TypeNotification;
import com.home_banking_.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "typeNotification", source = "typeNotification", qualifiedByName = "typeNotificationToString")
    NotificationResponseDto toDto(Notification notification);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shippingDate", ignore = true)
    @Mapping(target = "typeNotification", source = "typeNotification", qualifiedByName = "stringToTypeNotification")
    Notification toEntity(NotificationRequestDto dto);



    @Named("typeNotificationToString")
    static String typeNotificationToString(TypeNotification value){
        return value != null ? value.name() : null;
    }


    @Named("stringToTypeNotification")
    static TypeNotification stringToTypeNotification(String value){
        return  value != null ? TypeNotification.valueOf(value) : null;
    }
}
