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
package com.krawler.spring.accounting.goodsreceipt;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BillingGoodsReceipt;
import com.krawler.hql.accounting.BillingGoodsReceiptDetail;
import com.krawler.hql.accounting.BillingPurchaseOrderDetail;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.ExchangeRateDetails;
import com.krawler.hql.accounting.ExpenseGRDetail;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.GoodsReceiptDetail;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.PurchaseOrderDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.common.KwlReturnObject;
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
public class accGoodsReceiptController extends MultiActionController implements GoodsReceiptConstants, MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}


    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
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

    public ModelAndView saveGoodsReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try{
            List li = saveGoodsReceipt(request);
            String companyid = sessionHandlerImpl.getCompanyid(request); 
            
            boolean inCash = Boolean.parseBoolean(request.getParameter(INCASH));
            String[] id = (String[]) li.get(0);
            ArrayList discountArr=(ArrayList) li.get(1);
            jobj.put(INVOICEID, id[0]);
            msg = (inCash?messageSource.getMessage("acc.rem.120", null, RequestContextUtils.getLocale(request)):messageSource.getMessage("acc.rem.119", null, RequestContextUtils.getLocale(request)));
            txnManager.commit(status);
             status = txnManager.getTransaction(def);
            deleteEditedGoodsReceiptJE(id[1],companyid);
             txnManager.commit(status);
             status = txnManager.getTransaction(def);
            deleteEditedGoodsReceiptDiscount(discountArr,companyid);
             txnManager.commit(status);
             issuccess = true;
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveGoodsReceipt(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        String id = null;
        List ll = new ArrayList();
        String jeentryNumber= null;
        GoodsReceipt gr = null;
        ArrayList discountArr=new ArrayList();
        String oldjeid=null;
        try {
            KwlReturnObject result=null;
            int nocount;
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            DateFormat df = authHandler.getDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String grid =request.getParameter(INVOICEID);
            String jeid=null;
            boolean jeautogenflag = false;
//**For editing Invoice[PS]
            if (!StringUtil.isNullOrEmpty(grid)){
                KwlReturnObject invObj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grid);
                gr=(GoodsReceipt)invObj.getEntityList().get(0);
                oldjeid=gr.getJournalEntry().getID();
                jeautogenflag = gr.getJournalEntry().isAutoGenerated();
                             
//**Gettinging invoice inventory[PS]
                result = accGoodsReceiptobj.getGRInventory(grid);
//**Deleting invoice row[PS]
                accGoodsReceiptobj.deleteGoodsReceiptDetails(grid,companyid);
                List<String> list =  result.getEntityList();
                  if(list!=null && ! list.isEmpty()){
                       for(String inventoryid:list){
                        accProductObj.deleteInventory(inventoryid,companyid);
                    }
                 }
//**Deleting all Invoice Detail discounts[PS]
                result = accGoodsReceiptobj.getGRDetailsDiscount(grid);
                list =  result.getEntityList();
                if(list!=null && ! list.isEmpty()){
                    for(String discountid:list){
                        discountArr.add(discountid);
                    }
                }
//**Deleting Invoice discount[PS]
                String discountid=(gr.getDiscount()==null?null:gr.getDiscount().getID());
                gr.setDiscount(null);
                if (StringUtil.isNullOrEmpty(discountid))
                    discountArr.add(discountid);               
//**Setting other required values NULL[PS]
                String nl=null;
                Map<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put( GRID,grid);
                dataMap.put( "otherentryid",nl);
                dataMap.put( SHIPENTRYID,nl);
                dataMap.put( TAXID,nl);
                dataMap.put( TAXENTRYID,nl);
                dataMap.put( CUSTOMERENTRYID,nl);
                KwlReturnObject uresult = accGoodsReceiptobj.addGoodsReceipt(dataMap);
                gr = (GoodsReceipt) uresult.getEntityList().get(0);
                jeentryNumber=gr.getJournalEntry().getEntryNumber();
            }
            String costCenterId = request.getParameter(CCConstants.REQ_costcenter);
            String taxid = request.getParameter(TAXID);
            double taxamount = StringUtil.getDouble(request.getParameter(TAXAMOUNT));
            double externalCurrencyRate=StringUtil.getDouble(request.getParameter(EXTERNALCURRENCYRATE));
            Discount discount = null;
            double discValue = 0.0;
//            double shippingCharges = StringUtil.getDouble(request.getParameter("shipping"));   //Removed from all the ransactions [PS]
//            double otherCharges = StringUtil.getDouble(request.getParameter("othercharges"));
            boolean inCash = Boolean.parseBoolean(request.getParameter(INCASH));

            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmp.getEntityList().get(0);

            String currencyid= sessionHandlerImpl.getCurrencyID(request);
            //            KWLCurrency currency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter(CURRENCYID) == null ? currency.getCurrencyID() : request.getParameter(CURRENCYID));

            String entryNumber = request.getParameter(NUMBER);
            String vendorid = request.getParameter(VENDOR);
            Map<String, Object> greceipthm = new HashMap<String, Object>();
            greceipthm.put( GRID,grid);
            greceipthm.put( VENDORID,vendorid);
            greceipthm.put( ENTRYNUMBER,entryNumber);
            if (StringUtil.isNullOrEmpty(grid)){
                result = accGoodsReceiptobj.getReceiptFromNo(entryNumber, companyid);
                nocount = result.getRecordTotalCount();
                if (nocount > 0) {
                    if (inCash) {
                        throw new AccountingException("Purchase receipt number '" + entryNumber + "' already exists.");
                    } else {
                        throw new AccountingException("Vendor Invoice number '" + entryNumber + "' already exists.");
                    }
                }
                int from = StaticValues.AUTONUM_GOODSRECEIPT;
                if (inCash) {
                    from = StaticValues.AUTONUM_CASHPURCHASE;
                }
                String nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, from);
                greceipthm.put( ENTRYNUMBER,entryNumber);
                greceipthm.put(AUTOGENERATED, entryNumber.equals(nextAutoNo));
            }
            greceipthm.put(MEMO, request.getParameter(MEMO));
            greceipthm.put(BILLTO, request.getParameter(BILLTO));
            greceipthm.put(SHIPADDRESS, request.getParameter(SHIPADDRESS));
            greceipthm.put(SHIPDATE, df.parse(request.getParameter(SHIPDATE)));
            greceipthm.put(DUEDATE, df.parse(request.getParameter(DUEDATE)));
            greceipthm.put( CURRENCYID,currencyid);
            greceipthm.put( COMPANYID,companyid);
            greceipthm.put( "externalCurrencyRate",externalCurrencyRate);

//** Create Journal Entry[PS]
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;
                jeautogenflag = true;
            }

            Map<String,Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put( ENTRYNUMBER,jeentryNumber);
            jeDataMap.put( AUTOGENERATED,jeautogenflag);
            jeDataMap.put(ENTRYDATE, df.parse(request.getParameter(BILLDATE)));
            jeDataMap.put( COMPANYID,companyid);
            jeDataMap.put(MEMO, request.getParameter(MEMO));
            jeDataMap.put( CURRENCYID,currencyid);
            jeDataMap.put(CCConstants.JSON_costcenterid, costCenterId);
            Set<JournalEntryDetail> jedetails = new HashSet<JournalEntryDetail>();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();            
            greceipthm.put( "journalentryid",jeid);
            jeDataMap.put( JEID,jeid);
            Set<GoodsReceiptDetail> grdetails=null;
            Set<ExpenseGRDetail> expensegrdetails=null;
            double[] totals=null;
