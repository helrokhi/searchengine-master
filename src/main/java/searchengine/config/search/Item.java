package searchengine.config.search;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.model.LemmaEntity;

import java.util.List;

@Component
@Data
@NoArgsConstructor
public class Item {
    private int pageId;
    private List<LemmaEntity> lemmaEntities;
    private float countRank;
    private float relevance;
    private String snippet;

    //сравнить по релевантности
    public static int compareByRelevance(Item item1, Item item2) {
        return Float.compare(item2.getRelevance(), item1.getRelevance());
    }
}
