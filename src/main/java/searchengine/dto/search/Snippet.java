package searchengine.dto.search;

import org.jsoup.Jsoup;
import searchengine.repositories.PageRepository;
import searchengine.utils.gradations.CollectLemmas;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.utils.search.DataItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class Snippet implements Callable<String> {
    private final DataItem dataItem;
    private final PageRepository pageRepository;
    private final CollectLemmas collectLemmas;
    private final StringBuilder stringBuilder = new StringBuilder(0);
    private final static int MAX_STEP = 11;
    private final static int LIMIT_WORDS = 101;
    private final static String WORD_REGEX = "[^\\p{L}\\p{N}+]";

    public Snippet(
            DataItem dataItem,
            PageRepository pageRepository, CollectLemmas collectLemmas
    ) {
        this.dataItem = dataItem;
        this.pageRepository = pageRepository;
        this.collectLemmas = collectLemmas;
    }

    @Override
    public String call() {
        PageEntity pageEntity = pageRepository
                .findById(dataItem.getPageId()).orElse(null);
        if (pageEntity != null) {
            String content = pageEntity.getContent();
            String[] words = getBodyPage(content)
                    .trim()
                    .split("\\s+");

            List<Integer> integerList = new ArrayList<>(0);
            for (int i = 0; i < words.length; i++) {
                String word = words[i]
                        .toLowerCase(Locale.ROOT)
                        .replaceAll(WORD_REGEX, "");
                List<String> listWordLemma = collectLemmas.getAllWordForms(word);
                List<String> listLemma = getListLemmas();
                if (isWordInListOfLemmas(listLemma, listWordLemma)) {
                    integerList.add(i);
                }
            }
            getSnippetPart(words, integerList);
        }
        return stringBuilder.toString();
    }

    private void getSnippetPart(String[] words, List<Integer> integerList) {
        int step = (integerList.size() != 0) ? (LIMIT_WORDS / integerList.size()) : LIMIT_WORDS;
        step = (step % 2 == 0) ? step + 1 : step;
        step = Math.min(step, MAX_STEP);
        int halfStep = step / 2;

        if (integerList.size() == 1) {
            int i = integerList.get(0);
            getTextSnippet(words, i, halfStep);
        } else {
            int before = 0;
            while (integerList.iterator().hasNext()) {
                int first = integerList.get(0);

                if (before != 0) {
                    if (first - before < step) {
                        getAppendStringBuilder(words, before + 1, first - 1);
                    } else {
                        getAppendStringBuilder(words, before + 1, before + halfStep);
                        stringBuilder.append(stringBuilder.length() == 0 ? "" : " ")
                                .append("...");
                        getAppendStringBuilder(words, first - halfStep, first - 1);
                    }
                }

                stringBuilder.append(stringBuilder.length() == 0 ? "" : " ")
                        .append(getUpdatedWord(words[first]));

                before = first;
                integerList.remove(0);
            }
        }
    }

    private void getAppendStringBuilder(String[] words, int from, int to) {
        for (int i = from; i < to; i++) {
            stringBuilder.append(stringBuilder.length() == 0 ? "" : " ")
                    .append(words[i]);
        }
    }

    private void getTextSnippet(String[] words, int i, int halfStep) {
        getAppendStringBuilder(words, i - 1 - halfStep, i - 1);
        stringBuilder.append(stringBuilder.length() == 0 ? "" : " ")
                .append(getUpdatedWord(words[i]));
        getAppendStringBuilder(words, i + 1, i + 1 + halfStep);
    }

    private String getUpdatedWord(String word) {
        return "<b>" + word + "</b>";
    }

    private List<String> getListLemmas() {
        return dataItem.getLemmaEntities()
                .stream()
                .map(LemmaEntity::getLemma)
                .collect(Collectors.toList());
    }

    private boolean isWordInListOfLemmas(List<String> listLemma, List<String> listWordLemma) {
        List<String> wordInListOfLemmas = new ArrayList<>(listLemma);
        wordInListOfLemmas.retainAll(listWordLemma);
        return !wordInListOfLemmas.isEmpty();
    }

    private String getBodyPage(String content) {
        return Jsoup.parse(content).getElementsByTag("body").text();
    }
}
