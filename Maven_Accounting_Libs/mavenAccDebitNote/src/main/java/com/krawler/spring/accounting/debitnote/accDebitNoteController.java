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

package com.krawler.spring.accounting.debitnote;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BillingDebitNote;
import com.krawler.hql.accounting.BillingDebitNoteDetail;
import com.krawler.hql.accounting.BillingDebitNoteDiscount;
import com.krawler.hql.accounting.BillingGoodsReceiptDetail;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.DebitNote;
import com.krawler.hql.accounting.DebitNoteDetail;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.GoodsReceiptDetail;
import com.krawler.hql.accounting.Group;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accDebitNoteController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accDebitNoteDAO accDebitNoteobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accTaxDAO accTaxObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private String successView;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccDiscountDAO(accDiscountDAO accDiscountobj){
        this.accDiscountobj = accDiscountobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public String getSuccessView() {
        return successView;
    }

    public ModelAndView saveDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PaymentMethods_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try{
            DebitNote debitnote = saveDebitNote(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.debitN.save", null, RequestContextUtils.getLocale(request));   //"Debit Note has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveBillingDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PaymentMethods_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try{
            BillingDebitNote debitnote = saveBillingDebitNote(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.debitN.save", null, RequestContextUtils.getLocale(request));   //"Debit Note has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public DebitNote saveDebitNote(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        DebitNote debitnote = null;
        List list = new ArrayList();
        KwlReturnObject result;
        try {
            double externalCurrencyRate= StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            DateFormat df = authHandler.getDateFormatter(request);
            HashMap<String,Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", currencyid);
            GlobalParams.put("dateformat", df);

            Date creationDate = df.parse(request.getParameter("creationdate"));
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

//            KWLCurrency kwlcurrency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

//            DebitNote debitnote = new DebitNote();
            String entryNumber = request.getParameter("number");
            currencyid=(request.getParameter("currencyid")==null?kwlcurrency.getCurrencyID():request.getParameter("currencyid"));
//            KWLCurrency currency=(KWLCurrency)session.get(KWLCurrency.class,currencyid);
            curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

//            String q="from DebitNote where debitNoteNumber=? and company.companyID=?";
//            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                throw new AccountingException("Debit note number '" + entryNumber + "' already exists.");
            result = accDebitNoteobj.getDNFromNoteNo(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if(count > 0){
                throw new AccountingException("Debit note number '" + entryNumber + "' already exists.");
            }
            String nextDNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_DEBITNOTE);
//            debitnote.setDebitNoteNumber(request.getParameter("number"));
//            debitnote.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_DEBITNOTE).equals(entryNumber));
//            debitnote.setMemo(request.getParameter("memo"));
//            debitnote.setDeleted(false);
//            debitnote.setCompany(company);
//            debitnote.setCurrency(currency);
            HashMap<String,Object> dnhm = new HashMap<String,Object>();
            dnhm.put("entrynumber", entryNumber);
            dnhm.put("autogenerated", nextDNAutoNo.equals(entryNumber));
            dnhm.put("memo", request.getParameter("memo"));
            dnhm.put("companyid", companyid);
            dnhm.put("currencyid", currencyid);

            Long seqNumber = null;
//            String query = "select count(dn.ID) from DebitNote dn inner join dn.journalEntry je  where dn.company.companyID=? and je.entryDate<=?";
//            List list = HibernateUtil.executeQuery(session, query, new Object[]{AuthHandler.getCompanyid(request), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate"))});//
            result = accDebitNoteobj.getDNSequenceNo(companyid, creationDate);
            List li = result.getEntityList();
            if (!li.isEmpty()) {
                seqNumber = (Long) li.get(0);
            }
//            debitnote.setSequence(seqNumber.intValue());
            dnhm.put("sequence", seqNumber.intValue());
            
            String costCenterId = request.getParameter("costCenterId");
            boolean jeautogenflag = true;
            String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
            String jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
            Map<String,Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", creationDate);
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            if(!StringUtil.isNullOrEmpty(costCenterId)){
                jeDataMap.put("costcenterid", costCenterId);
            }
            jeDataMap.put("currencyid", currencyid);
            HashSet<JournalEntryDetail> jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            dnhm.put("journalentryid", jeid);
            
//            Double totalAmount = saveDebitNoteRows(session, request, debitnote, company, hs, preferences,kwlcurrency);
           // saveDebitNoteDiscountRows(session, request, debitnote, company);
            List DNlist = saveDebitNoteRows1(GlobalParams, request, company, currency, journalEntry, preferences,externalCurrencyRate);
            Double totalAmount = (Double) DNlist.get(0);
            Double discAccAmount = (Double) DNlist.get(1);
            HashSet<DebitNoteDetail> dndetails = (HashSet<DebitNoteDetail>) DNlist.get(2);
            jedetails = (HashSet<JournalEntryDetail>) DNlist.get(3);
//            JournalEntryDetail jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(totalAmount);
//            jed.setAccount((Account) session.get(Account.class, request.getParameter("accid")));
//            jed.setDebit(true);
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", totalAmount);
            jedjson.put("accountid", request.getParameter("accid"));
            jedjson.put("debit", true);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
            
//            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
//            request.getParameter("memo"), "JE" + debitnote.getDebitNoteNumber(),currency.getCurrencyID(), hs,request);
//            debitnote.setJournalEntry(journalEntry);

            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            result = accDebitNoteobj.addDebitNote(dnhm);
            debitnote = (DebitNote)result.getEntityList().get(0);

            dnhm.put("dnid", debitnote.getID());
            Iterator itr = dndetails.iterator();
            while (itr.hasNext()) {
                DebitNoteDetail cnd = (DebitNoteDetail) itr.next();
                cnd.setDebitNote(debitnote);
            }
            dnhm.put("dndetails", dndetails);
            
//            session.saveOrUpdate(debitnote);
            result = accDebitNoteobj.updateDebitNote(dnhm);
            debitnote = (DebitNote)result.getEntityList().get(0);
            list.add(debitnote);

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveDebitNote : "+ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveDebitNote : "+ex.getMessage(), ex);
        }
        return debitnote;
    }


    public BillingDebitNote saveBillingDebitNote(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        BillingDebitNote billingDebitnote = null;
        List list = new ArrayList();
        KwlReturnObject result;
        try {
            double externalCurrencyRate= StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            DateFormat df = authHandler.getDateFormatter(request);
            HashMap<String,Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", currencyid);
            GlobalParams.put("dateformat", df);
            Date creationDate = df.parse(request.getParameter("creationdate"));
            
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

//            KWLCurrency kwlcurrency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

//            BillingDebitNote debitnote = new BillingDebitNote();
            String entryNumber = request.getParameter("number");
            currencyid=(request.getParameter("currencyid")==null?kwlcurrency.getCurrencyID():request.getParameter("currencyid"));
            
//            KWLCurrency currency=(KWLCurrency)session.get(KWLCurrency.class,currencyid);
            curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

//            String q="from DebitNote where debitNoteNumber=? and company.companyID=?";
//            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                throw new AccountingException("Debit note number '" + entryNumber + "' already exists.");

            result = accDebitNoteobj.getBDNFromNoteNo(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if(count > 0){
                throw new AccountingException("Billing Debit note number '" + entryNumber + "' already exists.");
            }
            
//            debitnote.setDebitNoteNumber(request.getParameter("number"));
//            debitnote.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_DEBITNOTE).equals(entryNumber));
//            debitnote.setMemo(request.getParameter("memo"));
//            debitnote.setDeleted(false);
//            debitnote.setCompany(company);
//            debitnote.setCurrency(currency);
            String nextDNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGDEBITNOTE);
            HashMap<String,Object> bdnhm = new HashMap<String,Object>();
            bdnhm.put("entrynumber", entryNumber);
            bdnhm.put("autogenerated", nextDNAutoNo.equals(entryNumber));
            bdnhm.put("memo", request.getParameter("memo"));
            bdnhm.put("deleted", false);
            bdnhm.put("companyid", companyid);
            bdnhm.put("currencyid", currencyid);

            Long seqNumber = null;
//            String query = "select count(dn.ID) from DebitNote dn inner join dn.journalEntry je  where dn.company.companyID=? and je.entryDate<=?";
//            List list = HibernateUtil.executeQuery(session, query, new Object[]{AuthHandler.getCompanyid(request), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate"))});//

            result = accDebitNoteobj.getBDNSequenceNo(companyid, creationDate);
            List li = result.getEntityList();
            if (!li.isEmpty()) {
                seqNumber = (Long) li.get(0);
            }
            
//            debitnote.setSequence(seqNumber.intValue());
            bdnhm.put("sequence", seqNumber.intValue());

            String costCenterId = request.getParameter("costCenterId");
            boolean jeautogenflag = true;
            String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
            String jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;

            Map<String,Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", creationDate);
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            if(!StringUtil.isNullOrEmpty(costCenterId)){
                jeDataMap.put("costcenterid", costCenterId);
            }
            jeDataMap.put("currencyid", currencyid);
            HashSet<JournalEntryDetail> jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            bdnhm.put("journalentryid", jeid);

//            HashSet hs = new HashSet();
//            Double totalAmount = saveBillingDebitNoteRows(session, request, debitnote, company, hs, preferences,kwlcurrency,externalCurrencyRate);
           // saveBillingDebitNoteDiscountRows(session, request, debitnote, company);
           List DNlist = saveBillingDebitNoteRows(GlobalParams, request, company, currency, journalEntry, preferences,externalCurrencyRate);
            Double totalAmount = (Double) DNlist.get(0);
            HashSet<DebitNoteDetail> dndetails = (HashSet<DebitNoteDetail>) DNlist.get(2);
            jedetails = (HashSet<JournalEntryDetail>) DNlist.get(3);
            Double discAccAmount = (Double) DNlist.get(1);

//            JournalEntryDetail jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(totalAmount);
//            jed.setAccount((Account) session.get(Account.class, request.getParameter("accid")));
//            jed.setDebit(true);
//            hs.add(jed);

            HashMap<String, Object> jedMap = new HashMap();
            jedMap.put("srno", jedetails.size()+1);
            jedMap.put("companyid", company.getCompanyID());
            jedMap.put("amount", totalAmount);
            jedMap.put("accountid", request.getParameter("accid"));
            jedMap.put("debit", true);
            jedMap.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedMap);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);


//            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
//            request.getParameter("memo"), "JE" + debitnote.getDebitNoteNumber(),currency.getCurrencyID(),externalCurrencyRate, hs,request);
//            debitnote.setJournalEntry(journalEntry);
//            session.saveOrUpdate(debitnote);

            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            result = accDebitNoteobj.saveBillingDebitNote(bdnhm);
            billingDebitnote = (BillingDebitNote)result.getEntityList().get(0);

            bdnhm.put("bdnid", billingDebitnote.getID());
            Iterator itr = dndetails.iterator();
            while (itr.hasNext()) {
                BillingDebitNoteDetail cnd = (BillingDebitNoteDetail) itr.next();
                cnd.setDebitNote(billingDebitnote);
            }
            bdnhm.put("bdndetails", dndetails);

//            session.saveOrUpdate(debitnote);
            result = accDebitNoteobj.saveBillingDebitNote(bdnhm);
            billingDebitnote = (BillingDebitNote)result.getEntityList().get(0);
            list.add(billingDebitnote);

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveDebitNote : "+ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveDebitNote : "+ex.getMessage(), ex);
        }catch (Exception ex){
        	ex.printStackTrace();
        }
        return billingDebitnote;
    }

    public List saveDebitNoteRows(HashMap<String, Object> GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences,double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        List resultlist = new ArrayList();
        double totalAmount = 0;
        double totalTax = 0;
        double discAccAmount = 0;
        HashSet dndetails = new HashSet();
        HashSet jedetails = new HashSet();

        JournalEntryDetail jed;
        KwlReturnObject result;

        boolean includeTax = StringUtil.getBoolean(request.getParameter("includetax"));
        String companyid = (String) GlobalParams.get("companyid");
        String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
        JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
        List list = new ArrayList();
        for (int i = 0; i < jArr.length(); i++) {
            double taxamount = 0;
            double amount = 0;
            JSONObject jobj = jArr.getJSONObject(i);
            DebitNoteDetail row = new DebitNoteDetail();
            row.setSrno(i+1);
            double disc = jobj.getDouble("discamount");
            row.setCompany(company);
//            row.setDebitNote(dn);
            row.setMemo(request.getParameter("memo"));
            row.setQuantity(jobj.getInt("remquantity"));

//            GoodsReceiptDetail goodsReceiptRow = (GoodsReceiptDetail) session.get(GoodsReceiptDetail.class, jobj.getString("rowid"));
            result = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), jobj.getString("rowid"));
            GoodsReceiptDetail goodsReceiptRow = (GoodsReceiptDetail) result.getEntityList().get(0);

            row.setGoodsReceiptRow(goodsReceiptRow);
            Product product = goodsReceiptRow.getInventory().getProduct();
//            Account account = (Account) session.get(Account.class, product.getPurchaseReturnAccount().getID());
            result = accountingHandlerDAOobj.getObject(Account.class.getName(), product.getPurchaseReturnAccount().getID());
            Account account = (Account) result.getEntityList().get(0);

            double percent=0;
            if (goodsReceiptRow.getGoodsReceipt().getTax() != null) {
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceiptRow.getGoodsReceipt().getJournalEntry().getEntryDate(), goodsReceiptRow.getGoodsReceipt().getTax().getID());
                percent = (Double) perresult.getEntityList().get(0);
            }
            if (jobj.getInt("typeid") == 2 || jobj.getInt("typeid") == 3) {
//                Inventory inventory = CompanyHandler.makeInventory(session, request, product, jobj.getInt("remquantity"), jobj.optString("desc"), false, (jobj.getInt("typeid") == 3 ? false : true));
                JSONObject inventoryjson = new JSONObject();
                inventoryjson.put("productid", product.getID());
                inventoryjson.put("quantity", jobj.getInt("remquantity"));
                inventoryjson.put("description", jobj.optString("desc"));
                inventoryjson.put("carryin", false);
                inventoryjson.put("defective", (jobj.getInt("typeid") == 3 ? false : true));
                inventoryjson.put("newinventory", false);
                inventoryjson.put("companyid", company.getCompanyID());
                inventoryjson.put("updatedate", AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                Inventory inventory = (Inventory) invresult.getEntityList().get(0);
                row.setInventory(inventory);
                if (list.contains(account)) {
                    dndetails.add(row);
                    continue;
                }
                for (int k = 0; k < jArr.length(); k++) {
                    JSONObject jobj1 = jArr.getJSONObject(k);
//                    GoodsReceiptDetail compGoodsReceiptRow = (GoodsReceiptDetail) session.get(GoodsReceiptDetail.class, jobj1.getString("rowid"));
                    result = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), jobj1.getString("rowid"));
                    GoodsReceiptDetail compGoodsReceiptRow = (GoodsReceiptDetail) result.getEntityList().get(0);
                    Product compProduct = compGoodsReceiptRow.getInventory().getProduct();

//                    Account compAccount = (Account) session.get(Account.class, compProduct.getPurchaseReturnAccount().getID());
                    result = accountingHandlerDAOobj.getObject(Account.class.getName(), compProduct.getPurchaseReturnAccount().getID());
                    Account compAccount = (Account) result.getEntityList().get(0);
                    if (account == compAccount && (jobj1.getInt("typeid") == 2  || jobj.getInt("typeid") == 3)) {
                            amount = amount + jobj1.getDouble("discamount");
                        list.add(compAccount);
                    }
                    if (disc > 0) {
//                        Discount discount = new Discount();
//                        discount.setDiscount(disc);
//                        discount.setInPercent(false);
//                        discount.setOriginalAmount(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("amount"),currencyid,null));
//                        discount.setCompany(company);
//                        session.save(discount);
                        JSONObject discjson = new JSONObject();
                        discjson.put("discount", disc);
                        discjson.put("inpercent", false);
                        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null,externalCurrencyRate);
                        discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                        discjson.put("companyid", company.getCompanyID());
                        KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                        Discount discount = (Discount) dscresult.getEntityList().get(0);
                        row.setDiscount(discount);

                        if(includeTax){
                            taxamount=disc*percent/100;
                            row.setTaxAmount(taxamount);
                            totalTax+=taxamount;
                            if(includeTax&&taxamount>0){
//                                jed = new JournalEntryDetail();
//                                jed.setCompany(company);
//                                jed.setAmount(taxamount);
//                                jed.setAccount(goodsReceiptRow.getGoodsReceipt().getTax().getAccount());
//                                jed.setDebit(false);
//                                hs.add(jed);
                                JSONObject jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size()+1);
                                jedjson.put("companyid", company.getCompanyID());
                                jedjson.put("amount", taxamount);
                                jedjson.put("accountid", goodsReceiptRow.getGoodsReceipt().getTax().getAccount().getID());
                                jedjson.put("debit", false);
                                jedjson.put("jeid", je.getID());
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed);
                            }
                        }
                    }
                }
            } else {
                if(includeTax){
                    taxamount = disc * percent / 100;
                    row.setTaxAmount(taxamount);
                    totalTax += taxamount;
                    if (includeTax && taxamount > 0) {
//                        jed = new JournalEntryDetail();
//                        jed.setCompany(company);
//                        jed.setAmount(taxamount);
//                        jed.setAccount(goodsReceiptRow.getGoodsReceipt().getTax().getAccount());
//                        jed.setDebit(false);
//                        hs.add(jed);
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size()+1);
                        jedjson.put("companyid", company.getCompanyID());
                        jedjson.put("amount", taxamount);
                        jedjson.put("accountid", goodsReceiptRow.getGoodsReceipt().getTax().getAccount().getID());
                        jedjson.put("debit", false);
                        jedjson.put("jeid", je.getID());
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                    }
                }
                discAccAmount = discAccAmount + jobj.getDouble("discamount");
                dndetails.add(row);
                if (disc > 0) {
//                    Discount discount = new Discount();
//                    discount.setDiscount(disc);
//                    discount.setInPercent(false);
//                    discount.setOriginalAmount(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("amount"),currencyid,null));
//                    discount.setCompany(company);
//                    session.save(discount);
//                    row.setDiscount(discount);
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", disc);
                    discjson.put("inpercent", false);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, 0);
                    discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                }
                continue;
            }
            dndetails.add(row);
