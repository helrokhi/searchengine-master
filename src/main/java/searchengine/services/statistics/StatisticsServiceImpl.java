package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.services.gradations.LemmaService;
import searchengine.services.sitemaps.PageService;
import searchengine.services.sitemaps.SiteService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SiteService siteService;
    private final PageService pageService;
    private final LemmaService lemmaService;

    @Override
    public StatisticsResponse getStatistics() {
        System.out.println("1. StatisticsServiceImpl getStatistics" +
                " siteService.getCountSites() - " + siteService.getCountSites() +
                "");
        StatisticsResponse response = new StatisticsResponse();
        List<SiteEntity> siteEntityList = siteService.findAll();

        if (siteEntityList.isEmpty()) {
            response.setResult(false);
            response.setError("База данных пустая. Проведите индексацию сайтов.");
        } else {
            TotalStatistics total = new TotalStatistics();
            total.setSites(siteService.getCountSites());
            total.setIndexing(true);

            List<DetailedStatisticsItem> detailed = new ArrayList<>();

            for (SiteEntity siteEntity : siteEntityList) {
                DetailedStatisticsItem item = new DetailedStatisticsItem();
                item.setName(siteEntity.getName());
                item.setUrl(siteEntity.getUrl());
                item.setPages(pageService.getCountAllPagesBySiteEntity(siteEntity));
                item.setLemmas(lemmaService.getCountLemmasBySiteEntity(siteEntity));
                item.setStatus(siteEntity.getStatus().toString());
                item.setError(siteEntity.getLastError());
                item.setStatusTime(siteEntity.getStatusTime());

                total.setPages(pageService.getCountPages());
                total.setLemmas(lemmaService.getCountLemmas());
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
