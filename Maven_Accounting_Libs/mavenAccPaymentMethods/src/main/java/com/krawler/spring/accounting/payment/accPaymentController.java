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
package com.krawler.spring.accounting.payment;

import com.krawler.accounting.utils.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.PaymentMethod;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
public class accPaymentController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accPaymentDAO accPaymentDAOobj;
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
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
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

    public ModelAndView savePaymentMethod(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PaymentMethods_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            savePaymentMethod(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.pay1.methodupdate", null, RequestContextUtils.getLocale(request));   //"Payment method has been Updated successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void savePaymentMethod(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            int delCount = 0;
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String methodid = "";
            KwlReturnObject methodresult;
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("methodid")) == false) {
                    methodid = jobj.getString("methodid");
                    try {
                        methodresult = accPaymentDAOobj.deletePaymentMethod(methodid, companyid);
                        delCount += methodresult.getRecordTotalCount();
                    } catch (ServiceException ex) {
                        throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");  ///messageSource.getMessage("acc.pay1.excp1", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            PaymentMethod pom;
            String auditMsg;
            String auditID;
//            String fullName = AuthHandler.getFullName(session, AuthHandler.getUserid(request));
            if (delCount > 0) {
                auditTrailObj.insertAuditLog(AuditAction.PAYMENT_METHOD_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " deleted " + delCount + " Payment Method", request, "0");
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }

                HashMap<String, Object> methodMap = new HashMap<String, Object>();
                methodMap.put("methodname", URLDecoder.decode(jobj.getString("methodname"),StaticValues.ENCODING));
                methodMap.put("accountid", URLDecoder.decode(jobj.getString("accountid"),StaticValues.ENCODING));
                methodMap.put("detailtype", jobj.getInt("detailtype"));
                methodMap.put("companyid", companyid);

                if (StringUtil.isNullOrEmpty(jobj.getString("methodid"))) {
                    auditMsg = "added";
                    auditID = AuditAction.PAYMENT_METHOD_ADDED;
                    methodresult = accPaymentDAOobj.addPaymentMethod(methodMap);
                } else {
                    auditMsg = "updated";
                    auditID = AuditAction.PAYMENT_METHOD_CHANGED;
                    methodMap.put("methodid", URLDecoder.decode(jobj.getString("methodid"),StaticValues.ENCODING));
                    methodresult = accPaymentDAOobj.updatePaymentMethod(methodMap);
                }
                pom = (PaymentMethod) methodresult.getEntityList().get(0);

                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " " + auditMsg + " Payment Method to " + pom.getMethodName(), request, pom.getID());
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("savePaymentMethod : "+ex.getMessage(), ex);
        }
    }

    public ModelAndView getPaymentMethods(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject result = accPaymentDAOobj.getPaymentMethod(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = getPaymentMethodsJson(list);
            jobj.put("data", jArr);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getPaymentMethodsJson(List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
       try {
            Iterator itr = list.iterator();
            while(itr.hasNext()) {
                PaymentMethod paymethod = (PaymentMethod) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("methodid", paymethod.getID());
                obj.put("methodname", paymethod.getMethodName());
                obj.put("accountid", paymethod.getAccount().getID());
                obj.put("accountname", paymethod.getAccount().getName());
                obj.put("detailtype", paymethod.getDetailType());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getPaymentMethodsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }




}