//**Saving product grid[PS]
            if(!StringUtil.isNullOrEmpty(request.getParameter(DETAIL))){
                List li = saveGoodsReceiptRows(request, jeid, company, jedetails, currency,externalCurrencyRate);
                totals = (double[]) li.get(0);
                grdetails = (HashSet<GoodsReceiptDetail>) li.get(1);
            }
            
//**Saving account grid[PS]
            if(!StringUtil.isNullOrEmpty(request.getParameter(EXPENSEDETAIL))){
                List li = saveExpenseGRRows(request, jeid, company, jedetails, currency,externalCurrencyRate);
                totals = (double[]) li.get(0);
                expensegrdetails = (HashSet<ExpenseGRDetail>) li.get(1);
                greceipthm.put(ISEXPENSETYPE, true);
            }

            double disc = StringUtil.getDouble(request.getParameter(DISCOUNT));
            if (disc > 0) {
                JSONObject discjson = new JSONObject();
                discjson.put( DISCOUNT,disc);
                discjson.put(INPERCENT, Boolean.parseBoolean(request.getParameter(PERDISCOUNT)));
                discjson.put(ORIGINALAMOUNT, totals[1] - totals[0]+totals[2]);
                discjson.put( COMPANYID,companyid);
                KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                discount = (Discount) dscresult.getEntityList().get(0);
                greceipthm.put(DISCOUNTID, discount.getID());
                discValue = discount.getDiscountValue();
            }
            discValue += totals[0];
            JSONObject jedjson = new JSONObject();
            jedjson.put(SRNO, jedetails.size()+1);
            jedjson.put( COMPANYID,companyid);
            jedjson.put(AMOUNT, totals[1] - discValue + taxamount+ totals[2]);   //+  shippingCharges +otherCharges
            if (!inCash) {
                jedjson.put(ACCOUNTID, request.getParameter(VENDOR));
            } else {
                jedjson.put(ACCOUNTID, preferences.getCashAccount().getID());
            }
            jedjson.put(DEBIT, false);
            jedjson.put( JEID,jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
//            gReceipt.setVendorEntry(jed);
            greceipthm.put(VENDORENTRYID, jed.getID());

            if (discValue > 0) {
                jedjson = new JSONObject();
                jedjson.put(SRNO, jedetails.size()+1);
                jedjson.put( COMPANYID,companyid);
                jedjson.put( AMOUNT,discValue);
                jedjson.put(ACCOUNTID, preferences.getDiscountReceived().getID());
                jedjson.put(DEBIT, false);
                jedjson.put( JEID,jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }
//            if (shippingCharges > 0) {
//                jedjson = new JSONObject();
//                jedjson.put("srno", jedetails.size()+1);
//                jedjson.put("companyid", companyid);
//                jedjson.put("amount", shippingCharges);
//                jedjson.put("accountid", preferences.getShippingCharges().getID());
//                jedjson.put("debit", true);
//                jedjson.put("jeid", jeid);
//                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                jedetails.add(jed);
//                greceipthm.put("shipentryid", jed.getID());
//            }
//            if (otherCharges > 0) {
//                jedjson = new JSONObject();
//                jedjson.put("srno", jedetails.size()+1);
//                jedjson.put("companyid", companyid);
//                jedjson.put("amount", otherCharges);
//                jedjson.put("accountid", preferences.getOtherCharges().getID());
//                jedjson.put("debit", true);
//                jedjson.put("jeid", jeid);
//                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                jedetails.add(jed);
//                greceipthm.put("otherentryid", jed.getID());
//
//            }
            if (!StringUtil.isNullOrEmpty(taxid)) {
                result = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxid);
                Tax tax = (Tax) result.getEntityList().get(0);
                if (tax == null && !taxid.isEmpty()) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
                greceipthm.put(TAXID, tax.getID());
                if (taxamount > 0) {
                    jedjson = new JSONObject();
                    jedjson.put(SRNO, jedetails.size()+1);
                    jedjson.put( COMPANYID,companyid);
                    jedjson.put( AMOUNT,taxamount);
                    jedjson.put(ACCOUNTID, tax.getAccount().getID());
                    jedjson.put(DEBIT, true);
                    jedjson.put( JEID,jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                    greceipthm.put(TAXENTRYID, jed.getID());
                }
            }
            jeDataMap.put( JEDETAILS,jedetails);
            jeDataMap.put( "externalCurrencyRate",externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String erdid = null;
            Date billDate=request.getParameter(BILLDATE)==null?null:df.parse(request.getParameter(BILLDATE));
            KwlReturnObject ERresult = accCurrencyDAOobj.getExcDetailID(requestParams, currencyid, billDate, null);
            List ERlist = ERresult.getEntityList();
            if(!ERlist.isEmpty()) {
                Iterator itr = ERlist.iterator();
                ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                erdid = erd.getID();
            }
            greceipthm.put( ERDID,erdid);

            result = accGoodsReceiptobj.addGoodsReceipt(greceipthm);
            gr = (GoodsReceipt) result.getEntityList().get(0);
            greceipthm.put(GRID, gr.getID());
//**For product grid
            if(!StringUtil.isNullOrEmpty(request.getParameter(DETAIL))){
//                Iterator itr = grdetails.iterator();
//                while(itr.hasNext()){
//                    GoodsReceiptDetail grd = (GoodsReceiptDetail) itr.next();
                if(grdetails!=null && ! grdetails.isEmpty()){
                    for(GoodsReceiptDetail grd :grdetails){
                        grd.setGoodsReceipt(gr);
                    }
                }
                greceipthm.put( GRDETAILS,grdetails);
            }
//**For account grid
            if(!StringUtil.isNullOrEmpty(request.getParameter(EXPENSEDETAIL))){
//                Iterator itr = expensegrdetails.iterator();
//                while(itr.hasNext()){
//                    ExpenseGRDetail grd = (ExpenseGRDetail) itr.next();
                if(expensegrdetails!=null && ! expensegrdetails.isEmpty()){
                    for(ExpenseGRDetail grd :expensegrdetails){
                        grd.setGoodsReceipt(gr);
                    }
                }
                greceipthm.put( EXPENSEGRDETAILS,expensegrdetails);
            }
            result = accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
            gr = (GoodsReceipt) result.getEntityList().get(0);
            id = gr.getID();
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveGoodsReceipt : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveGoodsReceipt : "+ex.getMessage(), ex);
        }
        ll.add(new String[]{id, oldjeid});
        ll.add(discountArr);
        return  ll;
    }

    public void deleteEditedGoodsReceiptJE(String oldjeid,String companyid) throws ServiceException, AccountingException, SessionExpiredException {
      try{      //delete old invoice
//          JournalEntryDetail jed=null;
            if (!StringUtil.isNullOrEmpty(oldjeid)) {
                 KwlReturnObject   result = accJournalEntryobj.getJournalEntryDetail(oldjeid, companyid);
                List<JournalEntryDetail> list =  result.getEntityList();
//                Iterator itr = list.iterator();
//                while (itr.hasNext()) {
//                    jed = (JournalEntryDetail) itr.next();
               if(list!=null && ! list.isEmpty()){
                    for(JournalEntryDetail jed :list){
                        result = accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
                    }
               }
               result = accJournalEntryobj.permanentDeleteJournalEntry(oldjeid, companyid);

            }
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public void deleteEditedGoodsReceiptDiscount(ArrayList discArr,String companyid) throws ServiceException, AccountingException, SessionExpiredException {
      try{
           for (int i=0;i<discArr.size();i++)
               if(discArr.get(i)!=null)
                    accDiscountobj.deleteDiscount(discArr.get(i).toString(),companyid);
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public List saveGoodsReceiptRows(HttpServletRequest request, String jeid, Company company, Set<JournalEntryDetail> jeDetails, KWLCurrency currency,double externalCurrencyRate) throws ServiceException, SessionExpiredException, AccountingException {
        Set hs = new HashSet(), rows = new HashSet();
        double totaldiscount = 0, totalamount = 0,taxamount=0;
        List ll = new ArrayList();
        try{
            String currencyid=(request.getParameter(CURRENCYID)==null?currency.getCurrencyID():request.getParameter(CURRENCYID));
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(DATEFORMAT, authHandler.getDateFormatter(request));
            JSONArray jArr = new JSONArray(request.getParameter(DETAIL));
            for (int i = 0; i < jArr.length(); i++) {
//                JournalEntryDetail jed;
                JSONObject jobj = jArr.getJSONObject(i);
                GoodsReceiptDetail row = new GoodsReceiptDetail();
                row.setSrno(i+1);
//                PurchaseOrderDetail rd = (PurchaseOrderDetail) session.get(PurchaseOrderDetail.class, jobj.getString("rowid"));
                KwlReturnObject podresult = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), jobj.getString("rowid"));
                PurchaseOrderDetail rd = (PurchaseOrderDetail) podresult.getEntityList().get(0);
                row.setCompany(company);
                row.setPurchaseorderdetail(rd);
    //            row.setGoodsReceipt(gReceipt);
    //            row.setRate(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("rate"),currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate"))));
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, jobj.getDouble(RATE), currencyid, authHandler.getDateFormatter(request).parse(request.getParameter(BILLDATE)),externalCurrencyRate);
                row.setRate((Double) bAmt.getEntityList().get(0));
    //            Product product = (Product) session.get(Product.class, jobj.getString("productid"));
                KwlReturnObject prdresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString(PRODUCTID));
                Product product = (Product) prdresult.getEntityList().get(0);
    //            Inventory inventory = CompanyHandler.makeInventory(session, request, product, jobj.getInt("quantity"), jobj.optString("desc"), true, false);
                JSONObject inventoryjson = new JSONObject();
                inventoryjson.put(PRODUCTID, jobj.getString(PRODUCTID));
                inventoryjson.put(QUANTITY, jobj.getInt(QUANTITY));
                inventoryjson.put(DESCRIPTION, jobj.getString("desc"));
//                URLDecoder.decode(jobj.getString("desc"));
                inventoryjson.put(CARRYIN, true);
                inventoryjson.put(DEFECTIVE, false);
                inventoryjson.put(NEWINVENTORY, false);
                inventoryjson.put(COMPANYID, company.getCompanyID());
                inventoryjson.put(UPDATEDATE, AuthHandler.getDateFormatter(request).parse(request.getParameter(BILLDATE)));
                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                Inventory inventory = (Inventory) invresult.getEntityList().get(0);

                row.setInventory(inventory);
                totalamount += row.getRate() * inventory.getQuantity();
                Discount discount = null;
                double disc = jobj.getDouble(PRDISCOUNT);
                if (disc != 0.0) {
    //                discount = new Discount();
    //                discount.setDiscount(disc);
    //                discount.setOriginalAmount(row.getRate() * row.getInventory().getQuantity());
    //                discount.setInPercent(true);
    //                discount.setCompany(company);
    //                row.setDiscount(discount);
    //                session.save(discount);
                    JSONObject discjson = new JSONObject();
                    discjson.put( DISCOUNT,disc);
                    discjson.put(INPERCENT, true);
                    discjson.put(ORIGINALAMOUNT, row.getRate() * jobj.getInt(QUANTITY));
                    discjson.put(COMPANYID, company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                    totaldiscount += discount.getDiscountValue();
                }
                if (hs.add(product.getPurchaseAccount())) {
    //                jed = new JournalEntryDetail();
    //                jed.setCompany(company);
    //                jed.setAccount(product.getPurchaseAccount());
    //                jed.setAmount(row.getRate() * inventory.getQuantity());
    //                jed.setDebit(true);
                    JSONObject jedjson = new JSONObject();
                    jedjson.put(SRNO, jeDetails.size()+1);
                    jedjson.put(COMPANYID, company.getCompanyID());
                    jedjson.put(AMOUNT, row.getRate() * jobj.getInt(QUANTITY));
                    jedjson.put(ACCOUNTID, product.getPurchaseAccount().getID());
                    jedjson.put(DEBIT, true);
                    jedjson.put( JEID,jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);
                } else {
//                    Iterator itr = jeDetails.iterator();
//                    while (itr.hasNext()) {
//                        jed = (JournalEntryDetail) itr.next();
                    if(jeDetails!=null && ! jeDetails.isEmpty()){
                        for(JournalEntryDetail jed :jeDetails){
                            if (jed.getAccount() == product.getPurchaseAccount()) {
    //                            jed.setAmount(jed.getAmount() + row.getRate() * inventory.getQuantity());
                                JSONObject jedjson = new JSONObject();
                                jedjson.put(JEDID, jed.getID());
                                jedjson.put(AMOUNT, jed.getAmount() + row.getRate() * inventory.getQuantity());
                                KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                break;
                            }
                        }
                    }
                }
                String rowtaxid = jobj.getString(PRTAXID);
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    boolean taxExist=false;
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(),rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null)
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    else{
                        row.setTax(rowtax);
                        double rowtaxamount=StringUtil.getDouble(jobj.getString(TAXAMOUNT));
                        bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, rowtaxamount, currencyid, authHandler.getDateFormatter(request).parse(request.getParameter(BILLDATE)), externalCurrencyRate);
                        rowtaxamount= (Double) bAmt.getEntityList().get(0);
                        taxamount += rowtaxamount;
                        if (taxamount > 0) {
//                            Iterator itr = jeDetails.iterator();
//                            while (itr.hasNext()) {
//                                JournalEntryDetail jed = (JournalEntryDetail) itr.next();
                            if(jeDetails!=null && ! jeDetails.isEmpty()){
                                for(JournalEntryDetail jed :jeDetails){
                                    if (jed.getAccount() == rowtax.getAccount()) {
    //                                          jed.setAmount(jed.getAmount() + row.getRate() * inventory.getQuantity());
                                        JSONObject jedjson = new JSONObject();
                                        jedjson.put(JEDID, jed.getID());
                                        jedjson.put(AMOUNT,jed.getAmount() +rowtaxamount );
                                        KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                        taxExist=true;
                                        break;
                                    }
                                }
                            }
                            if(!taxExist){
                                JSONObject jedjson = new JSONObject();
                                jedjson = new JSONObject();
                                jedjson.put(SRNO, jeDetails.size()+1);
                                jedjson.put(COMPANYID, company.getCompanyID());
                                jedjson.put( AMOUNT,rowtaxamount);
                                jedjson.put(ACCOUNTID, rowtax.getAccount().getID());
                                jedjson.put(DEBIT, true);
                                jedjson.put( JEID,jeid);
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);

                            }
                        }
                    }
                }
                rows.add(row);
            }
    //        gReceipt.setRows(rows);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveGoodsReceiptRows : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveGoodsReceiptRows : "+ex.getMessage(), ex);
        } finally {
            ll.add(new double[]{totaldiscount, totalamount,taxamount});
            ll.add(rows);
        }
        return ll;
    }
    public List saveExpenseGRRows(HttpServletRequest request, String jeid, Company company, Set<JournalEntryDetail> jeDetails, KWLCurrency currency,double externalCurrencyRate) throws ServiceException, SessionExpiredException, AccountingException, JSONException {
        Set<ExpenseGRDetail> rows = new HashSet<ExpenseGRDetail>();
        double totaldiscount = 0, totalamount = 0,taxamount=0;
        List ll = new ArrayList();
        JSONArray jArr = new JSONArray(request.getParameter(EXPENSEDETAIL));
        try {

            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(DATEFORMAT, authHandler.getDateFormatter(request));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                ExpenseGRDetail row = new ExpenseGRDetail();
                row.setSrno(i+1);
                row.setCompany(company);
                row.setRate(jobj.getDouble(RATE));
                row.setAmount(jobj.getInt(CALAMOUNT));
                KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(),jobj.getString(ACCOUNTID)); // (Tax)session.get(Tax.class, taxid);
                Account account = (Account) accresult.getEntityList().get(0);
                row.setAccount(account);
                totalamount +=jobj.getDouble(RATE);
                Discount discount = null;
                double disc = jobj.getDouble(PRDISCOUNT);
                if (disc != 0.0) {
                    Map<String, Object> discMap = new HashMap();
                    discMap.put( DISCOUNT,disc);
                    discMap.put(INPERCENT, true);
                    discMap.put(ORIGINALAMOUNT, jobj.getDouble(RATE));
                    discMap.put(COMPANYID, company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.updateDiscount(discMap);
                    discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                    totaldiscount += discount.getDiscountValue();
                }
                String rowtaxid = jobj.getString(PRTAXID);
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    boolean taxExist=false;
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(),rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null)
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    else{
                        row.setTax(rowtax);
                        double rowtaxamount=StringUtil.getDouble(jobj.getString(TAXAMOUNT));
                        taxamount += rowtaxamount;
                        if (taxamount > 0) {
//                            Iterator itr = jeDetails.iterator();
//                            while (itr.hasNext()) {
//                                jed = (JournalEntryDetail) itr.next();
                           if(jeDetails!=null && ! jeDetails.isEmpty()){
                                for(JournalEntryDetail jed :jeDetails){
                                    if (jed.getAccount() == rowtax.getAccount()) {
    //                                          jed.setAmount(jed.getAmount() + row.getRate() * inventory.getQuantity());
                                        JSONObject jedjson = new JSONObject();
                                        jedjson.put(JEDID, jed.getID());
                                        jedjson.put(AMOUNT,jed.getAmount() +rowtaxamount );
                                        KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                        taxExist=true;
                                        break;
                                    }
                                }
                            }
                            if(!taxExist){
                                JSONObject jedjson = new JSONObject();
                                jedjson = new JSONObject();
                                jedjson.put(SRNO, jeDetails.size()+1);
                                jedjson.put(COMPANYID, company.getCompanyID());
                                jedjson.put( AMOUNT,rowtaxamount);
                                jedjson.put(ACCOUNTID, rowtax.getAccount().getID());
                                jedjson.put(DEBIT, true);
                                jedjson.put( JEID,jeid);
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);

                            }
                        }
                    }
                }
                JSONObject jedjson = new JSONObject();
                jedjson.put(SRNO, jeDetails.size()+1);
                jedjson.put(COMPANYID, company.getCompanyID());
                jedjson.put(AMOUNT, jobj.getInt(RATE));
                jedjson.put(ACCOUNTID, account.getID());
                jedjson.put(DEBIT, true);
                jedjson.put( JEID,jeid);
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);
                rows.add(row);
            }
        } catch (JSONException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ll.add(new double[]{totaldiscount, totalamount, taxamount});
            ll.add(rows);
        }
        return ll;
    }

    public ModelAndView saveBillingGoodsReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try{
            List li = saveBillingGoodsReceipt(request);
            issuccess = true;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean inCash = Boolean.parseBoolean(request.getParameter(INCASH));
            String[] id = (String[]) li.get(0);
            ArrayList discountArr=(ArrayList) li.get(1);
            jobj.put(INVOICEID, id[0]);
            msg = (inCash?"Purchase Receipt":"Vendor Invoice")+" has been saved successfully";
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteEditedGoodsReceiptJE(id[1],companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteEditedGoodsReceiptDiscount(discountArr,companyid);
             txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public List saveBillingGoodsReceipt(HttpServletRequest request) throws AccountingException, ServiceException, SessionExpiredException{
        BillingGoodsReceipt billingGoodsReceipt = null;
        String id =null;
        List ll = new ArrayList();
        String jeentryNumber= null;
        ArrayList discountArr=new ArrayList();
        String oldjeid=null;
		try {
            KwlReturnObject result=null;
            String jeid=null;
            boolean jeautogenflag = false;
            int nocount;
//            Discount discount=null;
            double externalCurrencyRate=StringUtil.getDouble(request.getParameter(EXTERNALCURRENCYRATE));
            Map<String, Object> requestMap = new HashMap();
            DateFormat df = authHandler.getDateFormatter(request);
            double discValue=0.0;
            String taxid=null;
            taxid=request.getParameter(TAXID);

            String costCenterId = request.getParameter(CCConstants.REQ_costcenter);
            double taxamount=StringUtil.getDouble(request.getParameter(TAXAMOUNT));
            double shippingCharges=StringUtil.getDouble(request.getParameter("shipping"));
            double otherCharges=StringUtil.getDouble(request.getParameter("othercharges"));
            boolean inCash=Boolean.parseBoolean(request.getParameter(INCASH));
            String grid =request.getParameter(INVOICEID);
            String companyid = sessionHandlerImpl.getCompanyid(request);
              if (!StringUtil.isNullOrEmpty(grid)){
                KwlReturnObject grObj = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), grid);
                billingGoodsReceipt=(BillingGoodsReceipt)grObj.getEntityList().get(0);
                oldjeid=billingGoodsReceipt.getJournalEntry().getID();
                jeautogenflag = billingGoodsReceipt.getJournalEntry().isAutoGenerated();
                  ////deleting invoice row
                result = accGoodsReceiptobj.deleteBillingGoodsReceiptDetails(grid,companyid);

                ////Deleting all Invoice Detail discount
              ////Deleting all Invoice Detail discount
                result = accGoodsReceiptobj.getGRDetailsDiscount(grid);
                List<String> list =  result.getEntityList();
//                Iterator itr = list.iterator();
//                while (itr.hasNext()) {
//                    String discountid = (String) itr.next();
               if(list!=null && ! list.isEmpty()){
                  for(String discountid:list){
                        discountArr.add(discountid);
               //     accDiscountobj.deleteDiscount(discountid,companyid);
                    }
               }
                String discountid=(billingGoodsReceipt.getDiscount()==null?null:billingGoodsReceipt.getDiscount().getID());
                billingGoodsReceipt.setDiscount(null);
                if (StringUtil.isNullOrEmpty(discountid))
                    discountArr.add(discountid);


                ////Deleting Invoice Detail Journalentry Detail
                String nl=null;
                Map<String, Object> dataMap = new HashMap<String, Object>();

                dataMap.put( ID,grid);
                dataMap.put( "otherentryid",nl);
                dataMap.put( SHIPENTRYID,nl);
                dataMap.put( TAXID,nl);
                dataMap.put( TAXENTRYID,nl);
                dataMap.put( CUSTOMERENTRYID,nl);
                KwlReturnObject uresult = accGoodsReceiptobj.saveBillingGoodsReceipt(dataMap);
                billingGoodsReceipt = (BillingGoodsReceipt) uresult.getEntityList().get(0);
                jeentryNumber=billingGoodsReceipt.getJournalEntry().getEntryNumber();
            }


            KwlReturnObject coPref = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) coPref.getEntityList().get(0);

            KwlReturnObject coCurr = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) coCurr.getEntityList().get(0);

            KwlReturnObject CompObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) CompObj.getEntityList().get(0);

            KwlReturnObject vendorObj = accountingHandlerDAOobj.getObject(Vendor.class.getName(), request.getParameter(VENDOR));
            Vendor vendor = (Vendor) vendorObj.getEntityList().get(0);

