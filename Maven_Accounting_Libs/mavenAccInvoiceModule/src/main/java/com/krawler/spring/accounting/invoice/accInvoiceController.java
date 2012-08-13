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
package com.krawler.spring.accounting.invoice;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BillingInvoice;
import com.krawler.hql.accounting.BillingInvoiceDetail;
import com.krawler.hql.accounting.BillingSalesOrderDetail;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.ExchangeRateDetails;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.RepeatedInvoices;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class accInvoiceController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accInvoiceDAO accInvoiceDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private String successView;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
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
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public ModelAndView saveInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try{
            List li = saveInvoice(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Boolean cash=Boolean.parseBoolean(request.getParameter("incash"));
//            if(!cash){
//            try{
//                AccountDBCon.sendMailToCustomer(request, StaticValues.AUTONUM_INVOICE, id);
//            }catch(Exception ex){
//            }
//            }
//            obj.put("msg", (cash?"Sales Receipt":"Invoice")+" has been saved successfully");
        String[] id = (String[]) li.get(0);
        ArrayList discountArr=(ArrayList) li.get(1);
            jobj.put("invoiceid", id[0]);
            msg = (cash?messageSource.getMessage("acc.inv.2", null, RequestContextUtils.getLocale(request)):messageSource.getMessage("acc.inv.1", null, RequestContextUtils.getLocale(request)));
            issuccess = true;
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteJEArray(id[1],companyid);
             txnManager.commit(status);
             status = txnManager.getTransaction(def);
            deleteEditedInvoiceDiscount(discountArr,companyid);
             txnManager.commit(status);
        } catch (SessionExpiredException ex){
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex){
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex){
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveInvoice(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        String id = null;
        List ll = new ArrayList();
        String jeentryNumber= null;
        Invoice invoice = null;
        ArrayList discountArr=new ArrayList();
        String oldjeid=null;
        try {
            KwlReturnObject result=null;
            int nocount;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            DateFormat df = authHandler.getDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String invoiceid =request.getParameter("invoiceid");
            String jeid=null;
            boolean jeautogenflag = false;
            
            if (!StringUtil.isNullOrEmpty(invoiceid)){
                KwlReturnObject invObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                invoice=(Invoice)invObj.getEntityList().get(0);
                oldjeid=invoice.getJournalEntry().getID();
                jeautogenflag = invoice.getJournalEntry().isAutoGenerated();
                result = accInvoiceDAOobj.getInvoiceInventory(invoiceid);
                ////deleting invoice row
                accInvoiceDAOobj.deleteInvoiceDtails(invoiceid,companyid);
                List list =  result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    String inventoryid = (String) itr.next();
                    accProductObj.deleteInventory(inventoryid,companyid);
                }

                ////Deleting all Invoice Detail discount
                result = accInvoiceDAOobj.getInvoiceDetailsDiscount(invoiceid);
                list =  result.getEntityList();
               itr = list.iterator();
                while (itr.hasNext()) {
                    String discountid = (String) itr.next();
                    if (StringUtil.isNullOrEmpty(discountid))
                        discountArr.add(discountid);
               //     accDiscountobj.deleteDiscount(discountid,companyid);
                }
                String discountid=(invoice.getDiscount()==null?null:invoice.getDiscount().getID());
                invoice.setDiscount(null);
                if (StringUtil.isNullOrEmpty(discountid))
                    discountArr.add(discountid);
             //   accDiscountobj.deleteDiscount(discountid,companyid);


                ////deleting invoice inventory
               
                ////Deleting Invoice Detail Journalentry Detail
                String nl=null;
                HashMap<String, Object> dataMap = new HashMap<String, Object>();

                dataMap.put("invoiceid", invoiceid);
                dataMap.put("otherentryid", nl);
                dataMap.put("shipentryid", nl);
                dataMap.put("taxid", nl);
                dataMap.put("taxentryid", nl);
                dataMap.put("customerentryid", nl);
                KwlReturnObject uresult = accInvoiceDAOobj.saveInvoice(dataMap);
                invoice = (Invoice) uresult.getEntityList().get(0);
                jeentryNumber=invoice.getJournalEntry().getEntryNumber();
            }
            String taxid = request.getParameter("taxid");
            String costCenterId = request.getParameter("costcenter");
            double taxamount = StringUtil.getDouble(request.getParameter("taxamount"));
            double externalCurrencyRate=StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            Discount discount = null;
            double discValue = 0.0;
            double shippingCharges = StringUtil.getDouble(request.getParameter("shipping"));
            double otherCharges = StringUtil.getDouble(request.getParameter("othercharges"));
            boolean inCash = Boolean.parseBoolean(request.getParameter("incash"));
            
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmp.getEntityList().get(0);
            
            String currencyid=(request.getParameter("currencyid")==null? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            
            String entryNumber = request.getParameter("number");
            String customerid = request.getParameter("customer");
            JSONObject invjson = new JSONObject();
            invjson.put("invoiceid", invoiceid);
            invjson.put("customerid", customerid);
             if (StringUtil.isNullOrEmpty(invoiceid)){
                result = accInvoiceDAOobj.getInvoiceCount(entryNumber, companyid);
                nocount = result.getRecordTotalCount();
                if (nocount > 0) {
                    throw new AccountingException("Invoice number '" + entryNumber + "' already exists.<br>Save again with given auto number.<br>Or type other number");
                }
                int from = StaticValues.AUTONUM_INVOICE;
                if (inCash) {
                    from = StaticValues.AUTONUM_CASHSALE;
                }
                    String nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, from);
                    invjson.put("entrynumber", entryNumber);
                    invjson.put("autogenerated", nextAutoNo.equals(entryNumber));
            }
            invjson.put("memo", request.getParameter("memo"));
            invjson.put("billto", request.getParameter("billto"));
            invjson.put("shipaddress", request.getParameter("shipaddress"));
            invjson.put("shipdate", df.parse(request.getParameter("shipdate")));
            invjson.put("porefno", request.getParameter("porefno"));
            invjson.put("duedate", df.parse(request.getParameter("duedate")));
            invjson.put("companyid", companyid);
            invjson.put("currencyid", currencyid);
            invjson.put("externalCurrencyRate", externalCurrencyRate);
// Create Journal Entry            
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            Map<String,Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", df.parse(request.getParameter("billdate")));
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            jeDataMap.put("costcenterid", costCenterId);
            HashSet jeDetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            invjson.put("journalerentryid", jeid);
            jeDataMap.put("jeid", jeid);


            String invoiceDetails = request.getParameter("detail");
            List dll = saveInvoiceRows(request, invoiceDetails, jeid, company, jeDetails, currencyid, externalCurrencyRate);
            double[] totals = (double[]) dll.get(0);
            HashSet<InvoiceDetail> invcdetails = (HashSet<InvoiceDetail>) dll.get(1);
            double disc = StringUtil.getDouble(request.getParameter("discount"));
            if (disc > 0) {
//                discount=new Discount();
//                discount.setDiscount(disc);
//                discount.setInPercent(Boolean.parseBoolean(request.getParameter("perdiscount")));
//                discount.setOriginalAmount(totals[1]-totals[0]);
//                discount.setCompany(company);
                JSONObject discjson = new JSONObject();
                discjson.put("discount", disc);
                discjson.put("inpercent", Boolean.parseBoolean(request.getParameter("perdiscount")));
                discjson.put("originalamount", totals[1] - totals[0]+totals[2]);
                discjson.put("companyid", companyid);
                KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                discount = (Discount) dscresult.getEntityList().get(0);
//                invoice.setDiscount(discount);
                invjson.put("discountid", discount.getID());
                discValue = discount.getDiscountValue();
            }
            discValue += totals[0];

            String accountid = request.getParameter("customer");
            if (inCash) {
                accountid = preferences.getCashAccount().getID();
            }
//            jed.setCompany(company);
//            jed.setAmount(totals[1]+shippingCharges+otherCharges-discValue+taxamount);
//            if(!inCash){
//                jed.setAccount((Account)session.get(Account.class, request.getParameter("customer")));
//            }else
//                jed.setAccount(preferences.getCashAccount());
//            jed.setDebit(true);
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jeDetails.size()+1);
            jedjson.put("companyid", companyid);
            jedjson.put("amount", totals[1] + shippingCharges + otherCharges - discValue + taxamount + totals[2]);
            jedjson.put("accountid", accountid);
            jedjson.put("debit", true);
            jedjson.put("jeid", jeid);
   //         jedjson.put("jedid", invoice==null?null:invoice.getCustomerEntry().getID());
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jeDetails.add(jed);
//            invoice.setCustomerEntry(jed);
            invjson.put("customerentryid", jed.getID());

            if (discValue > 0) {
//                jed=new JournalEntryDetail();
//                jed.setCompany(company);
//                jed.setAmount(discValue);
//                jed.setAccount(preferences.getDiscountGiven());
//                jed.setDebit(true);
                jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size()+1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", discValue);
                jedjson.put("accountid", preferences.getDiscountGiven().getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);
            }
            if (shippingCharges > 0) {
//                jed=new JournalEntryDetail();
//                jed.setCompany(company);
//                jed.setAmount(shippingCharges);
//                jed.setAccount(preferences.getShippingCharges());
//                jed.setDebit(false);
                jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size()+1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", shippingCharges);
                jedjson.put("accountid", preferences.getShippingCharges().getID());
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);
//                invoice.setShipEntry(jed);
                invjson.put("shipentryid", jed.getID());

            }
            if (otherCharges > 0) {
//                jed=new JournalEntryDetail();
//                jed.setCompany(company);
//                jed.setAmount(otherCharges);
//                jed.setAccount(preferences.getOtherCharges());
//                jed.setDebit(false);
                jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size()+1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", otherCharges);
                jedjson.put("accountid", preferences.getOtherCharges().getID());
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);
//                invoice.setOtherEntry(jed);
                invjson.put("otherentryid", jed.getID());

            }
            if (!StringUtil.isNullOrEmpty(taxid)) {
                KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(),taxid); // (Tax)session.get(Tax.class, taxid);
                Tax tax = (Tax) txresult.getEntityList().get(0);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
