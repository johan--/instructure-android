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

package com.instructure.teacher.factory;

import com.instructure.canvasapi2.models.Conversation;
import com.instructure.teacher.presenters.MessageThreadPresenter;

import instructure.androidblueprint.PresenterFactory;


public class MessageThreadPresenterFactory implements PresenterFactory<MessageThreadPresenter> {

    private Conversation mConversation;
    private int mPosition;

    public MessageThreadPresenterFactory(Conversation conversation, int position) {
        mConversation = conversation;
        mPosition = position;
    }

    @Override
    public MessageThreadPresenter create() {
        return new MessageThreadPresenter(mConversation, mPosition);
    }

}
