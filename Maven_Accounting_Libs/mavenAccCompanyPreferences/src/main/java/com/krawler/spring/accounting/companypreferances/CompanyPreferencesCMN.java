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

package com.krawler.spring.accounting.companypreferances;

import static com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants.*;

import com.krawler.common.admin.Company;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.YearLock;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public class CompanyPreferencesCMN {

    public static JSONObject getCompanyAccountPreferences (HttpServletRequest request, CompanyAccountPreferences pref)  throws ServiceException, SessionExpiredException {
        JSONObject obj = new JSONObject();
        try {
            if (pref == null) {
                return obj;
            }
            if (pref != null) {
                obj.put(PREFID, pref.getID());
                obj.put(FYFROM, authHandler.getDateFormatter(request).format(pref.getFinancialYearFrom()));
                obj.put(FIRSTFYFROM, pref.getFirstFinancialYearFrom()==null?authHandler.getDateFormatter(request).format(pref.getFinancialYearFrom()):authHandler.getDateFormatter(request).format(pref.getFirstFinancialYearFrom()));
                obj.put(BBFROM, authHandler.getDateFormatter(request).format(pref.getBookBeginningFrom()));
                obj.put(DISCOUNTGIVEN, pref.getDiscountGiven().getID());
                obj.put(DISCOUNTRECEIVED, pref.getDiscountReceived().getID());
                if (pref.getForeignexchange() != null) {
                    obj.put(FOREIGNEXCHANGE, pref.getForeignexchange().getID());
                }
                obj.put(SHIPPINGCHARGES,"");//pref.getShippingCharges().getID());
                obj.put(OTHERCHARGES, pref.getOtherCharges().getID());
                obj.put(CASHACCOUNT, pref.getCashAccount().getID());
                if (pref.getDepereciationAccount() != null) {
                    obj.put(DEPRECIATIONACCOUNT, pref.getDepereciationAccount().getID());
                }
                obj.put(AUTOINVOICE, pref.getInvoiceNumberFormat());
                obj.put(AUTOCREDITMEMO, pref.getCreditNoteNumberFormat());
                obj.put(AUTORECEIPT, pref.getReceiptNumberFormat());
                obj.put(AUTOJOURNALENTRY, pref.getJournalEntryNumberFormat());
                obj.put(AUTOGOODSRECEIPT, pref.getGoodsReceiptNumberFormat());
                obj.put(AUTODEBITNOTE, pref.getDebitNoteNumberFormat());
                obj.put(AUTOPAYMENT, pref.getPaymentNumberFormat());
                obj.put(AUTOSO, pref.getSalesOrderNumberFormat());
                obj.put(AUTOPO, pref.getPurchaseOrderNumberFormat());
                obj.put(AUTOCASHSALES, pref.getCashSaleNumberFormat());
                obj.put(AUTOBILLINGINVOICE, pref.getBillingInvoiceNumberFormat());
                obj.put(AUTOBILLINGRECEIPT, pref.getBillingReceiptNumberFormat());
                obj.put(AUTOBILLINGCASHSALES, pref.getBillingCashSaleNumberFormat());
                obj.put(AUTOBILLINGCASHPURCHASE, pref.getBillingCashPurchaseNumberFormat());
                obj.put(AUTOBILLINGGOODSRECEIPT, pref.getBillingGoodsReceiptNumberFormat());
                obj.put(AUTOBILLINGCREDITMEMO, pref.getBillingCreditNoteNumberFormat());
                obj.put(AUTOBILLINGDEBITNOTE, pref.getBillingDebitNoteNumberFormat());
                obj.put(AUTOBILLINGPAYMENT, pref.getBillingPaymentNumberFormat());
                obj.put(AUTOBILLINGSO, pref.getBillingSalesOrderNumberFormat());
                obj.put(AUTOBILLINGPO, pref.getBillingPurchaseOrderNumberFormat());
                obj.put(AUTOCASHPURCHASE, pref.getCashPurchaseNumberFormat());
                obj.put(EMAILINVOICE, pref.isEmailInvoice());
                obj.put(WITHOUTINVENTORY, pref.isWithoutInventory());
                obj.put(WITHOUTTAX1099, pref.isWithoutTax1099());
                obj.put(SETUPDONE, pref.isSetupDone());
                obj.put(COMPANYTYPE, pref.getCompanyType()==null?"":pref.getCompanyType().getName());
                obj.put(SERVERDATE, authHandler.getDateFormatter(request).format(new Date()));
                obj.put(AUTOQUOTATION, pref.getQuotationNumberFormat());
            }
            
            Company company = pref.getCompany();
            String creatorMailID = getSysEmailIdByCompanyID(company.getCompanyID());
            obj.put(COMPANYPHONENO, company.getPhoneNumber()==null?"":company.getPhoneNumber());
            obj.put(COMPANYEMAILID, StringUtil.isNullOrEmpty((String)company.getEmailID())?creatorMailID:company.getEmailID());
            obj.put("countryid", company.getCountry().getID());
        } catch (JSONException e) {
            throw ServiceException.FAILURE("getCompanyAccountPreferences : " + e.getMessage(), e);
        }
        return obj;
    }

    public static void checkLockPeriod(accCompanyPreferencesDAO accCompanyPreferencesObj, Map<String, Object> requestParams, Date date) throws ServiceException, AccountingException, SessionExpiredException, ParseException {
//        JSONObject jObjX=getYearLock(session,request);
        try {
            KwlReturnObject result = accCompanyPreferencesObj.getYearLock(requestParams);
            JSONArray DataJArr = getYearLockJson(accCompanyPreferencesObj, requestParams, result.getEntityList());
            JSONObject jObjX = new JSONObject();
            jObjX.put("data", DataJArr);

//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, companyid);
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(ID, (String) requestParams.get("companyid"));
            KwlReturnObject kresult = accCompanyPreferencesObj.getCompanyPreferences(filterParams);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) kresult.getEntityList().get(0);

            JSONArray jarr = jObjX.getJSONArray("data");
            for (int j = 0; j < jarr.length(); j++) {
                String sdate = jarr.getJSONObject(j).getString(STARTDATE);
                String edate = jarr.getJSONObject(j).getString(ENDDATE);
                if (StringUtil.isNullOrEmpty(sdate) || (StringUtil.isNullOrEmpty(edate))) {
                    throw new AccountingException("Please save the settings of Account Preferences first");
                }
                Date startDate = ((DateFormat) requestParams.get("df")).parse(sdate);
                Date endDate = ((DateFormat) requestParams.get("df")).parse(edate);
               
                if(startDate.getTime() != preferences.getFinancialYearFrom().getTime()){
	                if (((startDate.before(date) && endDate.after(date)) || (startDate.equals(date) || endDate.equals(date))) && jarr.getJSONObject(j).getBoolean(ISLOCK)) {
	                    throw new AccountingException("Transaction can not be completed. Date belongs to locked Period");
	                }
                }
                if (preferences.getBookBeginningFrom().compareTo(date) > 0 && preferences.getFirstFinancialYearFrom().compareTo(date) <= 0 && endDate.compareTo(date) > 0) {
                    throw new AccountingException("Transaction cannot be earlier than the book beginning date");
                }
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("checkLockPeriod : "+e.getMessage(), e);
        }
    }

    public static JSONArray getYearLockJson(accCompanyPreferencesDAO accCompanyPreferencesObj, Map<String, Object> requestParams, List<YearLock> list) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(ID, (String) requestParams.get("companyid"));
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(filterParams);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) result.getEntityList().get(0);

