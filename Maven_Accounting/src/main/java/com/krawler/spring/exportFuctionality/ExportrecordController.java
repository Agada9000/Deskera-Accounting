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
package com.krawler.spring.exportFuctionality;

import com.krawler.common.util.StringUtil;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.hql.accounting.Account;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class ExportrecordController extends MultiActionController {

    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private ExportRecord ExportrecordObj;

    public void setExportRecord(ExportRecord ExportrecordObj) {
        this.ExportrecordObj = ExportrecordObj;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }

    public ModelAndView exportRecords(HttpServletRequest request,
            HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
//        Session session = HibernateUtil.getCurrentSession();
        try {
            String filename = request.getParameter("filename") + ".pdf";
            ByteArrayOutputStream baos = null;
            double amount = Double.parseDouble(request.getParameter("amount"));
            int mode = Integer.parseInt(request.getParameter("mode"));
            String billid = request.getParameter("bills");
            boolean isexpenseinv=false;
            if(!StringUtil.isNullOrEmpty(request.getParameter("isexpenseinv")))
                isexpenseinv = Boolean.parseBoolean((String)request.getParameter("isexpenseinv"));
            String cust = request.getParameter("customer");
            String accname = request.getParameter("accname");
            String address = request.getParameter("address");
            String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
//            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Account.class.getName(), request.getParameter("personid"));
            Account account = (Account) cap.getEntityList().get(0);
            //Account account=(Account)session.get(Account.class, request.getParameter("personid"));
            String currencyid = account == null ? sessionHandlerImpl.getCurrencyID(request) : account.getCurrency().getCurrencyID();
            DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);

            baos = ExportrecordObj.createPdf(request, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address,isexpenseinv);
            if (baos != null) {
                ExportrecordObj.writeDataToFile(filename, baos, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            HibernateUtil.closeSession(session);
        }
        return new ModelAndView("", "", "");
    }

}
