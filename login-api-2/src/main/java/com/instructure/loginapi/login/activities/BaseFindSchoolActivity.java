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
package com.instructure.loginapi.login.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.AccountDomainManager;
import com.instructure.canvasapi2.models.AccountDomain;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.loginapi.login.R;
import com.instructure.loginapi.login.adapter.DomainAdapter;
import com.instructure.loginapi.login.api.zendesk.utilities.ZendeskDialogStyled;
import com.instructure.loginapi.login.util.Const;
import com.instructure.pandautils.utils.ColorUtils;
import com.instructure.pandautils.utils.ViewStyler;

import java.util.List;

import retrofit2.Response;

public abstract class BaseFindSchoolActivity extends AppCompatActivity implements ZendeskDialogStyled.ZendeskDialogResultListener {

    static { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); }

    private Toolbar mToolbar;
    private EditText mDomainInput;
    private DomainAdapter mDomainAdapter;
    private Handler mDelayFetchAccountHandler = new Handler();

    protected abstract @ColorInt int themeColor();
    protected abstract Intent signInActivityIntent(String url);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_school);
        bindViews();
        applyTheme();
    }

    private void bindViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDomainInput = (EditText) findViewById(R.id.domainInput);

        mToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_arrow_back));
        mToolbar.inflateMenu(R.menu.menu_next);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.next) {
                    validateDomain(mDomainInput.getText().toString());
                    return true;
                }
                return false;
            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mDomainInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            validateDomain(mDomainInput.getText().toString());
            return true;
            }
        });

        mDomainInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (mDomainAdapter != null) {
                    mDomainAdapter.getFilter().filter(s);
                    fetchAccountDomains();
                }
            }
        });

        mDomainAdapter = new DomainAdapter(new DomainAdapter.DomainEvents() {
            @Override
            public void onDomainClick(AccountDomain account) {
                mDomainInput.setText(account.getDomain());
                mDomainInput.setSelection(mDomainInput.getText().length());
                validateDomain(account.getDomain());
            }

            @Override
            public void onHelpClick() {
                ZendeskDialogStyled dialog = new ZendeskDialogStyled();
                dialog.setArguments(ZendeskDialogStyled.createBundle(true));
                dialog.show(getSupportFragmentManager(), ZendeskDialogStyled.TAG);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.findSchoolRecyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, RecyclerView.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(mDomainAdapter);
    }

    private void validateDomain(String domain) {
        String url = domain.toLowerCase().replace(" ", "");

        //if the user enters nothing, try to connect to canvas.instructure.com
        if (url.trim().length() == 0) {
            url = "canvas.instructure.com";
        }

        //if there are no periods, append .instructure.com
        if (!url.contains(".") || url.endsWith(".beta")) {
            url += ".instructure.com";
        }

        //URIs need to to start with a scheme.
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        //Get just the host.
        Uri uri = Uri.parse(url);
        url = uri.getHost();

        //Strip off www. if they typed it.
        if (url.startsWith("www.")) {
            url = url.substring(4);
        }

        Intent intent = signInActivityIntent(url);
        intent.putExtra(Const.CANVAS_LOGIN, getIntent().getExtras().getInt(Const.CANVAS_LOGIN, 0));
        startActivity(intent);
    }

    private void applyTheme() {
        final int color = themeColor();

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(ColorUtils.colorIt(color, ContextCompat.getDrawable(this, R.drawable.vd_canvas_logo)));

        mToolbar.addView(icon);

        ViewStyler.setStatusBarLight(this);
    }

    /**
     * Handles fetching account domains. Uses a worker runnable and handler to cancel fetching too often.
     */
    private void fetchAccountDomains() {
        mDelayFetchAccountHandler.removeCallbacks(mFetchAccountsWorker);
        mDelayFetchAccountHandler.postDelayed(mFetchAccountsWorker, 500);
    }

    /**
     * Worker thread for fetching account domains.
     */
    private Runnable mFetchAccountsWorker = new Runnable() {
        @Override
        public void run() {
            if(mDomainInput != null) {
                String query = mDomainInput.getText().toString();
                AccountDomainManager.searchAccounts(query, mAccountDomainCallback);
            }
        }
    };

    private StatusCallback<List<AccountDomain>> mAccountDomainCallback = new StatusCallback<List<AccountDomain>>() {

        @Override
        public void onResponse(Response<List<AccountDomain>> response, LinkHeaders linkHeaders, ApiType type) {
            if(type.isAPI()) {
                List<AccountDomain> domains = response.body();

                boolean isDebuggable = 0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);

                if (isDebuggable) {
                    //put these domains first
                    domains.add(0, createAccountForDebugging("mobiledev.instructure.com"));
                    domains.add(1, createAccountForDebugging("mobiledev.beta.instructure.com"));
                    domains.add(2, createAccountForDebugging("mobileqa.instructure.com"));
                    domains.add(3, createAccountForDebugging("mobileqat.instructure.com"));
                    domains.add(4, createAccountForDebugging("ben-k.instructure.com"));
                    domains.add(5, createAccountForDebugging("clare.instructure.com"));
                    domains.add(6, createAccountForDebugging("mobileqa.test.instructure.com"));
                }

                if (mDomainAdapter != null) {
                    mDomainAdapter.setItems(domains);
                    mDomainAdapter.getFilter().filter(mDomainInput.getText().toString());
                }
            }
        }
    };

    private AccountDomain createAccountForDebugging(String domain) {
        AccountDomain account = new AccountDomain();
        account.setDomain(domain);
        account.setName("@ " + domain);
        account.setDistance(null);
        return account;
    }

    @Override
    protected void onDestroy() {
        if(mDelayFetchAccountHandler != null && mFetchAccountsWorker != null) {
            mDelayFetchAccountHandler.removeCallbacks(mFetchAccountsWorker);
        }
        super.onDestroy();
    }

    //region Help & Support

    @Override
    public void onTicketPost() {
        Toast.makeText(this, R.string.zendesk_feedbackThankyou, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTicketError() {}

    //endregion
}
