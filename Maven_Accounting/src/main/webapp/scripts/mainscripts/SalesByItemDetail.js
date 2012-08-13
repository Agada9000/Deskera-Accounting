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
Wtf.account.SalesByItemDetail=function(config){
    this.rec = new Wtf.data.Record.create([{
         name:'productid'
        },{
         name:'invoiceid'
        },{
         name:'billno'
        },{
           name:'personname'
        },{
           name:'productname'
        },{
            name:'quantity'
        },{
            name:'rateinbase'
        },{
            name:'amount'
        },{
            name:'totalsales'
        },{
            name:'date', type:'date'
        },{
            name:'totalquantity'
        },{
            name:'totalsales'
        },{
            name:'memo'
        
        }]);
     
        this.store =new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.rec),
            groupField:'productname',
            sortInfo: {field: 'productname',direction: "DESC"},
    //        url: Wtf.req.account+'reporthandler.jsp',
            url : "ACCReports/getDetailedSalesByItem.do"
        });

    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });

    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'startdate',
        value:WtfGlobal.getDates(true)
    });
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });
    this.rowNo=new Wtf.KWLRowNumberer();
//    this.summary = new Wtf.ux.grid.GridSummary();
    this.cm= new Wtf.grid.ColumnModel([this.rowNo,{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridProduct"),  //"Product Name",
            hidden:true,
            dataIndex:'productname',
            groupRenderer: function(v){return v},
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridInvoice"),  //"Invoice Number",
            dataIndex:'billno',
            sortable: true,
            groupable: true,
            groupRenderer: function(v){return v},
            renderer:WtfGlobal.linkRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridDate"),  //"Date",
            dataIndex:'date',
            align:'center',
            groupRenderer:this.groupDateRender.createDelegate(this),
            renderer:WtfGlobal.onlyDateRenderer,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridMemo"),  //"Memo",
            align:'left',
            dataIndex:'memo',
            pdfwidth:100
        },{

            header:WtfGlobal.getLocaleText("acc.saleByItem.gridCustName"),  //"Customer Name",
            dataIndex:'personname',
            align:'left',
            sortable: true,
            groupable: true,
            groupRenderer: function(v){return v},
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridQty"),  //"Quantity",
            align:'right',
            dataIndex:'quantity',
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridSalesPrice"),  //"Sales Price",
            dataIndex:'rateinbase',
            align:'right',
          //  summaryType:'sum',
          //  summaryRenderer:WtfGlobal.withoutRateCurrencySymbol,
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            pdfwidth:100
          },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridAmount"),  //"Amount",
            dataIndex:'amount',
            align:'right',
          //  summaryType:this.isSummary?'sum':"",
         //   summaryRenderer:WtfGlobal.withoutRateCurrencySymbol,
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            pdfwidth:100
         },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridBalance"),  //"Balance",
            dataIndex:'totalsales',
            align:'right',
        //    summaryType:'sum',
        //    summaryRenderer:WtfGlobal.withoutRateCurrencySymbol,
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            pdfwidth:100
        
    }]);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.store,
        cm:this.cm,
        border:false,
//        plugins:[this.summary],
        layout:'fit',
        view:new Wtf.grid.GroupingView({
            forceFit:true
       }),
        loadMask : true
    })

    var btnArr=[];
    btnArr.push(
       this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.saleByItem.search"),  //'Search by Product Name',
            id:"quickSearch"+config.helpmodeid,
            width: 200,
            field: 'productname'
        }),this.resetBttn,'-',WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate, '-',{  
            xtype:'button',
             text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            iconCls:'accountingbase fetch',
            scope:this,
            tooltip:this.receivable?WtfGlobal.getLocaleText("acc.saleByItem.fetchTT"):WtfGlobal.getLocaleText("acc.saleByItem.fetchTT1"),//"Select a date to view Aged Receivable.":"Select a date to view Aged Payable.",
            handler:this.loadStore
        });
    btnArr.push(this.exportbtn = new Wtf.exportButton({
        obj:this,
        disabled:true,
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details.',
        id:"exportSalebyItemDetail",
        menuItem:{csv:true,pdf:true,rowPdf:false},
        get:914,
        label:"Sale by Item Detail Report"
    }));
    btnArr.push(this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        disabled:true,
        id:"printSalebyItemDetail",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        menuItem:{print:true},
        get:914,
        label:"Sale by Item Detail Report"
    }));
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
            store: this.store,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        })
    });

    Wtf.account.SalesByItemDetail.superclass.constructor.call(this,config);
    this.addEvents({
        'invoice':true
    });


    this.store.on('load',this.storeloaded,this);
    this.store.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);
    this.store.on("beforeload", function(s,o){
       s.baseParams= {
                fromDate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                toDate: WtfGlobal.convertToGenericDate(this.endDate.getValue())
            },
       this.setExportParams(false);
    },this);
    this.store.load({
        params:{
            start:0,
            limit:30
        }
    });
    this.grid.on('cellclick',this.onCellClick, this);
}

Wtf.extend( Wtf.account.SalesByItemDetail,Wtf.Panel,{  
    groupDateRender:function(v){
       return v.format(WtfGlobal.getOnlyDateFormat())
    },

    totalRender:function(v,m,rec){
        var val=WtfGlobal.withoutRateCurrencySymbol(v,m,rec);
       return "<b>"+val+"</b>"
    },
    loadStore:function(){
        this.store.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
        this.setExportParams(true);
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
            if(this.exportbtn)this.exportbtn.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportbtn)this.exportbtn.enable();
            if(this.printButton)this.printButton.enable();
        }
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="billno"){
            var invoiceid=this.store.getAt(i).data['invoiceid'];
            this.fireEvent('invoice',invoiceid,true);
        }
    },
    
    setExportParams:function(limit){
        this.printButton.setParams({
        	fromDate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
        	toDate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            start:0,
            limit:limit?this.pP.combo.value:30,
            name: WtfGlobal.getLocaleText("acc.saleByItem.detailReport"),
            filetype: 'print'
        });
        this.exportbtn.setParams({
        	fromDate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
        	toDate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            start:0,
            limit:limit?this.pP.combo.value:30,
            name: WtfGlobal.getLocaleText("acc.saleByItem.detailReport")
        });
    }
//    getDates:function(start) {
//        var d=new Date();
//        var monthDateStr=d.format('M d');
//        if(Wtf.account.companyAccountPref.fyfrom)
//            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
//        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
//        if(d<fd)
//            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
//        if(start)
//            return fd;
//        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
//    }
});
