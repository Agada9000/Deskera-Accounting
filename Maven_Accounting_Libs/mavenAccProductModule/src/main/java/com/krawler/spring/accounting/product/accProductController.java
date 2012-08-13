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
package com.krawler.spring.accounting.product;

import com.krawler.accounting.utils.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Cyclecount;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.PriceList;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.ProductAssembly;
import com.krawler.hql.accounting.ProductCyclecount;
import com.krawler.hql.accounting.Producttype;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accProductController extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accProductDAO accProductObj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDaoObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

//Product Type
    public ModelAndView saveProductTypes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveProductTypes(request);
            issuccess = true;
            msg = "Product Types has been updated successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public Producttype saveProductTypes(HttpServletRequest request) throws ServiceException, AccountingException {
        Producttype ptype = null;
        try {
            String delQuery = "";
            int delCount = 0;
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            KwlReturnObject typeresult = null;
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("id")) == false) {
                    try {
                        typeresult = accProductObj.deleteProductType(delQuery);
                        delCount += typeresult.getRecordTotalCount();
                    } catch (ServiceException ex) {
                        throw new AccountingException("Selected record(s) is currently used by the product(s).");
                    }
                }
            }

            String auditMsg = "", auditID = "";
            String fullName = sessionHandlerImpl.getUserFullName(request);
            if (delCount > 0) {
                auditTrailObj.insertAuditLog(AuditAction.PRODUCT_TYPE_DELETED, "User " + fullName + " deleted " + delCount + " product type", request, "0");
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }

                HashMap<String, Object> typeMap = new HashMap<String, Object>();
                typeMap.put("name", jobj.getString("name"));

                if (StringUtil.isNullOrEmpty(jobj.getString("id"))) {
                    auditMsg = "added";
                    auditID = AuditAction.PRODUCT_TYPE_CREATED;
                } else {
                    typeMap.put("id", jobj.getString("id"));
                    auditMsg = "updated";
                    auditID = AuditAction.PRODUCT_TYPE_UPDATED;
                }
                typeresult = accProductObj.saveProductTypes(typeMap);
                ptype = (Producttype) typeresult.getEntityList().get(0);
                auditTrailObj.insertAuditLog(auditID, "User " + fullName + " " + auditMsg + " product type to " + ptype.getName(), request, ptype.getID());
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return ptype;
    }

    public ModelAndView getProductTypes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject result = accProductObj.getProductTypes(requestParams);
            jobj = getProductTypesJson(request, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getProductTypesJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Producttype ptype = (Producttype) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", ptype.getID());
                obj.put("name", ptype.getName());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getProductTypesJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

//Product
    public ModelAndView getProducts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);

            KwlReturnObject result = accProductObj.getProducts(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

//            List pagingList = list;
//            String start = request.getParameter("start");
//            String limit = request.getParameter("limit");
//            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
//                pagingList = StringUtil.getPagedList(list, Integer.parseInt(start), Integer.parseInt(limit));
//            }

            JSONArray DataJArr = productHandler.getProductsJson(request, list);
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getProductsByType(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            List purchasePrice = new ArrayList();
            List salesPrice = new ArrayList();
            requestParams.put("type", request.getParameter("type"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accProductObj.getProductsByType(requestParams);
            Iterator iterator = result.getEntityList().iterator();
   
            while(iterator.hasNext()){
            	Object[] row = (Object[]) iterator.next();
                Product product = (Product) row[0];
            	
//            	Product product = (Product)iterator.next();
            	KwlReturnObject purchase = accProductObj.getProductPrice(product.getID(), true, null);
            	purchasePrice.add(purchase.getEntityList().get(0));
            	KwlReturnObject sales = accProductObj.getProductPrice(product.getID(), false, null);
            	salesPrice.add(sales.getEntityList().get(0));
            }
            
            jobj = getProductsByTypeJson(request, result.getEntityList(), purchasePrice, salesPrice);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getProductsByTypeJson(HttpServletRequest request, List list, List purchaseprice,List salesPrice) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            Iterator itr = list.iterator();
            Iterator iteratorPurchase = purchaseprice.iterator();
            Iterator iteratorSales = salesPrice.iterator();
            int index = 0;
            
            while(itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Product product = (Product) row[0];
                JSONObject obj = new JSONObject();
                obj.put("productid", product.getID());
                obj.put("productname", product.getName());
                obj.put("desc", product.getDescription());
                obj.put("producttype", (product.getProducttype()!=null?product.getProducttype().getID():""));
                obj.put("type", (product.getProducttype()!=null?product.getProducttype().getName():""));
                obj.put("quantity",(row[1]==null?0:row[1]));
                obj.put("purchaseprice", purchaseprice.get(index)); 
                obj.put("salesprice", salesPrice.get(index++));     
                jArr.put(obj);
            }
            
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getProductsByTypeJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView getSuggestedReorderProducts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            KwlReturnObject result = accProductObj.getSuggestedReorderProducts(requestParams);
            JSONArray DataJArr = productHandler.getProductsJson(request, result.getEntityList());
            jobj.put("data", DataJArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }


//Product Price
    public ModelAndView getProductPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("productid", request.getParameter("productid"));

            KwlReturnObject result = accProductObj.getPrice(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getPriceListJson(request, list);
            jobj.put("data", DataJArr);
            jobj.put("count", count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accProductController.getProductPrice : "+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getPriceListJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            DateFormat df = authHandler.getDateFormatter(request);
            while (itr.hasNext()) {
                PriceList price = (PriceList) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("priceid", price.getID());
                obj.put("applydate", df.format(price.getApplyDate()));
                obj.put("carryin", price.isCarryIn());
                obj.put("price", price.getPrice());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getPriceListJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView setNewPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            boolean dateexist = false;
            dateexist = setNewPrice(request);
            jobj.put("dateexist", dateexist);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.prod.priceapp", null, RequestContextUtils.getLocale(request));   //"New Price has been applied successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean setNewPrice(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        KwlReturnObject priceresult;
		try {
            boolean updatePrice=Boolean.parseBoolean(request.getParameter("changeprice"));
            String productid = request.getParameter("productid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean carryIn = Boolean.parseBoolean(request.getParameter("carryin"));
            Date appDate = authHandler.getDateFormatter(request).parse(request.getParameter("applydate"));
            double newprice = Double.parseDouble(request.getParameter("price"));

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("productid", productid);
            requestParams.put("carryin", carryIn);
            requestParams.put("applydate", appDate);
            requestParams.put("price", newprice);

            KwlReturnObject result = accProductObj.getPriceListEntry(requestParams);
            List list = result.getEntityList();

            HashMap<String, Object> priceMap = new HashMap<String, Object>();
            priceMap.put("price", newprice);

            if (list.size() > 0 && !updatePrice) {
                return true;
            } else {
                if (list.size() <= 0) {
                    priceMap.put("productid", productid);
                    priceMap.put("companyid", companyid);
                    priceMap.put("carryin", carryIn);
                    priceMap.put("applydate", appDate);
                    priceresult = accProductObj.addPriceList(priceMap);
                } else {
                    PriceList price = (PriceList) list.get(0);
                    priceMap.put("priceid", price.getID());
                    priceresult = accProductObj.updatePriceList(priceMap);
                }
                PriceList pl = (PriceList) priceresult.getEntityList().get(0);
                String pDescription=StringUtil.isNullOrEmpty(pl.getProduct().getDescription())?"":" ("+pl.getProduct().getDescription()+")";
                auditTrailObj.insertAuditLog(AuditAction.PRICE_CHANGED, "User "+sessionHandlerImpl.getUserFullName(request)+ " changed the price for product "+
                    pl.getProduct().getName()+pDescription
                    +" to "+pl.getPrice()+" ", request, pl.getID());  //+"currencyChange"
            }
        } catch (ParseException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("setNewPrice : "+ex.getMessage(), ex);
        }
        return false;
    }

    public ModelAndView saveProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject productresult = saveProduct(request);
            Product product = (Product) productresult.getEntityList().get(0);
            jobj.put("productID", product.getID());
            issuccess = true;
            msg = messageSource.getMessage("acc.prod.save", null, RequestContextUtils.getLocale(request));   //"Product/Service has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public KwlReturnObject saveProduct(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, ParseException {
        KwlReturnObject productresult;
        try {
            String auditMsg = "added";
            String auditID = AuditAction.PRODUCT_CREATION;
            
            String productid = request.getParameter("productid");
            String productName = request.getParameter("productname");
            String parentName = request.getParameter("parentname");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isRebuild = false;
            
            Date appDate = authHandler.getDateFormatter(request).parse(request.getParameter("applydate"));
            HashMap<String, Object> productMap = new HashMap<String, Object>();
            String parentid = request.getParameter("parentid");
            boolean issub = request.getParameter("subproduct") != null;
            boolean syncable = Boolean.parseBoolean(request.getParameter("syncable"));
            if (issub && !StringUtil.isNullOrEmpty(parentid)) {
                productMap.put("parentid", parentid);
            }else{
                productMap.put("parentid", null);
            }
            productMap.put("desc", request.getParameter("desc"));
            productMap.put("uomid", request.getParameter("uomid"));
            productMap.put("purchaseaccountid", request.getParameter("purchaseaccountid"));
            productMap.put("salesaccountid", request.getParameter("salesaccountid"));
            productMap.put("purchaseretaccountid", request.getParameter("purchaseretaccountid"));
            productMap.put("salesretaccountid", request.getParameter("salesretaccountid"));
            productMap.put("leadtime", Integer.parseInt(request.getParameter("leadtime")));
            productMap.put("reorderlevel", Integer.parseInt(request.getParameter("reorderlevel")));
            productMap.put("reorderquantity", Integer.parseInt(request.getParameter("reorderquantity")));
            productMap.put("companyid", companyid);
            productMap.put("vendorid", request.getParameter("vendor"));
            productMap.put("syncable", syncable);
            productMap.put("name", productName);

            if (StringUtil.isNullOrEmpty(productid)) {
                productMap.put("name", productName);
                productMap.put("producttype", request.getParameter("producttype"));
                productMap.put("productid", request.getParameter("pid"));

                productresult = accProductObj.addProduct(productMap);
            } else {
//                if(accProductObj.isChild(productid, parentid)){
//                    throw new AccountingException("\""+productName+"\" is a parent of \""+parentName+"\" so can't set \""+parentName+"\" as a parent.");
            	if(isChildorGrandChild(productid, parentid)){
                    throw new AccountingException("\""+productName+"\" is a parent of \""+parentName+"\" so can't set \""+parentName+"\" as a parent.");
                }
                auditMsg = "updated";
                auditID = AuditAction.PRODUCT_UPDATION;
                productMap.put("id", productid);
                productresult = accProductObj.updateProduct(productMap);
                
                if(Boolean.parseBoolean(request.getParameter("editQuantity")) == true){	    // Update initial quantity while product edit  
                	int quantity = Integer.parseInt(request.getParameter("quantity"));
                	JSONObject inventoryjson = new JSONObject();
                	inventoryjson.put("productid", productid);
                	inventoryjson.put("quantity", quantity);
                	inventoryjson.put("description", "Inventory Opened");
                	inventoryjson.put("carryin", true);
                	inventoryjson.put("defective", false);
                	inventoryjson.put("newinventory", true);
                	inventoryjson.put("companyid", companyid);
                	inventoryjson.put("updatedate", appDate);
                	accProductObj.updateInitialInventory(inventoryjson);
                }
            }

            Product product = (Product) productresult.getEntityList().get(0);
            if(product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
            	if (!StringUtil.isNullOrEmpty(productid))
            		isRebuild = Boolean.parseBoolean(request.getParameter("reBuild"));
                saveAssemblyProduct(request, product, isRebuild);
            }

            //Cycle count will performed all the item except service.
            if(!request.getParameter("producttype").equalsIgnoreCase(Producttype.SERVICE)){
                int interval = Integer.parseInt(request.getParameter("ccountinterval"));
                int tolerance = Integer.parseInt(request.getParameter("ccounttolerance"));
//                cyclecount.makeProductCyclecountEntry(session,product,productid,interval,tolerance);
                HashMap<String, Object> cycleParams = new HashMap<String, Object>();
                cycleParams.put("productid", product.getID());
                cycleParams.put("interval", interval);
                cycleParams.put("tolerance", tolerance);
                accProductObj.saveProductCycleCount(cycleParams);
            }

            Inventory inventory;
            int quantity;
            try {
                quantity = Integer.parseInt(request.getParameter("quantity"));
            } catch (Exception e) {
                quantity = 0;
            }
            if (quantity > 0) {
                if (StringUtil.isNullOrEmpty(productid)) {
//                    makeNewInventory(session, request, product, quantity, "Inventory Opened", true, false);
                    JSONObject inventoryjson = new JSONObject();
                    inventoryjson.put("productid", product.getID());
                    inventoryjson.put("quantity", quantity);
                    inventoryjson.put("description", "Inventory Opened");
                    inventoryjson.put("carryin", true);
                    inventoryjson.put("defective", false);
                    inventoryjson.put("newinventory", true);
                    inventoryjson.put("companyid", companyid);
                    inventoryjson.put("updatedate", appDate);
                    KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                    inventory = (Inventory) invresult.getEntityList().get(0);

//                    updateAssemblyInventory(session, request, quantity, "", "Inventory Opened", product);
                    HashMap<String, Object> assemblyParams = getAssemblyRequestParams(request);
                    assemblyParams.put("memo", "Inventory Opened");
                    assemblyParams.put("refno", "");
                    assemblyParams.put("buildproductid", product.getID());
                    accProductObj.updateAssemblyInventory(assemblyParams);
                } else {
//                    Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
//                    String selQuery = "select id from Inventory   where product.ID =? and newinv='T' and company.companyID=?";
//                    List list = HibernateUtil.executeQuery(session, selQuery, new Object[]{productid, company.getCompanyID()});
                    HashMap<String, Object> inventoryFilter = new HashMap<String, Object>();
                    inventoryFilter.put("productid", productid);
                    inventoryFilter.put("companyid", companyid);
                    KwlReturnObject result = accProductObj.getInventoryEntry(inventoryFilter);
                    List list = result.getEntityList();
//                    Iterator itr = list.iterator();
                    if (list.isEmpty()) {
//                        makeNewInventory(session, request, product, quantity, "Inventory Opened", true, false);
                        JSONObject inventoryjson = new JSONObject();
                        inventoryjson.put("productid", product.getID());
                        inventoryjson.put("quantity", quantity);
                        inventoryjson.put("description", "Inventory Opened");
                        inventoryjson.put("carryin", true);
                        inventoryjson.put("defective", false);
                        inventoryjson.put("newinventory", true);
                        inventoryjson.put("companyid", companyid);
                        inventoryjson.put("updatedate", appDate);
                        KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                        inventory = (Inventory) invresult.getEntityList().get(0);

//                        updateAssemblyInventory(session, request, quantity, "", "Inventory Opened", product);
                        HashMap<String, Object> assemblyParams = getAssemblyRequestParams(request);
                        assemblyParams.put("memo", "Inventory Opened");
                        assemblyParams.put("refno", "");
                        assemblyParams.put("buildproductid", product.getID());
                        accProductObj.updateAssemblyInventory(assemblyParams);
                    }
/*//Do not update intial quantity after product creation
                     else {
//                        String row = (String) itr.next();
//                        Inventory inventory = (Inventory) session.get(Inventory.class, row);
//                        inventory.setQuantity(quantity);
                        inventory = (Inventory) itr.next();
                        JSONObject inventoryjson = new JSONObject();
                        inventoryjson.put("inventoryid", inventory.getID());
                        inventoryjson.put("quantity", quantity);
                        inventoryjson.put("newinv", "T");
                        KwlReturnObject invresult = accProductObj.updateInventory(inventoryjson);
                        inventory = (Inventory) invresult.getEntityList().get(0);
                    }
 */
                }
            }
            String pDescription=StringUtil.isNullOrEmpty(product.getDescription())?"":" ("+product.getDescription()+")";
            auditTrailObj.insertAuditLog(auditID, "User "+sessionHandlerImpl.getUserFullName(request)+" "+auditMsg+" product "+product.getName()+pDescription, request, product.getID());
            if(auditMsg.equals("added"))
                auditTrailObj.insertAuditLog(AuditAction.INVENTORY_OPENED, "User "+sessionHandlerImpl.getUserFullName(request) +" added Product \""+product.getName()+ "\" in Inventory", request, product.getID());
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (NumberFormatException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return productresult;
    }

//Product Assembly
    public void saveAssemblyProduct(HttpServletRequest request, Product assemblyProduct, boolean isRebuild) throws ServiceException {
        String jsondata = request.getParameter("assembly");
        try {
            JSONArray jarr = new JSONArray("[" + jsondata + "]");
//            String query = "delete from ProductAssembly where product = ?";
//            HibernateUtil.executeUpdate(session, query, assemblyProduct);
            if(isRebuild){
            	updateBillofMaterialsInventory(request, assemblyProduct);
            }
            accProductObj.deleteProductAssembly(assemblyProduct.getID());

            for (int i = 0; i < jarr.length(); i++) {
                JSONObject jobj = jarr.getJSONObject(i);
//                ProductAssembly obj = new ProductAssembly();
//                obj.setProduct(assemblyProduct);
//                Product prd = (Product) session.get(Product.class, jobj.get("product").toString());
//                obj.setSubproducts(prd);
//                obj.setQuantity(Integer.parseInt(jobj.get("quantity").toString()));
//                session.save(obj);
                HashMap<String, Object> assemblyMap = new HashMap<String, Object>();
                assemblyMap.put("productid", assemblyProduct.getID());
                assemblyMap.put("subproductid", jobj.getString("product"));
                assemblyMap.put("quantity", Integer.parseInt(jobj.getString("quantity")));
                accProductObj.saveProductAssembly(assemblyMap);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveAssemblyProduct : "+ex.getMessage(), ex);
        }
    }

    public HashMap<String, Object> getAssemblyRequestParams(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("assembly", request.getParameter("assembly"));
        requestParams.put("applydate", request.getParameter("applydate"));
        requestParams.put("quantity", Integer.parseInt(request.getParameter("quantity")==null?"0":request.getParameter("quantity").toString()));
        return requestParams;
    }

    public ModelAndView getAssemblyItems(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("productid", request.getParameter("productid"));
            KwlReturnObject result = accProductObj.getAssemblyItems(requestParams);
            jobj = getAssemblyItemsJson(request, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getAssemblyItemsJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            Iterator itr = list.iterator();
            while(itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                ProductAssembly passembly = (ProductAssembly) row[0];
                JSONObject obj = new JSONObject();
                obj.put("id", passembly.getID());
                obj.put("productid", passembly.getSubproducts().getID());
                obj.put("productname", passembly.getSubproducts().getName());
                obj.put("desc", passembly.getSubproducts().getDescription());
                obj.put("producttype", passembly.getSubproducts().getProducttype().getID());
                obj.put("type", passembly.getSubproducts().getProducttype().getName());
                obj.put("purchaseprice", row[1]==null?0:row[1]);
                obj.put("saleprice",row[2]==null?0:row[2]);
                obj.put("quantity", passembly.getQuantity());
                obj.put("onhand", row[3]==null?0:row[3]);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getAssemblyItemsJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView buildProductAssembly(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            buildProductAssembly(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.prod.PA", null, RequestContextUtils.getLocale(request));   //"Product's Assemblies have been updated successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void buildProductAssembly(HttpServletRequest request) throws ServiceException{
		try {
            String productid=request.getParameter("product");
            int quantity = Integer.parseInt(request.getParameter("quantity")==null?"0":request.getParameter("quantity").toString());
//            Product product = (Product)session.get(Product.class,productid);
//            String refno = request.getParameter("refno");
//            String memostr = request.getParameter("memo");
//            updateAssemblyInventory(session, request, quantity, refno, memostr, product);
            HashMap<String, Object> assemblyParams = getAssemblyRequestParams(request);
            assemblyParams.put("memo", request.getParameter("memo"));
            assemblyParams.put("refno", request.getParameter("refno"));
            assemblyParams.put("buildproductid", request.getParameter("product"));
            accProductObj.updateAssemblyInventory(assemblyParams);

//            makeInventory(session, request, product, quantity, "Build Assembly", true, false,null);
            JSONObject inventoryjson = new JSONObject();
            inventoryjson.put("productid", productid);
            inventoryjson.put("quantity", quantity);
            inventoryjson.put("description", "Build Assembly");
            inventoryjson.put("carryin", true);
            inventoryjson.put("defective", false);
            inventoryjson.put("newinventory", false);
            inventoryjson.put("companyid", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildProductAssembly : "+e.getMessage(), e);
        }
    }

//Cycle Count
    public ModelAndView getCycleCountProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("type", request.getParameter("type"));
            requestParams.put("productid", request.getParameter("productid"));

            KwlReturnObject result = accProductObj.getCycleCountProduct(requestParams);
            JSONArray DataJArr = getCycleCountProductJson(request, result.getEntityList());
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getCycleCountProductJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            String productid = request.getParameter("productid");
            boolean recount = false;
            if(Integer.parseInt(request.getParameter("type"))!=0){//type = 0 include initialcount ; type =1 include recount items
                recount = true;
            }
            Iterator itr = list.iterator();
            while(itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Product product = (Product) row[0];
                ProductCyclecount cp =(ProductCyclecount)row[2];
                if(product.getID().equals(productid)) continue;
                JSONObject obj = new JSONObject();
                obj.put("productid", product.getID());
                obj.put("product", product.getName());
                UnitOfMeasure uom = product.getUnitOfMeasure();
                obj.put("uomid", uom.getID());
                obj.put("uom", uom.getName());
                int currentQty = row[1]==null?0:(Integer.parseInt(row[1].toString()));
                obj.put("currentQuantity",currentQty);
                obj.put("newQuantity",0);
                obj.put("varianceQuantity",currentQty);
                obj.put("tolerance",cp.getTolerance());
                float toleranceQua = ((float)(currentQty*cp.getTolerance()))/100;
                obj.put("tolerancemsg",(currentQty > toleranceQua)?1:0);// 1 for tolerance exceeded 0 : within limit
                if(recount){
                    obj.put("cyclecountId",row[3]);
                    obj.put("statusid",1);// 1 : Recount
                }
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCycleCountProductJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView makeCyclecountEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            makeCyclecountEntry(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.prod.cc", null, RequestContextUtils.getLocale(request));   //"Cycle count entry made successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void makeCyclecountEntry(HttpServletRequest request) throws ServiceException {
        String jsonData = request.getParameter("jsondata");
        Date dtObj = new Date();
        Date CtDate = new Date(new Date().getYear(), new Date().getMonth(), new Date().getDate());
        try {
            com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(jsonData);
            int count = jobj.getJSONArray("root").length();
            for (int i = 0; i < count; i++) {
                com.krawler.utils.json.base.JSONObject jobj1 = jobj.getJSONArray("root").getJSONObject(i);
                String productid = jobj1.getString("productid");
                int initQua = Integer.parseInt(jobj1.getString("initQua"));
                int countQua = Integer.parseInt(jobj1.getString("countQua"));
                int status = 0;
//                 Status = 0 Pending;  Status = 1 Recount; Status = 2 Approve.
                String reason = jobj1.getString("reason");
//                 Cyclecount obj = null;
//                 if(jobj1.has("cyclecountId")){
//                    obj = (Cyclecount) session.load(Cyclecount.class,jobj1.getString("cyclecountId"));
//                 }else{
//                     obj = new Cyclecount();
//                 }
//                 obj.setProduct((Product)session.get(Product.class,productid));
//                 obj.setIniquantity(initQua);
//                 obj.setCountedquantity(countQua);
//                 obj.setReason(reason);
//                 obj.setStatus(status);
//                 obj.setCountDate(CtDate);
//                 session.saveOrUpdate(obj);
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                if(jobj1.has("cyclecountId") && !StringUtil.isNullOrEmpty(jobj1.getString("cyclecountId"))) {
                    dataMap.put("id", jobj1.getString("cyclecountId"));
                }
                dataMap.put("productid", productid);
                dataMap.put("reason", reason);
                dataMap.put("status", status);
                dataMap.put("countquantity", countQua);
                dataMap.put("initquantity", initQua);
                dataMap.put("countdate", CtDate);
                KwlReturnObject result = accProductObj.saveCycleCount(dataMap);
                Cyclecount obj = (Cyclecount) result.getEntityList().get(0);


//                 String selQuery = "from ProductCyclecount pc where pc.product.id = ?";
//                 List list = HibernateUtil.executeQuery(session, selQuery,productid);
                HashMap<String, Object> ccfilterParams = new HashMap<String, Object>();
                ccfilterParams.put("productid", productid);
                KwlReturnObject pcresult = accProductObj.getCycleCountEntry(ccfilterParams);
                Iterator itr = pcresult.getEntityList().iterator();
                while (itr.hasNext()) {
                    ProductCyclecount pcObj = (ProductCyclecount) itr.next();
//                    pcObj.setPrevDate(CtDate);
                    Calendar c1 = Calendar.getInstance();
                    c1.add(Calendar.DATE, pcObj.getCountInterval());
//                    pcObj.setNextDate(new Date(c1.getTime().getYear(), c1.getTime().getMonth(), c1.getTime().getDate()));
//                    session.saveOrUpdate(pcObj);
                    HashMap<String, Object> pcdataMap = new HashMap<String, Object>();
                    pcdataMap.put("id", pcObj.getID());
                    pcdataMap.put("prevdate", CtDate);
                    pcdataMap.put("nextdate", new Date(c1.getTime().getYear(), c1.getTime().getMonth(), c1.getTime().getDate()));

                    KwlReturnObject pccresult = accProductObj.saveProductCycleCount(pcdataMap);
                }
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("makeCyclecountEntry : " + e.getMessage(), e);
        }
    }

    public ModelAndView cycleCountReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = getCycleCountRequestMap(request);
            KwlReturnObject result = accProductObj.cycleCountReport(requestParams, false);
            JSONArray DataJArr = getCycleCountReportJson(request, result.getEntityList(), false);
            jobj.put("data", DataJArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public HashMap<String, Object> getCycleCountRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("productid", request.getParameter("productid"));
        requestParams.put("stdate", request.getParameter("stdate"));
        requestParams.put("enddate", request.getParameter("enddate"));
        requestParams.put("start", request.getParameter("start"));
        requestParams.put("limit", request.getParameter("limit"));
        return requestParams;
    }
    
    public JSONArray getCycleCountReportJson(HttpServletRequest request, List list, boolean isExport) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            String productid = request.getParameter("productid");
            Iterator itr = list.iterator();
            while(itr.hasNext()) {
//                Object[] row = (Object[]) itr.next();
                Cyclecount cpObj = (Cyclecount) itr.next();
                Product product = cpObj.getProduct();
                if(product.getID().equals(productid)) continue;
//                ProductCyclecount cp = getCycleountDetail(session, product);
                HashMap<String, Object> filterParams = new HashMap<String, Object>();
                filterParams.put("productid", product.getID());
                KwlReturnObject result = accProductObj.getCycleCountEntry(filterParams);
                ProductCyclecount cp = null;
                Iterator resitr = result.getEntityList().iterator();
                while (resitr.hasNext()) {
                    cp = (ProductCyclecount) resitr.next();
                }

                int varianceQua = (cpObj.getIniquantity()-cpObj.getCountedquantity());
                float toleranceQua = ((float)(cpObj.getIniquantity()*cp.getTolerance()))/100;
                int positiveVarianceQua = varianceQua<0 ? varianceQua*(-1) : varianceQua;
                int tolerancemsg = (positiveVarianceQua > toleranceQua)?1:0;
                JSONObject obj = new JSONObject();
                obj.put("id", cpObj.getID());
                obj.put("productid", product.getID());
                obj.put("product", product.getName());
                UnitOfMeasure uom = product.getUnitOfMeasure();
                obj.put("uomid", uom.getID());
                obj.put("uom", uom.getName());
                obj.put("currentQuantity",cpObj.getIniquantity());
                obj.put("newQuantity",cpObj.getCountedquantity());
                obj.put("varianceQuantity",varianceQua);
                obj.put("reasone",cpObj.getReason());
                obj.put("tolerance",cp.getTolerance());
                if(isExport){
                    if(cpObj.getStatus()==0)
                        obj.put("statusid","Pending");
                    else if(cpObj.getStatus()==1)
                        obj.put("statusid","Recount");
                    else
                        obj.put("statusid","Approved");
                    if(tolerancemsg==1){
                        obj.put("tolerancemsg","Tolerance Exceeded");
                    }else{
                        obj.put("tolerancemsg","Tolerance within limit");
                    }
                }else{
                    obj.put("statusid",cpObj.getStatus());
                    obj.put("tolerancemsg",tolerancemsg);
                }
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCycleCountReportJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getCycleCountForApproval(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("productid", request.getParameter("productid"));
            requestParams.put("countdate", request.getParameter("countdate"));
            
            KwlReturnObject result = accProductObj.getCycleCountForApproval(requestParams);
            jobj = getCycleCountForApprovalJson(request, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getCycleCountForApprovalJson(HttpServletRequest request, List list) throws ServiceException{
        JSONObject jobj=new JSONObject();
        try {
            String productid = request.getParameter("productid");
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
//                Object[] row = (Object[]) itr.next();
                Cyclecount cpObj = (Cyclecount) itr.next();
                Product product = cpObj.getProduct();
//                ProductCyclecount cp = getCycleountDetail(session, product);
                if(product.getID().equals(productid)) continue;
                HashMap<String, Object> filterParams = new HashMap<String, Object>();
                filterParams.put("productid", product.getID());
                KwlReturnObject result = accProductObj.getCycleCountEntry(filterParams);
                ProductCyclecount cp = null;
                Iterator resitr = result.getEntityList().iterator();
                while (resitr.hasNext()) {
                    cp = (ProductCyclecount) resitr.next();
                }
                
                int varianceQua = (cpObj.getIniquantity()-cpObj.getCountedquantity());
                float toleranceQua = ((float)(cpObj.getIniquantity()*cp.getTolerance()))/100;
                int positiveVarianceQua = varianceQua<0 ? varianceQua*(-1) : varianceQua;
                int tolerancemsg = (positiveVarianceQua > toleranceQua)?1:0;
                JSONObject obj = new JSONObject();
                obj.put("id", cpObj.getID());
                obj.put("productid", product.getID());
                obj.put("product", product.getName());
                UnitOfMeasure uom = product.getUnitOfMeasure();
                obj.put("uomid", uom.getID());
                obj.put("uom", uom.getName());
                obj.put("currentQuantity",cpObj.getIniquantity());
                obj.put("newQuantity",cpObj.getCountedquantity());
                obj.put("varianceQuantity",varianceQua);
                obj.put("statusid",cpObj.getStatus());
               	obj.put("reasone",cpObj.getReason());
                obj.put("tolerance",cp.getTolerance());
                obj.put("tolerancemsg",tolerancemsg);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("getCycleCountForApproval : "+e.getMessage(), e);
        }
        return jobj;
    }

    public ModelAndView getCycleCountWorkSheet(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = getCycleCountWorkSheetRequestMap(request);
            KwlReturnObject result = accProductObj.getCycleCountWorkSheet(requestParams, false);
            jobj = getCycleCountWorkSheetJson(request, result.getEntityList(), false);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public HashMap<String, Object> getCycleCountWorkSheetRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("stdate", request.getParameter("stdate"));
        requestParams.put("enddate", request.getParameter("enddate"));
        requestParams.put("ctdatefr", request.getParameter("ctdatefr"));
        requestParams.put("ctdateto", request.getParameter("ctdateto"));
        requestParams.put("includerecount", request.getParameter("includerecount"));
        return requestParams;
    }

    public JSONObject getCycleCountWorkSheetJson(HttpServletRequest request, List list, boolean isExport) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String productid = request.getParameter("productid");
            SimpleDateFormat sdf= (SimpleDateFormat) authHandler.getGlobalDateFormat();
            if (Integer.parseInt(request.getParameter("includerecount")) == 0) {

                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Object[] row = (Object[]) itr.next();
                    ProductCyclecount cpObj = (ProductCyclecount) row[0];
                    Product product = cpObj.getProduct();
                    if (product.getID().equals(productid)) {
                        continue;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("productid", product.getID());
                    obj.put("product", product.getName());
                    UnitOfMeasure uom = product.getUnitOfMeasure();
                    obj.put("uomid", uom.getID());
                    obj.put("uom", uom.getName());
                    obj.put("currentQuantity", (row[1] == null ? 0 : row[1]));
                    obj.put("newQuantity", 0);
                    if (cpObj.getPrevDate().compareTo(new Date(0, 0, 1)) == 0) {
                        obj.put("lastcounteddate", "Not Counted");
                    } else {
                        obj.put("lastcounteddate", sdf.format(cpObj.getPrevDate()));
                    }
                    obj.put("countinterval", cpObj.getCountInterval());
                    obj.put("varianceQuantity", (row[1] == null ? 0 : row[1]));
                    jArr.put(obj);
                }
                jobj.put("data", jArr);
            } else {

                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Object[] row = (Object[]) itr.next();
                    ProductCyclecount cpObj = (ProductCyclecount) row[1];
                    Product product = cpObj.getProduct();
                    if (product.getID().equals(productid)) {
                        continue;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("productid", product.getID());
                    obj.put("product", product.getName());
                    UnitOfMeasure uom = product.getUnitOfMeasure();
                    obj.put("uomid", uom.getID());
                    obj.put("uom", uom.getName());
                    obj.put("currentQuantity", (row[2] == null ? 0 : row[2]));
                    obj.put("newQuantity", 0);
                    obj.put("lastcounteddate", sdf.format(cpObj.getPrevDate()));
                    obj.put("countinterval", cpObj.getCountInterval());
                    obj.put("varianceQuantity", (row[2] == null ? 0 : row[2]));
                    jArr.put(obj);
                }
                jobj.put("data", jArr);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCycleCountWorkSheetJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView approveCyclecountEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            approveCyclecountEntry(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.prod.ccupdate", null, RequestContextUtils.getLocale(request));   //"Cycle count updated successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void approveCyclecountEntry(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        String jsonData = request.getParameter("jsondata");
        try {
            com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(jsonData);
            int count = jobj.getJSONArray("root").length();
            for (int i = 0; i < count; i++) {
                com.krawler.utils.json.base.JSONObject jobj1 = jobj.getJSONArray("root").getJSONObject(i);
                String id = jobj1.getString("id");
                String st = jobj1.getString("statusid");
                int status = Integer.parseInt(st);
//                 Cyclecount obj = (Cyclecount)session.get(Cyclecount.class,id);
//                 obj.setStatus(status);
                Cyclecount obj = (Cyclecount) kwlCommonTablesDAOObj.getClassObject(Cyclecount.class.getName(), id);
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put("id", id);
                dataMap.put("status", status);
                KwlReturnObject result = accProductObj.saveCycleCount(dataMap);
                obj = (Cyclecount) result.getEntityList().get(0);

//               Status = 0 Pending Status = 1 Recount  Status = 2 Approved
                if (status == 2) {
                    int quantity = obj.getIniquantity() - obj.getCountedquantity();
                    boolean carryin = true;
                    if (obj.getIniquantity() > obj.getCountedquantity()) {
                        carryin = false;
                    } else if (obj.getIniquantity() < obj.getCountedquantity()) {
                        quantity = quantity * (-1);
                    }
//                     CompanyHandler.makeInventory(session,request,obj.getProduct(),quantity,"Cyclecount Diffrence",carryin,false,null);
                    JSONObject inventoryjson = new JSONObject();
                    inventoryjson.put("productid", obj.getProduct().getID());
                    inventoryjson.put("quantity", quantity);
                    inventoryjson.put("description", "Cyclecount Diffrence");
                    inventoryjson.put("carryin", carryin);
                    inventoryjson.put("defective", false);
                    inventoryjson.put("newinventory", false);
                    inventoryjson.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                }
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("approveCyclecountEntry : " + e.getMessage(), e);
        }
    }

    public ModelAndView exportCycleCount(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getCycleCountRequestMap(request);
            KwlReturnObject result = accProductObj.cycleCountReport(requestParams, true);
            JSONArray DataJArr = getCycleCountReportJson(request, result.getEntityList(), true);
            jobj.put("data", DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView exportCycleCountWorkSheet(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = getCycleCountWorkSheetRequestMap(request);
            KwlReturnObject result = accProductObj.getCycleCountWorkSheet(requestParams, true);
            jobj = getCycleCountWorkSheetJson(request, result.getEntityList(), true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    //Inventory
    public ModelAndView getInventory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("productid", request.getParameter("productid"));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("ss", request.getParameter("ss"));

            int count1 = 0, count2 = 0;
            List list1, list2;
            JSONArray DataJArr;

            KwlReturnObject result = accProductObj.getInventoryWOdetails(requestParams);
            list1 = result.getEntityList();
            count1 = result.getRecordTotalCount();
            Object[] res = getInventoryWOdetailsJson(list1);
            DataJArr = (JSONArray) res[0];
            int remQuantity = (Integer) res[1];

            result = accProductObj.getInventoryWithDetails(requestParams);
            list2 = result.getEntityList();
            count2 = result.getRecordTotalCount();
            DataJArr = getInventoryWithDetailsJson(request, list2, DataJArr, remQuantity);

            JSONArray pagedJSON = DataJArr;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJSON = StringUtil.getPagedJSON(pagedJSON, Integer.parseInt(start), Integer.parseInt(limit));
            }

            jobj.put("data", pagedJSON);
            jobj.put("count", count1 + count2);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private Object[] getInventoryWOdetailsJson(List list) throws ServiceException {
        int remQuantity = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
        JSONArray jArr = new JSONArray();
        try {
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Inventory inv = (Inventory) itr.next();
            if (inv.isCarryIn()) {
                remQuantity += inv.getQuantity();
            } else {
                remQuantity -= inv.getQuantity();
            }
            JSONObject obj = new JSONObject();
            obj.put("inventoryid", "");
            obj.put("productid", "");
            obj.put("productname", "");
            obj.put("quantity", inv.getQuantity());
            obj.put("remquantity", remQuantity);
            obj.put("date", sdf.format(inv.getUpdateDate()));
            obj.put("carryin", "Stock");
            obj.put("desc", "Stock exists.");
            obj.put("uom", inv.getProduct().getUnitOfMeasure().getName());
            jArr.put(obj);
        }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getInventoryWOdetailsJson : "+ex.getMessage(), ex);
        }
        return new Object[]{jArr, remQuantity};
    }

    private JSONArray getInventoryWithDetailsJson(HttpServletRequest request, List list, JSONArray jArr, int remQuantity) throws ServiceException, SessionExpiredException {
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
            	String carryIn = "";
                Object[] row = (Object[]) itr.next();
                Inventory inventory = (Inventory) row[0];
                JournalEntry je = (JournalEntry) row[1];
                JSONObject obj = new JSONObject();
                obj.put("inventoryid", inventory.getID());
                obj.put("productid", inventory.getProduct().getID());
                obj.put("productname", inventory.getProduct().getName());
                obj.put("quantity", inventory.getQuantity());
                if (inventory.isCarryIn()) {
                    remQuantity += inventory.getQuantity();
                } else {
                    remQuantity -= inventory.getQuantity();
                }
                obj.put("remquantity", remQuantity);
                obj.put("date", authHandler.getDateFormatter(request).format(je.getEntryDate()));
                if(!inventory.isCarryIn() && inventory.isDefective())
                	carryIn = "Debit";
                else if(inventory.isCarryIn() && inventory.isDefective())
                	carryIn = "Credit";
                obj.put("carryin", carryIn.equals("")?inventory.isCarryIn():carryIn);
                obj.put("desc", inventory.getDescription());
                obj.put("uom", inventory.getProduct().getUnitOfMeasure().getName());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getInventoryWithDetailsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public boolean isChildorGrandChild(String productid, String parentid) throws ServiceException{
    	try{
    		List Result = accProductObj.isChildorGrandChild(parentid);
    		Iterator iterator = Result.iterator();
    		if(iterator.hasNext()){
    			Object ResultObj = iterator.next();
    			Product ResultParentProduct = (Product) ResultObj;
    			ResultParentProduct = ResultParentProduct.getParent();
    			if(ResultParentProduct == null){
    				return false;
    			}	
    			else{
    				String Resultparent = ResultParentProduct.getID();
    				if(Resultparent.equals(productid))
    					return true;
    				else
    					return isChildorGrandChild(productid, Resultparent);
    			}
   	 		}
    	}catch(Exception ex){
   	       throw ServiceException.FAILURE("isChildorGrandChild : "+ex.getMessage(), ex);
    	}
    	return false;
    }
    
    public void updateBillofMaterialsInventory(HttpServletRequest request, Product assemblyProduct) throws ServiceException{
    	try{
    		String subproductid1;
    		int quantity1;
    		boolean flag = false;
    		KwlReturnObject originalAssembly;
    		KwlReturnObject result = accProductObj.getQuantity(assemblyProduct.getID());
    		if(result.getEntityList() != null){
	    		if(result.getEntityList().get(0) != null){	
		        	int qty = Integer.parseInt(result.getEntityList().get(0).toString());
		        	if(qty > 0){
		        		String jsondata = request.getParameter("assembly");
		        		JSONArray jarr = new JSONArray("[" + jsondata + "]");
		        		String[] subproductid = new String[jarr.length()];
		        		int[] quantity = new int[jarr.length()];
		        		for (int i = 0; i < jarr.length(); i++) {
		                    JSONObject jobj = jarr.getJSONObject(i);
		                    subproductid[i] = jobj.getString("product");
		                    quantity[i] = Integer.parseInt(jobj.getString("quantity"));
		        		}
		        		
		        		originalAssembly = accProductObj.getAssemblyProductBillofMaterials(assemblyProduct.getID());
		        		
		        		if(originalAssembly.getEntityList() != null){
			        		Iterator iterator = originalAssembly.getEntityList().iterator();
			        		while(iterator.hasNext()){
			        			Object ResultObj = iterator.next();
				        		ProductAssembly productAssembly = (ProductAssembly)ResultObj;
				        		subproductid1 = productAssembly.getSubproducts().getID();
				        		quantity1 = productAssembly.getQuantity();
				        		flag = false;
				        		for(int i = 0; i < subproductid.length; i++){
				        			if(subproductid1.equalsIgnoreCase(subproductid[i])){
				        				flag = true;
				        				if(quantity1 == quantity[i]){
				        					break;
				        				}else{
				        					if(quantity1 > quantity[i])
				    		        			addInventoryJson(request, subproductid1, (quantity1 - quantity[i]) * qty, true, "Rebuild quantity added after being removed from editing");
				    		        		else
				    		        			addInventoryJson(request, subproductid1, (quantity[i] - quantity1) * qty, false, "Rebuild quantity subtracted after being removed from editing");
				    		        	}
				        			}
				        		}
				        		if(!flag){
				        			addInventoryJson(request, subproductid1, quantity1 * qty, true, "Rebuild quantity added after being removed from editing");
				        		}
			        		}
		        		
			        		for(int i = 0; i < subproductid.length; i++){
				        		iterator = originalAssembly.getEntityList().iterator();
				        		flag = false;
				        		while(iterator.hasNext()){
				        			Object ResultObj = iterator.next();
					        		ProductAssembly productAssembly = (ProductAssembly)ResultObj;
					        		subproductid1 = productAssembly.getSubproducts().getID();
					        		if(subproductid[i].equalsIgnoreCase(subproductid1)){
				        				flag = true;
					        		}	
				        		}
				        		if(!flag)
				        			addInventoryJson(request, subproductid[i], quantity[i] * qty, false, "Rebuild quantity subtracted after being removed from editing");
			        		}
		        		}
		        	}
	    		}
    		}
    	}catch (Exception ex){
            throw ServiceException.FAILURE("updateBillofMaterialsInventory : " + ex.getMessage(), ex);
    	}
    }
    
    public void addInventoryJson(HttpServletRequest request, String productid, int quantity, boolean carryin, String description) throws ServiceException{
    	try{
    		JSONObject inventoryjson = new JSONObject();
            inventoryjson.put("productid", productid);
            inventoryjson.put("quantity", quantity);
            inventoryjson.put("description", description);
            inventoryjson.put("carryin", carryin);
            inventoryjson.put("defective", false);
            inventoryjson.put("newinventory", false);
            inventoryjson.put("companyid", sessionHandlerImpl.getCompanyid(request));
            accProductObj.addInventory(inventoryjson);
    	}catch (Exception ex){
    		throw ServiceException.FAILURE("addInventoryJson: "+ ex.getMessage(), ex);
    	}
    }
    
    public ModelAndView getProductIDAutoNumber(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
        	String autoNumber = accProductObj.getNextAutoProductIdNumber(sessionHandlerImpl.getCompanyid(request));
            issuccess = true;
            jobj.put("autoNumberID", autoNumber);
            msg = "Product ID generated successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView exportProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
        	HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);

            KwlReturnObject result = accProductObj.getProducts(requestParams);
            
            JSONArray DataJArr = productHandler.getProductsJson(request, result.getEntityList());
            jobj.put("data", DataJArr);
            
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
}
