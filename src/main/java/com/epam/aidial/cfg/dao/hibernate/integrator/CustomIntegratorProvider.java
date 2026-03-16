package com.epam.aidial.cfg.dao.hibernate.integrator;

import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;

import java.util.List;

@RequiredArgsConstructor
public class CustomIntegratorProvider implements IntegratorProvider {

    private final TransactionTimestampContext transactionTimestampContext;

    @Override
    public List<Integrator> getIntegrators() {
        return List.of(new CustomIntegrator(transactionTimestampContext));
    }
}
