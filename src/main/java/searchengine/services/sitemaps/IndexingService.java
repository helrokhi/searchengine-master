package searchengine.services.sitemaps;

import searchengine.dto.indexing.IndexDto;

public interface IndexingService {

    IndexDto getIndexDtoAndStartIndexing();

    IndexDto getIndexDtoAndStopIndexing();
}
