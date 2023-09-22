package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.search.DataItem;
import searchengine.utils.search.Search;
import searchengine.dto.search.response.DataResponse;
import searchengine.dto.search.response.SearchResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final Search search;
    private ExecutorService service = Executors.newCachedThreadPool();
    private List<DataItem> sortedDataItemList = new ArrayList<>(0);
    private SearchResponse response;

    public SearchResponse getSearchResponse(
            String query,
            String url,
            Integer offset,
            Integer limit) {
        System.out.println("1. SearchService getSearchResponse" +
                " siteService.getCountSites() - " + siteRepository.countAllSites() +
                " \n query - " + query +
                " \n offset " + offset +
                " \n limit " + limit +
                "");
        if (offset == 0) sortedDataItemList.clear();
        if (sortedDataItemList.isEmpty()) {
            System.out.println(
                    " Новый поиск." +
                            "");
            setResponse(query, url, offset, limit);
        } else {
            System.out.println(
                    " Выборка из готового List<DataItem> sortedDataItemList." +
                            " size - " + sortedDataItemList.size() +
                            "");
            response = getNewSearchResponse(offset, limit);
        }

        System.out.println("2. SearchService getSearchResponse" +
                " response " + response +
                "");
        return response;
    }

    private void setResponse(String query,
                            String url,
                            Integer offset,
                            Integer limit) {
        List<SiteEntity> siteEntityList = siteRepository.findAll();
        System.out.println("1. SearchService setResponse" +
                " query - " + query +
                " offset " + offset +
                " limit " + limit +
                "");

        if (query.isEmpty()) {
            response = getSearchResponseIfQueryIsEmpty();
            return;
        }

        if (siteEntityList.isEmpty()) {
            response = getSearchResponseIfDBIsEmpty();
            return;
        }

        if (url != null) {
            SiteEntity siteEntity = getSiteEntityByLink(url, siteEntityList);
            if (siteEntity == null) {
                response = getSearchResponseIfSiteEntityIsNull();
            } else {
                System.out.println(
                        " siteEntity - " + siteEntity.getUrl() +
                                "");
                if (siteEntity.getStatus() == Status.INDEXING) {
                    response = getSearchResponseIfSiteIsIndexing();
                    return;
                }

                if (siteEntity.getStatus() == Status.FAILED) {
                    response = getSearchResponseIfSiteIsFailed();
                    return;
                }

                List<DataItem> listDataItemBySite = getListItemBySiteEntity(query, siteEntity);
                if (!listDataItemBySite.isEmpty()) sortedDataItemList = getSortedDataItemList(listDataItemBySite);
                response = getNewSearchResponse(offset, limit);
            }
            return;
        } else {
            System.out.println(
                    "Поиск проводим по всем сайтам из списка." +
                            "");

            if (siteRepository.countAllByStatus(Status.INDEXING) != 0) {
                response = getSearchResponseIfSiteIsIndexing();
                return;
            }
            if (siteRepository.countAllByStatus(Status.FAILED) != 0) {
                response = getSearchResponseIfSiteIsFailed();
                return;
            }

            List<DataItem> listDataItemByAllSites = getListItemByAllSites(query, siteEntityList);
            if (!listDataItemByAllSites.isEmpty()) sortedDataItemList = getSortedDataItemList(listDataItemByAllSites);
            response = getNewSearchResponse(offset, limit);
        }
        System.out.println(
                " size - " + sortedDataItemList.size() +
                        "");
    }

    private SearchResponse getSearchResponseIfDBIsEmpty() {
        response = new SearchResponse();
        response.setResult(false);
        response.setError("Поиск невозможен. База данных пустая.");
        return response;
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
        response.setCount(sortedDataItemList.size());
        if (!sortedDataItemList.isEmpty()) response.setDataResponse(getDataResponses(offset, limit));
        return response;
    }

    private List<DataItem> getListItemByAllSites(String query, List<SiteEntity> siteEntityList) {
        List<DataItem> listDataItemByAllSites = new ArrayList<>(0);

        for (SiteEntity siteEntity : siteEntityList) {
            listDataItemByAllSites
                    .addAll(getListItemBySiteEntity(query, siteEntity));
        }
        return listDataItemByAllSites;
    }

    private List<DataItem> getListItemBySiteEntity(String query, SiteEntity siteEntity) {
        Future<List<DataItem>> future = service.submit(search.startScanning(query, siteEntity));
        List<DataItem> listDataItemBySite;
        try {
            listDataItemBySite = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return listDataItemBySite;
    }

    private List<DataResponse> setDataResponses(
            List<DataItem> dataItemList) {

        List<DataResponse> responseList = new ArrayList<>();

        for (DataItem dataItem : dataItemList) {
            PageEntity pageEntity = pageRepository
                    .getReferenceById(dataItem.getPageId());
            SiteEntity siteEntity = siteRepository
                    .getReferenceById(pageEntity.getSiteId());

            DataResponse dataResponse = new DataResponse();
            dataResponse.setSite(siteEntity.getUrl());
            dataResponse.setSiteName(siteEntity.getName());
            dataResponse.setUri(pageEntity.getPath());
            dataResponse.setTitle(getTitlePage(pageEntity.getContent()));
            dataResponse.setSnippet(dataItem.getSnippet());
            dataResponse.setRelevance(dataItem.getRelevance());
            responseList.add(dataResponse);
        }
        return responseList;
    }

    private List<DataResponse> getDataResponses(Integer offset, Integer limit) {
        return setDataResponses(getSortedItemListFromOffsetToLimit(offset, limit));
    }

    private String getTitlePage(String content) {
        return Jsoup.parse(content).getElementsByTag("title").text();
    }

    private List<DataItem> getSortedDataItemList(List<DataItem> dataItemList) {
        return dataItemList
                .stream()
                .sorted(DataItem::compareByRelevance)
                .collect(Collectors.toList());
    }

    private List<DataItem> getSortedItemListFromOffsetToLimit(Integer offset, Integer limit) {
        return sortedDataItemList
                .stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private SiteEntity getSiteEntityByLink(String link, List<SiteEntity> siteEntityList) {
        for (SiteEntity siteEntity : siteEntityList) {
            if (link.startsWith(siteEntity.getUrl()) || siteEntity.getUrl().equals(link)) {
                return siteEntity;
            }
        }
        return null;
    }
}
