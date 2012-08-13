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
package com.krawler.spring.accounting.salesorder;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BillingSalesOrder;
import com.krawler.hql.accounting.BillingSalesOrderDetail;
import com.krawler.hql.accounting.Quotation;
import com.krawler.hql.accounting.QuotationDetail;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
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
public class accSalesOrderController extends MultiActionController  implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accSalesOrderDAO accSalesOrderDAOobj;
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
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
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

    public ModelAndView saveSalesOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
        	SalesOrder saleSorder= saveSalesOrder(request);
        	issuccess = true;
        	msg = messageSource.getMessage("acc.so.save", null, RequestContextUtils.getLocale(request));   //"Sales order has been saved successfully";
        	txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
			Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public SalesOrder saveSalesOrder(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        SalesOrder salesOrder = null;
        try {
            String taxid = null;
            taxid = request.getParameter("taxid");
            double taxamount = StringUtil.getDouble(request.getParameter("taxamount"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String entryNumber = request.getParameter("number");
            String costCenterId = request.getParameter("costcenter");
            String nextAutoNumber;
            HashMap<String, Object> soDataMap = new HashMap<String, Object>();
            
            KwlReturnObject socnt = accSalesOrderDAOobj.getSalesOrderCount(entryNumber, companyid);
            if (socnt.getRecordTotalCount() > 0) {
//                throw new AccountingException("Sales Order number '" + entryNumber + "' already exists.");
            	nextAutoNumber = entryNumber;
            	soDataMap.put("id",request.getParameter("invoiceid"));
                accSalesOrderDAOobj.deleteSalesOrderDetails(request.getParameter("invoiceid"), companyid);
            }
            else{
            	nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_SALESORDER);
            }
            DateFormat df = authHandler.getDateFormatter(request);
            soDataMap.put("entrynumber", entryNumber);
            soDataMap.put("autogenerated", nextAutoNumber.equals(entryNumber));
            soDataMap.put("memo", request.getParameter("memo"));
            soDataMap.put("customerid", request.getParameter("customer"));
            soDataMap.put("orderdate", df.parse(request.getParameter("billdate")));
            soDataMap.put("duedate", df.parse(request.getParameter("duedate")));
            if(!StringUtil.isNullOrEmpty(costCenterId)){
                soDataMap.put("costCenterId", costCenterId);
            }
            soDataMap.put("companyid", companyid);
            
            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
                soDataMap.put("taxid", taxid);
            }else{
            	soDataMap.put("taxid", taxid);     // Put taxid as null if the SO doesnt have any total tax included. (To avoid problem while editing PO)
            }

            KwlReturnObject soresult = accSalesOrderDAOobj.saveSalesOrder(soDataMap);
            salesOrder = (SalesOrder) soresult.getEntityList().get(0);

            soDataMap.put("id", salesOrder.getID());
            HashSet sodetails = saveSalesOrderRows(request, salesOrder, companyid);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveSalesOrder : " + ex.getMessage(), ex);
        }
        return salesOrder;
    }

    public HashSet saveSalesOrderRows(HttpServletRequest request, SalesOrder SalesOrder, String companyid) throws ServiceException, AccountingException {
        HashSet rows = new HashSet();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            for(int i=0; i<jArr.length(); i++){
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> sodDataMap = new HashMap<String, Object>();
                sodDataMap.put("srno", i+1);
                sodDataMap.put("companyid", companyid);
                sodDataMap.put("soid", SalesOrder.getID());
                sodDataMap.put("productid", jobj.getString("productid"));
                sodDataMap.put("rate", jobj.getDouble("rate"));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                sodDataMap.put("quantity", jobj.getInt("quantity"));
                sodDataMap.put("remark", jobj.optString("remark"));
                String rowtaxid = jobj.getString("prtaxid");
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(),rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null)
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    else
                        sodDataMap.put("rowtaxid", rowtaxid);
                 }
                      //  row.setTax(rowtax);

                KwlReturnObject result = accSalesOrderDAOobj.saveSalesOrderDetails(sodDataMap);
                SalesOrderDetail row = (SalesOrderDetail) result.getEntityList().get(0);
                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveSalesOrderRows : " + ex.getMessage(), ex);
        }
        return rows;
    }


    public ModelAndView deleteSalesOrders(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteSalesOrders(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.so.del", null, RequestContextUtils.getLocale(request));   //"Sales Order has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteSalesOrders(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String soid = URLDecoder.decode(jobj.getString("billid"),StaticValues.ENCODING);
//            query = "update SalesOrder set deleted=true where ID in("+qMarks +") and company.companyID=?";
//            HibernateUtil.executeUpdate(session, query, params.toArray());
                    accSalesOrderDAOobj.deleteSalesOrder(soid, companyid);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

//Billing Sales order
    public ModelAndView saveBillingSalesOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
        	BillingSalesOrder bSalesOrder = saveBillingSalesOrder(request);
        	issuccess = true;
        	msg = messageSource.getMessage("acc.so.save", null, RequestContextUtils.getLocale(request));   //"Sales order has been saved successfully";
        	txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
			Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public BillingSalesOrder saveBillingSalesOrder(HttpServletRequest request) throws ServiceException, AccountingException {
        BillingSalesOrder bSalesOrder = null;
        try {
            String taxid = null;
            taxid = request.getParameter("taxid");
            double taxamount = StringUtil.getDouble(request.getParameter("taxamount"));

            String companyid = sessionHandlerImpl.getCompanyid(request);
            String entryNumber = request.getParameter("number");
            String costCenterId = request.getParameter("costcenter");
            String nextAutoNumber;
            HashMap<String, Object> soDataMap = new HashMap<String, Object>();
            
            KwlReturnObject socnt = accSalesOrderDAOobj.getBillingSalesOrderCount(entryNumber, companyid);
            if(socnt.getRecordTotalCount()>0){
//                throw new AccountingException("Sales Order number '" + entryNumber + "' already exists.");
            	nextAutoNumber = entryNumber;
            	soDataMap.put("id",request.getParameter("invoiceid"));
                accSalesOrderDAOobj.deleteBillingSalesOrderDetails(request.getParameter("invoiceid"), companyid);
            }else{
            	nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGSALESORDER);
            }

            DateFormat df = authHandler.getDateFormatter(request);
            soDataMap.put("entrynumber", entryNumber);
            soDataMap.put("autogenerated", nextAutoNumber.equals(entryNumber));
            soDataMap.put("memo", request.getParameter("memo"));
            soDataMap.put("customerid", request.getParameter("customer"));
            soDataMap.put("credito", request.getParameter("creditoraccount"));
            soDataMap.put("orderdate", df.parse(request.getParameter("billdate")));
            soDataMap.put("duedate", df.parse(request.getParameter("duedate")));
            if(!StringUtil.isNullOrEmpty(costCenterId)){
                soDataMap.put("costCenterId", costCenterId);
            }
            soDataMap.put("companyid", companyid);

            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
                soDataMap.put("taxid", taxid);
            }else if(!taxid.isEmpty()){
            	soDataMap.put("taxid", taxid);
            }

            //save Billing Sales Order
            KwlReturnObject result = accSalesOrderDAOobj.saveBillingSalesOrder(soDataMap);
            bSalesOrder = (BillingSalesOrder) result.getEntityList().get(0);

            //save Billing Sales Order Details
            saveBillingSalesOrderRows(request, bSalesOrder, companyid);



        } catch (ParseException ex) {
            throw ServiceException.PARSE_ERROR("saveBillingSalesOrder : "+ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveBillingSalesOrder : "+ex.getMessage(), ex);
        }
        return bSalesOrder;
    }

    private HashSet saveBillingSalesOrderRows(HttpServletRequest request, BillingSalesOrder salesOrder, String companyid) throws ServiceException, AccountingException {
        HashSet rows = new HashSet();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> sodDataMap = new HashMap<String, Object>();
                sodDataMap.put("srno", i+1);
                sodDataMap.put("companyid", companyid);
                sodDataMap.put("soid", salesOrder.getID());
                sodDataMap.put("rate", jobj.getDouble("rate"));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                sodDataMap.put("quantity", jobj.getDouble("quantity"));
                sodDataMap.put("remark", jobj.optString("remark"));
                sodDataMap.put("productdetail", URLDecoder.decode(jobj.getString("productdetail"), StaticValues.ENCODING));
                String rowtaxid = jobj.getString("prtaxid");
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(),rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null)
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    else
                        sodDataMap.put("rowtaxid", rowtaxid);
                 }
                      //  row.setTax(rowtax);
                KwlReturnObject result = accSalesOrderDAOobj.saveBillingSalesOrderDetails(sodDataMap);
                BillingSalesOrderDetail row = (BillingSalesOrderDetail) result.getEntityList().get(0);
                rows.add(row);
            }

        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveBillingSalesOrderRows : " + ex.getMessage(), ex);
        }
        return rows;
    }

    public ModelAndView deleteBillingSalesOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteBillingSalesOrders(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.so.del", null, RequestContextUtils.getLocale(request));   //"Sales Order has been deleted successfully";
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteBillingSalesOrders(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String soid = URLDecoder.decode(jobj.getString("billid"),StaticValues.ENCODING);
                    accSalesOrderDAOobj.deleteBillingSalesOrder(soid, companyid);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }
    
    public ModelAndView saveQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
        	Quotation quotation = saveQuotation(request);
        	issuccess = true;
        	msg = messageSource.getMessage("acc.so.save1", null, RequestContextUtils.getLocale(request));   //"Quotation has been saved successfully";
        	txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
			Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public Quotation saveQuotation(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
    	Quotation quotation = null;
        try {
            String taxid = null;
            taxid = request.getParameter("taxid");
            double taxamount = StringUtil.getDouble(request.getParameter("taxamount"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String entryNumber = request.getParameter("number");
            String costCenterId = request.getParameter("costcenter");
            String nextAutoNumber;
            HashMap<String, Object> qDataMap = new HashMap<String, Object>();
            
//            KwlReturnObject socnt = accSalesOrderDAOobj.getSalesOrderCount(entryNumber, companyid);
//            if (socnt.getRecordTotalCount() > 0) {
////                throw new AccountingException("Sales Order number '" + entryNumber + "' already exists.");
//            	nextAutoNumber = entryNumber;
//            	soDataMap.put("id",request.getParameter("invoiceid"));
//                accSalesOrderDAOobj.deleteSalesOrderDetails(request.getParameter("invoiceid"), companyid);
//            }
//            else{
            	nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_QUOTATION);
//            }
            DateFormat df = authHandler.getDateFormatter(request);
            qDataMap.put("entrynumber", entryNumber);
            qDataMap.put("autogenerated", nextAutoNumber.equals(entryNumber));
            qDataMap.put("memo", request.getParameter("memo"));
            qDataMap.put("customerid", request.getParameter("customer"));
            qDataMap.put("orderdate", df.parse(request.getParameter("billdate")));
            qDataMap.put("duedate", df.parse(request.getParameter("duedate")));
            qDataMap.put("perDiscount", StringUtil.getBoolean(request.getParameter("perdiscount")));
            qDataMap.put("discount", StringUtil.getDouble(request.getParameter("discount")));
            if(!StringUtil.isNullOrEmpty(costCenterId)){
                qDataMap.put("costCenterId", costCenterId);
            }
            qDataMap.put("companyid", companyid);
            
            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
                qDataMap.put("taxid", taxid);
            }

            KwlReturnObject soresult = accSalesOrderDAOobj.saveQuotation(qDataMap);
            quotation = (Quotation) soresult.getEntityList().get(0);

            qDataMap.put("id", quotation.getID());
            HashSet sodetails = saveQuotationRows(request, quotation, companyid);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveQuotation : " + ex.getMessage(), ex);
        }
        return quotation;
    }

    public HashSet saveQuotationRows(HttpServletRequest request, Quotation quotation, String companyid) throws ServiceException, AccountingException {
        HashSet rows = new HashSet();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            for(int i=0; i<jArr.length(); i++){
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put("srno", i+1);
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("soid", quotation.getID());
                qdDataMap.put("productid", jobj.getString("productid"));
                qdDataMap.put("rate", jobj.getDouble("rate"));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                qdDataMap.put("quantity", jobj.getInt("quantity"));
                qdDataMap.put("remark", jobj.optString("remark"));
                qdDataMap.put("discount", jobj.getDouble("prdiscount"));
                String rowtaxid = jobj.getString("prtaxid");
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(),rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null)
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    else
                        qdDataMap.put("rowtaxid", rowtaxid);
                 }
                      //  row.setTax(rowtax);

                KwlReturnObject result = accSalesOrderDAOobj.saveQuotationDetails(qdDataMap);
                QuotationDetail row = (QuotationDetail) result.getEntityList().get(0);
                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveQuotationRows : " + ex.getMessage(), ex);
        }
        return rows;
    }
    
    public ModelAndView deleteQuotations(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteQuotations(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.so.quotdel", null, RequestContextUtils.getLocale(request));   //"Quotation has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteQuotations(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String qid = URLDecoder.decode(jobj.getString("billid"),StaticValues.ENCODING);
                    accSalesOrderDAOobj.deleteQuotation(qid, companyid);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

}
