package searchengine.utils.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.search.Snippet;
import searchengine.utils.gradations.CollectLemmas;
import searchengine.utils.methods.Methods;

@Component
@RequiredArgsConstructor
public class Fragment {
    private final Methods methods;
    private final CollectLemmas collectLemmas;

    public Snippet startSnippet(DataItem dataItem) {
        return new Snippet(dataItem, methods, collectLemmas);
    }
}
