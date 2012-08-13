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
package com.krawler.spring.accounting.creditnote;

import com.krawler.accounting.utils.AuditAction;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BillingCreditNote;
import com.krawler.hql.accounting.BillingCreditNoteDetail;
import com.krawler.hql.accounting.BillingInvoiceDetail;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.CreditNote;
import com.krawler.hql.accounting.CreditNoteDetail;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.Group;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
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
public class accCreditNoteController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private exportMPXDAOImpl exportDaoObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
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
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
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

    public ModelAndView saveCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try{
            CreditNote creditnote = saveCreditNote(request, jobj);
            issuccess = true;
            msg = messageSource.getMessage("acc.creditN.save", null, RequestContextUtils.getLocale(request));   //"Credit Note has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public CreditNote saveCreditNote(HttpServletRequest request, JSONObject returnJobj) throws ServiceException, SessionExpiredException, AccountingException {
        CreditNote creditnote = null;
        KwlReturnObject result;
        try {
            boolean reloadInventory = false;//Flag used to reload inventory on Client Side If CN type equals to "Return" or "Defective"
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            DateFormat df = authHandler.getDateFormatter(request);
            double externalCurrencyRate= StringUtil.getDouble(request.getParameter("externalcurrencyrate"));

            HashMap<String,Object> GlobalParams = AccountingManager.getGlobalParams(request);

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

//            CreditNote creditnote = new CreditNote();
            String entryNumber = request.getParameter("number");
            currencyid = (request.getParameter("currencyid")==null?kwlcurrency.getCurrencyID():request.getParameter("currencyid"));
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class,currencyid);
            curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

//            String q="from CreditNote where creditNoteNumber=? and company.companyID=?";
//            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                throw new AccountingE xception("Credit note number '"+entryNumber+"' already exists.");
            result = accCreditNoteDAOobj.getCNFromNoteNo(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if(count > 0){
                throw new AccountingException("Credit note number '"+entryNumber+"' already exists.");
            }
            String nextCNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_CREDITNOTE);
//            creditnote.setCreditNoteNumber(entryNumber);
//            creditnote.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_CREDITNOTE).equals(entryNumber));
//            creditnote.setMemo(request.getParameter("memo"));
//            creditnote.setDeleted(false);
//            creditnote.setCompany(company);
//            creditnote.setCurrency(currency);
            HashMap<String,Object> credithm = new HashMap<String,Object>();
            credithm.put("entrynumber", entryNumber);
            credithm.put("autogenerated", nextCNAutoNo.equals(entryNumber));
            credithm.put("memo", request.getParameter("memo"));
            credithm.put("companyid", company.getCompanyID());
            credithm.put("currencyid", currencyid);

            Long seqNumber = null;
//            String query = "select count(cn.ID) from CreditNote cn inner join cn.journalEntry je  where cn.company.companyID=? and je.entryDate<=?";
//            List list = HibernateUtil.executeQuery(session, query, new Object[]{AuthHandler.getCompanyid(request), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate"))});
            result = accCreditNoteDAOobj.getCNSequenceNo(companyid, creationDate);
            List list = result.getEntityList();
            if (!list.isEmpty()) {
                seqNumber = (Long) list.get(0);
            }
//            creditnote.setSequence(seqNumber.intValue());
            credithm.put("sequence", seqNumber.intValue());

//            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
//                    request.getParameter("memo"), "JE" + creditnote.getCreditNoteNumber(),currencyid, hs,request);
//            creditnote.setJournalEntry(journalEntry);
            String costCenterId = request.getParameter("costCenterId");
            boolean jeautogenflag = true;
            String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
            String jeentryNumber = nextJEAutoNo;// +"/"+ entryNumber;
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
            credithm.put("journalentryid", jeid);

//            Double totalAmount = saveCreditNoteRows(session, request, creditnote, company, hs, preferences,kwlcurrency);
            List CNlist = saveCreditNoteRows2(GlobalParams, request, company, currency, journalEntry, preferences, externalCurrencyRate);
            Double totalAmount = (Double) CNlist.get(0);
            Double discAccAmount = (Double) CNlist.get(1);
            HashSet<CreditNoteDetail> cndetails = (HashSet<CreditNoteDetail>) CNlist.get(2);
            jedetails = (HashSet<JournalEntryDetail>) CNlist.get(3);
            reloadInventory = (Boolean) CNlist.get(4);
            returnJobj.put("reloadInventory", reloadInventory);
//            double amountDiff=oldcreditnoteRowsAmount(session, request, creditnote, company, jArr);
//
            //TODO   saveCreditNoteDiscountRows(session, request, creditnote, company);

//            if(jArr.length()>0){
//                amount = saveReceiptRows(session, request, receipt, company, jArr);
//                amountDiff=oldReceiptRowsAmount(session, request, receipt, company, jArr);
//                if(amountDiff-amount!=0&&preferences.getForeignexchange()!=null){
//                    amount=amount+(amountDiff-amount);
//                    hs = new HashSet();
    /****
                    JournalEntryDetail jed = new JournalEntryDetail();
                    jed.setCompany(company);
    ****/
//                    jed.setAmount(amountDiff-amount);
//                    jed.setAccount(preferences.getForeignexchange().getID());
//                    jed.setDebit(true);
//                    hs.add(jed);
//                }
//            }
//            JournalEntryDetail jed = new JournalEntryDetail();
//            jed.setCompany(company);
     /****
            jed.setAmount(totalAmount);
            jed.setAccount((Account) session.get(Account.class, request.getParameter("accid")));
            jed.setDebit(false);
     ****/
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", totalAmount);
            jedjson.put("accountid", request.getParameter("accid"));
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);


            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            result = accCreditNoteDAOobj.addCreditNote(credithm);
            creditnote = (CreditNote)result.getEntityList().get(0);

            credithm.put("cnid", creditnote.getID());
            Iterator itr = cndetails.iterator();
            while (itr.hasNext()) {
                CreditNoteDetail cnd = (CreditNoteDetail) itr.next();
                cnd.setCreditNote(creditnote);
            }
            credithm.put("cndetails", cndetails);

            result = accCreditNoteDAOobj.updateCreditNote(credithm);
            creditnote = (CreditNote)result.getEntityList().get(0);
            auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_CREATED, "User "+sessionHandlerImpl.getUserFullName(request) +" created new credit note ", request, creditnote.getID());