//                invoice.setTax(tax);
                invjson.put("taxid", taxid);

                if (taxamount > 0) {
//                    jed=new JournalEntryDetail();
//                    jed.setCompany(company);
//                    jed.setAmount(taxamount);
//                    jed.setAccount(tax.getAccount());
//                    jed.setDebit(false);
                    jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size()+1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", taxamount);
                    jedjson.put("accountid", tax.getAccount().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);
//                    invoice.setTaxEntry(jed);
                invjson.put("taxentryid", jed.getID());

            }

            }
//            JournalEntry journalEntry=CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),
//            request.getParameter("memo"), "JE"+invoice.getInvoiceNumber(), hs,request);
//            invoice.setJournalEntry(journalEntry);

            jeDataMap.put("jedetails", jeDetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

//            ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),null);
//            invoice.setExchangeRateDetail(erd);
            KwlReturnObject ERresult = accCurrencyDAOobj.getExcDetailID(requestParams, currencyid, df.parse(request.getParameter("billdate")), null);
            ExchangeRateDetails erd = (ExchangeRateDetails) ERresult.getEntityList().get(0);
            String erdid = (erd == null) ? null : erd.getID();
            invjson.put("erdid", erdid);

            result = accInvoiceDAOobj.addInvoice(invjson, new HashSet());
            invoice = (Invoice) result.getEntityList().get(0);//Create Invoice without invoice-details.
            Iterator itr = invcdetails.iterator();
            while (itr.hasNext()) {
                InvoiceDetail ivd = (InvoiceDetail) itr.next();
                ivd.setInvoice(invoice);
            }
            invjson.put("invoiceid", invoice.getID());
            result = accInvoiceDAOobj.updateInvoice(invjson, invcdetails);
            invoice = (Invoice) result.getEntityList().get(0);//Add invoice details
                        
              id = invoice.getID();
        } catch (ParseException ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        ll.add(new String[]{id, oldjeid});
        ll.add(discountArr);
       return  ll;
    }

    public void deleteJEArray(String oldjeid,String companyid) throws ServiceException, AccountingException, SessionExpiredException {
      try{      //delete old invoice
          JournalEntryDetail jed=null;
            if (!StringUtil.isNullOrEmpty(oldjeid)) {
                 KwlReturnObject   result = accJournalEntryobj.getJournalEntryDetail(oldjeid, companyid);
                List list =  result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    jed = (JournalEntryDetail) itr.next();
                    result = accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
                }
               result = accJournalEntryobj.permanentDeleteJournalEntry(oldjeid, companyid);
               
            }
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public void deleteEditedInvoiceDiscount(ArrayList discArr,String companyid) throws ServiceException, AccountingException, SessionExpiredException {
      try{
           for (int i=0;i<discArr.size();i++)
               if(discArr.get(i)!=null)
                    accDiscountobj.deleteDiscount(discArr.get(i).toString(),companyid);
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    private List saveInvoiceRows(HttpServletRequest request, String invoiceDetails, String jeid, Company company, HashSet jeDetails, String currencyid, Double externalCurrencyRate) throws ServiceException, SessionExpiredException, AccountingException{
        HashSet hs = new HashSet(), rows = new HashSet();
        double totaldiscount = 0, totalamount = 0,taxamount=0;
        List ll = new ArrayList();
        try {
            DateFormat df = authHandler.getDateFormatter(request);
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            String companyid = company.getCompanyID();
            JSONArray jArr = new JSONArray(invoiceDetails);
                for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                InvoiceDetail row = new InvoiceDetail();
                JournalEntryDetail jed;
                row.setSrno(i+1);
//                SalesOrderDetail rd = (SalesOrderDetail) session.get(SalesOrderDetail.class, jobj.getString("rowid"));\
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), jobj.getString("rowid"));
                SalesOrderDetail rd = (SalesOrderDetail) rdresult.getEntityList().get(0);
                row.setSalesorderdetail(rd);
                row.setCompany(company);
    //            row.setInvoice(invoice);
    //            row.setRate(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("rate"),currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate"))));
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("rate"), currencyid, df.parse(request.getParameter("billdate")), externalCurrencyRate);
                row.setRate((Double) bAmt.getEntityList().get(0));
                KwlReturnObject prdresult = accountingHandlerDAOobj.getObject(Product.class.getName(),jobj.getString("productid"));
                Product product = (Product) prdresult.getEntityList().get(0);
                //(Product)session.get(Product.class, jobj.getString("productid"));

                JSONObject inventoryjson = new JSONObject();
                inventoryjson.put("productid", jobj.getString("productid"));
                inventoryjson.put("quantity", jobj.getInt("quantity"));
                inventoryjson.put("description", jobj.getString("desc"));
                inventoryjson.put("carryin", false);
                inventoryjson.put("defective", false);
                inventoryjson.put("newinventory", false);
                inventoryjson.put("companyid", companyid);
                inventoryjson.put("updatedate", AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")));
                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                Inventory inventory = (Inventory) invresult.getEntityList().get(0);

                row.setInventory(inventory);
                totalamount += row.getRate() * jobj.getInt("quantity");
                Discount discount = null;
                double disc = jobj.getDouble("prdiscount");
                if (disc != 0.0) {
    //                discount=new Discount();
    //                discount.setDiscount(disc);
    //                discount.setOriginalAmount(row.getRate()*row.getInventory().getQuantity());
    //                discount.setInPercent(true);
    //                discount.setCompany(company);
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", disc);
                    discjson.put("inpercent", true);
                    discjson.put("originalamount", row.getRate() * jobj.getInt("quantity"));
                    discjson.put("companyid", companyid);
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                    totaldiscount += discount.getDiscountValue();
                }
                
                if (hs.add(product.getSalesAccount())) {
    //                jed=new JournalEntryDetail();
    //                jed.setCompany(company);
    //                jed.setAccount(product.getSalesAccount());
    //                jed.setAmount(row.getRate()*inventory.getQuantity());
    //                jed.setDebit(false);
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size()+1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", row.getRate() * jobj.getInt("quantity"));
                    jedjson.put("accountid", product.getSalesAccount().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);
                } else {
                    Iterator itr = jeDetails.iterator();
                    while (itr.hasNext()) {
                        jed = (JournalEntryDetail) itr.next();
                        if (jed.getAccount() == product.getSalesAccount()) {
//                            jed.setAmount(jed.getAmount() + row.getRate() * inventory.getQuantity());
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("jedid", jed.getID());
                            jedjson.put("amount", jed.getAmount() + row.getRate() * inventory.getQuantity());
                            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            break;
                        }
                    }
                }
                String rowtaxid = jobj.getString("prtaxid");
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    boolean taxExist=false;
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(),rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null)
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    else{
                        row.setTax(rowtax);
                        double rowtaxamount=StringUtil.getDouble(jobj.getString("taxamount"));
                        bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, rowtaxamount, currencyid, df.parse(request.getParameter("billdate")), externalCurrencyRate);
                        rowtaxamount= (Double) bAmt.getEntityList().get(0);
                        taxamount += rowtaxamount;
                        if (taxamount > 0) {
                            Iterator itr = jeDetails.iterator();
                            while (itr.hasNext()) {
                                jed = (JournalEntryDetail) itr.next();
                                if (jed.getAccount() == rowtax.getAccount()) {
//                                          jed.setAmount(jed.getAmount() + row.getRate() * inventory.getQuantity());
                                    JSONObject jedjson = new JSONObject();
                                    jedjson.put("jedid", jed.getID());
                                    jedjson.put("amount",jed.getAmount() +rowtaxamount );
                                    KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    taxExist=true;
                                    break;
                                }
                            }
                            if(!taxExist){
                                JSONObject jedjson = new JSONObject();
                                jedjson = new JSONObject();
                                jedjson.put("srno", jeDetails.size()+1);
                                jedjson.put("companyid", companyid);
                                jedjson.put("amount", rowtaxamount);
                                jedjson.put("accountid", rowtax.getAccount().getID());
                                jedjson.put("debit", false);
                                jedjson.put("jeid", jeid);
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);

                            }
                        }
                    }
                }
                rows.add(row);
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveInvoiceRows : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveInvoiceRows : "+ex.getMessage(), ex);
        }
