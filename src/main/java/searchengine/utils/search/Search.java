package searchengine.utils.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dto.search.Scanning;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.utils.gradations.CollectLemmas;
import searchengine.model.SiteEntity;

@Component
@RequiredArgsConstructor
public class Search {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final CollectLemmas collectLemmas;
    private final Fragment fragment;

    public Scanning startScanning(String query, SiteEntity siteEntity) {
        return new Scanning(query, siteEntity,
                pageRepository,
                lemmaRepository, indexRepository,
                collectLemmas,
                fragment);
    }
}
