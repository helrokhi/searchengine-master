package searchengine.utils.sitemaps;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.dto.sites.PageDto;

import java.io.IOException;

import static java.lang.Thread.sleep;

@Component
public class JsoupConnect {
    public PageDto setPageDto(String link) {
        Document document;
        PageDto pageDto = new PageDto();
        try {
            sleep(150);
            document = Jsoup.connect(link).get();
        } catch (HttpStatusException statusException) {
            pageDto.setException(statusException);
            pageDto.setCode(statusException.getStatusCode());
            return pageDto;
        } catch (IOException | InterruptedException exception) {
            pageDto.setException(exception);
            pageDto.setCode(404);
            return pageDto;
        }
        pageDto.setDocument(document);
        pageDto.setCode(document.connection().response().statusCode());

        return pageDto;
    }
}
