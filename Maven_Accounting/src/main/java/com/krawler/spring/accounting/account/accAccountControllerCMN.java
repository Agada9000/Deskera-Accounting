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

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Asset;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.depreciation.accDepreciationDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
public class accAccountControllerCMN extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accAccountDAO accAccountDAOobj;
    private accProductDAO accProductObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accPaymentDAO accPaymentDAOobj;
    private accTaxDAO accTaxObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private String successView;
    private accCurrencyDAO accCurrencyDAOobj;
    private accDepreciationDAO accDepreciationDAOobj;
    private MessageSource messageSource;
    
    @Override
	public void setMessageSource(MessageSource msg) {
		this.messageSource = msg;
	}
    public void setaccDepreciationDAO(accDepreciationDAO accDepreciationDAOobj) {
		this.accDepreciationDAOobj = accDepreciationDAOobj;
	}
	public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
	public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public ModelAndView deleteAccount(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
		try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("data", request.getParameter("data"));
            String fixAsset = request.getParameter("isFixedAsset");
            boolean isFixedAsset = StringUtil.isNullOrEmpty(fixAsset)?false:Boolean.parseBoolean(fixAsset);
            requestParams.put("isFixedAsset", isFixedAsset);

            String companyid=sessionHandlerImpl.getCompanyid(request);
            if(!isFixedAsset){
            	deleteAccount(requestParams, companyid);
            }else{
            	deleteAssetPurchase(request, requestParams, companyid);
            }
            
            issuccess = true;
            msg = (isFixedAsset?messageSource.getMessage("acc.acc.delasset", null, RequestContextUtils.getLocale(request)):messageSource.getMessage("acc.acc.del", null, RequestContextUtils.getLocale(request)));
            txnManager.commit(status);
		} catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
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

    public void deleteAccount(HashMap request, String companyid) throws AccountingException, ServiceException {
        KwlReturnObject result = null;
        try{
            JSONArray jArr = new JSONArray((String)request.get("data"));
            boolean isFixedAsset = (Boolean) request.get("isFixedAsset");
            String accountid = "";
            int count = 0;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                accountid = URLDecoder.decode(jobj.getString("accid"),StaticValues.ENCODING);
                if (!StringUtil.isNullOrEmpty(jobj.getString("accid"))) {
                       try {
                            //Delete Account and Subaccounts          Neeraj
                        	List<String> ChildsArray = new ArrayList<String>();
                        	ChildsArray = scanChildAccounts(accountid);
                        	Iterator<String> ChildsArrayiterator = ChildsArray.iterator();
                        	while(ChildsArrayiterator.hasNext()){
                        		Object obj = ChildsArrayiterator.next();
                        		Logger.getLogger(accAccountController.class.getName()).info("\n"+obj.toString());
                        		
                        		accountid = obj.toString();
                        		if(!isFixedAsset) { //Dont check Opening Balance for Fix Asset
                                    if (jobj.getDouble("openbalance") != 0) {
                                        throw new AccountingException("Selected record(s) is having the Opening Balance. So it cannot be deleted");
                                    }
                                }
                                // Check in Journal Entry
                                result = accJournalEntryobj.getJEDfromAccount(accountid, companyid);
                                count = result.getRecordTotalCount();
                                if (count > 0) {
                                    throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
                                }

                                // Check Product Entry
                                result = accProductObj.getProductfromAccount(accountid, companyid);
                                count = result.getRecordTotalCount();
                                if (count > 0) {
                                    throw new AccountingException("Selected record(s) is currently used in the Account Preferences. So it cannot be deleted.");
                                }

                                // Check for Preferances Entry
                                result = accCompanyPreferencesObj.getPreferencesFromAccount(accountid, companyid);
                                count = result.getRecordTotalCount();
                                if (count > 0) {
                                    throw new AccountingException("Selected record(s) is currently used in the Product(s). So it cannot be deleted.");
                                }

                                // Check fot Payment Entry
                                result = accPaymentDAOobj.getPaymentMethodFromAccount(accountid, companyid);
                                count = result.getRecordTotalCount();
                                if (count > 0) {
                                    throw new AccountingException("Selected record(s) is currently used in the Term(s). So it cannot be deleted.");
                                }

                                // Check for Tax Entry
                                result = accTaxObj.getTaxFromAccount(accountid, companyid);
                                count = result.getRecordTotalCount();
                                if (count > 0) {
                                    throw new AccountingException("Selected record(s) is currently used in the Tax(s). So it cannot be deleted.");
                                }
                        	}
          //   Now when the parent and children arent used in any of the transactions above we can now delete the whole hierrarchy
                        	Iterator<String> ChildsDeleteiterator = ChildsArray.iterator();
                            while(ChildsDeleteiterator.hasNext()){
                        		Object obj = ChildsDeleteiterator.next();
                        		accAccountDAOobj.deleteAccount(obj.toString(), true);
                            }
                      
                        } catch (ServiceException ex) {
                            throw new AccountingException("Selected record(s) is currently used in the transaction(s).");
                        }
                }
            }
        } catch(UnsupportedEncodingException ex){
            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("deleteAccount : "+ex.getMessage(), ex);
        }
    }

    /**
     * @Author Neeraj
     * @param accountID
     * @return ReturnAccountChilds "The list incuding the parent and their children and grandchildren and so on"
     * @throws ServiceException
     */
    public List<String> scanChildAccounts(String accountID) throws ServiceException{
    	try{
    		List<String> ReturnAccountChilds = new ArrayList<String>();
    		ReturnAccountChilds.add(accountID);
    		boolean flag = true;
    		List<String> AccountChilds = new ArrayList<String>();
    		AccountChilds.add(accountID);
    		while(flag == true){
    			String str = (String)AccountChilds.get(0);
    			List<?> Result = accAccountDAOobj.isChildforDelete(str);
    			if(Result != null){	
    				Iterator<?> resultIterator = Result.iterator();
    				while(resultIterator.hasNext()){
    					Object ResultObj = resultIterator.next();
    					Account account = (Account) ResultObj;
    					String Child = account.getID();
    					AccountChilds.add(Child);
    					ReturnAccountChilds.add(Child);
    				}
    			}	
    			AccountChilds.remove(0);
    			if(AccountChilds.isEmpty() == true){
    				flag = false;
    			}
    		}
    		return ReturnAccountChilds;
    	}
    	catch(Exception ex){
    		throw ServiceException.FAILURE("scanChildAccounts : "+ex.getMessage(), ex);
    	}
    }
    
    /**
     * @Author Neeraj
     * @param request	" The Account ID for which total balance sheet value is to be found out"
     * @param response	
     * @return netAssetValue   "Net Balance Sheet value of a Fixed Asset or  any other Account"
     */
    public ModelAndView getNetAssetValue(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg=""; 
        double netAssetValue = 0,JEamount = 0; 
        Double JEamount1;
        boolean issuccess = false;
        KwlReturnObject kwlReturnObject = null, kwlBaseCurrencyrate;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
		try{
			String FixedAssetId = request.getParameter("fixedAssetID");
            String currencyID;
			String companyid=sessionHandlerImpl.getCompanyid(request);
            Date dt = new Date();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
        
            
            
            
            kwlReturnObject = accJournalEntryobj.getAccountBalance(FixedAssetId, null, null);
            
            Iterator<?> netValue =  kwlReturnObject.getEntityList().iterator();
            
            while(netValue.hasNext()){
            	Object[] row = (Object[]) netValue.next();
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) row[1];
                currencyID = (String)row[2];
                
                boolean currencyEqual = currencyID.equalsIgnoreCase(sessionHandlerImpl.getCurrencyID(request));
                JEamount = journalEntryDetail.getAmount();
                JEamount1 = JEamount;
                if(!currencyEqual){
                	kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, JEamount1, currencyID, dt, 0);
                	JEamount = (Double)kwlBaseCurrencyrate.getEntityList().get(0);

                }
             
                
                if(journalEntryDetail.isDebit()){
                	netAssetValue = netAssetValue + JEamount;
                }else{
                	netAssetValue = netAssetValue - JEamount;
                }
            }
            
            issuccess = true;
            msg = "Net Fixed Asset Value has been fetched successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
            	jobj.put("netAssetValue", Double.parseDouble(decimalFormat.format(netAssetValue)));
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * @Author Neeraj
     * @param request
     * @param response 
     */
    public ModelAndView removeAsset(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
		try{
			String FixedAssetId = request.getParameter("fixedAssetID");
			String companyid=sessionHandlerImpl.getCompanyid(request);
            
            accAccountDAOobj.deleteAccount(FixedAssetId, true);
            
    		HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", request.getParameter("fixedAssetID"));
            requestParams.put("companyid", companyid);
            if(Boolean.parseBoolean(request.getParameter("isWriteOff"))){
            	requestParams.put("isSale", false);
            	requestParams.put("isWriteOff", true);
            }else{
            	requestParams.put("isSale", true);
            	requestParams.put("isWriteOff", false);
            }
            requestParams.put("deleteJe", request.getParameter("deleteJe"));
            
            accDepreciationDAOobj.addAssetDetail(requestParams);
            
            issuccess = true;
            msg = messageSource.getMessage("acc.main.fadel", null, RequestContextUtils.getLocale(request));   //"Fixed Asset has been removed successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
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
    
    
    public void deleteAssetPurchase(HttpServletRequest request1, HashMap request, String companyid) throws AccountingException, ServiceException{
    	try{
    		JSONArray jArr = new JSONArray((String)request.get("data"));
    		JSONObject jobj = jArr.getJSONObject(0);
            if (!StringUtil.isNullOrEmpty(jobj.getString("accid"))) {
                String assetid = URLDecoder.decode(jobj.getString("accid"),StaticValues.ENCODING);
        		HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("accountid", assetid);
                requestParams.put("companyid", companyid);
                KwlReturnObject result = accDepreciationDAOobj.getDepreciation(requestParams);
                if(result != null && result.getEntityList().size() != 0){
                	throw new AccountingException(messageSource.getMessage("acc.rem.156", null, RequestContextUtils.getLocale(request1)));
                }else{
                	requestParams.clear();
                	requestParams.put("id", assetid);
                	result = accDepreciationDAOobj.getAsset(requestParams);
                	if(result != null && result.getEntityList().size() > 0){
                		Asset asset = (Asset) result.getEntityList().get(0);
                		
                		String purchaseJe = asset.getPurchaseJe().getID();
                		accJournalEntryobj.deleteJEEntry(purchaseJe, companyid);
                		
                	}
                	accAccountDAOobj.deleteAccount(assetid, true);
                }

            }
    		
    	}catch(ServiceException ex){
    		throw new AccountingException("Selected record(s) is currently used in the transaction(s).");
    	} catch(UnsupportedEncodingException ex){
            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("deleteAssetPurchase : "+ex.getMessage(), ex);
        }
    }
}    
