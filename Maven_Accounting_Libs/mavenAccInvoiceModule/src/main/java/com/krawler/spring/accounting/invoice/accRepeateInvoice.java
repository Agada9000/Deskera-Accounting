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

package com.krawler.spring.accounting.invoice;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BillingInvoice;
import com.krawler.hql.accounting.BillingInvoiceDetail;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.RepeatedInvoices;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class accRepeateInvoice extends MultiActionController {
    private HibernateTransactionManager txnManager;
    private accInvoiceDAO accInvoiceDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccDiscountDAO(accDiscountDAO accDiscountobj){
        this.accDiscountobj = accDiscountobj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public ModelAndView repeateInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "", failed = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try{
             HashMap<String, Object> requestParams = new HashMap<String, Object>();//AccountingManager.getGlobalParams(request);
             KwlReturnObject repeateInvcs = accInvoiceDAOobj.getRepeateInvoices(requestParams);
             List list = repeateInvcs.getEntityList();
             Iterator itr = list.iterator();
             while(itr.hasNext()){
                 Invoice invoice = (Invoice)itr.next();
                 try {
                     Invoice repeatedInvoice = repeateInvoice(invoice);
                     msg += repeatedInvoice.getInvoiceNumber()+",";
                     if(invoice.getRepeateInvoice()!=null){
                         updateRepeateInfo(invoice.getRepeateInvoice());
                     }
                 } catch(Exception ex) {
                     failed += invoice.getInvoiceNumber()+"["+invoice.getID()+"]: "+ex.getMessage()+";";
                 }
             }

             KwlReturnObject repeateBInvcs = accInvoiceDAOobj.getRepeateBillingInvoices(requestParams);
             List Blist = repeateBInvcs.getEntityList();
             Iterator Bitr = Blist.iterator();
             while(Bitr.hasNext()){
                 BillingInvoice invoice = (BillingInvoice)Bitr.next();
                 try {
                     BillingInvoice repeatedBInvoice = repeateBillingInvoice(invoice);
                     msg += repeatedBInvoice.getBillingInvoiceNumber()+",";
                     if(invoice.getRepeateInvoice()!=null){
                         updateRepeateInfo(invoice.getRepeateInvoice());
                     }
                 } catch(Exception ex) {
                     failed += invoice.getBillingInvoiceNumber()+"["+invoice.getID()+"][B]: "+ex.getMessage()+";";
                 }

             }
             if(msg.length()>0){
                 msg = msg.substring(0, msg.length()-1);
                 msg = "Generated invoice(s) : "+msg+".";
             } else {
                 msg = "No any scheduled recurring invoice to generate.";
             }
             txnManager.commit(status);
             issuccess = true;
        } catch (Exception ex){
            msg = "" + ex.getMessage();
            try {
                txnManager.rollback(status);
            } catch(Exception txEx) {
                msg += ex.getMessage();
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("failed", failed);
                jobj.put("datetime", new Date());
                jobj.put("msg", msg);
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public void updateRepeateInfo(RepeatedInvoices rinfo) throws ServiceException {
//        Date nextDate = rinfo.getNextDate();
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(nextDate);
//        int interval = rinfo.getIntervalUnit();
//        String intervalType = rinfo.getIntervalType();
//        if (intervalType.equalsIgnoreCase("Days")) {
//            cal.add(Calendar.DATE, interval);
//        } else if (intervalType.equalsIgnoreCase("Week")) {
//            cal.add(Calendar.DATE, interval * 7);
//        } else if (intervalType.equalsIgnoreCase("Month")) {
//            cal.add(Calendar.MONTH, interval);
//        }
        Date nextDate = RepeatedInvoices.calculateNextDate(rinfo.getNextDate(), rinfo.getIntervalUnit(), rinfo.getIntervalType());
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("id", rinfo.getId());
        dataMap.put("nextDate", nextDate);
        accInvoiceDAOobj.saveRepeateInvoiceInfo(dataMap);
    }

    public Invoice repeateInvoice(Invoice invoice) throws ServiceException {
        Invoice repeatedInvoice = null;
        JSONObject invjson = new JSONObject();
        try {
            String companyid = invoice.getCompany().getCompanyID();
            String currencyid = invoice.getCurrency().getCurrencyID();

            invjson.put("customerid", invoice.getCustomer()==null?invoice.getCustomerEntry().getAccount().getID():invoice.getCustomer().getID());
            String nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_INVOICE);
            boolean nextInv=false;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("parentInvoiceId", invoice.getID());
            KwlReturnObject details = accInvoiceDAOobj.getRepeateInvoicesDetails(requestParams);
            List detailsList = details.getEntityList();
            int repInvoiceCount=detailsList.size();
            String entryNumber="";//nextAutoNo;
            while(nextInv==false){
                entryNumber=invoice.getInvoiceNumber()+"-"+repInvoiceCount;
                details = accInvoiceDAOobj.getInvoiceCount(entryNumber, companyid);
                int nocount = details.getRecordTotalCount();
                if (nocount > 0) {
                    repInvoiceCount++;
                    continue;
                }
                else nextInv=true;
            }
            invjson.put("entrynumber", entryNumber);
            invjson.put("autogenerated", nextAutoNo.equals(entryNumber));
            invjson.put("memo", invoice.getMemo());
            invjson.put("billto", invoice.getBillTo());
            invjson.put("shipaddress", invoice.getShipTo());
            invjson.put("companyid", companyid);
            invjson.put("currencyid", currencyid);
            invjson.put("externalCurrencyRate", invoice.getExternalCurrencyRate());
            invjson.put("erdid", invoice.getExchangeRateDetail().getID());

            Calendar cal = Calendar.getInstance();//Remove Time
            cal.set(Calendar.HOUR_OF_DAY, 12);
            cal.set(Calendar.MINUTE, 00);
            cal.set(Calendar.SECOND, 00);
            Date BillDate = cal.getTime();

            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(BillDate);
            if(invoice.getCustomer()!=null){
                if(invoice.getCustomer().getCreditTerm()!=null){
                    int termDays = invoice.getCustomer().getCreditTerm().getTermdays();
                    dueDate.add(Calendar.DATE, termDays);
                }
            }
            invjson.put("shipdate", BillDate);
            invjson.put("duedate", dueDate.getTime());


            Discount DSC = invoice.getDiscount();
            if (DSC != null) {
                JSONObject discjson = new JSONObject();
                discjson.put("discount", DSC.getDiscount());
                discjson.put("inpercent", DSC.isInPercent());
                discjson.put("originalamount", DSC.getOriginalAmount());
                discjson.put("companyid", companyid);
                KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                Discount discount = (Discount) dscresult.getEntityList().get(0);
                invjson.put("discountid", discount.getID());
            }

            // Create Journal Entry
            boolean jeautogenflag = true;
            String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
            String jeentryNumber = nextJEAutoNo;// + "/" + nextAutoNo;

            JournalEntry OLD_JE = invoice.getJournalEntry();
            Map<String,Object> jeDataMap = new HashMap<String, Object>();
            jeDataMap.put("DontCheckYearLock", true);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", BillDate);
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("memo", OLD_JE.getMemo());
            if(OLD_JE.getCostcenter()!=null){
                jeDataMap.put("costcenterid", OLD_JE.getCostcenter().getID());
            }
            jeDataMap.put("currencyid", currencyid);
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String jeid = journalEntry.getID();
            invjson.put("journalerentryid", jeid);
            jeDataMap.put("jeid", jeid);

            Set JE_DETAILS = invoice.getJournalEntry().getDetails();
            HashSet<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();
            HashMap<String, String> oldNnewJEDid = new HashMap<String, String>();
            Iterator jeditr = JE_DETAILS.iterator();
            while (jeditr.hasNext()) {
                JournalEntryDetail OLD_JED = (JournalEntryDetail) jeditr.next();
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", OLD_JED.getSrno());
                jedjson.put("companyid", OLD_JED.getCompany().getCompanyID());
                jedjson.put("amount", OLD_JED.getAmount());
                jedjson.put("accountid", OLD_JED.getAccount().getID());
                jedjson.put("debit", OLD_JED.isDebit());
                jedjson.put("jeid", jeid);
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);
                oldNnewJEDid.put(OLD_JED.getID(),jed.getID());
            }
            jeDataMap.put("jedetails", jeDetails);
            jeDataMap.put("externalCurrencyRate", OLD_JE.getExternalCurrencyRate());
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            JournalEntryDetail CUST_JED = invoice.getCustomerEntry();
            if(CUST_JED != null) {
                invjson.put("customerentryid", oldNnewJEDid.get(CUST_JED.getID()));
            }

            JournalEntryDetail SHIP_JED = invoice.getShipEntry();
            if (SHIP_JED != null) {
                invjson.put("shipentryid", oldNnewJEDid.get(SHIP_JED.getID()));
            }

            JournalEntryDetail OTHER_JED = invoice.getOtherEntry();
            if (OTHER_JED != null) {
                invjson.put("otherentryid", oldNnewJEDid.get(OTHER_JED.getID()));
            }

            Tax TAX = invoice.getTax();
            if(TAX != null){
                invjson.put("taxid", TAX.getID());
            }

            JournalEntryDetail TAX_JED = invoice.getTaxEntry();
            if (TAX_JED != null) {
                invjson.put("otherentryid", oldNnewJEDid.get(TAX_JED.getID()));
            }

            KwlReturnObject result = accInvoiceDAOobj.addInvoice(invjson, new HashSet());
            repeatedInvoice = (Invoice) result.getEntityList().get(0);//Create Invoice without invoice-details.

            //Invoice Details
            Set INVOICE_DETAILS = invoice.getRows();
            HashSet<InvoiceDetail> invcdetails = new HashSet<InvoiceDetail>();
            Iterator itr = INVOICE_DETAILS.iterator();
            while (itr.hasNext()) {
                InvoiceDetail ivd = (InvoiceDetail) itr.next();
                InvoiceDetail row = new InvoiceDetail();
                row.setSrno(ivd.getSrno());
                row.setSalesorderdetail(ivd.getSalesorderdetail());
                row.setCompany(ivd.getCompany());
                row.setRate(ivd.getRate());
                row.setInvoice(repeatedInvoice);
                if(ivd.getTax()!=null){
                    row.setTax(ivd.getTax());
                }

                Inventory ID_INVENTORY = ivd.getInventory();
                if(ID_INVENTORY != null) {
                    JSONObject inventoryjson = new JSONObject();
                    inventoryjson.put("productid", ID_INVENTORY.getProduct().getID());
                    inventoryjson.put("quantity", ID_INVENTORY.getQuantity());
                    inventoryjson.put("description", ID_INVENTORY.getDescription());
                    inventoryjson.put("carryin", false);
                    inventoryjson.put("defective", false);
                    inventoryjson.put("newinventory", false);
                    inventoryjson.put("companyid", companyid);
                    inventoryjson.put("updatedate", ID_INVENTORY.getUpdateDate());
                    KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                    Inventory inventory = (Inventory) invresult.getEntityList().get(0);
                    row.setInventory(inventory);
                }

                Discount ID_DISCOUNT = row.getDiscount();
                if (ID_DISCOUNT != null) {
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", ID_DISCOUNT.getDiscount());
                    discjson.put("inpercent", true);
                    discjson.put("originalamount", ID_DISCOUNT.getOriginalAmount());
                    discjson.put("companyid", companyid);
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                }
                invcdetails.add(row);
            }

            invjson.put("invoiceid", repeatedInvoice.getID());
            invjson.put("parentid", invoice.getID());
            result = accInvoiceDAOobj.updateInvoice(invjson, invcdetails);
            repeatedInvoice = (Invoice) result.getEntityList().get(0);//Add invoice details

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(""+ex.getMessage(), ex);
        } catch (AccountingException ex) {
            throw ServiceException.FAILURE(""+ex.getMessage(), ex);
        } catch(JSONException ex){
            throw ServiceException.FAILURE(""+ex.getMessage(), ex);
        } catch(Exception ex){
            throw ServiceException.FAILURE(""+ex.getMessage(), ex);
        }

        return repeatedInvoice;
    }

    public BillingInvoice repeateBillingInvoice(BillingInvoice invoice) throws ServiceException {
        BillingInvoice repeatedInvoice = null;
        JSONObject invjson = new JSONObject();
        try {
            String companyid = invoice.getCompany().getCompanyID();
            String currencyid = invoice.getCurrency().getCurrencyID();

            invjson.put("customerid", invoice.getCustomer()==null?invoice.getCustomerEntry().getAccount().getID():invoice.getCustomer().getID());
            String nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGINVOICE);
            boolean nextInv=false;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("parentInvoiceId", invoice.getID());
            KwlReturnObject details = accInvoiceDAOobj.getRepeateInvoicesDetails(requestParams);
            List detailsList = details.getEntityList();
            int repInvoiceCount=detailsList.size();
            String entryNumber="";//nextAutoNo;
            while(nextInv==false){
                entryNumber=invoice.getBillingInvoiceNumber()+"-"+repInvoiceCount;
                details = accInvoiceDAOobj.getBillingInvoiceCount(entryNumber, companyid);
                int nocount = details.getRecordTotalCount();
                if (nocount > 0) {
                    repInvoiceCount++;
                    continue;
                }
                else nextInv=true;
            }
            invjson.put("entrynumber", entryNumber);
            invjson.put("autogenerated", nextAutoNo.equals(entryNumber));
            invjson.put("memo", invoice.getMemo());
            invjson.put("billto", invoice.getBillTo());
            invjson.put("shipaddress", invoice.getShipTo());
            invjson.put("companyid", companyid);
            invjson.put("currencyid", currencyid);
            invjson.put("externalCurrencyRate", invoice.getExternalCurrencyRate());
            invjson.put("erdid", invoice.getExchangeRateDetail().getID());

            Calendar cal = Calendar.getInstance();//Remove Time
            cal.set(Calendar.HOUR_OF_DAY, 12);
            cal.set(Calendar.MINUTE, 00);
            cal.set(Calendar.SECOND, 00);
            Date BillDate = cal.getTime();
            
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(BillDate);
            if(invoice.getCustomer()!=null){
                if(invoice.getCustomer().getCreditTerm()!=null){
                    int termDays = invoice.getCustomer().getCreditTerm().getTermdays();
                    dueDate.add(Calendar.DATE, termDays);
                }
            }
            invjson.put("shipdate", BillDate);
            invjson.put("duedate", dueDate.getTime());


            Discount DSC = invoice.getDiscount();
            if (DSC != null) {
                JSONObject discjson = new JSONObject();
                discjson.put("discount", DSC.getDiscount());
                discjson.put("inpercent", DSC.isInPercent());
                discjson.put("originalamount", DSC.getOriginalAmount());
                discjson.put("companyid", companyid);
                KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                Discount discount = (Discount) dscresult.getEntityList().get(0);
                invjson.put("discountid", discount.getID());
            }

            // Create Journal Entry
            boolean jeautogenflag = true;
            String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
            String jeentryNumber = nextJEAutoNo;// + "/" + nextAutoNo;

            JournalEntry OLD_JE = invoice.getJournalEntry();
            Map<String,Object> jeDataMap = new HashMap<String, Object>();
            jeDataMap.put("DontCheckYearLock", true);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", BillDate);
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("memo", OLD_JE.getMemo());
            jeDataMap.put("currencyid", currencyid);
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String jeid = journalEntry.getID();
            invjson.put("journalerentryid", jeid);
            jeDataMap.put("jeid", jeid);

            Set JE_DETAILS = invoice.getJournalEntry().getDetails();
            HashSet<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();
            HashMap<String, String> oldNnewJEDid = new HashMap<String, String>();
            Iterator jeditr = JE_DETAILS.iterator();
            while (jeditr.hasNext()) {
                JournalEntryDetail OLD_JED = (JournalEntryDetail) jeditr.next();
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", OLD_JED.getSrno());
                jedjson.put("companyid", OLD_JED.getCompany().getCompanyID());
                jedjson.put("amount", OLD_JED.getAmount());
                jedjson.put("accountid", OLD_JED.getAccount().getID());
                jedjson.put("debit", OLD_JED.isDebit());
                jedjson.put("jeid", jeid);
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);
                oldNnewJEDid.put(OLD_JED.getID(),jed.getID());
            }
            jeDataMap.put("jedetails", jeDetails);
            jeDataMap.put("externalCurrencyRate", OLD_JE.getExternalCurrencyRate());
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            JournalEntryDetail CUST_JED = invoice.getCustomerEntry();
            if(CUST_JED != null) {
                invjson.put("customerentryid", oldNnewJEDid.get(CUST_JED.getID()));
            }

            JournalEntryDetail SHIP_JED = invoice.getShipEntry();
            if (SHIP_JED != null) {
                invjson.put("shipentryid", oldNnewJEDid.get(SHIP_JED.getID()));
            }

            JournalEntryDetail OTHER_JED = invoice.getOtherEntry();
            if (OTHER_JED != null) {
                invjson.put("otherentryid", oldNnewJEDid.get(OTHER_JED.getID()));
            }

            Tax TAX = invoice.getTax();
            if(TAX != null){
                invjson.put("taxid", TAX.getID());
            }

            JournalEntryDetail TAX_JED = invoice.getTaxEntry();
            if (TAX_JED != null) {
                invjson.put("otherentryid", oldNnewJEDid.get(TAX_JED.getID()));
            }

            JournalEntryDetail CREDITOR_JED = invoice.getCreditorEntry();
            if (CREDITOR_JED != null) {
                invjson.put("creditorentryid", oldNnewJEDid.get(CREDITOR_JED.getID()));
            }

            KwlReturnObject result = accInvoiceDAOobj.addBillingInvoice(invjson, new HashSet());
            repeatedInvoice = (BillingInvoice) result.getEntityList().get(0);//Create Invoice without invoice-details.

            //Invoice Details
            Set INVOICE_DETAILS = invoice.getRows();
            HashSet<BillingInvoiceDetail> invcdetails = new HashSet<BillingInvoiceDetail>();
            Iterator itr = INVOICE_DETAILS.iterator();
            while (itr.hasNext()) {
                BillingInvoiceDetail ivd = (BillingInvoiceDetail) itr.next();
                BillingInvoiceDetail row = new BillingInvoiceDetail();
                row.setSrno(ivd.getSrno());
                row.setSalesOrderDetail(ivd.getSalesOrderDetail());
                row.setCompany(ivd.getCompany());
                row.setRate(ivd.getRate());
                row.setAmount(ivd.getAmount());
                row.setQuantity(ivd.getQuantity());
                row.setProductDetail(ivd.getProductDetail());
                row.setBillingInvoice(repeatedInvoice);
                if(ivd.getTax()!=null){
                    row.setTax(ivd.getTax());
                }

                Discount ID_DISCOUNT = row.getDiscount();
                if (ID_DISCOUNT != null) {
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", ID_DISCOUNT.getDiscount());
                    discjson.put("inpercent", true);
                    discjson.put("originalamount", ID_DISCOUNT.getOriginalAmount());
                    discjson.put("companyid", companyid);
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                }
                invcdetails.add(row);
            }

            invjson.put("invoiceid", repeatedInvoice.getID());
            invjson.put("parentid", invoice.getID());
            result = accInvoiceDAOobj.updateBillingInvoice(invjson, invcdetails);
            repeatedInvoice = (BillingInvoice) result.getEntityList().get(0);//Add invoice details

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(""+ex.getMessage(), ex);
        } catch (AccountingException ex) {
            throw ServiceException.FAILURE(""+ex.getMessage(), ex);
        } catch(JSONException ex){
            throw ServiceException.FAILURE(""+ex.getMessage(), ex);
        } catch(Exception ex){
            throw ServiceException.FAILURE(""+ex.getMessage(), ex);
        }

        return repeatedInvoice;
    }

}
