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

package com.krawler.spring.accounting.vendorpayment;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;

import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.PaymentDetail;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentController;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
/**
 *
 * @author krawler
 */
public class accVendorPaymentControllerCMN extends MultiActionController implements MessageSourceAware{
    private accVendorPaymentDAO accVendorPaymentDAO;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private accGoodsReceiptCMN accGoodsReceiptCMN;
    private String successView;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setAccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentDAO) {
        this.accVendorPaymentDAO = accVendorPaymentDAO;
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
    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCMN) {
        this.accGoodsReceiptCMN = accGoodsReceiptCMN;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public ModelAndView getPaymentRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        try{
//            HashMap<String, Object> requestParams = new HashMap<String, Object>();
//            requestParams.put("gcurrencyid", AuthHandler.getCurrencyID(request));
//            requestParams.put("dateformat", AuthHandler.getDateFormatter(request));
//            requestParams.put("bills", request.getParameterValues("bills"));
//
//            JSONArray DataJArr = getPaymentRowsJson(requestParams);
//            jobj.put("data", DataJArr.length()>0?DataJArr:"");
            jobj = getPaymentRows(request, true);
            issuccess = true;
            msg = messageSource.getMessage("acc.main.rec", null, RequestContextUtils.getLocale(request));   //"Records fetched successfully";
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }


     public JSONObject getPaymentRows(HttpServletRequest request, boolean flag) throws SessionExpiredException, ServiceException{
        JSONObject jobj = new JSONObject();
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("dateformat", authHandler.getDateFormatter(request));
            requestParams.put("bills", request.getParameterValues("bills"));
            requestParams.put("isReceiptEdit", request.getParameter("isReceiptEdit"));
            JSONArray DataJArr = new JSONArray();
//            if(flag){
                DataJArr = getPaymentRowsJson(requestParams);
//            }else{
//                DataJArr = getBillingPaymentRowsJson(requestParams);
//            }
            jobj.put("data", DataJArr.length()>0?DataJArr:"");
        }
        catch (JSONException ex) {
            throw ServiceException.FAILURE("getPaymentRows : " + ex.getMessage(), ex);
//            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
   public JSONArray getPaymentRowsJson(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray JArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            double taxPercent=0;
            DateFormat df = (DateFormat) requestParams.get("dateformat");
            String[] receipt = (String[]) requestParams.get("bills");
            boolean isVendorPaymentEdit = Boolean.parseBoolean((String)requestParams.get("isReceiptEdit"));
           int i = 0;
            HashMap<String, Object> pRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("payment.ID");
            order_by.add("srno");
            order_type.add("asc");
            pRequestParams.put("filter_names", filter_names);
            pRequestParams.put("filter_params", filter_params);
            pRequestParams.put("order_by", order_by);
            pRequestParams.put("order_type", order_type);

            JSONArray jArr = new JSONArray();
            while (receipt != null && i < receipt.length) {
//                Payment re = (Payment) session.get(Payment.class, receipt[i]);
                KwlReturnObject presult = accountingHandlerDAOobj.getObject(Payment.class.getName(), receipt[i]);
                Payment re = (Payment) presult.getEntityList().get(0);
//                Iterator itr = re.getRows().iterator();
                filter_params.clear();
                filter_params.add(re.getID());
                KwlReturnObject grdresult = accVendorPaymentDAO.getPaymentDetails(pRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();

                while (itr.hasNext()) {
                    PaymentDetail row = (PaymentDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", isVendorPaymentEdit?row.getGoodsReceipt().getID():re.getID());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("transectionno", row.getGoodsReceipt().getGoodsReceiptNumber());
                    obj.put("transectionid", row.getGoodsReceipt().getID());
                    obj.put("amount",  (isVendorPaymentEdit?row.getGoodsReceipt().getVendorEntry().getAmount():row.getAmount()));
                    obj.put("currencysymbol", (row.getPayment().getCurrency()==null?currency.getSymbol():row.getPayment().getCurrency().getSymbol()));
                    obj.put("duedate", df.format(row.getGoodsReceipt().getDueDate()));
                    obj.put("creationdate", df.format(row.getGoodsReceipt().getJournalEntry().getEntryDate()));
                    JArr.put(obj);
                    if (row.getGoodsReceipt().getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getGoodsReceipt().getJournalEntry().getEntryDate(), row.getGoodsReceipt().getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);
                    obj.put("discount", row.getGoodsReceipt().getDiscount() == null ? 0 : row.getGoodsReceipt().getDiscount().getDiscountValue());
                    obj.put("payment", row.getGoodsReceipt().getID());
                    List ll;
                    if(row.getGoodsReceipt().isIsExpenseType())
                        ll= accGoodsReceiptCMN.getExpGRAmountDue(requestParams,row.getGoodsReceipt());
                    else
                        ll= accGoodsReceiptCMN.getGRAmountDue(requestParams,row.getGoodsReceipt());
                    double amountdue=(ll.isEmpty()?0:(Double)ll.get(1));
                    obj.put("amountduenonnegative", (isVendorPaymentEdit?amountdue+row.getAmount():amountdue));
                    obj.put("totalamount", row.getGoodsReceipt().getVendorEntry().getAmount());


                    jArr.put(obj);
                }
                i++;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getPaymentRowsJson : "+ex.getMessage(), ex);
        }
        return JArr;
    }

}
