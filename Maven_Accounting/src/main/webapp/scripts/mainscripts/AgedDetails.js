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
Wtf.account.AgedDetail=function(config){
    this.receivable=config.receivable||false;
    this.withinventory=config.withinventory||false;
    this.isSummary=config.isSummary||false;
    this.summary = new Wtf.ux.grid.GridSummary();
    this.uPermType=(this.receivable?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.permType=(this.receivable?Wtf.Perm.invoice:Wtf.Perm.vendorinvoice);
    this.exportPermType=(this.receivable?this.permType.exportdataagedreceivable:this.permType.exportdataagedpayable);
    this.printPermType=(this.receivable?this.permType.printagedreceivable:this.permType.printagedpayable);
    this.chartPermType=(this.receivable?this.permType.chartagedreceivable:this.permType.chartagedpayable);
    this.AgedRec = new Wtf.data.Record.create([{
            name:'billid'
        },{
            name:'journalentryid'
        },{
            name:'entryno'
        },{
            name:'billno'
        },{
            name:'date', type:'date'
        },{
            name:'duedate', type:'date'
        },{
            name:'personname'
        },{
            name:'amountdueinbase'
        },{
             name:'amountdue'
        },{
            name:'amountdue1'
        },{
            name:'amountdue2'
        },{
            name:'amountdue3'
        },{
            name:'amountdue4'
         },{
            name:'total'
        },{
            name:'amountdueinbase1'
        },{
            name:'amountdueinbase2'
        },{
            name:'amountdueinbase3'
        },{
            name:'amountdueinbase4'
        },{
            name:'total'
        },{
            name:'memo'
        },{
            name: 'currencysymbol'
        }
    ]);
     this.interval=new Wtf.form.NumberField({
        fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.till"),  //'Till',
        maxLength:2,
        width:30,
        allowDecimal:false,
        allowBlank:true,
        minValue:2,
        name:'duration',
        value:30
    });

    this.AgedStoreUrl = "";
    this.AgedStoreSummaryUrl = "";
    //(this.receivable?19:12),
    if(this.receivable){
//        //mode:(this.withinventory?12:16),
        this.AgedStoreUrl = this.withinventory?"ACCInvoiceCMN/getInvoices.do":"ACCInvoiceCMN/getBillingInvoices.do";
        this.expGet = this.withinventory?24:25;
//        //(this.isSummary?18:(this.withinventory?12:16))
        this.AgedStoreSummaryUrl = this.isSummary?"ACCInvoiceCMN/getCustomerAgedReceivable.do":(this.withinventory?"ACCInvoiceCMN/getInvoices.do":"ACCInvoiceCMN/getBillingInvoices.do");
        this.expSummGet = this.isSummary?26:this.expGet;
    }else{
//        //mode:(this.withinventory?12:16),
        this.AgedStoreUrl = this.withinventory?"ACCGoodsReceiptCMN/getGoodsReceipts.do":"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do";
        this.expGet = this.withinventory?21:22;
//        //(this.isSummary?18:(this.withinventory?12:16))
        this.AgedStoreSummaryUrl = this.isSummary?"ACCGoodsReceiptCMN/getVendorAgedPayable.do":(this.withinventory?"ACCGoodsReceiptCMN/getGoodsReceipts.do":"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do");
        this.expSummGet = this.isSummary?23:this.expGet;
    }
    this.AgedStore =this.isSummary? new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.AgedRec),
        groupField:'personname',
        sortInfo: {field: 'personname',direction: "DESC"},
        url: this.AgedStoreSummaryUrl,
        //url: Wtf.req.account+(this.receivable?'CustomerManager.jsp':'VendorManager.jsp'),
        baseParams:{
            mode:(this.isSummary?18:(this.withinventory?12:16)),
            creditonly:true,
            withinventory:this.withinventory,
            ignorezero:true,
            isdistributive:this.typeEditor != undefined?this.typeEditor.getValue():true,
            nondeleted:true
        }
    }):new Wtf.data.GroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.AgedRec),
        groupField:'personname',
        sortInfo: {field: 'personname',direction: "DESC"},
        url: this.AgedStoreUrl,
