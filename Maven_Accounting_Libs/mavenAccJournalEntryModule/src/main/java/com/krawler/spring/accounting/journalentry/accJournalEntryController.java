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


import com.krawler.accounting.utils.AuditAction;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class accJournalEntryController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accJournalEntryDAO accJournalEntryobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDaoObj;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }

    public ModelAndView saveJournalEntry(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="",id = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JournalEntry je = saveJournalEntry(request);
            issuccess = true;
            id = je.getID();
            msg = messageSource.getMessage("acc.je1.save", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("id", id);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JournalEntry saveJournalEntry(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry je = null;
        try {
            String companyid =  sessionHandlerImpl.getCompanyid(request);
            String entryNumber = request.getParameter("entryno");
            if(StringUtil.isNullOrEmpty(entryNumber)){
                throw new AccountingException(messageSource.getMessage("acc.je1.excp2", null, RequestContextUtils.getLocale(request)));
            }
            KwlReturnObject result = accJournalEntryobj.getJECount(entryNumber, companyid);
            int nocount = result.getRecordTotalCount();
            if (nocount > 0) {
                throw new AccountingException("Journal entry number '" + entryNumber + "' already exists.");
            }

            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            KwlReturnObject jedresult = accJournalEntryobj.getJEDset(jArr, companyid);
            HashSet jeDetails = (HashSet) jedresult.getEntityList().get(0);

            double externalCurrencyRate= StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateFormatter(request);
            String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
//            JournalEntry je=makeJournalEntry(session, company.getCompanyID(),
//                    AuthHandler.getDateFormatter(request).parse(request.getParameter("entrydate")),
//                    request.getParameter("memo"),
//                    request.getParameter("entryno"),
//                    hs,request);
            String costCenterId = request.getParameter(CCConstants.REQ_costcenter);
            Map<String,Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("entrydate", df.parse(request.getParameter("entrydate")));
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("entrynumber", entryNumber);
            jeDataMap.put("autogenerated", nextJEAutoNo.equals(entryNumber));
            jeDataMap.put("currencyid", request.getParameter("currencyid"));
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            if(!StringUtil.isNullOrEmpty(costCenterId)){
                jeDataMap.put(CCConstants.JSON_costcenterid, costCenterId);
            }
            jeDataMap.put("jedetails", jeDetails);            
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
            je = (JournalEntry) jeresult.getEntityList().get(0);
            auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User "+sessionHandlerImpl.getUserFullName(request) + " added new journal transaction: "+je.getEntryNumber(), request, je.getID());
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveJournalEntry : "+ex.getMessage(), ex);
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveJournalEntry : "+ex.getMessage(), ex);
        }
        return je;
    }

    public ModelAndView getJournalEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = getJournalEntryMap(request);
            if(request.getParameter("groupid") != null && Boolean.parseBoolean(request.getParameter("groupid")))
            	requestParams.put("groupid", true);
            KwlReturnObject result = accJournalEntryobj.getJournalEntry(requestParams);
            jobj = getJournalEntryJson(requestParams, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public HashMap<String, Object> getJournalEntryMap (HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.start, request.getParameter(Constants.start));
        requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(JournalEntryConstants.LINKID, request.getParameter(JournalEntryConstants.LINKID));
        requestParams.put(JournalEntryConstants.DELETED, request.getParameter(JournalEntryConstants.DELETED));
        requestParams.put(JournalEntryConstants.NONDELETED, request.getParameter(JournalEntryConstants.NONDELETED));
        requestParams.put(CCConstants.REQ_costCenterId,request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
        return requestParams;
    }

    public JSONObject getJournalEntryJson(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr=new JSONArray();
        try{
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            
            Iterator itr = list.iterator();
            while(itr.hasNext()) {
                JournalEntry entry = (JournalEntry) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("journalentryid", entry.getID());
                obj.put("entryno", entry.getEntryNumber());
                obj.put("currencysymbol",entry.getCurrency()==null?currency.getSymbol(): entry.getCurrency().getSymbol());
                obj.put("memo", entry.getMemo());
                obj.put("deleted", entry.isDeleted());
                obj.put("entrydate", df.format(entry.getEntryDate()));
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch(Exception ex) {
            throw ServiceException.FAILURE("getJournalEntryJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView getJournalEntryDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("dateformat", authHandler.getDateFormatter(request));
            requestParams.put("journalentryid", request.getParameter("journalentryid"));

            JSONArray DataJArr = getJournalEntryDetails(requestParams);
            jobj.put("data", DataJArr);
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
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getJournalEntryDetails(HashMap<String, Object> request) throws ServiceException {
        JSONArray jArr=new JSONArray();
        try {
            String jeid = (String) request.get("journalentryid");
//            Set details = ((JournalEntry)session.get(JournalEntry.class, request.getParameter("journalentryid"))).getDetails();
            KwlReturnObject result = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
            JournalEntry je = (JournalEntry) result.getEntityList().get(0);
//            Set details = ((JournalEntry) result.getEntityList().get(0)).getDetails();

//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), (String) request.get("gcurrencyid"));
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);

//            Iterator itr = details.iterator();
            HashMap<String, Object> jeRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("journalEntry.ID");
            filter_params.add(je.getID());
            order_by.add("debit");
            order_type.add("desc");
            jeRequestParams.put("filter_names", filter_names);
            jeRequestParams.put("filter_params", filter_params);
            jeRequestParams.put("order_by", order_by);
            jeRequestParams.put("order_type", order_type);
            KwlReturnObject jedresult = accJournalEntryobj.getJournalEntryDetails(jeRequestParams);
            Iterator itr = jedresult.getEntityList().iterator();

            while(itr.hasNext()) {
                JournalEntryDetail entry = (JournalEntryDetail) itr.next();
                String currencyid=entry.getJournalEntry().getCurrency()==null?currency.getCurrencyID(): entry.getJournalEntry().getCurrency().getCurrencyID();
                JSONObject obj = new JSONObject();
                obj.put("srno", entry.getSrno());
                obj.put("accountid", entry.getAccount().getID());
                obj.put("accountname", entry.getAccount().getName());
                obj.put("currencysymbol",entry.getJournalEntry().getCurrency()==null?currency.getSymbol(): entry.getJournalEntry().getCurrency().getSymbol());
                obj.put("description",entry.getDescription());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(request, entry.getAmount(), currencyid, entry.getJournalEntry().getEntryDate(),entry.getJournalEntry().getExternalCurrencyRate());
                if(entry.isDebit()==true) {
                    obj.put("debit", "Debit");
//                    obj.put("d_amount", CompanyHandler.getCurrencyToBaseAmount(session,request,entry.getAmount(),currencyid,entry.getJournalEntry().getEntryDate()));
                    obj.put("d_amount", bAmt.getEntityList().get(0));
                } else {
                    obj.put("debit", "Credit");
//                    obj.put("c_amount",CompanyHandler.getCurrencyToBaseAmount(session,request,entry.getAmount(),currencyid,entry.getJournalEntry().getEntryDate()));
                    obj.put("c_amount", bAmt.getEntityList().get(0));
                }
                jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getJournalEntryDetails : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView exportJournalEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getJournalEntryMap(request);
            KwlReturnObject result = accJournalEntryobj.getJournalEntry(requestParams);
            jobj = getJournalEntryJson(requestParams, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

}
