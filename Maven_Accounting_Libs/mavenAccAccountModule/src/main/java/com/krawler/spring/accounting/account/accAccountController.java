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
package com.krawler.spring.accounting.account;

import com.krawler.accounting.utils.AuditAction;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.DefaultAccount;
import com.krawler.hql.accounting.Group;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.Date;
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

import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import java.util.ArrayList;

/**
 *
 * @author krawler
 */
public class accAccountController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accAccountDAO accAccountDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDaoObj;
    private ImportHandler importHandler;
    private accCurrencyDAO accCurrencyDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
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

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }

    public ModelAndView getAccounts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
            KwlReturnObject result = accAccountDAOobj.getAccounts(requestParams);
            List list = result.getEntityList();
            jobj = accAccountHandler.getAccountJson(request, list, accCurrencyDAOobj);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch(Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAccountsForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr=new JSONArray();
        boolean issuccess = true;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
            KwlReturnObject result = accAccountDAOobj.getAccountsForCombo(requestParams);
            List list = result.getEntityList();
            ArrayList resultlist = new ArrayList();
            boolean ignoreCustomers=requestParams.get("ignorecustomers")!=null;
            boolean ignoreVendors=requestParams.get("ignorevendors")!=null;
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");
            String customerCpath = ConfigReader.getinstance().get("Customer");
            String vendorCpath = ConfigReader.getinstance().get("Vendor");
//            boolean deleted =Boolean.parseBoolean((String)requestParams.get("deleted"));
//            boolean nondeleted =Boolean.parseBoolean((String)requestParams.get("nondeleted"));
            String currencyid=(String)requestParams.get("currencyid");
            KWLCurrency currency = (KWLCurrency)kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.KWLCurrency", currencyid);

            Iterator itr = list.iterator();
//            int level=0;
            while (itr.hasNext()) {
                Object listObj = itr.next();
                Account account = (Account) listObj;
                if(excludeaccountid!=null&&account.getID().equals(excludeaccountid)) continue;
                if((includeparentid!=null&&(!account.getID().equals(includeparentid)||(account.getParent()!=null&&!account.getParent().getID().equals(includeparentid))))) continue;
                else if((includeaccountid!=null&&!account.getID().equals(includeaccountid))) continue;

                Object c = kwlCommonTablesDAOObj.getClassObject(customerCpath, account.getID());
                if(ignoreCustomers&&account.getGroup().getID().equals(Group.ACCOUNTS_RECEIVABLE)){
                    if(c!=null)continue;
                }

                Object v = kwlCommonTablesDAOObj.getClassObject(vendorCpath, account.getID());
                if(ignoreVendors&&account.getGroup().getID().equals(Group.ACCOUNTS_PAYABLE)){
                    if(v!=null)continue;
                }

                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("currencyid",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getSymbol()));
                obj.put("currencyname",(account.getCurrency()==null?currency.getName(): account.getCurrency().getName()));
                obj.put("deleted", account.isDeleted());
//                obj.put("depreciationaccount", account.getDepreciationAccont()==null?"":account.getDepreciationAccont().getID());
//                obj.put("openbalance", account.getOpeningBalance());
//                Account parentAccount = (Account) row[6];
//                if(parentAccount!=null){
//                    obj.put("parentid", parentAccount.getID());
//                    obj.put("parentname", parentAccount.getName());
//                }
//                obj.put("level", row[3]);
//                obj.put("leaf", row[4]);
//                obj.put("presentbalance", account.getPresentValue());
//                obj.put("life", account.getLife());
//                obj.put("salvage", account.getSalvage());
//                obj.put("posted", row[7]);
//                obj.put("creationDate", authHandler.getDateFormatter(request).format(account.getCreationDate()));
//                obj.put("categoryid", account.getCategory()==null?"":account.getCategory().getID());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch(Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String groupID = null, msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String groupid=request.getParameter("groupid");
            Group group = saveGroup(request);
            groupID = group.getID();
            issuccess = true;
            if(StringUtil.isNullOrEmpty(groupid)){
                msg = messageSource.getMessage("acc.acc.add", null, RequestContextUtils.getLocale(request));   //"Account type has been added successfully.";
            } else {
                msg =  messageSource.getMessage("acc.acc.update", null, RequestContextUtils.getLocale(request));   //"Account type has been updated successfully.";
            }
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("groupID",groupID);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public Group saveGroup(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        Group group = null;
        try {
            String auditMsg="", auditID="";

            String companyid=sessionHandlerImpl.getCompanyid(request);
            boolean issub = request.getParameter("subtype")!=null;
            String parentid = request.getParameter("parentid");
            if(!issub){
                parentid=null;
            }

            JSONObject groupjson = new JSONObject();
            groupjson.put("companyid", companyid);
            groupjson.put("name", request.getParameter("groupname"));
            groupjson.put("nature", Integer.parseInt(request.getParameter("nature")));
            groupjson.put("affectgp", request.getParameter("affectgp")!=null);
            groupjson.put("parentid", parentid);

            int dispOrder=0;
            String groupid=request.getParameter("groupid");
            if(StringUtil.isNullOrEmpty(groupid)){
                auditMsg = "added";
                auditID = AuditAction.ACCOUNT_CREATED;
                KwlReturnObject dspresult = accAccountDAOobj.getMaxGroupDisplayOrder();
                List l = dspresult.getEntityList();
                if (!l.isEmpty() && l.get(0) != null) {
                    dispOrder = (Integer) l.get(0);
                }
                dispOrder++;
                groupjson.put("disporder", dispOrder);

                KwlReturnObject grpresult = accAccountDAOobj.addGroup(groupjson);
                group = (Group) grpresult.getEntityList().get(0);
                accAccountDAOobj.updateChildrenGroup(group);
            } else {
                auditMsg = "updated";
                auditID = AuditAction.ACCOUNT_UPDATED;
                KwlReturnObject grpresult = accountingHandlerDAOobj.getObject(Group.class.getName(), groupid);
                dispOrder=((Group)grpresult.getEntityList().get(0)).getDisplayOrder();
                groupjson.put("disporder", dispOrder);
                groupjson.put("groupid", groupid);

                grpresult = accAccountDAOobj.updateGroup(groupjson);
                group = (Group) grpresult.getEntityList().get(0);
                accAccountDAOobj.updateChildrenGroup(group);
            }
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " " + auditMsg + " group " + group.getName(), request, group.getID());

        } catch (JSONException ex) {
//            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveGroup : "+ex.getMessage(), ex);
        }
        return group;
    }

    public ModelAndView getGroups(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        boolean issuccess = true;
        String msg = "";
		try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("groupid", request.getParameter("groupid"));
            requestParams.put("group", request.getParameterValues("group"));
            requestParams.put("ignore", request.getParameter("ignore"));
            requestParams.put("ignorecustomers", request.getParameter("ignorecustomers"));
            requestParams.put("ignorevendors", request.getParameter("ignorevendors"));
            requestParams.put("nature",request.getParameterValues("nature"));

            String companyid=sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = accAccountDAOobj.getGroups(requestParams);
            List ll = result.getEntityList();
            Iterator itr = ll.iterator();

            JSONArray jArr=new JSONArray();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Group group = (Group) row[0];
                JSONObject obj = new JSONObject();
                obj.put("groupid", group.getID());
                obj.put("groupname", group.getName());
                obj.put("nature", group.getNature());
                obj.put("affectgp", group.isAffectGrossProfit());
                obj.put("displayorder", group.getDisplayOrder());
                obj.put("companyid", (group.getCompany()==null?null:companyid));
                Group parentGroup = (Group) row[3];
                if(parentGroup!=null){
                    obj.put("parentid", parentGroup.getID());
                    obj.put("parentname", parentGroup.getName());
                }
                obj.put("level", row[1]);
                obj.put("leaf", row[2]);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch(Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "accAccountController.getGroups : "+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getDefaultAccount(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        boolean issuccess = true;
        String msg = "";
		try {
            String companyType = request.getParameter("companyType");
            KwlReturnObject result = accAccountDAOobj.getDefaultAccount(companyType);
            List ll = result.getEntityList();
            Iterator itr = ll.iterator();

            JSONArray jArr=new JSONArray();
            while (itr.hasNext()) {
                DefaultAccount dAccount = (DefaultAccount) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", dAccount.getID());
                obj.put("name", dAccount.getName());
                obj.put("groupname", dAccount.getGroup().getName());
                obj.put("companytype", dAccount.getCompanyType());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch(Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "accAccountController.getDefaultAccount : "+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = true;

		try {
            String groupid = request.getParameter("groupid");
            KwlReturnObject result = accAccountDAOobj.deleteGroup(groupid);

            msg = messageSource.getMessage("acc.acc.delGroup", null, RequestContextUtils.getLocale(request));
            issuccess = result.isSuccessFlag();
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accAccountController.deleteGroup : "+ex.getMessage();
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveAccount(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String accountID = "", msg="";
        boolean issuccess = true, isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String mode = request.getParameter("isFixedAsset")==null?"false":request.getParameter("isFixedAsset");
            boolean isFixedAsset = Boolean.parseBoolean(mode);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            filter_names.add("ISdeleted");
            filter_params.add(false);
            filter_names.add("name");
            filter_params.add(request.getParameter("accname"));
            if(isFixedAsset){
                filter_names.add("groupname");
                filter_params.add(Group.FIXED_ASSETS);//12:FIXED_ASSET
            } else {
                filter_names.add("NOTINgroupname");
                filter_params.add("'"+Group.ACCOUNTS_RECEIVABLE+"','"+Group.FIXED_ASSETS+"','"+Group.ACCOUNTS_PAYABLE+"'");//10:CUSTOMER, 12:FIXED_ASSET, 13: VENDOR
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("accid"))){
                filter_names.add("!ID");
                filter_params.add(request.getParameter("accid"));
            }
            requestParams.put("filter_names", filter_names);
            requestParams.put("filter_params", filter_params);
            KwlReturnObject result = accAccountDAOobj.getAccount(requestParams);
            if(result.getRecordTotalCount() > 0) {
                issuccess = false;
                msg = "This "+ (!isFixedAsset?"account name":"fixed asset name") +" already exists. Please enter a different name.";
            } else {
                Account account = saveAccount(request);
                accountID = account.getID();
                msg = (!isFixedAsset?"Account":"Fixed Asset")+ " has been saved successfully.";
            }
            try{
                txnManager.commit(status);
            }catch(Exception ex){
                isCommitEx = true;
            }
        } catch (Exception ex) {
            if(!isCommitEx){
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("accID",accountID);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public Account saveAccount(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        Account account;
        try {
            String companyid=sessionHandlerImpl.getCompanyid(request);
            String currencyid=(request.getParameter("currencyid")==null?sessionHandlerImpl.getCurrencyID(request):request.getParameter("currencyid"));

            boolean issub = StringUtil.getBoolean(request.getParameter("subaccount"));
            boolean debitType = StringUtil.getBoolean(request.getParameter("debitType"));
            double openBalance = StringUtil.getDouble(request.getParameter("openbalance"));
            openBalance=debitType?openBalance:-openBalance;
            String parentid=request.getParameter("parentid");
            if(!issub){
                parentid=null;
            }

            double life = StringUtil.getDouble(request.getParameter("life"));
            double salvage = StringUtil.getDouble(request.getParameter("salvage"));
            Date creationDate = authHandler.getDateFormatter(request).parse(request.getParameter("creationDate"));
            if (creationDate == null) {
                creationDate = new Date();
            }
            String depaccid = request.getParameter("depreciationaccount");
            String accountID = request.getParameter("accid");
            String accountName = request.getParameter("accname");
            String parentName = request.getParameter("parentname");

            JSONObject accjson = new JSONObject();
            if(!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))){
            	accjson.put("isFixedAsset", request.getParameter("isFixedAsset"));
            }	
            accjson.put("accountid", accountID);
            accjson.put("depaccountid", depaccid);
            accjson.put("name", accountName);
            accjson.put("balance", openBalance);
            accjson.put("parentid", parentid);
            accjson.put("groupid", request.getParameter("groupid"));
            accjson.put("companyid", companyid);
            accjson.put("currencyid", currencyid);
            accjson.put("life", life);
            accjson.put("salvage", salvage);
            accjson.put("creationdate", creationDate);
            accjson.put("category", request.getParameter("category"));
            accjson.put("costCenterId", request.getParameter("costcenter"));

            String auditMsg="", auditID="";
            KwlReturnObject accresult;
            if(StringUtil.isNullOrEmpty(accountID)){
                auditMsg = "added";
                auditID = AuditAction.ACCOUNT_CREATED;
                accresult = accAccountDAOobj.addAccount(accjson);
            } else {
//                if(accAccountDAOobj.isChild(accountID, parentid)){
//                    throw new AccountingException("\""+accountName+"\" is a parent of \""+parentName+"\" so can't set \""+parentName+"\" as a parent.");
            	if(isChildorGrandChild(accountID, parentid)){
                    throw new AccountingException("\""+accountName+"\" is a parent of \""+parentName+"\" so can't set \""+parentName+"\" as a parent.");
            
            }
                auditMsg = "updated";
                auditID = AuditAction.ACCOUNT_UPDATED;
                accresult = accAccountDAOobj.updateAccount(accjson);
            }
            account = (Account) accresult.getEntityList().get(0);
            accAccountDAOobj.updateChildrenAccount(account);

            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " " + auditMsg + " account " + account.getName(), request, account.getID());
        } catch (JSONException ex) {
//            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveAccount : "+ex.getMessage(), ex);
        } catch (ParseException ex) {
//            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveAccount : "+ex.getMessage(), ex);
        }
    return account;
    }

    public ModelAndView exportAccounts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
            KwlReturnObject result = accAccountDAOobj.getAccounts(requestParams);
            jobj = accAccountHandler.getAccountJson(request, result.getEntityList(), accCurrencyDAOobj);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView importAccounts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams)?new JSONObject():new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));
            extraParams.put("Currency", sessionHandlerImpl.getCurrencyID(request));
            extraParams.put("Life", 10.0);
            extraParams.put("Salvage", 0.0);

            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            if (doAction.compareToIgnoreCase("import") == 0 || doAction.compareToIgnoreCase("xlsImport") == 0) {
                System.out.println("A(( Import start : "+new Date());
                String exceededLimit = request.getParameter("exceededLimit");
                if(exceededLimit.equalsIgnoreCase("yes")){ //If file contains records more than 1500 then Import file in background using thread
                    String logId = importHandler.addPendingImportLog(requestParams);
                    requestParams.put("logId", logId);
                    importHandler.add(requestParams);
                    if (!importHandler.isIsWorking()) {
                        Thread t = new Thread(importHandler);
                        t.start();
                    }
                    jobj.put("success", true);
                } else {
                    jobj = importHandler.importFileData(requestParams);
                }
                jobj.put("exceededLimit", exceededLimit);
                System.out.println("A(( Import end : "+new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : "+new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : "+new Date());
                jobj.put("success", true);
            }
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", ""+ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView importDefaultAccounts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams)?new JSONObject():new JSONObject(eParams);
//            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));
            extraParams.put("Currency", sessionHandlerImpl.getCurrencyID(request));
            extraParams.put("Life", 5.0);
            extraParams.put("PresentValue", 0.0);
            extraParams.put("Salvage", 0.0);

//            jobj = importHandler.importCSVFile(request, extraParams, null);
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", ""+ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAccountsByCategory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));

            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            filter_names.add("ISdeleted");
            filter_params.add(false);
            if(request.getParameter("group")!=null){
                filter_names.add("group.ID");
                filter_params.add(request.getParameter("group"));
            }
            requestParams.put("filter_names", filter_names);
            requestParams.put("filter_params", filter_params);
            order_by.add("category");
            order_type.add("desc");
            requestParams.put("order_by", order_by);
            requestParams.put("order_type", order_type);

            KwlReturnObject result = accAccountDAOobj.getAccount(requestParams);
            jobj= getAccountsByCategoryJson(request, result.getEntityList());

            jobj.put("count", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getAccountsByCategoryJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr=new JSONArray();
        try{
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Account account = (Account) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("openbalance", account.getOpeningBalance());
                obj.put("depreciationaccount", account.getDepreciationAccont()==null?"":account.getDepreciationAccont().getID());

                obj.put("currencyid",(account.getCurrency()==null?"": account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol",(account.getCurrency()==null?"": account.getCurrency().getSymbol()));
                obj.put("currencyname",(account.getCurrency()==null?"": account.getCurrency().getName()));
                obj.put("presentbalance", account.getPresentValue());
                obj.put("life", account.getLife());
                obj.put("salvage", account.getSalvage());
                obj.put("deleted", account.isDeleted());
                obj.put("creationDate", authHandler.getDateFormatter(request).format(account.getCreationDate()));
                obj.put("category", account.getCategory()==null?"":account.getCategory().getValue());
                obj.put("categoryid", account.getCategory()==null?"":account.getCategory().getID());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountsByCategoryJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }
    
    public boolean isChildorGrandChild(String accountID, String parentid) throws ServiceException{
    	try{
    		List Result = accAccountDAOobj.isChildorGrandChild(parentid);
    		Iterator iterator = Result.iterator();
    		if(iterator.hasNext()){
    			Object ResultObj = iterator.next();
    			Account ResultParentac = (Account) ResultObj;
    			ResultParentac = ResultParentac.getParent();
    			if(ResultParentac == null){
    				return false;
    			}	
    			else{
    				String Resultparent = ResultParentac.getID();
    				if(Resultparent.equals(accountID))
    					return true;
    				else
    					return isChildorGrandChild(accountID, Resultparent);
    			}
   	 		}
    	}catch(Exception ex){
   	       throw ServiceException.FAILURE("isChildorGrandChild : "+ex.getMessage(), ex);
    	}
    	return false;
    }
}
