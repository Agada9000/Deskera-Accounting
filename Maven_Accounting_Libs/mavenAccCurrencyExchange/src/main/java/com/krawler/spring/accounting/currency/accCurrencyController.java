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
package com.krawler.spring.accounting.currency;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.DefaultExchangeRate;
import com.krawler.hql.accounting.DefaultExchangeRateDetails;
import com.krawler.hql.accounting.ExchangeRate;
import com.krawler.hql.accounting.ExchangeRateDetails;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Currency;
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
public class accCurrencyController extends MultiActionController implements CurrencyContants, MessageSourceAware{

    private HibernateTransactionManager txnManager;
    private accCurrencyDAO accCurrencyDAOobj;
    private String successView;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public ModelAndView saveCurrencyExchange(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Currency_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            boolean dateexist = false;
            dateexist = saveCurrencyExchange(request);
            jobj.put( DATEEXIST,dateexist);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.curex.update", null, RequestContextUtils.getLocale(request));   //"Currency Exchange Rate has been updated successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW,MODEL, jobj.toString());
    }

    public boolean saveCurrencyExchange(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        try {
            boolean updateRate = (request.getParameter("changerate")==null?false:Boolean.parseBoolean(request.getParameter("changerate")));
            JSONArray jArr = new JSONArray(request.getParameter(DATA));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                Date appDate = null;
                if(StringUtil.isNullOrEmpty(jobj.getString(APPLYDATE))){
                    throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, RequestContextUtils.getLocale(request)));
                }
                else{
                    appDate = authHandler.getDateOnlyFormatter(request).parse(URLDecoder.decode(jobj.getString(APPLYDATE),StaticValues.ENCODING));
                }
                Calendar applyDate = Calendar.getInstance();
                applyDate.setTime(appDate);
                String erid = URLDecoder.decode(jobj.getString(ID),StaticValues.ENCODING);
                Map<String, Object> filterParams = new HashMap<String, Object>();
                filterParams.put( ERID,erid);
                filterParams.put( APPLYDATE,appDate);
                filterParams.put( COMPANYID,companyid);
                KwlReturnObject result = accCurrencyDAOobj.getExchangeRateDetails(filterParams, false);
                List list = result.getEntityList();
                Map<String, Object> erdMap = new HashMap<String, Object>();
                if(StringUtil.isNullOrEmpty(jobj.getString(EXCHANGERATE))){
                    throw new AccountingException(messageSource.getMessage("acc.curex.excp2", null, RequestContextUtils.getLocale(request)));
                }
                else{
                    erdMap.put(EXCHANGERATE, Double.parseDouble(URLDecoder.decode(jobj.getString(EXCHANGERATE),StaticValues.ENCODING)));
                }
                ExchangeRateDetails erd;
                KwlReturnObject erdresult;
                if (list.size() > 0 && !updateRate) {
                    return true;
                } else {
                    if (list.size() <= 0) {
                        //throw new AccountingException("Can not change edit the Exchange Rate.");
                        erdMap.put(APPLYDATE,applyDate.getTime());
                        erdMap.put(ERID,erid);
                        erdMap.put(COMPANYID,companyid);
                        erdresult = accCurrencyDAOobj.addExchangeRateDetails(erdMap);
                    } else {
                        erd = (ExchangeRateDetails) list.get(0);
                        erdMap.put(ERDID,erd.getID());
                        erdresult = accCurrencyDAOobj.updateExchangeRateDetails(erdMap);
                    }
                    erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
        } catch (JSONException ex) {
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveCurrencyExchange : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveCurrencyExchange : " + ex.getMessage(), ex);
        }
        return false;
    }

    public ModelAndView getCurrencyExchange(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(TRANSACTIONDATE,request.getParameter(TRANSACTIONDATE));
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(FROMCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            String toCurrencyid=request.getParameter(TOCURRENCYID);
            if(!StringUtil.isNullOrEmpty(toCurrencyid))
                requestParams.put(TOCURRENCYID, request.getParameter(TOCURRENCYID));
            KwlReturnObject result = accCurrencyDAOobj.getCurrencyExchange(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = getCurrencyExchangeJson(request, list);
            jobj.put( DATA,jArr);
            jobj.put(COUNT, jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW,MODEL, jobj.toString());
    }

    public JSONArray getCurrencyExchangeJson(HttpServletRequest request, List<ExchangeRate> list) throws SessionExpiredException, ParseException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            Date transactiondate = null;
            String date = request.getParameter(TRANSACTIONDATE) == null ? null : request.getParameter(TRANSACTIONDATE);
            if (!StringUtil.isNullOrEmpty(date)) {
                transactiondate = authHandler.getDateFormatter(request).parse(date);
            }
            DateFormat df = authHandler.getDateFormatter(request);
//            Iterator itr = list.iterator();
//            while (itr.hasNext()) {
//                ExchangeRate ER = (ExchangeRate) itr.next();
              if(list!=null && !list.isEmpty()){
                    for(ExchangeRate ER :list){
                    String erID = ER.getID();
    //                ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,null,transactiondate,erID);
                    KwlReturnObject erdresult = accCurrencyDAOobj.getExcDetailID(requestParams, null, transactiondate, erID);
                    ExchangeRateDetails erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);
                    JSONObject obj = new JSONObject();
                    if (erd != null) {
                        obj.put(ID, erd.getExchangeratelink().getID());
                        obj.put(APPLYDATE, df.format(erd.getApplyDate()));
                        obj.put(EXCHANGERATE, erd.getExchangeRate());
                        obj.put(FROMCURRENCY, erd.getExchangeratelink().getFromCurrency().getName());
                        obj.put(SYMBOL, erd.getExchangeratelink().getToCurrency().getSymbol());
                        obj.put(HTMLCODE, erd.getExchangeratelink().getToCurrency().getHtmlcode());
                        obj.put(TOCURRENCY, erd.getExchangeratelink().getToCurrency().getName());
                        obj.put(TOCURRENCYID, erd.getExchangeratelink().getToCurrency().getCurrencyID());
                        obj.put(FROMCURRENCYID, erd.getExchangeratelink().getFromCurrency().getCurrencyID());
                        obj.put(COMPANYID, erd.getCompany().getCompanyID());
                        jArr.put(obj);
                    }
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCurrencyExchangeJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getCurrencyExchangeList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {

            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(ERID, request.getParameter("currencyid"));

            KwlReturnObject result = accCurrencyDAOobj.getExchangeRateDetails(requestParams, true);
            List list = result.getEntityList();

            JSONArray jArr = getCurrencyExchangeListJson(request, list);
            jobj.put( DATA,jArr);
            jobj.put(COUNT, jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW,MODEL, jobj.toString());
    }

    public JSONArray getCurrencyExchangeListJson(HttpServletRequest request, List<ExchangeRateDetails> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
//            Iterator itr = list.iterator();
//            while(itr.hasNext()) {
//                            ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
              if(list!=null && !list.isEmpty()){
                 for(ExchangeRateDetails erd :list){
                    JSONObject obj = new JSONObject();
                    obj.put(ID, erd.getExchangeratelink().getID());
                    obj.put(APPLYDATE, authHandler.getDateFormatter(request).format(erd.getApplyDate()));
                    obj.put(EXCHANGERATE, erd.getExchangeRate());
                    obj.put(FROMCURRENCY, erd.getExchangeratelink().getFromCurrency().getName());
                    obj.put(SYMBOL, erd.getExchangeratelink().getToCurrency().getSymbol());
                    obj.put(HTMLCODE, erd.getExchangeratelink().getToCurrency().getHtmlcode());
                    obj.put(TOCURRENCY, erd.getExchangeratelink().getToCurrency().getName());
                    obj.put(TOCURRENCYID, erd.getExchangeratelink().getToCurrency().getCurrencyID());
                    obj.put(FROMCURRENCYID, erd.getExchangeratelink().getFromCurrency().getCurrencyID());
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCurrencyExchangeListJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getDefaultCurrencyExchange(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(TRANSACTIONDATE,null);

            if(request.getParameter("currencyid") != null)
            	requestParams.put("gcurrencyid", request.getParameter("currencyid"));
            KwlReturnObject result = accCurrencyDAOobj.getDefaultCurrencyExchange(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = getDefaultCurrencyExchangeJson(request, list);
            jobj.put( DATA,jArr);
            jobj.put(COUNT, jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW,MODEL, jobj.toString());
    }

    public JSONArray getDefaultCurrencyExchangeJson(HttpServletRequest request, List<Object[]> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
//            Iterator itr = list.iterator();
//            while(itr.hasNext()) {
//                Object[] row = (Object[]) itr.next();
               if(list!=null && !list.isEmpty()){
                    for(Object[] row :list){
                    DefaultExchangeRate er = (DefaultExchangeRate) row[0];
                    DefaultExchangeRateDetails erd = (DefaultExchangeRateDetails) row[1];
                    JSONObject obj = new JSONObject();
                    obj.put(ID, er.getID());
                    if(er.getFromCurrency().getCurrencyID().equals(er.getToCurrency().getCurrencyID()))
                        obj.put(APPLYDATE, authHandler.getDateFormatter(request).format(new Date(1,1,1)));
                    else
                        obj.put(APPLYDATE, authHandler.getDateFormatter(request).format(new Date()));
                    obj.put(EXCHANGERATE, erd.getExchangeRate());
                    obj.put(FROMCURRENCY, er.getFromCurrency().getName());
                    obj.put(SYMBOL, er.getToCurrency().getSymbol());
                    obj.put(HTMLCODE, er.getToCurrency().getHtmlcode());
                    obj.put(TOCURRENCY, er.getToCurrency().getName());
                    obj.put(TOCURRENCYID, er.getToCurrency().getCurrencyID());
                    obj.put(FROMCURRENCYID, er.getFromCurrency().getCurrencyID());
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getDefaultCurrencyExchangeJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }



    /*
    public Double getCurrencyToBaseAmount(HttpServletRequest request, Double Amount, String currencyid, String companyid, Date transactiondate) throws ServiceException {
    if(Amount != 0) {
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", AuthHandler.getCurrencyID(request));

            KwlReturnObject result = accCurrencyDAOobj.getExcDetailID(requestParams, currencyid, transactiondate, null);
            List list = result.getEntityList();
            if(!list.isEmpty()) {
                Iterator itr = list.iterator();
                ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                Double rate= erd.getExchangeRate();
                Amount= Amount/rate;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    return Amount;
    }
 */
    
    
    public ModelAndView getCurrency(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            KwlReturnObject result = accCurrencyDAOobj.getCurrencies();
            List list = result.getEntityList();

            JSONArray jArr = getCurrenciesJson(list);
            jobj.put( DATA,jArr);
            jobj.put(COUNT, jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW,MODEL, jobj.toString());
    }
    
    public JSONArray getCurrenciesJson(List<Object[]> list) throws SessionExpiredException, ServiceException {
    	JSONArray jArr = new JSONArray();
    	try {
    		if(list!=null && !list.isEmpty()){
    			Iterator iterator = list.iterator(); 
    			while(iterator.hasNext()){
    				KWLCurrency currency = (KWLCurrency) iterator.next();
    				JSONObject obj = new JSONObject();
    				obj.put("currencyid", currency.getCurrencyID());
    				obj.put("name", currency.getName());
    				jArr.put(obj);
    			}
    		}
	    } catch (JSONException ex) {
	        throw ServiceException.FAILURE("getCurrenciesJson : " + ex.getMessage(), ex);
	    }
    return jArr;
    }
}
