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
package com.krawler.spring.accounting.vendorpayment;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BillingGoodsReceipt;
import com.krawler.hql.accounting.BillingPayment;
import com.krawler.hql.accounting.BillingPaymentDetail;
import com.krawler.hql.accounting.Card;
import com.krawler.hql.accounting.Cheque;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.PayDetail;
import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.PaymentDetail;
import com.krawler.hql.accounting.PaymentMethod;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

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
public class accVendorPaymentController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accPaymentDAO accPaymentDAOobj;
        private accTaxDAO accTaxObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private String successView;
    private MessageSource messageSource;

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
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
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
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
    public String getSuccessView() {
        return successView;
    }
        public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public ModelAndView savePayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
            List li = savePayment(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] id = (String[]) li.get(0);
            issuccess = true;
            msg = messageSource.getMessage("acc.pay.save", null, RequestContextUtils.getLocale(request));   //"Payment information has been saved successfully";
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteJEArray(id[0],companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteChequeOrCard(id[1],companyid);
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List savePayment(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        KwlReturnObject result;
        List list = new ArrayList();       
        Payment payment = null;
        String oldjeid=null;
        String Cardid=null;
        List ll = new ArrayList();
        try {
            Account dipositTo = null;
            //            Payment payment = new Payment();
            double amount = 0;
            double amountDiff = 0;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalCurrencyRate=StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateFormatter(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String drAccDetails = request.getParameter("detail");
            boolean isMultiDebit=StringUtil.getBoolean(request.getParameter("ismultidebit"));
            String entryNumber=request.getParameter("no");
            String receiptid =request.getParameter("billid");
            String jeid=null;            
            String payDetailID=null;
            String jeentryNumber=null;
            boolean jeautogenflag = false;
            HashMap paymenthm = new HashMap();

            if (!StringUtil.isNullOrEmpty(receiptid)){
                
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Payment.class.getName(), receiptid);
                payment=(Payment)receiptObj.getEntityList().get(0);
                jeentryNumber=payment.getJournalEntry().getEntryNumber();
                oldjeid=payment.getJournalEntry().getID();
                jeautogenflag = payment.getJournalEntry().isAutoGenerated();
                if(payment.getPayDetail()!=null){
                     payDetailID=payment.getPayDetail().getID();
                     if(payment.getPayDetail().getCard()!=null)
                            Cardid=payment.getPayDetail().getCard().getID();
                       // accPaymentDAOobj.deleteCard(receipt.getPayDetail().getCard().getID(),companyid);
                     if(payment.getPayDetail().getCheque()!=null)
                         Cardid=payment.getPayDetail().getCheque().getID();
                        //accPaymentDAOobj.deleteCheque(receipt.getPayDetail().getCheque().getID(),companyid);
                }
                 result = accVendorPaymentobj.deletePaymentsDetails(receiptid,companyid);
                

            }else{
//              String q = "from Payment where paymentNumber=? and company.companyID=?";
//            if (!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty()) {
//                throw new AccountingException("Payment number '" + entryNumber + "' already exists.");
            result = accVendorPaymentobj.getPaymentFromNo(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if(count > 0){
                throw new AccountingException("Payment number '" + entryNumber + "' already exists.");
            }

            String nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_PAYMENT);
//            payment.setPaymentNumber(request.getParameter("no"));
//            payment.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_PAYMENT).equals(entryNumber));
            paymenthm.put("entrynumber", entryNumber);

            paymenthm.put("autogenerated", entryNumber.equals(nextAutoNo));

            }


//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            
//            KWLCurrency currency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid=(request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            paymenthm.put("currencyid", currencyid);
            paymenthm.put("externalCurrencyRate", externalCurrencyRate);
//            PaymentMethod payMethod = (PaymentMethod) session.get(PaymentMethod.class, request.getParameter("pmtmethod"));
            result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap pdetailhm = new HashMap();
            pdetailhm.put("paymethodid", payMethod.getID());
            pdetailhm.put("companyid", companyid);
            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {
//                PayDetail pdetail = new PayDetail();
//                pdetail.setPaymentMethod(payMethod);
//                pdetail.setCompany(company);

                JSONObject obj = new JSONObject(request.getParameter("paydetail"));
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
//                    Cheque cheque = new Cheque();
//                    cheque.setChequeNo(obj.getString("chequeno"));
//                    cheque.setDescription(obj.getString("description"));
//                    cheque.setBankName(obj.getString("bankname"));
//                    session.save(cheque);
                    HashMap chequehm = new HashMap();
                    chequehm.put("chequeno", obj.getString("chequeno"));
                    chequehm.put("description", URLDecoder.decode(obj.getString("description"),StaticValues.ENCODING));
                    chequehm.put("bankname", obj.getString("bankname"));
                    KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                    Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
//                    pdetail.setCheque(cheque);
                    pdetailhm.put("chequeid", cheque.getID());
                } else if (payMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
//                    Card card = new Card();
//                    card.setCardNo(obj.getString("cardno"));
//                    card.setCardHolder(obj.getString("nameoncard"));
//                    card.setExpiryDate(obj.getString("expirydate"));
//                    card.setCardType(obj.getString("cardtype"));
//                    card.setRefNo(obj.getString("refno"));
//                    session.save(card);
                    HashMap cardhm = new HashMap();
                    cardhm.put("cardno", obj.getString("cardno"));
                    cardhm.put("nameoncard", obj.getString("nameoncard"));
                    cardhm.put("expirydate", df.parse(obj.getString("expirydate")));
                    cardhm.put("cardtype", obj.getString("cardtype"));
                    cardhm.put("refno", obj.getString("refno"));
                    KwlReturnObject cdresult = accPaymentDAOobj.addCard(cardhm);
                    Card card = (Card) cdresult.getEntityList().get(0);
//                    pdetail.setCard(card);
                    pdetailhm.put("cardid", card.getID());
                }
//                session.save(pdetail);
                
            }
            KwlReturnObject pdresult=null;
                if (!StringUtil.isNullOrEmpty(receiptid)&&!StringUtil.isNullOrEmpty(payDetailID))
                    pdetailhm.put("paydetailid", payDetailID);
                    pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);

                PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
//                payment.setPayDetail(pdetail);
                paymenthm.put("paydetailsid", pdetail.getID());
//            payment.setMemo(request.getParameter("memo"));
//            payment.setCurrency((KWLCurrency) session.get(KWLCurrency.class, currencyid));
//            payment.setDeleted(false);
//            payment.setCompany(company);
            paymenthm.put("memo", request.getParameter("memo"));          
            paymenthm.put("companyid", companyid);
            
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            Map<String,Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            Set jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail=  request.getParameter("detail");
             JSONArray jArr=new JSONArray();
            if(!StringUtil.isNullOrEmpty(detail))
                    jArr = new JSONArray(detail);
            if (jArr.length() > 0&&!isMultiDebit) {
//                amount = savePaymentRows(session, request, payment, company, jArr);
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }
                amountDiff = oldPaymentRowsAmount(request, jArr, currencyid,externalCurrencyRate);
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.common.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
//                    JournalEntryDetail jed = new JournalEntryDetail();
//                    jed.setCompany(company);
//                    jed.setAmount(amountDiff);
//                    jed.setAccount(preferences.getForeignexchange());
//                    jed.setDebit(false);
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size()+1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", amountDiff);
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }

            } else {
                amount = Double.parseDouble(request.getParameter("amount"));
            }
