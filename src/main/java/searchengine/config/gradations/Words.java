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

    /**
     * Метод преобразует HTML-код переданной страницы в набор лемм и их количеств
     * и сохранять эту информацию в таблицы lemma и index базы данных
     *
     * @param page объект класса Page
     * @return объект класса Gradation extends Thread для выполнения задач параллельно
     */
    public Gradation startGradation(Page page) {
        return new Gradation(page,
                lemmaService,
                indexService,
                wordsService);
    }
}
