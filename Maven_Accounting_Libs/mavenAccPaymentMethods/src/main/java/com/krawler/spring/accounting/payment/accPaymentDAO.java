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
package com.krawler.spring.accounting.payment;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface accPaymentDAO {
    public KwlReturnObject addPaymentMethod(HashMap<String, Object> pmMap) throws ServiceException;
    public KwlReturnObject deleteCard(String iD, String companyid) throws ServiceException;
    public KwlReturnObject deleteCheque(String iD, String companyid)throws ServiceException;
    public KwlReturnObject updatePaymentMethod(HashMap<String, Object> pmMap) throws ServiceException;
    public KwlReturnObject deletePaymentMethod(String methodid, String companyid) throws ServiceException;
    public KwlReturnObject getPaymentMethod(HashMap<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject addPayDetail(HashMap hm) throws ServiceException;
    public KwlReturnObject updatePayDetail(HashMap hm) throws ServiceException;

    public KwlReturnObject addCheque(HashMap hm) throws ServiceException;
    public KwlReturnObject updateCheque(HashMap hm) throws ServiceException;

    public KwlReturnObject addCard(HashMap hm) throws ServiceException;
    public KwlReturnObject updateCard(HashMap hm) throws ServiceException;

    public KwlReturnObject getPaymentMethodFromAccount(String accountid, String companyid) throws ServiceException;
    public void copyPaymentMethods(String companyid, HashMap hm) throws ServiceException;
}
