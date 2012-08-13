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
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.KWLDateFormat;
import com.krawler.common.admin.KWLTimeZone;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author Karthik
 */
public class authHandlerDAOImpl implements authHandlerDAO {

    private HibernateTemplate hibernateTemplate;
    private sessionHandlerImpl sessionHandlerImplObj;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public KwlReturnObject verifyLogin(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        String username = "";
        String passwd = "";
        String subdomain = "";
        try {
            List params = new ArrayList();
            String filterString = "";
            
            if (requestParams.containsKey("user") && !StringUtil.isNullOrEmpty(requestParams.get("user").toString())) {
                username = requestParams.get("user").toString();
                params.add(username);
                filterString += " and u.userLogin.userName = ?";
            }
            if (requestParams.containsKey("pass") && !StringUtil.isNullOrEmpty(requestParams.get("pass").toString())) {
                passwd = requestParams.get("pass").toString();
                params.add(passwd);
                filterString += " and u.userLogin.password = ?";
            }
            if (requestParams.containsKey("subdomain") && !StringUtil.isNullOrEmpty(requestParams.get("subdomain").toString())) {
                subdomain = requestParams.get("subdomain").toString();
                params.add(subdomain);
                filterString += " and u.company.subDomain=?";
            }

            String Hql = "select u, u.userLogin, u.company from User as u where u.company.deleted=0 and u.deleteflag = 0 and u.company.activated = true "+filterString;
            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql, params.toArray());
            dl = ll.size();

            KWLTimeZone timeZone = (KWLTimeZone) hibernateTemplate.load(KWLTimeZone.class, "1");
            KWLDateFormat dateFormat = (KWLDateFormat) hibernateTemplate.load(KWLDateFormat.class, "1");
            KWLCurrency currency = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, "1");

            ll.add(timeZone);
            ll.add(dateFormat);
            ll.add(currency);
        } catch (Exception e) {
            throw ServiceException.FAILURE("authHandlerDAOImpl.verifyLogin", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getPreferences(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        String timeZoneId = "";
        String dateFormatId = "";
        String currencyId = "";
        try {
            if (requestParams.containsKey("timezoneid") && !StringUtil.isNullOrEmpty(requestParams.get("timezoneid").toString())) {
                timeZoneId = requestParams.get("timezoneid").toString();
            }
            if (requestParams.containsKey("dateformatid") && !StringUtil.isNullOrEmpty(requestParams.get("dateformatid").toString())) {
                dateFormatId = requestParams.get("dateformatid").toString();
            }
            if (requestParams.containsKey("currencyid") && !StringUtil.isNullOrEmpty(requestParams.get("currencyid").toString())) {
                currencyId = requestParams.get("currencyid").toString();
            }

            KWLTimeZone timeZone = (KWLTimeZone) hibernateTemplate.load(KWLTimeZone.class, timeZoneId);
            KWLDateFormat dateFormat = (KWLDateFormat) hibernateTemplate.load(KWLDateFormat.class, dateFormatId);
            KWLCurrency currency = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, currencyId);

            ll.add(timeZone);
            ll.add(dateFormat);
            ll.add(currency);
        } catch (Exception e) {
            throw ServiceException.FAILURE("authHandlerDAOImpl.getPreferences", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public DateFormat getUserDateFormatter(String formatid, String diff, boolean onlydate) {
        KWLDateFormat df = (KWLDateFormat) hibernateTemplate.get(KWLDateFormat.class, formatid);
        String format = df.getJavaForm();
        int pos = format.length();
        if (onlydate) {
            pos = df.getJavaSeperatorPosition();
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format.substring(0, pos));
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + diff));
        return sdf;
    }

    public String getFormattedCurrency(double value, String currencyid) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        KWLCurrency c = (KWLCurrency) hibernateTemplate.get(KWLCurrency.class, currencyid);
        //Temp fix need to find out the html code of INR
          //Temp fix need to find out the html code of INR
//                if(c.getHtmlcode().equals("20A8"))
//                    return "Rs. " +df.format(value);
        String sym;
        try{
            sym=new Character((char)Integer.parseInt(c.getHtmlcode(),16)).toString();
        }catch(Exception e){
            sym=c.getHtmlcode();
        }
        return sym+" "+df.format(value);
    }
    
    @SuppressWarnings("finally")
	public  String getSysEmailIdByCompanyID(String companyid){
    String emailId = "admin@deskera.com";
        try {
            Company company = (Company)hibernateTemplate.get(Company.class, companyid);
            if(company!=null){
                emailId = company.getEmailID();
                if(StringUtil.isNullOrEmpty(emailId)){
                    emailId = company.getCreator().getEmailID();                   
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{   
            return emailId;
        }
    }
}
