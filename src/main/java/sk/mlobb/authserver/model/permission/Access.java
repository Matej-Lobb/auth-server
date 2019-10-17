package sk.mlobb.authserver.model.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Data
@Builder
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class Access {

    @Column(name = "read_all")
    private boolean readAll;
    @Column(name = "read_self")
    private boolean readSelf;
    @Column(name = "write_all")
    private boolean writeAll;
    @Column(name = "write_self")
    private boolean writeSelf;
}
