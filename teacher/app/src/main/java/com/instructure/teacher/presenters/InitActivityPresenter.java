/*
 * Copyright (C) 2017 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.teacher.presenters;

import android.support.annotation.NonNull;

import com.instructure.teacher.viewinterface.InitActivityView;

import instructure.androidblueprint.Presenter;

public class InitActivityPresenter implements Presenter<InitActivityView> {

    //Before the view is ready to be used we have a few pieces of data we need to get
    //The data can be from cache or network

    private InitActivityView mView;

    public InitActivityPresenter() {}

    @Override
    public InitActivityPresenter onViewAttached(@NonNull InitActivityView view) {
        mView = view;
        return this;
    }

    public void loadData(boolean forceNetwork) {

    }


    @Override
    public void onViewDetached() {
        mView = null;
    }

    @Override
    public void onDestroyed() {}
}
