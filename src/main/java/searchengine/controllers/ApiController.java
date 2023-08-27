package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.refresh.RefreshingService;
import searchengine.services.search.SearchService;
import searchengine.services.sitemaps.IndexingService;
import searchengine.services.statistics.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final RefreshingService refreshingService;
    private final SearchService searchService;

    @Autowired
    public ApiController(
            StatisticsService statisticsService,
            IndexingService indexingService,
            RefreshingService refreshingService,
            SearchService searchService
    ) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.refreshingService = refreshingService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        /**
         * Метод возвращает статистику и другую служебную информацию о
         * состоянии поисковых индексов и самого движка.
         * Если ошибок индексации того или иного сайта нет, задавать ключ error не
         * нужно.
         */
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponse> startIndexing() {
        /**
         * Метод запускает полную индексацию всех сайтов или полную
         * переиндексацию, если они уже проиндексированы.
         * Если в настоящий момент индексация или переиндексация уже
         * запущена, метод возвращает соответствующее сообщение об ошибке.
         */
        return ResponseEntity.ok(indexingService.getStartIndexingResponse());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexResponse> stopIndexing() {
        /**
         * Метод останавливает текущий процесс индексации (переиндексации).
         * Если в настоящий момент индексация или переиндексация не происходит,
         * метод возвращает соответствующее сообщение об ошибке.
         */
        return ResponseEntity.ok(indexingService.getStopIndexingResponse());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexResponse> indexPage(
            @RequestParam String url
    ) {
        /**
         * Метод добавляет в индекс или обновляет отдельную страницу, адрес
         * которой передан в параметре.
         * Если адрес страницы передан неверно, метод должен вернуть
         * соответствующую ошибку.
         */
        return ResponseEntity.ok(refreshingService.getIndexPageResponse(url));
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<SearchResponse> getSearchResult(
            @RequestParam("query") String query,
            @RequestParam(value = "site", required = false) String site,
            @RequestParam(value = "offset", required = false) Integer offset,
            @RequestParam(value = "limit", required = false) Integer limit)
    {
        /**
         * Метод осуществляет поиск страниц по переданному поисковому запросу
         * (параметр query).
         * Чтобы выводить результаты порционно, также можно задать параметры
         * offset (сдвиг от начала списка результатов) и limit (количество результатов,
         * которое необходимо вывести).
         * В ответе выводится общее количество результатов (count), не зависящее
         * от значений параметров offset и limit, и массив data с результатами поиска.
         * Каждый результат — это объект, содержащий свойства результата поиска (см.
         * ниже структуру и описание каждого свойства).
         * Если поисковый запрос не задан или ещё нет готового индекса (сайт, по
         * которому ищем, или все сайты сразу не проиндексированы), метод должен
         * вернуть соответствующую ошибку (см. ниже пример). Тексты ошибок должны
         * быть понятными и отражать суть ошибок.
         */
        return ResponseEntity.ok(searchService.getSearchResponse(query, site, offset, limit));
    }
}
