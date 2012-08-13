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

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.DefaultAccount;
import com.krawler.hql.accounting.Group;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.spring.accounting.handler.AccountingHandlerDAOImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
public class accAccountDAOImpl implements accAccountDAO{
    private HibernateTemplate hibernateTemplate;
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

    public KwlReturnObject getAccountEntry(HashMap<String, Object> filterParams) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition = "where deleted=false";
        String query="from Account ";

        if(filterParams.containsKey("id")){
            condition += (condition.length()==0?" where ":" and ") + "ID=?";
            params.add(filterParams.get("id"));
        }
        if(filterParams.containsKey("groupid")){
            condition += (condition.length()==0?" where ":" and ") + "group.ID=?";
            params.add(filterParams.get("groupid"));
        }
        if(filterParams.containsKey("companyid")){
            condition += (condition.length()==0?" where ":" and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        if(filterParams.containsKey("parent")){
            if(filterParams.get("parent")==null) {
                condition += (condition.length()==0?" where ":" and ") + "parent is null";
            }
        }

        if(filterParams.containsKey("order_by")) {
            condition += " order by "+filterParams.get("order_by");
        }
        query += condition;
//        query="from Account ac where ac.company.companyID=?";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    public KwlReturnObject getAccountDatewise(String companyid, Date startDate, Date endDate) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        params.add(companyid);
        if(startDate==null)startDate=new Date(0);
        if(endDate==null)endDate=new Date();
        params.add(startDate);
        params.add(endDate);
        String query="from Account ac where ac.company.companyID=? and ac.creationDate>=? and  ac.creationDate<? and ac.deleted=false";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    public KwlReturnObject getGroupForProfitNloss(String companyid, int nature, boolean affectGrossProfit) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        params.add(affectGrossProfit);
        params.add(companyid);
        String query="from Group where parent is null and nature in ("+nature+") and affectGrossProfit=? and (company is null or company.companyID=?) order by nature desc, displayOrder";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject addAccount(JSONObject accjson) throws ServiceException {
        List list = new ArrayList();
        try {
            Account account=new Account();
            account.setDeleted(false);

            if(accjson.has("creationdate")){
                account.setCreationDate((Date) accjson.get("creationdate"));
            }
            if(accjson.has("life")){
                account.setLife(accjson.getDouble("life"));
            }
            if(accjson.has("salvage")){
                account.setSalvage(accjson.getDouble("salvage"));
            }
            if(accjson.has("name")){
                account.setName(accjson.getString("name"));
            }
            if(accjson.has("balance")){
            	if(accjson.has("isFixedAsset")){
            		if(accjson.getBoolean("isFixedAsset")){
            			account.setOpeningBalance(0);
            		}else{
            			account.setOpeningBalance(accjson.getDouble("balance"));
            		}
                	account.setPresentValue(accjson.getDouble("balance"));
            	}else{
    				account.setOpeningBalance(accjson.getDouble("balance"));
                	account.setPresentValue(accjson.getDouble("balance"));
            	}
            }
            if(accjson.has("companyid")){
                account.setCompany((Company)hibernateTemplate.get(Company.class,accjson.getString("companyid")));
            }
            if(accjson.has("parentid")){
                account.setParent((Account) hibernateTemplate.get(Account.class,accjson.getString("parentid")));
            } else {
                account.setParent(null);
            }
            if(accjson.has("depaccountid")){
                account.setDepreciationAccont((Account) hibernateTemplate.get(Account.class,accjson.getString("depaccountid")));
            }
            if(accjson.has("groupid")){
                account.setGroup((Group) hibernateTemplate.get(Group.class,accjson.getString("groupid")));
            }
            if(accjson.has("currencyid")){
                account.setCurrency((KWLCurrency)hibernateTemplate.get(KWLCurrency.class,accjson.getString("currencyid")));
            }
            if(accjson.has("category")){
                account.setCategory((MasterItem) hibernateTemplate.get(MasterItem.class, accjson.getString("category")));
            }
            if (accjson.has("costCenterId")) {
                account.setCostcenter((CostCenter) hibernateTemplate.get(CostCenter.class, accjson.getString("costCenterId")));
            }

            hibernateTemplate.saveOrUpdate(account);
            list.add(account);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updateAccount(JSONObject accjson) throws ServiceException {
        List list = new ArrayList();
        try{
            String accountid = accjson.getString("accountid");
            Account account=new Account();
            account=(Account)hibernateTemplate.get(Account.class, accountid);
            if(account!=null){
                if(accjson.has("name")){
                    account.setName(accjson.getString("name"));
                }
                if(accjson.has("balance")){
                	if(accjson.has("isFixedAsset")){
                		if(accjson.getBoolean("isFixedAsset")){
                			account.setOpeningBalance(0);
                		}
                	}else{
                        account.setOpeningBalance(accjson.getDouble("balance"));
                	}
                }
                if(accjson.has("life")){
                    account.setLife(accjson.getDouble("life"));
                }
                if(accjson.has("salvage")){
                    account.setSalvage(accjson.getDouble("salvage"));
                }
                if(accjson.has("companyid")){
                    account.setCompany((Company)hibernateTemplate.get(Company.class,accjson.getString("companyid")));
                }
                if(accjson.has("parentid")){
                    account.setParent((Account) hibernateTemplate.get(Account.class,accjson.getString("parentid")));
                } else {
                    account.setParent(null);
                }
                if(accjson.has("groupid")){
                    account.setGroup((Group) hibernateTemplate.get(Group.class,accjson.getString("groupid")));
                }
                if(accjson.has("depaccountid")){
                    account.setDepreciationAccont((Account) hibernateTemplate.get(Account.class,accjson.getString("depaccountid")));
                }
                if(accjson.has("currencyid")){
                    account.setCurrency((KWLCurrency)hibernateTemplate.get(KWLCurrency.class,accjson.getString("currencyid")));
                }
                if(accjson.has("category")){
                    account.setCategory((MasterItem) hibernateTemplate.get(MasterItem.class, accjson.getString("category")));
                }
                if (accjson.has("costCenterId")) {
                    account.setCostcenter((CostCenter) hibernateTemplate.get(CostCenter.class, accjson.getString("costCenterId")));
                }
                hibernateTemplate.saveOrUpdate(account);
            }
            list.add(account);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject saveAccount(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try{
            String accountid = (String) dataMap.get("accountid");
            Account account = new Account();
            if (!StringUtil.isNullOrEmpty(accountid)) {
                account = (Account) hibernateTemplate.get(Account.class, accountid);
            }
            if(dataMap.containsKey("name")){
                account.setName((String) dataMap.get("name"));
            }
            if(dataMap.containsKey("balance")){
                account.setOpeningBalance((Double) dataMap.get("balance"));
            }
            if(dataMap.containsKey("life")){
                account.setLife((Double) dataMap.get("life"));
            }
            if(dataMap.containsKey("salvage")){
                account.setSalvage((Double) dataMap.get("salvage"));
            }
            if(dataMap.containsKey("creationdate")){
                account.setCreationDate((Date) dataMap.get("creationdate"));
            }
            if(dataMap.containsKey("companyid")){
                Company company = dataMap.get("companyid")==null?null:(Company)hibernateTemplate.get(Company.class,(String) dataMap.get("companyid"));
                account.setCompany(company);
            }
            if(dataMap.containsKey("parentid")){
                Account paccount = dataMap.get("parentid")==null?null:(Account) hibernateTemplate.get(Account.class,(String) dataMap.get("parentid"));
                account.setParent(paccount);
            }
            if(dataMap.containsKey("groupid")){
                Group group = dataMap.get("groupid")==null?null:(Group) hibernateTemplate.get(Group.class,(String) dataMap.get("groupid"));
                account.setGroup(group);
            }
            if(dataMap.containsKey("depaccountid")){
                Account daccount = dataMap.get("depaccountid")==null?null:(Account) hibernateTemplate.get(Account.class,(String) dataMap.get("depaccountid"));
                account.setDepreciationAccont(daccount);
            }
            if(dataMap.containsKey("currencyid")){
                KWLCurrency currency = dataMap.get("currencyid")==null?null:(KWLCurrency)hibernateTemplate.get(KWLCurrency.class,(String) dataMap.get("currencyid"));
                account.setCurrency(currency);
            }
            if(dataMap.containsKey("category")){
                account.setCategory(dataMap.get("category")==null?null:(MasterItem) hibernateTemplate.get(MasterItem.class, (String)dataMap.get("category")));
            }
            if (dataMap.containsKey("costCenterId")) {
                account.setCostcenter(dataMap.get("costCenterId")==null?null:(CostCenter)hibernateTemplate.get(CostCenter.class, (String) dataMap.get("costCenterId")));
            }
            hibernateTemplate.saveOrUpdate(account);
            list.add(account);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getAccount(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from Account";
        return StringUtil.buildNExecuteQuery(hibernateTemplate, query, requestParams);
    }

    public KwlReturnObject getGroup(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from Group";
        return StringUtil.buildNExecuteQuery(hibernateTemplate, query, requestParams);
    }
    
    public KwlReturnObject getAccounts(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try{
            int start = 0;
            int limit = 15;
            boolean pagingFlag = false;
            if (requestParams.containsKey("start") && requestParams.containsKey("limit") && !StringUtil.isNullOrEmpty(requestParams.get("start").toString())) {
                start = Integer.parseInt(requestParams.get("start").toString());
                limit = Integer.parseInt(requestParams.get("limit").toString());
                pagingFlag = true;
            }
            ArrayList params=new ArrayList();
            String[] groups=(String[]) requestParams.get("group");

            String condition=(requestParams.get("ignore")==null?"":" not ");
            String[] nature=(String[])requestParams.get("nature");
            String companyid=(String)requestParams.get("companyid");
            boolean deleted =Boolean.parseBoolean((String)requestParams.get("deleted"));
            boolean nondeleted =Boolean.parseBoolean((String)requestParams.get("nondeleted"));
            if(groups!=null){
                String qMarks="?";
                params.add("null");
                 for(int i=0;i<groups.length;i++){
                    qMarks+=",?";
                    params.add(groups[i]);
                }
                condition=" and ac.group.ID "+condition+" in ("+qMarks+") ";
            }
            else if(nature!=null){
                String qMarks="?";
                params.add(5);//not a nature
                 for(int i=0;i<nature.length;i++){
                    qMarks+=",?";
                    params.add(Integer.parseInt(nature[i]));
                }
                condition+=" and ac.group.nature "+condition+" in ("+qMarks+") ";
            }
            boolean ignoreCustomers=requestParams.get("ignorecustomers")!=null;
            boolean ignoreVendors=requestParams.get("ignorevendors")!=null;
            if(ignoreCustomers || ignoreVendors) {
                String qMarks="";
                if(ignoreCustomers){
                    qMarks="?";
                    params.add(Group.ACCOUNTS_RECEIVABLE);
                }
                if(ignoreVendors) {
                    if(!StringUtil.isNullOrEmpty(qMarks)) {
                        qMarks+=",?";
                    } else {
                        qMarks="?";
                    }
                    params.add(Group.ACCOUNTS_PAYABLE);
                }
                condition += " and ac.group.ID not in ("+qMarks+") ";
            }

            String costCenterId = (String) requestParams.get("costCenterId");
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and ac.costcenter.ID=?";
            }

            if(requestParams.containsKey("ss") && requestParams.get("ss") != null) {
                String ss=requestParams.get("ss").toString();
                if(!StringUtil.isNullOrEmpty(ss)){
                    String[] searchcol = new String[]{"ac.name"};
                    StringUtil.insertParamSearchString(params, ss, 1);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    condition +=searchQuery;
                }
            }
            if (nondeleted)
                condition += " and ac.deleted=false ";
            else if (deleted)
                condition += " and ac.deleted=true ";
            params.add(companyid);
            String query="from Account ac where ac.parent is null "+condition+" and ac.company.companyID=? order by ac.name";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query,params.toArray());
            int totalCount = list.size();
            if(pagingFlag) {
                list = HibernateUtil.executeQueryPaging(hibernateTemplate, query, params.toArray(), new Integer[]{start, limit});
            }
            List resultlist = getAccountArrayList(list, requestParams);
            result = new KwlReturnObject(true, null, null, resultlist, totalCount);
        }catch(Exception ex){
            result = new KwlReturnObject(false,"accAccountDAOImpl.getAccounts:"+ex.getMessage(), null, null ,0);
        }
        return result;
    }

    public KwlReturnObject getAccountsForCombo(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try{
            ArrayList params=new ArrayList();
            String[] groups=(String[]) requestParams.get("group");

            String condition=(requestParams.get("ignore")==null?"":" not ");
            String[] nature=(String[])requestParams.get("nature");
            String companyid=(String)requestParams.get("companyid");
            boolean deleted =Boolean.parseBoolean((String)requestParams.get("deleted"));
            boolean nondeleted =Boolean.parseBoolean((String)requestParams.get("nondeleted"));
            params.add(companyid);
            
            if(groups!=null){
                String qMarks="?";
                params.add("null");
                 for(int i=0;i<groups.length;i++){
                    qMarks+=",?";
                    params.add(groups[i]);
                }
                condition=" and ac.group.ID "+condition+" in ("+qMarks+") ";
            }
            else if(nature!=null){
                String qMarks="?";
                params.add(5);//not a nature
                 for(int i=0;i<nature.length;i++){
                    qMarks+=",?";
                    params.add(Integer.parseInt(nature[i]));
                }
                condition+=" and ac.group.nature "+condition+" in ("+qMarks+") ";
            }
            boolean ignoreCustomers=requestParams.get("ignorecustomers")!=null;
            boolean ignoreVendors=requestParams.get("ignorevendors")!=null;
            boolean ignoreAssets = requestParams.get("ignoreAssets")!=null;
            if(ignoreCustomers || ignoreVendors || ignoreAssets) {
                String qMarks="";
                if(ignoreCustomers){
                    qMarks="?";
                    params.add(Group.ACCOUNTS_RECEIVABLE);
                }
                if(ignoreVendors) {
                    if(!StringUtil.isNullOrEmpty(qMarks)) {
                        qMarks+=",?";
                    } else {
                        qMarks="?";
                    }
                    params.add(Group.ACCOUNTS_PAYABLE);
                }
                if(ignoreAssets) {
                    if(!StringUtil.isNullOrEmpty(qMarks)) {
                        qMarks+=",?";
                    } else {
                        qMarks="?";
                    }
                    params.add(Group.FIXED_ASSETS);
                }
                condition += " and ac.group.ID not in ("+qMarks+") ";
            }

            if (nondeleted)
                condition += " and ac.deleted=false ";
            else if (deleted)
                condition += " and ac.deleted=true ";

            String query="from Account ac where ac.company.companyID=? "+condition+" order by ac.name";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query,params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        }catch(Exception ex){
            result = new KwlReturnObject(false,"accAccountDAOImpl.getAccounts:"+ex.getMessage(), null, null ,0);
        }
        return result;
    }

    public ArrayList getAccountArrayList(List list, HashMap<String, Object> requestParams) throws ServiceException{
        ArrayList resultlist = new ArrayList();
            boolean ignoreCustomers=requestParams.get("ignorecustomers")!=null;
            boolean ignoreVendors=requestParams.get("ignorevendors")!=null;
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");
            String customerCpath = ConfigReader.getinstance().get("Customer");
            String vendorCpath = ConfigReader.getinstance().get("Vendor");
            boolean deleted =Boolean.parseBoolean((String)requestParams.get("deleted"));
            boolean nondeleted =Boolean.parseBoolean((String)requestParams.get("nondeleted"));
            String currencyid=(String)requestParams.get("currencyid");
            KWLCurrency currency = (KWLCurrency)hibernateTemplate.get(KWLCurrency.class, currencyid);
            
            Iterator itr = list.iterator();
            int level=0;
            while (itr.hasNext()) {
                Object listObj = itr.next();
                Account account = (Account) listObj;
                if(excludeaccountid!=null&&account.getID().equals(excludeaccountid)) continue;
                if((includeparentid!=null&&(!account.getID().equals(includeparentid)||(account.getParent()!=null&&!account.getParent().getID().equals(includeparentid))))) continue;
                else if((includeaccountid!=null&&!account.getID().equals(includeaccountid))) continue;

//                Customer c=(Customer)hibernateTemplate.get(Customer.class,account.getID());
                Object c = AccountingHandlerDAOImpl.getObject(hibernateTemplate, customerCpath, account.getID());
                if(ignoreCustomers&&account.getGroup().getID().equals(Group.ACCOUNTS_RECEIVABLE)){
                    if(c!=null)continue;
                }

//                Vendor v=(Vendor)hibernateTemplate.get(Vendor.class,account.getID());
                Object v = AccountingHandlerDAOImpl.getObject(hibernateTemplate, vendorCpath, account.getID());
                if(ignoreVendors&&account.getGroup().getID().equals(Group.ACCOUNTS_PAYABLE)){
                    if(v!=null)continue;
                }

                Object tmplist[] = new Object[8];
                tmplist[0]=listObj;
                tmplist[1]=c;
                tmplist[2]=v;
                tmplist[3]=level;
                resultlist.add(tmplist);
                tmplist[4]=getChildAccounts(account, resultlist, level, excludeaccountid, includeaccountid, ignoreCustomers, ignoreVendors, currency, customerCpath, vendorCpath,nondeleted,deleted);
                tmplist[5]=currency;
                tmplist[6]=null;//Parent Account
                tmplist[7]=isPosted(account);
            }
            return resultlist;
    }
    public boolean getChildAccounts(Account account, List resultlist, int level, String excludeaccountid, String includeaccountid, boolean ignoreCustomers, boolean ignoreVendors, KWLCurrency currency, String customerCpath, String vendorCpath,boolean nondeleted,boolean deleted) throws ServiceException {
        boolean leaf=true;
        Iterator<Account> itr = new TreeSet(account.getChildren()).iterator();
        level++;
        while(itr.hasNext()) {
            Object listObj = itr.next();
            Account child = (Account) listObj;
            if((excludeaccountid!=null&&child.getID().equals(excludeaccountid))||(child.isDeleted()&&nondeleted)||(!child.isDeleted()&&deleted)) continue;
            if((includeaccountid!=null&&!child.getID().equals(includeaccountid)||(child.isDeleted()&&nondeleted)||(!child.isDeleted()&&deleted))) continue;
            
//            Customer c=(Customer)hibernateTemplate.get(Customer.class,child.getID());
            Object c = AccountingHandlerDAOImpl.getObject(hibernateTemplate, customerCpath, child.getID());
            if(ignoreCustomers&&child.getGroup().getID().equals(Group.ACCOUNTS_RECEIVABLE)){
                if(c!=null)continue;
            }

//            Vendor v=(Vendor)hibernateTemplate.get(Vendor.class,child.getID());
            Object v = AccountingHandlerDAOImpl.getObject(hibernateTemplate, vendorCpath, child.getID());
            if(ignoreVendors&&child.getGroup().getID().equals(Group.ACCOUNTS_PAYABLE)){
                if(v!=null)continue;
            }
            leaf=false;

            Object tmplist[] = new Object[8];
            tmplist[0]=listObj;
            tmplist[1]=c;
            tmplist[2]=v;
            tmplist[3]=level;
            resultlist.add(tmplist);
            tmplist[4]=getChildAccounts(child, resultlist, level, excludeaccountid, excludeaccountid, ignoreCustomers, ignoreVendors, currency, customerCpath, vendorCpath,nondeleted,deleted);
            tmplist[5]=currency;
            tmplist[6]=account;//Parent Account
            tmplist[7]=isPosted(child);
        }
        return leaf;
    }

    public KwlReturnObject getGroups(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try{
            String groupid = (String) requestParams.get("groupid");
            boolean ignoreCustomer = StringUtil.getBoolean((String) requestParams.get("ignorecustomers"));
            boolean ignoreVendor = StringUtil.getBoolean((String) requestParams.get("ignorevendors"));
            String companyid = (String) requestParams.get("companyid");
            String[] groups = (String[]) requestParams.get("group");
            String ignoreGrp = (String) requestParams.get("ignore");
            String[] nature = (String[]) requestParams.get("nature");

            String condition="";
            ArrayList params = new ArrayList();
            ignoreGrp = (ignoreGrp == null ? "" : " not ");
            if (groups != null) {
                String qMarks = "?";
                params.add("null");
                for (int i = 0; i < groups.length; i++) {
                    qMarks += ",?";
                    params.add(groups[i]);
                }
                condition = " and ID " + ignoreGrp + " in (" + qMarks + ") ";
            }
             else if(nature!=null){
                String qMarks="?";
                params.add(5);//not a nature
                 for(int i=0;i<nature.length;i++){
                    qMarks+=",?";
                    params.add(Integer.parseInt(nature[i]));
                }
                condition=" and nature in ("+qMarks+") ";
             }
            params.add(companyid);
            if(ignoreCustomer)
                condition+=" and ID != '"+Group.ACCOUNTS_RECEIVABLE+"'";
            if(ignoreVendor)
                condition+=" and ID != '"+Group.ACCOUNTS_PAYABLE+"'";
            String query="from Group where deleted=false and parent is null "+condition+" and (company is null or company.companyID=?)  order by name";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
            Iterator itr = list.iterator();
            List resultlist = new ArrayList();
            int level=0;
            while (itr.hasNext()) {
                Object listObj = itr.next();
                Group group = (Group) listObj;
                if(group.getID().equals(groupid)) continue;

                Object tmplist[] = new Object[4]; //[0:List, 1:level, 2:leaf, 3:parent group]
                tmplist[0]=listObj;
                tmplist[1]=level;
                resultlist.add(tmplist);
                tmplist[2]=getChildGroups(group, resultlist, level, groupid, companyid);
                tmplist[3]=null;//parent group

            }
            result = new KwlReturnObject(true, null, null, resultlist, resultlist.size());
        } catch(Exception ex){
            result = new KwlReturnObject(false,"accCustomerDAOImpl.getGroups:"+ex.getMessage(), null, null ,0);
        }
        return result;
    }

    public boolean getChildGroups(Group group, List resultlist, int level, String groupid, String companyid) {
        boolean leaf=true;
        Iterator<Group> itr = new TreeSet(group.getChildren()).iterator();
        level++;
        while(itr.hasNext()) {
            Object listObj = itr.next();
            Group child = (Group) listObj;
            Company company=child.getCompany();
            if((company!=null&&!company.getCompanyID().equals(companyid))||child.getID().equals(groupid)||child.isDeleted()) continue;
            leaf=false;
            
            Object tmplist[] = new Object[4]; //[0:List, 1:level, 2:leaf, 3:parent group]
            tmplist[0]=listObj;
            tmplist[1]=level;
            resultlist.add(tmplist);
            tmplist[2]=getChildGroups(child, resultlist, level, groupid, companyid);
            tmplist[3]=group;//parent group
        }
        return leaf;
    }

    public void updateChildrenAccount(Account account) throws ServiceException {
        Set<Account> children=account.getChildren();
        if(children==null) return;
        Iterator<Account> itr=children.iterator();
        while(itr.hasNext()){
            Account child=itr.next();
            child.setGroup(account.getGroup());
            updateChildrenAccount(child);
        }
    }

    public KwlReturnObject deleteAccount(HashMap request, String companyid)throws ServiceException {
        KwlReturnObject result=null;
        String selQuery="";
        ArrayList params1 = new ArrayList();
        ArrayList params2 = new ArrayList();
        ArrayList params3 = new ArrayList();
        try{
            JSONArray jArr = new JSONArray((String)request.get("data"));
            String qMarks = "";
            Company company = (Company) hibernateTemplate.get(Company.class, companyid);
            for (int j = 0; j<5; j++) {
                qMarks = "";
                for (int i = 0; i < jArr.length(); i++){
                    JSONObject jobj = jArr.getJSONObject(i);
                    if (!StringUtil.isNullOrEmpty(jobj.getString("accid"))) {
                        if(j==0)
                            params1.add(jobj.getString("accid"));
                        if(j<=1)
                            params3.add(jobj.getString("accid"));
                        params2.add(jobj.getString("accid"));
                        qMarks += "?,";
                    }
                }
                qMarks=qMarks.substring(0,Math.max(0, qMarks.length()-1));
    
            }
            params1.add(company.getCompanyID());
            params2.add(company.getCompanyID());
            params3.add(company.getCompanyID());
            selQuery = "from JournalEntryDetail jed where account.ID in( "+qMarks +") and jed.company.companyID=?";
            List list = HibernateUtil.executeQuery(hibernateTemplate, selQuery, params1.toArray());
            int count = list.size();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
            }
            selQuery = "from Product pr where (purchaseAccount.ID in ( " + qMarks + ") or salesAccount.ID in ( " + qMarks + ") ) and pr.company.companyID=?";
            list = HibernateUtil.executeQuery(hibernateTemplate, selQuery, params3.toArray());
            count = list.size();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Account Preferences. So it cannot be deleted.");
            }
            selQuery = "from CompanyAccountPreferences acp where (discountGiven.ID in ( " + qMarks + ") or discountReceived.ID in ( " + qMarks + ") or shippingCharges.ID in ( " + qMarks + ")  or otherCharges.ID in ( " + qMarks + ") or cashAccount.ID in ( " + qMarks + ")) and acp.company.companyID=?";// (discountGiven.ID in ( "+qMarks +")
            list = HibernateUtil.executeQuery(hibernateTemplate, selQuery, params2.toArray());
            count = list.size();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Product(s). So it cannot be deleted.");
            }
            selQuery = "from PaymentMethod pm where account.ID in ( " + qMarks + ")  and pm.company.companyID=?";
            list = HibernateUtil.executeQuery(hibernateTemplate, selQuery, params1.toArray());
            count = list.size();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Term(s). So it cannot be deleted.");
            }
            selQuery = "from Tax t where account.ID in ( " + qMarks + ")  and t.company.companyID=?";
            list = HibernateUtil.executeQuery(hibernateTemplate, selQuery, params1.toArray());
            count = list.size();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Tax(s). So it cannot be deleted.");
            }

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("accid")) == false) {
                    if (jobj.getDouble("openbalance") != 0) {
                        throw new AccountingException("Selected record(s) is having the Opening Balance. So it cannot be deleted");
                    } else {
                        Account account = (Account) hibernateTemplate.get(Account.class, jobj.getString("accid"));
                        //account.setDeleted(true);
                        // hibernateTemplate.update(account);
                      result =   deleteAccount(account.getID(),companyid);
                       
                    }
                }
            }
        } catch (AccountingException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch(ServiceException ex){
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Selected record(s) is currently used in the transaction(s).", ex);
        }
        return result;
    }
    public KwlReturnObject deleteAccount(String accountid, String companyid) throws ServiceException {
        String delQuery = "delete from Account a where a.ID=? and a.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{accountid,companyid});
        return new KwlReturnObject(true, "Account has been deleted successfully.", null, null, numRows);
    }
    public KwlReturnObject deleteAccount(String accountid, boolean flag) throws ServiceException {
        List list = new ArrayList();
        try {
            Account account = (Account) hibernateTemplate.get(Account.class, accountid);
            account.setDeleted(flag);
            hibernateTemplate.update(account);
            list.add(account);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Account has been deleted successfully", null, list, list.size());
    }
    
    public KwlReturnObject deleteGroup(String groupid) throws ServiceException {
        List list = new ArrayList();
        try {
            Group group = (Group) hibernateTemplate.get(Group.class, groupid);
            group.setDeleted(true);
            hibernateTemplate.update(group);
            list.add(group);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Account Type has been deleted successfully", null, list, list.size());
    }

    public KwlReturnObject addGroup(JSONObject groupjson) throws ServiceException {
        List list = new ArrayList();
        try{
            Group group = new Group();
            group.setDeleted(false);

            if(groupjson.has("name")){
                group.setName(groupjson.getString("name"));
            }
            if(groupjson.has("nature")){
                group.setNature(groupjson.getInt("nature"));
            }
            if(groupjson.has("disporder")){
                group.setDisplayOrder(groupjson.getInt("disporder"));
            }
            if(groupjson.has("affectgp")){
                group.setAffectGrossProfit(groupjson.getBoolean("affectgp"));
            }
            if(groupjson.has("parentid")){
                group.setParent((Group) hibernateTemplate.get(Group.class, groupjson.getString("parentid")));
            }
            if(groupjson.has("companyid")){
                group.setCompany((Company) hibernateTemplate.get(Company.class, groupjson.getString("companyid")));
            }
            hibernateTemplate.saveOrUpdate(group);
            list.add(group);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updateGroup(JSONObject groupjson) throws ServiceException {
        List list = new ArrayList();
        try{
            String groupid = groupjson.getString("groupid");
            Group group = (Group)hibernateTemplate.get(Group.class, groupid);
            if (group != null) {
                if(groupjson.has("name")){
                    group.setName(groupjson.getString("name"));
                }
                if(groupjson.has("nature")){
                    group.setNature(groupjson.getInt("nature"));
                }
                if(groupjson.has("disporder")){
                    group.setDisplayOrder(groupjson.getInt("disporder"));
                }
                if(groupjson.has("affectgp")){
                    group.setAffectGrossProfit(groupjson.getBoolean("affectgp"));
                }
                if(groupjson.has("parentid")){
                    group.setParent((Group) hibernateTemplate.get(Group.class, groupjson.getString("parentid")));
                }
                if(groupjson.has("companyid")){
                    group.setCompany((Company) hibernateTemplate.get(Company.class, groupjson.getString("companyid")));
                }
                hibernateTemplate.saveOrUpdate(group);
            }
            list.add(group);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public void updateChildrenGroup(Group group) throws ServiceException {
        try{
            Set<Group> children=group.getChildren();
            if(children==null) return;
            Iterator<Group> itr=children.iterator();
            while(itr.hasNext()){
                Group child=itr.next();
                child.setNature(group.getNature());
                child.setAffectGrossProfit(group.isAffectGrossProfit());
                hibernateTemplate.update(child);
                updateChildrenGroup(child);
            }
        } catch(Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public KwlReturnObject getMaxGroupDisplayOrder() throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String query = "select max(displayOrder) from Group";
        list = HibernateUtil.executeQuery(hibernateTemplate, query);
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }


    public KwlReturnObject copyAccounts(String companyid, String currencyid, String companyType) throws ServiceException {
        List returnlist = new ArrayList();
        HashMap hm = new HashMap();
        try {
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, StorageHandler.getDefaultCurrencyID());
            KWLCurrency currency = (KWLCurrency) hibernateTemplate.get(KWLCurrency.class, currencyid);
            Company company = (Company) hibernateTemplate.get(Company.class, companyid);
            String query = "from DefaultAccount where parent is null ";
            List list = null;

            if(StringUtil.isNullOrEmpty(companyType)) {
                query += "and companytype is null";
                list = HibernateUtil.executeQuery(hibernateTemplate, query);
            } else {
                query += "and companytype=?";
                list = HibernateUtil.executeQuery(hibernateTemplate, query, companyType);
            }

            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                DefaultAccount defaultAccount = (DefaultAccount) iter.next();
                Account account = new Account();
                account.setCompany(company);
                account.setDeleted(false);
                account.setGroup(defaultAccount.getGroup());
                account.setName(defaultAccount.getName());
                account.setCurrency(currency);
                account.setCreationDate(new Date());
                account.setPresentValue(defaultAccount.getPresentValue());
                account.setOpeningBalance(defaultAccount.getOpeningBalance());
                hibernateTemplate.save(account);
                hm.put(defaultAccount, account);
                hm.putAll(saveChildren(account, defaultAccount));
            }
        }  catch (Exception ex) {
            throw ServiceException.FAILURE("copyAccounts : "+ex.getMessage(), ex);
        }
        returnlist.add(hm);
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    public KwlReturnObject getDefaultAccount(String companyType) throws ServiceException {
        String query = "from DefaultAccount where ";
        List returnlist = new ArrayList();
        if(StringUtil.isNullOrEmpty(companyType)) {
            query += " companytype is null";
            returnlist = HibernateUtil.executeQuery(hibernateTemplate, query);
        } else {
            query += " companytype=?";
            returnlist = HibernateUtil.executeQuery(hibernateTemplate, query, companyType);
        }
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    private HashMap saveChildren(Account account, DefaultAccount defaultAccount) throws ServiceException {
        HashMap hm=new HashMap();
        try {
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, StorageHandler.getDefaultCurrencyID());
            Set set = defaultAccount.getChildren();
            Iterator iter = set.iterator();
            while (iter.hasNext()) {
                DefaultAccount da = (DefaultAccount) iter.next();
                Account acc = new Account();
                acc.setCompany(account.getCompany());
                acc.setDeleted(false);
                acc.setGroup(account.getGroup());
                acc.setName(da.getName());
                acc.setOpeningBalance(da.getOpeningBalance());
                acc.setParent(account);
                acc.setCurrency(account.getCurrency());
                hibernateTemplate.save(acc);
                hm.put(da, acc);
                hm.putAll(saveChildren(account, defaultAccount));
            }
        }  catch (Exception ex) {
            throw ServiceException.FAILURE("saveChildren : "+ex.getMessage(), ex);
        }
        return hm;
    }

    public boolean isPosted(Account account) throws ServiceException{
        String query = "from DepreciationDetail where account.ID=? and company.companyID=?";
        List list = HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{account.getID(),account.getCompany().getCompanyID()});
        return !list.isEmpty();
    }

    @Override
    public boolean isChild(String ParentID, String childID) throws ServiceException{
        String query = "select id from account where parent=? and id=?";
        List list = HibernateUtil.executeSQLQuery(hibernateTemplate.getSessionFactory().getCurrentSession(), query, new Object[]{ParentID, childID});
        return !list.isEmpty();
    }
    
    @Override
    public List isChildorGrandChild(String childID) throws ServiceException{
        String query = "from Account where ID=? and parent != null";
        List Result = HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{childID});
        return Result;
    }
    
    @Override
    public List isChildforDelete(String childID) throws ServiceException{
        String query = "from Account where parent.ID=?";
        List Result = HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{childID});
        return Result;
    }

}
