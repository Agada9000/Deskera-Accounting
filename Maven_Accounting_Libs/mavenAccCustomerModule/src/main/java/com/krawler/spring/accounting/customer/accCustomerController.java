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
package com.krawler.spring.accounting.customer;

import com.krawler.accounting.utils.AuditAction;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Group;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
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

/**
 *
 * @author krawler
 */
public class accCustomerController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accCustomerDAO accCustomerDAOobj;
    private accAccountDAO accAccountDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDaoObj;
    private ImportHandler importHandler;
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
    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
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
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

//    public ModelAndView manageCustomer(HttpServletRequest request, HttpServletResponse response) {
//        KwlReturnObject result = new KwlReturnObject(true, null, null, null, 0);
//        JSONObject jobj = new JSONObject(), obj=new JSONObject();
//        String msg = "";
//        boolean issuccess = true;
//        try {
//            int mode = Integer.parseInt(request.getParameter("mode"));
//            String companyid = sessionHandlerImpl.getCompanyid(request);
//            DateFormat df = authHandler.getDateFormatter(request);
//            switch (mode) {
//                case 12 :
////                    jobj=getInvoices(request);
//                    break;
//            }
//            issuccess = result.isSuccessFlag();
//        } catch (Exception ex) {
//            issuccess = false;
//            msg = ex.getMessage();
//        } finally {
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//            } catch (JSONException ex) {
//                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }

    public ModelAndView getCustomers(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getCustomerRequestMap(request);
//            KwlReturnObject result = accAccountDAOobj.getAccounts(requestParams);
//            System.out.println(new Date());
//            JSONArray jArr= getCustomerJson(request, result.getEntityList());

            KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams);
            JSONArray jArr= getCustomerJson(request, list);

//             String start = request.getParameter("start");
//            String limit = request.getParameter("limit");
//             JSONArray pagedJson = jArr;
//            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
//                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
//            }

            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCustomers : "+ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCustomersForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        JSONArray jArr=new JSONArray();
        try {
            HashMap<String, Object> requestParams = getCustomerRequestMap(request);
            KwlReturnObject result = accCustomerDAOobj.getCustomersForCombo(requestParams);
//            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams);
//            ArrayList resultlist = new ArrayList();
//            boolean ignoreCustomers=requestParams.get("ignorecustomers")!=null;
//            boolean ignoreVendors=requestParams.get("ignorevendors")!=null;
//            boolean deleted =Boolean.parseBoolean((String)requestParams.get("deleted"));
//            boolean nondeleted =Boolean.parseBoolean((String)requestParams.get("nondeleted"));
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");

            String currencyid=(String)requestParams.get("currencyid");
            KWLCurrency currency = (KWLCurrency)kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.KWLCurrency", currencyid);

            Iterator itr = result.getEntityList().iterator();
//            int level=0;
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Account account = (Account) row[0];
                Customer customer = (Customer) row[1];
                if (customer == null) {
                    continue;
                }
                if(excludeaccountid!=null&&account.getID().equals(excludeaccountid)) continue;
                if((includeparentid!=null&&(!account.getID().equals(includeparentid)||(account.getParent()!=null&&!account.getParent().getID().equals(includeparentid))))) continue;
                else if((includeaccountid!=null&&!account.getID().equals(includeaccountid))) continue;

                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("currencyid",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getSymbol()));
                obj.put("currencyname",(account.getCurrency()==null?currency.getName(): account.getCurrency().getName()));
