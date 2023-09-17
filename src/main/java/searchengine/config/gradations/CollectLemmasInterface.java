package searchengine.config.gradations;

import org.apache.lucene.morphology.LuceneMorphology;

import java.util.*;

public interface CollectLemmasInterface {
    String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};

    default Map<String, Integer> collectLemmas(
            String text,
            String regex,
            LuceneMorphology luceneMorphology
    ) {
        String[] words = arrayContainsLanguageWords(text, regex);
        HashMap<String, Integer> lemmas = new HashMap<>();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }
            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }
            String normalWord = normalForms.get(0);
            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
        return lemmas;
    }

    default Set<String> getLemmaSet(
            String text,
            String WORD_TYPE_REGEX,
            String regex,
            LuceneMorphology luceneMorphology
    ) {
        String[] textArray = arrayContainsLanguageWords(text, regex);
        Set<String> lemmaSet = new HashSet<>();
        for (String word : textArray) {
            if (!word.isEmpty() && isCorrectWordForm(word, WORD_TYPE_REGEX, luceneMorphology)) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForms)) {
                    continue;
                }
                lemmaSet.addAll(luceneMorphology.getNormalForms(word));
            }
        }
        return lemmaSet;
    }

    default List<String> getAllWordForms(String word, LuceneMorphology luceneMorphology) {
        return (!word.isEmpty()) ? luceneMorphology.getNormalForms(word) : new ArrayList<>(0);
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream()
                .anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private String[] arrayContainsLanguageWords(String text, String regex) {
        return text
                .toLowerCase(Locale.ROOT)
                .replaceAll(regex, " ")
                .trim()
                .split("\\s+");
    }

    private boolean isCorrectWordForm(
            String word,
            String wordTypeRegex,
            LuceneMorphology luceneMorphology) {
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);
        for (String morphInfo : wordInfo) {
            if (morphInfo.matches(wordTypeRegex)) {
                return false;
            }
        }
        return true;
    }
}