//            jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(amount);
//            jed.setAccount(account);
//            jed.setDebit(false);
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", amount);
            jedjson.put("accountid", account.getID());
            jedjson.put("debit", false);
            jedjson.put("jeid", je.getID());
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);

            jedetails.add(jed);
            totalAmount += amount;
        }
        if (discAccAmount != 0.0) {
//            jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(discAccAmount);
//            jed.setAccount((Account) session.get(Account.class, preferences.getDiscountReceived().getID()));
//            jed.setDebit(false);
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", discAccAmount);
            jedjson.put("accountid", preferences.getDiscountReceived().getID());
            jedjson.put("debit", false);
            jedjson.put("jeid", je.getID());
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
            totalAmount += discAccAmount;
        }

        resultlist.add(totalAmount+totalTax);
        resultlist.add(discAccAmount);
        resultlist.add(dndetails);
        resultlist.add(jedetails);
        return resultlist;
    }

    public List saveBillingDebitNoteRows(HashMap<String, Object> GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences,double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        double totalAmount = 0;
        double totalTax=0;
        double discAccAmount = 0;
        List resultlist = new ArrayList();
        HashSet bdndetails = new HashSet();
        HashSet jedetails = new HashSet();

        JournalEntryDetail jed;
        KwlReturnObject result;

//            HashSet rows = new HashSet();
//            Account account;
           
            boolean includeTax=StringUtil.getBoolean(request.getParameter("includetax"));
            String companyid = (String) GlobalParams.get("companyid");
            String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
            JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
            List list = new ArrayList();
            for (int i = 0; i < jArr.length(); i++) {
                double taxamount=0;
                double amount = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                BillingDebitNoteDetail row = new BillingDebitNoteDetail();
                row.setSrno(i+1);
                double disc = jobj.getDouble("discamount");
                row.setCompany(company);
//                row.setDebitNote(dn);
//                row.setMemo(dn.getMemo());
                row.setMemo(request.getParameter("memo"));
                row.setQuantity(jobj.getDouble("remquantity"));
                
//                BillingGoodsReceiptDetail goodsReceiptRow = (BillingGoodsReceiptDetail) session.get(BillingGoodsReceiptDetail.class, jobj.getString("rowid"));
            result = accountingHandlerDAOobj.getObject(BillingGoodsReceiptDetail.class.getName(), jobj.getString("rowid"));
            BillingGoodsReceiptDetail goodsReceiptRow = (BillingGoodsReceiptDetail) result.getEntityList().get(0);
                row.setGoodsReceiptRow(goodsReceiptRow);
                String product = goodsReceiptRow.getProductDetail();
                double percent=0;
                if(goodsReceiptRow.getBillingGoodsReceipt().getTax()!=null){
//                  percent=CompanyHandler.getTaxPercent(session, request, goodsReceiptRow.getBillingGoodsReceipt().getJournalEntry().getEntryDate(),goodsReceiptRow.getBillingGoodsReceipt().getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceiptRow.getBillingGoodsReceipt().getJournalEntry().getEntryDate(), goodsReceiptRow.getBillingGoodsReceipt().getTax().getID());
                    percent = (Double) perresult.getEntityList().get(0);

                }
                    
                if(includeTax){
                    taxamount=disc*percent/100;
                    row.setTaxAmount(taxamount);
                    totalTax+=taxamount;
                    if(includeTax&&taxamount>0){
//                        jed = new JournalEntryDetail();
//                        jed.setCompany(company);
//                        jed.setAmount(taxamount);
//                        jed.setAccount(goodsReceiptRow.getBillingGoodsReceipt().getTax().getAccount());
//                        jed.setDebit(false);
//                        hs.add(jed);
                        HashMap<String, Object> jedMap = new HashMap();
                        jedMap.put("srno", jedetails.size()+1);
                        jedMap.put("companyid", company.getCompanyID());
                        jedMap.put("amount", taxamount);
                        jedMap.put("accountid", goodsReceiptRow.getBillingGoodsReceipt().getTax().getAccount().getID());
                        jedMap.put("debit", false);
                        jedMap.put("jeid", je.getID());
                        KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedMap);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                    }
                }
                discAccAmount = discAccAmount + jobj.getDouble("discamount");
                bdndetails.add(row);
                if (disc > 0) {
//                    Discount discount = new Discount();
//                    discount.setDiscount(disc);
//                    discount.setInPercent(false);
//                    discount.setOriginalAmount(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("amount"),currencyid,null,externalCurrencyRate));
//                    discount.setCompany(company);
//                    session.save(discount);
//                    row.setDiscount(discount);

                    HashMap<String, Object> discMap = new HashMap();
                    discMap.put("discount", disc);
                    discMap.put("inpercent", false);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                    discMap.put("originalamount", (Double) bAmt.getEntityList().get(0));
                    discMap.put("companyid", company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.updateDiscount(discMap);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);

                }
            }
            if (discAccAmount != 0.0) {
//                jed = new JournalEntryDetail();
//                jed.setCompany(company);
//                jed.setAmount(discAccAmount);
//                jed.setAccount((Account) session.get(Account.class, preferences.getDiscountReceived().getID()));
//                jed.setDebit(false);
//
                HashMap<String, Object> jedMap = new HashMap();
                jedMap.put("srno", jedetails.size()+1);
                jedMap.put("companyid", company.getCompanyID());
                jedMap.put("amount", discAccAmount);
                jedMap.put("accountid", preferences.getDiscountReceived().getID());
                jedMap.put("debit", false);
                jedMap.put("jeid", je.getID());
                KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedMap);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
                totalAmount += discAccAmount;
            }
        resultlist.add(totalAmount+totalTax);
        resultlist.add(discAccAmount);
        resultlist.add(bdndetails);
        resultlist.add(jedetails);
        return resultlist;
    }

    public ModelAndView getDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            KwlReturnObject result = accDebitNoteobj.getDebitNotes(requestParams);
            JSONArray DataJArr = getDebitNotesJson(requestParams, result.getEntityList());
            jobj.put("data", DataJArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getBillingDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            KwlReturnObject result = accDebitNoteobj.getBillingDebitNotes(requestParams);
            JSONArray DataJArr = getBillingDebitNotesJson(requestParams, result.getEntityList());
            jobj.put("data", DataJArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public HashMap<String, Object> gettDebitNoteMap (HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(Constants.start, request.getParameter(Constants.start));
        requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        requestParams.put(CCConstants.REQ_costCenterId,request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
        return requestParams;
    }

    public JSONArray getDebitNotesJson(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONArray JArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            double tax = 0;
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                DebitNote debitMemo = (DebitNote) row[0];
                JournalEntry je = debitMemo.getJournalEntry();
                Vendor vendor = (Vendor) row[1];
                JournalEntryDetail details = (JournalEntryDetail) row[2];
                JSONObject obj = new JSONObject();
                obj.put("noteid", debitMemo.getID());
                obj.put("noteno", debitMemo.getDebitNoteNumber());
                obj.put("journalentryid", je.getID());
                obj.put("currencysymbol", (debitMemo.getCurrency()==null?currency.getSymbol():debitMemo.getCurrency().getSymbol()));
                obj.put("currencyid", (debitMemo.getCurrency()==null?currency.getCurrencyID():debitMemo.getCurrency().getCurrencyID()));
                obj.put("entryno", je.getEntryNumber());
                obj.put("personid", vendor.getID());
                obj.put("personname", vendor.getAccount().getName());
                obj.put("amount", details.getAmount());
                obj.put("date", df.format(je.getEntryDate()));
                obj.put("memo", debitMemo.getMemo());
                obj.put("costcenterid", je.getCostcenter()==null?"":je.getCostcenter().getID());
                obj.put("costcenterName", je.getCostcenter()==null?"":je.getCostcenter().getName());
                
                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), je.getCompany().getCompanyID());
                Iterator iterator = result.getEntityList().iterator();
                while(iterator.hasNext()){
                	JournalEntryDetail jed = (JournalEntryDetail)iterator.next();
                	Account account=null;
                	account=jed.getAccount();
                	if(account.getGroup().getID().equals(Group.OTHER_CURRENT_LIABILITIES)){
                		if(!jed.isDebit()){
                	      tax = jed.getAmount(); 
                		}
                    }
                }
                obj.put("noteSubTotal", details.getAmount() - tax);
                obj.put("notetax", tax);
                JArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesJson : "+ex.getMessage(), ex);
        }
        return JArr;
    }

    public JSONArray getBillingDebitNotesJson(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            double tax = 0;
            Iterator itr = list.iterator();

            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                BillingDebitNote debitMemo = (BillingDebitNote) row[0];
                JournalEntry je = debitMemo.getJournalEntry();
                Vendor vendor = (Vendor) row[1];
                JournalEntryDetail details = (JournalEntryDetail) row[2];
                JSONObject obj = new JSONObject();
                obj.put("noteid", debitMemo.getID());
                obj.put("noteno", debitMemo.getDebitNoteNumber());
                obj.put("journalentryid", je.getID());
                obj.put("currencysymbol", (debitMemo.getCurrency() == null ? currency.getSymbol() : debitMemo.getCurrency().getSymbol()));
                obj.put("currencyid", (debitMemo.getCurrency()==null?currency.getCurrencyID():debitMemo.getCurrency().getCurrencyID()));
                obj.put("entryno", je.getEntryNumber());
                obj.put("personid", vendor.getID());
                obj.put("personname", vendor.getAccount().getName());
                obj.put("amount", details.getAmount());
                obj.put("date", df.format(je.getEntryDate()));
                obj.put("memo", debitMemo.getMemo());
                obj.put("costcenterid", je.getCostcenter()==null?"":je.getCostcenter().getID());
                obj.put("costcenterName", je.getCostcenter()==null?"":je.getCostcenter().getName());
                
                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), je.getCompany().getCompanyID());
                Iterator iterator = result.getEntityList().iterator();
                while(iterator.hasNext()){
                	JournalEntryDetail jed = (JournalEntryDetail)iterator.next();
                	Account account=null;
                	account=jed.getAccount();
                	if(account.getGroup().getID().equals(Group.OTHER_CURRENT_LIABILITIES)){
                		if(!jed.isDebit()){
                	      tax = jed.getAmount(); 
                		}
                    }
                }
                obj.put("noteSubTotal", details.getAmount() - tax);
                obj.put("notetax", tax);
                jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getBillingDebitNotesJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getDebitNoteRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("bills", request.getParameterValues("bills"));

            JSONArray DataJArr = getDebitNoteRowsJson(requestParams);
            jobj.put("data", DataJArr);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accDebitNoteController.getDebitNoteRows : "+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getBillingDebitNoteRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("bills", request.getParameterValues("bills"));

            JSONArray DataJArr = getBillingDebitNoteRowsJson(requestParams);
            jobj.put("data", DataJArr);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accDebitNoteController.getBillingDebitNoteRows : "+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getDebitNoteRowsJson(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray JArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            String[] creditNote = (String[]) requestParams.get("bills");
            int i = 0;

            HashMap<String, Object> dnRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("debitNote.ID");
            order_by.add("srno");
            order_type.add("asc");
            dnRequestParams.put("filter_names", filter_names);
            dnRequestParams.put("filter_params", filter_params);
            dnRequestParams.put("order_by", order_by);
            dnRequestParams.put("order_type", order_type);
            
            while (creditNote != null && i < creditNote.length) {
//                DebitNote dn = (DebitNote) session.get(DebitNote.class, creditNote[i]);
                KwlReturnObject dnresult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), creditNote[i]);
                DebitNote dn = (DebitNote) dnresult.getEntityList().get(0);
