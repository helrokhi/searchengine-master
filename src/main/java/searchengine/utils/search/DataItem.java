package searchengine.utils.search;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.model.LemmaEntity;

import java.util.List;

@Component
@Data
@NoArgsConstructor
public class DataItem {
    private int pageId;
    private List<LemmaEntity> lemmaEntities;
    private float countRank;
    private float relevance;
    private String snippet;

    //сравнить по релевантности
    public static int compareByRelevance(DataItem dataItem1, DataItem dataItem2) {
        return Float.compare(dataItem2.getRelevance(), dataItem1.getRelevance());
    }
}
