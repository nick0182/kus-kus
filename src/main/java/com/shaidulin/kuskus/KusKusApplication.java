package com.shaidulin.kuskus;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@SpringBootApplication
@EnableSolrRepositories("com.shaidulin.kuskus.repository")
public class KusKusApplication {

    public static void main(String[] args) {
        SpringApplication.run(KusKusApplication.class, args);
    }

    @Bean
    public SolrOperations solrTemplate(SolrClient solrClient) {
        return new SolrTemplate(solrClient);
    }
}