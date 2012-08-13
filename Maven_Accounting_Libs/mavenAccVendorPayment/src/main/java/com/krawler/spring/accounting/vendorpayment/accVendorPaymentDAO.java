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
package com.krawler.spring.accounting.vendorpayment;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface accVendorPaymentDAO {

    public KwlReturnObject deleteBillingPaymentsDetails(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject getBillingPaymentVendorNames(String companyid, String iD) throws ServiceException;
    public KwlReturnObject getPaymentVendorNames(String companyid, String iD) throws ServiceException;
    public KwlReturnObject savePayment(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getPayments(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject deletePayments(String paymentid, String companyid) throws ServiceException;
    public KwlReturnObject deletePaymentsDetails(String paymentid, String companyid) throws ServiceException;

    public KwlReturnObject deletePaymentEntry(String paymentid, String companyid) throws ServiceException;
    public KwlReturnObject getJEFromPayment(String paymentid) throws ServiceException;
//    public KwlReturnObject getPDFromGReceipt(String receiptid) throws ServiceException;
    public KwlReturnObject getPaymentsFromGReceipt(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject getPaymentFromNo(String pno, String companyid) throws ServiceException;
    public KwlReturnObject getPaymentDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getBillingPaymentDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPaymentFromJE(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getBillingPaymentsFromGReceipt(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject getBillingPaymentFromNo(String pno, String companyid) throws ServiceException;
    public KwlReturnObject saveBillingPayment(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getBillingPayments(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject deleteBillingPaymentEntry(String paymentid, String companyid) throws ServiceException;
    public KwlReturnObject getJEFromBillingPayment(String paymentid, String companyid) throws ServiceException;
}
