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
package com.krawler.spring.accounting.discount;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.Discount;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
public class accDiscountImpl implements accDiscountDAO {
    private HibernateTemplate hibernateTemplate;
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

    public KwlReturnObject deleteDiscount(String discountid, String companyid) throws ServiceException {
        String delQuery = "delete from Discount where ID=? and company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{discountid, companyid});

        return new KwlReturnObject(true, "Discount has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteDiscountEntry(String discountid, String companyid) throws ServiceException {
        String query = "update Discount set deleted=true where ID=? and company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, query, new Object[]{discountid, companyid});
        return new KwlReturnObject(true, "Credit Note has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject addDiscount(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        Discount discount=new Discount();
        try{
             if (json.has("discountid")) {
                discount = (Discount) hibernateTemplate.get(Discount.class, json.getString("jeid"));
            }
            if(json.has("discount")){
                discount.setDiscount(json.getDouble("discount"));
            }
            if(json.has("inpercent")){
                discount.setInPercent(json.getBoolean("inpercent"));
            }
            if(json.has("originalamount")){
                discount.setOriginalAmount(json.getDouble("originalamount"));
            }
            if(json.has("companyid")){
                discount.setCompany((Company) hibernateTemplate.get(Company.class, json.getString("companyid")));
            }
            hibernateTemplate.save(discount);
            list.add(discount);
        }catch(Exception ex){
            throw ServiceException.FAILURE("accDiscountImpl.addDiscount : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updateDiscount(Map requestParam) throws ServiceException {
        List list = new ArrayList();
        Discount discount= null;
        try{
            String discountid = (String) requestParam.get("discountid");
            if(StringUtil.isNullOrEmpty(discountid)){
                discount=new Discount();
            }else{
                discount = (Discount) hibernateTemplate.get(Discount.class, discountid);
            }
            
            if(requestParam.containsKey("discount")){
                discount.setDiscount((Double)requestParam.get("discount"));
            }
            if(requestParam.containsKey("inpercent")){
                discount.setInPercent((Boolean)requestParam.get("inpercent"));
            }
            if(requestParam.containsKey("originalamount")){
                discount.setOriginalAmount((Double)requestParam.get("originalamount"));
            }
            if(requestParam.containsKey("companyid")){
                discount.setCompany((Company) hibernateTemplate.get(Company.class, (String)requestParam.get("companyid")));
            }
            hibernateTemplate.save(discount);
            list.add(discount);
        }catch(Exception ex){
            throw ServiceException.FAILURE("accDiscountImpl.addDiscount : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

}
