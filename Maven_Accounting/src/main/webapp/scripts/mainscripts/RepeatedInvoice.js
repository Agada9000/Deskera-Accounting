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
Wtf.RepeatedInvoicesReport = function(config){
    this.isCustBill=config.isCustBill;
    this.GridRec = Wtf.data.Record.create ([
//        {name:'billid'},
//        {name:'billno'},
//        {name:'personid'},
//        {name:'personname'},
//        {name:'interval'},
//        {name:'intervalType'},
//        {name:'startDate', type:'date'},
//        {name:'nextDate', type:'date'},
//        {name:'expireDate'/*, type:'date'*/},
//        {name:'repeateid'}
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'orderamount'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'personname'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'termdays'},
        {name:'termname'},
        {name:'incash'},
        {name:'taxamount'},
        {name:'taxid'},
        {name:'orderamountwithTax'},
        {name:'taxincluded',type:'boolean'},
        {name:'taxname'},
        {name:'deleted'},
        {name:'amountinbase'},
        {name:'memo'},
        {name:'externalcurrencyrate'},
        {name:'ispercentdiscount'},
        {name:'discountval'},
        {name:'crdraccid'},
        {name:'creditDays'},
        {name:'isRepeated'},
        {name:'childCount'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'status'}
    ]);

    this.StoreUrl = "ACC" + (this.isCustBill?"InvoiceCMN/getBillingInvoices":"InvoiceCMN/getInvoices") + ".do";
    this.Store = new Wtf.data.Store({
        url:this.StoreUrl,
        baseParams: {
          getRepeateInvoice: true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });
 //    this.Store.on('load',this.hideLoading, this);
//    this.Store.on('loadexception',this.hideLoading, this);
    this.Store.load();
    var btnArr=[];
    btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.invList.repeated.search"),  //'Quick Search By Invoice No.',
        width: 200,
        field: 'billno',
        Store:this.Store
    }));
    btnArr.push(this.resetBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        handler: this.handleResetClick,
        disabled: false
    }));
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.editinvoice)){
        btnArr.push(this.editBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
            tooltip : WtfGlobal.getLocaleText("acc.invList.repeated.edit"),  //'Allows you to edit Recurring Invoice.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.edit),
            handler: this.repeateInvoice,
            disabled: true
        }))
   }

    this.expander = new Wtf.grid.RowExpander({});
    this.sm = new Wtf.grid.RowSelectionModel({singleSelect: true});
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        border:false,
        sm:this.sm,
        layout:'fit',
      // loadMask:true,
        plugins: this.expander,
        viewConfig:{
            forceFit:true,
            emptyText:""
        },
        columns:[
            this.expander,
            {
                header:WtfGlobal.getLocaleText("acc.invList.repeated.inv"),  //"Invoice No",
                dataIndex:'billno',
                pdfwidth:75,
                renderer:WtfGlobal.linkDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invList.repeated.cus"),  //"Customer",
                pdfwidth:75,
                renderer:WtfGlobal.deletedRenderer,
                dataIndex:'personname'
            },{
                header:WtfGlobal.getLocaleText("acc.invList.repeated.sched"),  //"Schedular Start Date",
                dataIndex:'startDate',
                pdfwidth:80,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invList.repeated.invgen"),  //"No. of invoice generated till now",
                dataIndex:'childCount',
                align: "right",
                pdfwidth:80
            },{
                header:WtfGlobal.getLocaleText("acc.invList.repeated.interval"),  //"Interval",
                dataIndex:'interval',
                pdfwidth:80,
                renderer: function(a,b,c){
                    var idx = Wtf.intervalTypeStore.find("id", c.data.intervalType);
                    if(idx == -1) {
                        return a+" "+c.data.intervalType;
                    } else {
                        return a+" "+Wtf.intervalTypeStore.getAt(idx).data.name;
                    }
                }
            },{
                header:WtfGlobal.getLocaleText("acc.invList.repeated.nextInv"),  //"Next Invoice Generation Date",
                dataIndex:'nextDate',
                pdfwidth:80,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            }],
            tbar:btnArr,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.Store,
                displayInfo: true,
                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id})
            })
        });
    this.items=[this.grid];

    this.expandRec = Wtf.data.Record.create ([
        {name:'parentInvoiceId'},
        {name:'invoiceId'},
        {name:'invoiceNo'}
    ]);
    this.expandStoreUrl = "ACCInvoice/" + (this.isCustBill?"getBillingInvoiceRepeateDetails":"getInvoiceRepeateDetails") + ".do";
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });

    this.grid.on('cellclick',this.onRowClick, this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
 //   this.Store.on('load',this.expandRow, this);

    Wtf.apply(this,config);
    Wtf.RepeatedInvoicesReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.RepeatedInvoicesReport, Wtf.Panel,{
    //hideLoading:function(){  Wtf.MessageBox.hide(); },
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.expandStore.load({params:{parentid:record.data['billid']}});
    },
    expandRow:function(){
        Wtf.MessageBox.hide();
    },

    fillExpanderBody:function(){
        var disHtml = "";
        var header = "";

        if(this.expandStore.getCount()==0){
            header = "<span style='color:#15428B;display:block;'>"+WtfGlobal.getLocaleText("acc.invList.repeated.invgen")+"</span>";   // No any invoices generated till now
        } else {
            var blkStyle = "display:block;float:left;width:150px;Height:14px;"
            header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.invList.repeated.genInv")+"</span>";  //Generated Invoices
            for(var i=0;i<this.expandStore.getCount();i++){
                var rec=this.expandStore.getAt(i);
                header += "<span style="+blkStyle+">"+
                            "<a class='jumplink' onclick=\"jumpToTemplate('"+rec.data.invoiceId+"',"+this.isCustBill+")\">"+rec.data.invoiceNo+"</a>"+
                        "</span>";
            }
        }
        disHtml += "<div style='width:95%;margin-left:3%;'>" + header + "<br/></div>";
        this.expanderBody.innerHTML = disHtml;
    },

    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.Store.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
            this.Store.on('load',this.storeloaded,this);
        }
    },
    storeloaded:function(store){
     //   Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
    },
    enableDisableButtons:function(){
        if(this.sm.getCount()==1){
            this.editBttn.enable();
        } else {
            this.editBttn.disable();
        }
    },
    repeateInvoice:function(){
        var formrec= this.grid.getSelectionModel().getSelected();
        callRepeatedInvoicesWindow(this.isCustBill, formrec);
    },
    onRowClick:function(g,i,j,e){
        e.stopEvent();
//        var el=e.getTarget("a");
//        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="billno"){
            var formrec = this.grid.getSelectionModel().getSelected();
            var incash=formrec.get("incash");
            if(incash &&!this.isCustBill)
                callViewCashReceipt(formrec, 'ViewInvoice');
            else if(incash)
                callViewBillingCashReceipt(formrec,null, 'ViewBillingCSInvoice',true);
            else if(this.isCustBill)
                callViewBillingInvoice(formrec,null, 'ViewBillingInvoice',false);
            else 
                callViewInvoice(formrec, 'ViewCashReceipt');
        }
    }
});

