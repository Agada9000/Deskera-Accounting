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
package com.krawler.spring.companyDetails;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CompanyHoliday;
import com.krawler.common.admin.Country;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.KWLTimeZone;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.FileUploadHandler;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.commons.fileupload.FileItem;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author Karthik
 */
public class companyDetailsDAOImpl implements companyDetailsDAO{

    private HibernateTemplate hibernateTemplate;
    private storageHandlerImpl storageHandlerImplObj;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public void setstorageHandlerImpl(storageHandlerImpl storageHandlerImplObj1) {
        this.storageHandlerImplObj = storageHandlerImplObj1;
    }
    
    public KwlReturnObject getCompanyInformation(HashMap<String, Object> requestParams, ArrayList filter_names, ArrayList filter_params) throws ServiceException {
        List ll = null;
        int dl = 0;
        String companyid = "";
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            String query = "from Company c ";
//            String query = "select c,cpr from CompanyPreferences c right outer join c.company cpr ";
            String filterQuery = StringUtil.filterQuery(filter_names, "where");
            query += filterQuery;

            ll = HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{companyid});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.getCompanyInformation", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getCompanyHolidays(HashMap<String, Object> requestParams, ArrayList filter_names, ArrayList filter_params) throws ServiceException {
        List ll = null;
        int dl = 0;
        String companyid = "";
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            String query = "from CompanyHoliday c ";
            String filterQuery = StringUtil.filterQuery(filter_names, "where");
            query += filterQuery;
            
            ll = HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{companyid});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.getCompanyInformation", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public void updateCompany(HashMap hm) throws ServiceException {
        String companyid = "";
        DateFormat dateformat = null;
        try {
            if (hm.containsKey("companyid") && hm.get("companyid") != null) {
                companyid = hm.get("companyid").toString();
            }
            if (hm.containsKey("dateformat") && hm.get("dateformat") != null) {
                dateformat = (DateFormat) hm.get("dateformat");
            }
            Company company = (Company) hibernateTemplate.load(Company.class, companyid);
            if (hm.containsKey("companyname") && hm.get("companyname") != null) {
                company.setCompanyName((String) hm.get("companyname"));
            }
            if (hm.containsKey("address") && hm.get("address") != null) {
                company.setAddress((String) hm.get("address"));
            }
            if (hm.containsKey("city") && hm.get("city") != null) {
                company.setCity((String) hm.get("city"));
            }
            if (hm.containsKey("state") && hm.get("state") != null) {
                company.setState((String) hm.get("state"));
            }
            if (hm.containsKey("zip") && hm.get("zip") != null) {
                company.setZipCode((String) hm.get("zip"));
            }
            if (hm.containsKey("phone") && hm.get("phone") != null) {
                company.setPhoneNumber((String) hm.get("phone"));
            }
            if (hm.containsKey("fax") && hm.get("fax") != null) {
                company.setFaxNumber((String) hm.get("fax"));
            }
            if (hm.containsKey("website") && hm.get("website") != null) {
                company.setWebsite((String) hm.get("website"));
            }
            if (hm.containsKey("mail") && hm.get("mail") != null) {
                company.setEmailID((String) hm.get("mail"));
            }
            if (hm.containsKey("domainname") && hm.get("domainname") != null) {
                company.setSubDomain((String) hm.get("domainname"));
            }
            if (hm.containsKey("country") && hm.get("country") != null) {
                company.setCountry((Country) hibernateTemplate.load(Country.class, (String) hm.get("country")));
            }
            if (hm.containsKey("currency") && hm.get("currency") != null) {
                company.setCurrency((KWLCurrency) hibernateTemplate.load(KWLCurrency.class, (String) hm.get("currency")));
            }
            if (hm.containsKey("timezone") && hm.get("timezone") != null) {
                KWLTimeZone timeZone = (KWLTimeZone) hibernateTemplate.load(KWLTimeZone.class, (String) hm.get("timezone"));
                company.setTimeZone(timeZone);
            }
            company.setModifiedOn(new Date());
            if (hm.containsKey("holidays") && hm.get("holidays") != null) {
                JSONArray jArr = new JSONArray((String) hm.get("holidays"));
                Set<CompanyHoliday> holidays = company.getHolidays();
                holidays.clear();
                DateFormat formatter = dateformat;
                for (int i = 0; i < jArr.length(); i++) {
                    CompanyHoliday day = new CompanyHoliday();
                    JSONObject obj = jArr.getJSONObject(i);
                    day.setDescription(obj.getString("description"));
                    day.setHolidayDate(formatter.parse(obj.getString("day")));
                    day.setCompany(company);
                    holidays.add(day);
                }
            }
            if (hm.containsKey("logo") && hm.get("logo") != null) {
                String imageName = ((FileItem) (hm.get("logo"))).getName();
                if (imageName != null && imageName.length() > 0) {
                    String fileName = companyid + FileUploadHandler.getCompanyImageExt();
                    company.setCompanyLogo(Constants.ImgBasePath + fileName);
                    new FileUploadHandler().uploadImage((FileItem) hm.get("logo"),
                            fileName,
                            storageHandlerImplObj.GetProfileImgStorePath(), 130, 25, true, false);
                }
            }
            hibernateTemplate.update(company);
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.updateCompany", e);
        }
    }

    public void deleteCompany(HashMap<String, Object> requestParams) throws ServiceException {
        String companyid = "";
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            
            Company company = (Company) hibernateTemplate.load(Company.class, companyid);
            company.setDeleted(1);
            
            hibernateTemplate.update(company);
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.deleteCompany", e);
        }
    }

    public String getSubDomain(String companyid) throws ServiceException {
        String subdomain = "";
        try {
            Company company = (Company) hibernateTemplate.get(Company.class, companyid);
            subdomain = company.getSubDomain();
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.getSubDomain", e);
        }
        return subdomain;
    }

    public String getCompanyid(String domain) throws ServiceException {
        String companyId = "";
        List ll = new ArrayList();
        try {
            String Hql = "select companyID from Company where subDomain = ?";
            ll = HibernateUtil.executeQuery(hibernateTemplate, Hql, new Object[]{domain});
            companyId = companyDetailsHandler.getCompanyid(ll);
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.getCompanyid", e);
        }
        return companyId;
    }
}
