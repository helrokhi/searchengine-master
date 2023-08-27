package searchengine.services.sitemaps;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.sites.PageResponse;

import java.io.IOException;

import static java.lang.Thread.sleep;

@Service
public class ConnectService {
    public PageResponse getPageResponse(String link) {
        Document document = null;
        PageResponse pageResponse = new PageResponse();
        try {
            sleep(150);
            document = Jsoup.connect(link).get();
        } catch (HttpStatusException statusException) {
            pageResponse.setException(statusException);
            pageResponse.setCode(statusException.getStatusCode());
            return pageResponse;
        } catch (IOException ioException) {
            pageResponse.setException(ioException);
            pageResponse.setCode(404);
            return pageResponse;
        } catch (InterruptedException interruptedException) {
            pageResponse.setException(interruptedException);
            pageResponse.setCode(404);
            return pageResponse;
        }
        pageResponse.setDocument(document);
        pageResponse.setCode(document.connection().response().statusCode());

        return pageResponse;
    }
}
