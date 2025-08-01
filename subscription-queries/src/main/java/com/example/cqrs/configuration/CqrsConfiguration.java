package com.example.cqrs.configuration;

import com.opencqrs.framework.eventhandler.progress.JdbcProgressTracker;
import org.postgresql.jdbc.PgConnection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jdbc.channel.PostgresChannelMessageTableSubscriber;
import org.springframework.integration.jdbc.channel.PostgresSubscribableChannel;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.DriverManager;

@Configuration
public class CqrsConfiguration {

    @Bean
    public DefaultLockRepository defaultLockRepository(DataSource dataSource) {
        var result = new DefaultLockRepository(dataSource);

        result.setPrefix("EVENTHANDLER_");
        return result;
    }

    @Bean
    public JdbcLockRegistry jdbcLockRegistry(LockRepository lockRepository) {
        return new JdbcLockRegistry(lockRepository);
    }

    @Bean
    public JdbcProgressTracker jdbcProgressTracker(
            DataSource dataSource, PlatformTransactionManager transactionManager) {
        return new JdbcProgressTracker(dataSource, transactionManager);
    }

    @Bean
    public JdbcChannelMessageStore messageStore(DataSource dataSource) {
        JdbcChannelMessageStore messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        return messageStore;
    }

    @Bean
    public PostgresChannelMessageTableSubscriber subscriber(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        return new PostgresChannelMessageTableSubscriber(() ->
                DriverManager.getConnection(url, username, password).unwrap(PgConnection.class));
    }

    @Bean
    public PostgresSubscribableChannel channel(
            PostgresChannelMessageTableSubscriber subscriber,
            JdbcChannelMessageStore messageStore) {
        return new PostgresSubscribableChannel(messageStore, "postgres", subscriber);
    }
}