//            CompanyAccountPreferences preferences=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
//            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));


            String currencyid=(request.getParameter(CURRENCYID)==null?currency.getCurrencyID():request.getParameter(CURRENCYID));
            //BillingGoodsReceipt bgr=new BillingGoodsReceipt();
            
            String entryNumber=request.getParameter(NUMBER);
            if (StringUtil.isNullOrEmpty(grid)){
                Map<String, Object> bgrMap = new HashMap();
                bgrMap.put("billingGoodsReceiptNumber", entryNumber);
                bgrMap.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
                KwlReturnObject accGrRc = accGoodsReceiptobj.getBillingGoodsReceipt(bgrMap);
                List bgrList = accGrRc.getEntityList();
                if(!bgrList.isEmpty()){
                    throw new AccountingException("Vendor Invoice number '"+entryNumber+"' already exists.<br>Save again with given auto number.<br>Or type other number");
                }

//            String q="from BillingGoodsReceipt where billingGoodsReceiptNumber=? and company.companyID=?";
//            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                throw new AccountingException("Vendor Invoice number '"+entryNumber+"' already exists.<br>Save again with given auto number.<br>Or type other number");

//            bgr.setBillingGoodsReceiptNumber(entryNumber);
                int from=StaticValues.AUTONUM_BILLINGGOODSRECEIPT;
                if(inCash)from=StaticValues.AUTONUM_BILLINGCASHPURCHASE;
                requestMap.put("billingGoodsReceiptNumber", entryNumber);
                requestMap.put("autoGenerated", accCompanyPreferencesObj.getNextAutoNumber(companyid, from).equals(entryNumber));
            }
            requestMap.put( ID,grid);
            requestMap.put(MEMO, request.getParameter(MEMO));
