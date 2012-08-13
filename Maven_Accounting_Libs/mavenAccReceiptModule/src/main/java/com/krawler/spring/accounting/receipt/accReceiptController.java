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

package com.krawler.spring.accounting.receipt;

import com.krawler.accounting.utils.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BillingInvoice;
import com.krawler.hql.accounting.BillingReceipt;
import com.krawler.hql.accounting.BillingReceiptDetail;
import com.krawler.hql.accounting.Card;
import com.krawler.hql.accounting.Cheque;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.PayDetail;
import com.krawler.hql.accounting.PaymentMethod;
import com.krawler.hql.accounting.Receipt;
import com.krawler.hql.accounting.ReceiptDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
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
public class accReceiptController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accReceiptDAO accReceiptDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accPaymentDAO accPaymentDAOobj;
    private accTaxDAO accTaxObj;
    private accCurrencyDAO accCurrencyobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDaoObj;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

	public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
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
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public ModelAndView saveReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
            List li = saveReceipt(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
             String[] id = (String[]) li.get(0);
            issuccess = true;
            msg = messageSource.getMessage("acc.receipt.save", null, RequestContextUtils.getLocale(request));   //"Receipt has been saved successfully";
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteJEArray(id[0],companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteChequeOrCard(id[1],companyid);
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        KwlReturnObject result;
        Receipt receipt = null;
        String oldjeid=null;
        String Cardid=null;
        List ll = new ArrayList();
        try {
            Account dipositTo = null;
//            BillingReceipt receipt = new BillingReceipt();
            double amount = 0;
            double amountDiff = 0;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalCurrencyRate=StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateFormatter(request);
            String entryNumber=request.getParameter("no");
            String receiptid =request.getParameter("billid");
            
            String jeid=null;
            boolean jeautogenflag = false;
            String payDetailID=null;
            String jeentryNumber=null;
            HashMap receipthm = new HashMap();

            if (!StringUtil.isNullOrEmpty(receiptid)){
                
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                receipt=(Receipt)receiptObj.getEntityList().get(0);
                jeentryNumber=receipt.getJournalEntry().getEntryNumber();
                oldjeid=receipt.getJournalEntry().getID();
                jeautogenflag =receipt.getJournalEntry().isAutoGenerated();
                if(receipt.getPayDetail()!=null){
                     payDetailID=receipt.getPayDetail().getID();
                     if(receipt.getPayDetail().getCard()!=null)
                            Cardid=receipt.getPayDetail().getCard().getID();
                       // accPaymentDAOobj.deleteCard(receipt.getPayDetail().getCard().getID(),companyid);
                     if(receipt.getPayDetail().getCheque()!=null)
                         Cardid=receipt.getPayDetail().getCheque().getID();
                        //accPaymentDAOobj.deleteCheque(receipt.getPayDetail().getCheque().getID(),companyid);
                }
                 result = accReceiptDAOobj.deleteReceiptDetails(receiptid,companyid);
                

            }else{
//            String q="from Receipt where receiptNumber=? and company.companyID=?";
//            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                throw new AccountingException("Receipt number '"+entryNumber+"' already exists.");
                result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
                int count = result.getRecordTotalCount();
                if(count > 0){
                    throw new AccountingException("Receipt number '"+entryNumber+"' already exists.");
                }


                String nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_RECEIPT);
    //            receipt.setBillingReceiptNumber(entryNumber);
    //            receipt.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
    //            receipt.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_BILLINGRECEIPT).equals(entryNumber));
                receipthm.put("entrynumber", entryNumber);

                receipthm.put("autogenerated", nextAutoNumber.equals(entryNumber));
            }


//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));

//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

          
            receipthm.put("currencyid", currencyid);
            receipthm.put("externalCurrencyRate", externalCurrencyRate);
//            PaymentMethod payMethod = (PaymentMethod) session.get(PaymentMethod.class, request.getParameter("pmtmethod"));
            KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap pdetailhm = new HashMap();
                pdetailhm.put("paymethodid", payMethod.getID());
                pdetailhm.put("companyid", company.getCompanyID());
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
                        chequehm.put("bankmasteritemid", obj.getString("bankmasteritemid"));
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
                }
//                session.save(pdetail);
                KwlReturnObject pdresult=null;
                if (!StringUtil.isNullOrEmpty(receiptid)&&!StringUtil.isNullOrEmpty(payDetailID))
                    pdetailhm.put("paydetailid", payDetailID);
                    pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);
                PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
