package sk.mlobb.authserver.service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import sk.mlobb.authserver.model.Application;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.rest.request.UpdateApplicationDetailsRequest;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface ApplicationMapper {
}
