package searchengine.dto.sites;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;

@Getter
@Setter
public class PageResponse {
    private Document document;
    private Exception exception;
    private int code;
}
