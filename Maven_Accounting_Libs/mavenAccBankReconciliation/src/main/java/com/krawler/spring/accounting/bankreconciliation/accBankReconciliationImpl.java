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
package com.krawler.spring.accounting.bankreconciliation;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.BankReconciliation;
import com.krawler.hql.accounting.BankReconciliationDetail;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
public class accBankReconciliationImpl implements accBankReconciliationDAO {
    private HibernateTemplate hibernateTemplate;
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

    public KwlReturnObject addBankReconciliation(HashMap<String, Object> brMap) throws ServiceException {
        List list = new ArrayList();
        try {
            BankReconciliation br = new BankReconciliation();
            br = buildBankReconciliation(br, brMap);
            hibernateTemplate.save(br);
            list.add(br);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addBankReconciliation : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Bank Reconciliation has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateBankReconciliation(HashMap<String, Object> brMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = (String) brMap.get("id");
            BankReconciliation br = (BankReconciliation) hibernateTemplate.get(BankReconciliation.class, id);
            if(br != null) {
                br = buildBankReconciliation(br, brMap);
                hibernateTemplate.saveOrUpdate(br);
            }
            list.add(br);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateBankReconciliation : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Bank Reconciliation has been updated successfully", null, list, list.size());
    }

    public BankReconciliation buildBankReconciliation(BankReconciliation br, HashMap<String, Object> brMap) {
        if(brMap.containsKey("startdate")) {
            br.setStartDate((Date) brMap.get("startdate"));
        }
        if(brMap.containsKey("enddate")) {
            br.setEndDate((Date) brMap.get("enddate"));
        }
        if(brMap.containsKey("clearingamount")) {
            br.setClearingAmount((Double) brMap.get("clearingamount"));
        }
        if(brMap.containsKey("endingamount")) {
            br.setEndingAmount((Double) brMap.get("endingamount"));
        }
        if (brMap.containsKey("accountid")) {
            Account account = brMap.get("accountid")==null?null:(Account)hibernateTemplate.get(Account.class, (String) brMap.get("accountid"));
            br.setAccount(account);
        }
        if (brMap.containsKey("companyid")) {
            Company company = brMap.get("companyid")==null?null:(Company)hibernateTemplate.get(Company.class, (String) brMap.get("companyid"));
            br.setCompany(company);
        }
        if (brMap.containsKey("brdetails")) {
            br.setDetails((Set<BankReconciliationDetail>) brMap.get("brdetails"));
        }
        return br;
    }

    public KwlReturnObject addBankReconciliationDetail(HashMap<String, Object> brdMap) throws ServiceException {
        List list = new ArrayList();
        try {
            BankReconciliationDetail brd = new BankReconciliationDetail();
            brd = buildBankReconciliationDetail(brd, brdMap);
            hibernateTemplate.save(brd);
            list.add(brd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addBankReconciliationDetail : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Bank Reconciliation Detail has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateBankReconciliationDetail(HashMap<String, Object> brdMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = (String) brdMap.get("id");
            BankReconciliationDetail brd = (BankReconciliationDetail) hibernateTemplate.get(BankReconciliationDetail.class, id);
            if(brd != null) {
                brd = buildBankReconciliationDetail(brd, brdMap);
                hibernateTemplate.saveOrUpdate(brd);
            }
            list.add(brd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateBankReconciliationDetail : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Bank Reconciliation Detail has been updated successfully", null, list, list.size());
    }

    public BankReconciliationDetail buildBankReconciliationDetail(BankReconciliationDetail brd, HashMap<String, Object> brdMap) {
        if(brdMap.containsKey("amount")) {
            brd.setAmount((Double) brdMap.get("amount"));
        }
        if(brdMap.containsKey("accountname")) {
            brd.setAccountnames((String) brdMap.get("accountname"));
        }
        if(brdMap.containsKey("debit")) {
            brd.setDebit((Boolean) brdMap.get("debit"));
        }
        if (brdMap.containsKey("brid")) {
            BankReconciliation br = brdMap.get("brid")==null?null:(BankReconciliation)hibernateTemplate.get(BankReconciliation.class, (String) brdMap.get("brid"));
            brd.setBankReconciliation(br);
        }
        if (brdMap.containsKey("jeid")) {
            JournalEntry je = brdMap.get("jeid")==null?null:(JournalEntry)hibernateTemplate.get(JournalEntry.class, (String) brdMap.get("jeid"));
            brd.setJournalEntry(je);
        }
        if (brdMap.containsKey("companyid")) {
            Company company = brdMap.get("companyid")==null?null:(Company)hibernateTemplate.get(Company.class, (String) brdMap.get("companyid"));
            brd.setCompany(company);
        }
        return brd;
    }

    public KwlReturnObject getBRfromJE(String jeid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from BankReconciliationDetail where journalEntry.ID=? and bankReconciliation.deleted=false and company.companyID=?";
        list = HibernateUtil.executeQuery(hibernateTemplate, q, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getBankReconciliation(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from BankReconciliation where deleted=false ";

        if (filterParams.containsKey("jeid")) {
            condition += " and journalEntry.ID=?";
            params.add(filterParams.get("jeid"));
        }
        if (filterParams.containsKey("accountid")) {
            condition += " and account.ID=?";
            params.add(filterParams.get("accountid"));
        }
        if (filterParams.containsKey("companyid")) {
            condition += " and company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        query += condition;
//        query="from BankReconciliationDetail where journalEntry.ID=? and bankReconciliation.deleted=false and company.companyID=?";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject deleteBankReconciliation(String brid, String companyid) throws ServiceException {
        BankReconciliation br = (BankReconciliation) hibernateTemplate.get(BankReconciliation.class, brid);
        br.setDeleted(true);
        hibernateTemplate.saveOrUpdate(br);
        return new KwlReturnObject(true, "Bank Reconciliation has been deleted successfully.", null, null, 1);
    }
}
