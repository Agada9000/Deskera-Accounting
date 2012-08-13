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
package com.krawler.spring.authHandler;
import com.krawler.common.admin.Company;
import com.krawler.common.util.StringUtil;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.KWLDateFormat;
import com.krawler.common.admin.KWLTimeZone;
import com.krawler.common.admin.Language;
import com.krawler.common.admin.Rolelist;
import com.krawler.common.admin.User;
import com.krawler.common.admin.UserLogin;
import com.krawler.common.util.URLUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.permissionHandler.permissionHandler;
import com.krawler.spring.profileHandler.profileHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap; 
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Karthik
 */
public class authHandlerController extends MultiActionController {

    private authHandlerDAO authHandlerDAOObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private profileHandlerDAO profileHandlerDAOObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private companyDetailsDAO companyDetailsDAOObj;
    private String successView;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }
    
    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj1) {
        this.profileHandlerDAOObj = profileHandlerDAOObj1;
    }

    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }

    public ModelAndView verifyLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        JSONObject rjobj = new JSONObject();
        JSONObject ujobj = new JSONObject();
        KwlReturnObject kmsg = null;
        String result = "";
        String userid = "";
        String companyid = "";
        HashMap<String, Object> requestParams2 = null;
        JSONObject obj = null, jret = new JSONObject();
        boolean isvalid = false;
        try {
            String user = request.getParameter("u");
            String pass = request.getParameter("p");
            String login = request.getParameter("blank");
            String subdomain = URLUtil.getDomainName(request);
            boolean isValidUser = false;

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("user", StringUtil.checkForNull(user));
            requestParams.put("pass", StringUtil.checkForNull(pass));
            requestParams.put("subdomain", StringUtil.checkForNull(subdomain));
            if (StringUtil.isNullOrEmpty(login)) {
                kmsg = authHandlerDAOObj.verifyLogin(requestParams);
                jobj = getVerifyLoginJson(kmsg.getEntityList(), request);
                if (jobj.has("success") && (jobj.get("success").equals(true))) {
                    obj = new JSONObject();
                    companyid = jobj.getString("companyid");
                    userid = jobj.getString("lid");
                    jobj.put("companyPreferences", "");

                    requestParams2 = new HashMap<String, Object>();
                    requestParams2.put("userid", userid);
                    kmsg = permissionHandlerDAOObj.getUserPermission(requestParams2);
                    Iterator ite = kmsg.getEntityList().iterator();
                    JSONArray jarr = new JSONArray();
                    while (ite.hasNext()) {
                        JSONObject jo = new JSONObject();
                        Object[] roww = (Object[]) ite.next();
                        jo.put(roww[0].toString(), roww[1]);
                        jarr.put(jo);
                    }
                    jobj.put("perms", jarr);

                    sessionHandlerImplObj.createUserSession(request, jobj);
                    setLocale(request, response, jobj.optString("language",null));
                    requestParams.put("userloginid", StringUtil.checkForNull(userid));
                    profileHandlerDAOObj.saveUserLogin(requestParams);
                    isvalid = true;
                } else {
                    jobj = new JSONObject();
                    jobj.put("success", false);
                    jobj.put("reason", "noaccess");
                    jobj.put("message", "Authentication failed");
                    isvalid = false;
                }
                
            } else {
                String username = request.getRemoteUser();
                if (!StringUtil.isNullOrEmpty(username)) {
                    boolean toContinue = true;
                       if(sessionHandlerImplObj.validateSession(request, response)){
                            String companyid_session =  sessionHandlerImplObj.getCompanyid(request);
                            String subdomainFromSession = companyDetailsDAOObj.getSubDomain(companyid_session);
                            if( !subdomain.equalsIgnoreCase(subdomainFromSession)){
                                result = "alreadyloggedin";
                                toContinue = false;
                            }
                    }
                    if(toContinue){
//                jbj = DBCon.AuthUser(username, subdomain);
                    requestParams = new HashMap<String, Object>();
                    requestParams.put("user", username);
                    requestParams.put("subdomain", subdomain);
                    kmsg = authHandlerDAOObj.verifyLogin(requestParams);
                    jobj = getVerifyLoginJson(kmsg.getEntityList(), request);
                    if (jobj.has("success")) {
//                    sessionbean.createUserSession(request, jbj);
                        requestParams2 = new HashMap<String, Object>();
                        requestParams2.put("userid", jobj.get("lid"));//userid
                        kmsg = permissionHandlerDAOObj.getUserPermission(requestParams2);

                        Iterator ite = kmsg.getEntityList().iterator();
                        JSONArray jarr = new JSONArray();
                        while (ite.hasNext()) {
                            JSONObject jo = new JSONObject();
                            Object[] roww = (Object[]) ite.next();
                            jo.put(roww[0].toString(), roww[1]);
                            jarr.put(jo);
                        }
                        jobj.put("perms", jarr);
                        jobj.put("companyPreferences", "");
                        
                        sessionHandlerImplObj.createUserSession(request, jobj);
                        profileHandlerDAOObj.saveUserLastLogin(requestParams2);
                        setLocale(request, response, jobj.optString("language",null));
                        isValidUser = true;
                    } else {
                        result = "noaccess";
                    }
		          }
                } else {
                    if (sessionHandlerImpl.isValidSession(request, response)) {
                        companyid = sessionHandlerImpl.getCompanyid(request);
                        String companyName = sessionHandlerImpl.getCompanyName(request);
                        username = sessionHandlerImpl.getUserName(request);
                        jobj.put("companyid", companyid);
                        jobj.put("company", companyName);
                        jobj.put("username", username);
                        jobj.put("subdomain", subdomain);				// subdomain for mailto support link on dashboard
                        isValidUser = true;
                    } else {
                        result = "timeout";
                    }
                }

                if (isValidUser) {
                        userid = sessionHandlerImpl.getUserid(request);
                        companyid = sessionHandlerImpl.getCompanyid(request);
                        jobj.put("fullname", profileHandlerDAOObj.getUserFullName(userid));
                        jobj.put("lid", userid);
                        jobj.put("callwith", sessionHandlerImplObj.getUserCallWith(request));
                        jobj.put("companyPreferences", "");

                        requestParams2 = new HashMap<String, Object>();
                        requestParams2.put("timezoneid", sessionHandlerImplObj.getTimeZoneID(request));
                        requestParams2.put("dateformatid", sessionHandlerImpl.getDateFormatID(request));
                        requestParams2.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
                        JSONObject prefJson = new JSONObject();
                        kmsg = authHandlerDAOObj.getPreferences(requestParams2);
                        prefJson = getPreferencesJson(kmsg.getEntityList(), request);
                        jobj.put("preferences", prefJson.getJSONArray("data").get(0));
                        jobj.put("accpref", prefJson.getJSONArray("data").get(0));

                        if (!permissionHandlerDAOObj.isSuperAdmin(userid, companyid)) {
                            JSONObject permJobj = new JSONObject();
                            kmsg = permissionHandlerDAOObj.getActivityFeature();
                            permJobj = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());

                            requestParams2 = new HashMap<String, Object>();
                            requestParams2.put("userid", userid);
                            kmsg = permissionHandlerDAOObj.getUserPermission(requestParams2);
                            permJobj = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), permJobj);

                            jobj.put("perm", permJobj);
                        } else {
                            jobj.put("deskeraadmin", true);
                        }

                        JSONObject roleJson = new JSONObject();
                        kmsg = permissionHandlerDAOObj.getRoleList(companyid);
                        Iterator ite = kmsg.getEntityList().iterator();
                        int inc = 0;
                        while (ite.hasNext()) {
                            Object row = (Object) ite.next();
                            String rname = ((Rolelist) row).getRolename();
                            rjobj.put(rname, (int) Math.pow(2, inc));
                            inc++;
                        }
                        kmsg = permissionHandlerDAOObj.getRoleofUser(userid);
                        ite = kmsg.getEntityList().iterator();
                        if(ite.hasNext()) {
                            Object[] row = (Object[]) ite.next();
                            ujobj.put("roleid", row[0].toString());
                        }
                        roleJson.put("Role", rjobj);
                        roleJson.put("URole", ujobj);
                        jobj.put("role", roleJson);
                        jobj.put("base_url", URLUtil.getPageURL(request,""));
                        isvalid = true;
                } else {
                    jobj.put("success", false);
                    jobj.put("reason", result);
                    isvalid = false;
                }
            }
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                jret.put("valid", isvalid);
                jret.put("data", jobj);
            } catch (JSONException ex) {
                Logger.getLogger(authHandlerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jret.toString());
    }

     public JSONObject getVerifyLoginJson(List ll, HttpServletRequest request) {
        JSONObject jobj = new JSONObject();
        try {
            Iterator ite = ll.iterator();
            if (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                User user = (User) row[0];
                UserLogin userLogin = (UserLogin) row[1];
                Company company = (Company) row[2];
                jobj.put("success", true);
                jobj.put("lid", userLogin.getUserID());
                jobj.put("username", userLogin.getUserName());
                jobj.put("companyid", company.getCompanyID());
                jobj.put("subdomain", company.getSubDomain());		// subdomain for mailto support link on dashboard
                jobj.put("company", company.getCompanyName());
                Language lang=company.getLanguage();
                if(lang!=null)
                	jobj.put("language", lang.getLanguageCode()+(lang.getCountryCode()!=null?"_"+lang.getCountryCode():""));
                jobj.put("roleid", user.getRoleID());
                jobj.put("callwith", user.getCallwith());
                jobj.put("timeformat", user.getTimeformat());
                jobj.put("userfullname", "" + user.getFirstName() + (StringUtil.isNullOrEmpty(user.getLastName())?"":(" "+ user.getLastName())));
                KWLTimeZone timeZone = user.getTimeZone();
                if (timeZone == null) {
                    timeZone = company.getTimeZone();
                }
                if (timeZone == null) {
                    timeZone = (KWLTimeZone) (KWLTimeZone) kwlCommonTablesDAOObj.getClassObject(KWLTimeZone.class.getName(), storageHandlerImpl.getDefaultTimeZoneID());
                }
                jobj.put("timezoneid", timeZone.getTimeZoneID());
                jobj.put("tzdiff", timeZone.getDifference());
                KWLDateFormat dateFormat = user.getDateFormat();
                if (dateFormat == null) {
                    dateFormat = (KWLDateFormat) kwlCommonTablesDAOObj.getClassObject(KWLDateFormat.class.getName(), storageHandlerImpl.getDefaultDateFormatID());
                }
                jobj.put("dateformatid", dateFormat.getFormatID());
                KWLCurrency currency = company.getCurrency();
                if (currency == null) {
                    currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), storageHandlerImpl.getDefaultCurrencyID());
                }
                jobj.put("currencyid", currency.getCurrencyID());
                jobj.put("superuser", user.getRoleID());
            } else {
                jobj.put("failure", true);
                jobj.put("success", false);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

    public JSONObject getPreferencesJson(List ll, HttpServletRequest request) {
        JSONObject jobj = new JSONObject();
        JSONObject retJobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        String dateformat = "";
        try {
            String timeformat = sessionHandlerImplObj.getUserTimeFormat(request);

            KWLTimeZone timeZone = (KWLTimeZone) ll.get(0);
            KWLDateFormat dateFormat = (KWLDateFormat) ll.get(1);
            KWLCurrency currency = (KWLCurrency) ll.get(2);

            jobj.put("Timezone", timeZone.getName());
            jobj.put("Timezoneid", timeZone.getTimeZoneID());
            jobj.put("Timezonediff", timeZone.getDifference());
            if (timeformat.equals("1")) {
                dateformat = dateFormat.getScriptForm().replace('H', 'h');
                if (!dateformat.equals(dateFormat.getScriptForm())) {
                    dateformat += " T";
                }
            } else {
                dateformat = dateFormat.getScriptForm();
            }
            jobj.put("DateFormat", dateformat);
            jobj.put("DateFormatid", dateFormat.getFormatID());
            jobj.put("seperatorpos", dateFormat.getScriptSeperatorPosition());
            jobj.put("Currency", currency.getHtmlcode());
            jobj.put("CurrencyName", currency.getName());
            jobj.put("CurrencySymbol", currency.getSymbol());
            jobj.put("Currencyid", currency.getCurrencyID());
            jarr.put(jobj);

            retJobj.put("data", jarr);
            retJobj.put("success", true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return retJobj;
    }

    public ModelAndView getPreferences(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("timezoneid", sessionHandlerImplObj.getTimeZoneID(request));
            requestParams.put("dateformatid", sessionHandlerImplObj.getDateFormatID(request));
            requestParams.put("currencyid", sessionHandlerImplObj.getCurrencyID(request));

            kmsg = authHandlerDAOObj.getPreferences(requestParams);
            jobj = getPreferencesJson(kmsg.getEntityList(), request);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    // Locale Implementation
    protected void setLocale(HttpServletRequest request, HttpServletResponse response, String newLocale) {
		if (newLocale != null) {
			LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
			if (localeResolver == null) {
				Logger.getLogger(authHandlerController.class.getName()).log(Level.SEVERE, null, "No LocaleResolver found: not in a DispatcherServlet request?");
				return;
			}
			LocaleEditor localeEditor = new LocaleEditor();
			localeEditor.setAsText(newLocale);
			localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
		}
	}
}