//                receipt.setPayDetail(pdetail);
                receipthm.put("paydetailsid", pdetail.getID());
            
//            receipt.setMemo(request.getParameter("memo"));
//            receipt.setDeleted(false);
//            receipt.setCompany(company);
            receipthm.put("memo", request.getParameter("memo"));
            receipthm.put("companyid", company.getCompanyID());
            
            Map<String,Object> jeDataMap = AccountingManager.getGlobalParams(request);            
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            HashSet jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail=  request.getParameter("detail");
             JSONArray jArr=new JSONArray();
            if(!StringUtil.isNullOrEmpty(detail))
                    jArr = new JSONArray(detail);
            if (jArr.length() > 0){
//            amount = saveReceiptRows(session, request, receipt, company, jArr);
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }

//            amountDiff=oldReceiptRowsAmount(session, request, jArr,currencyid);
                amountDiff = oldReceiptRowsAmount(request, jArr, currencyid, externalCurrencyRate);

                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
//                JournalEntryDetail jed = new JournalEntryDetail();
//                jed.setCompany(company);
//                jed.setAmount(amountDiff);
//                jed.setAccount(preferences.getForeignexchange());
//                jed.setDebit(true);
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size()+1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", amountDiff);
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    jedjson.put("debit", true);
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
//            jed.setAmount(amount+amountDiff);
//            jed.setAccount(((Account) session.get(Account.class, request.getParameter("accid"))));
//            jed.setDebit(false);
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", companyid);
            jedjson.put("amount", amount+amountDiff);
            jedjson.put("accountid", request.getParameter("accid"));
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);

//            jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(amount);
//            jed.setAccount(dipositTo);
//            jed.setDebit(true);
            jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", companyid);
            jedjson.put("amount", amount);
            jedjson.put("accountid", dipositTo.getID());
            jedjson.put("debit", true);
            jedjson.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
//            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
//                    request.getParameter("memo"), "JE" + receipt.getBillingReceiptNumber(),currencyid, hs,request);
//            jed.setJournalEntry(journalEntry);
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

//            receipt.setJournalEntry(journalEntry);
            receipthm.put("journalentryid", journalEntry.getID());
            if (!StringUtil.isNullOrEmpty(receiptid))
                receipthm.put("receiptid", receipt.getID());

            result = accReceiptDAOobj.saveReceipt(receipthm);
            receipt = (Receipt) result.getEntityList().get(0);
            receipthm.put("receiptid", receipt.getID());

            HashSet receiptDetails = saveReceiptRows(receipt, company, jArr);
            receipthm.put("receiptdetails", receiptDetails);

            result = accReceiptDAOobj.saveReceipt(receipthm);
            receipt = (Receipt) result.getEntityList().get(0);
            
