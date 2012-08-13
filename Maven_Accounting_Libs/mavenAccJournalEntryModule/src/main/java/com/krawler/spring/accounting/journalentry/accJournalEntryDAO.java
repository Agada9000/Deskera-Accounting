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
package com.krawler.spring.accounting.journalentry;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accJournalEntryDAO {
    public KwlReturnObject deleteJournalEntryDetailRow(String jeid, String companyid)throws ServiceException;
    public KwlReturnObject getJournalEntryDetail(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getTax1099AccJE(String string, Date date, String iD) throws ServiceException;
    public KwlReturnObject permanentDeleteJournalEntry(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject deleteJournalEntryDetails(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getJECount(String jeno, String companyid) throws ServiceException;
    public KwlReturnObject getJEDset(JSONArray JArr, String companyid) throws ServiceException;
    public KwlReturnObject addJournalEntryDetails(JSONObject json) throws ServiceException;

    public KwlReturnObject getJournalEntry(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject saveJournalEntry(Map<String, Object> dataMap) throws ServiceException, AccountingException;
    
    public KwlReturnObject updateJournalEntryDetails(JSONObject json) throws ServiceException;
    public KwlReturnObject addJournalEntry(JSONObject json, HashSet<JournalEntryDetail> details) throws ServiceException, AccountingException;
    public KwlReturnObject updateJournalEntry(JSONObject json, HashSet<JournalEntryDetail> details) throws ServiceException, AccountingException;
    public KwlReturnObject deleteJE(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject deleteJEDtails(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getJEDfromAccount(String accountid, String companyid) throws ServiceException;
    public KwlReturnObject deleteJournalEntry(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject getAccountBalance(String accountid, Date startDate, Date endDate) throws ServiceException;
    public KwlReturnObject getAccountBalance(String accountid, Date startDate, Date endDate, String costCenterID) throws ServiceException;
    public KwlReturnObject getLedger(String companyid, String accountid, Date startDate, Date endDate) throws ServiceException;
    public KwlReturnObject deleteJEEntry(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject updateJournalEntryDetails(Map requestMap) throws ServiceException;
    public KwlReturnObject getJournalEntryDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getJEDFixedAssetSale(String companyid, String accountid, boolean isDebit, String Memo) throws ServiceException;

}
