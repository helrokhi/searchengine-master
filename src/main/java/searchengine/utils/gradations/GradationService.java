package searchengine.utils.gradations;

import searchengine.dto.gradations.GradationThread;
import searchengine.utils.sitemaps.SitePage;

public interface GradationService {
    GradationThread startGradation(SitePage page);
}
