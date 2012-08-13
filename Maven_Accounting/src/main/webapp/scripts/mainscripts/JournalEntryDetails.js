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
Wtf.account.JournalEntryDetailsPanel=function(config){
    this.exponly=false;
    this.id = config.id;
    this.costCenterId = "";
    this.extraFilters = config.extraFilters;
    if(config.extraFilters != undefined){//Cost Center Report View
        this.costCenterId = config.extraFilters.costcenter?config.extraFilters.costcenter:"";
        this.groupid = config.extraFilters.groupid?config.extraFilters.groupid:null;
        this.groupid = config.extraFilters.groupid==true?true:false;
    }
    this.entryID=config.entryID;
    this.appendID = true;
    this.nondeleted=false;
    this.deleted=false;
    this.uPermType=Wtf.UPerm.coa;
    this.permType=Wtf.Perm.coa;
    this.typeEditor = new Wtf.form.ComboBox({
        store: Wtf.delTypeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.helpmodeid+this.id,
        valueField:'typeid',
        mode: 'local',
        value:0,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
   this.expandRec = Wtf.data.Record.create ([
        {name:'accountid'},
        {name:'debit'},
        {name:'accountname'},
        {name:'description'},
        {name:'d_amount'},
        {name:'c_amount'}
        
    ]);
    this.expandStore = new Wtf.data.Store({
        url: "ACCJournal/getJournalEntryDetails.do",
//        url: Wtf.req.account+'CompanyManager.jsp',
        baseParams:{
            mode:55
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.GridRec = Wtf.data.Record.create ([
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'deleted'},
        {name:'entrydate',type:'date'},
        {name:'memo'}
    ]);
    this.Store = new Wtf.data.Store({
        url: "ACCJournal/getJournalEntry.do",
//        url: Wtf.req.account+'CompanyManager.jsp',
        baseParams:{
            mode:54,
            costCenterId: this.costCenterId,
            groupid:this.groupid
        },
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.GridRec)
    });

    if(this.extraFilters != undefined){//Cost Center Report View
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = this.extraFilters.startdate;
        currentBaseParams.enddate = this.extraFilters.enddate;
        this.Store.baseParams=currentBaseParams;
    }
    this.expander = new Wtf.grid.RowExpander({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();

    this.grid = new Wtf.grid.GridPanel({
        id:"gridmsg"+config.helpmodeid+this.id,
        stripeRows :true,
        store:this.Store,
        sm:this.sm,
        border:false,
        viewConfig:{forceFit:true} ,//emptyText:WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:openJEntry()'>Get Started by adding a Journal Entry now...</a>")} ,
        forceFit:true,
        plugins: this.expander,
        loadMask : true,
        columns:[this.expander,this.sm,this.rowNo,{
            header:WtfGlobal.getLocaleText("acc.jeList.gridEntryDate"),  //"Entry Date",
            dataIndex:'entrydate',
            width:20,
            align:'center',
            pdfwidth:150,
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.jeList.gridEntryNo"),  //"Entry Number",
            dataIndex:'entryno',
            width:20,
            pdfwidth:150,
            sortable:true,
            renderer:WtfGlobal.deletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.jeList.gridMemo"),  //"Memo", 
            dataIndex:'memo',
            pdfwidth:150,
            sortable:true,
            renderer:WtfGlobal.deletedRenderer
        }]
    });
    var btnArr=[];
    btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.dnList.searchText") + ' '+WtfGlobal.getLocaleText("acc.je.msg2"),  //'Quick Search by JE Number',
        id:"quickSearch"+config.helpmodeid+this.id,
        width: 200
     }))
     btnArr.push(this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset Search Results',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    }));
    this.resetBttn.on('click',this.handleResetClick,this);
    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'Delete' Button
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.removeje)){
            btnArr.push('-',this.deleteBtn=new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.jeList.deleteJE"),  //'Delete Journal Entry',
                scope: this,
                disabled :true,
                tooltip:WtfGlobal.getLocaleText("acc.jeList.deleteJE"),  //{text:"Select a Journal Entry to delete.",dtext:"Select a Journal Entry to delete.", etext:"Delete selected Journal Entry details."},
                iconCls:getButtonIconCls(Wtf.etype.deletebutton),
                handler:this.performDelete.createDelegate(this)
            }));
        }
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdataje)){
        btnArr.push('-',this.exportButton=new Wtf.exportButton({
            obj:this,
            id:"exportReports"+config.helpmodeid+this.id,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            disabled :true,
            menuItem:{csv:true,pdf:true,rowPdf:false},
            get:Wtf.autoNum.JournalEntry
        }))
     }
     if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.printje)){
         btnArr.push('-',this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            menuItem:{print:true},
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            params:{name:WtfGlobal.getLocaleText("acc.jeList.tabTitle")},
            label:"Journal Entry",
            get:Wtf.autoNum.JournalEntry
        }));
    }

    chkCostCenterload();
    if(Wtf.CostCenterStore.getCount()==0) Wtf.CostCenterStore.on("load", this.setCostCenter, this);
    this.costCenter = new Wtf.form.ComboBox({
        store: Wtf.CostCenterStore,
        name:'costCenterId',
        width:100,
        listWidth:100,
        displayField:'name',
        valueField:'id',
        triggerAction: 'all',
        mode: 'local',
        typeAhead:true,
        value: this.costCenterId,
        selectOnFocus:true,
        forceSelection: true
    });

    this.costCenter.on("select", function(cmb, rec, ind){
        this.costCenterId = rec.data.id;

        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.costCenterId = this.costCenterId;
        this.Store.baseParams=currentBaseParams;

        this.loadJEStore();
    },this);

    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'cost center' filter
        btnArr.push("-",WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter);
    }
    
    btnArr.push("->",this.typeEditor,'-',getHelpButton(this,config.helpmodeid));
    Wtf.apply(this,{
         items:[{
                region:'center',
                layout:'fit',
                border:false,
                items:this.grid
            }],tbar:btnArr, 

        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
            searchField: this.quickPanelSearch,
            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
            plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id})
        })
    },config);
    this.Store.on('beforeload',function(s,o){
        if(!o.params)o.params={};
        o.params.deleted=this.deleted;
        o.params.nondeleted=this.nondeleted;
    },this);
    this.Store.on("load", function(store){
        if(store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
            if(this.deleteBtn)this.deleteBtn.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
            if(this.deleteBtn)this.deleteBtn.enable();
        }
    },this);
    this.typeEditor.on('select',this.loadTypeStore,this);
    this.expander.on("expand",this.onRowexpand,this);
