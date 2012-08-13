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

package com.krawler.hql.accounting;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class RepeatedInvoices {
    private String id;
    private int intervalUnit;
    private String intervalType;  //Day, Week, Month
    private Date startDate;
    private Date nextDate;
    private Date expireDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public int getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(int intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public String getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(String intervalType) {
        this.intervalType = intervalType;
    }

    public Date getNextDate() {
        return nextDate;
    }

    public void setNextDate(Date nextDate) {
        this.nextDate = nextDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public static Date calculateNextDate(Date startDate, int intervalUnit, String intervalType){
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        if (intervalType.equalsIgnoreCase("day")) {
            cal.add(Calendar.DATE, intervalUnit);
        } else if (intervalType.equalsIgnoreCase("week")) {
            cal.add(Calendar.DATE, intervalUnit * 7);
        } else if (intervalType.equalsIgnoreCase("month")) {
            cal.add(Calendar.MONTH, intervalUnit);
        }
        return cal.getTime();
    }
}
