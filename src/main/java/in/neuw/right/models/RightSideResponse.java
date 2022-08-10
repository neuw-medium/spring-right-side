package in.neuw.right.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Karanbir Singh on 08/09/2022
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@NoArgsConstructor
public class RightSideResponse {

    @JsonProperty("token_data")
    private String data;

    @JsonProperty("data_type")
    private String dataType;

    private String correlationId;

}
