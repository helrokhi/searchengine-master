package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import searchengine.dto.enums.SearchMessageType;
import searchengine.dto.search.response.DataDto;
import searchengine.dto.search.response.SearchDto;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.search.DataItem;
import searchengine.utils.search.ScanningService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchingServiceImpl implements SearchingService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final ScanningService scanningService;

    private List<DataItem> sortedDataItemList = new ArrayList<>(0);
    private SearchDto searchDto;

    @Override
    public SearchDto getSearchDto(
            String query,
            String url,
            Integer offset,
            Integer limit) {

        log.info("1. SearchingService: " +
                        " site count - {}  " +
                        " query - {}  offset {} limit {} searchDto {} sortedDataItemList {}",
                siteRepository.countAllSites(), query, offset, limit, searchDto, sortedDataItemList);
        if (offset == 0) sortedDataItemList.clear();
        if (sortedDataItemList.isEmpty()) {
            log.info("Новый поиск.");
            setSearchDto(query, url, offset, limit);
        } else {
            log.info("Выборка из готового List<DataItem> sortedDataItemList." +
                    " size - {}", sortedDataItemList.size());
            searchDto = setNewSearchDto(sortedDataItemList, offset, limit);
        }

        log.info("2. SearchingService " +
                " searchDto {}", searchDto);
        return searchDto;
    }

    private void setSearchDto(String query,
                                   String url,
                                   Integer offset,
                                   Integer limit) {
        List<SiteEntity> siteEntityList = siteRepository.findAll();
        log.info("1. SearchingService setSearchDto" +
                " query - {} offset {} limit {}", query, offset, limit);

        if (query.isEmpty()) {
            searchDto = setSearchDtoIfQueryIsEmpty();
            return;
        }

        if (siteEntityList.isEmpty()) {
            searchDto = setSearchDtoIfDBIsEmpty();
            return;
        }

        if (url != null) {
            SiteEntity siteEntity = getSiteEntityByLink(url, siteEntityList);
            if (siteEntity == null) {
                searchDto = setSearchDtoIfSiteEntityIsNull();
            } else {
                log.info(" siteEntity - {}", siteEntity.getUrl());
                if (siteEntity.getStatus() == Status.INDEXING) {
                    searchDto = setSearchDtoIfSiteIsIndexing();
                    return;
                }

                if (siteEntity.getStatus() == Status.FAILED) {
                    searchDto = setSearchDtoIfSiteIsFailed();
                    return;
                }

                List<DataItem> listDataItemBySite = getListItemBySiteEntity(query, siteEntity);
                if (!listDataItemBySite.isEmpty())
                    sortedDataItemList = getSortedDataItemList(listDataItemBySite);
                log.info("sortedDataItemList: {}", sortedDataItemList.size());
                searchDto = setNewSearchDto(sortedDataItemList, offset, limit);
            }
            return;
        } else {
            log.info("Поиск проводим по всем сайтам из списка.");

            if (siteRepository.countAllByStatus(Status.INDEXING) != 0) {
                searchDto = setSearchDtoIfSiteIsIndexing();
                return;
            }
            if (siteRepository.countAllByStatus(Status.FAILED) != 0) {
                searchDto = setSearchDtoIfSiteIsFailed();
                return;
            }

            List<DataItem> listDataItemByAllSites = getListItemByAllSites(query, siteEntityList);
            if (!listDataItemByAllSites.isEmpty())
                sortedDataItemList = getSortedDataItemList(listDataItemByAllSites);
            log.info("sortedDataItemList: {}", sortedDataItemList.size());
            searchDto = setNewSearchDto(sortedDataItemList, offset, limit);
        }
        log.info(" size - {}", sortedDataItemList.size());
        //return searchDto;
    }

    private SearchDto setNewSearchDto(List<DataItem> sortedDataItemList, Integer offset, Integer limit) {
        return (sortedDataItemList.isEmpty()) ?
                SearchDto.builder()
                        .result(true)
                        .count(sortedDataItemList.size())
                        .build() :
                SearchDto.builder()
                        .result(true)
                        .count(sortedDataItemList.size())
                        .dataDtoList(getDataDtoPage(sortedDataItemList, offset, limit))
                        .build();
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
        ExecutorService service = Executors.newCachedThreadPool();

        Future<List<DataItem>> future = service.submit(scanningService.startScanning(query, siteEntity));
        List<DataItem> listDataItemBySite;
        try {
            listDataItemBySite = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return listDataItemBySite;
    }

    private List<DataDto> getDataDtoListFromDataItemList(
            List<DataItem> sortedDataItemList) {
        return sortedDataItemList
                .stream()
                .map(this::getDataDtoFromDataItem)
                .collect(Collectors.toList());
    }

    private DataDto getDataDtoFromDataItem(DataItem dataItem) {
        PageEntity pageEntity = pageRepository
                .getReferenceById(dataItem.getPageId());
        SiteEntity siteEntity = siteRepository
                .getReferenceById(pageEntity.getSiteId());

        return DataDto.builder()
                .site(siteEntity.getUrl())
                .siteName(siteEntity.getName())
                .uri(pageEntity.getPath())
                .title(getTitlePage(pageEntity.getContent()))
                .snippet(dataItem.getSnippet())
                .relevance(dataItem.getRelevance())
                .build();
    }

    private List<DataDto> getDataDtoPage(List<DataItem> sortedDataItemList, Integer offset, Integer limit) {
        return getDataDtoListFromDataItemList(getSortedItemListFromOffsetToLimit(sortedDataItemList, offset, limit));
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

    private List<DataItem> getSortedItemListFromOffsetToLimit(
            List<DataItem> sortedDataItemList, Integer offset, Integer limit) {
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

    public SearchDto setSearchDtoIfDBIsEmpty() {
        return SearchDto.builder()
                .result(false)
                .error(SearchMessageType.ENTITY_DB_MESSAGE.getDescription())
                .build();
    }

    public SearchDto setSearchDtoIfQueryIsEmpty() {
        return SearchDto.builder()
                .result(false)
                .error(SearchMessageType.ENTITY_QUERY_MESSAGE.getDescription())
                .build();
    }

    public SearchDto setSearchDtoIfSiteIsIndexing() {
        return SearchDto.builder()
                .result(false)
                .error(SearchMessageType.INDEX_ERROR_MESSAGE.getDescription())
                .build();
    }

    public SearchDto setSearchDtoIfSiteIsFailed() {
        return SearchDto.builder()
                .result(false)
                .error(SearchMessageType.FAILED_ERROR_MESSAGE.getDescription())
                .build();
    }

    public SearchDto setSearchDtoIfSiteEntityIsNull() {
        return SearchDto.builder()
                .result(false)
                .error(SearchMessageType.NULL_ERROR_MESSAGE.getDescription())
                .build();
    }
}
