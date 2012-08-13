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

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BillingGoodsReceipt;
import com.krawler.hql.accounting.BillingGoodsReceiptDetail;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
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
public class accGoodsReceiptControllerCMN extends MultiActionController implements GoodsReceiptCMNConstants, MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDebitNoteDAO accDebitNoteobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accVendorDAO accVendorDAOobj;
    private accTaxDAO accTaxObj;
    private accDiscountDAO accDiscountobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private String successView;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accGoodsReceiptCMN accGoodsReceiptCommon;
    private MessageSource messageSource;
    
    @Override
	public void setMessageSource(MessageSource msg) {
		this.messageSource = msg;
	}

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCommon) {
        this.accGoodsReceiptCommon = accGoodsReceiptCommon;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccDiscountDAO(accDiscountDAO accDiscountobj){
        this.accDiscountobj = accDiscountobj;
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

    public ModelAndView getGoodsReceipts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{

            HashMap<String, Object> requestParams = getGoodsReceiptMap(request);
            KwlReturnObject result = accGoodsReceiptobj.getGoodsReceipts(requestParams);
            List list = result.getEntityList();

            JSONArray DataJArr = getGoodsReceiptsJson(requestParams, list);
            int count = DataJArr.length();

            JSONArray pagedJson = DataJArr;
            String start = request.getParameter(START);
            String limit = request.getParameter(LIMIT);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }

            jobj.put( DATA,pagedJson);
            jobj.put( COUNT,count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accGoodsReceiptController.getGoodsReceipts : "+ex.getMessage();
        } finally {
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW,MODEL, jobj.toString());
    }

    public ModelAndView exportGoodsReceipts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = JSONVIEW_EX;
        try{
            HashMap<String, Object> requestParams = getGoodsReceiptMap(request);
            KwlReturnObject result = accGoodsReceiptobj.getGoodsReceipts(requestParams);
            List<GoodsReceipt> list = result.getEntityList();
            requestParams.put("filetype", (String)request.getParameter("filetype"));
            JSONArray DataJArr = getGoodsReceiptsJson(requestParams, list);
            jobj.put( DATA,DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = JSONVIEWEMPTY;
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view,MODEL, jobj.toString());
    }

    public JSONArray getGoodsReceiptsJson(HashMap<String, Object> request, List<GoodsReceipt> list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String companyid = (String) request.get(COMPANYID);
            String currencyid = (String) request.get(GCURRENCYID);
            DateFormat df = (DateFormat) request.get(DATEFORMAT);
            String only1099AccStr=(String)request.get(ONLY1099ACC);
            List ll=null;
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            String cashAccount = preferences.getCashAccount().getID();
            double taxPercent = 0;
            boolean belongsTo1099=false;

            boolean only1099Acc = (only1099AccStr != null?Boolean.parseBoolean(only1099AccStr):false);
            boolean ignoreZero = request.get(IGNOREZERO) != null;
            boolean onlyAmountDue = request.get(ONLYAMOUNTDUE) != null;
//            Iterator itr = list.iterator();
//            while (itr.hasNext()) {
//                GoodsReceipt gReceipt = (GoodsReceipt) itr.next();
            if(list!=null && !list.isEmpty()){
                for(GoodsReceipt gReceipt:list){
                    JournalEntry je = gReceipt.getJournalEntry();
                    JournalEntryDetail d = gReceipt.getVendorEntry();
                    currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
                    Account account = d.getAccount();
                    double amountdue= 0;
                    if(gReceipt.isIsExpenseType()){
                        ll=accGoodsReceiptCommon.getExpGRAmountDue(request,gReceipt);
                        amountdue=(Double)ll.get(1);
                        belongsTo1099=(Boolean)ll.get(3);
                    }
                    else{
                        ll=accGoodsReceiptCommon.getGRAmountDue(request,gReceipt);
                        amountdue=(Double)ll.get(1);
                        belongsTo1099=(Boolean)ll.get(3);
                    }
                    if(onlyAmountDue&&authHandler.round(amountdue,2)==0||(only1099Acc&&!belongsTo1099))
                    {//remove //belongsTo1099&&gReceipt.isIsExpenseType()\\ in case of viewing all accounts. [PS]
                        continue;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put(BILLID, gReceipt.getID());
                    obj.put(PERSONID, gReceipt.getVendor()==null?account.getID():gReceipt.getVendor().getID());
                    obj.put(PERSONEMAIL, gReceipt.getVendor()==null?"":gReceipt.getVendor().getEmail());
                    obj.put(BILLNO, gReceipt.getGoodsReceiptNumber());
                    obj.put( CURRENCYID,currencyid);
                    obj.put(CURRENCYSYMBOL, (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
                    obj.put(COMPANYADDRESS, gReceipt.getCompany().getAddress());
                    obj.put(COMPANYNAME, gReceipt.getCompany().getCompanyName());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(request, 1.0, currencyid, je.getEntryDate(),0);
                    obj.put(OLDCURRENCYRATE, bAmt.getEntityList().get(0));
                    obj.put(BILLTO, gReceipt.getBillFrom());
                    obj.put(ISEXPENSEINV, gReceipt.isIsExpenseType());
                    obj.put(SHIPTO, gReceipt.getShipFrom());
                    obj.put(JOURNALENTRYID, je.getID());
                    obj.put(EXTERNALCURRENCYRATE, je.getExternalCurrencyRate());
                    obj.put(ENTRYNO, je.getEntryNumber());
                    obj.put(DATE, df.format(je.getEntryDate()));
                    obj.put(SHIPDATE, df.format(gReceipt.getShipDate()));
                    obj.put(DUEDATE, df.format(gReceipt.getDueDate()));
                    obj.put(PERSONNAME, gReceipt.getVendor()==null?account.getName():gReceipt.getVendor().getName());
                    obj.put(MEMO, gReceipt.getMemo());
                    obj.put(TERMNAME,gReceipt.getVendor()==null?"":gReceipt.getVendor().getDebitTerm().getTermname());
                    obj.put(DELETED, gReceipt.isDeleted());
                    obj.put(TAXINCLUDED, gReceipt.getTax() == null ? false : true);
                    obj.put(TAXID, gReceipt.getTax() == null ? "" : gReceipt.getTax().getID());
                    obj.put(TAXNAME, gReceipt.getTax() == null ? "" : gReceipt.getTax().getName());
                    obj.put(TAXAMOUNT, gReceipt.getTaxEntry() == null ? 0 : gReceipt.getTaxEntry().getAmount());
                    obj.put(DISCOUNT, gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscountValue());
                    obj.put(ISPERCENTDISCOUNT, gReceipt.getDiscount()==null?false:gReceipt.getDiscount().isInPercent());
                    obj.put(DISCOUNTVAL, gReceipt.getDiscount()==null?0:gReceipt.getDiscount().getDiscount());
                    obj.put(CCConstants.JSON_costcenterid, je.getCostcenter()==null?"":je.getCostcenter().getID());
                    obj.put(CCConstants.JSON_costcenterName, je.getCostcenter()==null?"":je.getCostcenter().getName());

    //For getting tax in percent applyied on invoice [PS]
                    if (gReceipt.getTax() != null) {
                        KwlReturnObject taxresult = accTaxObj.getTaxPercent(companyid, je.getEntryDate(), gReceipt.getTax().getID());
                        taxPercent = (Double) taxresult.getEntityList().get(0);
                    }
                    obj.put( TAXPERCENT,taxPercent);

    //For getting amountdue [PS]
                    if (account.getID().equals(cashAccount)) {
                        obj.put(AMOUNTDUEINBASE, 0);
                        obj.put(AMOUNTDUE, 0);
                        obj.put(AMOUNTDUENONNEGATIVE, 0);
                        obj.put(INCASH, true);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(request, amountdue, currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                        obj.put(AMOUNTDUEINBASE, authHandler.round((Double)bAmt.getEntityList().get(0),2));
                        obj.put(AMOUNTDUE,authHandler.round(amountdue,2));
                        obj.put(AMOUNTDUENONNEGATIVE, (amountdue <= 0) ? 0 : authHandler.round(amountdue,2));
                    }

    //for getting total invoice amount [PS]
                    if(gReceipt.isIsExpenseType()){
                        obj.put(AMOUNT, (Double)ll.get(0));//for expense invoice                        
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(request, (Double)ll.get(0), currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                        obj.put(AMOUNTINBASE, bAmt.getEntityList().get(0));
                    }
                    else{
                       obj.put(AMOUNT, d.getAmount()); //actual invoice amount
                       bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(request, d.getAmount(), currencyid, je.getEntryDate(),je.getExternalCurrencyRate());
                       obj.put(AMOUNTINBASE, bAmt.getEntityList().get(0));
                    }
                    obj.put(ACCOUNTNAMES, (String)ll.get(2));
                    if (!(ignoreZero && authHandler.round(amountdue,2) <= 0)) {
                        jArr.put(obj);
                    }
                }
            }
            if(request.containsKey("filetype")){
            	if(request.get("filetype").equals("print")){
            		double total = 0;
	            	for(int i = 0; i < jArr.length(); i++)
	            		total = total + (Double)jArr.getJSONObject(i).get(AMOUNTDUEINBASE);
	            	JSONObject obj1 = new JSONObject();
	            	obj1.put(AMOUNTDUEINBASE, total);
	            	obj1.put(BILLNO, "Total Amount Due");
	            	jArr.put(obj1);
	            }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getGoodsReceiptsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getGoodsReceiptRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            JSONArray DataJArr = accGoodsReceiptCommon.getGoodsReceiptRows(request,null);
            jobj.put( DATA,DataJArr);
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
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
        return new ModelAndView(JSONVIEW,MODEL, jobj.toString());
    }

    public ModelAndView getVendorAgedPayable(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(DATEFORMAT, authHandler.getDateFormatter(request));
            requestParams.put(START, request.getParameter(START));
            requestParams.put(LIMIT, request.getParameter(LIMIT));
            requestParams.put(SS, request.getParameter(SS));
            requestParams.put(ACCID, request.getParameter(ACCID));
            requestParams.put(CASHONLY, request.getParameter(CASHONLY));
            requestParams.put(CREDITONLY, request.getParameter(CREDITONLY));
            requestParams.put(IGNOREZERO, request.getParameter(IGNOREZERO));
            requestParams.put(CURDATE, request.getParameter(CURDATE));
            requestParams.put(PERSONGROUP, request.getParameter(PERSONGROUP));
            requestParams.put(ISAGEDGRAPH, request.getParameter(ISAGEDGRAPH));
            requestParams.put(VENDORID, request.getParameter(VENDORID));
            requestParams.put(NONDELETED, request.getParameter(NONDELETED));
            requestParams.put(DURATION, request.getParameter(DURATION));
            requestParams.put(ISDISTRIBUTIVE, request.getParameter(ISDISTRIBUTIVE));
            requestParams.put(WITHINVENTORY, request.getParameter(WITHINVENTORY));

            JSONArray DataJArr = getVendorAgedPayable(request, requestParams);
            JSONArray pagedJArr = DataJArr;
            String start = request.getParameter(START);
            String limit = request.getParameter(LIMIT);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJArr = StringUtil.getPagedJSON(pagedJArr, Integer.parseInt(start), Integer.parseInt(limit));
            }

            jobj.put( DATA,pagedJArr);
            jobj.put(COUNT, DataJArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accGoodsReceiptController.getVendorAgedPayable : "+ex.getMessage();
        } finally {
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW,MODEL, jobj.toString());
    }


    public ModelAndView exportVendorAgedPayable(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = JSONVIEW_EX;
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(DATEFORMAT, authHandler.getDateFormatter(request));
            requestParams.put(START, request.getParameter(START));
            requestParams.put(LIMIT, request.getParameter(LIMIT));
            requestParams.put(SS, request.getParameter(SS));
            requestParams.put(ACCID, request.getParameter(ACCID));
            requestParams.put(CASHONLY, request.getParameter(CASHONLY));
            requestParams.put(CREDITONLY, request.getParameter(CREDITONLY));
            requestParams.put(IGNOREZERO, request.getParameter(IGNOREZERO));
            requestParams.put(CURDATE, request.getParameter(CURDATE));
            requestParams.put(PERSONGROUP, request.getParameter(PERSONGROUP));
            requestParams.put(ISAGEDGRAPH, request.getParameter(ISAGEDGRAPH));
            requestParams.put(VENDORID, request.getParameter(VENDORID));
            requestParams.put(NONDELETED, request.getParameter(NONDELETED));
            requestParams.put(DURATION, request.getParameter(DURATION));
            requestParams.put(ISDISTRIBUTIVE, request.getParameter(ISDISTRIBUTIVE));
            requestParams.put(WITHINVENTORY, request.getParameter(WITHINVENTORY));

            JSONArray DataJArr = getVendorAgedPayable(request, requestParams);
            jobj.put( DATA,DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = JSONVIEWEMPTY;
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view,MODEL, jobj.toString());
    }
    public JSONArray getVendorAgedPayable(HttpServletRequest request, HashMap requestParams) throws SessionExpiredException, ServiceException {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            DecimalFormat twoDForm = new DecimalFormat("#.##");
            String curDateString = (request.getParameter(CURDATE)!=null?request.getParameter(CURDATE):(request.getParameter("stdate")!=null?request.getParameter("stdate"):""));
            Date curDate = authHandler.getDateFormatter(request).parse(curDateString);
            int duration = request.getParameter(DURATION)==null?0:Integer.parseInt(request.getParameter(DURATION));
            boolean isdistibutive = StringUtil.getBoolean(request.getParameter(ISDISTRIBUTIVE));
            boolean withinventory=StringUtil.getBoolean(request.getParameter(WITHINVENTORY));
            double amountdue1 = 0;
            double amountdue2 = 0;
            double amountdue3 = 0;
            double amountdue4 = 0;
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            cal1.setTime(curDate);
            cal2.setTime(curDate);
            cal3.setTime(curDate);
            cal2.add(Calendar.DAY_OF_YEAR, -duration);
            cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
//             ArrayList params = new ArrayList();
//            String condition = "";
//            params.add(sessionHandlerImpl.getCompanyid(request));
//             if (!StringUtil.isNullOrEmpty(customerid)) {
//                params.add(customerid);
//                condition += " and v.account.ID=? ";
//             }
//            String q = "select ID from Vendor v where company.companyID= ?"+condition;
//            Iterator itrcust = HibernateUtil.executeQuery(session, q,params.toArray()).iterator();
            KwlReturnObject result = accVendorDAOobj.getVendorForAgedPayable(requestParams);
            Iterator itrcust = result.getEntityList().iterator();
            while (itrcust.hasNext()) {
                amountdue1 = amountdue2 = amountdue3 = amountdue4 = 0;
//                JSONObject invJObj = new JSONObject();
                String personID = null;
                String personName = null;
                String currencySymbol=null;
                String currencyid=null;
                Object venid = itrcust.next();
                requestParams.put( VENDORID,venid);
//                    invJObj = VendorHandler.getGoodsReceipts(session, request, null, null,venid.toString());
                JSONArray invjarr = new JSONArray();
                if(withinventory){
                    result = accGoodsReceiptobj.getGoodsReceipts(requestParams);
                    invjarr = getGoodsReceiptsJson(requestParams, result.getEntityList());
                }else{
                    result = accGoodsReceiptobj.getBillingGoodsReceiptsData(requestParams);
                    invjarr = getBillingGoodsReceiptsJson(requestParams, result.getEntityList(), request);
                }

//                 JSONArray invjarr = invJObj.getJSONArray("data");
                for (int i = 0; i < invjarr.length(); i++) {
                    JSONObject invobj = invjarr.getJSONObject(i);
                    personID = invobj.getString(PERSONID);
                    personName = invobj.getString(PERSONNAME);
                    currencySymbol=invobj.getString(CURRENCYSYMBOL);
                    currencyid=invobj.getString(CURRENCYID);
                    Date dueDate = null;
                    if(!StringUtil.isNullOrEmpty(invobj.getString(DUEDATE))){
                        dueDate = authHandler.getDateFormatter(request).parse(invobj.getString(DUEDATE));
                    }
                    if (isdistibutive) {
                        if (dueDate.after(cal1.getTime()) || dueDate.equals(cal1.getTime())) {
                            amountdue1 += invobj.getDouble(AMOUNTDUE);
                        } else if ((cal2.getTime().before(dueDate) || cal2.getTime().equals(dueDate)) && cal1.getTime().after(dueDate)) {
                            amountdue2 += invobj.getDouble(AMOUNTDUE);
                        } else if ((cal3.getTime().before(dueDate) || cal3.getTime().equals(dueDate)) && cal2.getTime().after(dueDate)) {
                            amountdue3 += invobj.getDouble(AMOUNTDUE);
                        } else {
                            amountdue4 += invobj.getDouble(AMOUNTDUE);
                        }
                    } else {
                        if (dueDate.after(cal1.getTime()) || dueDate.equals(cal1.getTime())) {
                            amountdue1 += invobj.getDouble(AMOUNTDUE);
                        }
                        if (dueDate.after(cal2.getTime()) || dueDate.equals(cal2.getTime())) {
                            amountdue2 += invobj.getDouble(AMOUNTDUE);
                        }
                        if (dueDate.after(cal3.getTime()) || dueDate.equals(cal3.getTime())) {
                            amountdue3 += invobj.getDouble(AMOUNTDUE);
                        }
                        amountdue4 += invobj.getDouble(AMOUNTDUE);
                    }
                }
                if (invjarr.length() > 0) {
                    jObj = new JSONObject();
                    jObj.put( PERSONID,personID);
                    jObj.put( PERSONNAME,personName);
                    jObj.put(AMOUNTDUE1, Double.valueOf(twoDForm.format(amountdue1)));
                    jObj.put(AMOUNTDUE2,Double.valueOf(twoDForm.format(amountdue2)));
                    jObj.put(AMOUNTDUE3, Double.valueOf(twoDForm.format(amountdue3)));
                    jObj.put(AMOUNTDUE4, Double.valueOf(twoDForm.format(amountdue4)));
                    jObj.put( CURRENCYSYMBOL,currencySymbol);
                    jObj.put( CURRENCYID,currencyid);
                    double amountdue= Double.valueOf(twoDForm.format(amountdue1 + amountdue2 + amountdue3 + amountdue4));
                    jObj.put(TOTAL ,amountdue);
                    jArr.put(jObj);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayable : "+ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayable : "+ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayable : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView deleteGoodsReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteGoodsReceipt(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.gr.del", null, RequestContextUtils.getLocale(request));   //"Vendor Invoice(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW,MODEL, jobj.toString());
    }

    public void deleteGoodsReceipt(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
//            ArrayList params = new ArrayList();
            JSONArray jArr = new JSONArray(request.getParameter(DATA));
//            String qMarks = "";
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
//            qMarks = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);

//            String cashAccount = ((CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request))).getCashAccount().getID();
//            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
//            String cashAccount = preferences.getCashAccount().getID();

            String greceiptid = "";
            KwlReturnObject result;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                greceiptid = URLDecoder.decode(jobj.getString(BILLID), StaticValues.ENCODING);

//                if (URLDecoder.decode(jobj.getString("personid"), StaticValues.ENCODING).equals(cashAccount)) {
//                    throw new AccountingException("Payment against the selected Purchase Receipt(s) has been given. So, it cannot be deleted");
//                }

//                query = "from DebitNoteDetail dn  where dn.goodsReceiptRow.goodsReceipt.ID in ( "+qMarks +")  and dn.debitNote.deleted=false and dn.company.companyID=?";
//                list = HibernateUtil.executeQuery(session, query, params.toArray());
                result = accDebitNoteobj.getDNDetailsFromGReceipt(greceiptid, companyid);
                List<String> list = result.getEntityList();
                if (!list.isEmpty()) {
                    throw new AccountingException("Selected record(s) is currently used in the Debit Note(s). So it cannot be deleted.");
                }

//                query = "from PaymentDetail pd  where pd.goodsReceipt.ID in ( "+qMarks +")  and pd.payment.deleted=false and pd.company.companyID=?";
//                list = HibernateUtil.executeQuery(session, query, params.toArray());
                result = accVendorPaymentobj.getPaymentsFromGReceipt(greceiptid, companyid);
                list = result.getEntityList();
                if (!list.isEmpty()) {
                    throw new AccountingException("Payment against the selected Vendor Invoice(s) has been partially/fully given. So, it cannot be deleted.");
                }

//                query = "update GoodsReceipt gr set gr.deleted=true where gr.ID in( " + qMarks + ") and gr.company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                result = accGoodsReceiptobj.deleteGoodsReceiptEntry(greceiptid, companyid);

//                query = "update JournalEntry je set je.deleted=true  where je.ID in(select gr.journalEntry.ID from GoodsReceipt gr where gr.ID in( " + qMarks + ") and gr.company.companyID=je.company.companyID) and je.company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                result = accGoodsReceiptobj.getJEFromGR(greceiptid, companyid);
                list = result.getEntityList();
//                Iterator itr = list.iterator();
//                while (itr.hasNext()) {
//                    jeid = (String) itr.next();
                if(list!=null && !list.isEmpty()){
                    for(String jeid:list){
                        result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                    }
                }
//                query = "update Discount di set di.deleted=true  where di.ID in(select gr.discount.ID from GoodsReceipt gr where gr.ID in( " + qMarks + ") and gr.company.companyID=di.company.companyID) and di.company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                result = accGoodsReceiptobj.getGRDiscount(greceiptid);
                list = result.getEntityList();
//                itr = list.iterator();
//                while (itr.hasNext()) {
//                    String discountid = (String) itr.next();
                 if(list!=null && !list.isEmpty()){
                    for(String discountid:list){
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }
                 }

//                query = "update Discount di set di.deleted=true  where di.ID in(select grd.discount.ID from GoodsReceiptDetail grd where grd.goodsReceipt.ID in( " + qMarks + ") and grd.company.companyID=di.company.companyID) and di.company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                result = accGoodsReceiptobj.getGRDetailsDiscount(greceiptid);
                list = result.getEntityList();
//                itr = list.iterator();
//                while (itr.hasNext()) {
//                    String discountid = (String) itr.next();
                if(list!=null && !list.isEmpty()){
                    for(String discountid:list){
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }
                }

//                query = "update Inventory inv set inv.deleted=true  where inv.ID in(select grd.inventory.ID from GoodsReceiptDetail grd where grd.goodsReceipt.ID in( " + qMarks + ") and grd.company.companyID=inv.company.companyID) and inv.company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                result = accGoodsReceiptobj.getGRInventory(greceiptid);
                list = result.getEntityList();
//                itr = list.iterator();
//                while (itr.hasNext()) {
//                    String inventoryid = (String) itr.next();
                  if(list!=null && !list.isEmpty()){
                    for(String inventoryid:list){
                        result = accProductObj.deleteInventoryEntry(inventoryid, companyid);
                    }
                  }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
        } catch (JSONException ex) {
            throw new AccountingException("Cannot extract data from client");
        }
    }

    public ModelAndView deleteBillingGoodsReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteBillingGoodsReceipt(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.gr.del", null, RequestContextUtils.getLocale(request));   //"Vendor Invoice(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW,MODEL, jobj.toString());
    }

    public void deleteBillingGoodsReceipt(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
//            ArrayList params = new ArrayList();
            JSONArray jArr = new JSONArray(request.getParameter(DATA));
//            String qMarks = "";
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
//            qMarks = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);

//            String cashAccount = ((CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request))).getCashAccount().getID();
//            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
//            String cashAccount = preferences.getCashAccount().getID();

            String greceiptid = "", jeid = "";
            KwlReturnObject result;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                greceiptid = URLDecoder.decode(jobj.getString(BILLID), StaticValues.ENCODING);

//                if (URLDecoder.decode(jobj.getString("personid"), StaticValues.ENCODING).equals(cashAccount)) {
//                    throw new AccountingException("Payment against the selected Purchase Receipt(s) has been given. So, it cannot be deleted");
//                }

//                query = "from BillingDebitNoteDetail dn  where dn.goodsReceiptRow.billingGoodsReceipt.ID in ( "+qMarks +")  and dn.debitNote.deleted=false and dn.company.companyID=?";
                result = accDebitNoteobj.getBDNDetailsFromGReceipt(greceiptid, companyid);
                List<BillingGoodsReceipt> list = result.getEntityList();
                if (!list.isEmpty()) {
                    throw new AccountingException("Selected record(s) is currently used in the Debit Note(s). So it cannot be deleted.");
                }

//                query = "from BillingPaymentDetail pd  where pd.billingGoodsReceipt.ID in ( "+qMarks +")  and pd.billingPayment.deleted=false and pd.company.companyID=?";
                result = accVendorPaymentobj.getBillingPaymentsFromGReceipt(greceiptid, companyid);
                list = result.getEntityList();
                if (!list.isEmpty()) {
                    throw new AccountingException("Payment against the selected Vendor Invoice(s) has been partially/fully given. So, it cannot be deleted.");
                }

//                query = "update BillingGoodsReceipt gr set gr.deleted=true where gr.ID in( " + qMarks + ") and gr.company.companyID=?";
                result = accGoodsReceiptobj.deleteBillingGoodsReceiptEntry(greceiptid, companyid);

//                query = "update JournalEntry je set je.deleted=true  where je.ID in(select gr.journalEntry.ID from BillingGoodsReceipt gr where gr.ID in( " + qMarks + ") and gr.company.companyID=je.company.companyID) and je.company.companyID=?";
                result = accGoodsReceiptobj.getFromBGR(greceiptid, companyid);
                list = result.getEntityList();
//                Iterator itr = list.iterator();
//                while (itr.hasNext()) {
//                    BillingGoodsReceipt bgr = (BillingGoodsReceipt) itr.next();
                 if(list!=null && !list.isEmpty()){
                    for(BillingGoodsReceipt bgr:list){
                        if (bgr.getJournalEntry() != null) {
                            jeid = bgr.getJournalEntry().getID();
                            result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                        }
                    }
                }

                //query = "update Discount di set di.deleted=true  where di.ID in(select gr.discount.ID from BillingGoodsReceipt gr where gr.ID in( " + qMarks + ") and gr.company.companyID=di.company.companyID) and di.company.companyID=?";
                result = accGoodsReceiptobj.getBGRDiscount(greceiptid, companyid);
                list = result.getEntityList();
//                itr = list.iterator();
//                while (itr.hasNext()) {
//                    BillingGoodsReceipt bgr = (BillingGoodsReceipt) itr.next();
                if(list!=null && !list.isEmpty()){
                    for(BillingGoodsReceipt bgr:list){
                        if (bgr.getDiscount() != null) {
                            String discountid = bgr.getDiscount().getID();
                            result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                        }
                    }
                }
//               query = "update Discount di set di.deleted=true  where di.ID in(select grd.discount.ID from BillingGoodsReceiptDetail grd where grd.billingGoodsReceipt.ID in( " + qMarks + ") and grd.company.companyID=di.company.companyID) and di.company.companyID=?";
                result = accGoodsReceiptobj.getBGRDetailsDiscount(greceiptid, companyid);
                List<BillingGoodsReceiptDetail> bGRList = result.getEntityList();
//                itr = list.iterator();
//                while (itr.hasNext()) {
//                    BillingGoodsReceiptDetail bgrd = (BillingGoodsReceiptDetail) itr.next();
                if(list!=null && !list.isEmpty()){
                    for(BillingGoodsReceiptDetail bgrd:bGRList){
                        if (bgrd.getDiscount() !=  null) {
                            String discountid = bgrd.getDiscount().getID();
                            result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
        } catch (JSONException ex) {
            throw new AccountingException("Cannot extract data from client");
        }
    }

    public ModelAndView getAccountPayableChart(HttpServletRequest request, HttpServletResponse response) {
        JSONArray jarr = new JSONArray();
        String result = "";
        try {

            jarr = getMonthWisePayable(request);
            double amountreceived = 0;
            double amountdue = 0;
            for (int j = 0; j < jarr.length(); j++) {
                amountreceived = jarr.getJSONObject(j).getDouble(AMOUNTRECEIVED);
                amountdue = jarr.getJSONObject(j).getDouble(AMOUNTDUE);
                DecimalFormat twoDForm = new DecimalFormat("#.##");
                amountdue= Double.valueOf(twoDForm.format(amountdue));
                amountreceived= Double.valueOf(twoDForm.format(amountreceived));
                result += jarr.getJSONObject(j).get(MONTHNAME).toString() + ";" + amountreceived + ";" + amountdue + "\n";
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(JSONVIEW_EX, MODEL,result);
    }

    public JSONArray getMonthWisePayable(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONArray finalJArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getGoodsReceiptMap(request);

            boolean withinventory = StringUtil.getBoolean(request.getParameter(WITHINVENTORY));
            JSONArray jArr = new JSONArray();
            KwlReturnObject result = null;
            if (withinventory) {
                result = accGoodsReceiptobj.getGoodsReceipts(requestParams);
                List list = result.getEntityList();
                List pagingList = list;
                jArr = getGoodsReceiptsJson(requestParams, pagingList);
            } else {
                result = accGoodsReceiptobj.getBillingGoodsReceiptsData(requestParams);
                List list = result.getEntityList();
                List pagingList = list;
                jArr = getBillingGoodsReceiptsJson(requestParams, pagingList, request);
            }

//            KwlReturnObject result = accGoodsReceiptobj.getGoodsReceipts(requestParams);


            KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Calendar startFinYearCal = Calendar.getInstance();
            Calendar endFinYearCal = Calendar.getInstance();

            DateFormat sdf = new SimpleDateFormat("MMM");
            startFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.add(Calendar.YEAR, 1);
            int checkMonth = 0;
            for (int i = startFinYearCal.get(Calendar.MONTH); i < 12; i++) {
                JSONObject finalObj = new JSONObject();
                finalObj.put( MONTH,i);
                cal.set(Calendar.MONTH, i);
                finalObj.put(MONTHNAME, sdf.format(cal.getTime()));
                finalObj.put(TOTALAMOUNT, 0);
                finalObj.put(AMOUNTRECEIVED, 0);
                finalObj.put(AMOUNTDUE, 0);
                finalJArr.put(finalObj);
            }
            for (int i = 0; i < startFinYearCal.get(Calendar.MONTH); i++) {
                JSONObject finalObj = new JSONObject();
                finalObj.put( MONTH,i);
                cal.set(Calendar.MONTH, i);
                finalObj.put(MONTHNAME, sdf.format(cal.getTime()));
                finalObj.put(TOTALAMOUNT, 0);
                finalObj.put(AMOUNTRECEIVED, 0);
                finalObj.put(AMOUNTDUE, 0);
                finalJArr.put(finalObj);
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);
                Date dueDate =null;
                if(!StringUtil.isNullOrEmpty(obj.getString(DUEDATE))){
                    dueDate = authHandler.getDateFormatter(request).parse(obj.getString(DUEDATE));
                }
                cal.setTime(dueDate);
                if (dueDate.after(pref.getBookBeginningFrom()) && dueDate.before(endFinYearCal.getTime())) {
                    checkMonth = cal.get(Calendar.MONTH);
                    int month = (checkMonth - startFinYearCal.get(Calendar.MONTH)) > 0 ? checkMonth - startFinYearCal.get(Calendar.MONTH) : startFinYearCal.get(Calendar.MONTH) - checkMonth;
                    JSONObject finalObj = finalJArr.optJSONObject(month);
                    double tamount = obj.getDouble(AMOUNTINBASE);
                    double damount = obj.getDouble(AMOUNTDUEINBASE);
                    finalObj.put(TOTALAMOUNT, finalObj.getDouble(TOTALAMOUNT) + tamount);
                    finalObj.put(AMOUNTDUE, finalObj.getDouble(AMOUNTDUE) + damount);
                    finalObj.put(AMOUNTRECEIVED, finalObj.getDouble(AMOUNTRECEIVED) + tamount - damount);
                }
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getMonthWisePayable : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getMonthWisePayable : " + ex.getMessage(), ex);
        }
        return finalJArr;
    }

    public HashMap<String, Object> getGoodsReceiptMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
        requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
        requestParams.put(DATEFORMAT, authHandler.getDateFormatter(request));
        requestParams.put(START, request.getParameter(START));
        requestParams.put(LIMIT, request.getParameter(LIMIT));
        requestParams.put(SS, request.getParameter(SS));
        requestParams.put(ACCID, request.getParameter(ACCID));
        requestParams.put(CASHONLY, request.getParameter(CASHONLY));
        requestParams.put(CREDITONLY, request.getParameter(CREDITONLY));
        requestParams.put(IGNOREZERO, request.getParameter(IGNOREZERO));
        requestParams.put(CURDATE, request.getParameter(CURDATE));
        requestParams.put(PERSONGROUP, request.getParameter(PERSONGROUP));
        requestParams.put(ISAGEDGRAPH, request.getParameter(ISAGEDGRAPH));
        requestParams.put(VENDORID, request.getParameter(VENDORID));
        requestParams.put(DELETED, request.getParameter(DELETED));
        requestParams.put(NONDELETED, request.getParameter(NONDELETED));
        requestParams.put(YEAR, request.getParameter(YEAR));
        requestParams.put(BILLID, request.getParameter(BILLID));
        requestParams.put(ONLYAMOUNTDUE,request.getParameter("onlyAmountDue"));
        requestParams.put(ONLY1099VEND, request.getParameter(ONLY1099VEND));
        requestParams.put(ONLY1099ACC, request.getParameter(ONLY1099ACC));
        requestParams.put(ONLYEXPENSEINV, request.getParameter(ONLYEXPENSEINV));
        requestParams.put(FOR1099REPORT, request.getParameter(FOR1099REPORT));
        requestParams.put(CCConstants.REQ_costCenterId,request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
                return requestParams;
    }

    public ModelAndView getAgedReceivablePie(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        String result = "";
        ArrayList arr = new ArrayList();
        boolean flag = true;
        try {
            HashMap<String,Double> map = new HashMap<String,Double>();
            HashMap<String,String> personnameMap = new HashMap<String,String>();
            
            DecimalFormat twoDForm = new DecimalFormat("#.##");            
            HashMap<String, Object> requestParams = getGoodsReceiptMap(request);
            boolean withinventory = StringUtil.getBoolean(request.getParameter(WITHINVENTORY));
            JSONArray jarr = new JSONArray();
            KwlReturnObject resultObj = null;
            if (withinventory) {
                resultObj = accGoodsReceiptobj.getGoodsReceipts(requestParams);
                List list = resultObj.getEntityList();
                List pagingList = list;
                jarr = getGoodsReceiptsJson(requestParams, pagingList);
            } else {
                resultObj = accGoodsReceiptobj.getBillingGoodsReceiptsData(requestParams);
                List list = resultObj.getEntityList();
                List pagingList = list;
                jarr = getBillingGoodsReceiptsJson(requestParams, pagingList, request);
            }
            String personname = "";
            double amountdue = 0;            
            for (int j = 0; j < jarr.length(); j++) {
                flag = true;
                String personid = jarr.getJSONObject(j).getString(PERSONID);
                for (int i = 0; i < arr.size(); i++) {
                    if (arr.get(i).equals(personid)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    amountdue = 0;
                    for (int k = 0; k < jarr.length(); k++) {
                        if (personid.equals(jarr.getJSONObject(k).getString(PERSONID))) {
                            amountdue += jarr.getJSONObject(k).getDouble(AMOUNTDUEINBASE);
                        }
                    }
                    arr.add(personid);
                    if (amountdue > 0) {
                        personname = jarr.getJSONObject(j).getString(PERSONNAME);
                        amountdue= Double.valueOf(twoDForm.format(amountdue));
                        map.put(personid, amountdue);
                        personnameMap.put(personid, personname);
//                        result += "<slice title=\"" + personname + "\" >" + amountdue + "</slice>";
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
        } catch (JSONException ex) {
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(JSONVIEW_EX, MODEL,result);
    }

    public ModelAndView exportGoodsReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = JSONVIEW_EX;
        try{
            HashMap<String, Object> requestParams = getGoodsReceiptMap(request);
            KwlReturnObject result = accGoodsReceiptobj.getGoodsReceipts(requestParams);
            JSONArray DataJArr = getGoodsReceiptsJson(requestParams, result.getEntityList());
            jobj.put( DATA,DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = JSONVIEWEMPTY;
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view,MODEL, jobj.toString());
    }

    public ModelAndView getBillingGoodsReceipts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{

            HashMap<String, Object> requestParams = getBillingGoodsReceiptMap(request);
            KwlReturnObject result = accGoodsReceiptobj.getBillingGoodsReceiptsData(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            List pagingList = list;
            String start = request.getParameter(START);
            String limit = request.getParameter(LIMIT);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagingList = StringUtil.getPagedList(list, Integer.parseInt(start), Integer.parseInt(limit));
            }

            JSONArray DataJArr = getBillingGoodsReceiptsJson(requestParams, pagingList, request);
            jobj.put( DATA,DataJArr);
            jobj.put( COUNT,count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accGoodsReceiptController.getGoodsReceipts : "+ex.getMessage();
        } finally {
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW,MODEL, jobj.toString());
    }

    public ModelAndView exportBillingGoodsReceipts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = JSONVIEW_EX;
        try{
             HashMap<String, Object> requestParams = getBillingGoodsReceiptMap(request);
            KwlReturnObject result = accGoodsReceiptobj.getBillingGoodsReceiptsData(requestParams);
            int count = result.getRecordTotalCount();
            List list = result.getEntityList();
            JSONArray DataJArr = getBillingGoodsReceiptsJson(requestParams, list, request);
            jobj.put( DATA,DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = JSONVIEWEMPTY;
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view,MODEL, jobj.toString());
    }
    public HashMap<String, Object> getBillingGoodsReceiptMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
        requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
        requestParams.put(DATEFORMAT, authHandler.getDateFormatter(request));
        requestParams.put(START, request.getParameter(START));
        requestParams.put(LIMIT, request.getParameter(LIMIT));
        requestParams.put(SS, request.getParameter(SS));
        requestParams.put(ACCID, request.getParameter(ACCID));
        requestParams.put(CASHONLY, request.getParameter(CASHONLY));
        requestParams.put(CREDITONLY, request.getParameter(CREDITONLY));
        requestParams.put(IGNOREZERO, request.getParameter(IGNOREZERO));
        requestParams.put(CURDATE, request.getParameter(CURDATE));
        requestParams.put(DELETED, request.getParameter(DELETED));
        requestParams.put(NONDELETED, request.getParameter(NONDELETED));
        requestParams.put(BILLID, request.getParameter(BILLID));
        requestParams.put(VENDORID, request.getParameter(VENDORID));
        requestParams.put(CCConstants.REQ_costCenterId,request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
        return requestParams;
    }

    public JSONArray getBillingGoodsReceiptsJson(HashMap<String, Object> requestParam, List<BillingGoodsReceipt> list, HttpServletRequest request) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String companyid = (String) requestParam.get(COMPANYID);
            String currencyid = (String) requestParam.get(GCURRENCYID);
            DateFormat df = (DateFormat) requestParam.get(DATEFORMAT);
//
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
//
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            String cashAccount = preferences.getCashAccount().getID();
//
//            double taxPercent = 0;
            boolean ignoreZero = requestParam.get(IGNOREZERO) != null;
            boolean onlyAmountDue = requestParam.get(ONLYAMOUNTDUE) != null;
//            Iterator itr = list.iterator();
//            while (itr.hasNext()) {
//                BillingGoodsReceipt invoice = (BillingGoodsReceipt) itr.next();
            if(list!=null && !list.isEmpty()){
                for(BillingGoodsReceipt invoice:list){
                    double taxPercent = 0;
                    JournalEntry je = invoice.getJournalEntry();
                    JournalEntryDetail d = invoice.getVendorEntry();
                    Account account = d.getAccount();
                    double amount = 0, ramount = 0;
                    Iterator itrBir = accGoodsReceiptCommon.applyBillingDebitNotes(requestParam, invoice).values().iterator();
                    while (itrBir.hasNext()) {
                        Object[] temp = (Object[]) itrBir.next();
                        amount += (Double) temp[0]-(Double) temp[2];
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
                    KwlReturnObject brdAmt = accGoodsReceiptobj.getAmtromBPD(invoice.getID());
                    List l = brdAmt.getEntityList();
    //                String q = "select sum(amount) from BillingPaymentDetail pd where pd.billingPayment.deleted=false and pd.billingGoodsReceipt.ID=? group by pd.billingGoodsReceipt";
    //                List l = HibernateUtil.executeQuery(session, q, invoice.getID());
                    ramount = (l.isEmpty() ? 0 : (Double) l.get(0));
                    currencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
                    double amountdue=amount - ramount;

                    if(onlyAmountDue&&authHandler.round(amountdue,2)==0)
                        continue;
                    JSONObject obj = new JSONObject();
                    obj.put(BILLID, invoice.getID());
                    obj.put(PERSONID,invoice.getVendor()==null?account.getID():invoice.getVendor().getID());// account.getID());
                    obj.put(PERSONEMAIL, invoice.getVendor()==null?"":invoice.getVendor().getEmail());
                    obj.put("crdraccid",invoice.getDebtorEntry().getAccount().getID());// account.getID());
                    obj.put(BILLNO, invoice.getBillingGoodsReceiptNumber());
                    obj.put(CURRENCYID,currencyid);
                    obj.put(CURRENCYSYMBOL,(invoice.getCurrency()==null?"": invoice.getCurrency().getSymbol()));
                    obj.put(COMPANYADDRESS, invoice.getCompany().getAddress());
                    obj.put(OLDCURRENCYRATE, accCurrencyDAOobj.getBaseToCurrencyAmount(requestParam,1.0,currencyid,je.getEntryDate(),je.getExternalCurrencyRate()).getEntityList().get(0));
                    obj.put(BILLTO, invoice.getBillFrom());                    
                    obj.put(SHIPTO, invoice.getShipFrom());
                    obj.put(JOURNALENTRYID, je.getID());
                    obj.put(ENTRYNO, je.getEntryNumber());
                    obj.put("externalcurrencyrate", je.getExternalCurrencyRate());
                    obj.put(DATE, authHandler.getDateFormatter(request).format(je.getEntryDate()));
                    obj.put(SHIPDATE, authHandler.getDateFormatter(request).format(invoice.getShipDate()));
                    obj.put(DUEDATE, authHandler.getDateFormatter(request).format(invoice.getDueDate()));
                    obj.put(PERSONNAME, invoice.getVendor()==null?account.getName():invoice.getVendor().getName());
                    obj.put(TAXAMOUNT, invoice.getTaxEntry()==null?0:invoice.getTaxEntry().getAmount());
                    obj.put(TAXINCLUDED, invoice.getTax() == null ? false : true);
                    obj.put(TAXID, invoice.getTax() == null ? "" : invoice.getTax().getID());
                    obj.put(TAXNAME, invoice.getTax() == null ? "" : invoice.getTax().getName());
                    obj.put(MEMO, invoice.getMemo());
                    obj.put(DELETED, invoice.isDeleted());
                    obj.put(DISCOUNT, invoice.getDiscount()==null?0:invoice.getDiscount().getDiscountValue());
                    obj.put(ISPERCENTDISCOUNT, invoice.getDiscount()==null?false:invoice.getDiscount().isInPercent());
                    obj.put(DISCOUNTVAL, invoice.getDiscount()==null?0:invoice.getDiscount().getDiscount());
                    obj.put(CCConstants.JSON_costcenterid, je.getCostcenter()==null?"":je.getCostcenter().getID());
                    obj.put(CCConstants.JSON_costcenterName, je.getCostcenter()==null?"":je.getCostcenter().getName());
                    if(account.getID().equals(cashAccount)){
                          obj.put(AMOUNTDUE,0);
                          obj.put(INCASH,true);
                          obj.put(AMOUNTDUEINBASE, 0);

                    }
                    else{
                       // obj.put("amountdue", amountdue);
                        obj.put(AMOUNTDUE, authHandler.round(amountdue,2));
                        obj.put(AMOUNTDUEINBASE, authHandler.round((Double)accCurrencyDAOobj.getCurrencyToBaseAmount(requestParam,amountdue,currencyid,je.getEntryDate(),je.getExternalCurrencyRate()).getEntityList().get(0),2));
                    }
                    obj.put(AMOUNTDUENONNEGATIVE, authHandler.round(amountdue,2));
                    obj.put(AMOUNT, d.getAmount());
                    obj.put(AMOUNTINBASE, authHandler.round((Double)accCurrencyDAOobj.getCurrencyToBaseAmount(requestParam,d.getAmount(),currencyid,je.getEntryDate(),je.getExternalCurrencyRate()).getEntityList().get(0),2));
                    if (!(ignoreZero && authHandler.round(amountdue,2) <= 0)) {
                        jArr.put(obj);
                    }
                    if (invoice.getTax() != null) {
    //                    taxPercent = CompanyHandler.getTaxPercent(session, request, je.getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, je.getEntryDate(), invoice.getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put( TAXPERCENT,taxPercent);  //tax in percent applyind on invoice


                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getBillingGoodsReceiptsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getBillingGoodsReceiptRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            JSONArray DataJArr = accGoodsReceiptCommon.getBillingGoodsReceiptRows(request);
            jobj.put( DATA,DataJArr);
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
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
        return new ModelAndView(JSONVIEW,MODEL, jobj.toString());
    }

}
