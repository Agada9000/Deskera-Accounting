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

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.database.AccountDBCon;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Card;
import com.krawler.hql.accounting.Cheque;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.DebitNote;
import com.krawler.hql.accounting.DebitNoteDetail;
import com.krawler.hql.accounting.DebitNoteDiscount;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.ExchangeRateDetails;
import com.krawler.hql.accounting.Group;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.GoodsReceiptDetail;
import com.krawler.hql.accounting.PayDetail;
import com.krawler.hql.accounting.PaymentDetail;
import com.krawler.hql.accounting.PaymentMethod;
import com.krawler.hql.accounting.PurchaseOrder;
import com.krawler.hql.accounting.PurchaseOrderDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.Term;
import com.krawler.hql.accounting.Vendor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;

public class VendorHandler {

    /*public static String saveVendor(Session session, HttpServletRequest request) throws ServiceException, HibernateException, ParseException {
        try {
            Vendor vendor;
            Company company=(Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            Account account;
            Account parentAccount = null;
            boolean debitType = CompanyHandler.getBoolean(request,"debitType");
            double openBalance=CompanyHandler.getDouble(request, "openbalance");
            double life=CompanyHandler.getDouble(request, "life");
            double salvage=CompanyHandler.getDouble(request, "salvage");
             Date creationDate = AuthHandler.getDateFormatter(request).parse(request.getParameter("creationDate"));
             if(creationDate==null)
                creationDate=new Date();
            String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
            String vendorid = request.getParameter("accid");
            String parentid = request.getParameter("parentid");
            boolean issub = request.getParameter("issub")!=null;
            String auditMsg = " added new vendor ";
            String auditID = AuditAction.VENDOR_ADDED;
            if (issub&&!StringUtil.isNullOrEmpty(parentid)) {
                parentAccount = (Account) session.get(Account.class, parentid);
            }
            Group group = (Group) session.get(Group.class, Group.ACCOUNTS_PAYABLE);
            if (StringUtil.isNullOrEmpty(vendorid)) {
                vendor = new Vendor();
                vendor.setCreatedOn(new Date());
                openBalance=debitType?openBalance:-openBalance;
                account = CompanyHandler.makeAccount(session, company.getCompanyID(), null, request.getParameter("accname"), openBalance, parentAccount, group,currencyid,life,salvage,creationDate);
                vendor.setAccount(account);
            } else {
                vendor = (Vendor) session.get(Vendor.class, vendorid);
                vendor.setModifiedOn(new Date());
                account = vendor.getAccount();
                account.setName(request.getParameter("accname"));
                account.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
                openBalance=debitType?openBalance:-openBalance;
                account.setOpeningBalance(openBalance);
                account.setParent(parentAccount);
                session.update(account);
                auditMsg = " updated vendor ";
                auditID = AuditAction.VENDOR_UPDATED;
            }
            vendor.setTitle(request.getParameter("title"));
            vendor.setName(request.getParameter("accname"));
            vendor.setAddress(request.getParameter("address"));
            vendor.setEmail(request.getParameter("email"));
            vendor.setContactNumber(request.getParameter("contactno"));
            vendor.setAltContactNumber(request.getParameter("contactno2"));
            vendor.setBankaccountno(request.getParameter("bankaccountno"));
            vendor.setFax(request.getParameter("fax"));
            vendor.setOther(request.getParameter("other"));
            vendor.setDebitTerm((Term) session.get(Term.class, request.getParameter("termid")));
            vendor.setPreferedDeliveryMode(StaticValues.PDM_EMAIL);
//            if (Integer.parseInt(request.getParameter("pdm")) == StaticValues.PDM_PRINT) {
//                vendor.setPreferedDeliveryMode(StaticValues.PDM_PRINT);
//
//            }
            vendor.setCompany(company);
            session.saveOrUpdate(vendor);
            ProfileHandler.insertAuditLog(session, auditID, "User " + AuthHandler.getFullName(session, AuthHandler.getUserid(request)) + auditMsg + vendor.getName(), request);
            return vendor.getAccount().getID();
        } catch (NumberFormatException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }*/

    /*public static JSONObject getVendors(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException, AccountingException {
        JSONObject jobj = new JSONObject();
        try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            jobj = CompanyHandler.getAccounts(session, request);
            JSONArray jArr = jobj.getJSONArray("data");
            JSONArray finalJArr=new JSONArray();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);
                Vendor vendor = (Vendor) session.get(Vendor.class, obj.getString("accid"));
                if(vendor==null) continue;
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
                obj.put("creationDate", AuthHandler.getDateFormatter(request).format(vendor.getAccount().getCreationDate()));
                obj.put("currencyid",(vendor.getAccount().getCurrency()==null?currency.getCurrencyID(): vendor.getAccount().getCurrency().getCurrencyID()));
                obj.put("currencysymbol",(vendor.getAccount().getCurrency()==null?currency.getCurrencyID(): vendor.getAccount().getCurrency().getSymbol()));
                obj.put("currencyname",(vendor.getAccount().getCurrency()==null?currency.getName(): vendor.getAccount().getCurrency().getName()));
                 finalJArr.put(obj);
            }
            jobj.put("data", finalJArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }*/

    /*public static JSONObject getAddress(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            Vendor vendor = (Vendor) session.get(Vendor.class, request.getParameter("customerid"));
            jobj.put("shipingAddress", vendor.getAddress());
            jobj.put("billingAddress", vendor.getAddress());
            jobj.put("currencyid",(vendor.getAccount().getCurrency()==null?currency.getCurrencyID(): vendor.getAccount().getCurrency().getCurrencyID()));
            jobj.put("currencysymbol",(vendor.getAccount().getCurrency()==null?currency.getSymbol(): vendor.getAccount().getCurrency().getSymbol()));
            jobj.put("currencyname",(vendor.getAccount().getCurrency()==null?currency.getName(): vendor.getAccount().getCurrency().getName()));
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }*/

    /*public static void saveGoodsReceipt(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException {
        try {
             String taxid=null;
            taxid=request.getParameter("taxid");
            double taxamount=CompanyHandler.getDouble(request, "taxamount");
            Discount discount = null;
            double discValue = 0.0;
            double shippingCharges = CompanyHandler.getDouble(request, "shipping");
            double otherCharges = CompanyHandler.getDouble(request, "othercharges");
            boolean inCash=Boolean.parseBoolean(request.getParameter("incash"));
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
            GoodsReceipt gReceipt = new GoodsReceipt();
            //Changes  Made for goods invoice number.
//            String vendorInvoiceNumber = request.getParameter("vendorinvoice");
//            String vendorinvoiceQuery = "from GoodsReceipt where vendorInvoiceNumber=? and company.companyID=?";
//            if (!HibernateUtil.executeQuery(session, vendorinvoiceQuery, new Object[]{vendorInvoiceNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                if(inCash)
//                    throw new AccountingException("Purchase receipt number '" + vendorInvoiceNumber + "' already exists.");
//                else
//                    throw new AccountingException("Goods receipt number '" + vendorInvoiceNumber + "' already exists.");
            String entryNumber=request.getParameter("number");
            String q="from GoodsReceipt where goodsReceiptNumber=? and company.companyID=?";
            if (!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
                if(inCash)
                    throw new AccountingException("Purchase receipt number '" + entryNumber + "' already exists.");
                else
                    throw new AccountingException("Goods receipt number '" + entryNumber + "' already exists.");
            gReceipt.setGoodsReceiptNumber(entryNumber);
            //Changes  Made for goods invoice number.
//            gReceipt.setVendorInvoiceNumber(vendorInvoiceNumber);
            int from=StaticValues.AUTONUM_GOODSRECEIPT;
            if(inCash)from=StaticValues.AUTONUM_CASHPURCHASE;
            gReceipt.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, from).equals(entryNumber));
            gReceipt.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
            gReceipt.setMemo(request.getParameter("memo"));
            gReceipt.setBillFrom(request.getParameter("billto"));
            gReceipt.setShipFrom(request.getParameter("shipaddress"));
            gReceipt.setShipDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("shipdate")));
            gReceipt.setDueDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("duedate")));
            gReceipt.setCompany(company);
            HashSet hs = new HashSet();
            double[] totals = saveGoodsReceiptRows(session, request, gReceipt, company, hs,currency);
            double disc = CompanyHandler.getDouble(request, "discount");
            if (disc > 0) {
                discount = new Discount();
                discount.setDiscount(disc);
                discount.setInPercent(Boolean.parseBoolean(request.getParameter("perdiscount")));
                discount.setOriginalAmount(totals[1] - totals[0]);
                discount.setCompany(company);
                gReceipt.setDiscount(discount);
                session.save(discount);
                discValue = discount.getDiscountValue();
            }
            discValue += totals[0];
            JournalEntryDetail jed = new JournalEntryDetail();
            jed.setCompany(company);
            jed.setAmount(totals[1] + shippingCharges + otherCharges - discValue+taxamount);
            if (Boolean.parseBoolean(request.getParameter("incash")) == false) 
                jed.setAccount((Account) session.get(Account.class, request.getParameter("vendor")));
            else 
                jed.setAccount(preferences.getCashAccount());            
            jed.setDebit(false);
            hs.add(jed);
            gReceipt.setVendorEntry(jed);

            if (discValue > 0) {
                jed = new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(discValue);
                jed.setAccount(preferences.getDiscountReceived());
                jed.setDebit(false);
                hs.add(jed);
            }
            if (shippingCharges > 0) {
                jed = new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(shippingCharges);
                jed.setAccount(preferences.getShippingCharges());
                jed.setDebit(true);
                hs.add(jed);
                gReceipt.setShipEntry(jed);
            }
            if (otherCharges > 0) {
                jed = new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(otherCharges);
                jed.setAccount(preferences.getOtherCharges());
                jed.setDebit(true);
                hs.add(jed);
                gReceipt.setOtherEntry(jed);
            }
            if(taxid!=null){
            Tax tax=  (Tax)session.get(Tax.class, taxid);
            if(tax==null&&!taxid.isEmpty())
                    throw new  AccountingException("The Tax code(s) used in this transaction has been deleted.");
                gReceipt.setTax(tax);
                if(taxamount>0){
                    jed=new JournalEntryDetail();
                    jed.setCompany(company);
                    jed.setAmount(taxamount);
                    jed.setAccount(tax.getAccount());
                    jed.setDebit(true);
                    hs.add(jed);
                    gReceipt.setTaxEntry(jed);
                }
            }
            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),
                    request.getParameter("memo"), "JE" + gReceipt.getGoodsReceiptNumber(),currencyid, hs,request);
            gReceipt.setJournalEntry(journalEntry);
            ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),null);
            gReceipt.setExchangeRateDetail(erd);
            session.saveOrUpdate(gReceipt);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }*/

