package in.neuw.right.services;

import com.nimbusds.jose.JOSEException;
import in.neuw.right.models.RightSideResponse;
import in.neuw.right.models.RightSideSignedRequest;
import in.neuw.right.models.UserData;
import in.neuw.right.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.andreinc.mockneat.unit.user.Emails.emails;
import static net.andreinc.mockneat.unit.user.Genders.genders;
import static net.andreinc.mockneat.unit.user.Names.names;
import static net.andreinc.mockneat.unit.user.Users.users;

/**
 * @author Karanbir Singh on 08/10/2022
 */
@Slf4j
@Service
public class RightSideService {

    @Autowired
    private JwtUtil jwtUtil;

    public Mono<RightSideResponse> getResponse(RightSideSignedRequest request, String correlationId) throws ParseException, JOSEException {
        Map<String, Object> inputDataMap = jwtUtil.validateRequestToken(request.getData());
        // generally we will make use of the inputDataObjectMap to construct the response

        List users = new ArrayList<UserData>();

        int i = 0;
        while (i < 300) {
            users.add(new UserData()
                    .setName(names().get())
                    .setGender(genders().get())
                    .setUsername(users().get())
                    .setEmail(emails().get()).setId(UUID.randomUUID().toString()));
            i++;
        }

        String token = jwtUtil.createResponseToken(inputDataMap, users, correlationId);
        RightSideResponse response = new RightSideResponse();
        response.setData(token).setCorrelationId(correlationId);
        return Mono.just(response);
    }

}
