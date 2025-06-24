package com.home_banking_.mappers;

import com.home_banking_.dto.RequestDto.AuditLogRequestDto;
import com.home_banking_.dto.ResponseDto.AuditLogResponseDto;
import com.home_banking_.enums.AuditType;
import com.home_banking_.model.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(target = "type", source = "type", qualifiedByName = "enumToString")
    AuditLogResponseDto toDTO(AuditLog auditLog);


    @Mapping(source = "type", target = "type", qualifiedByName = "stringToAuditType")
    AuditLog toEntity(AuditLogRequestDto dto);


    @Named("enumToString")
    static String enumToString(AuditType e){
        return e != null ? e.name() : null;
    }

    @Named("stringToAuditType")
    static AuditType stringToAuditType(String value){
        return  value != null ? AuditType.valueOf(value) : null;
    }
}
