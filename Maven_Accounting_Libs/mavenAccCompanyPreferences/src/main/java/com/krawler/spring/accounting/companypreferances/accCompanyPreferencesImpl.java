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

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CompanyType;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.DefaultCompanyAccountPreferences;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.YearLock;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
public class accCompanyPreferencesImpl implements accCompanyPreferencesDAO {
    private HibernateTemplate hibernateTemplate;
    public void setSessionFactory(SessionFactory sessionFactory){
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public String getNextAutoNumber(String companyid, int from) throws ServiceException, AccountingException {
        String autoNumber="";
        String table="", field="", pattern="";
        CompanyAccountPreferences pref = (CompanyAccountPreferences) hibernateTemplate.get(CompanyAccountPreferences.class, companyid);
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
           case StaticValues.AUTONUM_BILLINGGOODSRECEIPT:
                table="BillingGoodsReceipt";
                field="billingGoodsReceiptNumber";
                pattern=pref.getBillingGoodsReceiptNumberFormat();
                break;
           case StaticValues.AUTONUM_BILLINGPAYMENT:
                table="BillingPayment";
                field="billingPaymentNumber";
                pattern=pref.getBillingPaymentNumberFormat();
                break;
           case StaticValues.AUTONUM_BILLINGCASHPURCHASE:
                table="BillingGoodsReceipt";
                field="billingGoodsReceiptNumber";
                pattern=pref.getBillingCashPurchaseNumberFormat();
                break;
           case StaticValues.AUTONUM_BILLINGPURCHASEORDER:
                table="BillingPurchaseOrder";
                field="purchaseOrderNumber";
                pattern=pref.getBillingPurchaseOrderNumberFormat();
                break;
           case StaticValues.AUTONUM_BILLINGSALESORDER:
                table="BillingSalesOrder";
                field="salesOrderNumber";
                pattern=pref.getBillingSalesOrderNumberFormat();
                break;
           case StaticValues.AUTONUM_BILLINGDEBITNOTE:
                table="BillingDebitNote";
                field="debitNoteNumber";
                pattern=pref.getBillingDebitNoteNumberFormat();
                break;
           case StaticValues.AUTONUM_BILLINGCREDITNOTE:
                table="BillingCreditNote";
                field="creditNoteNumber";
                pattern=pref.getBillingCreditNoteNumberFormat();
                break;
           case StaticValues.AUTONUM_QUOTATION:
               table="Quotation";
               field="quotationNumber";
               pattern=pref.getQuotationNumberFormat();
               break;     
        }
        if(pattern==null) return autoNumber;
        String query="select max("+field+") from "+table+" where autoGenerated=true and "+field+" like ? and company.companyID=?";
        List list=HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{pattern.replace('0', '_'), pref.getID()});
        if(list.isEmpty()==false)autoNumber=(String)list.get(0);

