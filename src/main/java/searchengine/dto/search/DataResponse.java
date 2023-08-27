package searchengine.dto.search;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class DataResponse {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet; //Фрагмент тека, в котором найдены совпадения, выделенные жирным.
    private float relevance;

    //сравнить по релевантности
    public static int compareByRelevance(DataResponse d1, DataResponse d2) {
        return Float.compare(d2.getRelevance(), d1.getRelevance());
    }
}
