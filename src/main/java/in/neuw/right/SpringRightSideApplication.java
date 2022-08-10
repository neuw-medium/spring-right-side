package in.neuw.right;

import in.neuw.right.config.properties.JwksProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.PostConstruct;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(JwksProperties.class)
public class SpringRightSideApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRightSideApplication.class, args);
    }

    @Autowired
    private JwksProperties jwksProperties;

    @PostConstruct
    public void afterInit() {
        log.info("jwksProperties getJwks initialized - "+ jwksProperties.getJwks());
        log.info("jwksProperties getIssuers initialized - "+ jwksProperties.getIssuers());
    }

}
