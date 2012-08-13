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
import com.krawler.common.admin.CompanyType;
import java.util.Date;


public class CompanyAccountPreferences {
    private String ID;
    private Date financialYearFrom;
    private Date firstFinancialYearFrom;
    private Date bookBeginningFrom;
    private Account discountGiven;
    private Account discountReceived;
    private Account shippingCharges;
    private Account otherCharges;
    private Account cashAccount;
    private Account foreignexchange;
    private Account depereciationAccount;
    private String journalEntryNumberFormat;
    private String creditNoteNumberFormat;
    private String invoiceNumberFormat;
    private String receiptNumberFormat;
    private String debitNoteNumberFormat;
    private String purchaseOrderNumberFormat;
    private String salesOrderNumberFormat;
    private String goodsReceiptNumberFormat;
    private String cashSaleNumberFormat;
    private String cashPurchaseNumberFormat;
    private String paymentNumberFormat;
    private String billingInvoiceNumberFormat;
    private String billingReceiptNumberFormat;
    private String billingCashSaleNumberFormat;
    private String billingGoodsReceiptNumberFormat;
    private String billingPaymentNumberFormat;
    private String billingCashPurchaseNumberFormat;
    private String billingCreditNoteNumberFormat;
    private String billingDebitNoteNumberFormat;
    private String billingPurchaseOrderNumberFormat;
    private String billingSalesOrderNumberFormat;
    private boolean emailInvoice;
    private boolean withoutInventory;
    private boolean withoutTax1099;
    private boolean setupDone;
    private CompanyType companyType;
    private Company company;
    private String quotationNumberFormat;
    private boolean currencyChange;
    private boolean countryChange;
    
    public boolean isCurrencyChange() {
		return currencyChange;
	}

	public void setCurrencyChange(boolean currencyChange) {
		this.currencyChange = currencyChange;
	}

	public boolean isCountryChange() {
		return countryChange;
	}

	public void setCountryChange(boolean countryChange) {
		this.countryChange = countryChange;
	}

	public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Date getBookBeginningFrom() {
        return bookBeginningFrom;
    }

