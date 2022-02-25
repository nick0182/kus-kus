package com.shaidulin.kuskus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shaidulin.kuskus.converter.DurationReadingConverter;
import com.shaidulin.kuskus.converter.PortionReadingConverter;
import com.shaidulin.kuskus.document.Portion;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.List;

@SpringBootApplication
public class KusKusApplication {

    @Value("${elasticsearch.address}")
    private String address;

    @Value("${elasticsearch.username}")
    private String username;

    @Value("${elasticsearch.password}")
    private String password;

    public static void main(String[] args) {
        SpringApplication.run(KusKusApplication.class, args);
    }

    @Bean
    ReactiveElasticsearchClient reactiveElasticsearchClient(SSLContext elasticsearchSSLContext) {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(address)
                .usingSsl(elasticsearchSSLContext)
                .withBasicAuth(username, password)
                .build();
        return ReactiveRestClients.create(clientConfiguration);
    }

    @Bean
    SSLContext elasticsearchSSLContext() throws CertificateException, IOException, KeyStoreException,
            NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate trustedCa;
        try (InputStream is = new ClassPathResource("keystore/http_ca.crt").getInputStream()) {
            trustedCa = factory.generateCertificate(is);
        }
        KeyStore trustStore = KeyStore.getInstance("pkcs12");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", trustedCa);
        return SSLContexts.custom().loadTrustMaterial(trustStore, null).build();
    }

    @Bean
    ElasticsearchCustomConversions elasticsearchCustomConversions(
            Converter<String, Duration> durationReadingConverter,
            Converter<String, Portion> portionReadingConverter) {
        return new ElasticsearchCustomConversions(List.of(durationReadingConverter, portionReadingConverter));
    }

    @Bean
    Converter<String, Duration> durationReadingConverter() {
        return new DurationReadingConverter();
    }

    @Bean
    Converter<String, Portion> portionReadingConverter() {
        return new PortionReadingConverter();
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