//            bgr.setMemo(request.getParameter("memo"));
            requestMap.put("billFrom", request.getParameter(BILLTO));
//            bgr.setBillFrom(request.getParameter("billto"));
            requestMap.put(SHIPFROM, request.getParameter(SHIPADDRESS));
//            bgr.setShipFrom(request.getParameter("shipaddress"));
            requestMap.put( CURRENCYID,currencyid);
//            bgr.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
            requestMap.put("shipDate", authHandler.getDateFormatter(request).parse(request.getParameter(SHIPDATE)));
//            bgr.setShipDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("shipdate")));
            requestMap.put("dueDate", authHandler.getDateFormatter(request).parse(request.getParameter(DUEDATE)));
//            bgr.setDueDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("duedate")));
            requestMap.put( COMPANYID,companyid);
//            bgr.setCompany(company);
            requestMap.put(VENDORID, request.getParameter(VENDOR));
//            Vendor vendor=(Vendor)session.get(Vendor.class, request.getParameter("vendor"));
//            bgr.setVendor(vendor);

//            JournalEntry journalEntry=CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),
//            request.getParameter("memo"), "JE"+bgr.getBillingGoodsReceiptNumber(),currencyid, externalCurrencyRate, hs,request);
//            
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            Map<String,Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put( ENTRYNUMBER,jeentryNumber);
            jeDataMap.put( AUTOGENERATED,jeautogenflag);
            jeDataMap.put(ENTRYDATE, df.parse(request.getParameter(BILLDATE)));
            jeDataMap.put( COMPANYID,companyid);
            jeDataMap.put(MEMO, request.getParameter(MEMO));
            jeDataMap.put( "externalCurrencyRate",externalCurrencyRate);
            jeDataMap.put( CURRENCYID,currencyid);
            jeDataMap.put(CCConstants.JSON_costcenterid, costCenterId);
            
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            requestMap.put( "journalEntryid",jeid);
            jeDataMap.put( JEID,jeid);


      //      HashSet jedetails = new HashSet();
            Set<JournalEntryDetail> hs=new HashSet<JournalEntryDetail>();