//            JournalEntryDetail jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(amount + amountDiff);
//            jed.setAccount(((Account) session.get(Account.class, request.getParameter("accid"))));
//            jed.setDebit(true);
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed=null;
            if(isMultiDebit){
                JSONArray drAccArr = new JSONArray(drAccDetails);
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size()+1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", Double.parseDouble(jobj.getString("dramount")));
                    jedjson.put("accountid", jobj.getString("accountid"));
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
        //           KwlReturnObject jedresult = (KwlReturnObject) accountingHandlerDAOobj.invokeMethod("JournalEntry", "addJournalEntryDetails", new Object[]{jedjson});
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }
            else{
                jedjson = new JSONObject();
                jedjson.put("companyid", companyid);
                jedjson.put("srno", jedetails.size()+1);
                jedjson.put("amount", amount+amountDiff);
                jedjson.put("accountid", request.getParameter("accid"));
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
    //           KwlReturnObject jedresult = (KwlReturnObject) accountingHandlerDAOobj.invokeMethod("JournalEntry", "addJournalEntryDetails", new Object[]{jedjson});
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }
//            jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(amount);
//            jed.setAccount(dipositTo);
//            jed.setDebit(false);
            jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", companyid);
            jedjson.put("amount", amount);
            jedjson.put("accountid", dipositTo.getID());
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//            jedresult = (KwlReturnObject) accountingHandlerDAOobj.invokeMethod("JournalEntry", "addJournalEntryDetails", new Object[]{jedjson});

            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
//            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
//                    request.getParameter("memo"), "JE" + payment.getPaymentNumber(), currencyid, hs, request);
//            jed.setJournalEntry(journalEntry);
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
//            jeresult = (KwlReturnObject) accountingHandlerDAOobj.invokeMethod("Account", "updateJournalEntry", new Object[]{jejson, jedetails});
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
//            payment.setJournalEntry(journalEntry);
            paymenthm.put("journalentryid", journalEntry.getID());
            if(payment!=null)
                paymenthm.put("paymentid", payment.getID());

            result = accVendorPaymentobj.savePayment(paymenthm);
            payment = (Payment) result.getEntityList().get(0);

            //Save Payment Details
            
                HashSet payDetails = savePaymentRows(payment, company, jArr,isMultiDebit);
                paymenthm.put("paymentid", payment.getID());
                paymenthm.put("pdetails", payDetails);
            
            result = accVendorPaymentobj.savePayment(paymenthm);
            payment = (Payment) result.getEntityList().get(0);

            list.add(payment);
