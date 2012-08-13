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
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.ProductAssembly;
import com.krawler.hql.accounting.Producttype;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
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
public class accProductControllerCMN extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accProductDAO accProductObj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accGoodsReceiptDAO accGoodsReceiptDAOobj;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setAccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptDAOobj) {
		this.accGoodsReceiptDAOobj = accGoodsReceiptDAOobj;
	}
	public void setAccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
		this.accInvoiceDAOobj = accInvoiceDAOobj;
	}
	public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
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


    public ModelAndView deleteProducts(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ProductCMN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteProducts(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.prod.del", null, RequestContextUtils.getLocale(request));   //"Product has been deleted successfully";
        } catch (AccountingException acc) {
            txnManager.rollback(status);
            msg = acc.getMessage();
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteProducts(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String ids[] = request.getParameterValues("ids");
            String prodNames = "";
            String productid = "";
            boolean unBuild = Boolean.parseBoolean(request.getParameter("unBuild"));
            for (int i = 0; i < ids.length; i++) {
                if (!StringUtil.isNullOrEmpty(ids[i])) {
                    productid = ids[i];
//                    selQuery = "from PurchaseOrderDetail pod where product.ID in( " + qMarks + ") and pod.company.companyID=?";
//                    List list1 = HibernateUtil.executeQuery(session, selQuery, params1.toArray());
                    KwlReturnObject result = accPurchaseOrderobj.getPO_Product(productid, companyid);
                    List list1 = result.getEntityList();
                    int count1 = list1.size();
//                    if (count1 > 0) {
//                        throw new AccountingException("Selected record(s) is currently used in the Purchase Order(s). So it cannot be deleted.");
//                    }

//                    selQuery = "from SalesOrderDetail sod where product.ID in( " + qMarks + ") and sod.company.companyID=?";
//                    List list2 = HibernateUtil.executeQuery(session, selQuery, params1.toArray());
                    result = accSalesOrderDAOobj.getSO_Product(productid, companyid);
                    List list2 = result.getEntityList();
                    int count2 = list2.size();
//                    if (count2 > 0) {
//                        throw new AccountingException("Selected record(s) is currently used in the Sales Order(s). So it cannot be deleted.");
//                    }

                     result = accGoodsReceiptDAOobj.getGoodsReceipt_Product(productid, companyid);
                    List list3 = result.getEntityList();
                    int count3 = list3.size();
                    
                     result = accInvoiceDAOobj.getInvoice_Product(productid, companyid);
                    List list4 = result.getEntityList();
                    int count4 = list4.size();
                    
                    result = accProductObj.checkSubProductforAssembly(productid);
                    List list5 = result.getEntityList();
                    int count5 = list5.size();

                    if (count1 > 0 || count2 > 0 || count3 > 0 || count4 > 0) {
                    	throw new AccountingException(messageSource.getMessage("acc.acc.excp1", null, RequestContextUtils.getLocale(request)));   //"Selected record(s) is currently used in the Transaction(s). So it cannot be deleted.");
                    }
                    
                    if(count5 > 0){
                    	throw new AccountingException("Selected Product(s)/Service(s) is currently used in some Assembly Product, so it cannot be deleted. To delete it, first remove the Product(s)/Service(s) from the Bill of Material of Assembly Product"); 
                    }
                    
                    
                    
                    KwlReturnObject rtObj = accProductObj.getProductByID(productid, companyid);
                    Product prd = ((Product) rtObj.getEntityList().get(0));
                    prodNames += ", " + prd.getName();
                    //Delete product from All Assemblies
                    
                    KwlReturnObject kwlReturnObject_SPA = accProductObj.selectSubProductFromAssembly(productid);
                    KwlReturnObject kwlReturnObject_I = accProductObj.selectInventoryByProduct(productid, companyid);
//                    KwlReturnObject kwlReturnObject_PP = accProductObj.selectProPricePermanently(productid, companyid);
                    KwlReturnObject kwlReturnObject_CC = accProductObj.selectCyclecountPermanently(productid, companyid);
//                    KwlReturnObject kwlReturnObject_PCC = accProductObj.selectProductCycleCountPermanently(productid, companyid);
                    
                    result = accProductObj.checkIfParentProduct(productid);
                    int count6 = result.getEntityList().size();
                    
                    
                    if(count1 > 0 || count2 > 0 || count6 > 0 || kwlReturnObject_SPA.getRecordTotalCount() > 0 || kwlReturnObject_I.getRecordTotalCount() > 0 || kwlReturnObject_CC.getRecordTotalCount() > 0)
                    {
                    	accProductObj.deleteProduct(productid);
                    }
                    else{
                    	accProductObj.deleteProductCycleCountPermanently(productid, companyid);
                    	accProductObj.deleteProPricePermanently(productid, companyid);
                    	if(prd.getProducttype().getID().equals(Producttype.ASSEMBLY)){
                    		accProductObj.deleteProductAssembly(productid);
                    	}	
                    		accProductObj.deleteProductPermanently(productid, companyid);
                    }
                    if(prd.getProducttype().getID().equals(Producttype.ASSEMBLY) && unBuild == true){
                    	RestockBillofMaterialsinInventory(productid, companyid);                    	
                    }
//                    accProductObj.deleteSubProductFromAssembly(productid);
//                    accProductObj.deleteInventoryByProduct(productid, companyid);
//                    accProductObj.deleteProPricePermanently(productid, companyid);
//                    accProductObj.deleteCyclecountPermanently(productid, companyid);
//                    accProductObj.deleteProductCycleCountPermanently(productid, companyid);
//                    accProductObj.deleteProductPermanently(productid, companyid);
//   Done by Neeraj                 
                    
                    
           //         KwlReturnObject rtObj = accProductObj.deleteProduct(productid);
         //           Product prd = ((Product) rtObj.getEntityList().get(0));
        //            prodNames += ", " + prd.getName();
                }
                if(prodNames.length()>0)
                    auditTrailObj.insertAuditLog(AuditAction.PRODUCT_DELETION, "User "+ sessionHandlerImpl.getUserFullName(request) +" deleted products "+prodNames.substring(1), request, "0");
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.acc.excp1", null, RequestContextUtils.getLocale(request)), ex);
        }
    }

    public ModelAndView editQuantity(HttpServletRequest request, HttpServletResponse response){
    	JSONObject jobj=new JSONObject();
    	boolean issuccess = false;
        String msg = "";
        try{
        	String productid = request.getParameter("productid");
        	String companyid = sessionHandlerImpl.getCompanyid(request);
        	List iCount = accProductObj.searchInventoryId(productid, false);
        	int invCount = iCount.size();
        	KwlReturnObject Count = accSalesOrderDAOobj.getSO_Product(productid, companyid);
            int SOCount = Count.getRecordTotalCount();
            if(invCount == 0 && SOCount == 0){
            	jobj.put("quantityEdit", true);
            	msg = "Product's initial quantity can be edited ";
            }else{
            	jobj.put("quantityEdit", false);
            	msg = "Product's initial quantity cannot be edited as it is used in transactions already";
            }
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    	return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void RestockBillofMaterialsinInventory(String productid, String companyid){
    	try{
	    	KwlReturnObject result1 = accProductObj.getQuantity(productid);
	    	if(result1.getEntityList() != null){
	        	int qty = Integer.parseInt(result1.getEntityList().get(0).toString());
				if(qty > 0){
					KwlReturnObject result2 = accProductObj.getAssemblyProductBillofMaterials(productid);
	        		if(result2.getEntityList() != null){
	        			Iterator<List> res2 = result2.getEntityList().iterator();
	        			while(res2.hasNext()){
	        				ProductAssembly productAssembly = (ProductAssembly) res2.next();
	        				try {
	            				JSONObject inventoryjson = new JSONObject();
	                            inventoryjson.put("productid", productAssembly.getSubproducts().getID());
	                            inventoryjson.put("quantity", qty * productAssembly.getQuantity());
	                            inventoryjson.put("description", "unBuild or Disassemble Product Assembly for "+productAssembly.getProduct().getName());
	                            inventoryjson.put("carryin", true);
	                            inventoryjson.put("defective", false);
	                            inventoryjson.put("newinventory", false);
								inventoryjson.put("companyid", companyid);
	                            accProductObj.addInventory(inventoryjson);
							} catch (JSONException e) {
								e.printStackTrace();
							}
	        			}
	        		}
	        		try {
        				JSONObject inventoryjson = new JSONObject();
                        inventoryjson.put("productid", productid);
                        inventoryjson.put("quantity", qty);
                        inventoryjson.put("description", "Inventory removed after deletion");
                        inventoryjson.put("carryin", false);
                        inventoryjson.put("defective", false);
                        inventoryjson.put("newinventory", false);
						inventoryjson.put("companyid", companyid);
                        accProductObj.addInventory(inventoryjson);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
	    	}
    	}catch (Exception ex){
            Logger.getLogger(accProductControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
    	}
    }
    
}