        while(!pattern.equals(autoNumber)){
            autoNumber = AccountingManager.generateNextAutoNumber(pattern, autoNumber);
            query="select "+field+" from "+table+" where "+field+" = ? and company.companyID=?";
            list=HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{autoNumber, pref.getID()});
            if(list.isEmpty()) return autoNumber;
        }
        throw new AccountingException("Auto number for the pattern '" + pattern + "' doesn't exist.<br>Please change the pattern or disable Auto generation.");

    }

    public KwlReturnObject getPreferencesFromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from CompanyAccountPreferences acp where (discountGiven.ID=? or discountReceived.ID=? or shippingCharges.ID=? or otherCharges.ID=? or cashAccount.ID=?) and acp.company.companyID=?";
        list = HibernateUtil.executeQuery(hibernateTemplate, q, new Object[]{accountid, accountid, accountid, accountid, accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCompanyPreferences(Map<String, Object> filterParams) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition = "";
        String query="from CompanyAccountPreferences ";

        if(filterParams.containsKey("id")){
            condition += (condition.length()==0?" where ":" and ") + "ID=?";
            params.add(filterParams.get("id"));
        }
        query += condition;
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
        }

    public KwlReturnObject addPreferences(HashMap<String, Object> prefMap) throws ServiceException {
        List list = new ArrayList();
        try {
            CompanyAccountPreferences preference = new CompanyAccountPreferences();
            preference = buildPreferences(preference, prefMap);
            hibernateTemplate.save(preference);
            list.add(preference);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addPreferences : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Company Account Preferences has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updatePreferences(HashMap<String, Object> prefMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = (String) prefMap.get("id");
            CompanyAccountPreferences preference = (CompanyAccountPreferences) hibernateTemplate.get(CompanyAccountPreferences.class, id);
            if (preference != null) {
                preference = buildPreferences(preference, prefMap);
                hibernateTemplate.saveOrUpdate(preference);
            }
            list.add(preference);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updatePreferences : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Company Account Preferences has been updated successfully", null, list, list.size());
    }

    public CompanyAccountPreferences buildPreferences(CompanyAccountPreferences preferences, HashMap<String, Object> prefMap) {
        if (prefMap.containsKey("fyfrom")) {
            preferences.setFinancialYearFrom((Date) prefMap.get("fyfrom"));
        }
        if (prefMap.containsKey("fyfrom")&&preferences.getFirstFinancialYearFrom()==null) {
            preferences.setFirstFinancialYearFrom((Date) prefMap.get("fyfrom"));
        }

        if (prefMap.containsKey("bbfrom")) {
            preferences.setBookBeginningFrom((Date) prefMap.get("bbfrom"));
        }

        String str;
        if (prefMap.containsKey("discountgiven")) {
            str = (String) prefMap.get("discountgiven");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setDiscountGiven((Account) hibernateTemplate.get(Account.class, str));
            } else {
                preferences.setDiscountGiven(null);
            }
        }

        if (prefMap.containsKey("discountreceived")) {
            str = (String) prefMap.get("discountreceived");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setDiscountReceived((Account) hibernateTemplate.get(Account.class, str));
            } else {
                preferences.setDiscountReceived(null);
            }
        }

//        if (prefMap.containsKey("shippingcharges")) {
//            str = (String) prefMap.get("shippingcharges");
//            if (!StringUtil.isNullOrEmpty(str)) {
//                preferences.setShippingCharges((Account) hibernateTemplate.get(Account.class, str));
//            } else {
//                preferences.setShippingCharges(null);
//            }
//        }

        if (prefMap.containsKey("othercharges")) {
            str = (String) prefMap.get("othercharges");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setOtherCharges((Account) hibernateTemplate.get(Account.class, str));
            } else {
                preferences.setOtherCharges(null);
            }
        }

        if (prefMap.containsKey("cashaccount")) {
            str = (String) prefMap.get("cashaccount");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setCashAccount((Account) hibernateTemplate.get(Account.class, str));
            } else {
                preferences.setCashAccount(null);
            }
        }
        if (prefMap.containsKey("foreignexchange")) {
            str = (String) prefMap.get("foreignexchange");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setForeignexchange((Account) hibernateTemplate.get(Account.class, str));
            } else {
                preferences.setForeignexchange(null);
            }
        }

        if (prefMap.containsKey("depreciationaccount")) {
            str = (String) prefMap.get("depreciationaccount");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setDepereciationAccount((Account) hibernateTemplate.get(Account.class, str));
            } else {
                preferences.setDepereciationAccount(null);
            }
        }

        if (prefMap.containsKey("autojournalentry")) {
            str = (String) prefMap.get("autojournalentry");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setJournalEntryNumberFormat(str);
            } else {
                preferences.setJournalEntryNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autoinvoice")) {
            str = (String) prefMap.get("autoinvoice");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setInvoiceNumberFormat(str);
            } else {
                preferences.setInvoiceNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autocreditmemo")) {
            str = (String) prefMap.get("autocreditmemo");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setCreditNoteNumberFormat(str);
            } else {
                preferences.setCreditNoteNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autoreceipt")) {
            str = (String) prefMap.get("autoreceipt");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setReceiptNumberFormat(str);
            } else {
                preferences.setReceiptNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autogoodsreceipt")) {
            str = (String) prefMap.get("autogoodsreceipt");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setGoodsReceiptNumberFormat(str);
            } else {
                preferences.setGoodsReceiptNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autodebitnote")) {
            str = (String) prefMap.get("autodebitnote");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setDebitNoteNumberFormat(str);
            } else {
                preferences.setDebitNoteNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autopayment")) {
            str = (String) prefMap.get("autopayment");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setPaymentNumberFormat(str);
            } else {
                preferences.setPaymentNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autoso")) {
            str = (String) prefMap.get("autoso");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setSalesOrderNumberFormat(str);
            } else {
                preferences.setSalesOrderNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autopo")) {
            str = (String) prefMap.get("autopo");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setPurchaseOrderNumberFormat(str);
            } else {
                preferences.setPurchaseOrderNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autocashsales")) {
            str = (String) prefMap.get("autocashsales");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setCashSaleNumberFormat(str);
            } else {
                preferences.setCashSaleNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autocashpurchase")) {
            str = (String) prefMap.get("autocashpurchase");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setCashPurchaseNumberFormat(str);
            } else {
                preferences.setCashPurchaseNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autobillinginvoice")) {
            str = (String) prefMap.get("autobillinginvoice");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setBillingInvoiceNumberFormat(str);
            } else {
                preferences.setBillingInvoiceNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autobillingreceipt")) {
            str = (String) prefMap.get("autobillingreceipt");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setBillingReceiptNumberFormat(str);
            } else {
                preferences.setBillingReceiptNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autobillingcashsales")) {
            str = (String) prefMap.get("autobillingcashsales");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setBillingCashSaleNumberFormat(str);
            } else {
                preferences.setBillingCashSaleNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autobillingcashpurchase")) {
            str = (String) prefMap.get("autobillingcashpurchase");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setBillingCashPurchaseNumberFormat(str);
            } else {
                preferences.setBillingCashPurchaseNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autobillinggoodsreceipt")) {
            str = (String) prefMap.get("autobillinggoodsreceipt");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setBillingGoodsReceiptNumberFormat(str);
            } else {
                preferences.setBillingGoodsReceiptNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autobillingpayment")) {
            str = (String) prefMap.get("autobillingpayment");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setBillingPaymentNumberFormat(str);
            } else {
                preferences.setBillingPaymentNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autobillingso")) {
            str = (String) prefMap.get("autobillingso");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setBillingSalesOrderNumberFormat(str);
            } else {
                preferences.setBillingSalesOrderNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autobillingpo")) {
            str = (String) prefMap.get("autobillingpo");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setBillingPurchaseOrderNumberFormat(str);
            } else {
                preferences.setBillingPurchaseOrderNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autobillingdebitnote")) {
            str = (String) prefMap.get("autobillingdebitnote");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setBillingDebitNoteNumberFormat(str);
            } else {
                preferences.setBillingDebitNoteNumberFormat(null);
            }
        }

        if (prefMap.containsKey("autobillingcreditmemo")) {
            str = (String) prefMap.get("autobillingcreditmemo");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setBillingCreditNoteNumberFormat(str);
            } else {
                preferences.setBillingCreditNoteNumberFormat(null);
            }
        }

        if (prefMap.containsKey("withoutinventory")) {
            preferences.setWithoutInventory((Boolean) prefMap.get("withoutinventory"));
        }

        if (prefMap.containsKey("withouttax1099")) {
            preferences.setWithoutTax1099((Boolean) prefMap.get("withouttax1099"));
        }

        if (prefMap.containsKey("emailinvoice")) {
            preferences.setEmailInvoice((Boolean) prefMap.get("emailinvoice"));
        }

        if (prefMap.containsKey("setupdone")) {
            preferences.setSetupDone((Boolean) prefMap.get("setupdone"));
        }

        if (prefMap.containsKey("companytype")) {
            str = (String) prefMap.get("companytype");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setCompanyType((CompanyType) hibernateTemplate.get(CompanyType.class, str));
            } else {
                preferences.setCompanyType(null);
            }
        }

        if (prefMap.containsKey("companyid")) {
            str = (String) prefMap.get("companyid");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setCompany((Company) hibernateTemplate.get(Company.class, str));
            } else {
                preferences.setCompany(null);
            }
        }
        
        if (prefMap.containsKey("autoquotation")) {
            str = (String) prefMap.get("autoquotation");
            if (!StringUtil.isNullOrEmpty(str)) {
                preferences.setQuotationNumberFormat(str);
            } else {
                preferences.setQuotationNumberFormat(null);
            }
        }
        if (prefMap.containsKey("currencyChange")) {
            preferences.setCurrencyChange((Boolean) prefMap.get("currencyChange"));
        }
        if (prefMap.containsKey("countryChange")) {
            preferences.setCountryChange((Boolean) prefMap.get("countryChange"));
        }
        return preferences;
    }


    public KwlReturnObject addYearLock(HashMap<String, Object> yearLockMap) throws ServiceException {
        List list = new ArrayList();
        try {
            YearLock yearlock = new YearLock();
            yearlock.setDeleted(false);
            yearlock = builYearLock(yearlock, yearLockMap);
            hibernateTemplate.save(yearlock);
            list.add(yearlock);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addYearLock : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Year Lock has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateYearLock(HashMap<String, Object> yearLockMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = (String) yearLockMap.get("id");
            YearLock yearlock = (YearLock) hibernateTemplate.get(YearLock.class, id);
            if (yearlock != null) {
                yearlock = builYearLock(yearlock, yearLockMap);
                hibernateTemplate.saveOrUpdate(yearlock);
            }
            list.add(yearlock);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateYearLock : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Year Lock has been updated successfully", null, list, list.size());
    }

    public YearLock builYearLock(YearLock yearlock, HashMap<String, Object> yearLockMap) {
        if (yearLockMap.containsKey("islock")) {
            yearlock.setIsLock((Boolean) yearLockMap.get("islock"));
        }

        if (yearLockMap.containsKey("yearid")) {
            yearlock.setYearid((Integer) yearLockMap.get("yearid"));
        }

        if (yearLockMap.containsKey("companyid")) {
            String str = (String) yearLockMap.get("companyid");
            if (!StringUtil.isNullOrEmpty(str)) {
                yearlock.setCompany((Company) hibernateTemplate.get(Company.class, str));
            } else {
                yearlock.setCompany(null);
            }
        }
        return yearlock;
    }
    
    public KwlReturnObject getYearLock(Map<String, Object> filterParams) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition = "";
        String query="from YearLock y where y.deleted=false ";

        if(filterParams.containsKey("companyid")){
            condition += " and y.company.companyID=? order by y.yearid desc";
            params.add(filterParams.get("companyid"));
        }
        query += condition;
//        String query="from YearLock where deleted=false and company.companyID=?";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getYearLockforPreferences(Map<String, Object> filterParams) throws ServiceException {
		try{
			List returnList = new ArrayList();
			ArrayList params = new ArrayList();
			String query="from YearLock y where y.yearid <? and y.company.companyID=? order by y.yearid desc";
			params.add(Integer.parseInt((String) filterParams.get("CurrentFinancialYear")));
			params.add(filterParams.get("companyid"));
			
			returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
			return new KwlReturnObject(true, "", null, returnList, returnList.size());
		}catch(Exception ex){
			throw ServiceException.FAILURE("getYearLockforPreferences : "+ex.getMessage(), ex);
		}
    }

    public void setAccountPreferences(String companyid, HashMap hm, Date curDate) throws ServiceException {
        try {
            String query = "from DefaultCompanyAccountPreferences order by ID";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query);
            Iterator iter = list.iterator();
            Company company = (Company) hibernateTemplate.get(Company.class, companyid);
            while (iter.hasNext()) {
            	DefaultCompanyAccountPreferences dcap = (DefaultCompanyAccountPreferences) iter.next();
                CompanyAccountPreferences cap = new CompanyAccountPreferences();
                cap.setBookBeginningFrom(curDate);
                cap.setFinancialYearFrom(curDate);
                cap.setCompany(company);
                cap.setCashAccount((Account) hm.get(dcap.getCashAccount()));
                cap.setDiscountGiven((Account) hm.get(dcap.getDiscountGiven()));
                cap.setDiscountReceived((Account) hm.get(dcap.getDiscountReceived()));
//                cap.setShippingCharges((Account) hm.get(dcap.getShippingCharges()));
                cap.setOtherCharges((Account) hm.get(dcap.getOtherCharges()));
                cap.setForeignexchange((Account) hm.get(dcap.getForeignExchange()));
                cap.setDepereciationAccount((Account) hm.get(dcap.getDepereciationAccount()));
                hibernateTemplate.save(cap);
                break;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("setAccountPreferences : "+ex.getMessage(), ex);
        }
    }
    
	@Override
	public void setNewYear(Date time, String companyid) throws ServiceException {
		// TODO Auto-generated method stub
		try{
			String query = "Update CompanyAccountPreferences set financialYearFrom=? where company.companyID=?";
			HibernateUtil.executeUpdate(hibernateTemplate, query, new Object[]{time,companyid});
		}catch(Exception ex){
			throw ServiceException.FAILURE("setNewYear : "+ex.getMessage(), ex);
		}
	}
	
	@Override
	public void setCurrentYear(int presentYear, int previousYear, String companyid) throws ServiceException {
		// TODO Auto-generated method stub
		try{
			String query = "from YearLock y where y.company.companyID=? and y.yearid=?";
			List resultList = HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{companyid,presentYear});
			Company company = (Company)hibernateTemplate.get(Company.class, companyid);
			YearLock yearLock = new YearLock();
			if(resultList.size() == 0){
				
				yearLock.setCompany(company);
				yearLock.setDeleted(true);
				yearLock.setIsLock(false);
				yearLock.setYearid(presentYear);
				
				hibernateTemplate.saveOrUpdate(yearLock);
				
				String query1 = "Update YearLock y set y.deleted=false, y.isLock=false where y.company.companyID=? and y.yearid=?";
				HibernateUtil.executeUpdate(hibernateTemplate, query1, new Object[]{companyid,previousYear});
			}
			else{
				String query1 = "Update YearLock y set y.deleted=false,y.isLock=false where y.company.companyID=? and y.yearid=?";
				HibernateUtil.executeUpdate(hibernateTemplate, query1, new Object[]{companyid,(presentYear+1)});

				String query2 = "Update YearLock y set y.deleted=true,y.isLock=false where y.company.companyID=? and y.yearid=?";
				HibernateUtil.executeUpdate(hibernateTemplate, query1, new Object[]{companyid,presentYear});
				
				String query3 = "Update YearLock y set y.deleted=false,y.isLock=false where y.company.companyID=? and y.yearid=?";
				HibernateUtil.executeUpdate(hibernateTemplate, query2, new Object[]{companyid,previousYear});
			}
		}catch(Exception ex){
			throw ServiceException.FAILURE("setCurrentYear : "+ex.getMessage(), ex);
		}
	}
	

}

