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
package com.instructure.teacher.utils;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.instructure.canvasapi2.models.BasicUser;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Course;
import com.instructure.pandautils.utils.PandaViewUtils;
import com.instructure.teacher.R;
import com.instructure.teacher.adapters.StudentContextFragment;
import com.instructure.teacher.router.Route;
import com.instructure.teacher.router.RouteMatcher;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This will need to be moved back into the login apis profile utils when in the future when we replace CanvasAPI with android-canvas-api
 */
public class ProfileUtils extends com.instructure.pandautils.utils.ProfileUtils {

    public static void configureAvatarViewConversations(final Context context, final CircleImageView avatar1, final CircleImageView avatar2, Conversation conversation) {
        final List<BasicUser> users = conversation.getParticipants();

        if(users.size() == 0) return;

        final boolean isGroup = isGroup(users);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            avatar1.setTransitionName(com.instructure.pandautils.utils.Const.MESSAGE + String.valueOf(conversation.getId()));
        }

        final BasicUser firstUser = users.get(0);
        firstUser.setAvatarUrl(conversation.getAvatarUrl());

        // Reset click listener
        avatar1.setOnClickListener(null);

        if(isGroup) {
            avatar1.setImageResource(R.drawable.vd_group);
            avatar1.setVisibility(View.VISIBLE);
            avatar2.setVisibility(View.GONE);
        } else {
            avatar2.setVisibility(View.INVISIBLE);
            avatar1.setAlpha(1F);
            loadAvatarForUser(context, avatar1, firstUser.getName(), firstUser.getAvatarUrl());

            // Set click listener to show context card
            final CanvasContext canvasContext = CanvasContext.fromContextCode(conversation.getContextCode());
            if (canvasContext != null && canvasContext instanceof Course) {
                PandaViewUtils.setupAvatarA11y(avatar1, firstUser.getName());
                avatar1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = StudentContextFragment.makeBundle(firstUser.getId(), canvasContext.getId(), false);
                        RouteMatcher.route(context, new Route(StudentContextFragment.class, null, bundle));
                    }
                });
            }
        }
    }

    public static void loadAvatarForUser(final Context context, final CircleImageView avatar, String displayName, String avatarUrl) {
        BasicUser user = new BasicUser();
        user.setName(displayName);
        user.setAvatarUrl(avatarUrl);
        loadAvatarForUser(context, avatar, user);
    }

    public static void loadAvatarForUser(final Context context, final CircleImageView avatar, final BasicUser user) {
        String url = user.getAvatarUrl();
        if (url == null || TextUtils.isEmpty(url) || url.contains(noPictureURL) || url.contains(noPictureURLAlternate) || url.contains(noPictureURLAlternateDecoded) || url.contains(noPictureURLGroup)) {
            Picasso.with(context).cancelRequest(avatar);
            PandaViewUtils.setUserAvatarImage(avatar, context, user.getName());
        } else {
            Picasso.with(context)
                    .load(url)
                    .fit()
                    .placeholder(R.drawable.recipient_avatar_placeholder)
                    .centerCrop()
                    .into(avatar, new Callback() {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onError() {
                            PandaViewUtils.setUserAvatarImage(avatar, context, user.getName());
                        }
                    });
        }
    }

    public static boolean isGroup(List<BasicUser> users) {
        return users != null && users.size() > 2;
    }
}
