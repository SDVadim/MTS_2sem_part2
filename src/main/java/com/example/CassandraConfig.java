package com.example;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.Map;

@Configuration
public class CassandraConfig {

  @Bean
  public CqlSession cqlSession() {
    InetSocketAddress address = InetSocketAddress.createUnresolved("localhost", 9042);
    CqlSession session = CqlSession.builder()
        .addContactPoint(address)
        .withLocalDatacenter("datacenter1")
        .build();

    session.execute("""
        CREATE TABLE IF NOT EXISTS my_keyspace.user_audit (
            user_id UUID,
            event_time TIMESTAMP,
            event_type TEXT,
            event_details TEXT,
            PRIMARY KEY ((user_id), event_time)
        ) WITH CLUSTERING ORDER BY (event_time DESC)
           AND default_time_to_live = 31536000;
    """);

    return session;
  }
}