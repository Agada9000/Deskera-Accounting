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
package com.krawler.spring.accounting.uom;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.DefaultUnitOfMeasure;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
public class accUomImpl implements accUomDAO {
    private HibernateTemplate hibernateTemplate;
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

    public KwlReturnObject getUnitOfMeasure(HashMap<String, Object> filterParams) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition = "";
        String query="from UnitOfMeasure ";

        if(filterParams.containsKey("companyid")){
            condition += (condition.length()==0?" where ":" and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        query += condition;
//        query="from UnitOfMeasure where company.companyID=?";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }


    public KwlReturnObject deleteUoM(String uomid, String companyid) throws ServiceException {
        String delQuery = "delete from UnitOfMeasure u where u.ID=? and u.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery,new Object[]{uomid, companyid});
        return new KwlReturnObject(true, "UoM has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject addUoM(HashMap<String, Object> uomMap) throws ServiceException {
        List list = new ArrayList();
        try {
            UnitOfMeasure uom = new UnitOfMeasure();
            uom = buildUoM(uom, uomMap);
            hibernateTemplate.save(uom);
            list.add(uom);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addUoM : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "UoM has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateUoM(HashMap<String, Object> uomMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String uomid = (String) uomMap.get("uomid");
            UnitOfMeasure uom = (UnitOfMeasure) hibernateTemplate.get(UnitOfMeasure.class, uomid);
            if(uom != null) {
                uom = buildUoM(uom, uomMap);
                hibernateTemplate.saveOrUpdate(uom);
            }
            list.add(uom);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateUoM : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "UoM has been updated successfully", null, list, list.size());
    }

    public UnitOfMeasure buildUoM(UnitOfMeasure uom, HashMap<String, Object> uomMap) throws ServiceException {
        try {
            if (uomMap.containsKey("uomname")) {
                uom.setName((String) uomMap.get("uomname"));
            }
            if (uomMap.containsKey("uomtype")) {
                uom.setType((String) uomMap.get("uomtype"));
            }
            if (uomMap.containsKey("precision")) {
                uom.setAllowedPrecision((Integer) uomMap.get("precision"));
            }
            if (uomMap.containsKey("companyid")) {
                Company company = uomMap.get("companyid")==null?null:(Company)hibernateTemplate.get(Company.class, (String) uomMap.get("companyid"));
                uom.setCompany(company);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildUoM : " + e.getMessage(), e);
        }
        return uom;
    }

    public void copyUOM(String companyid) throws ServiceException {
        try {
            String query = "from DefaultUnitOfMeasure";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query);
            Iterator iter = list.iterator();
            Company company = (Company)hibernateTemplate.get(Company.class, companyid);
            while (iter.hasNext()) {
                DefaultUnitOfMeasure defaultUOM = (DefaultUnitOfMeasure) iter.next();
                UnitOfMeasure uom = new UnitOfMeasure();
                uom.setCompany(company);
                uom.setName(defaultUOM.getName());
                uom.setType(defaultUOM.getType());
                hibernateTemplate.save(uom);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyUOM : "+ex.getMessage(), ex);
        }

    }
}
