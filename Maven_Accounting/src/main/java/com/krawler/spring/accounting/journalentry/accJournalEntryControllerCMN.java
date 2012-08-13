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
package com.krawler.spring.accounting.journalentry;


import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Asset;
import com.krawler.hql.accounting.DepreciationDetail;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax1099Category;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.depreciation.accDepreciationDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
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
public class accJournalEntryControllerCMN extends MultiActionController implements JournalEntryConstants, MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accJournalEntryDAO accJournalEntryobj;
    private accBankReconciliationDAO accBankReconciliationObj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accReceiptDAO accReceiptDAOobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accVendorDAO accVendorDAOobj;
    private accTaxDAO accTaxObj;
    private accDebitNoteDAO accDebitNoteobj;
    private accDepreciationDAO accDepreciationObj;
    private String successView;
    private accAccountDAO accAccountDAOobj;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    public void setaccDepreciationDAO(accDepreciationDAO accDepreciationObj) {
        this.accDepreciationObj = accDepreciationObj;
    }
    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public ModelAndView deleteJournalEntries(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JEC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteJournalEntries(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.je1.del", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been deleted successfully";
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.deleteJournalEntries", ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.deleteJournalEntries", ex);
        } finally{
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.deleteJournalEntries", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteJournalEntries(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
//        ArrayList params = new ArrayList();
        try {
            JSONArray jArr = new JSONArray(request.getParameter(DATA));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("journalentryid"))) {
                    String jeid = URLDecoder.decode(jobj.getString("journalentryid"),StaticValues.ENCODING);
                    List list;
                    KwlReturnObject result;

//                    query = "from BankReconciliationDetail where journalEntry.ID in( " + qMarks + ") and bankReconciliation.deleted=false and company.companyID=?";
//                    list = HibernateUtil.executeQuery(session, query, params.toArray());
                    result = accBankReconciliationObj.getBRfromJE(jeid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("some of selected record(s) are currently associated with Bank reconciliation(s). delete <b>Bank reconciliation</b> instead.");
                    }

//                    query = "from BillingInvoice where journalEntry.ID in( " + qMarks + ") and deleted=false and company.companyID=?";
//                    list = HibernateUtil.executeQuery(session, query, params.toArray());
                    result = accInvoiceDAOobj.getBIFromJE(jeid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("some of selected record(s) are currently associated with Billing invoice(s). delete <b>Billing invoice</b> instead.");
                    }

//                    query = "from BillingReceipt where journalEntry.ID in( " + qMarks + ") and deleted=false and company.companyID=?";
//                    list = HibernateUtil.executeQuery(session, query, params.toArray());
                    result = accReceiptDAOobj.getBReciptFromJE(jeid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("some of selected record(s) are currently associated with Billing receipt(s). delete <b>Billing receipt</b> instead.");
                    }

//                    query = "from CreditNote where journalEntry.ID in( " + qMarks + ") and deleted=false and company.companyID=?";
//                    list = HibernateUtil.executeQuery(session, query, params.toArray());
                    result = accCreditNoteDAOobj.getCNFromJE(jeid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("some of selected record(s) are currently associated with Credit Note(s). delete <b>Credit Note</b> instead.");
                    }

//                    query = "from DebitNote where journalEntry.ID in( " + qMarks + ") and deleted=false and company.companyID=?";
//                    list = HibernateUtil.executeQuery(session, query, params.toArray());
                    result = accDebitNoteobj.getDNFromJE(jeid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("some of selected record(s) are currently associated with Debit Note(s). delete <b>Debit Note</b> instead.");
                    }

//                    query = "from DepreciationDetail where journalEntry.ID in( " + qMarks + ") and company.companyID=?";
//                    list = HibernateUtil.executeQuery(session, query, params.toArray());
//                    result = accDepreciationObj.getDepreciationFromJE(jeid, companyid);
//                    list = result.getEntityList();
//                    if (!list.isEmpty()) {
//                        throw new AccountingException("some of selected record(s) are currently associated with Depreciation Detail(s). delete <b>Depreciation Detail</b> instead.");
//                    }

//                    query = "from Invoice where journalEntry.ID in( " + qMarks + ") and deleted=false and company.companyID=?";
//                    list = HibernateUtil.executeQuery(session, query, params.toArray());
                    result = accInvoiceDAOobj.getInvoiceFromJE(jeid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("some of selected record(s) are currently associated with Invoice(s). delete <b>Invoice</b> instead.");
                    }

//                    query = "from GoodsReceipt where journalEntry.ID in( " + qMarks + ") and deleted=false and company.companyID=?";
//                    list = HibernateUtil.executeQuery(session, query, params.toArray());
                    result = accGoodsReceiptobj.getGRFromJE(jeid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("some of selected record(s) are currently associated with Vendor Invoice(s). delete <b>Vendor Invoice</b> instead.");
                    }

//                    query = "from Payment where journalEntry.ID in( " + qMarks + ") and deleted=false and company.companyID=?";
//                    list = HibernateUtil.executeQuery(session, query, params.toArray());
                    result = accVendorPaymentobj.getPaymentFromJE(jeid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("some of selected record(s) are currently associated with Payment(s). delete <b>Payment</b> instead.");
                    }

//                    query = "from Receipt where journalEntry.ID in( " + qMarks + ") and deleted=false and company.companyID=?";
//                    list = HibernateUtil.executeQuery(session, query, params.toArray());
                    result = accReceiptDAOobj.getReciptFromJE(jeid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("some of selected record(s) are currently associated with Receipt(s). delete <b>Receipt</b> instead.");
                    }

//                    query = "update JournalEntry set deleted=true where ID in("+qMarks +") and company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    deleteAssetEntries(request, jeid, companyid);
                    accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
        } catch (AccountingException ex) {
//            throw new AccountingException("Some of selected record(s) are currently associated with other transactions(s).<br>Delete <b>other transactions</b> instead.");
            throw new AccountingException(ex.getMessage());
        } catch (JSONException ex) {
            throw new AccountingException("Cannot extract data from client");
        }
    }

    public ModelAndView getTax1099JE(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            DateFormat df = authHandler.getDateFormatter(request);
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(CURDATE, df.parse(request.getParameter(CURDATE)));
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(SS, request.getParameter(SS));
            requestParams.put( DATEFORMAT,df);
            JSONArray DataJArr = getTax1099JEDetails(requestParams);
            String start = request.getParameter(START);
            String limit = request.getParameter(LIMIT);
            JSONArray jArr1 = new JSONArray();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                int st = Integer.parseInt(start);
                int ed = Math.min(DataJArr.length(), st + Integer.parseInt(limit));
                for (int i = st; i < ed; i++) {
                    jArr1.put(DataJArr.getJSONObject(i));
                }
            }
            jobj.put("data", jArr1);
            jobj.put("count", DataJArr.length());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accJournalEntryController.getJournalEntryDetails : "+ex.getMessage();
        } finally {
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getTax1099JEDetails(Map<String, Object> request) throws ServiceException {
        JSONArray jArr=new JSONArray();
        try {
            String ss=request.get(SS)==null?null:(String)request.get(SS);
            KwlReturnObject venresult = accVendorDAOobj.get1099EligibleVendor((String)request.get(COMPANYID),ss);
//            Iterator itr = venresult.getEntityList().iterator();
//            while(itr.hasNext()) {
//                Vendor vendor = (Vendor) itr.next();
           List<Vendor> vendorList = venresult.getEntityList();
            if(vendorList!=null && !vendorList.isEmpty()){
                for(Vendor vendor:vendorList){
                    KwlReturnObject jedresult = accJournalEntryobj.getTax1099AccJE((String)request.get(COMPANYID),(Date)request.get(CURDATE),vendor.getID());
//                    Iterator jeditr = jedresult.getEntityList().iterator();
//                    while(jeditr.hasNext()) {//                    getTax1099AccCategory
//                        Object[] row = (Object[]) jeditr.next();
                    List<Object[]> jedList = jedresult.getEntityList();
                    if(jedList!=null && !jedList.isEmpty()){
                        for(Object[] row:jedList){
                            JournalEntryDetail jed = (JournalEntryDetail) row[0];
                            request.put(ACCOUNTID, jed.getAccount().getID());
                            request.put(AMOUNT, (Double)row[1]);
                            JSONObject obj = new JSONObject();
                            KwlReturnObject catresult = accTaxObj.getTax1099AccCategory(request);
    //                        Iterator catitr = catresult.getEntityList().iterator();
    //                        if(catitr.hasNext()) {
    //                            Tax1099Category taxCat = (Tax1099Category) catitr.next();
                            List<Tax1099Category> taxCatList = catresult.getEntityList();
                            if(taxCatList!=null && !taxCatList.isEmpty()){
                                for(Tax1099Category taxCat:taxCatList){
                                    obj.put(CATEGORYID, taxCat.getID());
                                    obj.put(CATEGORYNAME, taxCat.getCategory() );
                                    obj.put(ABOVETHRESHOLD, (Double)row[1]>taxCat.getThresholdValue() );
                                }
                            }
                            obj.put(SRNO, jed.getSrno());
                            obj.put(ACCOUNTID, jed.getAccount().getID());
                            obj.put(ACCOUNTNAME, jed.getAccount().getName());
                            obj.put(PERSONID, vendor.getID());
                            obj.put(PERSONNAME, vendor.getName());
                            obj.put(AMOUNT, (Double)row[1]);

                            jArr.put(obj);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getJournalEntryDetails : "+ex.getMessage(), ex);
        }
        return jArr;
    }
    
    /**
     * @author Neeraj
     * @param je
     * @param companyid
     * @throws SessionExpiredException
     * @throws AccountingException
     * @throws ServiceException
     */
    public void deleteAssetEntries(HttpServletRequest request, String je, String companyid) throws SessionExpiredException, AccountingException, ServiceException {
    	KwlReturnObject purchase, sale, dep;
    	String asset = "";
    	int period=0;
    	try{
    		HashMap<String, Object> requestParams = new HashMap<String, Object>();
    		
    		requestParams.put("purchaseJe", je);
    		purchase = accDepreciationObj.getAsset(requestParams);
    		
    		
    		if(purchase != null && purchase.getEntityList().size() > 0){      //  Purchase Journal Entry Case
    			Asset asset1 = (Asset) purchase.getEntityList().get(0);
    			if(asset1.getDeleteJe() != null){
    				throw new AccountingException(messageSource.getMessage("acc.rem.157", null, RequestContextUtils.getLocale(request)));
    			}else{
    				requestParams.clear();
    				requestParams.put("accountid", asset1.getAccount().getID());
    				dep = accDepreciationObj.getDepreciation(requestParams);
    				
    				if(dep != null && dep.getEntityList().size() > 0){
    					throw new AccountingException(messageSource.getMessage("acc.rem.155", null, RequestContextUtils.getLocale(request)));
    				}else{
    					accAccountDAOobj.deleteAccount(asset1.getAccount().getID(), true);
    				}
    			}																//  Purchase Journal Entry Case
    			
    		}else{														        //  Sell off and Write Off Journal Entry Case
    			requestParams.clear();
    			requestParams.put("deleteJe", je);
    			sale = accDepreciationObj.getAsset(requestParams);
    			
    			if(sale != null && sale.getEntityList().size() > 0){
    				Asset asset1 = (Asset) sale.getEntityList().get(0);
    				asset = asset1.getAccount().getID();
    				requestParams.clear();
    				requestParams.put("id", asset);
    				requestParams.put("deleteJe", null);
    				requestParams.put("isSale", false);
    				requestParams.put("isWriteOff", false);
    				accDepreciationObj.addAssetDetail(requestParams);
    				
    				accAccountDAOobj.deleteAccount(asset1.getAccount().getID(), false);
    																			//  Sell off and Write Off Journal Entry Case
    			}else{
    				dep = accDepreciationObj.getDepreciationFromJE(je,companyid);	//  Asset Depreciation Journal Entry Case
    				if(dep != null && dep.getEntityList().size() > 0){
    					DepreciationDetail depreciationDetail = (DepreciationDetail) dep.getEntityList().get(0);
    					asset = depreciationDetail.getAccount().getID();
    					
    					requestParams.clear();
    					requestParams.put("id", asset);
    					sale = accDepreciationObj.getAsset(requestParams);
    					if(sale != null && sale.getEntityList().size() > 0){
    	    				Asset asset1 = (Asset) sale.getEntityList().get(0);
    	    				if(asset1.getDeleteJe() != null){
        						throw new AccountingException(messageSource.getMessage("acc.rem.154", null, RequestContextUtils.getLocale(request)));
    	    				}
    					}
    					
    					period = depreciationDetail.getPeriod() + 1;
    					
    					requestParams.clear();
    					requestParams.put("period", period);
    					requestParams.put("accountid", asset);
    					dep = accDepreciationObj.getDepreciation(requestParams);
    					
    					if(dep != null && dep.getEntityList().size() > 0){
    						throw new AccountingException(messageSource.getMessage("acc.rem.153", null, RequestContextUtils.getLocale(request)));
    					}else{
    						accDepreciationObj.deleteDepreciationJE(je);
    					}
    					
    				}																//  Asset Depreciation Journal Entry Case
    			}
    		}
    		
    	} catch (AccountingException ex) {
    		throw new AccountingException(ex.getMessage());
    	} catch (Exception ex){
    		throw ServiceException.FAILURE("deleteAssetEntries : "+ex.getMessage(), ex);
    	}
    }
    
}