//        invoice.setRows(rows);
        ll.add(new double[]{totaldiscount, totalamount,taxamount});
        ll.add(rows);
        return ll;
    }

    public ModelAndView saveBillingInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try{
            List li = saveBillingInvoice(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Boolean cash=Boolean.parseBoolean(request.getParameter("incash"));
//            if(!cash){
//            try{
//                AccountDBCon.sendMailToCustomer(request, StaticValues.AUTONUM_INVOICE, id);
//            }catch(Exception ex){
//            }
//            }
//            obj.put("msg", (cash?"Sales Receipt":"Invoice")+" has been saved successfully");
            String[] id = (String[]) li.get(0);
            ArrayList discountArr=(ArrayList) li.get(1);
            jobj.put("invoiceid", id[0]);
            msg = (cash?"Sales Receipt":"Invoice")+" has been saved successfully";
            issuccess = true;
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteJEArray(id[1],companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteEditedInvoiceDiscount(discountArr,companyid);
            txnManager.commit(status);
        } catch (SessionExpiredException ex){
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex){
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex){
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return new ModelAndView("jsonView", "model", jobj.toString());
        }
    }

    public List saveBillingInvoice(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        String id =null;
        BillingInvoice invoice = null;
        List ll = new ArrayList();
        String jeentryNumber= null;
        ArrayList discountArr=new ArrayList();
        String oldjeid=null;
		try {
            KwlReturnObject result=null;
            String jeid=null;
            boolean jeautogenflag = false;
            int nocount;
            double externalCurrencyRate= StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            DateFormat df = authHandler.getDateFormatter(request);
            Discount discount = null;
            double discValue = 0.0;
            String taxid = request.getParameter("taxid");

            String costCenterId = request.getParameter("costcenter");
            double taxamount = StringUtil.getDouble(request.getParameter("taxamount"));
            double shippingCharges = StringUtil.getDouble(request.getParameter("shipping"));
            double otherCharges = StringUtil.getDouble(request.getParameter("othercharges"));
            boolean inCash = Boolean.parseBoolean(request.getParameter("incash"));
            String invoiceid =request.getParameter("invoiceid");
//            CompanyAccountPreferences preferences=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
//            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(invoiceid)){
                KwlReturnObject invObj = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), invoiceid);
                invoice=(BillingInvoice)invObj.getEntityList().get(0);
                oldjeid=invoice.getJournalEntry().getID();
                jeautogenflag = invoice.getJournalEntry().isAutoGenerated();
                  ////deleting invoice row
                result = accInvoiceDAOobj.deleteBillingInvoiceDtails(invoiceid,companyid);

                ////Deleting all Invoice Detail discount
                result = accInvoiceDAOobj.getBillingInvoiceDetailsDiscount(invoiceid);
                List list =  result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    String discountid = (String) itr.next();
                    if (StringUtil.isNullOrEmpty(discountid))
                        discountArr.add(discountid);
               //     accDiscountobj.deleteDiscount(discountid,companyid);
                }
                String discountid=(invoice.getDiscount()==null?null:invoice.getDiscount().getID());
                invoice.setDiscount(null);
                if (StringUtil.isNullOrEmpty(discountid))
                    discountArr.add(discountid);
             //   accDiscountobj.deleteDiscount(discountid,companyid);


