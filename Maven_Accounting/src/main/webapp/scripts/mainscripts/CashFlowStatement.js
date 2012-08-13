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
Wtf.account.CashFlowStatement = function(config){
	
	this.createGrid();
//	this.fetchData();

	config.layout='border';
    
	Wtf.apply(this,{
                autoScroll:true,
                border:false,
                defaults:{border:false,bodyStyle:"background-color:white;"},
                items:[this.Grid,{layout:'fit',region:'west',width:'20%'},{layout:'fit',region:'east',width:'20%'}],
                tbar:[{
                    xtype:'button',
                    text:WtfGlobal.getLocaleText("acc.ra.fetch"),
                    tooltip:WtfGlobal.getLocaleText("acc.nee.8"),
                    iconCls:getButtonIconCls(Wtf.etype.resetbutton),
//                    tooltip:"Select a time period to view corresponding ratio analysis.",
                    scope:this,
                    handler:this.fetchData
                },this.btnArr,this.printbtn]
                
    },config);
	
	Wtf.account.CashFlowStatement.superclass.constructor.call(this, config);
    
},

Wtf.extend(Wtf.account.CashFlowStatement, Wtf.Panel, {

	onRender: function(config) {
	
		Wtf.account.CashFlowStatement.superclass.onRender.call(this, config);
	
	    Wtf.Ajax.requestEx({
	      url : "ACCReports/getCashFlow.do",
	      params: {cashFlowReport : true}
	  }, this, this.successCallback);
	    
	},

	successCallback:function(response){
	    if(response.success){
	        this.Grid.store.loadData(response.data);
	        this.doLayout();
	    }
	    if(this.loadMask1!=undefined)
	    	this.loadMask1.hide();
	},

	fetchData:function(){
        this.loadMask1 = new Wtf.LoadMask("cashFlow", {msg: "Loading...", msgCls: "x-mask-loading acc-cashFlow-form-mask"});
        this.loadMask1.show();
		Wtf.Ajax.requestEx({
		      url : "ACCReports/getCashFlow.do",
		      params: {cashFlowReport : true}
		  }, this, this.successCallback);
	},
	
	createGrid:function(){
		 var rec = new Wtf.data.Record.create([
	                {name: 'name'},
	                {name: 'desc'},
	                {name: 'value'},
		            {name: 'fmt'}
		           ]);
		 
		 var Store = new Wtf.data.Store({
		                reader: new Wtf.data.KwlJsonReader({
		                },rec)
		              });
		
		this.Grid = new Wtf.grid.GridPanel({
	        autoScroll:true,
	        store: Store,
	        layout:'fit',
	    	width:'60%',
	      	region:'center',
	        columns: [{
	            header:'<font size=2px ><b>'+WtfGlobal.getLocaleText("acc.report.2")+'</b>',
	            dataIndex:'name',
	            renderer:this.formatName
	        },{
	            header:'<font size=2px ><b>'+WtfGlobal.getLocaleText("acc.ra.value")+'</b>',
	            align:'right',
	            dataIndex:'value',
	            renderer:this.opBalRenderer
	        }],
	        border : false,
	        loadMask : true,
	        viewConfig: {
	            forceFit:true,
	            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
	        }
	    });
		
		
	    Store.on('load', function() {
	        if(Store.getCount()<1) {
	            this.Grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
	            this.Grid.getView().refresh();
	        }
	    }, this);
	    
	    
		this.csvbtn=new Wtf.Action({
	        iconCls:'pwnd '+'exportcsv',
	        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
	        scope: this,
	        handler:function(){
	            this.exportWithTemplate()
	        }
	    });
		
	    this.printbtn=new Wtf.Action({
	        iconCls:'pwnd printButtonIcon',
	        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print Report Details.',
	        text : WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
	        scope: this,
	        handler:function(){
	            this.printcashFlow();
	        }
	    });
	    
	     this.pdfbtn=new Wtf.Action({
	         iconCls:'pwnd '+'exportpdf',
	         text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>",
	         scope: this,
	         handler:function(){
	             this.exportPdfTemplate()
	         }
	     });
	     
			this.mnuBtns=[];

			this.mnuBtns.push(this.csvbtn);
			this.mnuBtns.push(this.pdfbtn);
	     this.btnArr=[]; 
	    this.btnArr.push(this.expButton=new Wtf.Button({
	                    text:WtfGlobal.getLocaleText("acc.common.export"),  //'Export',
	                    iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
	                    tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
	                    scope: this,
	                    menu:this.mnuBtns
	                }));

	
	},

	formatName:function(val, m, rec){
	    if(rec.data.desc && rec.data.fmt != "title" && rec.data.fmt != "total"){
	    	return '<div><font size=2px >'+val+'</font></div><div class="grid-row-desc">'+rec.data.desc+'</div>';
	    }else if(rec.data.fmt == "total"){
	    	return '<div><font size=2px ><b>'+val+'</b></font><div class="grid-row-desc">'+rec.data.desc+'</div>';
	    }else if(rec.data.fmt == "title"){
	    	return '<div align=right><font size=2px ><b>'+val+'</b></font><div class="grid-row-desc">'+rec.data.desc+'</div>';
	    }
	    return val;
	},

	opBalRenderer:function(val,m,rec){
		if(rec.data.fmt != "title"){
			if(rec.data.fmt != "total")
				return WtfGlobal.withoutRateCurrencyDeletedSymbol(val,m,rec);
			else
				return '<font size=2px ><b>'+WtfGlobal.withoutRateCurrencyDeletedSymbol(val,m,rec)+'</b>';
		}
	},

	printcashFlow:function(){
        var exportUrl;
        var header = "lname,lvalue";
        var title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.ra.value");
        exportUrl = "ACCReports/exportCashFlow.do";
        var align = "none,none";
        var url = exportUrl+"?name="+WtfGlobal.getLocaleText("acc.dashboard.cashFlowStatement")+"&filetype=print&stdate="+this.sdate+"&enddate="+this.edate+"&accountid="
                            +"&header="+header+"&title="+title+"&width=150&get=27&align="+align;

        window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
    },    
    
    exportWithTemplate:function(){
        var exportUrl;
        var fileName;
        var header = "lname,lvalue";
        var title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.ra.value");
        
        exportUrl = "ACCReports/exportCashFlow.do";
        fileName = WtfGlobal.getLocaleText("acc.dashboard.cashFlowStatement");
        

        var align = "none,none";
        var url = exportUrl+"?filename="+fileName+"&filetype=csv&stdate="+this.sdate+"&enddate="+this.edate+"&accountid="
                            +"&header="+header+"&title="+title+"&width=150&get=27&align="+align;
        Wtf.get('downloadframe').dom.src = url;
    },
    
    exportPdfTemplate:function(){
        var get;
        var fileName;
        var jsonGrid;
        var exportUrl;
        exportUrl = "ACCReports/exportCashFlow.do";
        fileName = WtfGlobal.getLocaleText("acc.dashboard.cashFlowStatement");
        get = 29;
        jsonGrid = "{data:[{'header':'lname','title':'"+WtfGlobal.getLocaleText("acc.report.2")+"','width':'200','align':''},"+
                        "{'header':'lvalue','title':'"+WtfGlobal.getLocaleText("acc.ra.value")+"','width':'100','align':'currency'},";

        var configstr = "{%22landscape%22:%22true%22,%22pageBorder%22:%22true%22,%22gridBorder%22:%22true%22,%22title%22:%22Test%20Title%22,%22subtitles%22:%22%22,%22headNote%22:%22Test%20Header%22,%22showLogo%22:%22false%22,%22headDate%22:%22false%22,%22footDate%22:%22true%22,%22footPager%22:%22false%22,%22headPager%22:%22true%22,%22footNote%22:%22Test%20Footer%22,%22textColor%22:%22000000%22,%22bgColor%22:%22FFFFFF%22}"
        var url = exportUrl+"?filename="+fileName+"&config="+configstr+"&filetype=pdf&stdate="+this.sdate+"&enddate="+this.edate+"&accountid="
                     +"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid);
        Wtf.get('downloadframe').dom.src = url;
    },

	createReport:function(){
	
	} 


});
