package com.home_banking_.mappers;

import com.home_banking_.dto.RequestDto.UserRequestDto;
import com.home_banking_.dto.ResponseDto.UserResponseDto;
import com.home_banking_.enums.Rol;
import com.home_banking_.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UsersMapper {

    @Mapping(target = "rol", source = "rol", qualifiedByName = "rolToString")
    UserResponseDto toDTO(Users users);

    @Mapping(source = "rol", target = "rol", qualifiedByName = "stringToRol")
    Users toEntity(UserRequestDto dto);


    @Named("rolToString")
    static String rolToString(Rol e){
        return e != null ? e.name() : null;
    }

    @Named("stringToRol")
    static Rol stringToRol(String value){
        return value != null ? Rol.valueOf(value) : null;
    }


}
