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
package com.krawler.esp.handlers;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.KWLTimeZone;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.DefaultAccount;
import com.krawler.hql.accounting.DefaultCompanyAccountPreferences;
import com.krawler.hql.accounting.DefaultMasterItem;
import com.krawler.hql.accounting.DefaultPaymentMethod;
import com.krawler.hql.accounting.DefaultTax1099Category;
import com.krawler.hql.accounting.Term;
import com.krawler.hql.accounting.DefaultTerm;
import com.krawler.hql.accounting.DefaultUnitOfMeasure;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.PaymentMethod;
import com.krawler.hql.accounting.Tax1099Category;
import com.krawler.hql.accounting.UnitOfMeasure;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;

public class NewCompanyHandler {
    public static void setupNewCompany(Session session, HttpServletRequest request, Company company, User companyAdmin) throws SessionExpiredException {
		try {
            copyTerms(session,company);
            copyUOM(session,company);
            HashMap hmAcc=copyAccounts(session,request,company);
            copyPaymentMethods(session, company, hmAcc);
            copyMasterItems(session, company);
            setAccountPreferences(session, company, hmAcc);
            copyTax1099Category(session, company);
        } catch (ServiceException e) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, e.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private static void copyTerms(Session session, Company company) throws ServiceException {
        try {
            String query = "from DefaultTerm";
            List list = HibernateUtil.executeQuery(session, query);
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                DefaultTerm dt = (DefaultTerm) iter.next();
                Term term = new Term();
                term.setCompany(company);
                term.setTermdays(dt.getTermdays());
                term.setTermname(dt.getTermname());
                session.save(term);
            }
        } catch (HibernateException ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }  catch (Exception ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private static void copyUOM(Session session, Company company) throws ServiceException {
        try {
            String query = "from DefaultUnitOfMeasure";
            List list = HibernateUtil.executeQuery(session, query);
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                DefaultUnitOfMeasure defaultUOM = (DefaultUnitOfMeasure) iter.next();
                UnitOfMeasure uom = new UnitOfMeasure();
                uom.setCompany(company);
                uom.setName(defaultUOM.getName());
                uom.setType(defaultUOM.getType());
                session.save(uom);
            }
        } catch (HibernateException ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }  catch (Exception ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }

    }

    private static HashMap copyAccounts(Session session,HttpServletRequest request, Company company) throws ServiceException, SessionExpiredException {
        HashMap hm = new HashMap();
        try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, StorageHandler.getDefaultCurrencyID());
            String query = "from DefaultAccount where parent is null and companytype is null";
            List list = HibernateUtil.executeQuery(session, query);
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
                session.save(account);
                hm.put(defaultAccount, account);
                hm.putAll(saveChildren(session,request, account, defaultAccount));
            }
        } catch (HibernateException ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }  catch (Exception ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }
        return hm;
    }

    private static HashMap saveChildren(Session session,HttpServletRequest request, Account account, DefaultAccount defaultAccount) throws ServiceException, SessionExpiredException {
        HashMap hm=new HashMap();
        try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, StorageHandler.getDefaultCurrencyID());
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
                acc.setCurrency(currency);
                session.save(acc);
                hm.put(da, acc);
                hm.putAll(saveChildren(session,request, acc, da));
            }
        } catch (HibernateException ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }  catch (Exception ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }
        return hm;
    }
    
    private static void copyPaymentMethods(Session session,Company company, HashMap hm) throws ServiceException {
        try {
            String query = "from DefaultPaymentMethod";
            List list = HibernateUtil.executeQuery(session, query);
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                DefaultPaymentMethod defaultPM = (DefaultPaymentMethod) iter.next();
                PaymentMethod pm = new PaymentMethod();
                pm.setCompany(company);
                pm.setDetailType(defaultPM.getDetailType());
                pm.setMethodName(defaultPM.getMethodName());
                pm.setAccount((Account) hm.get(defaultPM.getAccount()));
                session.save(pm);
            }
        } catch (HibernateException ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }  catch (Exception ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private static void copyMasterItems(Session session, Company company) throws ServiceException {
        try {
            String query = "from DefaultMasterItem";
            List list = HibernateUtil.executeQuery(session, query);
            Iterator iter = list.iterator();
            while(iter.hasNext()){
                DefaultMasterItem defaultMasterItem = (DefaultMasterItem) iter.next();
                MasterItem masterItem = new MasterItem();
                masterItem.setCompany(company);
                masterItem.setMasterGroup(defaultMasterItem.getMasterGroup());
                masterItem.setValue(defaultMasterItem.getValue());
                session.save(masterItem);
            }
        } catch (HibernateException ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }  catch (Exception ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private static void setAccountPreferences(Session session, Company company, HashMap hm) throws ServiceException {
        try {
            String query = "from DefaultCompanyAccountPreferences order by ID";
            DateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
            KWLTimeZone tz=(KWLTimeZone)session.get(KWLTimeZone.class, StorageHandler.getDefaultTimeZoneID());
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"+tz.getDifference()));
            Date curDate = new Date();
            try {
                curDate = sdf.parse(sdf.format(curDate));
            } catch (ParseException ex) {
                Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
            }
            List list = HibernateUtil.executeQuery(session, query);
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                DefaultCompanyAccountPreferences dcap = (DefaultCompanyAccountPreferences) iter.next();
                CompanyAccountPreferences cap = new CompanyAccountPreferences();
                cap.setBookBeginningFrom(curDate);
                cap.setFinancialYearFrom(curDate);
                cap.setCompany(company);
                cap.setCashAccount((Account) hm.get(dcap.getCashAccount()));
                cap.setDiscountGiven((Account) hm.get(dcap.getDiscountGiven()));
                cap.setDiscountReceived((Account) hm.get(dcap.getDiscountReceived()));
//                cap.setShippingCharges((Account) hm.get(dcap.getShippingCharges()));
                cap.setOtherCharges((Account) hm.get(dcap.getOtherCharges()));
                cap.setForeignexchange((Account) hm.get(dcap.getForeignExchange()));
                cap.setDepereciationAccount((Account) hm.get(dcap.getDepereciationAccount()));
                session.save(cap);
                break;
            }
        } catch (HibernateException ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }
    }
     private static void copyTax1099Category(Session session, Company company) throws ServiceException {
        try {
            String query = "from DefaultTax1099Category";
            List<DefaultTax1099Category> list = HibernateUtil.executeQuery(session, query);
            if (list != null){
                for ( DefaultTax1099Category defaulttax: list){
                    Tax1099Category tax = new Tax1099Category();
                    tax.setCompany(company);
                    tax.setCategory(defaulttax.getCategory());
                    tax.setDeleted(defaulttax.isDeleted());
                    tax.setSrno(defaulttax.getSrno());
                    tax.setThresholdValue(defaulttax.getThresholdValue());
                    session.save(tax);
                }
            }
        }  catch (Exception ex) {
            throw ServiceException.FAILURE("copyTax1099Category : "+ex.getMessage(), ex);
        }
    }
}