//                obj.put("level", row[3]);
                obj.put("termdays", customer.getCreditTerm().getTermdays());
                obj.put("billto", customer.getBillingAddress());
                obj.put("deleted", customer.getAccount().isDeleted());
                obj.put("email", customer.getEmail());
                
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCustomersForCombo : "+ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public static HashMap<String, Object> getCustomerRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("group", request.getParameterValues("group"));
//        requestParams.put("query", request.getParameter("query"));
        requestParams.put("ignore", request.getParameter("ignore"));
        requestParams.put("ignorecustomers", request.getParameter("ignorecustomers"));
        requestParams.put("ignorevendors", request.getParameter("ignorevendors"));
        requestParams.put("accountid", request.getParameter("accountid"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        if(request.getParameter("query") != null && !StringUtil.isNullOrEmpty(request.getParameter("query"))) {
            requestParams.put("ss", request.getParameter("query"));
        }
        else if(request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            requestParams.put("ss", request.getParameter("ss"));
        }
        if(request.getParameter("start")!= null) {
            requestParams.put("start", request.getParameter("start"));
        }
        if(request.getParameter("limit")!= null) {
            requestParams.put("limit", request.getParameter("limit"));
        }
        requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
        return requestParams;
    }

    public static JSONArray getCustomerJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONArray jArr=new JSONArray();
        try{
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Account account = (Account) row[0];
                Customer customer = (Customer) row[1];
                if (customer == null) {
                    continue;
                }
                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("openbalance", account.getOpeningBalance());
                Account parentAccount = (Account) row[6];
                if(parentAccount!=null){
                    obj.put("parentid", parentAccount.getID());
                    obj.put("parentname", parentAccount.getName());
                }
                KWLCurrency currency = (KWLCurrency) row[5];
                obj.put("currencyid",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getSymbol()));
                obj.put("currencyname",(account.getCurrency()==null?currency.getName(): account.getCurrency().getName()));
                obj.put("level", row[3]);
                obj.put("leaf", row[4]);
                obj.put("title", customer.getTitle());
                obj.put("address", customer.getBillingAddress());
                obj.put("email", customer.getEmail());
                obj.put("contactno", customer.getContactNumber());
                obj.put("contactno2", customer.getAltContactNumber());
                obj.put("fax", customer.getFax());
                obj.put("shippingaddress", customer.getShippingAddress());
                obj.put("pdm", customer.getPreferedDeliveryMode());
                obj.put("termname", customer.getCreditTerm().getTermname());
                obj.put("termid", customer.getCreditTerm().getID());
                obj.put("termdays", customer.getCreditTerm().getTermdays());
                obj.put("nameinaccounts", customer.getAccount().getName());
                obj.put("bankaccountno", customer.getBankaccountno());
                obj.put("billto", customer.getBillingAddress());
                obj.put("other", customer.getOther());
                obj.put("deleted", customer.getAccount().isDeleted());
                obj.put("id", customer.getID());
                obj.put("taxno", customer.getTaxNo());
                obj.put("categoryid", account.getCategory()==null?"":account.getCategory().getID());
                obj.put("creationDate", authHandler.getDateFormatter(request).format(customer.getAccount().getCreationDate()));

                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCustomerJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView saveCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String customerID = null, msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Customer_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            Customer customer = saveCustomer(request);
            customerID = customer.getAccount().getID();
            issuccess = true;
            msg = messageSource.getMessage("acc.cus.save", null, RequestContextUtils.getLocale(request));   //"Customer information has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("perAccID", customerID);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public Customer saveCustomer(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        Customer customer = null;
        String auditMsg = "", auditID = "";
        try {
            String currencyid=(request.getParameter("currencyid")==null?sessionHandlerImpl.getCurrencyID(request):request.getParameter("currencyid"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String customerid = request.getParameter("accid");
            String accountName = request.getParameter("accname");
            String parentName = request.getParameter("parentname");

            boolean issub = request.getParameter("issub") != null;
            boolean debitType = StringUtil.getBoolean(request.getParameter("debitType"));
            double openBalance = StringUtil.getDouble(request.getParameter("openbalance"));
            openBalance = debitType ? openBalance : -openBalance;
            String parentid = request.getParameter("parentid");
            if (!issub) {
                parentid = null;
            }

            double life = StringUtil.getDouble(request.getParameter("life"));
            double salvage = StringUtil.getDouble(request.getParameter("salvage"));
            Date creationDate = authHandler.getDateFormatter(request).parse(request.getParameter("creationDate"));
            if (creationDate == null) {
                creationDate = new Date();
            }

            JSONObject accjson = new JSONObject();
            accjson.put("accountid", customerid);
            accjson.put("name", accountName);
            accjson.put("balance", openBalance);
            accjson.put("parentid", parentid);
            accjson.put("groupid", Group.ACCOUNTS_RECEIVABLE);
            accjson.put("companyid", companyid);
            accjson.put("currencyid", currencyid);
            accjson.put("life", life);
            accjson.put("salvage", salvage);
            accjson.put("creationdate", creationDate);
            accjson.put("category", request.getParameter("category"));

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", customerid);
            requestParams.put("accname", accountName);
            requestParams.put("parentid", parentid);
            requestParams.put("issub", request.getParameter("issub"));
            requestParams.put("debitType", request.getParameter("debitType"));
            requestParams.put("openbalance", request.getParameter("openbalance"));
            requestParams.put("title", request.getParameter("title"));
            requestParams.put("address", request.getParameter("address"));
            requestParams.put("bankaccountno", request.getParameter("bankaccountno"));
            requestParams.put("email", request.getParameter("email"));
            requestParams.put("contactno", request.getParameter("contactno"));
            requestParams.put("contactno2", request.getParameter("contactno2"));
            requestParams.put("fax", request.getParameter("fax"));
            requestParams.put("shippingaddress", request.getParameter("shippingaddress"));
            requestParams.put("termid", request.getParameter("termid"));
            requestParams.put("other", request.getParameter("other"));
            requestParams.put("taxno", request.getParameter("taxno"));
            requestParams.put("companyid", companyid);

            Account account;
            KwlReturnObject result;
            if (StringUtil.isNullOrEmpty(customerid)) {
                KwlReturnObject accresult = accAccountDAOobj.addAccount(accjson);
                account = (Account) accresult.getEntityList().get(0);
                requestParams.put("accountid", account.getID());
                result = accCustomerDAOobj.addCustomer(requestParams);
                auditMsg = " added new customer ";
                auditID = AuditAction.CUSTOMER_ADDED;
            } else {
//                if(accAccountDAOobj.isChild(customerid, parentid)){
//                    throw new AccountingException("\""+accountName+"\" is a parent of \""+parentName+"\" so can't set \""+parentName+"\" as a parent.");
                if(isChildorGrandChild(customerid, parentid)){
                    throw new AccountingException("\""+accountName+"\" is a parent of \""+parentName+"\" so can't set \""+parentName+"\" as a parent.");
               
            
            }
                KwlReturnObject accresult = accAccountDAOobj.updateAccount(accjson);
                account = (Account) accresult.getEntityList().get(0);
                requestParams.put("accountid", account.getID());
                result = accCustomerDAOobj.updateCustomer(requestParams);
                auditMsg = " updated customer ";
                auditID = AuditAction.CUSTOMER_UPDATED;
            }

            List ll = result.getEntityList();
            customer = (Customer) ll.get(0);
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg + customer.getName(), request, customer.getID());
        } catch (JSONException ex) {
//            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveCustomer : "+ex.getMessage(), ex);
        } catch (ParseException ex) {
//            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveCustomer : "+ex.getMessage(), ex);
        }
        return customer;
    }

    public ModelAndView getAddress(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        try {
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);

            String customerid = request.getParameter("customerid");
            result = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid);
            Customer customer = (Customer) result.getEntityList().get(0);
            jobj.put("shipingAddress", customer.getShippingAddress());
            jobj.put("billingAddress", customer.getBillingAddress());
            jobj.put("currencyid",(customer.getAccount().getCurrency()==null?currency.getCurrencyID(): customer.getAccount().getCurrency().getCurrencyID()));
            jobj.put("currencysymbol",(customer.getAccount().getCurrency()==null?currency.getSymbol(): customer.getAccount().getCurrency().getSymbol()));
            jobj.put("currencyname",(customer.getAccount().getCurrency()==null?currency.getName(): customer.getAccount().getCurrency().getName()));
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getAddress : " + ex;
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView exportCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getCustomerRequestMap(request);
            KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams);
            JSONArray jArr= getCustomerJson(request, list);
            jobj.put("data", jArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView importCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams)?new JSONObject():new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));

            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