//            double[] totals=saveBillingGoodsReceiptRows(session, request, bgr, company, hs);
            List li = saveBillingGoodsReceiptRows(request, jeid, company, hs, currency,externalCurrencyRate);
            double[] totals = (double[]) li.get(0);
            Set<BillingGoodsReceiptDetail> bgrdetails = (HashSet<BillingGoodsReceiptDetail>) li.get(1);
            
            double disc = StringUtil.getDouble(request.getParameter(DISCOUNT));
            //double disc=CompanyHandler.getDouble(request, "discount");
            if(disc>0){
                Map<String, Object> discountMap = new HashMap();
                discountMap.put( DISCOUNT,disc);
                discountMap.put(INPERCENT, (request.getParameter(PERDISCOUNT)==null?false:Boolean.parseBoolean(request.getParameter(PERDISCOUNT))));
                discountMap.put(ORIGINALAMOUNT, totals[1]-totals[0]+totals[2]);
                discountMap.put( COMPANYID,companyid);
//                discount=new Discount();
//                discount.setDiscount(disc);
//                discount.setInPercent(Boolean.parseBoolean(request.getParameter("perdiscount")));
//                discount.setOriginalAmount(totals[1]-totals[0]);
//                discount.setCompany(company);
                KwlReturnObject disObjKwl = accDiscountobj.updateDiscount(discountMap);
                Discount disObj = (Discount) disObjKwl.getEntityList().get(0);
                requestMap.put(DISCOUNTID, disObj.getID());
//                bgr.setDiscount(discount);

//                session.save(discount);
                discValue=disObj.getDiscountValue();
            }
            discValue+=totals[0];
            Map<String, Object> jeMap = new HashMap<String, Object>();
            jeMap.put(SRNO, hs.size()+1);
