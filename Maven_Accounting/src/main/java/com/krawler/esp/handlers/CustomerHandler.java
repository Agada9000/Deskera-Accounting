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
import com.krawler.common.util.StringUtil;
import com.krawler.esp.database.AccountDBCon;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.esp.servlets.ExportServlet;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.AccountingMsgs;
import com.krawler.hql.accounting.BillingInvoice;
import com.krawler.hql.accounting.BillingInvoiceDetail;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.CreditNote;
import com.krawler.hql.accounting.CreditNoteDetail;
import com.krawler.hql.accounting.CreditNoteDiscount;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.ReceiptDetail;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.utils.json.base.JSONException;
import com.lowagie.text.DocumentException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.HashMap;
import javax.mail.MessagingException;
import org.hibernate.HibernateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author krawler-user
 */
public class CustomerHandler {
    /*public static String saveCustomer(Session session, HttpServletRequest request) throws ServiceException, HibernateException, ParseException{
        try {
            Customer customer;
            Company company=(Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            Account account;
            boolean debitType = CompanyHandler.getBoolean(request,"debitType");
            double openBalance=CompanyHandler.getDouble(request, "openbalance");
            double life=CompanyHandler.getDouble(request, "life");
            double salvage=CompanyHandler.getDouble(request, "salvage");
             Date creationDate = AuthHandler.getDateFormatter(request).parse(request.getParameter("creationDate"));
             if(creationDate==null)
                creationDate=new Date();
            String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
            Account parentAccount = null;
            String customerid = request.getParameter("accid");
            String parentid = request.getParameter("parentid");
            boolean issub = request.getParameter("issub")!=null;
            String auditMsg = " added new customer ";
            String auditID = AuditAction.CUSTOMER_ADDED;
            if (issub&&!StringUtil.isNullOrEmpty(parentid)) {
                parentAccount = (Account) session.get(Account.class, parentid);
            }
            Group group = (Group) session.get(Group.class, Group.ACCOUNTS_RECEIVABLE);
            if (StringUtil.isNullOrEmpty(customerid)) {
                customer = new Customer();
                customer.setCreatedOn(new Date());
                openBalance=debitType?openBalance:-openBalance;
                account = CompanyHandler.makeAccount(session, company.getCompanyID(), null, request.getParameter("accname"), openBalance, parentAccount, group,currencyid,life,salvage,creationDate);
                customer.setAccount(account);
            } else {
                customer = (Customer) session.get(Customer.class, customerid);
                customer.setModifiedOn(new Date());
                account = customer.getAccount();
                String accountname = request.getParameter("accname");
                account.setName(accountname);
                openBalance=debitType?openBalance:-openBalance;
                account.setOpeningBalance(openBalance);
                account.setParent(parentAccount);
                session.update(account);
                auditMsg = " updated customer ";
                auditID = AuditAction.CUSTOMER_UPDATED;
            }
            customer.setTitle(request.getParameter("title"));
            customer.setName(request.getParameter("accname"));
            customer.setBillingAddress(request.getParameter("address"));
            customer.setBankaccountno(request.getParameter("bankaccountno"));
            customer.setEmail(request.getParameter("email"));
            customer.setContactNumber(request.getParameter("contactno"));
            customer.setAltContactNumber(request.getParameter("contactno2"));
            customer.setFax(request.getParameter("fax"));
            customer.setShippingAddress(request.getParameter("shippingaddress"));
            customer.setCompany(company);
            customer.setCreditTerm((Term) session.get(Term.class, request.getParameter("termid")));
            customer.setPreferedDeliveryMode(StaticValues.PDM_EMAIL);
//            if (Integer.parseInt(request.getParameter("pdm")) == StaticValues.PDM_PRINT) {
//                customer.setPreferedDeliveryMode(StaticValues.PDM_PRINT);
//            }
            customer.setCompany((Company) session.get(Company.class, AuthHandler.getCompanyid(request)));
            session.saveOrUpdate(customer);
            ProfileHandler.insertAuditLog(session, auditID, "User " + AuthHandler.getFullName(session, AuthHandler.getUserid(request)) + auditMsg + customer.getName(), request);
            return customer.getAccount().getID();
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (NumberFormatException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }*/

    /*public static JSONObject getCustomers(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException, AccountingException{
        JSONObject jobj=new JSONObject();
		try {
            Company company=(Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            jobj=CompanyHandler.getAccounts(session, request);
            JSONArray jArr=jobj.getJSONArray("data");
            JSONArray finalJArr=new JSONArray();
            for(int i=0;i<jArr.length();i++) {
                JSONObject obj = jArr.getJSONObject(i);
                Customer customer = (Customer)session.get(Customer.class, obj.getString("accid"));
                if(customer==null) continue;
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
                obj.put("id", customer.getID());
                obj.put("creationDate", AuthHandler.getDateFormatter(request).format(customer.getAccount().getCreationDate()));
                obj.put("currencyid",(customer.getAccount().getCurrency()==null?currency.getCurrencyID(): customer.getAccount().getCurrency().getCurrencyID()));
                obj.put("currencysymbol",(customer.getAccount().getCurrency()==null?currency.getSymbol(): customer.getAccount().getCurrency().getSymbol()));
                obj.put("currencyname",(customer.getAccount().getCurrency()==null?currency.getName(): customer.getAccount().getCurrency().getName()));

                finalJArr.put(obj);
            }
            jobj.put("data", finalJArr);
        } catch (JSONException e) {
                throw ServiceException.FAILURE(e.getCause().getMessage(), e);
        }
        return jobj;
    }*/

    /*public static JSONObject getAddress(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException{
        JSONObject jobj=new JSONObject();
		try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            Customer customer=(Customer) session.get(Customer.class, request.getParameter("customerid"));
            jobj.put("shipingAddress",customer.getShippingAddress());
            jobj.put("billingAddress", customer.getBillingAddress());
            jobj.put("currencyid",(customer.getAccount().getCurrency()==null?currency.getCurrencyID(): customer.getAccount().getCurrency().getCurrencyID()));
            jobj.put("currencysymbol",(customer.getAccount().getCurrency()==null?currency.getSymbol(): customer.getAccount().getCurrency().getSymbol()));
            jobj.put("currencyname",(customer.getAccount().getCurrency()==null?currency.getName(): customer.getAccount().getCurrency().getName()));

        } catch (JSONException e) {
                throw ServiceException.FAILURE(e.getCause().getMessage(), e);
        }
        return jobj;
    }*/

    /*public static String saveInvoice(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException{
        String id =null;
		try {
            String taxid=null;
            taxid=request.getParameter("taxid");
            double taxamount=CompanyHandler.getDouble(request, "taxamount");
            Discount discount=null;
            double discValue=0.0;
            double shippingCharges=CompanyHandler.getDouble(request, "shipping");
            double otherCharges=CompanyHandler.getDouble(request, "othercharges");
            boolean inCash=Boolean.parseBoolean(request.getParameter("incash"));
            CompanyAccountPreferences preferences=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            Invoice invoice=new Invoice();
            String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
            String entryNumber=request.getParameter("number");
            String q="from Invoice where invoiceNumber=? and company.companyID=?";
            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
                throw new AccountingException("Invoice number '"+entryNumber+"' already exists.<br>Save again with given auto number.<br>Or type other number");
            invoice.setInvoiceNumber(entryNumber);
            int from=StaticValues.AUTONUM_INVOICE;
            if(inCash)from=StaticValues.AUTONUM_CASHSALE;
            invoice.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, from).equals(entryNumber));
            invoice.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
            invoice.setMemo(request.getParameter("memo"));
            invoice.setBillTo(request.getParameter("billto"));
            invoice.setShipTo(request.getParameter("shipaddress"));
            invoice.setShipDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("shipdate")));
            invoice.setDueDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("duedate")));
            invoice.setCompany(company);
            HashSet hs=new HashSet();
            double[] totals=saveInvoiceRows(session, request, invoice, company, hs,currency);
            double disc=CompanyHandler.getDouble(request, "discount");
            if(disc>0){
                discount=new Discount();
                discount.setDiscount(disc);
                discount.setInPercent(Boolean.parseBoolean(request.getParameter("perdiscount")));
                discount.setOriginalAmount(totals[1]-totals[0]);
                discount.setCompany(company);
                invoice.setDiscount(discount);
                session.save(discount);
                discValue=discount.getDiscountValue();
            }
            discValue+=totals[0];
            JournalEntryDetail jed=new JournalEntryDetail();
            jed.setCompany(company);
            jed.setAmount(totals[1]+shippingCharges+otherCharges-discValue+taxamount);
            if(!inCash){
                jed.setAccount((Account)session.get(Account.class, request.getParameter("customer")));
            }else
                jed.setAccount(preferences.getCashAccount());
            jed.setDebit(true);
            hs.add(jed);
            invoice.setCustomerEntry(jed);

            if(discValue>0){
                jed=new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(discValue);
                jed.setAccount(preferences.getDiscountGiven());
                jed.setDebit(true);
                hs.add(jed);
            }
            if(shippingCharges>0){
                jed=new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(shippingCharges);
                jed.setAccount(preferences.getShippingCharges());
                jed.setDebit(false);
                hs.add(jed);
                invoice.setShipEntry(jed);
            }
            if(otherCharges>0){
                jed=new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(otherCharges);
                jed.setAccount(preferences.getOtherCharges());
                jed.setDebit(false);
                hs.add(jed);
                invoice.setOtherEntry(jed);
            }
            if(taxid!=null&&!taxid.isEmpty()){
                Tax tax=  (Tax)session.get(Tax.class, taxid);
                if(tax==null)
                    throw new  AccountingException("The Tax code(s) used in this transaction has been deleted.");
                    invoice.setTax(tax);
                    if(taxamount>0){
                        jed=new JournalEntryDetail();
                        jed.setCompany(company);
                        jed.setAmount(taxamount);
                        jed.setAccount(tax.getAccount());
                        jed.setDebit(false);
                        hs.add(jed);
                        invoice.setTaxEntry(jed);
                    }

            }
            JournalEntry journalEntry=CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),
            request.getParameter("memo"), "JE"+invoice.getInvoiceNumber(),currencyid, hs,request);
            invoice.setJournalEntry(journalEntry);
            ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),null);
            invoice.setExchangeRateDetail(erd);
            session.saveOrUpdate(invoice);
            id=invoice.getID();
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }

        return id;
    }*/

    /*private static double[] saveInvoiceRows(Session session, HttpServletRequest request, Invoice invoice, Company company, HashSet jeDetails,KWLCurrency currency) throws JSONException, ServiceException, HibernateException, AccountingException, SessionExpiredException, ParseException {
        HashSet hs=new HashSet();
        HashSet rows=new HashSet();
        double totaldiscount=0;
        double totalamount=0;
        String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
        JSONArray jArr=new JSONArray(request.getParameter("detail"));
        for(int i=0;i<jArr.length();i++){
            JSONObject jobj=jArr.getJSONObject(i);
            InvoiceDetail row=new InvoiceDetail();
            SalesOrderDetail rd = (SalesOrderDetail) session.get(SalesOrderDetail.class, jobj.getString("rowid"));
            row.setCompany(company);
            row.setInvoice(invoice);
            row.setSalesorderdetail(rd);
            row.setRate(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("rate"),currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate"))));
            Product product=(Product)session.get(Product.class, jobj.getString("productid"));
            Inventory inventory=CompanyHandler.makeInventory(session, request, product, jobj.getInt("quantity"), jobj.optString("desc"), false, false);
            row.setInventory(inventory);
            totalamount+=row.getRate()*inventory.getQuantity();
            Discount discount=null;
            double disc=jobj.getDouble("prdiscount");
            if(disc!=0.0){
                discount=new Discount();
                discount.setDiscount(disc);
                discount.setOriginalAmount(row.getRate()*row.getInventory().getQuantity());
                discount.setInPercent(true);
                discount.setCompany(company);
                row.setDiscount(discount);
                session.save(discount);
                totaldiscount+=discount.getDiscountValue();
            }
            JournalEntryDetail jed;
            if(hs.add(product.getSalesAccount())){
                jed=new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAccount(product.getSalesAccount());
                jed.setAmount(row.getRate()*inventory.getQuantity());
                jed.setDebit(false);
                jeDetails.add(jed);
            }else{
                Iterator itr=jeDetails.iterator();
                while(itr.hasNext()){
                    jed=(JournalEntryDetail)itr.next();
                    if(jed.getAccount()==product.getSalesAccount()){
                        jed.setAmount(jed.getAmount()+row.getRate()*inventory.getQuantity());
                        break;
                    }
                }
            }
            rows.add(row);
        }
        invoice.setRows(rows);
        return new double[]{totaldiscount,totalamount};
    }*/

