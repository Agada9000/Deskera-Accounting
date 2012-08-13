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

package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.APICallHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.ExchangeRateDetails;
import com.krawler.hql.accounting.Group;
import com.krawler.hql.accounting.PaymentMethod;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.TaxList;
import com.krawler.hql.accounting.YearLock;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class NewCompanySetupController extends MultiActionController {
    private HibernateTransactionManager txnManager;
    private accAccountDAO accAccountDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accTaxDAO accTaxObj;
    private accPaymentDAO accPaymentDAOobj;
    private accProductDAO accProductObj;
    private companyDetailsDAO companyDetailsDAOobj;


    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOobj) {
		this.companyDetailsDAOobj = companyDetailsDAOobj;
	}
	public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setaccTaxDAO (accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public ModelAndView SetupCompany(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        JSONObject curObj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
            JSONObject setUpData = new JSONObject(request.getParameter("data"));

            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
        	KwlReturnObject currencyrec = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), setUpData.getString("currencyid"));
            KWLCurrency kwlCurrency = (KWLCurrency) currencyrec.getEntityList().get(0);

            try {
            	HashMap<String, Object> hmcompany = new HashMap<String, Object>();
            	hmcompany.put("companyid", companyid);
            	if(setUpData.has("countryid")) {
            		hmcompany.put("country", setUpData.getString("countryid"));
            	}
            	if(setUpData.has("currencyid")) {
            		hmcompany.put("currency", setUpData.getString("currencyid"));
            	}
            	companyDetailsDAOobj.updateCompany(hmcompany);
            	
            	HttpSession sessionforCurrency = request.getSession(true);
            	sessionforCurrency.setAttribute("currencyid", setUpData.getString("currencyid"));
                
                curObj.put("Currency", kwlCurrency.getHtmlcode());
                curObj.put("CurrencyName", kwlCurrency.getName());
                curObj.put("CurrencySymbol", kwlCurrency.getSymbol());
                curObj.put("Currencyid", kwlCurrency.getCurrencyID());
                
            } catch (Exception e) {
            	throw new AccountingException("save Currency & Country: "+e.getMessage());
            }
            
            try {
            	JSONObject accjson = new JSONObject();
            	accjson.put("accountid", preferences.getDepereciationAccount().getID());
            	accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accAccountDAOobj.updateAccount(accjson);
                
                accjson = new JSONObject();
            	accjson.put("accountid", preferences.getDiscountGiven().getID());
            	accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accAccountDAOobj.updateAccount(accjson);
                
                accjson = new JSONObject();
            	accjson.put("accountid", preferences.getDiscountReceived().getID());
            	accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accAccountDAOobj.updateAccount(accjson);
                
                accjson = new JSONObject();
            	accjson.put("accountid", preferences.getCashAccount().getID());
            	accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accAccountDAOobj.updateAccount(accjson);
                
                accjson = new JSONObject();
                accjson.put("accountid", preferences.getForeignexchange().getID());
            	accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accAccountDAOobj.updateAccount(accjson);
                
                accjson = new JSONObject();
                accjson.put("accountid", preferences.getOtherCharges().getID());
            	accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accAccountDAOobj.updateAccount(accjson);
                
            } catch (Exception e) {
            	throw new AccountingException("change 5 accounts currencyid in preferences: "+e.getMessage());
            }

            currencyid = sessionHandlerImpl.getCurrencyID(request);
            
            try {
                if(setUpData.has("companyTypeId") && setUpData.has("companyTypeId")){
                    if(setUpData.getString("addDefaultAccount").equalsIgnoreCase("Yes")) {
                        accAccountDAOobj.copyAccounts(companyid, currencyid, setUpData.getString("companyTypeId"));
                    }
                }
            }catch(Exception ex) {
                throw new AccountingException("Copy Accounts: "+ex.getMessage());
            }

            // Save Currency Details
            try {
                JSONArray currJArr = setUpData.getJSONArray("currencyDetails");
                for (int i = 0; i < currJArr.length(); i++) {
                    JSONObject cJObj = currJArr.getJSONObject(i);

                    Date appDate = authHandler.getDateFormatter(request).parse(URLDecoder.decode(cJObj.getString("applydate"),StaticValues.ENCODING));
                    Calendar applyDate = Calendar.getInstance();
                    applyDate.setTime(appDate);

                    String erid = URLDecoder.decode(cJObj.getString("erid"),StaticValues.ENCODING);
                    HashMap<String, Object> filterParams = new HashMap<String, Object>();
                    filterParams.put("erid", erid);
                    filterParams.put("applydate", appDate);
                    filterParams.put("companyid", companyid);
                    KwlReturnObject result = accCurrencyDAOobj.getExchangeRateDetails(filterParams, false);
                    List list = result.getEntityList();

                    HashMap<String, Object> erdMap = new HashMap<String, Object>();
                    erdMap.put("exchangerate", Double.parseDouble(URLDecoder.decode(cJObj.getString("exchangerate"),StaticValues.ENCODING)));

                    ExchangeRateDetails erd;
                    KwlReturnObject erdresult;
                    if (list.size() <= 0) {
                        erdMap.put("applydate",applyDate.getTime());
                        erdMap.put("erid",erid);
                        erdMap.put("companyid",companyid);
                        erdresult = accCurrencyDAOobj.addExchangeRateDetails(erdMap);
                    } else {
                        erd = (ExchangeRateDetails) list.get(0);
                        erdMap.put("erdid",erd.getID());
                        erdresult = accCurrencyDAOobj.updateExchangeRateDetails(erdMap);
                    }
                    erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);
                }
            }catch(Exception ex) {
                throw new AccountingException("Save Currency rates: "+ex.getMessage());
            }

            
            // Save Tax Details ==> 1.Create Account, 2.Save Tax, 3.Save TaxList
            try {
                JSONArray taxJArr = setUpData.getJSONArray("taxDetails");
                for (int i = 0; i < taxJArr.length(); i++) {
                    JSONObject tJObj = taxJArr.getJSONObject(i);
                    Date appDate = authHandler.getDateFormatter(request).parse(URLDecoder.decode(tJObj.getString("applydate"),StaticValues.ENCODING));

                    //Create Account
                    JSONObject accjson = new JSONObject();
                    accjson.put("depaccountid", preferences.getDepereciationAccount().getID());
                    accjson.put("name", URLDecoder.decode(tJObj.getString("name"),StaticValues.ENCODING));
                    accjson.put("balance", 0.0);
                    accjson.put("groupid", Group.OTHER_CURRENT_LIABILITIES);
                    accjson.put("companyid", companyid);
                    accjson.put("currencyid", currencyid);
                    accjson.put("life", 10.0);
                    accjson.put("salvage", 0.0);
                    accjson.put("creationdate", appDate);
                    KwlReturnObject accresult = accAccountDAOobj.addAccount(accjson);
                    Account taxAccount = (Account) accresult.getEntityList().get(0);

                    //Create Tax
                    HashMap<String,Object> taxMap = new HashMap<String, Object>();
                    taxMap.put("taxname", URLDecoder.decode(tJObj.getString("name"),StaticValues.ENCODING));
                    taxMap.put("taxcode", URLDecoder.decode(tJObj.getString("code"),StaticValues.ENCODING));
                    taxMap.put("accountid", taxAccount.getID());
                    taxMap.put("companyid", companyid);
                    KwlReturnObject taxresult = accTaxObj.addTax(taxMap);
                    Tax tax = (Tax) taxresult.getEntityList().get(0);

                    //Create taxList
                    HashMap<String, Object> taxListMap = new HashMap<String, Object>();
                    taxListMap.put("taxid", tax.getID());
                    taxListMap.put("applydate", appDate);
                    taxListMap.put("companyid", companyid);
                    taxListMap.put("percent", Double.parseDouble(tJObj.getString("percent")));
                    KwlReturnObject taxlistresult = accTaxObj.addTaxList(taxListMap);
                    TaxList taxlist = (TaxList) taxlistresult.getEntityList().get(0);
                }
            }catch(Exception ex) {
                throw new AccountingException("Save Tax Details: "+ex.getMessage());
            }

            // Save Bank Details ==> 1.Create Account, 2.Save Bank
            try {
                JSONArray bankJArr = setUpData.getJSONArray("bankDetails");
                for (int i = 0; i < bankJArr.length(); i++) {
                    JSONObject bJObj = bankJArr.getJSONObject(i);
                    Date appDate = authHandler.getDateFormatter(request).parse(URLDecoder.decode(bJObj.getString("applydate"),StaticValues.ENCODING));

                    //Create Account
                    JSONObject accjson = new JSONObject();
                    accjson.put("depaccountid", preferences.getDepereciationAccount().getID());
                    accjson.put("name", URLDecoder.decode(bJObj.getString("name"),StaticValues.ENCODING));
                    accjson.put("balance", Double.parseDouble(URLDecoder.decode(bJObj.getString("balance"),StaticValues.ENCODING)));
                    accjson.put("groupid", Group.BANK_ACCOUNT);
                    accjson.put("companyid", companyid);
                    accjson.put("currencyid", currencyid);
                    accjson.put("life", 10.0);
                    accjson.put("salvage", 0.0);
                    accjson.put("creationdate", appDate);
                    KwlReturnObject accresult = accAccountDAOobj.addAccount(accjson);
                    Account bankAccount = (Account) accresult.getEntityList().get(0);

                    HashMap<String, Object> methodMap = new HashMap<String, Object>();
                    methodMap.put("methodname", URLDecoder.decode(bJObj.getString("accountname"),StaticValues.ENCODING));
                    methodMap.put("accountid", bankAccount.getID());
                    methodMap.put("detailtype", 2); //2:Bank Type
                    methodMap.put("companyid", companyid);

                    KwlReturnObject methodresult = accPaymentDAOobj.addPaymentMethod(methodMap);
                    PaymentMethod pom = (PaymentMethod) methodresult.getEntityList().get(0);
                }
            }catch(Exception ex) {
                throw new AccountingException("Save Bank Details: "+ex.getMessage());
            }

            // Save Lock Year Details
            try {
                JSONArray lockJArr = setUpData.getJSONArray("lockDetails");
                for (int i = 0; i < lockJArr.length(); i++) {
                    JSONObject lJObj = lockJArr.getJSONObject(i);
                    HashMap<String, Object> yearLockMap = new HashMap<String, Object>();
                    yearLockMap.put("yearid", Integer.parseInt(lJObj.getString("name")));
                    yearLockMap.put("islock", "true".equalsIgnoreCase(lJObj.getString("islock")));
                    yearLockMap.put("companyid", companyid);

                    String yearLockid = lJObj.getString("id");
                    KwlReturnObject result;
                    if (StringUtil.isNullOrEmpty(yearLockid)) {
                        result = accCompanyPreferencesObj.addYearLock(yearLockMap);
                    } else {
                        yearLockMap.put("id", yearLockid);
                        result = accCompanyPreferencesObj.updateYearLock(yearLockMap);
                    }
                    YearLock yearlock = (YearLock) result.getEntityList().get(0);
                }
            }catch(Exception ex) {
                throw new AccountingException("Save Lock Year Details: "+ex.getMessage());
            }

            KwlReturnObject cap1 = kwlCommonTablesDAOObj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cap1.getEntityList().get(0);

            try {
                HashMap<String, Object> prefMap = new HashMap<String, Object>();
                prefMap.put("setupdone", true);
                prefMap.put("companytype", setUpData.getString("companyTypeId"));
                prefMap.put("fyfrom", authHandler.getDateFormatter(request).parse(setUpData.getString("yearStartDate")));
                prefMap.put("bbfrom", authHandler.getDateFormatter(request).parse(setUpData.getString("bookStartDate")));
                prefMap.put("withoutinventory", (!setUpData.getString("withInventory").equalsIgnoreCase("Yes")));
                prefMap.put("withouttax1099", (!setUpData.getString("withTax1099").equalsIgnoreCase("Yes")));
                prefMap.put("id", sessionHandlerImpl.getCompanyid(request));
                prefMap.put("autojournalentry", "000000");
                if(!company.getCountry().getID().equals(setUpData.getString("countryid")))
                	prefMap.put("countryChange", true);
                if(!company.getCurrency().getCurrencyID().equals(setUpData.getString("currencyid")))
                	prefMap.put("currencyChange", true);
                KwlReturnObject result = accCompanyPreferencesObj.updatePreferences(prefMap);
            }catch(Exception ex) {
                throw new AccountingException("Save Preferences: "+ex.getMessage());
            }
            
            txnManager.commit(status);
            issuccess = true;
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("currency", curObj);
            } catch (JSONException ex) {
                Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView sendAccProducts(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg="";
        boolean issuccess=false;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String crmURL = this.getServletContext().getInitParameter("crmURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);

            Session session=HibernateUtil.getCurrentSession();
            String action = "99";
            JSONObject pjobj=getProducts(request, response);
            userData.put("data", pjobj);
            JSONObject resObj = APICallHandler.callApp(session, crmURL, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                issuccess=resObj.getBoolean("success");
                msg=resObj.getString("msg");
                jobj.put("success", true);
                jobj.put("msg", msg);
                jobj.put("companyexist", resObj.optBoolean("companyexist"));
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
         } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getProducts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();;
            KwlReturnObject result = accProductObj.getProductTypes(requestParams);
            List list = result.getEntityList();
            JSONArray DataJArr = productHandler.getProductTypesJson(request, list);
            jobj.put("typedata", DataJArr);

            requestParams = productHandler.getProductRequestMap(request);
            result = accProductObj.getProducts(requestParams);
            list = result.getEntityList();
            DataJArr = productHandler.getProductsJson(request, list);
            jobj.put("productdata", DataJArr); 
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return  jobj;
    }
}