//            ServerEventManager.publish("/importdata/111", "{total:34}",this.getServletContext());

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
            }
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", ""+ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCustomersByCategory(HttpServletRequest request, HttpServletResponse response) {
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
            filter_names.add("ISaccount.deleted");
            filter_params.add(false);
            requestParams.put("filter_names", filter_names);
            requestParams.put("filter_params", filter_params);
            order_by.add("account.category");
            order_type.add("desc");
            requestParams.put("order_by", order_by);
            requestParams.put("order_type", order_type);

            KwlReturnObject result = accCustomerDAOobj.getCustomerList(requestParams);
            JSONArray jArr= getCustomersByCategoryJson(request, result.getEntityList());

            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = ""+ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getCustomersByCategoryJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONArray jArr=new JSONArray();
        try{
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Customer customer = (Customer) itr.next();
                Account account = customer.getAccount();

                if(account.isDeleted()){
//                    continue;
                }
                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("openbalance", account.getOpeningBalance());

                obj.put("currencyid",(account.getCurrency()==null?"": account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol",(account.getCurrency()==null?"": account.getCurrency().getSymbol()));
                obj.put("currencyname",(account.getCurrency()==null?"": account.getCurrency().getName()));

                obj.put("title", customer.getTitle());
                obj.put("address", customer.getBillingAddress());
                obj.put("email", customer.getEmail());
                obj.put("contactno", customer.getContactNumber());
                obj.put("contactno2", customer.getAltContactNumber());
                obj.put("fax", customer.getFax());
                obj.put("shippingaddress", customer.getShippingAddress());
                obj.put("pdm", customer.getPreferedDeliveryMode());
                obj.put("termname", customer.getCreditTerm().getTermname());
                obj.put("termid", customer.getCreditTerm().getID());
                obj.put("termdays", customer.getCreditTerm().getTermdays());
                obj.put("nameinaccounts", customer.getAccount().getName());
                obj.put("bankaccountno", customer.getBankaccountno());
                obj.put("billto", customer.getBillingAddress());
                obj.put("other", customer.getOther());
                obj.put("deleted", customer.getAccount().isDeleted());
                obj.put("id", customer.getID());
                obj.put("taxno", customer.getTaxNo());
                obj.put("categoryid", account.getCategory()==null?"":account.getCategory().getID());
                obj.put("category", account.getCategory()==null?"":account.getCategory().getValue());
                obj.put("creationDate", authHandler.getDateFormatter(request).format(customer.getAccount().getCreationDate()));

                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCustomersByCategoryJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public boolean isChildorGrandChild(String customerid, String parentid) throws ServiceException{
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
    				if(Resultparent.equals(customerid))
    					return true;
    				else
    					return isChildorGrandChild(customerid, Resultparent);
    			}
   	 		}
    	}catch(Exception ex){
   	       throw ServiceException.FAILURE("isChildorGrandChild : "+ex.getMessage(), ex);
    	}
    	return false;
    }
}
