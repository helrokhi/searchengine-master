package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "page") //проиндексированные страницы сайта
@NoArgsConstructor
@Getter
@Setter
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "site_id", nullable = false)
    private int siteId; //ID веб-сайта из таблицы site;

    @Column(columnDefinition = "TEXT", nullable = false, unique = true)
    private String path; //адрес страницы от корня сайта

    @Column(nullable = false)
    private int code; //код HTTP-ответа, полученный при запросе страницы (например, 200, 404, 500 или другие);

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content; //контент страницы (HTML-код).
}
