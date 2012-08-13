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
package com.krawler.spring.accounting.salesorder;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.BillingInvoiceDetail;
import com.krawler.hql.accounting.BillingSalesOrder;
import com.krawler.hql.accounting.BillingSalesOrderDetail;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.Quotation;
import com.krawler.hql.accounting.QuotationDetail;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.hql.accounting.Tax;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.lang.reflect.Array;
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

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class accSalesOrderControllerCMN extends MultiActionController implements MessageSourceAware{
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accCurrencyDAO accCurrencyobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private exportMPXDAOImpl exportDaoObj;
     private accTaxDAO accTaxObj;
    private String successView;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
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

    public ModelAndView getSalesOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            KwlReturnObject result = accSalesOrderDAOobj.getSalesOrders(requestParams);
            JSONArray jarr = getSalesOrdersJson(request, result.getEntityList());
            jobj.put("data", jarr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public HashMap<String, Object> getSalesOrdersMap (HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(Constants.start, request.getParameter(Constants.start));
        requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        requestParams.put(CCConstants.REQ_costCenterId,request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
        return requestParams;
    }

    public JSONArray getSalesOrdersJson(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            boolean closeflag = request.getParameter("closeflag")!=null?true:false;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                SalesOrder salesOrder=(SalesOrder)itr.next();
                KWLCurrency currency=salesOrder.getCustomer().getAccount().getCurrency()==null?kwlcurrency:salesOrder.getCustomer().getAccount().getCurrency();
                Customer customer=salesOrder.getCustomer();
                JSONObject obj = new JSONObject();
                obj.put("billid", salesOrder.getID());
                obj.put("personid", customer.getID());
                obj.put("billno", salesOrder.getSalesOrderNumber());
                obj.put("duedate", authHandler.getDateFormatter(request).format(salesOrder.getDueDate()));
                obj.put("date", authHandler.getDateFormatter(request).format(salesOrder.getOrderDate()));
                Iterator itrRow = salesOrder.getRows().iterator();
                double amount = 0;
                while (itrRow.hasNext()) {
                    SalesOrderDetail sod= (SalesOrderDetail) itrRow.next();
                    amount+=sod.getQuantity()*sod.getRate();
                    double  rowTaxPercent=0;
                    if(sod.getTax()!=null){
                        requestParams.put("transactiondate", salesOrder.getOrderDate());
                        requestParams.put("taxid", sod.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }
                    amount+=sod.getQuantity() *sod.getRate()*rowTaxPercent/100;
                }
                obj.put("amount", amount);
                obj.put("amountinbase", amount);
//                    obj.put("orderamount", CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currency.getCurrencyID(),salesOrder.getOrderDate()));
                KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), salesOrder.getOrderDate(), 0);
                obj.put("currencysymbol", currency.getSymbol());
                obj.put("taxid", salesOrder.getTax()==null?"":salesOrder.getTax().getID());
                obj.put("taxname", salesOrder.getTax()==null?"":salesOrder.getTax().getName());
                double  taxPercent=0;
                if(salesOrder.getTax()!=null){
                    requestParams.put("transactiondate", salesOrder.getOrderDate());
                    requestParams.put("taxid", salesOrder.getTax().getID());
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
                obj.put("currencyid",currency.getCurrencyID());
                obj.put("personname", customer.getName());
                obj.put("memo", salesOrder.getMemo());
                obj.put("costcenterid", salesOrder.getCostcenter()==null?"":salesOrder.getCostcenter().getID());
                obj.put("costcenterName", salesOrder.getCostcenter()==null?"":salesOrder.getCostcenter().getName());
                String status = getSalesOrderStatus(salesOrder);
                obj.put("status",status);
                if (!closeflag || (closeflag && status.equalsIgnoreCase("open"))) {
                    jArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getSalesOrdersJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public String getSalesOrderStatus(SalesOrder so) throws ServiceException {
        Set<SalesOrderDetail> orderDetail = so.getRows();
        Iterator ite = orderDetail.iterator();
        String result = "Closed";
        while(ite.hasNext()){
            SalesOrderDetail soDetail = (SalesOrderDetail)ite.next();
//            String query = "from InvoiceDetail ge where ge.salesorderdetail.ID = ?";
//            List list =  HibernateUtil.executeQuery(session, query,pDetail.getID());
            KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(soDetail.getID());
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            int qua = 0;
            while(ite1.hasNext()){
                InvoiceDetail ge = (InvoiceDetail) ite1.next();
                qua += ge.getInventory().getQuantity();
            }
            if(qua < soDetail.getQuantity()){
                result = "Open";
                break;
            }
        }
        return result;
    }

    public ModelAndView getSalesOrderRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getSalesOrderRows(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesOrderController.getSalesOrderRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesOrderController.getSalesOrderRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getSalesOrderRows(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj=new JSONObject();
        try {
            HashMap<String,Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            String[] sos=request.getParameterValues("bills");
            int i=0;
            JSONArray jArr=new JSONArray();
            int addobj = 1;

            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("salesOrder.ID");
            order_by.add("srno");
            order_type.add("asc");
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);

            String closeflag = request.getParameter("closeflag");
            while(sos!=null&&i<sos.length){
//                SalesOrder so=(SalesOrder)session.get(SalesOrder.class, sos[i]);
                KwlReturnObject result = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), sos[i]);
                SalesOrder so = (SalesOrder) result.getEntityList().get(0);
                KWLCurrency currency=so.getCustomer().getAccount().getCurrency()==null?kwlcurrency:so.getCustomer().getAccount().getCurrency();
//                Iterator itr=so.getRows().iterator();
                filter_params.clear();
                filter_params.add(so.getID());
                KwlReturnObject podresult = accSalesOrderDAOobj.getSalesOrderDetails(soRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                
                while(itr.hasNext()) {
                    SalesOrderDetail row=(SalesOrderDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", so.getID());
                    obj.put("billno", so.getSalesOrderNumber());
                    obj.put("currencysymbol",currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("productname",row.getProduct().getName());
                    obj.put("unitname", row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getName());
                    obj.put("desc", row.getProduct().getDescription());
                    obj.put("type",row.getProduct().getProducttype()==null?"":row.getProduct().getProducttype().getName());
                    obj.put("pid",row.getProduct().getProductid());
                    obj.put("memo", row.getRemark());
                    double rowTaxPercent = 0;
                    if (row.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), so.getOrderDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("prtaxid",row.getTax()==null?"": row.getTax().getID());
                    obj.put("rate", row.getRate());
//                        obj.put("orderrate", CompanyHandler.getBaseToCurrencyAmount(session,request,row.getRate(),currency.getCurrencyID(),so.getOrderDate()));
                    KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getOrderDate(), 0);
                    obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
//                        obj.put("quantity", row.getQuantity());
                    if (closeflag != null) {
                        addobj = getSalesOrderDetailStatus(row);
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
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }

    public int getSalesOrderDetailStatus(SalesOrderDetail sod) throws ServiceException {
        int result = sod.getQuantity();
//        String query = "from InvoiceDetail ge where ge.salesorderdetail.ID = ?";
//        List list =  HibernateUtil.executeQuery(session, query,pod.getID());
        KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(sod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        int qua = 0;
        while (ite1.hasNext()) {
            InvoiceDetail ge = (InvoiceDetail) ite1.next();
            qua += ge.getInventory().getQuantity();
        }
        result = sod.getQuantity() - qua;
        return result;
    }

    public ModelAndView getBillingSalesOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            KwlReturnObject result = accSalesOrderDAOobj.getBillingSalesOrders(requestParams);
            JSONArray jarr = getBillingSalesOrdersJson(request, result.getEntityList());
            jobj.put("data", jarr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getBillingSalesOrdersJson(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            boolean closeflag = request.getParameter("closeflag") != null ? true : false;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                BillingSalesOrder salesOrder = (BillingSalesOrder) itr.next();
                Customer customer = salesOrder.getCustomer();
                KWLCurrency currency = salesOrder.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : salesOrder.getCustomer().getAccount().getCurrency();
                JSONObject obj = new JSONObject();
                obj.put("billid", salesOrder.getID());
                obj.put("personid", customer.getID());
                obj.put("billno", salesOrder.getSalesOrderNumber());
                obj.put("duedate", authHandler.getDateFormatter(request).format(salesOrder.getDueDate()));
                obj.put("date", authHandler.getDateFormatter(request).format(salesOrder.getOrderDate()));
                Iterator itrRow = salesOrder.getRows().iterator();
                double amount = 0;
                while (itrRow.hasNext()) {
                    BillingSalesOrderDetail sod = (BillingSalesOrderDetail) itrRow.next();
                    amount += sod.getQuantity() * sod.getRate();
                    double  rowTaxPercent=0;
                    if(sod.getTax()!=null){
                        requestParams.put("transactiondate", salesOrder.getOrderDate());
                        requestParams.put("taxid", sod.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }
                    amount+=sod.getQuantity() *sod.getRate()*rowTaxPercent/100;
                }
                double  taxPercent=0;
                if(salesOrder.getTax()!=null){
                    requestParams.put("transactiondate", salesOrder.getOrderDate());
                    requestParams.put("taxid", salesOrder.getTax().getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    taxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                }
                double ordertaxamount=(taxPercent==0?0:amount*taxPercent/100);

//                KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), salesOrder.getOrderDate(), 0);
//                double orderAmount=(Double) bAmt.getEntityList().get(0);
                obj.put("orderamountwithTax",amount+ordertaxamount);
                obj.put("taxpercent", taxPercent);
                obj.put("taxamount",ordertaxamount );
                obj.put("amount", amount);
                obj.put("orderamount",amount );
                obj.put("currencysymbol", currency.getSymbol());
                obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("personname", customer.getName());
                obj.put("creditoraccount", salesOrder.getCreditTo() == null ?"": salesOrder.getCreditTo().getID());
                obj.put("crdraccid", salesOrder.getCreditTo() == null ?"": salesOrder.getCreditTo().getID());
                obj.put("memo", salesOrder.getMemo());
                obj.put("costcenterid", salesOrder.getCostcenter()==null?"":salesOrder.getCostcenter().getID());
                obj.put("costcenterName", salesOrder.getCostcenter()==null?"":salesOrder.getCostcenter().getName());
                String status = getBillingSalesOrderStatus(salesOrder);
                obj.put("status", status);
                if (!closeflag || (closeflag && status.equalsIgnoreCase("open"))) {
                    jArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getSalesOrdersJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public String getBillingSalesOrderStatus(BillingSalesOrder so) throws ServiceException {
        Set<BillingSalesOrderDetail> orderDetail = so.getRows();
        Iterator ite = orderDetail.iterator();
        String result = "Closed";
        while(ite.hasNext()){
            BillingSalesOrderDetail sDetail = (BillingSalesOrderDetail)ite.next();
            KwlReturnObject bidResult = accInvoiceDAOobj.getBIDFromBSOD(sDetail.getID());
            Iterator ite1 = bidResult.getEntityList().iterator();
            int qua = 0;
            while(ite1.hasNext()){
                BillingInvoiceDetail ge = (BillingInvoiceDetail)ite1.next();
                qua += ge.getQuantity();
            }
            if(qua < sDetail.getQuantity()){
                result = "Open";
                break;
            }
        }
        return result;
    }

    public ModelAndView getBillingSalesOrderRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getBillingSalesOrderRows(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getBillingSalesOrderRows(HttpServletRequest request) throws ServiceException, SessionExpiredException{
        JSONObject jobj=new JSONObject();
		try {
            HashMap<String,Object> requestParams = AccountingManager.getGlobalParams(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            String[] sos=request.getParameterValues("bills");
            int i=0;
            JSONArray jArr=new JSONArray();
            double addobj = 1;

            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("salesOrder.ID");
            order_by.add("srno");
            order_type.add("asc");
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);

            String closeflag = request.getParameter("closeflag");
            while(sos!=null&&i<sos.length){
//                BillingSalesOrder so=(BillingSalesOrder)session.get(BillingSalesOrder.class, sos[i]);
                KwlReturnObject result = accountingHandlerDAOobj.getObject(BillingSalesOrder.class.getName(), sos[i]);
                BillingSalesOrder so = (BillingSalesOrder) result.getEntityList().get(0);
                KWLCurrency currency=so.getCustomer().getAccount().getCurrency()==null?kwlcurrency:so.getCustomer().getAccount().getCurrency();
//                Iterator itr=so.getRows().iterator();
                filter_params.clear();
                filter_params.add(so.getID());
                KwlReturnObject podresult = accSalesOrderDAOobj.getBillingSalesOrderDetails(soRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                
                while(itr.hasNext()) {
                    BillingSalesOrderDetail row=(BillingSalesOrderDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", so.getID());
                    obj.put("billno", so.getSalesOrderNumber());
                    obj.put("currencysymbol",currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productdetail", row.getProductDetail());
                    obj.put("memo", row.getRemark());
                    obj.put("rate", row.getRate());
                    double rowTaxPercent = 0;
                    if (row.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), so.getOrderDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("prtaxid", row.getTax()==null?"":row.getTax().getID());
                     KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams,row.getRate(),currency.getCurrencyID(),so.getOrderDate(),0);
                    obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
                  if(closeflag!=null){
                       addobj =  getBillingSalesOrderDetailStatus(row);
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
    }

    public double getBillingSalesOrderDetailStatus(BillingSalesOrderDetail sod) throws ServiceException {
        double result = sod.getQuantity();
        KwlReturnObject bidResult = accInvoiceDAOobj.getBIDFromBSOD(sod.getID());
        Iterator ite1 = bidResult.getEntityList().iterator();
        int qua = 0;
        while(ite1.hasNext()){
            BillingInvoiceDetail ge = (BillingInvoiceDetail)ite1.next();
            qua += ge.getQuantity();
        }
        result = sod.getQuantity()-qua;
        return result;
    }

    public ModelAndView exportSalesOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            KwlReturnObject result = accSalesOrderDAOobj.getSalesOrders(requestParams);
            JSONArray jarr = getSalesOrdersJson(request, result.getEntityList());
            jobj.put("data", jarr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView exportBillingSalesOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            KwlReturnObject result = accSalesOrderDAOobj.getBillingSalesOrders(requestParams);
            JSONArray jarr = getBillingSalesOrdersJson(request, result.getEntityList());
            jobj.put("data", jarr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView getQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            KwlReturnObject result = accSalesOrderDAOobj.getQuotations(requestParams);
            JSONArray jarr = getQuotationsJson(request, result.getEntityList());
            jobj.put("data", jarr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getQuotationsJson(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Quotation salesOrder=(Quotation)itr.next();
                KWLCurrency currency=salesOrder.getCustomer().getAccount().getCurrency()==null?kwlcurrency:salesOrder.getCustomer().getAccount().getCurrency();
                Customer customer=salesOrder.getCustomer();
                JSONObject obj = new JSONObject();
                obj.put("billid", salesOrder.getID());
                obj.put("personid", customer.getID());
                obj.put("personemail", salesOrder.getCustomer()==null?"":salesOrder.getCustomer().getEmail());
                obj.put("billno", salesOrder.getquotationNumber());
                obj.put("duedate", authHandler.getDateFormatter(request).format(salesOrder.getDueDate()));
                obj.put("date", authHandler.getDateFormatter(request).format(salesOrder.getQuotationDate()));
                Iterator itrRow = salesOrder.getRows().iterator();
                double amount = 0,amountinbase=0,totalDiscount = 0, discountPrice = 0;
                while (itrRow.hasNext()) {
                    QuotationDetail sod= (QuotationDetail) itrRow.next();
                    amount+=sod.getQuantity()*sod.getRate();
                    double  rowTaxPercent=0;
                    if(sod.getTax()!=null){
                        requestParams.put("transactiondate", salesOrder.getQuotationDate());
                        requestParams.put("taxid", sod.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }

                    discountPrice = (sod.getQuantity() * sod.getRate()) - (sod.getQuantity() * sod.getRate() * sod.getDiscount()/100);
                    amount = amount - (sod.getQuantity() * sod.getRate() * sod.getDiscount()/100);
                    amount = amount + (discountPrice * rowTaxPercent/100);
                }
                if(salesOrder.getDiscount() != 0){
                	if(salesOrder.isPerDiscount()){
                		totalDiscount = amount * salesOrder.getDiscount()/100;
                		amount = amount - totalDiscount ;
                	}else{
                		amount = amount - salesOrder.getDiscount();
                		totalDiscount = salesOrder.getDiscount();
                	}
                }
                obj.put("discount", totalDiscount);
                if(salesOrder.getTax() != null){
                	requestParams.put("transactiondate", salesOrder.getQuotationDate());
                    requestParams.put("taxid", salesOrder.getTax().getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    double TaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    amountinbase=amount+amount*TaxPercent/100;
                }
                if(salesOrder.getTax() != null)
                	obj.put("amountinbase", amountinbase);
                else{
                	obj.put("amountinbase", amount);
                }	
                obj.put("amount", amount);
                KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), salesOrder.getQuotationDate(), 0);
                obj.put("currencysymbol", currency.getSymbol());
                obj.put("taxid", salesOrder.getTax()==null?"":salesOrder.getTax().getID());
                obj.put("taxname", salesOrder.getTax()==null?"":salesOrder.getTax().getName());
                double  taxPercent=0;
                if(salesOrder.getTax()!=null){
                    requestParams.put("transactiondate", salesOrder.getQuotationDate());
                    requestParams.put("taxid", salesOrder.getTax().getID());
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
                obj.put("currencyid",currency.getCurrencyID());
                obj.put("personname", customer.getName());
                obj.put("memo", salesOrder.getMemo());
                    jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getQuotationsJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getQuotationRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getQuotationRows(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesOrderController.getQuotationRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesOrderController.getQuotationRows:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getQuotationRows(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj=new JSONObject();
        try {
            HashMap<String,Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            String[] sos=request.getParameterValues("bills");
            int i=0;
            JSONArray jArr=new JSONArray();
            int addobj = 1;

            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("quotation.ID");
            order_by.add("srno");
            order_type.add("asc");
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);

            while(sos!=null&&i<sos.length){
                KwlReturnObject result = accountingHandlerDAOobj.getObject(Quotation.class.getName(), sos[i]);
                Quotation so = (Quotation) result.getEntityList().get(0);
                KWLCurrency currency=so.getCustomer().getAccount().getCurrency()==null?kwlcurrency:so.getCustomer().getAccount().getCurrency();
                filter_params.clear();
                filter_params.add(so.getID());
                KwlReturnObject podresult = accSalesOrderDAOobj.getQuotationDetails(soRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                
                while(itr.hasNext()) {
                    QuotationDetail row=(QuotationDetail)itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", so.getID());
                    obj.put("billno", so.getquotationNumber());
                    obj.put("currencysymbol",currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("productname",row.getProduct().getName());
                    obj.put("unitname", row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getName());
                    obj.put("desc", row.getProduct().getDescription());
                    obj.put("type",row.getProduct().getProducttype()==null?"":row.getProduct().getProducttype().getName());
                    obj.put("pid",row.getProduct().getProductid());
                    obj.put("memo", row.getRemark());
                    obj.put("prdiscount", row.getDiscount());
                    double rowTaxPercent = 0;
                    if (row.getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), so.getQuotationDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("prtaxid",row.getTax()==null?"": row.getTax().getID());
                    obj.put("rate", row.getRate());
                    KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getQuotationDate(), 0);
                    obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
                    
                        obj.put("quantity", row.getQuantity());
                    if (addobj > 0) {
                        jArr.put(obj);
                    }
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }

    public ModelAndView exportQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            KwlReturnObject result = accSalesOrderDAOobj.getQuotations(requestParams);
            JSONArray jarr = getQuotationsJson(request, result.getEntityList());
            jobj.put("data", jarr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
}
