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
Wtf.account.SalesByItem=function(config){
    this.receivable=config.receivable||false;
    this.summary = new Wtf.ux.grid.GridSummary();
    this.prodRec = new Wtf.data.Record.create([{
            name:'productid'
        },{
            name:'productname'
        },{
            name:'quantity'
        },{
            name:'avgsale'
        },{
            name:'amount'
        },{
            name:'cogs'
        },{
            name:'avgcogs'
        },{
            name:'margin'
        },{
            name:'permargin'
         },{
            name:'uomname'
        }]);
    this.prodStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.prodRec),
//        url: Wtf.req.account+"reporthandler.jsp",
        url : "ACCReports/getSalesByItem.do",
        baseParams:{
            mode:1
        }
    });
    this.rowNo=new Wtf.KWLRowNumberer();
    this.prodStore.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);

    this.prodStore.on('load',this.storeloaded,this);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.prodStore,
        border:false,
        layout:'fit',
        viewConfig:{
            forceFit:true,
            enableRowBody: true/*,
            getRowClass: this.changeRowColor.createDelegate(this)*/
        },
        forceFit:true,
        loadMask : true,
        columns:[this.rowNo,{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridProduct"),  //' Product Name',
            dataIndex:'productname',
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridQtySold"),  //"Quantity Sold",
            dataIndex:'quantity',
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:100
//        },{
//            header:"Total Sales",
//            dataIndex:'avgsale',
//            align:'right',
//            renderer:this.decimalRenderer.createDelegate(this)
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridTotalSales"),  //"Total Sales",
            dataIndex:'amount',
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridPurchaseCost"),  //"Purchase Cost",
            dataIndex:'cogs',
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridAvgPurchaseCost"),  //"Average Purchase Cost",
            dataIndex:'avgcogs',
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridProfitMargin"),  //"Profit Margin",
            dataIndex:'margin',
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridPercentageProfitMargin"),  //"Percentage Profit Margin",
            dataIndex:'permargin',
            align:'right',
            renderer:this.decimalRenderer.createDelegate(this),
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
        name:'stdate',
        value:WtfGlobal.getDates(true)
    });
    this.toDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });
    var btnArr=[];
    this.productRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'productname'},
            {name:'desc'},
            {name:'uomid'},
            {name:'uomname'},
            {name:'parentid'},
            {name:'parentname'},
            {name:'purchaseaccountid'},
            {name:'salesaccountid'},
            {name:'purchaseretaccountid'},
            {name:'salesretaccountid'},
            {name:'reorderquantity'},
            {name:'quantity'},
            {name:'reorderlevel'},
            {name:'leadtime'},
            {name:'purchaseprice'},
            {name:'saleprice'},
            {name:'leaf'},
            {name:'currencysymbol'},
            {name:'currencyrate'},
            {name:'level'}
        ]);
        this.productStore = new Wtf.data.Store({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCProduct/getProducts.do",
            baseParams:{mode:22},
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
        this.productStore.load();

      btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.saleByItem.search"),  //'Search by Product Name',
            width: 200,
            field: 'productname'
        }),this.resetBttn
        ,'-','From',this.fromDate,'-','To',this.toDate,'-',{
            xtype:'button',
            text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            iconCls:'accountingbase fetch',
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.nee.9"),  //"Select a date to view sales by item.",
            handler:this.fetchAgedData
        }
    );
    
    btnArr.push(this.exportbtn = new Wtf.exportButton({
        obj:this,
        disabled:true,
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
        id:"exportSalebyItemSummary",
        menuItem:{csv:true,pdf:true,rowPdf:false},
        get:913,
        label:"Sale by Item Summary Report"
    }));
    
    btnArr.push(this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        disabled:true,
        id:"printSalebyItemSummary",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        menuItem:{print:true},
        get:913,
        label:"Sale by Item Summary Report"
    }));
    
    this.resetBttn.on('click',this.handleResetClick,this);
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

    Wtf.account.SalesByItem.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });

    this.prodStore.on("beforeload", function(s,o) {
        o.params.fromDate= WtfGlobal.convertToGenericDate(this.fromDate.getValue());
        o.params.toDate= WtfGlobal.convertToGenericDate(this.toDate.getValue());
     //   o.params.product=this.productEditor.getValue();
        
        this.fetchAgedData1(false);
    },this);

    this.prodStore.load({
        params:{
            start:0,
            limit:30,
            creditonly:true
        }
    });
}

Wtf.extend( Wtf.account.SalesByItem,Wtf.Panel,{
    decimalRenderer:function(val){
        return'<div class="currency">'+WtfGlobal.conventInDecimal(val,"")+'%</div>';
    },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.prodStore.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value,
                    aged:true,
                    creditonly:true
                }
            });
        }
    },

    changeRowColor:function(record){
        var dueDate = record.data['duedate'];
        var currentDate=new Date(new Date().format('M d, Y'))
        if(currentDate>dueDate)
            return 'red-background';
        return 'yellow-background';
    },

    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
        if(store.getCount()==0){
            if(this.exportbtn)this.exportbtn.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportbtn)this.exportbtn.enable();
            if(this.printButton)this.printButton.enable();
        }
    },

    fetchAgedData:function(){
        this.prodStore.load({
            params:{
                start:0,
                limit:this.pP.combo.value,
                creditonly:true
            }
        });
        this.fetchAgedData1(true);   
    },
    
    fetchAgedData1:function(limit){
        this.printButton.setParams({
        	fromDate:WtfGlobal.convertToGenericDate(this.fromDate.getValue()),
        	toDate:WtfGlobal.convertToGenericDate(this.toDate.getValue()),
            start:0,
            limit:limit?this.pP.combo.value:30,
            creditonly:true,
            name: WtfGlobal.getLocaleText("acc.saleByItem.summaryReport"),
            filetype: 'print'
        });
        this.exportbtn.setParams({
        	fromDate:WtfGlobal.convertToGenericDate(this.fromDate.getValue()),
        	toDate:WtfGlobal.convertToGenericDate(this.toDate.getValue()),
            start:0,
            limit:limit?this.pP.combo.value:30,
            creditonly:true,
            name: WtfGlobal.getLocaleText("acc.saleByItem.summaryReport")
        });
    },
//      getDates:function(start){
//        var d=new Date();
//        var monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
//        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
//        if(d<fd)
//            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
//        if(start)
//            return fd;
//        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
//    },
     unitRenderer:function(value,metadata,record){
        var unit=record.data['uomname'];
            value=value+" "+unit;
        return value;
    }
    
});
