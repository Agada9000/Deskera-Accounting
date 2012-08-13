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

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.CreditNoteDetail;
import com.krawler.hql.accounting.Discount;

import com.krawler.hql.accounting.BillingCreditNoteDetail;
import com.krawler.hql.accounting.BillingInvoice;
import com.krawler.hql.accounting.BillingInvoiceDetail;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.ReceiptDetail;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
/**
 *
 * @author krawler
 */
public class accInvoiceCMN {
    private accCreditNoteDAO accCreditNoteobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accTaxDAO accTaxObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accReceiptDAO accReceiptobj;

    public void setAccCreditNoteDAO(accCreditNoteDAO accCreditNoteobj) {
        this.accCreditNoteobj = accCreditNoteobj;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccReceiptDAO(accReceiptDAO accReceiptobj) {
        this.accReceiptobj = accReceiptobj;
    }

    public JSONArray getInvoiceRows(HttpServletRequest request, String[] invoices) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);

            KwlReturnObject curresult = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            invoices =(invoices==null)? request.getParameterValues("bills"):invoices;
            int i = 0;

            HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("invoice.ID");
            order_by.add("srno");
            order_type.add("asc");
            invRequestParams.put("filter_names", filter_names);
            invRequestParams.put("filter_params", filter_params);
            invRequestParams.put("order_by", order_by);
            invRequestParams.put("order_type", order_type);

            while (invoices != null && i < invoices.length) {
                KwlReturnObject result = kwlCommonTablesDAOObj.getObject(Invoice.class.getName(), invoices[i]);
                Invoice invoice = (Invoice) result.getEntityList().get(0);
//                Iterator itr = invoice.getRows().iterator();
                filter_params.clear();
                filter_params.add(invoice.getID());
                KwlReturnObject idresult = accInvoiceDAOobj.getInvoiceDetails(invRequestParams);
                Iterator itr = idresult.getEntityList().iterator();
                
                HashMap hm = applyCreditNotes(requestParams, invoice);
                while (itr.hasNext()) {
                    InvoiceDetail row = (InvoiceDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", invoice.getID());
                    String currencyid = (invoice.getCurrency() == null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());
                    obj.put("currencysymbol", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
                    obj.put("billno", invoice.getInvoiceNumber());
//                    obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,invoice.getJournalEntry().getEntryDate()));
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    obj.put("oldcurrencyrate", (Double) bAmt.getEntityList().get(0));
                    Inventory inv = row.getInventory();
                    Product prod = inv.getProduct();
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", prod.getID());
                    obj.put("productname", prod.getName());
                    obj.put("unitname", prod.getUnitOfMeasure()==null?"":prod.getUnitOfMeasure().getName());
                    obj.put("rateinbase",accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,(row.getRate()),row.getInvoice().getCurrency().getCurrencyID(),row.getInvoice().getJournalEntry().getEntryDate(),row.getInvoice().getExternalCurrencyRate()).getEntityList().get(0));
                    obj.put("pid",prod.getProductid());
                    obj.put("type",prod.getProducttype()==null?"":prod.getProducttype().getName());
                    obj.put("productname", prod.getName());
                    Discount disc = row.getDiscount();
                    if (disc != null && disc.isInPercent()) {
                        obj.put("prdiscount", disc.getDiscount()); //product discount in percent
                    } else {
                        obj.put("prdiscount", 0);
                    }
                    obj.put("desc", inv.getDescription());
                    obj.put("rate", row.getRate());  //rate of the product
                    obj.put("quantity", row.getInventory().getQuantity());
                    int remainingquantity = 0;

                    double amount = 0;
                    if (hm.containsKey(row)) {
                        Object[] val = (Object[]) hm.get(row);
                        amount = (Double) val[0];
                        remainingquantity = (Integer) val[1];
                        obj.put("remainingquantity", remainingquantity);
                        obj.put("remquantity", 0);
                        obj.put("amount", amount);
                    }
                    HashMap amthm = getInvoiceProductAmount(invoice);
                    Object[] val = (Object[]) amthm.get(row);
                    amount = (Double) val[0];
                    obj.put("orignalamount", amount);
                    double taxPercent = 0;
                    double rowTaxPercent = 0;
                    if (row.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("prtaxid", row.getTax()== null?"":row.getTax().getID());
                    if (invoice.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);
                    jArr.put(obj);
                }
                i++;
            }
        } catch(JSONException e) {
            throw ServiceException.FAILURE("accInvoiceController.getInvoiceRows", e);
        }
        return jArr;
    }

