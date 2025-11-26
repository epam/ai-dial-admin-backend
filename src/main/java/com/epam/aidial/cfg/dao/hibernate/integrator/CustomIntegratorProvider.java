package com.epam.aidial.cfg.dao.hibernate.integrator;

import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;

import java.util.List;

public class CustomIntegratorProvider implements IntegratorProvider {

    @Override
    public List<Integrator> getIntegrators() {
        return List.of(new CustomIntegrator());
    }
}