//            Iterator itr = list.iterator();
            if (list.isEmpty()) {
                for (int i = 1; i <= 5; i++) {
                    Calendar date = Calendar.getInstance();
                    int year = date.get(Calendar.YEAR);
                    JSONObject obj = new JSONObject();
                    obj.put(ID,"");
                    obj.put(NAME, year + i);
                    obj.put(ISLOCK, false);
                    obj.put(STARTDATE,"");
                    obj.put(ENDDATE,"");
                    jArr.put(obj);
                }
                for (int i = 1; i <= 5; i++) {
                    Calendar date = Calendar.getInstance();
                    int year = date.get(Calendar.YEAR);
                    JSONObject obj = new JSONObject();
                    obj.put(ID,"");
                    obj.put(NAME, year - i);
                    obj.put(ISLOCK, false);
                    obj.put(STARTDATE,"");
                    obj.put(ENDDATE,"");
                    jArr.put(obj);
                }
            } else {
                if(list!=null && !list.isEmpty()){
                    for(YearLock yl :list){
//                    YearLock yl = (YearLock) itr.next();
                        JSONObject obj = new JSONObject();
                        obj.put(ID, yl.getID());
                        obj.put(NAME, yl.getYearid());
                        obj.put(ISLOCK, yl.isIsLock());
                        if (pref != null) {
                            Calendar startFinYearCal = Calendar.getInstance();
                            Calendar endFinYearCal = Calendar.getInstance();
                            startFinYearCal.setTime(pref.getFinancialYearFrom());
                            endFinYearCal.setTime(pref.getFinancialYearFrom());
                            endFinYearCal.set(Calendar.YEAR, yl.getYearid() + 1);
                            endFinYearCal.add(Calendar.DATE, -1);
                            startFinYearCal.set(Calendar.YEAR, yl.getYearid());
                            obj.put(STARTDATE, ((DateFormat) requestParams.get("df")).format(startFinYearCal.getTime()));
                            obj.put(ENDDATE, ((DateFormat) requestParams.get("df")).format(endFinYearCal.getTime()));
                        }
                        jArr.put(obj);
                    }
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getYearLockJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    @SuppressWarnings("finally")
 	public static  String getSysEmailIdByCompanyID(String companyid){
     String emailId = "admin@deskera.com";
         try {
             List<?> result = HibernateUtil.executeQuery("from Company where companyID=?", companyid);
             if(result != null){
	             Company company = (Company) result.get(0);
	             if(company!=null){
	                 if(StringUtil.isNullOrEmpty(company.getEmailID()) && company.getCreator().getUserID() != null){
	                	 List<?> creatorResult = HibernateUtil.executeQuery("select emailID from User where userID=?", company.getCreator().getUserID());
	                	 if(creatorResult != null && creatorResult.size() > 0 && !StringUtil.isNullOrEmpty((String)creatorResult.get(0)) )
	                		 emailId = (String) creatorResult.get(0);
	                 } else 
		                 emailId = company.getEmailID();
	             }
             }
         } catch (Exception e) {
        	 throw ServiceException.FAILURE("getSysEmailIdByCompanyID : " + e.getMessage(), e);
         }
         finally{   
             return emailId;
         }
     } 
}
