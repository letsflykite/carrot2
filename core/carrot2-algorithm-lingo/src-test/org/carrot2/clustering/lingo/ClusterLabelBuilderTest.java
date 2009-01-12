
/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2008, Dawid Weiss, Stanisław Osiński.
 * Portions (C) Contributors listed in "carrot2.CONTRIBUTORS" file.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */

package org.carrot2.clustering.lingo;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for label building in {@link ClusterBuilder}.
 */
public class ClusterLabelBuilderTest extends TermDocumentMatrixBuilderTestBase
{
    /** Matrix reducer needed for test */
    private TermDocumentMatrixReducer reducer;

    /** Label builder under tests */
    private ClusterBuilder clusterBuilder;

    @Before
    public void setUpClusterLabelBuilder()
    {
        clusterBuilder = new ClusterBuilder();
        reducer = new TermDocumentMatrixReducer();
    }

    @Test
    public void testEmpty()
    {
        check(new int [0]);
    }

    @Test
    public void testNoPhrases()
    {
        createDocuments("", "aa . aa", "", "bb . bb", "", "cc . cc");
        final int [] expectedFeatureIndex = new int []
        {
            0, 1, 2
        };

        reducer.desiredClusterCountBase = 30;
        check(expectedFeatureIndex);
    }

    @Test
    public void testSinglePhraseNoSingleWords()
    {
        createDocuments("aa bb", "aa bb", "aa bb", "aa bb");

        final int [] expectedFeatureIndex = new int []
        {
            2
        };

        reducer.desiredClusterCountBase = 10;
        check(expectedFeatureIndex);
    }

    @Test
    public void testSinglePhraseSingleWords()
    {
        createDocuments("aa bb", "aa bb", "cc", "cc", "aa bb", "aa bb");
        clusterBuilder.phraseLabelBoost = 0.5;

        final int [] expectedFeatureIndex = new int []
        {
            2, 3
        };

        reducer.desiredClusterCountBase = 15;
        check(expectedFeatureIndex);
    }

    @Test
    public void testQueryWordsRemoval()
    {
        createDocuments("query word . aa", "query word . aa", "query . word",
            "query . word . aa");
        clusterBuilder.phraseLabelBoost = 0.5;

        final int [] expectedFeatureIndex = new int []
        {
            0
        };

        reducer.desiredClusterCountBase = 10;
        createPreprocessingContext("query word");
        check(expectedFeatureIndex);
    }

    @Test
    public void testExternalFeatureScores()
    {
        createDocuments("aa bb", "aa bb", "cc", "cc", "cc", "cc", "aa bb", "aa bb", "dd",
            "dd", "dd", "dd", "ee ff", "ee ff", "ee ff", "ee ff");
        clusterBuilder.phraseLabelBoost = 0.5;
        reducer.desiredClusterCountBase = 15;

        final int [] expectedFeatureIndex = new int []
        {
            6, 7, 2, 3
        };
        check(expectedFeatureIndex);

        // Make a copy of feature indices
        final int [] featureIndex = lingoContext.preprocessingContext.allLabels.featureIndex;

        for (int i = 0; i < featureIndex.length; i++)
        {
            clusterBuilder.featureScorer = new OneLabelFeatureScorer(i, 2);
            check(new int []
            {
                featureIndex[i], featureIndex[i], featureIndex[i], featureIndex[i]
            });
        }
    }

    private static class OneLabelFeatureScorer implements IFeatureScorer
    {
        private int labelIndex;
        private double score;

        OneLabelFeatureScorer(int labelIndex, double score)
        {
            this.labelIndex = labelIndex;
            this.score = score;
        }

        public double [] getFeatureScores(LingoProcessingContext lingoContext)
        {
            final double [] scores = new double [lingoContext.preprocessingContext.allLabels.featureIndex.length];
            scores[labelIndex] = score;
            return scores;
        }
    }

    private void check(int [] expectedFeatureIndex)
    {
        buildTermDocumentMatrix();
        reducer.reduce(lingoContext);
        clusterBuilder.buildLabels(lingoContext, new TfTermWeighting());

        assertThat(lingoContext.clusterLabelFeatureIndex).as("clusterLabelFeatureIndex")
            .containsOnly(expectedFeatureIndex);
    }
}