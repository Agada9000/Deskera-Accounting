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
package com.krawler.spring.accounting.purchaseorder;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BillingPurchaseOrder;
import com.krawler.hql.accounting.BillingPurchaseOrderDetail;
import com.krawler.hql.accounting.PurchaseOrder;
import com.krawler.hql.accounting.PurchaseOrderDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
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
public class accPurchaseOrderController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private String successView;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
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

    public ModelAndView savePurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
            PurchaseOrder purchaseorder = savePurchaseOrder(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.po.save1", null, RequestContextUtils.getLocale(request));   //"Purchase order has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    @SuppressWarnings("null")
	public PurchaseOrder savePurchaseOrder(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        PurchaseOrder purchaseOrder = null;
        try {
            String taxid=null;
            taxid=request.getParameter("taxid");
            double taxamount=StringUtil.getDouble("taxamount");
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            DateFormat df = authHandler.getDateFormatter(request);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
//            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
//            KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
//            Company company = (Company) cmp.getEntityList().get(0);

//            PurchaseOrder purchaseOrder = new PurchaseOrder();
            String entryNumber = request.getParameter("number");
            String costCenterId = request.getParameter("costcenter");
//            String q = "from PurchaseOrder where purchaseOrderNumber=? and company.companyID=?";
//            if (!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty()) {
//                throw new AccountingException("Purchase Order number '" + entryNumber + "' already exists.");
//            }
            String nextAutoNo;
            HashMap<String,Object> pohm = new HashMap<String, Object>();
            
            KwlReturnObject result = accPurchaseOrderobj.getPOCount(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if (count > 0) {
//                throw new AccountingException("Purchase Order number '" + entryNumber + "' already exists.");
                nextAutoNo = entryNumber;
                pohm.put("id",request.getParameter("invoiceid"));
                accPurchaseOrderobj.deletePurchaseOrderDetails(request.getParameter("invoiceid"), companyid);
            }
            else{
            	nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_PURCHASEORDER);
            }
            
//            purchaseOrder.setPurchaseOrderNumber(entryNumber);
//            purchaseOrder.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_PURCHASEORDER).equals(entryNumber));
//            purchaseOrder.setMemo(request.getParameter("memo"));
//            purchaseOrder.setVendor((Vendor) session.get(Vendor.class, request.getParameter("vendor")));
//            purchaseOrder.setOrderDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")));
//            purchaseOrder.setDueDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("duedate")));
//            purchaseOrder.setCompany(company);

            pohm.put("entrynumber", entryNumber);
            pohm.put("autogenerated", nextAutoNo.equals(entryNumber));
            pohm.put("memo", request.getParameter("memo"));
            pohm.put("vendorid", request.getParameter("vendor"));
            pohm.put("orderdate", df.parse(request.getParameter("billdate")));
            pohm.put("duedate", df.parse(request.getParameter("duedate")));
            if(!StringUtil.isNullOrEmpty(costCenterId)){
                pohm.put("costCenterId", costCenterId);
            }
            pohm.put("companyid", companyid);

            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
//                purchaseOrder.setTax(tax);
                pohm.put("taxid", taxid);
            }else{
            	pohm.put("taxid", taxid);     // Put taxid as null if the PO doesnt have any total tax included. (To avoid problem while editing PO)
            }
            
            KwlReturnObject poresult = accPurchaseOrderobj.savePurchaseOrder(pohm);
            purchaseOrder = (PurchaseOrder) poresult.getEntityList().get(0);

            HashSet podetails = savePurchaseOrderRows(request, purchaseOrder, companyid);
            
            pohm.put("id", purchaseOrder.getID());
            pohm.put("podetails",podetails);
            result = accPurchaseOrderobj.savePurchaseOrder(pohm);
            purchaseOrder = (PurchaseOrder) result.getEntityList().get(0);
//            savePurchaseOrderRows(session, request, purchaseOrder, company);
//            session.saveOrUpdate(purchaseOrder);
          //  list.add(purchaseOrder);
//            issuccess = true;
//            msg = "Purchase order has been saved successfully";
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("savePurchaseOrder : "+ex.getMessage(), ex);
         //   Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return purchaseOrder;
    }

    private HashSet savePurchaseOrderRows(HttpServletRequest request, PurchaseOrder purchaseOrder, String companyid) throws ServiceException, AccountingException {
        HashSet rows = new HashSet();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
//            PurchaseOrderDetail row=new PurchaseOrderDetail();
//            row.setCompany(company);
//            row.setPurchaseOrder(purchaseOrder);
//            row.setProduct((Product)session.get(Product.class, jobj.getString("productid")));
//            row.setRate(jobj.getDouble("rate"));
//            row.setQuantity( jobj.getInt("quantity"));
//            row.setRemark(jobj.optString("remark"));
                HashMap<String, Object> podDataMap = new HashMap<String, Object>();
                podDataMap.put("srno", i+1);
                podDataMap.put("companyid", companyid);
                podDataMap.put("poid", purchaseOrder.getID());
                podDataMap.put("productid", jobj.getString("productid"));
                podDataMap.put("rate", jobj.getDouble("rate"));
                podDataMap.put("quantity", jobj.getInt("quantity"));
                podDataMap.put("remark", jobj.optString("remark"));
                String rowtaxid = jobj.getString("prtaxid");
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(),rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null)
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    else
                        podDataMap.put("rowtaxid", rowtaxid);
                 }
                      //  row.setTax(rowtax);
                KwlReturnObject result = accPurchaseOrderobj.savePurchaseOrderDetails(podDataMap);
                PurchaseOrderDetail row = (PurchaseOrderDetail) result.getEntityList().get(0);
                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("savePurchaseOrderRows : " + ex.getMessage(), ex);
        }
        return rows;
    }

    public ModelAndView deletePurchaseOrders(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deletePurchaseOrders(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.po.del", null, RequestContextUtils.getLocale(request));   //"Purchase Order(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deletePurchaseOrders(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
//        ArrayList params = new ArrayList();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
//            String qMarks = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
//                    params.add(jobj.getString("billid"));
//                    qMarks += "?,";
                    String poid = URLDecoder.decode(jobj.getString("billid"),StaticValues.ENCODING);
//                    query = "update PurchaseOrder set deleted=true where ID in("+qMarks +") and company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    accPurchaseOrderobj.deletePurchaseOrder(poid, companyid);
                }
            }
//            params.add(company.getCompanyID());
//            qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
//            String query;
//            List list;
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }


    public ModelAndView saveBillingPurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
            BillingPurchaseOrder purchaseorder = saveBillingPurchaseOrder(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.so.save", null, RequestContextUtils.getLocale(request));   //"Purchase order has been saved successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public BillingPurchaseOrder saveBillingPurchaseOrder(HttpServletRequest request) throws ServiceException, AccountingException {
        BillingPurchaseOrder bPurchaseOrder = null;
        try {
            String taxid = null;
            taxid = request.getParameter("taxid");
            double taxamount = StringUtil.getDouble(request.getParameter("taxamount"));

            String companyid = sessionHandlerImpl.getCompanyid(request);
            String entryNumber = request.getParameter("number");
            String costCenterId = request.getParameter("costcenter");
            String nextAutoNumber;
            HashMap<String, Object> poDataMap = new HashMap<String, Object>();
            
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
//            BillingPurchaseOrder purchaseOrder = new BillingPurchaseOrder();
//            String entryNumber = request.getParameter("number");
//            String q = "from BillingPurchaseOrder where purchaseOrderNumber=? and company.companyID=?";
//            if (!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty()) {
//                throw new AccountingException("Purchase Order number '" + entryNumber + "' already exists.");
//            }
            KwlReturnObject pocnt = accPurchaseOrderobj.getBPOCount(entryNumber, companyid);
            if(pocnt.getRecordTotalCount()>0){
//                throw new AccountingException("Purchase Order number '" + entryNumber + "' already exists.");
            	nextAutoNumber = entryNumber;
            	poDataMap.put("id",request.getParameter("invoiceid"));
                accPurchaseOrderobj.deleteBillingPurchaseOrderDetails(request.getParameter("invoiceid"), companyid);
            }

//            purchaseOrder.setPurchaseOrderNumber(entryNumber);
//            purchaseOrder.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_BILLINGPURCHASEORDER).equals(entryNumber));

//            purchaseOrder.setMemo(request.getParameter("memo"));
//            purchaseOrder.setVendor((Vendor) session.get(Vendor.class, request.getParameter("vendor")));
//            if (!StringUtil.isNullOrEmpty(request.getParameter("creditoraccount"))) {
//                purchaseOrder.setDebitFrom((Account) session.get(Account.class, request.getParameter("creditoraccount")));
//            }
//            purchaseOrder.setOrderDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")));
//            purchaseOrder.setDueDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("duedate")));
//            purchaseOrder.setCompany(company);

            DateFormat df = authHandler.getDateFormatter(request);
            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGSALESORDER);

            poDataMap.put("entrynumber", entryNumber);
            poDataMap.put("autogenerated", nextAutoNumber.equals(entryNumber));
            poDataMap.put("memo", request.getParameter("memo"));
            poDataMap.put("vendorid", request.getParameter("vendor"));
            poDataMap.put("debitfrom", request.getParameter("creditoraccount"));
            poDataMap.put("orderdate", df.parse(request.getParameter("billdate")));
            poDataMap.put("duedate", df.parse(request.getParameter("duedate")));
            if(!StringUtil.isNullOrEmpty(costCenterId)){
                poDataMap.put("costCenterId", costCenterId);
            }
            poDataMap.put("companyid", companyid);

            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
//                purchaseOrder.setTax(tax);
                poDataMap.put("taxid", taxid);
            }else if(taxid.isEmpty()){
            	poDataMap.put("taxid", taxid);
            }
         
            //save Billing Sales Order
            KwlReturnObject result = accPurchaseOrderobj.saveBillingPurchaseOrder(poDataMap);
            bPurchaseOrder = (BillingPurchaseOrder) result.getEntityList().get(0);

            //save Billing Sales Order Details
            HashSet podetails = saveBillingPurchaseOrderRows(request, bPurchaseOrder, companyid);
            
            poDataMap.put("id", bPurchaseOrder.getID());
            poDataMap.put("podetails",podetails);
            result = accPurchaseOrderobj.saveBillingPurchaseOrder(poDataMap);
            bPurchaseOrder = (BillingPurchaseOrder) result.getEntityList().get(0);
//            saveBillingPurchaseOrderRows(session, request, purchaseOrder, company);
//            session.saveOrUpdate(purchaseOrder);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveBillingPurchaseOrder : "+ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveBillingPurchaseOrder : "+ex.getMessage(), ex);
        }
        return bPurchaseOrder;
    }

    private HashSet saveBillingPurchaseOrderRows(HttpServletRequest request, BillingPurchaseOrder purchaseOrder, String companyid) throws ServiceException, AccountingException {
        HashSet rows = new HashSet();
        try {
        JSONArray jArr = new JSONArray(request.getParameter("detail"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
//                BillingPurchaseOrderDetail row = new BillingPurchaseOrderDetail();
//                row.setCompany(company);
//                row.setPurchaseOrder(purchaseOrder);
//                row.setProductDetail(URLDecoder.decode(jobj.getString("productdetail"), StaticValues.ENCODING));
//                row.setRate(jobj.getDouble("rate"));
//                row.setQuantity(jobj.getInt("quantity"));
//                row.setRemark(jobj.optString("remark"));
                HashMap<String, Object> podDataMap = new HashMap<String, Object>();
                podDataMap.put("srno", i+1);
                podDataMap.put("companyid", companyid);
                podDataMap.put("poid", purchaseOrder.getID());
                podDataMap.put("rate", jobj.getDouble("rate"));
                podDataMap.put("quantity", jobj.getDouble("quantity"));
                podDataMap.put("remark", jobj.optString("remark"));
                podDataMap.put("productdetail", URLDecoder.decode(jobj.getString("productdetail"), StaticValues.ENCODING));
                String rowtaxid = jobj.getString("prtaxid");
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(),rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null)
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    else
                        podDataMap.put("rowtaxid", rowtaxid);
                 }
                      //  row.setTax(rowtax);
                KwlReturnObject result = accPurchaseOrderobj.saveBillingPurchaseOrderDetails(podDataMap);
                BillingPurchaseOrderDetail row = (BillingPurchaseOrderDetail) result.getEntityList().get(0);
                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveBillingPurchaseOrderRows : "+ex.getMessage(), ex);
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }
        return rows;
    }

    public ModelAndView deleteBillingPurchaseOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteBillingPurchaseOrders(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.so.del", null, RequestContextUtils.getLocale(request));   //"Purchase Order has been deleted successfully";
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteBillingPurchaseOrders(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String poid = URLDecoder.decode(jobj.getString("billid"),StaticValues.ENCODING);
                    accPurchaseOrderobj.deleteBillingPurchaseOrder(poid, companyid);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
    }
}