    public double getAmountDue(HashMap<String, Object> requestParams, Invoice invoice) throws ServiceException {
        double amountdue=0;
        double amount = 0, ramount = 0;
        String currencyid = (String) requestParams.get("gcurrencyid");
        String companyid = (String) requestParams.get("companyid");
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID=baseCurrency.getCurrencyID();
        Iterator itrCn = applyCreditNotes(requestParams, invoice).values().iterator();
        currencyid = (invoice.getCurrency() == null ?baseCurrencyID : invoice.getCurrency().getCurrencyID());
        while (itrCn.hasNext()) {
            Object[] temp = (Object[]) itrCn.next();
            amount += (Double) temp[0] - (Double) temp[2];
        }
        JournalEntryDetail tempd = invoice.getTaxEntry();
        tempd = invoice.getTaxEntry();
        if (tempd != null) {
            amount += tempd.getAmount();
        }
                KwlReturnObject result = accReceiptobj.getReceiptFromInvoice(invoice.getID(), companyid);
        List l = result.getEntityList();
        Iterator recitr = l.iterator();
         while (recitr.hasNext()) {
                    ReceiptDetail rd=(ReceiptDetail) recitr.next();
                    ramount+=rd.getAmount();
                    String fromcurrencyid=(rd.getReceipt().getCurrency()==null?baseCurrencyID:rd.getReceipt().getCurrency().getCurrencyID());
//                   ramount=CompanyHandler.getOneCurrencyToOther(session,request,ramount,fromcurrencyid,currencyid,invoice.getJournalEntry().getEntryDate());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, ramount, fromcurrencyid, currencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    ramount = (Double) bAmt.getEntityList().get(0);
                }
        amountdue=amount-ramount;
        return amountdue;
    }

