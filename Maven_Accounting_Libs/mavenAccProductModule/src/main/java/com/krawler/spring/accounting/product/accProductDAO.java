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
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 *
 * @author krawler
 */
public interface accProductDAO {
    //Product
    public KwlReturnObject addProduct(HashMap<String, Object> productMap) throws ServiceException;

    public KwlReturnObject deleteCyclecountPermanently(String productid, String companyid) throws ServiceException;

    public KwlReturnObject deleteInventoryByProduct(String productid, String companyid) throws ServiceException;
    public KwlReturnObject deleteProPricePermanently(String productid, String companyid) throws ServiceException;
    public KwlReturnObject deleteProductCycleCountPermanently(String productid, String companyid) throws ServiceException;
    public KwlReturnObject deleteProductPermanently(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getProductByID(String productid, String companyid) throws ServiceException;

    KwlReturnObject getProductList(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject updateProduct(HashMap<String, Object> productMap) throws ServiceException;
    public KwlReturnObject deleteProduct(String productid) throws ServiceException;
    public KwlReturnObject getProducts(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getProductfromAccount(String accountid, String companyid) throws ServiceException;
    public KwlReturnObject getProValuation(HashMap requestParam) throws ServiceException;
    public KwlReturnObject getSuggestedReorderProducts(HashMap<String, Object> requestParams) throws ServiceException;

    //Product Types
    public KwlReturnObject saveProductTypes(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getProductTypes(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteProductType(String typeid) throws ServiceException;
    public KwlReturnObject getProductsByType(HashMap<String, Object> requestParams) throws ServiceException;

    //Product Assembly
    public KwlReturnObject saveProductAssembly(HashMap<String, Object> assemblyMap) throws ServiceException;
    public KwlReturnObject deleteProductAssembly(String parentProductId) throws ServiceException;
    public KwlReturnObject deleteSubProductFromAssembly(String subProductId) throws ServiceException;
    public void updateAssemblyInventory(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getAssemblyItems(HashMap<String, Object> requestParams) throws ServiceException;

    
    //PriceList
    public KwlReturnObject addPriceList(HashMap<String, Object> priceMap) throws ServiceException;
    public KwlReturnObject updatePriceList(HashMap<String, Object> priceMap) throws ServiceException;
    public KwlReturnObject getProductPrice(String productid, boolean isPurchase, Date transactiondate) throws ServiceException;
    public KwlReturnObject getInitialPrice(String productid, boolean isPurchase) throws ServiceException;
    public KwlReturnObject getPrice(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getPriceListEntry(HashMap<String, Object> filterParams) throws ServiceException;
    public KwlReturnObject getAvgcostAssemblyProduct(String productid, Date stDate, Date endDate) throws ServiceException;

    //Cycle Count
    public KwlReturnObject getCycleCountForApproval(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getCycleCountWorkSheet(HashMap<String, Object> requestParams, boolean isExpotrt) throws ServiceException;
    public KwlReturnObject saveCycleCount(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getCycleCountProduct(HashMap<String, Object> filterParams) throws ServiceException;
    public KwlReturnObject cycleCountReport(HashMap<String, Object> filterParams, boolean isExport) throws ServiceException;
    public KwlReturnObject getCycleCountEntry(HashMap<String, Object> filterParams) throws ServiceException;
    public KwlReturnObject saveProductCycleCount(HashMap<String, Object> dataMap) throws ServiceException;

    //Inventory
    public KwlReturnObject addInventory(JSONObject json) throws ServiceException;
    public KwlReturnObject updateInventory(JSONObject json) throws ServiceException;
    public KwlReturnObject deleteInventory(String inventoryid, String companyid) throws ServiceException;
    public KwlReturnObject deleteInventoryEntry(String inventoryid, String companyid) throws ServiceException;
    public KwlReturnObject getInventoryEntry(HashMap<String, Object> filterParams) throws ServiceException;
    public KwlReturnObject getQuantity(String productid) throws ServiceException;
    public KwlReturnObject getInitialQuantity(String productid) throws ServiceException;

    KwlReturnObject getQuantityPurchaseOrSalesDetails(String productid, boolean isPurchase) throws ServiceException;
    public KwlReturnObject getQuantityPurchaseOrSales(String productid, boolean isPurchase) throws ServiceException;
    public KwlReturnObject getInventoryWOdetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getInventoryWithDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getInventoryOpeningBalanceDate(String companyid) throws ServiceException;

    public KwlReturnObject getInitialCost(String productid) throws ServiceException;
    public boolean isChild(String ParentID, String childID) throws ServiceException;
    public List isChildorGrandChild(String childID) throws ServiceException;
    
    public KwlReturnObject selectCyclecountPermanently(String productid, String companyid) throws ServiceException;
    public KwlReturnObject selectInventoryByProduct(String productid, String companyid) throws ServiceException;
    public KwlReturnObject selectProPricePermanently(String productid, String companyid) throws ServiceException;
    public KwlReturnObject selectProductCycleCountPermanently(String productid, String companyid) throws ServiceException;
    public KwlReturnObject selectSubProductFromAssembly(String subProductId) throws ServiceException;
    
    public List searchInventoryId(String productid,boolean flag) throws ServiceException;
    public KwlReturnObject updateInitialInventory(JSONObject json) throws ServiceException;
    
    public KwlReturnObject getQtyandUnitCost(String productid, Date endDate) throws ServiceException;
    public KwlReturnObject getAssemblyProductBillofMaterials(String productid) throws ServiceException;
    
    public String getNextAutoProductIdNumber(String companyid) throws ServiceException;
    
    public KwlReturnObject getSoldorPurchaseQty(String productid, boolean sold) throws ServiceException;
    public KwlReturnObject getRateandQtyfromInvoice(String productid) throws ServiceException;
    
    public KwlReturnObject checkSubProductforAssembly(String productid) throws ServiceException;
    public KwlReturnObject checkIfParentProduct(String productid) throws ServiceException;
}
