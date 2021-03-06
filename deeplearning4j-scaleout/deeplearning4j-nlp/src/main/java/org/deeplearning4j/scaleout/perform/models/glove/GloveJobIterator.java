package org.deeplearning4j.scaleout.perform.models.glove;

import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.glove.CoOccurrences;
import org.deeplearning4j.models.glove.GloveWeightLookupTable;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.scaleout.api.statetracker.NewUpdateListener;
import org.deeplearning4j.scaleout.api.statetracker.StateTracker;
import org.deeplearning4j.scaleout.job.Job;
import org.deeplearning4j.scaleout.job.JobIterator;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Glove job iterator
 *
 *
 * @author Adam Gibson
 */
public class GloveJobIterator implements JobIterator {

    private Iterator<List<Pair<VocabWord,VocabWord>>> sentenceIterator;
    private GloveWeightLookupTable table;
    private VocabCache cache;
    private int batchSize = 100;
    public final static String CO_OCCURRENCES = "cooccurrences";



    public GloveJobIterator(CoOccurrences coc, GloveWeightLookupTable table, VocabCache cache, StateTracker stateTracker, int batchSize) {
        this.sentenceIterator = coc.coOccurrenceIteratorVocabBatch(batchSize);
        this.table = table;
        this.cache = cache;
        addListener(stateTracker);
        this.batchSize = batchSize;
        stateTracker.define(CO_OCCURRENCES,coc);

    }








    private void addListener(StateTracker stateTracker) {
        stateTracker.addUpdateListener(new NewUpdateListener() {
            @Override
            public void onUpdate(Serializable update) {
                Job j = (Job) update;
                Collection<GloveResult> work = (Collection<GloveResult>) j.getResult();
                if(work == null || work.isEmpty())
                    return;

                GloveWeightLookupTable l = table;

                for(GloveResult work1 : work) {
                    for(String s : work1.getSyn0Change().keySet()) {
                        l.getSyn0().putRow(cache.indexOf(s),work1.getSyn0Change().get(s));



                    }
                }



            }
        });
    }


    private GloveWork create(List<Pair<VocabWord,VocabWord>> sentence) {
        if(cache == null)
            throw new IllegalStateException("Unable to create work; no vocab found");
        if(table == null)
            throw new IllegalStateException("Unable to create work; no table found");
        if(sentence == null)
            throw new IllegalArgumentException("Unable to create work from null sentence");
        GloveWork work = new GloveWork(table,sentence);
        return work;
    }

    @Override
    public Job next(String workerId) {

        List<Pair<VocabWord,VocabWord>> next = sentenceIterator.next();
        return new Job(create(next),workerId);
    }

    @Override
    public Job next() {
        List<Pair<VocabWord,VocabWord>> next = sentenceIterator.next();
        return new Job(create(next),"");
    }

    @Override
    public boolean hasNext() {
        return sentenceIterator.hasNext();
    }

    @Override
    public void reset() {

    }
}
