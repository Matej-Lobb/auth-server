package sk.mlobb.authserver.service.mappers;

import lombok.Builder;
import lombok.Data;
import sk.mlobb.authserver.model.UserEntity;
import sk.mlobb.authserver.model.rest.request.UpdateUserRequest;

@Data
@Builder
public class UpdateUserWrapper {

    private UpdateUserRequest request;
    private UserEntity userEntity;
}