//    this.grid.on('render',this.loadParmStore,this);
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);

    this.Store.on('load',this.expandRow, this);
    this.Store.on('datachanged', this.dataChanged, this);
    this.Store.on('load',this.storeloaded,this);

    Wtf.account.JournalEntryDetailsPanel.superclass.constructor.call(this,config);     
}
 Wtf.extend(Wtf.account.JournalEntryDetailsPanel,Wtf.Panel,{
    loadTypeStore:function(a,rec){
        this.deleted=false;
        this.nondeleted=false;
        var index=rec.data.typeid;
        if(this.deleteBtn){this.deleteBtn.enable();}
        if(index==2)
            this.nondeleted=true;
        if(index==1){
             this.deleted=true;
            if(this.deleteBtn){this.deleteBtn.disable();}
        }
        else{
             if(this.deleteBtn){this.deleteBtn.enable();}
        }
        this.Store.load({params:{
            start:0,
            limit:this.pP.combo.value,
            ss : this.quickPanelSearch.getValue()
        }});
       WtfComMsgBox(29,4,true);
        this.Store.on('load',this.storeloaded,this);
    },
    setCostCenter: function(){
        this.costCenter.setValue(this.costCenterId);//Select Default Cost Center
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
     enableDisableButtons:function(){
        if(this.deleteBtn){this.deleteBtn.enable();}
        var arr=this.grid.getSelectionModel().getSelections();
        for(var i=0;i<arr.length;arr++){
            if(arr[i]&&arr[i].data.deleted)
                if(this.deleteBtn){this.deleteBtn.disable();}
        }

    },
//    loadParmStore:function(){ //code merged in expandJournalEntry()
//        if(this.entryID==null)
//            this.Store.load({params:{start:0,limit:this.pP.combo.value}});
//         WtfComMsgBox(29,4,true);
//        this.Store.on('load',this.expandRow, this);
//        this.Store.on('datachanged', this.dataChanged, this);
//        this.Store.on('load',this.storeloaded,this);
//    },
    handleResetClick:function(){
       if(this.quickPanelSearch.getValue()){
           this.quickPanelSearch.reset();
           this.Store.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
           });
        }
    },
    dataChanged:function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    },    

    dateRenderer:function(v){
        if(v) return v.format('Y M d');
        return "";
    },

    onRowexpand:function(scope, record, body, rowIndex){
        this.expanderBody=body;
        this.expandStore.load({params:{journalentryid:record.data['journalentryid']}});
    },

    fillExpanderBody:function(){
        var disHtml = "";
        var arr=[];
        arr=[WtfGlobal.getLocaleText("acc.jeList.expandJE.accName"),'&nbsp;','<div align=left>'+WtfGlobal.getLocaleText("acc.jeList.expandJE.amtDebit")+'</div>','<div align=left>'+WtfGlobal.getLocaleText("acc.jeList.expandJE.amtCredit")+'</div>','<div align=left>'+WtfGlobal.getLocaleText("acc.jeList.expandJE.desc")+'</div>'];
        var header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.jeList.expandJE.transList")+"</span>";
        for(var i=0;i<arr.length;i++){
            header += "<span class='headerRow'  "+(i==0?"style='width:200px'":"style='width:100px'")+">" + arr[i] + "</span>";
        }
        header += "<span class='gridLine'></span>";
        for(i=0;i<this.expandStore.getCount();i++){
            var rec=this.expandStore.getAt(i);
            var accname="";
            if(rec.data["debit"]=='Debit'){
                accname= "<span class='gridRow' style='width:200px'  wtf:qtip='"+rec.data['accountname']+"'>"+Wtf.util.Format.ellipsis(rec.data['accountname'],20)+"&nbsp;</span>";
                accname += "<span class='gridRow' style='width:100px'>Dr</span>";
            }else{
                accname="<span class='gridRow' style='padding-left:50px; width:150px'  wtf:qtip='"+rec.data['accountname']+"'>To "+Wtf.util.Format.ellipsis(rec.data['accountname'],20)+"&nbsp;</span>";
                accname += "<span class='gridRow'  style='width:100px'>&nbsp;</span>";
            }
            header += accname;
            var desc = '';
            if(rec.data['description']!=''){
                desc = decodeURI(rec.data['description']);
            }
            header += "<span class='gridRow'  style='width:100px'>"+WtfGlobal.currencyRenderer(rec.data['d_amount'])+"&nbsp;</span>";
            header += "<span class='gridRow'  style='width:100px'>"+WtfGlobal.currencyRenderer(rec.data['c_amount'])+"&nbsp;</span>";
            header += "<span class='gridRow' style='width:200px;'  wtf:qtip='"+desc+"'>"+Wtf.util.Format.ellipsis(desc,30)+"</span>";
            header +="<br>";
        }
        disHtml += "<div class='expanderContainer'>" + header + "</div>";
        this.expanderBody.innerHTML = disHtml;
    },
    expandJournalEntry:function(id,exponly){
        this.entryID=id;
        this.exponly=exponly;

        if(this.entryID==undefined){//Journal Entry Report View
            if(this.exportButton)this.exportButton.setParams({linkid:''});
            if(this.printButton)this.printButton.setParams({linkid:''});
            this.quickPanelSearch.show();
            this.resetBttn.show();
            this.typeEditor.show();
            this.pagingToolbar.show();
            this.Store.load({params:{start:0,limit:this.pagingToolbar.pageSize}});
        } else {//Link to Journal Entry, Single JE View
            if(this.exportButton)this.exportButton.setParams({linkid:this.entryID});
            if(this.printButton)this.printButton.setParams({linkid:this.entryID});
            this.quickPanelSearch.hide();
            this.resetBttn.hide();
            this.typeEditor.hide();
            this.pagingToolbar.hide();
            this.Store.load({params:{linkid:this.entryID}});
        }
        WtfComMsgBox(29,4,true);
    },
    expandRow:function(id){
        Wtf.MessageBox.hide();
        this.Store.filter('journalentryid',this.entryID);
        if(this.exponly)
            this.expander.toggleRow(0);
    },

    performDelete:function(){
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rem.13"),function(btn){
            if(btn!="yes") {
                for(var i=0;i<this.recArr.length;i++){
                    var ind=this.Store.indexOf(this.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                }
                return;
            }
            for(i=0;i<this.recArr.length;i++){
                arr.push(this.Store.indexOf(this.recArr[i]));
            }
            data= WtfGlobal.getJSONArray(this.grid,true,arr);
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url: "ACCJournalCMN/deleteJournalEntries.do",
                params:{
                    data:data,
                    mode:205
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },

    loadJEStore:function(){
        this.Store.load({
           params : {
               start : 0,
               limit : this.pP.combo.value,
               ss : this.quickPanelSearch.getValue()
           }
       });
       this.Store.on('load',this.storeloaded,this);
    },

    storeloaded:function(store){
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
    },

    genSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.je.tabTitle"),response.msg],response.success*2+1);
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        if(response.success){
            (function(){
                this.loadJEStore();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }
    },

    genFailureResponse:function(response){
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
});
