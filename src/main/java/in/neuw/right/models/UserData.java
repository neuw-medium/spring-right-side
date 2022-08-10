package in.neuw.right.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Karanbir Singh on 08/10/2022
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@NoArgsConstructor
public class UserData {

    private String name;
    private String email;
    private String gender;
    private String username;
    private String id;

}
