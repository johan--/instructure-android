/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.ebuki.homework.test.adapter;

import android.content.Context;
import android.test.InstrumentationTestCase;
import com.ebuki.homework.adapter.RubricRecyclerAdapter;
import com.instructure.canvasapi2.models.RubricCriterionRating;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricTestRunner.class)
public class RubricRecyclerAdapterTest extends InstrumentationTestCase {
    private RubricRecyclerAdapter mAdapter;

    /**
     * Make it so the protected constructor can be called
     */
    public static class RubricRecyclerAdapterWrapper extends RubricRecyclerAdapter {
        protected RubricRecyclerAdapterWrapper(Context context) {
            super(context);
        }
    }

    @Before
    public void setup(){
        mAdapter = new RubricRecyclerAdapterWrapper(RuntimeEnvironment.application.getApplicationContext());
    }

    @Test
    public void testAreContentsTheSame_SameNotComment(){
        RubricCriterionRating item = new RubricCriterionRating();
        item.setRatingDescription("item");

        assertTrue(mAdapter.createItemCallback().areContentsTheSame(item, item));
    }

    @Test
    public void testAreContentsTheSame_DifferentNotComment(){
        RubricCriterionRating item = new RubricCriterionRating();
        item.setRatingDescription("item");

        RubricCriterionRating item1 = new RubricCriterionRating();
        item1.setRatingDescription("item1");

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(item, item1));
    }

    @Test
    public void testAreContentsTheSame_SameComment(){
        RubricCriterionRating item = new RubricCriterionRating();
        item.setRatingDescription("item");
        item.setComments("hodor");

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(item, item));
    }
}
