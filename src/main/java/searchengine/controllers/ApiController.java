package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexResponse;
import searchengine.dto.search.response.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.refresh.RefreshingService;
import searchengine.services.search.SearchService;
import searchengine.services.sitemaps.IndexingService;
import searchengine.services.statistics.StatisticsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final RefreshingService refreshingService;
    private final SearchService searchService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponse> startIndexing() {
        return ResponseEntity.ok(indexingService.getStartIndexingResponse());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexResponse> stopIndexing() {
        return ResponseEntity.ok(indexingService.getStopIndexingResponse());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexResponse> indexPage(
            @RequestParam String url) {
        return ResponseEntity.ok(refreshingService.getIndexPageResponse(url));
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<SearchResponse> getSearchResult(
            @RequestParam("query") String query,
            @RequestParam(value = "site", required = false) String site,
            @RequestParam(value = "offset", required = false) Integer offset,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ResponseEntity.ok(searchService.getSearchResponse(query, site, offset, limit));
    }
}
