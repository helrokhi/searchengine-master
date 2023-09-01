package searchengine.config.sitemaps;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Аннотация @Configuration означает, что в данном классе содержаться
 * методы, порождающие объекты, которые в дальнейшем могут использовать
 * другие классы.
 * Другим классам спринга, достаточно внедрить зависимость нужного типа,
 * и спринг автоматически найдет метод, создающий бин нужного типа.</p>
 */
@Configuration
public class SiteMapConfiguration {
    /**
     * Аннотация @Bean означает, что данный метод используется,
     * когда спрингу надо внедрить в другой класс спринга зависимость
     * типа ForkJoinPool
     * @return
     */
    @Bean
    public ForkJoinPool forkJoinPool() {
        return new ForkJoinPool();
    }

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }
}
