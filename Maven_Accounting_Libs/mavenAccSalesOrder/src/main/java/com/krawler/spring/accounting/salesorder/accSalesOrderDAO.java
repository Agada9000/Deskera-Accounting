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

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface accSalesOrderDAO {
// Sales Order
    public KwlReturnObject getSalesOrders(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getSalesOrderCount(String orderno, String companyid) throws ServiceException;
    public KwlReturnObject deleteSalesOrder(String soid, String companyid) throws ServiceException;
    public KwlReturnObject saveSalesOrder(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject saveSalesOrderDetails(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getSO_Product(String productid, String companyid) throws ServiceException;
    public KwlReturnObject getSalesOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;

// Billing Sales Order
    public KwlReturnObject getBillingSalesOrderDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getBillingSalesOrderCount(String orderno, String companyid) throws ServiceException;
    public KwlReturnObject saveBillingSalesOrder(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject saveBillingSalesOrderDetails(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getBillingSalesOrders(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject deleteBillingSalesOrder(String soid, String companyid) throws ServiceException;
	public KwlReturnObject deleteSalesOrderDetails(String soid, String companyid) throws ServiceException;
	public KwlReturnObject deleteBillingSalesOrderDetails(String soid, String companyid) throws ServiceException;
	
// Quotation
    public KwlReturnObject saveQuotationDetails(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject saveQuotation(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getQuotations(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getQuotationDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteQuotation(String qid, String companyid) throws ServiceException;

}
