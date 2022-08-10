package in.neuw.right.web.controllers;

import com.nimbusds.jose.JOSEException;
import in.neuw.right.models.RightSideResponse;
import in.neuw.right.models.RightSideSignedRequest;
import in.neuw.right.services.RightSideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.text.ParseException;

/**
 * @author Karanbir Singh on 08/10/2022
 */
@RestController
@RequestMapping("/apis/v1/right")
public class RightSideController {

    private final RightSideService rightSideService;

    public RightSideController(RightSideService rightSideService) {
        this.rightSideService = rightSideService;
    }

    @PostMapping
    public Mono<RightSideResponse> getData(@RequestBody final RightSideSignedRequest request,
                                           final ServerWebExchange exchange) throws ParseException, JOSEException {
        String correlationId = exchange.getAttribute("correlation-id");
        return rightSideService.getResponse(request, correlationId);
    }

}