//            session.saveOrUpdate(creditnote);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveCreditNote : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveCreditNote : "+ex.getMessage(), ex);
        }catch (Exception ex){
        	ex.printStackTrace();
        }
        return creditnote;
    }

    private List saveCreditNoteRows(HashMap GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        List resultlist = new ArrayList();
        double totalAmount = 0;
        double totalTax=0;
        double discAccAmount = 0.0;
        HashSet cndetails = new HashSet();
        HashSet jedetails = new HashSet();

        JournalEntryDetail jed;
        KwlReturnObject result;
        
        boolean reloadInventory = false;
        String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
        JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
        List list = new ArrayList();
        boolean includeTax = StringUtil.getBoolean(request.getParameter("includetax"));
        for (int i = 0; i < jArr.length(); i++) {
            double taxamount = 0;
            double amount = 0;
            JSONObject jobj = jArr.getJSONObject(i);
            CreditNoteDetail row = new CreditNoteDetail();
            row.setSrno(i+1);
            double disc = jobj.getDouble("discamount");
            row.setCompany(company);
//            row.setCreditNote(cn);
            row.setMemo(request.getParameter("memo"));
            row.setQuantity(jobj.getInt("remquantity"));
//            InvoiceDetail invoiceRow = (InvoiceDetail) session.get(InvoiceDetail.class, jobj.getString("rowid"));
            result = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), jobj.getString("rowid"));
            InvoiceDetail invoiceRow = (InvoiceDetail) result.getEntityList().get(0);

            row.setInvoiceRow(invoiceRow);
            Product product = invoiceRow.getInventory().getProduct();
//            Account account = (Account) session.get(Account.class, product.getSalesReturnAccount().getID());
            result = accountingHandlerDAOobj.getObject(Account.class.getName(), product.getSalesReturnAccount().getID());
            Account account = (Account) result.getEntityList().get(0);

            double percent=0;
            if (invoiceRow.getInvoice().getTax() != null) {
//                percent = CompanyHandler.getTaxPercent(session, request, invoiceRow.getInvoice().getJournalEntry().getEntryDate(), invoiceRow.getInvoice().getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), invoiceRow.getInvoice().getJournalEntry().getEntryDate(), invoiceRow.getInvoice().getTax().getID());
                percent = (Double) perresult.getEntityList().get(0);
            }

            if (jobj.getInt("typeid") == 2 || jobj.getInt("typeid") == 3) {
//                Inventory inventory = CompanyHandler.makeInventory(session, request, product, jobj.getInt("remquantity"), jobj.optString("desc"), true, (jobj.getInt("typeid") == 3 ? false : true));
                reloadInventory = true;
                JSONObject inventoryjson = new JSONObject();
                inventoryjson.put("productid", product.getID());
                inventoryjson.put("quantity", jobj.getInt("remquantity"));
                inventoryjson.put("description", jobj.optString("desc"));
                inventoryjson.put("carryin", true);
                inventoryjson.put("defective", (jobj.getInt("typeid") == 3 ? false : true));
                inventoryjson.put("newinventory", false);
                inventoryjson.put("companyid", company.getCompanyID());
                inventoryjson.put("updatedate", AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                Inventory inventory = (Inventory) invresult.getEntityList().get(0);
                row.setInventory(inventory);
                if (list.contains(account)) {
                    cndetails.add(row);
                    continue;
                }
                for (int k = 0; k < jArr.length(); k++) {
                    JSONObject jobj1 = jArr.getJSONObject(k);
//                    InvoiceDetail compInvoiceRow = (InvoiceDetail) session.get(InvoiceDetail.class, jobj1.getString("rowid"));
                    result = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), jobj1.getString("rowid"));
                    InvoiceDetail compInvoiceRow = (InvoiceDetail) result.getEntityList().get(0);
                    Product compProduct = compInvoiceRow.getInventory().getProduct();
                    
//                    Account compAccount = (Account) session.get(Account.class, compProduct.getSalesReturnAccount().getID());
                    result = accountingHandlerDAOobj.getObject(Account.class.getName(), compProduct.getSalesReturnAccount().getID());
                    Account compAccount = (Account) result.getEntityList().get(0);
                    if (account == compAccount && (jobj1.getInt("typeid") == 2 || jobj.getInt("typeid") == 3)) {
                        amount = amount + jobj1.getDouble("discamount");
                        list.add(compAccount);
                        if (disc > 0) {
//                            Discount discount = new Discount();
//                            discount.setDiscount(disc);
//                            discount.setInPercent(false);
//                            discount.setOriginalAmount(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("amount"),currencyid,null));
//                            discount.setCompany(company);
//                            session.save(discount);
                            JSONObject discjson = new JSONObject();
                            discjson.put("discount", disc);
                            discjson.put("inpercent", false);
                            KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                            discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                            discjson.put("companyid", company.getCompanyID());
                            KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                            Discount discount = (Discount) dscresult.getEntityList().get(0);
                            row.setDiscount(discount);
                            if (includeTax) {
                                taxamount = disc * percent / 100;
                                row.setTaxAmount(taxamount);
                                totalTax += taxamount;
                                if (includeTax && taxamount > 0) {
//                                    jed = new JournalEntryDetail();
//                                    jed.setCompany(company);
//                                    jed.setAmount(taxamount);
//                                    jed.setAccount(invoiceRow.getInvoice().getTax().getAccount());
//                                    jed.setDebit(true);
//                                    hs.add(jed);
                                    JSONObject jedjson = new JSONObject();
                                    jedjson.put("srno", jedetails.size()+1);
                                    jedjson.put("companyid", company.getCompanyID());
                                    jedjson.put("amount", taxamount);
                                    jedjson.put("accountid", invoiceRow.getInvoice().getTax().getAccount().getID());
                                    jedjson.put("debit", true);
                                    jedjson.put("jeid", je.getID());
                                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    jedetails.add(jed);
                                }
                            }
                        }
                    }
                }
            } else {
                if (includeTax) {
                    taxamount = disc * percent / 100;
                    row.setTaxAmount(taxamount);
                    totalTax += taxamount;
                    if (includeTax && taxamount > 0) {
//                        jed = new JournalEntryDetail();
//                        jed.setCompany(company);
//                        jed.setAmount(taxamount);
//                        jed.setAccount(invoiceRow.getInvoice().getTax().getAccount());
//                        jed.setDebit(true);
//                        hs.add(jed);
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size()+1);
                        jedjson.put("companyid", company.getCompanyID());
                        jedjson.put("amount", taxamount);
                        jedjson.put("accountid", invoiceRow.getInvoice().getTax().getAccount().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", je.getID());
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                    }
                }
                discAccAmount = discAccAmount + jobj.getDouble("discamount");
                cndetails.add(row);
                if (disc > 0) {
//                    Discount discount = new Discount();
//                    discount.setDiscount(disc);
//                    discount.setInPercent(false);
//                    discount.setOriginalAmount(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("amount"),currencyid,null));
//                    discount.setCompany(company);
//                    session.save(discount);
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", disc);
                    discjson.put("inpercent", false);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                    discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                }
                continue;
            }
            cndetails.add(row);
