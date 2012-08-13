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
Wtf.account.TrialBalance=function(config){
    this.summary = new Wtf.ux.grid.GridSummary();
    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.exportPermType=this.permType.exportdatatrialbalance;
    this.printPermType=this.permType.printtrialbalance
    this.TrialBalanceRec = new Wtf.data.Record.create([
        {name: 'accountid'},
        {name: 'accountname'},
        {name: 'd_amount'},
        {name: 'c_amount'},
        {name: 'fmt'}
    ]);
    this.TrialBalanceStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.TrialBalanceRec),
//                url: Wtf.req.account+'CompanyManager.jsp',
                url:"ACCReports/getTrialBalance.do",
                baseParams:{
                    mode:62
                }
            });
            this.rowNo=new Wtf.grid.RowNumberer();
            this.gridcm= new Wtf.grid.ColumnModel([this.rowNo,
            {
                dataIndex: 'accountid',
                hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.trial.acc"),  //"Account",
                dataIndex: 'accountname',
                pdfwidth:150,
                renderer:this.nameRenderer,
                summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
            },{
                header: WtfGlobal.getLocaleText("acc.trial.debit"),  //'Debit',
                dataIndex: 'd_amount',
                width:40,
                pdfwidth:150,
                align:'right',
                renderer:WtfGlobal.currencyRenderer,
                summaryType:'sum',
                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{
                header :WtfGlobal.getLocaleText("acc.trial.credit"),  //'Credit',
                dataIndex: 'c_amount',
                width:40,
                pdfwidth:150,
                align:'right',
                renderer:WtfGlobal.currencyRenderer,
                summaryType:'sum',
                summaryRenderer:WtfGlobal.currencySummaryRenderer
            }]);
            this.grid = new Wtf.grid.GridPanel({
                stripeRows :true,
                store: this.TrialBalanceStore,
                cm: this.gridcm,
                border : false,
                loadMask : true,
                plugins:[this.summary],
                viewConfig: {
                    forceFit:true,
                    getRowClass:this.getRowClass.createDelegate(this),
                    emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
                }
            });
            this.TrialBalanceStore.on("load", function(store){
                if(store.getCount()==0){
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
                        if(this.expButton)this.expButton.disable();}
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
                    if(this.printButton)this.printButton.disable();}

                    this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                    this.grid.getView().refresh();
                }else{
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))
                        if(this.expButton)this.expButton.enable();
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
                        if(this.printButton)this.printButton.enable();}
                }
            },this);

    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        readOnly:true,
        value:this.getDates(true)
    });
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        readOnly:true,
        value:this.getDates(false)
    });
    var btnArr=[];
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr.push(this.expButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            disabled :true,
            params:{stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
                   enddate:WtfGlobal.convertToGenericDate(this.getDates(false)),
                   accountid:this.accountID,
                   name: WtfGlobal.getLocaleText("acc.trial.tabtitle")
            },
            menuItem:{csv:true,pdf:true,rowPdf:false},
            get:116
        }));
     }
     if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
        btnArr.push(this.printButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.print"),
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            params:{stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
                   enddate:WtfGlobal.convertToGenericDate(this.getDates(false)),
                   accountid:this.accountID,
                   name: WtfGlobal.getLocaleText("acc.trial.tabtitle")
            },
            label:"Trial Balance",
            menuItem:{print:true},
            get:116
        }));
    }
    this.fetchTrialBalance();
    Wtf.apply(this,{
        items:[{
            border:false,
            layout : "border",
            scope:this,
            items:[{
                region:'center',
                layout:'fit',
                border:false,
                items:[this.grid]
            }],
            tbar:[WtfGlobal.getLocaleText("acc.common.from"),this.startDate,'-',WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                tooltip:WtfGlobal.getLocaleText("acc.trial.fetchTT"),  //"Select a time period to view corresponding trial balance records.",
                iconCls:'accountingbase fetch',
                scope:this,
                handler:this.fetchTrialBalance
            },'-',btnArr,"->",getHelpButton(this,25)]
        }]

    },config);
//    Wtf.apply(this,{
//        items:this.TrialBalancegrid
//    },config)

     Wtf.account.TrialBalance.superclass.constructor.call(this,config);
     this.addEvents({
        'account':true
     });
     this.grid.on('rowclick',this.onRowClick, this);
}
Wtf.extend( Wtf.account.TrialBalance,Wtf.Panel,{
    nameRenderer:function(v,m,rec){
        return (rec.data["fmt"]?'<b>'+v+'</b>':WtfGlobal.linkRenderer(v));
    },

    currencyRenderer:function(v,m,rec){
        return (rec.data["fmt"]?WtfGlobal.currencySummaryRenderer(v):WtfGlobal.currencyRenderer(v));
    },

    getRowClass:function(record,grid){
        var colorCss="";
        switch(record.data["fmt"]){
            case "T":colorCss=" grey-background";break;
            case "B":colorCss=" red-background";break;
            case "H":colorCss=" header-background";break;
            case "A":colorCss=" darkyellow-background";break;
        }
        return colorCss;
    },
    fetchTrialBalance:function(){
       this.sDate=this.startDate.getValue();
       this.eDate=this.endDate.getValue();

       if(this.sDate=="" || this.eDate=="") {
           WtfComMsgBox(42,2);
           return;
       }

       this.sdate=WtfGlobal.convertToGenericDate(this.startDate.getValue().add(Date.DAY,0));
       this.edate=WtfGlobal.convertToGenericDate(this.endDate.getValue().add(Date.DAY,1));
         if(this.sDate>this.eDate){
             WtfComMsgBox(1,2);
            return;
         }
        this.TrialBalanceStore.load({
            params:{
                  stdate:this.sdate,
                  enddate:this.edate
            }
         });
         if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
             this.expButton.setParams({
                   stdate:this.sdate,
                   enddate:this.edate,
                   accountid:this.accountID,
                   name: WtfGlobal.getLocaleText("acc.trial.tabtitle")
             });
         }
         if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
             this.printButton.setParams({
                   stdate:this.sdate,
                   enddate:this.edate,
                   accountid:this.accountID,
                   name: WtfGlobal.getLocaleText("acc.trial.tabtitle")
             });
         }
         
    },

    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.TrialBalanceStore.getAt(i).data['accountid'];
        this.fireEvent('account',accid);
    },
    getDates:function(start){
        var d=new Date();
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
