package com.home_banking_.mappers;

import com.home_banking_.dto.response.AuditLogResponseDto;
import com.home_banking_.enums.AuditType;
import com.home_banking_.model.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(target = "type", source = "type", qualifiedByName = "typeToString")
    @Mapping(target = "userId", source = "users.id")
    AuditLogResponseDto toDTO(AuditLog auditLog);


    @Named("typeToString")
    static String typeToString(AuditType e){
        return e != null ? e.name() : null;
    }

}
