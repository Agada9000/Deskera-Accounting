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
package com.krawler.esp.handlers;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ProjectActivity;
import com.krawler.common.admin.ProjectFeature;
import com.krawler.common.admin.Role;
import com.krawler.common.admin.RoleUserMapping;
import com.krawler.common.admin.UserPermission;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import com.krawler.utils.json.base.*;
import java.util.ArrayList;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class PermissionHandler {

    public static JSONObject getFeatureList(Session session) throws ServiceException {
		JSONObject jobj = new JSONObject();
		try {
            String query="select featureID, featureName, displayFeatureName from ProjectFeature";
            List list = HibernateUtil.executeQuery(session, query);
            Iterator ite = list.iterator();
            JSONArray jArr=new JSONArray();
            while(ite.hasNext() ) {
                Object[] row = (Object[])ite.next();
                JSONObject obj=new JSONObject();
                obj.put("featureid",row[0]);
                obj.put("featurename",row[1]);
                obj.put("displayfeaturename",row[2]);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("PermissionHandler.getFeatureList", e);
        }
        return jobj;
    }

    public static JSONObject getActivityList(Session session) throws ServiceException {
		JSONObject jobj = new JSONObject();
		try {
            String query="select feature.featureID, activityID, activityName, displayActivityName from ProjectActivity";
            List list = HibernateUtil.executeQuery(session, query);
            Iterator ite = list.iterator();
            JSONArray jArr=new JSONArray();
            while(ite.hasNext() ) {
                Object[] row = (Object[])ite.next();
                JSONObject obj=new JSONObject();
                obj.put("featureid",row[0]);
                obj.put("activityid",row[1]);
                obj.put("activityname",row[2]);
                obj.put("displayactivityname",row[3]);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("PermissionHandler.getActivityList", e);
        }
        return jobj;
    }
    
    public static JSONObject getPermissionCode(Session session, HttpServletRequest request)
			throws ServiceException {
		JSONObject jobj = new JSONObject();
		try {
            String query="select up.feature.featureID, up.permissionCode from UserPermission up inner join up.role r where r.ID=?";
            List list = HibernateUtil.executeQuery(session, query, request.getParameter("roleid"));
            Iterator ite = list.iterator();
            JSONArray jArr=new JSONArray();
            while(ite.hasNext() ) {
                Object[] row = (Object[])ite.next();
                JSONObject obj=new JSONObject();
                obj.put("featureid",row[0]);
                obj.put("permission",row[1]);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("PermissionHandler.getPermissionCode", e);
        }
        return jobj;
	}

    public static JSONObject getPermissions(Session session, String userid)
			throws ServiceException {
		JSONObject jobj = new JSONObject();
        JSONObject fjobj=new JSONObject();
        JSONObject ujobj=new JSONObject();
		try {
            String query="select pf, pa from ProjectActivity pa right outer join pa.feature pf order by pa.orderNo";
            List list = HibernateUtil.executeQuery(session, query);
            Iterator ite = list.iterator();
            while(ite.hasNext() ) {
                Object[] row = (Object[]) ite.next();
                String fName=((ProjectFeature)row[0]).getFeatureName();
                ProjectActivity activity=(ProjectActivity)row[1];
                if(!fjobj.has(fName))
                    fjobj.put(fName, new JSONObject());

                JSONObject temp=fjobj.getJSONObject(fName);
                if(activity!=null)
                    temp.put(activity.getActivityName(), (int)Math.pow(2, temp.length()));
            }

            query="select up.feature.featureName, up.permissionCode from UserPermission up inner join up.role r, User user where user.roleID=r.roleid and user.userID=?";
            list = HibernateUtil.executeQuery(session, query, userid);
            ite = list.iterator();
            while(ite.hasNext() ) {
                Object[] row = (Object[]) ite.next();
                ujobj.put(row[0].toString(), row[1]);
            }
            jobj.put("Perm", fjobj);
            jobj.put("UPerm", ujobj);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("PermissionHandler.getPermissions", e);
        }
        return jobj;
	}

    public static void saveActivity(Session session, HttpServletRequest request) throws ServiceException, HibernateException {
        String id = request.getParameter("activityid");
        ProjectActivity activity;

        if (StringUtil.isNullOrEmpty(id) == false) {
            activity = (ProjectActivity) session.get(ProjectActivity.class, id);
        } else {
            activity = new ProjectActivity();
            ProjectFeature feature = (ProjectFeature) session.get(ProjectFeature.class, request.getParameter("featureid"));
            activity.setFeature(feature);
        }
        activity.setActivityName(request.getParameter("activityname"));
        activity.setDisplayActivityName(request.getParameter("displayactivityname"));
        session.saveOrUpdate(activity);
        if (StringUtil.isNullOrEmpty(id) == false) {
            updatePermissionsForActivity(session, activity, activity.getFeature(), true);
        }
    }

    private static void updatePermissionsForActivity(Session session, ProjectActivity activity, ProjectFeature feature, boolean insert) throws ServiceException, HibernateException{
        int pos=0;
        String query="select activityID from ProjectActivity where feature.featureID=?  order by orderNo";
        List l=HibernateUtil.executeQuery(session, query,feature.getFeatureID());
        Iterator itr = l.iterator();
        while(itr.hasNext()){
            String row=(String)itr.next();
            if(activity.getActivityID().equals(row)) break;
            pos++;
        }
        query="from UserPermission where feature.featureID=?";
        List<UserPermission> list=HibernateUtil.executeQuery(session, query,feature.getFeatureID());
        itr = list.iterator();
        while(itr.hasNext()){
            UserPermission permission= (UserPermission)itr.next();
            long code=permission.getPermissionCode();
            if(insert) code=((code&~pos)<<1)+(code&pos);
            else code=((code&~(2*pos+1))>>>1)+(code&pos);

            if(code==0)
                session.delete(permission);
            else{
                permission.setPermissionCode(code);
                session.save(permission);
            }
        }
    }

    public static void saveFeature(Session session, HttpServletRequest request) throws ServiceException, HibernateException {
        String id=request.getParameter("featureid");
        ProjectFeature feature;
        if(StringUtil.isNullOrEmpty(id)==false){
            feature=(ProjectFeature)session.get(ProjectFeature.class, id);
        }else{
            feature=new ProjectFeature();
        }
        feature.setFeatureName(request.getParameter("featurename"));
        feature.setDisplayFeatureName(request.getParameter("displayfeaturename"));
        session.saveOrUpdate(feature);
    }

    public static void deleteFeature(Session session, HttpServletRequest request) throws ServiceException, HibernateException {
        String id=request.getParameter("featureid");
        ProjectFeature feature;
        feature=(ProjectFeature)session.get(ProjectFeature.class, id);
        session.delete(feature);
    }

    public static void deleteActivity(Session session, HttpServletRequest request) throws ServiceException, HibernateException {
        String id=request.getParameter("activityid");
        ProjectActivity activity;
        activity=(ProjectActivity)session.get(ProjectActivity.class, id);
        updatePermissionsForActivity(session, activity, activity.getFeature(), false);
        session.delete(activity);
    }

    public static void saveRole(Session session, HttpServletRequest request) throws ServiceException, HibernateException {
        try {
            String id = request.getParameter("roleid");
            Role role;
            if (StringUtil.isNullOrEmpty(id)) {
                role = new Role();
            } else {
                role = (Role) session.get(Role.class, id);
            }
            role.setCompany((Company) session.get(Company.class, sessionHandlerImpl.getCompanyid(request)));
            role.setName(request.getParameter("rolename"));
            session.saveOrUpdate(role);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public static void deleteRole(Session session, HttpServletRequest request) throws ServiceException, HibernateException, AccountingException {
        String id=request.getParameter("roleid");
        if(id.equals(Role.COMPANY_ADMIN)||id.equals(Role.COMPANY_USER))
            throw new AccountingException("Can't delete Predefiend Roles");
        Role role=(Role)session.get(Role.class, id);
        session.delete(role);
    }

    public static JSONObject getRoles(Session session, HttpServletRequest request) throws ServiceException {
		JSONObject jobj = new JSONObject();
		try {
            String query="from Role where company is null or company.companyID=?";
            List list = HibernateUtil.executeQuery(session, query,sessionHandlerImpl.getCompanyid(request));
            Iterator ite = list.iterator();
            JSONArray jArr=new JSONArray();
            while(ite.hasNext() ) {
                Role role = (Role)ite.next();
                JSONObject obj=new JSONObject();
                obj.put("roleid",role.getID());
                obj.put("rolename",role.getName());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    public static boolean isPermitted(JSONObject perms, String featureName, String activityName) throws JSONException {
        int perm=perms.getJSONObject("Perm").getJSONObject(featureName).optInt(activityName);
        int uperm=perms.getJSONObject("UPerm").optInt(featureName);
        if((perm & uperm)==perm)
            return true;
        return false;
    }
}
