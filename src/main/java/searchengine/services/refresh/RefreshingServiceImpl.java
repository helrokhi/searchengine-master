package searchengine.services.refresh;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.enums.MessageType;
import searchengine.dto.indexing.IndexDto;
import searchengine.model.SiteEntity;
import searchengine.repositories.SiteRepository;
import searchengine.utils.refresh.RefreshService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshingServiceImpl implements RefreshingService {
    private final RefreshService refreshService;
    private final SiteRepository siteRepository;

    @Override
    public IndexDto getIndexDtoAndStartRefreshing(String url) {
        List<SiteEntity> siteEntityList = siteRepository.findAll();
        log.info("1. Start refreshing url {} isLinkInSites {}",
                url, refreshService.isLinkInSites(url, siteEntityList));
        IndexDto indexDto = new IndexDto();

        if (!refreshService.isLinkInSites(url, siteEntityList)) {
            indexDto.setResult(false);
            indexDto.setError(MessageType.REFRESH_ERROR_MESSAGE.getDescription());
        } else {
            refreshService.refreshPageEntity(url, siteEntityList);
            indexDto.setResult(true);
        }
        log.info("2. End refreshing getIndexDto {}", indexDto);
        return indexDto;
    }
}
