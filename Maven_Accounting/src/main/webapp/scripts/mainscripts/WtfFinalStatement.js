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
Wtf.account.FinalStatement=function(config){
    this.total=[0,0];
    this.leftHeading=(config.statementType=="BalanceSheet"?"Liability":"Debit");
    this.rightHeading=(config.statementType=="BalanceSheet"?"Asset":"Credit");
     this.uPermType=Wtf.UPerm.fstatement;
     this.permType=Wtf.Perm.fstatement;
     this.exportPermType=(config.statementType=="BalanceSheet"?this.permType.exportdatabsheet:this.permType.exportdatatradingpnl);
     this.printPermType=(config.statementType=="BalanceSheet"?this.permType.printbsheet:this.permType.exportdatatradingpnl)
    this.toggle=0; 
    this.summaryL = new Wtf.ux.grid.GridSummary();
    this.summaryR = new Wtf.ux.grid.GridSummary();
    this.FinalStatementRec = new Wtf.data.Record.create([
        {name: 'accountname'},
        {name: 'accountid'},
        {name: 'amount'},
        {name: 'isdebit',type:'boolean'},
        {name: 'level'},
        {name: 'fmt'},
        {name: 'leaf'}
    ]);
    
    this.statementType=config.statementType||"Trading";

    var lStroe = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "left"							// Assets on Left side in Balance Sheet          Neeraj
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });
    var rStroe = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "right"							//  Liabilities on right side in Balance Sheet          Neeraj
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });
    this.lGrid = new Wtf.grid.HirarchicalGridPanel({
        plugins:[this.summaryL],
        //stripeRows :true,
        autoScroll:true,
        store: lStroe,
        hirarchyColNumber:0,
        columns: [{
            header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
            dataIndex:'accountname',
            renderer:this.formatAccountName,
            width:150,
            summaryRenderer:function(){return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));}.createDelegate(this)
        },{
            header:"<div align=right><b>"+(config.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(liability)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Debit)"))+"</b></div>",
            dataIndex:'amount',
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[0])
        }],
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true
        }
    });
    this.lGrid.on("render", WtfGlobal.autoApplyHeaderQtip);

    this.rGrid = new Wtf.grid.HirarchicalGridPanel({
        plugins:[this.summaryR],
        //stripeRows :true,
        autoScroll:true,
        store: rStroe,
        hirarchyColNumber:0,
        columns: [{
            header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
            dataIndex:'accountname',
            width:150,
            renderer:this.formatAccountName,
            summaryRenderer:function(){return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));}.createDelegate(this)
        },{
            header:"<div align=right><b>"+(config.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(asset)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)"))+"</b></div>",
            dataIndex:'amount',
            renderer:this.formatMoney,
            summaryRenderer:this.showLastRec.createDelegate(this,[1])
        }],
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true
        }
    });
    this.rGrid.on("render", WtfGlobal.autoApplyHeaderQtip);

    this.lGrid.on('render',function(){
        this.lGrid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.lGrid],1);
    },this);

    this.rGrid.on('render',function(){
        this.rGrid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.rGrid],1);
    },this);
    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        readOnly:true,
        hidden:config.statementType=='BalanceSheet',
        value:this.getDates(true)
    });
    this.endDate=new Wtf.form.DateField({
        fieldLabel:(config.statementType=='BalanceSheet'?WtfGlobal.getLocaleText("acc.balanceSheet.AsOn"):WtfGlobal.getLocaleText("acc.common.to")),
        format:WtfGlobal.getOnlyDateFormat(),
        readOnly:true,
        name:'enddate',
        value:this.getDates(false)
    });
    this.grid = this.rGrid;
    var mnuBtns=[];
    var csvbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate()
        }
    });
    mnuBtns.push(csvbtn)
     var pdfbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportpdf',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>",
        scope: this,
        handler:function(){
            this.exportPdfTemplate()
        }
    });
    mnuBtns.push(pdfbtn)

    chkCostCenterload();
    if(Wtf.CostCenterStore.getCount()==0) Wtf.CostCenterStore.on("load", this.setCostCenter, this);
    this.costCenter = new Wtf.form.ComboBox({
        store: Wtf.CostCenterStore,
        name:'costCenterId',
        width:140,
        displayField:'name',
        valueField:'id',
        triggerAction: 'all',
        mode: 'local',
        typeAhead:true,
        value:"",
        selectOnFocus:true,
        forceSelection: true,
        emptyText:WtfGlobal.getLocaleText("acc.rem.9")  //,"Select a Cost Center"
    });

    var btnArr=[];

    btnArr.push(
        (config.statementType=='BalanceSheet'?'':WtfGlobal.getLocaleText("acc.common.from")),this.startDate,
        (config.statementType=='BalanceSheet'?WtfGlobal.getLocaleText("acc.balanceSheet.AsOn"):WtfGlobal.getLocaleText("acc.common.to")),this.endDate
        );

    if(this.statementType=="TradingAndProfitLoss" || this.statementType=="CostCenter"){
        btnArr.push(WtfGlobal.getLocaleText("acc.common.costCenter"),this.costCenter);
    }

    btnArr.push('-',{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls:'accountingbase fetch',
        tooltip:(config.statementType=='BalanceSheet'?WtfGlobal.getLocaleText("acc.cc.26"):WtfGlobal.getLocaleText("acc.cc.27")),
        scope:this,
        handler:this.fetchStatement
    });
    
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr.push(this.expButton=new Wtf.Button({
    //        iconCls:'pwnd '+'exportcsv',
            text:WtfGlobal.getLocaleText("acc.common.export"),  //'Export',
            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
                    disabled :true,
            scope: this,
            menu:mnuBtns
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
        
        btnArr.push(this.printbtn=new Wtf.Button({
            iconCls:'pwnd printButtonIcon',
            text :WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details.',
            disabled :true,
            scope: this,
            handler:function(){
                this.exportWithTemplate("print")
            }
        }));
    }

    btnArr.push(this.ToggleButton=new Wtf.Button({			// Used for toggling assets and liabilities from Left to Right & Vice versa
            text:WtfGlobal.getLocaleText("acc.balanceSheet.toggle"),  //'Toggle',
            iconCls:'pwnd toggleButtonIcon',
            tooltip :(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.toggleTT"):WtfGlobal.getLocaleText("acc.balanceSheet.toggleTT")),
            scope: this,
            handler: this.swapGrids,
            hidden:(config.statementType=="BalanceSheet"?false:true)
        }));
    
//   this.expButton=new Wtf.exportButton({
//        obj:this,
//        id:"exportReports"+config.helpmodeid,
//        tooltip :'Export report details',
//        params:{ stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
//                 enddate:WtfGlobal.convertToGenericDate(this.getDates(false))/*,
//                 accountid:this.accountID||config.accountID*/
//        },
//        menuItem:{csv:true,pdf:true,rowPdf:false},
//        get:27
//    });
    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        items:[
            this.centerPanel = new Wtf.Panel({
                region:'center',
                layout:'fit',
                width:'49%',
                border:true,
                items:this.statementType=="BalanceSheet"?this.lGrid:this.rGrid
            }),
            this.westPanel = new Wtf.Panel({
                region:'west',
                layout:'fit',
                width:'49%',
                border:false,
                split:true,
                items:this.statementType=="BalanceSheet"?this.rGrid:this.lGrid
            })]
    });

    Wtf.apply(this,{
        saperate:true,
        statementType:"Trading",
        items:this.wrapperPanel,
        tbar:btnArr
    },config);

     Wtf.account.FinalStatement.superclass.constructor.call(this,config);
     if(this.statementType!="CostCenter"){
        this.fetchStatement();
     }
}