//            jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(amount);
//            jed.setAccount(account);
//            jed.setDebit(true);
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", amount);
            jedjson.put("accountid", account.getID());
            jedjson.put("debit", true);
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
//            jed.setAccount((Account) session.get(Account.class, preferences.getDiscountGiven().getID()));
//            jed.setDebit(true);
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", discAccAmount);
            jedjson.put("accountid", preferences.getDiscountGiven().getID());
            jedjson.put("debit", true);
            jedjson.put("jeid", je.getID());
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
            totalAmount += discAccAmount;
        }
//        cn.setRows(cndetails);
        resultlist.add(totalAmount + totalTax);
        resultlist.add(discAccAmount);
        resultlist.add(cndetails);
        resultlist.add(jedetails);
        resultlist.add(reloadInventory);
        return resultlist;
    }


    public ModelAndView getCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            KwlReturnObject result = accCreditNoteDAOobj.getCreaditNote(requestParams);
            jobj = getCreditNoteJson(request, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public HashMap<String, Object> getCreditNoteMap (HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(Constants.start, request.getParameter(Constants.start));
        requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        requestParams.put(CCConstants.REQ_costCenterId,request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
        return requestParams;
    }

    public JSONObject getCreditNoteJson(HttpServletRequest request, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            double tax=0;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                CreditNote creditMemo = (CreditNote) row[0];
                JournalEntry je = creditMemo.getJournalEntry();
                Customer customer = (Customer) row[1];
                JournalEntryDetail details = (JournalEntryDetail) row[2];
                JSONObject obj = new JSONObject();
                obj.put("noteid", creditMemo.getID());
                obj.put("noteno", creditMemo.getCreditNoteNumber());
                obj.put("journalentryid", je.getID());
                obj.put("currencysymbol", (creditMemo.getCurrency()==null?currency.getSymbol():creditMemo.getCurrency().getSymbol()));
                obj.put("currencyid", (creditMemo.getCurrency()==null?currency.getCurrencyID():creditMemo.getCurrency().getCurrencyID()));
                obj.put("entryno", je.getEntryNumber());
                obj.put("personid", customer.getID());
                obj.put("personname", customer.getAccount().getName());
                obj.put("amount", details.getAmount());
                obj.put("date", df.format(je.getEntryDate()));
                obj.put("memo", creditMemo.getMemo());
                obj.put("costcenterid", je.getCostcenter()==null?"":je.getCostcenter().getID());
                obj.put("costcenterName", je.getCostcenter()==null?"":je.getCostcenter().getName());
                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), sessionHandlerImpl.getCompanyid(request));
                Iterator iterator = result.getEntityList().iterator();
                while(iterator.hasNext()){
                	JournalEntryDetail jed = (JournalEntryDetail)iterator.next();
                	Account account=null;
                	account=jed.getAccount();
                	if(account.getGroup().getID().equals(Group.OTHER_CURRENT_LIABILITIES)){
                		if(jed.isDebit()){
                	      tax = jed.getAmount(); 
                		}
                    }
                }
                obj.put("noteSubTotal", details.getAmount() - tax);
                obj.put("notetax", tax);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getCreditNoteJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView getCreditNoteRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getCreditNoteRows(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "accCreditNoteController.getCreditNoteRows:" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getCreditNoteRows(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            String[] creditNote = request.getParameterValues("bills");
            int i = 0;
            JSONArray jArr = new JSONArray();

            HashMap<String, Object> cnRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("creditNote.ID");
            order_by.add("srno");
            order_type.add("asc");
            cnRequestParams.put("filter_names", filter_names);
            cnRequestParams.put("filter_params", filter_params);
            cnRequestParams.put("order_by", order_by);
            cnRequestParams.put("order_type", order_type);
            
            while (creditNote != null && i < creditNote.length) {
//                CreditNote cn = (CreditNote) session.get(CreditNote.class, creditNote[i]);
                KwlReturnObject result = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), creditNote[i]);
                CreditNote cn = (CreditNote) result.getEntityList().get(0);
//                Iterator itr = cn.getRows().iterator();
                filter_params.clear();
                filter_params.add(cn.getID());
                KwlReturnObject grdresult = accCreditNoteDAOobj.getCreditNoteDetails(cnRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();
                
                while (itr.hasNext()) {
                    CreditNoteDetail row = (CreditNoteDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", cn.getID());
                    obj.put("billno", cn.getCreditNoteNumber());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getInvoiceRow().getInventory().getProduct().getID());
                    obj.put("productname", row.getInvoiceRow().getInventory().getProduct().getName());
                    obj.put("desc", row.getInvoiceRow().getInventory().getProduct().getDescription());
                    obj.put("memo", row.getMemo());
                    obj.put("currencysymbol", (cn.getCurrency() == null ? currency.getCurrencyID() : cn.getCurrency().getSymbol()));
                    obj.put("transectionid", row.getInvoiceRow().getInvoice().getID());
                    obj.put("transectionno", row.getInvoiceRow().getInvoice().getInvoiceNumber());
                    Discount disc = row.getDiscount();
                    if (disc != null) {
                        obj.put("discount", disc.getDiscountValue());
                    } else {
                        obj.put("discount", 0);
                    }
                    obj.put("quantity", row.getQuantity());
                    obj.put("taxamount", row.getTaxAmount());
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCreditNoteRows : "+ex.getMessage(), ex);
        }
        return jobj;
    }


    public ModelAndView deleteCreditNotes(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteCreditNotes(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.creditN.dels", null, RequestContextUtils.getLocale(request));   //"Credit Note(s) has been deleted successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteCreditNotes(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("noteid"))) {
                    String cnid = URLDecoder.decode(jobj.getString("noteid"),StaticValues.ENCODING);
//                    query = "update CreditNote set deleted=true where ID in("+qMarks +") and company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                      KwlReturnObject result = accCreditNoteDAOobj.deleteCreditNote(cnid, companyid);
                      
//                    query = "update JournalEntry je set je.deleted=true  where je.ID in(select cn.journalEntry.ID from CreditNote cn where cn.ID in( " + qMarks + ") and cn.company.companyID=je.company.companyID) and je.company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                      result = accCreditNoteDAOobj.getJEFromCN(cnid);
                      List list = result.getEntityList();
                      Iterator itr = list.iterator();
                      while(itr.hasNext()) {
                          String jeid = (String) itr.next();
                          result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                      }

//                    query = "update Discount di set di.deleted=true  where di.ID in(select cnd.discount.ID from CreditNoteDiscount cnd where cnd.creditNote.ID in( " + qMarks + ") and cnd.company.companyID=di.company.companyID) and di.company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                      result = accCreditNoteDAOobj.getCNDFromCN(cnid);
                      list = result.getEntityList();
                      itr = list.iterator();
                      while(itr.hasNext()) {
                          String discountid = (String) itr.next();
                          result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                      }

                    /*
                    query = "update Discount di set di.deleted=true  where di.ID in(select cnd.discount.ID from CreditNoteDetail cnd where cnd.creditNote.ID in( " + qMarks + ") and cnd.company.companyID=di.company.companyID) and di.company.companyID=?";
                    HibernateUtil.executeUpdate(session, query, params.toArray());*/
                    result = accCreditNoteDAOobj.getCNDFromCND(cnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = (String) itr.next();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }
                    
                    /*
                    query = "update Inventory inv set inv.deleted=true  where inv.ID in(select cnd.inventory.ID from CreditNoteDetail cnd where cnd.creditNote.ID in( " + qMarks + ") and cnd.company.companyID=inv.company.companyID) and inv.company.companyID=?";
                    HibernateUtil.executeUpdate(session, query, params.toArray());
                     */
                    result = accCreditNoteDAOobj.getCNIFromCND(cnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String inventoryid = (String) itr.next();
                        result = accProductObj.deleteInventoryEntry(inventoryid, companyid);
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
    }


    public ModelAndView saveBillingCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try{
            BillingCreditNote creditnote = saveBillingCreditNote(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.creditN.save", null, RequestContextUtils.getLocale(request));   //"Credit Note has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public BillingCreditNote saveBillingCreditNote(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        BillingCreditNote creditnote = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            DateFormat df = authHandler.getDateFormatter(request);
            HashMap<String,Object> GlobalParams = AccountingManager.getGlobalParams(request);

            Date creationDate = df.parse(request.getParameter("creationdate"));
            double externalCurrencyRate= StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), companyid);
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
//            KWLCurrency kwlcurrency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KWLCurrency kwlcurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);

            currencyid=(request.getParameter("currencyid")==null?kwlcurrency.getCurrencyID():request.getParameter("currencyid"));
            KWLCurrency currency=(KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);

            String entryNumber=request.getParameter("number");
//            String q="from BillingCreditNote where creditNoteNumber=? and company.companyID=?";
//            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                throw new AccountingException("Credit note number '"+entryNumber+"' already exists.");
            KwlReturnObject result = accCreditNoteDAOobj.getBCNFromNoteNo(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if(count > 0){
                throw new AccountingException("Credit note number '"+entryNumber+"' already exists.");
            }
            String nextCNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGCREDITNOTE);
//            creditnote.setCreditNoteNumber(entryNumber);
//            creditnote.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_BILLINGCREDITNOTE).equals(entryNumber));
//            creditnote.setMemo(request.getParameter("memo"));
//            creditnote.setDeleted(false);
//            creditnote.setCompany(company);
//            creditnote.setCurrency(currency);
            HashMap<String,Object> cnDataMap = new HashMap<String,Object>();
            cnDataMap.put("entrynumber", entryNumber);
            cnDataMap.put("autogenerated", nextCNAutoNo.equals(entryNumber));
            cnDataMap.put("memo", request.getParameter("memo"));
            cnDataMap.put("companyid", company.getCompanyID());
            cnDataMap.put("currencyid", currency.getCurrencyID());

            Long seqNumber = null;
//            String query = "select count(cn.ID) from BillingCreditNote cn inner join cn.journalEntry je  where cn.company.companyID=? and je.entryDate<=?";
//            List list = HibernateUtil.executeQuery(session, query, new Object[]{AuthHandler.getCompanyid(request), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate"))});
            result = accCreditNoteDAOobj.getBCNSequenceNo(companyid, creationDate);
            List list = result.getEntityList();
            if (!list.isEmpty()) {
                seqNumber = (Long) list.get(0);
            }
//            creditnote.setSequence(seqNumber.intValue());
            cnDataMap.put("sequence", seqNumber.intValue());

//            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
//                    request.getParameter("memo"), "JE" + creditnote.getCreditNoteNumber(),currencyid,externalCurrencyRate, hs,request);
//            creditnote.setJournalEntry(journalEntry);
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
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            if(!StringUtil.isNullOrEmpty(costCenterId)){
                jeDataMap.put("costcenterid", costCenterId);
            }
            jeDataMap.put("currencyid", currencyid);
            HashSet<JournalEntryDetail> jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            cnDataMap.put("journalentryid", jeid);

//            Double totalAmount = saveBillingCreditNoteRows(session, request, creditnote, company, hs, preferences,kwlcurrency,externalCurrencyRate);
            List CNlist = saveBillingCreditNoteRows(GlobalParams, request, company, currency, journalEntry, preferences, externalCurrencyRate);
            Double totalAmount = (Double) CNlist.get(0);
            Double discAccAmount = (Double) CNlist.get(1);
            HashSet<CreditNoteDetail> cndetails = (HashSet<CreditNoteDetail>) CNlist.get(2);
            jedetails = (HashSet<JournalEntryDetail>) CNlist.get(3);
//            double amountDiff=oldcreditnoteRowsAmount(session, request, creditnote, company, jArr);
//
            //TODO   saveCreditNoteDiscountRows(session, request, creditnote, company);

//            if(jArr.length()>0){
//                amount = saveReceiptRows(session, request, receipt, company, jArr);
//                amountDiff=oldReceiptRowsAmount(session, request, receipt, company, jArr);
//                if(amountDiff-amount!=0&&preferences.getForeignexchange()!=null){
//                    amount=amount+(amountDiff-amount);
//                    hs = new HashSet();
    /****
                    JournalEntryDetail jed = new JournalEntryDetail();
                    jed.setCompany(company);
    ****/
//                    jed.setAmount(amountDiff-amount);
//                    jed.setAccount(preferences.getForeignexchange());
//                    jed.setDebit(true);
//                    hs.add(jed);
//                }
//            }
//            JournalEntryDetail jed = new JournalEntryDetail();
//            jed.setCompany(company);
    /****
            jed.setAmount(totalAmount);
            jed.setAccount((Account) session.get(Account.class, request.getParameter("accid")));
            jed.setDebit(false);
    ****/
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size()+1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", totalAmount);
            jedjson.put("accountid", request.getParameter("accid"));
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);


            jeDataMap.put("jedetails", jedetails);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            result = accCreditNoteDAOobj.saveBillingCreditNote(cnDataMap);
            creditnote = (BillingCreditNote)result.getEntityList().get(0);

            cnDataMap.put("id", creditnote.getID());
            Iterator itr = cndetails.iterator();
            while (itr.hasNext()) {
                BillingCreditNoteDetail cnd = (BillingCreditNoteDetail) itr.next();
                cnd.setCreditNote(creditnote);
            }
            cnDataMap.put("cndetails", cndetails);

            result = accCreditNoteDAOobj.saveBillingCreditNote(cnDataMap);
            creditnote = (BillingCreditNote)result.getEntityList().get(0);
//            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
//                    request.getParameter("memo"), "JE" + creditnote.getCreditNoteNumber(),currencyid,externalCurrencyRate, hs,request);
//            creditnote.setJournalEntry(journalEntry);
//            ProfileHandler.insertAuditLog(session, AuditAction.CREDIT_NOTE_CREATED, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) +" created new credit note ", request);
            auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_CREATED, "User "+sessionHandlerImpl.getUserFullName(request) +" created new credit note ", request, creditnote.getID());

//            session.saveOrUpdate(creditnote);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveCreditNote : "+ex.getMessage(), ex);
        } catch (ParseException pe) {
            throw ServiceException.FAILURE("saveCreditNote : "+pe.getMessage(), pe);
        } catch (SessionExpiredException see) {
            throw ServiceException.FAILURE("saveCreditNote : "+see.getMessage(), see);
        }
        return creditnote;
    }

    private List saveBillingCreditNoteRows(HashMap GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws ServiceException, AccountingException, SessionExpiredException {
        List resultlist = new ArrayList();
        double totalAmount = 0;
        double totalTax=0;
        double discAccAmount = 0.0;
        HashSet cndetails = new HashSet();
        HashSet jedetails = new HashSet();

        JournalEntryDetail jed;
        try {
            boolean includeTax = StringUtil.getBoolean(request.getParameter("includetax"));
            String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));
            JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
            List list = new ArrayList();
            for (int i = 0; i < jArr.length(); i++) {
                double taxamount = 0;
                double amount = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                BillingCreditNoteDetail row = new BillingCreditNoteDetail();
                row.setSrno(i+1);
                double disc = jobj.getDouble("discamount");
                row.setCompany(company);
//                row.setCreditNote(cn);
                row.setMemo(request.getParameter("memo"));
                row.setQuantity(jobj.getDouble("remquantity"));
                BillingInvoiceDetail invoiceRow = (BillingInvoiceDetail) kwlCommonTablesDAOObj.getClassObject(BillingInvoiceDetail.class.getName(), jobj.getString("rowid"));
                row.setInvoiceRow(invoiceRow);
                String product = invoiceRow.getProductDetail();
                double percent = 0;
                if (invoiceRow.getBillingInvoice().getTax() != null) {
//                    percent = CompanyHandler.getTaxPercent(session, request, invoiceRow.getBillingInvoice().getJournalEntry().getEntryDate(), invoiceRow.getBillingInvoice().getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), invoiceRow.getBillingInvoice().getJournalEntry().getEntryDate(), invoiceRow.getBillingInvoice().getTax().getID());
                    percent = (Double) perresult.getEntityList().get(0);
                }
                if (includeTax) {
                    taxamount = disc * percent / 100;
                    row.setTaxAmount(taxamount);
                    totalTax += taxamount;
                    if (includeTax && taxamount > 0) {
//                        jed = new JournalEntryDetail();
//                        jed.setCompany(company);
//                        jed.setAmount(taxamount);
//                        jed.setAccount(invoiceRow.getBillingInvoice().getTax().getAccount());
//                        jed.setDebit(true);
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size()+1);
                        jedjson.put("companyid", company.getCompanyID());
                        jedjson.put("amount", taxamount);
                        jedjson.put("accountid", invoiceRow.getBillingInvoice().getTax().getAccount().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", je.getID());
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                    }
                }
                discAccAmount = discAccAmount + jobj.getDouble("discamount");
                cndetails.add(row);
                if (disc > 0) {
//                    Discount discount = new Discount();
//                    discount.setDiscount(disc);
//                    discount.setInPercent(false);
//                    discount.setOriginalAmount(CompanyHandler.getBaseToCurrencyAmount(session, request, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate));
//                    discount.setCompany(company);
//                    session.save(discount);
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", disc);
                    discjson.put("inpercent", false);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                    discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                }
            }
            if (discAccAmount != 0.0) {
//                jed = new JournalEntryDetail();
//                jed.setCompany(company);
//                jed.setAmount(discAccAmount);
//                jed.setAccount((Account) session.get(Account.class, preferences.getDiscountGiven().getID()));
//                jed.setDebit(true);
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size()+1);
                jedjson.put("companyid", company.getCompanyID());
                jedjson.put("amount", discAccAmount);
                jedjson.put("accountid", preferences.getDiscountGiven().getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", je.getID());
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
                totalAmount += discAccAmount;
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CompanyHandler.saveDebitNote", ex);
        }
//        cn.setRows(rows);
//        return totalAmount + totalTax;
        resultlist.add(totalAmount + totalTax);
        resultlist.add(discAccAmount);
        resultlist.add(cndetails);
        resultlist.add(jedetails);
        return resultlist;
    }

    public ModelAndView getBillingCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            KwlReturnObject result = accCreditNoteDAOobj.getBillingCreaditNote(requestParams);
            jobj = getBillingCreditNoteJson(request, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getBillingCreditNoteJson(HttpServletRequest request, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            double tax=0;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");

            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                BillingCreditNote creditMemo = (BillingCreditNote) row[0];
                JournalEntry je = creditMemo.getJournalEntry();
                Customer customer = (Customer) row[1];
                JournalEntryDetail details = (JournalEntryDetail) row[2];
                JSONObject obj = new JSONObject();
                obj.put("noteid", creditMemo.getID());
                obj.put("noteno", creditMemo.getCreditNoteNumber());
                obj.put("journalentryid", je.getID());
                obj.put("currencysymbol", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));
                obj.put("currencyid", (creditMemo.getCurrency()==null?currency.getCurrencyID():creditMemo.getCurrency().getCurrencyID()));
                obj.put("entryno", je.getEntryNumber());
                obj.put("personid", customer.getID());
                obj.put("personname", customer.getAccount().getName());
                obj.put("amount", details.getAmount());
                obj.put("date", df.format(je.getEntryDate()));
                obj.put("memo", creditMemo.getMemo());
                obj.put("costcenterid", je.getCostcenter()==null?"":je.getCostcenter().getID());
                obj.put("costcenterName", je.getCostcenter()==null?"":je.getCostcenter().getName());
                
                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), sessionHandlerImpl.getCompanyid(request));
                Iterator iterator = result.getEntityList().iterator();
                while(iterator.hasNext()){
                	JournalEntryDetail jed = (JournalEntryDetail)iterator.next();
                	Account account=null;
                	account=jed.getAccount();
                	if(account.getGroup().getID().equals(Group.OTHER_CURRENT_LIABILITIES)){
                		if(jed.isDebit()){
                	      tax = jed.getAmount(); 
                		}
                    }
                }
                obj.put("noteSubTotal", details.getAmount() - tax);
                obj.put("notetax", tax);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBillingCreditNoteJson : " + ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("getBillingCreditNoteJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView getBillingCreditNoteRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getBillingCreditNoteRows(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getBillingCreditNoteRows(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            String[] creditNote = request.getParameterValues("bills");
            int i = 0;
            JSONArray jArr = new JSONArray();

            HashMap<String, Object> cnRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("creditNote.ID");
            order_by.add("srno");
            order_type.add("asc");
            cnRequestParams.put("filter_names", filter_names);
            cnRequestParams.put("filter_params", filter_params);
            cnRequestParams.put("order_by", order_by);
            cnRequestParams.put("order_type", order_type);

            while (creditNote != null && i < creditNote.length) {
                BillingCreditNote cn = (BillingCreditNote) kwlCommonTablesDAOObj.getClassObject(BillingCreditNote.class.getName(), creditNote[i]);
//                Iterator itr = cn.getRows().iterator();
                filter_params.clear();
                filter_params.add(cn.getID());
                KwlReturnObject grdresult = accCreditNoteDAOobj.getBillingCreditNoteDetails(cnRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();

                while (itr.hasNext()) {
                    BillingCreditNoteDetail row = (BillingCreditNoteDetail) itr.next();
                    BillingInvoiceDetail invRow = row.getInvoiceRow();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", cn.getID());
                    obj.put("billno", cn.getCreditNoteNumber());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", invRow.getProductDetail());
                    obj.put("productdetail", invRow.getProductDetail());
                    obj.put("desc", invRow.getProductDetail());
                    obj.put("memo", row.getMemo());
                    obj.put("currencysymbol", (cn.getCurrency() == null ? currency.getCurrencyID() : cn.getCurrency().getSymbol()));
                    obj.put("transectionid", row.getInvoiceRow().getBillingInvoice().getID());
                    obj.put("transectionno", row.getInvoiceRow().getBillingInvoice().getBillingInvoiceNumber());
                    Discount disc = row.getDiscount();
                    if (disc != null) {
                        obj.put("discount", disc.getDiscountValue());
                    } else {
                        obj.put("discount", 0);
                    }
                    obj.put("quantity", row.getQuantity());
                    obj.put("taxamount", row.getTaxAmount());
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBillingCreditNoteRows : "+ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView deleteBillingCreditNotes(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteBillingCreditNotes(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.creditN.dels", null, RequestContextUtils.getLocale(request));   //"Credit Note(s) has been deleted successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteBillingCreditNotes(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("noteid"))) {
                    String cnid = URLDecoder.decode(jobj.getString("noteid"),StaticValues.ENCODING);

//                    query = "update BillingCreditNote set deleted=true where ID in("+qMarks +") and company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    KwlReturnObject result = accCreditNoteDAOobj.deleteBillingCreditNote(cnid, companyid);
                    
//                    query = "update JournalEntry je set je.deleted=true  where je.ID in(select cn.journalEntry.ID from BillingCreditNote cn where cn.ID in( " + qMarks + ") and cn.company.companyID=je.company.companyID) and je.company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    result = accCreditNoteDAOobj.getJEFromBCN(cnid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeid = (String) itr.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                    }
                    
//                    query = "update Discount di set di.deleted=true  where di.ID in(select cnd.discount.ID from BillingCreditNoteDiscount cnd where cnd.creditNote.ID in( " + qMarks + ") and cnd.company.companyID=di.company.companyID) and di.company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    result = accCreditNoteDAOobj.getCNDFromBCN(cnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = (String) itr.next();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }
                    
//                    query = "update Discount di set di.deleted=true  where di.ID in(select cnd.discount.ID from BillingCreditNoteDetail cnd where cnd.creditNote.ID in( " + qMarks + ") and cnd.company.companyID=di.company.companyID) and di.company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    result = accCreditNoteDAOobj.getCNDFromBCND(cnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = (String) itr.next();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
        } catch (JSONException ex) {
            throw new AccountingException("Cannot extract data from client");
        }
    }

    public ModelAndView exportCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            KwlReturnObject result = accCreditNoteDAOobj.getCreaditNote(requestParams);
            jobj = getCreditNoteJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView exportBillingCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            KwlReturnObject result = accCreditNoteDAOobj.getBillingCreaditNote(requestParams);
            jobj = getBillingCreditNoteJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    private List saveCreditNoteRows2(HashMap GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        List resultlist = new ArrayList();
        double totalAmount = 0;
        double totalTax=0, prodTax = 0;
        double discAccAmount = 0.0;
        HashSet cndetails = new HashSet();
        HashSet jedetails = new HashSet();

        JournalEntryDetail jed;
        KwlReturnObject result;
        
        boolean reloadInventory = false;
        String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
        JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
        List list = new ArrayList();
        boolean includeTax = StringUtil.getBoolean(request.getParameter("includetax"));
        
        
        
        
        for (int i = 0; i < jArr.length(); i++) {
            double taxamount = 0;
            double amount = 0, amount1 = 0;
            prodTax = 0;
            discAccAmount = 0.0;
            JSONObject jobj = jArr.getJSONObject(i);
            CreditNoteDetail row = new CreditNoteDetail();
            row.setSrno(i+1);
            double disc = jobj.getDouble("discamount");
            row.setCompany(company);
            row.setMemo(request.getParameter("memo"));
            row.setQuantity(jobj.getInt("remquantity"));
            result = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), jobj.getString("rowid"));
            InvoiceDetail invoiceRow = (InvoiceDetail) result.getEntityList().get(0);

            row.setInvoiceRow(invoiceRow);
            Product product = invoiceRow.getInventory().getProduct();
            result = accountingHandlerDAOobj.getObject(Account.class.getName(), product.getSalesReturnAccount().getID());
            Account account = (Account) result.getEntityList().get(0);

            double percent=0;
            if (invoiceRow.getInvoice().getTax() != null) {
                KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), invoiceRow.getInvoice().getJournalEntry().getEntryDate(), invoiceRow.getInvoice().getTax().getID());
                percent = (Double) perresult.getEntityList().get(0);
            }

            
            
            
            
            
            
            if (jobj.getInt("typeid") == 2 || jobj.getInt("typeid") == 3) {
                
            	
///////////////////////////////////////////////   Reload Inventory            	
            	reloadInventory = true;
                JSONObject inventoryjson = new JSONObject();
                inventoryjson.put("productid", product.getID());
                inventoryjson.put("quantity", jobj.getInt("remquantity"));
                inventoryjson.put("description", jobj.optString("desc"));
                inventoryjson.put("carryin", true);
                inventoryjson.put("defective", true);
                inventoryjson.put("newinventory", false);
                inventoryjson.put("companyid", company.getCompanyID());
                inventoryjson.put("updatedate", AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                Inventory inventory = (Inventory) invresult.getEntityList().get(0);
                row.setInventory(inventory);
///////////////////////////////////////////////   Reload Inventory Over
                
                    result = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), jobj.getString("rowid"));
                    InvoiceDetail compInvoiceRow = (InvoiceDetail) result.getEntityList().get(0);
                    Product compProduct = compInvoiceRow.getInventory().getProduct();
                    
                    result = accountingHandlerDAOobj.getObject(Account.class.getName(), compProduct.getSalesReturnAccount().getID());
                    Account compAccount = (Account) result.getEntityList().get(0);
                    if (account == compAccount && (jobj.getInt("typeid") == 2 || jobj.getInt("typeid") == 3)) {
                        amount = jobj.getDouble("discamount");
                        amount1 = jobj.getDouble("discamount");
                        list.add(compAccount);
                        
                        
                        
                        if (disc > 0) {
                        	
/////////////////////////////////////////////    Total Tax                            
                            if (includeTax) {
                                taxamount = disc * percent / 100;
                                row.setTaxAmount(taxamount);
                                totalTax += taxamount;
                                if (includeTax && taxamount > 0) {
                                    JSONObject jedjson = new JSONObject();
                                    jedjson.put("srno", jedetails.size()+1);
                                    jedjson.put("companyid", company.getCompanyID());
                                    jedjson.put("amount", taxamount);
                                    jedjson.put("accountid", invoiceRow.getInvoice().getTax().getAccount().getID());
                                    jedjson.put("debit", true);
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
                            KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                            discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                            discjson.put("companyid", company.getCompanyID());
                            KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                            Discount discount = (Discount) dscresult.getEntityList().get(0);
                            row.setDiscount(discount);
                            cndetails.add(row);

/////////////////////////////////////////////    Discount Row Added Over                       

                            
                        }
                    }

/////////////////////////////////////////////    Product Tax                            
                    if(invoiceRow.getTax() != null){
        	            /*  Product level tax taken care of   */
        	
        	            KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), invoiceRow.getInvoice().getJournalEntry().getEntryDate(), invoiceRow.getTax().getID());
        	            percent = (Double) perresult.getEntityList().get(0);
        	            prodTax = percent * disc/(percent + 100);
//        	            amount = amount - prodTax;
        	                        
        	            JSONObject jedtaxjson = new JSONObject();
        	            jedtaxjson.put("srno", jedetails.size()+1);
        	            jedtaxjson.put("companyid", company.getCompanyID());
        	            jedtaxjson.put("amount", prodTax);
        	            jedtaxjson.put("accountid", invoiceRow.getTax().getAccount().getID());
        	            jedtaxjson.put("debit", true);
        	            jedtaxjson.put("jeid", je.getID());
        	            KwlReturnObject jedtaxresult = accJournalEntryobj.addJournalEntryDetails(jedtaxjson);
        	            jed = (JournalEntryDetail) jedtaxresult.getEntityList().get(0);
        	            jedetails.add(jed);
        	            
        	            /*  Product level tax taken care of   */
                	}
