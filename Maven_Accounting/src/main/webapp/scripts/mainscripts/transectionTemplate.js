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
Wtf.account.TransectionTemplate=function(config){
    this.isCustomer=config.isCustomer;
    this.isCustBill=config.isCustBill;
    this.inCash=config.inCash;
    this.isEdit=config.isEdit;
    this.isBillReceipt=config.isBillReceipt;
    this.businessPerson='Customer';
    if(config.isCustomer==false)
        this.businessPerson='Vendor';
    this.label=config.label;
    this.name = config.name;
    this.noteTemp=false;
    this.receiptTemp=false;
    this.isCard=false;
    this.isBank=false;
    this.isexpenseinv=false;
/*
    this.storeMode=this.isCustBill?16:12;
    if(this.label=='Credit Note'){
        this.noteTemp=true;
        this.storeMode=this.isCustBill?62:27;
    }
    if(this.label=='Debit Note'){
        this.noteTemp=true;
        this.storeMode=this.isCustBill?62:28;
    }
    if(this.label=='Receive Payment' || this.label=='Payment Made'){
        this.receiptTemp=true;
        this.storeMode=this.isCustBill?35:32;
    }
*/
    this.StoreUrl = "";
    this.subGridStoreUrl = "";
    if (this.businessPerson=='Customer') {
        this.storeMode = this.isCustBill?16:12;
        this.StoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoices.do":"ACCInvoiceCMN/getInvoices.do";
        this.subGridStoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoiceRows.do":"ACCInvoiceCMN/getInvoiceRows.do";

        if(this.label=='Credit Note'){
            this.noteTemp=true;
            this.storeMode = this.isCustBill?62:27;
            this.StoreUrl = this.isCustBill?"ACCCreditNote/getBillingCreditNote.do":"ACCCreditNote/getCreditNote.do";
            this.subGridStoreUrl = this.isCustBill?"ACCCreditNote/getBillingCreditNoteRows.do":"ACCCreditNote/getCreditNoteRows.do";
        }

        if(this.label=='Receive Payment' || this.label=='Payment Made'){
            this.receiptTemp=true;
            this.storeMode=this.isCustBill?35:32;
            this.StoreUrl = this.isCustBill?"ACCReceipt/getBillingReceipts.do":"ACCReceipt/getReceipts.do";
            this.subGridStoreUrl = this.isCustBill?"ACCReceipt/getBillingReceiptRows.do":"ACCReceiptCMN/getReceiptRows.do";
        }
    } else if (this.businessPerson=='Vendor') {
        this.storeMode = this.isCustBill?16:12;
        this.StoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do":"ACCGoodsReceiptCMN/getGoodsReceipts.do";
        this.subGridStoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceiptRows.do":"ACCGoodsReceiptCMN/getGoodsReceiptRows.do";

        if(this.label=='Debit Note'){
            this.noteTemp=true;
            this.storeMode = this.isCustBill?62:28;
            this.StoreUrl = this.isCustBill?"ACCDebitNote/getBillingDebitNote.do":"ACCDebitNote/getDebitNote.do";
            this.subGridStoreUrl = this.isCustBill?"ACCDebitNote/getBillingDebitNoteRows.do":"ACCDebitNote/getDebitNoteRows.do";
        }
        if(this.label=='Receive Payment' || this.label=='Payment Made'){
            this.receiptTemp=true;
            this.storeMode = this.isCustBill?35:32;
            this.StoreUrl = this.isCustBill?"ACCVendorPayment/getBillingPayments.do":"ACCVendorPayment/getPayments.do";
            this.subGridStoreUrl = this.isCustBill?"ACCVendorPayment/getBillingPaymentRows.do":"ACCVendorPaymentCMN/getPaymentRows.do";
        }
    }
    this.GridRec = Wtf.data.Record.create ([
        {name:'id'},
        {name:'billid'},
        {name:'billno'},
        {name:'billdate', type:'date'},
        {name:'paymentmethod'},
        {name:'noteid'},
        {name:'noteno'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'discount'},
        {name:'shipto'},
        {name:'refname'},
        {name:'refno'},
        {name:'mode'},
        {name:'taxamount'},
        {name:'no'},
        {name:'creationdate', type:'date', mapping:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'customername'},
        {name:'personid'},
        {name:'personname'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'amount'},
        {name:'oldcurrencyrate'},
        {name: 'currencysymbol'},
        {name: 'currencyrate'},
        {name:'amountdue'},
        {name:'companyaddress'},
        {name:'companyname'},
        {name:'memo'},
        {name:'prtaxid'},
        {name:'taxamount'},
        {name: 'isexpenseinv'},
        {name:'prtaxpercent'}
    ]);
    this.store = new Wtf.data.Store({
        url : this.StoreUrl,
//        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
        baseParams:{
            mode:this.storeMode
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.GridRec)
    });
    this.store.on('load',this.loadTemplate,this);
    Wtf.account.TransectionTemplate.superclass.constructor.call(this,config);
    this.addEvents({
        update:true
    });
}
Wtf.extend(Wtf.account.TransectionTemplate,Wtf.Panel,{
    layout: 'border',
    closable: true,
    style:'padding:10px',
    border:true,
    onRender:function(config){
        this.addtemplate();
        this.addCalTemp();
        this.addForm();
        this.add(this.NorthForm);
        this.add(this.grid);
        this.add(this.SouthForm);
        Wtf.account.TransectionTemplate.superclass.onRender.call(this,config);
    },
    addForm:function(){
        this.NorthForm=new Wtf.form.FormPanel({
            region : "north",
            height:170,
            border:false,
            style:'background:#F1F1F1;padding:20px 10px 0px 30px',
            defaults:{border:false},
            labelWidth:140,
            items:[{
                layout:'form',
                defaults:{border:false},
                items:[this.headerCalTemp]
               },{
                layout:'column',
                defaults:{border:false},
                items:[{
                    layout:'form',
                    columnWidth:0.7,
                    defaults:{border:false},
                    items:[this.leftCalTemp]
                },{
                    layout:'form',
                    columnWidth:0.3,
                    items:[this.rightCalTemp]
                }]
            }]
        });
        this.createGrid();
        this.SouthForm=new Wtf.Panel({
            region:'south',
            height:145,
            border:false,
            bodyStyle:'background:#F1F1F1;',
            layout:'border',
            defaults:{border:false},
            items:[{
                region:'center',
                bodyStyle:'padding:70px 0px 0px 40px',
                defaults:{border:false},
                items:[this.leftSouthTemp]
           },{
                region:'east',
                bodyStyle:'padding:10px 0px 20px 0px',
                width:320,
                defaults:{border:false},
                items:[this.rightSouthTemp]
            }]
        });
    },
    addCalTemp:function(){
         this.headerCalTemp=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            html:this.headerTplSummary.apply({transectionno:""})
        });
        this.leftCalTemp=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            html:this.leftTplSummary.apply({to:"",Address:"",totalamount:""})
        });
