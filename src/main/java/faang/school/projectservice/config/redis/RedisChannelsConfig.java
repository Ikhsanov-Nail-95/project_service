package faang.school.projectservice.config.redis;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis.channels")
public class RedisChannelsConfig {

    @NotEmpty
    private String projectCreatedEvent;

    @Bean
    public ChannelTopic projectCreatedEventTopic() {
        return new ChannelTopic(projectCreatedEvent);
    }
}