    public double applyInvDisount(InvoiceDetail invdetail, double withoutDTAmt) throws ServiceException {
        double disc = (invdetail.getInvoice().getDiscount() == null ? 0 : invdetail.getInvoice().getDiscount().getDiscountValue());
        if (disc == 0) {
            return 0;
        }
        int quantity = invdetail.getInventory().getQuantity();
        double amount = (quantity == 0 ? 0 : invdetail.getRate() * quantity);
        double rowDiscountRatio = (withoutDTAmt == 0 ? 0 : amount / withoutDTAmt);
        double rowDiscount = disc * rowDiscountRatio;
        return rowDiscount;
    }
    public HashMap applyCreditNotes(HashMap requestParams, Invoice invoice) throws ServiceException {
        HashMap hm=new HashMap();
        Set invRows=invoice.getRows();
        Iterator itr=invRows.iterator();
        KwlReturnObject curresult = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), (String) requestParams.get("gcurrencyid"));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        double amount;
        int quantity;
        double withoutDTAmt=0;
        while(itr.hasNext()){//reqiured for invoice discount row wise division[PS]
            InvoiceDetail temp=(InvoiceDetail)itr.next();
            withoutDTAmt+=temp.getRate()*temp.getInventory().getQuantity();
        }
        itr=invRows.iterator();
        while(itr.hasNext()){
            InvoiceDetail temp=(InvoiceDetail)itr.next();
            quantity=temp.getInventory().getQuantity();
            amount=temp.getRate()*quantity;
            double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
            double rowTaxPercent = 0;
            double rowWithDTAmt=0;
            if (temp.getTax() != null) {
                KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), invoice.getJournalEntry().getEntryDate(), temp.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
            }
            double rowWithDAmt=amount-rdisc;
            rowWithDTAmt=rowWithDAmt+ (rowWithDAmt*rowTaxPercent)/100;
            double invoiceDisc=temp.getInvoice().getDiscount()==null?0:applyInvDisount(temp,withoutDTAmt);
            rowWithDTAmt-=invoiceDisc;
            hm.put(temp, new Object[]{rowWithDTAmt, quantity, 0.0,rowWithDAmt-invoiceDisc});
            if(invoice==null)invoice=temp.getInvoice();
        }
        KwlReturnObject result = accCreditNoteobj.getCNRowsDiscountFromInvoice(invoice.getID());
        List list = result.getEntityList();
        Iterator cnitr = list.iterator();
        double cnTaxAmount=0;
        while (cnitr.hasNext()) {
            Object[] cnrow = (Object[]) cnitr.next();
            CreditNoteDetail cnr = (CreditNoteDetail) cnrow[1];
            InvoiceDetail temp = cnr.getInvoiceRow();
            if (!hm.containsKey(temp)) {
                continue;
            }
            Object[] val = (Object[]) hm.get(temp);
            String fromcurrencyid=(cnr.getCreditNote().getCurrency()==null?currency.getCurrencyID(): cnr.getCreditNote().getCurrency().getCurrencyID());
            String tocurrencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
//                double v=(Double)val[0]-(cnr.getDiscount()==null?0:CompanyHandler.getOneCurrencyToOther(session,request,cnr.getDiscount().getDiscountValue(),fromcurrencyid,tocurrencyid,invoice.getJournalEntry().getEntryDate()));
            double baseDisount = 0;
            if(cnr.getDiscount() != null){
               KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, cnr.getDiscount().getDiscountValue(), fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(),invoice.getJournalEntry().getExternalCurrencyRate());
               baseDisount = (Double) bAmt.getEntityList().get(0);
            }
            double v = (Double) val[0] - (cnr.getDiscount() == null ? 0 : baseDisount);
            if (cnr.getTaxAmount() != null) {
                cnTaxAmount = cnr.getTaxAmount() + (Double) val[2];
            }
            int q = (Integer) val[1];
            if (temp.getInventory() != null) {
                q -= cnr.getQuantity();
            }
            hm.put(temp, new Object[]{v,q,cnTaxAmount,val[3]});//formula for val[3]:(rate*quantity)-rowdiscount-invdiscount-cnamount[PS]
        }
        return hm;
    }

    private HashMap getInvoiceProductAmount(Invoice invoice) throws ServiceException {
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
            double rowTaxPercent = 0;
            if (temp.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent( invoice.getCompany().getCompanyID(), invoice.getJournalEntry().getEntryDate(), temp.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
            }
             double ramount=amount - rdisc;
             ramount+=ramount*rowTaxPercent/100;
            hm.put(temp, new Object[]{ramount, quantity});
        //    hm.put(temp, new Object[]{amount-rdisc+rowTaxPercent,quantity});
            if(invoice==null)invoice=temp.getInvoice();
        }
        return hm;
    }
    
    public HashMap applyBillingCreditNotes(HashMap requestParams, BillingInvoice invoice) throws ServiceException {
        HashMap hm=new HashMap();
        Set invRows=invoice.getRows();
        Iterator itr=invRows.iterator();
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), (String) requestParams.get("gcurrencyid"));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

        double amount;
        double quantity;
        double withoutDTAmt=0;
        while(itr.hasNext()){//reqiured for invoice discount row wise division[PS]
            BillingInvoiceDetail temp=(BillingInvoiceDetail)itr.next();
            withoutDTAmt+=temp.getRate()*temp.getQuantity();
        }
        itr=invRows.iterator();
        while(itr.hasNext()){
            BillingInvoiceDetail temp=(BillingInvoiceDetail)itr.next();
            quantity=temp.getQuantity();
            amount=temp.getRate()*quantity;
            double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
            double rowTaxPercent = 0;
            if (temp.getTax() != null) {
                KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), invoice.getJournalEntry().getEntryDate(), temp.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
            }
            double ramount=amount-rdisc;
            ramount+=ramount*rowTaxPercent/100;
            double invoiceDisc=temp.getBillingInvoice().getDiscount()==null?0:applyBillingInvDisount(temp,withoutDTAmt);
            ramount-=invoiceDisc;
            hm.put(temp, new Object[]{ramount, quantity, 0.0});
            if(invoice==null)invoice=temp.getBillingInvoice();
        }
        KwlReturnObject result = accCreditNoteobj.getCNRowsDiscountFromBillingInvoice(invoice.getID());
        List list = result.getEntityList();
        Iterator cnitr = list.iterator();
        double taxAmount=0;
        while(cnitr.hasNext()){
            Object[] cnrow=(Object[])cnitr.next();
            BillingCreditNoteDetail cnr=(BillingCreditNoteDetail)cnrow[1];
            BillingInvoiceDetail temp=cnr.getInvoiceRow();
            if(!hm.containsKey(temp))continue;
            Object[] val=(Object[])hm.get(temp);
            String fromcurrencyid=(cnr.getCreditNote().getCurrency()==null?currency.getCurrencyID(): cnr.getCreditNote().getCurrency().getCurrencyID());
            String tocurrencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
            double baseDisount = 0;
            if(cnr.getDiscount() != null){
               KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, cnr.getDiscount().getDiscountValue(), fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(),invoice.getJournalEntry().getExternalCurrencyRate());
               baseDisount = (Double) bAmt.getEntityList().get(0);
            }
            double v = (Double) val[0] - (cnr.getDiscount() == null ? 0 : baseDisount);
            if(cnr.getTaxAmount()!=null){
                taxAmount+=cnr.getTaxAmount();
            }
            double q=(Double)val[1];
            q-=cnr.getQuantity();
            hm.put(temp, new Object[]{v,q,taxAmount});
        }
        return hm;
    }
    
    public double applyBillingInvDisount(BillingInvoiceDetail invdetail, double withoutDTAmt) throws ServiceException {
        double disc = (invdetail.getBillingInvoice().getDiscount() == null ? 0 : invdetail.getBillingInvoice().getDiscount().getDiscountValue());
        if (disc == 0) {
            return 0;
        }
        double quantity = invdetail.getQuantity();
        double amount = (quantity == 0 ? 0 : invdetail.getRate() * quantity);
        double rowDiscountRatio = (withoutDTAmt == 0 ? 0 : amount / withoutDTAmt);
        double rowDiscount = disc * rowDiscountRatio;
        return rowDiscount;
    }
}
