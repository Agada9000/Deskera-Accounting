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
package com.krawler.spring.permissionHandler;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ProjectActivity;
import com.krawler.common.admin.ProjectFeature;
import com.krawler.common.admin.RoleUserMapping;
import com.krawler.common.admin.Rolelist;
import com.krawler.common.admin.User;
import com.krawler.common.admin.UserLogin;
import com.krawler.common.admin.UserPermission;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author Karthik
 */
public class permissionHandlerDAOImpl implements permissionHandlerDAO {

    private HibernateTemplate hibernateTemplate;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public KwlReturnObject getFeatureList() throws ServiceException {
        int dl = 0;
        List ll = null;
        try {
            String Hql = "select featureID, featureName, displayFeatureName,orderNo from ProjectFeature order by orderNo";
            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getFeatureList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getRoleList(String companyid) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String Hql = "from Rolelist where company is null or company.companyID=? order by roleid";
            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql,companyid);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getRoleList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getRoleofUser(String userid) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String Hql = "select roleId.roleid, roleId.displayrolename from RoleUserMapping where userId.userID=?";
            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql, userid);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getRoleofUser", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }
    
    public KwlReturnObject getActivityList() throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String Hql = "select feature.featureID, activityID, activityName, displayActivityName, parent.activityID from ProjectActivity order by orderNo";
            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getActivityList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject saveFeatureList(HashMap<String, Object> requestParams) throws ServiceException {
        int dl = 0;
        List ll = null;
        try {
            String id = requestParams.containsKey("featureid") && requestParams.get("featureid") != null ? requestParams.get("featureid").toString() : "";
            ProjectFeature feature;
            if (!StringUtil.isNullOrEmpty(id)) {
                feature = (ProjectFeature) hibernateTemplate.load(ProjectFeature.class, id);
            } else {
                feature = new ProjectFeature();
            }
            if (requestParams.containsKey("featurename") && requestParams.get("featurename") != null) {
                feature.setFeatureName(requestParams.get("featurename").toString());
            }
            if (requestParams.containsKey("displayfeaturename") && requestParams.get("displayfeaturename") != null) {
                feature.setDisplayFeatureName(requestParams.get("displayfeaturename").toString());
            }
            hibernateTemplate.saveOrUpdate(feature);
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.saveFeatureList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject saveRoleList(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String id = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";
            Rolelist role;
            if (!StringUtil.isNullOrEmpty(id)) {
                role = (Rolelist) hibernateTemplate.load(Rolelist.class, id);
            } else {
                role = new Rolelist();
            }
            if (requestParams.containsKey("rolename") && requestParams.get("rolename") != null) {
                role.setRolename(requestParams.get("rolename").toString());
            }
            if (requestParams.containsKey("displayrolename") && requestParams.get("displayrolename") != null) {
                role.setDisplayrolename(requestParams.get("displayrolename").toString());
            }
            role.setCompany( (Company) hibernateTemplate.load(Company.class, requestParams.get("companyid").toString()));
            hibernateTemplate.saveOrUpdate(role);

            RoleUserMapping rum = new RoleUserMapping();
            rum.setRoleId(role);
            if (requestParams.containsKey("userid") && requestParams.get("userid") != null) {
                User user=(User)hibernateTemplate.load(User.class, requestParams.get("userid").toString());
                user.setRoleID(role.getRoleid());
            }
            hibernateTemplate.saveOrUpdate(rum);
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.saveRoleList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject saveActivityList(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String id = requestParams.containsKey("activityid") && requestParams.get("activityid") != null ? requestParams.get("activityid").toString() : "";
            ProjectActivity activity;
            ProjectFeature feature = null;
            if (!StringUtil.isNullOrEmpty(id)) {
                activity = (ProjectActivity) hibernateTemplate.load(ProjectActivity.class, id);
            } else {
                activity = new ProjectActivity();
                feature = (ProjectFeature) hibernateTemplate.load(ProjectFeature.class, requestParams.get("featureid").toString());
                activity.setFeature(feature);
            }
            if (requestParams.containsKey("activityname") && requestParams.get("activityname") != null) {
                activity.setActivityName(requestParams.get("activityname").toString());
            }
            if (requestParams.containsKey("displayactivityname") && requestParams.get("displayactivityname") != null) {
                activity.setDisplayActivityName(requestParams.get("displayactivityname").toString());
            }
            hibernateTemplate.saveOrUpdate(activity);
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.saveActivityList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject deleteFeature(HashMap<String, Object> requestParams) throws ServiceException {
        int dl = 0;
        List ll = null;
        try {
            String id = requestParams.containsKey("featureid") && requestParams.get("featureid") != null ? requestParams.get("featureid").toString() : "";
            ProjectFeature feature;
            if (!StringUtil.isNullOrEmpty(id)) {
                feature = (ProjectFeature) hibernateTemplate.load(ProjectFeature.class, id);
                hibernateTemplate.delete(feature);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.deleteFeature", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject deleteRole(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        String Hql = "";
        Rolelist role = null;
        String msg = "";
        try {
            String id = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";
            if (!StringUtil.isNullOrEmpty(id)) {
                role = (Rolelist) hibernateTemplate.load(Rolelist.class, id);
                Hql = "from RoleUserMapping where roleId=?";
                ll = HibernateUtil.executeQuery(hibernateTemplate, Hql, role);
                if (ll.size() > 0) {
                    msg = "Role cannot be deleted as it is assigned to user(s)";
                } else {
                    hibernateTemplate.delete(role);
                    msg = "Role deleted successfully";
                }
            }
            ll = new ArrayList();
            ll.add(msg);
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.deleteRole", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject deleteActivity(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String id = requestParams.containsKey("activityid") && requestParams.get("activityid") != null ? requestParams.get("activityid").toString() : "";
            ProjectActivity activity;
            if (StringUtil.isNullOrEmpty(id)) {
                activity = (ProjectActivity) hibernateTemplate.load(ProjectActivity.class, id);
                hibernateTemplate.delete(activity);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.deleteActivity", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getActivityFeature() throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String Hql = "select pf, pa from ProjectActivity pa right outer join pa.feature pf order by pa.orderNo";
            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getActivityFeature", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getUserPermission(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        ArrayList params = null;
        try {
            String userid = requestParams.containsKey("userid") && requestParams.get("userid") != null ? requestParams.get("userid").toString() : "";
            String roleid = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";
            String mappingid = null;
            
            if(!StringUtil.isNullOrEmpty(userid)) {
                String Hql = "from RoleUserMapping where userId.userID=? ";
                ll = HibernateUtil.executeQuery(hibernateTemplate, Hql, userid);
                if(ll != null){
	                Iterator itr = ll.iterator();
	                if(itr.hasNext()) {
	                    RoleUserMapping rmapping = (RoleUserMapping) itr.next();
	                    roleid = rmapping.getRoleId().getRoleid();
	                    mappingid = rmapping.getId();
	                }
                }
            }

            String Hql = " select feature.featureName, permissionCode, feature.featureID from UserPermission up";
            String condition = "";
            params = new ArrayList();

            if(!StringUtil.isNullOrEmpty(roleid)) {
                 condition += (condition.length()==0?" where ":" and ")+"role.roleid=? ";
                 params.add(roleid);
            }
            
            if(!StringUtil.isNullOrEmpty(mappingid)) {
                condition += (condition.length()==0?" where ":" and ")+"roleUserMapping.id=? ";
                params.add(mappingid);
            }
            
            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql+condition, params.toArray());
            dl = ll.size();
            
            if(dl == 0 && (roleid.equals(Rolelist.COMPANY_ADMIN) || roleid.equals(Rolelist.COMPANY_USER))){
            	Hql = " select feature.featureName, permissionCode, feature.featureID from UserPermission up where role.roleid=? ";
            	ll = HibernateUtil.executeQuery(hibernateTemplate, Hql, roleid);
                dl = ll.size();
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getUserPermission", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public boolean isSuperAdmin(String userid, String companyid) throws ServiceException {
        boolean admin = false;
        try {
            // Hardcoded id of admin user and admin company.
            if (userid.equals("ff808081227d4f5801227d535ebb0009") && companyid.equals("ff808081227d4f5801227d535eba0008")) {
                admin = true;
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.isSuperAdmin", e);
        }
        return admin;
    }

    public KwlReturnObject setPermissions(HashMap<String, Object> requestParams, String[] features, String[] permissions) throws ServiceException {
        List ll = null;
        int dl = 0;
        String rid = "";
        String Hql = "";
        try {
            String id = requestParams.containsKey("userid") && requestParams.get("userid") != null ? requestParams.get("userid").toString() : "";
            String roleId = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";
            
            Hql = "select id from RoleUserMapping where userId.userID=?";
            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql, id);
            rid = ll.get(0).toString();
            
            Hql = "delete from UserPermission where roleUserMapping.id=?";
            HibernateUtil.executeUpdate(hibernateTemplate, Hql, new Object[]{rid});

            Hql = "delete from RoleUserMapping where userId.userID=?";
            HibernateUtil.executeUpdate(hibernateTemplate, Hql, id);

//            Hql = "select id from RoleUserMapping where userId.userID=? ";
//            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql, id);

          //  rid = ll.get(0).toString();
            RoleUserMapping rum = new RoleUserMapping();//) hibernateTemplate.load(RoleUserMapping.class, rid);
            Rolelist role = (Rolelist) hibernateTemplate.load(Rolelist.class, roleId);
            User user = (User) hibernateTemplate.load(User.class, id);
            rum.setUserId(user);
            rum.setRoleId(role);
            hibernateTemplate.save(rum);
            
            Hql = "select id from RoleUserMapping where userId.userID=? ";
            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql, id);
            rid = ll.get(0).toString();
            rum = (RoleUserMapping) hibernateTemplate.load(RoleUserMapping.class, rid);
            
            user.setRoleID(roleId);
            hibernateTemplate.save(user);

            if(!(roleId.equals(Rolelist.COMPANY_ADMIN) || roleId.equals(Rolelist.COMPANY_USER))){
//                Hql = "delete from UserPermission where role.roleid=?";
//                HibernateUtil.executeUpdate(hibernateTemplate, Hql, roleId);

                for (int i = 0; i < features.length; i++) {
                    if (permissions[i].equals("0")) {
                        continue;
                    }
                    UserPermission permission = new UserPermission();
                    permission.setRole(role);
                    permission.setFeature((ProjectFeature) hibernateTemplate.load(ProjectFeature.class, features[i]));
                    permission.setPermissionCode(Long.parseLong(permissions[i]));
                    permission.setRoleUserMapping(rum);
                    hibernateTemplate.save(permission);
                }
            }
            UserLogin userLogin = (UserLogin)hibernateTemplate.load(UserLogin.class, id);
            ll = new ArrayList();
            ll.add(userLogin);
//        } catch (Exception e) {
//            throw ServiceException.FAILURE("permissionHandlerDAOImpl.setPermissions", e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

	@Override
	public void deleteUserPermissions(String roleid) throws ServiceException {
		try {
            String Query = "delete from UserPermission where role.roleid=?";
            HibernateUtil.executeUpdate(hibernateTemplate, Query, roleid);
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.deleteUserPermissions", e);
        }
	}

    
}
