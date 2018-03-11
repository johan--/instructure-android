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

package com.ebuki.homework.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;
import com.ebuki.homework.R;
import com.ebuki.homework.activity.NotificationWidgetRouter;
import com.ebuki.homework.util.StringUtilities;
import com.instructure.canvasapi2.managers.ConversationManager;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.GroupManager;
import com.instructure.canvasapi2.managers.StreamManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.models.StreamItem;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.DateHelper;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NotificationViewWidgetService extends BaseRemoteViewsService implements Serializable {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new NotificationsRowFactory(this.getApplicationContext(), intent);
    }

    public static Intent createIntent(Context mContext, int appWidgetId) {
        Intent intent = new Intent(mContext, NotificationViewWidgetService.class);
        intent.setAction(NotificationWidgetProvider.REFRESH);
        intent.setData(Uri.fromParts("appWidgetId", String.valueOf(appWidgetId), null));
        return intent;
    }

    private class NotificationsRowFactory extends CanvasWidgetRowFactory <StreamItem>{

        private Intent intent;

        private final int numberToReturn = 25;
        public NotificationsRowFactory(Context context, Intent intent){
            this.mContext = context;
            this.intent = intent;
        }

        @Override
        protected int giveMeAppWidgetId() {
            return getAppWidgetId(intent);
        }

        @Override
        protected List<StreamItem> makeApiCalls() {
            // get courses, data and to do items
            List<Course> courses = CourseManager.getCoursesSynchronous(true);
            List<Group> groups = GroupManager.getGroupsSynchronous(true);

            if(courses == null || groups == null){
                return null;
            }

            Map<Long, Course> courseMap = CourseManager.createCourseMap(courses);
            Map<Long, Group> groupMap = GroupManager.createGroupMap(groups);

            ArrayList<StreamItem> streamItemArrayList;
            List<StreamItem> streamItemList = StreamManager.getUserStreamSynchronous(numberToReturn, true);
            //If the API returns a null array we need to return a non-null array or we will have NP crash later.
            if(streamItemList == null){
                return new ArrayList<>();
            }
            streamItemArrayList = new ArrayList<>(streamItemList);

            Collections.sort(streamItemArrayList);
            Collections.reverse(streamItemArrayList);

            populateActivityStreamAdapter(courseMap, groupMap, streamItemArrayList);

            return streamItemArrayList;
        }

        @Override
        protected int getLayoutId(){
            if(intent != null) {
                int appWidgetId = Integer.valueOf(intent.getData().getSchemeSpecificPart());
                if (shouldHideDetails(getApplicationContext(), appWidgetId)) {
                    return R.layout.listview_widget_notifications_minimum_item_row;
                }
            }
            return R.layout.listview_widget_notifications_item_row;

        }

        @Override
        protected void setViewData(StreamItem streamItem, RemoteViews row){

            int appWidgetId = getAppWidgetId(intent);

            row.setViewVisibility(R.id.icon, View.VISIBLE);
            row.setImageViewResource(R.id.icon, getDrawableId(streamItem));

            row.setTextViewText(R.id.title, streamItem.getTitle(mContext));
            row.setTextColor(R.id.title, getWidgetTextColor(appWidgetId, getApplicationContext()));

            if(streamItem.getCanvasContext() != null && streamItem.getCanvasContext().getType() != CanvasContext.Type.USER){
                row.setInt(R.id.icon,"setColorFilter", CanvasContextColor.getCachedColor(mContext, streamItem.getCanvasContext()));
            } else if(streamItem.getType() == StreamItem.Type.CONVERSATION){
                row.setInt(R.id.icon,"setColorFilter", getWidgetTextColor(appWidgetId, mContext));
            } else {
                row.setInt(R.id.icon,"setColorFilter", R.color.canvasRed);
            }

            if(!shouldHideDetails(getApplicationContext(), appWidgetId)) {
                if (streamItem.getMessage(mContext) != null) {
                    row.setTextViewText(R.id.message, StringUtilities.simplifyHTML(Html.fromHtml(streamItem.getMessage(mContext))));
                } else {
                    row.setTextViewText(R.id.message, "");
                    row.setViewVisibility(R.id.message, View.GONE);
                }
            }

            String courseAndDate = "";
            if (streamItem.getContextType() == CanvasContext.Type.COURSE && streamItem.getCanvasContext() != null) {
                courseAndDate = streamItem.getCanvasContext().getSecondaryName() + " ";
            }
            courseAndDate += DateHelper.getDateTimeString(mContext, streamItem.getUpdatedAtDate());
            row.setTextViewText(R.id.course_and_date, courseAndDate);

            row.setOnClickFillInIntent(R.id.widget_root, createIntent(streamItem));

        }

        @Override
        protected Intent createIntent(StreamItem streamItem) {
           return NotificationWidgetRouter.createIntent(mContext, streamItem);
        }

        @Override
        protected void clearViewData(RemoteViews row) {
            row.setTextViewText(R.id.course_and_date, "");
            row.setTextViewText(R.id.message, "");
            row.setTextViewText(R.id.title, "");
            row.setViewVisibility(R.id.icon, View.GONE);
        }

        private   int getDrawableId(StreamItem streamItem) {
            switch (streamItem.getType()) {
                case DISCUSSION_TOPIC:
                    return  R.drawable.ic_cv_discussions_fill;

                case ANNOUNCEMENT:
                    return R.drawable.ic_cv_announcements_fill;

                case SUBMISSION:
                    return R.drawable.ic_cv_assignments_fill;

                case CONVERSATION:
                    return R.drawable.ic_cv_messages_fill;

                case MESSAGE:
                    //a message could be related to an assignment, check the category

                    if(streamItem.getContextType() == CanvasContext.Type.COURSE) {
                        return R.drawable.ic_cv_assignments_fill;
                    } else if(streamItem.getNotificationCategory().toLowerCase().contains("assignment graded")) {
                        return R.drawable.ic_cv_grades_fill;
                    } else {
                        return R.drawable.ic_cv_user_fill;
                    }

                case CONFERENCE:
                    return R.drawable.ic_cv_conference_fill;
                case COLLABORATION:
                    return R.drawable.ic_cv_collaboration_fill;
                case COLLECTION_ITEM:
                default:
                    break;
            }

            return R.drawable.ic_cv_announcements_fill;

        }

        public void populateActivityStreamAdapter(Map<Long, Course> courseMap, Map<Long, Group> groupMap, List<StreamItem> streamItems) {
            // wait until both calls return;
            if (courseMap == null || groupMap == null || streamItems == null) {
                return;
            }

            for (final StreamItem streamItem : streamItems) {
                streamItem.setCanvasContextFromMap(courseMap, groupMap);

                // load conversations if needed
                if (streamItem.getType() == StreamItem.Type.CONVERSATION) {

                    Conversation conversation = ConversationManager.getConversationSynchronous(streamItem.getConversationId(), true);

                    streamItem.setConversation(mContext, conversation, ApiPrefs.getUser().getId(), mContext.getResources().getString(R.string.monologue));

                }
            }
        }
    }
}
