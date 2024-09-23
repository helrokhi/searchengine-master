package searchengine.utils.search;

import searchengine.dto.search.ScanningCall;
import searchengine.model.SiteEntity;

public interface ScanningService {
    ScanningCall startScanning(String query, SiteEntity siteEntity);
}