//            JournalEntryDetail jed=new JournalEntryDetail();
            jeMap.put( COMPANYID,companyid);
//            jed.setCompany(company);
            jeMap.put(AMOUNT, totals[1]+shippingCharges+otherCharges-discValue+taxamount +totals[2]);
//            jed.setAmount(totals[1]+shippingCharges+otherCharges-discValue+taxamount);
            if(!inCash){
                jeMap.put(ACCOUNTID, vendor.getAccount().getID());
//                jed.setAccount(vendor.getAccount());
            } else {
                jeMap.put(ACCOUNTID, preferences.getCashAccount().getID());
//                jed.setAccount(preferences.getCashAccount());
            }
            jeMap.put(DEBIT, false);
            jeMap.put( JEID,jeid);
//            jed.setDebit(false);
            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jeMap);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            hs.add(jed);
            requestMap.put("vendorEntryid", jed.getID());


//            bgr.setVendorEntry(jed);
            jeMap = new HashMap<String, Object>();
            jeMap.put(SRNO, hs.size()+1);
//            jed=new JournalEntryDetail();
            jeMap.put( COMPANYID,companyid);
//            jed.setCompany(company);
            jeMap.put(ACCOUNTID, request.getParameter(CREDITORACCOUNT));
//            jed.setAccount((Account)session.get(Account.class, request.getParameter("creditoraccount")));
            jeMap.put(AMOUNT, totals[1]);
