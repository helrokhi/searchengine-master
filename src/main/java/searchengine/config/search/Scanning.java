package searchengine.config.search;

import searchengine.config.gradations.CollectLemmas;
import searchengine.config.sites.Site;
import searchengine.model.LemmaEntity;
import searchengine.services.gradations.IndexService;
import searchengine.services.gradations.LemmaService;
import searchengine.services.sitemaps.PageService;
import searchengine.services.sitemaps.SiteService;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Scanning implements Callable<List<Item>> {
    private final String query;
    private final Site site;
    private final SiteService siteService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final CollectLemmas collectLemmas;
    private Fragment fragment;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public Scanning(
            String query, Site site,
            SiteService siteService,
            PageService pageService,
            LemmaService lemmaService,
            IndexService indexService,
            CollectLemmas collectLemmas,
            Fragment fragment
    ) {
        this.query = query;
        this.site = site;
        this.siteService = siteService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.collectLemmas = collectLemmas;
        this.fragment = fragment;
    }

    @Override
    public List<Item> call(){
        Set<String> lemmaSet = getLemmaSet(query);
        List<LemmaEntity> listOfUniqueLemmasBySite = getListOfUniqueLemmasBySite(lemmaSet, site);
        List<LemmaEntity> sortedListOfLemmas = getSortedListOfLemmas(listOfUniqueLemmasBySite, site);
        List<Integer> listPageIdBySite = getListPageIdBySortedListOfLemmas(sortedListOfLemmas, site);

        return getListItem(listPageIdBySite, sortedListOfLemmas);
    }

    private Set<String> getLemmaSet(String text) {
        return collectLemmas.getLemmaSet(text);
    }

    private double getCoefficientOfVariation(int frequency, int countPage) {
        double average = (countPage + frequency) / 2;
        double meanSquareDeviation =
                Math.sqrt(
                        Math.pow((countPage - average), 2) + Math.pow((frequency - average), 2));
        return (meanSquareDeviation / average) * 100;
    }

    private boolean isVariation(int frequency, int countPage) {
        return getCoefficientOfVariation(frequency, countPage) > 33;
    }

    private List<Integer> getLemmaIdList(List<LemmaEntity> list) {
        return list.stream()
                .map(LemmaEntity::getId)
                .collect(Collectors.toList());
    }

    private List<LemmaEntity> getSortedListOfLemmas(List<LemmaEntity> list, Site site) {
        int countPage = pageService.getCountAllPagesBySite(site);
        return list.stream()
                .filter(lemmaEntity -> (isVariation(lemmaEntity.getFrequency(), countPage)))
                .sorted(Comparator.comparing(LemmaEntity::getFrequency))
                .collect(Collectors.toList());
    }

    private List<LemmaEntity> getListOfUniqueLemmasBySite(Set<String> words, Site site) {
        return words
                .stream()
                .map(word -> lemmaService.findLemmaByLemmaAndSiteId(word, site))
                .filter(lemmaEntity -> !Objects.equals(lemmaEntity, null))
                .collect(Collectors.toList());
    }

    private List<Integer> getListPageIdBySortedListOfLemmas(List<LemmaEntity> list, Site site) {
        List<Integer> integers = (!list.isEmpty()) ? pageService.getListPageIdBySite(site) : new ArrayList<>(0);
        getLemmaIdList(list)
                .forEach(lemmaId ->
                        integers.retainAll(indexService.getAllPageIdByLemmaId(lemmaId, integers)));
        return integers;
    }

    private List<Item> getListItem(List<Integer> listPageId,
                                   List<LemmaEntity> lemmaEntities) {
        List<Item> itemList = new ArrayList<>(0);
        if (!listPageId.isEmpty()) {
            listPageId.forEach(pageId -> {
                Item item = new Item();
                item.setPageId(pageId);
                item.setLemmaEntities(lemmaEntities);
                item.setCountRank(indexService.countRank(pageId, getLemmaIdList(lemmaEntities)));
                itemList.add(item);
            });

            float maxRelevance = itemList
                    .stream()
                    .map(Item::getCountRank)
                    .max(Float::compareTo)
                    .get();

            itemList.forEach(item -> {
                item.setRelevance(item.getCountRank() / maxRelevance);
                Future<String> future = executorService.submit(fragment.startSnippet(item));

                try {
                    item.setSnippet(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return itemList;
    }
}
