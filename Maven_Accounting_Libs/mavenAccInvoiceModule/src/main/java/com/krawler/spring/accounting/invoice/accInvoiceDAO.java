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
package com.krawler.spring.accounting.invoice;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accInvoiceDAO {

    public KwlReturnObject getBillingInvoiceDetailsDiscount(String invoiceid) throws ServiceException;
    KwlReturnObject getCalculatedInvDtlTax(Map<String, Object> filterParams) throws ServiceException;
    public KwlReturnObject saveBillingInvoice(HashMap<String, Object> dataMap) throws ServiceException;
//    public KwlReturnObject getInvoiceObj(String invoiceid);
    public KwlReturnObject saveInvoice(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject addInvoice(JSONObject json, HashSet details) throws ServiceException;
    public KwlReturnObject updateInvoice(JSONObject json, HashSet details) throws ServiceException;
    public KwlReturnObject deleteInvoice(String invoiceid, String companyid) throws ServiceException;
    public KwlReturnObject deleteInvoiceDtails(String invoiceid, String companyid) throws ServiceException;
    public KwlReturnObject getInvoices(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getInvoiceCount(String invoiceno, String companyid) throws ServiceException;
    public KwlReturnObject deleteInvoiceEntry(String invoiceid, String companyid) throws ServiceException;
    public KwlReturnObject getJEFromInvoice(String receiptid) throws ServiceException;
    public KwlReturnObject getInvoiceDiscount(String invoiceid) throws ServiceException;
    public KwlReturnObject getInvoiceDetailsDiscount(String invoiceid) throws ServiceException;
    public KwlReturnObject getInvoiceInventory(String invoiceid) throws ServiceException;
    public KwlReturnObject getRepeateInvoices(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getRepeateBillingInvoices(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject saveRepeateInvoiceInfo(HashMap<String, Object> dataMap) throws ServiceException;
    public KwlReturnObject getRepeateInvoicesDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getRepeateBillingInvoicesDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getInvoiceDetails(HashMap<String, Object> requestParams) throws ServiceException;
    KwlReturnObject getCalculatedInvTax(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getInvoiceProductDetails(String productid, Date fromDate,Date toDate) throws ServiceException ;

    //    public KwlReturnObject getBillingInvoiceObj(String invoiceid);
    public KwlReturnObject addBillingInvoice(JSONObject json, HashSet details) throws ServiceException;
    public KwlReturnObject updateBillingInvoice(JSONObject json, HashSet details) throws ServiceException;
    public KwlReturnObject getBillingInvoices(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getBillingInvoiceCount(String invoiceno, String companyid) throws ServiceException;
    public KwlReturnObject deleteBillingInvoice(String invoiceid, String companyid) throws ServiceException;
    public KwlReturnObject deleteBillingInvoiceDtails(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getBillingInvoiceDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getIDFromSOD(String soid) throws ServiceException;
    public KwlReturnObject getBIDFromBSOD(String bsoid) throws ServiceException;
    public KwlReturnObject getInvoiceFromJE(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getBIFromJE(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject deleteBillingInvoiceEntry(String invoiceid, String companyid) throws ServiceException;
    public KwlReturnObject getJEFromBI(String invid, String companyid) throws ServiceException;
    public KwlReturnObject getDisIdFromBIDet(String invid, String companyid) throws ServiceException;

    public KwlReturnObject getInvoice_Product(String productid, String companyid) throws ServiceException;
}
