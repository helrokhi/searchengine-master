package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexDto;
import searchengine.dto.search.response.SearchDto;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.refresh.RefreshingService;
import searchengine.services.search.SearchingService;
import searchengine.services.sitemaps.IndexingService;
import searchengine.services.statistics.StatisticsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final RefreshingService refreshingService;
    private final SearchingService searchingService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexDto> startIndexing() {
        return ResponseEntity.ok(indexingService.getIndexDtoAndStartIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexDto> stopIndexing() {
        return ResponseEntity.ok(indexingService.getIndexDtoAndStopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexDto> indexPage(
            @RequestParam String url) {
        return ResponseEntity.ok(refreshingService.getIndexDtoAndStartRefreshing(url));
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<SearchDto> getSearchResult(
            @RequestParam("query") String query,
            @RequestParam(value = "site", required = false) String site,
            @RequestParam(value = "offset", required = false) Integer offset,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ResponseEntity.ok(searchingService.getSearchDto(query, site, offset, limit));
    }
}
