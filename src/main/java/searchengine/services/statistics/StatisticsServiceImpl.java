package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        System.out.println("1. StatisticsServiceImpl getStatistics" +
                " siteService.getCountSites() - " + siteRepository.countAllSites() +
                "");
        StatisticsResponse response = new StatisticsResponse();
        List<SiteEntity> siteEntityList = siteRepository.findAll();

        if (siteEntityList.isEmpty()) {
            response.setResult(false);
            response.setError("База данных пустая. Проведите индексацию сайтов.");
        } else {
            TotalStatistics total = new TotalStatistics();
            total.setSites(siteRepository.countAllSites());
            total.setIndexing(true);

            List<DetailedStatisticsItem> detailed = new ArrayList<>();

            for (SiteEntity siteEntity : siteEntityList) {
                DetailedStatisticsItem item = new DetailedStatisticsItem();
                item.setName(siteEntity.getName());
                item.setUrl(siteEntity.getUrl());
                item.setPages(pageRepository
                        .countAllPagesBySiteId(siteEntity.getId()));
                item.setLemmas(lemmaRepository.countAllLemmasBySiteId(siteEntity.getId()));
                item.setStatus(siteEntity.getStatus().toString());
                item.setError(siteEntity.getLastError());
                item.setStatusTime(siteEntity.getStatusTime());

                total.setPages(pageRepository.countAllPages());
                total.setLemmas(lemmaRepository.countAllLemmas());
                detailed.add(item);
            }
            StatisticsData data = new StatisticsData();
            data.setTotal(total);
            data.setDetailed(detailed);
            response.setStatistics(data);
            response.setResult(true);
        }
        System.out.println("2. StatisticsServiceImpl getStatistics" +
                " response " + response +
                "");
        return response;
    }
}
