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
package com.krawler.spring.accounting.debitnote;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface accDebitNoteDAO {
    public KwlReturnObject addDebitNote(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject updateDebitNote(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject deleteDebitNote(String dnid, String companyid) throws ServiceException;
    public KwlReturnObject getDebitNoteDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBillingDebitNoteDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getDebitNotes(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getDNFromNoteNo(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject getDNSequenceNo(String companyid, Date applydate) throws ServiceException;
    public KwlReturnObject getDNFromGReceipt(String receiptid) throws ServiceException;
    public KwlReturnObject getDNDetailsFromGReceipt(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject getDNFromJE(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getJEFromDN(String dnid) throws ServiceException;
    public KwlReturnObject getDNDFromDN(String dnid) throws ServiceException;
    public KwlReturnObject getDNDIFromDN(String dnid) throws ServiceException;
    public KwlReturnObject getDNDInvFromDN(String dnid) throws ServiceException;
    public KwlReturnObject getDNRFromBDN(String receiptid) throws ServiceException;
    public KwlReturnObject getBDNDetailsFromGReceipt(String receiptid, String companyid) throws ServiceException;
    public KwlReturnObject getBDNFromNoteNo(String noteno, String companyid) throws ServiceException;
    public KwlReturnObject getBDNSequenceNo(String companyid, Date applydate) throws ServiceException;
    public KwlReturnObject saveBillingDebitNote(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getBillingDebitNotes(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject deleteBillingDebitNote(String bdnid, String companyid) throws ServiceException;
    public KwlReturnObject getJEFromBDN(String dnid, String companyid) throws ServiceException;
    public KwlReturnObject getDNDFromBDN(String dnid, String companyid) throws ServiceException;
    public KwlReturnObject getDNDFromBDND(String bdnid, String companyid) throws ServiceException;
    
    public KwlReturnObject getTotalDiscount(String receiptid) throws ServiceException;
    public KwlReturnObject getTotalQty(String invId) throws ServiceException;
}
