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
package com.krawler.spring.accounting.companypreferances;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accCompanyPreferencesDAO {
    public KwlReturnObject addPreferences(HashMap<String, Object> prefMap) throws ServiceException;
    public KwlReturnObject updatePreferences(HashMap<String, Object> prefMap) throws ServiceException;
    public KwlReturnObject getCompanyPreferences(Map<String, Object> filterParams) throws ServiceException;
    
    public String getNextAutoNumber(String companyid, int from) throws ServiceException, AccountingException;
    public KwlReturnObject getPreferencesFromAccount(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject addYearLock(HashMap<String, Object> yearLockMap) throws ServiceException;
    public KwlReturnObject updateYearLock(HashMap<String, Object> yearLockMap) throws ServiceException;
    public KwlReturnObject getYearLock(Map<String, Object> filterParams) throws ServiceException;

    public void setAccountPreferences(String companyid, HashMap hm, Date curDate) throws ServiceException;
	public void setNewYear(Date time, String companyid) throws ServiceException;
	public void setCurrentYear(int presentYear, int previousYear, String companyid) throws ServiceException; 
	
    public KwlReturnObject getYearLockforPreferences(Map<String, Object> filterParams) throws ServiceException;
}