//        url: Wtf.req.account+(this.receivable?'CustomerManager.jsp':'VendorManager.jsp'),
        baseParams:{
            mode:(this.withinventory?12:16),
            creditonly:true,
            withinventory:this.withinventory,
            ignorezero:true,
            nondeleted:true
        }
    });

    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:"boolean"}, 'name'],
        data :[[true,WtfGlobal.getLocaleText("acc.rem.127")],[false,WtfGlobal.getLocaleText("acc.rem.128")]]
    });
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name:'isdistributive',
        displayField:'name',
        value:true,
        anchor:"50%",
        valueField:'typeid',
        mode: 'local',
        triggerAction: 'all'
    });

    this.rowNo=new Wtf.KWLRowNumberer();
    this.summary = new Wtf.ux.grid.GridSummary();
    this.cm= new Wtf.grid.ColumnModel([this.rowNo,{
       
            header:(this.receivable?WtfGlobal.getLocaleText("acc.agedPay.inv"): WtfGlobal.getLocaleText("acc.agedPay.venInv"))+" "+WtfGlobal.getLocaleText("acc.agedPay.number"),
            hidden:this.isSummary,
            dataIndex:'billno',
            pdfwidth:75,
//            renderer:config.isOrder?"":WtfGlobal.linkDeletedRenderer
            renderer:WtfGlobal.linkDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.agedPay.gridJEno"),  //"Journal Entry Number",
            dataIndex:'entryno',
            hidden:this.isSummary,
             pdfwidth:100,
            sortable: true,
            groupable: true,
            groupRenderer: function(v){return v},
            renderer:WtfGlobal.linkRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.agedPay.gridDate"),  //"Bill Date",
            dataIndex:'date',
             pdfwidth:100,
            align:'center',
            groupRenderer:this.groupDateRender.createDelegate(this),
            hidden:this.isSummary,
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.agedPay.gridDueDate"),  //"Due Date",
            dataIndex:'duedate',
            sortable: true,
             pdfwidth:100,
            groupable: true,
            hidden:this.isSummary,
            align:'center',
            groupRenderer: this.groupDateRender.createDelegate(this),
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:(this.receivable?WtfGlobal.getLocaleText("acc.agedPay.cus"):WtfGlobal.getLocaleText("acc.agedPay.ven"))+"/"+ WtfGlobal.getLocaleText("acc.agedPay.accName"),
            dataIndex:'personname',
             pdfwidth:150,
            sortable: true,
            groupable: true
        },{
            header:(this.isSummary?WtfGlobal.getLocaleText("acc.agedPay.gridCurrent"):WtfGlobal.getLocaleText("acc.agedPay.gridAmtDue")),  //"Current":"Amount Due",
            dataIndex:this.isSummary?'amountdue1':'amountdue',
            align:'right',
             pdfwidth:120,
           //  hidden:this.typeEditor.getValue(),
//            summaryType:this.isSummary?'sum':"",
//            summaryRenderer:this.isSummary?WtfGlobal.withoutRateCurrencySymbol:"",
            renderer:WtfGlobal.withoutRateCurrencySymbol
         },{
            header:(!this.typeEditor.getValue()?"":"1-")+ this.interval.getValue()+" "+WtfGlobal.getLocaleText("acc.agedPay.days")+ (!this.typeEditor.getValue()?" "+WtfGlobal.getLocaleText("acc.agedPay.before")+" ":""),
            dataIndex:this.isSummary?'amountdue2':'amountdue',
            hidden:!this.isSummary,
           // summaryType:'sum',
             pdfwidth:120,
            //summaryRenderer:this.isSummary?WtfGlobal.withoutRateCurrencySymbol:"",
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencySymbol
         },{            header:((!this.typeEditor.getValue()?"":(this.interval.getValue()*1+1)+"-")+(this.interval.getValue()*2))+" "+WtfGlobal.getLocaleText("acc.agedPay.days")+ (!this.typeEditor.getValue()?" "+WtfGlobal.getLocaleText("acc.agedPay.before")+" ":""),
            dataIndex:'amountdue3',
            hidden:!this.isSummary,            
            pdfwidth:120,
//            summaryType:'sum',
//            summaryRenderer:WtfGlobal.withoutRateCurrencySymbol,
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencySymbol
         },{
            header:(!this.typeEditor.getValue()?WtfGlobal.getLocaleText("acc.common.total")+" ":(">"+(this.interval.getValue()*2))+" "+WtfGlobal.getLocaleText("acc.agedPay.days")),
            hidden:!this.isSummary || !this.typeEditor.getValue(),            
            pdfwidth:120,
//            summaryType:'sum',
//            summaryRenderer:WtfGlobal.withoutRateCurrencySymbol,
            dataIndex:'amountdue4',
            align:'right',
            renderer:(!this.typeEditor.getValue()?this.totalRender.createDelegate(this):WtfGlobal.withoutRateCurrencySymbol)
        },{
            header:this.isSummary?'<b>'+WtfGlobal.getLocaleText("acc.common.total")+'</b>':WtfGlobal.getLocaleText("acc.common.memo"),  //"Memo",
            
            align:'right',
            pdfwidth:150,
//            hidden: !this.typeEditor.getValue(),
//            summaryType:this.isSummary?'sum':"",
//            summaryRenderer:this.isSummary?this.totalRender:"",
            dataIndex:this.isSummary?"total":'memo',
            pdfrenderer:"rowcurrency",
            renderer:this.isSummary?this.totalRender.createDelegate(this):""
             },{
            header:WtfGlobal.getLocaleText("acc.agedPay.gridAmtDueHomeCurrency"),  //"Amount Due (In Home Currency)",
            dataIndex:'amountdueinbase',
            align:'right',
             pdfwidth:100,
             hidden:this.isSummary,
             summaryType:'sum',
            summaryRenderer:this.isSummary?function(v,m,rec){return this.sumBaseAmount('amountdueinbase',v,m,rec)}.createDelegate(this):"" ,
            renderer:WtfGlobal.currencyRenderer
//        }, {
//            hidden:true,
//            summaryType:this.isSummary?'sum':"",
//            dataIndex:'amountdueinbase1'
//        },{
//            hidden:true,
//            summaryType:this.isSummary?'sum':"",
//            dataIndex:'amountdueinbase21'
//        },{    hidden:true,
//            summaryType:this.isSummary?'sum':"",
//            dataIndex:'amountdueinbase3'
//        },{
//            hidden:true,
//            summaryType:this.isSummary?'sum':"",
//            dataIndex:'amountdueinbase4'
//        },{
//            hidden:true,
//           summaryType:this.isSummary?'sum':"",
//            dataIndex:'totalinbase1'
    }]);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.AgedStore,
        cm:this.cm,
        border:false,
        plugins:[this.summary],
        layout:'fit',
        view:this.isSummary?new Wtf.grid.GridView({
            forceFit:true
       }):new Wtf.grid.GroupingView({
            forceFit:true
       }),
        loadMask : true
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        hidden:this.isSummary,
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });

    this.chart=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.chart"),  //'Chart',
        tooltip :WtfGlobal.getLocaleText("acc.graphTT.view")+' '+(this.receivable?WtfGlobal.getLocaleText("acc.graphTT.agedRec"):WtfGlobal.getLocaleText("acc.graphTT.agedPay")),
        id: 'chartRec'+config.helpmodeid,// + this.id,
        scope: this,
        handler:this.getChart,
        iconCls :(Wtf.isChrome?'accountingbase chartChrome':'accountingbase chart')

    });
    this.curDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.till"),  //'Till',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        id: 'dueDate'+config.helpmodeid,
        value:new Date(Wtf.serverDate.format('M d, Y')+" 12:00:00 AM")
    });
    this.expButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.agedPay.exportTT"),  //'Export report details',
        disabled :true,
        params:{ stdate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
                 duration:this.interval.getValue(),
                 enddate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
                 accountid:this.accountID||config.accountID,
                 curdate: WtfGlobal.convertToGenericDate(this.curDate.getValue()),
                 isdistributive:this.typeEditor.getValue()
        },
        menuItem:{csv:true,pdf:true,rowPdf:false},
        get:this.isSummary?this.expSummGet:this.expGet
    })
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.agedPay.printTT"),  //'Print report details',
        disabled :true,
        params:{ stdate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
                 duration:this.interval.getValue(),
                 enddate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
                 accountid:this.accountID||config.accountID,
                 curdate: WtfGlobal.convertToGenericDate(this.curDate.getValue()),
                 isdistributive:this.typeEditor.getValue(),
                 name: this.receivable?WtfGlobal.getLocaleText("acc.wtfTrans.agedr"):WtfGlobal.getLocaleText("acc.wtfTrans.agedp")
        },
        lable: this.receivable?"Aged Receivable":"Aged Payable",
        menuItem:{print:true},
        get:this.isSummary?this.expSummGet:this.expGet
    })
    var btnArr=[];
    btnArr.push(
       this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:this.receivable?WtfGlobal.getLocaleText("acc.agedPay.searchcus"):WtfGlobal.getLocaleText("acc.agedPay.searchven"),  //'Search by Person Name',
            id:"quickSearch"+config.helpmodeid,
            width: 200,
            hidden:this.isSummary,
            field: 'personname'
        }),
        this.resetBttn);
        btnArr.push('-',WtfGlobal.getLocaleText("acc.agedPay.till"),this.curDate);
        if(this.isSummary)
        btnArr.push('-',WtfGlobal.getLocaleText("acc.agedPay.interval"),this.interval,'-',this.typeEditor);
        btnArr.push("-",
        {
            xtype:'button',
             text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',
            iconCls:'accountingbase fetch',
            scope:this,
            tooltip:this.receivable?WtfGlobal.getLocaleText("acc.agedReceive.view"):WtfGlobal.getLocaleText("acc.agedPay.view"),  //"Select a date to view Aged Receivable.":"Select a date to view Aged Payable.",
            handler:this.fetchAgedData
        });
        if(!this.isSummary&&!WtfGlobal.EnableDisable(this.uPermType, this.chartPermType))
            btnArr.push(this.chart);        
        if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))
            btnArr.push(this.expButton);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType))
            btnArr.push(this.printButton);
    this.resetBttn.on('click',this.handleResetClick,this);
    if(config.helpmodeid!=null){
        btnArr.push("->");
        btnArr.push(getHelpButton(this,config.helpmodeid));
    }
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        tbar:btnArr,
        items:[this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.AgedStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.agedPay.norec"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        })
    });

    Wtf.account.AgedDetail.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });

    this.AgedStore.on("beforeload", function(s,o) {
        o.params.curdate= WtfGlobal.convertToGenericDate(this.curDate.getValue());
    },this);
    this.AgedStore.on('load',this.storeloaded,this);
    this.AgedStore.load({
        params:{
            start:0,
            duration:this.interval.getValue(),
            isdistributive:this.typeEditor.getValue(),
            limit:30,
            creditonly:true
        }
    });
    this.AgedStore.on('datachanged', function() {
        var p = 30;
        this.quickPanelSearch.setPage(p);
    }, this);
    this.grid.on('cellclick',this.onCellClick, this);
}

