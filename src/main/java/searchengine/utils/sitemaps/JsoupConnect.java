package searchengine.utils.sitemaps;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.dto.sites.PageResponse;

import java.io.IOException;

import static java.lang.Thread.sleep;

@Component
public class JsoupConnect {
    public PageResponse getPageResponse(String link) {
        Document document;
        PageResponse pageResponse = new PageResponse();
        try {
            sleep(150);
            document = Jsoup.connect(link).get();
        } catch (HttpStatusException statusException) {
            pageResponse.setException(statusException);
            pageResponse.setCode(statusException.getStatusCode());
            return pageResponse;
        } catch (IOException | InterruptedException exception) {
            pageResponse.setException(exception);
            pageResponse.setCode(404);
            return pageResponse;
        }
        pageResponse.setDocument(document);
        pageResponse.setCode(document.connection().response().statusCode());

        return pageResponse;
    }
}
