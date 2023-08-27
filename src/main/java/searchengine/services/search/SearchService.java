package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.search.Item;
import searchengine.config.search.Search;
import searchengine.config.sites.Site;
import searchengine.config.sites.SitesList;
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
@RequiredArgsConstructor
public class SearchService {
    private final SitesList sites;
    private final SiteService siteService;
    private final PageService pageService;
    private final Search search;
    private ExecutorService service = Executors.newCachedThreadPool();
    private List<DataResponse> dataResponses;

    public SearchResponse getSearchResponse(
            String query,
            String url,
            Integer offset,
            Integer limit) {
        System.out.println("1. SearchService getSearchResponse" +
                " siteService.getCountSites() - " + siteService.getCountSites() +
                " \n query - " + query +
                " \n offset '" + offset +
                " \n limit '" + limit +
                "'");

        SearchResponse response = new SearchResponse();

        if (offset == null) offset = 0;
        if (limit == null) limit = 20;

        if (offset == 0) response = getSearchResponseByNewSearch(query, url, response);

        response.setResult(true);
        response.setCount(dataResponses.size());
        if(!dataResponses.isEmpty()) response.setDataResponse(getDataResponses(offset, limit));
        System.out.println("2. SearchService getSearchResponse" +
                " response " + response +
                "");
        return response;
    }

    private SearchResponse getSearchResponseByNewSearch(String query,
                                                        String url,
                                                        SearchResponse response) {
        if (dataResponses != null) dataResponses.clear();

        if (query.isEmpty()) {
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
            return response;
        }

        if (url != null) {
            Site site = siteService.getSite(url, sites);
            System.out.println(
                    " site - " + site.getUrl() +
                            "");
            if (site == null) {
                response.setResult(false);
                response.setError("Сайт с этим путем не индексируется в нашем API. " +
                        "Попробуйте другой путь.");

            } else {
                if (siteService.getSiteEntity(site).getStatus() == Status.INDEXING) {
                    response.setResult(false);
                    response.setError("Поиск невозможен. Идет индексация.");
                    return response;
                }

                if (siteService.getSiteEntity(site).getStatus() == Status.FAILED) {
                    response.setResult(false);
                    response.setError("Поиск невозможен. Не выполнена индексация сайта.");
                    return response;
                }

                List<Item> listItemBySite = getListItemBySite(query, site);
                if (!listItemBySite.isEmpty()) dataResponses = setDataResponses(listItemBySite);
            }
        } else {
            System.out.println(
                    "Поиск проводим по всем сайтам из списка." +
                            "");

            if (siteService.isIndexing()) {
                response.setResult(false);
                response.setError("Поиск невозможен. Идет индексация.");
                return response;
            }
            if (siteService.isFailed()) {
                response.setResult(false);
                response.setError("Поиск невозможен. Не выполнена индексация сайтов.");
                return response;
            }

            List<Item> listItemByAllSites = getListItemByAllSites(query);
            if (!listItemByAllSites.isEmpty()) dataResponses = setDataResponses(listItemByAllSites);
        }
        System.out.println(
                        " size - " + dataResponses.size() +
                        "");
        return response;
    }

    private List<Item> getListItemByAllSites(String query) {
        List<Item> listItemByAllSites = new ArrayList<>(0);
        sites.getSites()
                .forEach(site -> listItemByAllSites.addAll(getListItemBySite(query, site)));
        return listItemByAllSites;
    }

    private List<Item> getListItemBySite(String query, Site site) {
        Future<List<Item>> future = service.submit(search.startScanning(query, site));
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
