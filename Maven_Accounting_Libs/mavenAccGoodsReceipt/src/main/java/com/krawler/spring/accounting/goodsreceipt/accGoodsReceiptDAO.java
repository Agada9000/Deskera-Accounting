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

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accGoodsReceiptDAO {
    public KwlReturnObject addGoodsReceipt(Map<String, Object> hm) throws ServiceException;
    public KwlReturnObject deleteBillingGoodsReceiptDetails(String invoiceid, String companyid)throws ServiceException;
    public KwlReturnObject getCalculatedExpenseGRDtlTax(Map<String, Object> filterParams)throws ServiceException;
    public KwlReturnObject getCalculatedGRDtlTax(Map<String, Object> filterParams)throws ServiceException;
    public KwlReturnObject getCalculatedGRTax(Map<String, Object> filterParams)throws ServiceException;
    public KwlReturnObject getExpenseGRDetails(HashMap<String, Object> grRequestParams)throws ServiceException;
    public KwlReturnObject updateGoodsReceipt(Map<String, Object> hm) throws ServiceException;
    public KwlReturnObject getGoodsReceipts(Map<String, Object> request) throws ServiceException;
    public KwlReturnObject deleteGoodsReceipts(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject deleteGoodsReceiptDetails(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject getReceiptFromNo(String receiptno, String companyid) throws ServiceException;
    public KwlReturnObject getReceiptDFromPOD(String podid) throws ServiceException;
    public KwlReturnObject getGRFromJE(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getJEFromGR(String greceiptid, String companyid)throws ServiceException;
    public KwlReturnObject getGoodsReceiptData (Map requestParam) throws ServiceException;
    public KwlReturnObject getGoodsReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBillingGoodsReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteGoodsReceiptEntry(String grid, String companyid) throws ServiceException;
    public KwlReturnObject getGRJournalEntry(String grid) throws ServiceException;
    public KwlReturnObject getGRDiscount(String grid) throws ServiceException;
    public KwlReturnObject getGRDetailsDiscount(String grid) throws ServiceException;
    public KwlReturnObject getGRInventory(String grid) throws ServiceException;
    public KwlReturnObject getBRDFromBPOD(String podid) throws ServiceException;
    public KwlReturnObject getBillingGoodsReceipt(Map requestParam) throws ServiceException;
    public KwlReturnObject saveBillingGoodsReceipt(Map<String, Object> hm) throws ServiceException;
    public KwlReturnObject getBillingGoodsReceiptsData(Map<String, Object> request) throws ServiceException;
    public KwlReturnObject getAmtromBPD(String receiptId) throws ServiceException;
    public KwlReturnObject deleteBillingGoodsReceiptEntry(String grid, String companyid) throws ServiceException;
    public KwlReturnObject getFromBGR(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getBGRDiscount(String bgrid, String companyid) throws ServiceException;
    public KwlReturnObject getBGRDetailsDiscount(String bgrid, String companyid) throws ServiceException;
    
    public KwlReturnObject getQtyandUnitCost(String productid, Date endDate) throws ServiceException;

    public KwlReturnObject getGoodsReceipt_Product(String productid, String companyid) throws ServiceException;
    
    public KwlReturnObject getGoodsReceipt_Rate(String inventoryid) throws ServiceException;
    
    public KwlReturnObject getGR_ProductTaxPercent(String inventoryid) throws ServiceException;
}
