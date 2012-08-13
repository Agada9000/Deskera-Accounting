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
package com.krawler.spring.accounting.bankreconciliation;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.BankReconciliation;
import com.krawler.hql.accounting.BankReconciliationDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnMsg;
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
import java.util.HashSet;
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
public class accBankReconciliationController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accBankReconciliationDAO accBankReconciliationObj;
    private String successView;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public ModelAndView saveBankReconciliation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BRecnl_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveBankReconciliation(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.br.save", null, RequestContextUtils.getLocale(request));   //"Bank Reconciliation Entry has been saved successfully.";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveBankReconciliation(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
            String accountid = request.getParameter("accid");
//            Account account=(Account)session.get(Account.class,request.getParameter("accid"));
            Date startdate=authHandler.getDateFormatter(request).parse(request.getParameter("startdate"));
            Date enddate=authHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            double clearingAmount=Double.parseDouble(request.getParameter("clearingbalance"));
            double endingAmount=Double.parseDouble(request.getParameter("endingbalance"));

//            BankReconciliation br=new BankReconciliation();
//            br.setStartDate(startdate);
//            br.setEndDate(enddate);
//            br.setAccount(account);
//            br.setClearingAmount(clearingAmount);
//            br.setEndingAmount(endingAmount);
//            br.setCompany((Company) session.get(Company.class, company.getCompanyID()));
            HashMap<String, Object> brMap = new HashMap<String, Object>();
            brMap.put("startdate", startdate);
            brMap.put("enddate", enddate);
            brMap.put("clearingamount", clearingAmount);
            brMap.put("endingamount", endingAmount);
            brMap.put("accountid", accountid);
            brMap.put("companyid", companyid);
            KwlReturnObject brresult = accBankReconciliationObj.addBankReconciliation(brMap);
            BankReconciliation br = (BankReconciliation) brresult.getEntityList().get(0);
            String brid = br.getID();

            HashSet hs = new HashSet();
            JSONArray jArr=new JSONArray(request.getParameter("d_details"));
            for(int i=0;i<jArr.length();i++){
                JSONObject jobj=jArr.getJSONObject(i);
//                BankReconciliationDetail brd=new BankReconciliationDetail();
//                brd.setCompany(company);
//                brd.setAmount(jobj.getDouble("d_amount"));
//                brd.setJournalEntry((JournalEntry)session.get(JournalEntry.class,jobj.getString("d_journalentryid")));
//                brd.setAccountnames(jobj.getString("d_accountname"));
//                brd.setDebit(true);
                HashMap<String, Object> brdMap = new HashMap<String, Object>();
                brdMap.put("companyid", companyid);
                brdMap.put("amount", jobj.getDouble("d_amount"));
                brdMap.put("jeid", URLDecoder.decode(jobj.getString("d_journalentryid"),StaticValues.ENCODING));
                brdMap.put("accountname", URLDecoder.decode(jobj.getString("d_accountname"),StaticValues.ENCODING));
                brdMap.put("debit", true);
                brdMap.put("brid", brid);
                KwlReturnObject brdresult = accBankReconciliationObj.addBankReconciliationDetail(brdMap);
                BankReconciliationDetail brd = (BankReconciliationDetail) brdresult.getEntityList().get(0);
                hs.add(brd);
            }
            jArr=new JSONArray(request.getParameter("c_details"));
            for(int i=0;i<jArr.length();i++){
                JSONObject jobj=jArr.getJSONObject(i);
//                BankReconciliationDetail brd=new BankReconciliationDetail();
//                brd.setCompany(company);
//                brd.setAmount(jobj.getDouble("c_amount"));
//                brd.setJournalEntry((JournalEntry)session.get(JournalEntry.class,jobj.getString("c_journalentryid")));
//                brd.setAccountnames(jobj.getString("c_accountname"));
//                brd.setDebit(false);
                HashMap<String, Object> brdMap = new HashMap<String, Object>();
                brdMap.put("companyid", companyid);
                brdMap.put("amount", jobj.getDouble("c_amount"));
                brdMap.put("jeid", URLDecoder.decode(jobj.getString("c_journalentryid"),StaticValues.ENCODING));
                brdMap.put("accountname", URLDecoder.decode(jobj.getString("c_accountname"),StaticValues.ENCODING));
                brdMap.put("debit", false);
                brdMap.put("brid", brid);
                KwlReturnObject brdresult = accBankReconciliationObj.addBankReconciliationDetail(brdMap);
                BankReconciliationDetail brd = (BankReconciliationDetail) brdresult.getEntityList().get(0);
                hs.add(brd);
            }
            
//            Iterator<BankReconciliationDetail> itr = hs.iterator();
//            while (itr.hasNext()) {
//                BankReconciliationDetail brd = itr.next();
//                brd.setBankReconciliation(br);
//            }

//            br.setDetails(hs);
            brMap.put("id", brid);
            brMap.put("brdetails", hs);
            brresult = accBankReconciliationObj.updateBankReconciliation(brMap);
            br = (BankReconciliation) brresult.getEntityList().get(0);
//            session.saveOrUpdate(br);
        } catch (UnsupportedEncodingException e) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), e);
        } catch (ParseException ex) {
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveBankReconciliation : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveBankReconciliation : "+ex.getMessage(), ex);
        }
    }
    
    public ModelAndView getBankReconciliation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("accountid", request.getParameter("accid"));
            KwlReturnObject result = accBankReconciliationObj.getBankReconciliation(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = getBankReconciliationJson(request, list);
            jobj.put("data", jArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getBankReconciliationJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            DateFormat df = authHandler.getDateFormatter(request);
            Iterator itr = list.iterator();
            while(itr.hasNext()) {
                BankReconciliation entry = (BankReconciliation) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", entry.getID());
                obj.put("startdate", df.format(entry.getStartDate()));
                obj.put("enddate", df.format(entry.getEndDate()));
                obj.put("clearingbalance", entry.getClearingAmount());
                obj.put("endingbalance", entry.getEndingAmount());
                obj.put("difference", entry.getEndingAmount()-entry.getClearingAmount());
                obj.put("accountname", entry.getAccount().getName());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBankReconciliationJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView deleteBankReconciliation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BRecnl_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteBankReconciliation(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.br.del", null, RequestContextUtils.getLocale(request));   //"Bank Reconciliation has been deleted successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteBankReconciliation(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                String brid = URLDecoder.decode(jobj.getString("id"),StaticValues.ENCODING);
                try {
                    accBankReconciliationObj.deleteBankReconciliation(brid, companyid);
                } catch (ServiceException ex) {
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), e);
        } catch (JSONException ex) {
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("deleteBankReconciliation : "+ex.getMessage(), ex);
        }
    }


}
