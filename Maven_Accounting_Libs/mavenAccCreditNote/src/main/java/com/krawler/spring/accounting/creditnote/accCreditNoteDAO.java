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
package com.krawler.spring.accounting.creditnote;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface accCreditNoteDAO {
    public KwlReturnObject addCreditNote(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject updateCreditNote(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject deleteCreditNote(String cnid, String companyid) throws ServiceException;

    public KwlReturnObject getCreaditNote(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getCNFromNoteNo(String noteno, String companyid) throws ServiceException;
    public KwlReturnObject getCNSequenceNo(String companyid, Date applydate) throws ServiceException;

    public KwlReturnObject getCreditNoteDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getCNFromInvoice(String invoiceid, String companyid) throws ServiceException;
    public KwlReturnObject getCNRowsDiscountFromInvoice(String invoiceid) throws ServiceException;
    public KwlReturnObject getCNFromJE(String jeid, String companyid) throws ServiceException;
    public KwlReturnObject getJEFromCN(String cnid) throws ServiceException;
    public KwlReturnObject getCNDFromCN(String cnid) throws ServiceException;
    public KwlReturnObject getCNDFromCND(String cnid) throws ServiceException;
    public KwlReturnObject getCNIFromCND(String cnid) throws ServiceException;
    public KwlReturnObject getBillingCreditNoteDet(String bInvid, String companyid) throws ServiceException;


    
    public KwlReturnObject getBillingCreditNoteDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getBCNFromNoteNo(String noteno, String companyid) throws ServiceException;
    public KwlReturnObject getBCNSequenceNo(String companyid, Date applydate) throws ServiceException;
    public KwlReturnObject saveBillingCreditNote(HashMap<String, Object> hm) throws ServiceException;
    public KwlReturnObject getBillingCreaditNote(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getJEFromBCN(String cnid) throws ServiceException;
    public KwlReturnObject getCNDFromBCN(String cnid) throws ServiceException;
    public KwlReturnObject getCNDFromBCND(String cnid) throws ServiceException;
    public KwlReturnObject deleteBillingCreditNote(String cnid, String companyid) throws ServiceException;
    public KwlReturnObject getCNRowsDiscountFromBillingInvoice(String invoiceid) throws ServiceException;
}
