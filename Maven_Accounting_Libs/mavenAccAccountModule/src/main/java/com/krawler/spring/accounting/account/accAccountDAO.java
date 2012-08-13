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
package com.krawler.spring.accounting.account;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.Group;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author krawler
 */
public interface accAccountDAO {

    public KwlReturnObject deleteAccount(String accountid, String companyid) throws ServiceException;
    public KwlReturnObject saveAccount(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getAccount(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject addAccount(JSONObject accjson) throws ServiceException;
    public KwlReturnObject updateAccount(JSONObject accjson) throws ServiceException;
    public void updateChildrenAccount(Account account) throws ServiceException;
    public KwlReturnObject getAccountsForCombo(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getAccounts(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteAccount(HashMap request, String companyid) throws ServiceException;
    public KwlReturnObject deleteAccount(String accountid, boolean flag) throws ServiceException;

    public KwlReturnObject getGroup(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject addGroup(JSONObject groupjson) throws ServiceException;
    public KwlReturnObject updateGroup(JSONObject groupjson) throws ServiceException;
    public void updateChildrenGroup(Group group) throws ServiceException;
    public KwlReturnObject getGroups(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteGroup(String groupid) throws ServiceException;
    public KwlReturnObject getMaxGroupDisplayOrder() throws ServiceException;

    public KwlReturnObject getAccountEntry(HashMap<String, Object> filterParams) throws ServiceException;
    public KwlReturnObject getAccountDatewise(String companyid, Date startDate, Date endDate) throws ServiceException;
    public KwlReturnObject getGroupForProfitNloss(String companyid, int nature, boolean affectGrossProfit) throws ServiceException;

    public ArrayList getAccountArrayList(List list, HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject copyAccounts(String companyid, String currencyid, String companyType) throws ServiceException;
    public KwlReturnObject getDefaultAccount(String companyType) throws ServiceException;
    public boolean isChild(String ParentID, String childID) throws ServiceException;
    public List isChildorGrandChild(String childID) throws ServiceException;
    public List isChildforDelete(String childID) throws ServiceException;
}
