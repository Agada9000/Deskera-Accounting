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
Wtf.account.productValuationGrid = function (config){
    Wtf.apply(this,config);
    Wtf.account.productValuationGrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.account.productValuationGrid,Wtf.Panel,{
    initComponent:function (){
       Wtf.account.productValuationGrid.superclass.initComponent.call(this);

       this.startDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:this.getDates(true)
        });
        this.endDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            value:this.getDates(false)

        });

       this.gridRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'productname'},
            {name:'productdesc'},
            {name:'productType'},
            {name:'purchasecost'},
            {name:'avgcost'},
            {name:'lifo'},
            {name:'fifo'},
            {name:'quantity'},
            {name:'valuation'}
        ]);
        this.gridStore = new Wtf.data.Store({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url : "ACCReports/getProValuation.do",
            baseParams:{mode:29,isprovalreport:true},
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.gridRec)
        });

        this.gridcm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.invReport.prod"),  //"Product",
                dataIndex:'productname'
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.type"),  //"Product Type",
                dataIndex:'productType'
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.desc"),  //"Description",
                dataIndex:'productdesc',
                renderer : function(val) {
                    return "<div wtf:qtip=\""+val+"\" wtf:qtitle='Description'>"+val+"</div>";
                }
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.curPurchaseCost"),  //"Current Purchase Cost",
                dataIndex:'purchasecost',
                align:'right',
                renderer:WtfGlobal.currencyRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.avgPurchaseCost"),  //"Avg. Purchase Cost",
                dataIndex:'avgcost',
                align:'right',
                renderer:WtfGlobal.currencyRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.qty"),  //"Quantity On Hand",
                dataIndex:'quantity',
                 summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
                align:'right'
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.lifo"),  //"LIFO Valuation",
                dataIndex:'lifo',
                align:'right',
                renderer:WtfGlobal.currencyRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.fifo"),  //"FIFO Valuation",
                dataIndex:'fifo',
                align:'right',
                renderer:WtfGlobal.currencyRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.WTavg"),  //"Weighted Avg. Valuation",
                dataIndex:'valuation',
                align:'right',
                summaryType:'sum',
                summaryRenderer:this.opBalRenderer,
                renderer:function(val){
                    if(val==0){
                        return "<div style='color:#990000'>"+WtfGlobal.currencyRenderer(val)+"</div>";
                    }else{
                        return WtfGlobal.currencyRenderer(val);
                    }
                }
            }]);
        this.summary = new Wtf.ux.grid.GridSummary();
        this.itemsgrid = new Wtf.grid.GridPanel({
            layout:'fit',
            region:"center",
            store: this.gridStore,
            cm:this.gridcm,
            border : false,
            loadMask : true,
            plugins:[this.summary],
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            tbar:[//'From',this.startDate,
                  WtfGlobal.getLocaleText("acc.invReport.ason"),this.endDate,'-',{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                iconCls:'accountingbase fetch',
                tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),  //"Select a time period to view corresponding records.",
                scope:this,
                handler:this.fetchStatement
            }],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 15,
                id: "pagingtoolbar" + this.id,
                store: this.gridStore,
    //            searchField: this.quickPanelSearch,
                displayInfo: true,
                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
                })
            })
        });

        this.gridStore.on('load', function() {
            if(this.gridStore.getCount()<1) {
                this.itemsgrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.itemsgrid.getView().refresh();
            }
        }, this);

//        this.gridStore.load({
//            params:{
//                start:0,
//                limit:15
//            }
//        });
        this.add(this.itemsgrid);
        this.fetchStatement();
    },
    summaryRenderer:function(val){
        return WtfGlobal.currencyRenderer(Math.abs(val));
    },
    fetchStatement:function(){
//        this.sDate=this.startDate.getValue();
//        this.eDate=this.endDate.getValue();
        this.sdate=WtfGlobal.convertToGenericDate(this.startDate.getValue().add(Date.DAY,0));
        this.edate=WtfGlobal.convertToGenericDate(this.endDate.getValue().add(Date.DAY,1));
        if(this.sDate>this.eDate){
            WtfComMsgBox(2,2);
            return;
        }


        this.gridStore.load({params:{
            stdate:this.sdate,
            enddate:this.edate,
            start:0,
            limit:(this.pP.combo==undefined?15:this.pP.combo.value)
        }})
    },

    getDates:function(start){
        var d=new Date();
        if(this.statementType=='BalanceSheet'){
            if(start){
                return new Date('January 1, 1970 00:00:00 AM');
            }else
                return d;

        }
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    }
});