//            issuccess = true;
//            msg = "Payment information has been saved successfully";
//            session.saveOrUpdate(payment);
        //ProfileHandler.insertAuditLog(session, AuditAction.RECEIPT_ADDED, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) +" created new receipt "+receipt.getReceiptNumber()+" for invoice "+receipt.getInvoice().getInvoiceNumber(), request);
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("savePayment : "+ex.getMessage(), ex);
//            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("savePayment : "+ex.getMessage(), ex);
//            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            msg = ex.getMessage();
//            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
       ll.add(new String[]{oldjeid,Cardid});
       return  (ArrayList) ll;
        //return new KwlReturnObject(issuccess, msg, null, list, list.size());
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
   public void deleteChequeOrCard(String id,String companyid) throws ServiceException, AccountingException, SessionExpiredException {
      try{
          if(id!=null){
               accPaymentDAOobj.deleteCard(id,companyid);
               accPaymentDAOobj.deleteCheque(id,companyid);
          }
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

      public ModelAndView saveBillingPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
              List li =saveBillingPayment(request);
                          String companyid = sessionHandlerImpl.getCompanyid(request);
             String[] id = (String[]) li.get(0);
            issuccess = true;
            msg = messageSource.getMessage("acc.pay.billsave", null, RequestContextUtils.getLocale(request));   //"Billing Payment information has been saved successfully";
//            msg = result.getMsg();
             txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteJEArray(id[0],companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteChequeOrCard(id[1],companyid);
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public List saveBillingPayment(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        List list = new ArrayList();
        KwlReturnObject result;
        BillingPayment payment = null;
                String oldjeid=null;
        String Cardid=null;
        List ll = new ArrayList();
        try {
              Account dipositTo = null;
//            BillingPayment payment = new BillingPayment();
            double amount = 0;
            double amountDiff = 0;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalCurrencyRate =StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateFormatter(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String drAccDetails = request.getParameter("detail");
            boolean isMultiDebit=StringUtil.getBoolean(request.getParameter("ismultidebit"));
             String entryNumber=request.getParameter("no");
            String receiptid =request.getParameter("billid");
            String jeid=null;
            String payDetailID=null;
            String jeentryNumber=null;
            boolean jeautogenflag = false;
            HashMap billingPaymenthm = new HashMap();
            if (!StringUtil.isNullOrEmpty(receiptid)){
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(BillingPayment.class.getName(), receiptid);
                payment=(BillingPayment)receiptObj.getEntityList().get(0);
                jeentryNumber=payment.getJournalEntry().getEntryNumber();
                oldjeid=payment.getJournalEntry().getID();
                jeautogenflag = payment.getJournalEntry().isAutoGenerated();
                if(payment.getPayDetail()!=null){
                     payDetailID=payment.getPayDetail().getID();
                     if(payment.getPayDetail().getCard()!=null)
                            Cardid=payment.getPayDetail().getCard().getID();
                       // accPaymentDAOobj.deleteCard(receipt.getPayDetail().getCard().getID(),companyid);
                     if(payment.getPayDetail().getCheque()!=null)
                         Cardid=payment.getPayDetail().getCheque().getID();
                        //accPaymentDAOobj.deleteCheque(receipt.getPayDetail().getCheque().getID(),companyid);
                }
                result = accVendorPaymentobj.deleteBillingPaymentsDetails(receiptid,companyid);
            }else{
//              String q = "from Payment where paymentNumber=? and company.companyID=?";
//            if (!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty()) {
//                throw new AccountingException("Payment number '" + entryNumber + "' already exists.");
            result = accVendorPaymentobj.getBillingPaymentFromNo(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if(count > 0){
                throw new AccountingException("Payment number '" + entryNumber + "' already exists.");
            }
            String nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGPAYMENT);
//            payment.setPaymentNumber(request.getParameter("no"));
//            payment.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_PAYMENT).equals(entryNumber));
//            payment.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_BILLINGPAYMENT).equals(entryNumber));
            billingPaymenthm.put("entrynumber", entryNumber);
            billingPaymenthm.put("autogenerated", entryNumber.equals(nextAutoNo));
            }

//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            //            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            

            billingPaymenthm.put("currencyid", currencyid);

//            payment.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
//            PaymentMethod payMethod = (PaymentMethod) session.get(PaymentMethod.class, request.getParameter("pmtmethod"));
            result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap bpdetailhm = new HashMap();
//                pdetail.setPaymentMethod(payMethod);
                bpdetailhm.put("paymethodid", payMethod.getID());
//                pdetail.setCompany(company);
                bpdetailhm.put("companyid", companyid);
            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {
//                PayDetail pdetail = new PayDetail();
                JSONObject obj = new JSONObject(request.getParameter("paydetail"));
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
//                    Cheque cheque = new Cheque();
//                    cheque.setChequeNo(obj.getString("chequeno"));
//                    cheque.setDescription(obj.getString("description"));
//                    cheque.setBankName(obj.getString("bankname"));

                    HashMap chequehm = new HashMap();
                    chequehm.put("chequeno", obj.getString("chequeno"));
                    chequehm.put("description", obj.getString("description"));
                    chequehm.put("bankname", obj.getString("bankname"));
                    chequehm.put("bankmasteritemid", obj.getString("bankmasteritemid"));
                    KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                    Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
//                    session.save(cheque);
//                    pdetail.setCheque(cheque);
                    bpdetailhm.put("chequeid", cheque.getID());
                } else if (payMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
//                    Card card = new Card();
//                    card.setCardNo(obj.getString("cardno"));
//                    card.setCardHolder(obj.getString("nameoncard"));
//                    card.setExpiryDate(obj.getString("expirydate"));
//                    card.setCardType(obj.getString("cardtype"));
//                    card.setRefNo(obj.getString("refno"));
//                    session.save(card);
//                    pdetail.setCard(card);

                    HashMap cardhm = new HashMap();
                    cardhm.put("cardno", obj.getString("cardno"));
                    cardhm.put("nameoncard", obj.getString("nameoncard"));
                    cardhm.put("expirydate", obj.getString("expirydate"));
                    cardhm.put("cardtype", obj.getString("cardtype"));
                    cardhm.put("refno", obj.getString("refno"));
                    KwlReturnObject cdresult = accPaymentDAOobj.addCard(cardhm);
                    Card card = (Card) cdresult.getEntityList().get(0);
//                    pdetail.setCard(card);
                    bpdetailhm.put("cardid", card.getID());
                }
//                session.save(pdetail);
              
            }
                  KwlReturnObject pdresult=null;
                if (!StringUtil.isNullOrEmpty(receiptid)&&!StringUtil.isNullOrEmpty(payDetailID))
                    bpdetailhm.put("paydetailid", payDetailID);
                    pdresult = accPaymentDAOobj.addPayDetail(bpdetailhm);

                PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
//                payment.setPayDetail(pdetail);
                billingPaymenthm.put("paydetailsid", pdetail.getID());
//            payment.setMemo(request.getParameter("memo"));
//            payment.setDeleted(false);
//            payment.setCompany(company);
            billingPaymenthm.put("memo", request.getParameter("memo"));
            billingPaymenthm.put("companyid", companyid);
            billingPaymenthm.put("deleted", false);
            
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            HashMap<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            HashSet jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
//            KwlReturnObject jeresult = (KwlReturnObject) accountingHandlerDAOobj.invokeMethod("JournalEntry", "addJournalEntry", new Object[]{jejson, jedetails});
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail=  request.getParameter("detail");
             JSONArray jArr=new JSONArray();
              if(!StringUtil.isNullOrEmpty(detail))
                    jArr = new JSONArray(detail);
            HashMap<String, Object> jParam = new HashMap();
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;

            if (jArr.length() > 0&&!isMultiDebit) {
//                amount = saveBillingPaymentRows(session, request, payment, company, jArr);
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }
                amountDiff = oldBillingPaymentRowsAmount(request, jArr, currencyid, externalCurrencyRate);
//                hs = new HashSet();
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.pay.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
//                    JournalEntryDetail jed = new JournalEntryDetail();
//                    jed.setCompany(company);
//                    jed.setAmount(amountDiff);
//                    jed.setAccount(preferences.getForeignexchange());
//                    jed.setDebit(false);

                    jParam.put("srno", jedetails.size()+1);
                    jParam.put("companyid", companyid);
                    jParam.put("amount", amountDiff);
                    jParam.put("accountid", preferences.getForeignexchange().getID());
                    jParam.put("debit", false);
                    jParam.put("jeid", jeid);
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(jParam);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            } else {
                amount = Double.parseDouble(request.getParameter("amount"));
            }
//            JournalEntryDetail jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(amount+amountDiff);
//            jed.setAccount(((Account) session.get(Account.class, request.getParameter("accid"))));
//            jed.setDebit(true);
//            hs.add(jed);

//            JSONObject jedjson = new JSONObject();
            if(isMultiDebit){
                JSONArray drAccArr = new JSONArray(drAccDetails);
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    jParam = new HashMap();
                    jParam.put("srno", jedetails.size()+1);
                    jParam.put("companyid", companyid);
                    jParam.put("amount", Double.parseDouble(jobj.getString("dramount")));
                    jParam.put("accountid", jobj.getString("accountid"));
                    jParam.put("debit", true);
                    jParam.put("jeid", jeid);
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(jParam);
        //           KwlReturnObject jedresult = (KwlReturnObject) accountingHandlerDAOobj.invokeMethod("JournalEntry", "addJournalEntryDetails", new Object[]{jedjson});
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }
            else{

                jParam = new HashMap();
                jParam.put("srno", jedetails.size()+1);
                jParam.put("companyid", companyid);
                jParam.put("amount", amount + amountDiff);
                jParam.put("accountid", request.getParameter("accid"));
                jParam.put("debit", true);
                jParam.put("jeid", jeid);
                jedresult = accJournalEntryobj.updateJournalEntryDetails(jParam);
    //           KwlReturnObject jedresult = (KwlReturnObject) accountingHandlerDAOobj.invokeMethod("JournalEntry", "addJournalEntryDetails", new Object[]{jedjson});
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }



//            jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(amount);
//            jed.setAccount(dipositTo);
//            jed.setDebit(false);
//            hs.add(jed);

            jParam = new HashMap();
            jParam.put("srno", jedetails.size()+1);
            jParam.put("companyid", companyid);
            jParam.put("amount", amount);
            jParam.put("accountid", dipositTo.getID());
            jParam.put("debit", false);
            jParam.put("jeid", jeid);
            jedresult = accJournalEntryobj.updateJournalEntryDetails(jParam);


            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);

//            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
//                    request.getParameter("memo"), "JE" + payment.getBillingPaymentNumber(),currencyid, externalCurrencyRate, hs,request);
//            jed.setJournalEntry(journalEntry);
//            payment.setJournalEntry(journalEntry);
//            session.saveOrUpdate(payment);
//            ProfileHandler.insertAuditLog(session, AuditAction.RECEIPT_ADDED, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) +" created new receipt ", request);

            //            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
//                    request.getParameter("memo"), "JE" + payment.getPaymentNumber(), currencyid, hs, request);
//            jed.setJournalEntry(journalEntry);
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
//            jeresult = (KwlReturnObject) accountingHandlerDAOobj.invokeMethod("Account", "updateJournalEntry", new Object[]{jejson, jedetails});
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
//            payment.setJournalEntry(journalEntry);
            billingPaymenthm.put("journalentryid", journalEntry.getID());
            if(payment!=null)
                billingPaymenthm.put("billingPaymentid", payment.getID());
            result = accVendorPaymentobj.saveBillingPayment(billingPaymenthm);
            payment = (BillingPayment) result.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(receiptid))
                billingPaymenthm.put("billingpaymentid", payment.getID());
            //Save Payment Details
            HashSet payDetails = saveBillingPaymentRows(payment, company, jArr,isMultiDebit);
            billingPaymenthm.put("billingPaymentid", payment.getID());
            billingPaymenthm.put("externalCurrencyRate", externalCurrencyRate);
            billingPaymenthm.put("bpdetails", payDetails);

            result = accVendorPaymentobj.saveBillingPayment(billingPaymenthm);
            payment = (BillingPayment) result.getEntityList().get(0);

            list.add(payment);
//            issuccess = true;
//            msg = "Payment information has been saved successfully";
//            session.saveOrUpdate(payment);
        //ProfileHandler.insertAuditLog(session, AuditAction.RECEIPT_ADDED, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) +" created new receipt "+receipt.getReceiptNumber()+" for invoice "+receipt.getInvoice().getInvoiceNumber(), request);


        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        //return new KwlReturnObject(issuccess, msg, null, list, list.size());
         ll.add(new String[]{oldjeid,Cardid});
       return  (ArrayList) ll;
    }

    public HashSet savePaymentRows(Payment payment, Company company, JSONArray jArr,boolean isMultiDebit) throws JSONException, ServiceException {
        HashSet pdetails = new HashSet();
        if(!isMultiDebit){
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                PaymentDetail pd = new PaymentDetail();
                pd.setSrno(i+1);
                pd.setAmount(jobj.getDouble("payment"));
                pd.setCompany(company);
    //            pd.setGoodsReceipt((GoodsReceipt) session.get(GoodsReceipt.class, jobj.getString("billid")));
                KwlReturnObject result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("billid"));
                pd.setGoodsReceipt((GoodsReceipt) result.getEntityList().get(0));
                pd.setPayment(payment);
                pdetails.add(pd);
            }
        }
        return pdetails;
    }

     public HashSet saveBillingPaymentRows(BillingPayment payment, Company company, JSONArray jArr,boolean isMultiDebit) throws JSONException, ServiceException {
        HashSet hs = new HashSet();
        if(!isMultiDebit){
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            BillingPaymentDetail rd = new BillingPaymentDetail();
            rd.setSrno(i+1);
            rd.setAmount(jobj.getDouble("payment"));
            rd.setCompany(company);
//            rd.setBillingGoodsReceipt((BillingGoodsReceipt) session.get(BillingGoodsReceipt.class, jobj.getString("billid")));
            KwlReturnObject result = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), jobj.getString("billid"));
            rd.setBillingGoodsReceipt((BillingGoodsReceipt) result.getEntityList().get(0));
            rd.setBillingPayment(payment);
