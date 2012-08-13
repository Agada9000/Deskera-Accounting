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
package com.krawler.spring.common;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.KWLDateFormat;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author Karthik
 */
public class kwlCommonTablesDAOImpl implements kwlCommonTablesDAO{

    private HibernateTemplate hibernateTemplate;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public KwlReturnObject getObject(String classpath, String id) throws ServiceException {
        List list = new ArrayList();
        try {
            Class cls = Class.forName(classpath);
            Object obj = hibernateTemplate.get(cls, id);
            list.add(obj);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(kwlCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public Object getClassObject(String classpath, String id) throws ServiceException {
        Object obj = null;
        try {
            Class cls = Class.forName(classpath);
            obj = hibernateTemplate.get(cls, id);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(kwlCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return obj;
    }

    public static Object getObject(HibernateTemplate hibernateTemplate, String classpath, String id) throws ServiceException {
        Object obj = null;
        try {
            Class cls = Class.forName(classpath);
            obj = hibernateTemplate.get(cls, id);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(kwlCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return obj;
    }

    public KwlReturnObject getAllTimeZones() throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String query = "from KWLTimeZone";
            ll = HibernateUtil.executeQuery(hibernateTemplate, query);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getAllTimeZones", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getAllCurrencies() throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String query = "from KWLCurrency";
            ll = HibernateUtil.executeQuery(hibernateTemplate, query);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getAllCurrencies", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getAllCountries() throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String query = "from Country";
            ll = HibernateUtil.executeQuery(hibernateTemplate, query);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getAllCountries", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getAllDateFormats() throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String query = "from KWLDateFormat";
            ll = HibernateUtil.executeQuery(hibernateTemplate, query);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getCompanyInformation", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public DateFormat getUserDateFormatter(String dateFormatId, String userTimeFormatId, String timeZoneDiff) throws ServiceException {
        SimpleDateFormat sdf = null;
        try {
            KWLDateFormat df = (KWLDateFormat) hibernateTemplate.load(KWLDateFormat.class, dateFormatId);
            String dateformat = "";
            if (userTimeFormatId.equals("1")) {
                dateformat = df.getJavaForm().replace('H', 'h');
                if (!dateformat.equals(df.getJavaForm())) {
                    dateformat += " a";
                }
            } else {
                dateformat = df.getJavaForm();
            }
            sdf = new SimpleDateFormat(dateformat);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT" + timeZoneDiff));
        } catch (Exception e) {
            throw ServiceException.FAILURE("authHandlerDAOImpl.getUserDateFormatter", e);
        }
        return sdf;
    }

    public String currencyRender(String currency, String currencyid) throws SessionExpiredException {
        KWLCurrency cur = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, currencyid);
        String symbol = cur.getHtmlcode();
        char temp = (char) Integer.parseInt(symbol, 16);
        symbol = Character.toString(temp);
        float v = 0;
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        if (currency.equals("")) {
            return symbol;
        }
        v = Float.parseFloat(currency);
        String fmt = decimalFormat.format(v);
        fmt = symbol + fmt;
        return fmt;
    }

    public KwlReturnObject getEditHelpComponent(HashMap requestMap) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            ArrayList al = new ArrayList();
            String modname = (String)requestMap.get("modname");
            al.add(modname);
            String query = "select c from EditHelp c where c.modeid= ? order by (c.id * 1)";
            ll = HibernateUtil.executeQuery(hibernateTemplate, query, al.toArray());
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("kwlCommonTablesDAOImpl.getEditHelpComponent", e);
        }
        return new KwlReturnObject(true, "", "", ll, dl);
    }
}
