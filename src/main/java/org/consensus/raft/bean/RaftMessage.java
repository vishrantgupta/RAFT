package org.consensus.raft.bean;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.annotation.Validated;

@Data
@SuperBuilder
@Validated
public class RaftMessage implements Serializable {

    @NotNull
    private int currentTerm;

}
