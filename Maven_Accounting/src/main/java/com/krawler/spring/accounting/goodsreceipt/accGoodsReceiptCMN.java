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

package com.krawler.spring.accounting.goodsreceipt;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.BillingDebitNoteDetail;
import com.krawler.hql.accounting.BillingGoodsReceipt;
import com.krawler.hql.accounting.BillingGoodsReceiptDetail;
import com.krawler.hql.accounting.DebitNoteDetail;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.ExpenseGRDetail;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.GoodsReceiptDetail;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.PaymentDetail;
import com.krawler.hql.accounting.Product;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
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
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
/**
 *
 * @author krawler
 */
public class accGoodsReceiptCMN implements GoodsReceiptCMNConstants{

    private accDebitNoteDAO accDebitNoteobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accTaxDAO accTaxObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accVendorPaymentDAO accVendorPaymentobj;

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
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
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }

    public JSONArray getGoodsReceiptRows(HttpServletRequest request, String[] greceipts) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(BILLS, request.getParameterValues(BILLS));
            String isexpenseinvStr=(String)request.getParameter("isexpenseinv");
            boolean isexpenseinv =false;
            if(!StringUtil.isNullOrEmpty(isexpenseinvStr)){
                isexpenseinv = Boolean.parseBoolean(isexpenseinvStr);
            }
            String currencyid = (String) requestParams.get(GCURRENCYID);
            KwlReturnObject curresult = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            greceipts = (greceipts == null)? (String[]) requestParams.get(BILLS) : greceipts;
            int i = 0;

            HashMap<String, Object> grRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("goodsReceipt.ID");
            order_by.add(SRNO);
            order_type.add("asc");
            grRequestParams.put( FILTER_NAMES,filter_names);
            grRequestParams.put( FILTER_PARAMS,filter_params);
            grRequestParams.put( ORDER_BY,order_by);
            grRequestParams.put( ORDER_TYPE,order_type);

            while (greceipts != null && i < greceipts.length) {
//                GoodsReceipt gReceipt = (GoodsReceipt) session.get(GoodsReceipt.class, greceipts[i]);
                KwlReturnObject grresult = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(), greceipts[i]);
                GoodsReceipt gReceipt = (GoodsReceipt) grresult.getEntityList().get(0);
