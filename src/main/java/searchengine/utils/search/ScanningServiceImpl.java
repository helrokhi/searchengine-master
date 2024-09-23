package searchengine.utils.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.ScanningCall;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.utils.gradations.CollectLemmas;
import searchengine.model.SiteEntity;

@Service
@RequiredArgsConstructor
public class ScanningServiceImpl implements ScanningService {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final CollectLemmas collectLemmas;
    private final FragmentService fragmentService;

    @Override
    public ScanningCall startScanning(String query, SiteEntity siteEntity) {
        return new ScanningCall(query, siteEntity,
                pageRepository,
                lemmaRepository, indexRepository,
                collectLemmas,
                fragmentService);
    }
}
