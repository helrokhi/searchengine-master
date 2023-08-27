package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "`index`") //поисковый индекс
@NoArgsConstructor
@Getter
@Setter
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "page_id", nullable = false)
    private int pageId; //идентификатор страницы;

    @Column(name = "lemma_id", nullable = false)
    private int lemmaId; //идентификатор леммы;

    @Column(name = "`rank`", nullable = false)
    private float rank; //количество данной леммы для данной страницы.
}
