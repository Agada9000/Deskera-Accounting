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
<%@page import="com.krawler.esp.database.*" %>
<%@page import="com.krawler.utils.json.base.*" %>
<%@page import="java.util.*" %>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@page import="com.krawler.esp.handlers.*" %>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
        String result = "";
        
        String flag = request.getParameter("flag");
        String ls= request.getParameter("comboname");
        try{
           switch (Integer.parseInt(flag)) {
                case 1:
                    result = AccountDBCon.getAccountReceivableChart(request);
                    break;
                case 2:
                    result = AccountDBCon.getAccountPayableChart(request);
                    break;
                case 3:
                    result = AccountDBCon.getTopCustomerChart(request);
                break;
                case 4:
                    result = AccountDBCon.getTopVendorsChart(request);
                break;
                case 5:
                    result = AccountDBCon.getAccountReceivablePieChart(request);
                    break;
                case 6:
                    result = AccountDBCon.getAccountPayablePieChart(request);
                    break;
                default:
                    break;
           }
        } catch(Exception e) {
            e.getMessage();
        }
        out.print(result);
%>
