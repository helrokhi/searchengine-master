package searchengine.config.sitemaps;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ForkJoinPool;

@Configuration
public class SiteMapConfiguration {
    @Bean
    public ForkJoinPool forkJoinPool() {
        return new ForkJoinPool();
    }
}