function jumpToTemplate(billid, isCustBill){
    Wtf.Ajax.requestEx({
        url: "ACC" + (isCustBill?"InvoiceCMN/getBillingInvoices":"InvoiceCMN/getInvoices") + ".do",
        params: {billid:billid}
        },this,
        function(response){
            var rec = response;
            rec.data = response.data[0];
            var incash=rec.data.incash;
            if(incash &&!isCustBill)
                callViewCashReceipt(rec, 'ViewInvoice');
            else if(incash)
                callViewBillingCashReceipt(rec,null, 'ViewBillingCSInvoice',true);
            else if(isCustBill)
                callViewBillingInvoice(rec,null, 'ViewBillingInvoice',false);
            else
                callViewInvoice(rec, 'ViewCashReceipt');
        },
        function(response){

        });
}
/*/////////////////////////////////////   FORM   ////////////////////////////////////////////////////*/

Wtf.RepeateInvoiceForm = function(config){
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.repeated.recInv"),  //"Recurring Invoice",
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.repeated.savNclose"),  //'Save and Close',
            scope: this,
            handler: this.saveData.createDelegate(this)
        },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.RepeateInvoiceForm.superclass.constructor.call(this, config);
    this.addEvents({
        'update': true,
        'cancel': true
    });
}
Wtf.extend( Wtf.RepeateInvoiceForm, Wtf.Window, {
    defaultCurreny:false,
    onRender: function(config){
        Wtf.RepeateInvoiceForm.superclass.onRender.call(this, config);

this.isEdit = false;
if(this.invoiceRec!=undefined){
    if(this.invoiceRec.data.repeateid){
        this.isEdit = true;
    }
}
this.startDateValue = new Date();
this.startDateValue = new Date(this.startDateValue.getFullYear(),this.startDateValue.getMonth(),this.startDateValue.getDate()+1);
//this.startDateValue.setDate(this.startDateValue.getDate()+1);
this.nextDateValue = this.startDateValue;
if(this.isEdit){
    this.nextDateValue = this.invoiceRec.data.nextDate;
}
this.creditTermDays = 0;
if(this.invoiceRec!=undefined){
    this.creditTermDays = this.invoiceRec.data.creditDays?this.invoiceRec.data.creditDays:0;
}
this.dueDateValue = this.calculateDueDate();

       this.repeateForm = new Wtf.form.FormPanel({
            border: false,
            labelWidth:230,
            items : [
                this.repeateId = new Wtf.form.Hidden({
                    hidden:true,
                    name:"repeateid"
                }),
                this.nextDate = new Wtf.form.DateField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.repeated.nextDate") +"*",  //"Next Invoice Generation Date*",
                    width:200,
                    name:'startDate',
                    readOnly:true,
//                    minValue : this.isEdit?null:new Date(),
                    value: this.nextDateValue,
                    format: "Y-m-d"
                }),
                this.dueDate = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.repeated.nextDueDate"),  //"Next Invoice Due Date",
                    width:200,
                    maxLength:50,
                    allowBlank: false,
                    value: this.dueDateValue,
                    disabled: true,
                    readOnly: true,
                    name:"dueDate"
                }),
                this.intervalPanel = new Wtf.Panel({
                    layout: "column",
                    border: false,
                    items:[
                        new Wtf.Panel({
                            columnWidth: 0.66,
                            layout: "form",
                            border: false,
                            anchor:'100%',
                            items : this.interval = new Wtf.form.NumberField({
                                    fieldLabel:WtfGlobal.getLocaleText("acc.repeated.repInv") +"*",  //"Repeate this invoice every*",
                                    width: 50,
                                    allowBlank: false,
                                    minValue: 1,
                                    maxValue: 999,
                                    allowNegative: false,
                                    maxLength: 50,
                                    name: "interval"
                                })
                        }),
                        new Wtf.Panel({
                            columnWidth: 0.3,
                            layout: "form",
                            border: false,
                            anchor:'100%',
                            items : this.intervalType = new Wtf.form.ComboBox({
                                        store: Wtf.intervalTypeStore,
                                        hiddenName:'intervalType',
                                        displayField:'name',
                                        valueField:'id',
                                        mode: 'local',
                                        value: "day",
                                        triggerAction: 'all',
                                        typeAhead:true,
                                        hideLabel: true,
                                        labelWidth: 5,
                                        width: 128,
                                        selectOnFocus:true
                                    })
                        })
                        
                    ]
                }),
                this.expireDate = new Wtf.form.DateField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.repeated.endDate"),  //"Recurring Invoice End Date (Optional)",
                    width:200,
                    name:'expireDate',
//                    readOnly:true,
                    format: "Y-m-d"
                })

            ]
        });

        this.nextDate.on("change",function(df, nvalue, ovalue){
//            this.startDateValue = nvalue;
            this.dueDateValue = this.calculateDueDate();
            this.dueDate.setValue(this.dueDateValue);
        },this);

        if(this.invoiceRec!=undefined){
            if(this.invoiceRec.data.repeateid){ //Update
        //          this.repeateForm.getForm().loadRecord(this.invoiceRec);
                this.nextDateValue = this.invoiceRec.data.nextDate;
                this.dueDateValue = this.calculateDueDate();
                this.dueDate.setValue(this.dueDateValue);
                this.repeateId.setValue(this.invoiceRec.data.repeateid);
                this.nextDate.setValue(this.nextDateValue);
                this.dueDate.setValue(this.dueDateValue);
                this.interval.setValue(this.invoiceRec.data.interval);
                this.intervalType.setValue(this.invoiceRec.data.intervalType);
                this.expireDate.setValue(this.invoiceRec.data.expireDate);
            }
        }

        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(WtfGlobal.getLocaleText("acc.repeated.recInv"),WtfGlobal.getLocaleText("acc.repeated.recInvInfo"),"../../images/accounting_image/Chart-of-Accounts.gif", false)
        },new Wtf.Panel({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            bodyStyle: 'border-bottom:1px solid #bfbfbf;padding:20px 0px 20px 20px',
            items: this.repeateForm
        }));
    },
    
    calculateDueDate: function() {
        var stdate = this.nextDate==undefined ? new Date(this.startDateValue): new Date(this.nextDate.getValue());
        stdate.setDate(stdate.getDate()+this.creditTermDays);
        return stdate.format("Y-m-d");
    },
    
    closeWin:function(){
        this.fireEvent('cancel',this)
        this.close();
    },

    saveData:function(){

        var valid = this.repeateForm.getForm().isValid();
        var minNextDate = this.startDateValue<this.nextDateValue ? this.startDateValue : this.nextDateValue;
        if(this.nextDate.getValue()<minNextDate){
            var minDate = new Date(minNextDate);
            minDate.setDate(minDate.getDate()-1);
            this.nextDate.markInvalid(WtfGlobal.getLocaleText("acc.repeated.msg")+minDate.format("Y-m-d"));    //"Please select 'Next date' greater than "
            valid = false;
        }

        if(this.expireDate.getValue()!="" && this.expireDate.getValue()<this.nextDate.getValue()){
            this.expireDate.markInvalid(WtfGlobal.getLocaleText("acc.repeated.msg1"));    // "'End date' should be greater than 'Next date'"
            valid = false;
        }

        if(!valid){
            WtfComMsgBox(2,2);
            return;
        }
        var rec=[];
        rec = this.repeateForm.getForm().getValues();
        rec.isCustBill = this.isCustBill;
        rec.invoiceid = this.invoiceRec.data.billid;

//        alert(rec);return;
        Wtf.Ajax.requestEx({
            url:"ACCInvoice/saveRepeateInvoiceInfo.do",
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
    },

    genSuccessResponse:function(response){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
            if(response.success) this.fireEvent('update',this);
            this.close();
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
});
