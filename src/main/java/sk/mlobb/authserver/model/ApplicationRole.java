package sk.mlobb.authserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "application_roles")
public class ApplicationRole implements Serializable {

    @Id
    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "role_id")
    private Long roleId;
}
