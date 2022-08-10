package in.neuw.right.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * @author Karanbir Singh on 08/10/2022
 */
@ConstructorBinding
@ConfigurationProperties(prefix="app")
public class JwksProperties {

    private Set<String> issuers;

    private Map<String, String> jwks;

    public JwksProperties(Set<String> issuers, Map<String, String> jwks) {
        this.issuers = issuers;
        this.jwks = jwks;
    }

    public Set<String> getIssuers() {
        return issuers;
    }

    public Map<String, String> getJwks() {
        return jwks;
    }

}
