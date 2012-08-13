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
package com.krawler.spring.accounting.companypreferances;

import com.krawler.accounting.utils.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.YearLock;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
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
public class accCompanyPreferencesController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
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
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
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

    public ModelAndView saveCompanyAccountPreferences(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveCompanyAccountPreferences(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.cp.save", null, RequestContextUtils.getLocale(request));   //"Account Preferences have been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void saveCompanyAccountPreferences(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            HashMap<String, Object> prefMap = new HashMap<String, Object>();
            prefMap.put("fyfrom", authHandler.getDateFormatter(request).parse(request.getParameter("fyfrom")));
            prefMap.put("bbfrom", authHandler.getDateFormatter(request).parse(request.getParameter("bbfrom")));
            prefMap.put("firstfyfrom", authHandler.getDateFormatter(request).parse(request.getParameter("fyfrom")));
            prefMap.put("withoutinventory", !StringUtil.isNullOrEmpty(request.getParameter("withoutinventory")));
            if(!StringUtil.isNullOrEmpty(request.getParameter("withouttax1099")))
                prefMap.put("withouttax1099", request.getParameter("withouttax1099"));
            prefMap.put("emailinvoice", !StringUtil.isNullOrEmpty(request.getParameter("emailinvoice")));
            prefMap.put("companyid", sessionHandlerImpl.getCompanyid(request));

            prefMap.put("discountgiven", request.getParameter("discountgiven"));
            prefMap.put("discountreceived", request.getParameter("discountreceived"));
            prefMap.put("shippingcharges", request.getParameter("shippingcharges"));
            prefMap.put("othercharges", request.getParameter("othercharges"));
            prefMap.put("cashaccount", request.getParameter("cashaccount"));
            prefMap.put("foreignexchange", request.getParameter("foreignexchange"));
            prefMap.put("depreciationaccount", request.getParameter("depreciationaccount"));

            prefMap.put("autojournalentry", request.getParameter("autojournalentry"));
            prefMap.put("autoinvoice", request.getParameter("autoinvoice"));
            prefMap.put("autocreditmemo", request.getParameter("autocreditmemo"));
            prefMap.put("autoreceipt", request.getParameter("autoreceipt"));
            prefMap.put("autogoodsreceipt", request.getParameter("autogoodsreceipt"));

            prefMap.put("autodebitnote", request.getParameter("autodebitnote"));
            prefMap.put("autopayment", request.getParameter("autopayment"));
            prefMap.put("autoso", request.getParameter("autoso"));
            prefMap.put("autopo", request.getParameter("autopo"));
            prefMap.put("autocashsales", request.getParameter("autocashsales"));

            prefMap.put("autocashpurchase", request.getParameter("autocashpurchase"));
            prefMap.put("autobillinginvoice", request.getParameter("autobillinginvoice"));
            prefMap.put("autobillingreceipt", request.getParameter("autobillingreceipt"));
            prefMap.put("autobillingcashsales", request.getParameter("autobillingcashsales"));
            prefMap.put("autobillingcashpurchase", request.getParameter("autobillingcashpurchase"));

            prefMap.put("autobillinggoodsreceipt", request.getParameter("autobillinggoodsreceipt"));
            prefMap.put("autobillingpayment", request.getParameter("autobillingpayment"));
            prefMap.put("autobillingso", request.getParameter("autobillingso"));
            prefMap.put("autobillingpo", request.getParameter("autobillingpo"));
            prefMap.put("autobillingdebitnote", request.getParameter("autobillingdebitnote"));
            prefMap.put("autobillingcreditmemo", request.getParameter("autobillingcreditmemo"));
            
            prefMap.put("autoquotation", request.getParameter("autoquotation"));

            CompanyAccountPreferences preferences;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);
            if (preferences == null) {
                preferences = new CompanyAccountPreferences();
                result = accCompanyPreferencesObj.addPreferences(prefMap);
            } else {
                prefMap.put("id", sessionHandlerImpl.getCompanyid(request));
                result = accCompanyPreferencesObj.updatePreferences(prefMap);
            }
            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);
            saveYearLock(request);
            auditTrailObj.insertAuditLog(AuditAction.COMPANY_ACCOUNT_PREFERENCES_UPDATE, "User "+ sessionHandlerImpl.getUserFullName(request) + " from "+preferences.getCompany().getCompanyName()+" changed company's account preferences", request, preferences.getID());
        } catch (ParseException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveCompanyAccountPreferences : "+ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveCompanyAccountPreferences : "+ex.getMessage(), ex);
        }
    }

    public ModelAndView getCompanyAccountPreferences(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) result.getEntityList().get(0);
            
            
            Calendar systemDate = Calendar.getInstance();
            Calendar financialYearFromTemp = Calendar.getInstance();
            financialYearFromTemp.setTime(pref.getFinancialYearFrom());
            Calendar financialYearFrom = Calendar.getInstance();
            financialYearFrom.setTime(pref.getFinancialYearFrom());
            financialYearFrom.set(Calendar.YEAR,financialYearFrom.get(Calendar.YEAR) + 1);
            if(systemDate.after(financialYearFrom)){
            	pref.setFinancialYearFrom(financialYearFrom.getTime());
            	accCompanyPreferencesObj.setNewYear(financialYearFrom.getTime(),sessionHandlerImpl.getCompanyid(request));
//            	accCompanyPreferencesObj.setCurrentYear(financialYearFrom.get(Calendar.YEAR),(financialYearFrom.get(Calendar.YEAR) - 1),sessionHandlerImpl.getCompanyid(request));
            }else if(systemDate.before(financialYearFromTemp)){
            	financialYearFromTemp.set(Calendar.YEAR,financialYearFromTemp.get(Calendar.YEAR) - 1);
            	pref.setFinancialYearFrom(financialYearFromTemp.getTime());
            	accCompanyPreferencesObj.setNewYear(financialYearFromTemp.getTime(),sessionHandlerImpl.getCompanyid(request));
//            	accCompanyPreferencesObj.setCurrentYear((financialYearFrom.get(Calendar.YEAR) - 1),(financialYearFrom.get(Calendar.YEAR) - 2),sessionHandlerImpl.getCompanyid(request));
            }
            JSONObject prefJobj = CompanyPreferencesCMN.getCompanyAccountPreferences(request, pref);
            jobj.put("data", prefJobj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getNextAutoNumber(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            int from=Integer.parseInt(request.getParameter("from"));
            String nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, from);
            jobj.put("data", nextAutoNumber);
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveYearLock(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveYearLock(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.cp.lock", null, RequestContextUtils.getLocale(request));   //"Lock has been Updated successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveYearLock(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            YearLock yearlock;
            KwlReturnObject result;
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> yearLockMap = new HashMap<String, Object>();
                yearLockMap.put("yearid", Integer.parseInt(URLDecoder.decode(jobj.getString("name"),StaticValues.ENCODING)));
                yearLockMap.put("islock", "true".equalsIgnoreCase(URLDecoder.decode(jobj.getString("islock"),StaticValues.ENCODING)));
                yearLockMap.put("companyid", companyid);

                String yearLockid = URLDecoder.decode(jobj.getString("id"),StaticValues.ENCODING);
                if (StringUtil.isNullOrEmpty(yearLockid)) {
//                    yearlock = new YearLock();
                    result = accCompanyPreferencesObj.addYearLock(yearLockMap);
                } else {
//                    yearlock = (YearLock) session.get(YearLock.class, jobj.getString("id"));
                    yearLockMap.put("id", yearLockid);
                    result = accCompanyPreferencesObj.updateYearLock(yearLockMap);
                }
                yearlock = (YearLock) result.getEntityList().get(0);
//                yearlock.setYearid(Integer.parseInt(jobj.getString("name")));
//                yearlock.setIsLock("true".equalsIgnoreCase(jobj.getString("islock")));
//                yearlock.setDeleted(false);
//                yearlock.setCompany(company);
//                session.saveOrUpdate(yearlock);
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveYearLock : "+ex.getMessage(), ex);
        }
    }

    public ModelAndView getYearLock(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        KwlReturnObject result;
        try{
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("CurrentFinancialYear", request.getParameter("CurrentFinancialYear"));
            if(request.getParameter("CurrentFinancialYear") == null)
            	result = accCompanyPreferencesObj.getYearLock(requestParams);
            else
            	result = accCompanyPreferencesObj.getYearLockforPreferences(requestParams);
            if(result != null && result.getEntityList().size() != 0){
            JSONArray DataJArr = CompanyPreferencesCMN.getYearLockJson(accCompanyPreferencesObj, requestParams, result.getEntityList());
	            jobj.put("data", DataJArr);
	            jobj.put("count", result.getRecordTotalCount());
	            issuccess = true;
            }else{
            	JSONArray DataJArr = CompanyPreferencesCMN.getYearLockJson(accCompanyPreferencesObj, requestParams, result.getEntityList());
            	jobj.put("data", DataJArr);
	            jobj.put("count", 0);
	            issuccess = true;
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
