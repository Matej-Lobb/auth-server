package sk.mlobb.authserver.model.rest.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

    private Set<Permission> permissions;

    @Getter
    @Setter
    @Builder
    public static class Permission {
        private String nameAlias;
        private Access access;

        @Getter
        @Setter
        @Builder
        public static class Access {
            private boolean readAll;
            private boolean readSelf;
            private boolean writeAll;
            private boolean writeSelf;
        }
    }
}
