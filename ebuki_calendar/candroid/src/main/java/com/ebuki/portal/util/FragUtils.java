/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
 *
 */

package com.ebuki.portal.util;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.ebuki.portal.fragment.CourseGridFragment;
import com.ebuki.portal.fragment.FileListFragment;
import com.ebuki.portal.fragment.InternalWebviewFragment;
import com.ebuki.portal.fragment.MessageListFragment;
import com.ebuki.portal.fragment.NotificationListFragment;
import com.ebuki.portal.fragment.ParentFragment;
import com.ebuki.portal.fragment.ProfileFragment;
import com.ebuki.portal.fragment.ToDoListFragment;
import com.instructure.canvasapi2.models.CanvasContext;

public class FragUtils {
    public static <Type extends ParentFragment> Type getFrag(Class<Type> cls, FragmentActivity activity, CanvasContext canvasContext) {

        ParentFragment fragment = null;

        Bundle bundle = null;

        if(cls.isAssignableFrom(ProfileFragment.class)) {
            bundle = ProfileFragment.createBundle(canvasContext, ParentFragment.FRAGMENT_PLACEMENT.MASTER);
        } else if(cls.isAssignableFrom(CourseGridFragment.class)) {
            bundle = CourseGridFragment.createBundle(canvasContext);
        }  else if(cls.isAssignableFrom(NotificationListFragment.class)) {
            bundle = NotificationListFragment.createBundle(canvasContext);
        } else if(cls.isAssignableFrom(ToDoListFragment.class)) {
            bundle = ToDoListFragment.createBundle(canvasContext);
        } else if(cls.isAssignableFrom(MessageListFragment.class)) {
            bundle =  MessageListFragment.createBundle(canvasContext);
        } else if (cls.isAssignableFrom(FileListFragment.class)){
            bundle = FileListFragment.createBundle(canvasContext);
        } else if (cls.isAssignableFrom(InternalWebviewFragment.class)){
            bundle = InternalWebviewFragment.createDefaultBundle(canvasContext);
        }

        if(fragment == null) {
            return ParentFragment.createFragment(cls, bundle);
        } else {
            fragment.handleIntentExtras(bundle);
            return (Type)fragment;
        }
    }

    public static <Type extends ParentFragment> Type getFrag(Class<Type> cls, Bundle bundle){
        return ParentFragment.createFragment(cls, bundle);
    }

    public static <Type extends ParentFragment> Type getFrag(Class<Type> cls, FragmentActivity activity) {
        return getFrag(cls, activity, CanvasContext.emptyUserContext());
    }
}