//                ////deleting invoice inventory
//                result = accInvoiceDAOobj.getInvoiceInventory(invoiceid);
//                list =  result.getEntityList();
//                itr = list.iterator();
//                while (itr.hasNext()) {
//                    String inventoryid = (String) itr.next();
//                    accProductObj.deleteInventory(inventoryid,companyid);
//                }

                ////Deleting Invoice Detail Journalentry Detail
                String nl=null;
                HashMap<String, Object> dataMap = new HashMap<String, Object>();

                dataMap.put("invoiceid", invoiceid);
                dataMap.put("otherentryid", nl);
                dataMap.put("shipentryid", nl);
                dataMap.put("taxid", nl);
                dataMap.put("taxentryid", nl);
                dataMap.put("customerentryid", nl);
                dataMap.put("creditorentryid", nl);
                KwlReturnObject uresult = accInvoiceDAOobj.saveBillingInvoice(dataMap);
                invoice = (BillingInvoice) uresult.getEntityList().get(0);
                jeentryNumber=invoice.getJournalEntry().getEntryNumber();
            }
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmp.getEntityList().get(0);
            String currencyid=(request.getParameter("currencyid")==null? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));

//            BillingInvoice invoice = new BillingInvoice();
            JSONObject invjson = new JSONObject();
            String entryNumber = request.getParameter("number");
            invjson.put("invoiceid", invoiceid);

