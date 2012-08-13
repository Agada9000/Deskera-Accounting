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

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
public class accJournalEntryImpl implements accJournalEntryDAO,JournalEntryConstants {
    private HibernateTemplate hibernateTemplate;
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

    public KwlReturnObject getJEDfromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from JournalEntryDetail jed where account.ID=? and jed.journalEntry.deleted=false and jed.company.companyID=?";
        list = HibernateUtil.executeQuery(hibernateTemplate, q, new Object[]{accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getJECount(String jeno, String companyid) throws ServiceException{
        List list = new ArrayList();
        String q = "from JournalEntry where entryNumber=? and company.companyID=?";
        list = HibernateUtil.executeQuery(hibernateTemplate, q, new Object[]{jeno, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getJEDset(JSONArray JArr, String companyid) throws ServiceException {
        List list = new ArrayList();
        try{
            HashSet hs = new HashSet();
            for(int i=0; i<JArr.length(); i++){
                JSONObject jobj=JArr.getJSONObject(i);
                JournalEntryDetail jed = new JournalEntryDetail();
                jed.setSrno(i+1);
                jed.setCompany((Company)hibernateTemplate.get(Company.class, companyid));
                if (jobj.has(AMOUNT)) {
                    jed.setAmount(jobj.getDouble(AMOUNT));
                }
                if (jobj.has(ACCOUNTID)) {
                    jed.setAccount((Account)hibernateTemplate.get(Account.class,jobj.getString(ACCOUNTID)));
                }
                if (jobj.has(DEBIT)) {
                    jed.setDebit(jobj.getBoolean(DEBIT));
                }
                if (jobj.has(DESCRIPTION)) {
                    String desc = "";
                    if(!jobj.getString(DESCRIPTION).equals("")){
                        desc = java.net.URLDecoder.decode(jobj.getString(DESCRIPTION));
                    }
                    jed.setDescription(desc);
                }
                hs.add(jed);
            }
            list.add(hs);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accJournalEntryImpl.getJEDset : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

     public KwlReturnObject addJournalEntryDetails(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        JournalEntryDetail jed=new JournalEntryDetail();
        try{
            if(json.has(JEDID)){
                if(!StringUtil.isNullOrEmpty(json.getString(JEDID)))
                jed = (JournalEntryDetail) hibernateTemplate.get(JournalEntryDetail.class, json.getString(JEDID));
            }
             if(json.has(SRNO)){
                jed.setSrno((Integer)json.get(SRNO));
            }
            if(json.has(AMOUNT)){
                jed.setAmount(json.getDouble(AMOUNT));
            }
            if(json.has(DEBIT)){
                jed.setDebit(json.getBoolean(DEBIT));
            }
            if(json.has(JEID)){
                jed.setJournalEntry((JournalEntry) hibernateTemplate.get(JournalEntry.class, json.getString(JEID)));
            }
            if(json.has(ACCOUNTID)){
                jed.setAccount((Account) hibernateTemplate.get(Account.class, json.getString(ACCOUNTID)));
            }
            if(json.has(COMPANYID)){
                jed.setCompany((Company) hibernateTemplate.get(Company.class, json.getString(COMPANYID)));
            }
            hibernateTemplate.save(jed);
            list.add(jed);
        }catch(Exception ex){
            throw ServiceException.FAILURE("accJournalEntryImpl.addJournalEntryDetails : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updateJournalEntryDetails(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        try{
            String jedid = json.getString(JEDID);
            JournalEntryDetail jed = (JournalEntryDetail) hibernateTemplate.get(JournalEntryDetail.class, jedid);
            if(jed != null){
                if(json.has(SRNO)){
                    jed.setSrno((Integer)json.get(SRNO));
                }
                if(json.has(AMOUNT)){
                    jed.setAmount(json.getDouble(AMOUNT));
                }
                if(json.has(DEBIT)){
                    jed.setDebit(json.getBoolean(DEBIT));
                }
                if(json.has(JEID)){
                    jed.setJournalEntry((JournalEntry) hibernateTemplate.get(JournalEntry.class, json.getString(JEID)));
                }
                if(json.has(ACCOUNTID)){
                    jed.setAccount((Account) hibernateTemplate.get(Account.class, json.getString(ACCOUNTID)));
                }
                if(json.has(COMPANYID)){
                    jed.setCompany((Company) hibernateTemplate.get(Company.class, json.getString(COMPANYID)));
                }
                hibernateTemplate.save(jed);
            }
            list.add(jed);
        }catch(Exception ex){
            throw ServiceException.FAILURE("accJournalEntryImpl.updateJournalEntryDetails : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updateJournalEntryDetails(Map requestMap) throws ServiceException {
        List list = new ArrayList();
        JournalEntryDetail jed = null;
        try{
            String jedid = (String)requestMap.get(JEDID);
            if(StringUtil.isNullOrEmpty(jedid)){
                 jed = new JournalEntryDetail();
            }else{
                jed = (JournalEntryDetail) hibernateTemplate.get(JournalEntryDetail.class, jedid);
            }
            
            if(jed != null){
                if(requestMap.containsKey(SRNO)){
                    jed.setSrno((Integer)requestMap.get(SRNO));
                }
                if(requestMap.containsKey(AMOUNT)){
                    jed.setAmount((Double)requestMap.get(AMOUNT));
                }
                if(requestMap.containsKey(DEBIT)){
                    jed.setDebit((Boolean)requestMap.get(DEBIT));
                }
                if (requestMap.containsKey(DESCRIPTION)) {
                    jed.setDescription((String) requestMap.get(DESCRIPTION));
                }

                if(requestMap.containsKey(JEID)){
                    JournalEntry je = requestMap.get(JEID)==null?null:(JournalEntry) hibernateTemplate.get(JournalEntry.class, (String)requestMap.get(JEID));
                    jed.setJournalEntry(je);
                }
                if(requestMap.containsKey(ACCOUNTID)){
                    Account account = requestMap.get(ACCOUNTID)==null?null:(Account) hibernateTemplate.get(Account.class, (String)requestMap.get(ACCOUNTID));
                    jed.setAccount(account);
                }
                if(requestMap.containsKey(COMPANYID)){
                    Company company = requestMap.get(COMPANYID)==null?null:(Company)hibernateTemplate.get(Company.class, (String) requestMap.get(COMPANYID));
                    jed.setCompany(company);
                }
                hibernateTemplate.saveOrUpdate(jed);
            }
            list.add(jed);
        }catch(Exception ex){
            throw ServiceException.FAILURE("accJournalEntryImpl.updateJournalEntryDetails : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject addJournalEntry(JSONObject json, HashSet<JournalEntryDetail> details) throws ServiceException, AccountingException {
        List list = new ArrayList();
        try {
            JournalEntry je = new JournalEntry();
            je = buildJE(je, json, details);
            hibernateTemplate.save(je);
            list.add(je);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("addJournalEntry : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updateJournalEntry(JSONObject json, HashSet<JournalEntryDetail> details) throws ServiceException, AccountingException {
        List list = new ArrayList();
        JournalEntry je;
        try {
            String jeid = json.getString(JEID);
            je = (JournalEntry)hibernateTemplate.get(JournalEntry.class, jeid);
            je = buildJE(je, json, details);
            hibernateTemplate.save(je);
        } catch(JSONException ex) {
            throw ServiceException.FAILURE("updateJournalEntry : "+ex.getMessage(), ex);
        }
        list.add(je);
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    private JournalEntry buildJE(JournalEntry je, JSONObject json, HashSet<JournalEntryDetail> details) throws ServiceException, AccountingException, JSONException {
        if(json.has(ENTRYNUMBER)){
            je.setEntryNumber(json.getString(ENTRYNUMBER));
        }
        if(json.has(AUTOGENERATED)){
            je.setAutoGenerated(json.getBoolean(AUTOGENERATED));
        }
        if(json.has(ENTRYDATE)){
            je.setEntryDate((Date) json.get(ENTRYDATE));
        }
        if(json.has(MEMO)){
            je.setMemo(json.getString(MEMO));
        }
        if(json.has(COMPANYID)){
            je.setCompany((Company) hibernateTemplate.get(Company.class, json.getString(COMPANYID)));
        }
        if(json.has(CURRENCYID)){
            je.setCurrency((KWLCurrency)hibernateTemplate.get(KWLCurrency.class,json.getString(CURRENCYID)));
        }
        if(!details.isEmpty()){
            Iterator<JournalEntryDetail> itr = details.iterator();
            double amount = 0.0;
            while (itr.hasNext()) {
                JournalEntryDetail jed = itr.next();
                jed.setJournalEntry(je);
                if (jed.isDebit()) {
                    amount += jed.getAmount();
                } else {
                    amount -= jed.getAmount();
                }
            }
            if (Math.abs(amount) >= 0.000001) {
                throw new AccountingException("Debit and credit amounts are not same");
            }
            je.setDetails(details);
        }
        return je;
    }

    public KwlReturnObject deleteJE(String jeid, String companyid) throws ServiceException {
        //Delete Journal Entry
        String delQuery = "delete from JournalEntry je where ID=? and je.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery,new Object[]{jeid, companyid});

        return new KwlReturnObject(true, "Journal Entry has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteJEEntry(String jeid, String companyid) throws ServiceException {
        //Delete Journal Entry
        String delQuery = "update JournalEntry je set je.deleted=true  where je.ID = ? and je.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery,new Object[]{jeid, companyid});

        return new KwlReturnObject(true, "Journal Entry has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteJEDtails(String jeid, String companyid) throws ServiceException {
        //Delete Journal Entry details
        String delQuery = "delete from JournalEntryDetail jed where jed.journalEntry.ID=? and jed.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{jeid, companyid});

        return new KwlReturnObject(true, "JournalEntry details has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getJournalEntry(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
//            KWLCurrency currency = (KWLCurrency)hibernateTemplate.get(KWLCurrency.class, (String) request.get("gcurrencyid"));
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(START);
            String limit = (String) request.get(LIMIT);
            String ss = (String) request.get(SS);
            String linkentry = (String) request.get(LINKID);
            boolean deleted =Boolean.parseBoolean((String)request.get(DELETED));
            boolean nondeleted =Boolean.parseBoolean((String)request.get(NONDELETED));
            ArrayList params = new ArrayList();
            params.add((String) request.get(COMPANYID));
            String condition = " where company.companyID=? ";
            
            if(request.containsKey("groupid") && Boolean.parseBoolean(request.get("groupid").toString())){
            	params.add("12");
            	params.add((String) request.get(COMPANYID));
            	condition += " and ID in (select distinct(jed.journalEntry.ID) from JournalEntryDetail jed where jed.account.group.ID=? and jed.company.companyID=?) ";
            }

            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and costcenter.ID=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (entryDate >=? and entryDate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            if (nondeleted)
                condition += " and deleted=false ";
            else if (deleted)
                condition += " and deleted=true ";
            if (!StringUtil.isNullOrEmpty(linkentry)) {
                params.add(linkentry);
                condition += " and id=? order by entryDate";
            } else {
                if (!StringUtil.isNullOrEmpty(ss)) {
                    params.add(ss + "%");
                    params.add(ss + "%");
                    condition += " and (entryno like ? or memo like ?) order by entryDate";
                } else {
                    condition += " order by entryDate";
                }
            }
            String query = "from JournalEntry" + condition;

            list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());//params.toArray() sessionHandlerImpl.getCompanyid(request));  new Object[]{ sessionHandlerImpl.getCompanyid(request)}
            count = list.size();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                list = HibernateUtil.executeQueryPaging(hibernateTemplate, query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getJournalEntry : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject deleteJournalEntry(String jeid, String companyid) throws ServiceException {
        String query = "update JournalEntry set deleted=true where ID=? and company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, query, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "Journal Entry has been deleted successfully.", null, null, numRows);
    }
     public KwlReturnObject permanentDeleteJournalEntry(String jeid, String companyid) throws ServiceException {
        String query = "delete from JournalEntry where ID=? and company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, query, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "Journal Entry has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getAccountBalance(String accountid, Date startDate, Date endDate) throws ServiceException {
        return getAccountBalance(accountid, startDate, endDate, "");
    }
    
    public KwlReturnObject getAccountBalance(String accountid, Date startDate, Date endDate, String costCenterID) throws ServiceException {
        List list = new ArrayList();
        String query = "select (case when debit=true then amount else -amount end) ,jed , jed.journalEntry.currency.currencyID from JournalEntryDetail jed where account.ID=? and jed.journalEntry.deleted=false and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<? ";
        ArrayList params = new ArrayList();
        params.add(accountid);
        if (startDate == null) {
            startDate = new Date(0);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        params.add(startDate);
        params.add(endDate);
        
        if(!StringUtil.isNullOrEmpty(costCenterID)){
            query += " and jed.journalEntry.costcenter.ID=?";
            params.add(costCenterID);
        }
        list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getLedger(String companyid, String accountid, Date startDate, Date endDate) throws ServiceException {
        List list = new ArrayList();
        String query = "select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and je.entryDate >= ? and je.entryDate <= ? and ac.company.companyID=? and je.deleted=false order by je.entryDate";
        Object[] params = {
            accountid,
            AccountingManager.setFilterTime(startDate, true),
            AccountingManager.setFilterTime(endDate, false),
            companyid
        };
        list = HibernateUtil.executeQuery(hibernateTemplate, query, params);
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getTax1099AccJE(String companyid,Date endDate,String vendorid) throws ServiceException {
        List list = new ArrayList();
        String query = "select jed,sum(jed.amount) from JournalEntryDetail jed where jed.account.ID in (select t.account.ID from Tax1099Accounts  t where t.company.companyID=?) and  " +
                "jed.debit=true and jed.journalEntry.entryDate <= ?  and jed.journalEntry.deleted=false and  jed.journalEntry.ID in" +
                "(select sjed.journalEntry.ID from JournalEntryDetail sjed where  sjed.account.ID=? and sjed.debit=false and sjed.company.companyID=?)  group by jed.account.ID";
        Object[] params = { 
            companyid, endDate,vendorid,companyid           
        };
        list = HibernateUtil.executeQuery(hibernateTemplate, query, params);
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getJournalEntryDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from JournalEntryDetail";
        return StringUtil.buildNExecuteQuery(hibernateTemplate, query, requestParams);
    }

    public KwlReturnObject saveJournalEntry(Map<String, Object> dataMap) throws ServiceException, AccountingException {
        List list = new ArrayList();
        try {
            JournalEntry je = new JournalEntry();

            if (dataMap.containsKey(JEID)) {
                je = (JournalEntry) hibernateTemplate.get(JournalEntry.class, (String) dataMap.get(JEID));
            }
            
            if (dataMap.containsKey(ENTRYNUMBER)) {
                je.setEntryNumber((String) dataMap.get(ENTRYNUMBER));
            }
            if (dataMap.containsKey(AUTOGENERATED)) {
                je.setAutoGenerated((Boolean) dataMap.get(AUTOGENERATED));
            }
            if (dataMap.containsKey(ENTRYDATE)) {
                je.setEntryDate((Date) dataMap.get(ENTRYDATE));
            }
            if (dataMap.containsKey(MEMO)) {
                je.setMemo((String) dataMap.get(MEMO));
            }
            if (dataMap.containsKey(EXTERNALCURRENCYRATE)) {
                je.setExternalCurrencyRate((Double) dataMap.get(EXTERNALCURRENCYRATE));
            }
            if (dataMap.containsKey(COMPANYID)) {
                Company company = dataMap.get(COMPANYID)==null?null:(Company)hibernateTemplate.get(Company.class, (String) dataMap.get(COMPANYID));
                je.setCompany(company);
            }
            if (dataMap.containsKey(CURRENCYID)) {
                KWLCurrency currency = dataMap.get(CURRENCYID)==null?null:(KWLCurrency)hibernateTemplate.get(KWLCurrency.class, (String) dataMap.get(CURRENCYID));
                je.setCurrency(currency);
            }
            if (dataMap.containsKey(CCConstants.JSON_costcenterid)) {
                CostCenter costCenter = dataMap.get(CCConstants.JSON_costcenterid)==null?null:(CostCenter)hibernateTemplate.get(CostCenter.class, (String) dataMap.get(CCConstants.JSON_costcenterid));
                je.setCostcenter(costCenter);
            }
            if (dataMap.containsKey(JEDETAILS)) {
                HashSet<JournalEntryDetail> details = (HashSet<JournalEntryDetail>) dataMap.get(JEDETAILS);
                if (!details.isEmpty()) {
                    Iterator<JournalEntryDetail> itr = details.iterator();
                    double amount = 0.0;
                    while (itr.hasNext()) {
                        JournalEntryDetail jed = itr.next();
                        jed.setJournalEntry(je);
                        if (jed.isDebit()) {
                            amount += jed.getAmount();
                        } else {
                            amount -= jed.getAmount();
                        }
                    }
                    if (Math.abs(amount) >= 0.000001) {
                        throw new AccountingException("Debit and credit amounts are not same");
                    }
                    je.setDetails(details);
                }
            }
            hibernateTemplate.save(je);
            list.add(je);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveJournalEntry : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

     public KwlReturnObject getJournalEntryDetail(String jeid, String companyid) throws ServiceException {
         List list = new ArrayList();
        String query = "from JournalEntryDetail jed where jed.journalEntry.ID=? and company.companyID=?";
        ArrayList params = new ArrayList();
        params.add(jeid);
        params.add(companyid);
        list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }



    public KwlReturnObject deleteJournalEntryDetails(String jeid, String companyid) throws ServiceException {
 //SQL       delete from jedetail where journalEntry ='ff80808128d936af0128d9395fb00002' and  (select count(*) from invoice where journalEntry ='ff80808128d936af0128d9395fb00002' and (centry=jedetail.id or sentry=jedetail.id or oentry=jedetail.id or taxentry=jedetail.id))=0;
        String delQuery = "delete from JournalEntryDetail jed where jed.journalEntry.ID=?  and jed.company.companyID=?";// and (select count(*) from Invoice inv where inv.journalEntry.ID =? and inv.customerEntry=jed.id) =0";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery,new Object[]{jeid,companyid});//,jeid});

        return new KwlReturnObject(true, "Invoice has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteJournalEntryDetailRow(String jedid, String companyid) throws ServiceException {
 //SQL       delete from jedetail where journalEntry ='ff80808128d936af0128d9395fb00002' and  (select count(*) from invoice where journalEntry ='ff80808128d936af0128d9395fb00002' and (centry=jedetail.id or sentry=jedetail.id or oentry=jedetail.id or taxentry=jedetail.id))=0;
     String delQuery = "delete from JournalEntryDetail jed where jed.ID=?  and jed.company.companyID=?";// and (select count(*) from Invoice inv where inv.journalEntry.ID =? and inv.customerEntry=jed.id) =0";
     int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery,new Object[]{jedid,companyid});//,jeid});

        return new KwlReturnObject(true, "Invoice has been deleted successfully.", null, null, numRows);
    }

	@Override
	public KwlReturnObject getJEDFixedAssetSale(String companyid, String accountid, boolean isDebit, String Description) throws ServiceException {
		// TODO Auto-generated method stub
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(companyid);
        params.add(accountid);
        params.add(isDebit);
        params.add(Description);
		String Query = "from JournalEntryDetail jed where jed.company.companyID=? and jed.account.ID=? and jed.debit=? and jed.description=? and jed.journalEntry.deleted = false";
		list = HibernateUtil.executeQuery(hibernateTemplate, Query, params.toArray());
		return new KwlReturnObject(true, "", null, list, list.size());
	}
}