//            amount += jobj.getDouble("payment");
            hs.add(rd);
        }
        }
//        payment.setRows(hs);
        return hs;
    }

    public double oldPaymentRowsAmount(HttpServletRequest request, JSONArray jArr, String currencyid,double externalCurrencyRate) throws ServiceException, SessionExpiredException{
        double ratio = 0;
        double amount = 0;
        try {            
            String basecurrency=sessionHandlerImpl.getCurrencyID(request);
            HashMap<String,Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            GlobalParams.put("gcurrencyid", basecurrency);
            GlobalParams.put("dateformat", authHandler.getDateFormatter(request));
            Date creationDate = authHandler.getDateFormatter(request).parse(request.getParameter("creationdate"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
//            GoodsReceipt gr = (GoodsReceipt) session.get(GoodsReceipt.class, jobj.getString("billid"));
                KwlReturnObject result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("billid"));
                GoodsReceipt gr = (GoodsReceipt) result.getEntityList().get(0);
                Double recinvamount = jobj.getDouble("payment");
//            KWLCurrency currency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
                result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
                KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
                String currid = currency.getCurrencyID();
                if (gr.getCurrency() != null) {
                    currid = gr.getCurrency().getCurrencyID();
                }
//            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, gr.getJournalEntry().getEntryDate(),gr.getJournalEntry().getExternalCurrencyRate());
                double oldrate = (Double) bAmt.getEntityList().get(0);
//            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate,externalCurrencyRate);
                double newrate = (Double) bAmt.getEntityList().get(0);
                ratio = oldrate - newrate;
                amount += recinvamount * ratio;
            }
    //        amount = CompanyHandler.getBaseToCurrencyAmount(session, request, amount, currencyid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate,externalCurrencyRate);
            amount = (Double) bAmt.getEntityList().get(0);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("oldPaymentRowsAmount : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("oldPaymentRowsAmount : "+ex.getMessage(), ex);
        }
        return (amount);
    }

    private double oldBillingPaymentRowsAmount(HttpServletRequest request,  JSONArray jArr,String currencyid,double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        double ratio=0;
        double amount = 0;
        try{
           KwlReturnObject result;
            HashMap<String,Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            GlobalParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            GlobalParams.put("dateformat", authHandler.getDateFormatter(request));
            Date creationDate = authHandler.getDateFormatter(request).parse(request.getParameter("creationdate"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
//            BillingGoodsReceipt bgr=(BillingGoodsReceipt) session.get(BillingGoodsReceipt.class, jobj.getString("billid"));
            result = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), jobj.getString("billid"));
            BillingGoodsReceipt bgr = (BillingGoodsReceipt) result.getEntityList().get(0);

            Double recinvamount = jobj.getDouble("payment");
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            
            String currid=currency.getCurrencyID();
            if(bgr.getCurrency()!=null)
                 currid=bgr.getCurrency().getCurrencyID();
//            double oldrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,currid,bgr.getJournalEntry().getEntryDate(),externalCurrencyRate);
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, bgr.getJournalEntry().getEntryDate(),bgr.getJournalEntry().getExternalCurrencyRate());
            double oldrate = (Double) bAmt.getEntityList().get(0);

//            double  newrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,currid,AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),externalCurrencyRate);
            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate,externalCurrencyRate);
            double newrate = (Double) bAmt.getEntityList().get(0);
            
            ratio=oldrate-newrate;
            amount+=recinvamount*ratio;

        }
