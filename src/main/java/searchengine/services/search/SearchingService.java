package searchengine.services.search;

import searchengine.dto.search.response.SearchDto;

public interface SearchingService {
    SearchDto getSearchDto(
            String query,
            String url,
            Integer offset,
            Integer limit);
}
