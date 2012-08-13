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
package com.krawler.esp.handlers;


import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.ExchangeRateDetails;
import com.krawler.hql.accounting.Group;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.TaxList;
import com.krawler.hql.accounting.UnitOfMeasure;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;

public class CompanyHandler {
    public static double getDouble(HttpServletRequest request, String fieldName){
        double value=0.0;
        try{
            value=Double.parseDouble(request.getParameter(fieldName));
        }catch(NumberFormatException e){
             //if wrong format will be given, 0.0 will be used as default
        }catch(NullPointerException e){
             //if no value will be given, 0.0 will be used as default
        }
        return value;
    }

    public static boolean getBoolean(HttpServletRequest request, String fieldName){
        return "true".equalsIgnoreCase(request.getParameter(fieldName));
    }

    public static String generateNextAutoNumber(String pattern, String strCurrent){
        StringBuffer strNext=new StringBuffer(pattern);
        int x=0;
        if(strCurrent!=null&&pattern.length()==strCurrent.length()){
            for(x=0;x<pattern.length();x++){
                if(pattern.charAt(x)=='0'&&(strCurrent.charAt(x)<'0'||strCurrent.charAt(x)>'9'))
                    break;
            }
        }
        if(x==pattern.length())
            strNext=new StringBuffer(strCurrent);
        int carry=1;
        for(int i=strNext.length()-1;i>=0;i--){
            if(pattern.charAt(i)!='0') continue;
            int sum=(strNext.charAt(i)-'0')+carry;
            strNext.setCharAt(i, (char)(sum%10+'0'));
            carry=sum/10;
        }
        return strNext.toString();
    }
    /*public static void saveCurrencyExchange(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException, JSONException{
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            for (int i = 0; i < jArr.length(); i++) {
                ArrayList params = new ArrayList();
                JSONObject jobj = jArr.getJSONObject(i);
                ExchangeRate er = (ExchangeRate) session.get(ExchangeRate.class, jobj.getString("id"));
                Date appDate=AuthHandler.getDateFormatter(request).parse(jobj.getString("applydate"));
                Calendar applyDate = Calendar.getInstance();
                applyDate.setTime(appDate);
                String query="from ExchangeRateDetails where applyDate=? and exchangeratelink.ID=? and company.companyID=?";
                params.add(appDate);
                params.add(er.getID());
                params.add(company.getCompanyID());
                List list = HibernateUtil.executeQuery(session, query, params.toArray());
                ExchangeRateDetails erd;
                if(list.size()<=0){
                    //throw new AccountingException("Can not change edit the Exchange Rate.");
                    erd=new ExchangeRateDetails();
                    erd.setApplyDate(applyDate.getTime());
                    erd.setExchangeratelink(er);
                    erd.setCompany(company);
                }else
                    erd=(ExchangeRateDetails)list.get(0);
                erd.setExchangeRate(Double.parseDouble(jobj.getString("exchangerate")));
                session.saveOrUpdate(erd);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.saveCurrencyExchange", ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("CompanyHandler.saveCurrencyExchange", ex);
        }
    }*/