//            String q="from BillingInvoice where billingInvoiceNumber=? and company.companyID=?";
//            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                throw new AccountingException("Invoice number '"+entryNumber+"' already exists.<br>Save again with given auto number.<br>Or type other number");
            if (StringUtil.isNullOrEmpty(invoiceid)){
                result = accInvoiceDAOobj.getBillingInvoiceCount(entryNumber, companyid);
                nocount = result.getRecordTotalCount();
                if (nocount > 0) {
                    throw new AccountingException("Invoice number '" + entryNumber + "' already exists.<br>Save again with given auto number.<br>Or type other number");
                }
    //            invoice.setBillingInvoiceNumber(entryNumber);

                int from = StaticValues.AUTONUM_BILLINGINVOICE;
                if (inCash) {
                    from = StaticValues.AUTONUM_BILLINGCASHSALE;
                }
                String nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, from);
                 invjson.put("entrynumber", entryNumber);
                invjson.put("autogenerated", nextAutoNo.equals(entryNumber));
            }
//            invoice.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, from).equals(entryNumber));
//            invoice.setMemo(request.getParameter("memo"));
//            invoice.setBillTo(request.getParameter("billto"));
//            invoice.setShipTo(request.getParameter("shipaddress"));
//            invoice.setShipDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("shipdate")));
//            invoice.setDueDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("duedate")));
//            invoice.setCompany(company);
            
            invjson.put("memo", request.getParameter("memo"));
            invjson.put("billto", request.getParameter("billto"));
            invjson.put("shipaddress", request.getParameter("shipaddress"));
            invjson.put("shipdate", df.parse(request.getParameter("shipdate")));
            invjson.put("porefno", request.getParameter("porefno"));
            invjson.put("duedate", df.parse(request.getParameter("duedate")));
            invjson.put("companyid", companyid);
            invjson.put("currencyid", currencyid);
            invjson.put("customerid", request.getParameter("customer"));
            invjson.put("externalCurrencyRate", externalCurrencyRate);

