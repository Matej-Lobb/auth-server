package sk.mlobb.authserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "role_permissions")
public class RolePermissions {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "permission_id")
    private Long permissionId;
}