//            jed.setAmount(totals[1]);
            jeMap.put(DEBIT, true);
//            jed.setDebit(true);
            jeMap.put( JEID,jeid);
            jedresult = accJournalEntryobj.updateJournalEntryDetails(jeMap);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            hs.add(jed);
            requestMap.put("debtorEntryid", jed.getID());
//            hs.add(jed);
//            bgr.setDebtorEntry(jed);
            if(discValue>0){
                jeMap = new HashMap<String, Object>();
                jeMap.put(SRNO, hs.size()+1);
//                jed=new JournalEntryDetail();
                jeMap.put( COMPANYID,companyid);
//                jed.setCompany(company);
                jeMap.put( AMOUNT,discValue);
//                jed.setAmount(discValue);
                jeMap.put(ACCOUNTID, preferences.getDiscountReceived().getID());
//                jed.setAccount(preferences.getDiscountReceived());
                jeMap.put(DEBIT, false);
//                jed.setDebit(false);
                jeMap.put( JEID,jeid);
                jedresult = accJournalEntryobj.updateJournalEntryDetails(jeMap);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                hs.add(jed);
//                hs.add(jed);
            }
            if(shippingCharges>0){
//                jed=new JournalEntryDetail();
                jeMap = new HashMap<String, Object>();
                jeMap.put(SRNO, hs.size()+1);
                jeMap.put( COMPANYID,companyid);
//                jed.setCompany(company);
                jeMap.put( AMOUNT,shippingCharges);
//                jed.setAmount(shippingCharges);
                jeMap.put(ACCOUNTID, preferences.getShippingCharges().getID());
//                jed.setAccount(preferences.getShippingCharges());
                jeMap.put(DEBIT, true);
//                jed.setDebit(true);
                jeMap.put( JEID,jeid);
                jedresult = accJournalEntryobj.updateJournalEntryDetails(jeMap);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                hs.add(jed);
                requestMap.put("shipEntryid", jed.getID());
//                hs.add(jed);
//                bgr.setShipEntry(jed);
            }
            if(otherCharges>0){
//                jed=new JournalEntryDetail();
                jeMap = new HashMap<String, Object>();
                jeMap.put(SRNO, hs.size()+1);
                jeMap.put( COMPANYID,companyid);
//                jed.setCompany(company);
                jeMap.put( AMOUNT,otherCharges);
//                jed.setAmount(otherCharges);
                jeMap.put(ACCOUNT, preferences.getOtherCharges().getID());
//                jed.setAccount(preferences.getOtherCharges());
                jeMap.put(DEBIT, true);
//                jed.setDebit(true);
                jeMap.put( JEID,jeid);
                jedresult = accJournalEntryobj.updateJournalEntryDetails(jeMap);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                hs.add(jed);
                requestMap.put("otherEntryid", jed.getID());
//                hs.add(jed);
//                bgr.setOtherEntry(jed);
            }
            if(taxid!=null&&!taxid.isEmpty()){
                //Tax tax=  (Tax)session.get(Tax.class, taxid);
                result = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxid);
                Tax tax = (Tax) result.getEntityList().get(0);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
                requestMap.put( TAXID,taxid);

