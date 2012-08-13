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

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BillingCreditNoteDetail;
import com.krawler.hql.accounting.BillingInvoice;
import com.krawler.hql.accounting.BillingInvoiceDetail;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.RepeatedInvoices;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
public class accInvoiceControllerCMN extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accInvoiceDAO accInvoiceDAOobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accReceiptDAO accReceiptDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCustomerDAO accCustomerDAOobj;
    private accTaxDAO accTaxObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private String successView;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accInvoiceCMN accInvoiceCommon;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccDiscountDAO(accDiscountDAO accDiscountobj) {
        this.accDiscountobj = accDiscountobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }

    public ModelAndView getInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap requestParams = getInvoiceRequestMap(request);
            KwlReturnObject result = accInvoiceDAOobj.getInvoices(requestParams);
            List list = result.getEntityList();
            JSONArray DataJArr = new JSONArray();
//            if(requestParams.containsKey("getRepeateInvoice")){
//                if(Boolean.parseBoolean((String) requestParams.get("getRepeateInvoice"))){
//                    DataJArr = getRepeateInvoiceJson(request, list);
//                } else {
                    DataJArr = getInvoiceJson(request, list).getJSONArray("data");
//                }
//            }
            int count = DataJArr.length();
            JSONArray pagedJson = DataJArr;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }

            jobj.put("data", pagedJson);
            jobj.put("count", count);
            issuccess = true;
        } catch (Exception ex){
            msg = ""+ex.getMessage();
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
    public ModelAndView exportInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
             HashMap<String, Object> requestParams = getInvoiceRequestMap(request);
            KwlReturnObject result = accInvoiceDAOobj.getInvoices(requestParams);
//            int count = result.getRecordTotalCount();
//            List list = result.getEntityList();
//            JSONArray DataJArr = getInvoiceJson(request, list).getJSONArray("data");
//            jobj.put("data", DataJArr);
            jobj = getInvoiceJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public JSONObject getInvoiceJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONObject jobj=new JSONObject();
        JSONArray jArr=new JSONArray();
        try{
            HashMap requestParams = getInvoiceRequestMap(request);
            DateFormat df = authHandler.getDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean ignoreZero = request.getParameter("ignorezero") != null;
            boolean onlyAmountDue = requestParams.get("onlyamountdue") != null;
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String cashAccount = pref.getCashAccount().getID();

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                double taxPercent = 0;
                Invoice invoice = (Invoice) itr.next();
                JournalEntry je = invoice.getJournalEntry();
                JournalEntryDetail d = invoice.getCustomerEntry();
                Account account = d.getAccount();
                String currencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
                double amountdue= accInvoiceCommon.getAmountDue(requestParams,invoice);
                if(onlyAmountDue&&authHandler.round(amountdue,2)==0)
                    continue;
                JSONObject obj = new JSONObject();
                obj.put("billid", invoice.getID());
                obj.put("personid", invoice.getCustomer()==null?account.getID():invoice.getCustomer().getID());
                obj.put("personemail", invoice.getCustomer()==null?"":invoice.getCustomer().getEmail());
                obj.put("accid", account.getID());
                obj.put("billno", invoice.getInvoiceNumber()); 
                obj.put("currencyid",currencyid);
                obj.put("currencysymbol",(invoice.getCurrency()==null?currency.getSymbol(): invoice.getCurrency().getSymbol()));
                obj.put("companyaddress", invoice.getCompany().getAddress());
                obj.put("companyname", invoice.getCompany().getCompanyName());
//                obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,je.getEntryDate()));
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, je.getEntryDate(), 0);
                obj.put("oldcurrencyrate", (Double) bAmt.getEntityList().get(0));
                obj.put("billto", invoice.getBillTo());
                obj.put("shipto", invoice.getShipTo());
                obj.put("journalentryid", je.getID());
                obj.put("porefno", invoice.getPoRefNumber());
                obj.put("externalcurrencyrate", je.getExternalCurrencyRate());
                obj.put("entryno", je.getEntryNumber());
                obj.put("date", df.format(je.getEntryDate()));
                obj.put("shipdate", df.format(invoice.getShipDate()));
                obj.put("duedate", df.format(invoice.getDueDate()));
                obj.put("personname", invoice.getCustomer()==null?account.getName():invoice.getCustomer().getName());
                obj.put("memo", invoice.getMemo());
                obj.put("termname",invoice.getCustomer()==null?"":invoice.getCustomer().getCreditTerm().getTermname());
                obj.put("deleted", invoice.isDeleted());
                obj.put("taxincluded", invoice.getTax() == null ? false : true);
                obj.put("taxid", invoice.getTax() == null ? "" : invoice.getTax().getID());
                obj.put("taxname", invoice.getTax() == null ? "" : invoice.getTax().getName());
                obj.put("taxamount", invoice.getTaxEntry() == null ? 0 : invoice.getTaxEntry().getAmount());
                obj.put("discount", invoice.getDiscount() == null ? 0 : invoice.getDiscount().getDiscountValue());
                obj.put("ispercentdiscount", invoice.getDiscount()==null?false:invoice.getDiscount().isInPercent());
                obj.put("discountval", invoice.getDiscount()==null?0:invoice.getDiscount().getDiscount());
                obj.put("costcenterid", je.getCostcenter()==null?"":je.getCostcenter().getID());
                obj.put("costcenterName", je.getCostcenter()==null?"":je.getCostcenter().getName());
                if (account.getID().equals(cashAccount)) {
                    obj.put("amountdue", 0);
                    obj.put("amountdueinbase",0);
                    obj.put("incash", true);
                } else {
//                    obj.put("amountdueinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,amount - ramount,currencyid,je.getEntryDate()));  //amount left after apllying receipt and CN
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                     obj.put("amountdueinbase", authHandler.round((Double) bAmt.getEntityList().get(0),2));
                      obj.put("amountdue", authHandler.round(amountdue,2));
                   // obj.put("amountdue", amountdue);
                }
                obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue,2));
                obj.put("amount", d.getAmount());   //actual invoice amount
