package searchengine.config.gradations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.sitemaps.Page;
import searchengine.services.gradations.WordsService;
import searchengine.services.gradations.IndexService;
import searchengine.services.gradations.LemmaService;

@Component
public class Words {
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final WordsService wordsService;

    @Autowired
    public Words(
            LemmaService lemmaService,
            IndexService indexService,
            WordsService wordsService
    ) {
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.wordsService = wordsService;
    }

    public Gradation startGradation(Page page) {
        return new Gradation(page,
                lemmaService,
                indexService,
                wordsService);
    }
}
