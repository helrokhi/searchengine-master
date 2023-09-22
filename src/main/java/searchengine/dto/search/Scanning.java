package searchengine.dto.search;

import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.utils.gradations.CollectLemmas;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.utils.search.DataItem;
import searchengine.utils.search.Fragment;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Scanning implements Callable<List<DataItem>> {
    private final String query;
    private final SiteEntity siteEntity;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final CollectLemmas collectLemmas;
    private final Fragment fragment;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public Scanning(
            String query,
            SiteEntity siteEntity,
            PageRepository pageRepository,
            LemmaRepository lemmaRepository,
            IndexRepository indexRepository,
            CollectLemmas collectLemmas,
            Fragment fragment
    ) {
        this.query = query;
        this.siteEntity = siteEntity;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.collectLemmas = collectLemmas;
        this.fragment = fragment;
    }

    @Override
    public List<DataItem> call() {
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
        int countPage = pageRepository
                .countAllPagesBySiteId(siteEntity.getId());
        return list.stream()
                .filter(lemmaEntity -> (isVariation(lemmaEntity.getFrequency(), countPage)))
                .sorted(Comparator.comparing(LemmaEntity::getFrequency))
                .collect(Collectors.toList());
    }

    private List<LemmaEntity> getListOfUniqueLemmasBySite(
            Set<String> words,
            SiteEntity siteEntity) {
        return words
                .stream()
                .map(word -> lemmaRepository.findByLemma(word, siteEntity.getId()))
                .filter(lemmaEntity -> !Objects.equals(lemmaEntity, null))
                .collect(Collectors.toList());
    }

    private List<Integer> getListPageIdBySortedListOfLemmas(
            List<LemmaEntity> list,
            SiteEntity siteEntity) {
        List<Integer> integers = (!list.isEmpty()) ?
                (List<Integer>) pageRepository.findAllPagesIdBySiteId(siteEntity.getId()) :
                new ArrayList<>(0);

        getLemmaIdListByLemmaEntityList(list)
                .forEach(lemmaId ->
                        integers.retainAll(
                                indexRepository.getAllPageIdByLemmaId(lemmaId, integers)
                        ));
        return integers;
    }

    private List<DataItem> getListItem(List<Integer> listPageId,
                                       List<LemmaEntity> lemmaEntities) {
        List<DataItem> listDataItem = new ArrayList<>(0);
        List<DataItem> dataItemListFromDataBase = getItemListFromDataBase(listPageId, lemmaEntities);

        float maxRelevance = dataItemListFromDataBase
                .stream()
                .max(Comparator.comparing(DataItem::getCountRank))
                .map(DataItem::getCountRank).orElse(0F);

        for (DataItem dataItem : dataItemListFromDataBase) {
            dataItem.setRelevance(dataItem.getCountRank() / maxRelevance);
            FutureTask<String> futureTask = new FutureTask<>(fragment.startSnippet(dataItem));
            executorService.execute(futureTask);

            try {
                dataItem.setSnippet(futureTask.get());
                if (!dataItem.getSnippet().isEmpty()) listDataItem.add(dataItem);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return listDataItem;
    }

    private List<DataItem> getItemListFromDataBase(List<Integer> listPageId,
                                                   List<LemmaEntity> lemmaEntities) {
        List<Integer> lemmaIdList =
                getLemmaIdListByLemmaEntityList(lemmaEntities);

        List<DataItem> dataItemList = new ArrayList<>(0);

        List<Object[]> objects =
                indexRepository.getListFromPageIdAndCountRank(listPageId, lemmaIdList);

        for (Object[] o : objects) {
            DataItem dataItem = new DataItem();
            dataItem.setPageId((int) o[0]);
            dataItem.setLemmaEntities(lemmaEntities);
            dataItem.setCountRank((float) ((double) o[1]));
            dataItemList.add(dataItem);
        }
        return dataItemList;
    }

    private List<Integer> getLemmaIdListByLemmaEntityList(List<LemmaEntity> list) {
        return list.stream()
                .map(LemmaEntity::getId)
                .collect(Collectors.toList());
    }
}