Wtf.extend( Wtf.account.FinalStatement,Wtf.Panel,{
    exportPdfTemplate:function(){
        var get;
        var fileName;
        var jsonGrid;
        var exportUrl;
        var header;
        var extraParams="";
        if(this.statementType=="BalanceSheet"){
//            title = "Particulars,Amount(Asset),Particulars,Amount(Liability)";
            exportUrl = getExportUrl(27);
            fileName = "BalanceSheet";
            get = 27;
            toggle = this.toggle;
            jsonGrid = "{data:[{'header':'laccountname','title':'Particulars','width':'150','align':''},"+
                            "{'header':'lamount','title':'Amount(Asset)','width':'150','align':'currency'},"+
                            "{'header':'raccountname','title':'Particulars','width':'150','align':''},"+
                            "{'header':'ramount','title':'Amount(Liability)','width':'150','align':'currency'}]}";
            header =  "Balance Sheet";
        }else{
//            title = "Particulars,Amount(Debit),Particulars,Amount(Credit)";
            exportUrl = getExportUrl(28);
            if(this.statementType=="TradingAndProfitLoss"){
                fileName = "Trading Profit and Loss Statement";
            } else if (this.statementType=="CostCenter"){
                fileName = "Cost Center Report";
            }
            extraParams += "&costcenter="+this.costCenter.getValue();
            extraParams += "&reportView="+this.statementType;
            get = 28;
            jsonGrid = "{data:[{'header':'laccountname','title':'Particulars','width':'150','align':''},"+
                            "{'header':'lamount','title':'Amount(Debit)','width':'150','align':'currency'},"+
                            "{'header':'raccountname','title':'Particulars','width':'150','align':''},"+
                            "{'header':'ramount','title':'Amount(Credit)','width':'150','align':'currency'}]}";
            header =  fileName;
        }
        var configstr = "{%22landscape%22:%22true%22,%22pageBorder%22:%22true%22,%22gridBorder%22:%22true%22,%22title%22:%22Test%20Title%22,%22subtitles%22:%22%22,%22headNote%22:%22Test%20Header%22,%22showLogo%22:%22false%22,%22headDate%22:%22false%22,%22footDate%22:%22true%22,%22footPager%22:%22false%22,%22headPager%22:%22true%22,%22footNote%22:%22Test%20Footer%22,%22textColor%22:%22000000%22,%22bgColor%22:%22FFFFFF%22}"
         var url = exportUrl+"?filename="+fileName+extraParams+"&config="+configstr+"&header"+header+"&filetype=pdf&stdate="+this.sdate+"&enddate="+this.edate+"&toggle="+this.toggle
                     +"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid);
            Wtf.get('downloadframe').dom.src = url;

        /*new Wtf.selectTempWin({
                type:'pdf',
                get:get,
                stdate:this.sdate,
                enddate:this.edate,
                accountid:"",
                extra:{},
                mode:"",
                paramstring:"",
                filename:fileName,
                storeToload:"",//obj.pdfStore,
                gridConfig : jsonGrid,
                grid:"",
                json:""
            });*/
    },
    setCostCenter: function(){
        this.costCenter.setValue("");//Select Default Cost Center as None
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
    resetFilterAndFetchReport: function(costCenterId, startDate, endDate){
        this.costCenter.setValue(costCenterId);
        this.startDate.setValue(startDate?startDate:this.getDates(true));
        this.endDate.setValue(endDate?endDate:this.getDates(false));
        this.fetchStatement();
    },
    exportWithTemplate:function(type){
        type = type?type:"csv";
        var exportUrl;
        var fileName;
        var reportName;
        var header = "laccountname,lamount,raccountname,ramount";
         var title = "";
         var extraParams = "";
        if(this.statementType=="BalanceSheet"){
        	if(this.toggle == 0){
                title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.18")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.19");
        	}else{
                title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.19")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.18");
        	}
            header = "raccountname,ramount,laccountname,lamount";
            exportUrl = getExportUrl(27);
            fileName = WtfGlobal.getLocaleText("acc.balanceSheet");
            reportName = WtfGlobal.getLocaleText("acc.balanceSheet");
        }else{
            title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.3")+","+WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.4");
            exportUrl = getExportUrl(28);
            if(this.statementType=="TradingAndProfitLoss"){
                fileName = WtfGlobal.getLocaleText("acc.P&L.tabTitle");
                reportName = WtfGlobal.getLocaleText("acc.P&L.tabTitle");
            } else if (this.statementType=="CostCenter"){
                fileName = WtfGlobal.getLocaleText("acc.ccReport.tabTitle");
                reportName = WtfGlobal.getLocaleText("acc.ccReport.tabTitle");
            }
            extraParams += "&costcenter="+this.costCenter.getValue();
            extraParams += "&reportView="+this.statementType;
        }

        var align = "none,currency,none,currency";
        var url = exportUrl+"?filename="+fileName+extraParams+"&filetype="+type+"&stdate="+this.sdate+"&enddate="+this.edate+"&nondeleted="+true+"&accountid="
                            +"&header="+header+"&title="+title+"&name="+reportName+"&width=150&get=27&align="+align+"&toggle="+this.toggle;

        if(type == "print") {
            window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
        } else {
            Wtf.get('downloadframe').dom.src  = url;
        }
        Wtf.get('downloadframe').dom.src = url;
    },
    getRowClass:function(record,grid){
        var colorCss="";
        switch(record.data["fmt"]){
            case "T":colorCss=" grey-background";break;
            case "B":colorCss=" red-background";break;
            case "H":colorCss=" header-background";break;
            case "A":colorCss=" darkyellow-background";break;
        }
        return grid.getRowClass()+colorCss;
    },
    
    formatAccountName:function(val,m,rec,i,j,s){
        var fmtVal=val;
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal; 
    },

    formatMoney:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    },

    fetchStatement:function(){
       WtfComMsgBox(29,4,true); //Show loading mask
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

        var params={
         //   fordate:WtfGlobal.convertToGenericDate(this.forDate.getValue().add(Date.DAY,1)),
            stdate:this.sdate,
            enddate:this.edate
        }
        this.ajxUrl = Wtf.req.account+'CompanyManager.jsp';
        if(this.statementType=="Trading") {
            params.mode=63;
            params.nondeleted=true;
            this.ajxUrl = "ACCReports/getTrading.do";
        }
        if(this.statementType=="ProfitAndLoss") {
            params.mode=64;
            params.nondeleted=true;
            this.ajxUrl = "ACCReports/getProfitLoss.do";
        }
        if(this.statementType=="TradingAndProfitLoss" || this.statementType=="CostCenter") {
            params.mode=65;
            params.nondeleted=true;
            params.costcenter = this.costCenter.getValue();
            params.reportView = this.statementType;
            this.ajxUrl = "ACCReports/getTradingAndProfitLoss.do";
        }
        if(this.statementType=="BalanceSheet") {
            params.mode=66;
            params.nondeleted=true;
            this.ajxUrl = "ACCReports/getBalanceSheet.do";
        }
//        this.expButton.setParams({
////                    accountid:this.accountID,
//                    stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
//                    enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue())
//            });
        Wtf.Ajax.requestEx({url:this.ajxUrl,params:params}, this, this.successCallback, this.failureCallback);
    },

    successCallback:function(response){
        if(response.success){
            this.total=response.data.total;
            this.lGrid.store.loadData(response.data);
            this.rGrid.store.loadData(response.data);
            this.doLayout();
            if((this.statementType!="TradingAndProfitLoss" && this.total && this.total[0]==0 && this.total[1]==0)
                ||(this.statementType=="TradingAndProfitLoss" && this.lGrid.store.getCount()<=3 && this.rGrid.store.getCount()<=3)){
                    if(this.expButton)this.expButton.disable();
                    if(this.printbtn)this.printbtn.disable();
                }else{
                    if(this.expButton)this.expButton.enable();
                    if(this.printbtn)this.printbtn.enable();
            }
        }
        this.hideLoading();
    },

    failureCallback:function(response){
        this.hideLoading();
    },

    hideLoading:function(){
        Wtf.MessageBox.hide();
    },
    
    getDates:function(start){
        var d=Wtf.serverDate;
        if(this.statementType=='BalanceSheet'&&start)
             return new Date('January 1, 1970 00:00:00 AM');
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom&&this.statementType!='BalanceSheet')
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start||this.statementType=='BalanceSheet')
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    showLastRec:function(pos){
        return WtfGlobal.currencySummaryRenderer(this.total[pos]);
    },
    
    swapGrids:function(){			// The summaryRenderer doesnt swap when the grids swap. Only the header and grid data swaps.
    	this.Lcm = this.lGrid.getColumnModel();
    	this.Rcm = this.rGrid.getColumnModel();
    	this.lstore1 = this.lGrid.getStore();
    	this.rstore1 = this.rGrid.getStore();
    	this.lGrid. reconfigure(this.rstore1,this.Rcm);
    	this.rGrid. reconfigure(this.lstore1,this.Lcm);
    	if(this.toggle==0)
    		this.toggle=1;
    	else
    		this.toggle=0;
    }
});
