package searchengine.services.refresh;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.refresh.Refreshing;
import searchengine.config.sites.SitesList;
import searchengine.dto.indexing.IndexResponse;

@Service
@RequiredArgsConstructor
public class RefreshingService {
    private final SitesList sites;
    private final Refreshing refreshing;

    public IndexResponse getIndexPageResponse(String url) {
        System.out.println("1. RefreshingService getIndexPageResponse" +
                " url " + url +
                " isLinkInSites " + (refreshing.isLinkInSites(url, sites)) +
                "");
        IndexResponse response = new IndexResponse();

        if (!refreshing.isLinkInSites(url, sites)) {
            response.setResult(false);
            response.setError("Данная страница находится за пределами сайтов, " +
                    "\nуказанных в конфигурационном файле");
            return response;
        } else {
            refreshing.refreshPageEntity(url, sites);
            response.setResult(true);
        }
        System.out.println("2. RefreshingService getIndexPageResponse " + response);
        return response;
    }
}
