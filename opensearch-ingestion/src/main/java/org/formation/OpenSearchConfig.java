package org.formation;

import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenSearchConfig {
    @Value("${opensearch.host}")  private String host;
    @Value("${opensearch.port}")  private int    port;
    @Value("${opensearch.scheme}") private String scheme;

    @Bean
    public OpenSearchClient openSearchClient() {
        HttpHost host = new HttpHost(scheme, this.host, port);

        ApacheHttpClient5TransportBuilder builder =
                ApacheHttpClient5TransportBuilder.builder(host);

        builder.setMapper(new JacksonJsonpMapper());

        return new OpenSearchClient(builder.build());
    }
}
