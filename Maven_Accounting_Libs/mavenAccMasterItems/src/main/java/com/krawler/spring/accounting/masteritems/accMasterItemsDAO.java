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
package com.krawler.spring.accounting.masteritems;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface accMasterItemsDAO {
    public KwlReturnObject addMasterItem(HashMap<String, Object> mitemmap) throws ServiceException;
    public KwlReturnObject updateMasterItem(HashMap<String, Object> itemmap) throws ServiceException;
    public KwlReturnObject daleteMasterItem(String itemid) throws ServiceException;
//    public KwlReturnObject getMasterItems(String groupid, String companyid) throws ServiceException;
    public KwlReturnObject getMasterItems(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject addMasterGroup(HashMap<String, Object> groupmap) throws ServiceException;
    public KwlReturnObject updateMasterGroup(HashMap<String, Object> groupmap) throws ServiceException;
    public KwlReturnObject getMasterGroups() throws ServiceException;
    public KwlReturnObject deleteMasterGroup(String groupid) throws ServiceException;

    public void copyMasterItems(String companyid) throws ServiceException;
}
