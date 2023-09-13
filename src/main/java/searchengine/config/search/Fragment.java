package searchengine.config.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.gradations.CollectLemmas;
import searchengine.utils.sitemaps.PageService;

@Component
public class Fragment {
    private final PageService pageService;
    private final CollectLemmas collectLemmas;

    @Autowired
    public Fragment(PageService pageService, CollectLemmas collectLemmas) {
        this.pageService = pageService;
        this.collectLemmas = collectLemmas;
    }

    /**
     * Метод создает сниппет (фрагмент текста, в котором найдены совпадения)
     * для объекта класса Item
     *
     * @param item объект класса Item
     * @return объект класса Snippet implements Callable<String> для выполнения задач параллельно
     */
    public Snippet startSnippet(Item item) {
        return new Snippet(item, pageService, collectLemmas);
    }
}
