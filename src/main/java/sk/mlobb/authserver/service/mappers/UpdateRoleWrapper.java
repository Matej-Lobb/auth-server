package sk.mlobb.authserver.service.mappers;

import lombok.Builder;
import lombok.Data;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.rest.request.UpdateRoleRequest;

@Data
@Builder
public class UpdateRoleWrapper {

    private UpdateRoleRequest request;
    private Role role;
}
