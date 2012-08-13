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
package com.krawler.spring.accounting.purchaseorder;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.BillingGoodsReceiptDetail;
import com.krawler.hql.accounting.BillingPurchaseOrder;
import com.krawler.hql.accounting.BillingPurchaseOrderDetail;
import com.krawler.hql.accounting.GoodsReceiptDetail;
import com.krawler.hql.accounting.PurchaseOrder;
import com.krawler.hql.accounting.PurchaseOrderDetail;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
public class accPurchaseOrderControllerCMN extends MultiActionController{
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private accTaxDAO accTaxObj;
    private String successView;

    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setaccTaxDAO (accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public ModelAndView getPurchaseOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            KwlReturnObject result = accPurchaseOrderobj.getPurchaseOrders(requestParams);
            jobj = getPurchaseOrdersJson(requestParams, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getPurchaseOrders : "+ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public HashMap<String, Object> getPurchaseOrderMap (HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.start, request.getParameter(Constants.start));
        requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(CCConstants.REQ_costCenterId,request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
        boolean closeflag = request.getParameter("closeflag")!=null?true:false;
        requestParams.put("closeflag", closeflag);
        return requestParams;
    }

    public JSONObject getPurchaseOrdersJson(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            boolean closeflag = (Boolean) requestParams.get("closeflag");
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                String currencyid = (String) requestParams.get("gcurrencyid");
                KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
                KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

                DateFormat df = (DateFormat) requestParams.get("df");
                PurchaseOrder purchaseOrder = (PurchaseOrder) itr.next();
                Vendor vendor = purchaseOrder.getVendor();
                KWLCurrency currency=purchaseOrder.getVendor().getAccount().getCurrency()==null?kwlcurrency:purchaseOrder.getVendor().getAccount().getCurrency();
                JSONObject obj = new JSONObject();
                obj.put("billid", purchaseOrder.getID());
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("personid", vendor.getID());
                obj.put("billno", purchaseOrder.getPurchaseOrderNumber());
                obj.put("duedate", df.format(purchaseOrder.getDueDate()));
                obj.put("date", df.format(purchaseOrder.getOrderDate()));
                Iterator itrRow = purchaseOrder.getRows().iterator();
                double amount = 0;
                while (itrRow.hasNext()) {
                    PurchaseOrderDetail pod = (PurchaseOrderDetail) itrRow.next();
                    amount += pod.getQuantity() * pod.getRate();
                    double  rowTaxPercent=0;
                    if(pod.getTax()!=null){
                        requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                        requestParams.put("taxid", pod.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }
                    amount+=pod.getQuantity() *pod.getRate()*rowTaxPercent/100;
                }
                obj.put("currencysymbol", currency.getSymbol());
//                obj.put("orderamount", CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currency.getCurrencyID(),purchaseOrder.getOrderDate()));
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), purchaseOrder.getOrderDate(), 0);
                double  taxPercent=0;
                if(purchaseOrder.getTax()!=null){
                    requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                    requestParams.put("taxid", purchaseOrder.getTax().getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    taxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                }
                double orderAmount=(Double) bAmt.getEntityList().get(0);
                double ordertaxamount=(taxPercent==0?0:orderAmount*taxPercent/100);
                obj.put("taxpercent", taxPercent);
                obj.put("taxamount",ordertaxamount );
                obj.put("orderamount",orderAmount );
                obj.put("orderamountwithTax",orderAmount+ordertaxamount);
                obj.put("amount", amount);
                obj.put("personname", vendor.getName());
                obj.put("memo", purchaseOrder.getMemo());
                obj.put("taxid", purchaseOrder.getTax()==null?"":purchaseOrder.getTax().getID());
                obj.put("taxname", purchaseOrder.getTax()==null?"":purchaseOrder.getTax().getName());
                obj.put("costcenterid", purchaseOrder.getCostcenter()==null?"":purchaseOrder.getCostcenter().getID());
                obj.put("costcenterName", purchaseOrder.getCostcenter()==null?"":purchaseOrder.getCostcenter().getName());
                String status = getPurchaseOrderStatus(purchaseOrder);
                obj.put("status",status);
                if (!closeflag || (closeflag && status.equalsIgnoreCase("open"))) {
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
        } catch (Exception ex){
            throw ServiceException.FAILURE("getPurchaseOrdersJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }

    public String getPurchaseOrderStatus(PurchaseOrder po) throws ServiceException {
        Set<PurchaseOrderDetail> orderDetail = po.getRows();
        Iterator ite = orderDetail.iterator();
        String result = "Closed";
        while(ite.hasNext()){
            PurchaseOrderDetail pDetail = (PurchaseOrderDetail)ite.next();
//            String query = "from GoodsReceiptDetail ge where ge.purchaseorderdetail.ID = ?";
//            List list =  HibernateUtil.executeQuery(session, query,pDetail.getID());
            KwlReturnObject grresult = accGoodsReceiptobj.getReceiptDFromPOD(pDetail.getID());
            List list = grresult.getEntityList();
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

    public ModelAndView getPurchaseOrderRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("bills", request.getParameterValues("bills"));
            requestParams.put("closeflag", request.getParameter("closeflag"));

            JSONArray DataJArr = getPurchaseOrderRows(requestParams);
            jobj.put("data", DataJArr);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getPurchaseOrderRows : "+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getPurchaseOrderRows(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            String[] pos = (String[]) requestParams.get("bills");
            int i = 0;
            int addobj = 1;
            String closeflag = (String) requestParams.get("closeflag");

            HashMap<String, Object> poRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("purchaseOrder.ID");
            order_by.add("srno");
            order_type.add("asc");
            poRequestParams.put("filter_names", filter_names);
            poRequestParams.put("filter_params", filter_params);
            poRequestParams.put("order_by", order_by);
            poRequestParams.put("order_type", order_type);

            while (pos != null && i < pos.length) {
//                PurchaseOrder po = (PurchaseOrder) session.get(PurchaseOrder.class, pos[i]);
                KwlReturnObject poresult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), pos[i]);
                PurchaseOrder po = (PurchaseOrder) poresult.getEntityList().get(0);
                KWLCurrency currency=po.getVendor().getAccount().getCurrency()==null?kwlcurrency:po.getVendor().getAccount().getCurrency();
//                Iterator itr = po.getRows().iterator();
                filter_params.clear();
                filter_params.add(po.getID());
                KwlReturnObject podresult = accPurchaseOrderobj.getPurchaseOrderDetails(poRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                while (itr.hasNext()) {
                    PurchaseOrderDetail row = (PurchaseOrderDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", po.getID());
                    obj.put("billno", po.getPurchaseOrderNumber());
                    obj.put("currencysymbol",currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("productname", row.getProduct().getName());
                    obj.put("type",row.getProduct().getProducttype()==null?"":row.getProduct().getProducttype().getName());
                    obj.put("pid",row.getProduct().getProductid());
                    obj.put("desc", row.getProduct().getDescription());
                    obj.put("unitname", row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getName());
                    obj.put("memo", row.getRemark());
                    obj.put("rate", row.getRate());
                    double rowTaxPercent = 0;
                    if (row.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), po.getOrderDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("prtaxid", row.getTax()==null?"":row.getTax().getID());
//                    obj.put("orderrate", CompanyHandler.getBaseToCurrencyAmount(session,request,row.getRate(),currency.getCurrencyID(),po.getOrderDate()));
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), po.getOrderDate(), 0);
                    obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
//                    obj.put("quantity", row.getQuantity());
                    if(closeflag!=null){
                       addobj = getPurchaseOrderDetailStatus(row);
                       obj.put("quantity", addobj);
                    }else{
                       obj.put("quantity", row.getQuantity());
                    }

                    if (addobj > 0) {
                        jArr.put(obj);
                    }
                }
                i++;
            }
        } catch (Exception je) {
            throw ServiceException.FAILURE("getPurchaseOrderRows : "+je.getMessage(), je);
        }
        return jArr;
    }

    public int getPurchaseOrderDetailStatus(PurchaseOrderDetail pod) throws ServiceException {
        int result = pod.getQuantity();
//        String query = "from GoodsReceiptDetail ge where ge.purchaseorderdetail.ID = ?";
//        List list =  HibernateUtil.executeQuery(session, query,pod.getID());
        KwlReturnObject grresult = accGoodsReceiptobj.getReceiptDFromPOD(pod.getID());
        List list = grresult.getEntityList();
        Iterator ite1 = list.iterator();
        int qua = 0;
        while (ite1.hasNext()) {
            GoodsReceiptDetail ge = (GoodsReceiptDetail) ite1.next();
            qua += ge.getInventory().getQuantity();
        }
        result = pod.getQuantity() - qua;
        return result;
    }

//Billing purchase Order
    public ModelAndView getBillingPurchaseOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            KwlReturnObject result = accPurchaseOrderobj.getBillingPurchaseOrders(requestParams);
            jobj = getBillingPurchaseOrdersJson(request, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = ""+ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getBillingPurchaseOrdersJson(HttpServletRequest request, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            KwlReturnObject result = accPurchaseOrderobj.getBillingPurchaseOrders(requestParams);
            Iterator itr = result.getEntityList().iterator();
            JSONArray jArr = new JSONArray();
            boolean closeflag = request.getParameter("closeflag") != null ? true : false;
            while (itr.hasNext()) {
                BillingPurchaseOrder purchaseOrder = (BillingPurchaseOrder) itr.next();
                Vendor vendor = purchaseOrder.getVendor();
                KWLCurrency currency = purchaseOrder.getVendor().getAccount().getCurrency() == null ? kwlcurrency : purchaseOrder.getVendor().getAccount().getCurrency();
                JSONObject obj = new JSONObject();
                obj.put("billid", purchaseOrder.getID());
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("personid", vendor.getID());
                obj.put("billno", purchaseOrder.getPurchaseOrderNumber());
                obj.put("duedate", authHandler.getDateFormatter(request).format(purchaseOrder.getDueDate()));
                obj.put("date", authHandler.getDateFormatter(request).format(purchaseOrder.getOrderDate()));
                Iterator itrRow = purchaseOrder.getRows().iterator();
                double amount = 0;
                while (itrRow.hasNext()) {
                    BillingPurchaseOrderDetail pod = (BillingPurchaseOrderDetail) itrRow.next();
                    amount += pod.getQuantity() * pod.getRate();
                    double  rowTaxPercent=0;
                    if(pod.getTax()!=null){
                        requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                        requestParams.put("taxid", pod.getTax().getID());
                        result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }
                    amount+=pod.getQuantity() *pod.getRate()*rowTaxPercent/100;
                }
                double  taxPercent=0;
                if(purchaseOrder.getTax()!=null){
                    requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                    requestParams.put("taxid", purchaseOrder.getTax().getID());
                    result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    taxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                }
                double ordertaxamount=(taxPercent==0?0:amount*taxPercent/100);
//              KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), purchaseOrder.getOrderDate(), 0);
//                double orderAmount=(Double) bAmt.getEntityList().get(0);
                obj.put("orderamountwithTax",amount+ordertaxamount);
                obj.put("taxpercent", taxPercent);
                obj.put("taxamount",ordertaxamount );
                obj.put("currencysymbol", currency.getSymbol());
                obj.put("orderamount",amount );
                obj.put("amount", amount);
                obj.put("personname", vendor.getName());
                obj.put("creditoraccount", purchaseOrder.getDebitFrom() == null ?"" : purchaseOrder.getDebitFrom().getID());
                obj.put("crdraccid", purchaseOrder.getDebitFrom() == null ?"" : purchaseOrder.getDebitFrom().getID());
                obj.put("memo", purchaseOrder.getMemo());
                obj.put("taxid", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getID());
                obj.put("taxname", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getName());
                obj.put("costcenterid", purchaseOrder.getCostcenter()==null?"":purchaseOrder.getCostcenter().getID());
                obj.put("costcenterName", purchaseOrder.getCostcenter()==null?"":purchaseOrder.getCostcenter().getName());
                String status = getBillingPurchaseOrderStatus(purchaseOrder);
                obj.put("status", status);
                if (!closeflag || (closeflag && status.equalsIgnoreCase("open"))) {
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException see) {
            throw ServiceException.FAILURE("getBillingPurchaseOrdersJson : "+see.getMessage(), see);
        } catch (NumberFormatException nfe) {
            throw ServiceException.FAILURE("getBillingPurchaseOrdersJson : "+nfe.getMessage(), nfe);
        } catch (JSONException jse) {
            throw ServiceException.FAILURE("getBillingPurchaseOrdersJson : "+jse.getMessage(), jse);
        }
        return jobj;
    }

    public String getBillingPurchaseOrderStatus(BillingPurchaseOrder po) throws ServiceException {
        Set<BillingPurchaseOrderDetail> orderDetail = po.getRows();
        Iterator ite = orderDetail.iterator();
        String result = "Closed";
        while(ite.hasNext()){
            BillingPurchaseOrderDetail pDetail = (BillingPurchaseOrderDetail)ite.next();
//            String query = "from BillingGoodsReceiptDetail ge where ge.purchaseOrderDetail.ID = ?";
//            List list =  HibernateUtil.executeQuery(session, query,pDetail.getID());
            KwlReturnObject bgrresult  = accGoodsReceiptobj.getBRDFromBPOD(pDetail.getID());
            Iterator ite1 = bgrresult.getEntityList().iterator();
            int qua = 0;
            while(ite1.hasNext()){
                BillingGoodsReceiptDetail ge = (BillingGoodsReceiptDetail)ite1.next();
                qua += ge.getQuantity();
            }
            if(qua < pDetail.getQuantity()){
                result = "Open";
                break;
            }
        }
        return result;
    }

    public ModelAndView getBillingPurchaseOrderRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getBillingPurchaseOrderRows(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getBillingPurchaseOrderRows(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
//            KWLCurrency kwlcurrency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            HashMap<String,Object> requestParams = AccountingManager.getGlobalParams(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            String[] pos = request.getParameterValues("bills");
            int i = 0;
            JSONArray jArr = new JSONArray();
            double addobj = 1;
            String closeflag = request.getParameter("closeflag");

            HashMap<String, Object> poRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("purchaseOrder.ID");
            order_by.add("srno");
            order_type.add("asc");
            poRequestParams.put("filter_names", filter_names);
            poRequestParams.put("filter_params", filter_params);
            poRequestParams.put("order_by", order_by);
            poRequestParams.put("order_type", order_type);

            while (pos != null && i < pos.length) {
//                BillingPurchaseOrder po = (BillingPurchaseOrder) session.get(BillingPurchaseOrder.class, pos[i]);
                KwlReturnObject poresult = accountingHandlerDAOobj.getObject(BillingPurchaseOrder.class.getName(), pos[i]);
                BillingPurchaseOrder po = (BillingPurchaseOrder) poresult.getEntityList().get(0);

                KWLCurrency currency = po.getVendor().getAccount().getCurrency() == null ? kwlcurrency : po.getVendor().getAccount().getCurrency();

//                Iterator itr = po.getRows().iterator();
                filter_params.clear();
                filter_params.add(po.getID());
                KwlReturnObject podresult = accPurchaseOrderobj.getBillingPurchaseOrderDetails(poRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                while (itr.hasNext()) {
                    BillingPurchaseOrderDetail row = (BillingPurchaseOrderDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", po.getID());
                    obj.put("billno", po.getPurchaseOrderNumber());
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productdetail", row.getProductDetail());
                  //  obj.put("unitname", row.g);
                    obj.put("memo", row.getRemark());
                    obj.put("rate", row.getRate());
                    double rowTaxPercent = 0;
                    if (row.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), po.getOrderDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("prtaxid", row.getTax()==null?"":row.getTax().getID());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), po.getOrderDate(), 0);
                    obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
                    if (closeflag != null) {
                        addobj = getBillingPurchaseOrderDetailStatus(row);
                        obj.put("quantity", addobj);
                    } else {
                        obj.put("quantity", row.getQuantity());
                    }

                    if (addobj > 0) {
                        jArr.put(obj);
                    }
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE("getBillingPurchaseOrderRows : "+je.getMessage(), je);
        }
        return jobj;
    }

    public double getBillingPurchaseOrderDetailStatus(BillingPurchaseOrderDetail pod) throws ServiceException{
        double result = pod.getQuantity();
//        String query = "from BillingGoodsReceiptDetail ge where ge.purchaseOrderDetail.ID = ?";
//        List list =  HibernateUtil.executeQuery(session, query,pod.getID());
        KwlReturnObject bgrresult  = accGoodsReceiptobj.getBRDFromBPOD(pod.getID());
        Iterator ite1 = bgrresult.getEntityList().iterator();
        int qua = 0;
        while(ite1.hasNext()){
            BillingGoodsReceiptDetail ge = (BillingGoodsReceiptDetail)ite1.next();
            qua += ge.getQuantity();
        }
        result = pod.getQuantity()-qua;
        return result;
    }

    public ModelAndView exportPurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            KwlReturnObject result = accPurchaseOrderobj.getPurchaseOrders(requestParams);
            jobj = getPurchaseOrdersJson(requestParams, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView exportBillingPurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            KwlReturnObject result = accPurchaseOrderobj.getBillingPurchaseOrders(requestParams);
            jobj = getBillingPurchaseOrdersJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
}
