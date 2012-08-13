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

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface accPurchaseOrderDAO {
    public KwlReturnObject addPurchaseOrder(HashMap hm) throws ServiceException;
    public KwlReturnObject updatePurchaseOrder(HashMap hm) throws ServiceException;
    public KwlReturnObject deletePurchaseOrder(String poid, String companyid) throws ServiceException;
    public KwlReturnObject getPurchaseOrders(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getPOCount(String pono, String companyid) throws ServiceException;
    public KwlReturnObject savePODetails(JSONArray podjarr, String poid, String companyid, boolean issave) throws ServiceException;
    public KwlReturnObject savePurchaseOrder(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject savePurchaseOrderDetails(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getPO_Product(String productid, String companyid) throws ServiceException;
    public KwlReturnObject getPurchaseOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;


    public KwlReturnObject getBillingPurchaseOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getBPOCount(String pono, String companyid) throws ServiceException;
    public KwlReturnObject saveBillingPurchaseOrder(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject saveBillingPurchaseOrderDetails(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getBillingPurchaseOrders(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject deleteBillingPurchaseOrder(String poid, String companyid) throws ServiceException;
    
    public KwlReturnObject deletePurchaseOrderDetails(String poid, String companyid) throws ServiceException;
    public KwlReturnObject deleteBillingPurchaseOrderDetails(String poid, String companyid) throws ServiceException;
}