/////////////////////////////////////////////    Product Tax  Over                            
                    totalAmount = totalAmount + amount + taxamount; // + prodTax;

//                }
                    
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size()+1);
                    jedjson.put("companyid", company.getCompanyID());
                    jedjson.put("amount", (amount - prodTax));
                    jedjson.put("accountid", account.getID());
                    jedjson.put("debit", true);
                    jedjson.put("jeid", je.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                    
            } else {
                
                discAccAmount = jobj.getDouble("discamount");
                
                if (includeTax) {
                    taxamount = disc * percent / 100;
                    row.setTaxAmount(taxamount);
                    totalTax += taxamount;
                    if (includeTax && taxamount > 0) {
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size()+1);
                        jedjson.put("companyid", company.getCompanyID());
                        jedjson.put("amount", taxamount);
                        jedjson.put("accountid", invoiceRow.getInvoice().getTax().getAccount().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", je.getID());
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                    }
                }

                
                if (disc > 0) {
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", disc);
                    discjson.put("inpercent", false);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                    discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                    cndetails.add(row);
                }
                
                if(invoiceRow.getTax() != null){
    	            /*  Product level tax taken care of   */
    	
    	            KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), invoiceRow.getInvoice().getJournalEntry().getEntryDate(), invoiceRow.getTax().getID());
    	            percent = (Double) perresult.getEntityList().get(0);
    	            prodTax = percent * discAccAmount/(percent + 100);
    	                        
    	            JSONObject jedtaxjson = new JSONObject();
    	            jedtaxjson.put("srno", jedetails.size()+1);
    	            jedtaxjson.put("companyid", company.getCompanyID());
    	            jedtaxjson.put("amount", prodTax);
    	            jedtaxjson.put("accountid", invoiceRow.getTax().getAccount().getID());
    	            jedtaxjson.put("debit", true);
    	            jedtaxjson.put("jeid", je.getID());
    	            KwlReturnObject jedtaxresult = accJournalEntryobj.addJournalEntryDetails(jedtaxjson);
    	            jed = (JournalEntryDetail) jedtaxresult.getEntityList().get(0);
    	            jedetails.add(jed);
    	            
    	            /*  Product level tax taken care of   */
            	}
                
              if (discAccAmount != 0.0) {
              JSONObject jedjson = new JSONObject();
              jedjson.put("srno", jedetails.size()+1);
              jedjson.put("companyid", company.getCompanyID());
              jedjson.put("amount", (discAccAmount - prodTax));
              jedjson.put("accountid", preferences.getDiscountGiven().getID());
              jedjson.put("debit", true);
              jedjson.put("jeid", je.getID());
              KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
              jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
              jedetails.add(jed);
              
              totalAmount += discAccAmount + taxamount;
          }

                
            }
        }

        resultlist.add(totalAmount);  //resultlist.add(totalAmount + totalTax);
        resultlist.add(discAccAmount);
        resultlist.add(cndetails);
        resultlist.add(jedetails);
        resultlist.add(reloadInventory);
        return resultlist;
    }
    
}
