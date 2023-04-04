package org.consensus.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Validated
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class KeyValueModel implements Serializable {

    @JsonProperty("key")
    @Valid
    @NotNull
    private String key;

    @JsonProperty("value")
    @NotNull
    @Valid
    private String value;

    @JsonProperty("cmd")
    @NotNull
    @Valid
    private String cmd;

}
