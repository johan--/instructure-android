package com.instructure.loginapi.login.unit;

import com.instructure.canvasapi2.models.AccountDomain;
import com.instructure.loginapi.login.model.Account;

import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
public class AccountTest {
    @Test
    public void scrubList_nullList() throws Exception {

        ArrayList<AccountDomain> nullList = null;

        assertNotNull(Account.scrubList(nullList));
    }

    @Test
    public void scrubList_noLocations() throws Exception {

        ArrayList<AccountDomain> locationList = new ArrayList<>();

        AccountDomain accountDomain = new AccountDomain();
        accountDomain.setName("Test");
        accountDomain.setDomain("www.test.com");
        accountDomain.setDistance(null);
        locationList.add(accountDomain);

        assertEquals(0, Account.scrubList(locationList).size());
    }

    @Test
    public void scrubList_oneLocation() throws Exception {

        ArrayList<AccountDomain> locationList = new ArrayList<>();

        AccountDomain accountDomain = new AccountDomain();
        accountDomain.setName("Test");
        accountDomain.setDomain("www.test.com");
        accountDomain.setDistance(2.0);
        locationList.add(accountDomain);

        assertEquals(1, Account.scrubList(locationList).size());
    }

    @Test
    public void scrubList_twoAccountsOneLocation() throws Exception {

        ArrayList<AccountDomain> locationList = new ArrayList<>();

        AccountDomain accountDomain = new AccountDomain();
        accountDomain.setName("Test");
        accountDomain.setDomain("www.test.com");
        accountDomain.setDistance(2.0);
        locationList.add(accountDomain);

        AccountDomain accountDomainNoDistance = new AccountDomain();
        accountDomainNoDistance.setName("Test2");
        accountDomainNoDistance.setDomain("www.test2.com");
        accountDomainNoDistance.setDistance(null);
        locationList.add(accountDomainNoDistance);


        assertEquals(1, Account.scrubList(locationList).size());
    }
}