package com.epam.aidial.cfg;

import com.epam.aidial.cfg.configuration.DatasourceVendorValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Created by Aliaksei Kurnosau on 9/9/24.
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class Application {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.addListeners(new DatasourceVendorValidator());
        application.run(args);
    }
}