    public void setBookBeginningFrom(Date bookBeginningFrom) {
        this.bookBeginningFrom = bookBeginningFrom;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getFinancialYearFrom() {
        return financialYearFrom;
    }

    public void setFinancialYearFrom(Date financialYearFrom) {
        this.financialYearFrom = financialYearFrom;
    }

    public Date getFirstFinancialYearFrom() {
        return firstFinancialYearFrom;
    }

    public void setFirstFinancialYearFrom(Date firstFinancialYearFrom) {
        this.firstFinancialYearFrom = firstFinancialYearFrom;
    }

    public Account getDiscountGiven() {
        return discountGiven;
    }

    public void setDiscountGiven(Account discountGiven) {
        this.discountGiven = discountGiven;
    }

    public Account getDiscountReceived() {
        return discountReceived;
    }

    public void setDiscountReceived(Account discountReceived) {
        this.discountReceived = discountReceived;
    }

    public Account getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(Account otherCharges) {
        this.otherCharges = otherCharges;
    }

    public Account getShippingCharges() {
        return shippingCharges;
    }

    public void setShippingCharges(Account shippingCharges) {
        this.shippingCharges = shippingCharges;
    }

    public Account getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(Account cashAccount) {
        this.cashAccount = cashAccount;
    }

    public String getCreditNoteNumberFormat() {
        return creditNoteNumberFormat;
    }

    public void setCreditNoteNumberFormat(String creditNoteNumberFormat) {
        this.creditNoteNumberFormat = creditNoteNumberFormat;
    }
    
    public String getInvoiceNumberFormat() {
        return invoiceNumberFormat;
    }

    public void setInvoiceNumberFormat(String invoiceNumberFormat) {
        this.invoiceNumberFormat = invoiceNumberFormat;
    }

    public String getJournalEntryNumberFormat() {
        return journalEntryNumberFormat;
    }

    public void setJournalEntryNumberFormat(String journalEntryNumberFormat) {
        this.journalEntryNumberFormat = journalEntryNumberFormat;
    }

    public String getReceiptNumberFormat() {
        return receiptNumberFormat;
    }
    public void setReceiptNumberFormat(String receiptNumberFormat) {
        this.receiptNumberFormat = receiptNumberFormat;
    }

    public String getDebitNoteNumberFormat() {
        return debitNoteNumberFormat;
    }

    public void setDebitNoteNumberFormat(String debitNoteNumberFormat) {
        this.debitNoteNumberFormat = debitNoteNumberFormat;
    }

    public String getPaymentNumberFormat() {
        return paymentNumberFormat;
    }

    public void setPaymentNumberFormat(String paymentNumberFormat) {
        this.paymentNumberFormat = paymentNumberFormat;
    }

    public String getPurchaseOrderNumberFormat() {
        return purchaseOrderNumberFormat;
    }

    public void setPurchaseOrderNumberFormat(String purchaseOrderNumberFormat) {
        this.purchaseOrderNumberFormat = purchaseOrderNumberFormat;
    }

    public String getGoodsReceiptNumberFormat() {
        return goodsReceiptNumberFormat;
    }

    public void setGoodsReceiptNumberFormat(String goodsReceiptNumberFormat) {
        this.goodsReceiptNumberFormat = goodsReceiptNumberFormat;
    }
    
    public String getSalesOrderNumberFormat() {
        return salesOrderNumberFormat;
    }

    public void setSalesOrderNumberFormat(String salesOrderNumberFormat) {
        this.salesOrderNumberFormat = salesOrderNumberFormat;
    }

    public String getCashPurchaseNumberFormat() {
        return cashPurchaseNumberFormat;
    }

    public void setCashPurchaseNumberFormat(String cashPurchaseNumberFormat) {
        this.cashPurchaseNumberFormat = cashPurchaseNumberFormat;
    }

    public String getCashSaleNumberFormat() {
        return cashSaleNumberFormat;
    }

    public void setCashSaleNumberFormat(String cashSaleNumberFormat) {
        this.cashSaleNumberFormat = cashSaleNumberFormat;
    }

    public String getBillingInvoiceNumberFormat() {
        return billingInvoiceNumberFormat;
    }

    public void setBillingInvoiceNumberFormat(String billingInvoiceNumberFormat) {
        this.billingInvoiceNumberFormat = billingInvoiceNumberFormat;
    }

    public String getBillingReceiptNumberFormat() {
        return billingReceiptNumberFormat;
    }

    public void setBillingReceiptNumberFormat(String billingReceiptNumberFormat) {
        this.billingReceiptNumberFormat = billingReceiptNumberFormat;
    }

    public boolean isEmailInvoice() {
        return emailInvoice;
    }

    public void setEmailInvoice(boolean emailInvoice) {
        this.emailInvoice = emailInvoice;
    }

    public boolean isWithoutInventory() {
        return withoutInventory;
    }

    public void setWithoutInventory(boolean withoutInventory) {
        this.withoutInventory = withoutInventory;
    }

    public boolean isWithoutTax1099() {
        return withoutTax1099;
    }

    public void setWithoutTax1099(boolean withoutTax1099) {
        this.withoutTax1099 = withoutTax1099;
    }

    public String getBillingCashPurchaseNumberFormat() {
        return billingCashPurchaseNumberFormat;
    }

    public void setBillingCashPurchaseNumberFormat(String billingCashPurchaseNumberFormat) {
        this.billingCashPurchaseNumberFormat = billingCashPurchaseNumberFormat;
    }

    public String getBillingCashSaleNumberFormat() {
        return billingCashSaleNumberFormat;
    }

    public void setBillingCashSaleNumberFormat(String billingCashSaleNumberFormat) {
        this.billingCashSaleNumberFormat = billingCashSaleNumberFormat;
    }

    public String getBillingGoodsReceiptNumberFormat() {
        return billingGoodsReceiptNumberFormat;
    }

    public void setBillingGoodsReceiptNumberFormat(String billingGoodsReceiptNumberFormat) {
        this.billingGoodsReceiptNumberFormat = billingGoodsReceiptNumberFormat;
    }

    public String getBillingPaymentNumberFormat() {
        return billingPaymentNumberFormat;
    }

    public void setBillingPaymentNumberFormat(String billingPaymentNumberFormat) {
        this.billingPaymentNumberFormat = billingPaymentNumberFormat;
    }

    public String getBillingCreditNoteNumberFormat() {
        return billingCreditNoteNumberFormat;
    }

    public void setBillingCreditNoteNumberFormat(String billingCreditNoteNumberFormat) {
        this.billingCreditNoteNumberFormat = billingCreditNoteNumberFormat;
    }

    public String getBillingDebitNoteNumberFormat() {
        return billingDebitNoteNumberFormat;
    }

    public void setBillingDebitNoteNumberFormat(String billingDebitNoteNumberFormat) {
        this.billingDebitNoteNumberFormat = billingDebitNoteNumberFormat;
    }

    public String getBillingPurchaseOrderNumberFormat() {
        return billingPurchaseOrderNumberFormat;
    }

    public void setBillingPurchaseOrderNumberFormat(String billingPurchaseOrderNumberFormat) {
        this.billingPurchaseOrderNumberFormat = billingPurchaseOrderNumberFormat;
    }

    public String getBillingSalesOrderNumberFormat() {
        return billingSalesOrderNumberFormat;
    }

    public void setBillingSalesOrderNumberFormat(String billingSalesOrderNumberFormat) {
        this.billingSalesOrderNumberFormat = billingSalesOrderNumberFormat;
    }

     public Account getForeignexchange() {
        return foreignexchange;
     }

     public void setForeignexchange(Account foreignexchange) {
       this.foreignexchange = foreignexchange;
     }
    public Account getDepereciationAccount() {
        return depereciationAccount;
    }
    
    public void setDepereciationAccount(Account depereciationAccount) {
        this.depereciationAccount = depereciationAccount;
    }

    public boolean isSetupDone() {
        return setupDone;
    }

    public void setSetupDone(boolean setupDone) {
        this.setupDone = setupDone;
    }

    public CompanyType getCompanyType() {
        return companyType;
    }

    public void setCompanyType(CompanyType companyType) {
        this.companyType = companyType;
    }
    
    public String getQuotationNumberFormat() {
        return quotationNumberFormat;
    }

    public void setQuotationNumberFormat(String quotationNumberFormat) {
        this.quotationNumberFormat = quotationNumberFormat;
    }
}
