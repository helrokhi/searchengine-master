package searchengine.utils.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SnippetCall;
import searchengine.repositories.PageRepository;
import searchengine.utils.gradations.CollectLemmas;

@Service
@RequiredArgsConstructor
public class FragmentServiceImpl implements FragmentService {
    private final PageRepository pageRepository;
    private final CollectLemmas collectLemmas;

    @Override
    public SnippetCall startSnippet(DataItem dataItem) {
        return new SnippetCall(dataItem, pageRepository, collectLemmas);
    }
}