//                    bgr.setTax(tax);
                if(taxamount>0){
                    jeMap = new HashMap<String, Object>();
                    jeMap.put(SRNO, hs.size()+1);
//                    jed=new JournalEntryDetail();
                    jeMap.put( COMPANYID,companyid);
//                    jed.setCompany(company);
                    jeMap.put( AMOUNT,taxamount);
//                    jed.setAmount(taxamount);
                    jeMap.put(ACCOUNTID, tax.getAccount().getID());
//                    jed.setAccount(tax.getAccount());
                    jeMap.put(DEBIT, true);
//                    jed.setDebit(true);
                    jeMap.put( JEID,jeid);
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(jeMap);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    hs.add(jed);
                    requestMap.put("taxEntryid", jed.getID());

//                    hs.add(jed);
//                    bgr.setTaxEntry(jed);
                }
            }

            
            jeDataMap.put( JEDETAILS,hs);
            jeDataMap.put( "externalCurrencyRate",externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            

//            JournalEntry journalEntry=CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),
//            request.getParameter("memo"), "JE"+bgr.getBillingGoodsReceiptNumber(),currencyid, externalCurrencyRate, hs,request);
//            bgr.setJournalEntry(journalEntry);

            String erdid = null;
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            Date billDate=request.getParameter(BILLDATE)==null?null:df.parse(request.getParameter(BILLDATE));

            KwlReturnObject ERresult = accCurrencyDAOobj.getExcDetailID(requestParams, currencyid, billDate, null);
            List ERlist = ERresult.getEntityList();
            if(!ERlist.isEmpty()) {
                Iterator itr = ERlist.iterator();
                ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                erdid = erd.getID();
            }
            requestMap.put("exchangeRateDetailsid", erdid);

//            ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),null);
//            bgr.setExchangeRateDetail(erd);


            result = accGoodsReceiptobj.saveBillingGoodsReceipt(requestMap);
            billingGoodsReceipt = (BillingGoodsReceipt) result.getEntityList().get(0);

            requestMap.put(ID, billingGoodsReceipt.getID());
//            Iterator itr = bgrdetails.iterator();
//            while(itr.hasNext()){
//                BillingGoodsReceiptDetail bgrd = (BillingGoodsReceiptDetail) itr.next();
            if(bgrdetails!=null && ! bgrdetails.isEmpty()){
                for(BillingGoodsReceiptDetail bgrd:bgrdetails){
                    bgrd.setBillingGoodsReceipt(billingGoodsReceipt);
                }
            }
            requestMap.put( ROWS,bgrdetails);

            result = accGoodsReceiptobj.saveBillingGoodsReceipt(requestMap);
            billingGoodsReceipt = (BillingGoodsReceipt) result.getEntityList().get(0);

//            session.saveOrUpdate(bgr);
           id=billingGoodsReceipt.getID();
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }

        ll.add(new String[]{id, oldjeid});
        ll.add(discountArr);
        return  ll;
    }

    public List saveBillingGoodsReceiptRows(HttpServletRequest request, String jeid, Company company, Set<JournalEntryDetail> jeDetails, KWLCurrency currency,double externalCurrencyRate) throws JSONException, ServiceException, AccountingException, SessionExpiredException, ParseException {
//        HashSet rows = new HashSet();
//        double totaldiscount = 0;
//        double totalamount = 0;
        Set rows = new HashSet();
        double totaldiscount = 0, totalamount = 0,taxamount=0;
        List ll = new ArrayList();
        JSONArray jArr = new JSONArray(request.getParameter(DETAIL));
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(DATEFORMAT, authHandler.getDateFormatter(request));
            for (int i = 0; i < jArr.length(); i++) {
//                JournalEntryDetail jed;
                JSONObject jobj = jArr.getJSONObject(i);
                BillingGoodsReceiptDetail row = new BillingGoodsReceiptDetail();
                row.setSrno(i+1);
                //BillingPurchaseOrderDetail rd = (BillingPurchaseOrderDetail) session.get(BillingPurchaseOrderDetail.class, jobj.getString("rowid"));
                KwlReturnObject bpodresult = accountingHandlerDAOobj.getObject(BillingPurchaseOrderDetail.class.getName(), jobj.getString("rowid"));
                BillingPurchaseOrderDetail rd = (BillingPurchaseOrderDetail) bpodresult.getEntityList().get(0);

                row.setCompany(company);
                row.setPurchaseOrderDetail(rd);
//                row.setBillingGoodsReceipt(bgr);
                row.setRate(jobj.getDouble(RATE));
              
                row.setQuantity(jobj.getDouble(QUANTITY));
                row.setAmount(jobj.getInt(CALAMOUNT));
                row.setProductDetail(URLDecoder.decode(jobj.getString("productdetail"), StaticValues.ENCODING));
                totalamount += (row.getRate() * row.getQuantity());
                Discount discount = null;
                double disc = jobj.getDouble(PRDISCOUNT);
                if (disc != 0.0) {
                    Map<String, Object> discMap = new HashMap();
                    discMap.put( DISCOUNT,disc);
                    discMap.put(INPERCENT, true);
                    discMap.put(ORIGINALAMOUNT, row.getRate() * jobj.getInt(QUANTITY));
                    discMap.put(COMPANYID, company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.updateDiscount(discMap);
                    discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);

//                    discount = new Discount();
//                    discount.setDiscount(disc);
//                    discount.setOriginalAmount(row.getRate() * row.getQuantity());
//                    discount.setInPercent(true);
//                    discount.setCompany(company);
//                    row.setDiscount(discount);
//                    session.save(discount);
                    totaldiscount += discount.getDiscountValue();
                }
                String rowtaxid = jobj.getString(PRTAXID);
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    boolean taxExist=false;
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(),rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null)
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    else{
                        row.setTax(rowtax);
                        double rowtaxamount=StringUtil.getDouble(jobj.getString(TAXAMOUNT));
                       // KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, rowtaxamount, currencyid, df.parse(request.getParameter("billdate")), externalCurrencyRate);
                       // rowtaxamount= (Double) bAmt.getEntityList().get(0);
                        taxamount += rowtaxamount;
                        if (taxamount > 0) {
//                            Iterator itr = jeDetails.iterator();
//                            while (itr.hasNext()) {
//                                jed = (JournalEntryDetail) itr.next();
                          if(jeDetails!=null && ! jeDetails.isEmpty()){
                               for(JournalEntryDetail jed:jeDetails){
                                    if (jed.getAccount() == rowtax.getAccount()) {
    //                                          jed.setAmount(jed.getAmount() + row.getRate() * inventory.getQuantity());
                                        JSONObject jedjson = new JSONObject();
                                        jedjson.put(JEDID, jed.getID());
                                        jedjson.put(AMOUNT,jed.getAmount() +rowtaxamount );
                                        KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                        taxExist=true;
                                        break;
                                    }
                                }
                            }
                            if(!taxExist){
                                JSONObject jedjson = new JSONObject();
                                jedjson = new JSONObject();
                                jedjson.put(SRNO, jeDetails.size()+1);
                                jedjson.put(COMPANYID, company.getCompanyID());
                                jedjson.put( AMOUNT,rowtaxamount);
                                jedjson.put(ACCOUNTID, rowtax.getAccount().getID());
                                jedjson.put(DEBIT, true);
                                jedjson.put( JEID,jeid);
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);

                            }
                        }
                    }
                }
                rows.add(row);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ll.add(new double[]{totaldiscount, totalamount, taxamount});
            ll.add(rows);
        }
        //bgr.setRows(rows);
        return ll;
    }



}
