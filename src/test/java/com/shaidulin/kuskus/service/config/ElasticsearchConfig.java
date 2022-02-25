package com.shaidulin.kuskus.service.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shaidulin.kuskus.converter.DurationReadingConverter;
import com.shaidulin.kuskus.converter.PortionReadingConverter;
import com.shaidulin.kuskus.document.Portion;
import com.shaidulin.kuskus.service.impl.IngredientServiceImpl;
import com.shaidulin.kuskus.service.impl.ReceiptServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
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
import java.time.Duration;
import java.util.List;

@Configuration
@EnableReactiveElasticsearchRepositories(basePackages = "com.shaidulin.kuskus.repository")
@Import({
        IngredientServiceImpl.class,
        ReceiptServiceImpl.class,
        DurationReadingConverter.class,
        PortionReadingConverter.class
})
class ElasticsearchConfig {

    // To move to elasticsearch 8 see https://github.com/testcontainers/testcontainers-java/issues/5048
    // https://www.elastic.co/guide/en/elasticsearch/reference/current/configuring-stack-security.html
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
    ElasticsearchCustomConversions elasticsearchCustomConversions(
            Converter<String, Duration> durationReadingConverter,
            Converter<String, Portion> portionReadingConverter) {
        return new ElasticsearchCustomConversions(List.of(durationReadingConverter, portionReadingConverter));
    }

    @Bean
    public SimpleElasticsearchMappingContext elasticsearchMappingContext() {
        return new SimpleElasticsearchMappingContext();
    }

    @Bean
    public ElasticsearchConverter elasticsearchConverter(SimpleElasticsearchMappingContext elasticsearchMappingContext,
                                                         ElasticsearchCustomConversions elasticsearchCustomConversions) {
        MappingElasticsearchConverter mappingElasticsearchConverter = new MappingElasticsearchConverter(elasticsearchMappingContext);
        mappingElasticsearchConverter.setConversions(elasticsearchCustomConversions);
        return mappingElasticsearchConverter;
    }

    @Bean
    public ReactiveElasticsearchOperations reactiveElasticsearchTemplate(
            ReactiveElasticsearchClient reactiveElasticsearchClient, ElasticsearchConverter elasticsearchConverter) {
        return new ReactiveElasticsearchTemplate(reactiveElasticsearchClient, elasticsearchConverter);
    }

    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        return objectMapper;
    }
}