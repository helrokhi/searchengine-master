package searchengine.config.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import searchengine.model.LemmaEntity;

import java.util.List;

@Component
@Getter
@Setter
@NoArgsConstructor
public class Item {
    private int pageId;
    private List<LemmaEntity> lemmaEntities;
    private float countRank;
    private float relevance;
    private String snippet;
}