    public static void deleteInvoices(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException, JSONException, SessionExpiredException {
        ArrayList params = new ArrayList();
        JSONArray jArr = new JSONArray(request.getParameter("data"));
        String qMarks = "";
        Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
        qMarks = "";
        String cashAccount = ((CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request))).getCashAccount().getID();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            if (jobj.getString("personid").equals(cashAccount)) {
                throw new AccountingException("Payment against the selected Sales Receipt(s) has been received. So, it cannot be deleted.");
            }
            params.add(jobj.getString("billid"));
            qMarks += "?,";
        }
        qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
        params.add(company.getCompanyID());
        String query;
        List list;
        query = "from CreditNoteDetail cn  where cn.invoiceRow.invoice.ID in ( " + qMarks + ") and cn.creditNote.deleted=false and cn.company.companyID=?";
        list = HibernateUtil.executeQuery(session, query, params.toArray());
        if (!list.isEmpty()) {
            throw new AccountingException("Selected record(s) is currently used in the Credit Note(s). So it cannot be deleted.");
        }
        query = "from ReceiptDetail rd  where rd.invoice.ID in ( " + qMarks + ")  and rd.receipt.deleted=false and rd.company.companyID=?";
        list = HibernateUtil.executeQuery(session, query, params.toArray());
        if (!list.isEmpty()) {
            throw new AccountingException("Payment against the selected Invoice(s) has been partially/fully received. So, it cannot be deleted.");
        }
        query = "update Invoice inv set inv.deleted=true where inv.ID in( " + qMarks + ") and inv.company.companyID=?";
        HibernateUtil.executeUpdate(session, query, params.toArray());
        query = "update JournalEntry je set je.deleted=true  where je.ID in(select inv.journalEntry.ID from Invoice inv where inv.ID in( " + qMarks + ") and inv.company.companyID=je.company.companyID) and je.company.companyID=?";
        HibernateUtil.executeUpdate(session, query, params.toArray());
        query = "update Discount di set di.deleted=true  where di.ID in(select inv.discount.ID from Invoice inv where inv.ID in( " + qMarks + ") and inv.company.companyID=di.company.companyID) and di.company.companyID=?";
        HibernateUtil.executeUpdate(session, query, params.toArray());
        query = "update Discount di set di.deleted=true  where di.ID in(select invd.discount.ID from InvoiceDetail invd where invd.invoice.ID in( " + qMarks + ") and invd.company.companyID=di.company.companyID) and di.company.companyID=?";
        HibernateUtil.executeUpdate(session, query, params.toArray());
        query = "update Inventory inv set inv.deleted=true  where inv.ID in(select invd.inventory.ID from InvoiceDetail invd where invd.invoice.ID in( " + qMarks + ") and invd.company.companyID=inv.company.companyID) and inv.company.companyID=?";
        HibernateUtil.executeUpdate(session, query, params.toArray());
    }

    public static JSONObject getAgedReceivable(Session session, HttpServletRequest request, String start, String limit) throws ServiceException, HibernateException, JSONException{
        JSONObject jobj=getAgedReceivable(session,request);
        if(!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)){
            try {
                int st = Integer.parseInt(start);
                JSONArray jArr = new JSONArray(), temp = jobj.getJSONArray("data");
                int ed = Math.min(temp.length(),st+Integer.parseInt(limit));
                for (int i = st; i < ed; i++)
                    jArr.put(temp.getJSONObject(i));
                jobj.put("data", jArr);
                jobj.put("count", temp.length());
            } catch (NumberFormatException ne) {
                throw ServiceException.FAILURE("CompanyHandler.getInvoices", ne);
            } catch (JSONException jse) {
                throw ServiceException.FAILURE("CompanyHandler.getInvoices", jse);
            }
        }
        return jobj;
    }

     public static JSONObject getAgedReceivable(Session session,HttpServletRequest request) throws ServiceException, JSONException {
        JSONObject jObj1 = new JSONObject();
        try {
            session = HibernateUtil.getCurrentSession();
            boolean withInventory =Boolean.parseBoolean(request.getParameter("withinventory"));
            if(withInventory)
                jObj1 = CustomerHandler.getInvoices(session, request, null, null,null);
            else
                jObj1 = CustomerHandler.getBillingInvoices(session, request, null, null,null);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj1;
    }
     
     /*public static JSONArray getCustomerInvoiceAgedReceivable(Session session,HttpServletRequest request, Boolean isInvoice) throws ServiceException, JSONException, SessionExpiredException, ParseException {
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
                condition += " and c.account.ID=? ";
             }
            String q = "select ID from Customer c where company.companyID= ?"+condition;
            Iterator itrcust = HibernateUtil.executeQuery(session, q,params.toArray()).iterator();   
            while (itrcust.hasNext()) {
                amountdue1=amountdue2=amountdue3=amountdue4=0;
                JSONObject invJObj = new JSONObject();
                String personID=null;
                String personName=null;
                Object custid=itrcust.next();
                if(isInvoice)
                    invJObj = CustomerHandler.getInvoices(session, request, null, null,custid.toString());
                else
                    invJObj = CustomerHandler.getBillingInvoices(session, request, null, null,custid.toString());
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
     
   /*public static JSONObject getCustomerAgedReceivable(Session session, HttpServletRequest request, String start, String limit) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONArray invJArr = new JSONArray();
        JSONObject jObj = new JSONObject();

        try {
            boolean withInventory =Boolean.parseBoolean(request.getParameter("withinventory"));
            if(withInventory)
             invJArr= CustomerHandler.getCustomerInvoiceAgedReceivable(session, request,true);
            else
             invJArr= CustomerHandler.getCustomerInvoiceAgedReceivable(session, request,false);
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

    public static JSONObject getInvoices(Session session, HttpServletRequest request, String start, String limit,String custid) throws ServiceException, HibernateException{
        JSONObject jobj=getInvoices(session, request,custid);
        if(!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)){
            try {
                int st = Integer.parseInt(start);
                JSONArray jArr = new JSONArray(), temp = jobj.getJSONArray("data");
                int ed = Math.min(temp.length(),st+Integer.parseInt(limit));
                for (int i = st; i < ed; i++)
                    jArr.put(temp.getJSONObject(i));
                jobj.put("data", jArr);
                jobj.put("count", temp.length());
            } catch (NumberFormatException ne) {
                throw ServiceException.FAILURE("CompanyHandler.getInvoices", ne);
            } catch (JSONException jse) {
                throw ServiceException.FAILURE("CompanyHandler.getInvoices", jse);
            }
        }
        return jobj;
    }


    public static JSONObject getInvoicesCurrencyRate(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException{
        JSONObject jobj=new JSONObject();
        try {
            String customerid = request.getParameter("accid");
            ArrayList params = new ArrayList();
            String condition = "";
            params.add(AuthHandler.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(customerid)) {
                params.add(customerid);
                condition += " and inv.customerEntry.account.ID=?";
            }
            String query = "from Invoice inv where inv.deleted=false and inv.company.companyID=? " + condition + " order by inv.ID";
            List list = HibernateUtil.executeQuery(session, query, params.toArray());
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                Invoice invoice = (Invoice) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("billid", invoice.getID());
                obj.put("billno", invoice.getInvoiceNumber());
                JSONObject currObj = CompanyHandler.getCurrencyExchange(session,request,invoice.getJournalEntry().getEntryDate());
                JSONArray jarr2 = currObj.getJSONArray("data");
                for (int j = 0; j < jarr2.length(); j++) {
                    jarr2.getJSONObject(j).put("billid",invoice.getID());
                    jArr.put(jarr2.getJSONObject(j));
                }
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    public static JSONObject getInvoices(Session session, HttpServletRequest request, String custid) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
        try {
            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            Calendar startFinYearCal = Calendar.getInstance();
            Calendar endFinYearCal = Calendar.getInstance();
            startFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.add(Calendar.YEAR,1);
            String customerid = custid==null?request.getParameter("accid"):custid;
            String ss=request.getParameter("ss");
            String cashAccount=((CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request))).getCashAccount().getID();
            boolean cashonly=false;
            boolean creditonly=false;
            boolean personGroup=false;
            boolean isagedgraph=false;
            
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
                condition += " and inv.customerEntry.account.ID=?";
            } else {
                String q = "select ID from Customer where company.companyID=?";
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
                    condition += " and inv.customerEntry.account.ID in (" + qMarks + ")";
            }

            if (!StringUtil.isNullOrEmpty(dueDate)) {
                params.add(AuthHandler.getDateFormatter(request).parse(dueDate));
                condition += " and inv.dueDate<=?";
            }
            if(StringUtil.isNullOrEmpty(ss)==false){
               for(int i=0;i<=4;i++){
                 params.add(ss+"%");
               }
                 condition+= " and (inv.invoiceNumber like ? or inv.billTo like ?  or inv.journalEntry.entryNumber like ? or inv.memo like ? or inv.customerEntry.account.name like ? ) ";
            }
            if(personGroup){
                params.add(startFinYearCal.getTime());
                 params.add(endFinYearCal.getTime());
                 condition+=" and inv.dueDate>=? and inv.dueDate<=?";
            }
             if(isagedgraph){
                 params.add(startFinYearCal.getTime());
                 params.add(endFinYearCal.getTime());
                 condition+=" and inv.dueDate>=? and inv.dueDate<=?";
            }
            String query = "from Invoice inv where inv.deleted=false and inv.company.companyID=? " + condition +group+ " order by inv.ID";
            List list = HibernateUtil.executeQuery(session, query, params.toArray());
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                double taxPercent=0;
                Invoice invoice = (Invoice) itr.next();
                JournalEntry je = invoice.getJournalEntry();
                JournalEntryDetail d = invoice.getCustomerEntry();
                Account account = d.getAccount();
                KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
                String currencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
                double amount = 0, ramount = 0;
                Iterator itrCn = applyCreditNotes(session,request, invoice).values().iterator();
                while (itrCn.hasNext()) {
                    Object[] temp = (Object[]) itrCn.next();
                    amount += (Double) temp[0]-(Double) temp[2];
                }
                JournalEntryDetail tempd = invoice.getShipEntry();
                if (tempd != null) {
                    amount += tempd.getAmount();
                }
                tempd = invoice.getOtherEntry();
                if (tempd != null) {
                    amount += tempd.getAmount();
                }
                tempd = invoice.getTaxEntry();
                if (tempd != null) {
                    amount += tempd.getAmount();
                }
                String q = "select  rd from ReceiptDetail rd where rd.receipt.deleted=false and rd.invoice.ID=?";
                List l = HibernateUtil.executeQuery(session, q, invoice.getID());

                Iterator recitr = l.iterator();
                while (recitr.hasNext()) {
                    ReceiptDetail rd=(ReceiptDetail) recitr.next();
                    ramount+=rd.getAmount();
                    String fromcurrencyid=(rd.getReceipt().getCurrency()==null?currency.getCurrencyID():rd.getReceipt().getCurrency().getCurrencyID());
                   ramount=CompanyHandler.getOneCurrencyToOther(session,request,ramount,fromcurrencyid,currencyid,invoice.getJournalEntry().getEntryDate());
                }

                JSONObject obj = new JSONObject();
                obj.put("billid", invoice.getID());
                obj.put("personid", account.getID());
                obj.put("billno", invoice.getInvoiceNumber());
                obj.put("currencyid",currencyid);
                obj.put("currencysymbol",(invoice.getCurrency()==null?currency.getSymbol(): invoice.getCurrency().getSymbol()));
                obj.put("companyaddress", invoice.getCompany().getAddress());
                obj.put("companyname", invoice.getCompany().getCompanyName());
                obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,je.getEntryDate()));
                obj.put("billto", invoice.getBillTo());
                obj.put("shipto", invoice.getShipTo());
                obj.put("journalentryid", je.getID());
                obj.put("entryno", je.getEntryNumber());
                obj.put("date", AuthHandler.getDateFormatter(request).format(je.getEntryDate()));
                obj.put("shipdate", AuthHandler.getDateFormatter(request).format(invoice.getShipDate()));
                obj.put("duedate", AuthHandler.getDateFormatter(request).format(invoice.getDueDate()));
                obj.put("personname", account.getName());
                obj.put("memo", invoice.getMemo());
                obj.put("taxamount", invoice.getTaxEntry()==null?0:invoice.getTaxEntry().getAmount());
                obj.put("discount", invoice.getDiscount()==null?0:invoice.getDiscount().getDiscountValue());
                if(account.getID().equals(cashAccount)){
                      obj.put("amountdue",0);
                      obj.put("amountdueinbase",0);
                      obj.put("incash",true);
                }
                else{
                    obj.put("amountdueinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,amount - ramount,currencyid,je.getEntryDate()));  //amount left after apllying receipt and CN
                    obj.put("amountdue", amount - ramount);
                }
                obj.put("amountduenonnegative", (amount - ramount<=0)?0:amount - ramount);
                obj.put("amount", d.getAmount());  //actual invoice amount
                obj.put("amountinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,d.getAmount(),currencyid,je.getEntryDate()));
                if(invoice.getTax()!=null)
                    taxPercent=CompanyHandler.getTaxPercent(session, request,je.getEntryDate(),invoice.getTax().getID());

                obj.put("taxpercent", taxPercent);  //tax in percent applyind on invoice
                 if (!(ignoreZero && amount - ramount <= 0)) {
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }


      public static JSONObject getTopCustomersChart(Session session, HttpServletRequest request, int limit) throws ServiceException, HibernateException {
        JSONObject jobj=new JSONObject();
        try {
            ArrayList params = new ArrayList();
            int i=0;
            params.add(AuthHandler.getCompanyid(request));
            String query=null;
            boolean withInventory =Boolean.parseBoolean(request.getParameter("withinventory"));
            if(withInventory)
                query = "select sum(inv.customerEntry.amount/inv.exchangeRateDetail.exchangeRate) as invoiceamount," +
                    "acc.name, "+
                    "acc.id"+
                    " from Invoice as inv "+
                    ", JournalEntryDetail as jed, Account as acc where jed.ID=inv.customerEntry.ID and jed.account.ID=acc.ID"+
                    " and inv.company.companyID=?  group by acc.name ";
            else
               query = "select sum(inv.customerEntry.amount/inv.exchangeRateDetail.exchangeRate) as invoiceamount," +
                "acc.name, "+
                "acc.id"+
                " from BillingInvoice as inv "+
                ", JournalEntryDetail as jed, Account as acc where jed.ID=inv.customerEntry.ID and jed.account.ID=acc.ID"+
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
        }catch (HibernateException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    private static HashMap applyCreditNotes(Session session,HttpServletRequest request,Invoice invoice) throws ServiceException, SessionExpiredException, JSONException{
        HashMap hm=new HashMap();
        Set invRows=invoice.getRows();
        Iterator itr=invRows.iterator();
        KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
        double amount;
        int quantity;
        double disc=(invoice.getDiscount()==null?0:invoice.getDiscount().getDiscountValue())/invRows.size();
        while(itr.hasNext()){
            InvoiceDetail temp=(InvoiceDetail)itr.next();
            quantity=temp.getInventory().getQuantity();
            amount=temp.getRate()*quantity;
            double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
            hm.put(temp, new Object[]{amount-rdisc-disc,quantity,0.0});
            if(invoice==null)invoice=temp.getInvoice();
        }
        String query="select cn, cnr, cnd from CreditNote cn left join cn.rows cnr left join cn.discounts cnd where cn.deleted=false and (cnr.invoiceRow.invoice.ID=? or cnd.invoice.ID=?) order by cn.sequence";
        Iterator cnitr=HibernateUtil.executeQuery(session, query, new Object[]{invoice.getID(),invoice.getID()}).iterator();
        double taxAmount=0;
        while(cnitr.hasNext()){
            Object[] cnrow=(Object[])cnitr.next();
            CreditNoteDetail cnr=(CreditNoteDetail)cnrow[1];
            InvoiceDetail temp=cnr.getInvoiceRow();
            if(!hm.containsKey(temp))continue;
            Object[] val=(Object[])hm.get(temp);
            String fromcurrencyid=(cnr.getCreditNote().getCurrency()==null?currency.getCurrencyID(): cnr.getCreditNote().getCurrency().getCurrencyID());
            String tocurrencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
            double v=(Double)val[0]-(cnr.getDiscount()==null?0:CompanyHandler.getOneCurrencyToOther(session,request,cnr.getDiscount().getDiscountValue(),fromcurrencyid,tocurrencyid,invoice.getJournalEntry().getEntryDate()));
            if(cnr.getTaxAmount()!=null)
                taxAmount+=cnr.getTaxAmount();
            int q=(Integer)val[1];
            if(temp.getInventory()!=null)
               q-=cnr.getQuantity();
            hm.put(temp, new Object[]{v,q,taxAmount});
        }
        return hm;
    }

     private static HashMap getInvoiceProductAmount(Session session,Invoice invoice) throws ServiceException{
        HashMap hm=new HashMap();
        Set invRows=invoice.getRows();
        Iterator itr=invRows.iterator();
        double amount;
        int quantity;
        while(itr.hasNext()){
            InvoiceDetail temp=(InvoiceDetail)itr.next();
            quantity=temp.getInventory().getQuantity();
            amount=temp.getRate()*quantity;
            double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
            hm.put(temp, new Object[]{amount-rdisc,quantity});
            if(invoice==null)invoice=temp.getInvoice();
        }
        return hm;
    }

    /*public static JSONObject getInvoiceRows(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException{
        JSONObject jobj=new JSONObject();
		try {
            String[] invoices=request.getParameterValues("bills");
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            int i=0;
            JSONArray jArr=new JSONArray();
            while(invoices!=null&&i<invoices.length){
                Invoice invoice=(Invoice)session.get(Invoice.class, invoices[i]);
                Iterator itr=invoice.getRows().iterator();
                HashMap hm=applyCreditNotes(session,request,invoice);
                while(itr.hasNext()) {
                    InvoiceDetail row=(InvoiceDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", invoice.getID());
                    String currencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
                    obj.put("currencysymbol", (invoice.getCurrency()==null?currency.getSymbol():invoice.getCurrency().getSymbol()));
                    obj.put("billno", invoice.getInvoiceNumber());
                    obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,invoice.getJournalEntry().getEntryDate()));
                    Inventory inv=row.getInventory();
                    Product prod=inv.getProduct();
                    obj.put("rowid", row.getID());
                    obj.put("productid", prod.getID());
                    obj.put("productname",prod.getName());
                    Discount disc= row.getDiscount();
                    if(disc!=null&&disc.isInPercent())
                        obj.put("prdiscount",disc.getDiscount()); //product discount in percent
                    else
                        obj.put("prdiscount",0);
                    obj.put("desc", inv.getDescription());
                    obj.put("rate", row.getRate());  //rate of the product
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
                    HashMap amthm=getInvoiceProductAmount(session,invoice);
                    Object[] val=(Object[])amthm.get(row);
                    amount=(Double)val[0];
                    obj.put("orignalamount", amount);
                    double percent=0;
                    if(invoice.getTax()!=null)
                        percent=CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(),invoice.getTax().getID());
                    obj.put("taxpercent", percent);
                jArr.put(obj);
                i++;
            }
            jobj.put("data", jArr);
            }
        } catch (JSONException e) {
                throw ServiceException.FAILURE("CompanyHandler.getInvoiceRows", e);
        }
        return jobj;
    }*/

   /* public static void saveCreditNote(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException, SessionExpiredException {
        try {
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KWLCurrency kwlcurrency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            CreditNote creditnote = new CreditNote();
            String entryNumber=request.getParameter("number");
            String currencyid=(request.getParameter("currencyid")==null?kwlcurrency.getCurrencyID():request.getParameter("currencyid"));
            KWLCurrency currency=(KWLCurrency)session.get(KWLCurrency.class,currencyid);
            String q="from CreditNote where creditNoteNumber=? and company.companyID=?";
            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
                throw new AccountingException("Credit note number '"+entryNumber+"' already exists.");
            creditnote.setCreditNoteNumber(entryNumber);
            creditnote.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_CREDITNOTE).equals(entryNumber));
            creditnote.setMemo(request.getParameter("memo"));
            creditnote.setDeleted(false);
            creditnote.setCompany(company);
            creditnote.setCurrency(currency);
            Long seqNumber = null;
            String query = "select count(cn.ID) from CreditNote cn inner join cn.journalEntry je  where cn.company.companyID=? and je.entryDate<=?";
            List list = HibernateUtil.executeQuery(session, query, new Object[]{AuthHandler.getCompanyid(request), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate"))});
            if (!list.isEmpty()) {
                seqNumber = (Long) list.get(0);
            }
            creditnote.setSequence(seqNumber.intValue());
            HashSet hs = new HashSet();
            Double totalAmount = saveCreditNoteRows(session, request, creditnote, company, hs, preferences,kwlcurrency);
//            double amountDiff=oldcreditnoteRowsAmount(session, request, creditnote, company, jArr);
//
            //TODO   saveCreditNoteDiscountRows(session, request, creditnote, company);

//            if(jArr.length()>0){
//                amount = saveReceiptRows(session, request, receipt, company, jArr);
//                amountDiff=oldReceiptRowsAmount(session, request, receipt, company, jArr);
//                if(amountDiff-amount!=0&&preferences.getForeignexchange()!=null){
//                    amount=amount+(amountDiff-amount);
//                    hs = new HashSet();
                    JournalEntryDetail jed = new JournalEntryDetail();
                    jed.setCompany(company);
//                    jed.setAmount(amountDiff-amount);
//                    jed.setAccount(preferences.getForeignexchange());
//                    jed.setDebit(true);
//                    hs.add(jed);
//                }
//            }
//            JournalEntryDetail jed = new JournalEntryDetail();
//            jed.setCompany(company);
            jed.setAmount(totalAmount);
            jed.setAccount((Account) session.get(Account.class, request.getParameter("accid")));
            jed.setDebit(false);
            hs.add(jed);
            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
                    request.getParameter("memo"), "JE" + creditnote.getCreditNoteNumber(),currencyid, hs,request);
            creditnote.setJournalEntry(journalEntry);
            ProfileHandler.insertAuditLog(session, AuditAction.CREDIT_NOTE_CREATED, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) +" created new credit note ", request);
            session.saveOrUpdate(creditnote);
        } catch (JSONException je) {
            throw ServiceException.FAILURE("CompanyHandler.saveCreditNote", je);
        } catch (ParseException pe) {
            throw ServiceException.FAILURE("CompanyHandler.saveCreditNote", pe);
        } catch (SessionExpiredException see) {
            throw ServiceException.FAILURE("CompanyHandler.saveCreditNote", see);
        }
    }*/
// private static double oldcreditnoteRowsAmount(Session session, HttpServletRequest request, CreditNote receipt, Company company, JSONArray jArr) throws JSONException, ServiceException, HibernateException, SessionExpiredException, ParseException {
//        double ratio=0;
//        double amount = 0;
//        for (int i = 0; i < jArr.length(); i++) {
//            JSONObject jobj = jArr.getJSONObject(i);
//            Invoice invoice=(Invoice) session.get(Invoice.class, jobj.getString("billid"));
//            Double recinvamount = jobj.getDouble("payment");
//            double oldrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,invoice.getCurrency().getCurrencyID(),invoice.getJournalEntry().getEntryDate());
//            double  newrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,invoice.getCurrency().getCurrencyID(),AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
//            ratio=newrate/oldrate;
//            amount+=recinvamount*ratio;
//        }
//        return (amount);
//    }
    /*private static Double saveCreditNoteRows(Session session, HttpServletRequest request, CreditNote cn, Company company, HashSet hs, CompanyAccountPreferences preferences,KWLCurrency currency) throws JSONException, ServiceException, HibernateException, AccountingException, SessionExpiredException {
        double totalAmount = 0;
        double totalTax=0;
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
            CreditNoteDetail row = new CreditNoteDetail();
            double disc = jobj.getDouble("discamount");
            row.setCompany(company);
            row.setCreditNote(cn);
            row.setMemo(jobj.optString("desc"));
            row.setQuantity(jobj.getInt("remquantity"));
            InvoiceDetail invoiceRow = (InvoiceDetail) session.get(InvoiceDetail.class, jobj.getString("rowid"));
            row.setInvoiceRow(invoiceRow);
            Product product = invoiceRow.getInventory().getProduct();
            account = (Account) session.get(Account.class, product.getSalesReturnAccount().getID());
            double percent=0;
            if(invoiceRow.getInvoice().getTax()!=null)
                    percent=CompanyHandler.getTaxPercent(session, request, invoiceRow.getInvoice().getJournalEntry().getEntryDate(),invoiceRow.getInvoice().getTax().getID());
            if (jobj.getInt("typeid") == 2 || jobj.getInt("typeid") == 3) {
                Inventory inventory = CompanyHandler.makeInventory(session, request, product, jobj.getInt("remquantity"), jobj.optString("desc"), true, (jobj.getInt("typeid") == 3 ? false : true));
                row.setInventory(inventory);
                if (list.contains(account)) {
                    rows.add(row);
                    continue;
                }
                for (int k = 0; k < jArr.length(); k++) {
                    JSONObject jobj1 = jArr.getJSONObject(k);
                    InvoiceDetail compInvoiceRow = (InvoiceDetail) session.get(InvoiceDetail.class, jobj1.getString("rowid"));
                    Product compProduct = compInvoiceRow.getInventory().getProduct();
                    Account compAccount = (Account) session.get(Account.class, compProduct.getSalesReturnAccount().getID());
                    if (account == compAccount && (jobj1.getInt("typeid") == 2 || jobj.getInt("typeid") == 3)) {
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
                                jed.setAccount(invoiceRow.getInvoice().getTax().getAccount());
                                jed.setDebit(true);
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
                        jed.setAccount(invoiceRow.getInvoice().getTax().getAccount());
                        jed.setDebit(true);
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
            jed.setDebit(true);
            hs.add(jed);
            totalAmount += amount;
        }
        if (discAccAmount != 0.0) {
            jed = new JournalEntryDetail();
            jed.setCompany(company);
            jed.setAmount(discAccAmount);
            jed.setAccount((Account) session.get(Account.class, preferences.getDiscountGiven().getID()));
            jed.setDebit(true);
            hs.add(jed);
            totalAmount += discAccAmount;
        }
        cn.setRows(rows);
        return totalAmount+totalTax;
    }*/

    private static void saveCreditNoteDiscountRows(Session session, HttpServletRequest request, CreditNote cn, Company company) throws JSONException, HibernateException {
        HashSet rows = new HashSet();
        List list = new ArrayList();
        JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
        for (int i = 0; i < jArr.length(); i++) {
            Double disc = 0.0;
            Double totalAmount = 0.0;
            JSONObject jobj = jArr.getJSONObject(i);
            Invoice invoice = (Invoice) session.get(Invoice.class, jobj.getString("billid"));
            if (list.contains(invoice)) {
                continue;
            }
            for (int k = 0; k < jArr.length(); k++) {
                JSONObject jobj1 = jArr.getJSONObject(k);
                Invoice compInvoice = (Invoice) session.get(Invoice.class, jobj1.getString("billid"));
                if (invoice == compInvoice) {
                    disc = disc + jobj1.getDouble("discamount");
                    totalAmount += jobj.getDouble("amount");
                    list.add(compInvoice);
                }
            }

            if (disc != 0.0) {
                CreditNoteDiscount row = new CreditNoteDiscount();
                row.setCompany(company);
                row.setCreditNote(cn);
                row.setInvoice(invoice);
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
        cn.setDiscounts(rows);
    }

    /*public static JSONObject getCreditNote(Session session, HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String ss = request.getParameter("ss");
            ArrayList params = new ArrayList();
            params.add(AuthHandler.getCompanyid(request));
            String condition = " where ac.ID=c.account.ID and cn.deleted=false and cn.company.companyID=?";
            if (StringUtil.isNullOrEmpty(ss) == false) {
                for(int i=0;i<=3;i++){
                    params.add(ss + "%");
                }
                 condition += " and ( cn.creditNoteNumber like ? or c.name like ? or cn.journalEntry.entryNumber like ? or cn.memo like ? ) ";
            }
            String query = "select cn, c, jed from CreditNote cn inner join cn.journalEntry je inner join je.details jed inner join jed.account ac, Customer c" + condition;
            List list = HibernateUtil.executeQuery(session, query, params.toArray());
            int count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = HibernateUtil.executeQueryPaging(session, query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                CreditNote creditMemo = (CreditNote) row[0];
                Customer customer = (Customer) row[1];
                JournalEntryDetail details = (JournalEntryDetail) row[2];
                JSONObject obj = new JSONObject();
                obj.put("noteid", creditMemo.getID());
                obj.put("noteno", creditMemo.getCreditNoteNumber());
                obj.put("journalentryid", creditMemo.getJournalEntry().getID());
                obj.put("currencysymbol", (creditMemo.getCurrency()==null?currency.getSymbol():creditMemo.getCurrency().getSymbol()));
                obj.put("entryno", creditMemo.getJournalEntry().getEntryNumber());
                obj.put("personid", customer.getID());
                obj.put("personname", customer.getAccount().getName());
                obj.put("amount", details.getAmount());
                obj.put("date", AuthHandler.getDateFormatter(request).format(creditMemo.getJournalEntry().getEntryDate()));
                obj.put("memo", creditMemo.getMemo());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getCreditNote", ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.getCreditNote", ex);
        }

        return jobj;
    }*/

    /*public static JSONObject getCreditNoteRows(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));            
            String[] creditNote = request.getParameterValues("bills");
            int i = 0;
            JSONArray jArr = new JSONArray();
            while (creditNote != null && i < creditNote.length) {
                CreditNote cn = (CreditNote) session.get(CreditNote.class, creditNote[i]);
                Iterator itr = cn.getRows().iterator();
                while (itr.hasNext()) {
                    CreditNoteDetail row = (CreditNoteDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", cn.getID());
                    obj.put("billno", cn.getCreditNoteNumber());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getInvoiceRow().getInventory().getProduct().getID());
                    obj.put("productname", row.getInvoiceRow().getInventory().getProduct().getName());
                    obj.put("desc", row.getInvoiceRow().getInventory().getProduct().getDescription());
                    obj.put("memo", row.getMemo());
                    obj.put("currencysymbol", (cn.getCurrency()==null?currency.getCurrencyID():cn.getCurrency().getSymbol()));
                    obj.put("transectionid", row.getInvoiceRow().getInvoice().getID());
                    obj.put("transectionno", row.getInvoiceRow().getInvoice().getInvoiceNumber());
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
            throw ServiceException.FAILURE("CompanyHandler.getCreditNoteRows", ex);
        }
        return jobj;
    }*/

    /*public static void saveReceipt(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException{
        try {
            Account dipositTo = null;
            Receipt receipt = new Receipt();
            double amount = 0;
            double amountDiff = 0;
            HashSet hs = new HashSet();
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            String entryNumber=request.getParameter("no");
            String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
            String q="from Receipt where receiptNumber=? and company.companyID=?";
            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
                throw new AccountingException("Receipt number '"+entryNumber+"' already exists.");
            receipt.setReceiptNumber(entryNumber);
            receipt.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_RECEIPT).equals(entryNumber));
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
                receipt.setPayDetail(pdetail);
            }
            receipt.setMemo(request.getParameter("memo"));
            receipt.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
            receipt.setDeleted(false);
            receipt.setCompany(company);
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            hs = new HashSet();
            if(jArr.length()>0){
                amount = saveReceiptRows(session, request, receipt, company, jArr);
                amountDiff=oldReceiptRowsAmount(session, request, jArr,currencyid);
                if(preferences.getForeignexchange()==null)
                    throw new AccountingException("Please Set Foreign Exchange account in Account Preference first");
                if(amountDiff!=0&&preferences.getForeignexchange()!=null){
                    hs = new HashSet();
                    JournalEntryDetail jed = new JournalEntryDetail();
                    jed.setCompany(company);
                    jed.setAmount(amountDiff);
                    jed.setAccount(preferences.getForeignexchange());
                    jed.setDebit(true);
                    hs.add(jed);
                }

            }

            else amount= Double.parseDouble(request.getParameter("amount"));

            JournalEntryDetail jed = new JournalEntryDetail();
            jed.setCompany(company);
            jed.setAmount(amount+amountDiff);
            jed.setAccount(((Account) session.get(Account.class, request.getParameter("accid"))));
            jed.setDebit(false);
            hs.add(jed);
            jed = new JournalEntryDetail();
            jed.setCompany(company);
            jed.setAmount(amount);
            jed.setAccount(dipositTo);
            jed.setDebit(true);
            hs.add(jed);
            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
                    request.getParameter("memo"), "JE" + receipt.getReceiptNumber(),currencyid, hs,request);
            jed.setJournalEntry(journalEntry);
            receipt.setJournalEntry(journalEntry);
            session.saveOrUpdate(receipt);
            ProfileHandler.insertAuditLog(session, AuditAction.RECEIPT_ADDED, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) +" created new receipt ", request);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }*/
    /*private static double oldReceiptRowsAmount(Session session, HttpServletRequest request, JSONArray jArr,String currencyid) throws JSONException, ServiceException, HibernateException, SessionExpiredException, ParseException {
        double ratio=0;
        double amount = 0;
        for (int i = 0; i < jArr.length(); i++){
            JSONObject jobj = jArr.getJSONObject(i);
            Invoice invoice=(Invoice) session.get(Invoice.class, jobj.getString("billid"));
            Double recinvamount = jobj.getDouble("payment");
            
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String currid=currency.getCurrencyID();
            if(invoice.getCurrency()!=null)
                 currid=invoice.getCurrency().getCurrencyID();
            double oldrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,currid,invoice.getJournalEntry().getEntryDate());
            double  newrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,currid,AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            ratio=oldrate-newrate;
            amount+=recinvamount*ratio;

        }
        amount=CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
        return (amount);
    }*/
    /*private static double saveReceiptRows(Session session, HttpServletRequest request, Receipt receipt, Company company, JSONArray jArr) throws JSONException, ServiceException, HibernateException, SessionExpiredException {
        HashSet hs = new HashSet();
        double amount = 0;
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            ReceiptDetail rd = new ReceiptDetail();
            rd.setAmount(jobj.getDouble("payment"));
            rd.setCompany(company);
            rd.setInvoice((Invoice) session.get(Invoice.class, jobj.getString("billid")));
            rd.setReceipt(receipt);
            amount += jobj.getDouble("payment");
            hs.add(rd);
        }
        receipt.setRows(hs);
        return amount;
    }*/
    /*public static JSONObject getReceipts(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String start=request.getParameter("start");
            String limit=request.getParameter("limit");
            String ss=request.getParameter("ss");
            ArrayList params=new ArrayList();
            params.add(AuthHandler.getCompanyid(request));
            String condition=" where r.deleted=false and  r.company.companyID=?  and jed.debit=false ";
            if(StringUtil.isNullOrEmpty(ss)==false){
               params.add(ss+"%");
               condition+= " and (ac.name like ? ) ";
           }
           String query="select r, ac from Receipt r inner join r.journalEntry je inner join je.details jed inner join jed.account ac"+condition;
           List list = HibernateUtil.executeQuery(session, query,params.toArray());
           int count=list.size();
           if(StringUtil.isNullOrEmpty(start)==false&&StringUtil.isNullOrEmpty(limit)==false){
                list=HibernateUtil.executeQueryPaging(session, query,params.toArray(), new Integer[]{Integer.parseInt(start),Integer.parseInt(limit)});
            }
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Object[] row=(Object[])itr.next();
                Receipt receipt = (Receipt) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                Customer customer = (Customer) session.get(Customer.class, acc.getID());
                if(customer!=null)
                    obj.put("address", customer.getBillingAddress());
                else {
                    Vendor vendor = (Vendor) session.get(Vendor.class, acc.getID());
                    if(vendor!=null)
                        obj.put("address", vendor.getAddress());
                }
                obj.put("billid", receipt.getID());
                obj.put("entryno", receipt.getJournalEntry().getEntryNumber());
                obj.put("journalentryid", receipt.getJournalEntry().getID());
                obj.put("personid", acc.getID());
                obj.put("billno", receipt.getReceiptNumber());
                obj.put("billdate", AuthHandler.getDateFormatter(request).format(receipt.getJournalEntry().getEntryDate()));//receiptdate
                Iterator itrRow=receipt.getRows().iterator();
                double amount=0;
                if(!receipt.getRows().isEmpty())
                    while(itrRow.hasNext()){
                        amount+=((ReceiptDetail)itrRow.next()).getAmount();
                    }
                else{
                    itrRow=receipt.getJournalEntry().getDetails().iterator();
                    amount+=((JournalEntryDetail)itrRow.next()).getAmount();
                }
                obj.put("amount", amount);
                obj.put("personname", acc.getName());
                obj.put("memo", receipt.getMemo());
                obj.put("currencysymbol", (receipt.getCurrency()==null?currency.getSymbol():receipt.getCurrency().getSymbol()));
                obj.put("paymentmethod",(receipt.getPayDetail()==null?"Cash":receipt.getPayDetail().getPaymentMethod().getMethodName()));
                if(receipt.getPayDetail()!=null){
                    obj.put("refno",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":receipt.getPayDetail().getCheque().getChequeNo()):receipt.getPayDetail().getCard().getRefNo()));
                    obj.put("refname",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":receipt.getPayDetail().getCheque().getBankName()):receipt.getPayDetail().getCard().getCardHolder()));
                }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CustomerManager.getReciepts", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CustomerManager.getReciepts", ex);
        }
        return jobj;
    }*/

    /*public static void saveSalesOrder(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException{
        try {
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            SalesOrder salesOrder = new SalesOrder();
            String entryNumber = request.getParameter("number");
            String q = "from SalesOrder where salesOrderNumber=? and company.companyID=?";
            if (!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
                throw new AccountingException("Sales Order number '" + entryNumber + "' already exists.");
            salesOrder.setSalesOrderNumber(entryNumber);
     //       salesOrder.setCurrency((KWLCurrency)session.get(KWLCurrency.class,request.getParameter("currencyid")));
            salesOrder.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_SALESORDER).equals(entryNumber));
            salesOrder.setMemo(request.getParameter("memo"));
            salesOrder.setCustomer((Customer) session.get(Customer.class, request.getParameter("customer")));
            salesOrder.setOrderDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")));
            salesOrder.setDueDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("duedate")));
            salesOrder.setCompany(company);
            saveSalesOrderRows(session, request, salesOrder, company);
            session.saveOrUpdate(salesOrder);
        } catch (ParseException ex) {
            throw ServiceException.PARSE_ERROR("CustomerManager.saveSalesOrder", ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CustomerManager.saveSalesOrder", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CustomerManager.saveSalesOrder", ex);
        }
    }*/

    /*private static void saveSalesOrderRows(Session session, HttpServletRequest request, SalesOrder purchaseOrder, Company company) throws JSONException, ServiceException, HibernateException, SessionExpiredException {
        HashSet rows=new HashSet();
        JSONArray jArr=new JSONArray(request.getParameter("detail"));
        for(int i=0;i<jArr.length();i++){
            JSONObject jobj=jArr.getJSONObject(i);
            SalesOrderDetail row=new SalesOrderDetail();
            row.setCompany(company);
            row.setSalesOrder(purchaseOrder);
            row.setProduct((Product)session.get(Product.class, jobj.getString("productid")));
            row.setRate(jobj.getDouble("rate"));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
            row.setQuantity( jobj.getInt("quantity"));
            row.setRemark(jobj.optString("remark"));
            rows.add(row);
        }
        purchaseOrder.setRows(rows);
    }*/

    /*public static JSONObject getSalesOrders(Session session, HttpServletRequest request) throws ServiceException {
        JSONObject jobj=new JSONObject();
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
                condition += " and ( salesOrderNumber like ? or so.memo like ? or so.customer.name like ? )";
            }
            String query = "from SalesOrder so" + condition;
            List list = HibernateUtil.executeQuery(session, query, params.toArray());
            int count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = HibernateUtil.executeQueryPaging(session, query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            boolean closeflag = request.getParameter("closeflag")!=null?true:false;
            while (itr.hasNext()) {
                SalesOrder salesOrder=(SalesOrder)itr.next();
                Customer customer=salesOrder.getCustomer();
                KWLCurrency currency=salesOrder.getCustomer().getAccount().getCurrency()==null?kwlcurrency:salesOrder.getCustomer().getAccount().getCurrency();
                JSONObject obj = new JSONObject();
                obj.put("billid", salesOrder.getID());
                obj.put("personid", customer.getID());
                obj.put("billno", salesOrder.getSalesOrderNumber());
                obj.put("duedate", AuthHandler.getDateFormatter(request).format(salesOrder.getDueDate()));
                obj.put("date", AuthHandler.getDateFormatter(request).format(salesOrder.getOrderDate()));
                Iterator itrRow = salesOrder.getRows().iterator();
                double amount = 0;
                while (itrRow.hasNext()) {
                    SalesOrderDetail sod= (SalesOrderDetail) itrRow.next();
                    amount+=sod.getQuantity()*sod.getRate();
                }
                obj.put("amount", amount);
                obj.put("orderamount", CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currency.getCurrencyID(),salesOrder.getOrderDate()));
                obj.put("currencysymbol", currency.getSymbol());
                obj.put("currencyid",currency.getCurrencyID());
                obj.put("personname", customer.getName());
                obj.put("memo", salesOrder.getMemo());
                String status = getPurchaseOrderStatus(session,salesOrder);
                obj.put("status",status);
                if(!closeflag || (closeflag&&status.equalsIgnoreCase("open")))
                    jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (SessionExpiredException see) {
            throw ServiceException.FAILURE("CustomerManager.getSalesOrders", see);
        } catch (NumberFormatException nfe) {
            throw ServiceException.FAILURE("CustomerManager.getSalesOrders", nfe);
        } catch (JSONException jse) {
            throw ServiceException.FAILURE("CustomerManager.getSalesOrders", jse);
        }
        return jobj;
    }*/
     public static String getPurchaseOrderStatus(Session session, SalesOrder po) throws ServiceException {
        Set<SalesOrderDetail> orderDetail = po.getRows();
        Iterator ite = orderDetail.iterator();
        String result = "Closed";
        while(ite.hasNext()){
            SalesOrderDetail pDetail = (SalesOrderDetail)ite.next();
            String query = "from InvoiceDetail ge where ge.salesorderdetail.ID = ?";
            List list =  HibernateUtil.executeQuery(session, query,pDetail.getID());
            Iterator ite1 = list.iterator();
            int qua = 0;
            while(ite1.hasNext()){
                InvoiceDetail ge = (InvoiceDetail)ite1.next();
                qua += ge.getInventory().getQuantity();
            }
            if(qua < pDetail.getQuantity()){
                result = "Open";
                break;
            }
        }
        return result;
    }
    /*public static JSONObject getReceiptRows(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
		try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
             Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            String[] receipt=request.getParameterValues("bills");
            int i=0;
            JSONArray jArr=new JSONArray();
            while(receipt!=null&&i<receipt.length){
                Receipt re=(Receipt)session.get(Receipt.class, receipt[i]);
                Iterator itr=re.getRows().iterator();
                while(itr.hasNext()) {
                    ReceiptDetail row=(ReceiptDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", re.getID());
                    obj.put("rowid", row.getID());
                    obj.put("currencysymbol", (row.getReceipt().getCurrency()==null?currency.getSymbol():row.getReceipt().getCurrency().getSymbol()));
                    obj.put("transectionno", row.getInvoice().getInvoiceNumber());
                    obj.put("transectionid", row.getInvoice().getID());
                    obj.put("amount",row.getAmount());
                    obj.put("duedate", AuthHandler.getDateFormatter(request).format(row.getInvoice().getDueDate()));
                    obj.put("creationdate", AuthHandler.getDateFormatter(request).format(row.getInvoice().getJournalEntry().getEntryDate()));
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.saveCreditNote", ex);
        } catch (JSONException je) {
            throw ServiceException.FAILURE("CompanyHandler.saveCreditNote", je);
        }
        return jobj;
    }*/

    /*public static void deleteReceipt(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException, JSONException, SessionExpiredException{
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
            query = "update Receipt set deleted=true where ID in("+qMarks +") and company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
            query = "update JournalEntry je set je.deleted=true where je.ID in(select r.journalEntry.ID from Receipt r where r.ID in("+qMarks +") and r.company.companyID=je.company.companyID) and je.company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
        } catch(ServiceException ex){
            throw ServiceException.FAILURE("Selected record(s) are currently used in transaction(s)", ex);
        }
    }*/

    public static JSONObject getMonthWiseReceivable(Session session, HttpServletRequest request) throws ServiceException, HibernateException, JSONException, AccountingException {
        JSONObject jobj = AccountDBCon.getAgedReceivable(request);
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
                finalObj.put("amountreceived", 0);
                finalObj.put("amountdue", 0);
                finalJArr.put(finalObj);
            }
            for(int i=0;i<startFinYearCal.get(Calendar.MONTH);i++){
                JSONObject finalObj=new JSONObject();
                finalObj.put("month", i);
                cal.set(Calendar.MONTH, i);
                finalObj.put("monthname", sdf.format(cal.getTime()));
                finalObj.put("totalamount", 0);
                finalObj.put("amountreceived", 0);
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
                    finalObj.put("amountreceived", finalObj.getDouble("amountreceived")+tamount-damount);
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

    /*public static JSONObject getSalesOrderRows(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException{
        JSONObject jobj=new JSONObject();
		try {
            KWLCurrency kwlcurrency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String[] sos=request.getParameterValues("bills");
            int i=0;
            JSONArray jArr=new JSONArray();
           int addobj = 1;
            String closeflag = request.getParameter("closeflag");
            while(sos!=null&&i<sos.length){
                SalesOrder so=(SalesOrder)session.get(SalesOrder.class, sos[i]);
                KWLCurrency currency=so.getCustomer().getAccount().getCurrency()==null?kwlcurrency:so.getCustomer().getAccount().getCurrency();
                Iterator itr=so.getRows().iterator();
                while(itr.hasNext()) {
                    SalesOrderDetail row=(SalesOrderDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", so.getID());
                    obj.put("billno", so.getSalesOrderNumber());
                    obj.put("currencysymbol",currency.getSymbol());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("productname",row.getProduct().getName());
                    obj.put("desc", row.getProduct().getDescription());
                    obj.put("memo", row.getRemark());
                    obj.put("rate", row.getRate());
                    obj.put("orderrate", CompanyHandler.getBaseToCurrencyAmount(session,request,row.getRate(),currency.getCurrencyID(),so.getOrderDate()));
//                    obj.put("quantity", row.getQuantity());
                    if(closeflag!=null){
                       addobj =  getPurchaseOrderDetailStatus(session,row);
                       obj.put("quantity", addobj);
                    }else{
                       obj.put("quantity", row.getQuantity());
                    }
                    if(addobj>0)
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

    public static int getPurchaseOrderDetailStatus(Session session,SalesOrderDetail pod) throws ServiceException{
        int result = pod.getQuantity();
        String query = "from InvoiceDetail ge where ge.salesorderdetail.ID = ?";
        List list =  HibernateUtil.executeQuery(session, query,pod.getID());
        Iterator ite1 = list.iterator();
        int qua = 0;
        while(ite1.hasNext()){
            InvoiceDetail ge = (InvoiceDetail)ite1.next();
            qua += ge.getInventory().getQuantity();
        }
        result = pod.getQuantity()-qua;
        return result;
    }

    /*
    public static JSONObject saveReportTemplate(Session session, HttpServletRequest request) throws ServiceException {
        JSONObject jobj = null;
        try {
            String jsondata = request.getParameter("data");
            String userid = request.getParameter("userid");
            String name = request.getParameter("name");
            String desc = request.getParameter("desc");

            jobj = new JSONObject();

            PDFReportTemplate proj_temp = new PDFReportTemplate();
            proj_temp.setName(name);
            proj_temp.setDescription(desc);
            proj_temp.setConfiguration(jsondata);
            proj_temp.setUser((User) session.get(User.class, userid));
            session.save(proj_temp);
            jobj.put("success",true);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("CustomerHandler.saveReportTemplate", e);
        } catch (HibernateException e) {
            throw ServiceException.FAILURE("CustomerHandler.saveReportTemplate", e);
        }
        return jobj;
    }
     public static JSONObject getAllReportTemplate(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        try {
            String userid = AuthHandler.getUserid(request);
            String Hql = "select p from PDFReportTemplate p where p.user.userID=?and deleted=false";

            List ll = HibernateUtil.executeQuery(session, Hql, new Object[]{userid});
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                PDFReportTemplate obj = (PDFReportTemplate) ite.next();
                JSONObject jtemp = new JSONObject();

                jtemp.put("tempid",obj.getID());
                jtemp.put("tempname",obj.getName());
                jtemp.put("description",obj.getDescription());
                jtemp.put("configstr",obj.getConfiguration());
                jarr.put(jtemp);
            }
            jobj.put("data", jarr);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("Accounting.getAllReportTemplate", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
*/
    //@@@
    public static void sendMail(Session session, HttpServletRequest request, int mode, String billid) throws ServiceException, DocumentException{
//        {
//            java.io.OutputStream os = null;
//            try {
//                CompanyAccountPreferences preferences=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
//                Company company = preferences.getCompany();
//                KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
//                String currencyid = currency.getCurrencyID();
//                DateFormat formatter = AuthHandler.getUserDateFormatter(session, AuthHandler.getDateFormatID(request), AuthHandler.getTimeZoneDifference(request), true);
//                String logoPath=ProfileImageServlet.getProfileImagePath(request, true,null);
//                boolean isSend=false;
//                String htmlMsg="",plainMsg="",subject="";
//                String[] emails={};
//                double amount=0;
//                switch(mode){
//                    case StaticValues.AUTONUM_INVOICE:
//                        Invoice inv=(Invoice)session.get(Invoice.class, billid);
//                        Customer customer=(Customer)session.get(Customer.class, inv.getCustomerEntry().getAccount().getID());
//                        if(customer==null)return;
//                        ArrayList<String> em=new ArrayList<String>();
//                        em.add(customer.getEmail());
//                        emails=em.toArray(emails);
//                        isSend=preferences.isEmailInvoice();
//                        subject=AccountingMsgs.invoiceSubject;
//                        htmlMsg=String.format(AccountingMsgs.invoiceHtmlMsg,customer.getName(),inv.getInvoiceNumber(),company.getEmailID(),company.getEmailID());
//                        plainMsg=String.format(AccountingMsgs.invoicePlainMsg,customer.getName(),inv.getInvoiceNumber(),company.getEmailID(),company.getEmailID());
//                        double disc=Double.parseDouble(request.getParameter("discount"));
//                        amount=Double.parseDouble(request.getParameter("subTotal"));
//                        if("true".equals(request.getParameter("perdiscount")))
//                            amount=amount-amount*disc/100;
//                        else
//                            amount=amount-disc;
//
//                        break;
//
//                      case StaticValues.AUTONUM_BILLINGINVOICE:
//                        BillingInvoice billinginv=(BillingInvoice)session.get(BillingInvoice.class, billid);
//                        Customer billingCustomer=(Customer)session.get(Customer.class, billinginv.getCustomerEntry().getAccount().getID());
//                        if(billingCustomer==null)return;
//                        ArrayList<String> billingem=new ArrayList<String>();
//                        billingem.add(billingCustomer.getEmail());
//                        emails=billingem.toArray(emails);
//                        isSend=preferences.isEmailInvoice();
//                        subject=AccountingMsgs.invoiceSubject;
//                        htmlMsg=String.format(AccountingMsgs.invoiceHtmlMsg,billingCustomer.getName(),billinginv.getBillingInvoiceNumber(),company.getEmailID(),company.getEmailID());
//                        plainMsg=String.format(AccountingMsgs.invoicePlainMsg,billingCustomer.getName(),billinginv.getBillingInvoiceNumber(),company.getEmailID(),company.getEmailID());
//                        double billingDisc=Double.parseDouble(request.getParameter("discount"));
//                        amount=Double.parseDouble(request.getParameter("subTotal"));
//                        if("true".equals(request.getParameter("perdiscount")))
//                            amount=amount-amount*billingDisc/100;
//                        else
//                            amount=amount-billingDisc;
//
//                        break;
//                }
//                ByteArrayOutputStream baos = ExportServlet.createPdf(session, currencyid, billid, formatter, mode, amount, logoPath, null,null,null);
//                File destDir = new File(StorageHandler.GetProfileImgStorePath(), "BILL"+billid+".pdf");
//                os = new FileOutputStream(destDir);
//                baos.writeTo(os);
//                try {
//                    if(emails.length>0&&isSend)
//                        SendMailHandler.postMail(emails,subject, htmlMsg, plainMsg, company.getEmailID(),new String[]{destDir.getAbsolutePath()});
//                } catch (MessagingException e) {
//                }
//            } catch (IOException e) {
//                throw ServiceException.FAILURE(e.getMessage(), e);
//            } catch (SessionExpiredException e) {
//                throw ServiceException.FAILURE(e.getMessage(), e);
//            } finally {
//                try {
//                    os.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(CustomerHandler.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
    }

    /*private static double[] saveBillingInvoiceRows(Session session, HttpServletRequest request, BillingInvoice invoice, Company company, HashSet jeDetails) throws JSONException, ServiceException, HibernateException, AccountingException {
        HashSet rows=new HashSet();
        double totaldiscount=0;
        double totalamount=0;
        JSONArray jArr=new JSONArray(request.getParameter("detail"));
        for(int i=0;i<jArr.length();i++){
            JSONObject jobj=jArr.getJSONObject(i);
            BillingInvoiceDetail row=new BillingInvoiceDetail();
            row.setCompany(company);
            row.setBillingInvoice(invoice);
            row.setRate(jobj.getDouble("rate"));
            row.setQuantity(jobj.getInt("quantity"));
            row.setAmount(jobj.getInt("calamount"));
            row.setProductDetail(jobj.getString("productdetail"));
            totalamount+=(row.getRate()*row.getQuantity());
            Discount discount=null;
            double disc=jobj.getDouble("prdiscount");
            if(disc!=0.0){
                discount=new Discount();
                discount.setDiscount(disc);
                discount.setOriginalAmount(row.getRate()*row.getQuantity());
                discount.setInPercent(true);
                discount.setCompany(company);
                row.setDiscount(discount);
                session.save(discount);
                totaldiscount+=discount.getDiscountValue();
            }
            rows.add(row);
        }
        invoice.setRows(rows);
        return new double[]{totaldiscount,totalamount};
    }*/

     /*public static String saveBillingInvoice(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException{
        String id =null;
		try {
            Discount discount=null;
            double discValue=0.0;
            String taxid=null;
            taxid=request.getParameter("taxid");
            double taxamount=CompanyHandler.getDouble(request, "taxamount");
            double shippingCharges=CompanyHandler.getDouble(request, "shipping");
            double otherCharges=CompanyHandler.getDouble(request, "othercharges");
            boolean inCash=Boolean.parseBoolean(request.getParameter("incash"));
            CompanyAccountPreferences preferences=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));
            String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
            BillingInvoice invoice=new BillingInvoice();
            String entryNumber=request.getParameter("number");
            String q="from BillingInvoice where billingInvoiceNumber=? and company.companyID=?";
            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
                throw new AccountingException("Invoice number '"+entryNumber+"' already exists.<br>Save again with given auto number.<br>Or type other number");
            invoice.setBillingInvoiceNumber(entryNumber);
            int from=StaticValues.AUTONUM_BILLINGINVOICE;
            if(inCash)from=StaticValues.AUTONUM_BILLINGCASHSALE;
            invoice.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, from).equals(entryNumber));
            invoice.setMemo(request.getParameter("memo"));
            invoice.setBillTo(request.getParameter("billto"));
            invoice.setShipTo(request.getParameter("shipaddress"));
            invoice.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
            invoice.setShipDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("shipdate")));
            invoice.setDueDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("duedate")));
            invoice.setCompany(company);
            HashSet hs=new HashSet();
            double[] totals=saveBillingInvoiceRows(session, request, invoice, company, hs);
            double disc=CompanyHandler.getDouble(request, "discount");
            if(disc>0){
                discount=new Discount();
                discount.setDiscount(disc);
                discount.setInPercent(Boolean.parseBoolean(request.getParameter("perdiscount")));
                discount.setOriginalAmount(totals[1]-totals[0]);
                discount.setCompany(company);
                invoice.setDiscount(discount);

                session.save(discount);
                discValue=discount.getDiscountValue();
            }
            discValue+=totals[0];
            JournalEntryDetail jed=new JournalEntryDetail();
            jed.setCompany(company);
            jed.setAmount(totals[1]+shippingCharges+otherCharges-discValue+taxamount);
            if(!inCash)
                jed.setAccount((Account)session.get(Account.class, request.getParameter("customer")));
            else
                jed.setAccount(preferences.getCashAccount());
            jed.setDebit(true);
            hs.add(jed);
            invoice.setCustomerEntry(jed);
            jed=new JournalEntryDetail();
            jed.setCompany(company);
            jed.setAccount((Account)session.get(Account.class, request.getParameter("creditoraccount")));
            jed.setAmount(totals[1]);
            jed.setDebit(false);
            hs.add(jed);
            invoice.setCreditorEntry(jed);
            if(discValue>0){
                jed=new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(discValue);
                jed.setAccount(preferences.getDiscountGiven());
                jed.setDebit(true);
                hs.add(jed);
            }
            if(shippingCharges>0){
                jed=new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(shippingCharges);
                jed.setAccount(preferences.getShippingCharges());
                jed.setDebit(false);
                hs.add(jed);
                invoice.setShipEntry(jed);
            }
            if(otherCharges>0){
                jed=new JournalEntryDetail();
                jed.setCompany(company);
                jed.setAmount(otherCharges);
                jed.setAccount(preferences.getOtherCharges());
                jed.setDebit(false);
                hs.add(jed);
                invoice.setOtherEntry(jed);
            }
            if(taxid!=null&&!taxid.isEmpty()){
                Tax tax=  (Tax)session.get(Tax.class, taxid);
                if(tax==null)
                    throw new  AccountingException("The Tax code(s) used in this transaction has been deleted.");
                invoice.setTax(tax);
                if(taxamount>0){
                    jed=new JournalEntryDetail();
                    jed.setCompany(company);
                    jed.setAmount(taxamount);
                    jed.setAccount(tax.getAccount());
                    jed.setDebit(false);
                    hs.add(jed);
                    invoice.setTaxEntry(jed);
                }
            }
            JournalEntry journalEntry=CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),
            request.getParameter("memo"), "JE"+invoice.getBillingInvoiceNumber(),currencyid, hs,request);
            invoice.setJournalEntry(journalEntry);
            ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),null);
            invoice.setExchangeRateDetail(erd);
            session.saveOrUpdate(invoice);
            id=invoice.getID();
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }

        return id;
    }*/
    public static JSONObject getBillingInvoices(Session session, HttpServletRequest request, String start, String limit,String custid) throws ServiceException, HibernateException{
        JSONObject jobj=getBillingInvoices(session, request,custid);
        if(!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)){
            try {
                int st = Integer.parseInt(start);
                JSONArray jArr = new JSONArray(), temp = jobj.getJSONArray("data");
                int ed = Math.min(temp.length(),st+Integer.parseInt(limit));
                for (int i = st; i < ed; i++)
                    jArr.put(temp.getJSONObject(i));
                jobj.put("data", jArr);
                jobj.put("count", temp.length());
            } catch (NumberFormatException ne) {
                throw ServiceException.FAILURE("CompanyHandler.getInvoices", ne);
            } catch (JSONException jse) {
                throw ServiceException.FAILURE("CompanyHandler.getInvoices", jse);
            }
        }
        return jobj;
    }
    public static JSONObject getBillingInvoices(Session session, HttpServletRequest request,String custid) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
        try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String customerid =custid==null?request.getParameter("accid"):custid;
            String ss=request.getParameter("ss");
            String cashAccount=((CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request))).getCashAccount().getID();
            boolean cashonly=false;
            boolean creditonly=false;
            cashonly =Boolean.parseBoolean(request.getParameter("cashonly"));
            creditonly =Boolean.parseBoolean(request.getParameter("creditonly"));
            if(cashonly)
                customerid=cashAccount;
            boolean ignoreZero = request.getParameter("ignorezero") != null;
            String dueDate = request.getParameter("curdate");
            ArrayList params = new ArrayList();
            String condition = "";
            params.add(AuthHandler.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(customerid)) {
                params.add(customerid);
                condition += " and inv.customerEntry.account.ID=?";
            } else {
                String q = "select ID from Customer where company.companyID=?";
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
                    condition += " and inv.customerEntry.account.ID in (" + qMarks + ")";
            }

            if (!StringUtil.isNullOrEmpty(dueDate)) {
                params.add(AuthHandler.getDateFormatter(request).parse(dueDate));
                condition += " and inv.dueDate<=?";
            }
            if(StringUtil.isNullOrEmpty(ss)==false){
               for(int i=0;i<=4;i++){
                 params.add(ss+"%");
               }
                 condition+= " and (inv.billingInvoiceNumber like ? or inv.billTo like ?  or inv.journalEntry.entryNumber like ? or inv.memo like ? or inv.customerEntry.account.name like ? ) ";
            }
             String query = "from BillingInvoice inv where inv.deleted=false and inv.company.companyID=? " + condition + " order by inv.ID";
            List list = HibernateUtil.executeQuery(session, query, params.toArray());
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                BillingInvoice invoice = (BillingInvoice) itr.next();
                JournalEntry je = invoice.getJournalEntry();
                JournalEntryDetail d = invoice.getCustomerEntry();
                Account account = d.getAccount();
                double amount = 0, ramount = 0;
                Iterator itrBir = applyBillingInvoiceAmount(session, invoice).values().iterator();
                while (itrBir.hasNext()) {
                    Object[] temp = (Object[]) itrBir.next();
                    amount += (Double) temp[0];
                }
                JournalEntryDetail tempd = invoice.getShipEntry();
                if (tempd != null) {
                    amount += tempd.getAmount();
                }
                tempd = invoice.getOtherEntry();
                if (tempd != null) {
                    amount += tempd.getAmount();
                }
                tempd = invoice.getTaxEntry();
                if (tempd != null) {
                    amount += tempd.getAmount();
                }
                String q = "select sum(amount) from BillingReceiptDetail rd where rd.billingInvoice.ID=? group by rd.billingInvoice";
                List l = HibernateUtil.executeQuery(session, q, invoice.getID());
                ramount = (l.isEmpty() ? 0 : (Double) l.get(0));
                String currencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
                JSONObject obj = new JSONObject();
                obj.put("billid", invoice.getID());
                obj.put("personid", account.getID());
                obj.put("billno", invoice.getBillingInvoiceNumber());
                obj.put("currencyid",currencyid);
                obj.put("currencysymbol",(invoice.getCurrency()==null?"": invoice.getCurrency().getSymbol()));
                obj.put("companyaddress", invoice.getCompany().getAddress());
                obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,je.getEntryDate()));
                obj.put("billto", invoice.getBillTo());
                obj.put("shipto", invoice.getShipTo());
                obj.put("journalentryid", je.getID());
                obj.put("entryno", je.getEntryNumber());
                obj.put("date", AuthHandler.getDateFormatter(request).format(je.getEntryDate()));
                obj.put("shipdate", AuthHandler.getDateFormatter(request).format(invoice.getShipDate()));
                obj.put("duedate", AuthHandler.getDateFormatter(request).format(invoice.getDueDate()));
                obj.put("personname", account.getName());
                obj.put("taxamount", invoice.getTaxEntry()==null?0:invoice.getTaxEntry().getAmount());
                obj.put("memo", invoice.getMemo());
                obj.put("discount", invoice.getDiscount()==null?0:invoice.getDiscount().getDiscountValue());
                if(account.getID().equals(cashAccount)){
                      obj.put("amountdue",0);
                      obj.put("incash",true);
                      obj.put("amountdueinbase", 0);

                }
                else{
                    obj.put("amountdue", amount - ramount);
                    obj.put("amountdueinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,amount - ramount,currencyid,je.getEntryDate()));
                }
                obj.put("amountduenonnegative", amount - ramount);
                obj.put("amount", d.getAmount());
                obj.put("amountinbase", CompanyHandler.getCurrencyToBaseAmount(session,request,d.getAmount(),currencyid,je.getEntryDate()));
                if (!(ignoreZero && amount - ramount <= 0)) {
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    private static HashMap applyBillingInvoiceAmount(Session session,BillingInvoice invoice) throws ServiceException{
        HashMap hm=new HashMap();
        Set invRows=invoice.getRows();
        Iterator itr=invRows.iterator();
        double amount;
        double quantity;
        double disc=(invoice.getDiscount()==null?0:invoice.getDiscount().getDiscountValue())/invRows.size();
         while(itr.hasNext()){
            BillingInvoiceDetail temp=(BillingInvoiceDetail)itr.next();
            quantity=temp.getQuantity();
            amount=temp.getRate()*quantity;
            double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
            hm.put(temp, new Object[]{amount-rdisc-disc,quantity});
            if(invoice==null)invoice=temp.getBillingInvoice();
        }
        return hm;
    }
     /*public static void deleteBillingInvoices(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException, JSONException, SessionExpiredException{
        ArrayList params = new ArrayList();
        ArrayList jeParams = new ArrayList();
        try{
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String qMarks = "";
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            qMarks = "";
            for (int i = 0; i < jArr.length(); i++){
                String cashAccount=((CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request))).getCashAccount().getID();
                JSONObject jobj = jArr.getJSONObject(i);
                if(jobj.getString("personid").equals(cashAccount))
                    throw new AccountingException("Payment against the selected Sales Receipt(s) has been received. So, it cannot be deleted.");
                params.add(jobj.getString("billid"));
                jeParams.add(jobj.getString("journalentryid"));
                qMarks += "?,";
            }
            qMarks=qMarks.substring(0,Math.max(0, qMarks.length()-1));
            params.add(company.getCompanyID());
            jeParams.add(company.getCompanyID());
            CheckBillingReceipt(session, request,params,qMarks);
            deleteBillingInvoicesRows(session, request);
            String delQuery = "delete from BillingInvoice inv where ID in( "+qMarks +") and inv.company.companyID=?";
            HibernateUtil.executeUpdate(session, delQuery, params.toArray());
            delQuery = "delete from JournalEntryDetail jed where jed.journalEntry.ID in( "+qMarks +") and jed.company.companyID=?";
            HibernateUtil.executeUpdate(session, delQuery, jeParams.toArray());
            delQuery = "delete from JournalEntry je where ID in( "+qMarks +") and je.company.companyID=?";
            HibernateUtil.executeUpdate(session, delQuery, jeParams.toArray());
            params = new ArrayList();
            for (int i = 0; i < jArr.length(); i++){
                JSONObject jobj = jArr.getJSONObject(i);
                BillingInvoice invoice=(BillingInvoice)session.get(BillingInvoice.class, jobj.getString("billid"));
                qMarks = "";
                if(invoice.getDiscount()!=null){
                     qMarks += "?,";
                    params.add(invoice.getDiscount().getID());
                }
            }
            qMarks=qMarks.substring(0,Math.max(0, qMarks.length()-1));
            if(!params.isEmpty()){
             params.add(company.getCompanyID());
                delQuery = "delete from Discount where ID in ( "+qMarks +") and company.companyID=?";
                HibernateUtil.executeUpdate(session, delQuery, params.toArray()) ;
             }
        } catch(ServiceException ex){
            throw ServiceException.FAILURE("Selected record(s) are currently used in transaction(s)", ex);
        }
    }*/

      /*public static void CheckBillingReceipt(Session session, HttpServletRequest request, ArrayList params, String qMarks) throws ServiceException, HibernateException, SessionExpiredException, AccountingException{
          try {
                String selQuery = "from BillingReceiptDetail rd  where rd.billingInvoice.ID in ( "+qMarks +")  and rd.company.companyID=?";
                List list = HibernateUtil.executeQuery(session, selQuery, params.toArray());
                int count=list.size();
                if(count>0)
                    throw new AccountingException("Payment against the selected Invoice(s) has been partially/fully received. So, it cannot be deleted.");
            } catch (ServiceException e) {
                throw ServiceException.FAILURE("CompanyHandler.getInvoiceRows", e);
            }
        }*/

       /*public static void deleteBillingInvoicesRows(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException{
           try {
                JSONArray jArr = new JSONArray(request.getParameter("data"));
                for (int i = 0; i < jArr.length(); i++){
                    JSONObject jobj = jArr.getJSONObject(i);
                    BillingInvoice invoice=(BillingInvoice)session.get(BillingInvoice.class, jobj.getString("billid"));
                    Iterator itr=invoice.getRows().iterator();
                    while(itr.hasNext()) {
                        BillingInvoiceDetail row=(BillingInvoiceDetail)itr.next();
                        String delQuery = "delete from BillingInvoiceDetail where ID=? and company.companyID=?";
                        HibernateUtil.executeUpdate(session, delQuery, new Object[]{row.getID(), AuthHandler.getCompanyid(request)}) ;
                        if(row.getDiscount()!=null){
                            delQuery = "delete from Discount where ID=? and company.companyID=?";
                            HibernateUtil.executeUpdate(session, delQuery, new Object[]{row.getDiscount().getID(), AuthHandler.getCompanyid(request)}) ;
                        }
                    }
                }
            } catch (JSONException e) {
                throw ServiceException.FAILURE("CompanyHandler.getInvoiceRows", e);
        }
    }*/

    /*public static JSONObject getBillingInvoiceRows(Session session, HttpServletRequest request) throws ServiceException, HibernateException, SessionExpiredException{
        JSONObject jobj=new JSONObject();
		try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String[] invoices=request.getParameterValues("bills");
            int i=0;
            JSONArray jArr=new JSONArray();
            while(invoices!=null&&i<invoices.length){
                BillingInvoice invoice=(BillingInvoice)session.get(BillingInvoice.class, invoices[i]);
                Iterator itr=invoice.getRows().iterator();
                HashMap hm=applyBillingInvoiceAmount(session,invoice);
                while(itr.hasNext()) {
                    BillingInvoiceDetail row=(BillingInvoiceDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", invoice.getID());
                    obj.put("billno", invoice.getBillingInvoiceNumber());
                    String currencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
                    obj.put("currencysymbol", (invoice.getCurrency()==null?currency.getCurrencyID():invoice.getCurrency().getSymbol()));
                    obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,invoice.getJournalEntry().getEntryDate()));
                    obj.put("rowid", row.getID());
                    obj.put("productdetail",row.getProductDetail());
                    obj.put("quantity",row.getQuantity());
                    Discount disc= row.getDiscount();
                    if(disc!=null&&disc.isInPercent())
                        obj.put("prdiscount",disc.getDiscount());
                    else
                        obj.put("prdiscount",0);
                    obj.put("rate", row.getRate());
                    double amount=0;
                    if(hm.containsKey(row)){
                        Object[] val=(Object[])hm.get(row);
                        amount=(Double)val[0];
                    obj.put("amount", amount);
                    jArr.put(obj);
                }
                i++;
            }
            jobj.put("data", jArr);
            }
        } catch (JSONException e) {
                throw ServiceException.FAILURE("CompanyHandler.getInvoiceRows", e);
        }
        return jobj;
    }*/
  /*public static void deleteBillingReceipt(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException, JSONException, SessionExpiredException{
        ArrayList params = new ArrayList();
        ArrayList jeParams = new ArrayList();
        try{
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String qMarks = "";
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            qMarks = "";
            for (int i = 0; i < jArr.length(); i++){
                 JSONObject jobj = jArr.getJSONObject(i);
                params.add(jobj.getString("billid"));
                jeParams.add(jobj.getString("journalentryid"));
                qMarks += "?,";
            }
            qMarks=qMarks.substring(0,Math.max(0, qMarks.length()-1));
            params.add(company.getCompanyID());
            jeParams.add(company.getCompanyID());
             String delQuery = "delete from BillingReceiptDetail brd where brd.billingReceipt.ID in( "+qMarks +") and brd.company.companyID=?";
            HibernateUtil.executeUpdate(session, delQuery, params.toArray());
            delQuery = "delete from BillingReceipt br where ID in( "+qMarks +") and br.company.companyID=?";
            HibernateUtil.executeUpdate(session, delQuery, params.toArray());
            delQuery  = "delete from JournalEntryDetail jed where jed.journalEntry.ID in( "+qMarks +") and jed.company.companyID=?";
            HibernateUtil.executeUpdate(session, delQuery, jeParams.toArray());
            delQuery = "delete from JournalEntry je where ID in( "+qMarks +") and je.company.companyID=?";
            HibernateUtil.executeUpdate(session, delQuery, jeParams.toArray());
            params = new ArrayList();
            qMarks=qMarks.substring(0,Math.max(0, qMarks.length()-1));
        } catch(ServiceException ex){
            throw ServiceException.FAILURE("Selected record(s) are currently used in transaction(s)", ex);
        }
    }*/

    /*public static void saveBillingReceipt(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException{
        try {
            Account dipositTo = null;
            BillingReceipt receipt = new BillingReceipt();
            double amount = 0;
            double amountDiff = 0;
            HashSet hs = new HashSet();
            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String currencyid=(request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid"));
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            String entryNumber=request.getParameter("no");
            String q="from BillingReceipt where billingReceiptNumber=? and company.companyID=?";
            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
                throw new AccountingException("Receipt number '"+entryNumber+"' already exists.");
            receipt.setBillingReceiptNumber(entryNumber);
            receipt.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
            receipt.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_BILLINGRECEIPT).equals(entryNumber));
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
                receipt.setPayDetail(pdetail);
            }
            receipt.setMemo(request.getParameter("memo"));
            receipt.setDeleted(false);
            receipt.setCompany(company);
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            if(jArr.length()>0){
                amount = saveBillingReceiptRows(session, request, receipt, company, jArr);
                amountDiff=oldBillingReceiptRowsAmount(session, request, jArr,currencyid);
                hs = new HashSet();
                if(preferences.getForeignexchange()==null)
                    throw new AccountingException("Please Set Foreign Exchange account in Account Preference first");
                if(amountDiff!=0&&preferences.getForeignexchange()!=null){
                    hs = new HashSet();
                    JournalEntryDetail jed = new JournalEntryDetail();
                    jed.setCompany(company);
                    jed.setAmount(amountDiff);
                    jed.setAccount(preferences.getForeignexchange());
                    jed.setDebit(true);
                    hs.add(jed);
                }
             }
                else amount= Double.parseDouble(request.getParameter("amount"));
           
            JournalEntryDetail jed = new JournalEntryDetail();
            jed.setCompany(company);
            jed.setAmount(amount+amountDiff);
            jed.setAccount(((Account) session.get(Account.class, request.getParameter("accid"))));
            jed.setDebit(false);
            hs.add(jed);
            jed = new JournalEntryDetail();
            jed.setCompany(company);
            jed.setAmount(amount);
            jed.setAccount(dipositTo);
            jed.setDebit(true);
            hs.add(jed);
            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
                    request.getParameter("memo"), "JE" + receipt.getBillingReceiptNumber(),currencyid, hs,request);
            jed.setJournalEntry(journalEntry);
            receipt.setJournalEntry(journalEntry);
            session.saveOrUpdate(receipt);
            ProfileHandler.insertAuditLog(session, AuditAction.RECEIPT_ADDED, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) +" created new receipt ", request);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }*/

     /*private static double oldBillingReceiptRowsAmount(Session session, HttpServletRequest request,  JSONArray jArr,String currencyid) throws JSONException, ServiceException, HibernateException, SessionExpiredException, ParseException {
        double ratio=0;
        double amount = 0;
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            BillingInvoice invoice=(BillingInvoice) session.get(BillingInvoice.class, jobj.getString("billid"));
            Double recinvamount = jobj.getDouble("payment");
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String currid=currency.getCurrencyID();
            if(invoice.getCurrency()!=null)
                 currid=invoice.getCurrency().getCurrencyID();
            double oldrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,currid,invoice.getJournalEntry().getEntryDate());
            double  newrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,currid,AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            ratio=oldrate-newrate;
            amount+=recinvamount*ratio;

        }
         amount=CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
        return (amount);
    }*/
    /*private static double saveBillingReceiptRows(Session session, HttpServletRequest request, BillingReceipt receipt, Company company, JSONArray jArr) throws JSONException, ServiceException, HibernateException {
        HashSet hs = new HashSet();
        double amount = 0;
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            BillingReceiptDetail rd = new BillingReceiptDetail();
            rd.setAmount(jobj.getDouble("payment"));
            rd.setCompany(company);
            rd.setBillingInvoice((BillingInvoice) session.get(BillingInvoice.class, jobj.getString("billid")));
            rd.setBillingReceipt(receipt);
            amount += jobj.getDouble("payment");
            hs.add(rd);
        }
        receipt.setRows(hs);
        return amount;
    }*/
    /*public static JSONObject getBillingReceipts(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String start=request.getParameter("start");
            String limit=request.getParameter("limit");
            String ss=request.getParameter("ss");
            ArrayList params=new ArrayList();
            params.add(AuthHandler.getCompanyid(request));
            String condition=" where r.deleted=false and  r.company.companyID=?  and jed.debit=false ";
            if(StringUtil.isNullOrEmpty(ss)==false){
               params.add(ss+"%");
               condition+= " and (ac.name like ? ) ";
           }
           String query="select r, ac from BillingReceipt r inner join r.journalEntry je inner join je.details jed inner join jed.account ac"+condition;
           List list = HibernateUtil.executeQuery(session, query,params.toArray());
           int count=list.size();
           if(StringUtil.isNullOrEmpty(start)==false&&StringUtil.isNullOrEmpty(limit)==false){
                list=HibernateUtil.executeQueryPaging(session, query,params.toArray(), new Integer[]{Integer.parseInt(start),Integer.parseInt(limit)});
            }
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Object[] row=(Object[])itr.next();
                BillingReceipt receipt = (BillingReceipt) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                obj.put("billid", receipt.getID());
                obj.put("entryno", receipt.getJournalEntry().getEntryNumber());
                obj.put("journalentryid", receipt.getJournalEntry().getID());
                obj.put("personid", acc.getID());
                obj.put("billno", receipt.getBillingReceiptNumber());
                obj.put("currencysymbol", (receipt.getCurrency()==null?currency.getCurrencyID():receipt.getCurrency().getSymbol()));
                obj.put("billdate", AuthHandler.getDateFormatter(request).format(receipt.getJournalEntry().getEntryDate()));//receiptdate
                Iterator itrRow=receipt.getRows().iterator();
                double amount=0;
                if(!receipt.getRows().isEmpty())
                    while(itrRow.hasNext()){
                        amount+=((BillingReceiptDetail)itrRow.next()).getAmount();
                    }
                else{
                    itrRow=receipt.getJournalEntry().getDetails().iterator();
                    amount+=((JournalEntryDetail)itrRow.next()).getAmount();
                }
                obj.put("amount", amount);
                obj.put("personname", acc.getName());
                obj.put("memo", receipt.getMemo());
                obj.put("paymentmethod",(receipt.getPayDetail()==null?"Cash":receipt.getPayDetail().getPaymentMethod().getMethodName()));
                if(receipt.getPayDetail()!=null){
                    obj.put("refno",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":receipt.getPayDetail().getCheque().getChequeNo()):receipt.getPayDetail().getCard().getRefNo()));
                    obj.put("refname",(receipt.getPayDetail().getCard()==null?(receipt.getPayDetail().getCheque()==null?"":receipt.getPayDetail().getCheque().getBankName()):receipt.getPayDetail().getCard().getCardHolder()));
                }

                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CustomerManager.getReciepts", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CustomerManager.getReciepts", ex);
        }
        return jobj;
    }*/
 /*public static JSONObject getBillingReceiptRows(Session session, HttpServletRequest request) throws ServiceException, HibernateException{
        JSONObject jobj=new JSONObject();
		try {
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String[] billingreceipt=request.getParameterValues("bills");
            int i=0;
            JSONArray jArr=new JSONArray();
            while(billingreceipt!=null&&i<billingreceipt.length){
                BillingReceipt re=(BillingReceipt)session.get(BillingReceipt.class, billingreceipt[i]);
                Iterator itr=re.getRows().iterator();
                while(itr.hasNext()) {
                    BillingReceiptDetail row=(BillingReceiptDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", re.getID());
                    obj.put("rowid", row.getID());
                    obj.put("currencysymbol", (row.getBillingReceipt().getCurrency()==null?currency.getCurrencyID():row.getBillingReceipt().getCurrency().getSymbol()));
                    obj.put("transectionno", row.getBillingInvoice().getBillingInvoiceNumber());
                    obj.put("transectionid", row.getBillingInvoice().getID());
                    obj.put("amount",row.getAmount());
                    obj.put("duedate", AuthHandler.getDateFormatter(request).format(row.getBillingInvoice().getDueDate()));
                    obj.put("creationdate", AuthHandler.getDateFormatter(request).format(row.getBillingInvoice().getJournalEntry().getEntryDate()));
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CompanyHandler.saveCreditNote", ex);
        } catch (JSONException je) {
            throw ServiceException.FAILURE("CompanyHandler.saveCreditNote", je);
        }
        return jobj;
    }*/

    /*public static void deleteSalesOrders(Session session, HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
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
            query = "update SalesOrder set deleted=true where ID in("+qMarks +") and company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
        } catch (JSONException ex) {
            throw new AccountingException("Cannot extract data from client");
        }
    }*/

    /*public static void deleteCreditNotes(Session session, HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
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
            query = "update CreditNote set deleted=true where ID in("+qMarks +") and company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
            query = "update JournalEntry je set je.deleted=true  where je.ID in(select cn.journalEntry.ID from CreditNote cn where cn.ID in( " + qMarks + ") and cn.company.companyID=je.company.companyID) and je.company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
            query = "update Discount di set di.deleted=true  where di.ID in(select cnd.discount.ID from CreditNoteDiscount cnd where cnd.creditNote.ID in( " + qMarks + ") and cnd.company.companyID=di.company.companyID) and di.company.companyID=?";
            HibernateUtil.executeUpdate(session, query, params.toArray());
        } catch (JSONException ex) {
            throw new AccountingException("Cannot extract data from client");
        }
    }*/
}
