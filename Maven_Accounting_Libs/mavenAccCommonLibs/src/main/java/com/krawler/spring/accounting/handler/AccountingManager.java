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
package com.krawler.spring.accounting.handler;


import com.krawler.common.admin.Role;
import com.krawler.common.admin.User;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public class AccountingManager {
    public static String generateNextAutoNumber(String pattern, String strCurrent){
        StringBuffer strNext=new StringBuffer(pattern);
        int x=0;
        if(strCurrent!=null&&pattern.length()==strCurrent.length()){
            for(x=0;x<pattern.length();x++){
                if(pattern.charAt(x)=='0'&&(strCurrent.charAt(x)<'0'||strCurrent.charAt(x)>'9'))
                    break;
            }
        }
        if(x==pattern.length())
            strNext=new StringBuffer(strCurrent);
        int carry=1;
        for(int i=strNext.length()-1;i>=0;i--){
            if(pattern.charAt(i)!='0') continue;
            int sum=(strNext.charAt(i)-'0')+carry;
            strNext.setCharAt(i, (char)(sum%10+'0'));
            carry=sum/10;
        }
        return strNext.toString();
    }

    public static HashMap<String, Object> getGlobalParams(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
        requestMap.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
        requestMap.put(Constants.df, authHandler.getDateFormatter(request));
        return requestMap;
    }

   public static boolean isCompanyAdmin(User user){
        if(user!=null) {
            return user.getRoleID().equals(Role.COMPANY_ADMIN);
        }
        return false;
    }

    public static Date resetTimeField(Date date) {
        Date returnDate = date;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 12);//Reset Time field
            cal.set(Calendar.MINUTE, 00);
            cal.set(Calendar.SECOND, 00);
            returnDate = cal.getTime();
        } catch (Exception ex) {
        }
        return returnDate;
    }


     /* Function used reseting time field in dates used filtering the report having StartDate & EndDate as filters
     * e.g. For StartDate ==> setFilterTime("20101-12-01 02:34:56", true) ===> "20101-12-01 00:00:00"
     *      For EndDate ====> setFilterTime("20101-12-01 02:34:56", false) ==> "20101-12-01 23:59:59"
     * */
    public static Date setFilterTime(Date date, boolean isStartDate) {
        Date returnDate = date;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, isStartDate?00:23);
            cal.set(Calendar.MINUTE, isStartDate?00:59);
            cal.set(Calendar.SECOND, isStartDate?00:59);
            returnDate = cal.getTime();
        } catch (Exception ex) {
        }
        return returnDate;
    }
}
