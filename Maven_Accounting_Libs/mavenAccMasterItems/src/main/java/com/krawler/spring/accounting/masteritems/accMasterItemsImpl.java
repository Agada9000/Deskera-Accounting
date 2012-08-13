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

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.DefaultMasterItem;
import com.krawler.hql.accounting.MasterGroup;
import com.krawler.hql.accounting.MasterItem;
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
public class accMasterItemsImpl implements accMasterItemsDAO {
    private HibernateTemplate hibernateTemplate;
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

    public KwlReturnObject addMasterItem(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            MasterItem masterItem = new MasterItem();
            if(itemmap.containsKey("name")) {
                masterItem.setValue((String) itemmap.get("name"));
            }
            if(itemmap.containsKey("groupid")) {
                MasterGroup group = itemmap.get("groupid")==null?null:(MasterGroup) hibernateTemplate.get(MasterGroup.class, (String) itemmap.get("groupid"));
                masterItem.setMasterGroup(group);
            }
            if(itemmap.containsKey("companyid")) {
                Company company = itemmap.get("companyid")==null?null:(Company) hibernateTemplate.get(Company.class, (String) itemmap.get("companyid"));
                masterItem.setCompany(company);
            }
            hibernateTemplate.save(masterItem);
            list.add(masterItem);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterItem :"+ ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been added successfully.", null, list, list.size());
    }

    public KwlReturnObject updateMasterItem(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            String itemID = (String) itemmap.get("id");
            MasterItem masterItem = (MasterItem) hibernateTemplate.get(MasterItem.class, itemID);
            if(masterItem != null) {
                if(itemmap.containsKey("name")) {
                    masterItem.setValue((String) itemmap.get("name"));
                }
                if(itemmap.containsKey("groupid")) {
                    MasterGroup group = itemmap.get("groupid")==null?null:(MasterGroup) hibernateTemplate.get(MasterGroup.class, (String) itemmap.get("groupid"));
                    masterItem.setMasterGroup(group);
                }
                if(itemmap.containsKey("companyid")) {
                    Company company = itemmap.get("companyid")==null?null:(Company) hibernateTemplate.get(Company.class, (String) itemmap.get("companyid"));
                    masterItem.setCompany(company);
                }
                hibernateTemplate.saveOrUpdate(masterItem);
                list.add(masterItem);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.updateMasterItem :"+ ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been update successfully.", null, list, list.size());
    }

    public KwlReturnObject daleteMasterItem(String itemid) throws ServiceException {
        boolean successflag = false;
        try {
             MasterItem mdata=(MasterItem) hibernateTemplate.get(MasterItem.class,itemid);
             if(mdata != null) {
                hibernateTemplate.delete(mdata);
                successflag = true;
             }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.daleteMasterItem :"+ ex.getMessage(), ex);
        }
        return new KwlReturnObject(successflag, "Master item has been deleted successfully.", null, null, 1);
    }

//    public KwlReturnObject getMasterItems(String groupid, String companyid) throws ServiceException {
//        List list = new ArrayList();
//        try {
//            String query="from MasterItem me where masterGroup.ID=? and company.companyID=? order by me.value";
//            list = HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{groupid, companyid});
//        } catch (Exception ex) {
//            throw ServiceException.FAILURE("accMasterItemsImpl.getMasterItems :"+ ex.getMessage(), ex);
//        }
//        return new KwlReturnObject(true, "", null, list, list.size());
//    }

    public KwlReturnObject getMasterItems(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from MasterItem";
        return StringUtil.buildNExecuteQuery(hibernateTemplate, query, requestParams);
    }

    public KwlReturnObject addMasterGroup(HashMap<String, Object> groupmap) throws ServiceException {
        List list = new ArrayList();
        try {
            MasterGroup mastergroup = new MasterGroup();
            if(groupmap.containsKey("name")) {
                mastergroup.setGroupName((String) groupmap.get("name"));
            }
            hibernateTemplate.save(mastergroup);
            list.add(mastergroup);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterGroup :"+ ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master Group has been added successfully.", null, list, list.size());
    }

    public KwlReturnObject updateMasterGroup(HashMap<String, Object> groupmap) throws ServiceException {
        List list = new ArrayList();
        try {
            String groupID = (String) groupmap.get("id");
            MasterGroup mastergroup = (MasterGroup) hibernateTemplate.get(MasterGroup.class, groupID);
            if(mastergroup != null) {
                if(groupmap.containsKey("name")) {
                    mastergroup.setGroupName((String) groupmap.get("name"));
                }
                hibernateTemplate.saveOrUpdate(mastergroup);
                list.add(mastergroup);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.updateMasterGroup :"+ ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master Group has been updated successfully.", null, list, list.size());
    }

    public KwlReturnObject getMasterGroups() throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "from MasterGroup";
            list = HibernateUtil.executeQuery(hibernateTemplate, query);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getMasterGroups :"+ ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject deleteMasterGroup(String groupid) throws ServiceException {
        try {
             // Code for deletion of Master Group --- pending
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteMasterGroup :"+ ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master Group has been deleted successfully.", null, null, 1);
    }

    public void copyMasterItems(String companyid) throws ServiceException {
        try {
            String query = "from DefaultMasterItem";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query);
            Iterator iter = list.iterator();
            Company company = (Company) hibernateTemplate.get(Company.class, companyid);
            while(iter.hasNext()){
                DefaultMasterItem defaultMasterItem = (DefaultMasterItem) iter.next();
                MasterItem masterItem = new MasterItem();
                masterItem.setCompany(company);
                masterItem.setMasterGroup(defaultMasterItem.getMasterGroup());
                masterItem.setValue(defaultMasterItem.getValue());
                hibernateTemplate.save(masterItem);
            }
        }  catch (Exception ex) {
            throw ServiceException.FAILURE("copyMasterItems : "+ex.getMessage(), ex);
        }
    }
}
