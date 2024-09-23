package searchengine.utils.gradations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.gradations.GradationThread;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.utils.sitemaps.SitePage;

@Service
@RequiredArgsConstructor
public class Gradations implements GradationService {
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final GradationCollectLemmas gradationCollectLemmas;

    @Override
    public GradationThread startGradation(SitePage page) {
        return new GradationThread(page, lemmaRepository, indexRepository, gradationCollectLemmas);
    }
}
