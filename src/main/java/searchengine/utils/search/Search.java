package searchengine.utils.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.search.Scanning;
import searchengine.utils.gradations.CollectLemmas;
import searchengine.model.SiteEntity;
import searchengine.utils.methods.Methods;

@Component
@RequiredArgsConstructor
public class Search {
    private final Methods methods;
    private final CollectLemmas collectLemmas;
    private final Fragment fragment;

    public Scanning startScanning(String query, SiteEntity siteEntity) {
        return new Scanning(query, siteEntity,
                methods,
                collectLemmas,
                fragment);
    }
}
