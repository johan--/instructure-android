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
package com.instructure.teacher.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Recipient;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.pandautils.fragments.BaseSyncFragment;
import com.instructure.pandautils.utils.ThemePrefs;
import com.instructure.pandautils.utils.ViewStyler;
import com.instructure.teacher.R;
import com.instructure.teacher.adapters.ChooseMessageRecipientRecyclerAdapter;
import com.instructure.teacher.events.ChooseMessageEvent;
import com.instructure.teacher.factory.ChooseRecipientsPresenterFactory;
import com.instructure.teacher.holders.RecipientViewHolder;
import com.instructure.teacher.interfaces.RecipientAdapterCallback;
import com.instructure.teacher.presenters.ChooseRecipientsPresenter;
import com.instructure.teacher.utils.RecyclerViewUtils;
import com.instructure.teacher.utils.ViewUtils;
import com.instructure.teacher.view.EmptyPandaView;
import com.instructure.teacher.viewinterface.ChooseRecipientsView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import instructure.androidblueprint.PresenterFactory;

public class ChooseRecipientsFragment extends BaseSyncFragment<Recipient, ChooseRecipientsPresenter, ChooseRecipientsView, RecipientViewHolder, ChooseMessageRecipientRecyclerAdapter> implements ChooseRecipientsView {

    private ChooseMessageRecipientRecyclerAdapter mRecyclerAdapter;

    private static final String RECIPIENT_LIST = "recipient_list";
    private static final String CONTEXT_ID = "context_id";

    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    @BindView(R.id.emptyPandaView) EmptyPandaView mEmptyPandaView;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layoutResId(), container, false);
        ButterKnife.bind(this, view);

        setupToolbar();
        ((TextView)view.findViewById(R.id.menu_done)).setTextColor(ThemePrefs.getButtonColor());
        return view;
    }

    @Override
    public int layoutResId() {
        return R.layout.fragment_choose_recipients;
    }

    @Override
    public void onCreateView(View view) { }

    private void setupToolbar() {
        // Set 'close' button
        ViewUtils.setupToolbarBackButton(mToolbar, this);

        // Set titles
        mToolbar.setTitle(R.string.select_recipients);

        // Set up menu
        mToolbar.inflateMenu(R.menu.menu_done_text);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_done:
                        //send the recipient list back to the message
                        EventBus.getDefault().postSticky(new ChooseMessageEvent(getPresenter().getRecipients(), null));
                        //clear the backstack because we want to go back to the message, not necessarily the previous screen
                        getPresenter().clearBackStack();
                        getActivity().onBackPressed();
                }
                return false;
            }
        });

        // Apply toolbar theme
        ViewStyler.themeToolbarBottomSheet(getActivity(), getResources().getBoolean(R.bool.is_device_tablet), mToolbar, Color.BLACK, false);
    }

    @Override
    protected void onReadySetGo(ChooseRecipientsPresenter presenter) {
        getPresenter().loadData(false);
        if(getArguments().getParcelableArrayList(RECIPIENT_LIST) != null) {
            ArrayList<Recipient> recipients = getArguments().getParcelableArrayList(RECIPIENT_LIST);
            getPresenter().addAlreadySelectedRecipients(recipients);
        }
    }

    @Override
    protected PresenterFactory<ChooseRecipientsPresenter> getPresenterFactory() {
        return new ChooseRecipientsPresenterFactory(getArguments().getString(CONTEXT_ID));
    }

    @Override
    protected void onPresenterPrepared(ChooseRecipientsPresenter presenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(getActivity().getWindow().getDecorView().getRootView(),
                getContext(), getAdapter(), presenter, R.id.swipeRefreshLayout, R.id.recyclerView, R.id.emptyPandaView, getString(R.string.no_items_to_display_short));
    }

    @Override
    protected ChooseMessageRecipientRecyclerAdapter getAdapter() {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new ChooseMessageRecipientRecyclerAdapter(getContext(), getPresenter(), mAdapterToFragmentCallback);
        }
        return mRecyclerAdapter;
    }

    private RecipientAdapterCallback mAdapterToFragmentCallback = new RecipientAdapterCallback() {

        @Override
        public void onRowClicked(Recipient recipient, int position, boolean isCheckbox) {
            if (recipient.getRecipientType() == Recipient.Type.person) {
                //select and deselect individuals.
                getPresenter().addOrRemoveRecipient(recipient, position);
                mRecyclerAdapter.notifyItemChanged(position);
            } else if (recipient.getRecipientType() == Recipient.Type.metagroup) {
                //always go to a metagroup - Canvas won't let you send a message to an entire metagroup
                getPresenter().setContextRecipient(recipient);
            } else if (recipient.getRecipientType() == Recipient.Type.group) {
                //If it's a group, make sure there are actually users in that group.
                if (recipient.getUserCount() > 0) {
                    if (isCheckbox) {
                        getPresenter().addOrRemoveRecipient(recipient, position);
                    } else {
                        // filter down to the group
                        if (getPresenter().isRecipientSelected(recipient)) {
                            showToast(R.string.entire_group_selected);
                        } else {
                            getPresenter().setContextRecipient(recipient);
                        }
                    }
                } else {
                    showToast(R.string.no_users_in_group);
                }
            }
        }

        @Override
        public boolean isRecipientSelected(Recipient recipient) {
            return getPresenter().isRecipientSelected(recipient);
        }

        @Override
        public boolean isRecipientCurrentUser(Recipient recipient) {
            return isSelfSelected(recipient.getStringId());
        }
    };

    @NonNull
    @Override
    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void onRefreshFinished() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefreshStarted() {
        mEmptyPandaView.setLoading();
    }

    @Override
    public boolean withPagination() {
        return true;
    }

    @Override
    public void checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(mEmptyPandaView, mRecyclerView, mSwipeRefreshLayout, getAdapter(), getPresenter().isEmpty());
    }

    private boolean isSelfSelected(String stringId) {
        try {
            if (Long.parseLong(stringId) == ApiPrefs.getUser().getId()) {
                return true;
            }
        } catch (NumberFormatException ignore) {
        }
        return false;
    }


    @Override
    public boolean onHandleBackPressed() {
        return getPresenter().popBackStack();
    }

    public static Bundle createBundle(CanvasContext canvasContext, ArrayList<Recipient> addedRecipients) {
        Bundle bundle = new Bundle();
        bundle.putString(CONTEXT_ID, canvasContext.getContextId());
        bundle.putParcelableArrayList(RECIPIENT_LIST, addedRecipients);
        return bundle;
    }

    public static ChooseRecipientsFragment newInstance(Bundle bundle) {
        ChooseRecipientsFragment chooseRecipientsFragment = new ChooseRecipientsFragment();
        chooseRecipientsFragment.setArguments(bundle);
        return chooseRecipientsFragment;
    }

    @Override
    public void unBundle(@NonNull Bundle extras) {

    }

    @Override
    protected int perPageCount() {
        return ApiPrefs.getPerPageCount();
    }
}
