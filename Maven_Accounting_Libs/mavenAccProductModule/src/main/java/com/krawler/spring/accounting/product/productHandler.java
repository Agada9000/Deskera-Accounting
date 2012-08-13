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

import com.krawler.common.service.ServiceException;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.ProductCyclecount;
import com.krawler.hql.accounting.Producttype;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public class productHandler {

    public static HashMap<String, Object> getProductRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("productid", request.getParameter("productid"));
        requestParams.put("transactiondate", request.getParameter("transactiondate"));
        requestParams.put("type", request.getParameter("type"));
        requestParams.put("ids",  request.getParameterValues("ids"));
        return requestParams;
    }

    public static JSONArray getProductsJson(HttpServletRequest request, List list) throws JSONException, ServiceException {
        Iterator itr = list.iterator();
        JSONArray jArr=new JSONArray();
        Producttype producttype = new Producttype();
        String productid = request.getParameter("productid");
        Boolean nonSaleInventory = Boolean.parseBoolean((String)request.getParameter("loadInventory"));
        while(itr.hasNext()) {
            Object[] row = (Object[]) itr.next();
            Product product = (Product) row[0];
            Product parentProduct = product.getParent();
            if(product.getID().equals(productid)) continue;
            ProductCyclecount pcObject = (ProductCyclecount) row[8];
            JSONObject obj = new JSONObject();
            obj.put("productid", product.getID());
            obj.put("productname", product.getName());
            obj.put("desc", product.getDescription());
            UnitOfMeasure uom = product.getUnitOfMeasure();
            obj.put("uomid", uom==null?"":uom.getID());
            obj.put("uomname", uom==null?"":uom.getName());
            obj.put("precision", uom==null?0:(Integer) uom.getAllowedPrecision());
            obj.put("leadtime", product.getLeadTimeInDays());
            obj.put("syncable", product.isSyncable());
            obj.put("reorderlevel", product.getReorderLevel());
            obj.put("reorderquantity", product.getReorderQuantity());
            obj.put("purchaseaccountid", (product.getPurchaseAccount()!=null?product.getPurchaseAccount().getID():""));
            obj.put("salesaccountid", (product.getSalesAccount()!=null?product.getSalesAccount().getID():""));
            obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount()!=null?product.getPurchaseReturnAccount().getID():""));
            obj.put("salesretaccountid", (product.getSalesReturnAccount()!=null?product.getSalesReturnAccount().getID():""));
            obj.put("vendor", (product.getVendor()!=null?product.getVendor().getID():""));
            obj.put("vendornameid", (product.getVendor()!=null?product.getVendor().getName():""));
            obj.put("producttype", (product.getProducttype()!=null?product.getProducttype().getID():""));
            obj.put("vendorphoneno", (product.getVendor()!=null?product.getVendor().getContactNumber():""));
            obj.put("vendoremail", (product.getVendor()!=null?product.getVendor().getEmail():""));
            obj.put("type", (product.getProducttype()!=null?product.getProducttype().getName():""));
            obj.put("pid",product.getProductid());
            obj.put("parentuuid", parentProduct==null?"":parentProduct.getID());
            obj.put("parentid", parentProduct==null?"":parentProduct.getProductid());
            obj.put("parentname", parentProduct==null?"":parentProduct.getName());
            obj.put("level", row[1]);
            obj.put("leaf",row[2]);
            obj.put("purchaseprice", row[3]);
            obj.put("saleprice",row[4]);
            obj.put("quantity",(row[5]==null?0:row[5]));
            obj.put("initialquantity",(row[6]==null?0:row[6]));
            obj.put("initialprice",(row[7]==null?0:row[7]));
            obj.put("salespricedatewise",(row[9]==null?0:row[9]));
            obj.put("purchasepricedatewise",(row[10]==null?0:row[10]));
            obj.put("initialsalesprice",(row[11]==null?0:row[11]));
            obj.put("ccountinterval",pcObject!=null?pcObject.getCountInterval():"");
            obj.put("ccounttolerance",pcObject!=null?pcObject.getTolerance():"");
            SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
         //  obj.put("createdon", (row[12]==null?"":sdf.format(row[12])));
//            jArr.put(obj);
            if(nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)){
            	// Do Nothing
            }else{
                jArr.put(obj);
            }
        }
        return jArr;
    }

    public static JSONArray getProductTypesJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Producttype ptype = (Producttype) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", ptype.getID());
                obj.put("name", ptype.getName());
                jArr.put(obj);
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getProductTypesJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }


}
