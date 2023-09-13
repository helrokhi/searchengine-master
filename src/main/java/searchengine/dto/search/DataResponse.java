package searchengine.dto.search;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class DataResponse {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet; //Фрагмент тека, в котором найдены совпадения, выделенные жирным.
    private float relevance;
}
