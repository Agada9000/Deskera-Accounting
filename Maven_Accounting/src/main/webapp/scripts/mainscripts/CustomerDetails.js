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
function openPersonWin(isCustomer){
    if(isCustomer=='true')
        callBusinessContactWindow(false,null,null,true);
    else
        callBusinessContactWindow(false,null,null,false);
}
Wtf.account.BusinessContactPanel=function(config){
    this.withinventory=config.withinventory||false;
    this.summary = new Wtf.ux.grid.GridSummary();
    this.personlinkid=config.personlinkid;
    this.perAccID=null;
    this.openperson=config.openperson;
    this.isEdit=false;
    this.recArr=[];
    this.isAdd=false;
    this.nondeleted=false;
    this.deleted=false;
    this.typeEditor = new Wtf.form.ComboBox({
        store: Wtf.delTypeStore,
        name:'typeid',
        displayField:'name',
        valueField:'typeid',
        mode: 'local',
        value:0,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    this.businessPerson=(config.isCustomer?'Customer':'Vendor');
    this.uPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.permType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.GridRec = Wtf.data.Record.create ([
        {name:'accid'},
        {name:'openbalance'},
        {name:'id'},
        {name:'title'},
        {name:'accname'},
        {name:'address'},
        {name:'personname',mapping:'accname'},
        {name:'personemail',mapping:'email'},
        {name:'personid',mapping:'id'},
        {name:'taxeligible',type:'boolean'},
        {name:'taxidnumber'},
        {name:'company'},
        {name:'email'},
        {name:'contactno'},
        {name:'contactno2'},
        {name:'fax'},
        {name:'shippingaddress'},
        {name:'pdm'},
        {name:'pdmname'},
        {name:'parentid'},
        {name:'parentname'},
        {name:'bankaccountno'},
        {name:'termid'},
        {name:'termname'},
        {name:'other'},
        {name: 'leaf'},
        {name: 'currencysymbol'},
        {name: 'currencyname'},
        {name: 'currencyid'},
        {name: 'istaxeligible'},
        {name: 'deleted'},
        {name: 'creationDate' ,type:'date'},
        {name: 'categoryid'},
        {name: 'taxno'},
        {name: 'level'},
        {name: 'amountdue'}
    ]);
    this.msgLmt = 30;
    this.jReader = new Wtf.data.KwlJsonReader({
        totalProperty: 'totalCount',
        root: "data"
    }, this.GridRec);
    this.Store = new Wtf.data.Store({
        title:this.businessPerson+" Information",
//        url:Wtf.req.account+this.businessPerson+'Manager.jsp',
        url:"ACC"+this.businessPerson+"CMN/get"+this.businessPerson+"s.do",
        baseParams:{
            mode:2,
            group:(config.isCustomer?[10]:[13])
        },
        reader: this.jReader
    });
    this.Store.on('beforeload',function(s,o){
        if(!o.params)o.params={};
        o.params.deleted=this.deleted;
        o.params.nondeleted=this.nondeleted;
    },this);

    var glblStore=(config.isCustomer?Wtf.customerAccStore:Wtf.vendorAccStore);
    glblStore.on('beforeload',function(){this.Store.reload();},this);
    this.Store.load({
        params: {
            start: 0,
            limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
        }
    });
    WtfComMsgBox(29,4,true);
    this.btnArr=[];
    this.btnArrEDSingleS=[]; // Enable/Disable button's indexes on single select
    this.btnArrEDMultiS=[]; // Enable/Disable button's indexes on multi select
    var chart=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.chart"),  //'Chart',
        tooltip :config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.chartTT"):WtfGlobal.getLocaleText("acc.vendorList.chartTT"),  //'Get the graphical view of your key '+this.businessPerson+'(s).',
       // id: 'chartRec' + this.businessPerson,
        scope: this,
        handler:this.getChart,
        id:'chart'+(config.isCustomer?6:7),
       iconCls :(Wtf.isChrome?'accountingbase chartChrome':'accountingbase chart')

    });
    this.addPerson=new Wtf.Toolbar.Button({
            text:config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.addNewCustomer"):WtfGlobal.getLocaleText("acc.vendorList.addNewvendor"),  //'Add New '+this.businessPerson,
            id:(config.isCustomer?"addNewCustomer6":"vendorsManagement7"),
            scope:this,
            tooltip:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.addTT"):WtfGlobal.getLocaleText("acc.vendorList.addTT")), //"Add new "+this.businessPerson+" details. You may also add a sub-"+this.businessPerson+" to an existing "+this.businessPerson+".",
            iconCls:getButtonIconCls(Wtf.etype.add),
            handler:this.showForm.createDelegate(this,[false])
        })
        this.editPerson=new Wtf.Toolbar.Button({
            text:config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.editCustomer"):WtfGlobal.getLocaleText("acc.vendorList.editvendor"),  //'Edit '+this.businessPerson,
            id:(config.isCustomer?'editCustomer6':'editVendor7'),
            scope:this,
            tooltip:config.isCustomer?WtfGlobal.getLocaleText("acc.rem.51"):WtfGlobal.getLocaleText("acc.rem.52"),  //{text:"Select a "+this.businessPerson+" to edit.",dtext:"Select a "+this.businessPerson+" to edit.", etext:"Edit selected "+this.businessPerson+" details."},
            disabled:true,
            iconCls:getButtonIconCls(Wtf.etype.edit),
            handler:this.showForm.createDelegate(this,[true])
        })
       this.deletePerson=new Wtf.Toolbar.Button({
            text:config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.deleteCustomer"):WtfGlobal.getLocaleText("acc.vendorList.deletevendor"),  // 'Delete '+this.businessPerson,
            id:(config.isCustomer?'deleteCustomer6':'deleteVendor7'),
            scope: this,
            tooltip:config.isCustomer?WtfGlobal.getLocaleText("acc.rem.49"):WtfGlobal.getLocaleText("acc.rem.50"),  //{text:"Select a "+this.businessPerson+" to delete.",dtext:"Select a "+this.businessPerson+" to delete.", etext:"Delete selected "+this.businessPerson+" details."},
            disabled:true,
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            handler:this.deletePersonFunc.createDelegate(this) 
        })
        this.quickSearchTF = new Wtf.KWLTagSearch({
            emptyText:config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.search"):WtfGlobal.getLocaleText("acc.vendorList.search"),  //'Search by '+this.businessPerson+' Name',
            width: 150,
            field: 'accname',
            Store:this.Store
        });
        this.btnArr.push(this.quickSearchTF);
                 this.btnArr.push(this.resetBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            hidden:this.isSummary,
            tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search '+this.businessPerson+' name'+' by clearing existing search  '+this.businessPerson+' name'+'.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        }));
        this.resetBttn.on('click',this.handleResetClick,this);
    if(!WtfGlobal.EnableDisable(this.uPermType,this.permType.create)) {
        this.btnArr.push(this.addPerson);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)) {
        this.btnArr.push(this.editPerson);
        this.btnArrEDSingleS.push(this.btnArr.length-1);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove)) {
        this.btnArr.push(this.deletePerson);
        this.btnArrEDMultiS.push(this.btnArr.length-1);
    }

    this.sm = new Wtf.grid.CheckboxSelectionModel({
    	header: (Wtf.isIE7)?"":'<div class="x-grid3-hd-checker"> </div>'    // For IE 7 the all select option not available
    });
    this.grid = new Wtf.grid.HirarchicalGridPanel({
        store:this.Store,
        sm: this.sm,
        border:false,
        layout:'fit',
        hirarchyColNumber:1,
        plugins:[this.summary],
        viewConfig:{
            forceFit:true,emptyText:WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:openPersonWin(\""+config.isCustomer+"\")'>"+(config.isCustomer?WtfGlobal.getLocaleText("acc.cus.rem1"):WtfGlobal.getLocaleText("acc.ven.rem1"))+"</a>")
        },
        forceFit:true,
        loadMask:true,
        columns:[this.sm,{
            header:WtfGlobal.getLocaleText("acc.customerList.gridName"),  //"Name",
            dataIndex:'accname',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridAddress"),  //"Address",
            dataIndex:'address',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridEmail"),  //"Email",
            dataIndex:'email',
            pdfwidth:110,
            renderer:WtfGlobal.renderDeletedEmailTo
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridContactNo"),  //"Contact No",
            dataIndex:'contactno',
            pdfwidth:75,
            renderer:WtfGlobal.renderDeletedContactToSkype
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalance"),  //"Opening Balance",
            dataIndex:'openbalance',
            align:'right',
            renderer:this.opBalRenderer,
//            summaryType:'sum',
//            summaryRenderer:this.opBalRenderer,
            pdfwidth:75,
            pdfrenderer:"rowcurrency"				// Opening Balance to be displayed in customer/vendor currency in CSV and pdf Export
        },{
            header: WtfGlobal.getLocaleText("acc.customerList.gridCreationDate"),  //"Creation Date",
            dataIndex: "creationDate",
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            pdfwidth:150
        },{
            header :WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalanceType"),  //'Opening Balance Type',
            dataIndex: 'openbalance',
            pdfwidth:75,
//            summaryType:'sum',
//            summaryRenderer:this.balTypeRenderer,
            renderer:this.balTypeRenderer
         },{
                header :WtfGlobal.getLocaleText("acc.customerList.gridCurrency"),  //'Currency',
            pdfwidth:75,
            dataIndex: 'currencyname',
            renderer:WtfGlobal.deletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridOtherInfo"),  //"Other Information",
            dataIndex:'other',
            renderer:WtfGlobal.deletedRenderer,
            hidden: !Wtf.account.companyAccountPref.withouttax1099,
            pdfwidth:50,
            pdfrenderer:"rowcurrency"
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridAmountDue"),  //"Amount Due",
            dataIndex:'amountdue',
            align: 'right',
            pdfwidth:75,
            pdfrenderer:"rowcurrency",
            renderer:this.opBalRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridShippingAddress"),  //"Shipping Address",
            dataIndex:'shippingaddress',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75,
            hidden: !config.isCustomer

        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridTaxIDno"),  //"Tax ID Number",
            dataIndex:'taxidnumber',
            renderer:WtfGlobal.deletedRenderer,
            hidden:config.isCustomer||Wtf.account.companyAccountPref.withouttax1099,
            pdfwidth:125
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.grid1099TaxEligible"),  //" 1099 Tax Eligible",
            dataIndex:'istaxeligible',
            hidden:config.isCustomer||Wtf.account.companyAccountPref.withouttax1099,
            pdfwidth:125
        },{
            header:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.gridCreditTerm"):WtfGlobal.getLocaleText("acc.customerList.gridDebitTerm")),  //"Credit":"Debit")+" Term",
            dataIndex:'termname',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:125//,
//            hidden: true
        }]
    });
    this.grid.on("render", function(grid) {
        WtfGlobal.autoApplyHeaderQtip(grid);
    },this);
    this.pageLimit = new Wtf.forumpPageSize({
        ftree:this.grid
    });
    this.pToolBar = new Wtf.PagingSearchToolbar({
        id: 'pgTbar' + this.id,
        pageSize: this.msgLmt,
        store: this.Store,
        searchField: this.quickSearchTF,
        displayInfo: true,
//        displayMsg: 'Displaying records {0} - {1} of {2}',
//        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No records to display",
        plugins: this.pageLimit
    });
    this.Store.on("load", this.setPageSize, this);
    this.Store.on('datachanged', function() {
        var p = this.pageLimit.combo.value;
        this.quickSearchTF.setPage(p);
    }, this);

       if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdata)) {
        this.btnArr.push(this.exportButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.exportCustomer"):WtfGlobal.getLocaleText("acc.vendorList.exportVendor")),  //"Export "+this.businessPerson+" details",  //.toLowerCase()+" details",
            disabled :true,
            id:(config.isCustomer?"exportCustomerLists6":"exportVendorLists7"),
            menuItem:{csv:true,pdf:true,rowPdf:false},
            get:(config.isCustomer?113:114),
            label:this.businessPerson.toLowerCase()
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.importdata)) {
        var extraConfig = {};
        extraConfig.url= config.isCustomer?"ACCCustomer/importCustomer.do":"ACCVendor/importVendor.do";
        var extraParams = "{\"DepreciationAccont\":\""+Wtf.account.companyAccountPref.depreciationaccount+"\"}";
        this.importBtnArray= Wtf.importMenuArray(this, this.businessPerson, this.Store, extraParams, extraConfig);
        this.importButton= Wtf.importMenuButtonA(this.importBtnArray, this, this.businessPerson);
        this.btnArr.push(this.importButton);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.print)) {
        this.btnArr.push(this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            id:(config.isCustomer?"printCustomer6":"printVendor7"),
            tooltip:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.printCustomer"):WtfGlobal.getLocaleText("acc.vendorList.printVendor")),  //"Print "+this.businessPerson+" details",   //.toLowerCase()+" details",
            disabled :true,
            params:{name:config.isCustomer?WtfGlobal.getLocaleText("acc.rem.14"):WtfGlobal.getLocaleText("acc.vendorList.tab")},
            menuItem:{print:true},
            get:(config.isCustomer?113:114),
            label:this.businessPerson.toLowerCase()
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.chart)) {
        this.btnArr.push(chart);
    }
    this.Store.on('load',this.hideMsg,this)
    this.sm.on('selectionchange', this.changeMsg, this);
    this.btnArr.push("->");
    this.btnArr.push(this.typeEditor);
    this.btnArr.push(getHelpButton(this,(config.isCustomer?6:7)));
    this.tbar=this.btnArr;
    this.bbar = this.pToolBar;
    this.typeEditor.on('select',this.loadTypeStore,this);
    Wtf.account.BusinessContactPanel.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.BusinessContactPanel,Wtf.Panel,{  
    loadTypeStore:function(a,rec){ 
        this.deleted=false;
        this.nondeleted=false;
        var index=rec.data.typeid;
        if(index==1){
            this.deleted=true;
        }
        else if(index==2)
            this.nondeleted=true;
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt,
                ss : this.quickSearchTF.getValue()
            }
        });
        WtfComMsgBox(29,4,true);
    },
    handleResetClick:function(){
        if(this.quickSearchTF.getValue()){
            this.quickSearchTF.reset();
            this.Store.load({
            params: {
                start: 0,
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
            }
        });
        }
    },
    setPageSize: function(store, rec, opt){
        var count = 0;
        for (var i = 0; i < store.getCount(); i++) {
            if (rec[i].data['level'] == 0 && (rec[i].data['parentid'] == "" || rec[i].data['parentid'] == undefined))
                count++;
        }
//        if(this.jReader.jsonData['totalCount']) {
//            this.getBottomToolbar().displayEl.update("Displaying records " + parseInt(opt.params["start"] + 1) + " - " + parseInt(count + opt.params["start"]) + " of " + this.jReader.jsonData['totalCount']);
//        }
        this.pageLimit.totalSize = this.jReader.jsonData['totalCount'];
    },
    storeloaded:function(store){
        Wtf.MessageBox.hide();
    },
   calllinkRowColor:function(id){
        var index=this.Store.find('id',id );
         var rec=this.Store.getAt(index);
         if(index>=0)
            WtfGlobal.highLightRowColor(this.grid,rec,true,0,0);
   },
    changeMsg:function(sm,index,record){
        Wtf.uncheckSelAllCheckbox(this.sm);

        WtfGlobal.enableDisableBtnArr(this.btnArr, this.grid, this.btnArrEDSingleS, this.btnArrEDMultiS);
        var arr=this.grid.getSelectionModel().getSelections();
        for(var i=0;i<arr.length;arr++){
            if(arr[i]&&arr[i].data.deleted) {
                this.deletePerson.disable();
                this.editPerson.disable();
            }
        }
    },

   hideMsg: function(store){
        Wtf.uncheckSelAllCheckbox(this.sm);
        this.quickSearchTF.StorageChanged(store);
        if(this.Store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
         Wtf.MessageBox.hide();
         if(this.personlinkid!=undefined)
         this.calllinkRowColor(this.personlinkid);
    },

    onRender: function(config){
        this.add(this.grid);
        if(this.openperson){
            callBusinessContactWindow(false, null, 'bcwin', this.isCustomer);
            Wtf.getCmp("bcwin").on('update',this.updateGrid,this);
        }
        Wtf.account.BusinessContactPanel.superclass.onRender.call(this,config);
    },

    showForm:function(isEdit){
       this.recArr =[] ;
       this.isEdit=isEdit;
        if(isEdit){
            this.recArr =this.grid.getSelectionModel().getSelections();
            if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                if(this.isCustomer)
                    WtfComMsgBox(11,2);
                else
                    WtfComMsgBox(19,2);
                return;
                }
                this.grid.getSelectionModel().clearSelections();
                WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,1);
        }
        var  rec=isEdit?this.recArr[0]:null;
        callBusinessContactWindow(isEdit, rec, 'bcwin', this.isCustomer);
        Wtf.getCmp("bcwin").on('update',this.updateGrid,this);
        Wtf.getCmp("bcwin").on('cancel',function(){ var num= (this.Store.indexOf(this.recArr[0]))%2;WtfGlobal.highLightRowColor(this.grid,this.recArr,false,num,1);},this);
    },

    updateGrid: function(obj,perAccID){
        this.perAccID=perAccID;
        this.Store.reload();
        this.isAdd=true;
        this.Store.on('load',this.colorRow,this)
    },

    colorRow: function(store){
        if(this.isAdd && (!this.isEdit)){
            this.recArr=[];
            if(store.find('accid',this.perAccID) != -1) {
                this.recArr.push(store.getAt(store.find('accid',this.perAccID)));
                WtfGlobal.highLightRowColor(this.grid,this.recArr[0],true,0,0);
            }
            this.isAdd=false;
        }
    },

    deletePersonFunc: function(){
       var arr=[];
       var data=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
       this.grid.getSelectionModel().clearSelections();
      WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
       Wtf.MessageBox.show({
       title: WtfGlobal.getLocaleText("acc.common.warning"),  //"Warning",
//       msg: "Are you sure you want to delete the selected "+this.businessPerson+" and all associated sub "+this.businessPerson+"(s)?<div><b>Note: This data cannot be retrieved later</b></div>",
       msg: this.businessPerson=="Customer"?WtfGlobal.getLocaleText("acc.customerList.delTT"):WtfGlobal.getLocaleText("acc.vendorList.delTT")+"<div><b>"+WtfGlobal.getLocaleText("acc.customerList.delTT1")+"</b></div>",
       width: 560,
       buttons: Wtf.MessageBox.OKCANCEL,
       animEl: 'upbtn',
       icon: Wtf.MessageBox.QUESTION,
       scope:this,
       fn:function(btn){
            if(btn!="ok"){
                for(var i=0;i<this.recArr.length;i++){
                    var ind=this.Store.indexOf(this.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                }
                return;
            }
            for(i=0;i<this.recArr.length;i++){
                arr.push(this.Store.indexOf(this.recArr[i]));
                data += "{accid:"+this.recArr[i].data.accid+",openbalance:"+this.recArr[i].data.openbalance+"}";
                if(this.Store.find('parentid',this.recArr[i].data.accid) != -1){
                	data += ",";
                	var child = this.Store.getAt(this.Store.find('parentid',this.recArr[i].data.accid));
                	data += "{accid:"+child.data.accid+",openbalance:"+child.data.openbalance+"}";
                	
                	while(this.Store.find('parentid',child.data.accid) != -1){
                		data += ",";
                		child = this.Store.getAt(this.Store.find('parentid',child.data.accid));
                    	data += "{accid:"+child.data.accid+",openbalance:"+child.data.openbalance+"}";
                	}
                }
                if(i<this.recArr.length-1)
                	data += ",";
            }
            data = "["+data+"]";
//        data= WtfGlobal.getJSONArray(this.grid,true,arr);     // [Issue 24363] New: [Delete Customer] Unable to Delete 300 Customers in one go.
        this.deleteUrl = "";
        if(this.businessPerson=="Customer") {
            this.deleteUrl = "ACCCustomerCMN/deleteCustomer.do";
        } else if(this.businessPerson=="Vendor") {
            this.deleteUrl = "ACCVendorCMN/deleteVendor.do";
        }
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                url:this.deleteUrl,
                params:{
                   data:data,
                    mode:3
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
       }});
    },

    genSuccessResponse:function(response){
       WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.Store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        if(response.success){
            (function(){
            WtfGlobal.loadpersonacc(this.isCustomer);//this.Store.reload();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }

    },

    genFailureResponse:function(response){
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.Store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    getChart:function(){
          var chartid=this.isCustomer?"customerprioritychartid":"vendorprioritychartid";
          var swf="../../scripts/graph/krwcolumn/krwcolumn/krwcolumn.swf";
          var id1=this.isCustomer?"customerpriorityid":"vendorpriorityid"
//          var dataflag=this.isCustomer?3:4;
          var dataflag=this.isCustomer?"ACCChart/getTopCustomerChart":"ACCChart/getTopVendorsChart";
          var mainid=this.isCustomer?"mainCustomerDetails":"mainVendorDetails";
          var xmlpath= this.isCustomer?'../../scripts/graph/krwcolumn/examples/CustomerPriority/customer_settings.xml':'../../scripts/graph/krwcolumn/examples/VendorPriority/vendor_settings.xml';
          globalChart(chartid,id1,swf,dataflag,mainid,xmlpath,true,this.withinventory);
    },

    opBalRenderer:function(val,m,rec){
//        return WtfGlobal.currencyDeletedRenderer(Math.abs(val),m,rec);
    	return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
    },

    balTypeRenderer:function(val,m,rec){
        val=val==0?"N/A":val>0?"Debit":"Credit";
        return WtfGlobal.deletedRenderer(val,m,rec)
    }
//,
//    importCSVRecords : function(obj, moduleName, store, extraParams, extraConfig){
//        this.impWin1 = Wtf.commonFileImportWindow(obj, moduleName, store, extraParams, extraConfig);
//        this.impWin1.show();
//    },
//    importXLSRecords :function(obj,moduleName,store,extraParams, extraConfig){
//        this.impWin1 = Wtf.xlsCommonFileImportWindow(obj,moduleName,store,extraParams, extraConfig);
//        this.impWin1.show();
//    },
//    mappingCSVInterface:function(Header, res, impWin1, delimiterType, extraParams, extraConfig) {
//        this.filename=res.FileName;
//
//        this.mapCSV=new Wtf.csvFileMappingInterface({
//            csvheaders:Header,
//            modName:this.businessPerson,
//            impWin1:impWin1,
//            delimiterType:delimiterType,
//            cm:this.gridcm,
//            extraParams: extraParams,
//            extraConfig: extraConfig
//        }).show();
//        Wtf.getCmp("csvMappingInterface").on('importfn',this.importCSVfunction, this);
//    },
//    importCSVfunction:function(response, delimiterType, extraParams, extraConfig) {
//        Wtf.importMappedRecords(this, response, this.businessPerson, this.filename, this.Store, delimiterType, extraParams, extraConfig);
//    }
});


//===== By Category Report ========
Wtf.account.BusinessPersonListByCategory=function(config){
     Wtf.apply(this, config);
     this.businessPerson=(config.isCustomer?'Customer':'Vendor');
     this.isCategoryUpdated = false;
     this.GridRec = Wtf.data.Record.create ([
        {name: 'accid'},
        {name: 'openbalance'},
        {name: 'id'},
        {name: 'title'},
        {name: 'accname'},
        {name: 'address'},
        {name: 'company'},
        {name: 'email'},
        {name: 'contactno'},
        {name: 'contactno2'},
        {name: 'fax'},
        {name: 'shippingaddress'},
        {name: 'pdm'},
        {name: 'pdmname'},
        {name: 'parentid'},
        {name: 'parentname'},
        {name: 'bankaccountno'},
        {name: 'termid'},
        {name: 'termname'},
        {name: 'other'},
        {name: 'currencysymbol'},
        {name: 'currencyname'},
        {name: 'currencyid'},
        {name: 'deleted'},
        {name: 'creationDate' ,type:'date'},
        {name: 'taxno'},
        {name: 'categoryid'},
        {name: 'category'}
    ]);
    this.ByCategoryStore = new Wtf.data.GroupingStore({
        url:"ACC"+this.businessPerson+"/get"+this.businessPerson+"sByCategory.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        },this.GridRec),
        groupField:"category",
        sortInfo: {field: 'accname',direction: "ASC"}
    });
    this.ByCategoryStore.on('beforeload',function(){
        WtfComMsgBox(29,4,true);
    },this);
    this.ByCategoryStore.on('load',this.storeloaded, this);
    this.ByCategoryStore.on('loadException',this.storeloaded, this);
        
    this.sm = new Wtf.grid.RowSelectionModel();
    this.grid = new Wtf.grid.GridPanel({
        store:this.ByCategoryStore,
        border:false,
        layout:'fit',
        view: new Wtf.grid.GroupingView({
            forceFit:true
        }),
        loadMask:true,
        columns:[
            {
                header:WtfGlobal.getLocaleText("acc.cust.category"),  //"Category",
                dataIndex:'category',
                hidden: true,
                fixed: true,
                renderer:WtfGlobal.deletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridName"),  //"Name",
                dataIndex:'accname',
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridAddress"),  //"Address",
                dataIndex:'address',
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridEmail"),  //"Email",
                dataIndex:'email',
                pdfwidth:110,
                renderer:WtfGlobal.renderDeletedEmailTo
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridContactNo"),  //"Contact No",
                dataIndex:'contactno',
                pdfwidth:75,
                renderer:WtfGlobal.renderDeletedContactToSkype
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalance"),  //"Opening Balance",
                dataIndex:'openbalance',
                align:'right',
                renderer:this.opBalRenderer,
                summaryType:'sum',
                summaryRenderer:this.opBalRenderer,
                pdfwidth:75,
                pdfrenderer:"currency"
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridCreationDate"),  // "Creation Date",
                dataIndex: "creationDate",
                renderer:WtfGlobal.onlyDateDeletedRenderer,
                pdfwidth:150
            },{
                header :WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalanceType"),  //'Opening Balance Type',
                dataIndex: 'openbalance',
                pdfwidth:75,
                summaryType:'sum',
                summaryRenderer:this.balTypeRenderer,
                renderer:this.balTypeRenderer
             },{
                header :WtfGlobal.getLocaleText("acc.customerList.gridCurrency"),  //'Currency',
                pdfwidth:75,
                dataIndex: 'currencyname',
                renderer:WtfGlobal.deletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridOtherInfo"),  //"Other Information",
                dataIndex:'other',
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:50
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridShippingAddress"),  //"Shipping Address",
                dataIndex:'shippingaddress',
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:75,
                hidden: !config.isCustomer
            },{
                header:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.gridCreditTerm"):WtfGlobal.getLocaleText("acc.customerList.gridDebitTerm")),  //"Credit":"Debit")+" Term",
                dataIndex:'termname',
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:125
            }],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.ByCategoryStore,
//                searchField: this.quickPanelSearch,
                displayInfo: true,
//                displayMsg: 'Displaying records {0} - {1} of {2}',
//                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                })
            })
    });
    Wtf.account.BusinessPersonListByCategory.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.BusinessPersonListByCategory,Wtf.Panel,{
        onRender: function(config){
        this.ByCategoryStore.load({
            params:{
                start:0,
                limit:30
            }
        });
           
        this.add(this.grid);
        this.on("activate",function(){
            if(this.isCategoryUpdated){
                this.ByCategoryStore.reload();
                this.isCategoryUpdated=false;
            }
        },this);
        Wtf.account.BusinessPersonListByCategory.superclass.onRender.call(this,config);
    },
    storeloaded:function(store){
        Wtf.MessageBox.hide();
    },
    opBalRenderer:function(val,m,rec){
//        return WtfGlobal.currencyDeletedRenderer(Math.abs(val),m,rec);
        return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
    },

    balTypeRenderer:function(val,m,rec){
        val=val==0?"N/A":val>0?"Debit":"Credit";
        return WtfGlobal.deletedRenderer(val,m,rec)
    }
});
