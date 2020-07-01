package sk.mlobb.authserver.service.mappers;

import lombok.Builder;
import lombok.Data;
import sk.mlobb.authserver.model.RoleEntity;
import sk.mlobb.authserver.model.rest.request.UpdateRoleRequest;

@Data
@Builder
public class UpdateRoleWrapper {

    private UpdateRoleRequest request;
    private RoleEntity roleEntity;
}
