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
Wtf.account.sampleTaxCalculation=function(config){
    this.summary = new Wtf.ux.grid.GridSummary();
    this.isSales=config.isSales||false;
    this.ReportType=this.isSales?"Sales":"Purchase";
    this.prodRec = new Wtf.data.Record.create([{
            name:'taxcode'
        },{
            name:'taxname'
        },{
            name:'totalsale'
        },{
            name:'nontaxablesale'
        },{
            name:'taxablesale'
        },{
            name:'taxrate'
        },{
            name:'taxcollected'
        },{
            name:'taxpayable'
        },{
            name:'taxamount'
        }]);
    this.prodStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.prodRec),
//        url: Wtf.req.account+"reporthandler.jsp",
        url : "ACCReports/getCalculatedTax.do",
        baseParams:{
            issales:this.isSales,
            mode:2
        }
    });
    this.rowNo=new Wtf.KWLRowNumberer();

    this.prodStore.on('load',this.storeloaded,this);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.prodStore,
        border:false,
        layout:'fit',
        viewConfig:{
            forceFit:true,
            enableRowBody: true
        },
        forceFit:true,
        loadMask : true,
        columns:[this.rowNo,{
            header:WtfGlobal.getLocaleText("acc.taxReport.taxName"),  //'Tax Name',
            dataIndex:'taxname',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.taxReport.taxCode"),  //'Tax Code',
            dataIndex:'taxcode',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:100
        },{
            header:(!this.isSales?WtfGlobal.getLocaleText("acc.taxReport.totalPurchase"):WtfGlobal.getLocaleText("acc.taxReport.totalSale")),
            dataIndex:'totalsale',
            renderer:WtfGlobal.currencyRendererDeletedSymbol,
            pdfwidth:100
//        },{
//            header:"Non-Taxable Sales",
//            dataIndex:'nontaxablesale',
//            renderer:WtfGlobal.currencyRendererDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.taxReport.taxAmount"),  //"Tax Amount",
            dataIndex:'taxamount',
            renderer:WtfGlobal.currencyRendererDeletedSymbol,
            pdfwidth:100
//        },{
//            header:"Taxable Sales",
//            dataIndex:'taxablesale',
//            renderer:WtfGlobal.currencyRendererDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.taxReport.taxRate"),  //"Tax Rate",
            dataIndex:'taxrate',
            renderer:function(v){return "<div style='float:right'>"+v+"%</div>";},
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.taxReport.taxCollected"),  //"Tax Collected",
            dataIndex:'taxcollected',
            renderer:WtfGlobal.currencyRendererDeletedSymbol,
            pdfwidth:100
        },{
            header:(this.isSales?WtfGlobal.getLocaleText("acc.taxReport.payable"):WtfGlobal.getLocaleText("acc.taxReport.recievable")),   //" Tax Payable":"Tax Receivable"),
            dataIndex:'taxpayable',
            renderer:WtfGlobal.currencyRendererDeletedSymbol,
            pdfwidth:100
        }]
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });

    this.fromDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:new Date(new Date().format('M d, Y')+" 12:00:00 AM")
    });
    this.toDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:new Date(new Date().format('M d, Y')+" 12:00:00 AM")
    });
    var btnArr=[];
  
    
    btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.taxReport.search"),  //'Search by Tax Name',
            width: 200,
            field: 'taxname'
        }),this.resetBttn,
        this.exportButton=new Wtf.exportButton({
            obj:this,
            disabled:true,
            tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export Report details.",  
            id:(this.isSales?"exportsalestaxreport":"exportpurchasetaxreport"),
            params:{name:this.isSales?WtfGlobal.getLocaleText("acc.taxReport.salesTax"):WtfGlobal.getLocaleText("acc.taxReport.purchaseTax"),issales:this.isSales},
            menuItem:{csv:true,pdf:true,rowPdf:false},
            get:911,
            label:this.isSales?"Sales Tax Report":"Purchase Tax Report"
        }),
        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            disabled:true,
            tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
            params:{name:this.isSales?WtfGlobal.getLocaleText("acc.taxReport.salesTax"):WtfGlobal.getLocaleText("acc.taxReport.purchaseTax"),issales:this.isSales},
            menuItem:{print:true},
            get:911,
            label:this.isSales?"Sales Tax Report":"Purchase Tax Report"
        })
//         ,'-','From :',this.fromDate,'-','To :',this.toDate,'-'
//        ,{
//            xtype:'button',
//            text:'Fetch',
//            iconCls:'accountingbase fetch',
//            scope:this,
//            handler:this.fetchAgedData
//        }
    );

   
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        tbar:btnArr,
        items:[this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.prodStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
            id : "pPageSize_"+this.id
            })
        })
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    Wtf.account.sampleTaxCalculation.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });

    this.prodStore.on("beforeload", function(s,o) {
        o.params.fromDate= WtfGlobal.convertToGenericDate(this.fromDate.getValue());
        o.params.toDate= WtfGlobal.convertToGenericDate(this.toDate.getValue());
    },this);
    this.prodStore.on('datachanged', function() {
        var p = 30;
        this.quickPanelSearch.setPage(p);
    }, this);
    this.prodStore.load({
        params:{
            start:0,
            limit:30
        }
    });
}

Wtf.extend( Wtf.account.sampleTaxCalculation,Wtf.Panel,{
    loadStore:function(){
         this.prodStore.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
    },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    },

    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
        if(store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
    }
});
