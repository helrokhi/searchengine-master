package searchengine.utils.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dto.search.Snippet;
import searchengine.repositories.PageRepository;
import searchengine.utils.gradations.CollectLemmas;

@Component
@RequiredArgsConstructor
public class Fragment {
    private final PageRepository pageRepository;

    private final CollectLemmas collectLemmas;

    public Snippet startSnippet(DataItem dataItem) {
        return new Snippet(dataItem, pageRepository, collectLemmas);
    }
}
