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
package com.krawler.spring.accounting.costCenter;

import com.krawler.common.admin.CostCenter;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
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
public class AccCostCenterController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private AccCostCenterDAO accCostCenterObj;
    private String successView;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccCostCenterDAO (AccCostCenterDAO accCostCenterDAOObj) {
        this.accCostCenterObj = accCostCenterDAOObj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public ModelAndView getCostCenter(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try{
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);

            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(requestParams.get(Constants.companyKey));
            requestParams.put(Constants.filterNamesKey, filter_names);
            requestParams.put(Constants.filterParamsKey, filter_params);

            KwlReturnObject result = accCostCenterObj.getCostCenter(requestParams);
            List<CostCenter> list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getCostCenterJson(request, list);
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_count, count);
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null": msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public JSONArray getCostCenterJson(HttpServletRequest request, List<CostCenter> costCenters) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String forCombo = request.getParameter(CCConstants.REQ_FORCOMBO)==null?"":request.getParameter(CCConstants.REQ_FORCOMBO);
            if(!costCenters.isEmpty() && !StringUtil.isNullOrEmpty(forCombo)){
                JSONObject obj = new JSONObject();
                obj.put(CCConstants.JSON_ID, "");
                obj.put(CCConstants.JSON_CCID, "");
                obj.put(CCConstants.JSON_NAME, forCombo.equalsIgnoreCase(CCConstants.REQ_COMBO_REPORT)?messageSource.getMessage("acc.rem.110", null, RequestContextUtils.getLocale(request)):messageSource.getMessage("acc.rem.111", null, RequestContextUtils.getLocale(request)));
                obj.put(CCConstants.JSON_DESC, "");
                jArr.put(obj);
            }

            if (costCenters != null && !costCenters.isEmpty()) {
                for (CostCenter costCenter : costCenters) {
                    JSONObject obj = new JSONObject();
                    obj.put(CCConstants.JSON_ID, costCenter.getID());
                    obj.put(CCConstants.JSON_CCID, costCenter.getCcid());
                    obj.put(CCConstants.JSON_NAME, costCenter.getName());
                    obj.put(CCConstants.JSON_DESC, costCenter.getDescription());
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCostCenterJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }


    public ModelAndView saveCostCenter(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false, isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CCenter_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveCostCenter(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.rem.171", null, RequestContextUtils.getLocale(request));  //CCConstants.CC_SUCCESS_MSG;

            try{
                txnManager.commit(status);
            }catch(Exception ex){
                isCommitEx = true;
                msg = ex.getMessage();
            }
        } catch (Exception ex) {
            if(!isCommitEx){
                txnManager.rollback(status);
            }
            msg = ""+ex.getMessage();
            Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public void saveCostCenter(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String id="", ccid = "", ccname="";
            
            for (int i = 0; i < jDelArr.length(); i++) {
            	String deleteid=""; 
            	JSONObject jobj = jDelArr.getJSONObject(i);
            	if (!StringUtil.isNullOrEmpty(jobj.getString("id"))){
            		try{
	            		deleteid = jobj.getString("id");
	            		accCostCenterObj.deleteCostCenter(deleteid, companyid);
            		} catch (ServiceException ex) {
                        throw new  AccountingException(messageSource.getMessage("acc.cc.excp1", null, RequestContextUtils.getLocale(request)));   //"The Cost Center(s) is or had been used in transaction(s). So, it cannot be deleted.");
                    }
            	}
            }
            HashMap<String, Object> ccMap;
            KwlReturnObject unqRes;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }
                ccid = URLDecoder.decode(jobj.getString(CCConstants.JSON_CCID),StaticValues.ENCODING);
                ccname = URLDecoder.decode(jobj.getString(CCConstants.JSON_NAME),StaticValues.ENCODING);
                id = jobj.getString(CCConstants.JSON_ID);
                unqRes = accCostCenterObj.checkUniqueCostCenter(id, ccid, ccname, companyid);
                if(unqRes.getRecordTotalCount()>0){
                    throw new AccountingException("Can't update Cost Center, Record already exists having Cost Center ID='"+ccid+"' or Name='"+ccname+"'");
                } else {
                    ccMap = new HashMap<String, Object>();
                    ccMap.put("Ccid", ccid);
                    ccMap.put("Name", ccname);
                    ccMap.put("Description", URLDecoder.decode(jobj.getString(CCConstants.JSON_DESC),StaticValues.ENCODING));
                    ccMap.put("Company", companyid);

                    if (!StringUtil.isNullOrEmpty(id)) {
                        ccMap.put("ID", id);
                    }
                    accCostCenterObj.saveCostCenter(ccMap);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);   //"Can't extract the records. <br>Encoding not supported", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (AccountingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

}