//                obj.put("amountinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,d.getAmount(),currencyid,je.getEntryDate()));
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, d.getAmount(), currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                double amountinbase=(Double)bAmt.getEntityList().get(0);
                obj.put("amountinbase",authHandler.round(amountinbase,2) );

                if (invoice.getTax() != null) {
//                    taxPercent = CompanyHandler.getTaxPercent(session, request, je.getEntryDate(), invoice.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, je.getEntryDate(), invoice.getTax().getID());
                    taxPercent = (Double) perresult.getEntityList().get(0);
                }
                obj.put("taxpercent", taxPercent);  //tax in percent applyind on invoice
                try {
                    obj.put("creditDays", invoice.getCustomer().getCreditTerm().getTermdays());
                } catch(Exception ex) {
                    obj.put("creditDays", 0);
                }
                RepeatedInvoices repeatedInvoice = invoice.getRepeateInvoice();
                obj.put("isRepeated", repeatedInvoice==null?false:true);
                if(repeatedInvoice!=null){
                    obj.put("repeateid",repeatedInvoice.getId());
                    obj.put("interval",repeatedInvoice.getIntervalUnit());
                    obj.put("intervalType",repeatedInvoice.getIntervalType());
                    SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
//                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"+sessionHandlerImpl.getTimeZoneDifference(request)));
                    obj.put("startDate",sdf.format(repeatedInvoice.getStartDate()));
                    obj.put("nextDate",sdf.format(repeatedInvoice.getNextDate()));
                    obj.put("expireDate",repeatedInvoice.getExpireDate()==null?"":sdf.format(repeatedInvoice.getExpireDate()));
                    requestParams.put("parentInvoiceId", invoice.getID());
                    KwlReturnObject details = accInvoiceDAOobj.getRepeateInvoicesDetails(requestParams);
                    List detailsList = details.getEntityList();
                    obj.put("childCount", detailsList.size());
                }

                if (!(ignoreZero && authHandler.round(amountdue,2) <= 0)) {
                    jArr.put(obj);
                }
                
            }
            if(request.getParameter("filename") != null){
            	if(request.getParameter("filename").equals("Aged Receivable")){
		            if(request.getParameter("filetype") != null){
			            if(request.getParameter("filetype").equals("print")){
			            	if(!request.getParameter("mode").equals("18")){
				            	double total = 0;
				            	for(int i = 0; i < jArr.length(); i++)
				            		total = total + (Double)jArr.getJSONObject(i).get("amountdueinbase");
				            	JSONObject obj1 = new JSONObject();
				            	obj1.put("amountdueinbase", total);
				            	obj1.put("billno", "Total Amount Due");
				            	jArr.put(obj1);
			            	}
			            }
		            }
            	}
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getInvoiceJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }
    
    public JSONArray getRepeateInvoiceJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONArray jArr=new JSONArray();
        try{
            DateFormat df = authHandler.getDateFormatter(request);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Invoice invoice = (Invoice) itr.next();
                JournalEntryDetail d = invoice.getCustomerEntry();
                Account account = d.getAccount();
                JSONObject obj = new JSONObject();
                obj.put("billid", invoice.getID());
                obj.put("personid", invoice.getCustomer()==null?account.getID():invoice.getCustomer().getID());
                obj.put("billno", invoice.getInvoiceNumber());
                obj.put("personname", invoice.getCustomer()==null?account.getName():invoice.getCustomer().getName());

                obj.put("repeateid",invoice.getRepeateInvoice().getId());
                obj.put("interval",invoice.getRepeateInvoice().getIntervalUnit());
                obj.put("intervalType",invoice.getRepeateInvoice().getIntervalType());
                obj.put("startDate",df.format(invoice.getRepeateInvoice().getStartDate()));
                obj.put("nextDate",df.format(invoice.getRepeateInvoice().getNextDate()));
                obj.put("expireDate",invoice.getRepeateInvoice().getExpireDate()==null?"":df.format(invoice.getRepeateInvoice().getExpireDate()));
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getRepeateInvoiceJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getRepeateBillingInvoiceJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONArray jArr=new JSONArray();
        try{
            DateFormat df = authHandler.getDateFormatter(request);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                BillingInvoice invoice = (BillingInvoice) itr.next();
                JournalEntryDetail d = invoice.getCustomerEntry();
                Account account = d.getAccount();
                JSONObject obj = new JSONObject();
                obj.put("billid", invoice.getID());
                obj.put("personid", invoice.getCustomer()==null?account.getID():invoice.getCustomer().getID());
                obj.put("billno", invoice.getBillingInvoiceNumber());
                obj.put("personname", invoice.getCustomer()==null?account.getName():invoice.getCustomer().getName());

                obj.put("repeateid",invoice.getRepeateInvoice().getId());
                obj.put("interval",invoice.getRepeateInvoice().getIntervalUnit());
                obj.put("intervalType",invoice.getRepeateInvoice().getIntervalType());
                obj.put("startDate",df.format(invoice.getRepeateInvoice().getStartDate()));
                obj.put("nextDate",df.format(invoice.getRepeateInvoice().getNextDate()));
                obj.put("expireDate",invoice.getRepeateInvoice().getExpireDate()==null?"":df.format(invoice.getRepeateInvoice().getExpireDate()));
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getRepeateBillingInvoiceJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getInvoiceRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONArray DataJArr = accInvoiceCommon.getInvoiceRows(request,null);
          jobj.put("data", DataJArr);
          //  jobj = getInvoiceRows(request);
          issuccess = true;
        } catch (SessionExpiredException ex){
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
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

    private HashMap getBillingInvoiceProductAmount(BillingInvoice invoice) throws ServiceException{
        HashMap hm=new HashMap();
        Set invRows=invoice.getRows();
        Iterator itr=invRows.iterator();
        double amount;
        double quantity;
        while(itr.hasNext()){
            BillingInvoiceDetail temp=(BillingInvoiceDetail)itr.next();
            quantity=temp.getQuantity();
            amount=temp.getRate()*quantity;
            double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
            double rowTaxPercent = 0;
            if (temp.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent( invoice.getCompany().getCompanyID(), invoice.getJournalEntry().getEntryDate(), temp.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
            }
             double ramount=amount - rdisc;
             ramount+=ramount*rowTaxPercent/100;
            hm.put(temp, new Object[]{ramount, quantity});
            //hm.put(temp, new Object[]{amount-rdisc,quantity});
            if(invoice==null)invoice=temp.getBillingInvoice();
        }
        return hm;
    }

    public ModelAndView getBillingInvoiceRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            jobj = getBillingInvoiceRowsJSON(request);
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = ""+ex.getMessage();
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

    public JSONObject getBillingInvoiceRowsJSON(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String[] invoices = request.getParameterValues("bills");
            int i = 0;

            HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("billingInvoice.ID");
            order_by.add("srno");
            order_type.add("asc");
            invRequestParams.put("filter_names", filter_names);
            invRequestParams.put("filter_params", filter_params);
            invRequestParams.put("order_by", order_by);
            invRequestParams.put("order_type", order_type);

            JSONArray jArr = new JSONArray();
            while (invoices != null && i < invoices.length) {

                KwlReturnObject result = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), invoices[i]);
                BillingInvoice invoice = (BillingInvoice) result.getEntityList().get(0);
//                    Iterator itr = invoice.getRows().iterator();
                filter_params.clear();
                filter_params.add(invoice.getID());
                KwlReturnObject idresult = accInvoiceDAOobj.getBillingInvoiceDetails(invRequestParams);
                Iterator itr = idresult.getEntityList().iterator();

                HashMap hm = applyBillingCreditNotes(requestParams, invoice);
                while (itr.hasNext()) {
                    BillingInvoiceDetail row = (BillingInvoiceDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", invoice.getID());
                    obj.put("billno", invoice.getBillingInvoiceNumber());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    String currencyid = (invoice.getCurrency() == null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());
                    obj.put("currencysymbol", (invoice.getCurrency() == null ? currency.getCurrencyID() : invoice.getCurrency().getSymbol()));
//                        obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,invoice.getJournalEntry().getEntryDate()));
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    obj.put("oldcurrencyrate", (Double) bAmt.getEntityList().get(0));
                    obj.put("productdetail", row.getProductDetail());
                    obj.put("quantity", row.getQuantity());
                    Discount disc = row.getDiscount();
                    if (disc != null && disc.isInPercent()) {
                        obj.put("prdiscount", disc.getDiscount());
                    } else {
                        obj.put("prdiscount", 0);
                    }
                    obj.put("rate", row.getRate());
                    double remainingquantity=0;
                    double amount = 0;
                    if (hm.containsKey(row)) {
                        Object[] val = (Object[]) hm.get(row);
                        amount = (Double) val[0];


                        remainingquantity=(Double)val[1];
                        obj.put("remainingquantity", remainingquantity);
                        obj.put("remquantity", 0);
                        obj.put("amount", amount);
                    }
                    HashMap amthm = getBillingInvoiceProductAmount(invoice);
                    Object[] val = (Object[]) amthm.get(row);
                    amount = (Double) val[0];
                    obj.put("orignalamount", amount);
                    double taxPercent = 0;
                    double rowTaxPercent = 0;
                    if (row.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("prtaxid", row.getTax()== null?"":row.getTax().getID());

                    if (invoice.getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("accInvoiceController.getBillingInvoiceRowsJSON", e);
        }
        return jobj;
    }

    public ModelAndView deleteInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteInvoices(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.rem.179", null, RequestContextUtils.getLocale(request)); //"Invoice(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteInvoices(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
    try {
        JSONArray jArr = new JSONArray(request.getParameter("data"));
        String companyid = sessionHandlerImpl.getCompanyid(request);

//        String cashAccount = ((CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request))).getCashAccount().getID();
        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
       // String cashAccount = preferences.getCashAccount().getID();

        String invoiceid, jeid;
        KwlReturnObject result;
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            invoiceid = jobj.getString("billid");

//            if (jobj.getString("personid").equals(cashAccount)) {
//                throw new AccountingException("Payment against the selected Sales Receipt(s) has been received. So, it cannot be deleted.");
//            }

//            query = "from CreditNoteDetail cn  where cn.invoiceRow.invoice.ID in ( " + qMarks + ") and cn.creditNote.deleted=false and cn.company.companyID=?";
//            list = HibernateUtil.executeQuery(session, query, params.toArray());
            result = accCreditNoteDAOobj.getCNFromInvoice(invoiceid, companyid);
            List list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Selected record(s) is currently used in the Credit Note(s). So it cannot be deleted.");
            }

//            query = "from ReceiptDetail rd  where rd.invoice.ID in ( " + qMarks + ")  and rd.receipt.deleted=false and rd.company.companyID=?";
//            list = HibernateUtil.executeQuery(session, query, params.toArray());
            result = accReceiptDAOobj.getReceiptFromInvoice(invoiceid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Payment against the selected Invoice(s) has been partially/fully received. So, it cannot be deleted.");
            }

//            query = "update Invoice inv set inv.deleted=true where inv.ID in( " + qMarks + ") and inv.company.companyID=?";
//            HibernateUtil.executeUpdate(session, query, params.toArray());
            result = accInvoiceDAOobj.deleteInvoiceEntry(invoiceid, companyid);

//            query = "update JournalEntry je set je.deleted=true  where je.ID in(select inv.journalEntry.ID from Invoice inv where inv.ID in( " + qMarks + ") and inv.company.companyID=je.company.companyID) and je.company.companyID=?";
//            HibernateUtil.executeUpdate(session, query, params.toArray());
            result = accInvoiceDAOobj.getJEFromInvoice(invoiceid);
            list = result.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                jeid = (String) itr.next();
                result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
            }

//            query = "update Discount di set di.deleted=true  where di.ID in(select inv.discount.ID from Invoice inv where inv.ID in( " + qMarks + ") and inv.company.companyID=di.company.companyID) and di.company.companyID=?";
//            HibernateUtil.executeUpdate(session, query, params.toArray());
            result = accInvoiceDAOobj.getInvoiceDiscount(invoiceid);
            list = result.getEntityList();
            itr = list.iterator();
            while (itr.hasNext()) {
                String discountid = (String) itr.next();
                result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
            }
//            query = "update Discount di set di.deleted=true  where di.ID in(select invd.discount.ID from InvoiceDetail invd where invd.invoice.ID in( " + qMarks + ") and invd.company.companyID=di.company.companyID) and di.company.companyID=?";
//            HibernateUtil.executeUpdate(session, query, params.toArray());
            result = accInvoiceDAOobj.getInvoiceDetailsDiscount(invoiceid);
            list = result.getEntityList();
            itr = list.iterator();
            while (itr.hasNext()) {
                String discountid = (String) itr.next();
                result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
            }

//            query = "update Inventory inv set inv.deleted=true  where inv.ID in(select invd.inventory.ID from InvoiceDetail invd where invd.invoice.ID in( " + qMarks + ") and invd.company.companyID=inv.company.companyID) and inv.company.companyID=?";
//            HibernateUtil.executeUpdate(session, query, params.toArray());
            result = accInvoiceDAOobj.getInvoiceInventory(invoiceid);
            list = result.getEntityList();
            itr = list.iterator();
            while (itr.hasNext()) {
                String inventoryid = (String) itr.next();
                result = accProductObj.deleteInventoryEntry(inventoryid, companyid);
            }
        }
    } catch (JSONException ex) {
        throw new AccountingException("Cannot extract data from client");
    }
    }

    public ModelAndView getBillingInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap requestParams = getInvoiceRequestMap(request);
            KwlReturnObject result = accInvoiceDAOobj.getBillingInvoices(requestParams);
            List list = result.getEntityList();

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                list = StringUtil.getPagedList(list, Integer.parseInt(start), Integer.parseInt(limit));
            }

//            jobj = getBillingInvoiceJson(request, list);
            JSONArray DataJArr = new JSONArray();
//            if(requestParams.containsKey("getRepeateInvoice")){
//                if(Boolean.parseBoolean((String) requestParams.get("getRepeateInvoice"))){
//                    DataJArr = getRepeateBillingInvoiceJson(request, list);
//                } else {
                    DataJArr = getBillingInvoiceJson(request, list).getJSONArray("data");
//                }
//            }
            jobj.put("data", DataJArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex){
            msg = ""+ ex.getMessage();
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

     public ModelAndView exportBillingInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
             HashMap<String, Object> requestParams = getInvoiceRequestMap(request);
            KwlReturnObject result = accInvoiceDAOobj.getBillingInvoices(requestParams);
            int count = result.getRecordTotalCount();
            List list = result.getEntityList();
            jobj = getBillingInvoiceJson(request, list);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public JSONObject getBillingInvoiceJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONObject jobj=new JSONObject();
        JSONArray jArr=new JSONArray();
        try{
            HashMap requestParams = getInvoiceRequestMap(request);
            DateFormat df = authHandler.getDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean ignoreZero = request.getParameter("ignorezero") != null;
            boolean onlyAmountDue = requestParams.get("onlyamountdue") != null;
            double externalCurrencyRate=StringUtil.getDouble(request.getParameter("externalcurrencyrate"));

            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String cashAccount = pref.getCashAccount().getID();

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                double taxPercent = 0;
                BillingInvoice invoice = (BillingInvoice) itr.next();
                JournalEntry je = invoice.getJournalEntry();
                JournalEntryDetail d = invoice.getCustomerEntry();
                Account account = d.getAccount();
                double amount = 0, ramount = 0;
                Iterator itrBir = applyBillingCreditNotes(requestParams, invoice).values().iterator();
                while (itrBir.hasNext()) {
                    Object[] temp = (Object[]) itrBir.next();
                    amount += (Double) temp[0] - (Double) temp[2];
                }
                JournalEntryDetail tempd = invoice.getShipEntry();
                if (tempd != null) {
                    amount += tempd.getAmount();
                }
                tempd = invoice.getOtherEntry();
                if (tempd != null) {
                    amount += tempd.getAmount();
                }
                tempd = invoice.getTaxEntry();
                if (tempd != null) {
                    amount += tempd.getAmount();
                }
//                String q = "select sum(amount) from BillingReceiptDetail rd where rd.billingInvoice.ID=? and rd.billingReceipt.deleted=false group by rd.billingInvoice";
//                List l = HibernateUtil.executeQuery(session, q, invoice.getID());
//                ramount = (l.isEmpty() ? 0 : (Double) l.get(0));
                KwlReturnObject amtrs = accReceiptDAOobj.getBillingReceiptAmountFromInvoice(invoice.getID());
                ramount = amtrs.getEntityList().size()>0 ? (Double) amtrs.getEntityList().get(0) : 0;
                String currencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
                double amountdue=amount - ramount;
                if(onlyAmountDue&&authHandler.round(amountdue,2)==0)
                    continue;
                JSONObject obj = new JSONObject();
                obj.put("billid", invoice.getID());
                obj.put("personid",invoice.getCustomer()==null?account.getID():invoice.getCustomer().getID());// account.getID());
                obj.put("personemail", invoice.getCustomer()==null?"":invoice.getCustomer().getEmail());
                obj.put("crdraccid",invoice.getCreditorEntry().getAccount().getID());// account.getID());
                obj.put("billno", invoice.getBillingInvoiceNumber());
                obj.put("currencyid",currencyid);
                obj.put("currencysymbol",(invoice.getCurrency()==null?"": invoice.getCurrency().getSymbol()));
                obj.put("companyaddress", invoice.getCompany().getAddress());
//                obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,je.getEntryDate()));
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                obj.put("oldcurrencyrate", (Double) bAmt.getEntityList().get(0));
                obj.put("billto", invoice.getBillTo());
                obj.put("shipto", invoice.getShipTo());
                obj.put("porefno", invoice.getPoRefNumber());
                obj.put("journalentryid", je.getID());
                obj.put("entryno", je.getEntryNumber());
                obj.put("externalcurrencyrate", je.getExternalCurrencyRate());
                obj.put("date", df.format(je.getEntryDate()));
                obj.put("shipdate", df.format(invoice.getShipDate()));
                obj.put("duedate", df.format(invoice.getDueDate()));
                obj.put("personname", invoice.getCustomer()==null?account.getName():invoice.getCustomer().getName());
                obj.put("taxamount", invoice.getTaxEntry() == null ? 0 : invoice.getTaxEntry().getAmount());
                obj.put("taxincluded", invoice.getTax() == null ? false : true);
                obj.put("taxid", invoice.getTax() == null ? "" : invoice.getTax().getID());
                obj.put("taxname", invoice.getTax() == null ? "" : invoice.getTax().getName());
                obj.put("memo", invoice.getMemo());
                obj.put("deleted", invoice.isDeleted());
                obj.put("discount", invoice.getDiscount() == null ? 0 : invoice.getDiscount().getDiscountValue());
                obj.put("ispercentdiscount", invoice.getDiscount()==null?false:invoice.getDiscount().isInPercent());
                obj.put("discountval", invoice.getDiscount()==null?0:invoice.getDiscount().getDiscount());
                obj.put("costcenterid", je.getCostcenter()==null?"":je.getCostcenter().getID());
                obj.put("costcenterName", je.getCostcenter()==null?"":je.getCostcenter().getName());
                if (account.getID().equals(cashAccount)) {
                    obj.put("amountdue", 0);
                    obj.put("incash", true);
                    obj.put("amountdueinbase", 0);
                } else {
                     obj.put("amountdue",  authHandler.round(amountdue,2));
                   // obj.put("amountdue", amount - ramount);
//                    obj.put("amountdueinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,amount - ramount,currencyid,je.getEntryDate()));
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                    double amountdueinbase=(Double)bAmt.getEntityList().get(0);
                    obj.put("amountdueinbase", authHandler.round(amountdueinbase ,2));
                }
                obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue,2));
                obj.put("amount", d.getAmount());
//                obj.put("amountinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,d.getAmount(),currencyid,je.getEntryDate()));
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, d.getAmount(), currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                double amountinbase=(Double)bAmt.getEntityList().get(0);
                obj.put("amountinbase", authHandler.round(amountinbase,2));
                if (!(ignoreZero && authHandler.round(amountdue,2) <= 0)) {
                    jArr.put(obj);
                }
                if (invoice.getTax() != null) {
//                    taxPercent = CompanyHandler.getTaxPercent(session, request, je.getEntryDate(), invoice.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, je.getEntryDate(), invoice.getTax().getID());
                    taxPercent = (Double) perresult.getEntityList().get(0);
                }
                obj.put("taxpercent", taxPercent);  //tax in percent applyind on invoice
                try {
                    obj.put("creditDays", invoice.getCustomer().getCreditTerm().getTermdays());
                } catch(Exception ex) {
                    obj.put("creditDays", 0);
                }
                RepeatedInvoices repeatedInvoice = invoice.getRepeateInvoice();
                obj.put("isRepeated", repeatedInvoice==null?false:true);
                if(repeatedInvoice!=null){
                    obj.put("repeateid",repeatedInvoice.getId());
                    obj.put("interval",repeatedInvoice.getIntervalUnit());
                    obj.put("intervalType",repeatedInvoice.getIntervalType());
                    SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
//                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"+sessionHandlerImpl.getTimeZoneDifference(request)));
                    obj.put("startDate",sdf.format(repeatedInvoice.getStartDate()));
                    obj.put("nextDate",sdf.format(repeatedInvoice.getNextDate()));
                    obj.put("expireDate",repeatedInvoice.getExpireDate()==null?"":sdf.format(repeatedInvoice.getExpireDate()));
                    requestParams.put("parentInvoiceId", invoice.getID());
                    KwlReturnObject details = accInvoiceDAOobj.getRepeateBillingInvoicesDetails(requestParams);
                    List detailsList = details.getEntityList();
                    obj.put("childCount", detailsList.size());
                }

            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBillingInvoiceJson : "+ex.getMessage(), ex);
        }
        return jobj;
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
    public double applyBillingInvDisount(BillingInvoiceDetail invdetail, double withoutDTAmt) throws ServiceException {
        double disc = (invdetail.getBillingInvoice().getDiscount() == null ? 0 : invdetail.getBillingInvoice().getDiscount().getDiscountValue());
        if (disc == 0) {
            return 0;
        }
        double quantity = invdetail.getQuantity();
        double amount = (quantity == 0 ? 0 : invdetail.getRate() * quantity);
        double rowDiscountRatio = (withoutDTAmt == 0 ? 0 : amount / withoutDTAmt);
        double rowDiscount = disc * rowDiscountRatio;
        return rowDiscount;
    }
  private HashMap applyBillingCreditNotes(HashMap requestParams, BillingInvoice invoice) throws ServiceException {
        HashMap hm=new HashMap();
        Set invRows=invoice.getRows();
        Iterator itr=invRows.iterator();
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), (String) requestParams.get("gcurrencyid"));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

        double amount;
        double quantity;
        double withoutDTAmt=0;
        while(itr.hasNext()){//reqiured for invoice discount row wise division[PS]
            BillingInvoiceDetail temp=(BillingInvoiceDetail)itr.next();
            withoutDTAmt+=temp.getRate()*temp.getQuantity();
        }
        itr=invRows.iterator();
        while(itr.hasNext()){
            BillingInvoiceDetail temp=(BillingInvoiceDetail)itr.next();
            quantity=temp.getQuantity();
            amount=temp.getRate()*quantity;
            double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
            double rowTaxPercent = 0;
            if (temp.getTax() != null) {
                KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), invoice.getJournalEntry().getEntryDate(), temp.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
            }
            double ramount=amount-rdisc;
            ramount+=ramount*rowTaxPercent/100;
            double invoiceDisc=temp.getBillingInvoice().getDiscount()==null?0:applyBillingInvDisount(temp,withoutDTAmt);
            ramount-=invoiceDisc;
            hm.put(temp, new Object[]{ramount, quantity, 0.0});
            if(invoice==null)invoice=temp.getBillingInvoice();
        }
        KwlReturnObject result = accCreditNoteDAOobj.getCNRowsDiscountFromBillingInvoice(invoice.getID());
        List list = result.getEntityList();
        Iterator cnitr = list.iterator();
        double taxAmount=0;
        while(cnitr.hasNext()){
            Object[] cnrow=(Object[])cnitr.next();
            BillingCreditNoteDetail cnr=(BillingCreditNoteDetail)cnrow[1];
            BillingInvoiceDetail temp=cnr.getInvoiceRow();
            if(!hm.containsKey(temp))continue;
            Object[] val=(Object[])hm.get(temp);
            String fromcurrencyid=(cnr.getCreditNote().getCurrency()==null?currency.getCurrencyID(): cnr.getCreditNote().getCurrency().getCurrencyID());
            String tocurrencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
            double baseDisount = 0;
            if(cnr.getDiscount() != null){
               KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, cnr.getDiscount().getDiscountValue(), fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(),invoice.getJournalEntry().getExternalCurrencyRate());
               baseDisount = (Double) bAmt.getEntityList().get(0);
            }
            double v = (Double) val[0] - (cnr.getDiscount() == null ? 0 : baseDisount);
            if(cnr.getTaxAmount()!=null){
                taxAmount+=cnr.getTaxAmount();
            }
            double q=(Double)val[1];
            q-=cnr.getQuantity();
            hm.put(temp, new Object[]{v,q,taxAmount});
        }
        return hm;
    }

    public ModelAndView deleteBillingInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            KwlReturnObject result = deleteBillingInvoices(request);
            issuccess = result.isSuccessFlag();
            msg = result.getMsg();
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accInvoiceController.deleteBillingInvoices : "+ex.getMessage();
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

    public KwlReturnObject deleteBillingInvoices(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException{
        boolean issuccess = false;
        String msg = "";
        int numRows = 0;
        List list = null;
        try{
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);

//            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
//            String cashAccount = preferences.getCashAccount().getID();

            String invoiceid/*, jeid*/;
            KwlReturnObject result;
            for (int i = 0; i < jArr.length(); i++){
                JSONObject jobj = jArr.getJSONObject(i);
                try{
                    invoiceid = URLDecoder.decode(jobj.getString("billid"),StaticValues.ENCODING);
                } catch (UnsupportedEncodingException ex) {
                    throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
                }
//                if (URLDecoder.decode(jobj.getString("personid"),StaticValues.ENCODING).equals(cashAccount)) {
//                    throw new AccountingException("Payment against the selected Sales Receipt(s) has been received. So, it cannot be deleted.");
//                }

//                CheckBillingReceipt(invoiceid, companyid);


            //query = "from BillingCreditNoteDetail dn  where dn.invoiceRow.billingInvoice.ID in ( "+qMarks +")  and dn.creditNote.deleted=false and dn.company.companyID=?";
            result = accCreditNoteDAOobj.getBillingCreditNoteDet(invoiceid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Selected record(s) is currently used in the Credit Note(s). So it cannot be deleted.");
            }
            //query = "from BillingReceiptDetail rd  where rd.billingInvoice.ID in ( "+qMarks +")  and rd.billingReceipt.deleted=false and rd.company.companyID=?";
            result = accReceiptDAOobj.getBillingReceiptDetail(invoiceid, companyid);
            list = result.getEntityList();
            if (!list.isEmpty()) {
                throw new AccountingException("Payment against the selected Invoice(s) has been partially/fully received. So, it cannot be deleted.");
            }

            // Delete from billing invoice
            //query = "update BillingInvoice inv set inv.deleted=true where inv.ID in( " + qMarks + ") and inv.company.companyID=?";
            result = accInvoiceDAOobj.deleteBillingInvoiceEntry(invoiceid, companyid);


            // Delete from journal entry
            //query = "update JournalEntry je set je.deleted=true  where je.ID in(select inv.journalEntry.ID from BillingInvoice inv where inv.ID in( " + qMarks + ") and inv.company.companyID=je.company.companyID) and je.company.companyID=?";
            result = accInvoiceDAOobj.getJEFromBI(invoiceid, companyid);
            list = result.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                BillingInvoice billingInvoice = (BillingInvoice) itr.next();
                if (billingInvoice.getJournalEntry()!= null) {
                    result = accJournalEntryobj.deleteJEEntry(billingInvoice.getJournalEntry().getID(), companyid);
                }
            }

            // Delete from discount
            //query = "update Discount di set di.deleted=true  where di.ID in(    select inv.discount.ID     from BillingInvoice inv where inv.ID in( " + qMarks + ") and inv.company.companyID=di.company.companyID) and di.company.companyID=?";
            result = accInvoiceDAOobj.getJEFromBI(invoiceid, companyid);
            list = result.getEntityList();
            itr = list.iterator();
            while (itr.hasNext()) {
                BillingInvoice billingInvoice = (BillingInvoice) itr.next();
                if (billingInvoice.getDiscount()!= null) {
                    result = accDiscountobj.deleteDiscountEntry(billingInvoice.getDiscount().getID(), companyid);
                }
            }

            //delete from discount
            //query = "update Discount di set di.deleted=true  where di.ID in(select invd.discount.ID from BillingInvoiceDetail invd where invd.billingInvoice.ID in( " + qMarks + ") and invd.company.companyID=di.company.companyID) and di.company.companyID=?";
            result = accInvoiceDAOobj.getDisIdFromBIDet(invoiceid, companyid);
            list = result.getEntityList();
            itr = list.iterator();
            while (itr.hasNext()) {
                BillingInvoiceDetail billingInvoice = (BillingInvoiceDetail) itr.next();
                if (billingInvoice.getDiscount()!= null) {
                    result = accDiscountobj.deleteDiscountEntry(billingInvoice.getDiscount().getID(), companyid);
                }
            }

                /*KwlReturnObject binvoiceres = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), invoiceid);
                BillingInvoice binvoice = (BillingInvoice) binvoiceres.getEntityList().get(0);

//                deleteBillingInvoicesRows(invoiceid, companyid);
                //Delete Invoice details
                result = accInvoiceDAOobj.deleteBillingInvoiceDtails(invoiceid, companyid);
                //Delete Invoice detail's discount
                result = deleteBillingInvoiceDetailsDiscount(invoiceid, companyid);

                //Delete Invoice and Invoice-details
                result = accInvoiceDAOobj.deleteBillingInvoice(invoiceid, companyid);

                //Delete Invoice's discount
                if (binvoice.getDiscount() != null) {
                    result = accDiscountobj.deleteDiscount(binvoice.getDiscount().getID(), companyid);
                }

                //jeid = URLDecoder.decode(jobj.getString("journalentryid"),StaticValues.ENCODING);
                //Delete Journal Entry and Details
                result = accJournalEntryobj.deleteJEDtails(jeid, companyid);

                //Delete Journal Entry Details
                result = accJournalEntryobj.deleteJE(jeid, companyid);
*/
                numRows++;
            }
            issuccess = true;
            msg = "Invoice(s) has been deleted successfully";
        } catch (AccountingException ex) {
            msg = ex.getMessage();
        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(ServiceException ex){
            msg = "Selected record(s) is currently used in the transaction(s)";
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(issuccess, msg, null, null, numRows);
    }

    public KwlReturnObject deleteBillingInvoiceDetailsDiscount(String invoiceid, String companyid) throws ServiceException {
        KwlReturnObject result;
        KwlReturnObject binvoiceres = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), invoiceid);
        BillingInvoice binvoice = (BillingInvoice) binvoiceres.getEntityList().get(0);
        Iterator itr = binvoice.getRows().iterator();
        int numRows = 0;
        while (itr.hasNext()) {
            BillingInvoiceDetail row = (BillingInvoiceDetail) itr.next();
            if (row.getDiscount() != null) {
                String discountid = row.getDiscount().getID();
                result = accDiscountobj.deleteDiscount(discountid, companyid);
                numRows += result.getRecordTotalCount();
            }
        }
        return new KwlReturnObject(true, "Invoice Details Discount has been deleted successfully", null, null, numRows);
    }

    public JSONArray getCustomerAgedReceivable(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            DecimalFormat twoDForm = new DecimalFormat("#.##");
            HashMap invoiceRequestParams = getInvoiceRequestMap(request);
            String curDateString = request.getParameter("curdate")!=null?request.getParameter("curdate"):request.getParameter("stdate");
            Date curDate = authHandler.getDateFormatter(request).parse(curDateString);
            int duration = request.getParameter("duration") == null ? 0 : Integer.parseInt(request.getParameter("duration"));
            boolean isdistibutive = StringUtil.getBoolean(request.getParameter("isdistributive"));
            boolean withinventory = StringUtil.getBoolean(request.getParameter("withinventory"));
//            String customerid = request.getParameter("accid");
            double amountdue1 = 0;
            double amountdue2 = 0;
            double amountdue3 = 0;
            double amountdue4 = 0;
             double amountdueinbase1 = 0;
            double amountdueinbase2 = 0;
            double amountdueinbase3 = 0;
            double amountdueinbase4 = 0;
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            cal1.setTime(curDate);
            cal2.setTime(curDate);
            cal3.setTime(curDate);
            cal2.add(Calendar.DAY_OF_YEAR, -duration);
            cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
//            ArrayList params = new ArrayList();
//            String condition = "";
//            params.add(AuthHandler.getCompanyid(request));
//            if (!StringUtil.isNullOrEmpty(customerid)) {
//                params.add(customerid);
//                condition += " and c.account.ID=? ";
//            }
//            String q = "select ID from Customer c where company.companyID= ?" + condition;
//            Iterator itrcust = HibernateUtil.executeQuery(session, q, params.toArray()).iterator();
            KwlReturnObject custresult = accCustomerDAOobj.getCustomerForAgedReceivable(invoiceRequestParams);
            Iterator itrcust = custresult.getEntityList().iterator();
            while (itrcust.hasNext()) {
                amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdueinbase1 = amountdueinbase2 = amountdueinbase3 = amountdueinbase4 =0;
                JSONObject invJObj = new JSONObject();
                String personID = null;
                String personName = null;
                String amountdueInBase = null;
                String currencySymbol=null;
                String currencyid=null;
                Object custid = itrcust.next();
                invoiceRequestParams.put("customerid", custid);
                if (withinventory) {
                    KwlReturnObject result = accInvoiceDAOobj.getInvoices(invoiceRequestParams);
                    invJObj = getInvoiceJson(request, result.getEntityList());
                } else {
                    KwlReturnObject result = accInvoiceDAOobj.getBillingInvoices(invoiceRequestParams);
                    invJObj = getBillingInvoiceJson(request, result.getEntityList());
                }
                JSONArray invjarr = invJObj.getJSONArray("data");
                for (int i = 0; i < invjarr.length(); i++) {
                    JSONObject invobj = invjarr.getJSONObject(i);
                    personID = (invobj.has("personid"))?invobj.getString("personid"):"";
                    personName = (invobj.has("personname"))?invobj.getString("personname"):"";
                    amountdueInBase = invobj.getString("amountdueinbase");
                    currencySymbol=(invobj.has("currencysymbol"))?invobj.getString("currencysymbol"):"";
                    currencyid=(invobj.has("currencyid"))?invobj.getString("currencyid"):"";
                    Date dueDate = authHandler.getDateFormatter(request).parse(invobj.getString("duedate"));

                    if (isdistibutive) {
                        if (dueDate.after(cal1.getTime()) || dueDate.equals(cal1.getTime())) {
                            amountdueinbase1 += invobj.getDouble("amountdueinbase");
                            amountdue1 += invobj.getDouble("amountdue");
                        } else if ((cal2.getTime().before(dueDate) || cal2.getTime().equals(dueDate)) && cal1.getTime().after(dueDate)) {
                            amountdueinbase2 += invobj.getDouble("amountdueinbase");
                            amountdue2 += invobj.getDouble("amountdue");
                        } else if ((cal3.getTime().before(dueDate) || cal3.getTime().equals(dueDate)) && cal2.getTime().after(dueDate)) {
                            amountdueinbase3 += invobj.getDouble("amountdueinbase");
                            amountdue3 += invobj.getDouble("amountdue");
                        } else {
                            amountdueinbase4 += invobj.getDouble("amountdueinbase");
                            amountdue4 += invobj.getDouble("amountdue");
                        }
                    } else {
                        if (dueDate.after(cal1.getTime()) || dueDate.equals(cal1.getTime())) {
                            amountdueinbase1 += invobj.getDouble("amountdueinbase");
                            amountdue1 += invobj.getDouble("amountdue");
                        }
                        if (dueDate.after(cal2.getTime()) || dueDate.equals(cal2.getTime())) {
                            amountdueinbase2 += invobj.getDouble("amountdueinbase");
                            amountdue2 += invobj.getDouble("amountdue");
                        }
                        if (dueDate.after(cal3.getTime()) || dueDate.equals(cal3.getTime())) {
                            amountdueinbase3 += invobj.getDouble("amountdueinbase");
                            amountdue3 += invobj.getDouble("amountdue");
                        }
                        amountdueinbase4 += invobj.getDouble("amountdueinbase");
                        amountdue4 += invobj.getDouble("amountdue");
                    }
                }
                if (invjarr.length() > 0) {
                    jObj = new JSONObject();
                    jObj.put("personid", personID);
                    jObj.put("amountdueinbase", amountdueInBase);
                    jObj.put("personname", personName);
                    jObj.put("amountdue1", Double.valueOf(twoDForm.format(amountdue1)));
                    jObj.put("amountdue2",Double.valueOf(twoDForm.format(amountdue2)));
                    jObj.put("amountdue3", Double.valueOf(twoDForm.format(amountdue3)));
                    jObj.put("amountdue4", Double.valueOf(twoDForm.format(amountdue4)));
                    jObj.put("amountdueinbase1", Double.valueOf(twoDForm.format(amountdueinbase1)));
                    jObj.put("amountdueinbase2",Double.valueOf(twoDForm.format(amountdueinbase2)));
                    jObj.put("amountdueinbase3", Double.valueOf(twoDForm.format(amountdueinbase3)));
                    jObj.put("amountdueinbase4", Double.valueOf(twoDForm.format(amountdueinbase4)));
                    jObj.put("currencysymbol", currencySymbol);
                    jObj.put("currencyid", currencyid);
                    double amountdue= Double.valueOf(twoDForm.format(amountdue1 + amountdue2 + amountdue3 + amountdue4));
                    jObj.put("total",amountdue );
                    double amountdueinbase= Double.valueOf(twoDForm.format(amountdueinbase1 + amountdueinbase2 + amountdueinbase3 + amountdueinbase4));
                    jObj.put("totalinbase", amountdueinbase);
                    jArr.put(jObj);
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        return jArr;
    }

    public ModelAndView getCustomerAgedReceivable(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            JSONArray invJArr = new JSONArray();
//            boolean withInventory = Boolean.parseBoolean(request.getParameter("withinventory"));
//            if (withInventory) {
//                invJArr = getCustomerInvoiceAgedReceivable(request, true);
//            } else {
//                invJArr = getCustomerInvoiceAgedReceivable(request, false);
//            }
            invJArr = getCustomerAgedReceivable(request);


            String start = request.getParameter("start");
            String limit = request.getParameter("limit");

            JSONArray jArr = new JSONArray(), temp = invJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                int st = Integer.parseInt(start);
                int ed = Math.min(temp.length(), st + Integer.parseInt(limit));
                for (int i = st; i < ed; i++) {
                    jArr.put(temp.getJSONObject(i));
                }
            }

            jobj.put("data", jArr);
            jobj.put("count", invJArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accInvoiceController.deleteBillingInvoices : "+ex.getMessage();
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

     public ModelAndView exportCustomerAgedReceivable(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            JSONArray invJArr = new JSONArray();
            invJArr = getCustomerAgedReceivable(request);
            jobj.put("data",invJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public static HashMap<String, Object> getInvoiceRequestMap (HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(CCConstants.REQ_costCenterId,request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(InvoiceConstants.accid, request.getParameter(InvoiceConstants.accid));
        requestParams.put(InvoiceConstants.cashonly, request.getParameter(InvoiceConstants.cashonly));
        requestParams.put(InvoiceConstants.creditonly, request.getParameter(InvoiceConstants.creditonly));
        requestParams.put(InvoiceConstants.ignorezero, request.getParameter(InvoiceConstants.ignorezero));
        requestParams.put(InvoiceConstants.persongroup, request.getParameter(InvoiceConstants.persongroup));
        requestParams.put(InvoiceConstants.isagedgraph, request.getParameter(InvoiceConstants.isagedgraph));
        requestParams.put(InvoiceConstants.curdate, request.getParameter(InvoiceConstants.curdate));
        requestParams.put(InvoiceConstants.customerid, request.getParameter(InvoiceConstants.customerid));
        requestParams.put(InvoiceConstants.deleted, request.getParameter(InvoiceConstants.deleted));
        requestParams.put(InvoiceConstants.nondeleted, request.getParameter(InvoiceConstants.nondeleted));
        requestParams.put(InvoiceConstants.billid, request.getParameter(InvoiceConstants.billid));
        requestParams.put(InvoiceConstants.getRepeateInvoice, request.getParameter(InvoiceConstants.getRepeateInvoice));
        requestParams.put(InvoiceConstants.onlyamountdue,request.getParameter(InvoiceConstants.REQ_onlyAmountDue));
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
        return requestParams;
    }

    public ModelAndView exportInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap requestParams = getInvoiceRequestMap(request);
            KwlReturnObject result = accInvoiceDAOobj.getInvoices(requestParams);
            jobj = getInvoiceJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView exportBillingInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap requestParams = getInvoiceRequestMap(request);
            KwlReturnObject result = accInvoiceDAOobj.getBillingInvoices(requestParams);
            jobj = getBillingInvoiceJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView getAgedReceivableChart(HttpServletRequest request, HttpServletResponse response) {
        JSONArray jarr = new JSONArray();
        String result = "";
        try {

            jarr = getMonthWiseReceivable(request);
            double amountreceived = 0;
            double amountdue = 0;
            for (int j = 0; j < jarr.length(); j++) {
                amountreceived = jarr.getJSONObject(j).getDouble("amountreceived");
                amountdue = jarr.getJSONObject(j).getDouble("amountdue");
                DecimalFormat twoDForm = new DecimalFormat("#.##");
                amountdue= Double.valueOf(twoDForm.format(amountdue));
                amountreceived= Double.valueOf(twoDForm.format(amountreceived));
                result += jarr.getJSONObject(j).get("monthname").toString() + ";" + amountreceived + ";" + amountdue + "\n";
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", result);
    }

    public JSONArray getMonthWiseReceivable(HttpServletRequest request) throws ServiceException, SessionExpiredException{
        boolean withinventory=StringUtil.getBoolean(request.getParameter("withinventory"));
        JSONObject jobj;
        if(withinventory){
            HashMap requestParams = getInvoiceRequestMap(request);
            KwlReturnObject result = accInvoiceDAOobj.getInvoices(requestParams);
            List list = result.getEntityList();
            jobj = getInvoiceJson(request, list);
        }else{
            HashMap requestParams = getInvoiceRequestMap(request);
            KwlReturnObject result = accInvoiceDAOobj.getBillingInvoices(requestParams);
            List list = result.getEntityList();
            jobj = getBillingInvoiceJson(request, list);
        }

        JSONArray finalJArr=new JSONArray();
        try {
            KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Calendar startFinYearCal = Calendar.getInstance();
            Calendar endFinYearCal = Calendar.getInstance();

            DateFormat sdf= new SimpleDateFormat("MMM");
            startFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.add(Calendar.YEAR,1);
            int checkMonth=0;
            JSONArray jArr = jobj.getJSONArray("data");


            for(int i=startFinYearCal.get(Calendar.MONTH);i<12;i++){
                JSONObject finalObj=new JSONObject();
                finalObj.put("month", i);
                cal.set(Calendar.MONTH, i);
                finalObj.put("monthname", sdf.format(cal.getTime()));
                finalObj.put("totalamount", 0);
                finalObj.put("amountreceived", 0);
                finalObj.put("amountdue", 0);
                finalJArr.put(finalObj);
            }
            for(int i=0;i<startFinYearCal.get(Calendar.MONTH);i++){
                JSONObject finalObj=new JSONObject();
                finalObj.put("month", i);
                cal.set(Calendar.MONTH, i);
                finalObj.put("monthname", sdf.format(cal.getTime()));
                finalObj.put("totalamount", 0);
                finalObj.put("amountreceived", 0);
                finalObj.put("amountdue", 0);
                finalJArr.put(finalObj);
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);
                Date dueDate=authHandler.getDateFormatter(request).parse(obj.getString("duedate"));
                cal.setTime(dueDate);
                if (dueDate.after(pref.getBookBeginningFrom()) && dueDate.before(endFinYearCal.getTime())){
                    checkMonth = cal.get(Calendar.MONTH);
                    int month=(checkMonth-startFinYearCal.get(Calendar.MONTH))>0?checkMonth-startFinYearCal.get(Calendar.MONTH):startFinYearCal.get(Calendar.MONTH)-checkMonth;
                    JSONObject finalObj=finalJArr.optJSONObject(month);
                    double tamount=obj.getDouble("amountinbase");
                    double damount=obj.getDouble("amountdueinbase");
                    finalObj.put("totalamount", finalObj.getDouble("totalamount")+tamount);
                    finalObj.put("amountdue", finalObj.getDouble("amountdue")+damount);
                    finalObj.put("amountreceived", finalObj.getDouble("amountreceived")+tamount-damount);
                }
            }
            jobj.put("data", finalJArr);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getMonthWiseReceivable : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getMonthWiseReceivable : "+ex.getMessage(), ex);
        }
            return finalJArr;
    }

    public ModelAndView getAgedReceivablePie(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        String result = "";
        ArrayList arr = new ArrayList();
             boolean flag=true;
        try {
            JSONObject jObjX = null;
            HashMap<String,Double> map = new HashMap<String,Double>();
            HashMap<String,String> personnameMap = new HashMap<String,String>();

            DecimalFormat twoDForm = new DecimalFormat("#.##");
            boolean withInventory = Boolean.parseBoolean(request.getParameter("withinventory"));
            HashMap requestParams = getInvoiceRequestMap(request);
            if (withInventory) {
                KwlReturnObject resultObj = accInvoiceDAOobj.getInvoices(requestParams);
                jObjX = getInvoiceJson(request, resultObj.getEntityList());
            } else {
                KwlReturnObject resultObj = accInvoiceDAOobj.getBillingInvoices(requestParams);
                jObjX = getBillingInvoiceJson(request, resultObj.getEntityList());
            }
            JSONArray jarr = jObjX.getJSONArray("data");
             String personname="";
             double amountdue=0;
             for (int j = 0; j < jarr.length(); j++) {
                flag=true;
                String personid = jarr.getJSONObject(j).getString("personid");
                for (int i=0;i<arr.size();i++) {
                    if(arr.get(i).equals(personid)){
                        flag=false;
                        break;
                    }
                }
                if(flag){
                    amountdue=0;
                    for(int k = 0;k < jarr.length();k++){
                         if(personid.equals(jarr.getJSONObject(k).getString("personid")))
                             amountdue+=jarr.getJSONObject(k).getDouble("amountdueinbase");
                    }
                    arr.add(personid);
                    amountdue= Double.valueOf(twoDForm.format(amountdue));
                    if (amountdue>0) {
                        personname= jarr.getJSONObject(j).getString("personname");
                        map.put(personid, amountdue);
                        personnameMap.put(personid, personname);
//                        result += "<slice title=\"" + personname + "\" >" + amountdue  + "</slice>";
                    }
                }
             }
             
            HashMap sorted_map = StringUtil.sortHashMapByValuesD(map);
            result += "<pie>";
            int custCnt = 0;
            double othersAmtDue = 0;
            Iterator it = sorted_map.keySet().iterator();
            while(it.hasNext()) {
                Object key = it.next();
                if(custCnt < 10) {
//                    System.out.println("key/value: " + personnameMap.get(key) + "/"+sorted_map.get(key));
                    result += "<slice title=\"" + personnameMap.get(key) + "\" >" + sorted_map.get(key) + "</slice>";
                } else {
                    othersAmtDue += Double.parseDouble(sorted_map.get(key).toString());
                }
                custCnt++;
            }
            if(othersAmtDue > 0) {
                othersAmtDue = Double.valueOf(twoDForm.format(othersAmtDue));
                result += "<slice title=\"Others\" >" + othersAmtDue + "</slice>";
            }
            result += "</pie>";
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", result);
    }
}