    /*private static double[] saveGoodsReceiptRows(Session session, HttpServletRequest request, GoodsReceipt gReceipt, Company company, HashSet jeDetails,KWLCurrency currency) throws JSONException, ServiceException, HibernateException, AccountingException, SessionExpiredException, ParseException {
        HashSet hs = new HashSet();
        HashSet rows = new HashSet();
        double totaldiscount = 0;
        double totalamount = 0;
        String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
        JSONArray jArr = new JSONArray(request.getParameter("detail"));
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            GoodsReceiptDetail row = new GoodsReceiptDetail();
            PurchaseOrderDetail rd = (PurchaseOrderDetail) session.get(PurchaseOrderDetail.class, jobj.getString("rowid"));
            row.setCompany(company);
            row.setGoodsReceipt(gReceipt);
              row.setPurchaseorderdetail(rd);
            row.setRate(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("rate"),currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate"))));
            Product product = (Product) session.get(Product.class, jobj.getString("productid"));
            Inventory inventory = CompanyHandler.makeInventory(session, request, product, jobj.getInt("quantity"), jobj.optString("desc"), true, false);
            row.setInventory(inventory);
            totalamount += row.getRate() * inventory.getQuantity();
            Discount discount = null;
            double disc = jobj.getDouble("prdiscount");
            if (disc != 0.0) {
                discount = new Discount();
                discount.setDiscount(disc);
                discount.setOriginalAmount(row.getRate() * row.getInventory().getQuantity());
                discount.setInPercent(true);
                discount.setCompany(company);
                row.setDiscount(discount);
                session.save(discount);
                totaldiscount += discount.getDiscountValue();
            }
            JournalEntryDetail jed;
            if (hs.add(product.getPurchaseAccount())) {
                jed = new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAccount(product.getPurchaseAccount());
                jed.setAmount(row.getRate() * inventory.getQuantity());
                jed.setDebit(true);
                jeDetails.add(jed);
            } else {
                Iterator itr = jeDetails.iterator();
                while (itr.hasNext()) {
                    jed = (JournalEntryDetail) itr.next();
                    if (jed.getAccount() == product.getPurchaseAccount()) {
                        jed.setAmount(jed.getAmount() + row.getRate() * inventory.getQuantity());
                        break;
                    }
                }
            }
            rows.add(row);
        }
        gReceipt.setRows(rows);
        return new double[]{totaldiscount, totalamount};
    }*/

    /*public static void deleteGoodsReceipt(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException, JSONException, SessionExpiredException {
        ArrayList params = new ArrayList();
        JSONArray jArr = new JSONArray(request.getParameter("data"));
        String qMarks = "";
        Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
        qMarks = "";
        String cashAccount = ((CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request))).getCashAccount().getID();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            if (jobj.getString("personid").equals(cashAccount)) {
                throw new AccountingException("Payment against the selected Purchase Receipt(s) has been given. So, it cannot be deleted");
            }
            params.add(jobj.getString("billid"));
            qMarks += "?,";
        }
        qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
        params.add(company.getCompanyID());
        String query;
        List list;
        query = "from DebitNoteDetail dn  where dn.goodsReceiptRow.goodsReceipt.ID in ( "+qMarks +")  and dn.debitNote.deleted=false and dn.company.companyID=?";
        list = HibernateUtil.executeQuery(session, query, params.toArray());
        if (!list.isEmpty()) {
            throw new AccountingException("Selected record(s) are currently used in Debit Note(s).So it cannot be deleted.");
        }
        query = "from PaymentDetail pd  where pd.goodsReceipt.ID in ( "+qMarks +")  and pd.payment.deleted=false and pd.company.companyID=?";
        list = HibernateUtil.executeQuery(session, query, params.toArray());
        if (!list.isEmpty()) {
            throw new AccountingException("Payment against the selected Goods Receipt(s) has been partially/fully given. So, it cannot be deleted.");
        }
        query = "update GoodsReceipt gr set gr.deleted=true where gr.ID in( " + qMarks + ") and gr.company.companyID=?";
        HibernateUtil.executeUpdate(session, query, params.toArray());
        query = "update JournalEntry je set je.deleted=true  where je.ID in(select gr.journalEntry.ID from GoodsReceipt gr where gr.ID in( " + qMarks + ") and gr.company.companyID=je.company.companyID) and je.company.companyID=?";
        HibernateUtil.executeUpdate(session, query, params.toArray());
        query = "update Discount di set di.deleted=true  where di.ID in(select gr.discount.ID from GoodsReceipt gr where gr.ID in( " + qMarks + ") and gr.company.companyID=di.company.companyID) and di.company.companyID=?";
        HibernateUtil.executeUpdate(session, query, params.toArray());
        query = "update Discount di set di.deleted=true  where di.ID in(select grd.discount.ID from GoodsReceiptDetail grd where grd.goodsReceipt.ID in( " + qMarks + ") and grd.company.companyID=di.company.companyID) and di.company.companyID=?";
        HibernateUtil.executeUpdate(session, query, params.toArray());
        query = "update Inventory inv set inv.deleted=true  where inv.ID in(select grd.inventory.ID from GoodsReceiptDetail grd where grd.goodsReceipt.ID in( " + qMarks + ") and grd.company.companyID=inv.company.companyID) and inv.company.companyID=?";
        HibernateUtil.executeUpdate(session, query, params.toArray());
    }*/

