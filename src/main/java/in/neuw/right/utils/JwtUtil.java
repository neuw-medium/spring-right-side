package in.neuw.right.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import in.neuw.right.config.properties.JwksProperties;
import in.neuw.right.models.UserData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

import static net.andreinc.mockneat.unit.user.Emails.emails;
import static net.andreinc.mockneat.unit.user.Genders.genders;
import static net.andreinc.mockneat.unit.user.Names.names;
import static net.andreinc.mockneat.unit.user.Users.users;

/**
 * @author Karanbir Singh on 08/10/2022
 */
@Slf4j
@Component
public class JwtUtil {

    @Autowired
    private JwksProperties jwksProperties;

    private Map<String, JWKSet> jwkSetMap = new HashMap();

    @PostConstruct
    public void init() {
        jwksProperties.getIssuers().forEach(i -> {
            if (jwksProperties.getJwks().containsKey(i)) {
                String url = jwksProperties.getJwks().get(i);
                // TODO use proper url validator?
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    try {
                        JWKSet jwkSet = JWKSet.load(new URL(url));
                        jwkSetMap.put(i, jwkSet);
                        jwkSetMap.put(url, jwkSet);
                        log.info("successfully added remote jwks from "+ url +" for issuer "+i);
                    } catch (IOException | ParseException e) {
                        log.error("error while fetching the remote jwks from "+ url +" for issuer "+i);
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public Map<String, Object> validateRequestToken(final String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSHeader jwsHeader = signedJWT.getHeader();
        String issuerName = (String) jwsHeader.getCustomParam("issuer_name");
        String kid = jwsHeader.getKeyID();

        JWKSet jwkSet;
        if (jwkSetMap.containsKey(issuerName)) {
            jwkSet = jwkSetMap.get(issuerName);
        } else {
            throw new RuntimeException("could not fetch the jwkSet for the input JWS token");
        }

        Optional<JWK> optionalJWK = jwkSet.getKeys().stream().filter(k -> k.getKeyID().equals(kid)).findFirst();

        if (optionalJWK.isPresent()) {
            ECKey ecKey = (ECKey) optionalJWK.get();
            // can make it dynamic based on alg and typ for now ECDSA only
            try {
                signedJWT.verify(new ECDSAVerifier(ecKey));
            } catch (JOSEException e) {
                log.error("error while verifying the input token "+e.getMessage());
            }
        } else {
            throw new RuntimeException("could not fetch the JWK for the input JWS token with kid "+kid);
        }

        return signedJWT.getJWTClaimsSet().getClaims();
    }

    public String createResponseToken(final Map<String, Object> inputDataMap,
                                      final Object responseData,
                                      final String correlationId) throws JOSEException {
        String issuerName = (String) inputDataMap.get("issuer_name");
        JWKSet jwkSet;
        if (jwkSetMap.containsKey(issuerName)) {
            jwkSet = jwkSetMap.get(issuerName);
        } else {
            throw new RuntimeException("could not fetch the jwkSet for the input JWS token");
        }

        Optional<JWK> optionalJWK = jwkSet.getKeys().stream().filter(k -> k.getKeyType().equals(KeyType.EC) && k.getKeyUse().equals(KeyUse.ENCRYPTION)).findFirst();

        ECDHEncrypter ecdhEncrypter;
        ECKey ecKey;
        if (optionalJWK.isPresent()) {
            ecKey = (ECKey) optionalJWK.get();
            // can make it dynamic based on alg and typ for now ECDSA only
            try {
                ecdhEncrypter = new ECDHEncrypter(ecKey.toECPublicKey());
            } catch (JOSEException e) {
                log.error("error while generating the ecdhEncrypter "+e.getMessage());
                throw new RuntimeException("error while generating the ecdhEncrypter "+e.getMessage());
            }
        } else {
            // should use mine = right-side's?
            throw new RuntimeException("could not fetch the JWK for the input JWS token");
        }

        var jweHeader = new JWEHeader.Builder(JWEAlgorithm.ECDH_ES_A256KW, EncryptionMethod.A128CBC_HS256)
                .customParam("issuer_name", "right-side")
                .keyID(ecKey.getKeyID()).build();

        var payload = new JWTClaimsSet.Builder()
                .issuer("right-side")
                .audience("left-side")
                .subject("data-share")
                .claim("data", responseData)
                .expirationTime(Date.from(Instant.now().plusSeconds(120)))
                .build();

        EncryptedJWT encryptedJWT = new EncryptedJWT(jweHeader, payload);

        encryptedJWT.encrypt(ecdhEncrypter);

        return encryptedJWT.serialize();
    }

}