//                Iterator itr = dn.getRows().iterator();
                filter_params.clear();
                filter_params.add(dn.getID());
                KwlReturnObject grdresult = accDebitNoteobj.getDebitNoteDetails(dnRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();

                while (itr.hasNext()) {
                    DebitNoteDetail row = (DebitNoteDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", dn.getID());
                    obj.put("billno", dn.getDebitNoteNumber());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getGoodsReceiptRow().getInventory().getProduct().getID());
                    obj.put("productname", row.getGoodsReceiptRow().getInventory().getProduct().getName());
                    obj.put("desc", row.getGoodsReceiptRow().getInventory().getProduct().getDescription());
                    obj.put("memo", row.getMemo());
                    obj.put("currencysymbol", (dn.getCurrency() == null ? currency.getSymbol() : dn.getCurrency().getSymbol()));
                    obj.put("transectionid", row.getGoodsReceiptRow().getGoodsReceipt().getID());
                    obj.put("transectionno", row.getGoodsReceiptRow().getGoodsReceipt().getGoodsReceiptNumber());
                    Discount disc = row.getDiscount();
                    if (disc != null) {
                        obj.put("discount", row.getDiscount().getDiscountValue());
                    } else {
                        obj.put("discount", 0);
                    }
                    obj.put("quantity", row.getQuantity());
                    obj.put("taxamount", row.getTaxAmount());
                    JArr.put(obj);
                }
                i++;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesRowsJson : "+ex.getMessage(), ex);
        }
        return JArr;
    }

    public JSONArray getBillingDebitNoteRowsJson(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray JArr = new JSONArray();
        try {
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            
//            String[] creditNote = request.getParameterValues("bills");
            String[] creditNote = (String[]) requestParams.get("bills");
            int i = 0;

            HashMap<String, Object> dnRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("debitNote.ID");
            order_by.add("srno");
            order_type.add("asc");
            dnRequestParams.put("filter_names", filter_names);
            dnRequestParams.put("filter_params", filter_params);
            dnRequestParams.put("order_by", order_by);
            dnRequestParams.put("order_type", order_type);
            
            while (creditNote != null && i < creditNote.length) {
//                BillingDebitNote dn = (BillingDebitNote) session.get(BillingDebitNote.class, creditNote[i]);
                KwlReturnObject dnresult = accountingHandlerDAOobj.getObject(BillingDebitNote.class.getName(), creditNote[i]);
                BillingDebitNote dn = (BillingDebitNote) dnresult.getEntityList().get(0);
//                Iterator itr = dn.getRows().iterator();
                filter_params.clear();
                filter_params.add(dn.getID());
                KwlReturnObject grdresult = accDebitNoteobj.getBillingDebitNoteDetails(dnRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();

                while (itr.hasNext()) {
                    BillingDebitNoteDetail row = (BillingDebitNoteDetail) itr.next();
                    BillingGoodsReceiptDetail grRow = row.getGoodsReceiptRow();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", dn.getID());
                    obj.put("billno", dn.getDebitNoteNumber());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", grRow.getProductDetail());
                    obj.put("productdetail", grRow.getProductDetail());
                    obj.put("desc", grRow.getProductDetail());
                    obj.put("memo", row.getMemo());
                    obj.put("currencysymbol", (dn.getCurrency()==null?currency.getSymbol():dn.getCurrency().getSymbol()));
                    obj.put("transectionid", grRow.getBillingGoodsReceipt().getID());
                    obj.put("transectionno", grRow.getBillingGoodsReceipt().getBillingGoodsReceiptNumber());
                    Discount disc=row.getDiscount();
                    if(disc!=null){
                        obj.put("discount", disc.getDiscountValue());
                    }else{
                        obj.put("discount", 0);
                    }
                    obj.put("quantity", row.getQuantity());
                    obj.put("taxamount", row.getTaxAmount());
                    JArr.put(obj);
                }
                i++;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getBillingDebitNoteRowsJson : "+ex.getMessage(), ex);
        }
        return JArr;
    }

    public ModelAndView deleteDebitNotes(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("DN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteDebitNotes(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.debitN.del", null, RequestContextUtils.getLocale(request));   //"Debit Note(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteBillingDebitNotes(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("DN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteBillingDebitNotes(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.debitN.billDel", null, RequestContextUtils.getLocale(request));   //"Billing Debit Note(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteDebitNotes(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
//        ArrayList params = new ArrayList();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
//            String qMarks = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("noteid"))) {
                    String dnid = URLDecoder.decode(jobj.getString("noteid"),StaticValues.ENCODING);
//                    params.add(jobj.getString("noteid"));
//                    qMarks += "?,";
//                    query = "update DebitNote set deleted=true where ID in("+qMarks +") and company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    KwlReturnObject result = accDebitNoteobj.deleteDebitNote(dnid, companyid);

//                    query = "update JournalEntry je set je.deleted=true  where je.ID in(select dn.journalEntry.ID from DebitNote dn where dn.ID in( " + qMarks + ") and dn.company.companyID=je.company.companyID) and je.company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    result = accDebitNoteobj.getJEFromDN(dnid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeid = (String) itr.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                    }

//                    query = "update Discount di set di.deleted=true  where di.ID in(select dnd.discount.ID from DebitNoteDiscount dnd where dnd.debitNote.ID in( " + qMarks + ") and dnd.company.companyID=di.company.companyID) and di.company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    result = accDebitNoteobj.getDNDFromDN(dnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = (String) itr.next();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }

                    /*
                    query = "update Discount di set di.deleted=true  where di.ID in(select dnd.discount.ID from DebitNoteDetail dnd where dnd.debitNote.ID in( " + qMarks + ") and dnd.company.companyID=di.company.companyID) and di.company.companyID=?";
                    HibernateUtil.executeUpdate(session, query, params.toArray());
                     */
                    result = accDebitNoteobj.getDNDIFromDN(dnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = (String) itr.next();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }
                    /*
                    query = "update Inventory inv set inv.deleted=true  where inv.ID in(select dnd.inventory.ID from DebitNoteDetail dnd where dnd.debitNote.ID in( " + qMarks + ") and dnd.company.companyID=inv.company.companyID) and inv.company.companyID=?";
                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    */
                    result = accDebitNoteobj.getDNDInvFromDN(dnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String inventoryid = (String) itr.next();
                        result = accProductObj.deleteInventoryEntry(inventoryid, companyid);
                    }
                }
            }
//            params.add(company.getCompanyID());
//            qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
//            String query;
//            List list;
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public void deleteBillingDebitNotes(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
//        ArrayList params = new ArrayList();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
//            String qMarks = "";
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("noteid"))) {
                    String bdnid = URLDecoder.decode(jobj.getString("noteid"), StaticValues.ENCODING);
//                    params.add(URLDecoder.decode(jobj.getString("noteid"),StaticValues.ENCODING));
//                    qMarks += "?,";
//                }
//            }
//            params.add(company.getCompanyID());
//            qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
//            String query;
//            List list;
//            query = "update BillingDebitNote set deleted=true where ID in("+qMarks +") and company.companyID=?";
//            HibernateUtil.executeUpdate(session, query, params.toArray());
                    KwlReturnObject result = accDebitNoteobj.deleteBillingDebitNote(bdnid, companyid);


                    //query = "update JournalEntry je set je.deleted=true  where je.ID in(select dn.journalEntry.ID from BillingDebitNote dn where dn.ID in( " + qMarks + ") and dn.company.companyID=je.company.companyID) and je.company.companyID=?";
                    result = accDebitNoteobj.getJEFromBDN(bdnid, companyid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeid = ((BillingDebitNote) itr.next()).getJournalEntry().getID();
                        result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                    }

//            query = "update Discount di set di.deleted=true  where di.ID in(select dnd.discount.ID from BillingDebitNoteDiscount dnd where dnd.debitNote.ID in( " + qMarks + ") and dnd.company.companyID=di.company.companyID) and di.company.companyID=?";
                    result = accDebitNoteobj.getDNDFromBDN(bdnid, companyid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = ((BillingDebitNoteDiscount) itr.next()).getDiscount().getID();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }

//            query = "update Discount di set di.deleted=true  where di.ID in(select dnd.discount.ID from BillingDebitNoteDetail dnd where dnd.debitNote.ID in( " + qMarks + ") and dnd.company.companyID=di.company.companyID) and di.company.companyID=?";
                    result = accDebitNoteobj.getDNDFromBDND(bdnid, companyid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = ((BillingDebitNoteDetail) itr.next()).getDiscount().getID();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public ModelAndView exportDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            KwlReturnObject result = accDebitNoteobj.getDebitNotes(requestParams);
            JSONArray DataJArr = getDebitNotesJson(requestParams, result.getEntityList());
            jobj.put("data", DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView exportBillingDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            KwlReturnObject result = accDebitNoteobj.getBillingDebitNotes(requestParams);
            JSONArray DataJArr = getBillingDebitNotesJson(requestParams, result.getEntityList());
            jobj.put("data", DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    
    public List saveDebitNoteRows1(HashMap<String, Object> GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences,double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        List resultlist = new ArrayList();
        double totalAmount = 0;
        double totalTax = 0, prodTax = 0;
        double discAccAmount = 0;
        HashSet dndetails = new HashSet();
        HashSet jedetails = new HashSet();

        JournalEntryDetail jed;
        KwlReturnObject result;

        boolean includeTax = StringUtil.getBoolean(request.getParameter("includetax"));
        String companyid = (String) GlobalParams.get("companyid");
        String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
        JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
        List list = new ArrayList();
        for (int i = 0; i < jArr.length(); i++) {
            double taxamount = 0;
            double amount = 0;
            prodTax = 0;
            JSONObject jobj = jArr.getJSONObject(i);
            DebitNoteDetail row = new DebitNoteDetail();
            row.setSrno(i+1);
            double disc = jobj.getDouble("discamount");
            row.setCompany(company);
            row.setMemo(request.getParameter("memo"));
            row.setQuantity(jobj.getInt("remquantity"));

            result = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), jobj.getString("rowid"));
            GoodsReceiptDetail goodsReceiptRow = (GoodsReceiptDetail) result.getEntityList().get(0);

            row.setGoodsReceiptRow(goodsReceiptRow);
            Product product = goodsReceiptRow.getInventory().getProduct();
            result = accountingHandlerDAOobj.getObject(Account.class.getName(), product.getPurchaseReturnAccount().getID());
            Account account = (Account) result.getEntityList().get(0);

            double percent=0;
            if (goodsReceiptRow.getGoodsReceipt().getTax() != null) {
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceiptRow.getGoodsReceipt().getJournalEntry().getEntryDate(), goodsReceiptRow.getGoodsReceipt().getTax().getID());
                percent = (Double) perresult.getEntityList().get(0);
            }
            
            
            amount = jobj.getDouble("discamount");
            
            if (jobj.getInt("typeid") == 2 || jobj.getInt("typeid") == 3) {
                
///////////////////////////////////////////////   Reload Inventory            	
            	JSONObject inventoryjson = new JSONObject();
                inventoryjson.put("productid", product.getID());
                inventoryjson.put("quantity", jobj.getInt("remquantity"));
                inventoryjson.put("description", jobj.optString("desc"));
                inventoryjson.put("carryin", false);
                inventoryjson.put("defective", true);
                inventoryjson.put("newinventory", false);
                inventoryjson.put("companyid", company.getCompanyID());
                inventoryjson.put("updatedate", AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                Inventory inventory = (Inventory) invresult.getEntityList().get(0);
                row.setInventory(inventory);
///////////////////////////////////////////////   Reload Inventory Over            	

                
                result = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), jobj.getString("rowid"));
                    GoodsReceiptDetail compGoodsReceiptRow = (GoodsReceiptDetail) result.getEntityList().get(0);
                    Product compProduct = compGoodsReceiptRow.getInventory().getProduct();

                    result = accountingHandlerDAOobj.getObject(Account.class.getName(), compProduct.getPurchaseReturnAccount().getID());
                    Account compAccount = (Account) result.getEntityList().get(0);

                    
                    if (disc > 0) {
                    	
/////////////////////////////////////////////    Total Tax                            
                        if(includeTax){
                            taxamount=disc*percent/100;
                            row.setTaxAmount(taxamount);
                            totalTax+=taxamount;
                            if(includeTax&&taxamount>0){
                                JSONObject jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size()+1);
                                jedjson.put("companyid", company.getCompanyID());
                                jedjson.put("amount", taxamount);
                                jedjson.put("accountid", goodsReceiptRow.getGoodsReceipt().getTax().getAccount().getID());
                                jedjson.put("debit", false);
                                jedjson.put("jeid", je.getID());
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed);
                            }
                        }
/////////////////////////////////////////////    Total Tax Over                            

                    	
/////////////////////////////////////////////    Discount Row Added                        
					    JSONObject discjson = new JSONObject();
					    discjson.put("discount", disc);
					    discjson.put("inpercent", false);
					    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null,externalCurrencyRate);
					    discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
					    discjson.put("companyid", company.getCompanyID());
					    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
					    Discount discount = (Discount) dscresult.getEntityList().get(0);
					    row.setDiscount(discount);
					    dndetails.add(row);
/////////////////////////////////////////////    Discount Row Added Over                        
                        
                    }
                    

