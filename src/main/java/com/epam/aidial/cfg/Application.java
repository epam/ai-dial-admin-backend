package com.epam.aidial.cfg;

import com.epam.aidial.cfg.configuration.DatasourceVendorValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Set;

/**
 * Created by Aliaksei Kurnosau on 9/9/24.
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class Application {

    private static final Set<String> CLI_COMMANDS = Set.of("validate");

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        if (args.length > 0 && CLI_COMMANDS.contains(args[0])) {
            application.setWebApplicationType(WebApplicationType.NONE);
            application.setAdditionalProfiles("cli");
        } else {
            application.addListeners(new DatasourceVendorValidator());
        }
        application.run(args);
    }
}
