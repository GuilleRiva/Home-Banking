package com.home_banking_.mappers;

import com.home_banking_.dto.RequestDto.IPAddressRequestDto;
import com.home_banking_.dto.ResponseDto.IPAddressResponseDto;
import com.home_banking_.model.IPAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IPAddressMapper {

    IPAddressResponseDto toDTO(IPAddress ipAddress);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    IPAddress toEntity(IPAddressRequestDto dto);
}
