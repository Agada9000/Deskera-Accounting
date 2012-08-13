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

package com.krawler.spring.accounting.receipt;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;

import com.krawler.hql.accounting.Receipt;
import com.krawler.hql.accounting.ReceiptDetail;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
/**
 *
 * @author krawler
 */
public class accReceiptControllerCMN extends MultiActionController {
    private accReceiptDAO accReceiptDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private accInvoiceCMN accInvoiceCommon;
    private String successView;

    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
     public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }   
    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public ModelAndView getReceiptRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String msg = "";
		try {
            jobj = getReceiptRowsJSON(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try{
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public HashMap<String, Object> getReceiptRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("ss", request.getParameter("ss"));
        requestParams.put("start", request.getParameter("start"));
        requestParams.put("limit", request.getParameter("limit"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        return requestParams;
    }

   private JSONObject getReceiptRowsJSON(HttpServletRequest request) throws SessionExpiredException, ServiceException {
    JSONObject jobj=new JSONObject();
    try {
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        double taxPercent=0;
        DateFormat df = authHandler.getDateFormatter(request);
        String[] receipt = request.getParameterValues("bills");
        boolean isReceiptEdit = Boolean.parseBoolean(request.getParameter("isReceiptEdit"));
        boolean isAmountDue = Boolean.parseBoolean(request.getParameter("isAmountDue"));
        int i = 0;
        HashMap requestParams = getReceiptRequestMap(request);
        HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("receipt.ID");
        order_by.add("srno");
        order_type.add("asc");
        rRequestParams.put("filter_names", filter_names);
        rRequestParams.put("filter_params", filter_params);
        rRequestParams.put("order_by", order_by);
        rRequestParams.put("order_type", order_type);

        JSONArray jArr = new JSONArray();
        while (receipt != null && i < receipt.length) {
//                Receipt re=(Receipt)session.get(Receipt.class, receipt[i]);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receipt[i]);
            Receipt re = (Receipt) result.getEntityList().get(0);
//                Iterator itr = re.getRows().iterator();
            filter_params.clear();
            filter_params.add(re.getID());
            KwlReturnObject grdresult = accReceiptDAOobj.getReceiptDetails(rRequestParams);
            Iterator itr = grdresult.getEntityList().iterator();

            while (itr.hasNext()) {
                    ReceiptDetail row = (ReceiptDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", isReceiptEdit?row.getInvoice().getID():re.getID());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("currencysymbol", row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol());
                    obj.put("transectionno", row.getInvoice().getInvoiceNumber());
                    obj.put("transectionid", row.getInvoice().getID());
                    obj.put("amount",  (isReceiptEdit?row.getInvoice().getCustomerEntry().getAmount():row.getAmount()));
                    obj.put("duedate", df.format(row.getInvoice().getDueDate()));
                    obj.put("creationdate", df.format(row.getInvoice().getJournalEntry().getEntryDate()));
                    if (row.getInvoice().getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getInvoice().getJournalEntry().getEntryDate(), row.getInvoice().getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);
                    obj.put("discount", row.getInvoice().getDiscount() == null ? 0 : row.getInvoice().getDiscount().getDiscountValue());
                    obj.put("payment", row.getInvoice().getID());
                    double amountdue= accInvoiceCommon.getAmountDue(requestParams,row.getInvoice());
                    obj.put("amountduenonnegative", (isReceiptEdit?amountdue+row.getAmount():amountdue));
                    obj.put("totalamount", row.getInvoice().getCustomerEntry().getAmount());
            //        obj.put("receiptamount", (row.getAmount()));


                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceiptRowsJSON : "+ex.getMessage(), ex);
        }
        return jobj;
    }

}
