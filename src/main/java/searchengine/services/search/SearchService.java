package searchengine.services.search;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.search.Item;
import searchengine.config.search.Search;
import searchengine.dto.search.DataResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.sitemaps.PageService;
import searchengine.services.sitemaps.SiteService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@Getter
@RequiredArgsConstructor
public class SearchService {
    private final SiteService siteService;
    private final PageService pageService;
    private final Search search;
    private ExecutorService service = Executors.newCachedThreadPool();
    private List<DataResponse> dataResponses = new ArrayList<>(0);
    private SearchResponse response;

    public SearchResponse getSearchResponse(
            String query,
            String url,
            Integer offset,
            Integer limit) {
        System.out.println("1. SearchService getSearchResponse" +
                " siteService.getCountSites() - " + siteService.getCountSites() +
                " \n query - " + query +
                " \n offset " + offset +
                " \n limit " + limit +
                "");
        if (offset == 0) dataResponses.clear();
        if (dataResponses.isEmpty()) {
            System.out.println(
                    " Новый поиск." +
                            "");
            setResponse(query, url, offset, limit);
        } else {
            System.out.println(
                    " Выборка из готового List<DataResponse> dataResponses." +
                            " size - " + dataResponses.size() +
                            "");
            response = getNewSearchResponse(offset, limit);
        }

        System.out.println("2. SearchService getSearchResponse" +
                " response " + response +
                "");
        return response;
    }

    public void setResponse(String query,
                            String url,
                            Integer offset,
                            Integer limit) {
        List<SiteEntity> siteEntityList = siteService.findAll();
        System.out.println("1. SearchService setResponse" +
                " \n query - " + query +
                " \n offset " + offset +
                " \n limit " + limit +
                "");

        if (query.isEmpty()) {
            response = getSearchResponseIfQueryIsEmpty();
            return;
        }

        if (url != null) {
            SiteEntity siteEntity = siteService.getSiteEntityByLink(url, siteEntityList);
            System.out.println(
                    " siteEntity - " + siteEntity.getUrl() +
                            "");
            if (siteEntity == null) {
                response = getSearchResponseIfSiteEntityIsNull();
            } else {
                if (siteEntity.getStatus() == Status.INDEXING) {
                    response = getSearchResponseIfSiteIsIndexing();
                    return;
                }

                if (siteEntity.getStatus() == Status.FAILED) {
                    response = getSearchResponseIfSiteIsFailed();
                    return;
                }

                List<Item> listItemBySite = getListItemBySiteEntity(query, siteEntity);
                if (!listItemBySite.isEmpty()) dataResponses = setDataResponses(listItemBySite);
                response = getNewSearchResponse(offset, limit);
            }
            return;
        } else {
            System.out.println(
                    "Поиск проводим по всем сайтам из списка." +
                            "");

            if (siteService.isIndexing()) {
                response = getSearchResponseIfSiteIsIndexing();
                return;
            }
            if (siteService.isFailed()) {
                response = getSearchResponseIfSiteIsFailed();
                return;
            }

            List<Item> listItemByAllSites = getListItemByAllSites(query, siteEntityList);
            if (!listItemByAllSites.isEmpty()) dataResponses = setDataResponses(listItemByAllSites);
            response = getNewSearchResponse(offset, limit);
        }
        System.out.println(
                " size - " + dataResponses.size() +
                        "");
    }

    private SearchResponse getSearchResponseIfQueryIsEmpty() {
        response = new SearchResponse();
        response.setResult(false);
        response.setError("Задан пустой поисковый запрос");
        return response;
    }

    private SearchResponse getSearchResponseIfSiteIsIndexing() {
        response = new SearchResponse();
        response.setResult(false);
        response.setError("Поиск невозможен. Идет индексация.");
        return response;
    }

    private SearchResponse getSearchResponseIfSiteIsFailed() {
        response = new SearchResponse();
        response.setResult(false);
        response.setError("Поиск невозможен. Не выполнена индексация сайтов.");
        return response;
    }

    private SearchResponse getSearchResponseIfSiteEntityIsNull() {
        response = new SearchResponse();
        response.setResult(false);
        response.setError("Сайт с этим путем не индексируется в нашем API. " +
                "Попробуйте другой путь.");
        return response;
    }

    private SearchResponse getNewSearchResponse(Integer offset, Integer limit) {
        response = new SearchResponse();
        response.setResult(true);
        response.setCount(dataResponses.size());
        if (!dataResponses.isEmpty()) response.setDataResponse(getDataResponses(offset, limit));
        return response;
    }

    private List<Item> getListItemByAllSites(String query, List<SiteEntity> siteEntityList) {
        List<Item> listItemByAllSites = new ArrayList<>(0);
        siteEntityList
                .forEach(siteEntity -> listItemByAllSites
                        .addAll(getListItemBySiteEntity(query, siteEntity)));
        return listItemByAllSites;
    }

    private List<Item> getListItemBySiteEntity(String query, SiteEntity siteEntity) {
        Future<List<Item>> future = service.submit(search.startScanning(query, siteEntity));
        List<Item> listItemBySite;
        try {
            listItemBySite = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return listItemBySite;
    }

    private List<DataResponse> setDataResponses(
            List<Item> itemList) {

        List<DataResponse> responseList = new ArrayList<>();

        itemList.forEach(item -> {
            PageEntity pageEntity = pageService.getPageById(item.getPageId());
            SiteEntity siteEntity = siteService.getSiteById(pageEntity.getSiteId());

            DataResponse dataResponse = new DataResponse();
            dataResponse.setSite(siteEntity.getUrl());
            dataResponse.setSiteName(siteEntity.getName());
            dataResponse.setUri(pageEntity.getPath());
            dataResponse.setTitle(getTitlePage(pageEntity.getContent()));
            dataResponse.setSnippet(item.getSnippet());
            dataResponse.setRelevance(item.getRelevance());
            responseList.add(dataResponse);
        });

        return responseList
                .stream()
                .sorted(DataResponse::compareByRelevance)
                .collect(Collectors.toList());
    }

    private List<DataResponse> getDataResponses(Integer offset, Integer limit) {
        return dataResponses
                .stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private String getTitlePage(String content) {
        return Jsoup.parse(content).getElementsByTag("title").text();
    }
}
