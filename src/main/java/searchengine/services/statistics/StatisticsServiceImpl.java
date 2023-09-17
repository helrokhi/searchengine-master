package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.utils.methods.Methods;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final Methods methods;

    @Override
    public StatisticsResponse getStatistics() {
        System.out.println("1. StatisticsServiceImpl getStatistics" +
                " siteService.getCountSites() - " + methods.getCountSites() +
                "");
        StatisticsResponse response = new StatisticsResponse();
        List<SiteEntity> siteEntityList = methods.findAllSiteEntities();

        if (siteEntityList.isEmpty()) {
            response.setResult(false);
            response.setError("База данных пустая. Проведите индексацию сайтов.");
        } else {
            TotalStatistics total = new TotalStatistics();
            total.setSites(methods.getCountSites());
            total.setIndexing(true);

            List<DetailedStatisticsItem> detailed = new ArrayList<>();

            for (SiteEntity siteEntity : siteEntityList) {
                DetailedStatisticsItem item = new DetailedStatisticsItem();
                item.setName(siteEntity.getName());
                item.setUrl(siteEntity.getUrl());
                item.setPages(methods.getCountAllPagesBySiteEntity(siteEntity));
                item.setLemmas(methods.getCountLemmasBySiteEntity(siteEntity));
                item.setStatus(siteEntity.getStatus().toString());
                item.setError(siteEntity.getLastError());
                item.setStatusTime(siteEntity.getStatusTime());

                total.setPages(methods.getCountPages());
                total.setLemmas(methods.getCountLemmas());
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
