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
package com.krawler.spring.accounting.term;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.DefaultTerm;
import com.krawler.hql.accounting.Term;
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
public class accTermImpl implements accTermDAO {
    private HibernateTemplate hibernateTemplate;
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

    public KwlReturnObject addTerm(HashMap<String, Object> termMap) throws ServiceException {
        List list = new ArrayList();
        try {
            Term term = new Term();
            term = buildTerm(term, termMap);
            hibernateTemplate.save(term);
            list.add(term);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addTerm : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Term has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateTerm(HashMap<String, Object> termMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String termid = (String) termMap.get("termid");
            Term term = (Term) hibernateTemplate.get(Term.class, termid);
            if(term != null) {
                term = buildTerm(term, termMap);
                hibernateTemplate.saveOrUpdate(term);
            }
            list.add(term);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateTerm : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Term has been updated successfully", null, list, list.size());
    }

    public Term buildTerm(Term term, HashMap<String, Object> termMap) {
        if (termMap.containsKey("termdays")) {
            term.setTermdays((Integer) termMap.get("termdays"));
        }
        if (termMap.containsKey("termname")) {
            term.setTermname((String) termMap.get("termname"));
        }
        if (termMap.containsKey("companyid")) {
            Company company = termMap.get("companyid")==null?null:(Company)hibernateTemplate.get(Company.class, (String) termMap.get("companyid"));
            term.setCompany(company);
        }
        return term;
    }

    public KwlReturnObject getTerm(HashMap<String, Object> filterParams) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition = "";
        String query="from Term ";

        if(filterParams.containsKey("companyid")){
            condition += (condition.length()==0?" where ":" and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        query += condition;
//        query="from Term where company.companyID=?";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject deleteTerm(String termid, String companyid) throws ServiceException {
        String delQuery = "delete from Term c where c.ID=? and c.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{termid, companyid});
        return new KwlReturnObject(true, "Term entry has been deleted successfully.", null, null, numRows);
    }

    public void copyTerms(String companyid) throws ServiceException {
        try {
            String query = "from DefaultTerm";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query);
            Iterator iter = list.iterator();
            Company company = (Company)hibernateTemplate.get(Company.class, companyid);
            while (iter.hasNext()) {
                DefaultTerm dt = (DefaultTerm) iter.next();
                Term term = new Term();
                term.setCompany(company);
                term.setTermdays(dt.getTermdays());
                term.setTermname(dt.getTermname());
                hibernateTemplate.save(term);
            }
        } catch (Exception ex) {
            throw  ServiceException.FAILURE("copyTerms : "+ex.getMessage(), ex);
        }
    }
}