    public static JSONObject getGoodsReceipts(Session session, HttpServletRequest request, String start, String limit,String venid) throws ServiceException, HibernateException {
        JSONObject jobj = getGoodsReceipts(session, request,venid);
        if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
            try {
                int st = Integer.parseInt(start);
                JSONArray jArr = new JSONArray(), temp = jobj.getJSONArray("data");
                int ed = Math.min(temp.length(), st + Integer.parseInt(limit));
                for (int i = st; i < ed; i++) 
                    jArr.put(temp.getJSONObject(i));      
                jobj.put("data", jArr);
                jobj.put("count", temp.length());
            } catch (NumberFormatException ne) {
                throw ServiceException.FAILURE("CompanyHandler.getGoodsReceipts", ne);
            } catch (JSONException jse) {
                throw ServiceException.FAILURE("CompanyHandler.getGoodsReceipts", jse);
            }
        }
        return jobj;
    }

      /*  public static JSONArray getVendorAgedPayable(Session session,HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            Date curDate = AuthHandler.getDateFormatter(request).parse(request.getParameter("curdate"));
            int duration=Integer.parseInt(request.getParameter("duration"));
            boolean isdistibutive=CompanyHandler.getBoolean(request, "isdistributive");
            String customerid = request.getParameter("accid");
            double amountdue1=0;
            double amountdue2=0;
            double amountdue3=0;
            double amountdue4=0;
            session = HibernateUtil.getCurrentSession();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            cal1.setTime(curDate);
            cal2.setTime(curDate);
            cal3.setTime(curDate);
            cal2.add(Calendar.DAY_OF_YEAR,-duration);
            cal3.add(Calendar.DAY_OF_YEAR,-(duration*2));
             ArrayList params = new ArrayList();
            String condition = "";
            params.add(AuthHandler.getCompanyid(request));
             if (!StringUtil.isNullOrEmpty(customerid)) {
                params.add(customerid);
                condition += " and v.account.ID=? ";
             }
            String q = "select ID from Vendor v where company.companyID= ?"+condition;
            Iterator itrcust = HibernateUtil.executeQuery(session, q,params.toArray()).iterator();
            while (itrcust.hasNext()) {
                amountdue1=amountdue2=amountdue3=amountdue4=0;
                JSONObject invJObj = new JSONObject();
                String personID=null;
                String personName=null;
                Object venid=itrcust.next();
                    invJObj = VendorHandler.getGoodsReceipts(session, request, null, null,venid.toString());
                 JSONArray invjarr = invJObj.getJSONArray("data");
                for (int i = 0; i < invjarr.length(); i++) {
                   JSONObject invobj = invjarr.getJSONObject(i);
                   personID=invobj.getString("personid");
                   personName=invobj.getString("personname");
                    Date dueDate=AuthHandler.getDateFormatter(request).parse(invobj.getString("duedate"));

                    if(isdistibutive){
                        if(dueDate.after(cal1.getTime())||dueDate.equals(cal1.getTime()))
                            amountdue1+=invobj.getDouble("amountdueinbase");
                        else if((cal2.getTime().before(dueDate)||cal2.getTime().equals(dueDate))&&cal1.getTime().after(dueDate))
                            amountdue2+=invobj.getDouble("amountdueinbase");
                        else if((cal3.getTime().before(dueDate)||cal3.getTime().equals(dueDate))&&cal2.getTime().after(dueDate))
                            amountdue3+=invobj.getDouble("amountdueinbase");
                        else
                            amountdue4+=invobj.getDouble("amountdueinbase");
                    }
                    else{
                        if(dueDate.after(cal1.getTime())||dueDate.equals(cal1.getTime()))
                            amountdue1+=invobj.getDouble("amountdueinbase");
                         if(dueDate.after(cal2.getTime())||dueDate.equals(cal2.getTime()))
                            amountdue2+=invobj.getDouble("amountdueinbase");
                         if(dueDate.after(cal3.getTime())||dueDate.equals(cal3.getTime()))
                            amountdue3+=invobj.getDouble("amountdueinbase");
                            amountdue4+=invobj.getDouble("amountdueinbase");
                    }
                }
                if(invjarr.length()>0){
                    jObj = new JSONObject();
                    jObj.put("personid", personID);
                    jObj.put("personname", personName);
                    jObj.put("amountdue1", amountdue1);
                    jObj.put("amountdue2", amountdue2);
                    jObj.put("amountdue3", amountdue3);
                    jObj.put("amountdue4", amountdue4);
                    jObj.put("amountdue4", amountdue4);
                    jObj.put("total", amountdue1+amountdue2+amountdue3+amountdue4);
                    jArr.put(jObj);
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jArr;
    }*/

   /*public static JSONObject getVendorAgedPayable(Session session, HttpServletRequest request, String start, String limit) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONArray invJArr = new JSONArray();
        JSONObject jObj = new JSONObject();
        try {
             invJArr= VendorHandler.getVendorAgedPayable(session, request);
             if(!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)){
                int st = Integer.parseInt(start);
                JSONArray jArr = new JSONArray(), temp = invJArr;
                int ed = Math.min(temp.length(),st+Integer.parseInt(limit));
                for (int i = st; i < ed; i++)
                    jArr.put(temp.getJSONObject(i));
                jObj.put("data", jArr);
                jObj.put("count", temp.length());
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jObj;
    }*/

    public static JSONObject getGoodsReceipts(Session session, HttpServletRequest request, String venid) throws ServiceException, HibernateException {
        JSONObject jobj = new JSONObject();
        try {
            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            Calendar startFinYearCal = Calendar.getInstance();
            Calendar endFinYearCal = Calendar.getInstance();
            startFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.add(Calendar.YEAR,1);
            String customerid = venid==null?request.getParameter("accid"):venid;
            String ss=request.getParameter("ss");
            String cashAccount=((CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request))).getCashAccount().getID();
            boolean cashonly=false;
            boolean creditonly=false;
            boolean personGroup=false;
             boolean isagedgraph=false;
            double taxPercent=0;
            String group="";
            cashonly =Boolean.parseBoolean(request.getParameter("cashonly"));
            creditonly =Boolean.parseBoolean(request.getParameter("creditonly"));
            if(cashonly)
                customerid=cashAccount;
            boolean ignoreZero = request.getParameter("ignorezero") != null;
            personGroup=Boolean.parseBoolean(request.getParameter("persongroup"));
             isagedgraph=Boolean.parseBoolean(request.getParameter("isagedgraph"));
            String dueDate = request.getParameter("curdate");
             ArrayList params = new ArrayList();
            String condition = "";
            params.add(AuthHandler.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(customerid)) {
                params.add(customerid);
                condition += " and gr.vendorEntry.account.ID=?";
            } else {
                String q = "select ID from Vendor where company.companyID=?";
                Iterator itrcust = HibernateUtil.executeQuery(session, q, AuthHandler.getCompanyid(request)).iterator();
                String qMarks = "";
                if(!creditonly){
                    qMarks = "?,";
                    params.add(cashAccount);
                }
                while (itrcust.hasNext()) {
                    qMarks += "?,";
                    params.add(itrcust.next());
                }
                qMarks=qMarks.substring(0,Math.max(0, qMarks.length()-1));
                if(!StringUtil.isNullOrEmpty(qMarks))
                    condition += " and gr.vendorEntry.account.ID in (" + qMarks + ")";
            }

            if (!StringUtil.isNullOrEmpty(dueDate)) {
                params.add(AuthHandler.getDateFormatter(request).parse(dueDate));
                condition += " and gr.dueDate<=?";
            }
            if(StringUtil.isNullOrEmpty(ss)==false){
               for(int i=0;i<=4;i++){
                 params.add(ss+"%");
               }
                 condition+= " and (gr.goodsReceiptNumber like ? or gr.billFrom like ?  or gr.journalEntry.entryNumber like ? or gr.memo like ? or gr.vendorEntry.account.name like ? ) ";
            }
            if(personGroup){
                params.add(startFinYearCal.getTime());
                params.add(endFinYearCal.getTime());
                condition+=" and gr.dueDate>=? and gr.dueDate<=?";
            }
             if(isagedgraph){
                 params.add(startFinYearCal.getTime());
                 params.add(endFinYearCal.getTime());
                 condition+=" and gr.dueDate>=? and gr.dueDate<=?";
            }
            String query = "from GoodsReceipt gr where gr.deleted=false and gr.company.companyID=? " + condition + group+" order by gr.ID";
            List list = HibernateUtil.executeQuery(session, query, params.toArray());
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                GoodsReceipt gReceipt = (GoodsReceipt) itr.next();
                JournalEntry je = gReceipt.getJournalEntry();
                JournalEntryDetail d = gReceipt.getVendorEntry();
                Account account = d.getAccount();
                KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
                String currencyid=(gReceipt.getCurrency()==null?currency.getCurrencyID(): gReceipt.getCurrency().getCurrencyID());
                double amount = 0, ramount = 0;
                Iterator itrCn = applyDebitNotes(session,request, gReceipt).values().iterator();
                while (itrCn.hasNext()) {
                    Object[] temp = (Object[]) itrCn.next();
                    amount += (Double) temp[0]-(Double) temp[2];
                }
                JournalEntryDetail tempd = gReceipt.getShipEntry();
                if (tempd != null) 
                    amount += tempd.getAmount();                
                tempd = gReceipt.getOtherEntry();
                if (tempd != null) 
                    amount += tempd.getAmount();               
                tempd = gReceipt.getTaxEntry();
                if (tempd != null)
                    amount += tempd.getAmount();             
                String q = "select pd from PaymentDetail pd where pd.payment.deleted=false and pd.goodsReceipt.ID=?";
                List l = HibernateUtil.executeQuery(session, q, gReceipt.getID());
                Iterator recitr = l.iterator();
                while (recitr.hasNext()) {
                    PaymentDetail pd=(PaymentDetail) recitr.next();
                    ramount+=pd.getAmount();
                    String fromcurrencyid=(pd.getPayment().getCurrency()==null?currency.getCurrencyID(): pd.getPayment().getCurrency().getCurrencyID());
                    ramount=CompanyHandler.getOneCurrencyToOther(session,request,ramount,fromcurrencyid,currencyid,gReceipt.getJournalEntry().getEntryDate());
                }
                JSONObject obj = new JSONObject();
                obj.put("billid", gReceipt.getID());
                obj.put("personid", account.getID());
                obj.put("billno", gReceipt.getGoodsReceiptNumber());
                obj.put("currencyid",currencyid);
                obj.put("currencysymbol",(gReceipt.getCurrency()==null?currency.getSymbol(): gReceipt.getCurrency().getSymbol()));
                obj.put("companyaddress", gReceipt.getCompany().getAddress());
                obj.put("companyname", gReceipt.getCompany().getCompanyName());
                obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,je.getEntryDate()));
                obj.put("billto", gReceipt.getBillFrom());
                obj.put("shipto", gReceipt.getShipFrom());
