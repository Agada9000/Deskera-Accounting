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
package com.krawler.spring.accounting.receipt;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface accReceiptDAO {

//    public KwlReturnObject getReceiptObj(String receiptid);
    public KwlReturnObject saveReceipt(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getReceipts(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getReceiptFromBillNo(String billno, String companyid) throws ServiceException;
    public KwlReturnObject getReceiptFromInvoice(String invoiceid, String companyid) throws ServiceException;
    public KwlReturnObject getReceiptAmountFromInvoice(String invoiceid) throws ServiceException;
    public KwlReturnObject deleteReceiptDetails(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject deleteReceipt(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject getReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getBillingReceiptDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteReceiptEntry(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject getJEFromReceipt(String receiptid) throws ServiceException;
//    public KwlReturnObject getBillingReceiptObj(String receiptid);
    public KwlReturnObject saveBillingReceipt(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getBillingReceipts(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getBillingReceiptFromBillNo(String billno, String companyid) throws ServiceException;
    public KwlReturnObject getBillingReceiptAmountFromInvoice(String invoiceid) throws ServiceException;
    public KwlReturnObject deleteBillingReceiptDetails(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject deleteBillingReceipt(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject deleteBillingReceiptEntry(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject getBReceiptFromBInvoice(String invoiceid, String companyid) throws ServiceException;

    public KwlReturnObject getReciptFromJE(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getBReciptFromJE(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getJEFromBR(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject getBillingReceiptDetail(String receiptid, String companyid) throws ServiceException;
}
