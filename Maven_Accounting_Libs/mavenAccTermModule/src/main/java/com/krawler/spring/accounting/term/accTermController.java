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
package com.krawler.spring.accounting.term;

import com.krawler.accounting.utils.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Term;
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
public class accTermController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accTermDAO accTermObj;
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
    public void setaccTermDAO(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
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

    public ModelAndView getTerm(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject result = accTermObj.getTerm(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = getTermJson(list);
            jobj.put("data", jArr);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTermController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accTermController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTermController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getTermJson(List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while(itr.hasNext()) {
                Term ct = (Term) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("termid", ct.getID());
                obj.put("termname", ct.getTermname());
                obj.put("termdays", ct.getTermdays());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTermJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView saveTerm(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Term_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveTerm(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.term.update", null, RequestContextUtils.getLocale(request));   //"Term has been Updated successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accTermController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accTermController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTermController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveTerm(HttpServletRequest request) throws AccountingException, ServiceException, SessionExpiredException {
        try {
            int delCount = 0;
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String termid="";
            KwlReturnObject termresult;
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("termid")) == false) {
                    termid = jobj.getString("termid");
                    try {
                        termresult = accTermObj.deleteTerm(termid, companyid);
                        delCount += termresult.getRecordTotalCount();
                    } catch (ServiceException ex) {
                        throw new AccountingException(messageSource.getMessage("acc.uom.excp1", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
//            params.add(company.getCompanyID());
//            try {
//                delQuery = "delete from Term c where c.ID in(" + qMarks + ") and c.company.companyID=?";
//                delCount = HibernateUtil.executeUpdate(session, delQuery, params.toArray());
//            } catch (ServiceException ex) {
//                int type = 0;
//                String tablename = "in Transaction (s)";
//                String query = "from Customer c where c.creditTerm.ID not in(" + qMarks + ") and c.company.companyID=?";
//                list = HibernateUtil.executeQuery(session, query, params.toArray());
//                Iterator itr = list.iterator();
//                if (itr.hasNext()) {
//                    tablename = "by Customer(s)";
//                    type = 1;
//                }
//                query = "from Vendor v where v.debitTerm.ID not in(" + qMarks + ") and v.company.companyID=?";
//                list = HibernateUtil.executeQuery(session, query, params.toArray());
//                itr = list.iterator();
//                if (itr.hasNext()) {
//                    if (type == 1) {
//                        tablename = "by Vendor(s) and Customer(s)";
//                    } else {
//                        tablename = "by Vendor(s)";
//                    }
//                }
//                throw new AccountingException("Selected record(s) are currently used " + tablename);
//            }
            Term term;
            String auditMsg;
            String auditID;
//            String fullName = AuthHandler.getFullName(session, AuthHandler.getUserid(request));
            if (delCount > 0) {
                auditTrailObj.insertAuditLog(AuditAction.CREDIT_TERM_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " deleted " + delCount + " Credit Term", request, "0");
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }

                HashMap<String, Object> termMap = new HashMap<String, Object>();
                termMap.put("termname", URLDecoder.decode(jobj.getString("termname"),StaticValues.ENCODING));
                termMap.put("termdays", Integer.parseInt(URLDecoder.decode(jobj.getString("termdays"),StaticValues.ENCODING)));
                termMap.put("companyid", companyid);

                if (StringUtil.isNullOrEmpty(URLDecoder.decode(jobj.getString("termid"),StaticValues.ENCODING))) {
                    auditMsg = "added";
                    auditID = AuditAction.CREDIT_TERM_ADDED;
                    termresult = accTermObj.addTerm(termMap);
                } else {
                    auditMsg = "updated";
                    auditID = AuditAction.CREDIT_TERM_CHANGED;
                    termMap.put("termid", jobj.getString("termid"));
                    termresult = accTermObj.updateTerm(termMap);
                }
                term = (Term) termresult.getEntityList().get(0);

                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " " + auditMsg + " Credit Term to " + term.getTermname(), request, term.getID());
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.uom.excp2", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveTerm : "+ex.getMessage(), ex);
        }
    }

}
