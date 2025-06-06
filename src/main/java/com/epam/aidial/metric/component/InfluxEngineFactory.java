package com.epam.aidial.metric.component;

import com.epam.aidial.metric.config.InfluxDatasetConfiguration;
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.TokenAuthorizationDeclaration;
import com.epam.aidial.metric.model.configuration.influx.InfluxDataSourceDeclaration;
import com.epam.aidial.metric.model.configuration.influx.InfluxDatasetDeclaration;
import com.epam.aidial.metric.service.influx.FluxQueryBuilder;
import com.epam.aidial.metric.service.influx.FluxQueryBuilderFactory;
import com.epam.aidial.metric.service.influx.InfluxEngine;
import com.epam.aidial.ql.Engine;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class InfluxEngineFactory implements EngineFactory {

    private final OkHttpClient.Builder clientBuilder;
    private final InfluxDatasetConfiguration datasetConfiguration;

    public InfluxEngineFactory(@Qualifier("influxOkHttpClientBuilder") OkHttpClient.Builder clientBuilder,
                               InfluxDatasetConfiguration datasetConfiguration) {
        this.clientBuilder = clientBuilder;
        this.datasetConfiguration = datasetConfiguration;
    }

    @Override
    public boolean supports(DatasetDeclaration dataset) {
        return dataset instanceof InfluxDatasetDeclaration;
    }

    @Override
    public Engine createEngine(DatasetDeclaration datasetDeclaration) {
        var influxDatasetDeclaration = (InfluxDatasetDeclaration) datasetDeclaration;
        var influxDbClient = createInfluxDbClient(influxDatasetDeclaration.getSource());
        var builderFactory = createFluxQueryBuilderFactory(influxDatasetDeclaration);

        return new InfluxEngine(influxDatasetDeclaration, influxDbClient, builderFactory);
    }

    private InfluxDBClient createInfluxDbClient(InfluxDataSourceDeclaration source) {
        if (source.getAuth() instanceof TokenAuthorizationDeclaration tokenAuthorization) {
            return InfluxDBClientFactory.create(
                    InfluxDBClientOptions.builder()
                            .url(source.getUrl())
                            .org(source.getOrg())
                            .authenticateToken(tokenAuthorization.getToken().toCharArray())
                            .okHttpClient(clientBuilder)
                            .build()
            );
        } else {
            throw new IllegalStateException("Unsupported auth type: " + source.getAuth().getClass().getName());
        }
    }

    private FluxQueryBuilderFactory createFluxQueryBuilderFactory(InfluxDatasetDeclaration datasetDeclaration) {
        return new FluxQueryBuilderFactory(datasetDeclaration, datasetConfiguration);
    }

}