// Create Journal Entry            
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            Map<String,Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", df.parse(request.getParameter("billdate")));
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("costcenterid", costCenterId);
            jeDataMap.put("currencyid", currencyid);
            
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            invjson.put("journalerentryid", jeid);
            jeDataMap.put("jeid", jeid);

            
            HashSet jedetails = new HashSet();
//            double[] totals = saveBillingInvoiceRows(session, request, invoice, company, jedetails);
            String invoiceDetails = request.getParameter("detail");
            List dll = saveBillingInvoiceRows(request,invoiceDetails, company,jeid,jedetails,currencyid,externalCurrencyRate);
            double[] totals = (double[]) dll.get(0);
            HashSet<InvoiceDetail> invcdetails = (HashSet<InvoiceDetail>) dll.get(1);


            double disc = StringUtil.getDouble(request.getParameter("discount"));
            if(disc>0){
//                discount=new Discount();
//                discount.setDiscount(disc);
//                discount.setInPercent(Boolean.parseBoolean(request.getParameter("perdiscount")));
//                discount.setOriginalAmount(totals[1]-totals[0]);
//                discount.setCompany(company);
//                invoice.setDiscount(discount);
//                session.save(discount);
                JSONObject discjson = new JSONObject();
                discjson.put("discount", disc);
                discjson.put("inpercent", Boolean.parseBoolean(request.getParameter("perdiscount")));
                discjson.put("originalamount", totals[1] - totals[0]+totals[2]);
                discjson.put("companyid", companyid);
                KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                discount = (Discount) dscresult.getEntityList().get(0);

                invjson.put("discountid", discount.getID());
                discValue = discount.getDiscountValue();
            }
            discValue += totals[0];

            String accountid = request.getParameter("customer");
            if (inCash) {
                accountid = preferences.getCashAccount().getID();
            }
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", companyid);
            jedjson.put("amount", totals[1] + shippingCharges + otherCharges - discValue + taxamount+totals[2]);
            jedjson.put("accountid", accountid);
            jedjson.put("debit", true);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);

//            JournalEntryDetail jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(totals[1]+shippingCharges+otherCharges-discValue+taxamount);
//            if (!inCash) {
//                jed.setAccount((Account) session.get(Account.class, request.getParameter("customer")));
//            } else {
//                jed.setAccount(preferences.getCashAccount());
//            }
//            jed.setDebit(true);
            jedetails.add(jed);
//            invoice.setCustomerEntry(jed);
            invjson.put("customerentryid", jed.getID());

            jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", companyid);
            jedjson.put("amount", totals[1]);
            jedjson.put("accountid", request.getParameter("creditoraccount"));
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);

//            jed=new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAccount((Account)session.get(Account.class, request.getParameter("creditoraccount")));
//            jed.setAmount(totals[1]);
//            jed.setDebit(false);
            jedetails.add(jed);