//                Iterator itr = gReceipt.getRows().iterator();
                filter_params.clear();
                filter_params.add(gReceipt.getID());
                if(isexpenseinv){
                    KwlReturnObject grdresult = accGoodsReceiptobj.getExpenseGRDetails(grRequestParams);
                    List<ExpenseGRDetail> expenseGRDetailList = grdresult.getEntityList();

                    if (expenseGRDetailList != null && !expenseGRDetailList.isEmpty())
                    {
                        for (ExpenseGRDetail expenseGRDetail: expenseGRDetailList)
                        {
                            jArr=getExpenseGRRow(requestParams,expenseGRDetail,gReceipt,currency,jArr);
                        }
                    }
                    /*Iterator itr = grdresult.getEntityList().iterator();

                     while (itr.hasNext()) {
                        ExpenseGRDetail row = (ExpenseGRDetail) itr.next();
                        jArr=getExpenseGRRow(requestParams,row,gReceipt,currency,jArr);
                    }*/
                }
                else{
                    Map<GoodsReceiptDetail, Object[]> hm = applyDebitNotes(requestParams, gReceipt);
                    KwlReturnObject grdresult = accGoodsReceiptobj.getGoodsReceiptDetails(grRequestParams);
                    List<GoodsReceiptDetail> goodsReceiptDetailList= grdresult.getEntityList();
                    if (goodsReceiptDetailList != null && !goodsReceiptDetailList.isEmpty()){
                        for (GoodsReceiptDetail row: goodsReceiptDetailList){
                            jArr=getGRRow(requestParams,row,hm,gReceipt,currency,jArr);
                        }
                    }
//                    KwlReturnObject grdresult = accGoodsReceiptobj.getGoodsReceiptDetails(grRequestParams);
//                    Iterator itr = grdresult.getEntityList().iterator();
//                    Map<GoodsReceiptDetail, Object[]> hm = applyDebitNotes(requestParams, gReceipt);
//                    while (itr.hasNext()) {
//                        GoodsReceiptDetail row = (GoodsReceiptDetail) itr.next();
//                        jArr=getGRRow(requestParams,row,hm,gReceipt,currency,jArr);
//                    }



                   
                }
                i++;
            }
        } catch(Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getGoodsReceiptRows : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getExpenseGRRow(Map<String, Object> requestParams,ExpenseGRDetail row, GoodsReceipt gReceipt, KWLCurrency currency,JSONArray jArr) throws ServiceException {
        try {
            String companyid=(String) requestParams.get(COMPANYID);
            JSONObject obj = new JSONObject();
            obj.put(BILLID, gReceipt.getID());
            obj.put(BILLNO, gReceipt.getGoodsReceiptNumber());
            String currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
            obj.put(CURRENCYSYMBOL, (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
            KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, gReceipt.getJournalEntry().getEntryDate(),gReceipt.getJournalEntry().getExternalCurrencyRate());
            obj.put(OLDCURRENCYRATE, (Double) bAmt.getEntityList().get(0));
            obj.put(SRNO, row.getSrno());
            obj.put(ROWID, row.getID());
            obj.put(ACCOUNTID, row.getAccount().getID());
            obj.put(ACCOUNTNAME, row.getAccount().getName());
            obj.put(RATEINBASE,accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,(row.getRate()),row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getJournalEntry().getEntryDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
            Discount disc = row.getDiscount();
            if (disc != null && disc.isInPercent()) {
                obj.put(PRDISCOUNT, disc.getDiscount());
            } else {
                obj.put(PRDISCOUNT, 0);
            }
            obj.put(RATE, row.getRate());


//** Code for cal Debit Note amount[PS]
//                    double amount = 0;
//                    if (hm.containsKey(row)) {
//                        Object[] val = (Object[]) hm.get(row);
//                      //  amount = (Double) val[0];//without invoice tax
//                        remainingquantity = (Integer) val[1];
//                        obj.put("remainingquantity", remainingquantity);
//                        obj.put("remquantity", 0);
//
//                    }
///

//** Code for Cal Payment Received [PS]
                    Map amthm = getExpenseGRAmount(gReceipt);
                    Object[] val = (Object[]) amthm.get(row);
                    double amount = (Double) val[0];
                    obj.put( ORIGNALAMOUNT,amount);
                    obj.put( AMOUNT,amount);
///

            double taxPercent = 0;
            double rowTaxPercent = 0;
            if (row.getTax() != null) {
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), row.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
            }
            obj.put( PRTAXPERCENT,rowTaxPercent);
            obj.put(PRTAXID, row.getTax()== null?"":row.getTax().getID());
            if (gReceipt.getTax() != null) {
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getTax().getID());
                taxPercent = (Double) perresult.getEntityList().get(0);
            }
            obj.put( TAXPERCENT,taxPercent);
            jArr.put(obj);
        } catch(Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getGoodsReceiptRows : "+ex.getMessage(), ex);
        }
        return jArr;
    }


    public JSONArray getGRRow(Map<String, Object> requestParams,GoodsReceiptDetail row,Map dnhm, GoodsReceipt gReceipt, KWLCurrency currency,JSONArray jArr) throws ServiceException {

        try {
            String companyid=(String) requestParams.get(COMPANYID);
            JSONObject obj = new JSONObject();
            obj.put(BILLID, gReceipt.getID());
            obj.put(BILLNO, gReceipt.getGoodsReceiptNumber());
            String currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
            obj.put(CURRENCYSYMBOL, (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
//                    obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,gReceipt.getJournalEntry().getEntryDate()));
            KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, gReceipt.getJournalEntry().getEntryDate(),gReceipt.getJournalEntry().getExternalCurrencyRate());
            obj.put(OLDCURRENCYRATE, (Double) bAmt.getEntityList().get(0));
            Inventory inv = row.getInventory();
            Product prod = inv.getProduct();
            obj.put(SRNO, row.getSrno());
            obj.put(ROWID, row.getID());
            obj.put(PRODUCTID, prod.getID());
            obj.put(PRODUCTNAME, prod.getName());
            obj.put(UNITNAME, prod.getUnitOfMeasure()==null?"":prod.getUnitOfMeasure().getName());
            obj.put(RATEINBASE,accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,(row.getRate()),row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getJournalEntry().getEntryDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
            obj.put(PID,prod.getProductid());
            obj.put(TYPE,prod.getProducttype()==null?"":prod.getProducttype().getName());

            Discount disc = row.getDiscount();
            if (disc != null && disc.isInPercent()) {
                obj.put(PRDISCOUNT, disc.getDiscount());
            } else {
                obj.put(PRDISCOUNT, 0);
            }
            obj.put(DESC, inv.getDescription());
            obj.put(RATE, row.getRate());
            obj.put(QUANTITY, row.getInventory().getQuantity());
            int remainingquantity = 0;

            double amount = 0, discount = 0;
            if (dnhm.containsKey(row)) {
                Object[] val = (Object[]) dnhm.get(row);
              //  amount = (Double) val[0];//without invoice tax
                remainingquantity = (Integer) val[1];
                obj.put( REMAININGQUANTITY,remainingquantity);
                obj.put(REMQUANTITY, 0);

            }
            Map amthm = getGoodsReceiptProductAmount(gReceipt);
            Object[] val = (Object[]) amthm.get(row);
            amount = (Double) val[0];
            obj.put( ORIGNALAMOUNT,amount);
//            select sum(discount) from discount where id in (select discount from dndetails where goodsReceiptRow = 'ff8080813402854c013402c5d1490018');
//            KwlReturnObject remAmount = accDebitNoteobj.getTotalDiscount(row.getID());
//            if(remAmount.getEntityList().get(0) != null)
//            	discount = (Double) remAmount.getEntityList().get(0);
            obj.put( AMOUNT,amount);
            double taxPercent = 0;
            double rowTaxPercent = 0;
            if (row.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), row.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
            }
            obj.put( PRTAXPERCENT,rowTaxPercent);
            obj.put(PRTAXID, row.getTax()== null?"":row.getTax().getID());
            if (gReceipt.getTax() != null) {
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getTax().getID());
                taxPercent = (Double) perresult.getEntityList().get(0);
            }
            obj.put( TAXPERCENT,taxPercent);
            jArr.put(obj);
        } catch(Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getGoodsReceiptRows : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public Map<GoodsReceiptDetail, Object[]> applyDebitNotes(Map request, GoodsReceipt gReceipt) throws ServiceException {
        Map<GoodsReceiptDetail, Object[]> hm = new HashMap<GoodsReceiptDetail, Object[]>();
        String accName="";
        String accID="";
        Set<GoodsReceiptDetail> grRows = gReceipt.getRows();
        
        KwlReturnObject curresult = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), (String) request.get(GCURRENCYID));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        double amount;
        int quantity;
        double disc = (gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscountValue()) / grRows.size();
//        Iterator itr = grRows.iterator();
//        while (itr.hasNext()) {
//            GoodsReceiptDetail temp = (GoodsReceiptDetail) itr.next();
        if (grRows != null && !grRows.isEmpty()){
            for ( GoodsReceiptDetail temp: grRows){
                quantity = temp.getInventory().getQuantity();
                amount = temp.getRate() * quantity;
                double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                double rowTaxPercent = 0;
                if (temp.getTax() != null) {
    //                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent((String) request.get(COMPANYID), gReceipt.getJournalEntry().getEntryDate(), temp.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                }
                double ramount=amount-rdisc;
                ramount+=ramount*rowTaxPercent/100;
                ramount-=disc;
                accName = temp.getInventory().getProduct().getPurchaseAccount().getName();// required for 1099 report[PS]
                accID = temp.getInventory().getProduct().getPurchaseAccount().getID();
                hm.put(temp, new Object[]{ramount, quantity, 0.0,accName,accID });

                if (gReceipt == null) {
                    gReceipt = temp.getGoodsReceipt();
                }
            }
        }
//        String query = "select dn, dnr, dnd from DebitNote dn left join dn.rows dnr left join dn.discounts dnd where dn.deleted=false and (dnr.goodsReceiptRow.goodsReceipt.ID=? or dnd.goodsReceipt.ID=?) order by dn.sequence";
//        Iterator dnitr = HibernateUtil.executeQuery(session, query, new Object[]{gReceipt.getID(), gReceipt.getID()}).iterator();
        KwlReturnObject result = accDebitNoteobj.getDNFromGReceipt(gReceipt.getID());
//         Iterator dnitr = result.getEntityList().iterator();
//         while (dnitr.hasNext()) {
//            Object[] dnrow = (Object[]) dnitr.next();
        List<Object[]> list= result.getEntityList();
        double taxAmount=0;
        if (list != null && !list.isEmpty()){
            for (Object[] dnrow : list){
                DebitNoteDetail dnr = (DebitNoteDetail) dnrow[1];
                GoodsReceiptDetail temp = dnr.getGoodsReceiptRow();
                if (!hm.containsKey(temp)) {
                    continue;
                }
                Object[] val = (Object[]) hm.get(temp);
                String fromcurrencyid = (dnr.getDebitNote().getCurrency() == null ? currency.getCurrencyID() : dnr.getDebitNote().getCurrency().getCurrencyID());
                String tocurrencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
                double baseDiscount = 0;
                if(dnr.getDiscount()!=null){
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, dnr.getDiscount().getDiscountValue(), fromcurrencyid, tocurrencyid, gReceipt.getJournalEntry().getEntryDate(), 0);
                    baseDiscount = (Double) bAmt.getEntityList().get(0);
                }
                double v = (Double) val[0] - baseDiscount;
                if (dnr.getTaxAmount() != null) {
                    taxAmount = dnr.getTaxAmount() + (Double) val[2];
                }
                int q = (Integer) val[1];
                if (temp.getInventory() != null) {
                    q -= dnr.getQuantity();
                }
                hm.put(temp, new Object[]{v,q,taxAmount,accName,accID });
            }
         }
        return hm;
    }
    public List getGRAmountDue(Map<String, Object> request, GoodsReceipt gReceipt) throws ServiceException {
       List ll = new ArrayList();
       String accNames="";
        double amountdue=0;
        boolean belongsTo1099=false;
        double amount = 0, ramount = 0;
        String currencyid = (String) request.get(GCURRENCYID);
        String companyid = (String) request.get(COMPANYID);
        ArrayList acclist=new ArrayList();
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID=baseCurrency.getCurrencyID();
        Iterator itrCn = applyDebitNotes(request, gReceipt).values().iterator();
        currencyid = (gReceipt.getCurrency() == null ?baseCurrencyID : gReceipt.getCurrency().getCurrencyID());
        while (itrCn.hasNext()) {
            Object[] temp = (Object[]) itrCn.next();
            amount += (Double) temp[0] - (Double) temp[2];
            accNames += (String)temp[3];// required for 1099 report[PS]
            accNames +=",";
            acclist.add((String)temp[4]);
        }
        accNames=accNames.substring(0,Math.max(0, accNames.length()-1));
        KwlReturnObject result = accTaxObj.belongsTo1099(companyid,acclist);
        List l = result.getEntityList();
        if(l.size()>0) {
            belongsTo1099=true;
        }
        JournalEntryDetail tempd = gReceipt.getTaxEntry();
        if (tempd != null) {
            amount += tempd.getAmount();
        }
       result = accVendorPaymentobj.getPaymentsFromGReceipt(gReceipt.getID(), companyid);
       List<PaymentDetail> list= result.getEntityList();
        if (list != null && !list.isEmpty()){
            for (PaymentDetail pd : list){
                ramount += pd.getAmount();
                String fromcurrencyid = (pd.getPayment().getCurrency() == null ? baseCurrencyID : pd.getPayment().getCurrency().getCurrencyID());
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, ramount, fromcurrencyid, currencyid, gReceipt.getJournalEntry().getEntryDate(),gReceipt.getJournalEntry().getExternalCurrencyRate());
                ramount = (Double) bAmt.getEntityList().get(0);
            }
        }
        amountdue=amount-ramount;
        ll.add(amount);
        ll.add(amountdue);
        ll.add(accNames);
        ll.add(belongsTo1099);
        return ll;
    }
    public List getExpGRAmountDue(Map<String, Object> request, GoodsReceipt gReceipt) throws ServiceException {
        List ll = new ArrayList();
        double amount = 0, ramount = 0,amountdue=0;
        boolean belongsTo1099=false;
        String currencyid = (String) request.get(GCURRENCYID);
        String accNames="";
        String companyid = (String) request.get(COMPANYID);
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID=baseCurrency.getCurrencyID();
        currencyid = (gReceipt.getCurrency() == null ?baseCurrencyID : gReceipt.getCurrency().getCurrencyID());
        Set<ExpenseGRDetail> grRows = gReceipt.getExpenserows();
        ArrayList acclist=new ArrayList();
//        Iterator itr = grRows.iterator();
//        while (itr.hasNext()) {
//            ExpenseGRDetail temp = (ExpenseGRDetail) itr.next();
        if (grRows != null && !grRows.isEmpty())
        {
            for (ExpenseGRDetail temp: grRows)
            {
                double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                double rowTaxPercent = 0;
                if (temp.getTax() != null) {
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), temp.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                }
                ramount=temp.getRate()-rdisc;
                amount+=ramount+ramount*rowTaxPercent/100;
                accNames += temp.getAccount().getName();// required for 1099 report[PS]
                accNames +=",";
                acclist.add((String)temp.getAccount().getID());
            }
        }
        accNames=accNames.substring(0,Math.max(0, accNames.length()-1));
        KwlReturnObject result = accTaxObj.belongsTo1099(companyid,acclist);
        List l = result.getEntityList();
        if(l.size()>0) {
            belongsTo1099=true;
        }
        
        double disc = (gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscountValue());//main gr discount
        amount-=disc; //discount on invoice[PS]
