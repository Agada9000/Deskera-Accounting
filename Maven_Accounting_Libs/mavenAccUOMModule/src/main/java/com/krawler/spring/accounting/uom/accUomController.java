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
package com.krawler.spring.accounting.uom;

import com.krawler.accounting.utils.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.spring.accounting.handler.AccountingManager;
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
public class accUomController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accUomDAO accUomObj;
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
    public void setaccUomDAO (accUomDAO accUomObj) {
        this.accUomObj = accUomObj;
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

    public ModelAndView getUnitOfMeasure(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);

            KwlReturnObject result = accUomObj.getUnitOfMeasure(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getUoMJson(request, list);
            jobj.put("data", DataJArr);
            jobj.put("count", count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getUoMJson(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                UnitOfMeasure uom = (UnitOfMeasure) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("uomid", uom.getID());
                obj.put("uomname", uom.getName());
                obj.put("precision", uom.getAllowedPrecision());
                obj.put("uomtype", uom.getType());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getUoMJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView saveUnitOfMeasure(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("UoM_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveUOM(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.uom.update", null, RequestContextUtils.getLocale(request));   //"Unit of measure has been updated successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveUOM(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String uomid = "";
            KwlReturnObject result;
            int delCount = 0;
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("uomid")) == false) {
                    uomid = jobj.getString("uomid");
                    try {
                        result = accUomObj.deleteUoM(uomid, companyid);
                        delCount += result.getRecordTotalCount();
                    } catch (ServiceException ex) {
                        throw new AccountingException(messageSource.getMessage("acc.uom.excp1", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            String auditMsg;
            String auditID;
            UnitOfMeasure uom;
            KwlReturnObject uomresult;
            HashMap<String, Object> uomMap;
//            String fullName = AuthHandler.getFullName(session, AuthHandler.getUserid(request));
            if (delCount > 0) {
                auditTrailObj.insertAuditLog(AuditAction.UNIT_OF_MEASURE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " deleted " + delCount + " unit of measure", request, uomid +"0");
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }

                uomMap = new HashMap<String, Object>();
                uomMap.put("uomname", URLDecoder.decode(jobj.getString("uomname"),StaticValues.ENCODING));
                uomMap.put("uomtype", URLDecoder.decode(jobj.getString("uomtype"),StaticValues.ENCODING));
                uomMap.put("precision", jobj.getInt("precision"));
                uomMap.put("companyid", companyid);

                if (StringUtil.isNullOrEmpty(URLDecoder.decode(jobj.getString("uomid"),StaticValues.ENCODING))) {
                    auditMsg = "added";
                    auditID = AuditAction.UNIT_OF_MEASURE_CREATED;
                    uomresult = accUomObj.addUoM(uomMap);
                } else {
                    auditMsg = "updated";
                    auditID = AuditAction.UNIT_OF_MEASURE_UPDATED;
                    uomMap.put("uomid", jobj.getString("uomid"));
                    uomresult = accUomObj.updateUoM(uomMap);
                }
                uom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " " + auditMsg + " unit of measure to " + uom.getName(), request, uom.getID());
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (AccountingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

}
