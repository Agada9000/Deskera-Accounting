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
Wtf.ExportInterface=function(config) {
    Wtf.ExportInterface.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.ExportInterface, Wtf.Window, {

    initComponent : function(config) {
        this.winHeight = 424;
        if(Wtf.isIE6)
            this.winHeight = 413;
        
        this.operation = (this.type=="print")?"Print":"Export";
        this.topTitle = this.operation=="Print"?WtfGlobal.getLocaleText("acc.rem.28"):(this.type=="pdf"?WtfGlobal.getLocaleText("acc.common.exportToPDF") :WtfGlobal.getLocaleText("acc.common.exportToCSV") );  //"Export "+this.type+" file ";
        this.opt = this.operation=="Print"?WtfGlobal.getLocaleText("acc.rem.28"):WtfGlobal.getLocaleText("acc.rem.132")+" "+this.type+".";

        this.colSM = new Wtf.grid.CheckboxSelectionModel({
            width: 25
        });
        this.colCM = new Wtf.grid.ColumnModel([ this.colSM,{
            header: WtfGlobal.getLocaleText("acc.rem.29"),  //"Column",
            dataIndex: "title"
        },{
            header: "title",
            dataIndex: "header",
            hidden:true
        },{
            header: "index",
            dataIndex: "index",
            hidden:true
        },{
            header: "align",
            dataIndex: "align",
            hidden:true
        },{
            header: "Width",
            hidden:((this.type=="pdf")?false:true),
            dataIndex: 'width',
            editor: new Wtf.form.NumberField({
                allowBlank: false,
                maxValue: 850,
                minValue: 50
            })
        }]);
        this.headerField = new Wtf.form.TextField({
            labelSeparator:'',
            width: 180,
            emptyText: mainPanel.getActiveTab().title
        });
        this.colG = new Wtf.grid.EditorGridPanel({
            store: this.pdfDs,
            border: false,
            layout: "fit",
            width : 328,
            height:280,
            viewConfig: {
                forceFit: true
            },
            cm: this.colCM,
            autoScroll: true,
            clicksToEdit: 1,
            sm: this.colSM
        });
        this.colG.on("render", function(obj){
            obj.getSelectionModel().selectAll();
        }, this);
        this.title=this.type=="print"?WtfGlobal.getLocaleText("acc.common.print"):WtfGlobal.getLocaleText("acc.common.export"),  //this.operation;
        this.iconCls='pwnd deskeraImage';
        this.height= this.winHeight;
        this.width= 350;
        this.modal=true;
        this.layout="table";
        this.layoutConfig= {
            columns: 1
        };
        this.resizable=false;
        this.items= [{
            height: 75,
            border : false,
            cls :'exportFormat1',
            html : getTopHtml(this.topTitle ,this.opt,'../../images/createuser.png',true)
        },{
            cls :'exportFormat1',
            layout: 'fit',
            width : 338,
            items: [this.colG]
        }];
        this.buttons= [{
            text:"<< Previous",
            scope:this,
            hidden:((this.type=="pdf")?false:true),
            handler:function() {
                this.hide();
                this.parent.show();
            }
        },{
            text: (this.operation == "Print"? WtfGlobal.getLocaleText("acc.common.print"):WtfGlobal.getLocaleText("acc.common.export") ),
            scope: this,
            handler: function() {
            if(this.operation == "Print") {
                this.headerField.setValue(this.name);
            }
            var selCol = this.colSM.getSelections();
            if(selCol.length > 0){
                var header = [];
                var title = [];
                var width = [];
                var indx = [];
                var align=[];
                var k = 0;
                var flag=0;

                for(var i = 0; i < selCol.length; i++) {
                    var recData = selCol[i].data;
                    header.push(recData.header);
//                    if(recData.title.indexOf('(')!=-1) {
//                        recData.title=recData.title.substring(0,recData.title.indexOf('(')-1);
//                    }
                    if(recData.title.indexOf('*')!=-1) {
                        recData.title=recData.title.substring(0,recData.title.length-1);
                        title.push(recData.title);
                    } else
                        title.push(recData.title);
                    width.push(recData.width);
                    indx.push(recData.index);
                    if(recData.align=='')
                        align.push('none');
                    else
                        align.push(recData.align);


                }
                k = indx.length;
                for(i = 0; i < k; i++) {   //sort based on index
                    for(var j = i+1; j < k; j++) {
                        if(indx[i] > indx[j]) {
                            var temp = header[i];
                            header[i] = header[j];
                            header[j] = temp;

                            temp = title[i];
                            title[i] = title[j];
                            title[j] = temp;

                            temp = width[i];
                            width[i] = width[j];
                            width[j] = temp;

                            temp = align[i];
                            align[i] = align[j];
                            align[j] = temp;
                        }
                    }
                }
                if(this.type == "pdf") {
                    var max = Math.floor(820/k);  //820 = total width of pdf page
                    if(k >= (this.pdfDs.getTotalCount()*0.75)) {
                        max = 150;
                    }
                    max=Math.round(max);
                    for(i = 0; i < selCol.length; i++) {
                        if(selCol[i].data["width"] > max) {
                        flag = 1;
                        }
                    }
                    if(flag == 1) {
                        flag = 1;
                        Wtf.MessageBox.alert("Caution","The maximum width for fields is "+max);
                    }
                }
                if(flag == 0) {
                    this.close();
                    this.extra=Wtf.urlEncode(this.extra);
                    if(this.extra.length>0)
                        this.extra="&"+this.extra;
                    var exportUrl = getExportUrl(this.get);
                    var url = exportUrl+"?"+this.mode+this.extra+"&config="+this.configstr+"&filename="+this.filename+"&filetype=" + this.type+"&stdate="+this.stdate+"&enddate="+this.enddate+"&accountid="+this.accountid+"&nondeleted="+this.nondeleted+"&deleted="+this.deleted 
                            +"&header="+header+"&title="+title+"&width="+width+"&get="+this.get+"&align="+align+this.paramstring+"&neeraj="+this.neeraj;

                    if(this.ss!=undefined && this.ss!="") {
                        url += "&ss="+this.ss;
                    }
                    if(this.operation == "Print") {
                        window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src  = url;
                    }
                }
               } else {
                WtfComMsgBox(["Error", "Select at-least one column to display."], 1);
            }
         }
    },{
        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
        scope:this,
        handler: function(){
            this.close();
        }
    }];
    Wtf.ExportInterface.superclass.initComponent.call(this,config);
    }
 });

