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
package com.krawler.spring.accounting.tax;

import com.krawler.accounting.utils.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.Tax1099Accounts;
import com.krawler.hql.accounting.Tax1099Category;
import com.krawler.hql.accounting.TaxList;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
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
public class accTaxController extends MultiActionController implements TaxConstants, MessageSourceAware{

    private HibernateTransactionManager txnManager;
    private accTaxDAO accTaxObj;
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
    public void setaccTaxDAO (accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }//getTax1099Category

    public ModelAndView getTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            DateFormat df = authHandler.getDateFormatter(request);
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String transDate=request.getParameter("transactiondate");
            if(transDate!=null)
                requestParams.put("transactiondate", df.parse(transDate)) ;
            KwlReturnObject result = accTaxObj.getTax(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getTaxJson(request, list);
            jobj.put( DATA,DataJArr);
            jobj.put( COUNT,count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getTaxJson(HttpServletRequest request, List<Object[]> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
//            Iterator itr = list.iterator();
//            while (itr.hasNext()) {
//               Object[] row = (Object[]) itr.next();
             if(list!=null && ! list.isEmpty()){
                 for(Object[] row :list){                
                    if(row[2]==null)continue;
                    Tax tax = (Tax) row[0];
                    JSONObject obj = new JSONObject();
                    obj.put(TAXID, tax.getID());
                    obj.put(TAXNAME, tax.getName());
                    obj.put(PERCENT, row[1]);
                    obj.put(TAXCODE, tax.getTaxCode());
                    obj.put(ACCOUNTID, tax.getAccount().getID());
                    obj.put(ACCOUNTNAME, tax.getAccount().getName());
                    obj.put(APPLYDATE, authHandler.getDateFormatter(request).format(row[2]));
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTaxJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }

     public ModelAndView getTax1099Category(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);

            KwlReturnObject result = accTaxObj.getTax1099Category(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getTax1099CategoryJson(requestParams, list);
            jobj.put( DATA,DataJArr);
            jobj.put( COUNT,count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     
      public JSONArray getTax1099CategoryJson(Map<String, Object> requestParams, List<Tax1099Category> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String companyid=(String) requestParams.get(COMPANYID);
//            Iterator itr = list.iterator();
//            while (itr.hasNext()) {
//              //  Object[] row = (Object[]) itr.next();
//                Tax1099Category taxcategory =(Tax1099Category)itr.next();
            if(list!=null && ! list.isEmpty()){
                 for( Tax1099Category taxcategory :list){
                    JSONObject obj = new JSONObject();
                    obj.put(CATEGORYID, taxcategory.getID());
                    obj.put(CATEGORYNAME, taxcategory.getCategory());
                    obj.put(THRESHOLDVALUE, taxcategory.getThresholdValue());
                    obj.put(ISDELETED, taxcategory.isDeleted());
                    obj.put(SRNO, taxcategory.getSrno());
                    KwlReturnObject result = accTaxObj.getTaxCategoryAccount(companyid,taxcategory.getID());
                    List<Account> accList = result.getEntityList();
//                    Iterator accItr = accList.iterator();
                    String accNames="";
//                    while (accItr.hasNext()) {
//                        Account acc= (Account) accItr.next();
                    if(accList!=null && ! accList.isEmpty()){
                        for(Account acc :accList){
                            accNames +=acc.getID();
                            accNames +=",";
                        }
                    }
                    accNames=accNames.substring(0,Math.max(0, accNames.length()-1));
      //              obj.put("accountid", taxcategory.getAccount().getID());
                   obj.put( ACCOUNTID,accNames);
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTaxJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView checkApplyTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean flag;
        String msg="";
        try{
            flag = checkApplyTax(request);
            jobj.put( MSG,flag);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put( SUCCESS,issuccess);
                if (msg.length() > 0) {
                    jobj.put( MSG,msg);
                }
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean checkApplyTax(HttpServletRequest request) throws JSONException, SessionExpiredException, ParseException, ServiceException {
        boolean flag = true;
        JSONArray jArr = new JSONArray(request.getParameter(DATA));
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);

            Date appDate=authHandler.getDateFormatter(request).parse(jobj.getString(APPLYDATE));
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(TAXID, jobj.getString(TAXID));
            filterParams.put( APPDATE,appDate);
            filterParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject result = accTaxObj.getTaxList(filterParams);
            List list = result.getEntityList();
            if(!list.isEmpty()){
                flag=false;
                return false;
            }
        }
        return flag;
    }

    public ModelAndView saveTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Tax_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveTax(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.tax.update2", null, RequestContextUtils.getLocale(request));  //"Tax details has been updated successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveTax(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter(DATA));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String taxid = "";
            KwlReturnObject result;
            int delCount = 0;
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString(TAXID)) == false) {
                    taxid = jobj.getString(TAXID);
                    try {
                        result = accTaxObj.deleteTaxList(taxid, companyid);
                        result = accTaxObj.deleteTax(taxid, companyid);
                        delCount += result.getRecordTotalCount();
                    } catch (ServiceException ex) {
                        throw new  AccountingException(messageSource.getMessage("acc.tax.excp1", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            String auditMsg;
            String auditID;
            Tax tax;
            TaxList taxlist;
            KwlReturnObject taxresult;
            Map<String, Object> taxMap;
//            String fullName = AuthHandler.getFullName(session, AuthHandler.getUserid(request));
            if (delCount > 0) {
                auditTrailObj.insertAuditLog(AuditAction.TAX_DETAIL_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " deleted " + delCount + " Tax Details", request, "0");
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }

//                tax.setName(jobj.getString("taxname"));
//                tax.setTaxCode(jobj.getString("taxcode"));//accountid
//                tax.setCompany(company);
//                tax.setAccount((Account) session.get(Account.class, jobj.getString("accountid")));
                taxMap = new HashMap<String, Object>();
                taxMap.put(TAXNAME, URLDecoder.decode(jobj.getString(TAXNAME),StaticValues.ENCODING));
                taxMap.put(TAXCODE, URLDecoder.decode(jobj.getString(TAXCODE),StaticValues.ENCODING));
                taxMap.put(ACCOUNTID, jobj.getString(ACCOUNTID));
                taxMap.put( COMPANYID,companyid);

                if (StringUtil.isNullOrEmpty(jobj.getString(TAXID))) {
                    auditMsg = "added";
                    auditID = AuditAction.TAX_DETAIL_CREATED;
                    taxresult = accTaxObj.addTax(taxMap);
                } else {
                    auditMsg = "updated";
                    auditID = AuditAction.TAX_DETAIL_DELETED;
                    taxMap.put(TAXID, jobj.getString(TAXID));
                    taxresult = accTaxObj.updateTax(taxMap);
                }
                tax = (Tax) taxresult.getEntityList().get(0);
                taxlist = setNewTax(request, jobj, tax);
                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " " + auditMsg +" "+ tax.getName(), request, tax.getID());
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public ModelAndView saveTax1099Category(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Tax_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveTax1099Category(request);
            issuccess = true;
            msg = "Tax Category details has been updated successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveTax1099Category(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, JSONException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter(DATA));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Tax1099Category taxCategory;
            KwlReturnObject taxresult=null;
            Map<String, Object> taxCategoryMap;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                taxCategoryMap = new HashMap<String, Object>();
                taxCategoryMap.put(THRESHOLDVALUE,Double.parseDouble(jobj.getString(THRESHOLDVALUE)));// URLDecoder.decode(jobj.getString("taxname"),StaticValues.ENCODING));
                taxCategoryMap.put(CATEGORYID, jobj.getString(CATEGORYID));
                taxCategoryMap.put( COMPANYID,companyid);
                taxresult = accTaxObj.updateTax1099Category(taxCategoryMap);
                taxCategory = (Tax1099Category) taxresult.getEntityList().get(0);
                accTaxObj.deleteTax1099AccountList(jobj.getString(CATEGORYID),companyid);
                Tax1099Accounts taxcatacclist=setTaxCategoryAccount(request, jobj, taxCategory);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public Tax1099Accounts setTaxCategoryAccount(HttpServletRequest request, JSONObject jobj, Tax1099Category taxCategory) throws ServiceException, SessionExpiredException {
        Tax1099Accounts taxcatacclist=null;
        try {
            String taxcategoryid = taxCategory.getID();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] accountarr=jobj.getString(ACCOUNTID).split(",");
            for(int i=0;i<accountarr.length;i++) {
                String accountid=accountarr[i];
                if(!StringUtil.isNullOrEmpty(accountid)){
                    Map<String, Object> taxListMap = new HashMap<String, Object>();
                    taxListMap.put( COMPANYID,companyid);
                    taxListMap.put( CATEGORYID,taxcategoryid);
                    taxListMap.put( ACCOUNTID,accountid);
                    KwlReturnObject taxlistresult;                    
                    taxlistresult = accTaxObj.updateTax1099Account(taxListMap);
                    taxcatacclist = (Tax1099Accounts) taxlistresult.getEntityList().get(0);
                }
            }
        } catch (JSONException ex) {
           throw ServiceException.FAILURE("setNewTax : "+ex.getMessage(), ex);
        }
        return taxcatacclist;
    }

    public TaxList setNewTax(HttpServletRequest request, JSONObject jobj, Tax tax) throws ServiceException, SessionExpiredException {
        TaxList taxlist=null;
        try {
//            List list = null;
//            ArrayList params=new ArrayList();
//            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
//                String query="from TaxList where applyDate=? and tax.ID=?  and company.companyID=?";
//                params.add(appDate);
//                params.add(tax.getID());
//                params.add(company.getCompanyID());
//                list = HibernateUtil.executeQuery(session, query, params.toArray());

            String taxid = tax.getID();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Date appDate=authHandler.getDateFormatter(request).parse(jobj.getString(APPLYDATE));

            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put( APPLYDATE,appDate);
            filterParams.put( COMPANYID,companyid);
            filterParams.put( TAXID,taxid);

            KwlReturnObject result = accTaxObj.getTaxList(filterParams);
            List list = result.getEntityList();
//            taxlist.setApplyDate(appDate);
//            taxlist.setTax(tax);
//            taxlist.setCompany(company);
//            taxlist.setPercent(Double.parseDouble(jobj.getString("percent")));
//            session.saveOrUpdate(taxlist);
            Map<String, Object> taxListMap = new HashMap<String, Object>();
            taxListMap.put( TAXID,taxid);
            taxListMap.put( APPLYDATE,appDate);
            taxListMap.put( COMPANYID,companyid);
            taxListMap.put(PERCENT, Double.parseDouble(jobj.getString(PERCENT)));

            KwlReturnObject taxlistresult;
            if (list != null && !list.isEmpty()) {
                taxlist = (TaxList) list.get(0);
                taxListMap.put(TAXLISTID, taxlist.getID());
                taxlistresult = accTaxObj.updateTaxList(taxListMap);
            } else {
//                taxlist = new TaxList();
                taxlistresult = accTaxObj.addTaxList(taxListMap);
            }

            taxlist = (TaxList) taxlistresult.getEntityList().get(0);
        } catch (JSONException ex) {
           throw ServiceException.FAILURE("setNewTax : "+ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("setNewTax : "+ex.getMessage(), ex);
        }
        return taxlist;
    }

}