//        JournalEntryDetail tempd = gReceipt.getShipEntry();
        JournalEntryDetail tempd = gReceipt.getTaxEntry();
        if (tempd != null) {
            amount += tempd.getAmount(); //tax on invoice[PS]
        }
//        if (tempd != null) {// Not used now[PS]
//            amount += tempd.getAmount();
//        }
//        tempd = gReceipt.getOtherEntry();
//        if (tempd != null) {
//            amount += tempd.getAmount();
//        }

       result = accVendorPaymentobj.getPaymentsFromGReceipt(gReceipt.getID(), companyid);
        List<PaymentDetail> list = result.getEntityList();
        ramount=0;
        if (list != null && !list.isEmpty())
        {
            for (PaymentDetail pd: list)
            {
                ramount = pd.getAmount();
                String fromcurrencyid = (pd.getPayment().getCurrency() == null ? baseCurrencyID : pd.getPayment().getCurrency().getCurrencyID());
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, ramount, fromcurrencyid, currencyid, gReceipt.getJournalEntry().getEntryDate(),gReceipt.getJournalEntry().getExternalCurrencyRate());
                ramount = (Double) bAmt.getEntityList().get(0);
            }
        }


//       result = accVendorPaymentobj.getPaymentsFromGReceipt(gReceipt.getID(), companyid);
//        l = result.getEntityList();
//        Iterator recitr = l.iterator();
//        ramount=0;
//        while (recitr.hasNext()) {
//            PaymentDetail pd = (PaymentDetail) recitr.next();
//            ramount = pd.getAmount();
//            String fromcurrencyid = (pd.getPayment().getCurrency() == null ? baseCurrencyID : pd.getPayment().getCurrency().getCurrencyID());
//            KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, ramount, fromcurrencyid, currencyid, gReceipt.getJournalEntry().getEntryDate(),gReceipt.getJournalEntry().getExternalCurrencyRate());
//            ramount = (Double) bAmt.getEntityList().get(0);
//        }
        amountdue=amount-ramount;
        ll.add(amount);
        ll.add(amountdue);
        ll.add(accNames);
        ll.add(belongsTo1099);
        return ll;
    }

    public Map getGoodsReceiptProductAmount(GoodsReceipt gr) throws ServiceException {
        Map<GoodsReceiptDetail, Object[]> hm = new HashMap<GoodsReceiptDetail, Object[]>();
        Set<GoodsReceiptDetail> invRows = gr.getRows();        
        double amount;
        int quantity;
//        Iterator itr = invRows.iterator();
//        while (itr.hasNext()) {
//        GoodsReceiptDetail temp = (GoodsReceiptDetail) itr.next();
         if (invRows != null && !invRows.isEmpty())
        {
            for (GoodsReceiptDetail temp: invRows)
            {            
                quantity = temp.getInventory().getQuantity();
                amount = temp.getRate() * quantity;
                double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                 double rowTaxPercent = 0;
                if (temp.getTax() != null) {
    //                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent( gr.getCompany().getCompanyID(), gr.getJournalEntry().getEntryDate(), temp.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                }
                 double ramount=amount - rdisc;
                 ramount+=ramount*rowTaxPercent/100;
                hm.put(temp, new Object[]{ramount, quantity});
                if (gr == null) {
                    gr = temp.getGoodsReceipt();
                }
            }
         }
        return hm;
    }

    public Map getExpenseGRAmount(GoodsReceipt gr) throws ServiceException {
        Map<ExpenseGRDetail, Object[]> hm = new HashMap<ExpenseGRDetail, Object[]>();
        Set<ExpenseGRDetail> invRows = gr.getExpenserows();
//        Iterator itr = invRows.iterator();
//        while (itr.hasNext()) {
//            ExpenseGRDetail temp = (ExpenseGRDetail) itr.next();
        if (invRows != null && !invRows.isEmpty())
        {
            for (ExpenseGRDetail temp: invRows)
            {
                double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                double rowTaxPercent = 0;
                if (temp.getTax() != null) {

    //                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent( gr.getCompany().getCompanyID(), gr.getJournalEntry().getEntryDate(), temp.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);

                }
                 double ramount=temp.getRate() - rdisc;
                 ramount+=ramount*rowTaxPercent/100;
                hm.put(temp, new Object[]{ramount});
                if (gr == null) {
                    gr = temp.getGoodsReceipt();
                }
            }
        }
        return hm;
    }

    public Map getBillingGoodsReceiptProductAmount(BillingGoodsReceipt gr) throws ServiceException{
        Map<BillingGoodsReceiptDetail, Object[]> hm=new HashMap<BillingGoodsReceiptDetail, Object[]>();
        Set<BillingGoodsReceiptDetail> invRows=gr.getRows();
        double amount;
        double quantity;
//        Iterator itr=invRows.iterator();
//        while(itr.hasNext()){
//            BillingGoodsReceiptDetail temp=(BillingGoodsReceiptDetail)itr.next();
//

        if(invRows!=null && !invRows.isEmpty()){
            for(BillingGoodsReceiptDetail temp:invRows){
                quantity=temp.getQuantity();
                amount=temp.getRate()*quantity;
                double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
                             double rowTaxPercent = 0;
                if (temp.getTax() != null) {
    //                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent( gr.getCompany().getCompanyID(), gr.getJournalEntry().getEntryDate(), temp.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                }
                 double ramount=amount - rdisc;
                 ramount+=ramount*rowTaxPercent/100;
                hm.put(temp, new Object[]{ramount, quantity});

               // hm.put(temp, new Object[]{amount-rdisc,quantity});
                if(gr==null)gr=temp.getBillingGoodsReceipt();
            }
        }
        return hm;
    }

    public Map applyBillingDebitNotes(Map request, BillingGoodsReceipt gReceipt) throws ServiceException, SessionExpiredException {
        Map<BillingGoodsReceiptDetail, Object[]> hm = new HashMap<BillingGoodsReceiptDetail, Object[]>();
        Set<BillingGoodsReceiptDetail> grRows = gReceipt.getRows();
        KwlReturnObject curresult = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), (String) request.get(GCURRENCYID));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        double amount;
        double quantity;
        double disc = (gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscountValue()) / grRows.size();
        if(grRows!=null && !grRows.isEmpty()){
            for(BillingGoodsReceiptDetail temp:grRows){
                quantity = temp.getQuantity();
                amount = temp.getRate() * quantity;
                double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                double rowTaxPercent = 0;
                if (temp.getTax() != null) {
                    KwlReturnObject perresult = accTaxObj.getTaxPercent((String) request.get(COMPANYID), gReceipt.getJournalEntry().getEntryDate(), temp.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                }
                double ramount=amount-rdisc;
                ramount+=ramount*rowTaxPercent/100;
                ramount-=disc;
                hm.put(temp, new Object[]{ramount, quantity, 0.0});
                if (gReceipt == null) gReceipt = temp.getBillingGoodsReceipt();
            }
        }
        KwlReturnObject llObject = accDebitNoteobj.getDNRFromBDN(gReceipt.getID());
        double taxAmount=0;
        List<Object[]> list = llObject.getEntityList();
        if(list!=null && !list.isEmpty()){
            for(Object[] dnrow:list){
                BillingDebitNoteDetail dnr = (BillingDebitNoteDetail) dnrow[1];
                BillingGoodsReceiptDetail temp = dnr.getGoodsReceiptRow();
                if (!hm.containsKey(temp))continue;
                Object[] val = (Object[]) hm.get(temp);
                String fromcurrencyid=(dnr.getDebitNote().getCurrency()==null?currency.getCurrencyID(): dnr.getDebitNote().getCurrency().getCurrencyID());
                String tocurrencyid=(gReceipt.getCurrency()==null?currency.getCurrencyID(): gReceipt.getCurrency().getCurrencyID());
                double v=(Double)val[0]-(dnr.getDiscount()==null?0:(Double)accCurrencyDAOobj.getOneCurrencyToOther(request,dnr.getDiscount().getDiscountValue(),fromcurrencyid,tocurrencyid,gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate()).getEntityList().get(0));
                if(dnr.getTaxAmount()!=null)
                    taxAmount+=dnr.getTaxAmount();
                double q=(Double)val[1];
                q-=dnr.getQuantity();
                hm.put(temp, new Object[]{v,q,taxAmount});
            }
        }
        return hm;
    }

        
    public JSONArray getBillingGoodsReceiptRows(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
		try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(BILLS, request.getParameterValues(BILLS));

            String companyid = (String) requestParams.get(COMPANYID);
            String currencyid = (String) requestParams.get(GCURRENCYID);
            KwlReturnObject curresult = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String[] invoices=request.getParameterValues(BILLS);
            int i=0;

            HashMap<String, Object> grRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("billingGoodsReceipt.ID");
            order_by.add(SRNO);
            order_type.add("asc");
            grRequestParams.put( FILTER_NAMES,filter_names);
            grRequestParams.put( FILTER_PARAMS,filter_params);
            grRequestParams.put( ORDER_BY,order_by);
            grRequestParams.put( ORDER_TYPE,order_type);

            while(invoices!=null&&i<invoices.length){
//                BillingGoodsReceipt invoice=(BillingGoodsReceipt)session.get(BillingGoodsReceipt.class, invoices[i]);
                BillingGoodsReceipt invoice = (BillingGoodsReceipt) kwlCommonTablesDAOObj.getClassObject(BillingGoodsReceipt.class.getName(), invoices[i]);
//                Iterator itr = invoice.getRows().iterator();
                filter_params.clear();
                filter_params.add(invoice.getID());
                KwlReturnObject grdresult = accGoodsReceiptobj.getBillingGoodsReceiptDetails(grRequestParams);
//                Iterator itr = grdresult.getEntityList().iterator();
//                while(itr.hasNext()) {
//                    BillingGoodsReceiptDetail row=(BillingGoodsReceiptDetail)itr.next();

                Map hm= applyBillingDebitNotes(requestParams,invoice);
                List<BillingGoodsReceiptDetail> list=grdresult.getEntityList();
                if (list != null && !list.isEmpty())
                {
                    for (BillingGoodsReceiptDetail row: list)
                    {
                        JSONObject obj = new JSONObject();
                        obj.put(BILLID, invoice.getID());
                        obj.put(BILLNO, invoice.getBillingGoodsReceiptNumber());
                        currencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
                        obj.put(CURRENCYSYMBOL, (invoice.getCurrency()==null?currency.getCurrencyID():invoice.getCurrency().getSymbol()));
                        obj.put(OLDCURRENCYRATE, accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams,1.0,currencyid,invoice.getJournalEntry().getEntryDate(),invoice.getJournalEntry().getExternalCurrencyRate()));
                        obj.put(SRNO, row.getSrno());
                        obj.put(ROWID, row.getID());
                        obj.put(PRODUCTDETAIL,row.getProductDetail());
                        obj.put(QUANTITY,row.getQuantity());
                        Discount disc= row.getDiscount();
                        if(disc!=null&&disc.isInPercent())
                            obj.put(PRDISCOUNT,disc.getDiscount());
                        else
                            obj.put(PRDISCOUNT,0);
                        obj.put(RATE, row.getRate());
                        double remainingquantity=0;
                        double amount=0;
                        if(hm.containsKey(row)){
                            Object[] val=(Object[])hm.get(row);
                            amount=(Double)val[0];
                            remainingquantity=(Double)val[1];
                            obj.put( REMAININGQUANTITY,remainingquantity);
                            obj.put(REMQUANTITY, 0);
                            obj.put( AMOUNT,amount);
                        }
                        Map amthm=getBillingGoodsReceiptProductAmount(invoice);
                        Object[] val=(Object[])amthm.get(row);
                        amount=(Double)val[0];
                        obj.put( ORIGNALAMOUNT,amount);
                        double taxPercent = 0;
                        double rowTaxPercent = 0;
                        if (row.getTax() != null) {
    //                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), row.getTax().getID());
                            rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        }
                        obj.put( PRTAXPERCENT,rowTaxPercent);
                        obj.put(PRTAXID, row.getTax()== null?"":row.getTax().getID());
                        if(invoice.getTax()!=null){
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(),invoice.getTax().getID());
                            taxPercent = (Double) perresult.getEntityList().get(0);
                        }
                        obj.put( TAXPERCENT,taxPercent);
                        jArr.put(obj);
                    }
                }
            i++;
//            jobj.put("data", jArr);
            }
        } catch (JSONException e) {
                throw ServiceException.FAILURE("accGoodsReceiptCMN.getBillingGoodsReceiptRows", e);
        }
        return jArr;
    }

}