//            list.add(receipt);
//            result = new KwlReturnObject(true, "Receipt has been saved successfully", null, list, list.size());
//            session.saveOrUpdate(receipt);
            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User "+sessionHandlerImpl.getUserFullName(request) +" created new receipt ", request, receipt.getID());
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        }
       ll.add(new String[]{oldjeid,Cardid});
       return  (ArrayList) ll;
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
    private double oldReceiptRowsAmount(HttpServletRequest request, JSONArray jArr,String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        double ratio=0;
        double amount = 0;
        KwlReturnObject result;
        try {
            HashMap<String,Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            Date creationDate =authHandler.getDateFormatter(request).parse(request.getParameter("creationdate"));
            for (int i = 0; i < jArr.length(); i++){
                JSONObject jobj = jArr.getJSONObject(i);
    //            Invoice invoice=(Invoice) session.get(Invoice.class, jobj.getString("billid"));
                result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("billid"));
                Invoice invoice = (Invoice) result.getEntityList().get(0);

    //            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
                result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
                KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
                String currid=currency.getCurrencyID();
                if (invoice.getCurrency() != null) {
                    currid = invoice.getCurrency().getCurrencyID();
                }

    //            double oldrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,currid,invoice.getJournalEntry().getEntryDate());
                result = accCurrencyobj.getCurrencyToBaseAmount(requestParams, 1.0, currid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                double oldrate = (Double) result.getEntityList().get(0);

    //            double  newrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,currid,AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                result = accCurrencyobj.getCurrencyToBaseAmount(requestParams, 1.0, currid, creationDate, externalCurrencyRate);
                double newrate = (Double) result.getEntityList().get(0);

                ratio=oldrate-newrate;
                Double recinvamount = jobj.getDouble("payment");
                amount+=recinvamount*ratio;
            }
    //        amount=CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            result = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
            amount = (Double) result.getEntityList().get(0);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("oldReceiptRowsAmount : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("oldReceiptRowsAmount : "+ex.getMessage(), ex);
        }
        return (amount);
    }

    private HashSet saveReceiptRows(Receipt receipt, Company company, JSONArray jArr) throws JSONException, ServiceException {
        HashSet details = new HashSet();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            ReceiptDetail rd = new ReceiptDetail();
            rd.setSrno(i+1);
            rd.setAmount(jobj.getDouble("payment"));
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("billid"));
            rd.setInvoice((Invoice) result.getEntityList().get(0));
            rd.setReceipt(receipt);
            details.add(rd);
        }
        return details;
    }

    public ModelAndView getReceipts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String msg = "";
		try {
            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
            KwlReturnObject result = accReceiptDAOobj.getReceipts(requestParams);
            jobj = getReceiptJson(request, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE("CustomerManager.getReciepts", ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try{
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public HashMap<String, Object> getReceiptRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("ss", request.getParameter("ss"));
        requestParams.put("start", request.getParameter("start"));
        requestParams.put("limit", request.getParameter("limit"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        return requestParams;
    }

    public JSONObject getReceiptJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONObject jobj=new JSONObject();
        JSONArray jArr=new JSONArray();
        try{
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            
            Iterator itr = list.iterator();
            while(itr.hasNext()) {
                Object[] row=(Object[])itr.next();
                Receipt receipt = (Receipt) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
//                Customer customer = (Customer) session.get(Customer.class, acc.getID());
//                if (customer != null) {
//                    obj.put("address", customer.getBillingAddress());
//                } else {
//                    Vendor vendor = (Vendor) session.get(Vendor.class, acc.getID());
//                    if (vendor != null) {
//                        obj.put("address", vendor.getAddress());
//                    }
//                }
                String address = "";
                KwlReturnObject cresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), acc.getID());
                Customer customer = (Customer) cresult.getEntityList().get(0);
                 obj.put("personemail", customer==null?"":customer.getEmail());
                if (customer != null) {
                    address = customer.getBillingAddress();
                } else {
                   
                        address = "";                   
                }
                obj.put("address", address);

                obj.put("billid", receipt.getID());
                obj.put("entryno", receipt.getJournalEntry().getEntryNumber()); 
                obj.put("journalentryid", receipt.getJournalEntry().getID());
                obj.put("personid", acc.getID());
                obj.put("billno", receipt.getReceiptNumber());
                obj.put("billdate", authHandler.getDateFormatter(request).format(receipt.getJournalEntry().getEntryDate()));//receiptdate
                Iterator itrRow=receipt.getRows().iterator();
                double amount=0;
                if(!receipt.getRows().isEmpty())
                    while(itrRow.hasNext()){
                        amount+=((ReceiptDetail)itrRow.next()).getAmount();
                    }
                else{
                    itrRow=receipt.getJournalEntry().getDetails().iterator();
                    amount+=((JournalEntryDetail)itrRow.next()).getAmount();
                }
                obj.put("amount", amount);
                obj.put("personname", acc.getName());
                obj.put("memo", receipt.getMemo());
                obj.put("deleted", receipt.isDeleted());
                obj.put("currencysymbol", (receipt.getCurrency()==null?currency.getSymbol():receipt.getCurrency().getSymbol()));
                obj.put("externalcurrencyrate", receipt.getExternalCurrencyRate());
                obj.put("currencyid", (receipt.getCurrency()==null?currency.getCurrencyID():receipt.getCurrency().getCurrencyID()));
                obj.put("methodid",(receipt.getPayDetail()==null?"":receipt.getPayDetail().getPaymentMethod().getID()));
                obj.put("detailtype",(receipt.getPayDetail()==null?"":receipt.getPayDetail().getPaymentMethod().getDetailType()));
                obj.put("paymentmethod",(receipt.getPayDetail()==null?"":receipt.getPayDetail().getPaymentMethod().getMethodName()));
                if(receipt.getPayDetail()!=null){
                    try{
                        obj.put("expirydate",(receipt.getPayDetail().getCard()==null?"": (receipt.getPayDetail().getCard().getExpiryDate()==null?"":receipt.getPayDetail().getCard().getExpiryDate())));
                    }catch(Exception ae){
                        obj.put("expirydate","");
                    }
                    obj.put("refdetail",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":receipt.getPayDetail().getCheque().getDescription()):receipt.getPayDetail().getCard().getCardType()));
                    obj.put("refno",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":receipt.getPayDetail().getCheque().getChequeNo()):receipt.getPayDetail().getCard().getRefNo()));
                    obj.put("refname",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":(receipt.getPayDetail().getCheque().getBankMasterItem()==null?receipt.getPayDetail().getCheque().getBankName():receipt.getPayDetail().getCheque().getBankMasterItem().getID())):receipt.getPayDetail().getCard().getCardHolder()));
                }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceiptJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }
    


    public ModelAndView deleteReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteReceipt(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.receipt.del", null, RequestContextUtils.getLocale(request));   //"Receipt(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteReceipt(HttpServletRequest request) throws AccountingException, SessionExpiredException, ServiceException {
        try{
            String receiptsJson = request.getParameter("data");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray(receiptsJson);

            String receiptid, jeid;
            KwlReturnObject result;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                receiptid = URLDecoder.decode(jobj.getString("billid"), StaticValues.ENCODING);
                /*
                //Delete Billing receipt details
                result = accReceiptDAOobj.deleteReceiptDetails(receiptid, companyid);
                //Delete Billing Receipt
                result = accReceiptDAOobj.deleteReceipt(receiptid, companyid);

                jeid = jobj.getString("journalentryid");
                //Delete Journal Entry and Details
                result = accJournalEntryobj.deleteJEDtails(jeid, companyid);
                //Delete Journal Entry Details
                result = accJournalEntryobj.deleteJE(jeid, companyid);
                */

//                query = "update Receipt set deleted=true where ID in("+qMarks +") and company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                result = accReceiptDAOobj.deleteReceiptEntry(receiptid, companyid);

//                query = "update JournalEntry je set je.deleted=true where je.ID in(select r.journalEntry.ID from Receipt r where r.ID in("+qMarks +") and r.company.companyID=je.company.companyID) and je.company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                result = accReceiptDAOobj.getJEFromReceipt(receiptid);
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



    public ModelAndView saveBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
            List li =saveBillingReceipt(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
             String[] id = (String[]) li.get(0);
            issuccess = true;
            msg = messageSource.getMessage("acc.receipt.save", null, RequestContextUtils.getLocale(request));   //"Receipt has been saved successfully";
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteJEArray(id[0],companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteChequeOrCard(id[1],companyid);
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            issuccess = false;
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveBillingReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        KwlReturnObject result;
        BillingReceipt receipt = null;
        String oldjeid=null;
        String Cardid=null;
        List ll = new ArrayList();
        try {
            Account dipositTo = null;
//            BillingReceipt receipt = new BillingReceipt();
            double amount = 0;
            double amountDiff = 0;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalExchangeRate=StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateFormatter(request);
            String entryNumber=request.getParameter("no");
            String receiptid =request.getParameter("billid");
            String jeid=null;
            String payDetailID=null;
            String jeentryNumber=null;
            boolean jeautogenflag = false;

            HashMap receipthm = new HashMap();
            if (!StringUtil.isNullOrEmpty(receiptid)){
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(BillingReceipt.class.getName(), receiptid);
                receipt=(BillingReceipt)receiptObj.getEntityList().get(0);
                jeentryNumber=receipt.getJournalEntry().getEntryNumber();
                oldjeid=receipt.getJournalEntry().getID();
                jeautogenflag = receipt.getJournalEntry().isAutoGenerated();
                if(receipt.getPayDetail()!=null){
                     payDetailID=receipt.getPayDetail().getID();
                     if(receipt.getPayDetail().getCard()!=null)
                            Cardid=receipt.getPayDetail().getCard().getID();
                       // accPaymentDAOobj.deleteCard(receipt.getPayDetail().getCard().getID(),companyid);
                     if(receipt.getPayDetail().getCheque()!=null)
                         Cardid=receipt.getPayDetail().getCheque().getID();
                        //accPaymentDAOobj.deleteCheque(receipt.getPayDetail().getCheque().getID(),companyid);
                }
                result = accReceiptDAOobj.deleteBillingReceiptDetails(receiptid,companyid);
            }else{
//              String q="from Receipt where receiptNumber=? and company.companyID=?";
//              if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                throw new AccountingException("Receipt number '"+entryNumber+"' already exists.");
                result = accReceiptDAOobj.getBillingReceiptFromBillNo(entryNumber, companyid);
                int count = result.getRecordTotalCount();
                if(count > 0){
                    throw new AccountingException("Receipt number '"+entryNumber+"' already exists.");
                }
                String nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGRECEIPT);
    //            receipt.setBillingReceiptNumber(entryNumber);
    //            receipt.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
    //            receipt.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_BILLINGRECEIPT).equals(entryNumber));
                receipthm.put("entrynumber", entryNumber);
                receipthm.put("autogenerated", nextAutoNumber.equals(entryNumber));
            }

//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));

//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

//            String q="from BillingReceipt where billingReceiptNumber=? and company.companyID=?";
//            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                throw new AccountingException("Receipt number '"+entryNumber+"' already exists.");

            receipthm.put("currencyid", currencyid);
            receipthm.put("externalCurrencyRate", externalExchangeRate);
//            PaymentMethod payMethod = (PaymentMethod) session.get(PaymentMethod.class, request.getParameter("pmtmethod"));
            KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap pdetailhm = new HashMap();
            pdetailhm.put("paymethodid", payMethod.getID());
            pdetailhm.put("companyid", company.getCompanyID());
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
                    chequehm.put("description", obj.getString("description"));
                    chequehm.put("bankname", obj.getString("bankname"));
                    chequehm.put("bankmasteritemid", obj.getString("bankmasteritemid"));
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
                    cardhm.put("expirydate", obj.getString("expirydate"));
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
//                receipt.setPayDetail(pdetail);
                receipthm.put("paydetailsid", pdetail.getID());
//            receipt.setMemo(request.getParameter("memo"));
//            receipt.setDeleted(false);
//            receipt.setCompany(company);
            receipthm.put("memo", request.getParameter("memo"));
            receipthm.put("companyid", company.getCompanyID());

            Map<String,Object> jeDataMap = AccountingManager.getGlobalParams(request);
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            HashSet jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail=  request.getParameter("detail");
             JSONArray jArr=new JSONArray();
              if(!StringUtil.isNullOrEmpty(detail))
                    jArr = new JSONArray(detail);
            if (jArr.length() > 0){
//            amount = saveBillingReceiptRows(session, request, receipt, company, jArr);
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }
//            amountDiff=oldBillingReceiptRowsAmount(session, request, jArr,currencyid);
                amountDiff = oldBillingReceiptRowsAmount(request, jArr, currencyid,externalExchangeRate);

                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
//                JournalEntryDetail jed = new JournalEntryDetail();
//                jed.setCompany(company);
//                jed.setAmount(amountDiff);
//                jed.setAccount(preferences.getForeignexchange());
//                jed.setDebit(true);
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", companyid);
            jedjson.put("amount", amountDiff);
            jedjson.put("accountid", preferences.getForeignexchange().getID());
            jedjson.put("debit", true);
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
//            jed.setAmount(amount+amountDiff);
//            jed.setAccount(((Account) session.get(Account.class, request.getParameter("accid"))));
//            jed.setDebit(false);
             JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", companyid);
            jedjson.put("amount", amount+amountDiff);
            jedjson.put("accountid", request.getParameter("accid"));
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);

//            jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(amount);
//            jed.setAccount(dipositTo);
//            jed.setDebit(true);
            jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", companyid);
            jedjson.put("amount", amount);
            jedjson.put("accountid", dipositTo.getID());
            jedjson.put("debit", true);
            jedjson.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
//            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
//                    request.getParameter("memo"), "JE" + receipt.getBillingReceiptNumber(),currencyid, hs,request);
//            jed.setJournalEntry(journalEntry);
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalExchangeRate", externalExchangeRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

//            receipt.setJournalEntry(journalEntry);
            receipthm.put("journalentryid", journalEntry.getID());
            
            if (!StringUtil.isNullOrEmpty(receiptid))
                receipthm.put("billingreceiptid", receipt.getID());
            result = accReceiptDAOobj.saveBillingReceipt(receipthm);
            receipt = (BillingReceipt) result.getEntityList().get(0);
            receipthm.put("billingreceiptid", receipt.getID());

            HashSet receiptDetails = saveBillingReceiptRows(receipt, company, jArr);
            receipthm.put("receiptdetails", receiptDetails);

            result = accReceiptDAOobj.saveBillingReceipt(receipthm);
            receipt = (BillingReceipt) result.getEntityList().get(0);

          //  list.add(receipt);
//            session.saveOrUpdate(receipt);
            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User "+sessionHandlerImpl.getUserFullName(request) +" created new receipt ", request, receipt.getID());
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveBillingReceipt : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveBillingReceipt : " + ex.getMessage(), ex);
        }
        ll.add(new String[]{oldjeid,Cardid});
       return  (ArrayList) ll;
    }

    private HashSet saveBillingReceiptRows(BillingReceipt receipt, Company company, JSONArray jArr) throws JSONException, ServiceException {
        HashSet details = new HashSet();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            BillingReceiptDetail rd = new BillingReceiptDetail();
            rd.setSrno(i+1);
            rd.setAmount(jobj.getDouble("payment"));
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), jobj.getString("billid"));
            rd.setBillingInvoice((BillingInvoice) result.getEntityList().get(0));
            rd.setBillingReceipt(receipt);
            details.add(rd);
        }
        return details;
    }
    
    private double oldBillingReceiptRowsAmount( HttpServletRequest request, JSONArray jArr, String currencyid,double externalExchangeRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        double ratio = 0;
        double amount = 0;
        KwlReturnObject result;
        HashMap<String,Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
//            BillingInvoice invoice = (BillingInvoice) session.get(BillingInvoice.class, jobj.getString("billid"));
            result = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), jobj.getString("billid"));
            BillingInvoice invoice = (BillingInvoice) result.getEntityList().get(0);

//            KWLCurrency currency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (invoice.getCurrency() != null) {
                currid = invoice.getCurrency().getCurrencyID();
            }
            
//            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, invoice.getJournalEntry().getEntryDate());
            result = accCurrencyobj.getCurrencyToBaseAmount(requestParams, 1.0, currid, invoice.getJournalEntry().getEntryDate(),invoice.getJournalEntry().getExternalCurrencyRate());
            double oldrate = (Double) result.getEntityList().get(0);
//            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            result = accCurrencyobj.getCurrencyToBaseAmount(requestParams, 1.0, currid, authHandler.getDateFormatter(request).parse(request.getParameter("creationdate")), externalExchangeRate);
            double newrate = (Double) result.getEntityList().get(0);

            ratio = oldrate - newrate;
            Double recinvamount = jobj.getDouble("payment");
            amount += recinvamount * ratio;
        }
//        amount = CompanyHandler.getBaseToCurrencyAmount(session, request, amount, currencyid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
        result = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, authHandler.getDateFormatter(request).parse(request.getParameter("creationdate")), externalExchangeRate);
        amount = (Double) result.getEntityList().get(0);
        return (amount);
    }
    
    public ModelAndView getBillingReceipts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String msg = "";
		try {
            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
            KwlReturnObject result = accReceiptDAOobj.getBillingReceipts(requestParams);
            jobj = getBillingReceiptJson(request, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try{
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getBillingReceiptJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONObject jobj=new JSONObject();
        JSONArray jArr=new JSONArray();
        try{
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            Iterator itr = list.iterator();
            while(itr.hasNext()) {
                Object[] row=(Object[])itr.next();
                BillingReceipt receipt = (BillingReceipt) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                obj.put("billid", receipt.getID());
                obj.put("entryno", receipt.getJournalEntry().getEntryNumber());
                obj.put("journalentryid", receipt.getJournalEntry().getID());
                KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), acc.getID());
                Customer customer = (Customer) customerresult.getEntityList().get(0);
                obj.put("personemail", customer !=null?customer .getEmail():"");
                obj.put("personid", acc.getID());
                obj.put("billno", receipt.getBillingReceiptNumber());
                obj.put("billdate", authHandler.getDateFormatter(request).format(receipt.getJournalEntry().getEntryDate()));//receiptdate
                obj.put("currencysymbol", (receipt.getCurrency()==null?currency.getCurrencyID():receipt.getCurrency().getSymbol()));
                obj.put("externalcurrencyrate", receipt.getExternalCurrencyRate());
                Iterator itrRow=receipt.getRows().iterator();
                double amount=0;
                if(!receipt.getRows().isEmpty())
                    while(itrRow.hasNext()){
                        amount+=((BillingReceiptDetail)itrRow.next()).getAmount();
                    }
                else{
                    itrRow=receipt.getJournalEntry().getDetails().iterator();
                    amount+=((JournalEntryDetail)itrRow.next()).getAmount();
                }
                obj.put("amount", amount);
                obj.put("personname", acc.getName());
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
                        obj.put("expirydate",(receipt.getPayDetail().getCard()==null?"": authHandler.getDateFormatter(request).format(receipt.getPayDetail().getCard().getExpiryDate())));
                    }catch(IllegalArgumentException ae){
                        obj.put("expirydate","");
                    }
                    obj.put("refdetail",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":receipt.getPayDetail().getCheque().getDescription()):receipt.getPayDetail().getCard().getCardType()));
                    obj.put("refno",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":receipt.getPayDetail().getCheque().getChequeNo()):receipt.getPayDetail().getCard().getRefNo()));
                    obj.put("refname",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":(receipt.getPayDetail().getCheque().getBankMasterItem()==null?receipt.getPayDetail().getCheque().getBankName():receipt.getPayDetail().getCheque().getBankMasterItem().getID())):receipt.getPayDetail().getCard().getCardHolder()));
                }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBillingReceiptJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView getBillingReceiptRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String msg = "";
		try {
            jobj = getBillingReceiptRowsJSON(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try{
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private JSONObject getBillingReceiptRowsJSON(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj=new JSONObject();
        try {
//                KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean isReceiptEdit = Boolean.parseBoolean(request.getParameter("isReceiptEdit"));
            String[] billingreceipt=request.getParameterValues("bills");
            int i=0;
            double taxPercent=0;
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("billingReceipt.ID");
            order_by.add("srno");
            order_type.add("asc");
            rRequestParams.put("filter_names", filter_names);
            rRequestParams.put("filter_params", filter_params);
            rRequestParams.put("order_by", order_by);
            rRequestParams.put("order_type", order_type);

            JSONArray jArr=new JSONArray();
            while(billingreceipt!=null&&i<billingreceipt.length){
//                    BillingReceipt re=(BillingReceipt)session.get(BillingReceipt.class, billingreceipt[i]);
                KwlReturnObject result = accountingHandlerDAOobj.getObject(BillingReceipt.class.getName(), billingreceipt[i]);
                BillingReceipt re = (BillingReceipt) result.getEntityList().get(0);
//                Iterator itr=re.getRows().iterator();
                filter_params.clear();
                filter_params.add(re.getID());
                KwlReturnObject grdresult = accReceiptDAOobj.getBillingReceiptDetails(rRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();

                while(itr.hasNext()) {
                    BillingReceiptDetail row=(BillingReceiptDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", isReceiptEdit?row.getBillingInvoice().getID():re.getID());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("currencysymbol", (row.getBillingReceipt().getCurrency()==null?currency.getCurrencyID():row.getBillingReceipt().getCurrency().getSymbol()));
                    obj.put("transectionno", row.getBillingInvoice().getBillingInvoiceNumber());
                    obj.put("transectionid", row.getBillingInvoice().getID());
                    obj.put("amount",(isReceiptEdit?row.getBillingInvoice().getCustomerEntry().getAmount():row.getAmount()));
                    obj.put("duedate",  authHandler.getDateFormatter(request).format(row.getBillingInvoice().getDueDate()));
                    obj.put("creationdate", authHandler.getDateFormatter(request).format(row.getBillingInvoice().getJournalEntry().getEntryDate()));
                    double totalamount = row.getBillingInvoice().getCustomerEntry().getAmount();
                    obj.put("totalamount", totalamount);

                    KwlReturnObject amtrs = accReceiptDAOobj.getBillingReceiptAmountFromInvoice(row.getBillingInvoice().getID());
                    double ramount = amtrs.getEntityList().size()>0 ? (Double) amtrs.getEntityList().get(0) : 0;
                    double amountdue=totalamount-ramount;
                    obj.put("amountduenonnegative", (isReceiptEdit?amountdue+row.getAmount():amountdue));
                   if (row.getBillingInvoice().getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getBillingInvoice().getJournalEntry().getEntryDate(), row.getBillingInvoice().getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);
                    obj.put("discount", row.getBillingInvoice().getDiscount() == null ? 0 : row.getBillingInvoice().getDiscount().getDiscountValue());
                    obj.put("payment", row.getBillingInvoice().getID());                  
                    obj.put("totalamount", row.getBillingInvoice().getCustomerEntry().getAmount());
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBillingReceiptRowsJSON : "+ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView deleteBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
            String receiptsJson = request.getParameter("data");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            int no = deleteBillingReceipt(receiptsJson, companyid);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.receipt.billdel", null, RequestContextUtils.getLocale(request));   //"Billing Receipt(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int deleteBillingReceipt(String receiptsJson, String companyid) throws AccountingException, ServiceException {
        String msg = "";
        int numRows = 0;
        try{
            JSONArray jArr = new JSONArray(receiptsJson);

            String receiptid;
            KwlReturnObject result;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                receiptid = URLDecoder.decode(jobj.getString("billid"), StaticValues.ENCODING);
                //Delete Billing receipt details
//                result = accReceiptDAOobj.deleteBillingReceiptDetails(receiptid, companyid);
                //Delete Billing Receipt
                result = accReceiptDAOobj.deleteBillingReceiptEntry(receiptid, companyid);
            
//                jeid = URLDecoder.decode(jobj.getString("journalentryid"), StaticValues.ENCODING);
                //Delete Journal Entry and Details
//                result = accJournalEntryobj.deleteJEDtails(jeid, companyid);
                //Delete Journal Entry Details

                result = accReceiptDAOobj.getJEFromBR(receiptid, companyid);
                      List list = result.getEntityList();
                      Iterator itr = list.iterator();
                      while(itr.hasNext()) {
                          String jeid = (String) itr.next();
                          result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                      }

//              query = "update JournalEntry je set je.deleted=true where je.ID in(select p.journalEntry.ID from BillingReceipt p where p.ID in("+qMarks +") and p.company.companyID=je.company.companyID) and je.company.companyID=?";

                numRows++;
            }
//            issuccess = true;
//            msg = "Billing Receipt(s) has been deleted successfully";
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
        } catch (ServiceException ex) {
            msg = "Selected record(s) is currently used in the transaction(s).";
            throw new AccountingException(msg);
           // Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            msg = ex.getMessage();
            throw new AccountingException(msg);
           // Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numRows;

    }

    public ModelAndView exportReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
            KwlReturnObject result = accReceiptDAOobj.getReceipts(requestParams);
            jobj = getReceiptJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView exportBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
            KwlReturnObject result = accReceiptDAOobj.getBillingReceipts(requestParams);
            jobj = getBillingReceiptJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

}
