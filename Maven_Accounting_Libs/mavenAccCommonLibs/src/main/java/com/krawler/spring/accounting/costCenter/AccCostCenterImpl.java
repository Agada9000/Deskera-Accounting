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

package com.krawler.spring.accounting.costCenter;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
public class AccCostCenterImpl implements AccCostCenterDAO {
    private HibernateTemplate hibernateTemplate;
    public void setSessionFactory(SessionFactory sessionFactory){
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public KwlReturnObject getCostCenter(HashMap<String, Object> requestParams) throws ServiceException {
		String query = "from CostCenter";
        return StringUtil.buildNExecuteQuery(hibernateTemplate, query, requestParams);
    }

    public Object saveCostCenter(HashMap<String, Object> dataMap) throws ServiceException{
        return HibernateUtil.setterMethod(hibernateTemplate, dataMap, "com.krawler.common.admin.CostCenter", "ID");
    }

    public KwlReturnObject deleteCostCenter(String ccid, String companyid) throws ServiceException {
        String delQuery = "delete from CostCenter cc where cc.ID=? and cc.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery,new Object[]{ccid, companyid});
        return new KwlReturnObject(true, "Cost Center has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject checkUniqueCostCenter(String id, String ccid, String name, String companyid) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition="";
        String query="from CostCenter where company.companyID=? and (ccid=? or name=?)";
        params.add(companyid);
        params.add(ccid);
        params.add(name);

        if(!StringUtil.isNullOrEmpty(id)){
            condition=" and id!=?";
            params.add(id);
        }
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query+condition, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }


}