// TODO       this.centerCalTemp=new Wtf.Panel({
//            border:false,
//            baseCls:'tempbackgroundview',
//            html:this.centerTplSummary.apply({from:"",compaddress:""})
//        });

        this.rightCalTemp=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            html:this.rightTplSummary.apply({transectionno:"",createdon:"",duedate:"",total:0})
        });
        this.leftSouthTemp=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            html:this.leftSouthTplSummary.apply({memo:""})
        });
        this.rightSouthTemp=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            html:this.rightSouthTplSummary.apply({subtotal:0,discount:0,total:0,tax:0,aftertaxamt:0})
        });
    },
    addLeftTemp:function(){
        var paymentmethod='';
         if(this.receiptTemp){
            paymentmethod='<trclass="templabelview"><td class="tempdataview"><b>Payment Type:</b></td><td >{paymentmethod}  </td>';
            paymentmethod+='<tpl if="'+this.Bank+'">',
            paymentmethod+='<td class="tempdataview"><b>Check No:</b></td><td >{refno}  </td>';
            paymentmethod+='<td class="tempdataview"><b>Bank Name:</b></td><td >{refname}  </td></tr>';
            paymentmethod+='</tpl>',
            paymentmethod+='<tpl if="'+this.Card+'">',
            paymentmethod+='<td class="tempdataview"><b>Reference No:</b></td><td >{refno}</td>';
            paymentmethod+='<td class="tempdataview"><b>Card Name:</b></td><td >{refname}</td></tr>';
            paymentmethod+='</tpl>'
        }
        this.headerTplSummary=new Wtf.XTemplate(
            '<div class="currency-view tempheaderview" >'+this.name+' '+WtfGlobal.getLocaleText("acc.nee.50")+': {transectionno}</div>'
        );
        this.leftTplSummary=new Wtf.XTemplate(
            '<div class="currency-view temptextview" >',
            '<table >',
            '<tr><td class="templabelview"><b>'+WtfGlobal.getLocaleText("acc.common.to")+',</b></td></tr>',
            '<tr><td class="tempdataview">{to}</td></tr>',
            '<tpl if="'+!(this.noteTemp ||this.receiptTemp)+'">',
            '<tr><td class="tempdataview">{address}</td></tr>',
            '</tpl>','</table>',
            '<br ><br >',
            '<table >',
            paymentmethod,
            '</table>',
            '</div>'
        );
    },

    addtemplate:function(){
        
       this.addLeftTemp(); 

        this.rightTplSummary=new Wtf.XTemplate(
            '<div class="currency-view temptabledivview">',
            '<table class="template">',
            '<tr><td class="templabelview"><b>'+WtfGlobal.getLocaleText("acc.nee.51")+': </b></td><td class="templabelview">{createdon}</td></tr>',
            '<tpl if="'+!(this.noteTemp ||this.receiptTemp)+'">',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.agedPay.gridDueDate")+': </b></td><td >{duedate}</td></tr>',
            '<tr><td class="templabelview"><b>'+(!(this.noteTemp ||this.receiptTemp)?WtfGlobal.getLocaleText("acc.agedPay.gridAmtDue"):WtfGlobal.getLocaleText("acc.invoiceList.totAmt"))+':</b></td><td class="templabelview">{total}</td></tr>',
            '</tpl>',
            '</table>',
            '</div>'
        );
        this.rightSouthTplSummary=new Wtf.XTemplate(
            '<div class="currency-view temptextview">',
            '<table class="template">',
            '<tpl if="'+(!this.noteTemp)+'">',
            '<tr><td class="templabelview"><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+'</b></td><td class="templabelview">{subtotal}</td></tr>',
            '</tpl>',
            '<tpl if="'+!(this.noteTemp ||this.receiptTemp)+'">',
            '<tr><td><b>- '+WtfGlobal.getLocaleText("acc.invoiceList.discount")+': </b></td><td >{discount}</td></tr>',
            '</table>',
            '<table class="template">',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoiceList.expand.amt")+': </b></td><td align=right>{total}</td></tr>',
            '<tr><td><b>+ '+WtfGlobal.getLocaleText("acc.up.12")+': </b></td><td align=right>{tax}</td></tr>',
            '</table>',
            '</tpl>',
            '<tpl if="'+(this.noteTemp)+'">',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+' </b></td><td align=right>{noteSubTotal}</td></tr>',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.up.12")+': </b></td><td align=right>{noteTax}</td></tr>',
            '</tpl>',
            '<table class="template"><tr><td class="templabelview"><b>'+WtfGlobal.getLocaleText("acc.invoiceList.totAmt")+':</b></td><td class="templabelview">{aftertaxamt}</td></tr>',
            '</table>',
            '</div>'
        );
        this.leftSouthTplSummary=new Wtf.XTemplate(
            '<div class="currency-view tempmemoview">',
            '<b>'+WtfGlobal.getLocaleText("acc.common.memo")+' : </b>{memo}',
            '</div>'
        );
    },
    loadTemplate:function(store){
        var data=this.rec.data;
        var id;
        var idlabel;
        var  mode;
        if(this.noteTemp){
            id= data.noteid;
            idlabel="noteid";
            if(this.label=='Debit Note'){
                mode=this.isCustBill?63:29;
            }else{
                mode=this.isCustBill?63:28;
            }
        }else if(this.receiptTemp){ 
            id= data.billid;
            idlabel="billid";
            mode=this.isCustBill?36:33;
        }else{
            id=data.billid;
            idlabel="billid";
            mode=this.isCustBill?17:14;
        }
        store.filter(idlabel,id);
        var fRec=store.getAt(0);
        this.Bank=fRec.data['paymentmethod']=="Cheque"?true:false;
        this.Card=fRec.data['paymentmethod']=="Card"?true:false;
        this.addtemplate(); 
        var no=this.noteTemp?fRec.data['noteno']:fRec.data['billno'];
        this.grid.getStore().proxy.conn.url = this.subGridStoreUrl;
        this.grid.getStore().load({params:{bills:id,mode:mode,isexpenseinv:this.isexpenseinv}});
        this.grid.getStore().on('load',this.getCurrencySymbol,this);
        this.headerTplSummary.overwrite(this.headerCalTemp.body,{transectionno:no});
        this.leftTplSummary.overwrite(this.leftCalTemp.body,{to:fRec.data['personname'],address:fRec.data['billto'],paymentmethod:fRec.data['paymentmethod'],refno:fRec.data['refno'],refname:fRec.data['refname']});
     // TODO  this.centerTplSummary.overwrite(this.centerCalTemp.body,{from:fRec.data['companyname'],compaddress:fRec.data['companyaddress'],paymentmethod:fRec.data['paymentmethod']});
        this.rightTplSummary.overwrite(this.rightCalTemp.body,{transectionno:fRec.data['no'],createdon:this.receiptTemp?WtfGlobal.onlyDateRightRenderer(fRec.data['billdate']):WtfGlobal.onlyDateRightRenderer(fRec.data['creationdate']),duedate:WtfGlobal.onlyDateRightRenderer(fRec.data['duedate']),total:WtfGlobal.addCurrencySymbolOnly((!(this.noteTemp ||this.receiptTemp)?fRec.data['amountdue']:fRec.data['amount']),fRec.data['currencysymbol'])});
        this.leftSouthTplSummary.overwrite(this.leftSouthTemp.body,{memo:fRec.data['memo']});
        this.rightSouthTplSummary.overwrite(this.rightSouthTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(fRec.data['amount']+fRec.data['discount']-fRec.data['taxamount'],fRec.data['currencysymbol']),discount:WtfGlobal.addCurrencySymbolOnly(fRec.data['discount'],fRec.data['currencysymbol']),total:WtfGlobal.addCurrencySymbolOnly(fRec.data['amount']-fRec.data['taxamount'],fRec.data['currencysymbol']),tax:WtfGlobal.addCurrencySymbolOnly(fRec.data['taxamount'],fRec.data['currencysymbol']),aftertaxamt:WtfGlobal.addCurrencySymbolOnly(fRec.data['amount'],fRec.data['currencysymbol']),noteTax:WtfGlobal.addCurrencySymbolOnly(fRec.json['notetax'],fRec.data['currencysymbol']),noteSubTotal:WtfGlobal.addCurrencySymbolOnly(fRec.json['noteSubTotal'],fRec.data['currencysymbol'])});
        this.Bank=false;
        this.Card=false;
   },

    getCurrencySymbol:function(){
        var symbol="";
        if(this.grid.getStore().getCount()>0){
            var rec=this.grid.getStore().getAt(0);
            symbol=  rec.data["currencysymbol"];
        }
        return symbol;
    },
    refreshView:function(rec){
        this.rec=rec;
        this.isexpenseinv=this.rec.data.isexpenseinv;
        this.store.load();
    },
    createGrid:function(){
        var isCN=this.label=='Credit Note'?true:false;
        if(!this.receiptTemp) {
            if(this.isCustBill){
                this.grid=new Wtf.account.BillingProductDetailsGrid({
                    region:'center',
                    style:'padding:0px 20px 10px 20px;background:#F1F1F1;',
                    readOnly:true,
                    autoScroll:true,
                    noteTemp:this.noteTemp,
                    isNote:this.noteTemp,
                    isCN:isCN,
                    isCustomer:this.isCustomer,
                    isCustBill:this.isCustBill,
                    border:false
                });
            }else if(this.isexpenseinv){
                   this.grid = new Wtf.account.ExpenseInvoiceGrid({
                        height: 200,
                        region:'center',
                        style:'padding:0px 20px 10px 20px;background:#F1F1F1;',
                        readOnly:true,
                        border:true,
                        title: 'Expense',
                        viewConfig:{forceFit:true},
                        isCustomer:this.isCustomer,
                        editTransaction:this.isEdit,
                        isCustBill:this.isCustBill,
                        id:this.id+"expensegrid",
                        fromOrder:true,
                        closable: false,
                        isOrder:this.isOrder,
                        forceFit:true,
                        loadMask : true
                    });
            }else{
                this.grid=new Wtf.account.ProductDetailsGrid({
                    region:'center',
                    style:'padding:0px 20px 10px 20px;background:#F1F1F1;',
                    autoScroll:true,
                    noteTemp:this.noteTemp,
                    isNote:this.noteTemp,
                    readOnly:true,
                    isCN:isCN,
                    isCustomer:this.isCustomer,
                    layout:'fit',
                    viewConfig:{
                        forceFit:true
                    },
                    loadMask : true
                });
            }
        } else {
            this.grid = new Wtf.account.OSDetailGrid({
                region:'center',
                readOnly:true,
                border:false,
                isCustomer:this.isCustomer,
                isCustBill:this.isCustBill,
                viewConfig:{
                    forceFit:true,
                    emptyText:"<div class='grid-empty-text'>No data to display</div>"
                },
                isReceipt:this.label=='Receive Payment'?true:false,
                amount:0,
                id : this.label=='Receive Payment'?'viewcustomergrid':'viewvendorgrid',
                closable: true
            });

        }
    }
}); 
