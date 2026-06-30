package com.home_banking_.mappers;

import com.home_banking_.dto.response.AuditLogResponseDto;
import com.home_banking_.enums.audit.AuditType;
import com.home_banking_.model.AuditLog;
import com.home_banking_.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(target = "type", source = "type", qualifiedByName = "typeToString")
    @Mapping(target = "userId", source = "users", qualifiedByName = "userToId")
    AuditLogResponseDto toDTO(AuditLog auditLog);


    @Named("typeToString")
    static String typeToString(AuditType e){
        return e != null ? e.name() : null;
    }

    @Named("userToId")
    static Long userToId(Users user) {
        return user != null ? user.getId() : null;
    }

}
