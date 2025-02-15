package sk.mlobb.authserver.model.rest.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String email;
    private Boolean keepUpdated;
    private String firstName;
    private String lastName;
    private String password;
    private Boolean active;
    private LocalDate dateOfBirth;
    private String country;
    private byte[] profilePicture;
    private Set<String> roles;
}