//            invoice.setCreditorEntry(jed);
            invjson.put("creditorentryid", jed.getID());

            if(discValue>0){
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size()+1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", discValue);
                jedjson.put("accountid", preferences.getDiscountGiven().getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                jed=new JournalEntryDetail();
//                jed.setCompany(company);
//                jed.setAmount(discValue);
//                jed.setAccount(preferences.getDiscountGiven());
//                jed.setDebit(true);
                jedetails.add(jed);
            }
            if(shippingCharges>0){
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size()+1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", shippingCharges);
                jedjson.put("accountid", preferences.getShippingCharges().getID());
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                jed=new JournalEntryDetail();
//                jed.setCompany(company);
//                jed.setAmount(shippingCharges);
//                jed.setAccount(preferences.getShippingCharges());
//                jed.setDebit(false);
                jedetails.add(jed);
//                invoice.setShipEntry(jed);
                invjson.put("shipentryid", jed.getID());
            }
            if(otherCharges>0){
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size()+1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", otherCharges);
                jedjson.put("accountid", preferences.getOtherCharges().getID());
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                jed=new JournalEntryDetail();
//                jed.setCompany(company);
//                jed.setAmount(otherCharges);
//                jed.setAccount(preferences.getOtherCharges());
//                jed.setDebit(false);
                jedetails.add(jed);
//                invoice.setOtherEntry(jed);
                invjson.put("otherentryid", jed.getID());
            }
            if(taxid!=null&&!taxid.isEmpty()){
//                Tax tax = (Tax)session.get(Tax.class, taxid);
                KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxid); // (Tax)session.get(Tax.class, taxid);
                Tax tax = (Tax) txresult.getEntityList().get(0);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
//                invoice.setTax(tax);
                invjson.put("taxid", taxid);
                
                if(taxamount>0){
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size()+1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", taxamount);
                    jedjson.put("accountid", tax.getAccount().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                    jed=new JournalEntryDetail();
//                    jed.setCompany(company);
//                    jed.setAmount(taxamount);
//                    jed.setAccount(tax.getAccount());
//                    jed.setDebit(false);
                    jedetails.add(jed);
//                    invoice.setTaxEntry(jed);
                    invjson.put("taxentryid", jed.getID());
                }
            }
//            JournalEntry journalEntry=CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),
//            request.getParameter("memo"), "JE"+invoice.getBillingInvoiceNumber(), hs,request);
//            invoice.setJournalEntry(journalEntry);
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

//            ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),null);
//            invoice.setExchangeRateDetail(erd);
            KwlReturnObject ERresult = accCurrencyDAOobj.getExcDetailID(requestParams, currencyid, df.parse(request.getParameter("billdate")), null);
            ExchangeRateDetails erd = (ExchangeRateDetails) ERresult.getEntityList().get(0);
            String erdid = (erd == null) ? null : erd.getID();
            invjson.put("erdid", erdid);

            result = accInvoiceDAOobj.addBillingInvoice(invjson, new HashSet());
            invoice = (BillingInvoice) result.getEntityList().get(0);//Create Invoice without invoice-details.

            Iterator itr = invcdetails.iterator();
            while (itr.hasNext()) {
                BillingInvoiceDetail ivd = (BillingInvoiceDetail) itr.next();
                ivd.setBillingInvoice(invoice);
            }

            invjson.put("invoiceid", invoice.getID());
            result = accInvoiceDAOobj.updateBillingInvoice(invjson, invcdetails);
            invoice = (BillingInvoice) result.getEntityList().get(0);//Add invoice details

//            session.saveOrUpdate(invoice);
            id = invoice.getID();
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }

         ll.add(new String[]{id, oldjeid});
        ll.add(discountArr);
       return  ll;
    }

    private List saveBillingInvoiceRows(HttpServletRequest request,String InvoiceDetails, Company company,String jeid, HashSet jeDetails,String currencyid,double externalCurrencyRate) throws JSONException, ServiceException, AccountingException, ParseException, SessionExpiredException {
        List list = new ArrayList();
        HashSet rows = new HashSet();
        double totaldiscount = 0;
        double totalamount = 0,taxamount=0;
        JSONArray jArr = new JSONArray(InvoiceDetails);
        DateFormat df = authHandler.getDateFormatter(request);
       HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
        try {
            for (int i = 0; i < jArr.length(); i++) {
                JournalEntryDetail jed;
                JSONObject jobj = jArr.getJSONObject(i);
                BillingInvoiceDetail row = new BillingInvoiceDetail();
                row.setSrno(i+1);
                row.setCompany(company);
//            row.setBillingInvoice(invoice);
                row.setRate(jobj.getDouble("rate"));
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(BillingSalesOrderDetail.class.getName(), jobj.getString("rowid"));
                BillingSalesOrderDetail salesOrderDetails = (BillingSalesOrderDetail) cap.getEntityList().get(0);
                row.setSalesOrderDetail(salesOrderDetails);
                row.setQuantity(jobj.getDouble("quantity"));
                row.setAmount(jobj.getInt("calamount"));
                row.setProductDetail(URLDecoder.decode(jobj.getString("productdetail"), StaticValues.ENCODING));
                totalamount += (row.getRate() * row.getQuantity());
                Discount discount = null;
                double disc = jobj.getDouble("prdiscount");
                if (disc != 0.0) {
//                discount=new Discount();
//                discount.setDiscount(disc);
//                discount.setOriginalAmount(row.getRate()*row.getQuantity());
//                discount.setInPercent(true);
//                discount.setCompany(company);
//                row.setDiscount(discount);
//                session.save(discount);
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", disc);
                    discjson.put("inpercent", true);
                    discjson.put("originalamount", row.getRate() * row.getQuantity());
                    discjson.put("companyid", company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                    totaldiscount += discount.getDiscountValue();
                }
                String rowtaxid = jobj.getString("prtaxid");
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    boolean taxExist=false;
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(),rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null)
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    else{
                        row.setTax(rowtax);
                        double rowtaxamount=StringUtil.getDouble(jobj.getString("taxamount"));
                       // KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, rowtaxamount, currencyid, df.parse(request.getParameter("billdate")), externalCurrencyRate);
                       // rowtaxamount= (Double) bAmt.getEntityList().get(0);
                        taxamount += rowtaxamount;
                        if (taxamount > 0) {
                            Iterator itr = jeDetails.iterator();
                            while (itr.hasNext()) {
                                jed = (JournalEntryDetail) itr.next();
                                if (jed.getAccount() == rowtax.getAccount()) {
//                                          jed.setAmount(jed.getAmount() + row.getRate() * inventory.getQuantity());
                                    JSONObject jedjson = new JSONObject();
                                    jedjson.put("jedid", jed.getID());
                                    jedjson.put("amount",jed.getAmount() +rowtaxamount );
                                    KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    taxExist=true;
                                    break;
                                }
                            }
                            if(!taxExist){
                                JSONObject jedjson = new JSONObject();
                                jedjson = new JSONObject();
                                jedjson.put("srno", jeDetails.size()+1);
                                jedjson.put("companyid", company.getCompanyID());
                                jedjson.put("amount", rowtaxamount);
                                jedjson.put("accountid", rowtax.getAccount().getID());
                                jedjson.put("debit", false);
                                jedjson.put("jeid", jeid);
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);

                            }
                        }
                    }
                }
                rows.add(row);
            }

