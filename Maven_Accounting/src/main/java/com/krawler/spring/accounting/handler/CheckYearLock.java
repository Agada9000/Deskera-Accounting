/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package com.krawler.spring.accounting.handler;

import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import java.util.Date;
import java.util.HashMap;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 *
 * @author krawler
 */
public class CheckYearLock implements MethodInterceptor {
    private accCompanyPreferencesDAO accCompanyPreferencesObj;

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        String methodName = mi.getMethod().getName();

        if(methodName.equals("saveJournalEntry")) {
            Object arguments[] = mi.getArguments();
            HashMap<String, Object> requestParams = (HashMap<String, Object>) arguments[0];
            if(!requestParams.containsKey("DontCheckYearLock")){
                Date date = (Date) requestParams.get("entrydate");
                CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, date);
            }
        }

        return mi.proceed();
    }

    
}
