package searchengine.config.sites;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Setter
@Getter
public class Site {
    private String url;
    private String name;
}