//        invoice.setRows(rows);
            list.add(new double[]{totaldiscount, totalamount,taxamount});
            list.add(rows);
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
//        return new double[]{totaldiscount,totalamount};
        return list;
    }



    public HashMap applyBillingInvoiceAmount(BillingInvoice invoice) throws ServiceException {
        HashMap hm = new HashMap();
        Set invRows = invoice.getRows();
        Iterator itr = invRows.iterator();
        double amount;
        double quantity;
        double disc = (invoice.getDiscount() == null ? 0 : invoice.getDiscount().getDiscountValue()) / invRows.size();
        while (itr.hasNext()) {
            BillingInvoiceDetail temp = (BillingInvoiceDetail) itr.next();
            quantity = temp.getQuantity();
            amount = temp.getRate() * quantity;
            double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
            hm.put(temp, new Object[]{amount - rdisc - disc, quantity});
            if (invoice == null) {
                invoice = temp.getBillingInvoice();
            }
        }
        return hm;
    }

    public ModelAndView saveRepeateInvoiceInfo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try{
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
//            sdf.setTimeZone(TimeZone.getTimeZone("GMT"+sessionHandlerImpl.getTimeZoneDifference(request)));

            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            int intervalUnit = Integer.parseInt(request.getParameter("interval"));
            dataMap.put("intervalUnit", intervalUnit);
            dataMap.put("intervalType", request.getParameter("intervalType"));
            Date startDate = df.parse(request.getParameter("startDate"));

            String repeateId = request.getParameter("repeateid");
            if(StringUtil.isNullOrEmpty(repeateId)) {
                dataMap.put("startDate", startDate);
                dataMap.put("nextDate", startDate);
            } else {
                dataMap.put("id", repeateId);
                Date nextDate = startDate;//RepeatedInvoices.calculateNextDate(startDate, intervalUnit, request.getParameter("intervalType"));
                dataMap.put("nextDate", nextDate);
            }
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("expireDate"))) {
                dataMap.put("expireDate", df.parse(request.getParameter("expireDate")));
            }
            KwlReturnObject rObj = accInvoiceDAOobj.saveRepeateInvoiceInfo(dataMap);
            RepeatedInvoices rinvoice = (RepeatedInvoices) rObj.getEntityList().get(0);

            JSONObject invjson = new JSONObject();
            invjson.put("invoiceid", request.getParameter("invoiceid"));
            invjson.put("repeateid", rinvoice.getId());
            boolean isCustBill = Boolean.parseBoolean(request.getParameter("isCustBill"));
            if(isCustBill){
                accInvoiceDAOobj.updateBillingInvoice(invjson, null);
            } else {
                accInvoiceDAOobj.updateInvoice(invjson, null);
            }
            msg = messageSource.getMessage("acc.inv.recSave", null, RequestContextUtils.getLocale(request));   //"Recurring Invoice has been saved successfully";
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex){
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getInvoiceRepeateDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String parentInvoiceId = request.getParameter("parentid");
            HashMap<String, Object>requestParams = new HashMap<String, Object>();
            requestParams.put("parentInvoiceId", parentInvoiceId);
            KwlReturnObject details = accInvoiceDAOobj.getRepeateInvoicesDetails(requestParams);
            List detailsList = details.getEntityList();
            Iterator itr = detailsList.iterator();

            JSONArray JArr = new JSONArray();
            while(itr.hasNext()){
                Invoice repeatedInvoice = (Invoice) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("invoiceId", repeatedInvoice.getID());
                obj.put("invoiceNo", repeatedInvoice.getInvoiceNumber());
                obj.put("parentInvoiceId", parentInvoiceId);
                JArr.put(obj);
            }

            jobj.put("data", JArr);
            jobj.put("count", details.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getBillingInvoiceRepeateDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String parentInvoiceId = request.getParameter("parentid");
            HashMap<String, Object>requestParams = new HashMap<String, Object>();
            requestParams.put("parentInvoiceId", parentInvoiceId);
            KwlReturnObject details = accInvoiceDAOobj.getRepeateBillingInvoicesDetails(requestParams);
            List detailsList = details.getEntityList();
            Iterator itr = detailsList.iterator();

            JSONArray JArr = new JSONArray();
            while(itr.hasNext()){
                BillingInvoice repeatedInvoice = (BillingInvoice) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("invoiceId", repeatedInvoice.getID());
                obj.put("invoiceNo", repeatedInvoice.getBillingInvoiceNumber());
                obj.put("parentInvoiceId", parentInvoiceId);
                JArr.put(obj);
            }

            jobj.put("data", JArr);
            jobj.put("count", details.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
}
