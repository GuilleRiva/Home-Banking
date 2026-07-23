package com.home_banking_.mappers;

import com.home_banking_.dto.response.NotificationResponseDto;
import com.home_banking_.model.Notification;
import org.mapstruct.Mapper;


import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponseDto toResponseDto(Notification notification);

    List<NotificationResponseDto> toResponseDtoList(
            List<Notification> notifications
    );
}

