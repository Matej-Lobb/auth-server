package sk.mlobb.authserver.model.rest.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckUserExistenceResponse {

    private Boolean usernameIsUnique;
    private Boolean emailIsUnique;
}