Wtf.extend( Wtf.account.AgedDetail,Wtf.Panel,{  
    sumBaseAmount:function(dataindex,v,m,rec){       
        if(!this.isSummary){
            v=rec.data[dataindex];
            return WtfGlobal.withoutRateCurrencySymbol(v,m,rec)
        }
        return "";
    }, 
    groupDateRender:function(v){
       return v.format(WtfGlobal.getOnlyDateFormat())
    },
    totalRender:function(v,m,rec){
        var val=WtfGlobal.withoutRateCurrencySymbol(v,m,rec);
       return "<b>"+val+"</b>"
    },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.AgedStore.load({
                params: {
                    start:0,
                    duration:this.interval.getValue(),
                    isdistributive:this.typeEditor.getValue(),
                    limit:this.pP.combo.value,
                    aged:true,
                    creditonly:true
                }
            });
        }
    },

  
    storeloaded:function(store){
        if(store.getCount()==0){
            if(this.expButton)this.expButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        this.quickPanelSearch.StorageChanged(store);
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.AgedStore.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true);
        }
        if(header=="billno"){
            this.viewTransection(g,i,e)
        }
    },
    viewTransection:function(){						// Function for viewing the invoice details from the invoice list  Neeraj
        var formrec=null;
//        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
//                WtfComMsgBox(15,2);
//                return;
//        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(this.receivable) {
            if(!this.withinventory) {
                callViewBillingInvoice(formrec,null, 'ViewBillingInvoice',false);
            } else {
                callViewInvoice(formrec, 'ViewCashReceipt');
            }
        } else {
            if(!this.withinventory) {
                callViewBillingGoodsReceipt(formrec,null, 'ViewBillingInvoice',false);
            } else {
                callViewGoodsReceipt(formrec, 'ViewGoodsReceipt');
            }
        }
    },
    fetchAgedData:function(){
        if(this.interval.getValue()==""||this.interval.getValue()<=1){
              WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.agedPay.msg1")], 2);   //"Alert","Please enter interval greater than one."], 2);
              return;
        }
        this.AgedStore.load({
            params:{
                duration:this.interval.getValue(),
                isdistributive:this.typeEditor.getValue(),
                start:0,
                limit:this.pP.combo.value,
                creditonly:true
            }
        });
        if(this.isSummary){
            this.cm.setColumnHeader(6,WtfGlobal.getLocaleText("acc.agedPay.gridCurrent")); //"Current")
            this.cm.setColumnHeader(7,(!this.typeEditor.getValue()?"":"1-")+this.interval.getValue()+" "+WtfGlobal.getLocaleText("acc.agedPay.days")+ (!this.typeEditor.getValue()?" "+WtfGlobal.getLocaleText("acc.agedPay.before")+" ":""))
            this.cm.setColumnHeader(8,((!this.typeEditor.getValue()?"":(this.interval.getValue()*1+1)+"-")+(this.interval.getValue()*2))+" "+WtfGlobal.getLocaleText("acc.agedPay.days")+ (!this.typeEditor.getValue()?" "+WtfGlobal.getLocaleText("acc.agedPay.before")+" ":""))
            this.cm.setColumnHeader(9,(!this.typeEditor.getValue()?WtfGlobal.getLocaleText("acc.agedPay.gridTotal")+" ":(">"+(this.interval.getValue()*2))+" "+WtfGlobal.getLocaleText("acc.agedPay.days")))
            this.cm.setHidden( 9, !this.typeEditor.getValue() );
        }
        this.expButton.setParams({
             stdate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
             duration:this.interval.getValue(),
             accountid:this.accountID,
             enddate:WtfGlobal.convertToGenericDate(this.curDate.getValue()),
             curdate: WtfGlobal.convertToGenericDate(this.curDate.getValue()),
             isdistributive:this.typeEditor.getValue()
        });

        this.printButton.setParams({
             stdate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
             duration:this.interval.getValue(),
             accountid:this.accountID,
             enddate:WtfGlobal.convertToGenericDate(this.curDate.getValue()),
             curdate: WtfGlobal.convertToGenericDate(this.curDate.getValue()),
             isdistributive:this.typeEditor.getValue(),
             name: this.receivable?"Aged Receivable":"Aged Payable"
        })
    }, 
    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.AgedStore.getAt(i).data['accountid'];
        this.fireEvent('account',accid);
    },
    getChart:function(){
          var chartid=this.receivable?"receivablechartid":"payablechartid";
          var swf1="../../scripts/graph/krwcolumn/krwcolumn/krwcolumn.swf";
          var id1=this.receivable?"receivableid":"payableid"
//          var dataflag1=this.receivable?1:2;
          var dataflag1=this.receivable?"ACCInvoiceCMN/getAgedReceivableChart":"ACCGoodsReceiptCMN/getAccountPayableChart";
          var mainid=this.receivable?"mainAgedRecievable":"mainAgedPayable";
          var xmlpath1= this.receivable?'../../scripts/graph/krwcolumn/examples/AgesReceivable/agedreceivable_settings.xml':'../../scripts/graph/krwcolumn/examples/AgesPayable/agedpayable_settings.xml';
          var id2=this.receivable?"piereceivableid":"piepayableid"
          var swf2="../../scripts/graph/krwcolumn/krwpie/krwpie.swf";
//          var dataflag2=this.receivable?5:6;
          var dataflag2=this.receivable?"ACCInvoiceCMN/getAgedReceivablePie":"ACCGoodsReceiptCMN/getAgedReceivablePie";
          var xmlpath2= this.receivable?'../../scripts/graph/krwcolumn/examples/AgesReceivable/pieagedreceivable_settings.xml':'../../scripts/graph/krwcolumn/examples/AgesPayable/pieagedpayable_settings.xml';
          globalAgedChart(chartid,id1,swf1,dataflag1,mainid,xmlpath1,id2,swf2,dataflag2,xmlpath2,this.withinventory,true,false);
    }
});