    public static JSONObject getCurrencyExchange(Session session, HttpServletRequest request,Date transactiondate ) throws ServiceException, SessionExpiredException {
        JSONObject jobj=new JSONObject();
		try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            ArrayList params = new ArrayList();
            params.add(currency.getCurrencyID());
            String erIDQuery = "select ID from ExchangeRate where fromCurrency.currencyID=?";
            List list = HibernateUtil.executeQuery(session, erIDQuery,params.toArray());
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                String erID=(String)itr.next();
                ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,null,transactiondate,erID);
                JSONObject obj = new JSONObject();
                if(erd!=null) {
                    obj.put("id", erd.getExchangeratelink().getID());
                    obj.put("applydate", AuthHandler.getDateFormatter(request).format(erd.getApplyDate()));
                    obj.put("exchangerate", erd.getExchangeRate());
                    obj.put("fromcurrency", erd.getExchangeratelink().getFromCurrency().getName());
                    obj.put("symbol", erd.getExchangeratelink().getToCurrency().getSymbol());
                    obj.put("htmlcode", erd.getExchangeratelink().getToCurrency().getHtmlcode());
                    obj.put("tocurrency", erd.getExchangeratelink().getToCurrency().getName());
                    obj.put("tocurrencyid", erd.getExchangeratelink().getToCurrency().getCurrencyID());
                    obj.put("fromcurrencyid", erd.getExchangeratelink().getFromCurrency().getCurrencyID());
                    obj.put("companyid",erd.getCompany().getCompanyID());
                    jArr.put(obj);
                }
            }
             jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    /*public static JSONObject getDefaultCurrencyExchange(Session session, HttpServletRequest request,Date transactiondate ) throws ServiceException, SessionExpiredException {
        JSONObject jobj=new JSONObject();
		try {
           KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
           ArrayList params = new ArrayList();
           String condition = "";
           if (transactiondate!=null){
                params.add(transactiondate);
                condition += " and applyDate <= ?  ";
            }
            params.add(currency.getCurrencyID());
            String query="select er,erd from DefaultExchangeRateDetails erd, DefaultExchangeRate er where erd.exchangeratelink.ID=er.ID and"+
                        " applyDate in (select max(applyDate) from DefaultExchangeRateDetails where exchangeratelink.ID=erd.exchangeratelink.ID "+condition+" group by exchangeratelink )"+
                        " and fromCurrency=? order by toCurrency desc";
            List list = HibernateUtil.executeQuery(session, query,params.toArray());
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
            Object[] row = (Object[]) itr.next();
                DefaultExchangeRate er = (DefaultExchangeRate) row[0];
                DefaultExchangeRateDetails erd = (DefaultExchangeRateDetails) row[1];
                JSONObject obj = new JSONObject();
                obj.put("id", er.getID());
                obj.put("applydate", AuthHandler.getDateFormatter(request).format(new Date()));
                obj.put("exchangerate", erd.getExchangeRate());
                obj.put("fromcurrency", er.getFromCurrency().getName());
                obj.put("symbol", er.getToCurrency().getSymbol());
                obj.put("htmlcode", er.getToCurrency().getHtmlcode());
                obj.put("tocurrency", er.getToCurrency().getName());
                obj.put("tocurrencyid", er.getToCurrency().getCurrencyID());
                obj.put("fromcurrencyid", er.getFromCurrency().getCurrencyID());
                jArr.put(obj);
            }
             jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }*/

    /*public static JSONObject getCurrencyExchangeList(Session session, HttpServletRequest request ) throws ServiceException, SessionExpiredException {
        JSONObject jobj=new JSONObject();
		try {
            String id =request.getParameter("currencyid");
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            String query="from ExchangeRateDetails where exchangeratelink.ID=? and company.companyID=? order by applyDate asc";
            List list = HibernateUtil.executeQuery(session, query,new Object[]{id,company.getCompanyID()});
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", id);
                obj.put("applydate", AuthHandler.getDateFormatter(request).format(erd.getApplyDate()));
                obj.put("exchangerate", erd.getExchangeRate());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }*/

    public static Double getBaseToCurrencyAmount(Session session, HttpServletRequest request,Double Amount,String newcurrencyid,Date transactiondate ) throws ServiceException, SessionExpiredException {
        try {
            if(Amount==0)
                return Amount;
        ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,newcurrencyid,transactiondate,null);
        Double rate= erd.getExchangeRate();
            Amount= Amount*rate;
        } catch ( ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }finally{
            return Amount;
        }
    }
     public static Double getOneCurrencyToOther(Session session, HttpServletRequest request,Double Amount,String oldcurrencyid,String newcurrencyid,Date transactiondate ) throws ServiceException, SessionExpiredException {
         Double currencyAmount=0.0;
         try {
            if(Amount==0)
                return Amount;
            Double baseAmount=CompanyHandler.getCurrencyToBaseAmount(session,request,Amount,oldcurrencyid,transactiondate);
            currencyAmount=CompanyHandler. getBaseToCurrencyAmount(session,request,baseAmount,newcurrencyid,transactiondate);

        } catch ( ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }finally{
            return currencyAmount;
        }
    }

    public static Double getCurrencyToBaseAmount(Session session, HttpServletRequest request,Double Amount,String currencyid ,Date transactiondate ) throws ServiceException, SessionExpiredException {
        try {
          if(Amount==0)
            return Amount;
            ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,currencyid,transactiondate,null);
            Double rate= erd.getExchangeRate();
            Amount= Amount/rate;
        } catch ( ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }finally{
            return Amount;
        }
    }
    public static ExchangeRateDetails getExcDetailID(Session session, HttpServletRequest request,String currencyid ,Date transactiondate,String erid ) throws ServiceException, SessionExpiredException {
        ExchangeRateDetails erd=null;
        try {
            String condition="";
            ArrayList params = new ArrayList();
            params.add(AuthHandler.getCurrencyID(request));
            params.add(currencyid);
            if(erid==null){
                String erIDQuery = "select ID from ExchangeRate where fromCurrency.currencyID=? and toCurrency.currencyID=? ";
                List erIDList = HibernateUtil.executeQuery(session, erIDQuery,params.toArray());
                Iterator erIDitr = erIDList.iterator();
                erid = (String) erIDitr.next();
            }
            params = new ArrayList();
            params.add(AuthHandler.getCompanyid(request));
            params.add(erid);
            if (transactiondate!=null){
                params.add(transactiondate);
                condition += " and applyDate <= ?  ";
            }
            String applyDateQuery = "select max(erd.applyDate) from ExchangeRateDetails erd where erd.company.companyID=? and  erd.exchangeratelink.ID = ? "+condition  ;
            List applyDateList = HibernateUtil.executeQuery(session, applyDateQuery,params.toArray());
            Iterator itr = applyDateList.iterator();

            Date maxDate = (Date) itr.next();
            params = new ArrayList();
            params.add(maxDate);
            params.add(erid);
            params.add(AuthHandler.getCompanyid(request));

            String erdIDQuery = "from ExchangeRateDetails erd where erd.applyDate=? and erd.exchangeratelink.ID=? and erd.company.companyID=?";
            List erdIDList = HibernateUtil.executeQuery(session, erdIDQuery,params.toArray());
            Iterator erdIDItr = erdIDList.iterator();
            erd = (ExchangeRateDetails) erdIDItr.next();

        } catch ( ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }finally{
            return erd;
        }
    }

    /*public static void saveYearLock(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException{
		try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            YearLock yearlock;
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("id")))
                    yearlock = new YearLock();
                else
                    yearlock = (YearLock) session.get(YearLock.class, jobj.getString("id"));
                yearlock.setYearid(Integer.parseInt(jobj.getString("name")));
                yearlock.setIsLock("true".equalsIgnoreCase(jobj.getString("islock")));
                yearlock.setDeleted(false);
                yearlock.setCompany(company);
                session.saveOrUpdate(yearlock);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }*/

    /*public static JSONObject getYearLock(Session session, HttpServletRequest request ) throws ServiceException, SessionExpiredException {
        JSONObject jobj=new JSONObject();
		try {
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,company.getCompanyID());
            String query="from YearLock where deleted=false and company.companyID=?";
            List list = HibernateUtil.executeQuery(session, query,company.getCompanyID());
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            if(list.isEmpty()){
                for(int i=1;i<=5;i++){
                    Calendar date = Calendar.getInstance();
                    int year=date.get(Calendar.YEAR);
                    JSONObject obj = new JSONObject();
                    obj.put("id", "");
                    obj.put("name", year+i);
                    obj.put("islock", false);
                    obj.put("startdate","");
                    obj.put("enddate", "");
                    jArr.put(obj);
                }

                for(int i=1;i<=5;i++){
                    Calendar date = Calendar.getInstance();
                    int year=date.get(Calendar.YEAR);
                    JSONObject obj = new JSONObject();
                    obj.put("id", "");
                    obj.put("name", year-i);
                    obj.put("islock", false);
                    obj.put("startdate","");
                    obj.put("enddate", "");
                    jArr.put(obj);
                }
            }
            else
            while(itr.hasNext()) {
                YearLock yl = (YearLock) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", yl.getID());
                obj.put("name", yl.getYearid());
                obj.put("islock", yl.isIsLock());
                if(pref!=null){
                    Calendar startFinYearCal = Calendar.getInstance();
                    Calendar endFinYearCal = Calendar.getInstance();
                    startFinYearCal.setTime(pref.getFinancialYearFrom());
                    endFinYearCal.setTime(pref.getFinancialYearFrom());
                    endFinYearCal.set(Calendar.YEAR,yl.getYearid()+1);
                    endFinYearCal.add(Calendar.DATE, -1);
                    startFinYearCal.set(Calendar.YEAR,yl.getYearid());
                    obj.put("startdate", AuthHandler.getDateFormatter(request).format(startFinYearCal.getTime()));
                    obj.put("enddate", AuthHandler.getDateFormatter(request).format(endFinYearCal.getTime()));
                }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }*/

    /*public static void saveCompanyAccountPreferences(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException{
		try {
            CompanyAccountPreferences preferences;
            preferences=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            if(preferences==null)
                preferences=new CompanyAccountPreferences();

            preferences.setFinancialYearFrom(AuthHandler.getDateFormatter(request).parse(request.getParameter("fyfrom")));
            preferences.setBookBeginningFrom(AuthHandler.getDateFormatter(request).parse(request.getParameter("bbfrom")));
            preferences.setDiscountGiven((Account)session.get(Account.class,request.getParameter("discountgiven")));
            preferences.setDiscountReceived((Account)session.get(Account.class,request.getParameter("discountreceived")));
            preferences.setShippingCharges((Account)session.get(Account.class,request.getParameter("shippingcharges")));
            preferences.setOtherCharges((Account)session.get(Account.class,request.getParameter("othercharges")));
            preferences.setCashAccount((Account)session.get(Account.class,request.getParameter("cashaccount")));
             preferences.setForeignexchange((Account)session.get(Account.class,request.getParameter("foreignexchange")));
            preferences.setDepereciationAccount((Account)session.get(Account.class,request.getParameter("depreciationaccount")));
            String str=request.getParameter("autojournalentry");
            if(!StringUtil.isNullOrEmpty(str))preferences.setJournalEntryNumberFormat(str);
            else preferences.setJournalEntryNumberFormat(null);
            str=request.getParameter("autoinvoice");
            if(!StringUtil.isNullOrEmpty(str))preferences.setInvoiceNumberFormat(str);
            else preferences.setInvoiceNumberFormat(null);
            str=request.getParameter("autocreditmemo");
            if(!StringUtil.isNullOrEmpty(str))preferences.setCreditNoteNumberFormat(str);
            else preferences.setCreditNoteNumberFormat(null);
            str=request.getParameter("autoreceipt");
            if(!StringUtil.isNullOrEmpty(str))preferences.setReceiptNumberFormat(str);
            else preferences.setReceiptNumberFormat(null);
            str=request.getParameter("autogoodsreceipt");
            if(!StringUtil.isNullOrEmpty(str))preferences.setGoodsReceiptNumberFormat(str);
            else preferences.setGoodsReceiptNumberFormat(null);
            str=request.getParameter("autodebitnote");
            if(!StringUtil.isNullOrEmpty(str))preferences.setDebitNoteNumberFormat(str);
            else preferences.setDebitNoteNumberFormat(null);
            str=request.getParameter("autopayment");
            if(!StringUtil.isNullOrEmpty(str))preferences.setPaymentNumberFormat(str);
            else preferences.setPaymentNumberFormat(null);
            str=request.getParameter("autoso");
            if(!StringUtil.isNullOrEmpty(str))preferences.setSalesOrderNumberFormat(str);
            else preferences.setSalesOrderNumberFormat(null);
            str=request.getParameter("autopo");
            if(!StringUtil.isNullOrEmpty(str))preferences.setPurchaseOrderNumberFormat(str);
            else preferences.setPurchaseOrderNumberFormat(null);
            str=request.getParameter("autocashsales");
            if(!StringUtil.isNullOrEmpty(str))preferences.setCashSaleNumberFormat(str);
            else preferences.setCashSaleNumberFormat(null);
            str=request.getParameter("autocashpurchase");
            if(!StringUtil.isNullOrEmpty(str))preferences.setCashPurchaseNumberFormat(str);
            else preferences.setCashPurchaseNumberFormat(null);
            str=request.getParameter("autobillinginvoice");
            if(!StringUtil.isNullOrEmpty(str))preferences.setBillingInvoiceNumberFormat(str);
            else preferences.setBillingInvoiceNumberFormat(null);
            str=request.getParameter("autobillingreceipt");
            if(!StringUtil.isNullOrEmpty(str))preferences.setBillingReceiptNumberFormat(str);
            else preferences.setBillingReceiptNumberFormat(null);
            str=request.getParameter("autobillingcashsales");
            if(!StringUtil.isNullOrEmpty(str))preferences.setBillingCashSaleNumberFormat(str);
            else preferences.setBillingCashSaleNumberFormat(null);
            str=request.getParameter("autobillingcashpurchase");
            if(!StringUtil.isNullOrEmpty(str))preferences.setBillingCashPurchaseNumberFormat(str);
            else preferences.setBillingCashPurchaseNumberFormat(null);
            preferences.setWithoutInventory(!StringUtil.isNullOrEmpty(request.getParameter("withoutinventory")));
            preferences.setEmailInvoice(!StringUtil.isNullOrEmpty(request.getParameter("emailinvoice")));
            preferences.setCompany((Company)session.get(Company.class,AuthHandler.getCompanyid(request)));
            session.saveOrUpdate(preferences);
            saveYearLock(session,request);
            ProfileHandler.insertAuditLog(session, AuditAction.COMPANY_ACCOUNT_PREFERENCES_UPDATE, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) + " from "+preferences.getCompany().getCompanyName()+" changed company's account preferences", request);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }*/

    /*public static JSONObject getCompanyAccountPreferences(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
		try {
            JSONObject obj = new JSONObject();
            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            if(pref==null){
                return jobj;
            }
            if(pref!=null){

                obj.put("prefid", pref.getID());
                obj.put("fyfrom", AuthHandler.getDateFormatter(request).format(pref.getFinancialYearFrom()));
                obj.put("bbfrom", AuthHandler.getDateFormatter(request).format(pref.getBookBeginningFrom()));
                obj.put("discountgiven", pref.getDiscountGiven().getID());
                obj.put("discountreceived", pref.getDiscountReceived().getID());
                if(pref.getForeignexchange()!=null)
                    obj.put("foreignexchange", pref.getForeignexchange().getID());
                obj.put("shippingcharges", pref.getShippingCharges().getID());
                obj.put("othercharges", pref.getOtherCharges().getID());
                obj.put("cashaccount", pref.getCashAccount().getID());
                if(pref.getDepereciationAccount()!=null)
                    obj.put("depreciationaccount", pref.getDepereciationAccount().getID());
                obj.put("autoinvoice", pref.getInvoiceNumberFormat());
                obj.put("autocreditmemo", pref.getCreditNoteNumberFormat());
                obj.put("autoreceipt", pref.getReceiptNumberFormat());
                obj.put("autojournalentry", pref.getJournalEntryNumberFormat());
                obj.put("autogoodsreceipt", pref.getGoodsReceiptNumberFormat());
                obj.put("autodebitnote", pref.getDebitNoteNumberFormat());
                obj.put("autopayment", pref.getPaymentNumberFormat());
                obj.put("autoso", pref.getSalesOrderNumberFormat());
                obj.put("autopo", pref.getPurchaseOrderNumberFormat());
                obj.put("autocashsales", pref.getCashSaleNumberFormat());
                obj.put("autobillinginvoice", pref.getBillingInvoiceNumberFormat());
                obj.put("autobillingreceipt", pref.getBillingReceiptNumberFormat());
                obj.put("autobillingcashsales", pref.getBillingCashSaleNumberFormat());
                obj.put("autobillingcashpurchase", pref.getBillingCashPurchaseNumberFormat());
                obj.put("autocashpurchase", pref.getCashPurchaseNumberFormat());
                obj.put("emailinvoice", pref.isEmailInvoice());
                obj.put("withoutinventory", pref.isWithoutInventory());
            }
            jobj.put("data", obj);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getCompanyAccountPreferences", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("CompanyHandler.getCompanyAccountPreferences", e);
        }
        return jobj;
    }*/
    /*public static JSONObject getAccounts(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
        try {
            ArrayList params=new ArrayList();
            Company company=(Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String[] groups=request.getParameterValues("group");
            boolean ignoreCustomers=request.getParameter("ignorecustomers")!=null;
            boolean ignoreVendors=request.getParameter("ignorevendors")!=null;
            String condition=(request.getParameter("ignore")==null?"":" not ");
            if(groups!=null){
                String qMarks="?";
                params.add("null");
                 for(int i=0;i<groups.length;i++){
                    qMarks+=",?";
                    params.add(groups[i]);
                }
                condition=" and ac.group.ID "+condition+" in ("+qMarks+") ";
            }
            String excludeaccountid=request.getParameter("accountid");
            String includeaccountid=request.getParameter("includeaccountid");
            params.add(AuthHandler.getCompanyid(request));
            String query="from Account ac where ac.parent is null and ac.deleted=false "+condition+" and ac.company.companyID=? order by ac.name";
            List list = HibernateUtil.executeQuery(session, query,params.toArray());
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();

            while(itr.hasNext()) {
                Account account = (Account) itr.next();
                if(account.getID().equals(excludeaccountid)) continue;
                if(includeaccountid!=null&&!account.getID().equals(includeaccountid)) continue;
                if(ignoreCustomers&&account.getGroup().getID().equals(Group.ACCOUNTS_RECEIVABLE)){
                    Customer c=(Customer)session.get(Customer.class,account.getID());
                    if(c!=null)continue;
                }
                if(ignoreVendors&&account.getGroup().getID().equals(Group.ACCOUNTS_PAYABLE)){
                    Vendor v=(Vendor)session.get(Vendor.class,account.getID());
                    if(v!=null)continue;
                }
                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("openbalance", account.getOpeningBalance());
                obj.put("currencyid",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getSymbol()));
                obj.put("currencyname",(account.getCurrency()==null?currency.getName(): account.getCurrency().getName()));
                obj.put("presentbalance", account.getPresentValue());
                obj.put("life", account.getLife());
                obj.put("salvage", account.getSalvage());
                obj.put("creationDate", AuthHandler.getDateFormatter(request).format(account.getCreationDate()));
                obj.put("level", 0);
                jArr.put(obj);
                obj.put("leaf",getChildAccounts(session, account, jArr, 0, excludeaccountid,includeaccountid,ignoreCustomers,ignoreVendors,company,currency));
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getAccounts", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("CompanyHandler.getAccounts", e);
        }
        return jobj;
    }

    private static boolean getChildAccounts(Session session, Account account, JSONArray jArr, int level, String excludeaccountid, String includeaccountid,boolean ignoreCustomers, boolean ignoreVendors, Company company,KWLCurrency currency) throws JSONException {
        boolean leaf=true;
        Iterator<Account> itr = new TreeSet(account.getChildren()).iterator();
        level++;
        while(itr.hasNext()) {
            Account child = itr.next();
            if(child.getID().equals(excludeaccountid)||child.isDeleted()) continue;
            if(!child.getID().equals(includeaccountid)||child.isDeleted()) continue;
            if(ignoreCustomers&&child.getGroup().getID().equals(Group.ACCOUNTS_RECEIVABLE)){
                Customer c=(Customer)session.get(Customer.class,child.getID());
                if(c!=null)continue;
            }
            if(ignoreVendors&&child.getGroup().getID().equals(Group.ACCOUNTS_PAYABLE)){
                Vendor v=(Vendor)session.get(Vendor.class,child.getID());
                if(v!=null)continue;
            }
            leaf=false;
            JSONObject obj = new JSONObject();
            obj.put("accid", child.getID());
            obj.put("accname", child.getName());
            obj.put("groupid", child.getGroup().getID());
            obj.put("groupname", child.getGroup().getName());
            obj.put("openbalance", child.getOpeningBalance());
            obj.put("parentid", account.getID());
            obj.put("parentname", account.getName());
            obj.put("currencyid",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getCurrencyID()));
            obj.put("currencysymbol",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getSymbol()));
            obj.put("currencyname",(account.getCurrency()==null?currency.getName(): account.getCurrency().getName()));

            obj.put("presentbalance", account.getPresentValue());
            obj.put("life", account.getLife());
            obj.put("salvage", account.getSalvage());
            obj.put("creationDate", account.getCreationDate());
            obj.put("level", level);
            jArr.put(obj);
            obj.put("leaf", getChildAccounts(session, child, jArr, level, excludeaccountid,excludeaccountid, ignoreCustomers, ignoreVendors,company,currency));
        }

        return leaf;
    }*/
    /*public static Double calMonthwiseDepreciation(Double openingbalance, Double salvage,Double month) throws ServiceException, HibernateException, JSONException {
        double amount;
        try {
            amount=(openingbalance-salvage)/month;

        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("CompanyHandler.calMonthwiseDepreciation", ne);
        }
        return amount;
    }
    public static JSONObject getAccountDepreciation(Session session, HttpServletRequest request) throws ServiceException, HibernateException, JSONException, ParseException {
        JSONObject jobj = CompanyHandler.getAccounts(session,request);
        try {
            String accountid=request.getParameter("includeaccountid");
            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            Calendar startcal = Calendar.getInstance();
            Calendar endcal= Calendar.getInstance();
            Calendar cal= Calendar.getInstance();

            DateFormat sdf= new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
            JSONArray jArr = jobj.getJSONArray("data");
            JSONObject obj = jArr.getJSONObject(0);
            Date creationDate = AuthHandler.getDateFormatter(request).parse(obj.getString("creationDate"));
            double openingbalance=obj.getDouble("openbalance");
            double balance=obj.getDouble("presentbalance");
            double life=obj.getDouble("life");
            double salvage=obj.getDouble("salvage");
            if(balance==0)
                return jobj;
            double periodDepreciation=CompanyHandler.calMonthwiseDepreciation(openingbalance,salvage,life*12) ;
            double accDepreciation=0;
            JSONArray finalJArr=new JSONArray();
            cal.setTime(creationDate);
            for(int j=0;j<life;j++){
                startcal.setTime(creationDate);
                endcal.setTime(creationDate);
                startcal.add(Calendar.YEAR, j);
                endcal.add(Calendar.YEAR, j);
            for(int i=0;i<12;i++){
                if(balance<=0)
                    break;
                 if(i<cal.getTime().getMonth()&&startcal.getTime().getYear()==cal.getTime().getYear())
                    continue;

                int period=(12*j)+i+1 ;
                accDepreciation+=periodDepreciation;
                balance-=periodDepreciation;
                JSONObject finalObj=new JSONObject();
                finalObj.put("period",period);
                startcal.set(Calendar.MONTH, i);
                endcal.set(Calendar.MONTH, i+1);
                startcal.set(Calendar.HOUR,0);
                startcal.set(Calendar.MINUTE,0);
                startcal.set(Calendar.SECOND,0);
                endcal.set(Calendar.HOUR,0);
                endcal.set(Calendar.MINUTE,0);
                endcal.set(Calendar.SECOND,0);
                finalObj.put("perioddepreciation",periodDepreciation);
                finalObj.put("accdepreciation",accDepreciation);
                finalObj.put("netbookvalue", balance);                
                finalObj.put("frommonth", sdf.format(startcal.getTime()));
                finalObj.put("tomonth", sdf.format(endcal.getTime()));
                ArrayList params = new ArrayList();
                params.add(period);
                params.add(accountid);
                params.add(AuthHandler.getCompanyid(request));
                String query="from DepreciationDetail where period=? and account.ID=? and company.companyID=?";
                Iterator itrcust=HibernateUtil.executeQuery(session, query,params.toArray()).iterator();
                finalObj.put("isje",!itrcust.hasNext()?false:true );
                finalObj.put("depdetailid", !itrcust.hasNext()?"":itrcust.next());
                finalJArr.put(finalObj);
            }
            }
            jobj.put("data", finalJArr);
       
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getAccountDepreciation", ex);
        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("CompanyHandler.getAccountDepreciation", ne);
        } catch (JSONException jse) {
            throw ServiceException.FAILURE("CompanyHandler.getAccountDepreciation", jse);
        }
        return jobj;
    }*/

     /*public static void saveAccountDepreciation(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException, ParseException, AccountingException{
        try {
            Calendar Cal = Calendar.getInstance();
            Cal.setTime(new Date());
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
            CompanyAccountPreferences preferences=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            if(preferences.getDepereciationAccount()==null)
                 throw new AccountingException("Please Set Depreciation account in Account Preference first");
            if(jArr.length()>0)
            for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            DepreciationDetail dd = new DepreciationDetail();
            Account account=(Account) session.get(Account.class, request.getParameter("accountid"));
            double perioddepreciation=Double.parseDouble(jobj.getString("perioddepreciation"));
            dd.setAccount(account);
            dd.setPeriod(Integer.parseInt(jobj.getString("period")));
            dd.setCompany(company);
            HashSet hs=new HashSet();
            JournalEntryDetail jed=new JournalEntryDetail();
            
            if(perioddepreciation>0){
                jed=new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(perioddepreciation);
                jed.setAccount(account);
                jed.setDebit(false);
                hs.add(jed);

                jed=new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(perioddepreciation);
                jed.setAccount(preferences.getDepereciationAccount());
                jed.setDebit(true);
                hs.add(jed);
            }
            String entryNumber=getNextAutoNumber(session, preferences, StaticValues.AUTONUM_JOURNALENTRY);
            JournalEntry journalEntry=CompanyHandler.makeJournalEntry(session, company.getCompanyID(), Cal.getTime(),
            request.getParameter("memo"), entryNumber,currency.getCurrencyID(), hs,request);
            
            dd.setJournalEntry(journalEntry);
           session.saveOrUpdate(dd);
        }
         } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }*/

    /*public static JSONObject getGroups(Session session, String groupid, boolean ignoreCustomer, boolean ignoreVendor, String companyid) throws ServiceException {
        JSONObject jobj=new JSONObject();
		try {
            String condition="";
            ArrayList params = new ArrayList();
            if(ignoreCustomer)
                condition+=" and ID != '"+Group.ACCOUNTS_RECEIVABLE+"'";
            if(ignoreVendor)
                condition+=" and ID != '"+Group.ACCOUNTS_PAYABLE+"'";
            String query="from Group where deleted=false and parent is null and (company is null or company.companyID=?) "+condition+" order by name";
            List list = HibernateUtil.executeQuery(session, query,companyid);
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Group group = (Group) itr.next();
                if(group.getID().equals(groupid)) continue;
                JSONObject obj = new JSONObject();
                obj.put("groupid", group.getID());
                obj.put("groupname", group.getName());
                obj.put("nature", group.getNature());
                obj.put("affectgp", group.isAffectGrossProfit());
                obj.put("displayorder", group.getDisplayOrder());
                obj.put("companyid", (group.getCompany()==null?null:companyid));
                obj.put("level", 0);
                jArr.put(obj);
                obj.put("leaf",getChildGroups(session, group, jArr, 0, groupid, companyid));
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }*/

    /*private static boolean getChildGroups(Session session, Group group, JSONArray jArr, int level,String groupid, String companyid) throws JSONException {
        boolean leaf=true;
        Iterator<Group> itr = new TreeSet(group.getChildren()).iterator();
        level++;
        while(itr.hasNext()) {
            Group child = itr.next();
            Company company=child.getCompany();
            if((company!=null&&!company.getCompanyID().equals(companyid))||child.getID().equals(groupid)||child.isDeleted()) continue;

            leaf=false;
            JSONObject obj = new JSONObject();
            obj.put("groupid", child.getID());
            obj.put("groupname", child.getName());
            obj.put("nature", child.getNature());
            obj.put("affectgp", child.isAffectGrossProfit());
            obj.put("displayorder", child.getDisplayOrder());
            obj.put("companyid", (company==null?null:companyid));
            obj.put("parentid", group.getID());
            obj.put("parentname", group.getName());
            obj.put("level", level);
            jArr.put(obj);
            obj.put("leaf", getChildGroups(session, child, jArr, level, groupid, companyid));
        }

        return leaf;
    }*/

    /*public static String saveGroup(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException{
        String parentid = request.getParameter("parentid");
        boolean issub = request.getParameter("subtype")!=null;
        Group parent = null;
        Group group = null;
        String groupid=request.getParameter("groupid");
        String auditMsg = "added";
        String auditID = AuditAction.ACCOUNT_CREATED;
        try {
            String query="select max(displayOrder) from Group";
            List l=HibernateUtil.executeQuery(session, query);
            int dispOrder=0;
            if(!l.isEmpty()&&l.get(0)!=null)dispOrder=(Integer)l.get(0);
            dispOrder++;
            if(!StringUtil.isNullOrEmpty(groupid)){
                auditMsg="updated";
                auditID=AuditAction.ACCOUNT_UPDATED;
                dispOrder=((Group)session.get(Group.class, groupid)).getDisplayOrder();
            }
            if (issub&&!StringUtil.isNullOrEmpty(parentid)) {
                parent = (Group) session.get(Group.class, parentid);
            }
            group = makeGroup(session, AuthHandler.getCompanyid(request),
                    groupid,
                    request.getParameter("groupname"),
                    Integer.parseInt(request.getParameter("nature")),
                    request.getParameter("affectgp")!=null,
                    dispOrder,
                    parent);
            ProfileHandler.insertAuditLog(session, auditID, "User " + AuthHandler.getFullName(session, AuthHandler.getUserid(request)) + " " + auditMsg + " group " + group.getName(), request);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return group.getID();
    }

    public static Group makeGroup(Session session, String companyid, String groupid, String name, int nature, boolean affectGP,int displayOrder, Group parent) throws ServiceException, HibernateException, AccountingException{
        Group group;
        if(StringUtil.isNullOrEmpty(groupid)){
            group=new Group();
            group.setDeleted(false);
        }else{
            group=(Group)session.get(Group.class, groupid);
        }
        group.setName(name);
        group.setNature(nature);
        group.setAffectGrossProfit(affectGP);
        group.setCompany((Company)session.get(Company.class,companyid));
        group.setParent(parent);
        group.setDisplayOrder(displayOrder);
        session.saveOrUpdate(group);
        updateChildren(session, group);
        return group;
    }*/

      private static void updateChildren(Session session, Group group) throws AccountingException{
        Set<Group> children=group.getChildren();
        if(children==null) return;
        Iterator<Group> itr=children.iterator();
        while(itr.hasNext()){
            Group child=itr.next();
            child.setNature(group.getNature());
            child.setAffectGrossProfit(group.isAffectGrossProfit());
            session.update(child);
            updateChildren(session, child);
        }
    }

    /*public static void deleteGroup(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
        Group group=(Group)session.get(Group.class, request.getParameter("groupid"));
        group.setDeleted(true);
        session.update(group);
    }*/

    /*public static JSONObject getPaymentMethods(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String query="from PaymentMethod where company.companyID=?";
            List list = HibernateUtil.executeQuery(session, query, AuthHandler.getCompanyid(request));
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                PaymentMethod paymethod = (PaymentMethod) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("methodid", paymethod.getID());
                obj.put("methodname", paymethod.getMethodName());
                obj.put("accountid", paymethod.getAccount().getID());
                obj.put("accountname", paymethod.getAccount().getName());
                obj.put("detailtype", paymethod.getDetailType());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getPaymentMethods", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("CompanyHandler.getPaymentMethods", e);
        }
        return jobj;
    }*/

    /*public static void savePaymentMethod(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException {
        try {
            String delQuery="";
            int delCount=0;
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            ArrayList params = new ArrayList();
            params.add("null");
            String qMarks = "?";
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("methodid")) == false) {
                    params.add(jobj.getString("methodid"));
                    qMarks += ",?";
                }
            }
            params.add(company.getCompanyID());
            try{
                delQuery = "delete from PaymentMethod p where p.ID in(" + qMarks + ") and p.company.companyID=?";
                delCount = HibernateUtil.executeUpdate(session, delQuery, params.toArray());
            }
            catch(ServiceException ex){
                    throw new AccountingException("Selected record(s) as are currently used in Transaction(s) ");
            }
            PaymentMethod pom;
            String auditMsg;
            String auditID;
            String fullName = AuthHandler.getFullName(session, AuthHandler.getUserid(request));
            if (delCount > 0) {
                ProfileHandler.insertAuditLog(session, AuditAction.PAYMENT_METHOD_DELETED, "User " + fullName + " deleted " + delCount + " Payment Method", request);
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }
                if (StringUtil.isNullOrEmpty(jobj.getString("methodid"))) {
                    auditMsg = "added";
                    pom = new PaymentMethod();
                    auditID = AuditAction.PAYMENT_METHOD_ADDED;
                } else {
                    pom = (PaymentMethod) session.get(PaymentMethod.class, jobj.getString("methodid"));
                    auditMsg = "updated";
                    auditID = AuditAction.PAYMENT_METHOD_CHANGED;
                }

                pom.setMethodName(jobj.getString("methodname"));
                pom.setAccount((Account) session.get(Account.class, jobj.getString("accountid")));
                pom.setDetailType(jobj.getInt("detailtype"));
                pom.setCompany(company);
                session.saveOrUpdate(pom);
                ProfileHandler.insertAuditLog(session, auditID, "User " + fullName + " " + auditMsg + " Payment Method to " + pom.getMethodName(), request);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }*/

    /*public static String saveAccount(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException, ParseException{
        Group group = (Group) session.get(Group.class, request.getParameter("groupid"));
        String parentid = request.getParameter("parentid");
        KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
        String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
        boolean issub = CompanyHandler.getBoolean(request, "subaccount");//baltype
        boolean debitType = CompanyHandler.getBoolean(request, "debitType");
        Date creationDate = AuthHandler.getDateFormatter(request).parse(request.getParameter("creationDate"));
        double openBalance=CompanyHandler.getDouble(request, "openbalance");
        double life=CompanyHandler.getDouble(request, "life");
        double salvage=CompanyHandler.getDouble(request, "salvage");
        if(creationDate==null)
            creationDate=new Date();
        Account parent = null;
        Account account = null;
        String accid=request.getParameter("accid");
        String auditMsg = "added";
        String auditID = AuditAction.ACCOUNT_CREATED;
        if(StringUtil.isNullOrEmpty(accid)){
            auditMsg="updated";
            auditID=AuditAction.ACCOUNT_UPDATED;
        }
        if (issub&&!StringUtil.isNullOrEmpty(parentid)) {
            parent = (Account) session.get(Account.class, parentid);
        }
        try {
            openBalance=debitType?openBalance:-openBalance;
            account = makeAccount(session, AuthHandler.getCompanyid(request),
                    request.getParameter("accid"),
                    request.getParameter("accname"),
                    openBalance,
                    parent, group,currencyid,
                    life,salvage,creationDate);
            ProfileHandler.insertAuditLog(session, auditID, "User " + AuthHandler.getFullName(session, AuthHandler.getUserid(request)) + " " + auditMsg + " account " + account.getName(), request);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return account.getID();
    }*/

    /*public static void deleteAccount(Session session, HttpServletRequest request) throws ServiceException, HibernateException, JSONException, AccountingException, SessionExpiredException{
        String selQuery="";
        ArrayList params1 = new ArrayList();
        ArrayList params2 = new ArrayList();
        ArrayList params3 = new ArrayList();
        try{
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String qMarks = "";
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            for (int j = 0; j<5; j++) {
                qMarks = "";
                for (int i = 0; i < jArr.length(); i++){
                    JSONObject jobj = jArr.getJSONObject(i);
                    if (!StringUtil.isNullOrEmpty(jobj.getString("accid"))) {
                        if(j==0)
                            params1.add(jobj.getString("accid"));
                        if(j<=1)
                            params3.add(jobj.getString("accid"));
                        params2.add(jobj.getString("accid"));
                        qMarks += "?,";
                    }
                }
                qMarks=qMarks.substring(0,Math.max(0, qMarks.length()-1));

            }
            params1.add(company.getCompanyID());
            params2.add(company.getCompanyID());
            params3.add(company.getCompanyID());
            selQuery = "from JournalEntryDetail jed where account.ID in( "+qMarks +") and jed.journalEntry.deleted=false and jed.company.companyID=?";
            List list = HibernateUtil.executeQuery(session, selQuery, params1.toArray());
            int count=list.size();
            if(count>0)
                 throw new AccountingException("Selected record(s) are currently used in transaction(s).So it cannot be deleted.");
            selQuery = "from Product pr where (purchaseAccount.ID in ( "+qMarks +") or salesAccount.ID in ( "+qMarks +") ) and pr.company.companyID=?";
            list = HibernateUtil.executeQuery(session, selQuery, params3.toArray());
            count=list.size();
            if(count>0)
                throw new AccountingException("Selected record(s) are currently used in Account Preferences.So it cannot be deleted.");
            selQuery = "from CompanyAccountPreferences acp where (discountGiven.ID in ( "+qMarks +") or discountReceived.ID in ( "+qMarks +") or shippingCharges.ID in ( "+qMarks +")  or otherCharges.ID in ( "+qMarks +") or cashAccount.ID in ( "+qMarks +")) and acp.company.companyID=?";// (discountGiven.ID in ( "+qMarks +")
            list = HibernateUtil.executeQuery(session, selQuery, params2.toArray());
            count=list.size();
            if(count>0)
                 throw new AccountingException("Selected record(s) are currently used in Product(s).So it cannot be deleted.");
            selQuery = "from PaymentMethod pm where account.ID in ( "+qMarks +")  and pm.company.companyID=?";
            list = HibernateUtil.executeQuery(session, selQuery, params1.toArray());
            count=list.size();
            if(count>0)
                 throw new AccountingException("Selected record(s) are currently used in Term(s).So it cannot be deleted.");
            selQuery = "from Tax t where account.ID in ( "+qMarks +")  and t.company.companyID=?";
            list = HibernateUtil.executeQuery(session, selQuery, params1.toArray());
            count=list.size();
            if(count>0)
                 throw new AccountingException("Selected record(s) are currently used in Tax(s).So it cannot be deleted.");

                 for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    if (StringUtil.isNullOrEmpty(jobj.getString("accid")) == false) {
                        if(jobj.getDouble("openbalance")!=0)
                            throw new AccountingException("Selected record(s) is having the Opening Balance. So it cannot be deleted");
                        else{
                            Account  account=(Account)session.get(Account.class, jobj.getString("accid"));
                            account.setDeleted(true);
                            session.update(account);
                        }
                    }
                }

        }catch(ServiceException ex){
            throw ServiceException.FAILURE("Selected record(s) are currently used in transaction(s)", ex);
        }
    }
*/
    /*public static Account makeAccount(Session session, String companyid, String accountid, String name, double balance, Account parent, Group group,String currencyid,double life,double salvage,Date creationDate) throws ServiceException, HibernateException{
        Account account;
            if(StringUtil.isNullOrEmpty(accountid)){
                account=new Account();
                account.setDeleted(false);
                account.setCreationDate(creationDate);
                account.setLife(life);
                account.setPresentValue(balance);
                account.setSalvage(salvage);
                account.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
            }else{
                account=(Account)session.get(Account.class, accountid);
            }
            account.setName(name);
            account.setOpeningBalance(balance);
            account.setCompany((Company)session.get(Company.class,companyid));
            account.setParent(parent);         
            account.setGroup(group);          
            session.saveOrUpdate(account);
            updateChildren(session, account);
        return account;
    }*/

    private static void updateChildren(Session session, Account account){
        Set<Account> children=account.getChildren();
        if(children==null) return;
        Iterator<Account> itr=children.iterator();
        while(itr.hasNext()){
            Account child=itr.next();
            child.setGroup(account.getGroup());
            updateChildren(session, child);
        }
    }

    /*public static JSONObject getJournalEntry(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String start=request.getParameter("start");
            String limit=request.getParameter("limit");
            String ss=request.getParameter("ss");
            String linkentry=request.getParameter("linkid");
            ArrayList params=new ArrayList();
            params.add(AuthHandler.getCompanyid(request));
            String condition=" where deleted=false and company.companyID=? ";

            if(StringUtil.isNullOrEmpty(linkentry)==false){
                 params.add(linkentry);
                 condition+= " and  id=? order by entryDate";
            }else{
                   if(!StringUtil.isNullOrEmpty(ss)){
                     params.add(ss+"%");
                     params.add(ss+"%");
                     condition+= " and (entryno like ? or memo like ?) order by entryDate";
                   }else{
                         condition+= " order by entryDate";
                   }
            }
            String query="from JournalEntry"+condition;

            List list = HibernateUtil.executeQuery(session, query,params.toArray());//params.toArray() AuthHandler.getCompanyid(request));  new Object[]{ AuthHandler.getCompanyid(request)}
            int count=list.size();
            if(StringUtil.isNullOrEmpty(start)==false&&StringUtil.isNullOrEmpty(limit)==false){
                list=HibernateUtil.executeQueryPaging(session, query,params.toArray(), new Integer[]{Integer.parseInt(start),Integer.parseInt(limit)});
            }
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                JournalEntry entry = (JournalEntry) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("journalentryid", entry.getID());
                obj.put("entryno", entry.getEntryNumber());
                obj.put("currencysymbol",entry.getCurrency()==null?currency.getSymbol(): entry.getCurrency().getSymbol());
                obj.put("memo", entry.getMemo());
                obj.put("entrydate", AuthHandler.getDateFormatter(request).format(entry.getEntryDate()));
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getJournalEntry", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("CompanyHandler.getJournalEntry", e);
        }
        return jobj;
    }*/
     /*public static JSONObject getJournalEntryDetails(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException{
        JSONObject jobj=new JSONObject();
		try {
            Set details = ((JournalEntry)session.get(JournalEntry.class, request.getParameter("journalentryid"))).getDetails();
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            Iterator itr = details.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                JournalEntryDetail entry = (JournalEntryDetail) itr.next();
                String currencyid=entry.getJournalEntry().getCurrency()==null?currency.getCurrencyID(): entry.getJournalEntry().getCurrency().getCurrencyID();
                if(entry.isDebit()==false)continue;
                JSONObject obj = new JSONObject();
                obj.put("accountid", entry.getAccount().getID());
                obj.put("accountname", entry.getAccount().getName());
                obj.put("currencysymbol",entry.getJournalEntry().getCurrency()==null?currency.getSymbol(): entry.getJournalEntry().getCurrency().getSymbol());
                obj.put("debit", "Debit");
                 obj.put("description",entry.getDescription());
                obj.put("d_amount", CompanyHandler.getCurrencyToBaseAmount(session,request,entry.getAmount(),currencyid,entry.getJournalEntry().getEntryDate()));
                jArr.put(obj);
            }
            itr = details.iterator();
            while(itr.hasNext()) {
                JournalEntryDetail entry = (JournalEntryDetail) itr.next();
                 String currencyid=entry.getJournalEntry().getCurrency()==null?currency.getCurrencyID(): entry.getJournalEntry().getCurrency().getCurrencyID();
                if(entry.isDebit()==true)continue;
                JSONObject obj = new JSONObject();
                obj.put("accountid", entry.getAccount().getID());
                obj.put("accountname", entry.getAccount().getName());
                obj.put("currencysymbol",entry.getJournalEntry().getCurrency()==null?currency.getSymbol(): entry.getJournalEntry().getCurrency().getSymbol());
                obj.put("debit", "Credit");
                 obj.put("description",entry.getDescription());
                obj.put("c_amount",CompanyHandler.getCurrencyToBaseAmount(session,request,entry.getAmount(),currencyid,entry.getJournalEntry().getEntryDate()));
                jArr.put(obj);
            }
            jobj.put("data", jArr);
         } catch (JSONException e) {
                throw ServiceException.FAILURE("CompanyHandler.getJournalEntryDetails", e);
        }
         return jobj;
    }*/
    /*public static void saveJournalEntry(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException{
		try {
            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
            HashSet hs = new HashSet();
            JSONArray jArr=new JSONArray(request.getParameter("detail"));
            for(int i=0;i<jArr.length();i++){
                JSONObject jobj=jArr.getJSONObject(i);
                JournalEntryDetail jed=new JournalEntryDetail();
                jed=new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(jobj.getDouble("amount"));
                jed.setAccount((Account)session.get(Account.class,jobj.getString("accountid")));
                jed.setDebit(jobj.getBoolean("debit"));
                String desc = "";
                if(!jobj.getString("description").equals("")){
                       desc = java.net.URLDecoder.decode(jobj.getString("description"));
                }
                jed.setDescription(desc);
                hs.add(jed);
            }
            JournalEntry je=makeJournalEntry(session, company.getCompanyID(),
                    AuthHandler.getDateFormatter(request).parse(request.getParameter("entrydate")),
                    request.getParameter("memo"),
                    request.getParameter("entryno"),request.getParameter("currencyid"),
                    hs,request);
            ProfileHandler.insertAuditLog(session, AuditAction.JOURNAL_ENTRY_MADE, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) + " added new journal transaction: "+je.getEntryNumber(), request);
        } catch (ParseException ex) {
                throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException e) {
                throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (JSONException e) {
                throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }*/

     /*spublic static void saveBankReconciliation(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException, ParseException{
		try {
            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
            Account account=(Account)session.get(Account.class,request.getParameter("accid"));
            Date startdate=AuthHandler.getDateFormatter(request).parse(request.getParameter("startdate"));
            Date enddate=AuthHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            double clearingAmount=Double.parseDouble(request.getParameter("clearingbalance"));
            double endingAmount=Double.parseDouble(request.getParameter("endingbalance"));
            HashSet hs = new HashSet();
            JSONArray jArr=new JSONArray(request.getParameter("d_details"));
            for(int i=0;i<jArr.length();i++){
                JSONObject jobj=jArr.getJSONObject(i);
                BankReconciliationDetail brd=new BankReconciliationDetail();
                brd.setCompany(company);
                brd.setAmount(jobj.getDouble("d_amount"));
                brd.setJournalEntry((JournalEntry)session.get(JournalEntry.class,jobj.getString("d_journalentryid")));
                brd.setAccountnames(jobj.getString("d_accountname"));
                brd.setDebit(true);
                hs.add(brd);
            }
            jArr=new JSONArray(request.getParameter("c_details"));
            for(int i=0;i<jArr.length();i++){
                JSONObject jobj=jArr.getJSONObject(i);
                BankReconciliationDetail brd=new BankReconciliationDetail();
                brd.setCompany(company);
                brd.setAmount(jobj.getDouble("c_amount"));
                brd.setJournalEntry((JournalEntry)session.get(JournalEntry.class,jobj.getString("c_journalentryid")));
                brd.setAccountnames(jobj.getString("c_accountname"));
                brd.setDebit(false);
                hs.add(brd);
            }
            BankReconciliation br=new BankReconciliation();
            br.setStartDate(startdate);
            br.setEndDate(enddate);
            br.setAccount(account);
            br.setClearingAmount(clearingAmount);
            br.setEndingAmount(endingAmount);
            br.setCompany((Company) session.get(Company.class, company.getCompanyID()));
            Iterator<BankReconciliationDetail> itr = hs.iterator();
            while (itr.hasNext()) {
                BankReconciliationDetail brd = itr.next();
                brd.setBankReconciliation(br);
            }
            br.setDetails(hs);
            session.saveOrUpdate(br);
        } catch (SessionExpiredException e) {
                throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (JSONException e) {
                throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }*/
    /*public static JSONObject getBankReconciliation(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String accid=request.getParameter("accid");
            ArrayList params=new ArrayList();
            params.add(AuthHandler.getCompanyid(request));
            String condition=" where company.companyID=? and deleted=false ";
            if(!StringUtil.isNullOrEmpty(accid)){
             params.add(accid);
             condition+= " and account.ID=?";
            }
            String query="from BankReconciliation"+condition;
            List list = HibernateUtil.executeQuery(session, query,params.toArray());
            int count=list.size();
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                BankReconciliation entry = (BankReconciliation) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", entry.getID());
                obj.put("startdate", AuthHandler.getDateFormatter(request).format(entry.getStartDate()));
                obj.put("enddate", AuthHandler.getDateFormatter(request).format(entry.getEndDate()));
                obj.put("clearingbalance", entry.getClearingAmount());
                obj.put("endingbalance", entry.getEndingAmount());
                obj.put("difference", entry.getEndingAmount()-entry.getClearingAmount());
                obj.put("accountname", entry.getAccount().getName());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getJournalEntry", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("CompanyHandler.getJournalEntry", e);
        }
        return jobj;
    }*/

    /*public static void deleteBankReconciliation(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException, JSONException{
        try{
        JSONArray jArr=new JSONArray(request.getParameter("data"));
            for(int i=0;i<jArr.length();i++){
                JSONObject jobj=jArr.getJSONObject(i);
                    BankReconciliation br=(BankReconciliation) session.get(BankReconciliation.class,jobj.getString("id") );
                    br.setDeleted(true);
                    session.save(br);
                }
        }
        catch(HibernateException ex){
            throw ServiceException.FAILURE("Selected record(s) are currently used in transaction(s)", ex);
        }

    }*/
    /*public static void checkLockPeriod(Session session,Date date,String companyid, HttpServletRequest request) throws ServiceException, SessionExpiredException, ParseException, AccountingException{
        JSONObject jObjX=getYearLock(session,request);
         try {
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, companyid);
                JSONArray jarr = jObjX.getJSONArray("data");
                for (int j = 0; j < jarr.length(); j++) {
                    String sdate=jarr.getJSONObject(j).getString("startdate");
                    String edate=jarr.getJSONObject(j).getString("enddate");
                 if(StringUtil.isNullOrEmpty(sdate)||(StringUtil.isNullOrEmpty(edate)))
                     throw new AccountingException("Please save the settings of Account Preferences first");
                 Date startDate= AuthHandler.getDateFormatter(request).parse(sdate);
                 Date endDate= AuthHandler.getDateFormatter(request).parse(edate);
                 if (((startDate.before(date)&&endDate.after(date))||(startDate.equals(date)||endDate.equals(date)))&&jarr.getJSONObject(j).getBoolean("islock"))
                    throw new AccountingException("Transaction can not be completed. Date belongs to locked Period");

                 if (preferences.getBookBeginningFrom().compareTo(date) > 0&&preferences.getFinancialYearFrom().compareTo(date)<=0&&endDate.compareTo(date)>0)
                    throw new AccountingException("Transaction cannot be earlier than the book beginning date");

             }
         }  catch (JSONException e) {
                throw ServiceException.FAILURE(e.getMessage(), e);
         }
    }*/

    /*public static JournalEntry makeJournalEntry(Session session, String companyid, Date date,String memo, String entryNumber,String currencyid, HashSet<JournalEntryDetail> details, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException, SessionExpiredException, ParseException {
        checkLockPeriod(session,date,companyid,request);
        JournalEntry je=new JournalEntry();
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, companyid);
             String q = "from JournalEntry where entryNumber=? and company.companyID=?";
            if (!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, companyid}).isEmpty())
                entryNumber=getNextAutoNumber(session, preferences, StaticValues.AUTONUM_JOURNALENTRY);
            je.setEntryNumber(entryNumber);
            je.setAutoGenerated(getNextAutoNumber(session, preferences, StaticValues.AUTONUM_JOURNALENTRY).equals(entryNumber));
            je.setEntryDate(date);
            je.setMemo(memo);
            je.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
            je.setCompany((Company) session.get(Company.class, companyid));
            Iterator<JournalEntryDetail> itr = details.iterator();
            double amount = 0.0;
            while (itr.hasNext()) {
                JournalEntryDetail jed = itr.next();
                jed.setJournalEntry(je);
                if (jed.isDebit()) {
                    amount += jed.getAmount();
                } else {
                    amount -= jed.getAmount();
                }
            }
            if (Math.abs(amount) >= 0.000001) {
                throw new AccountingException("Debit and credit amounts are not same");
            }
            je.setDetails(details);
            session.saveOrUpdate(je);
        return je;
    }*/

    /*public static JSONObject getInventory(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
           String start=request.getParameter("start");
           String limit=request.getParameter("limit");
           String ss=request.getParameter("ss");
           ArrayList params=new ArrayList();
           params.add(request.getParameter("productid"));
           params.add(AuthHandler.getCompanyid(request));
           String condition=" and inven.deleted=false and inven.company.companyID=?";
            if(StringUtil.isNullOrEmpty(ss)==false){
                params.add(ss+"%");
                condition+= " and inven.product.name like ?";
            }
            String query="select inven from " +
                    "Inventory inven " +
                    "where " +
                    "    inven not in (select inventory from InvoiceDetail invd) " +
                    "and inven not in (select inventory from CreditNoteDetail cnd)" +
                    "and inven not in (select inventory from GoodsReceiptDetail cnd)" +
                    "and inven not in (select inventory from DebitNoteDetail cnd)" +
                    " and inven.product.ID = ? "+condition +
                    " group by inven.ID";
            List list = HibernateUtil.executeQuery(session,query,params.toArray());
            int remQuantity=0;
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
           while (itr.hasNext()) {
                Inventory inv=(Inventory)itr.next();
                if(inv.isCarryIn())
                    remQuantity +=inv.getQuantity();
                else
                    remQuantity -=inv.getQuantity();
                JSONObject obj = new JSONObject();
                obj.put("inventoryid", "");
                obj.put("productid", "");
                obj.put("productname", "");
                obj.put("quantity", inv.getQuantity());
                obj.put("remquantity", remQuantity);
                obj.put("date", "");
                obj.put("carryin", "Stock");
                obj.put("desc", "Stock exists.");
                obj.put("uom", inv.getProduct().getUnitOfMeasure().getName());
                jArr.put(obj);
             }
            query="select inven, je from " +
                    "Inventory inven, JournalEntry je " +
                    "where (" +
                    "    inven.ID in (select ID from InvoiceDetail where invoice.journalEntry=je)" +
                    " or inven.ID in (select ID from CreditNoteDetail where creditNote.journalEntry=je)" +
                    " or inven.ID in (select ID from GoodsReceiptDetail where goodsReceipt.journalEntry=je)" +
                    " or inven.ID in (select ID from DebitNoteDetail where debitNote.journalEntry=je)" +
                    ") and je.deleted=false and inven.product.ID = ? "+condition +
                    " order by je.entryDate";
            list = HibernateUtil.executeQuery(session,query,params.toArray());
            int count=list.size();
            if(StringUtil.isNullOrEmpty(start)==false && StringUtil.isNullOrEmpty(limit)==false){
                list=HibernateUtil.executeQueryPaging(session, query,params.toArray(), new Integer[]{Integer.parseInt(start),Integer.parseInt(limit)});
            }
            itr = list.iterator();

            while(itr.hasNext()) {
                Object[] row=(Object[])itr.next();
                Inventory inventory = (Inventory) row[0];
                JournalEntry je = (JournalEntry) row[1];
                JSONObject obj = new JSONObject();
                obj.put("inventoryid", inventory.getID());
                obj.put("productid", inventory.getProduct().getID());
                obj.put("productname", inventory.getProduct().getName());
                obj.put("quantity", inventory.getQuantity());
                if(inventory.isCarryIn())
                    remQuantity+=inventory.getQuantity();
                else
                    remQuantity-=inventory.getQuantity();
                obj.put("remquantity", remQuantity);
                obj.put("date", AuthHandler.getDateFormatter(request).format(je.getEntryDate()));
                obj.put("carryin", inventory.isCarryIn());
                obj.put("desc", inventory.getDescription());
//                obj.put("rate");
                obj.put("uom", inventory.getProduct().getUnitOfMeasure().getName());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getInventory", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("CompanyHandler.getInventory", e);
        }
        return jobj;
    }*/

    /*public static Inventory makeInventory(Session session, HttpServletRequest request, Product product, int quantity, String description, boolean carryIn, boolean defective) throws ServiceException, HibernateException, AccountingException {
        Inventory invetory=new Inventory();
        try {
            String query="select sum((case when carryIn=true then quantity else -quantity end)) from Inventory where product.ID=? group by product.ID";
            List l=HibernateUtil.executeQuery(session, query, product.getID());
            invetory.setProduct(product);
            invetory.setQuantity(quantity);
            invetory.setDescription(description);
            invetory.setCarryIn(carryIn);
            invetory.setDefective(defective);
            invetory.setNewInv(false);
            invetory.setCompany((Company)session.get(Company.class,AuthHandler.getCompanyid(request)));
            session.save(invetory);
        } catch (SessionExpiredException ex) {
            ServiceException.FAILURE("CompanyHandler.makeInventory", ex);
        }
        return invetory;
    }*/
     /*public static Inventory makeNewInventory(Session session, HttpServletRequest request, Product product, int quantity, String description, boolean carryIn, boolean defective) throws ServiceException, HibernateException, AccountingException {
        Inventory invetory=new Inventory();
        try {
            String query="select sum((case when carryIn=true then quantity else -quantity end)) from Inventory where product.ID=? group by product.ID";
            List l=HibernateUtil.executeQuery(session, query, product.getID());
           invetory.setProduct(product);
            invetory.setQuantity(quantity);
            invetory.setDescription(description);
            invetory.setCarryIn(carryIn);
            invetory.setDefective(defective);
            invetory.setNewInv(true);
            invetory.setCompany((Company)session.get(Company.class,AuthHandler.getCompanyid(request)));
            session.save(invetory);
        } catch (SessionExpiredException ex) {
            ServiceException.FAILURE("CompanyHandler.makeInventory", ex);
        }
        return invetory;
    }*/
    /*public static JSONObject getPrice(Session session, HttpServletRequest request, String productid) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String query="from PriceList";
            String start=request.getParameter("start");
            String limit=request.getParameter("limit");
            String ss=request.getParameter("ss");
            String condition=" where company.companyID=? and product.ID=? ";
            ArrayList params=new ArrayList();
            params.add(AuthHandler.getCompanyid(request));
            params.add(productid);
            if(StringUtil.isNullOrEmpty(ss)==false){
                params.add(ss+"%");
                if(condition.length()>0)
                    condition+=" and";
                else
                    condition+=" where";
                condition+= " product.name like ?";
            }
            condition+= " order by applyDate desc";
            query+=condition;
            List list = HibernateUtil.executeQuery(session, query, params.toArray());
            int count=list.size();
            if(StringUtil.isNullOrEmpty(start)==false&&StringUtil.isNullOrEmpty(limit)==false){
                list=HibernateUtil.executeQueryPaging(session, query, params.toArray(), new Integer[]{Integer.parseInt(start),Integer.parseInt(limit)});
            }
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                PriceList price = (PriceList) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("priceid", price.getID());
                obj.put("applydate", AuthHandler.getDateFormatter(request).format(price.getApplyDate()));
                obj.put("carryin", price.isCarryIn());
                obj.put("price", price.getPrice());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getParentAccounts", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("CompanyHandler.getParentAccounts", e);
        }
        return jobj;
    }*/

    /*public static void setNewPrice(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
		try {
            ArrayList params=new ArrayList();
            boolean carryIn=Boolean.parseBoolean(request.getParameter("carryin"));
            Date appDate=AuthHandler.getDateFormatter(request).parse(request.getParameter("applydate"));
            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
            Product product=(Product)session.get(Product.class,request.getParameter("productid"));
            String query="from PriceList where applyDate=? and product.ID=? and carryIn=? and company.companyID=?";
            params.add(appDate);
            params.add(product.getID());
            params.add(carryIn);
            params.add(company.getCompanyID());
            List list = HibernateUtil.executeQuery(session, query, params.toArray());
            PriceList price;
            if(list.size()<=0){
                price=new PriceList();
                price.setCarryIn(carryIn);
                price.setApplyDate(appDate);
                price.setProduct(product);
                price.setCompany(company);
            }else{
                price=(PriceList)list.get(0);
            }
            price.setPrice(Double.parseDouble(request.getParameter("price")));
            session.saveOrUpdate(price);
            ProfileHandler.insertAuditLog(session, AuditAction.PRICE_CHANGED, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) + " changed the price for product "+
                    price.getProduct().getName()+"("+price.getProduct().getDescription()+")"
                    +" to "+price.getPrice()+" "+((KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request))).getName(), request);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.setNewPrice", ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("CompanyHandler.setNewPrice", ex);
        }
    }*/

   /* public static String saveProduct(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException{
		try {
            Product product;
            String auditMsg="added";
            String auditID=AuditAction.PRODUCT_CREATION;
            String productid=request.getParameter("productid");
            if(StringUtil.isNullOrEmpty(productid)){
                product=new Product();
                product.setDeleted(false);
            }else{
                product=(Product)session.get(Product.class, productid);
                auditMsg="updated";
                auditID=AuditAction.PRODUCT_UPDATION;
            }
            product.setName(request.getParameter("productname"));
            product.setDescription(request.getParameter("desc"));
            String parentid=request.getParameter("parentid");
            boolean issub = request.getParameter("subproduct")!=null;
            if(issub&&!StringUtil.isNullOrEmpty(parentid)){
                product.setParent((Product)session.get(Product.class, parentid));
            }
            product.setUnitOfMeasure((UnitOfMeasure)session.get(UnitOfMeasure.class,request.getParameter("uomid")));
            product.setPurchaseAccount((Account)session.get(Account.class,request.getParameter("purchaseaccountid")));
            product.setSalesAccount((Account)session.get(Account.class,request.getParameter("salesaccountid")));
            product.setPurchaseReturnAccount((Account)session.get(Account.class,request.getParameter("purchaseretaccountid")));
            product.setSalesReturnAccount((Account)session.get(Account.class,request.getParameter("salesretaccountid")));
            product.setLeadTimeInDays(Integer.parseInt(request.getParameter("leadtime")));
            product.setReorderLevel(Integer.parseInt(request.getParameter("reorderlevel")));
            product.setReorderQuantity(Integer.parseInt(request.getParameter("reorderquantity")));
            product.setCompany((Company)session.get(Company.class,AuthHandler.getCompanyid(request)));
            session.saveOrUpdate(product);
                int quantity;
                try{
                    quantity=Integer.parseInt(request.getParameter("quantity"));
                }catch(Exception e){
                    quantity=0;
                }
                if(quantity>0 ){
                    if(StringUtil.isNullOrEmpty(productid))
                        makeNewInventory(session, request, product, quantity, "Inventory Opened", true, false);
                    else{
                        Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
                        String selQuery = "select id from Inventory   where product.ID =? and newinv='T' and company.companyID=?";
                        List list = HibernateUtil.executeQuery(session, selQuery, new Object[]{productid, company.getCompanyID()});
                        Iterator itr = list.iterator();
                        if(list.isEmpty()){
                            makeNewInventory(session, request, product, quantity, "Inventory Opened", true, false);
                        }
                        else{
                            String row= (String) itr.next();
                            Inventory inventory = (Inventory)session.get(Inventory.class,row);
                            inventory.setQuantity(quantity);
                        }
                    }
                }
            ProfileHandler.insertAuditLog(session, auditID, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) +" "+auditMsg+" product "+product.getName()+"("+product.getDescription()+")", request);
            if(auditMsg.equals("added"))
                ProfileHandler.insertAuditLog(session, AuditAction.INVENTORY_OPENED, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) +" added Product \""+product.getName()+ "\" in Inventory", request);
            return product.getID();
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }catch (HibernateException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }*/
    public static JSONObject getProducts(Session session, String productid, String companyID) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            ArrayList params=new ArrayList();
            params.add(companyID);
            String query="select p," +
                    "(select pl1.price from PriceList pl1 where product.ID=p.ID and carryIn=true and applyDate in (select max(applyDate) as ld from PriceList where product.ID=pl1.product.ID and carryIn=pl1.carryIn group by product))," +
                    "(select pl2.price from PriceList pl2 where product.ID=p.ID and carryIn=false and applyDate in (select max(applyDate) as ld from PriceList where product.ID=pl2.product.ID and carryIn=pl2.carryIn group by product)), " +
                    "(select sum((case when carryIn=true then quantity else -quantity end)) from Inventory where product.ID=p.ID group by product.ID), " +
                    "(select quantity from Inventory where product.ID=p.ID and newinv='T' group by product.ID), " +
                    "(select pl1.price from PriceList pl1 where product.ID=p.ID and carryIn=true and applyDate in (select min(applyDate) as ld from PriceList where product.ID=pl1.product.ID and carryIn=pl1.carryIn group by product)) " +
                    "from Product p where p.parent is null and p.deleted=false and p.company.companyID=? order by p.name ";
            List list = HibernateUtil.executeQuery(session, query,params.toArray());
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Product product = (Product) row[0];
                if(product.getID().equals(productid)) continue;
                JSONObject obj = new JSONObject();
                obj.put("productid", product.getID());
                obj.put("productname", product.getName());
                 obj.put("desc", product.getDescription());
                UnitOfMeasure uom = product.getUnitOfMeasure();
                obj.put("uomid", uom==null?"":uom.getID());
                obj.put("uomname", uom==null?"":uom.getName());
                obj.put("precision", uom==null?0:(Integer) uom.getAllowedPrecision());
                obj.put("leadtime", product.getLeadTimeInDays());
                obj.put("reorderlevel", product.getReorderLevel());
                obj.put("reorderquantity", product.getReorderQuantity());
                obj.put("purchaseaccountid", (product.getPurchaseAccount()!=null?product.getPurchaseAccount().getID():""));
                obj.put("salesaccountid", (product.getSalesAccount()!=null?product.getSalesAccount().getID():""));
                obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount()!=null?product.getPurchaseReturnAccount().getID():""));
                obj.put("salesretaccountid", (product.getSalesReturnAccount()!=null?product.getSalesReturnAccount().getID():""));
                obj.put("level", 0);
                obj.put("purchaseprice", row[1]);
                obj.put("saleprice",row[2]);
                obj.put("quantity",(row[3]==null?0:row[3]));
                obj.put("initialquantity",(row[4]==null?0:row[4]));
                obj.put("initialprice",(row[5]==null?0:row[5]));
                jArr.put(obj);
                obj.put("leaf",getChildProducts(session, product, jArr, 0, productid));
            }
            jobj.put("data", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    private static boolean getChildProducts(Session session, Product product, JSONArray jArr, int level, String productid) throws JSONException, ServiceException {
        boolean leaf=true;
        Iterator<Product> itr = new TreeSet(product.getChildren()).iterator();
        level++;
        while(itr.hasNext()) {
            Product child = itr.next();
            if(child.getID().equals(productid)||child.isDeleted()) continue;
            leaf=false;
            JSONObject obj = new JSONObject();
            obj.put("productid", child.getID());
            obj.put("productname", child.getName());
            obj.put("desc", child.getDescription());
            obj.put("uomid", child.getUnitOfMeasure().getID());
            obj.put("uomname", child.getUnitOfMeasure().getName());
            obj.put("leadtime", child.getLeadTimeInDays());
            obj.put("reorderlevel", child.getReorderLevel());
            obj.put("reorderquantity", child.getReorderQuantity());
            obj.put("purchaseaccountid", (child.getPurchaseAccount()!=null?child.getPurchaseAccount().getID():""));
            obj.put("salesaccountid", (child.getSalesAccount()!=null?child.getSalesAccount().getID():""));
            obj.put("purchaseretaccountid", (child.getPurchaseReturnAccount()!=null?child.getPurchaseReturnAccount().getID():""));
            obj.put("salesretaccountid", (child.getSalesReturnAccount()!=null?child.getSalesReturnAccount().getID():""));
            obj.put("parentid", product.getID());
            obj.put("parentname", product.getName());
            obj.put("level", level);
            String query="select (select pl1.price from PriceList pl1 where product.ID=p.ID and carryIn=true and applyDate in (select max(applyDate) as ld from PriceList where product.ID=pl1.product.ID and carryIn=pl1.carryIn group by product))," +
                    "(select pl2.price from PriceList pl2 where product.ID=p.ID and carryIn=false and applyDate in (select max(applyDate) as ld from PriceList where product.ID=pl2.product.ID and carryIn=pl2.carryIn group by product))," +
                    "(select sum((case when carryIn=true then quantity else -quantity end)) from Inventory where product.ID=p.ID group by product.ID)" +
                    " from Product p where p.ID=?";
            List list=HibernateUtil.executeQuery(session, query, child.getID());
            if(!list.isEmpty()){
                Object[] row=(Object[])list.get(0);
                obj.put("purchaseprice", row[0]);
                obj.put("saleprice",row[1]);
                obj.put("quantity",(row[2]==null?0:row[2]));
            }
            jArr.put(obj);
            obj.put("leaf",getChildProducts(session, child, jArr, level, productid));
        }

        return leaf;
    }

    /*public static void deleteProducts(Session session,HttpServletRequest request) throws HibernateException, JSONException, SessionExpiredException, AccountingException, ServiceException {
        String ids[] = request.getParameterValues("ids");
        String selQuery="";
        ArrayList params1 = new ArrayList();
        try{
            String qMarks = "";
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));

                qMarks = "";
                for (int i = 0; i < ids.length; i++){
                    if (!StringUtil.isNullOrEmpty(ids[i])) {
                            params1.add(ids[i]);
                            qMarks += "?,";
                    }
                }
                qMarks=qMarks.substring(0,Math.max(0, qMarks.length()-1));
            params1.add(company.getCompanyID());
            selQuery = "from PurchaseOrderDetail pod where product.ID in( "+qMarks +") and pod.company.companyID=?";
            List list1 = HibernateUtil.executeQuery(session, selQuery, params1.toArray());
            selQuery = "from SalesOrderDetail sod where product.ID in( "+qMarks +") and sod.company.companyID=?";
            List list2 = HibernateUtil.executeQuery(session, selQuery, params1.toArray());
            int count1=list1.size();
            int count2=list2.size();
             if(count1>0)
                 throw new AccountingException("Selected record(s) are currently used in Purchase Order(s).So it cannot be deleted.");
            else if(count2>0)
                 throw new AccountingException("Selected record(s) are currently used in Sales Order(s).So it cannot be deleted.");
            else{
                for (int i = 0; i < ids.length; i++) {
                    Product product=(Product) session.get(Product.class,ids[i] );
                    product.setDeleted(true);
                    session.save(product);
                }
            }
        }
        catch(ServiceException ex){
            throw ServiceException.FAILURE("Selected record(s) are currently used in transaction(s)", ex);
        }
    }*/

    /*public static void saveUnitOfMeasure(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException {
        try {
            String delQuery="";
            int delCount=0;
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            ArrayList params = new ArrayList();
            params.add("null");
            String qMarks = "?";
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("uomid")) == false) {
                    params.add(jobj.getString("uomid"));
                    qMarks += ",?";
                }
            }
            params.add(company.getCompanyID());
            try{
                delQuery = "delete from UnitOfMeasure u where u.ID in(" + qMarks + ") and u.company.companyID=?";
                delCount = HibernateUtil.executeUpdate(session, delQuery, params.toArray());
            }
            catch(ServiceException ex){
                throw new AccountingException("Selected record(s) are currently used by product(s)");
            }
            UnitOfMeasure uom;
            String auditMsg;
            String auditID;
            String fullName = AuthHandler.getFullName(session, AuthHandler.getUserid(request));
            if (delCount > 0) {
                ProfileHandler.insertAuditLog(session, AuditAction.UNIT_OF_MEASURE_DELETED, "User " + fullName + " deleted " + delCount + " unit of measure", request);
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }
                if (StringUtil.isNullOrEmpty(jobj.getString("uomid"))) {
                    auditMsg = "added";
                    uom = new UnitOfMeasure();
                    auditID = AuditAction.UNIT_OF_MEASURE_CREATED;
                } else {
                    uom = (UnitOfMeasure) session.get(UnitOfMeasure.class, jobj.getString("uomid"));
                    auditMsg = "updated";
                    auditID = AuditAction.UNIT_OF_MEASURE_UPDATED;
                }

                uom.setName(jobj.getString("uomname"));
                uom.setType(jobj.getString("uomtype"));
                uom.setAllowedPrecision(jobj.getInt("precision"));
                uom.setCompany(company);
                session.saveOrUpdate(uom);
                ProfileHandler.insertAuditLog(session, auditID, "User " + fullName + " " + auditMsg + " unit of measure to " + uom.getName(), request);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }*/

    /*public static JSONObject getUnitOfMeasure(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String query="from UnitOfMeasure where company.companyID=?";
            List list = HibernateUtil.executeQuery(session, query, AuthHandler.getCompanyid(request));
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                UnitOfMeasure uom = (UnitOfMeasure) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("uomid", uom.getID());
                obj.put("uomname", uom.getName());
                obj.put("precision", uom.getAllowedPrecision());
                obj.put("uomtype", uom.getType());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getParentAccounts", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("CompanyHandler.getParentAccounts", e);
        }

        return jobj;
    }*/

    /*public static void saveTax(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            ArrayList params = new ArrayList();
            params.add("null");
            String qMarks = "?";
            Company company = (Company) session.load(Company.class, AuthHandler.getCompanyid(request));
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("taxid")) == false) {
                    params.add(jobj.getString("taxid"));
                    qMarks += ",?";
                }
            }
            params.add(company.getCompanyID());
            int delCount=0;
            try{
                String delQuery = "delete from TaxList  where tax.ID in(" + qMarks + ") and company.companyID=?";
                delCount = HibernateUtil.executeUpdate(session, delQuery, params.toArray());
                delQuery = "delete from Tax  where ID in(" + qMarks + ") and company.companyID=?";
                delCount = HibernateUtil.executeUpdate(session, delQuery, params.toArray());
            }
            catch(ServiceException ex){
                throw new  AccountingException("The Tax code(s) is used in transaction(s). So, it cannot be deleted.");
            }

            Tax tax;
            String auditMsg;
            String auditID;
            String fullName = AuthHandler.getFullName(session, AuthHandler.getUserid(request));
            if (delCount > 0) {
                ProfileHandler.insertAuditLog(session, AuditAction.TAX_DETAIL_DELETED, "User " + fullName + " deleted " + delCount + " Tax Details", request);
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }
                if (StringUtil.isNullOrEmpty(jobj.getString("taxid"))) {
                    auditMsg = "added";
                    tax = new Tax();
                    auditID = AuditAction.TAX_DETAIL_CREATED;
                } else {
                    tax = (Tax) session.load(Tax.class, jobj.getString("taxid"));
                    auditMsg = "updated";
                    auditID = AuditAction.TAX_DETAIL_DELETED;
                }
                tax.setName(jobj.getString("taxname"));
                tax.setTaxCode(jobj.getString("taxcode"));//accountid
                tax.setCompany(company);
                tax.setAccount((Account) session.get(Account.class, jobj.getString("accountid")));

                session.saveOrUpdate(tax);
                setNewTax(session, request,jobj,tax);
                ProfileHandler.insertAuditLog(session, auditID, "User " + fullName + " " + auditMsg +" "+ tax.getName(), request);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }*/

    /*public static boolean checkApplyTax(Session session, HttpServletRequest request) throws ServiceException, HibernateException, JSONException, SessionExpiredException, ParseException{
        List list=null;
        boolean flag=true;
        String taxid=null;
        JSONArray jArr = new JSONArray(request.getParameter("data"));
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            ArrayList params=new ArrayList();
            taxid=jobj.getString("taxid");
            Date appDate=AuthHandler.getDateFormatter(request).parse(jobj.getString("applydate"));
            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
            String query="from TaxList where applyDate=? and tax.ID=?  and company.companyID=?";
            params.add(appDate);
            params.add(taxid);
            params.add(company.getCompanyID());
            list = HibernateUtil.executeQuery(session, query, params.toArray());
            if(!list.isEmpty()){
                flag=false;
                return false;
            }
        }
        return flag;
    }*/

    public static TaxList setNewTax(Session session, HttpServletRequest request,JSONObject jobj,Tax tax) throws ServiceException, HibernateException, JSONException{
		TaxList taxlist=null;
        try {
            List list = null;
            ArrayList params=new ArrayList();
            Date appDate=AuthHandler.getDateFormatter(request).parse(jobj.getString("applydate"));
            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
                String query="from TaxList where applyDate=? and tax.ID=?  and company.companyID=?";
                params.add(appDate);
                params.add(tax.getID());
                params.add(company.getCompanyID());
                list = HibernateUtil.executeQuery(session, query, params.toArray());
            if(list!=null&&!list.isEmpty()){
                taxlist=(TaxList)list.get(0);

            }else
                taxlist=new TaxList();
            taxlist.setApplyDate(appDate);
            taxlist.setTax(tax);
            taxlist.setCompany(company);

            taxlist.setPercent(Double.parseDouble(jobj.getString("percent")));
            session.saveOrUpdate(taxlist);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.setNewTax", ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("CompanyHandler.setNewTax", ex);
        }
        return taxlist;
    }

    /*public static JSONObject getTax(Session session, HttpServletRequest request,Date transactiondate ) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            ArrayList params = new ArrayList();
            String condition = "";
            if (transactiondate!=null){
                    params.add(transactiondate);
                    condition += " and tl1.applyDate <= ?  ";
            }
             String query="select t," +
                    "(select tl1.percent from TaxList tl1 where tax.ID=t.ID  and applyDate in (select max(applyDate) as ld from TaxList where tax.ID=tl1.tax.ID "+condition+" group by tax))," +
                    "(select max(tl1.applyDate) from TaxList tl1 where tax.ID=t.ID  and applyDate in (select max(applyDate) as ld from TaxList where tax.ID=tl1.tax.ID "+condition+" group by tax))" +
                    "from Tax t where company.companyID=? and t.deleted=false  order by t.name ";
            List list = HibernateUtil.executeQuery(session, query,AuthHandler.getCompanyid(request));
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Tax tax = (Tax) row[0];
                JSONObject obj = new JSONObject();
                obj.put("taxid", tax.getID());
                obj.put("taxname", tax.getName());
                obj.put("percent", row[1]);
                obj.put("taxcode", tax.getTaxCode());
                obj.put("accountid", tax.getAccount().getID());
                obj.put("accountname", tax.getAccount().getName());
                obj.put("applydate", AuthHandler.getDateFormatter(request).format(row[2]));
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(CompanyHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("CompanyHandler:getTax: "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(CompanyHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("CompanyHandler:getTax: "+ex.getMessage(), ex);
        } catch (HibernateException ex) {
            Logger.getLogger(CompanyHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("CompanyHandler:getTax: "+ex.getMessage(), ex);
        }
        return jobj;
    }*/
    
    public static double getTaxPercent(Session session, HttpServletRequest request,Date transactiondate,String taxid) throws ServiceException, JSONException, SessionExpiredException{
        double percent=0;
		try {
             ArrayList params = new ArrayList();
             params.add(taxid);
             params.add(taxid);
             params.add(transactiondate);
             params.add(AuthHandler.getCompanyid(request));
             String query= "select tl1.percent from TaxList tl1 where tl1.tax.ID=?  and applyDate in (select max(applyDate) from TaxList where tax.ID=? and tl1.applyDate <= ? ) and company.companyID=?" ;
             Iterator itr =HibernateUtil.executeQuery(session, query,params.toArray()).iterator();
             while(itr.hasNext()){
                 percent=Double.parseDouble(itr.next().toString());
             }
        } catch (ServiceException ex) {
            Logger.getLogger(CompanyHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("CompanyHandler:getTax: "+ex.getMessage(), ex);
        } catch (NumberFormatException ex) {
            Logger.getLogger(CompanyHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("CompanyHandler:getTax: "+ex.getMessage(), ex);
        }
        return percent;
    }

     /*public static double getAccountBalanceDateWise(Session session,HttpServletRequest request, String accountid, Date startDate, Date endDate) throws ServiceException, SessionExpiredException {
        double amount=0;
        KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
        Account account=(Account)session.get(Account.class, accountid);
        ArrayList params=new ArrayList();
        params.add(accountid);
        Calendar cal= Calendar.getInstance();
        cal.setTime(account.getCreationDate());
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.HOUR,0);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"+AuthHandler.getTimeZoneDifference(request)));
      
        params.add(startDate);
        params.add(endDate);
        Date creationDate=cal.getTime();
        if((creationDate.after(startDate)||creationDate.equals(startDate))&&creationDate.before(endDate))
            amount=account.getOpeningBalance();
        String query="select (case when debit=true then amount else -amount end) ,jed from JournalEntryDetail jed where account.ID=? and jed.journalEntry.deleted=false and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<? ";

        List list = HibernateUtil.executeQuery(session, query, params.toArray());
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] row=(Object[])itr.next();
            JournalEntryDetail jed=(JournalEntryDetail)row[1];
            String fromcurrencyid=(jed.getJournalEntry().getCurrency()==null?currency.getCurrencyID():jed.getJournalEntry().getCurrency().getCurrencyID());
            amount+=CompanyHandler.getCurrencyToBaseAmount(session,request,((Double)row[0]).doubleValue(),fromcurrencyid,jed.getJournalEntry().getEntryDate());
        }
        if(itr.hasNext()) {
            amount+=((Double)itr.next()).doubleValue();
        }
        return amount;
    }*/

    /*public static double getAccountBalance(Session session,HttpServletRequest request, String accountid, Date startDate, Date endDate) throws ServiceException, SessionExpiredException {
        double amount=0;
        KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
        Account account=(Account)session.get(Account.class, accountid);
        amount=account.getOpeningBalance();
        String query="select (case when debit=true then amount else -amount end) ,jed from JournalEntryDetail jed where account.ID=? and jed.journalEntry.deleted=false and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<? ";
        ArrayList params=new ArrayList();
        params.add(accountid);
        if(startDate==null)startDate=new Date(0);
        if(endDate==null)endDate=new Date();
        params.add(startDate);
        params.add(endDate);
        List list = HibernateUtil.executeQuery(session, query, params.toArray());
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] row=(Object[])itr.next();
            JournalEntryDetail jed=(JournalEntryDetail)row[1];
            String fromcurrencyid=(jed.getJournalEntry().getCurrency()==null?currency.getCurrencyID():jed.getJournalEntry().getCurrency().getCurrencyID());
            amount+=CompanyHandler.getCurrencyToBaseAmount(session,request,((Double)row[0]).doubleValue(),fromcurrencyid,jed.getJournalEntry().getEntryDate());
        }
        if(itr.hasNext()) {
            amount+=((Double)itr.next()).doubleValue();
        }
        return amount;
    }*/

    /*public static JSONObject getLedger(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
		try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String entryChar="c",emptyChar="d";
            String query="select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and je.entryDate between ? and ? and ac.company.companyID=? and je.deleted=false order by je.entryDate";

            Date startDate = AuthHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate= AuthHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            Object[] params={
                request.getParameter("accountid"),
                startDate,
                endDate,
                AuthHandler.getCompanyid(request)
            };
            List list = HibernateUtil.executeQuery(session, query, params);
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            double balance=getAccountBalance(session,request, request.getParameter("accountid"), null, startDate);
            if(balance!=0){
                if(balance>0){
                    entryChar="d";emptyChar="c";
                }else{
                    entryChar="c";emptyChar="d";
                }
                JSONObject objlast = new JSONObject();
                objlast.put(entryChar+"_date", AuthHandler.getDateFormatter(request).format(startDate));
                objlast.put(entryChar+"_accountname", "Balance b/d");
                objlast.put(entryChar+"_journalentryid", "");
                objlast.put(entryChar+"_amount", Math.abs(balance));
                objlast.put(emptyChar+"_date", "");
                objlast.put(emptyChar+"_accountname", "");
                objlast.put(emptyChar+"_journalentryid", "");
                objlast.put(emptyChar+"_amount", "");
                jArr.put(objlast);
            }
            while(itr.hasNext()) {
                Object[] row=(Object[])itr.next();
                JournalEntry entry = (JournalEntry)row[0];
                JournalEntryDetail jed=(JournalEntryDetail)row[1];
                 String currencyid=(jed.getJournalEntry().getCurrency()==null?currency.getCurrencyID(): jed.getJournalEntry().getCurrency().getCurrencyID());
                JSONObject obj = new JSONObject();
                if(jed.isDebit()){
                    balance+=CompanyHandler.getCurrencyToBaseAmount(session,request,jed.getAmount(),currencyid,jed.getJournalEntry().getEntryDate());
                    entryChar="d";emptyChar="c";
                }else{
                    balance-=CompanyHandler.getCurrencyToBaseAmount(session,request,jed.getAmount(),currencyid,jed.getJournalEntry().getEntryDate());
                    entryChar="c";emptyChar="d";
                }
                Set details=entry.getDetails();
                Iterator iter=details.iterator();
                String accountName="";
                while(iter.hasNext()){
                    JournalEntryDetail d=(JournalEntryDetail)iter.next();
                    if(d.isDebit()==jed.isDebit()) continue;
                    accountName+=d.getAccount().getName()+", ";
                }
                accountName=accountName.substring(0, Math.max(0, accountName.length()-2));
                obj.put(entryChar+"_date", AuthHandler.getDateFormatter(request).format(entry.getEntryDate()));
                obj.put(entryChar+"_accountname", accountName);
                obj.put(entryChar+"_entryno", entry.getEntryNumber());
                obj.put(entryChar+"_journalentryid", entry.getID());
                obj.put(entryChar+"_amount",CompanyHandler.getCurrencyToBaseAmount(session,request,jed.getAmount(),currencyid,jed.getJournalEntry().getEntryDate())) ;
                obj.put(emptyChar+"_date", "");
                obj.put(emptyChar+"_accountname", "");
                obj.put(emptyChar+"_entryno", "");
                obj.put(emptyChar+"_journalentryid", "");
                obj.put(emptyChar+"_amount", "");
                jArr.put(obj);
            }
            if(balance!=0){
                if(balance>0){
                    entryChar="c";emptyChar="d";
                }else{
                    entryChar="d";emptyChar="c";
                }
                JSONObject objlast = new JSONObject();
                objlast.put(entryChar+"_date", request.getParameter("enddate"));
                objlast.put(entryChar+"_accountname", "Balance c/f");
                objlast.put(entryChar+"_journalentryid", "");
                objlast.put(entryChar+"_amount", Math.abs(balance));
                objlast.put(emptyChar+"_date", "");
                objlast.put(emptyChar+"_accountname", "");
                objlast.put(emptyChar+"_journalentryid", "");
                objlast.put(emptyChar+"_amount", "");
                jArr.put(objlast);
            }
            jobj.put("data", jArr);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getLedger", ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getLedger", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getLedger", ex);
        }
        return jobj;
    }*/

     /*public static JSONObject getReconciliationData(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException{
        JSONObject jobj=new JSONObject();
        KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
		try {
            String entryChar="c",emptyChar="d";
            String query="select je, jed from JournalEntry je inner join je.details jed inner join jed.account ac where ac.ID=? and je.deleted=false and je.entryDate between ? and ? and ac.company.companyID=? order by je.entryDate";

            Date startDate = AuthHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate= AuthHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            Object[] params={
                request.getParameter("accountid"),
                startDate,
                endDate,
                AuthHandler.getCompanyid(request)
            };
            List list = HibernateUtil.executeQuery(session, query, params);
            Iterator itr = list.iterator();
            JSONArray jArrL=new JSONArray();
            JSONArray jArrR=new JSONArray();
            double debitbalance=0;
            double creditbalance=0;
            double openingbalance=0;

            openingbalance=getAccountBalance(session,request, request.getParameter("accountid"), null, startDate);
            while(itr.hasNext()) {
                Object[] row=(Object[])itr.next();
                JournalEntry entry = (JournalEntry)row[0];
                JournalEntryDetail jed=(JournalEntryDetail)row[1];
                 String currencyid=(jed.getJournalEntry().getCurrency()==null?currency.getCurrencyID(): jed.getJournalEntry().getCurrency().getCurrencyID());
                JSONObject obj = new JSONObject();
                if(jed.isDebit()){

                    debitbalance+=CompanyHandler.getCurrencyToBaseAmount(session,request,jed.getAmount(),currencyid,jed.getJournalEntry().getEntryDate());
                    entryChar="d";emptyChar="c";
                }else{
                    creditbalance+=CompanyHandler.getCurrencyToBaseAmount(session,request,jed.getAmount(),currencyid,jed.getJournalEntry().getEntryDate());
                    entryChar="c";emptyChar="d";
                }
                Set details=entry.getDetails();
                Iterator iter=details.iterator();
                String accountName="";
                while(iter.hasNext()){
                    JournalEntryDetail d=(JournalEntryDetail)iter.next();
                    if(d.isDebit()==jed.isDebit()) continue;
                    accountName+=d.getAccount().getName()+", ";
                }
                accountName=accountName.substring(0, Math.max(0, accountName.length()-2));

                obj.put(entryChar+"_date", AuthHandler.getDateFormatter(request).format(entry.getEntryDate()));
                obj.put(entryChar+"_accountname", accountName);
                obj.put(entryChar+"_entryno", entry.getEntryNumber());
                obj.put(entryChar+"_journalentryid", entry.getID());
                obj.put(entryChar+"_amount", CompanyHandler.getCurrencyToBaseAmount(session,request,jed.getAmount(),currencyid,jed.getJournalEntry().getEntryDate())) ;
                obj.put(emptyChar+"_date", "");
                obj.put(emptyChar+"_accountname", "");
                obj.put(emptyChar+"_entryno", "");
                obj.put(emptyChar+"_journalentryid", "");
                obj.put(emptyChar+"_amount", "");
                if(entryChar.equals("d"))
                    jArrL.put(obj);
                else
                    jArrR.put(obj);
            }
            JSONObject fobj=new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("openingbalance", new JSONArray("["+openingbalance+"]"));
            fobj.put("total", new JSONArray("["+debitbalance+","+-creditbalance+"]"));
            jobj.put("data", fobj);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getLedger", ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getLedger", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getLedger", ex);
        }
        return jobj;
    }*/

    public static double[] getOpeningBalances(Session session, String companyid) throws ServiceException{
        double []balances={0,0};
        String query="from Account ac where ac.company.companyID=?";
        List list = HibernateUtil.executeQuery(session, query, companyid);
        Iterator itr = list.iterator();
           while(itr.hasNext()) {
                Account account=(Account)itr.next();
                double bal=account.getOpeningBalance();
                if(bal>0){
                    balances[0]+=bal;
                }else if(bal<0){
                    balances[1]+=bal;
                }
           }
        return balances;
    }

    public static double[] getOpeningBalancesDateWise(Session session, String companyid, Date startDate, Date endDate) throws ServiceException{
        double []balances={0,0};
        ArrayList params=new ArrayList();
        params.add(companyid);
        if(startDate==null)startDate=new Date(0);
        if(endDate==null)endDate=new Date();
        params.add(startDate);
        params.add(endDate);
        String query="from Account ac where ac.company.companyID=? and ac.creationDate>=? and  ac.creationDate<?";
        List list = HibernateUtil.executeQuery(session, query,params.toArray());
        Iterator itr = list.iterator();
           while(itr.hasNext()) {
                Account account=(Account)itr.next();
                double bal=account.getOpeningBalance();
                if(bal>0){
                    balances[0]+=bal;
                }else if(bal<0){
                    balances[1]+=bal;
                }
           }
        return balances;
    }

    /*public static JSONObject getTrialBalance(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
		try {
            String query="from Account ac where ac.company.companyID=? order by name";
            List list = HibernateUtil.executeQuery(session, query, AuthHandler.getCompanyid(request));
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            JSONArray tmpjArr=new JSONArray();
            Date startDate=AuthHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate=AuthHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
             double bals[]=getOpeningBalancesDateWise(session, AuthHandler.getCompanyid(request),startDate,endDate);

            double balance=-(bals[1]+bals[0]);
            if(balance!=0){
                JSONObject obj = new JSONObject();
                if(balance>0) {
                    obj.put("d_amount", balance);
                    obj.put("c_amount", "");
                }else {
                    obj.put("c_amount", -balance);
                    obj.put("d_amount", "");
                }
                obj.put("accountid", "");
                obj.put("fmt", "A");
                obj.put("accountname", "Difference in Opening balances");
                    jArr.put(obj);
            }
            while(itr.hasNext()) {
                Account account=(Account)itr.next();
                double amount=getAccountBalanceDateWise(session,request,account.getID(),startDate,endDate);
                if(amount==0)continue;
                JSONObject obj = new JSONObject();
                if(amount>0) {
                    obj.put("d_amount", amount);
                    obj.put("c_amount", "");
                }else {
                    obj.put("c_amount", -amount);
                    obj.put("d_amount", "");
                }
                obj.put("accountid", account.getID());
                obj.put("accountname", account.getName());
                if(Double.isNaN(obj.optDouble("c_amount")))
                    jArr.put(obj);
                else
                    tmpjArr.put(obj);
            }
            for(int i=0;i<tmpjArr.length();i++){
                jArr.put(tmpjArr.getJSONObject(i));
            }
            jobj.put("data", jArr);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }*/

    /*public static JSONObject getTrading(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
		try {
            double dtotal=0,ctotal=0;
            JSONArray jArrL=new JSONArray();
            JSONArray jArrR=new JSONArray();
            JSONObject objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Debit)</div>");
            objlast.put("fmt", "H");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Credit)</div>");
            objlast.put("fmt", "H");
//            jArrR.put(objlast);
            dtotal=getTrading(session, request, Group.NATURE_EXPENSES, jArrL);
            ctotal=getTrading(session, request, Group.NATURE_INCOME, jArrR);
            double balance=dtotal+ctotal;
            if(balance>0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", "Gross Loss");
                objlast.put("amount", balance);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal-=balance;
            }
            if(balance<0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("accountname", "Gross Profit");
                objlast.put("amount", -balance);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal-=balance;
            }
            objlast = new JSONObject();
            objlast.put("accountname", "Total Debit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Total Credit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", -ctotal);
            objlast.put("fmt", "T");
//            jArrR.put(objlast);

            JSONObject fobj=new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("["+dtotal+","+-ctotal+"]"));
            jobj.put("data", fobj);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getTrading", ex);
        }
        return jobj;
    }*/

    /*public static double getTrading(Session session, HttpServletRequest request, int nature, JSONArray jArr) throws ServiceException, HibernateException{
        double total=0;
		try {
            String query="from Group where parent is null and nature in ("+nature+") and affectGrossProfit=true and (company is null or company.companyID=?) order by nature, displayOrder";
            List list = HibernateUtil.executeQuery(session, query, AuthHandler.getCompanyid(request));
            Iterator itr = list.iterator();

            Date startDate=AuthHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate=AuthHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            while(itr.hasNext()) {
                Group group=(Group)itr.next();
                total+=formatGroupDetails(session,request, AuthHandler.getCompanyid(request), group, startDate, endDate, 0, false, jArr);
            }

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getTrading", ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getTrading", ex);
        }
        return total;
    }*/

    /*private static double formatAccountDetails(Session session,HttpServletRequest request, Account account, Date startDate, Date endDate, int level,boolean isDebit, boolean isBalanceSheet, JSONArray jArr) throws ServiceException, HibernateException, SessionExpiredException {
        double amount=getAccountBalance(session,request, account.getID(), startDate, endDate);
        double totalAmount=amount;

        Iterator<Account> itr=account.getChildren().iterator();
        JSONArray chArr=new JSONArray();
        while(itr.hasNext()){
            Account child=itr.next();
            totalAmount+=formatAccountDetails(session,request, child, startDate, endDate, level+1, isDebit, isBalanceSheet, chArr);
        }


        try{
            if(chArr.length()>0){
                JSONObject obj=new JSONObject();
                obj.put("accountname", account.getName());
                obj.put("accountid", account.getID());
                obj.put("level", level);
                obj.put("leaf", false);
                obj.put("amount", "");
                obj.put("isdebit", isDebit);
                jArr.put(obj);
                for(int i=0;i<chArr.length();i++){
                    jArr.put(chArr.getJSONObject(i));
                }
                if(amount!=0){
                    obj=new JSONObject();
                    obj.put("accountname", "Other "+account.getName());
                    obj.put("accountid", account.getID());
                    obj.put("level", level+1);
                    obj.put("leaf", true);
                    if(!isDebit)amount=-amount;
                    if(isBalanceSheet)amount=-amount;
                    obj.put("amount", amount);
                    obj.put("isdebit", isDebit);
                    jArr.put(obj);
                }

                obj=new JSONObject();
                obj.put("accountname", "Total "+account.getName());
                obj.put("accountid", account.getID());
                obj.put("level", level);
                obj.put("leaf", true);
                obj.put("show",true);
                double ta=totalAmount;
                if(!isDebit)ta=-ta;
                if(isBalanceSheet)ta=-ta;
                obj.put("amount", ta);
                obj.put("isdebit", isDebit);
                jArr.put(obj);
            }else if(amount!=0){
                JSONObject obj=new JSONObject();
                obj.put("accountname", account.getName());
                obj.put("accountid", account.getID());
                obj.put("level", level);
                obj.put("leaf", true);
                if(!isDebit)amount=-amount;
                if(isBalanceSheet)amount=-amount;
                obj.put("amount", (amount!=0.0?amount:""));
                obj.put("isdebit", isDebit);
                jArr.put(obj);
            }
        } catch (JSONException e) {
                throw ServiceException.FAILURE("CompanyHandler.formatAccountDetails", e);
        }
        return totalAmount;
    }*/

    /*private static double formatGroupDetails(Session session,HttpServletRequest request, String companyid, Group group, Date startDate, Date endDate, int level, boolean isBalanceSheet, JSONArray jArr) throws ServiceException, HibernateException, SessionExpiredException {
        double totalAmount=0;
        boolean isDebit=false;

        try{
            if(isBalanceSheet){
                if(group.getNature()==Group.NATURE_LIABILITY){
                    isDebit=true;
                }
            }else if(group.getNature()==Group.NATURE_EXPENSES){
                isDebit=true;
            }
            Set children=group.getChildren();
            JSONArray chArr=new JSONArray();
            String query="from Account where parent is null and group.ID=? and company.companyID=?";
            List list = HibernateUtil.executeQuery(session, query, new Object[]{group.getID(),companyid});
            Iterator itr=list.iterator();
            while(itr.hasNext()){
                Account account=(Account)itr.next();
                totalAmount+=formatAccountDetails(session,request, account, startDate, endDate, level+1, isDebit, isBalanceSheet, chArr);
            }
            if(children!=null&&!children.isEmpty()){
                itr=children.iterator();
                while(itr.hasNext()){
                    Group child=(Group)itr.next();
                    totalAmount+=formatGroupDetails(session,request, companyid, child, startDate, endDate, level+1, isBalanceSheet, chArr);
                }
            }

            if(chArr.length()>0){
                JSONObject obj=new JSONObject();
                obj.put("accountname", group.getName());
                obj.put("accountid", group.getID());
                obj.put("level", level);
                obj.put("leaf", false);
                obj.put("amount", "");
                obj.put("isdebit", isDebit);
                jArr.put(obj);
                for(int i=0;i<chArr.length();i++){
                    jArr.put(chArr.getJSONObject(i));
                }

                obj=new JSONObject();
                obj.put("accountname", "Total "+group.getName());
                obj.put("accountid", group.getID());
                obj.put("level", level);
                obj.put("leaf", true);
                obj.put("show",true);
                double ta=totalAmount;
                if(!isDebit)ta=-ta;
                if(isBalanceSheet)ta=-ta;
                obj.put("amount", ta);
                obj.put("isdebit", isDebit);
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CompanyHandler.formatGroupDetails", ex);
        }
        return totalAmount;
    }*/

    /*public static JSONObject getProfitLoss(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
		try {
            double dtotal=0,ctotal=0;
            JSONArray jArrL=new JSONArray();
            JSONArray jArrR=new JSONArray();
            JSONObject objlast = new JSONObject();
            double balance=getTrading(session, request, Group.NATURE_EXPENSES, new JSONArray())+
                    getTrading(session, request, Group.NATURE_INCOME, new JSONArray());
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Debit)</div>");
            objlast.put("fmt", "H");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Credit)</div>");
            objlast.put("fmt", "H");
//            jArrR.put(objlast);
            if(balance>0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("accountname", "Gross Loss");
                objlast.put("amount", balance);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal=balance;
            }
            if(balance<0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", "Gross Profit");
                objlast.put("amount", -balance);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal=balance;
            }
            dtotal+=getProfitLoss(session, request, Group.NATURE_EXPENSES, jArrL);
            ctotal+=getProfitLoss(session, request, Group.NATURE_INCOME, jArrR);
            balance=dtotal+ctotal;
            if(balance>0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", "Net Loss");
                objlast.put("amount", balance);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal-=balance;
            }
            if(balance<0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("accountname", "Net Profit");
                objlast.put("amount", -balance);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal-=balance;
            }
            objlast = new JSONObject();
            objlast.put("accountname", "Total Debit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Total Credit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", -ctotal);
            objlast.put("fmt", "T");
//            jArrR.put(objlast);

            JSONObject fobj=new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("["+dtotal+","+-ctotal+"]"));
            jobj.put("data", fobj);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getProfitLoss", ex);
        }
        return jobj;
    }*/

    /*public static double getProfitLoss(Session session, HttpServletRequest request, int nature, JSONArray jArr) throws ServiceException, HibernateException{
        double total=0;
		try {
            String query="from Group where parent is null and nature in ("+nature+") and affectGrossProfit=false and (company is null or company.companyID=?) order by nature, displayOrder";
            List list = HibernateUtil.executeQuery(session, query, AuthHandler.getCompanyid(request));
            Iterator itr = list.iterator();

            Date startDate=AuthHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate=AuthHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            while(itr.hasNext()) {
                Group group=(Group)itr.next();
                total+=formatGroupDetails(session,request, AuthHandler.getCompanyid(request), group, startDate, endDate, 0, false, jArr);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getProfitLoss", ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getProfitLoss", ex);
        }
        return total;
    }*/

    /*public static JSONObject getTradingAndProfitLoss(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
		try {
            double dtotal=0,ctotal=0;
            JSONArray jArrL=new JSONArray();
            JSONArray jArrR=new JSONArray();
            JSONObject objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Debit)</div>");
            objlast.put("fmt", "H");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Credit)</div>");
            objlast.put("fmt", "H");
//            jArrR.put(objlast);
            dtotal=getTrading(session, request, Group.NATURE_EXPENSES, jArrL);
            ctotal=getTrading(session, request, Group.NATURE_INCOME, jArrR);
            int len=jArrL.length()-jArrR.length();
            JSONArray jArr=jArrR;
            if(len<0){
                len=-len;
                jArr=jArrL;
            }
            for(int i=0;i<len;i++)
                jArr.put(new JSONObject());

            double balance=dtotal+ctotal;
            if(balance>0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", "Gross Loss");
                objlast.put("amount", balance);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                jArrL.put(new JSONObject());
                ctotal-=balance;
            }
            if(balance<0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("accountname", "Gross Profit");
                objlast.put("amount", -balance);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                jArrR.put(new JSONObject());
                dtotal-=balance;
            }
            objlast = new JSONObject();
            objlast.put("accountname", "Total Debit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Total Credit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", -ctotal);
            objlast.put("fmt", "T");
            jArrR.put(objlast);

            objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Debit)</div>");
            objlast.put("fmt", "H");
            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Credit)</div>");
            objlast.put("fmt", "H");
            jArrR.put(objlast);
            dtotal=0;
            ctotal=0;
            if(balance>0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("accountname", "Gross Loss");
                objlast.put("amount", balance);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal=balance;
            }
            if(balance<0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", "Gross Profit");
                objlast.put("amount", -balance);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal=balance;
            }
            dtotal+=getProfitLoss(session, request, Group.NATURE_EXPENSES, jArrL);
            ctotal+=getProfitLoss(session, request, Group.NATURE_INCOME, jArrR);
            balance=dtotal+ctotal;
            if(balance>0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", "Net Loss");
                objlast.put("amount", balance);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal-=balance;
            }
            if(balance<0){
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("accountname", "Net Profit");
                objlast.put("amount", -balance);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal-=balance;
            }
            objlast = new JSONObject();
            objlast.put("accountname", "Total Debit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Total Credit");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", -ctotal);
            objlast.put("fmt", "T");
//            jArrR.put(objlast);

            JSONObject fobj=new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("["+dtotal+","+-ctotal+"]"));
            jobj.put("data", fobj);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("CompanyHandler.getTradingAndProfitLoss", e);
        }
        return jobj;
    }*/

    public static double getBalanceAmount(JSONArray jArr) throws ServiceException{
        double balance=0;
		try {
            for(int i=0;i<jArr.length();i++){
                JSONObject obj=jArr.getJSONObject(i);
                try{balance+=obj.getDouble("d_amount");}catch(Exception ee){}
                try{balance-=obj.getDouble("c_amount");}catch(Exception ee){}
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("CompanyHandler.getBalanceAmount", e);
        }
        return balance;
    }

    public static double[] getTotals(JSONArray jArr) throws ServiceException{
        double d_total=0;
        double c_total=0;
		try {
            for(int i=0;i<jArr.length();i++){
                JSONObject obj=jArr.getJSONObject(i);if(obj.optBoolean("show"))continue;
                try{if(obj.getBoolean("isdebit"))d_total+=obj.getDouble("amount");}catch(Exception ee){}
                try{if(!obj.getBoolean("isdebit"))c_total+=obj.getDouble("amount");}catch(Exception ee){}
            }
        } catch (JSONException e) {
                throw ServiceException.FAILURE("CompanyHandler.getTotals", e);
        }
        double[] res={d_total, c_total};
        return res;
    }

    /*public static JSONObject getBalanceSheet(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
		try {
            double dtotal=0,ctotal=0;
            double balance=getTrading(session, request, Group.NATURE_EXPENSES, new JSONArray())+
                    getTrading(session, request, Group.NATURE_INCOME, new JSONArray())+
                    getProfitLoss(session, request, Group.NATURE_EXPENSES, new JSONArray())+
                    getProfitLoss(session, request, Group.NATURE_INCOME, new JSONArray());
            JSONArray jArrL=new JSONArray();
            JSONArray jArrR=new JSONArray();
            JSONObject objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Assets)</div>");
            objlast.put("fmt", "H");
//            jArrR.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Particulars");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", "<div align=right>Amount (Liabilities)</div>");
            objlast.put("fmt", "H");
//            jArrL.put(objlast);
            dtotal=-getBalanceSheet(session, request, Group.NATURE_LIABILITY, jArrL);
            ctotal=-getBalanceSheet(session, request, Group.NATURE_ASSET, jArrR);
            System.out.println(dtotal+","+ctotal);
            if(balance>0){
                objlast = new JSONObject();
                objlast.put("accountname","Net Loss");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("leaf", true);
                objlast.put("amount", balance);
                objlast.put("isdebit", false);
                objlast.put("fmt", "B");
                jArrR.put(objlast);
                ctotal-=balance;
            }

            if(balance<0){
                objlast = new JSONObject();
                objlast.put("accountname","Net Profit");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("leaf", true);
                objlast.put("amount", -balance);
                objlast.put("isdebit", true);
                objlast.put("fmt", "B");
                jArrL.put(objlast);
                dtotal-=balance;
            }

            double bals[]=getOpeningBalances(session, AuthHandler.getCompanyid(request));

            balance=bals[0]+bals[1];
            if(balance!=0){
                objlast = new JSONObject();
                objlast.put("accountname", "Difference in Opening balances");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("fmt", "A");
                objlast.put("amount", Math.abs(balance));
                objlast.put("leaf", true);
                objlast.put("isdebit", balance>0);
                if(balance>0){
                    dtotal+=balance;
                    jArrL.put(objlast);
                }else{
                    ctotal+=balance;
                    jArrR.put(objlast);
                }
            }
            objlast = new JSONObject();
            objlast.put("accountname", "Total Assets");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", dtotal);
            objlast.put("fmt", "T");
//            jArrR.put(objlast);
            objlast = new JSONObject();
            objlast.put("accountname", "Total Liabilities");
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", true);
            objlast.put("leaf", true);
            objlast.put("amount", -ctotal);
            objlast.put("fmt", "T");
//            jArrL.put(objlast);
            JSONObject fobj=new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("["+dtotal+","+-ctotal+"]"));
            jobj.put("data", fobj);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getBalanceSheet", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getBalanceSheet", ex);
        }
        return jobj;
    }*/

    /*public static double getBalanceSheet(Session session, HttpServletRequest request, int nature, JSONArray jArr) throws ServiceException, HibernateException{
        double total=0;
		try {
            String query="from Group where parent is null and nature in ("+nature+") and affectGrossProfit=false and (company is null or company.companyID=?) order by nature desc, displayOrder";
            List list = HibernateUtil.executeQuery(session, query,AuthHandler.getCompanyid(request));
            Iterator itr = list.iterator();

            Date startDate=AuthHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            Date endDate=AuthHandler.getDateFormatter(request).parse(request.getParameter("enddate"));
            while(itr.hasNext()) {
                Group group=(Group)itr.next();
                total+=formatGroupDetails(session,request, AuthHandler.getCompanyid(request), group, startDate, endDate, 0, true, jArr);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getBalanceSheet", ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getBalanceSheet", ex);
        }
        return total;
    }*/

    /*public static JSONObject getTerm(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try{
            String query="from Term where company.companyID=?";
            List list = HibernateUtil.executeQuery(session, query, AuthHandler.getCompanyid(request));
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Term ct = (Term) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("termid", ct.getID());
                obj.put("termname", ct.getTermname());
                obj.put("termdays", ct.getTermdays());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getCreditTerm", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getCreditTerm", ex);
        }
        return jobj;
    }*/

    /*public static void saveTerm(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException {
       try {
            String delQuery="";
            int delCount =0;
            List list =null;
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            ArrayList params = new ArrayList();
            params.add("null");
            String qMarks = "?";
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("termid")) == false) {
                    params.add(jobj.getString("termid"));
                    qMarks += ",?";
                }
            }
            params.add(company.getCompanyID());
            try{
                delQuery = "delete from Term c where c.ID in(" + qMarks + ") and c.company.companyID=?";
                delCount = HibernateUtil.executeUpdate(session, delQuery, params.toArray());
            }
            catch(ServiceException ex){
                int type=0;
                String tablename="in Transection (s)";
                String query ="from Customer c where c.creditTerm.ID not in(" + qMarks + ") and c.company.companyID=?";
                list = HibernateUtil.executeQuery(session, query, params.toArray());
                Iterator itr = list.iterator();
                if (itr.hasNext()){
                    tablename="by Customer(s)";
                    type=1;
                }
                query ="from Vendor v where v.debitTerm.ID not in(" + qMarks + ") and v.company.companyID=?";
                list = HibernateUtil.executeQuery(session, query, params.toArray());
                itr = list.iterator();
                if (itr.hasNext() ){
                    if(type==1)
                        tablename="by Vendor(s) and Customer(s)";
                    else
                        tablename="by Vendor(s)";
                }
                throw new AccountingException("Selected record(s) are currently used "+tablename);
            }
            Term crt;
            String auditMsg;
            String auditID;
            String fullName = AuthHandler.getFullName(session, AuthHandler.getUserid(request));
            if (delCount > 0) {
                ProfileHandler.insertAuditLog(session, AuditAction.CREDIT_TERM_DELETED, "User " + fullName + " deleted " + delCount + " Credit Term", request);
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }
                if (StringUtil.isNullOrEmpty(jobj.getString("termid"))) {
                    auditMsg = "added";
                    crt = new Term();
                    auditID = AuditAction.CREDIT_TERM_ADDED;
                } else {
                    crt = (Term) session.get(Term.class, jobj.getString("termid"));
                    auditMsg = "updated";
                    auditID = AuditAction.CREDIT_TERM_CHANGED;
                }

                crt.setTermname(jobj.getString("termname"));
                crt.setTermdays(Integer.parseInt(jobj.getString("termdays")));
                crt.setCompany(company);
                session.saveOrUpdate(crt);
                ProfileHandler.insertAuditLog(session, auditID, "User " + fullName + " " + auditMsg + " Credit Term to " + crt.getTermname(), request);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }*/

    /*public static JSONObject getMasterGroups(Session session, HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            String query = "from MasterGroup ";
            List lst = HibernateUtil.executeQuery(session, query);
            Iterator iter = lst.iterator();
            JSONArray jArr = new JSONArray();
            while (iter.hasNext()){
                MasterGroup mst = (MasterGroup) iter.next();
                JSONObject tmpObj = new JSONObject();
                tmpObj.put("id", mst.getID());
                tmpObj.put("name", mst.getGroupName());
                jArr.put(tmpObj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
                throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }*/

    /*public static JSONObject saveMasterGroup(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
        MasterGroup mastergp;
        String groupID=request.getParameter("id");
        if (StringUtil.isNullOrEmpty(groupID))
            mastergp = new MasterGroup();
        else
            mastergp = (MasterGroup) session.get(MasterGroup.class, groupID);

        mastergp.setGroupName(request.getParameter("name"));
        session.save(mastergp);
        return jobj;
    }*/

    /*public static JSONObject deleteMasterGroup(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            // Code for deletion of Master Group --- pending
        } catch (Exception e) {
                throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }*/

    /*public static JSONObject saveMasterItem(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
        MasterItem mstItem;
        String itemID=request.getParameter("id");
		try {
            if (StringUtil.isNullOrEmpty(itemID))
                mstItem = new MasterItem();
            else
                mstItem = (MasterItem) session.get(MasterItem.class, itemID);

            mstItem.setValue(request.getParameter("name"));
            mstItem.setMasterGroup((MasterGroup)session.get(MasterGroup.class, request.getParameter("groupid")));
            mstItem.setCompany((Company)session.get(Company.class, AuthHandler.getCompanyid(request)));
            session.save(mstItem);
        } catch (SessionExpiredException e) {
                throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }*/

    /*public static JSONObject deleteMasterItem(Session session, HttpServletRequest request) throws HibernateException{
        JSONObject jobj=new JSONObject();
        String ids[] = request.getParameterValues("ids");
        for (int i = 0; i < ids.length; i++) {
            MasterItem mdata=(MasterItem) session.get(MasterItem.class,ids[i] );
             session.delete(mdata);
        }
        return jobj;
    }*/

    /*public static JSONObject getMasterItems(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String query="from MasterItem me where masterGroup.ID=? and company.companyID=? order by me.value";
            List list = HibernateUtil.executeQuery(session, query,new Object[]{request.getParameter("groupid"), AuthHandler.getCompanyid(request)});
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                MasterItem item = (MasterItem) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", item.getID());
                obj.put("name", item.getValue());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }*/

   /* public static String getNextAutoNumber(Session session, CompanyAccountPreferences pref, int from) throws ServiceException, AccountingException{
        String autoNumber="";
        String table="", field="", pattern="";
        if(pref==null) return autoNumber;
        switch(from){
            case StaticValues.AUTONUM_JOURNALENTRY:
                table="JournalEntry";
                field="entryNumber";
                pattern=pref.getJournalEntryNumberFormat();
                break;
            case StaticValues.AUTONUM_SALESORDER:
                table="SalesOrder";
                field="salesOrderNumber";
                pattern=pref.getSalesOrderNumberFormat();
                break;
            case StaticValues.AUTONUM_INVOICE:
                table="Invoice";
                field="invoiceNumber";
                pattern=pref.getInvoiceNumberFormat();
                break;
            case StaticValues.AUTONUM_CASHSALE:
                table="Invoice";
                field="invoiceNumber";
                pattern=pref.getCashSaleNumberFormat();
                break;
            case StaticValues.AUTONUM_CREDITNOTE:
                table="CreditNote";
                field="creditNoteNumber";
                pattern=pref.getCreditNoteNumberFormat();
                break;
            case StaticValues.AUTONUM_RECEIPT:
                table="Receipt";
                field="receiptNumber";
                pattern=pref.getReceiptNumberFormat();
                break;
            case StaticValues.AUTONUM_PURCHASEORDER:
                table="PurchaseOrder";
                field="purchaseOrderNumber";
                pattern=pref.getPurchaseOrderNumberFormat();
                break;
            case StaticValues.AUTONUM_GOODSRECEIPT:
                table="GoodsReceipt";
                field="goodsReceiptNumber";
                pattern=pref.getGoodsReceiptNumberFormat();
                break;
            case StaticValues.AUTONUM_CASHPURCHASE:
                table="GoodsReceipt";
                field="goodsReceiptNumber";
                pattern=pref.getCashPurchaseNumberFormat();
                break;
            case StaticValues.AUTONUM_DEBITNOTE:
                table="DebitNote";
                field="debitNoteNumber";
                pattern=pref.getDebitNoteNumberFormat();
                break;
            case StaticValues.AUTONUM_PAYMENT:
                table="Payment";
                field="paymentNumber";
                pattern=pref.getPaymentNumberFormat();
                break;
            case StaticValues.AUTONUM_BILLINGINVOICE:
                table="BillingInvoice";
                field="billingInvoiceNumber";
                pattern=pref.getBillingInvoiceNumberFormat();
                break;
           case StaticValues.AUTONUM_BILLINGRECEIPT:
                table="BillingReceipt";
                field="billingReceiptNumber";
                pattern=pref.getBillingReceiptNumberFormat();
                break;
           case StaticValues.AUTONUM_BILLINGCASHSALE:
                table="BillingInvoice";
                field="billingInvoiceNumber";
                pattern=pref.getBillingCashSaleNumberFormat();
                break;
//  TODO         case StaticValues.AUTONUM_BILLINGCASHPURCHASE:
//                table="BillingInvoice";
//                field="billingInvoiceNumber";
//                pattern=pref.getBillingCashPurchaseNumberFormat();
//                break;
        }
        if(pattern==null) return autoNumber;
        String query="select max("+field+") from "+table+" where autoGenerated=true and "+field+" like ? and company.companyID=?";
        List list=HibernateUtil.executeQuery(session, query, new Object[]{pattern.replace('0', '_'), pref.getID()});
        if(list.isEmpty()==false)autoNumber=(String)list.get(0);

        while(!pattern.equals(autoNumber)){
            autoNumber=CompanyHandler.generateNextAutoNumber(pattern, autoNumber);
            query="select "+field+" from "+table+" where "+field+" = ? and company.companyID=?";
            list=HibernateUtil.executeQuery(session, query, new Object[]{autoNumber, pref.getID()});
            if(list.isEmpty()) return autoNumber;
        }
        throw new AccountingException("Auto number for the pattern '" + pattern + "' doesn't exist.<br>Please change the pattern or disable Auto generation.");

    }

    public static JSONObject deleteTemplate(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj = null;
        try {
            String tempid = request.getParameter("deleteflag");
            jobj = new JSONObject();
            PDFReportTemplate pdf_temp = (PDFReportTemplate) session.load(PDFReportTemplate.class, tempid);
            pdf_temp.setDeleted(true);
            session.save(pdf_temp);
            jobj.put("success",true);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("companyHandler.deleteReportTemplate", e);
        } catch (HibernateException e) {
            throw ServiceException.FAILURE("companyHandler.deleteReportTemplate", e);
        }
        return jobj;
    }

    public static JSONObject editTemplate(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj = null;
        try {
            String tempid = request.getParameter("edit");
            String newconfig = request.getParameter("data");
            jobj = new JSONObject();
            PDFReportTemplate pdf_temp = (PDFReportTemplate) session.load(PDFReportTemplate.class, tempid);
            pdf_temp.setConfiguration(newconfig);
            session.save(pdf_temp);
            jobj.put("success",true);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("companyHandler.editReportTemplate", e);
        } catch (HibernateException e) {
            throw ServiceException.FAILURE("companyHandler.editReportTemplate", e);
        }
        return jobj;
    }

    public static void deleteJournalEntries(Session session, HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        ArrayList params = new ArrayList();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String qMarks = "";
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("journalentryid"))) {
                    params.add(jobj.getString("journalentryid"));
                    qMarks += "?,";
                }
            }
            params.add(company.getCompanyID());
            qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
            String query;
            List list;

            query = "from BankReconciliationDetail where journalEntry.ID in( "+qMarks +") and bankReconciliation.deleted=false and company.companyID=?";
            list = HibernateUtil.executeQuery(session, query, params.toArray());
            if(!list.isEmpty())
                 throw new AccountingException("some of selected record(s) are currently associated with Bank reconciliation(s). delete <b>Bank reconciliation</b> instead.");

            query = "from BillingInvoice where journalEntry.ID in( "+qMarks +") and deleted=false and company.companyID=?";
            list = HibernateUtil.executeQuery(session, query, params.toArray());
            if(!list.isEmpty())
                 throw new AccountingException("some of selected record(s) are currently associated with Billing invoice(s). delete <b>Billing invoice</b> instead.");

            query = "from BillingReceipt where journalEntry.ID in( "+qMarks +") and deleted=false and company.companyID=?";
            list = HibernateUtil.executeQuery(session, query, params.toArray());
            if(!list.isEmpty())
                 throw new AccountingException("some of selected record(s) are currently associated with Billing receipt(s). delete <b>Billing receipt</b> instead.");

            query = "from CreditNote where journalEntry.ID in( "+qMarks +") and deleted=false and company.companyID=?";
            list = HibernateUtil.executeQuery(session, query, params.toArray());
            if(!list.isEmpty())
                 throw new AccountingException("some of selected record(s) are currently associated with Credit Note(s). delete <b>Credit Note</b> instead.");

            query = "from DebitNote where journalEntry.ID in( "+qMarks +") and deleted=false and company.companyID=?";
            list = HibernateUtil.executeQuery(session, query, params.toArray());
            if(!list.isEmpty())
                 throw new AccountingException("some of selected record(s) are currently associated with Debit Note(s). delete <b>Debit Note</b> instead.");

            query = "from DepreciationDetail where journalEntry.ID in( "+qMarks +") and company.companyID=?";
            list = HibernateUtil.executeQuery(session, query, params.toArray());
            if(!list.isEmpty())
                 throw new AccountingException("some of selected record(s) are currently associated with Depreciation Detail(s). delete <b>Depreciation Detail</b> instead.");

            query = "from Invoice where journalEntry.ID in( "+qMarks +") and deleted=false and company.companyID=?";
            list = HibernateUtil.executeQuery(session, query, params.toArray());
            if(!list.isEmpty())
                 throw new AccountingException("some of selected record(s) are currently associated with Invoice(s). delete <b>Invoice</b> instead.");

            query = "from GoodsReceipt where journalEntry.ID in( "+qMarks +") and deleted=false and company.companyID=?";
            list = HibernateUtil.executeQuery(session, query, params.toArray());
            if(!list.isEmpty())
                 throw new AccountingException("some of selected record(s) are currently associated with Goods Receipt(s). delete <b>Goods Receipt</b> instead.");

            query = "from Payment where journalEntry.ID in( "+qMarks +") and deleted=false and company.companyID=?";
            list = HibernateUtil.executeQuery(session, query, params.toArray());
            if(!list.isEmpty())
                 throw new AccountingException("some of selected record(s) are currently associated with Payment(s). delete <b>Payment</b> instead.");

            query = "from Receipt where journalEntry.ID in( "+qMarks +") and deleted=false and company.companyID=?";
            list = HibernateUtil.executeQuery(session, query, params.toArray());
            if(!list.isEmpty())
                 throw new AccountingException("some of selected record(s) are currently associated with Receipt(s). delete <b>Receipt</b> instead.");

            query = "update JournalEntry set deleted=true where ID in("+qMarks +") and company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
        } catch (AccountingException ex) {
            throw new AccountingException("Some of selected record(s) are currently associated with other transactions(s).<br>Delete <b>other transactions</b> instead.");
        } catch (JSONException ex) {
            throw new AccountingException("Cannot extract data from client");
        }
    }*/
}
