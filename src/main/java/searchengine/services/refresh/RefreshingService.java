package searchengine.services.refresh;

import searchengine.dto.indexing.IndexDto;

public interface RefreshingService {
    IndexDto getIndexDtoAndStartRefreshing(String url);
}
