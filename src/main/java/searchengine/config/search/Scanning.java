package searchengine.config.search;

import searchengine.config.gradations.CollectLemmas;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.utils.gradations.IndexService;
import searchengine.utils.gradations.LemmaService;
import searchengine.utils.sitemaps.PageService;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Scanning implements Callable<List<Item>> {
    private final String query;
    private final SiteEntity siteEntity;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final CollectLemmas collectLemmas;
    private Fragment fragment;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public Scanning(
            String query,
            SiteEntity siteEntity,
            PageService pageService,
            LemmaService lemmaService,
            IndexService indexService,
            CollectLemmas collectLemmas,
            Fragment fragment
    ) {
        this.query = query;
        this.siteEntity = siteEntity;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.collectLemmas = collectLemmas;
        this.fragment = fragment;
    }

    @Override
    public List<Item> call() {
        Set<String> lemmaSet = getLemmaSet(query);
        List<LemmaEntity> listOfUniqueLemmasBySite =
                getListOfUniqueLemmasBySite(lemmaSet, siteEntity);
        List<LemmaEntity> sortedListOfLemmas =
                getSortedListOfLemmas(listOfUniqueLemmasBySite, siteEntity);
        List<Integer> listPageIdBySite =
                getListPageIdBySortedListOfLemmas(sortedListOfLemmas, siteEntity);
        return !listPageIdBySite.isEmpty() ?
                getListItem(listPageIdBySite, sortedListOfLemmas) :
                new ArrayList<>(0);
    }

    private Set<String> getLemmaSet(String text) {
        return collectLemmas.getLemmaSet(text);
    }

    private double getCoefficientOfVariation(int frequency, int countPage) {
        double average = (double) (countPage + frequency) / 2;
        double meanSquareDeviation =
                Math.sqrt(
                        Math.pow((countPage - average), 2) + Math.pow((frequency - average), 2));
        return (meanSquareDeviation / average) * 100;
    }

    private boolean isVariation(int frequency, int countPage) {
        return getCoefficientOfVariation(frequency, countPage) > 33;
    }

    private List<LemmaEntity> getSortedListOfLemmas(
            List<LemmaEntity> list,
            SiteEntity siteEntity) {
        int countPage = pageService.getCountAllPagesBySiteEntity(siteEntity);
        return list.stream()
                .filter(lemmaEntity -> (isVariation(lemmaEntity.getFrequency(), countPage)))
                .sorted(Comparator.comparing(LemmaEntity::getFrequency))
                .collect(Collectors.toList());
    }

    private List<LemmaEntity> getListOfUniqueLemmasBySite(
            Set<String> words,
            SiteEntity siteEntity
    ) {
        return words
                .stream()
                .map(word -> lemmaService.findLemmaByLemmaAndSiteId(word, siteEntity))
                .filter(lemmaEntity -> !Objects.equals(lemmaEntity, null))
                .collect(Collectors.toList());
    }

    private List<Integer> getListPageIdBySortedListOfLemmas(
            List<LemmaEntity> list,
            SiteEntity siteEntity) {
        List<Integer> integers = (!list.isEmpty()) ?
                pageService.getListPageIdBySiteEntity(siteEntity) :
                new ArrayList<>(0);

        lemmaService.getLemmaIdListByLemmaEntityList(list)
                .forEach(lemmaId ->
                        integers.retainAll(indexService.getAllPageIdByLemmaId(lemmaId, integers)));
        return integers;
    }

    private List<Item> getListItem(List<Integer> listPageId,
                                   List<LemmaEntity> lemmaEntities) {
        List<Item> listItem = new ArrayList<>(0);
        List<Item> itemListFromDataBase = getItemListFromDataBase(listPageId, lemmaEntities);

        float maxRelevance = itemListFromDataBase
                .stream()
                .max(Comparator.comparing(Item::getCountRank))
                .map(Item::getCountRank).orElse(0F);

        for (Item item : itemListFromDataBase) {
            item.setRelevance(item.getCountRank() / maxRelevance);
            FutureTask<String> futureTask = new FutureTask<>(fragment.startSnippet(item));
            executorService.execute(futureTask);

            try {
                item.setSnippet(futureTask.get());
                if (!item.getSnippet().isEmpty()) listItem.add(item);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return listItem;
    }

    private List<Item> getItemListFromDataBase(List<Integer> listPageId,
                                               List<LemmaEntity> lemmaEntities) {
        List<Integer> lemmaIdList =
                lemmaService.getLemmaIdListByLemmaEntityList(lemmaEntities);

        List<Item> itemList = new ArrayList<>(0);

        List<Object[]> objects =
                indexService.getListFromPageIdAndCountRank(listPageId, lemmaIdList);

        for (Object[] o : objects) {
            Item item = new Item();
            item.setPageId((int) o[0]);
            item.setLemmaEntities(lemmaEntities);
            item.setCountRank((float) ((double) o[1]));
            itemList.add(item);
        }
        return itemList;
    }
}