//                obj.put("vendorinvoice",gReceipt.getVendorInvoiceNumber());
                obj.put("journalentryid", je.getID());
                obj.put("entryno", je.getEntryNumber());
                obj.put("date", AuthHandler.getDateFormatter(request).format(je.getEntryDate()));
                obj.put("shipdate", AuthHandler.getDateFormatter(request).format(gReceipt.getShipDate()));
                obj.put("duedate", AuthHandler.getDateFormatter(request).format(gReceipt.getDueDate()));
                obj.put("personname", account.getName());
                obj.put("memo", gReceipt.getMemo());
                obj.put("taxamount", gReceipt.getTaxEntry()==null?0:gReceipt.getTaxEntry().getAmount());
                obj.put("discount", gReceipt.getDiscount()==null?0:gReceipt.getDiscount().getDiscountValue());
                if(account.getID().equals(cashAccount)){
                      obj.put("amountdue",0);
                      obj.put("incash",true);
                      obj.put("amountdueinbase", 0);
                }
                else{
                    obj.put("amountdue", amount - ramount);
                    obj.put("amountdueinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,amount - ramount,currencyid,je.getEntryDate()));
                }
                obj.put("amountduenonnegative", (amount - ramount<=0)?0:amount - ramount);
                obj.put("amount", d.getAmount());
                obj.put("amountinbase",CompanyHandler.getCurrencyToBaseAmount(session,request, d.getAmount(),currencyid,je.getEntryDate()));
                if(gReceipt.getTax()!=null)
                    taxPercent=CompanyHandler.getTaxPercent(session, request,je.getEntryDate(),gReceipt.getTax().getID());
                obj.put("taxpercent", taxPercent);  //tax in percent applyind on invoice
                if (!(ignoreZero && amount - ramount <= 0)) {
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException see) {
            throw ServiceException.FAILURE("CompanyHandler.getGoodsReceipts", see);
        } catch (ParseException pe) {
            throw ServiceException.FAILURE("CompanyHandler.getGoodsReceipts", pe);
        } catch (JSONException jse) {
            throw ServiceException.FAILURE("CompanyHandler.getGoodsReceipts", jse);
        }
        return jobj;
    }


    public static JSONObject getTopVendorsChart(Session session, HttpServletRequest request,int limit) throws ServiceException, HibernateException, SessionExpiredException {
            JSONObject jobj=new JSONObject();
        try {
            ArrayList params = new ArrayList();
            int i=0;
            params.add(AuthHandler.getCompanyid(request));
            String query=null;
            query = "select sum(inv.vendorEntry.amount/inv.exchangeRateDetail.exchangeRate) as invoiceamount," +
                    "acc.name, "+
                    " acc.id"+
                    " from GoodsReceipt as inv "+
                    ", JournalEntryDetail as jed, Account as acc where jed.ID=inv.vendorEntry.ID and jed.account.ID=acc.ID"+
                    " and inv.company.companyID=?  group by acc.name ";
            List list = HibernateUtil.executeQuery(session, query,params.toArray());

            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JSONObject obj = new JSONObject();
                String cashAccount=((CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request))).getCashAccount().getID();
                if(cashAccount.equals(row[2])){
                    i--;
                    continue;
                }

                obj.put("personname", row[1]);
                obj.put("amount",row[0]);
                    jArr.put(obj);
                    i++;
                    if(i>=10)
                        break;
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    private static HashMap applyDebitNotes(Session session,HttpServletRequest request, GoodsReceipt gReceipt) throws ServiceException, SessionExpiredException {
        HashMap hm = new HashMap();
        Set grRows = gReceipt.getRows();
        Iterator itr = grRows.iterator();
        KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
        double amount;
        int quantity;
        double disc = (gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscountValue()) / grRows.size();
        while (itr.hasNext()) {
            GoodsReceiptDetail temp = (GoodsReceiptDetail) itr.next();
            quantity = temp.getInventory().getQuantity();
            amount = temp.getRate() * quantity;
            double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
            hm.put(temp, new Object[]{amount - rdisc - disc, quantity,0.0});
            if (gReceipt == null) gReceipt = temp.getGoodsReceipt();
        }
        String query="select dn, dnr, dnd from DebitNote dn left join dn.rows dnr left join dn.discounts dnd where dn.deleted=false and (dnr.goodsReceiptRow.goodsReceipt.ID=? or dnd.goodsReceipt.ID=?) order by dn.sequence";
        Iterator dnitr=HibernateUtil.executeQuery(session, query, new Object[]{gReceipt.getID(), gReceipt.getID()}).iterator();
        double taxAmount=0;
        while (dnitr.hasNext()){
            Object[] dnrow = (Object[]) dnitr.next();
            DebitNoteDetail dnr = (DebitNoteDetail) dnrow[1];
            GoodsReceiptDetail temp = dnr.getGoodsReceiptRow();
            if (!hm.containsKey(temp))continue;
            Object[] val = (Object[]) hm.get(temp);
            String fromcurrencyid=(dnr.getDebitNote().getCurrency()==null?currency.getCurrencyID(): dnr.getDebitNote().getCurrency().getCurrencyID());
            String tocurrencyid=(gReceipt.getCurrency()==null?currency.getCurrencyID(): gReceipt.getCurrency().getCurrencyID());
            double v=(Double)val[0]-(dnr.getDiscount()==null?0:CompanyHandler.getOneCurrencyToOther(session,request,dnr.getDiscount().getDiscountValue(),fromcurrencyid,tocurrencyid,gReceipt.getJournalEntry().getEntryDate()));
            if(dnr.getTaxAmount()!=null)
                taxAmount+=dnr.getTaxAmount();
            int q=(Integer)val[1];
            if(temp.getInventory()!=null)
               q-=dnr.getQuantity();
            hm.put(temp, new Object[]{v,q,taxAmount});
            }
        return hm;
    }

    private static HashMap getGoodsReceiptProductAmount(Session session,GoodsReceipt gr) throws ServiceException{
        HashMap hm=new HashMap();
        Set invRows=gr.getRows();
        Iterator itr=invRows.iterator();
        double amount;
        int quantity;
        while(itr.hasNext()){
            GoodsReceiptDetail temp=(GoodsReceiptDetail)itr.next();
            quantity=temp.getInventory().getQuantity();
            amount=temp.getRate()*quantity;
            double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
            hm.put(temp, new Object[]{amount-rdisc,quantity});
            if(gr==null)gr=temp.getGoodsReceipt();
        }
        return hm;
    }

    /*public static JSONObject getGoodsReceiptRows(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            String[] greceipts = request.getParameterValues("bills");
             KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            int i = 0;
            JSONArray jArr = new JSONArray();
            while (greceipts != null && i < greceipts.length) {
                GoodsReceipt gReceipt = (GoodsReceipt) session.get(GoodsReceipt.class, greceipts[i]);
                Iterator itr = gReceipt.getRows().iterator();
                HashMap hm = applyDebitNotes(session,request, gReceipt);
                while (itr.hasNext()) {
                    GoodsReceiptDetail row = (GoodsReceiptDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", gReceipt.getID());
                    obj.put("billno", gReceipt.getGoodsReceiptNumber());
                    String currencyid=(gReceipt.getCurrency()==null?currency.getSymbol(): gReceipt.getCurrency().getSymbol());
                    obj.put("currencysymbol", currencyid);
                    obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,gReceipt.getJournalEntry().getEntryDate()));
                    Inventory inv=row.getInventory();
                    Product prod=inv.getProduct();
                    obj.put("rowid", row.getID());
                    obj.put("productid", prod.getID());
                    obj.put("productname", prod.getName());
                    Discount disc= row.getDiscount();
                    if(disc!=null&&disc.isInPercent())
                        obj.put("prdiscount",disc.getDiscount());
                    else
                        obj.put("prdiscount",0);
                    obj.put("desc", inv.getDescription());
                    obj.put("rate", row.getRate());
                    obj.put("quantity", row.getInventory().getQuantity());
                    int remainingquantity=0;
                    double amount=0;
                    if(hm.containsKey(row)){
                        Object[] val=(Object[])hm.get(row);
                        amount=(Double)val[0];
                        remainingquantity=(Integer)val[1];
                        obj.put("remainingquantity", remainingquantity);
                        obj.put("remquantity", 0);
                        obj.put("amount", amount);
                    }
                    HashMap amthm=getGoodsReceiptProductAmount(session,gReceipt);
                    Object[] val=(Object[])amthm.get(row);
                    amount=(Double)val[0];
                    obj.put("orignalamount", amount);
                    double percent=0;
                    if(gReceipt.getTax()!=null)
                        percent=CompanyHandler.getTaxPercent(session, request, gReceipt.getJournalEntry().getEntryDate(),gReceipt.getTax().getID());
                    obj.put("taxpercent", percent);
                    jArr.put(obj);
                    i++;
                }
                jobj.put("data", jArr);
            }
        } catch (JSONException jse) {
            throw ServiceException.FAILURE(jse.getMessage(), jse);
        }
        return jobj;
    }*/

    /*public static void saveDebitNote(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException {
        try {
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KWLCurrency kwlcurrency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            DebitNote debitnote = new DebitNote();
            String entryNumber = request.getParameter("number");
            String currencyid=(request.getParameter("currencyid")==null?kwlcurrency.getCurrencyID():request.getParameter("currencyid"));
            KWLCurrency currency=(KWLCurrency)session.get(KWLCurrency.class,currencyid);
            String q="from DebitNote where debitNoteNumber=? and company.companyID=?";
            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
                throw new AccountingException("Debit note number '" + entryNumber + "' already exists.");
            debitnote.setDebitNoteNumber(request.getParameter("number"));
            debitnote.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_DEBITNOTE).equals(entryNumber));
            debitnote.setMemo(request.getParameter("memo"));
            debitnote.setDeleted(false);
            debitnote.setCompany(company);
            debitnote.setCurrency(currency);
            Long seqNumber = null;
            String query = "select count(dn.ID) from DebitNote dn inner join dn.journalEntry je  where dn.company.companyID=? and je.entryDate<=?";
            List list = HibernateUtil.executeQuery(session, query, new Object[]{AuthHandler.getCompanyid(request), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate"))});//
            if (!list.isEmpty()) {
                seqNumber = (Long) list.get(0);
            }
            debitnote.setSequence(seqNumber.intValue());
            HashSet hs = new HashSet();
            Double totalAmount = saveDebitNoteRows(session, request, debitnote, company, hs, preferences,kwlcurrency);
           // saveDebitNoteDiscountRows(session, request, debitnote, company);

            JournalEntryDetail jed = new JournalEntryDetail();
            jed.setCompany(company);
            jed.setAmount(totalAmount);
            jed.setAccount((Account) session.get(Account.class, request.getParameter("accid")));
            jed.setDebit(true);
            hs.add(jed);
            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
            request.getParameter("memo"), "JE" + debitnote.getDebitNoteNumber(),currency.getCurrencyID(), hs,request);
            debitnote.setJournalEntry(journalEntry);
            session.saveOrUpdate(debitnote);
        } catch (ParseException pe) {
            throw ServiceException.FAILURE("CompanyHandler.saveDebitNote", pe);
        } catch (SessionExpiredException see) {
            throw ServiceException.FAILURE("CompanyHandler.saveDebitNote", see);
        }
    }*/

    /*private static Double saveDebitNoteRows(Session session, HttpServletRequest request, DebitNote dn, Company company, HashSet hs, CompanyAccountPreferences preferences,KWLCurrency currency) throws ServiceException, HibernateException, AccountingException, SessionExpiredException {
        double totalAmount = 0;
        double totalTax=0;
        try {
            HashSet rows = new HashSet();
            JournalEntryDetail jed;
            Account account;
            double discAccAmount = 0;
            boolean includeTax=CompanyHandler.getBoolean(request,"includetax");
            String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
            JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
            List list = new ArrayList();
            for (int i = 0; i < jArr.length(); i++) {
                double taxamount=0;
                double amount = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                DebitNoteDetail row = new DebitNoteDetail();
                double disc = jobj.getDouble("discamount");
                row.setCompany(company);
                row.setDebitNote(dn);
                row.setMemo(dn.getMemo());
                row.setQuantity(jobj.getInt("remquantity"));
                GoodsReceiptDetail goodsReceiptRow = (GoodsReceiptDetail) session.get(GoodsReceiptDetail.class, jobj.getString("rowid"));
                row.setGoodsReceiptRow(goodsReceiptRow);
                Product product = goodsReceiptRow.getInventory().getProduct();
                account = (Account) session.get(Account.class, product.getPurchaseReturnAccount().getID());
                double percent=0;
                if(goodsReceiptRow.getGoodsReceipt().getTax()!=null)
                    percent=CompanyHandler.getTaxPercent(session, request, goodsReceiptRow.getGoodsReceipt().getJournalEntry().getEntryDate(),goodsReceiptRow.getGoodsReceipt().getTax().getID());
                if (jobj.getInt("typeid") == 2 || jobj.getInt("typeid") == 3) {
                    Inventory inventory = CompanyHandler.makeInventory(session, request, product, jobj.getInt("remquantity"), jobj.optString("desc"), false, (jobj.getInt("typeid") == 3 ? false : true));
                    row.setInventory(inventory);
                    if (list.contains(account)) {
                        rows.add(row);
                        continue;
                    }
                    for (int k = 0; k < jArr.length(); k++) {
                        JSONObject jobj1 = jArr.getJSONObject(k);
                        GoodsReceiptDetail compGoodsReceiptRow = (GoodsReceiptDetail) session.get(GoodsReceiptDetail.class, jobj1.getString("rowid"));
                        Product compProduct = compGoodsReceiptRow.getInventory().getProduct();
                        Account compAccount = (Account) session.get(Account.class, compProduct.getPurchaseReturnAccount().getID());
                        if (account == compAccount && (jobj1.getInt("typeid") == 2  || jobj.getInt("typeid") == 3)) {
                            amount = amount + jobj1.getDouble("discamount");
                            list.add(compAccount);
                        }
                        if (disc > 0) {
                            Discount discount = new Discount();
                            discount.setDiscount(disc);
                            discount.setInPercent(false);
                            discount.setOriginalAmount(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("amount"),currencyid,null));
                            discount.setCompany(company);
                            session.save(discount);
                            row.setDiscount(discount);
                            if(includeTax){
                                taxamount=disc*percent/100;
                                row.setTaxAmount(taxamount);
                                totalTax+=taxamount;
                                if(includeTax&&taxamount>0){
                                    jed = new JournalEntryDetail();
                                    jed.setCompany(company);
                                    jed.setAmount(taxamount);
                                    jed.setAccount(goodsReceiptRow.getGoodsReceipt().getTax().getAccount());
                                    jed.setDebit(false);
                                    hs.add(jed);
                                }
                            }
                        }
                    }
                } else {
                     if(includeTax){
                            taxamount=disc*percent/100;
                            row.setTaxAmount(taxamount);
                            totalTax+=taxamount;
                            if(includeTax&&taxamount>0){
                                jed = new JournalEntryDetail();
                                jed.setCompany(company);
                                jed.setAmount(taxamount);
                                jed.setAccount(goodsReceiptRow.getGoodsReceipt().getTax().getAccount());
                                jed.setDebit(false);
                                hs.add(jed);
                            }
                        }
                    discAccAmount = discAccAmount + jobj.getDouble("discamount");
                    rows.add(row);
                    if (disc > 0) {
                        Discount discount = new Discount();
                        discount.setDiscount(disc);
                        discount.setInPercent(false);
                        discount.setOriginalAmount(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("amount"),currencyid,null));
                        discount.setCompany(company);
                        session.save(discount);
                        row.setDiscount(discount);

                    }
                    continue;
                }
                rows.add(row);
                jed = new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(amount);
                jed.setAccount(account);
                jed.setDebit(false);
                hs.add(jed);
                totalAmount += amount;
                
            }
            if (discAccAmount != 0.0) {
                jed = new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(discAccAmount);
                jed.setAccount((Account) session.get(Account.class, preferences.getDiscountReceived().getID()));
                jed.setDebit(false);
                hs.add(jed);
                totalAmount += discAccAmount;
            }

            dn.setRows(rows);
        } catch (JSONException je) {
            throw ServiceException.FAILURE("CompanyHandler.saveDebitNote", je);
        }
        return totalAmount+totalTax;
    }*/

    private static void saveDebitNoteDiscountRows(Session session, HttpServletRequest request, DebitNote dn, Company company) throws ServiceException, HibernateException {
        try {
            HashSet rows = new HashSet();
            List list = new ArrayList();
            JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
            for (int i = 0; i < jArr.length(); i++) {
                Double disc = 0.0;
                Double totalAmount = 0.0;
                JSONObject jobj = jArr.getJSONObject(i);
                GoodsReceipt gr = (GoodsReceipt) session.get(GoodsReceipt.class, jobj.getString("billid"));
                if (list.contains(gr)) {
                    continue;
                }
                for (int k = 0; k < jArr.length(); k++) {
                    JSONObject jobj1 = jArr.getJSONObject(k);
                    GoodsReceipt compGR = (GoodsReceipt) session.get(GoodsReceipt.class, jobj1.getString("billid"));
                    if (gr == compGR) {
                        disc = disc + jobj1.getDouble("discamount");
                        totalAmount += jobj.getDouble("amount");
                        list.add(compGR);
                    }
                }

                if (disc != 0.0) {
                    DebitNoteDiscount row = new DebitNoteDiscount();
                    row.setCompany(company);
                    row.setDebitNote(dn);
                    row.setGoodsReceipt(gr);
                    Discount discount = new Discount();
                    discount.setDiscount(disc);
                    discount.setInPercent(false);
                    discount.setOriginalAmount(totalAmount);
                    discount.setCompany(company);
                    row.setDiscount(discount);
                    session.save(discount);
                    rows.add(row);
                }
            }
            dn.setDiscounts(rows);
        } catch (JSONException je) {
            throw ServiceException.FAILURE("CompanyHandler.saveDebitNote", je);
        }
    }

    /*public static JSONObject getDebitNotes(Session session, HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String ss = request.getParameter("ss");
            ArrayList params = new ArrayList();
            params.add(AuthHandler.getCompanyid(request));
            String condition = " where ac.ID=v.account.ID and dn.deleted=false and dn.company.companyID=?";
            if (StringUtil.isNullOrEmpty(ss) == false) {
            for(int i=0;i<=3;i++){
                    params.add(ss + "%");
                }
                 condition += " and ( dn.debitNoteNumber like ? or v.name like ?  or dn.memo like ? or dn.journalEntry.entryNumber like ?) ";
            }
            String query = "select dn, v, jed from DebitNote dn inner join dn.journalEntry je inner join je.details jed inner join jed.account ac, Vendor v" + condition;
            List list = HibernateUtil.executeQuery(session, query, params.toArray());
            int count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = HibernateUtil.executeQueryPaging(session, query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                DebitNote debitMemo = (DebitNote) row[0];
                Vendor vendor = (Vendor) row[1];
                JournalEntryDetail details = (JournalEntryDetail) row[2];
                JSONObject obj = new JSONObject();
                obj.put("noteid", debitMemo.getID());
                obj.put("noteno", debitMemo.getDebitNoteNumber());
                obj.put("journalentryid", debitMemo.getJournalEntry().getID());
                obj.put("currencysymbol", (debitMemo.getCurrency()==null?currency.getSymbol():debitMemo.getCurrency().getSymbol()));
                obj.put("entryno", debitMemo.getJournalEntry().getEntryNumber());
                obj.put("personid", vendor.getID());
                obj.put("personname", vendor.getAccount().getName());
                obj.put("amount", details.getAmount());
                obj.put("date", AuthHandler.getDateFormatter(request).format(debitMemo.getJournalEntry().getEntryDate()));
                obj.put("memo", debitMemo.getMemo());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (JSONException je) {
            throw ServiceException.FAILURE("CompanyHandler.saveDebitNote", je);
        } catch (SessionExpiredException see) {
            throw ServiceException.FAILURE("CompanyHandler.saveDebitNote", see);
        }
        return jobj;
    }*/

    /*public static JSONObject getDebitNoteRows(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String[] creditNote = request.getParameterValues("bills");
            int i = 0;
            JSONArray jArr = new JSONArray();
            while (creditNote != null && i < creditNote.length) {
                DebitNote dn = (DebitNote) session.get(DebitNote.class, creditNote[i]);
                Iterator itr = dn.getRows().iterator();
                while (itr.hasNext()) {
                    DebitNoteDetail row = (DebitNoteDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", dn.getID());
                    obj.put("billno", dn.getDebitNoteNumber());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getGoodsReceiptRow().getInventory().getProduct().getID());
                    obj.put("productname", row.getGoodsReceiptRow().getInventory().getProduct().getName());
                    obj.put("desc", row.getGoodsReceiptRow().getInventory().getProduct().getDescription());
                    obj.put("memo", row.getMemo());
                    obj.put("currencysymbol", (dn.getCurrency()==null?currency.getSymbol():dn.getCurrency().getSymbol()));
                    obj.put("transectionid", row.getGoodsReceiptRow().getGoodsReceipt().getID());
                    obj.put("transectionno", row.getGoodsReceiptRow().getGoodsReceipt().getGoodsReceiptNumber());
                    Discount disc=row.getDiscount();
                    if(disc!=null)
                        obj.put("discount", disc.getDiscountValue());
                    else
                        obj.put("discount", 0);
                    obj.put("quantity", row.getQuantity());
                    obj.put("taxamount", row.getTaxAmount());
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getDebitNoteRows", ex);
        }
        return jobj;
    }*/

    /*public static void savePayment(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException {
        try {
            Account dipositTo=null;
            Payment payment = new Payment();
            double amount = 0;
            double amountDiff = 0;
            HashSet hs = new HashSet();
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            String entryNumber=request.getParameter("no");
            String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
            String q="from Payment where paymentNumber=? and company.companyID=?";
            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
                throw new AccountingException("Payment number '" + entryNumber + "' already exists.");
            payment.setPaymentNumber(request.getParameter("no"));
            payment.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_PAYMENT).equals(entryNumber));
            PaymentMethod payMethod = (PaymentMethod) session.get(PaymentMethod.class, request.getParameter("pmtmethod"));
            dipositTo = payMethod.getAccount();
            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {
                PayDetail pdetail = new PayDetail();
                pdetail.setPaymentMethod(payMethod);
                pdetail.setCompany(company);
                JSONObject obj = new JSONObject(request.getParameter("paydetail"));
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                    Cheque cheque = new Cheque();
                    cheque.setChequeNo(obj.getString("chequeno"));
                    cheque.setDescription(obj.getString("description"));
                    cheque.setBankName(obj.getString("bankname"));
                    session.save(cheque);
                    pdetail.setCheque(cheque);
                } else if (payMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
                    Card card = new Card();
                    card.setCardNo(obj.getString("cardno"));
                    card.setCardHolder(obj.getString("nameoncard"));
                    card.setExpiryDate(obj.getString("expirydate"));
                    card.setCardType(obj.getString("cardtype"));
                    card.setRefNo(obj.getString("refno"));
                    session.save(card);
                    pdetail.setCard(card);
                }
                session.save(pdetail);
                payment.setPayDetail(pdetail);
            }
            payment.setMemo(request.getParameter("memo"));
            payment.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
            payment.setDeleted(false);
            payment.setCompany(company);
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
           hs = new HashSet();
            if(jArr.length()>0){
                amount = savePaymentRows(session, request, payment, company, jArr);
                amountDiff=oldPaymentRowsAmount(session, request, jArr,currencyid);
                if(preferences.getForeignexchange()==null)
                    throw new AccountingException("Please Set Foreign Exchange account in Account Preference first");
                if(amountDiff!=0&&preferences.getForeignexchange()!=null){
                    hs = new HashSet();
                    JournalEntryDetail jed = new JournalEntryDetail();
                    jed.setCompany(company);
                    jed.setAmount(amountDiff);
                    jed.setAccount(preferences.getForeignexchange());
                    jed.setDebit(false);
                    hs.add(jed);
                }

            }

            else amount= Double.parseDouble(request.getParameter("amount"));

            JournalEntryDetail jed = new JournalEntryDetail();
            jed.setCompany(company);
            jed.setAmount(amount+amountDiff);
            jed.setAccount(((Account) session.get(Account.class, request.getParameter("accid"))));
            jed.setDebit(true);
            hs.add(jed);
            jed = new JournalEntryDetail();
            jed.setCompany(company);
            jed.setAmount(amount);
            jed.setAccount(dipositTo);
            jed.setDebit(false);
            hs.add(jed);
            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
                    request.getParameter("memo"), "JE" + payment.getPaymentNumber(),currencyid, hs,request);
            jed.setJournalEntry(journalEntry);
            payment.setJournalEntry(journalEntry);
            session.saveOrUpdate(payment);
        //ProfileHandler.insertAuditLog(session, AuditAction.RECEIPT_ADDED, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) +" created new receipt "+receipt.getReceiptNumber()+" for invoice "+receipt.getInvoice().getInvoiceNumber(), request);
        } catch (JSONException je) {
            throw ServiceException.FAILURE("CompanyHandler.saveReceipt", je);
        } catch (ParseException pe) {
            throw ServiceException.FAILURE("CompanyHandler.saveReceipt", pe);
        } catch (SessionExpiredException se) {
            throw ServiceException.FAILURE("CompanyHandler.saveReceipt", se);
        }
    }*/
     /* private static double oldPaymentRowsAmount(Session session, HttpServletRequest request, JSONArray jArr,String currencyid) throws JSONException, ServiceException, HibernateException, SessionExpiredException, ParseException {
        double ratio=0;
        double amount = 0;
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
           GoodsReceipt gr=(GoodsReceipt) session.get(GoodsReceipt.class, jobj.getString("billid"));
            Double recinvamount = jobj.getDouble("payment");
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String currid=currency.getCurrencyID();
            if(gr.getCurrency()!=null)
                 currid=gr.getCurrency().getCurrencyID();
            double oldrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,currid,gr.getJournalEntry().getEntryDate());
            double  newrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,currid,AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            ratio=oldrate-newrate;
            amount+=recinvamount*ratio;

        }
        amount=CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
        return (amount);
    }*/
    private static double savePaymentRows(Session session, HttpServletRequest request, Payment payment, Company company, JSONArray jArr) throws JSONException, ServiceException, HibernateException {
        HashSet hs = new HashSet();
        double amount = 0;
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            PaymentDetail pd = new PaymentDetail();
            pd.setAmount(jobj.getDouble("payment"));
            pd.setCompany(company);
            pd.setGoodsReceipt((GoodsReceipt) session.get(GoodsReceipt.class, jobj.getString("billid")));
            pd.setPayment(payment);
            amount += jobj.getDouble("payment");
            hs.add(pd);
        }
        payment.setRows(hs);
        return amount;
    }
    /*public static JSONObject getPayments(Session session, HttpServletRequest request) throws ServiceException {
        JSONObject jobj=new JSONObject();
        try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String start=request.getParameter("start");
            String limit=request.getParameter("limit");
            String ss=request.getParameter("ss");
            ArrayList params=new ArrayList();
            params.add(AuthHandler.getCompanyid(request));
            String condition=" where p.deleted=false and p.company.companyID=? and jed.debit=true ";
            if(StringUtil.isNullOrEmpty(ss)==false){
               params.add(ss+"%");
               condition+= " and (ac.name like ? ) ";
           }
           String query="select p, ac from Payment p inner join p.journalEntry je inner join je.details jed inner join jed.account ac" + condition;
           List list = HibernateUtil.executeQuery(session, query, params.toArray());
           int count=list.size();
           if(StringUtil.isNullOrEmpty(start)==false&&StringUtil.isNullOrEmpty(limit)==false){
                list=HibernateUtil.executeQueryPaging(session, query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Object[] row=(Object[])itr.next();
                Payment payment = (Payment) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                obj.put("billid", payment.getID());
                obj.put("entryno", payment.getJournalEntry().getEntryNumber());
                obj.put("journalentryid", payment.getJournalEntry().getID());
                obj.put("personid", acc.getID());
                obj.put("billno", payment.getPaymentNumber());
                obj.put("billdate", AuthHandler.getDateFormatter(request).format(payment.getJournalEntry().getEntryDate()));//receiptdate
                Iterator itrRow = payment.getRows().iterator();
                double amount = 0;
                if(!payment.getRows().isEmpty())
                    while(itrRow.hasNext()){
                        amount += ((PaymentDetail) itrRow.next()).getAmount();
                    }
                else{
                    itrRow=payment.getJournalEntry().getDetails().iterator();
                    amount+=((JournalEntryDetail)itrRow.next()).getAmount();
                }
                obj.put("amount", amount);
                obj.put("personname", acc.getName());
                obj.put("memo", payment.getMemo());
                obj.put("currencysymbol", (payment.getCurrency()==null?currency.getSymbol():payment.getCurrency().getSymbol()));
                obj.put("paymentmethod", (payment.getPayDetail() == null ? "Cash" : payment.getPayDetail().getPaymentMethod().getMethodName()));
                if(payment.getPayDetail()!=null){
                    obj.put("refno",(payment.getPayDetail().getCard()==null?(payment.getPayDetail().getCheque()==null?"":payment.getPayDetail().getCheque().getChequeNo()):payment.getPayDetail().getCard().getRefNo()));
                    obj.put("refname",(payment.getPayDetail().getCard()==null?(payment.getPayDetail().getCheque()==null?"":payment.getPayDetail().getCheque().getBankName()):payment.getPayDetail().getCard().getCardHolder()));
                }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (JSONException je) {
            throw ServiceException.FAILURE("CompanyHandler.saveReceipt", je);
        } catch (SessionExpiredException se) {
            throw ServiceException.FAILURE("CompanyHandler.saveReceipt", se);
        }
        return jobj;
    }*/

    /*public static void savePurchaseOrder(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException {
        try {
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            PurchaseOrder purchaseOrder = new PurchaseOrder();
                String entryNumber = request.getParameter("number");
                String q = "from PurchaseOrder where purchaseOrderNumber=? and company.companyID=?";
                if (!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty()) {
                    throw new AccountingException("Purchase Order number '" + entryNumber + "' already exists.");
                }
                purchaseOrder.setPurchaseOrderNumber(entryNumber);
                purchaseOrder.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_PURCHASEORDER).equals(entryNumber));

            purchaseOrder.setMemo(request.getParameter("memo"));
            purchaseOrder.setVendor((Vendor) session.get(Vendor.class, request.getParameter("vendor")));
            purchaseOrder.setOrderDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")));
            purchaseOrder.setDueDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("duedate")));
            purchaseOrder.setCompany(company);
            savePurchaseOrderRows(session, request, purchaseOrder, company);
            session.saveOrUpdate(purchaseOrder);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("VendorHandler.savePurchaseOrder", ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("VendorHandler.savePurchaseOrder", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("VendorHandler.savePurchaseOrder", ex);
        }
    }*/

    private static void savePurchaseOrderRows(Session session, HttpServletRequest request, PurchaseOrder purchaseOrder, Company company) throws JSONException, ServiceException {
        HashSet rows = new HashSet();
        JSONArray jArr = new JSONArray(request.getParameter("detail"));
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            PurchaseOrderDetail row = new PurchaseOrderDetail();
            row.setCompany(company);
            row.setPurchaseOrder(purchaseOrder);
            row.setProduct((Product) session.get(Product.class, jobj.getString("productid")));
            row.setRate(jobj.getDouble("rate"));
            row.setQuantity(jobj.getInt("quantity"));
            row.setRemark(jobj.optString("remark"));
            rows.add(row);
        }
        purchaseOrder.setRows(rows);
    }

       /*public static JSONObject getPurchaseOrders(Session session, HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            KWLCurrency kwlcurrency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
             String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String ss = request.getParameter("ss");
            ArrayList params = new ArrayList();
            params.add(AuthHandler.getCompanyid(request));
            String condition = " where deleted=false and company.companyID=?";
            if (!StringUtil.isNullOrEmpty(ss)) {
                for (int i=0;i<3;i++)
                    params.add(ss + "%");
                condition += " and (purchaseOrderNumber like ? or po.memo like ? or po.vendor.name like ? )";
            }
            String query = "from PurchaseOrder po" + condition;
            List list = HibernateUtil.executeQuery(session, query, params.toArray());
            int count = list.size();
            if (!StringUtil.isNullOrEmpty(start) && StringUtil.isNullOrEmpty(limit) == false) {
                list = HibernateUtil.executeQueryPaging(session, query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            boolean closeflag = request.getParameter("closeflag")!=null?true:false;
            while (itr.hasNext()) {
                PurchaseOrder purchaseOrder = (PurchaseOrder) itr.next();
                Vendor vendor = purchaseOrder.getVendor();
                KWLCurrency currency=purchaseOrder.getVendor().getAccount().getCurrency()==null?kwlcurrency:purchaseOrder.getVendor().getAccount().getCurrency();
                JSONObject obj = new JSONObject();
                obj.put("billid", purchaseOrder.getID());
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("personid", vendor.getID());
                obj.put("billno", purchaseOrder.getPurchaseOrderNumber());
                obj.put("duedate", AuthHandler.getDateFormatter(request).format(purchaseOrder.getDueDate()));
                obj.put("date", AuthHandler.getDateFormatter(request).format(purchaseOrder.getOrderDate()));
                Iterator itrRow = purchaseOrder.getRows().iterator();
                double amount = 0;
                while (itrRow.hasNext()) {
                    PurchaseOrderDetail sod = (PurchaseOrderDetail) itrRow.next();
                    amount += sod.getQuantity() * sod.getRate();
                }
                obj.put("currencysymbol", currency.getSymbol());
                obj.put("orderamount", CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currency.getCurrencyID(),purchaseOrder.getOrderDate()));
                obj.put("amount", amount);
                obj.put("personname", vendor.getName());
                obj.put("memo", purchaseOrder.getMemo());
                String status = getPurchaseOrderStatus(session,purchaseOrder);
                obj.put("status",status);
                if(!closeflag || (closeflag&&status.equalsIgnoreCase("open")))
                    jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (SessionExpiredException see) {
            throw ServiceException.FAILURE("CustomerManager.getPurchaseOrders", see);
        } catch (NumberFormatException nfe) {
            throw ServiceException.FAILURE("CustomerManager.getPurchaseOrders", nfe);
        } catch (JSONException jse) {
            throw ServiceException.FAILURE("CustomerManager.getPurchaseOrders", jse);
        }
        return jobj;
    }*/
    public static String getPurchaseOrderStatus(Session session, PurchaseOrder po) throws ServiceException {
        Set<PurchaseOrderDetail> orderDetail = po.getRows();
        Iterator ite = orderDetail.iterator();
        String result = "Closed";
        while(ite.hasNext()){
            PurchaseOrderDetail pDetail = (PurchaseOrderDetail)ite.next();
            String query = "from GoodsReceiptDetail ge where ge.purchaseorderdetail.ID = ?";
            List list =  HibernateUtil.executeQuery(session, query,pDetail.getID());
            Iterator ite1 = list.iterator();
            int qua = 0;
            while(ite1.hasNext()){
                GoodsReceiptDetail ge = (GoodsReceiptDetail)ite1.next();
                qua += ge.getInventory().getQuantity();
            }
            if(qua < pDetail.getQuantity()){
                result = "Open";
                break;
            }
        }
        return result;
    }
      /*    public static JSONObject getPaymentRows(Session session, HttpServletRequest request) throws ServiceException, HibernateException {
        JSONObject jobj = new JSONObject();
        try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String[] receipt = request.getParameterValues("bills");
            int i = 0;
            JSONArray jArr = new JSONArray();
            while (receipt != null && i < receipt.length) {
                Payment re = (Payment) session.get(Payment.class, receipt[i]);
                Iterator itr = re.getRows().iterator();
                while (itr.hasNext()) {
                    PaymentDetail row = (PaymentDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", re.getID());
                    obj.put("rowid", row.getID());
                    obj.put("transectionno", row.getGoodsReceipt().getGoodsReceiptNumber());
                    obj.put("transectionid", row.getGoodsReceipt().getID());
                    obj.put("amount", row.getAmount());
                    obj.put("currencysymbol", (row.getPayment().getCurrency()==null?currency.getSymbol():row.getPayment().getCurrency().getSymbol()));
                    obj.put("duedate", AuthHandler.getDateFormatter(request).format(row.getGoodsReceipt().getDueDate()));
                    obj.put("creationdate", AuthHandler.getDateFormatter(request).format(row.getGoodsReceipt().getJournalEntry().getEntryDate()));
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getPaymentRows", ex);
        } catch (JSONException je) {
            throw ServiceException.FAILURE("CompanyHandler.getPaymentRows", je);
        }
        return jobj;
    }*/

    /*public static void deletePayment(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException, JSONException, SessionExpiredException{
        ArrayList params = new ArrayList();
        try{
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String qMarks = "";
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            qMarks = "";
            for (int i = 0; i < jArr.length(); i++){
                 JSONObject jobj = jArr.getJSONObject(i);
                params.add(jobj.getString("billid"));
                qMarks += "?,";
            }
            qMarks=qMarks.substring(0,Math.max(0, qMarks.length()-1));
            params.add(company.getCompanyID());
            String query;
            query = "update Payment set deleted=true where ID in("+qMarks +") and company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
            query = "update JournalEntry je set je.deleted=true where je.ID in(select p.journalEntry.ID from Payment p where p.ID in("+qMarks +") and p.company.companyID=je.company.companyID) and je.company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
        } catch(ServiceException ex){
            throw ServiceException.FAILURE("Selected record(s) are currently used in transaction(s)", ex);
        }
    }*/

    public static JSONObject getMonthWisePayable(Session session, HttpServletRequest request) throws ServiceException, HibernateException {
        JSONObject jobj = getGoodsReceipts(session, request,null);
        try {
            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            Calendar cal = Calendar.getInstance();
            Calendar startFinYearCal = Calendar.getInstance();
            Calendar endFinYearCal = Calendar.getInstance();

            DateFormat sdf= new SimpleDateFormat("MMM");
            startFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.add(Calendar.YEAR,1);
            int checkMonth=0;
            JSONArray jArr = jobj.getJSONArray("data");
            JSONArray finalJArr=new JSONArray();

            for(int i=startFinYearCal.get(Calendar.MONTH);i<12;i++){
                JSONObject finalObj=new JSONObject();
                finalObj.put("month", i);
                 cal.set(Calendar.MONTH, i);
                finalObj.put("monthname", sdf.format(cal.getTime()));
                finalObj.put("totalamount", 0);
                finalObj.put("amountpayed", 0);
                finalObj.put("amountdue", 0);
                finalJArr.put(finalObj);
            }
            for(int i=0;i<startFinYearCal.get(Calendar.MONTH);i++){
                JSONObject finalObj=new JSONObject();
                finalObj.put("month", i);
                 cal.set(Calendar.MONTH, i);
                finalObj.put("monthname", sdf.format(cal.getTime()));
                finalObj.put("totalamount", 0);
                finalObj.put("amountpayed", 0);
                finalObj.put("amountdue", 0);
                finalJArr.put(finalObj);
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);
                Date dueDate=AuthHandler.getDateFormatter(request).parse(obj.getString("duedate"));
                cal.setTime(dueDate);
                if (dueDate.after(pref.getBookBeginningFrom()) && dueDate.before(endFinYearCal.getTime())){
                    checkMonth = cal.get(Calendar.MONTH);
                    int month=(checkMonth-startFinYearCal.get(Calendar.MONTH))>0?checkMonth-startFinYearCal.get(Calendar.MONTH):startFinYearCal.get(Calendar.MONTH)-checkMonth;
                    JSONObject finalObj=finalJArr.optJSONObject(month);
                    double tamount=obj.getDouble("amountinbase");
                    double damount=obj.getDouble("amountdueinbase");
                    finalObj.put("totalamount", finalObj.getDouble("totalamount")+tamount);
                    finalObj.put("amountdue", finalObj.getDouble("amountdue")+damount);
                    finalObj.put("amountpayed", finalObj.getDouble("amountpayed")+tamount-damount);
                }
            }
            jobj.put("data", finalJArr);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getInvoices", ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getInvoices", ex);
        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("CompanyHandler.getInvoices", ne);
        } catch (JSONException jse) {
            throw ServiceException.FAILURE("CompanyHandler.getInvoices", jse);
        }
        return jobj;
    }

     /* public static JSONObject getPurchaseOrderRows(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException {
        JSONObject jobj=new JSONObject();
        try {
            KWLCurrency kwlcurrency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String[] pos = request.getParameterValues("bills");
            int i = 0;
            JSONArray jArr = new JSONArray();
            int addobj = 1;
            String closeflag = request.getParameter("closeflag");
            while (pos != null && i < pos.length) {
                PurchaseOrder po = (PurchaseOrder) session.get(PurchaseOrder.class, pos[i]);
                KWLCurrency currency=po.getVendor().getAccount().getCurrency()==null?kwlcurrency:po.getVendor().getAccount().getCurrency();
                Iterator itr = po.getRows().iterator();
                while (itr.hasNext()) {
                    PurchaseOrderDetail row = (PurchaseOrderDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", po.getID());
                    obj.put("billno", po.getPurchaseOrderNumber());
                     obj.put("currencysymbol",currency.getSymbol());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("productname",row.getProduct().getName());
                    obj.put("desc", row.getProduct().getDescription());
                    obj.put("memo", row.getRemark());
                    obj.put("rate", row.getRate());
                    obj.put("orderrate", CompanyHandler.getBaseToCurrencyAmount(session,request,row.getRate(),currency.getCurrencyID(),po.getOrderDate()));
//                    obj.put("quantity", row.getQuantity());
                    if(closeflag!=null){
                       addobj =  getPurchaseOrderDetailStatus(session,row);
                       obj.put("quantity", addobj);
                    }else{
                       obj.put("quantity", row.getQuantity());
                    }

                    if(addobj> 0)
                        jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE("CompanyHandler.getPurchaseOrderRows", je);
        }
        return jobj;
    }*/

    public static int getPurchaseOrderDetailStatus(Session session,PurchaseOrderDetail pod) throws ServiceException{
        int result = pod.getQuantity();
        String query = "from GoodsReceiptDetail ge where ge.purchaseorderdetail.ID = ?";
        List list =  HibernateUtil.executeQuery(session, query,pod.getID());
        Iterator ite1 = list.iterator();
        int qua = 0;
        while(ite1.hasNext()){
            GoodsReceiptDetail ge = (GoodsReceiptDetail)ite1.next();
            qua += ge.getInventory().getQuantity();
        }
        result = pod.getQuantity()-qua;
        return result;
    }

    /*public static void deletePurchaseOrders(Session session, HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        ArrayList params = new ArrayList();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String qMarks = "";
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    params.add(jobj.getString("billid"));
                    qMarks += "?,";
                }
            }
            params.add(company.getCompanyID());
            qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
            String query;
            List list;
            query = "update PurchaseOrder set deleted=true where ID in("+qMarks +") and company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
        } catch (JSONException ex) {
            throw new AccountingException("Cannot extract data from client");
        }
    }*/

    /*public static void deleteDebitNotes(Session session, HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        ArrayList params = new ArrayList();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String qMarks = "";
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("noteid"))) {
                    params.add(jobj.getString("noteid"));
                    qMarks += "?,";
                }
            }
            params.add(company.getCompanyID());
            qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
            String query;
            List list;
            query = "update DebitNote set deleted=true where ID in("+qMarks +") and company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
            query = "update JournalEntry je set je.deleted=true  where je.ID in(select dn.journalEntry.ID from DebitNote dn where dn.ID in( " + qMarks + ") and dn.company.companyID=je.company.companyID) and je.company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
            query = "update Discount di set di.deleted=true  where di.ID in(select dnd.discount.ID from DebitNoteDiscount dnd where dnd.debitNote.ID in( " + qMarks + ") and dnd.company.companyID=di.company.companyID) and di.company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
        } catch (JSONException ex) {
            throw new AccountingException("Cannot extract data from client");
        }
    }*/
}
