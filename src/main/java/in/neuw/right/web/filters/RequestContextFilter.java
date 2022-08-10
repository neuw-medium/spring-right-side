package in.neuw.right.web.filters;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Karanbir Singh on 08/09/2022
 */
@Slf4j
public class RequestContextFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId;
        long startTime = System.currentTimeMillis();
        //log.info("url = {} startTime {} ", exchange.getRequest().getURI().getPath(), startTime);
        if (!StringUtils.hasText(exchange.getRequest().getHeaders().getFirst("x-correlation-id"))) {
            correlationId = UUID.randomUUID().toString();
        } else {
            correlationId = exchange.getRequest().getHeaders().getFirst("x-correlation-id");
        }
        return chain.filter(exchange).contextWrite((context) -> {
            MDC.put("correlation-id", correlationId);
            log.info("setting the correlation-id to the request, correlation-id={}", correlationId);
            Map<String, String> map = new HashMap();
            map.put("correlation-id", correlationId);
            context = context.put("request-context", map);
            exchange.getAttributes().put("correlation-id", correlationId);
            exchange.getResponse().beforeCommit(() -> {
                exchange.getResponse().getHeaders().add("x-correlation-id", correlationId);
                exchange.getResponse().getHeaders().setDate(ZonedDateTime.ofInstant(new Date().toInstant(), ZoneId.of("UTC")));
                long totalTime = System.currentTimeMillis() - startTime;
                exchange.getResponse().getHeaders().add("x-trace-time", Long.toString(totalTime)+"ms");
                return Mono.empty();
            });
            return context;
        }).doFinally(signalType -> {
            long totalTime = System.currentTimeMillis() - startTime;
            exchange.getAttributes().put("totalTime", totalTime);
            log.info("{}: url = {} processed with signalType={} with correlation-id={} in {} ms",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI().getPath(),
                    signalType,
                    correlationId,
                    totalTime);
        });
    }
}
