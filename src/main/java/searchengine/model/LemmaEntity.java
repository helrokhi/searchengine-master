package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "lemma",
        indexes = @Index(name = "lemma_index", columnList = "site_id, lemma", unique = true))
//леммы, встречающиеся в текстах
@NoArgsConstructor
@Getter
@Setter

public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "site_id", nullable = false)
    private int siteId; //ID веб-сайта из таблицы site;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma; //нормальная форма слова (лемма);

    @Column(nullable = false)
    private int frequency; //количество страниц, на которых слово встречается хотя бы один раз.
}
