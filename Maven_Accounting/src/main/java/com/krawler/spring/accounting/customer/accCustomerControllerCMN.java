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
package com.krawler.spring.accounting.customer;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BillingInvoice;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceControllerCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
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
public class accCustomerControllerCMN extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accAccountDAO accAccountDAOobj;
    private accProductDAO accProductObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accPaymentDAO accPaymentDAOobj;
    private accTaxDAO accTaxObj;
    private accCustomerDAO accCustomerDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private String successView;
    private accInvoiceDAO accInvoiceDAOobj;
    private accInvoiceCMN accInvoiceCMNobj;
    private accReceiptDAO accReceiptDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private MessageSource messageSource;
    
    @Override
	public void setMessageSource(MessageSource msg) {
		this.messageSource = msg;
	}
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj){
    	this.exportDaoObj = exportDaoObj;
    }

    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
    public void setaccInvoiceCMN(accInvoiceCMN accInvoiceCMNobj) {
        this.accInvoiceCMNobj = accInvoiceCMNobj;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public ModelAndView deleteCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Customer_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("data", request.getParameter("data"));

            String companyid = sessionHandlerImpl.getCompanyid(request);
            deleteCustomer(requestParams, companyid);

            issuccess = true;
            msg = messageSource.getMessage("acc.cus.del", null, RequestContextUtils.getLocale(request));   //"Customer has been deleted successfully";
            txnManager.commit(status);
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteCustomer(HashMap request, String companyid) throws ServiceException, AccountingException {
        KwlReturnObject result = null;
        try{
            JSONArray jArr = new JSONArray((String)request.get("data"));
            String accountid = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                accountid = jobj.getString("accid");
                if (StringUtil.isNullOrEmpty(jobj.getString("accid")) == false) {
                    if (jobj.getDouble("openbalance") != 0) {
                        throw new AccountingException("Selected record(s) is having the Opening Balance. So it cannot be deleted");
                    } else {
                        // Check in Journal Entry
                        result = accJournalEntryobj.getJEDfromAccount(accountid, companyid);
                        int count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
                        }

                        // Check Product Entry
                        result = accProductObj.getProductfromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the Account Preferences. So it cannot be deleted.");
                        }

                        // Check for Preferances Entry
                        result = accCompanyPreferencesObj.getPreferencesFromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the Product(s). So it cannot be deleted.");
                        }

                        // Check fot Payment Entry
                        result = accPaymentDAOobj.getPaymentMethodFromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the Term(s). So it cannot be deleted.");
                        }

                        // Check for Tax Entry
                        result = accTaxObj.getTaxFromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the Tax(s). So it cannot be deleted.");
                        }

                        try {
//                        Delete Account
                            result = accCustomerDAOobj.deleteCustomer(accountid, companyid);
                            result = accAccountDAOobj.deleteAccount(accountid, companyid);
                        } catch (ServiceException ex) {
                            try {
                               result = accAccountDAOobj.deleteAccount(accountid, true);
                            } catch (ServiceException e) {
                                throw new AccountingException("Selected record(s) is currently used in the transaction(s).");
                            }
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("deleteAccount : "+ex.getMessage(), ex);
        }
    }

    /**
     * @author neeraj
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getCustomers(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = accCustomerController.getCustomerRequestMap(request);
            KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams);
            JSONArray jArr= accCustomerController.getCustomerJson(request, list);
            jArr = getCustomerAmountDue(jArr, request);
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCustomers : "+ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * @author neeraj
     * @param jArr
     * @param request
     * @return
     */
    public JSONArray getCustomerAmountDue(JSONArray jArr, HttpServletRequest request){
    	try{
    		HashMap<String, Object> requestParams = accInvoiceControllerCMN.getInvoiceRequestMap(request);
    		requestParams.put("nondeleted", "true");
    		requestParams.put("deleted", "false");
    		for(int i = 0; i < jArr.length(); i++){
    			String accid = jArr.getJSONObject(i).getString("accid");
                requestParams.put(InvoiceConstants.accid, accid);
                double amountdue = 0;
                KwlReturnObject result = accInvoiceDAOobj.getInvoices(requestParams);
                if(result.getEntityList() != null){
                	Iterator itr = result.getEntityList().iterator();
                    while (itr.hasNext()) {
                        Invoice invoice = (Invoice) itr.next();
                        amountdue = amountdue + accInvoiceCMNobj.getAmountDue(requestParams, invoice);
                    }
                    jArr.getJSONObject(i).put("amountdue", amountdue);
                }else{
                	jArr.getJSONObject(i).put("amountdue", 0);
                }
                result = accInvoiceDAOobj.getBillingInvoices(requestParams);
                if(result.getEntityList() != null){
                	Iterator itr = result.getEntityList().iterator();
                    while (itr.hasNext()) {
                    	double amount = 0, ramount;
                    	BillingInvoice billingInvoice = (BillingInvoice) itr.next();
                    	Iterator itrBir = accInvoiceCMNobj.applyBillingCreditNotes(requestParams, billingInvoice).values().iterator();
                        while (itrBir.hasNext()) {
                            Object[] temp = (Object[]) itrBir.next();
                            amount += (Double) temp[0]-(Double) temp[2];
                        }
                        JournalEntryDetail tempd = billingInvoice.getShipEntry();
                        if (tempd != null) {
                            amount += tempd.getAmount();
                        }
                        tempd = billingInvoice.getOtherEntry();
                        if (tempd != null) {
                            amount += tempd.getAmount();
                        }
                        tempd = billingInvoice.getTaxEntry();
                        if (tempd != null) {
                            amount += tempd.getAmount();
                        }
                        KwlReturnObject brdAmt = accReceiptDAOobj.getBillingReceiptAmountFromInvoice(billingInvoice.getID());
                        List l = brdAmt.getEntityList();
                        ramount = (l.isEmpty() ? 0 : (Double) l.get(0));
                        amountdue = amountdue + amount - ramount;
                    }
                    jArr.getJSONObject(i).put("amountdue", amountdue);
                }
    		}    
    		
    	}catch (Exception ex){
    		Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
    	}
    	return jArr;
    }
    
    public ModelAndView exportCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = accCustomerController.getCustomerRequestMap(request);
            KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams);
            JSONArray jArr= accCustomerController.getCustomerJson(request, list);
            jArr = getCustomerAmountDue(jArr, request);
            jobj.put("data", jArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
}

