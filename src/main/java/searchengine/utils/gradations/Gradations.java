package searchengine.utils.gradations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dto.gradations.Gradation;
import searchengine.utils.sitemaps.Page;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;

@Component
@RequiredArgsConstructor
public class Gradations {
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final GradationCollectLemmas gradationCollectLemmas;

    public Gradation startGradation(Page page) {
        return new Gradation(page, lemmaRepository, indexRepository, gradationCollectLemmas);
    }
}
