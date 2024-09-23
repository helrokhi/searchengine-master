package searchengine.dto.sites;

import lombok.Data;
import org.jsoup.nodes.Document;

@Data
public class PageDto {
    private Document document;
    private Exception exception;
    private int code;
}
