package com.shaidulin.kuskus.config;

import com.shaidulin.kuskus.converter.DurationReadingConverter;
import com.shaidulin.kuskus.converter.PortionReadingConverter;
import com.shaidulin.kuskus.service.impl.ReceiptServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.net.InetSocketAddress;

@Configuration
@EnableReactiveElasticsearchRepositories(basePackages = "com.shaidulin.kuskus.repository")
@Import({ReceiptServiceImpl.class, ElasticsearchCustomConversions.class,
        DurationReadingConverter.class, PortionReadingConverter.class})
public class ElasticsearchConfig {

    @Autowired
    private ElasticsearchCustomConversions elasticsearchCustomConversions;

    @Bean(destroyMethod = "close")
    public ElasticsearchContainer container() {
        ElasticsearchContainer container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.12.0");
        container.start();
        return container;
    }

    @Bean
    public ReactiveElasticsearchClient reactiveElasticsearchClient(ElasticsearchContainer container) {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(InetSocketAddress.createUnresolved(container().getHost(), container.getFirstMappedPort()))
                .build();
        return ReactiveRestClients.create(clientConfiguration);

    }

    @Bean
    public SimpleElasticsearchMappingContext elasticsearchMappingContext() {
        return new SimpleElasticsearchMappingContext();
    }

    @Bean
    public ElasticsearchConverter elasticsearchConverter(SimpleElasticsearchMappingContext elasticsearchMappingContext) {
        MappingElasticsearchConverter mappingElasticsearchConverter = new MappingElasticsearchConverter(elasticsearchMappingContext);
        mappingElasticsearchConverter.setConversions(elasticsearchCustomConversions);
        return mappingElasticsearchConverter;
    }

    @Bean
    public ReactiveElasticsearchOperations reactiveElasticsearchTemplate(
            ReactiveElasticsearchClient reactiveElasticsearchClient, ElasticsearchConverter elasticsearchConverter) {
        return new ReactiveElasticsearchTemplate(reactiveElasticsearchClient, elasticsearchConverter);
    }
}