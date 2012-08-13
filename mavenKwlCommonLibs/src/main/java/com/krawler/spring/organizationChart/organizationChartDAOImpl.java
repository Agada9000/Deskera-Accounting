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
package com.krawler.spring.organizationChart;

import com.krawler.common.admin.Assignmanager;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import org.hibernate.SessionFactory;
import java.util.List;
import com.krawler.common.service.ServiceException;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author Karthik
 */
public class organizationChartDAOImpl implements organizationChartDAO {

    private HibernateTemplate hibernateTemplate;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public KwlReturnObject getUnmappedUsers(HashMap<String, Object> requestParams) throws ServiceException {
        int dl = 0;
        List ll = null;
        try {
            String companyid = requestParams.get("companyid").toString();
            Company company = (Company) hibernateTemplate.get(Company.class, companyid);
            User userC = (User) company.getCreator();

            String Hql = "from User where userID NOT IN (select assignemp.userID from Assignmanager) and userID NOT IN (select assignman.userID from Assignmanager) and company.companyID = ? and deleteflag=0 and userID != ?";
            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql, new Object[]{company.getCompanyID(), userC.getUserID()});
            dl = ll.size();

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("organizationChartDAOImpl.getUnmappedUsers", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject deleteNode(HashMap<String, Object> requestParams) throws Exception {
        int dl = 0;
        List ll = new ArrayList();
        String nodeid = "";
        String details = "";
        String details2 = "";
        try {
            if (requestParams.containsKey("nodeid") && requestParams.get("nodeid").toString() != null) {
                nodeid = requestParams.get("nodeid").toString();
            }
            String getChild = "from Assignmanager a where a.assignman.userID = ? ";
            String getParent = "select a.id from Assignmanager a where a.assignemp.userID = ? ";

            List childList = HibernateUtil.executeQuery(getChild, new Object[]{nodeid});
            List parentList = HibernateUtil.executeQuery(getParent, new Object[]{nodeid});

            Object amPidObj = (Object) parentList.iterator().next();

            String ampid = amPidObj.toString();
            Assignmanager amP = (Assignmanager) hibernateTemplate.get(Assignmanager.class, ampid);
            User u = (User) amP.getAssignemp();
            User p = (User) amP.getAssignman();
            details= u.getUserLogin().getUserName() + " [ " +u.getFirstName()+" "+u.getLastName() + " ] Un-assigned from "+
                    p.getFirstName()+" "+p.getLastName() + " , and removed from Organization.";
            Iterator iteC = childList.iterator();
            while (iteC.hasNext()) {
                Assignmanager amC = (Assignmanager) iteC.next();
                amC.setAssignman(amP.getAssignman());
                hibernateTemplate.update(amC);
                User u2 = (User) amC.getAssignemp();
                details2 = u2.getFirstName()+" "+u2.getLastName() + "  re-assigned to "+
                        p.getFirstName()+" "+p.getLastName() + "  ";
            }
            ll.add(details);
            ll.add(details2);
            hibernateTemplate.delete(amP);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("organizationChartDAOImpl.deleteNode", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public void getAssignManager(String manID, List appendList, int exceptionAt, String extraQuery) throws ServiceException {
        int dl = 0;
        Object[] params = null;
        List ll = null;
        try {
            String Hql = "from Assignmanager where assignman.userID = ? " + extraQuery;
            params = new Object[]{manID};

            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql, params);
            dl = ll.size();
            appendList.addAll(ll);
            exceptionAt++;
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                Assignmanager am = (Assignmanager) ite.next();
                getAssignManager(am.getAssignemp().getUserID(), appendList, exceptionAt, extraQuery);
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("organizationChartDAOImpl.recursiveUsers", ex);
        }
    }

    public void rootUser(JSONArray jarr, String userid) throws ServiceException {
        JSONObject objU = new JSONObject();
        try {
            User user = (User) hibernateTemplate.get(User.class, userid);
            objU.put("fromuid", user.getUserID());
            objU.put("userid", user.getUserID());
            objU.put("username", user.getUserLogin().getUserName());
            objU.put("emailid", user.getEmailID());
            objU.put("contactno", user.getContactNumber());
            objU.put("fname", user.getFirstName());
            objU.put("lname", user.getLastName());
            objU.put("image", user.getImage());
            objU.put("nodeid", user.getUserID());
            objU.put("address", user.getAddress());
            objU.put("level", 0);
            objU.put("designation", "<span id='rootnode'>(You are here.)</span>");
            jarr.put(objU);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("organizationChartDAOImpl.rootUser", ex);
        }
    }
}
