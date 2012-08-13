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

import com.krawler.common.admin.Company;
import java.util.Date;

/**
 *
 * @author krawler-user
 */
public class Vendor {
    private String ID;
    private String title;
    private String name;
    private String address;
    private Company company;
    private String email;
    private String contactNumber;
    private String altContactNumber;
    private String fax;
    private int preferedDeliveryMode;
    private Term debitTerm;
    private String bankaccountno;
    private String other;
    private boolean taxEligible;
    private String taxIDNumber;
    private Account account;
    private Date createdOn;
    private Date modifiedOn;
    private Date mailOn;
    private boolean taxSet;


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAltContactNumber() {
        return altContactNumber;
    }

    public void setAltContactNumber(String altContactNumber) {
        this.altContactNumber = altContactNumber;
    }


    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBankaccountno() {
        return bankaccountno;
    }

    public void setBankaccountno(String other) {
        this.bankaccountno = other;
    }
    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isTaxEligible() {
        return taxEligible;
    }

    public void setTaxEligible(boolean taxEligible) {
        this.taxEligible = taxEligible;
    }

    public String getTaxIDNumber() {
        return taxIDNumber;
    }

    public void setTaxIDNumber(String taxIDNumber) {
        this.taxIDNumber = taxIDNumber;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public Term getDebitTerm() {
        return debitTerm;
    }

    public void setDebitTerm(Term debitTerm) {
        this.debitTerm = debitTerm;
    }

    public int getPreferedDeliveryMode() {
        return preferedDeliveryMode;
    }

    public void setPreferedDeliveryMode(int preferedDeliveryMode) {
        this.preferedDeliveryMode = preferedDeliveryMode;
    }

    public Date getMailOn() {
        return mailOn;
    }

    public void setMailOn(Date mailOn) {
        this.mailOn = mailOn;
    }

    public boolean isTaxSet() {
        return taxSet;
    }

    public void setTaxSet(boolean taxSet) {
        this.taxSet = taxSet;
    }
    
}
