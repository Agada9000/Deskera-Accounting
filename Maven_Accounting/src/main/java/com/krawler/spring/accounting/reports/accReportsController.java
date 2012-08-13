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
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.Asset;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.GoodsReceiptDetail;
import com.krawler.hql.accounting.Group;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.PaymentDetail;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Producttype;
import com.krawler.hql.accounting.Receipt;
import com.krawler.hql.accounting.ReceiptDetail;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.TaxList;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.krawler.spring.exportFuctionality.ExportRecord;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.hql.accounting.ExpenseGRDetail;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.Invoice;
import java.util.Map;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.depreciation.accDepreciationDAO;

import java.text.DateFormat;
import java.util.ArrayList;


/**
 *
 * @author krawler
 */
public class accReportsController extends MultiActionController implements MessageSourceAware{
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accProductDAO accProductObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accAccountDAO accAccountDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private String successView;
    private accGoodsReceiptDAO accGoodsReceiptDAOObj;
    private accGoodsReceiptCMN accGoodsReceiptCommon;
    private accInvoiceDAO accInvoiceDAOobj;
    private accTaxDAO accTaxObj;
    private accInvoiceCMN accInvoiceCommon;
    private authHandlerDAO authHandlerDAO;
    private ExportRecord ExportrecordObj;
    private AccCostCenterDAO accCostCenterObj;
    private accReceiptDAO accReceiptDao;
    private accVendorPaymentDAO accVendorPaymentDao;
    private MessageSource messageSource;
    private accDebitNoteDAO accDebitNoteobj;
    private accDepreciationDAO accDepreciationObj;
    
    public void setaccDepreciationDAO(accDepreciationDAO accDepreciationObj) {
        this.accDepreciationObj = accDepreciationObj;
    }
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
	public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentDao) {
		this.accVendorPaymentDao = accVendorPaymentDao;
	}
	public void setaccReceiptDAO(accReceiptDAO accReceiptDao) {
		this.accReceiptDao = accReceiptDao;
	}
	public void setAuthHandlerDAO(authHandlerDAO authHandlerDAO) {
        this.authHandlerDAO = authHandlerDAO;
    }
    public void setExportRecord(ExportRecord ExportrecordObj) {
        this.ExportrecordObj = ExportrecordObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setaccCostCenterDAO (AccCostCenterDAO accCostCenterDAOObj) {
        this.accCostCenterObj = accCostCenterDAOObj;
    }

    public void setAccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptDAOObj){
        this.accGoodsReceiptDAOObj = accGoodsReceiptDAOObj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCommon) {
        this.accGoodsReceiptCommon = accGoodsReceiptCommon;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccTaxDAO (accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }

    public ModelAndView getAccountOpeningBalance(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            Double openingbalance=0.0;
            Date startDate = authHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            openingbalance = getAccountBalance(request, request.getParameter("accountid"), null, startDate);
            JSONObject fobj=new JSONObject();
            fobj.put("openingbalance", new JSONArray("["+openingbalance+"]"));
            jobj.put("data", fobj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public double getAccountBalance(HttpServletRequest request, String accountid, Date startDate, Date endDate) throws ServiceException, SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("costcenter", request.getParameter("costcenter"));
        return getAccountBalance(requestParams, accountid, startDate, endDate);
    }
    public double getAccountBalance(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate) throws ServiceException {
        double amount = 0;
        try {
//            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
//            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
//        KWLCurrency currency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

//        Account account = (Account) session.get(Account.class, accountid);
            KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
            Account account = (Account) accresult.getEntityList().get(0);

            String costCenterId = (String)requestParams.get("costcenter");
            if(StringUtil.isNullOrEmpty(costCenterId)){ //Don't consider opening balance for CostCenter
                KwlReturnObject result = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,account.getOpeningBalance(),account.getCurrency().getCurrencyID(),account.getCreationDate(),0);
                amount = (Double) result.getEntityList().get(0);
            }

//            amount = account.getOpeningBalance();
            
//        String query = "select (case when debit=true then amount else -amount end) ,jed from JournalEntryDetail jed where account.ID=? and jed.journalEntry.deleted=false and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<? ";
//        ArrayList params = new ArrayList();
//        params.add(accountid);
//        if (startDate == null) {
//            startDate = new Date(0);
//        }
//        if (endDate == null) {
//            endDate = new Date();
//        }
//        params.add(startDate);
//        params.add(endDate);
//        List list = HibernateUtil.executeQuery(session, query, params.toArray());
            KwlReturnObject abresult = accJournalEntryobj.getAccountBalance(accountid, startDate, endDate, costCenterId);
            List list = abresult.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JournalEntryDetail jed = (JournalEntryDetail) row[1];
                String fromcurrencyid = (jed.getJournalEntry().getCurrency() == null ? currency.getCurrencyID() : jed.getJournalEntry().getCurrency().getCurrencyID());
//            amount += CompanyHandler.getCurrencyToBaseAmount(session, request, ((Double) row[0]).doubleValue(), fromcurrencyid, jed.getJournalEntry().getEntryDate());
                KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ((Double) row[0]).doubleValue(), fromcurrencyid, jed.getJournalEntry().getEntryDate(), jed.getJournalEntry().getExternalCurrencyRate());
                amount += (Double) crresult.getEntityList().get(0);
            }
            if (itr.hasNext()) {
                amount += ((Double) itr.next()).doubleValue();
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAccountBalance : "+ex.getMessage(), ex);
        }
        return amount;
    }

    public double[] getOpeningBalances(HttpServletRequest request, String companyid, Date endDate) throws ServiceException, SessionExpiredException {
        double[] balances = {0, 0};
//        String query="from Account ac where ac.company.companyID=?";
//        List list = HibernateUtil.executeQuery(session, query, companyid);
        HashMap<String, Object> filterParams = new HashMap<String, Object>();
        filterParams.put("companyid", companyid);
        KwlReturnObject accresult = accAccountDAOobj.getAccountEntry(filterParams);
        List list = accresult.getEntityList();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Account account = (Account) itr.next();
            Date createdOn = AccountingManager.resetTimeField(account.getCreationDate());
            Date toDate = AccountingManager.resetTimeField(endDate);
            if(toDate.compareTo(createdOn)<=0){
                continue;
            }
            
//            double bal= CompanyHandler.getCurrencyToBaseAmount(session,request,account.getOpeningBalance(),account.getCurrency().getCurrencyID(),account.getCreationDate());
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
           KwlReturnObject retObj = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,account.getOpeningBalance(),account.getCurrency().getCurrencyID(),account.getCreationDate(),0);
            double bal = (Double) retObj.getEntityList().get(0);//account.getOpeningBalance();//
            if (bal > 0) {
                balances[0] += bal;
            } else if (bal < 0) {
                balances[1] += bal;
            }
        }
        return balances;
    }

    public double[] getOpeningBalancesDateWise(HashMap requestMap, String companyid, Date startDate, Date endDate) throws ServiceException {
        double[] balances = {0, 0};
//        ArrayList params=new ArrayList();
//        params.add(companyid);
//        if(startDate==null)startDate=new Date(0);
//        if(endDate==null)endDate=new Date();
//        params.add(startDate);
//        params.add(endDate);
//        String query="from Account ac where ac.company.companyID=? and ac.creationDate>=? and  ac.creationDate<?";
//        List list = HibernateUtil.executeQuery(session, query,params.toArray());
//        Calendar stratCal= Calendar.getInstance();
//        stratCal.setTime(startDate);
//        stratCal.set(Calendar.MINUTE,0);
//        stratCal.set(Calendar.SECOND,0);
//        stratCal.set(Calendar.HOUR,0);
//        startDate=stratCal.getTime();

//        Calendar endCal= Calendar.getInstance();
//        endCal.setTime(endDate);
//        endCal.set(Calendar.MINUTE,0);
//        endCal.set(Calendar.SECOND,0);
//        endCal.set(Calendar.HOUR,0);
//        endDate=endCal.getTime();
        
        KwlReturnObject accresult = accAccountDAOobj.getAccountDatewise(companyid, startDate, endDate);
        List list = accresult.getEntityList();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Account account = (Account) itr.next();
            
            KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestMap,account.getOpeningBalance(),account.getCurrency().getCurrencyID(),account.getCreationDate(),0);
            double bal = (Double) crresult.getEntityList().get(0);
//            double bal = account.getOpeningBalance();
            if (bal > 0) {
                balances[0] += bal;
            } else if (bal < 0) {
                balances[1] += bal;
            }
        }
        return balances;
    }

    public double getAccountBalanceDateWise(HttpServletRequest request, String accountid, Date startDate, Date endDate) throws ServiceException, SessionExpiredException {
    double amount = 0;
    try {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        String currencyid = sessionHandlerImpl.getCurrencyID(request);
//        KWLCurrency currency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

//        Account account=(Account)session.get(Account.class, accountid);
        KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
        Account account = (Account) accresult.getEntityList().get(0);

//        ArrayList params = new ArrayList();
//        params.add(accountid);
        Calendar cal = Calendar.getInstance();
        cal.setTime(account.getCreationDate());
//        cal.set(Calendar.MINUTE, 0);
//        cal.set(Calendar.SECOND, 0);
//        cal.set(Calendar.HOUR, 0);
//        cal.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));

//        params.add(startDate);
//        params.add(endDate);
//        Calendar startCal= Calendar.getInstance();
//        startCal.setTime(startDate);
//        startCal.set(Calendar.MINUTE,0);
//        startCal.set(Calendar.SECOND,0);
//        startCal.set(Calendar.HOUR,0);
//
//        Calendar endCal= Calendar.getInstance();
//        endCal.setTime(endDate);
//        endCal.set(Calendar.MINUTE,0);
//        endCal.set(Calendar.SECOND,0);
//        endCal.set(Calendar.HOUR,0);
//        endDate=endCal.getTime();
        
        Date creationDate = account.getCreationDate();
//        startDate=startCal.getTime();
        if ((creationDate.after(startDate) || creationDate.equals(startDate)) && creationDate.before(endDate)) {
            KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,account.getOpeningBalance(),account.getCurrency().getCurrencyID(),account.getCreationDate(),0);
            amount = (Double) crresult.getEntityList().get(0);
//           amount = account.getOpeningBalance();
        }
//        String query = "select (case when debit=true then amount else -amount end) ,jed from JournalEntryDetail jed where account.ID=? and jed.journalEntry.deleted=false and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<? ";
//        List list = HibernateUtil.executeQuery(session, query, params.toArray());
        KwlReturnObject abresult = accJournalEntryobj.getAccountBalance(accountid, startDate, endDate);
        List list = abresult.getEntityList();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] row = (Object[]) itr.next();
            JournalEntryDetail jed = (JournalEntryDetail) row[1];
            String fromcurrencyid = (jed.getJournalEntry().getCurrency() == null ? currency.getCurrencyID() : jed.getJournalEntry().getCurrency().getCurrencyID());
