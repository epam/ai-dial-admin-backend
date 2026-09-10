package com.epam.aidial.metric.component;

import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.TokenAuthorizationDeclaration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DataSourceDeclaration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
import com.epam.aidial.metric.service.WindowGapFiller;
import com.epam.aidial.metric.service.influx3.Influx3Engine;
import com.epam.aidial.metric.service.influx3.SqlQueryBuilderFactory;
import com.epam.aidial.ql.Engine;
import com.influxdb.v3.client.InfluxDBClient;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class Influx3EngineFactory implements EngineFactory {

    private final WindowGapFiller windowGapFiller;

    public Influx3EngineFactory(WindowGapFiller windowGapFiller) {
        this.windowGapFiller = windowGapFiller;
    }

    @Override
    public boolean supports(DatasetDeclaration dataset) {
        return dataset instanceof Influx3DatasetDeclaration;
    }

    @Override
    public Engine createEngine(DatasetDeclaration datasetDeclaration) {
        var influx3DatasetDeclaration = (Influx3DatasetDeclaration) datasetDeclaration;
        var influx3DbClient = createInflux3DbClient(influx3DatasetDeclaration.getSource());
        var builderFactory = new SqlQueryBuilderFactory(influx3DatasetDeclaration);

        return new Influx3Engine(influx3DatasetDeclaration, influx3DbClient, builderFactory, windowGapFiller);
    }

    private InfluxDBClient createInflux3DbClient(Influx3DataSourceDeclaration source) {
        if (source.getAuth() instanceof TokenAuthorizationDeclaration tokenAuthorization) {
            return InfluxDBClient.getInstance(
                    source.getUrl(),
                    tokenAuthorization.getToken().toCharArray(),
                    source.getDatabase()
            );
        } else {
            throw new IllegalStateException("Unsupported auth type: " + source.getAuth().getClass().getName());
        }
    }
}