/////////////////////////////////////////////    Product Tax                            
                    if(compGoodsReceiptRow.getTax() != null){
        	            /*  Product level tax taken care of   */
        	
        	            KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), compGoodsReceiptRow.getGoodsReceipt().getJournalEntry().getEntryDate(), compGoodsReceiptRow.getTax().getID());
        	            percent = (Double) perresult.getEntityList().get(0);
        	            prodTax = percent * amount/(percent + 100);
        	                        
        	            JSONObject jedtaxjson = new JSONObject();
        	            jedtaxjson.put("srno", jedetails.size()+1);
        	            jedtaxjson.put("companyid", company.getCompanyID());
        	            jedtaxjson.put("amount", prodTax);
        	            jedtaxjson.put("accountid", compGoodsReceiptRow.getTax().getAccount().getID());
        	            jedtaxjson.put("debit", false);
        	            jedtaxjson.put("jeid", je.getID());
        	            KwlReturnObject jedtaxresult = accJournalEntryobj.addJournalEntryDetails(jedtaxjson);
        	            jed = (JournalEntryDetail) jedtaxresult.getEntityList().get(0);
        	            jedetails.add(jed);
        	            
        	            /*  Product level tax taken care of   */
                	}
/////////////////////////////////////////////    Product Tax                            
                    
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size()+1);
                    jedjson.put("companyid", company.getCompanyID());
                    jedjson.put("amount", (amount - prodTax));
                    jedjson.put("accountid", account.getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", je.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);

                    jedetails.add(jed);
                    
                    totalAmount = totalAmount + amount + taxamount;

            } else {
            	
                discAccAmount = jobj.getDouble("discamount");
            	
/////////////////////////////////////////////    Total Tax                            
                if(includeTax){
                    taxamount = disc * percent / 100;
                    row.setTaxAmount(taxamount);
                    totalTax += taxamount;
                    if (includeTax && taxamount > 0) {
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size()+1);
                        jedjson.put("companyid", company.getCompanyID());
                        jedjson.put("amount", taxamount);
                        jedjson.put("accountid", goodsReceiptRow.getGoodsReceipt().getTax().getAccount().getID());
                        jedjson.put("debit", false);
                        jedjson.put("jeid", je.getID());
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                    }
                }
