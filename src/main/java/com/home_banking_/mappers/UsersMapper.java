package com.home_banking_.mappers;

import com.home_banking_.dto.request.UserRequestDto;
import com.home_banking_.dto.response.UserProfileResponseDto;
import com.home_banking_.dto.response.UserResponseDto;
import com.home_banking_.enums.Rol;
import com.home_banking_.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsersMapper {

    // ADMIN / INTERNAL DTO //
    @Mapping(target = "rol", source = "rol", qualifiedByName = "rolToString")
    UserResponseDto toUserResponseDto(Users users);

    @Mapping(target = "rol", source = "rol", qualifiedByName = "rolToString")
    @Mapping(target = "fullName", source = ".", qualifiedByName = "buildFullName")
    UserProfileResponseDto toUserProfileResponseDto(Users users);


    Users toEntity(UserRequestDto dto);

    @Named("rolToString")
    static String rolToString(Rol e){
        return e != null ? e.name() : null;
    }

    @Named("buildFullName")
    static String buildFullName(Users u) {
        if (u == null) return null;
        String name = u.getName() != null ? u.getName().trim() : "";
        String surname = u.getSurname() != null ? u.getSurname().trim() : "";
        String full = (name + "." + surname).trim();
        return full.isEmpty() ? null : full;
    }

}
