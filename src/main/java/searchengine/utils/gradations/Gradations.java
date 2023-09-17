package searchengine.utils.gradations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.gradations.Gradation;
import searchengine.config.sitemaps.Page;
import searchengine.utils.gradations.GradationCollectLemmas;
import searchengine.utils.methods.Methods;

@Component
@RequiredArgsConstructor
public class Gradations {
    private final Methods methods;
    private final GradationCollectLemmas gradationCollectLemmas;

    public Gradation startGradation(Page page) {
        return new Gradation(page, methods, gradationCollectLemmas);
    }
}