/////////////////////////////////////////////    Total Tax Over                           
                
/////////////////////////////////////////////    Discount Row Added                        
                if (disc > 0) {
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", disc);
                    discjson.put("inpercent", false);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, 0);
                    discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                    dndetails.add(row);
                }
/////////////////////////////////////////////    Discount Row Added Over                       
                
/////////////////////////////////////////////    Product Tax                            
                if(goodsReceiptRow.getTax() != null){
    	            /*  Product level tax taken care of   */
    	
    	            KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), goodsReceiptRow.getGoodsReceipt().getJournalEntry().getEntryDate(), goodsReceiptRow.getTax().getID());
    	            percent = (Double) perresult.getEntityList().get(0);
    	            prodTax = percent * discAccAmount/(percent + 100);
   	                        
    	            JSONObject jedtaxjson = new JSONObject();
    	            jedtaxjson.put("srno", jedetails.size()+1);
    	            jedtaxjson.put("companyid", company.getCompanyID());
    	            jedtaxjson.put("amount", prodTax);
    	            jedtaxjson.put("accountid", goodsReceiptRow.getTax().getAccount().getID());
    	            jedtaxjson.put("debit", false);
    	            jedtaxjson.put("jeid", je.getID());
    	            KwlReturnObject jedtaxresult = accJournalEntryobj.addJournalEntryDetails(jedtaxjson);
    	            jed = (JournalEntryDetail) jedtaxresult.getEntityList().get(0);
    	            jedetails.add(jed);
    	            
    	            /*  Product level tax taken care of   */
            	}
/////////////////////////////////////////////    Product Tax                            
                
/////////////////////////////////////////////    Discount Received                 
                if (discAccAmount != 0.0) {
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size()+1);
                    jedjson.put("companyid", company.getCompanyID());
                    jedjson.put("amount", discAccAmount - prodTax);
                    jedjson.put("accountid", preferences.getDiscountReceived().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", je.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
/////////////////////////////////////////////    Discount Received Over                 

                totalAmount = totalAmount + discAccAmount + taxamount;
            }
        }

        resultlist.add(totalAmount);    //+totalTax);
        resultlist.add(discAccAmount);
        resultlist.add(dndetails);
        resultlist.add(jedetails);
        return resultlist;
    }
    
    
}
