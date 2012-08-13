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
package com.krawler.spring.accounting.masteritems;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.MasterGroup;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
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
public class accMasterItemsController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private String successView;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    
    public ModelAndView saveMasterItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveMasterItem(request);
            issuccess = true;
            msg = result.getMsg();
            MasterItem masterItem = (MasterItem) result.getEntityList().get(0);
            jobj.put("id", masterItem.getID());
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
            	jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public KwlReturnObject saveMasterItem(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        boolean isPresent = false;
        String itemID = request.getParameter("id");
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put("id", itemID);
        requestParam.put("name", request.getParameter("name"));
        requestParam.put("groupid", request.getParameter("groupid"));

        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(),filter_params = new ArrayList();
        filter_names.add("masterGroup.ID");
        filter_params.add(request.getParameter("groupid"));
        filter_names.add("company.companyID");
        filter_params.add(requestParam.get("companyid"));
        filter_names.add("value");
        filter_params.add(request.getParameter("name"));
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
        int count = cntResult.getRecordTotalCount();

        if(count == 1) {
            String recordID = ((MasterItem)cntResult.getEntityList().get(0)).getID();
            isPresent = itemID.equals(recordID) ? false : true; //Allow Editing same record
        } else if(count > 1){
            isPresent = true;
        }

        if(isPresent) {
            msg = "Master item entry for <b>"+request.getParameter("name") + "</b> already exists.";
        } else {
            if (StringUtil.isNullOrEmpty(itemID)) {
                result = accMasterItemsDAOobj.addMasterItem(requestParam);
            } else {
                result = accMasterItemsDAOobj.updateMasterItem(requestParam);
            }
        }
        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);
    }

    public ModelAndView deleteMasterItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int no = deleteMasterItem(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.master.del", null, RequestContextUtils.getLocale(request));   //"Master item has been deleted successfully";
            try{
                txnManager.commit(status);
            }catch(Exception ex){
                isCommitEx = true;
                msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));   //"The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.";
            }
        } catch (ServiceException ex) {
        	if(!isCommitEx){
        		txnManager.rollback(status);
        	}
        	msg = ""+ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int deleteMasterItem(HttpServletRequest request) throws ServiceException, AccountingException {
        String ids[] = request.getParameterValues("ids");
        int numRows = 0;
        try{
	        for (int i = 0; i < ids.length; i++) {
	            accMasterItemsDAOobj.daleteMasterItem(ids[i]);
	            numRows++;
	        }
        }catch (ServiceException ex){
            throw new  AccountingException(messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return numRows;
    }

    public ModelAndView getMasterItems(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getMasterItems(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getMasterItems : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getMasterItems(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("masterGroup.ID");
            filter_params.add(request.getParameter("groupid"));
            filter_names.add("company.companyID");
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            order_by.add("value");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterItems(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                MasterItem item = (MasterItem) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", item.getID());
                obj.put("name", item.getValue());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }


    public ModelAndView saveMasterGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveMasterGroup(request);
            issuccess = true;
            msg = "Master group has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public KwlReturnObject saveMasterGroup(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result;

        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put("id", request.getParameter("id"));
        requestParam.put("name", request.getParameter("name"));

        String groupID = request.getParameter("id");
        if (StringUtil.isNullOrEmpty(groupID)) {
            result = accMasterItemsDAOobj.addMasterGroup(requestParam);
        } else {
            result = accMasterItemsDAOobj.updateMasterGroup(requestParam);
        }
        return result;
    }

    public ModelAndView getMasterGroups(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getMasterGroups(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getMasterGroups : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getMasterGroups(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            KwlReturnObject result = accMasterItemsDAOobj.getMasterGroups();
            List list = result.getEntityList();
            Iterator iter = list.iterator();
            JSONArray jArr = new JSONArray();
            while (iter.hasNext()){
                MasterGroup mst = (MasterGroup) iter.next();
                JSONObject tmpObj = new JSONObject();
                tmpObj.put("id", mst.getID());
//                tmpObj.put("name", mst.getGroupName());
                tmpObj.put("name", messageSource.getMessage("acc.masterConfig."+mst.getID(), null, RequestContextUtils.getLocale(request)));
                jArr.put(tmpObj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public ModelAndView deleteMasterGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = accMasterItemsDAOobj.deleteMasterGroup("groupid");
            issuccess = true;
            msg = "Master group has been deleted successfully";
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

}