//         amount=CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),externalCurrencyRate);
        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate,externalCurrencyRate);
        amount = (Double) bAmt.getEntityList().get(0);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("oldPaymentRowsAmount : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("oldPaymentRowsAmount : "+ex.getMessage(), ex);
        }
        return (amount);
    }

    public ModelAndView deletePayment(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("VP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deletePayment(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.pay.del", null, RequestContextUtils.getLocale(request));  //"Payment(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteBillingPayment(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("VP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteBillingPayment(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.pay.delb", null, RequestContextUtils.getLocale(request));   //"Billing Payment(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deletePayment(HttpServletRequest request) throws AccountingException, SessionExpiredException, ServiceException {
        try{
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String paymentid = "", jeid = "";
            KwlReturnObject result;
            for (int i = 0; i < jArr.length(); i++){
                JSONObject jobj = jArr.getJSONObject(i);
                paymentid = URLDecoder.decode(jobj.getString("billid"),StaticValues.ENCODING);

                /*
                //Delete Payment Details
                result = accVendorPaymentobj.deletePaymentsDetails(paymentid, companyid);
                //Delete Payment
                result = accVendorPaymentobj.deletePayments(paymentid, companyid);

                jeid = jobj.getString("journalentryid");
                //Delete Journal Entry and Details
                result = accJournalEntryobj.deleteJEDtails(jeid, companyid);
                //Delete Journal Entry Details
                result = accJournalEntryobj.deleteJE(jeid, companyid);
                */

//                query = "update Payment set deleted=true where ID in("+qMarks +") and company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                result = accVendorPaymentobj.deletePaymentEntry(paymentid, companyid);

//                query = "update JournalEntry je set je.deleted=true where je.ID in(select p.journalEntry.ID from Payment p where p.ID in("+qMarks +") and p.company.companyID=je.company.companyID) and je.company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                result = accVendorPaymentobj.getJEFromPayment(paymentid);
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    jeid = (String) itr.next();
                    result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                }

            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public void deleteBillingPayment(HttpServletRequest request) throws AccountingException, SessionExpiredException, ServiceException {
        try{
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String paymentid = "", jeid = "";
            KwlReturnObject result;
            for (int i = 0; i < jArr.length(); i++){
                JSONObject jobj = jArr.getJSONObject(i);
                paymentid = URLDecoder.decode(jobj.getString("billid"),StaticValues.ENCODING);

            //    query = "update BillingPayment set deleted=true where ID in("+qMarks +") and company.companyID=?";
                result = accVendorPaymentobj.deleteBillingPaymentEntry(paymentid, companyid);

            //query = "update JournalEntry je set je.deleted=true where je.ID in(select p.journalEntry.ID from BillingPayment p where p.ID in("+qMarks +") and p.company.companyID=je.company.companyID) and je.company.companyID=?";
                result = accVendorPaymentobj.getJEFromBillingPayment(paymentid, companyid);
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    jeid = ((BillingPayment) itr.next()).getJournalEntry().getID();
                    result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public ModelAndView getPayments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = getPaymentMap(request);
            KwlReturnObject result = accVendorPaymentobj.getPayments(requestParams);
            jobj = getPaymentsJson(requestParams, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getBillingPayments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = getPaymentMap(request);
            KwlReturnObject result = accVendorPaymentobj.getBillingPayments(requestParams);
            jobj = getBillingPaymentsJson(requestParams, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public HashMap<String, Object> getPaymentMap (HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("start", request.getParameter("start"));
        requestParams.put("limit", request.getParameter("limit"));
        requestParams.put("ss", request.getParameter("ss"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        return requestParams;
    }

    public JSONObject getPaymentsJson(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray JArr = new JSONArray();
        try {
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Payment payment = (Payment) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                KwlReturnObject vendorresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), acc.getID());
                Vendor vendor = (Vendor) vendorresult.getEntityList().get(0);
                if(vendor!=null){
                    obj.put("address", vendor.getAddress());
                    obj.put("personemail", vendor.getEmail());
                }else{
                    obj.put("address", "");
                    obj.put("personemail", "");
                }
//                Vendor vendor = (Vendor) session.get(Vendor.class, acc.getID());
//                if(vendor!=null)
//                   obj.put("address", vendor.getAddress());
//                        obj.put("address", vendor.getAddress());

                obj.put("billid", payment.getID());
                obj.put("entryno", payment.getJournalEntry().getEntryNumber());
                obj.put("journalentryid", payment.getJournalEntry().getID());
                obj.put("personid", acc.getID());
                obj.put("billno", payment.getPaymentNumber());
                obj.put("billdate", df.format(payment.getJournalEntry().getEntryDate()));//receiptdate
                Iterator itrRow = payment.getRows().iterator();
                double amount = 0;
                if (!payment.getRows().isEmpty()) {
                    while (itrRow.hasNext()) {
                        amount += ((PaymentDetail) itrRow.next()).getAmount();
                    }
                } else {
                    itrRow = payment.getJournalEntry().getDetails().iterator();
                     while (itrRow.hasNext()) {                         
                        JournalEntryDetail jed=((JournalEntryDetail) itrRow.next());
                        if(!jed.isDebit())
                            amount = jed.getAmount();
                     }
                }
                obj.put("amount", amount);
                KwlReturnObject result = accVendorPaymentobj.getPaymentVendorNames(companyid,payment.getID());
                List vNameList = result.getEntityList();
                Iterator vNamesItr = vNameList.iterator();
                String vendorNames="";
                while (vNamesItr.hasNext()) {
                    vendorNames += (String) vNamesItr.next();
                    vendorNames +=",";
                }
                vendorNames=vendorNames.substring(0,Math.max(0, vendorNames.length()-1));
                obj.put("personname", vendorNames);
                obj.put("memo", payment.getMemo());
                obj.put("deleted", payment.isDeleted());
                obj.put("currencysymbol", (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol()));
                obj.put("externalcurrencyrate", payment.getExternalCurrencyRate());
                obj.put("paymentmethod", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getMethodName()));
                obj.put("currencyid", (payment.getCurrency()==null?currency.getCurrencyID():payment.getCurrency().getCurrencyID()));
                obj.put("methodid",(payment.getPayDetail()==null?"":payment.getPayDetail().getPaymentMethod().getID()));
                obj.put("detailtype",(payment.getPayDetail()==null?"":payment.getPayDetail().getPaymentMethod().getDetailType()));
                if(payment.getPayDetail()!=null){
                    try{
                        obj.put("expirydate",(payment.getPayDetail().getCard()==null?"": df.format(payment.getPayDetail().getCard().getExpiryDate())));
                    }catch(IllegalArgumentException ae){
                        obj.put("expirydate","");
                    }
                    obj.put("refdetail",(payment.getPayDetail().getCard()==null?(payment.getPayDetail().getCheque()==null?"":payment.getPayDetail().getCheque().getDescription()):payment.getPayDetail().getCard().getCardType()));

                if (payment.getPayDetail() != null) {
                    obj.put("refno", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getChequeNo()) : payment.getPayDetail().getCard().getRefNo()));
                    obj.put("refname", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getBankName()) : payment.getPayDetail().getCard().getCardHolder()));
                }
                }
                JArr.put(obj);
            }
            jobj.put("data", JArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getPaymentsJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }

    public JSONObject getBillingPaymentsJson(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");

           Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Object[] row=(Object[])itr.next();
                BillingPayment receipt = (BillingPayment) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                obj.put("billid", receipt.getID());
                obj.put("entryno", receipt.getJournalEntry().getEntryNumber());
                obj.put("journalentryid", receipt.getJournalEntry().getID());
                obj.put("personid", acc.getID());
                obj.put("billno", receipt.getBillingPaymentNumber());
                obj.put("currencysymbol", (receipt.getCurrency()==null?currency.getCurrencyID():receipt.getCurrency().getSymbol()));
                obj.put("externalcurrencyrate", receipt.getExternalCurrencyRate());
                obj.put("billdate", df.format(receipt.getJournalEntry().getEntryDate()));//receiptdate
                Iterator itrRow=receipt.getRows().iterator();
                double amount=0;
                if(!receipt.getRows().isEmpty())
                    while(itrRow.hasNext()){
                        amount+=((BillingPaymentDetail)itrRow.next()).getAmount();
                    }
                else{
                    itrRow=receipt.getJournalEntry().getDetails().iterator();
                    while (itrRow.hasNext()) {
                        JournalEntryDetail jed=((JournalEntryDetail) itrRow.next());
                        if(!jed.isDebit())
                            amount = jed.getAmount();
                    }
                }
                KwlReturnObject result = accVendorPaymentobj.getBillingPaymentVendorNames(companyid,receipt.getID());
                List vNameList = result.getEntityList();
                Iterator vNamesItr = vNameList.iterator();
                String vendorNames="";
                while (vNamesItr.hasNext()) {
                    vendorNames += (String) vNamesItr.next();
                    vendorNames +=",";
                }
                vendorNames=vendorNames.substring(0,Math.max(0, vendorNames.length()-1));
                 KwlReturnObject vendorresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), acc.getID());
                Vendor vendor = (Vendor) vendorresult.getEntityList().get(0);
                obj.put("personemail", vendor!=null?vendor.getEmail():"");
                obj.put("personname", vendorNames);
                obj.put("memo", receipt.getMemo());
                obj.put("deleted", receipt.isDeleted());
                obj.put("paymentmethod",(receipt.getPayDetail()==null?"":receipt.getPayDetail().getPaymentMethod().getMethodName()));
                obj.put("amount", amount);
                obj.put("currencysymbol", (receipt.getCurrency()==null?currency.getSymbol():receipt.getCurrency().getSymbol()));
                obj.put("currencyid", (receipt.getCurrency()==null?currency.getCurrencyID():receipt.getCurrency().getCurrencyID()));
                obj.put("methodid",(receipt.getPayDetail()==null?"":receipt.getPayDetail().getPaymentMethod().getID()));
                obj.put("detailtype",(receipt.getPayDetail()==null?"":receipt.getPayDetail().getPaymentMethod().getDetailType()));
                if(receipt.getPayDetail()!=null){
                     try{
                        obj.put("expirydate",(receipt.getPayDetail().getCard()==null?"": df.format(receipt.getPayDetail().getCard().getExpiryDate())));
                    }catch(IllegalArgumentException ae){
                        obj.put("expirydate","");
                    }
                    obj.put("refdetail",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":receipt.getPayDetail().getCheque().getDescription()):receipt.getPayDetail().getCard().getCardType()));
                    obj.put("refno",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":receipt.getPayDetail().getCheque().getChequeNo()):receipt.getPayDetail().getCard().getRefNo()));
                    obj.put("refname",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":receipt.getPayDetail().getCheque().getBankName()):receipt.getPayDetail().getCard().getCardHolder()));
                }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getBillingPaymentsJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView getPaymentRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        
        try{
//            HashMap<String, Object> requestParams = new HashMap<String, Object>();
//            requestParams.put("gcurrencyid", AuthHandler.getCurrencyID(request));
//            requestParams.put("dateformat", AuthHandler.getDateFormatter(request));
//            requestParams.put("bills", request.getParameterValues("bills"));
//
//            JSONArray DataJArr = getPaymentRowsJson(requestParams);
//            jobj.put("data", DataJArr.length()>0?DataJArr:"");
            jobj = getPaymentRows(request, true);
            issuccess = true;
            msg = messageSource.getMessage("acc.common.rec", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getBillingPaymentRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            jobj = getPaymentRows(request, false);
            issuccess = true;
            msg = messageSource.getMessage("acc.common.rec", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }


    public JSONArray getBillingPaymentRowsJson(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray JArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean isVendorPaymentEdit = (Boolean.parseBoolean((String)requestParams.get("isReceiptEdit"))) ;
            String[] billingreceipt = (String[]) requestParams.get("bills");
            int i=0;
            double taxPercent=0;
            DateFormat df = (DateFormat) requestParams.get("dateformat");
            HashMap<String, Object> pRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("billingPayment.ID");
            order_by.add("srno");
            order_type.add("asc");
            pRequestParams.put("filter_names", filter_names);
            pRequestParams.put("filter_params", filter_params);
            pRequestParams.put("order_by", order_by);
            pRequestParams.put("order_type", order_type);
            

            while(billingreceipt!=null&&i<billingreceipt.length){
//                BillingPayment re=(BillingPayment)session.get(BillingPayment.class, billingreceipt[i]);
                KwlReturnObject presult = accountingHandlerDAOobj.getObject(BillingPayment.class.getName(), billingreceipt[i]);
                BillingPayment re = (BillingPayment) presult.getEntityList().get(0);
//                Iterator itr=re.getRows().iterator();
                filter_params.clear();
                filter_params.add(re.getID());
                KwlReturnObject grdresult = accVendorPaymentobj.getBillingPaymentDetails(pRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();


                while(itr.hasNext()) {
                    BillingPaymentDetail row=(BillingPaymentDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", isVendorPaymentEdit?row.getBillingGoodsReceipt().getID():re.getID());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("transectionno", row.getBillingGoodsReceipt().getBillingGoodsReceiptNumber());
                    obj.put("transectionid", (isVendorPaymentEdit?row.getBillingGoodsReceipt().getVendorEntry().getAmount():row.getBillingGoodsReceipt().getID()));
                    obj.put("amount",row.getAmount());
                    obj.put("currencysymbol", (row.getBillingPayment().getCurrency()==null?currency.getCurrencyID():row.getBillingPayment().getCurrency().getSymbol()));
                    obj.put("duedate", df.format(row.getBillingGoodsReceipt().getDueDate()));
                    obj.put("creationdate", df.format(row.getBillingGoodsReceipt().getJournalEntry().getEntryDate()));
                    double totalamount = row.getBillingGoodsReceipt().getVendorEntry().getAmount();
                    obj.put("totalamount", totalamount);

                    KwlReturnObject amtrs = accGoodsReceiptobj.getAmtromBPD(row.getBillingGoodsReceipt().getID());
                    double ramount = amtrs.getEntityList().size()>0 ? (Double) amtrs.getEntityList().get(0) : 0;
                    double amountdue=totalamount-ramount;
                    
                    if (row.getBillingGoodsReceipt().getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getBillingGoodsReceipt().getJournalEntry().getEntryDate(), row.getBillingGoodsReceipt().getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);
                    obj.put("discount", row.getBillingGoodsReceipt().getDiscount() == null ? 0 : row.getBillingGoodsReceipt().getDiscount().getDiscountValue());
                    obj.put("payment", row.getBillingGoodsReceipt().getID());
                    obj.put("amountduenonnegative", (isVendorPaymentEdit?amountdue+row.getAmount():amountdue));
                    obj.put("totalamount", row.getBillingGoodsReceipt().getVendorEntry().getAmount());
                    JArr.put(obj);
                }
                i++;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getBillingPaymentRowsJson : "+ex.getMessage(), ex);
        }
        return JArr;
    }

    public JSONObject getPaymentRows(HttpServletRequest request, boolean flag) throws SessionExpiredException, ServiceException{
        JSONObject jobj = new JSONObject();
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("dateformat", authHandler.getDateFormatter(request));
            requestParams.put("bills", request.getParameterValues("bills"));
            requestParams.put("isReceiptEdit", request.getParameter("isReceiptEdit"));
            JSONArray DataJArr = new JSONArray();
//            if(flag){
//                DataJArr = getPaymentRowsJson(requestParams); xz
//            }else{
                DataJArr = getBillingPaymentRowsJson(requestParams);
//            }
            jobj.put("data", DataJArr.length()>0?DataJArr:"");
        }
        catch (JSONException ex) {
            throw ServiceException.FAILURE("getPaymentRows : " + ex.getMessage(), ex);
//            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    public ModelAndView exportPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getPaymentMap(request);
            KwlReturnObject result = accVendorPaymentobj.getPayments(requestParams);
            jobj = getPaymentsJson(requestParams, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public ModelAndView exportBillingPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getPaymentMap(request);
            KwlReturnObject result = accVendorPaymentobj.getBillingPayments(requestParams);
            jobj = getBillingPaymentsJson(requestParams, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
		
	}
}