Wtf.exportButton=function(config){
    var mnuBtns=[];
    var btn=[];
    if(config.menuItem.csv==true){
        this.isPDF = false;
        mnuBtns.push(this.createButton("csv",config));
    }
    if(config.menuItem.pdf==true){
        this.isPDF = true;
        mnuBtns.push(this.createButton("pdf",config));
    }
    if(config.menuItem.rowPdf==true){
        mnuBtns.push(this.createRowButton(config));
        btn.push(2);
    }
    if(config.menuItem.print==true){
        mnuBtns.push(this.createPrintButton("print",config));
    }
    config.obj.grid.getSelectionModel().on('selectionchange', function(){
        WtfGlobal.enableDisableBtnArr(mnuBtns, config.obj.grid, btn,[]);
    }, this);

    var mainConfig={
        menu:mnuBtns
    };
    if(mnuBtns.length==1){
        var fbtn=mnuBtns.pop();
        mainConfig = fbtn.initialConfig;
    }
    Wtf.apply(this,mainConfig,config);
    Wtf.exportButton.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.exportButton,Wtf.Toolbar.Button,{
    text:WtfGlobal.getLocaleText("acc.common.export"),  //'Export',
    iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
    createPrintButton:function(type,config){
        if(config.label==undefined || config.label==null) {
            config.label="report";
        }
        var btn=new Wtf.Action({
            iconCls: 'pwnd printButtonIcon',
            tooltip: WtfGlobal.getLocaleText("acc.common.print"),  //{text: "Print "+config.label+" details."},
            scope: this,
            text:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print",
            handler:function(){
                this.exportWithTemplate(this.obj,type,this.get)
            }
        });
        return btn;
    },
    createButton:function(type,config){
        if(config.label==undefined || config.label==null)
            config.label="report";
        var btn=new Wtf.Action({
            iconCls:'pwnd '+(this.isPDF?'exportpdf':'exportcsv'),
            text :this.isPDF?("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>"):("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>"),
            scope: this,
            handler:function(){
                this.exportWithTemplate(this.obj,type,this.get)
            }
        });
        return btn;
    },
    createRowButton:function(config){
        var t=config.menuItem.rowPdfTitle||"Export Row";
        var btn=new Wtf.Action({
            text :"<span wtf:qtip=' "+t+" "+WtfGlobal.getLocaleText("acc.rem.129")+"'>"+t+" </span>",
            iconCls: 'pwnd exportpdf',
            scope: this,
            disabled:true,
            handler:function(){
                this.exportSingleRow(this.obj,config.get)
            }
        });
        return btn;
    },

    exportWithTemplate:function(obj,type,get) {
        obj.pdfStore =new Wtf.data.Store({});
        obj.pdfStore=this.filPdfStore(obj,obj.grid.getColumnModel());
        var paramsString = '';
        if(this.params){
            for(var index in this.params) {
               paramsString += "&"+index +"="+this.params[index]+"";
            }
        }
        var jsonGrid =this.genJsonForPdf(obj);
        if(type == "pdf") {
            new Wtf.selectTempWin({
                type:type,
                get:get,
                stdate:this.params?this.params.stdate:"",
                enddate:this.params?this.params.enddate:"",
                ss : (obj.quickSearchTF!=undefined) ? obj.quickSearchTF.getValue() : "",
                nondeleted:(obj.nondeleted!=undefined)?obj.nondeleted:true,
                deleted:(obj.deleted!=undefined)?obj.deleted:false,
                accountid:this.params?(this.params.accountid!=""?this.params.accountid:""):"",
                extra:this.extra||{},
                mode:Wtf.urlEncode(obj.grid.getStore().baseParams),
                paramstring:paramsString,
                filename:encodeURIComponent(Wtf.getCmp('as').getActiveTab().title),
                storeToload:obj.pdfStore,
                gridConfig : jsonGrid,
                grid:obj.EditorGrid,
                json:(obj.searchJson!=undefined)?obj.searchJson:""
            });
        } else {
            var expt =new Wtf.ExportInterface({
                type:type,
                get:get,
                ss : (obj.quickSearchTF!=undefined) ? obj.quickSearchTF.getValue() : "",
                nondeleted:(obj.nondeleted!=undefined)?obj.nondeleted:"",
                deleted:(obj.deleted!=undefined)?obj.deleted:"",
                stdate:this.params?this.params.stdate:"",
                enddate:this.params?this.params.enddate:"",
                accountid:this.params?(this.params.accountid!=""?this.params.accountid:""):"",
                name:this.params?(this.params.name!=""?this.params.name:""):"",
                extra:this.extra||{},
                paramstring:paramsString,
                mode:Wtf.urlEncode(obj.grid.getStore().baseParams),
                filename:encodeURIComponent(Wtf.getCmp('as').getActiveTab().title),
                pdfDs:obj.pdfStore,
                gridConfig : jsonGrid,
                grid:obj.EditorGrid
            });
            expt.show();
        }
    },
    setParams:function(params){
        if(!this.params)this.params={};
        for(var e in params){
            this.params[e]=params[e];
        }
    },
filPdfStore:function(obj,column)
{
    var k=1;
    for(var i=1 ; i<column.getColumnCount() ; i++) { // skip row numberer
      if(column.isHidden(i)==true||column.getColumnHeader(i)==""||column.getDataIndex(i)==""){
                continue;
      }
      else{
        if( column.config[i].pdfwidth!=undefined) {
            var format="";
            var title;
            if(column.getRenderer(i)==WtfGlobal.currencyRenderer || column.getRenderer(i)==WtfGlobal.currencyDeletedRenderer ||column.getRenderer(i)==WtfGlobal.currencySummaryRenderer || column.getRenderer(i)==WtfGlobal.currencyRendererDeletedSymbol) {
                format='currency';
            } else if(column.getRenderer(i)==WtfGlobal.withoutRateCurrencySymbol || column.getRenderer(i)==WtfGlobal.withoutRateCurrencyDeletedSymbol) {
                format='rowcurrency';
            } else if(column.getRenderer(i)==WtfGlobal.onlyDateRenderer || column.getRenderer(i)==WtfGlobal.onlyDateDeletedRenderer) {
                format='date';
            } else {
                if(column.config[i].pdfrenderer!=undefined) {
                    format=column.config[i].pdfrenderer;
                }
            }

            if(column.config[i].title==undefined)
                title=column.config[i].dataIndex;
            else
                title=column.config[i].title;
            obj.newPdfRec = new Wtf.data.Record({
                header : title,
                title : column.config[i].header,
                width : column.config[i].pdfwidth,
                align : format,
                index : k
            });
            obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
            k++;
        }
      }
    }
   return obj.pdfStore;
},

genJsonForPdf:function (obj)
{
	//this.neeraj = "";
    var jsondata = [];
    for(i=0;i<obj.pdfStore.getCount();i++) {
        var recData = obj.pdfStore.getAt(i).data;
//        jsondata="{'header':'" + recData.header + "',";
        if(recData.align=="right" && recData.title.indexOf("(")!=-1) {
            recData.title=recData.title.substring(0,recData.title.indexOf("(")-1);
        }
//        jsondata+="'title':'" + recData.title + "',";
//        jsondata+="'width':'" + recData.width + "',";
//        jsondata+="'align':'" + recData.align + "'},";
        var temp = {
        		header:recData.header,
        		title:recData.title,
        		width:recData.width,
        		align:recData.align
        };
        jsondata.push(temp)
    }

    return Wtf.encode({data:jsondata});
},
exportSingleRow:function(obj,get){
      var mode=0;
      var selRec=null;
      var fileName;			// File name to be assigned to the exported pdf.     Neeraj
        var recData=obj.grid.getSelectionModel().getSelected().data;
           if(get==Wtf.autoNum.Invoice||get==Wtf.autoNum.BillingInvoice){
              mode=get;
              selRec = "&amount="+recData.amount+"&bills="+recData.billid+"&customer="+recData.personname;
              fileName="Invoice "+recData.billno;
           }else if(get==Wtf.autoNum.PurchaseOrder||get==Wtf.autoNum.BillingPurchaseOrder){
              mode=get;
              selRec = "&amount="+recData.amount+"&bills="+recData.billid;
              fileName="Purchase Order "+recData.billno;
           }else if(get==Wtf.autoNum.SalesOrder||get==Wtf.autoNum.BillingSalesOrder){
              mode=get;
              selRec = "&amount="+recData.amount+"&bills="+recData.billid;
              fileName="Sales Order "+recData.billno;
           }else if(get==Wtf.autoNum.GoodsReceipt||get==Wtf.autoNum.BillingGoodsReceipt){
              mode=get;
              selRec = "&amount="+recData.amount+"&isexpenseinv="+recData.isexpenseinv+"&bills="+recData.billid;
              fileName="Vendor Invoice "+recData.billno;
           }else if(get==Wtf.autoNum.CreditNote||get==Wtf.autoNum.BillingCreditNote){
              mode=get;
              selRec = "&amount="+recData.amount+"&bills="+recData.noteid;
              fileName="Credit Note "+recData.noteno;
           }else if(get==Wtf.autoNum.DebitNote||get==Wtf.autoNum.BillingDebitNote){
               mode=get;
               selRec = "&amount="+recData.amount+"&bills="+recData.noteid;
               fileName="Debit Note "+recData.noteno;   
           }else if(get==Wtf.autoNum.Payment||get==Wtf.autoNum.BillingPayment){
              mode=get;
              selRec = "&amount="+recData.amount+"&bills="+recData.billid+"&customer="+recData.paymentmethod+"&accname="+recData.personname+"&personid="+recData.personid+"&address="+recData.address;
              fileName="Payment Made "+recData.billno;
           }else if(get==Wtf.autoNum.Receipt||get==Wtf.autoNum.BillingReceipt){
               mode=get;
               selRec = "&amount="+recData.amount+"&bills="+recData.billid+"&customer="+recData.paymentmethod+"&accname="+recData.personname+"&personid="+recData.personid+"&address="+recData.address;
               fileName="Payment Recieved "+recData.billno;   
           }else if(get==Wtf.autoNum.Quotation){
        	   mode=get;
               selRec = "&amount="+recData.amount+"&bills="+recData.billid;
               fileName="Quotation "+recData.billno;
           }
           if(get==Wtf.autoNum.GoodsReceipt||get==Wtf.autoNum.Payment||get==Wtf.autoNum.Receipt||get==Wtf.autoNum.Invoice||get==Wtf.autoNum.BillingInvoice||get==Wtf.autoNum.BillingReceipt||get==Wtf.autoNum.BillingGoodsReceipt||get==Wtf.autoNum.BillingPayment||get==Wtf.autoNum.BillingSalesOrder||get==Wtf.autoNum.BillingPurchaseOrder||get==Wtf.autoNum.CreditNote||get==Wtf.autoNum.DebitNote
            ||get==Wtf.autoNum.SalesOrder||get==Wtf.autoNum.PurchaseOrder||get==Wtf.autoNum.BillingDebitNote||get==Wtf.autoNum.BillingCreditNote||get==Wtf.autoNum.Quotation){
//               Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode="+mode+"&rec="+selRec+"&personid="+recData.personid+"&filename="+Wtf.getCmp('as').getActiveTab().title+"&filetype=pdf";
        	   Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode="+mode+"&rec="+selRec+"&personid="+recData.personid+"&filename="+fileName+"&filetype=pdf";								// File Name Changed     Neeraj 
            }else{
//                Wtf.get('downloadframe').dom.src = "ACCExportInvoice/getExportInv.do?mode="+mode+"&rec="+selRec+"&personid="+recData.personid+"&filename="+Wtf.getCmp('as').getActiveTab().title+"&filetype=pdf";
                Wtf.get('downloadframe').dom.src = "ACCExportInvoice/getExportInv.do?mode="+mode+"&rec="+selRec+"&personid="+recData.personid+"&filename="+fileName+"&filetype=pdf";								
            }

    }
 });
