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
package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.AccountingMsgs;
import com.krawler.hql.accounting.BillingInvoice;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.ExportRecord;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.JSONException;
import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;

/**
 *
 * @author krawler
 */
public class AopAdvisor implements MethodInterceptor {
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private ExportRecord ExportrecordObj;
    private accCreditNoteDAO accCreditNoteDao;
    private accReceiptDAO accReceiptDao;
    private accDebitNoteDAO accDebitNoteDao;
    private accVendorPaymentDAO accVendorPaymentDao;

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    public void setexportRecord(ExportRecord ExportrecordObj) {
        this.ExportrecordObj = ExportrecordObj;
    }
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDao) {
        this.accCreditNoteDao = accCreditNoteDao;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptDao) {
        this.accReceiptDao = accReceiptDao;
    }

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteDao) {
        this.accDebitNoteDao = accDebitNoteDao;
    }

    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentDao) {
        this.accVendorPaymentDao = accVendorPaymentDao;
    }
    
    public Object invoke(MethodInvocation mi) throws Throwable {
        Object valueReturn = null;
        
        ServletRequestAttributes ss = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        Object obj = mi.getThis();
        Class cls = mi.getThis().getClass();
        String className = cls.getSimpleName();
        boolean issuccess = true;

        if (className.equals("accInvoiceController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforInvoiceSaveMethods(ss.getRequest(), methodName);
                beforBillingInvoiceSaveMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("accGoodsReceiptController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforGoodsReceiptSaveMethods(ss.getRequest(), methodName);
                beforBillingGoodsReceiptSaveMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }
        else {
            valueReturn = mi.proceed();
        }

        return valueReturn;
    }

     private void beforInvoiceSaveMethods(HttpServletRequest request,  String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
            if (methodName.equals("saveInvoice") ) {
                String invoiceid =request.getParameter("invoiceid");
                String companyid = sessionHandlerImpl.getCompanyid(request);
                if (!StringUtil.isNullOrEmpty(invoiceid)){
                    KwlReturnObject result = accCreditNoteDao.getCNFromInvoice(invoiceid, companyid);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Selected record(s) is currently used in the Credit Note(s). So it cannot be edited.");
                    }
                    result = accReceiptDao.getReceiptFromInvoice(invoiceid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Payment against the selected Invoice(s) has been partially/fully received. So, it cannot be edited.");
                    }
                }
            }
    }
     private void beforBillingInvoiceSaveMethods(HttpServletRequest request,  String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
            if (methodName.equals("saveBillingInvoice") ) {
                String invoiceid =request.getParameter("invoiceid");
                String companyid = sessionHandlerImpl.getCompanyid(request);
                if (!StringUtil.isNullOrEmpty(invoiceid)){
                    KwlReturnObject result = accCreditNoteDao.getBillingCreditNoteDet(invoiceid, companyid);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Selected record(s) is currently used in the Credit Note(s). So it cannot be edited.");
                    }
                    result = accReceiptDao.getBReceiptFromBInvoice(invoiceid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Payment against the selected Invoice(s) has been partially/fully received. So, it cannot be edited.");
                    }
                }
            }
    }
    private void beforGoodsReceiptSaveMethods(HttpServletRequest request,  String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
            if (methodName.equals("saveGoodsReceipt") ) {
                String grid =request.getParameter("invoiceid");
                String companyid = sessionHandlerImpl.getCompanyid(request);
                if (!StringUtil.isNullOrEmpty(grid)){
                    KwlReturnObject result = accDebitNoteDao.getDNDetailsFromGReceipt(grid, companyid);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Selected record(s) is currently used in the Debit Note(s). So it cannot be edited.");
                    }
                    result = accVendorPaymentDao.getPaymentsFromGReceipt(grid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Payment against the selected Vendor Invoice(s) has been partially/fully made. So, it cannot be edited.");
                    }
                }
            }
    }
    private void beforBillingGoodsReceiptSaveMethods(HttpServletRequest request,  String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
            if (methodName.equals("saveBillingGoodsReceipt") ) {
                String grid =request.getParameter("invoiceid");
                String companyid = sessionHandlerImpl.getCompanyid(request);
                if (!StringUtil.isNullOrEmpty(grid)){
                    KwlReturnObject result = accDebitNoteDao.getBDNDetailsFromGReceipt(grid, companyid);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Selected record(s) is currently used in the Debit Note(s). So it cannot be edited.");
                    }
                    result = accVendorPaymentDao.getBillingPaymentsFromGReceipt(grid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Payment against the selected Vendor Invoice(s) has been partially/fully made. So, it cannot be edited.");
                    }
                }
            }
    }

    private void afterReturningAOPMethods(HttpServletRequest request, String className, String methodName, Object valueReturn) {
        try {
            if (methodName.equals("saveInvoice") || methodName.equals("saveBillingInvoice")) {
                ModelAndView mv = (ModelAndView) valueReturn;
                Map map = mv.getModel();
                String model = (String) map.get("model");
                JSONObject jobj = new JSONObject(model);

//                if(jobj.has("success") && jobj.getBoolean("success")) {
//                    if(jobj.has("invoiceid")) {
//                        String invoiceid = jobj.getString("invoiceid");
//                        Boolean cash = Boolean.parseBoolean(request.getParameter("incash"));
//                        if (!cash) {
//                            try {
//                                if (methodName.equals("saveInvoice")) {
//                                    sendMail(request, StaticValues.AUTONUM_INVOICE, invoiceid);
//                                } else if (methodName.equals("saveBillingInvoice")) {
//                                    sendMail(request, StaticValues.AUTONUM_BILLINGINVOICE, invoiceid);
//                                }
//                            } catch (Exception ex) {
//                            }
//                        }
//
//                    }
//                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendMail(HttpServletRequest request, int mode, String billid) {
        java.io.OutputStream os = null;
        {
            ByteArrayOutputStream baos = null;
            try {
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
                Company company = preferences.getCompany();
                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
                String currencyid = currency.getCurrencyID();
                DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
                String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                boolean isSend = false;
                String htmlMsg = "";
                String plainMsg = "";
                String subject = "";
                String[] emails = {};
                double amount = 0;
                Date invDate = new Date();
                String fromID = StringUtil.isNullOrEmpty(company.getEmailID())?authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID()):company.getEmailID();
                switch (mode) {
                    case StaticValues.AUTONUM_INVOICE:
                        Invoice inv = (Invoice) kwlCommonTablesDAOObj.getClassObject(Invoice.class.getName(), billid);
                        Customer customer = (Customer) kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(), inv.getCustomerEntry().getAccount().getID());
                        if (customer == null) {
                            return;
                        }
                        invDate = inv.getJournalEntry().getEntryDate();
                        ArrayList<String> em = new ArrayList<String>();
                        em.add(customer.getEmail());
                        emails = em.toArray(emails);
                        isSend = preferences.isEmailInvoice();
                        subject = AccountingMsgs.invoiceSubject;
                        htmlMsg = String.format(AccountingMsgs.invoiceHtmlMsg, customer.getName(), inv.getInvoiceNumber(), fromID, fromID);
                        plainMsg = String.format(AccountingMsgs.invoicePlainMsg, customer.getName(), inv.getInvoiceNumber(), fromID, fromID);
                        double disc = Double.parseDouble(request.getParameter("discount"));
                        amount = Double.parseDouble(request.getParameter("subTotal"));
                        if ("true".equals(request.getParameter("perdiscount"))) {
                            amount = amount - amount * disc / 100;
                        } else {
                            amount = amount - disc;
                        }
                        break;

                    case StaticValues.AUTONUM_BILLINGINVOICE:
                        BillingInvoice billinginv = (BillingInvoice) kwlCommonTablesDAOObj.getClassObject(BillingInvoice.class.getName(), billid);
                        Customer billingCustomer = (Customer) kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(), billinginv.getCustomerEntry().getAccount().getID());
                        if (billingCustomer == null) {
                            return;
                        }
                        invDate = billinginv.getJournalEntry().getEntryDate();
                        ArrayList<String> billingem = new ArrayList<String>();
                        billingem.add(billingCustomer.getEmail());
                        emails = billingem.toArray(emails);
                        isSend = preferences.isEmailInvoice();
                        subject = AccountingMsgs.invoiceSubject;
                        htmlMsg = String.format(AccountingMsgs.invoiceHtmlMsg, billingCustomer.getName(), billinginv.getBillingInvoiceNumber(), fromID, fromID);
                        plainMsg = String.format(AccountingMsgs.invoicePlainMsg, billingCustomer.getName(), billinginv.getBillingInvoiceNumber(), fromID, fromID);
                        double billingDisc = Double.parseDouble(request.getParameter("discount"));
                        amount = Double.parseDouble(request.getParameter("subTotal"));
                        if ("true".equals(request.getParameter("perdiscount"))) {
                            amount = amount - amount * billingDisc / 100;
                        } else {
                            amount = amount - billingDisc;
                        }
                        break;
                }
                String dateStr = "";
                try {
                    DateFormat df = new SimpleDateFormat("yyyyMMdd");
                    dateStr = df.format(invDate);
                } catch(Exception ex) {
                	Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }

                baos = ExportrecordObj.createPdf(request, currencyid, billid, formatter, mode, amount, logoPath, null, null, null,false);
                File destDir = new File(storageHandlerImpl.GetProfileImgStorePath(), "Invoice"+dateStr+".pdf");
                FileOutputStream oss = new FileOutputStream(destDir);
                baos.writeTo(oss);
                try {
                    if (emails.length > 0 && isSend) {
                        SendMailHandler.postMail(emails, subject, htmlMsg, plainMsg, fromID, new String[]{destDir.getAbsolutePath()});
                    }
                } catch (MessagingException e) {
                }
            } catch (DocumentException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ServiceException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    os.close();
                    baos.close();
                } catch (IOException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