//            amount += CompanyHandler.getCurrencyToBaseAmount(session, request, ((Double) row[0]).doubleValue(), fromcurrencyid, jed.getJournalEntry().getEntryDate());
            KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ((Double) row[0]).doubleValue(), fromcurrencyid, jed.getJournalEntry().getEntryDate(),0);
            amount += (Double) crresult.getEntityList().get(0);
        }
        if (itr.hasNext()) {
            amount += ((Double) itr.next()).doubleValue();
        }
    } catch (Exception ex) {
        throw ServiceException.FAILURE("getAccountBalanceDateWise : "+ex.getMessage(), ex);
    }
    return amount;
    }


    public ModelAndView getLedger(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getLedger(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getLedger(HttpServletRequest request) throws ServiceException, SessionExpiredException {
    JSONObject jobj = new JSONObject();
    try {
    	double total = 0;
        String accountid = request.getParameter("accountid");
        Date endDate = authHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
        KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
        Account account = (Account) accresult.getEntityList().get(0);
        //BUG Fixed #16739 : Creation date check
        Date createdOn = AccountingManager.resetTimeField(account.getCreationDate());
        Date toDate = AccountingManager.resetTimeField(endDate);
        if(toDate.compareTo(createdOn)<0){
            jobj.put("data", new JSONArray()); //Return Empty Data
            return jobj;
        }

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
//        KWLCurrency currency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            String entryChar = "c", emptyChar = "d";
//            String query="select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and je.entryDate between ? and ? and ac.company.companyID=? and je.deleted=false order by je.entryDate";
            Date startDate = authHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
//            Object[] params={
//                request.getParameter("accountid"),
//                startDate,
//                endDate,
//                AuthHandler.getCompanyid(request)
//            };
//            List list = HibernateUtil.executeQuery(session, query, params);
            KwlReturnObject lresult = accJournalEntryobj.getLedger(companyid, accountid, startDate, endDate);
            List list = lresult.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            double balance = getAccountBalance(request, accountid, null, startDate);
            if (balance != 0) {
                if (balance > 0) {
                    entryChar = "d";
                    emptyChar = "c";
                } else {
                    entryChar = "c";
                    emptyChar = "d";
                }
                JSONObject objlast = new JSONObject();
                objlast.put(entryChar + "_date", authHandler.getDateFormatter(request).format(startDate));
                objlast.put(entryChar + "_accountname", "Balance b/d");
                objlast.put(entryChar + "_journalentryid", "");
                objlast.put(entryChar + "_amount", Math.abs(balance));
                objlast.put(emptyChar + "_date", "");
                objlast.put(emptyChar + "_accountname", "");
                objlast.put(emptyChar + "_journalentryid", "");
                objlast.put(emptyChar + "_amount", "");
                jArr.put(objlast);
                
                if(request.getParameter("filetype") != null){
                	if(request.getParameter("filetype").equals("print")){
                		if(emptyChar == "d"){
                			total = total + Math.abs(balance);
                		}
                	}
                }
            }
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JournalEntry entry = (JournalEntry) row[0];
                JournalEntryDetail jed = (JournalEntryDetail) row[1];
                currencyid = (jed.getJournalEntry().getCurrency() == null ? currency.getCurrencyID() : jed.getJournalEntry().getCurrency().getCurrencyID());
                JSONObject obj = new JSONObject();
                if (jed.isDebit()) {
//                    balance += CompanyHandler.getCurrencyToBaseAmount(session, request, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate());
                    KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate(), jed.getJournalEntry().getExternalCurrencyRate());
                    balance += (Double) crresult.getEntityList().get(0);
                    entryChar = "d";
                    emptyChar = "c";
                } else {
//                    balance -= CompanyHandler.getCurrencyToBaseAmount(session, request, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate());
                    KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate(), jed.getJournalEntry().getExternalCurrencyRate());
                    balance -= (Double) crresult.getEntityList().get(0);
                    entryChar = "c";
                    emptyChar = "d";
                }
                Set details = entry.getDetails();
                Iterator iter = details.iterator();
                String accountName = "";
                while (iter.hasNext()) {
                    JournalEntryDetail d = (JournalEntryDetail) iter.next();
                    if (d.isDebit() == jed.isDebit()) {
                        continue;
                    }
                    accountName += d.getAccount().getName() + ", ";
                }
                accountName = accountName.substring(0, Math.max(0, accountName.length() - 2));
                obj.put(entryChar + "_date", authHandler.getDateFormatter(request).format(entry.getEntryDate()));
                obj.put(entryChar + "_accountname", accountName);
                obj.put(entryChar + "_entryno", entry.getEntryNumber());
                obj.put(entryChar + "_journalentryid", entry.getID());
//                obj.put(entryChar + "_amount", CompanyHandler.getCurrencyToBaseAmount(session, request, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate()));
                KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate(),jed.getJournalEntry().getExternalCurrencyRate());
                obj.put(entryChar + "_amount", (Double) crresult.getEntityList().get(0));
                obj.put(emptyChar + "_date", "");
                obj.put(emptyChar + "_accountname", "");
                obj.put(emptyChar + "_entryno", "");
                obj.put(emptyChar + "_journalentryid", "");
                obj.put(emptyChar + "_amount", "");
                jArr.put(obj);
                
                if(request.getParameter("filetype") != null){
                	if(request.getParameter("filetype").equals("print")){
                		if(emptyChar == "d"){
                			total = total + (Double) crresult.getEntityList().get(0);
                		}
                	}
                }
            }
            if (balance != 0) {
                if (balance > 0) {
                    entryChar = "c";
                    emptyChar = "d";
                } else {
                    entryChar = "d";
                    emptyChar = "c";
                }
                JSONObject objlast = new JSONObject();
                objlast.put(entryChar + "_date", request.getParameter("enddate"));
                objlast.put(entryChar + "_accountname", "Balance c/f");
                objlast.put(entryChar + "_journalentryid", "");
                objlast.put(entryChar + "_amount", Math.abs(balance));
                objlast.put(emptyChar + "_date", "");
                objlast.put(emptyChar + "_accountname", "");
                objlast.put(emptyChar + "_journalentryid", "");
                objlast.put(emptyChar + "_amount", "");
                jArr.put(objlast);
                
                if(request.getParameter("filetype") != null){
                	if(request.getParameter("filetype").equals("print")){
                		if(emptyChar == "d"){
                			total = total + Math.abs(balance);
                		}
                		JSONObject total1 = new JSONObject();
                		total1.put(entryChar + "_date", request.getParameter("enddate"));
                		total1.put(entryChar + "_accountname", "Total");
                		total1.put(entryChar + "_journalentryid", "");
                		total1.put(entryChar + "_amount", total);
                		total1.put(emptyChar + "_date", request.getParameter("enddate"));
                		total1.put(emptyChar + "_accountname", "Total");
                		total1.put(emptyChar + "_journalentryid", "");
                		total1.put(emptyChar + "_amount", total);
                        jArr.put(total1);
                	}
                }
            }
            jobj.put("data", jArr);
        } catch (ParseException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getLedger : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getLedger : "+ex.getMessage(), ex);
        }
        return jobj;
    }


    public ModelAndView getTrialBalance(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getTrialBalance(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getTrialBalance(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
//            String query="from Account ac where ac.company.companyID=? order by name";
//            List list = HibernateUtil.executeQuery(session, query, AuthHandler.getCompanyid(request));
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put("companyid", companyid);
            filterParams.put("order_by", "name");
            KwlReturnObject accresult = accAccountDAOobj.getAccountEntry(filterParams);
            List list = accresult.getEntityList();
            
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            JSONArray tmpjArr = new JSONArray();
            Date startDate = authHandler.getDateOnlyFormatter(request).parse(request.getParameter("stdate"));
            Date endDate = authHandler.getDateOnlyFormatter(request).parse(request.getParameter("enddate"));
//            double bals[]=getOpeningBalancesDateWise(session, AuthHandler.getCompanyid(request),startDate,endDate);
            double bals[] = getOpeningBalancesDateWise(requestParams, sessionHandlerImpl.getCompanyid(request), startDate, endDate);

            double balance = -(bals[1] + bals[0]);
            if (balance != 0) {
                JSONObject obj = new JSONObject();
                if (balance > 0) {
                    obj.put("d_amount", balance);
                    obj.put("c_amount", "");
                } else {
                    obj.put("c_amount", -balance);
                    obj.put("d_amount", "");
                }
                obj.put("accountid", "");
                obj.put("fmt", "A");
                obj.put("accountname", "Difference in Opening balances");
                jArr.put(obj);
            }
            while (itr.hasNext()) {
                Account account = (Account) itr.next();
//                double amount=getAccountBalanceDateWise(session,request,account.getID(),startDate,endDate);
                double amount = getAccountBalanceDateWise(request, account.getID(), startDate, endDate);
                if (amount == 0) {
                    continue;
                }
                JSONObject obj = new JSONObject();
                if (amount > 0) {
                    obj.put("d_amount", amount);
                    obj.put("c_amount", "");
                } else {
                    obj.put("c_amount", -amount);
                    obj.put("d_amount", "");
                }
                obj.put("accountid", account.getID());
                obj.put("accountname", account.getName());
                if (Double.isNaN(obj.optDouble("c_amount"))) {
                    jArr.put(obj);
                } else {
                    tmpjArr.put(obj);
                }
            }
            for (int i = 0; i < tmpjArr.length(); i++) {
                jArr.put(tmpjArr.getJSONObject(i));
            }
            jobj.put("data", jArr);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getTrialBalance : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTrialBalance : "+ex.getMessage(), ex);
        }
        return jobj;
    }


    public ModelAndView getReconciliationData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getReconciliationData(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getReconciliationData(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
//        KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String accountid = request.getParameter("accountid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            String entryChar = "c", emptyChar = "d";
//            String query="select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and je.deleted=false and je.entryDate between ? and ? and ac.company.companyID=? order by je.entryDate";

            Date startDate = authHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate = authHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
//            Object[] params={
//                request.getParameter("accountid"),
//                startDate,
//                endDate,
//                AuthHandler.getCompanyid(request)
//            };
//            List list = HibernateUtil.executeQuery(session, query, params);
            KwlReturnObject lresult = accJournalEntryobj.getLedger(companyid, accountid, startDate, endDate);
            List list = lresult.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArrL = new JSONArray();
            JSONArray jArrR = new JSONArray();
            double debitbalance = 0;
            double creditbalance = 0;
            double openingbalance = 0;

//            openingbalance=getAccountBalance(session,request, request.getParameter("accountid"), null, startDate);
            openingbalance = getAccountBalance(request, accountid, null, startDate);
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JournalEntry entry = (JournalEntry) row[0];
                JournalEntryDetail jed = (JournalEntryDetail) row[1];
                currencyid = (jed.getJournalEntry().getCurrency() == null ? currency.getCurrencyID() : jed.getJournalEntry().getCurrency().getCurrencyID());
                JSONObject obj = new JSONObject();
                if (jed.isDebit()) {
//                    debitbalance += CompanyHandler.getCurrencyToBaseAmount(session, request, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate());
                    KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate(),jed.getJournalEntry().getExternalCurrencyRate());
                    debitbalance += (Double) crresult.getEntityList().get(0);
                    entryChar = "d";
                    emptyChar = "c";
                } else {
//                    creditbalance += CompanyHandler.getCurrencyToBaseAmount(session, request, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate());
                    KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate(),jed.getJournalEntry().getExternalCurrencyRate());
                    creditbalance += (Double) crresult.getEntityList().get(0);
                    entryChar = "c";
                    emptyChar = "d";
                }
                Set details = entry.getDetails();
                Iterator iter = details.iterator();
                String accountName = "";
                while (iter.hasNext()) {
                    JournalEntryDetail d = (JournalEntryDetail) iter.next();
                    if (d.isDebit() == jed.isDebit()) {
                        continue;
                    }
                    accountName += d.getAccount().getName() + ", ";
                }
                accountName = accountName.substring(0, Math.max(0, accountName.length() - 2));

                obj.put(entryChar + "_date", authHandler.getDateFormatter(request).format(entry.getEntryDate()));
                obj.put(entryChar + "_accountname", accountName);
                obj.put(entryChar + "_entryno", entry.getEntryNumber());
                obj.put(entryChar + "_journalentryid", entry.getID());
//                obj.put(entryChar + "_amount", CompanyHandler.getCurrencyToBaseAmount(session, request, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate()));
                KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate(),jed.getJournalEntry().getExternalCurrencyRate());
                obj.put(entryChar + "_amount", (Double) crresult.getEntityList().get(0));
                obj.put(emptyChar + "_date", "");
                obj.put(emptyChar + "_accountname", "");
                obj.put(emptyChar + "_entryno", "");
                obj.put(emptyChar + "_journalentryid", "");
                obj.put(emptyChar + "_amount", "");
                if (entryChar.equals("d")) {
                    jArrL.put(obj);
                } else {
                    jArrR.put(obj);
                }
            }
            JSONObject fobj = new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("openingbalance", new JSONArray("[" + openingbalance + "]"));
            fobj.put("total", new JSONArray("[" + debitbalance + "," + -creditbalance + "]"));
            jobj.put("data", fobj);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getReconciliationData : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReconciliationData : " + ex.getMessage(), ex);
        }
        return jobj;
    }


    public ModelAndView getTrading(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getTrading(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getTrading(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj=new JSONObject();
		try {
            double dtotal=0,ctotal=0;
            JSONArray jArrL=new JSONArray();
            JSONArray jArrR=new JSONArray();
            JSONObject objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Debit)</div>");
            objlast.put("fmt", "H");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Credit)</div>");
            objlast.put("fmt", "H");
//            jArrR.put(objlast);

//            dtotal=getTrading(session, request, Group.NATURE_EXPENSES, jArrL);
            dtotal=getTrading(request, Group.NATURE_EXPENSES, jArrL);
//            ctotal=getTrading(session, request, Group.NATURE_INCOME, jArrR);
            ctotal=getTrading(request, Group.NATURE_INCOME, jArrR);

            double balance=dtotal+ctotal;
            if(balance>0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", "Gross Loss");
                objlast.put("amount", balance);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal-=balance;
            }
            if(balance<0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("accountname", "Gross Profit");
                objlast.put("amount", -balance);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal-=balance;
            }
            objlast = new JSONObject();
            objlast.put("accountname", "Total Debit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Total Credit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", -ctotal);
            objlast.put("fmt", "T");
//            jArrR.put(objlast);

            JSONObject fobj=new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("["+dtotal+","+-ctotal+"]"));
            jobj.put("data", fobj);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTrading : "+ex.getMessage(), ex);
        }
        return jobj;
    }

    public double getTrading(HttpServletRequest request, int nature, JSONArray jArr) throws ServiceException, SessionExpiredException {
        double total = 0;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String query="from Group where parent is null and nature in ("+nature+") and affectGrossProfit=true and (company is null or company.companyID=?) order by nature, displayOrder";
//            List list = HibernateUtil.executeQuery(session, query, AuthHandler.getCompanyid(request));
            KwlReturnObject plresult = accAccountDAOobj.getGroupForProfitNloss(companyid, nature, true);
            List list = plresult.getEntityList();
            Iterator itr = list.iterator();

            Date startDate = authHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate = authHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            while (itr.hasNext()) {
                Group group = (Group) itr.next();
//                total += formatGroupDetails(session, request, AuthHandler.getCompanyid(request), group, startDate, endDate, 0, false, jArr);
                total += formatGroupDetails(request, companyid, group, startDate, endDate, 0, false, jArr);
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getTrading : "+ex.getMessage(), ex);
        }
        return total;
    }

    private double formatGroupDetails(HttpServletRequest request, String companyid, Group group, Date startDate, Date endDate, int level, boolean isBalanceSheet, JSONArray jArr) throws ServiceException, SessionExpiredException {
        double totalAmount = 0;
        boolean isDebit = false;
        try {
            if (isBalanceSheet) {
                if (group.getNature() == Group.NATURE_LIABILITY) {
                    isDebit = true;
                }
            } else if (group.getNature() == Group.NATURE_EXPENSES) {
                isDebit = true;
            }
            Set children = group.getChildren();
            JSONArray chArr = new JSONArray();
//            String query="from Account where parent is null and group.ID=? and company.companyID=?";
//            List list = HibernateUtil.executeQuery(session, query, new Object[]{group.getID(),companyid});
            HashMap<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put("companyid", companyid);
            filterParams.put("groupid", group.getID());
            filterParams.put("parent", null);
            KwlReturnObject accresult = accAccountDAOobj.getAccountEntry(filterParams);
            List list = accresult.getEntityList();

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Account account = (Account) itr.next();
//                totalAmount += formatAccountDetails(session, request, account, startDate, endDate, level + 1, isDebit, isBalanceSheet, chArr);
                totalAmount += formatAccountDetails(request, account, startDate, endDate, level + 1, isDebit, isBalanceSheet, chArr);
            }
            
            if (children != null && !children.isEmpty()) {
                itr = children.iterator();
                while (itr.hasNext()) {
                    Group child = (Group) itr.next();
//                    totalAmount+=formatGroupDetails(session,request, companyid, child, startDate, endDate, level+1, isBalanceSheet, chArr);
                    totalAmount += formatGroupDetails(request, companyid, child, startDate, endDate, level + 1, isBalanceSheet, chArr);
                }
            }

            if (chArr.length() > 0) {
                JSONObject obj = new JSONObject();
                obj.put("accountname", group.getName());
                obj.put("accountid", group.getID());
                obj.put("level", level);
                obj.put("leaf", false);
                obj.put("amount", "");
                obj.put("isdebit", isDebit);
                jArr.put(obj);
                for (int i = 0; i < chArr.length(); i++) {
                    jArr.put(chArr.getJSONObject(i));
                }

                obj = new JSONObject();
                obj.put("accountname", "Total " + group.getName());
                obj.put("accountid", group.getID());
                obj.put("level", level);
                obj.put("leaf", true);
                obj.put("show", true);
                double ta = totalAmount;
                if (!isDebit) {
                    ta = -ta;
                }
                if (isBalanceSheet) {
                    ta = -ta;
                }
                obj.put("amount", ta);
                obj.put("isdebit", isDebit);
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("formatGroupDetails : " + ex.getMessage(), ex);
        }
        return totalAmount;
    }

    private double formatAccountDetails(HttpServletRequest request, Account account, Date startDate, Date endDate, int level, boolean isDebit, boolean isBalanceSheet, JSONArray jArr) throws ServiceException, SessionExpiredException {
//        double amount = getAccountBalance(session, request, account.getID(), startDate, endDate);
        boolean isDeleted = false;
        if(account.isDeleted()){ //BUG #16733: Deleted account check for sub Assets/Account
            isDeleted = true;
        }
        if(account.getGroup()!= null && account.getGroup().getID().equalsIgnoreCase(Group.FIXED_ASSETS)){ //BUG Fixed #16739 : Creation date check for Fixed Assets
            Date createdOn = AccountingManager.resetTimeField(account.getCreationDate());
            Date toDate = AccountingManager.resetTimeField(endDate);
            if(toDate.compareTo(createdOn)<=0){
                isDeleted = true;
            }
        }
        double amount = 0;
        if(!isDeleted){
            amount = getAccountBalance(request, account.getID(), startDate, endDate);
        }
        double totalAmount = amount;

        Iterator<Account> itr = account.getChildren().iterator();
        JSONArray chArr = new JSONArray();
        while (itr.hasNext()) {
            Account child = itr.next();
//            totalAmount += formatAccountDetails(session, request, child, startDate, endDate, level + 1, isDebit, isBalanceSheet, chArr);
            totalAmount += formatAccountDetails(request, child, startDate, endDate, level + 1, isDebit, isBalanceSheet, chArr);
        }

        try {
            if (chArr.length() > 0) {
                JSONObject obj = new JSONObject();
                obj.put("accountname", account.getName());
                obj.put("accountid", account.getID());
                obj.put("level", level);
                obj.put("leaf", false);
                obj.put("amount", "");
                obj.put("isdebit", isDebit);
                jArr.put(obj);
                for (int i = 0; i < chArr.length(); i++) {
                    jArr.put(chArr.getJSONObject(i));
                }
                
                if (amount != 0) {
                    obj = new JSONObject();
                    obj.put("accountname", "Other " + account.getName());
                    obj.put("accountid", account.getID());
                    obj.put("level", level + 1);
                    obj.put("leaf", true);
                    if (!isDebit) {
                        amount = -amount;
                    }
                    if (isBalanceSheet) {
                        amount = -amount;
                    }
                    obj.put("amount", amount);
                    obj.put("isdebit", isDebit);
                    jArr.put(obj);
                }

                obj = new JSONObject();
                obj.put("accountname", "Total " + account.getName());
                obj.put("accountid", account.getID());
                obj.put("level", level);
                obj.put("leaf", true);
                obj.put("show", true);
                double ta = totalAmount;
                if (!isDebit) {
                    ta = -ta;
                }
                if (isBalanceSheet) {
                    ta = -ta;
                }
                obj.put("amount", ta);
                obj.put("isdebit", isDebit);
                jArr.put(obj);
            } else if (amount != 0) {
                JSONObject obj = new JSONObject();
                obj.put("accountname", account.getName());
                obj.put("accountid", account.getID());
                obj.put("level", level);
                obj.put("leaf", true);
                if (!isDebit) {
                    amount = -amount;
                }
                if (isBalanceSheet) {
                    amount = -amount;
                }
                obj.put("amount", (amount != 0.0 ? amount : ""));
                obj.put("isdebit", isDebit);
                jArr.put(obj);
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("formatAccountDetails : " + e.getMessage(), e);
        }
        return totalAmount;
    }


    public ModelAndView getProfitLoss(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getProfitLoss(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getProfitLoss(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            double dtotal = 0, ctotal = 0;
            JSONArray jArrL = new JSONArray();
            JSONArray jArrR = new JSONArray();
            JSONObject objlast = new JSONObject();
//            double balance=getTrading(session, request, Group.NATURE_EXPENSES, new JSONArray())+
//                    getTrading(session, request, Group.NATURE_INCOME, new JSONArray());
            double balance = getTrading(request, Group.NATURE_EXPENSES, new JSONArray()) +
                    getTrading(request, Group.NATURE_INCOME, new JSONArray());
            objlast.put("accountname", messageSource.getMessage("", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.3", null, RequestContextUtils.getLocale(request))+"</div>");   //Amount (Debit)
            objlast.put("fmt", "H");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.4", null, RequestContextUtils.getLocale(request))+"</div>");   // Amount (Credit)
            objlast.put("fmt", "H");
//            jArrR.put(objlast);
            if (balance > 0) {
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Gross Loss");
                objlast.put("amount", balance);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal = balance;
            }
            if (balance < 0) {
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", messageSource.getMessage("acc.report.6", null, RequestContextUtils.getLocale(request)));  //"Gross Profit");
                objlast.put("amount", -balance);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal = balance;
            }

//            dtotal += getProfitLoss(session, request, Group.NATURE_EXPENSES, jArrL);
            dtotal += getProfitLoss(request, Group.NATURE_EXPENSES, jArrL);
//            ctotal += getProfitLoss(session, request, Group.NATURE_INCOME, jArrR);
            ctotal += getProfitLoss(request, Group.NATURE_INCOME, jArrR);
            
            balance = dtotal + ctotal;
            if (balance > 0) {
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", messageSource.getMessage("acc.report.9", null, RequestContextUtils.getLocale(request)));  //"Net Loss");
                objlast.put("amount", balance);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal -= balance;
            }
            if (balance < 0) {
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("accountname", messageSource.getMessage("acc.report.10", null, RequestContextUtils.getLocale(request)));  //"Net Profit");
                objlast.put("amount", -balance);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal -= balance;
            }
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.7", null, RequestContextUtils.getLocale(request)));  //"Total Debit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.8", null, RequestContextUtils.getLocale(request)));  //"Total Credit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", -ctotal);
            objlast.put("fmt", "T");
//            jArrR.put(objlast);

            JSONObject fobj = new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("[" + dtotal + "," + -ctotal + "]"));
            jobj.put("data", fobj);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getProfitLoss : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public double getProfitLoss(HttpServletRequest request, int nature, JSONArray jArr) throws ServiceException, SessionExpiredException {
        double total = 0;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String query="from Group where parent is null and nature in ("+nature+") and affectGrossProfit=false and (company is null or company.companyID=?) order by nature, displayOrder";
//            List list = HibernateUtil.executeQuery(session, query, AuthHandler.getCompanyid(request));
            KwlReturnObject plresult = accAccountDAOobj.getGroupForProfitNloss(companyid, nature, false);
            List list = plresult.getEntityList();
            Iterator itr = list.iterator();

            Date startDate = authHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate = authHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            while (itr.hasNext()) {
                Group group = (Group) itr.next();
//                total+=formatGroupDetails(session,request, AuthHandler.getCompanyid(request), group, startDate, endDate, 0, false, jArr);
                total += formatGroupDetails(request, companyid, group, startDate, endDate, 0, false, jArr);
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getProfitLoss : " + ex.getMessage(), ex);
        }
        return total;
    }


    public ModelAndView getTradingAndProfitLoss(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getTradingAndProfitLoss(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     
    public ModelAndView exportTradingAndProfitLoss(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                jobj = getExportBalanceSheetJSON(getTradingAndProfitLoss(request),2, 0);
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
                exportDaoObj.processRequest(request, response, jobj);
            } else {
                if(fileType.equals("csv")){
                    jobj = getExportBalanceSheetJSON(getTradingAndProfitLoss(request),2, 0);
                    exportDaoObj.processRequest(request, response, jobj);
                }
                else{
                    jobj = getTradingAndProfitLoss(request);
                    String currencyid = sessionHandlerImpl.getCurrencyID(request);
                    java.text.DateFormat formatter = authHandler.getDateOnlyFormatter(request);
                    String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                    java.io.ByteArrayOutputStream baos = null;
                    String filename = request.getParameter("filename");
                    String comName = sessionHandlerImpl.getCompanyName(request);
                    Date endDate=authHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
                    Date startDate=authHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
                    Calendar cal = Calendar.getInstance();
                    cal.set(1900+endDate.getYear(), endDate.getMonth(), endDate.getDate());
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    endDate = cal.getTime(); //Get actual end date i.e. 1 day before given date
                    baos = ExportrecordObj.exportBalanceSheetPdf(request, currencyid, formatter, logoPath, comName, jobj,startDate,endDate,2, 0);
                    if (baos != null) {
                        ExportrecordObj.writeDataToFile(filename+"."+fileType, baos, response);
                    }

                }
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }


    public JSONObject getTradingAndProfitLoss(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        double invOpeBal=0, invCloseBal=0, assemblyValuation=0;
        try {
            String costCenterId = request.getParameter("costcenter"); //Filter for costcenter
            String reportView = request.getParameter("reportView"); //"TradingAndProfitLoss","CostCenter"
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double dtotal = 0, ctotal = 0;
            JSONArray jArrL = new JSONArray();
            JSONArray jArrR = new JSONArray();
            JSONObject objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.3", null, RequestContextUtils.getLocale(request))+"</div>");    // Amount (Debit)
            objlast.put("fmt", "H");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.4", null, RequestContextUtils.getLocale(request))+"</div>");      //Amount (Credit)
            objlast.put("fmt", "H");
//            jArrR.put(objlast);
            Date startDate=authHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate=authHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            if(!"CostCenter".equalsIgnoreCase(reportView) && StringUtil.isNullOrEmpty(costCenterId)){ //Don't show Opening/Closing Stock for any Cost-Center
                JSONObject jObjX= getInventoryOpeningBalance(request, companyid, startDate);
                JSONArray jarr = jObjX.getJSONArray("data");
                for (int i = 0; i < jarr.length(); i++) {
                    invOpeBal+= jarr.getJSONObject(i).getDouble("valuation");
                }
                jObjX=new JSONObject();
                jObjX =jObjX =getInventoryOpeningBalance(request, companyid, endDate);
                jarr = jObjX.getJSONArray("data");
                for (int i = 0; i < jarr.length(); i++) {
                    JSONObject productJson = jarr.getJSONObject(i);
                    invCloseBal += productJson.getDouble("valuation");
                     if(productJson.has("productTypeID") && productJson.getString("productTypeID").equals(Producttype.ASSEMBLY)){
                        assemblyValuation += productJson.getDouble("valuation");
                    }
                }

                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.13", null, RequestContextUtils.getLocale(request)));
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", invOpeBal);
                objlast.put("fmt", "H");
                jArrL.put(objlast);
            }
//            dtotal = getTrading(session, request, Group.NATURE_EXPENSES, jArrL);
            dtotal = getTrading(request, Group.NATURE_EXPENSES, jArrL);
//            ctotal = getTrading(session, request, Group.NATURE_INCOME, jArrR);
            ctotal = getTrading(request, Group.NATURE_INCOME, jArrR);

            dtotal+=invOpeBal;
            ctotal-=invCloseBal;

            if(!"CostCenter".equalsIgnoreCase(reportView) && StringUtil.isNullOrEmpty(costCenterId)){ //Don't show Opening/Closing Stock for any Cost-Center
                JSONObject obj = new JSONObject();
                if(invCloseBal+assemblyValuation > 0){ //Show details if Closing_Stock > 0
                    obj.put("accountname", messageSource.getMessage("acc.report.17", null, RequestContextUtils.getLocale(request)));  //"Closing Stock");
                    obj.put("accountid", "");
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", false);
                    obj.put("amount", "");
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname",messageSource.getMessage("acc.report.14", null, RequestContextUtils.getLocale(request)));  // "Finish Products (Total Value of \"Inventory Assembly\" products)");
                    obj.put("accountid", "");
                    obj.put("level", 1);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", assemblyValuation);
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname", messageSource.getMessage("acc.report.15", null, RequestContextUtils.getLocale(request)));  //"Raw Materials (Total Value of \"Inventory Item\" products)");
                    obj.put("accountid", "");
                    obj.put("level", 1);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", invCloseBal-assemblyValuation);
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname", messageSource.getMessage("acc.report.16", null, RequestContextUtils.getLocale(request)));  //"Total Closing Stock");
                    obj.put("accountid", "");
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", invCloseBal);
                    objlast.put("fmt", "H");
                    jArrR.put(obj);
                } else { // Show single line if Closing_Stock = 0
                    obj = new JSONObject();
                    obj.put("accountname",messageSource.getMessage("acc.report.17", null, RequestContextUtils.getLocale(request)));  // "Closing Stock");
                    obj.put("accountid", "");
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", invCloseBal);
                    obj.put("fmt", "H");
                    jArrR.put(obj);
                }
            }

            if(!"CostCenter".equalsIgnoreCase(reportView)) {//Don't Adjust report layout for cost center report
                int len = jArrL.length() - jArrR.length(); //Adjust report layout by equaling no. of rows
                JSONArray jArr = jArrR;
                if (len < 0) {
                    len = -len;
                    jArr = jArrL;
                }
                for (int i = 0; i < len; i++) {
                    jArr.put(new JSONObject());
                }
            }

            double balance = dtotal + ctotal;
            if(!"CostCenter".equalsIgnoreCase(reportView)) {//Don't show GrossLoss,GrossProfit for cost center report
                if (balance > 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", false);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.5", null, RequestContextUtils.getLocale(request)));  //"Gross Loss");
                    objlast.put("amount", balance);
                    objlast.put("fmt", "B");
                    jArrR.put(objlast);
                    jArrL.put(new JSONObject());
                    ctotal -= balance;
                }
                if (balance < 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.6", null, RequestContextUtils.getLocale(request)));  //"Gross Profit");
                    objlast.put("amount", balance==0?balance:-balance);//Remove '-' sign if 0
                    objlast.put("fmt", "B");
                    jArrL.put(objlast);
                    jArrR.put(new JSONObject());
                    dtotal -= balance;
                }
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.7", null, RequestContextUtils.getLocale(request)));  //"Total Debit");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", dtotal);
                objlast.put("fmt", "T");
                jArrL.put(objlast);
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.8", null, RequestContextUtils.getLocale(request)));  //"Total Credit");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("amount", ctotal==0?ctotal:-ctotal);//Remove '-' sign if 0
                objlast.put("fmt", "T");
                jArrR.put(objlast);

                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.3", null, RequestContextUtils.getLocale(request))+"</div>");   //Amount (Debit)
                objlast.put("fmt", "H");
                jArrL.put(objlast);
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("amount", "<div align=right>"+messageSource.getMessage("acc.report.4", null, RequestContextUtils.getLocale(request))+"</div>");       //Amount (Credit)
                objlast.put("fmt", "H");
                jArrR.put(objlast);
                dtotal = 0;
                ctotal = 0;
                if (balance > 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.5", null, RequestContextUtils.getLocale(request)));  //"Gross Loss");
                    objlast.put("amount", balance);
                    objlast.put("fmt", "B");
                    jArrL.put(objlast);
                    dtotal = balance;
                }
                if (balance < 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", false);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.6", null, RequestContextUtils.getLocale(request)));  //"Gross Profit");
                    objlast.put("amount", balance==0?balance:-balance);//Remove '-' sign if 0
                    objlast.put("fmt", "B");
                    jArrR.put(objlast);
                    ctotal = balance;
                }
            }

//            dtotal += getProfitLoss(session, request, Group.NATURE_EXPENSES, jArrL);
            dtotal += getProfitLoss(request, Group.NATURE_EXPENSES, jArrL);
//            ctotal += getProfitLoss(session, request, Group.NATURE_INCOME, jArrR);
            ctotal += getProfitLoss(request, Group.NATURE_INCOME, jArrR);

            if(!"CostCenter".equalsIgnoreCase(reportView)) { //Don't show NetLoss,NetProfit for cost center report
                balance = dtotal + ctotal;
                if (balance > 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", false);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.9", null, RequestContextUtils.getLocale(request)));  //"Net Loss");
                    objlast.put("amount", balance);
                    objlast.put("fmt", "B");
                    jArrR.put(objlast);
                    ctotal -= balance;
                }
                if (balance < 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "");
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.10", null, RequestContextUtils.getLocale(request)));  //"Net Profit");
                    objlast.put("amount", balance==0?balance:-balance);//Remove '-' sign if 0
                    objlast.put("fmt", "B");
                    jArrL.put(objlast);
                    dtotal -= balance;
                }
            }

            if("CostCenter".equalsIgnoreCase(reportView)) { //Add LIABILITY for cost center report (Tax Amount)
                KwlReturnObject ret =  accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Group", Group.OTHER_CURRENT_LIABILITIES);
                if(!ret.getEntityList().isEmpty()){
                    Group liab_group = (Group) ret.getEntityList().get(0);
                    ctotal += formatGroupDetails(request, companyid, liab_group, startDate, endDate, 0, true, jArrR); //Bug Fixed #16746
                    liab_group.getName();
                }
            }

            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.7", null, RequestContextUtils.getLocale(request)));  //"Total Debit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.8", null, RequestContextUtils.getLocale(request)));  //"Total Credit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", ctotal==0?ctotal:-ctotal);//Remove '-' sign if 0
            objlast.put("fmt", "T");
//            jArrR.put(objlast);

            JSONObject fobj = new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("[" + dtotal + "," + (ctotal==0?ctotal:-ctotal) + "]"));
            jobj.put("data", fobj);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : "+ex.getMessage(), ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : "+e.getMessage(), e);
        }
        return jobj;
    }


    public ModelAndView getBalanceSheet(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getBalanceSheet(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public JSONObject getExportBalanceSheetJSON(JSONObject jobj, int flag, int toggle){
        JSONObject retObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        JSONArray rightObjArr = new JSONArray();
        JSONArray leftObjArr = new JSONArray();
        try{
            jobj = jobj.getJSONObject("data");
            if(toggle == 0){
            	rightObjArr = jobj.getJSONArray("right");
            	leftObjArr = jobj.getJSONArray("left");
            }else{
            	rightObjArr = jobj.getJSONArray("left");
                leftObjArr = jobj.getJSONArray("right");
            }
            int length = leftObjArr.length()>rightObjArr.length()?leftObjArr.length():rightObjArr.length();
            for(int i=0;i<length;i++){
                JSONObject tempObj = new JSONObject();
                if(i<leftObjArr.length() && !leftObjArr.getJSONObject(i).toString().equalsIgnoreCase("{}")){
                    JSONObject leftObj = leftObjArr.getJSONObject(i);
                    tempObj.put("laccountname",leftObj.get("accountname"));
                    tempObj.put("laccountid",leftObj.get("accountid"));
                    tempObj.put("llevel",leftObj.get("level"));
                    tempObj.put("lisdebit",leftObj.get("isdebit"));
                    tempObj.put("lleaf",leftObj.get("leaf"));
                    tempObj.put("lamount",com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.get("amount").toString()));/*
                    tempObj.put("lfmt",leftObj.get("fmt"));*/
                }else{
                    tempObj.put("laccountname","");
                    tempObj.put("laccountid","");
                    tempObj.put("llevel","");
                    tempObj.put("lisdebit","");
                    tempObj.put("lleaf","");
                    tempObj.put("lamount","");
                }
                if(i<rightObjArr.length() && !rightObjArr.getJSONObject(i).toString().equalsIgnoreCase("{}")){
                    JSONObject rightObj = rightObjArr.getJSONObject(i);
                     tempObj.put("raccountname",rightObj.get("accountname"));
                     tempObj.put("raccountid",rightObj.get("accountid"));
                     tempObj.put("rlevel",rightObj.get("level"));
                     tempObj.put("risdebit",rightObj.get("isdebit"));
                     tempObj.put("rleaf",rightObj.get("leaf"));
                     tempObj.put("ramount",com.krawler.common.util.StringUtil.serverHTMLStripper(rightObj.get("amount").toString()));/*
                     tempObj.put("rfmt",rightObj.get("fmt"));*/
                }else{
                     tempObj.put("raccountname","");
                     tempObj.put("raccountid","");
                     tempObj.put("rlevel","");
                     tempObj.put("risdebit","");
                     tempObj.put("rleaf","");
                     tempObj.put("ramount","");
                }
                jArr.put(tempObj);
            }
            if(flag!=-1){
                double totalAsset=0, totalLibility=0;
                JSONArray finalValArr = jobj.getJSONArray("total");
                totalAsset = Double.parseDouble(finalValArr.getString(0));
                totalLibility = Double.parseDouble(finalValArr.getString(1));
                String leftSummaryHeader = "", rightSummaryHeader = "";
                if(flag==1){
                	if(toggle == 0){
                		leftSummaryHeader = "Total Liability";
                		rightSummaryHeader = "Total Asset";
                	}else{
                		leftSummaryHeader = "Total Asset";
                		rightSummaryHeader = "Total Liability";
                	}
                }else if(flag==2){
                    leftSummaryHeader = "Total Debit";
                    rightSummaryHeader = "Total Credit";
                }

                JSONObject tempObj = new JSONObject();
                tempObj.put("laccountname",leftSummaryHeader);
                tempObj.put("laccountid","");
                tempObj.put("llevel","");
                tempObj.put("lisdebit","");
                tempObj.put("lleaf","");
                tempObj.put("lamount",totalAsset);

                tempObj.put("raccountname",rightSummaryHeader);
                tempObj.put("raccountid","");
                tempObj.put("rlevel","");
                tempObj.put("risdebit","");
                tempObj.put("rleaf","");
                tempObj.put("ramount",totalLibility);
                jArr.put(tempObj);
            }
            retObj.put("data",jArr);
        }catch(Exception ex){
             Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retObj;
    }
     public ModelAndView exportBalanceSheet(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                jobj = getExportBalanceSheetJSON(getBalanceSheet(request),1, Integer.parseInt(request.getParameter("toggle")));
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
                exportDaoObj.processRequest(request, response, jobj);
            } else {
                if (StringUtil.equal(fileType, "csv")) {
                    jobj = getExportBalanceSheetJSON(getBalanceSheet(request),1,Integer.parseInt(request.getParameter("toggle")));
                    exportDaoObj.processRequest(request, response, jobj);
                }else{
                    jobj = getBalanceSheet(request);
                    String currencyid = sessionHandlerImpl.getCurrencyID(request);
                    java.text.DateFormat formatter = authHandler.getDateOnlyFormatter(request);
                    String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                    java.io.ByteArrayOutputStream baos = null;
                    String filename = request.getParameter("filename");
                    String comName = sessionHandlerImpl.getCompanyName(request);
                    Date endDate=authHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
                    baos = ExportrecordObj.exportBalanceSheetPdf(request, currencyid, formatter, logoPath, comName, jobj,null,endDate,1, Integer.parseInt(request.getParameter("toggle")));
                    if (baos != null) {
                        ExportrecordObj.writeDataToFile(filename+"."+fileType, baos, response);
                    }

                }
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public JSONObject getBalanceSheet(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            double dtotal=0, ctotal=0, invCloseBal=0,invOpeBal=0;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Date startDate=authHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate=authHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            JSONObject jObjX= getInventoryOpeningBalance(request, companyid, startDate);
            JSONArray jarr = jObjX.getJSONArray("data");
            for (int i = 0; i < jarr.length(); i++) {
                invOpeBal+= jarr.getJSONObject(i).getDouble("valuation");
            }
            jObjX=new JSONObject();
            jObjX =getInventoryOpeningBalance(request, companyid, endDate);
            jarr = jObjX.getJSONArray("data");
            for (int i = 0; i < jarr.length(); i++) {
                invCloseBal+= jarr.getJSONObject(i).getDouble("valuation");
            }
//            double balance=getTrading(session, request, Group.NATURE_EXPENSES, new JSONArray())+
//                    getTrading(session, request, Group.NATURE_INCOME, new JSONArray())+
//                    getProfitLoss(session, request, Group.NATURE_EXPENSES, new JSONArray())+
//                    getProfitLoss(session, request, Group.NATURE_INCOME, new JSONArray());
            double balance = getTrading(request, Group.NATURE_EXPENSES, new JSONArray()) -invCloseBal +
                    getTrading(request, Group.NATURE_INCOME, new JSONArray()) +
                    getProfitLoss(request, Group.NATURE_EXPENSES, new JSONArray()) +
                    getProfitLoss(request, Group.NATURE_INCOME, new JSONArray()) +invOpeBal;
            JSONArray jArrL = new JSONArray();
            JSONArray jArrR = new JSONArray();
            JSONObject objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+ messageSource.getMessage("acc.report.18", null, RequestContextUtils.getLocale(request))+"</div>");   //       Amount (Assets)
            objlast.put("fmt", "H");
//            jArrR.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)));  //"Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>"+ messageSource.getMessage("acc.report.19", null, RequestContextUtils.getLocale(request))+"</div>");     //Amount (Liabilities)
            objlast.put("fmt", "H");
//            jArrL.put(objlast);

//            dtotal=-getBalanceSheet(session, request, Group.NATURE_LIABILITY, jArrL);
            dtotal = -getBalanceSheet(request, Group.NATURE_LIABILITY, jArrL);
//            ctotal=-getBalanceSheet(session, request, Group.NATURE_ASSET, jArrR);
            ctotal = -getBalanceSheet(request, Group.NATURE_ASSET, jArrR);

            System.out.println(dtotal + "," + ctotal);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.17", null, RequestContextUtils.getLocale(request)));  //"Closing Stock");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", invCloseBal);
            objlast.put("fmt", "H");
            jArrR.put(objlast);
            
            if (balance > 0) {
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.9", null, RequestContextUtils.getLocale(request)));  //"Net Loss");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("leaf", true);
                objlast.put("amount", balance);
                objlast.put("isdebit", false);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal -= balance;
            }

            if (balance < 0) {
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.10", null, RequestContextUtils.getLocale(request)));  //"Net Profit");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("leaf", true);
                objlast.put("amount", -balance);
                objlast.put("isdebit", true);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal -= balance;
            }

//            double bals[]=getOpeningBalances(session, request, AuthHandler.getCompanyid(request));
            double bals[] = getOpeningBalances(request, sessionHandlerImpl.getCompanyid(request), endDate);

            balance = bals[0] + bals[1];////+invCloseBal;
            ctotal-=invCloseBal;
            if (balance != 0) {
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.1", null, RequestContextUtils.getLocale(request)));  //"Difference in Opening balances");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("fmt", "A");
                objlast.put("amount", Math.abs(balance));
                objlast.put("leaf", true);
                objlast.put("isdebit", balance > 0);
                // balance+=invCloseBal;
                if (balance > 0) {
                    dtotal += balance;
                    jArrL.put(objlast);
                } else {
                    ctotal += balance;
                    jArrR.put(objlast);
                }
            }
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.20", null, RequestContextUtils.getLocale(request)));  //"Total Assets");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
//            jArrR.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", messageSource.getMessage("acc.report.21", null, RequestContextUtils.getLocale(request)));  //"Total Liabilities");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", -ctotal);
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            JSONObject fobj = new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("[" + dtotal + "," + -ctotal + "]"));
            jobj.put("data", fobj);
        } catch (ParseException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBalanceSheet : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public double getBalanceSheet(HttpServletRequest request, int nature, JSONArray jArr) throws ServiceException, SessionExpiredException {
        double total=0;
		try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String query="from Group where parent is null and nature in ("+nature+") and affectGrossProfit=false and (company is null or company.companyID=?) order by nature desc, displayOrder";
//            List list = HibernateUtil.executeQuery(session, query,AuthHandler.getCompanyid(request));
            KwlReturnObject plresult = accAccountDAOobj.getGroupForProfitNloss(companyid, nature, false);
            List list = plresult.getEntityList();
            Iterator itr = list.iterator();

            Date startDate=authHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate=authHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            while(itr.hasNext()) {
                Group group=(Group)itr.next();
//                total+=formatGroupDetails(session,request, AuthHandler.getCompanyid(request), group, null, endDate, 0, true, jArr);
                total+=formatGroupDetails(request, companyid, group, null, endDate, 0, true, jArr);
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getBalanceSheet : "+ex.getMessage(), ex);
        }
        return total;
    }

    public ModelAndView exportLedger(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            jobj = getLedger(request);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView exportTrialBalance(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            jobj = getTrialBalance(request);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView getProValuation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String enddate = request.getParameter("enddate");
            Date endDate = null;
            if (enddate != null) {
                endDate = authHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            }
            jobj = getInventoryOpeningBalance(request, companyid, endDate);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getProValuation(HttpServletRequest request, Date stDate, Date endDate) throws ServiceException {
        JSONObject jobj=new JSONObject();
        try {
            JSONArray DataJArr =getProValuationArray(request, stDate, endDate);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            JSONArray jArr1 = new JSONArray();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                int st = Integer.parseInt(start);
                int ed = Math.min(DataJArr.length(), st + Integer.parseInt(limit));
                for (int i = st; i < ed; i++) {
                    jArr1.put(DataJArr.getJSONObject(i));
                }
            }
            else{
                jArr1=DataJArr;
            }
            jobj.put("data", jArr1);
            jobj.put("count", DataJArr.length());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    public JSONArray getProValuationArray(HttpServletRequest request, Date stDate, Date endDate) throws ServiceException {
        double valuation = 0;
       JSONArray jArr=new JSONArray();
        try {
            Calendar startcal= Calendar.getInstance();
            Calendar endcal= Calendar.getInstance();
            if (stDate != null) {
                startcal.setTime(stDate);
            }
            if (endDate != null) {
                endcal.setTime(endDate);
            }
            boolean isprovalReport=false;
            if(request.getParameter("isprovalreport")!=null)
            {
                isprovalReport= Boolean.parseBoolean(request.getParameter("isprovalreport"));
            }
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("stDate", stDate);
            requestParam.put("endDate", endDate);
            requestParam.put("companyid", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject rtObject = accProductObj.getProValuation(requestParam);
            List list = rtObject.getEntityList();
            Iterator itr = list.iterator();
            while(itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Product product = (Product) row[0];
                JSONObject obj = new JSONObject();
                obj.put("productid", product.getID());
                obj.put("productname", product.getName());
                obj.put("productdesc", product.getDescription());
                obj.put("productType", product.getProducttype().getName());
                obj.put("productTypeID", product.getProducttype().getID());
                double avgcost =0;// Double.parseDouble(row[1]==null?"0":row[1].toString());
                HashMap<String, Object> requestParam1 = new HashMap();
                requestParam1.put("productid", product.getID());
                requestParam1.put("stDate", stDate);
                requestParam1.put("endDate", endDate);
                if(!product.getProducttype().getID().equalsIgnoreCase(Producttype.ASSEMBLY)){
                    double totalProPurchase = 0;
                    double totalQuantityIn = 0;
                    
                    List ll = getClosingStockVal(product.getID());
                    
//                    List ll=getTotalPurchaseCost(product.getID());
                    totalProPurchase=(Double)ll.get(0);
                    totalQuantityIn=(Double)ll.get(1);
                    if(totalQuantityIn!=0.0)
                    {
                        avgcost =  (totalProPurchase/totalQuantityIn);
                    }
                }else{
                    KwlReturnObject avgcostLi = accProductObj.getAvgcostAssemblyProduct(product.getID(), startcal.getTime(), endcal.getTime());
                    avgcost = (Double) avgcostLi.getEntityList().get(0);
                }
                double purchasecost = Double.parseDouble(row[2]==null?"0":row[2].toString());
                double onhand = Double.parseDouble(row[3]==null?"0":row[3].toString());
                valuation = avgcost*onhand;
                if(isprovalReport&&valuation==0){
                    continue;
                }
                
                double lifo = 0,fifo = 0;
//                if(!(product.getProducttype().getID().equals(Producttype.ASSEMBLY))) {
                	if(onhand > 0){
                		lifo = getFIFO(product.getID(),endDate,onhand,true);
                	    fifo = getFIFO(product.getID(),endDate,onhand,false);
                	}
//                }
                
                obj.put("fifo", fifo);
                obj.put("lifo", lifo);
                
                obj.put("avgcost", (avgcost!=0 && onhand!=0)?avgcost:"N.A");
                obj.put("purchasecost", purchasecost);
                obj.put("quantity", onhand);
                obj.put("valuation", valuation);
                jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jArr;
    }

    public double getFIFO(String productid, Date endDate, double onhand, boolean isLifo) throws ServiceException {
    	try{
    		double lifo = 0,GRrate = 0;
    		int rateCount = 0,soldQty = 0, purchaseQty = 0, totalPurchaseQty = 0;
			List<Integer> qty = new ArrayList();
			List<Double> rate = new ArrayList();
			KwlReturnObject initialQty, initialPurchasePrice, qtyfrominv,qtyRatefromInv;
			initialQty = accProductObj.getInitialQuantity(productid);
			if(initialQty.getEntityList().get(0) != null){
				initialPurchasePrice = accProductObj.getInitialCost(productid);
				if(initialPurchasePrice.getEntityList().get(0) != null){
					qty.add(Integer.parseInt(initialQty.getEntityList().get(0).toString()));
					rate.add(Double.parseDouble(initialPurchasePrice.getEntityList().get(0).toString()));
				}
			}
    		
			qtyRatefromInv = accProductObj.getRateandQtyfromInvoice(productid);
			Iterator<List> iteratorInv = qtyRatefromInv.getEntityList().iterator();
			while(iteratorInv.hasNext()){
				GoodsReceiptDetail goodsReceiptDetail = (GoodsReceiptDetail)iteratorInv.next();
				purchaseQty = goodsReceiptDetail.getInventory().getQuantity();
				GRrate = goodsReceiptDetail.getRate();
				qty.add(purchaseQty);
				rate.add(GRrate);
			}
			
			double totalQty = onhand;
			if(onhand > 0 && isLifo){
				for(int i = 0; i < qty.size(); i++){
					if(totalQty >= qty.get(i)  && totalQty != 0){
						lifo = lifo + (qty.get(i) * rate.get(i));
						totalQty = totalQty - qty.get(i);
					}else if(totalQty < qty.get(i) && totalQty != 0){
						lifo = lifo + totalQty * rate.get(i);
						totalQty = 0;
					}
				}
			}
			
			if(onhand > 0 && !isLifo){
				for(int i = qty.size(); i > 0; i--)
					if(totalQty >= qty.get(i - 1)   && totalQty != 0){
						lifo = lifo + (qty.get(i - 1) * rate.get(i - 1));
						totalQty = totalQty - qty.get(i - 1);
					}else if(totalQty < qty.get(i - 1)   && totalQty != 0){
						lifo = lifo + totalQty * rate.get(i - 1);
						totalQty = 0;
					}
			}
			
			
    		return lifo;
    	}catch(Exception ex){
    		ex.printStackTrace();
    		throw ServiceException.FAILURE(ex.getMessage(), ex);
		}
    }
    
    public double getLIFO(String productid, Date endDate, double onhand, boolean isLifo) throws ServiceException {
		try{
			double lifo = 0;
			int rateCount = 0,soldQty = 0;
			List<Integer> qty = new ArrayList();
			List<Double> rate = new ArrayList();
			KwlReturnObject initialQty, initialPurchasePrice, qtyfrominv,qtyPrice;
			initialQty = accProductObj.getInitialQuantity(productid);
			if(initialQty.getEntityList().get(0) != null){
				initialPurchasePrice = accProductObj.getInitialCost(productid);
				if(initialPurchasePrice.getEntityList().get(0) != null){
					qty.add(Integer.parseInt(initialQty.getEntityList().get(0).toString()));
					rate.add(Double.parseDouble(initialPurchasePrice.getEntityList().get(0).toString()));
				}
			}
			qtyfrominv = accProductObj.getQtyandUnitCost(productid, endDate);
			Iterator qtyRateIterator = qtyfrominv.getEntityList().iterator();
			while (qtyRateIterator.hasNext()){
				Object[] row = (Object[]) qtyRateIterator.next();
				qty.add(Integer.parseInt(row[0]==null?"0":row[0].toString()));
				qtyPrice = accProductObj.getProductPrice(productid, true, (Date) row[1]);
				if(qtyPrice.getEntityList().get(0) == null){
					qtyPrice = accProductObj.getInitialPrice(productid, true);
				}
				rate.add(Double.parseDouble(qtyPrice.getEntityList().get(0).toString()));
			}
		
			for(int i = 0; i < qty.size(); i++)
				soldQty = soldQty + qty.get(i);
			soldQty = ((int) (soldQty - onhand));
			if(isLifo){
				while(soldQty > 0){
					soldQty = soldQty - qty.get(rateCount++);
					if(soldQty <= 0){
						qty.set(rateCount - 1, (int) (-soldQty));
					}else{
						qty.set(rateCount - 1, 0);
					}
				}	
				}else{
					rateCount = qty.size() - 1;
					while(soldQty > 0){
						soldQty = soldQty - qty.get(rateCount--);
						if(soldQty <= 0){
							qty.set(rateCount + 1, (int) (-soldQty));
						}else{
							qty.set(rateCount + 1, 0);
						}
					}
				}
			for(int i = 0; i < qty.size(); i++)
				lifo = lifo + (qty.get(i) * rate.get(i));
			return lifo;
		}catch(Exception ex){
    		System.out.print(ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
		}
    }
    
    
    public JSONObject getInventoryOpeningBalance(HttpServletRequest request, String companyid, Date stDate){
        JSONObject jobj=new JSONObject();
        try{
//            String companyid = AuthHandler.getCompanyid(request);
            
            Date date = null;
            KwlReturnObject rtObj = accProductObj.getInventoryOpeningBalanceDate(companyid);
//            String query = "select min(updateDate) from Inventory where product.company.companyID = ?";
//            List lst = HibernateUtil.executeQuery(session,query,companyid);
            List lst = rtObj.getEntityList();
            Iterator ite = lst.iterator();
            while(ite.hasNext()){
                date =  (Date)ite.next();
            }
            jobj = getProValuation(request,date,stDate);
        }catch(Exception ex){
        }
        return jobj;
    }

    public ModelAndView getRatioAnalysis(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getRatioAnalysis(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getRatioAnalysisJSON(JSONObject jobj,String currencyid){
        JSONObject retObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try{
            jobj = jobj.getJSONObject("data");
            JSONArray rightObjArr = jobj.getJSONArray("right");
            JSONArray leftObjArr = jobj.getJSONArray("left");
            int length = leftObjArr.length()>rightObjArr.length()?leftObjArr.length():rightObjArr.length();
            for(int i=0;i<length;i++){
                JSONObject tempObj = new JSONObject();
                if(i<leftObjArr.length() && !leftObjArr.getJSONObject(i).toString().equalsIgnoreCase("{}")){
                    JSONObject leftObj = leftObjArr.getJSONObject(i);
                    String name = "";
                    String format = leftObj.has("fmt")?leftObj.get("fmt").toString():"";
                    String value = formatValue(leftObj.get("value").toString(),format,currencyid);
                    if(leftObj.has("desc")){
                        name = leftObj.get("name").toString()+" "+leftObj.get("desc").toString();
                    }else{
                        name = leftObj.get("name").toString();
                    }
                    tempObj.put("lname",name);
                    tempObj.put("lvalue",value);
                    tempObj.put("lfmt",format);
                }else{
                    tempObj.put("lname","");
                    tempObj.put("lvalue","");
                    tempObj.put("lfmt","");
                }
                if(i<rightObjArr.length() && !rightObjArr.getJSONObject(i).toString().equalsIgnoreCase("{}")){
                    JSONObject rightObj = rightObjArr.getJSONObject(i);
                    String name = "";
                    String format = rightObj.has("fmt")?rightObj.get("fmt").toString():"";
                    String value = formatValue(rightObj.get("value").toString(),format,currencyid);

                    if(rightObj.has("desc")){
                        name = rightObj.get("name").toString()+rightObj.get("desc").toString();
                    }else{
                        name = rightObj.get("name").toString();
                    }
                    tempObj.put("rname",name);
                    tempObj.put("rvalue",value);
                    tempObj.put("rfmt",format);
                }else{
                    tempObj.put("rname","");
                    tempObj.put("rvalue","");
                    tempObj.put("rfmt","");
                }
                jArr.put(tempObj);
            }
            retObj.put("data",jArr);
        }catch(Exception ex){
             Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retObj;
    }

    public ModelAndView exportRatioAnalysis(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            jobj = getRatioAnalysisJSON(getRatioAnalysis(request), currencyid);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
//                jobj = getExportBalanceSheetJSON(getTradingAndProfitLoss(request), -1, 0);
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
                exportDaoObj.processRequest(request, response, jobj);
            } else {
                if (StringUtil.equal(fileType, "csv")) {
                    exportDaoObj.processRequest(request, response, jobj);
                }else{
                    String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                    java.io.ByteArrayOutputStream baos = null;
                    String filename = request.getParameter("filename");
                    String comName = sessionHandlerImpl.getCompanyName(request);
                    baos = ExportrecordObj.exportRatioAnalysis(request,jobj,logoPath,comName);
                    if (baos != null) {
                        ExportrecordObj.writeDataToFile(filename+"."+fileType, baos, response);
                    }

                }
            }
        } catch(Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

//    public ModelAndView exportCashFlowStatement(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        String view = "jsonView_ex";
//        try{
//            String currencyid = sessionHandlerImpl.getCurrencyID(request);
//            jobj = (getCashFlow(request));
//            String fileType = request.getParameter("filetype");
//            if (StringUtil.equal(fileType, "print")) {
////                jobj = getExportBalanceSheetJSON(getTradingAndProfitLoss(request), -1, 0);
//                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
//                jobj.put("GenerateDate", GenerateDate);
//                view = "jsonView-empty";
//                exportDaoObj.processRequest(request, response, jobj);
//            } else {
//                if (StringUtil.equal(fileType, "csv")) {
//                    exportDaoObj.processRequest(request, response, jobj);
//                }else{
//                    String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
//                    java.io.ByteArrayOutputStream baos = null;
//                    String filename = request.getParameter("filename");
//                    String comName = sessionHandlerImpl.getCompanyName(request);
//                    baos = ExportrecordObj.exportCashFlowStatement(jobj,logoPath,comName);
//                    if (baos != null) {
//                        ExportrecordObj.writeDataToFile(filename+"."+fileType, baos, response);
//                    }
//
//                }
//            }
//        } catch(Exception ex) {
//            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return new ModelAndView(view, "model", jobj.toString());
//    }
    
    public String formatValue(String value,String format,String currencyid){
        String result = "";
        try{
            
            double no = Double.parseDouble(value);
            String val = "";
            if(format.equals("CD") || format.equals("total") || format.equals("cash") || format.equals("export")){                
                if(no>0){
                    val = authHandlerDAO.getFormattedCurrency(no,currencyid);
                    val = val + " Dr";
                }else if(no<0){
                    val = authHandlerDAO.getFormattedCurrency((-no),currencyid);
                    val = val + " Cr";
                }else{
                   val = "0";
                }
            }else if(format.equals("RAT")){
                java.text.DecimalFormat obj = new java.text.DecimalFormat("#,##0.00");
                val = obj.format(no) + " : 1";
            }else if(format.equals("PER")){
                java.text.DecimalFormat obj = new java.text.DecimalFormat("#,##0.00");
                val = obj.format(no) + " %";
            }else if(format.equals("DAY")){
                java.text.DecimalFormat obj = new java.text.DecimalFormat("#,##0.00");
                val = obj.format(no) + " days";
            }else{
                java.text.DecimalFormat obj = new java.text.DecimalFormat("#,##0.00");
                val = obj.format(no) + "";
            }
            result = val;
        }catch(Exception ex){
        }
        return result;
    }

    public JSONObject getRatioAnalysis(HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArrL = new JSONArray();
            JSONArray jArrR = new JSONArray();
            JSONObject obj;
//            Date startDate=AuthHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate = authHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) curresult.getEntityList().get(0);
            JSONObject bobj = getBalanceSheet(request);
            double curAsset = getGroupsAmount(bobj, new String[]{Group.CURRENT_ASSETS, Group.BANK_ACCOUNT, Group.ACCOUNTS_RECEIVABLE, Group.OTHER_ASSETS, Group.OTHER_CURRENT_ASSETS}, "right");
            double curLiability = getGroupsAmount(bobj, new String[]{Group.ACCOUNTS_PAYABLE, Group.CREDIT_CARD, Group.OTHER_CURRENT_LIABILITIES}, "left");
            double cash = getAccountBalance(request, preferences.getCashAccount().getID(), null, endDate);
            double bank = getGroupsAmount(bobj, new String[]{Group.BANK_ACCOUNT}, "right");
            double sc = getGroupsAmount(bobj, new String[]{Group.ACCOUNTS_PAYABLE}, "left");
            double sd = getGroupsAmount(bobj, new String[]{Group.ACCOUNTS_RECEIVABLE}, "right");
            double capital = getGroupsAmount(bobj, new String[]{Group.EQUITY}, "left");
            double clStock = 0;
            JSONArray jarr = getInventoryOpeningBalance(request, companyid, endDate).getJSONArray("data");//getProValuation(session, request,startDate,endDate);
            for (int i = 0; i < jarr.length(); i++) {
                clStock += jarr.getJSONObject(i).getDouble("valuation");
            }
            curAsset += clStock;
            double netprofit = getProfit(bobj, "Net");
            bobj = getTradingAndProfitLoss(request);
            double sale = getGroupsAmount(bobj, new String[]{Group.INCOME}, "right");
            double purchase = getGroupsAmount(bobj, new String[]{Group.COST_OF_GOODS_SOLD}, "left");
            double grossprofit = getProfit(bobj, "Gross");
            double wrkCap = curAsset - curLiability;
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.22", null, RequestContextUtils.getLocale(request)));  //"Working Capital");
            obj.put("desc", messageSource.getMessage("acc.report.23", null, RequestContextUtils.getLocale(request)));  //"(Current Assets - Current Liabilities)");
            obj.put("value", wrkCap);
            obj.put("fmt", "CD");
            jArrL.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.24", null, RequestContextUtils.getLocale(request)));  //"Cash-in-hand");
            obj.put("value", cash);
            obj.put("fmt", "CD");
            jArrL.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.25", null, RequestContextUtils.getLocale(request)));  //"Bank Accounts");
            obj.put("value", bank);
            obj.put("fmt", "CD");
            jArrL.put(obj);
//            obj = new JSONObject();
//            obj.put("name", "Bank OD A/c");
//            obj.put("value", 0);
//            obj.put("fmt", "CD");
//            obj.put("pending", true);
//            jArrL.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.26", null, RequestContextUtils.getLocale(request)));  //"Sundry Debtors");
            obj.put("desc", messageSource.getMessage("acc.report.27", null, RequestContextUtils.getLocale(request)));  //"(due till today)");
            obj.put("value", sd);
            obj.put("fmt", "CD");
            jArrL.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.28", null, RequestContextUtils.getLocale(request)));  //"Sundry Creditors");
            obj.put("desc", messageSource.getMessage("acc.report.27", null, RequestContextUtils.getLocale(request)));  //"(due till today)");
            obj.put("value", -sc);
            obj.put("fmt", "CD");
            jArrL.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.29", null, RequestContextUtils.getLocale(request)));  //"Sales Accounts");
            obj.put("value", -sale);
            obj.put("fmt", "CD");
            jArrL.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.30", null, RequestContextUtils.getLocale(request)));  //"Purchase Accounts");
            obj.put("value", purchase);
            obj.put("fmt", "CD");
            jArrL.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.31", null, RequestContextUtils.getLocale(request)));  //"Stock-in-hand");
            obj.put("value", clStock);
            obj.put("fmt", "CD");
            jArrL.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.32", null, RequestContextUtils.getLocale(request)));  //"Net Profit/Loss");
            obj.put("value", netprofit);
            obj.put("fmt", "CD");
            jArrL.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.33", null, RequestContextUtils.getLocale(request)));  //"Wkg. Capital Turnover");
            obj.put("desc", messageSource.getMessage("acc.report.34", null, RequestContextUtils.getLocale(request)));  //"(Sales Accounts / Working Capital)");
            obj.put("value", (Math.abs(wrkCap) > 0.001 ? sale / wrkCap : "N/A"));
            jArrL.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.35", null, RequestContextUtils.getLocale(request)));  //"Inventory Turnover");
            obj.put("desc", messageSource.getMessage("acc.report.36", null, RequestContextUtils.getLocale(request)));  //"(Sales Accounts / Closing Stock)");
            obj.put("value", (Math.abs(clStock) > 0.001 ? sale / clStock : "N/A"));
            jArrL.put(obj);

            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.37", null, RequestContextUtils.getLocale(request)));  //"Current Ratio");
            obj.put("desc", messageSource.getMessage("acc.report.38", null, RequestContextUtils.getLocale(request)));  //"(Current Assets : Current Liabilities)");
            obj.put("value", (Math.abs(curLiability) > 0.001 ? curAsset / curLiability : "N/A"));
            obj.put("fmt", "RAT");
            jArrR.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.39", null, RequestContextUtils.getLocale(request)));  //"Quick Ratio");
            obj.put("desc", messageSource.getMessage("acc.report.40", null, RequestContextUtils.getLocale(request)));  //"(Current Assets - Stock-in-hand : Current Liabilities)");
            obj.put("value", (Math.abs(curLiability) > 0.001 ? (curAsset - clStock) / curLiability : "N/A"));
            obj.put("fmt", "RAT");
            jArrR.put(obj);
//            obj = new JSONObject();
//            obj.put("name", "Debt/Equity Ratio");
//            obj.put("desc", "(Loans (Liability) : Capital Account + Net Profit)");
//            obj.put("value", 0.453);
//            obj.put("pending", true);
//            obj.put("fmt", "RAT");
//            jArrR.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.41", null, RequestContextUtils.getLocale(request)));  //"Gross Profit %");
            obj.put("value", (Math.abs(sale) > 0.001 ? grossprofit * 100 / sale : "N/A"));
            obj.put("fmt", "PER");
            jArrR.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.42", null, RequestContextUtils.getLocale(request)));  //"Net Profit %");
            obj.put("value", (Math.abs(sale) > 0.001 ? netprofit * 100 / sale : "N/A"));
            obj.put("fmt", "PER");
            jArrR.put(obj);
//            obj = new JSONObject();
//            obj.put("name", "Operating cost %");
//            obj.put("desc", "(as percentage of Sales Accounts)");
//            obj.put("value", 0.453);
//            obj.put("fmt", "PER");
//            obj.put("pending", true);
//            jArrR.put(obj);
//            obj = new JSONObject();
//            obj.put("name", "Recv. turnover in days");
//            obj.put("desc", "(payment performance of Debtors)");
//            obj.put("value", 0.453);
//            obj.put("fmt", "DAY");
//            obj.put("pending", true);
//            jArrR.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.43", null, RequestContextUtils.getLocale(request)));  //"Return on Investment %");
            obj.put("desc", messageSource.getMessage("acc.report.44", null, RequestContextUtils.getLocale(request)));  //"(Net Profit / (Capital Account + Net Profit) ) %");
            obj.put("value", (Math.abs(capital + netprofit) > 0.001 ? netprofit * 100 / (capital + netprofit) : "N/A"));
            obj.put("fmt", "PER");
            jArrR.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.67", null, RequestContextUtils.getLocale(request)));
            obj.put("desc", messageSource.getMessage("acc.report.68", null, RequestContextUtils.getLocale(request)));
            obj.put("value", (Math.abs(wrkCap) > 0.001 ? netprofit * 100 / wrkCap : "N/A"));
            obj.put("fmt", "PER");
            jArrR.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.69", null, RequestContextUtils.getLocale(request)));
            obj.put("desc", messageSource.getMessage("acc.report.70", null, RequestContextUtils.getLocale(request)));
            obj.put("value", (Math.abs(sale) > 0.001 ? wrkCap / sale : "N/A"));
            obj.put("fmt", "RAT");
            jArrR.put(obj);
//            obj = new JSONObject();
//            obj.put("name", "Operating Expenses to Sales Ratio %");
//            obj.put("value", (Math.abs(sale) > 0.001 ? (purchase ) * 100 / sale : "N/A"));
//            obj.put("fmt", "PER");
//            jArrR.put(obj);
            JSONObject fobj = new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            jobj.put("data", fobj);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getRatioAnalysis : " + ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("getRatioAnalysis : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getRatioAnalysis : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    private double getGroupsAmount(JSONObject jobj, String[] groupids, String side) {
        double val = 0;
        try {
            JSONArray jArr = jobj.getJSONObject("data").getJSONArray(side);
            JSONObject obj;
            String gids = ":" + StringUtil.join(":", groupids) + ":";
            for (int i = 0; i < jArr.length(); i++) {
                obj = jArr.getJSONObject(i);
                if (obj.getInt("level") == 0 && gids.indexOf(":" + obj.getString("accountid") + ":") >= 0 && obj.getString("accountname").startsWith("Total ")) {
                    val += obj.getDouble("amount");
//                    System.out.println(obj);				// For testing Purpose
                }
            }
        } catch (JSONException ex) {
        }
        return val;
    }

    private double getProfit(JSONObject jobj, String type) {
        double val = 0;
        try {
            JSONArray jArr = jobj.getJSONObject("data").getJSONArray("left");
            JSONObject obj;
            for (int i = 0; i < jArr.length(); i++) {
                obj = jArr.getJSONObject(i);
                if(obj.has("level")){
                	if (obj.getInt("level") == 0 && StringUtil.isNullOrEmpty(obj.getString("accountid")) && obj.getString("accountname").startsWith(type + " Profit")) {
                		val += obj.getDouble("amount");
                		break;
                	}
                }
            }
            if (val == 0.0) {
                jArr = jobj.getJSONObject("data").getJSONArray("right");
                for (int i = 0; i < jArr.length(); i++) {
                    obj = jArr.getJSONObject(i);
                    if(obj.has("level")){
                    	if (obj.getInt("level") == 0 && StringUtil.isNullOrEmpty(obj.getString("accountid")) && obj.getString("accountname").startsWith(type + " Loss")) {
                    		val -= obj.getDouble("amount");
                    		break;
                    	}
                    }
                }
            }
        } catch (JSONException ex) {
        	Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return val;
    }
    
    public ModelAndView exportSalesByItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getSalesByItem(request);
            issuccess = true;
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
              String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
              jobj.put("GenerateDate", GenerateDate);
              exportDaoObj.processRequest(request, response, jobj);
            } else {
              if (StringUtil.equal(fileType, "csv")) {
                  exportDaoObj.processRequest(request, response, jobj);
              }else{
                  exportDaoObj.processRequest(request, response, jobj);
              }
            }
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView-empty", "model", jobj.toString());
    }

    public ModelAndView getSalesByItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getSalesByItem(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public List getTotalSalesCost(HashMap<String, Object> requestParams,String productid) throws ServiceException, ParseException {
      List ll=new ArrayList() ;
        try {
            DateFormat df1 = (DateFormat) requestParams.get("df1");
            Date fromDate = df1.parse((String)requestParams.get("fromDate"));
            Date toDate= df1.parse((String)requestParams.get("toDate"));
            KwlReturnObject invoiceDetailResult = accInvoiceDAOobj.getInvoiceProductDetails(productid, fromDate, toDate);
            List<InvoiceDetail> invDetailList=  invoiceDetailResult.getEntityList();
            double totalQuantityOut = 0;
            double totalProSales = 0;
            if (invDetailList != null ){
                for (InvoiceDetail invDetail : invDetailList){
                    double temqua=0;
                    double invProSales=0;
                     HashMap hm = accInvoiceCommon.applyCreditNotes(requestParams, invDetail.getInvoice());
                     if (hm.containsKey(invDetail)) {
                        Object[] val = (Object[]) hm.get(invDetail);
                        invProSales = (Double) val[3];//formula:(rate*quantity)-rowdiscount-invdiscount-cnamount[PS]
                        temqua= (Integer) val[1];
                        KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,invProSales,invDetail.getInvoice().getCurrency().getCurrencyID(),invDetail.getInvoice().getJournalEntry().getEntryDate(),invDetail.getInvoice().getExternalCurrencyRate());
                        invProSales = (Double) crresult.getEntityList().get(0);
                        totalQuantityOut += temqua;
                        totalProSales += invProSales;
                     }
                }
                ll.add(0,totalProSales);
                ll.add(1,totalQuantityOut);
            }
         } catch (Exception ex) {
            throw ServiceException.FAILURE("getSalesByItem : "+ex.getMessage(), ex);
         }
        return ll;
    }
    
    public List getClosingStockVal(String productid) throws ServiceException, ParseException {
        List ll=new ArrayList() ;
          try {
          double totalProPurchase=0;
          double totalQuantityIn = 0;
          
          KwlReturnObject quantityResult, priceResult, amtResult, tax;
          
          quantityResult = accProductObj.getQuantityPurchaseOrSalesDetails(productid, true);
               List<Inventory> quantityList=  quantityResult.getEntityList();
              if (quantityList != null ){
                  for (Inventory inv : quantityList){
                	 if(!inv.isDefective()){ 
	                     double quantityIn = 0;
	                     double proPrice=0, ProPurchase=0, taxPercent=0;
	                      quantityIn = inv.getQuantity();
	                      Date invDate = inv.getUpdateDate();
	                      priceResult = accProductObj.getProductPrice(productid, true, invDate);
	                      List<Object> priceList= priceResult.getEntityList();
	                      if (priceList != null){
	                          for (Object cogsval : priceList){
	                              proPrice = (cogsval==null?0.0:(Double)cogsval);
	                          }
	                          // new logic for fetching product price
	                          priceResult = accGoodsReceiptDAOObj.getGoodsReceipt_Rate(inv.getID());
	                          priceList= priceResult.getEntityList();
	                          if (priceList != null){
	                              for (Object cogsval : priceList){
	                                  proPrice = (cogsval==null?0.0:(Double)cogsval);
	                              }
	                          }
	                          // new logic for fetching product price
	                          
	                          ProPurchase += (quantityIn*proPrice);

	 	                     double debitQty = 0, debitAmt =0;
	      // Debit Note Qty                        
	                          quantityResult = accDebitNoteobj.getTotalQty(inv.getID());
	                          if(quantityResult.getEntityList() != null && quantityResult.getEntityList().size()>0 && quantityResult.getEntityList().get(0)!=null)
	                        	  debitQty = Double.parseDouble(quantityResult.getEntityList().get(0).toString());
	                          quantityIn = quantityIn - debitQty;
	                          
	                          if(quantityIn != 0){
	                        	  amtResult = accDebitNoteobj.getTotalDiscount(inv.getID());
	                        	  if(amtResult.getEntityList() != null && amtResult.getEntityList().size()>0 && amtResult.getEntityList().get(0)!=null){
	                        		  debitAmt = Double.parseDouble(amtResult.getEntityList().get(0).toString());
	                        		  tax = accGoodsReceiptDAOObj.getGR_ProductTaxPercent(inv.getID());
	                        		  if(tax.getEntityList() != null && tax.getEntityList().size()>0 && tax.getEntityList().get(0)!=null)
	                        			  taxPercent = Double.parseDouble(tax.getEntityList().get(0).toString());
	                        		  debitAmt = debitAmt - (taxPercent/(taxPercent+100) * debitAmt);
	                        		  ProPurchase = ProPurchase - debitAmt;
	                        	  }
	                          }else
	                        	  ProPurchase = 0; 
	                          
		                      totalQuantityIn+=quantityIn;
	                          totalProPurchase += ProPurchase;
	                      }
                	 }
                  }
                  ll.add(0,totalProPurchase);
                  ll.add(1,totalQuantityIn);
              }
           } catch (Exception ex) {
          	 ex.printStackTrace();
              throw ServiceException.FAILURE("getClosingStockVal : "+ex.getMessage(), ex);
           }
          return ll;
      }
    
    public double getNoteAmount(String invId) throws ServiceException {
    	double amount = 0;
    	try{
    	}catch(Exception ex){
    		 ex.printStackTrace();
             throw ServiceException.FAILURE("getNoteAmount : "+ex.getMessage(), ex);
    	}
    	return amount;
    }

    public List getTotalPurchaseCost(String productid) throws ServiceException, ParseException {
      List ll=new ArrayList() ;
        try {
        double totalProPurchase=0;
        double totalQuantityIn = 0;
        KwlReturnObject quantityResult = accProductObj.getQuantityPurchaseOrSalesDetails(productid, true);
             List<Inventory> quantityList=  quantityResult.getEntityList();
            if (quantityList != null ){
                for (Inventory inv : quantityList){
                   double quantityIn = 0;
                   double proPrice=0;
                    quantityIn = inv.getQuantity();
                    totalQuantityIn+=quantityIn;
                    Date invDate = inv.getUpdateDate();
                    KwlReturnObject priceResult = accProductObj.getProductPrice(productid, true, invDate);
                     List<Object> priceList= priceResult.getEntityList();
                    if (priceList != null){
                        for (Object cogsval : priceList){
                            proPrice = (cogsval==null?0.0:(Double)cogsval);
                        }
                        // new logic for fetching product price
                        priceResult = accGoodsReceiptDAOObj.getGoodsReceipt_Rate(inv.getID());
                        priceList= priceResult.getEntityList();
                        if (priceList != null){
                            for (Object cogsval : priceList){
                                proPrice = (cogsval==null?0.0:(Double)cogsval);
                            }
                        }
                        // new logic for fetching product price
                        totalProPurchase += (quantityIn*proPrice);
                    }
                }
                ll.add(0,totalProPurchase);
                ll.add(1,totalQuantityIn);
            }
         } catch (Exception ex) {
        	 ex.printStackTrace();
            throw ServiceException.FAILURE("getSalesByItem : "+ex.getMessage(), ex);
         }
        return ll;
    }
    public JSONObject getSalesByItem(HttpServletRequest request) throws ServiceException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("fromDate", request.getParameter("fromDate")); //not using right now[PS]
            requestParams.put("toDate", request.getParameter("toDate"));//not using right now[PS]
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("df1", authHandler.getDateOnlyFormatter(request));

            KwlReturnObject result = accProductObj.getProductList((Map)requestParams);
            int count = result.getRecordTotalCount();
            List<Product> list= result.getEntityList();
            JSONArray jArr=new JSONArray();
            
            if (list != null ){
                 for (Product proObj : list){
                    double totalProPurchase=0;
                    double totalQuantityIn = 0;
                    double totalQuantityOut = 0;
                    double margin = 0;
                    double totalProSales = 0;

                    List ll=getTotalSalesCost(requestParams,proObj.getID());
                    totalProSales=(Double)ll.get(0);
                    totalQuantityOut=(Double)ll.get(1);

                    ll=getTotalPurchaseCost(proObj.getID());
                    totalProPurchase=(Double)ll.get(0);
                    totalQuantityIn=(Double)ll.get(1);

                    margin = (totalQuantityOut==0||totalQuantityIn==0?0:(totalProSales/totalQuantityOut)-(totalProPurchase/totalQuantityIn));
                    if(totalProSales==0)continue;
                    JSONObject obj = new JSONObject();
                    obj.put("productname",proObj.getName());
                    obj.put("totalQuantityIn",totalQuantityIn);//cal. through inventory
                    obj.put("quantity",totalQuantityOut);//cal. through invoice
                    obj.put("amount",totalProSales);
                    obj.put("cogs",totalProPurchase);
                    obj.put("avgsale",(totalQuantityOut==0||totalProSales==0?0:(totalProSales/totalQuantityOut)));//
                    obj.put("avgcogs",totalProPurchase==0||totalQuantityIn==0?0:(totalProPurchase/totalQuantityIn));
                    obj.put("margin",margin);
                    obj.put("permargin", (totalProPurchase==0||margin==0?0:((margin*100*totalQuantityIn)/totalProPurchase)));//margin*/100/avg purchase cost


                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
            jobj.put("count",count);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("getSalesByItem : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getSalesByItem : "+ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView exportDetailedSalesByItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getDetailedSalesByItem(request);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
              String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
              jobj.put("GenerateDate", GenerateDate);
              exportDaoObj.processRequest(request, response, jobj);
            } else {
              if (StringUtil.equal(fileType, "csv")) {
                  exportDaoObj.processRequest(request, response, jobj);
              }else{
                  exportDaoObj.processRequest(request, response, jobj);
              }
            }
        } catch (Exception ex) {
        	msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView-empty", "model", jobj.toString());
   }
    
   public ModelAndView getDetailedSalesByItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getDetailedSalesByItem(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getDetailedSalesByItem(HttpServletRequest request) throws ServiceException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("fromDate", request.getParameter("fromDate"));
            requestParams.put("toDate", request.getParameter("toDate"));
            requestParams.put("ss", request.getParameter("ss"));
            Date fromDate = authHandler.getDateOnlyFormatter(request).parse(request.getParameter("fromDate"));
            Date toDate=authHandler.getDateOnlyFormatter(request).parse(request.getParameter("toDate"));
            KwlReturnObject result = accProductObj.getProductList((Map)requestParams);
            Iterator itr = result.getEntityList().iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                double cumquantity = 0;
                double cumsales=0;
                Product proObj = (Product)itr.next();
                KwlReturnObject invoiceDetaailResult = accInvoiceDAOobj.getInvoiceProductDetails(proObj.getID(), fromDate, toDate);
               Iterator itr1 = invoiceDetaailResult.getEntityList().iterator();
                while(itr1.hasNext()) {
                    InvoiceDetail invDetail =  (InvoiceDetail) itr1.next();
                     HashMap hm = accInvoiceCommon.applyCreditNotes(requestParams, invDetail.getInvoice());
                     if (hm.containsKey(invDetail)) {
                        Object[] val = (Object[]) hm.get(invDetail);
                        double invProSales = (Double) val[3];//formula:(rate*quantity)-rowdiscount-invdiscount-cnamount[PS]
                        double temqua= (Integer) val[1];

                        KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,invProSales,invDetail.getInvoice().getCurrency().getCurrencyID(),invDetail.getInvoice().getJournalEntry().getEntryDate(),invDetail.getInvoice().getExternalCurrencyRate());
                        invProSales = (Double) crresult.getEntityList().get(0);
                        cumquantity += temqua;
                        cumsales += invProSales;
                        cumquantity += temqua;

                        JournalEntryDetail d = invDetail.getInvoice().getCustomerEntry();
                        Account account = d.getAccount();
                        JSONObject obj = new JSONObject();
                        obj.put("productname",proObj.getName());
                        obj.put("productid",proObj.getID());
                        obj.put("billno",invDetail.getInvoice().getInvoiceNumber());
                        obj.put("invoiceid",invDetail.getInvoice().getID());
                        obj.put("date", authHandler.getDateFormatter(request).format(invDetail.getInvoice().getJournalEntry().getEntryDate()));
                        obj.put("memo",invDetail.getInvoice().getMemo());
                        obj.put("personname", invDetail.getInvoice().getCustomer()==null?account.getName():invDetail.getInvoice().getCustomer().getName());
                        obj.put("quantity",temqua);
                        obj.put("rateinbase",accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,(invDetail.getRate()),invDetail.getInvoice().getCurrency().getCurrencyID(),invDetail.getInvoice().getJournalEntry().getEntryDate(),invDetail.getInvoice().getExternalCurrencyRate()).getEntityList().get(0));
                        obj.put("amount",invProSales);
                        obj.put("totalsales", cumsales);
                        obj.put("totalquantity",cumquantity);
                        jArr.put(obj);
                    }
                }
                jobj.put("data", jArr);
             }
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");

            JSONArray jArr1 = new JSONArray(), temp = jArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                int st = Integer.parseInt(start);
                int ed = Math.min(temp.length(), st + Integer.parseInt(limit));
                for (int i = st; i < ed; i++) {
                    jArr1.put(temp.getJSONObject(i));
                }
            }

            jobj.put("data", jArr1);
            jobj.put("count", jArr.length());

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("getSalesByItem : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getSalesByItem : "+ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView exportCalculatedTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            JSONArray jArr = getCalculatedTax(request);
            String fileType = request.getParameter("filetype");
            jobj.put("data", jArr);
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
                exportDaoObj.processRequest(request, response, jobj);
            } else {
                if (StringUtil.equal(fileType, "csv")) {
                    exportDaoObj.processRequest(request, response, jobj);
                }else{
                    exportDaoObj.processRequest(request, response, jobj);
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView getCalculatedTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        JSONArray jArr=new JSONArray();
        String msg="";
        boolean issuccess = false;
        try {
            jArr = getCalculatedTax(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            JSONArray jArr1 = new JSONArray();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                int st = Integer.parseInt(start);
                int ed = Math.min(jArr.length(), st + Integer.parseInt(limit));
                for (int i = st; i < ed; i++) {
                    jArr1.put(jArr.getJSONObject(i));
                }
            }

            jobj.put("data", jArr1);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getCalculatedTax(HttpServletRequest request) throws ServiceException, ParseException {
        JSONArray jArr=new JSONArray();
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("ss", request.getParameter("ss"));
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String)requestParams.get("companyid"));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String cashAccount = pref.getCashAccount().getID();
            requestParams.put("cashaccountid", cashAccount);
            KwlReturnObject result = accTaxObj.getCalculatedTax((Map)requestParams);
            List list= result.getEntityList();
            boolean isSalesTax=Boolean.parseBoolean(request.getParameter("issales"));
            if(isSalesTax){
                jArr=getCalculatedSalesTax(requestParams,list);
            }else{
                jArr=getCalculatedPurchaseTax(requestParams,list);}
             } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("getCalculatedTax : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getCalculatedSalesTax(HashMap<String, Object> requestParams,List taxList) throws ServiceException, ParseException {
        JSONArray jArr=new JSONArray();
        try {
            Iterator itr = taxList.iterator();
                while(itr.hasNext()) {
                 Object[] row=(Object[])itr.next();
                 Tax taxObj = (Tax)row[0];
                 TaxList taxListObj = (TaxList)row[1];
//                 String invoiceQuery = "select inv.taxEntry.amount,inv.customerEntry.amount from Invoice inv where inv.tax.ID = ? ";//and inv.journalEntry.entryDate between ? and ? ";
//                 List l1 = HibernateUtil.executeQuery(session,invoiceQuery,new Object[]{taxObj.getID()/*,fromDate,toDate*/});
                 Map<String, Object> filterParams = new HashMap<String, Object>();
                 filterParams.put("taxid", taxObj.getID());
                 KwlReturnObject result = accInvoiceDAOobj.getCalculatedInvTax(filterParams);
                 Iterator ite1 = result.getEntityList().iterator();
                 double saleAmount = 0;
                 double taxableAmount = 0;
                 double taxCollected = 0;
                 while(ite1.hasNext()){
                    double taxPercent = 0;
                    double invAmtDue = 0;
                    Invoice temp= (Invoice)ite1.next();
                    double invSalesAmt=(Double)(temp.getCustomerEntry()==null?0:temp.getCustomerEntry().getAmount()); //Calculating total sales in customer currency     [PS]
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map)requestParams, invSalesAmt, temp.getCurrency().getCurrencyID(), temp.getJournalEntry().getEntryDate(), temp.getExternalCurrencyRate());                        //Converting into base [PS]
                    invSalesAmt= (Double) bAmt.getEntityList().get(0);
                    if (temp.getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), temp.getJournalEntry().getEntryDate(), temp.getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                     saleAmount += invSalesAmt;//505*100=(100+p)x
                     taxableAmount+=(invSalesAmt-(invSalesAmt*100)/(100+taxPercent));
                     if(!temp.getCustomerEntry().getAccount().getID().equals((String) requestParams.get("cashaccountid"))){
                        invAmtDue=accInvoiceCommon.getAmountDue(requestParams,temp);
                    }
                     if(invAmtDue==0){
                         taxCollected+=(invSalesAmt-(invSalesAmt*100)/(100+taxPercent));}
                 }


                 result = accInvoiceDAOobj.getCalculatedInvDtlTax(filterParams);
                 List<InvoiceDetail> list = result.getEntityList();
                if(list!=null){
                    for(InvoiceDetail temp:list){
                        int quantity;
                        double rowTaxPercent = 0;
                        double invAmtDue= 0;
                        double ramount=0;
                        quantity=temp.getInventory().getQuantity();
                        ramount=temp.getRate()*quantity;
                        double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
                        ramount-=rdisc;
    //Converting row amount in base   [PS]
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map)requestParams, ramount, temp.getInvoice().getCurrency().getCurrencyID(), temp.getInvoice().getJournalEntry().getEntryDate(), temp.getInvoice().getExternalCurrencyRate());                        //Converting into base [PS]
                        ramount= (Double) bAmt.getEntityList().get(0);
    //Calculating tax on base row amount [PS]
                        if (temp.getTax() != null) {
                            KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), temp.getInvoice().getJournalEntry().getEntryDate(), temp.getTax().getID());
                            rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        }
                        saleAmount +=ramount;//row wise sales[PS]
                        saleAmount +=ramount*rowTaxPercent/100;
                        taxableAmount+=ramount*rowTaxPercent/100;
                        if(!temp.getInvoice().getCustomerEntry().getAccount().getID().equals((String) requestParams.get("cashaccountid"))){
                            invAmtDue=accInvoiceCommon.getAmountDue(requestParams,temp.getInvoice());
                        }
                        if(invAmtDue==0){
                             taxCollected+=ramount*rowTaxPercent/100;
                        }
                    }
                }
                 JSONObject obj = new JSONObject();
                 obj.put("taxname",taxObj.getName());
                 obj.put("taxcode",taxObj.getTaxCode());
                 obj.put("totalsale",saleAmount);//sales including tax[PS]
     //            obj.put("nontaxablesale",saleAmount-taxableAmount);
   //              obj.put("taxablesale",(saleAmount-taxableAmount));
                 obj.put("taxrate",taxListObj.getPercent());
                 obj.put("taxamount",taxableAmount);
                 obj.put("taxcollected",taxCollected);  //taxamount of all thoose invoices whose amount due is 0[PS]
                 obj.put("taxpayable",taxableAmount-taxCollected);
                 jArr.put(obj);
             }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCalculatedTax : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getCalculatedPurchaseTax(Map<String, Object> requestParams,List taxList) throws ServiceException, ParseException {
        JSONArray jArr=new JSONArray();
        try {
            Iterator itr = taxList.iterator();
             while(itr.hasNext()) {
                 Object[] row=(Object[])itr.next();
                 Tax taxObj = (Tax)row[0];
                 TaxList taxListObj = (TaxList)row[1];
                 Map<String, Object> filterParams = new HashMap<String, Object>();
                 filterParams.put("taxid", taxObj.getID());
                 KwlReturnObject result = accGoodsReceiptDAOObj.getCalculatedGRTax(filterParams);
                 List<GoodsReceipt> list = result.getEntityList();
                 double saleAmount = 0;
                 double taxableAmount = 0;
                 double taxCollected = 0;

//Cal Tax for whole GR[PS]

                 if(list!=null){
                    for(GoodsReceipt temp:list){
                        double taxPercent = 0;
                        double invSalesAmt=(Double)(temp.getVendorEntry()==null?0:temp.getVendorEntry().getAmount()); //Calculating total sales in customer currency     [PS]
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invSalesAmt, temp.getCurrency().getCurrencyID(), temp.getJournalEntry().getEntryDate(), temp.getExternalCurrencyRate());                        //Converting into base [PS]
                        invSalesAmt= (Double) bAmt.getEntityList().get(0);
                        if (temp.getTax() != null) {
                            KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), temp.getJournalEntry().getEntryDate(), temp.getTax().getID());
                            taxPercent = (Double) perresult.getEntityList().get(0);
                        }
                         saleAmount += invSalesAmt;//505*100=(100+p)x
                         taxableAmount+=(invSalesAmt-(invSalesAmt*100)/(100+taxPercent));
                         double invAmtDue=0;
                         if(!temp.getVendorEntry().getAccount().getID().equals((String) requestParams.get("cashaccountid"))){
                            List amtList;
                            if(temp.isIsExpenseType()){
                                 amtList  =accGoodsReceiptCommon.getExpGRAmountDue(requestParams,temp);
                            }else{
                                 amtList =accGoodsReceiptCommon.getGRAmountDue(requestParams,temp);
                            }
                            if(!amtList.isEmpty()&&amtList!=null){
                                invAmtDue=(Double)amtList.get(1);
                            }
                        }
                        if(invAmtDue==0){
                             taxCollected+=(invSalesAmt-(invSalesAmt*100)/(100+taxPercent));
                        }
                    }
                }

//Cal Tax for GR Details[PS]

                result = accGoodsReceiptDAOObj.getCalculatedGRDtlTax(filterParams);
                List<GoodsReceiptDetail> GRList = result.getEntityList();
                if(GRList!=null){
                    for(GoodsReceiptDetail temp:GRList){
                        int quantity;
                        double rowTaxPercent = 0;
                        double ramount=0;
                        quantity=temp.getInventory().getQuantity();
                        ramount=temp.getRate()*quantity;
                        double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
                        ramount-=rdisc;
//Converting row amount in base   [PS]
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map)requestParams, ramount, temp.getGoodsReceipt().getCurrency().getCurrencyID(), temp.getGoodsReceipt().getJournalEntry().getEntryDate(), temp.getGoodsReceipt().getExternalCurrencyRate());                        //Converting into base [PS]
                        ramount= (Double) bAmt.getEntityList().get(0);
//Calculating tax on base row amount [PS]
                        if (temp.getTax() != null) {
                            KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), temp.getGoodsReceipt().getJournalEntry().getEntryDate(), temp.getTax().getID());
                            rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        }
                        saleAmount +=ramount;//row wise sales[PS]
                        saleAmount +=ramount*rowTaxPercent/100;
                        taxableAmount+=ramount*rowTaxPercent/100;
                        double invAmtDue=0;
                        if(!temp.getGoodsReceipt().getVendorEntry().getAccount().getID().equals((String) requestParams.get("cashaccountid"))){
                            List amtList;
                            amtList =accGoodsReceiptCommon.getGRAmountDue(requestParams,temp.getGoodsReceipt());
                            if(!amtList.isEmpty()&&amtList!=null){
                                invAmtDue=(Double)amtList.get(1);
                            }
                        }
                         if(invAmtDue==0){
                             taxCollected+=ramount*rowTaxPercent/100;
                         }
                    }
                }

//Cal Tax for Expense GR Details[PS]
                result = accGoodsReceiptDAOObj.getCalculatedExpenseGRDtlTax(filterParams);
                List<ExpenseGRDetail> expList = result.getEntityList();
                if(expList!=null){
                    for(ExpenseGRDetail temp:expList){
                        double rowTaxPercent = 0;
                        double ramount=0;
                        ramount=temp.getRate();
                        double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
                        ramount-=rdisc;
//Converting row amount in base   [PS]
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map)requestParams, ramount, temp.getGoodsReceipt().getCurrency().getCurrencyID(), temp.getGoodsReceipt().getJournalEntry().getEntryDate(), temp.getGoodsReceipt().getExternalCurrencyRate());                        //Converting into base [PS]
                        ramount= (Double) bAmt.getEntityList().get(0);
//Calculating tax on base row amount [PS]
                        if (temp.getTax() != null) {
                            KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), temp.getGoodsReceipt().getJournalEntry().getEntryDate(), temp.getTax().getID());
                            rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        }
                        saleAmount +=ramount;//row wise sales[PS]
                        saleAmount +=ramount*rowTaxPercent/100;
                        taxableAmount+=ramount*rowTaxPercent/100;
                        double invAmtDue=0;
                        if(!temp.getGoodsReceipt().getVendorEntry().getAccount().getID().equals((String) requestParams.get("cashaccountid"))){
                            List amtList;
                            amtList  =accGoodsReceiptCommon.getExpGRAmountDue(requestParams,temp.getGoodsReceipt());
                            if(!amtList.isEmpty()&&amtList!=null){
                                invAmtDue=(Double)amtList.get(1);
                            }
                        }
                        if(invAmtDue==0){
                            taxCollected+=ramount*rowTaxPercent/100;
                        }
                    }
                }
                 JSONObject obj = new JSONObject();
                 obj.put("taxname",taxObj.getName());
                 obj.put("taxcode",taxObj.getTaxCode());
                 obj.put("totalsale",saleAmount);//sales including tax[PS]
     //            obj.put("nontaxablesale",saleAmount-taxableAmount);
   //              obj.put("taxablesale",(saleAmount-taxableAmount));
                 obj.put("taxrate",taxListObj.getPercent());
                 obj.put("taxamount",taxableAmount);
                 obj.put("taxcollected",taxCollected);  //taxamount of all thoose invoices whose amount due is 0[PS]
                 obj.put("taxpayable",taxableAmount-taxCollected);
                 jArr.put(obj);
             }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCalculatedTax : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getCostCenterSummary(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            JSONArray DataJArr = new JSONArray();
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            Date startDate = authHandler.getDateFormatter(request).parse(request.getParameter(Constants.REQ_startdate));
            Date endDate = authHandler.getDateFormatter(request).parse(request.getParameter(Constants.REQ_enddate));

            String companyid = (String) requestParams.get(Constants.companyKey);
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(companyid);
            requestParams.put(Constants.filterNamesKey, filter_names);
            requestParams.put(Constants.filterParamsKey, filter_params);
            KwlReturnObject result = accCostCenterObj.getCostCenter(requestParams);
            List<CostCenter> costCenters = result.getEntityList();

            KwlReturnObject ret =  accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Group", Group.OTHER_CURRENT_LIABILITIES);
            Group liab_group = null;
            if(!ret.getEntityList().isEmpty()){
                liab_group = (Group) ret.getEntityList().get(0);
            }

            double debitAmount = 0, creditAmount = 0;
            if (costCenters != null && !costCenters.isEmpty()) {
                for (CostCenter costCenter : costCenters) {
                    debitAmount = 0; creditAmount = 0;
                    JSONObject obj = new JSONObject();
                    requestParams.put(CCConstants.REQ_costcenter, costCenter.getID());
                    debitAmount = getSummaryAmount(requestParams, Group.NATURE_EXPENSES, null, startDate, endDate);
                    creditAmount = getSummaryAmount(requestParams, Group.NATURE_INCOME, null, startDate, endDate);
                    if(liab_group!=null){//Calculate and Add liablities for credit amount
                        creditAmount += getSummaryAmount(requestParams, -1, liab_group.getID(), startDate, endDate);
                    }
                    obj.put(CCConstants.JSON_costcenterid, costCenter.getID());
                    obj.put(CCConstants.JSON_costcenterName, costCenter.getName());
                    obj.put("debitAmount", debitAmount<0?(debitAmount*-1):debitAmount);//Show summary amount in positive
                    obj.put("creditAmount", creditAmount<0?(creditAmount*-1):creditAmount);//Show summary amount in positive
                    DataJArr.put(obj);
                }
            }

            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_count, DataJArr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg += ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public double getSummaryAmount(HashMap<String, Object> requestParams, int groupNature, String groupId, Date startDate, Date endDate) throws ServiceException{
        double amount=0;
            HashMap<String, Object> filterParams = new HashMap<String, Object>();
            List filter_names = new ArrayList(),filter_params = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(requestParams.get(Constants.companyKey));
            filter_names.add("ISdeleted");
            filter_params.add(false);
            if(groupNature!=-1){
                filter_names.add("group.nature");
                filter_params.add(groupNature);
            }
            if(!StringUtil.isNullOrEmpty(groupId)){
                filter_names.add("group.ID");
                filter_params.add(groupId);
            }
            filterParams.put("filter_names", filter_names);
            filterParams.put("filter_params", filter_params);
            KwlReturnObject result = accAccountDAOobj.getAccount(filterParams);
            List<Account> resultList = result.getEntityList();

            if (resultList != null && !resultList.isEmpty()) {
                for (Account account : resultList) {
                    amount += getAccountBalance(requestParams, account.getID(), startDate, endDate);
                }
            }

        return amount;
    }
    
    /**
     * @author Neeraj
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getCashFlow(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;  
        try {
            jobj = getCashFlow(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * @author Neeraj
     * @param request
     * @return jobj
     * @throws ServiceException
     * @throws SessionExpiredException
     */
    public JSONObject getCashFlow(HttpServletRequest request) throws ServiceException, SessionExpiredException {
    	JSONObject jobj = new JSONObject();
    	try{
    		double cash1,cash2,cash3,cash4,cash5;

    		JSONArray jArr = new JSONArray();
    		    		
    		JSONObject obj = new JSONObject();
    		obj.put("name", messageSource.getMessage("acc.report.45", null, RequestContextUtils.getLocale(request)));  //"Operating Activities");
            obj.put("desc", messageSource.getMessage("acc.report.46", null, RequestContextUtils.getLocale(request)));  //"Cash Flow from sale & purchase of Products & Services");
            obj.put("fmt", "title");
            jArr.put(obj);

    		obj = new JSONObject();
    		obj.put("name", messageSource.getMessage("acc.report.47", null, RequestContextUtils.getLocale(request)));  //"Cash Sales");
            obj.put("desc", messageSource.getMessage("acc.report.48", null, RequestContextUtils.getLocale(request)));  //"Direct Cash Sales");
            cash1 = cashPurchaseOrSaleTotalAmount(request, false);
            obj.put("value", cash1);
            obj.put("fmt", "export");
            jArr.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.49", null, RequestContextUtils.getLocale(request)));  //"Cash Payment Received from Customers");
            obj.put("desc", messageSource.getMessage("acc.report.50", null, RequestContextUtils.getLocale(request)));  //"Customer Invoice Cash Payments");
            cash2 = ReceiptOrPaymentTotalAmount(request,true);
            obj.put("value", cash2);
            obj.put("fmt", "export");
            jArr.put(obj);
                        
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.51", null, RequestContextUtils.getLocale(request)));  //"Cash Purchases");
            obj.put("desc", messageSource.getMessage("acc.report.52", null, RequestContextUtils.getLocale(request)));  //"(Less) Direct Cash Purchases");
            cash3 = cashPurchaseOrSaleTotalAmount(request, true);
            obj.put("value", cash3);
            obj.put("fmt", "export");
            jArr.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.53", null, RequestContextUtils.getLocale(request)));  //"Cash Paid to Vendors");
            obj.put("desc", messageSource.getMessage("acc.report.54", null, RequestContextUtils.getLocale(request)));  //"(Less) Vendor Invoice Cash Payments");
            cash4 = ReceiptOrPaymentTotalAmount(request,false);
            obj.put("value", cash4);
            obj.put("fmt", "export");
            jArr.put(obj);
                      
            
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.55", null, RequestContextUtils.getLocale(request)));  //"Cash Flow from Operating Activities");
            obj.put("fmt", "total");
            obj.put("desc", messageSource.getMessage("acc.report.56", null, RequestContextUtils.getLocale(request)));  //"Difference (Total Received - Total Paid)");
            obj.put("value", ((cash1+cash2) - (cash3+cash4)));
            jArr.put(obj);
            
            cash5 = ((cash1+cash2) - (cash3+cash4));
            
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.57", null, RequestContextUtils.getLocale(request)));  //"Investing Activities");
            obj.put("desc", messageSource.getMessage("acc.report.58", null, RequestContextUtils.getLocale(request)));  //"Cash Flow from sale & purchase of Fixed Assets");
            obj.put("fmt", "title");
            jArr.put(obj);
            
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.59", null, RequestContextUtils.getLocale(request)));  //"Cash received from Sale of Fixed Asset");
            obj.put("desc", messageSource.getMessage("acc.report.60", null, RequestContextUtils.getLocale(request)));  //"Sale of Fixed Asset");
            cash2 = fixedAssetCash(request,false);
            obj.put("value", cash2);
            obj.put("fmt", "export");
            jArr.put(obj);
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.61", null, RequestContextUtils.getLocale(request)));  //"Purchase of fixed asset");
            obj.put("desc", messageSource.getMessage("acc.report.62", null, RequestContextUtils.getLocale(request)));  //"(Less) Purchase of Fixed Asset");
            cash1 = fixedAssetCash(request,true);
            obj.put("value", cash1);
            obj.put("fmt", "export");
            jArr.put(obj);
            
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.63", null, RequestContextUtils.getLocale(request)));  //"Cash Flow from Investing Activities");
            obj.put("fmt", "total");
            obj.put("desc", messageSource.getMessage("acc.report.64", null, RequestContextUtils.getLocale(request)));  //"Difference (Total Received - Total Paid)");
            obj.put("value", (cash2 - cash1));
            jArr.put(obj);
            
            obj = new JSONObject();
            obj.put("name", messageSource.getMessage("acc.report.65", null, RequestContextUtils.getLocale(request))); //"Net Increase or Decrease in Cash");
            obj.put("fmt", "total");
            obj.put("desc", messageSource.getMessage("acc.report.66", null, RequestContextUtils.getLocale(request)));  //"Total Cash Flow");
            obj.put("value", ((cash2 - cash1) + cash5));
            jArr.put(obj);

            jobj.put("data", jArr);
            
	    } catch (JSONException ex) {
	        Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
	        throw ServiceException.FAILURE("getCashFlow : "+ex.getMessage(), ex);
	    }
	    return jobj;
	}
    
    /**
     * @author Neeraj
     * @param request
     * @param isCashPurchase
     * @return cashTotal
     * @throws ServiceException
     * @throws SessionExpiredException
     */
    public double cashPurchaseOrSaleTotalAmount(HttpServletRequest request, boolean isCashPurchase) throws ServiceException, SessionExpiredException {
    	try{
			double cashTotal = 0,amount= 0;
	        List ll=null;
	
			KwlReturnObject cashPurchase,bAmt;
	        HashMap<String, Object> requestParams = new HashMap<String, Object>();
	        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
	        requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
	        requestParams.put("dateformat", authHandler.getDateFormatter(request));
	        requestParams.put("nondeleted", "true");
	        requestParams.put("cashonly", "true");
	        requestParams.put("creditonly", "false");
	        
	        String companyid = sessionHandlerImpl.getCompanyid(request);
	        String currencyid = sessionHandlerImpl.getCurrencyID(request);

	        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
	        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

	        
	        if(isCashPurchase){
	        	cashPurchase = accGoodsReceiptDAOObj.getGoodsReceipts(requestParams);
	        	List<GoodsReceipt> list = cashPurchase.getEntityList();
	        
	        	if(list!=null && !list.isEmpty()){
		            for(GoodsReceipt gReceipt:list){
		                JournalEntry je = gReceipt.getJournalEntry();
		                JournalEntryDetail d = gReceipt.getVendorEntry();
		                currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
		                if(gReceipt.isIsExpenseType()){
		                    ll=accGoodsReceiptCommon.getExpGRAmountDue(requestParams,gReceipt);
		                    amount=(Double)ll.get(1);
		                }
		                else{
		                    ll=accGoodsReceiptCommon.getGRAmountDue(requestParams,gReceipt);
		                    amount=(Double)ll.get(1);
		                }
		                if(gReceipt.isIsExpenseType()){
		                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, (Double)ll.get(0), currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
		                    cashTotal = cashTotal + (Double) bAmt.getEntityList().get(0);
		                }
		                else{
		                   bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, d.getAmount(), currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
		                   cashTotal = cashTotal + (Double) bAmt.getEntityList().get(0);
		                }
		            }
		        }
	        }else{
	        	cashPurchase = accInvoiceDAOobj.getInvoices(requestParams);
	        	List<Invoice> list = cashPurchase.getEntityList();
	        	
	        	if(list!=null && !list.isEmpty()){
		            for(Invoice invoice:list){
		                JournalEntry je = invoice.getJournalEntry();
		                JournalEntryDetail d = invoice.getCustomerEntry();
		                currencyid = (invoice.getCurrency() == null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());
		                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, d.getAmount(), currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
		                cashTotal = cashTotal + (Double) bAmt.getEntityList().get(0);
		            }
		        }
	        }
		 return cashTotal;
    	} catch (SessionExpiredException ex) {
	        Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
	        throw ServiceException.FAILURE("cashPurchaseOrSaleTotalAmount : "+ex.getMessage(), ex);
	    }
    }
    
    /**
     * @author Neeraj
     * @param request
     * @param iscashReceipt
     * @return cashTotal
     * @throws ServiceException
     * @throws SessionExpiredException
     */
    public double ReceiptOrPaymentTotalAmount(HttpServletRequest request, boolean iscashReceipt) throws ServiceException, SessionExpiredException {
    	try{
			double cashTotal = 0,amount= 0;
	        List ll=null;
	
			KwlReturnObject cashReceipt,bAmt;
	        HashMap<String, Object> requestParams = new HashMap<String, Object>();
	        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
	        requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
	        requestParams.put("dateformat", authHandler.getDateFormatter(request));
	        requestParams.put("nondeleted", "true");
	        
	        String companyid = sessionHandlerImpl.getCompanyid(request);
	        String currencyid = sessionHandlerImpl.getCurrencyID(request);

	        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
	        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

	        KwlReturnObject prefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) prefResult.getEntityList().get(0);
            
	        
	        if(iscashReceipt){
	        	cashReceipt = accReceiptDao.getReceipts(requestParams);
	        	if(cashReceipt!=null){
		        	Iterator itr  = cashReceipt.getEntityList().iterator();
		        	while(itr.hasNext()){
		        		Object[] row=(Object[])itr.next();
		        		Receipt receipt = (Receipt) row[0];
	                    Iterator itrRow=receipt.getRows().iterator();
	                	if(preferences.getCashAccount().getID().equals((receipt.getPayDetail()==null?"":receipt.getPayDetail().getPaymentMethod().getAccount().getID()))){
		                	amount=0;
		                    if(!receipt.getRows().isEmpty())
		                        while(itrRow.hasNext()){
		                            amount+=((ReceiptDetail)itrRow.next()).getAmount();
		                        }
		                    else{
		                        itrRow=receipt.getJournalEntry().getDetails().iterator();
		                        amount+=((JournalEntryDetail)itrRow.next()).getAmount();
		                    }
		                    currencyid = (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID());
		                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, currencyid, receipt.getJournalEntry().getEntryDate(),receipt.getJournalEntry().getExternalCurrencyRate());
			                amount = (Double) bAmt.getEntityList().get(0);
		                    cashTotal = cashTotal + amount;
	                	}
	                }
	        	}
	        }else{
	        	cashReceipt = accVendorPaymentDao.getPayments(requestParams);
	        	if(cashReceipt!=null){
		        	Iterator itr  = cashReceipt.getEntityList().iterator();
		        	while(itr.hasNext()){
		        		Object[] row=(Object[])itr.next();
		        		Payment payment = (Payment) row[0];
	                    Iterator itrRow=payment.getRows().iterator();
	                	if(preferences.getCashAccount().getID().equals((payment.getPayDetail()==null?"":payment.getPayDetail().getPaymentMethod().getAccount().getID()))){
		                    amount=0;
		                    if(!payment.getRows().isEmpty())
		                        while(itrRow.hasNext()){
		                            amount+=((PaymentDetail)itrRow.next()).getAmount();
		                        }
		                    else{
		                        itrRow=payment.getJournalEntry().getDetails().iterator();
		                        amount+=((JournalEntryDetail)itrRow.next()).getAmount();
		                    }
		                    currencyid = (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID());
		                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, currencyid, payment.getJournalEntry().getEntryDate(),payment.getJournalEntry().getExternalCurrencyRate());
			                amount = (Double) bAmt.getEntityList().get(0);
		                    cashTotal = cashTotal + amount;
	                	}
	                }
	        	}
	        }
		 return cashTotal;
    	} catch (SessionExpiredException ex) {
	        Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
	        throw ServiceException.FAILURE("ReceiptOrPaymentTotalAmount : "+ex.getMessage(), ex);
	    }
    }
    
    /**
     * @author Neeraj
     * @param request
     * @param isPurchase
     * @return total asset cash invested or sold
     * @throws ServiceException
     * @throws SessionExpiredException
     */
    public double fixedAssetCash(HttpServletRequest request, boolean isPurchase) throws ServiceException, SessionExpiredException {
    	try{
    		KwlReturnObject result, jeResult;
    		double cashTotal = 0;
    		String companyid = sessionHandlerImpl.getCompanyid(request);
    		
    		KwlReturnObject prefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) prefResult.getEntityList().get(0);
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
    		requestParams.put("companyid", companyid);
    		result = accDepreciationObj.getAsset(requestParams);
    		
    		
    		if(result != null && result.getEntityList().size() > 0){
    			Iterator itr  = result.getEntityList().iterator();
	        	while(itr.hasNext()){
	    			Asset asset = (Asset) itr.next();
	    			if(isPurchase){
	    				if(asset.getPurchaseJe() != null && !asset.getPurchaseJe().isDeleted()){
	    					jeResult = accJournalEntryobj.getJEDFixedAssetSale(companyid, preferences.getCashAccount().getID(), false, asset.getPurchaseJe().getID());
	    					if(jeResult != null && jeResult.getEntityList().size() > 0) {
		    					JournalEntryDetail jed = (JournalEntryDetail) jeResult.getEntityList().get(0);
		    					cashTotal = cashTotal + jed.getAmount();
	    					}
	    				}
	    			} else {
	    				if(asset.getDeleteJe() != null && !asset.getDeleteJe().isDeleted()){
	    					jeResult = accJournalEntryobj.getJEDFixedAssetSale(companyid, preferences.getCashAccount().getID(), true, asset.getDeleteJe().getID());
	    					if(jeResult != null && jeResult.getEntityList().size() > 0) {
		    					JournalEntryDetail jed = (JournalEntryDetail) jeResult.getEntityList().get(0);
		    					cashTotal = cashTotal + jed.getAmount();
	    					}
	    				}
	    			}
	        	}
    		}
		 return cashTotal;
    	} catch (SessionExpiredException ex) {
	        Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
	        throw ServiceException.FAILURE("fixedAssetCash : "+ex.getMessage(), ex);
	    }
    }
    
    
    public ModelAndView exportCashFlow(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            jobj = getCashFlowJSON(getCashFlow(request), currencyid);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
                exportDaoObj.processRequest(request, response, jobj);
            } else {
                if (StringUtil.equal(fileType, "csv")) {
                    exportDaoObj.processRequest(request, response, jobj);
                }else{
                    String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                    java.io.ByteArrayOutputStream baos = null;
                    String filename = request.getParameter("filename");
                    String comName = sessionHandlerImpl.getCompanyName(request);
                    baos = ExportrecordObj.exportCashFlow(jobj,logoPath,comName, request);
                    if (baos != null) {
                        ExportrecordObj.writeDataToFile(filename+"."+fileType, baos, response);
                    }
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public JSONObject getCashFlowJSON(JSONObject jobj,String currencyid){
        JSONObject retObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try{
            JSONArray leftObjArr = jobj.getJSONArray("data");
            int length = leftObjArr.length();
            for(int i=0;i<length;i++){
                JSONObject tempObj = new JSONObject();
                if(i<leftObjArr.length() && !leftObjArr.getJSONObject(i).toString().equalsIgnoreCase("{}")){
                    JSONObject leftObj = leftObjArr.getJSONObject(i);
                    String name = "";
                    String format = leftObj.has("fmt")?leftObj.get("fmt").toString():"";
                    String value = leftObj.has("value")?formatValue(leftObj.get("value").toString(),format,currencyid):"";
                    if(leftObj.has("desc")){
                        name = leftObj.get("name").toString()+" ("+leftObj.get("desc").toString()+")";
                    }else{
                        name = leftObj.get("name").toString();
                    }
                    tempObj.put("lname",name);
                    tempObj.put("lvalue",value);
                    tempObj.put("lfmt",format);
                }else{
                    tempObj.put("lname","");
                    tempObj.put("lvalue","");
                    tempObj.put("lfmt","");
                }
                jArr.put(tempObj);
            }
            retObj.put("data",jArr);
        }catch(Exception ex){
             Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retObj;
    }
     
    public ModelAndView exportCostCenterSummary(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            JSONArray DataJArr = new JSONArray();
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            Date startDate = authHandler.getDateFormatter(request).parse(request.getParameter(Constants.REQ_startdate));
            Date endDate = authHandler.getDateFormatter(request).parse(request.getParameter(Constants.REQ_enddate));

            String companyid = (String) requestParams.get(Constants.companyKey);
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(companyid);
            requestParams.put(Constants.filterNamesKey, filter_names);
            requestParams.put(Constants.filterParamsKey, filter_params);
            KwlReturnObject result = accCostCenterObj.getCostCenter(requestParams);
            List<CostCenter> costCenters = result.getEntityList();

            KwlReturnObject ret =  accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Group", Group.OTHER_CURRENT_LIABILITIES);
            Group liab_group = null;
            if(!ret.getEntityList().isEmpty()){
                liab_group = (Group) ret.getEntityList().get(0);
            }

            double debitAmount = 0, creditAmount = 0;
            if (costCenters != null && !costCenters.isEmpty()) {
                for (CostCenter costCenter : costCenters) {
                    debitAmount = 0; creditAmount = 0;
                    JSONObject obj = new JSONObject();
                    requestParams.put(CCConstants.REQ_costcenter, costCenter.getID());
                    debitAmount = getSummaryAmount(requestParams, Group.NATURE_EXPENSES, null, startDate, endDate);
                    creditAmount = getSummaryAmount(requestParams, Group.NATURE_INCOME, null, startDate, endDate);
                    if(liab_group!=null){//Calculate and Add liablities for credit amount
                        creditAmount += getSummaryAmount(requestParams, -1, liab_group.getID(), startDate, endDate);
                    }
                    obj.put(CCConstants.JSON_costcenterid, costCenter.getID());
                    obj.put(CCConstants.JSON_costcenterName, costCenter.getName());
                    obj.put("debitAmount", debitAmount<0?(debitAmount*-1):debitAmount);//Show summary amount in positive
                    obj.put("creditAmount", creditAmount<0?(creditAmount*-1):creditAmount);//Show summary amount in positive
                    DataJArr.put(obj);
                }
            }

            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_count, DataJArr.length());
            issuccess = true;
            String view = "jsonView_ex";
            String fileType = request.getParameter("fileType");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
                exportDaoObj.processRequest(request, response, jobj);
            } else {
                if (StringUtil.equal(fileType, "csv")) {
                    exportDaoObj.processRequest(request, response, jobj);
                }else{
                    String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                    String filename = request.getParameter("name");
                    String comName = sessionHandlerImpl.getCompanyName(request);
                    exportDaoObj.processRequest(request, response, jobj);
                }
            }
        } catch (Exception ex) {
            msg += ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView-empty", "model", jobj.toString());
    }

	@Override
	public void setMessageSource(MessageSource msg) {
		this.messageSource = msg;
	}
}
