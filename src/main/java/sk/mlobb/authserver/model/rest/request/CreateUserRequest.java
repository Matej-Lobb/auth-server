package sk.mlobb.authserver.model.rest.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotNull
    @Email
    @NotNull(message = "'email' may not be null")
    private String email;

    @NotNull(message = "'keepUpdated' may not be null")
    private Boolean keepUpdated;

    @NotNull(message = "'username' may not be null")
    private String username;

    @NotNull(message = "'firstName' may not be null")
    private String firstName;

    @NotNull(message = "'lastName' may not be null")
    private String lastName;

    @NotNull(message = "'password' may not be null")
    private String password;

    @NotNull(message = "'active' may not be null")
    private Boolean active;

    @NotNull(message = "'dateOfBirth' may not be null")
    private LocalDate dateOfBirth;

    @NotNull(message = "'country' may not be null")
    private String country;

    private byte[] profilePicture;
}
