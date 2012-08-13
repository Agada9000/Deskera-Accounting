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
package com.krawler.spring.accounting.vendor;

import com.krawler.accounting.utils.AuditAction;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Group;
import com.krawler.hql.accounting.Vendor;
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
public class accVendorController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accVendorDAO accVendorDAOobj;
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
    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
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

    public ModelAndView getVendors(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = getVendorRequestMap(request);
//            KwlReturnObject result = accAccountDAOobj.getAccounts(requestParams);
//            JSONArray jArr = getVendorJson(request, result.getEntityList());

            KwlReturnObject result = accVendorDAOobj.getVendor(requestParams);
            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams);
            JSONArray jArr= getVendorJson(request, list);

            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            issuccess = false;
            msg = "accVendorController.getVendors : " + ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getVendorsForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        JSONArray jArr=new JSONArray();
        try {
            HashMap<String, Object> requestParams = getVendorRequestMap(request);
            KwlReturnObject result = accVendorDAOobj.getVendorsForCombo(requestParams);
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
                Vendor vendor = (Vendor) row[1];
                if (vendor == null) {
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
                obj.put("termdays", vendor.getDebitTerm().getTermdays());
                obj.put("billto", vendor.getAddress());
                obj.put("deleted", vendor.getAccount().isDeleted());
                obj.put("email", vendor.getEmail());

                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accVendorController.getVendorsForCombo : "+ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public static HashMap<String, Object> getVendorRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("group", request.getParameterValues("group"));
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

    public static JSONArray getVendorJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONArray jArr=new JSONArray();
        try{
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Account account = (Account) row[0];
                Vendor vendor = (Vendor) row[2];
                if(vendor==null) continue;
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
                obj.put("id", vendor.getID());
                obj.put("title", vendor.getTitle());
                obj.put("address", vendor.getAddress());
                obj.put("email", vendor.getEmail());
                obj.put("contactno", vendor.getContactNumber());
                obj.put("contactno2", vendor.getAltContactNumber());
                obj.put("istaxeligible", vendor.isTaxEligible()?"Yes":"No");
                obj.put("fax", vendor.getFax());
                obj.put("pdm", vendor.getPreferedDeliveryMode());
                obj.put("termname", vendor.getDebitTerm().getTermname());
                obj.put("termdays", vendor.getDebitTerm().getTermdays());
                obj.put("termid", vendor.getDebitTerm().getID());
                obj.put("bankaccountno", vendor.getBankaccountno());
                obj.put("other", vendor.getOther());
                obj.put("billto", vendor.getAddress());
                obj.put("categoryid", account.getCategory()==null?"":account.getCategory().getID()); 
                obj.put("taxeligible", vendor.isTaxEligible());
                obj.put("taxidnumber", vendor.getTaxIDNumber());
                obj.put("creationDate", authHandler.getDateFormatter(request).format(vendor.getAccount().getCreationDate()));
                obj.put("deleted", vendor.getAccount().isDeleted());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getVendorJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView saveVendor(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String vendorID = null, msg="";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Vendor_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
		try {
            Vendor vendor = saveVendor(request);
            vendorID = vendor.getAccount().getID();

            issuccess = true;
            msg = messageSource.getMessage("acc.ven.save", null, RequestContextUtils.getLocale(request));   //"Vendor information has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg",msg);
                jobj.put("perAccID",vendorID);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public Vendor saveVendor(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        Vendor vendor = null;
        String auditMsg="", auditID="";
		try {
            String companyid=sessionHandlerImpl.getCompanyid(request);
            String vendorid=request.getParameter("accid");
            String accountName = request.getParameter("accname");
            String parentName = request.getParameter("parentname");
            String taxIDNumber=request.getParameter("taxidnumber");
            String currencyid=(request.getParameter("currencyid")==null?sessionHandlerImpl.getCurrencyID(request):request.getParameter("currencyid"));

            boolean issub = request.getParameter("issub")!=null;
            boolean debitType = StringUtil.getBoolean(request.getParameter("debitType"));
            boolean taxEligible = StringUtil.getBoolean(request.getParameter("taxeligible"));
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

            JSONObject accjson = new JSONObject();
            accjson.put("accountid", vendorid);
            accjson.put("name", accountName);
            accjson.put("balance", openBalance);
            accjson.put("parentid", parentid);
            accjson.put("groupid", Group.ACCOUNTS_PAYABLE);
            accjson.put("companyid", companyid);
            accjson.put("currencyid", currencyid);
            accjson.put("life", life);
            accjson.put("salvage", salvage);
            accjson.put("creationdate", creationDate);
            accjson.put("category", request.getParameter("category"));

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", vendorid);
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
            requestParams.put("taxidmailon", request.getParameter("taxidmailon"));
            requestParams.put("companyid", companyid);
            requestParams.put("taxeligible", taxEligible);
            requestParams.put("taxidnumber", taxIDNumber);

            Account account;
            KwlReturnObject result;
            if(StringUtil.isNullOrEmpty(vendorid)){
                KwlReturnObject accresult = accAccountDAOobj.addAccount(accjson);
                account = (Account) accresult.getEntityList().get(0);
                requestParams.put("accountid", account.getID());
                result = accVendorDAOobj.addVendor(requestParams);
                auditMsg = " added new vendor ";
                auditID = AuditAction.VENDOR_ADDED;
            }else{
//                if(accAccountDAOobj.isChild(vendorid, parentid)){
//                    throw new AccountingException("\""+accountName+"\" is a parent of \""+parentName+"\" so can't set \""+parentName+"\" as a parent.");
            	  if(isChildorGrandChild(vendorid, parentid)){
                     throw new AccountingException("\""+accountName+"\" is a parent of \""+parentName+"\" so can't set \""+parentName+"\" as a parent.");
            }
                KwlReturnObject accresult = accAccountDAOobj.updateAccount(accjson);
                account = (Account) accresult.getEntityList().get(0);
                requestParams.put("accountid", account.getID());
                result = accVendorDAOobj.updateVendor(requestParams);
                auditMsg = " updated vendor ";
                auditID = AuditAction.VENDOR_UPDATED;
            }

            List ll = result.getEntityList();
            vendor = (Vendor)ll.get(0);
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg + vendor.getName(), request, vendor.getID());
        } catch (JSONException ex) {
//            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveVendor : "+ex.getMessage(), ex);
        } catch (ParseException ex) {
//            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveVendor : "+ex.getMessage(), ex);
        }
        return vendor;
    }
public ModelAndView saveVendorMailingDate(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String vendorID = null, msg="";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Vendor_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
		try {
            Vendor vendor = saveVendorMailingDate(request);
            vendorID = vendor.getAccount().getID();

            issuccess = true;
            msg = messageSource.getMessage("acc.ven.save", null, RequestContextUtils.getLocale(request));   //"Vendor information has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg",msg);
                jobj.put("perAccID",vendorID);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

     public Vendor saveVendorMailingDate(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        Vendor vendor = null;
		try {
            Date mailedOn = request.getParameter("taxidmailon")==null?null:authHandler.getDateFormatter(request).parse(request.getParameter("taxidmailon"));
            if (mailedOn == null) {
                mailedOn = new Date();
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", request.getParameter("accid"));
            requestParams.put("taxidmailon", mailedOn);
            KwlReturnObject result;
            result = accVendorDAOobj.updateVendor(requestParams);
            List ll = result.getEntityList();
            vendor = (Vendor)ll.get(0);
        } catch (ParseException ex) {
//            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveVendor : "+ex.getMessage(), ex);
        }
        return vendor;
    }
    public ModelAndView getAddress(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = true;
		try {
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);

            String vendorid = request.getParameter("customerid");
            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorid);
            Vendor vendor = (Vendor) venresult.getEntityList().get(0);
            jobj.put("shipingAddress",vendor.getAddress());
            jobj.put("billingAddress",vendor.getAddress());
            jobj.put("currencyid",(vendor.getAccount().getCurrency()==null?currency.getCurrencyID(): vendor.getAccount().getCurrency().getCurrencyID()));
            jobj.put("currencysymbol",(vendor.getAccount().getCurrency()==null?currency.getSymbol(): vendor.getAccount().getCurrency().getSymbol()));
            jobj.put("currencyname",(vendor.getAccount().getCurrency()==null?currency.getName(): vendor.getAccount().getCurrency().getName()));
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg="accVendorController.getAddress : "+ex;
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView exportVendor(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getVendorRequestMap(request);
            KwlReturnObject result = accVendorDAOobj.getVendor(requestParams);
            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams);
            JSONArray jArr = getVendorJson(request, list);
            jobj.put("data", jArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView importVendor(HttpServletRequest request, HttpServletResponse response) {
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
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getVendorsByCategory(HttpServletRequest request, HttpServletResponse response) {
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

            KwlReturnObject result = accVendorDAOobj.getVendorList(requestParams);
            JSONArray jArr= getCustomersByCategoryJson(request, result.getEntityList());

            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getCustomersByCategoryJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONArray jArr=new JSONArray();
        try{
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Vendor vendor = (Vendor) itr.next();
                Account account = vendor.getAccount();

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

                obj.put("id", vendor.getID());
                obj.put("title", vendor.getTitle());
                obj.put("address", vendor.getAddress());
                obj.put("email", vendor.getEmail());
                obj.put("contactno", vendor.getContactNumber());
                obj.put("contactno2", vendor.getAltContactNumber());
                obj.put("fax", vendor.getFax());
                obj.put("pdm", vendor.getPreferedDeliveryMode());
                obj.put("termname", vendor.getDebitTerm().getTermname());
                obj.put("termdays", vendor.getDebitTerm().getTermdays());
                obj.put("termid", vendor.getDebitTerm().getID());
                obj.put("bankaccountno", vendor.getBankaccountno());
                obj.put("other", vendor.getOther());
                obj.put("billto", vendor.getAddress());
                obj.put("creationDate", authHandler.getDateFormatter(request).format(vendor.getAccount().getCreationDate()));
                obj.put("deleted", vendor.getAccount().isDeleted());
                obj.put("categoryid", account.getCategory()==null?"":account.getCategory().getID());
                obj.put("category", account.getCategory()==null?"":account.getCategory().getValue());

                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCustomersByCategoryJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public boolean isChildorGrandChild(String vendorid, String parentid) throws ServiceException{
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
    				if(Resultparent.equals(vendorid))
    					return true;
    				else
    					return isChildorGrandChild(vendorid, Resultparent);
    			}
   	 		}
    	}catch(Exception ex){
   	       throw ServiceException.FAILURE("isChildorGrandChild : "+ex.getMessage(), ex);
    	}
    	return false;
    }
    
}
