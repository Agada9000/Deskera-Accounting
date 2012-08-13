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
Wtf.account.GroupReport = function(config){
    this.createStore();
    this.groupID="";
    this.isAdd=false;
    this.recArr=[];
    this.isEdit=false;
    this.createColumnModel();
    this.createGrid();
    this.store.on('load',this.hideMsg,this)
    Wtf.account.GroupReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.GroupReport, Wtf.Panel,{
    onRender: function(config){
        Wtf.account.GroupReport.superclass.onRender.call(this, config);
        this.add(this.grid);
  },
    hideMsg:function(){
        Wtf.MessageBox.hide();
    },
    createStore:function(){
        this.coaRec = new Wtf.data.Record.create([
            {name: 'groupid'},
            {name: 'groupname'},
            {name: 'nature',type:'int'},
            {name: 'affectgp', type:'boolean'},
            {name: 'displayorder'},
            {name: 'level',type:'int'},
            {name: 'leaf',type:'boolean'},
            {name: 'parentid'},
            {name: 'parentname'},
            {name: 'companyid'}
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.coaRec),
            url:"ACCAccount/getGroups.do",
//            url: Wtf.req.account +'CompanyManager.jsp',
            baseParams:{
                mode:1
            }
        });
        this.store.load();
        WtfComMsgBox(29,4,true);
    },

    createColumnModel:function(){
//        this.selectionModel = new Wtf.grid.CheckboxSelectionModel({singleSelect:true});
        this.selectionModel = new Wtf.grid.RadioSelectionModel();
        this.gridcm= new Wtf.grid.ColumnModel([this.selectionModel,{
            header: WtfGlobal.getLocaleText("acc.coa.gridAccountType"),  //"Account Type",
            dataIndex: 'groupname',
            renderer:this.formatPredefinedGroup,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.coa.gridNature"),  //"Nature",
            dataIndex: 'nature',
            renderer:this.natureRenderer,
            pdfwidth:200
        },{
            header :WtfGlobal.getLocaleText("acc.coa.gridAffectsGrossProfit"),  //'Affects Gross Profit',
            dataIndex: 'affectgp',
            pdfwidth:200,
            renderer:WtfGlobal.boolRenderer("Yes", "No")
        }]);
    },

    formatPredefinedGroup:function(val, m, r){
        if(!r.data['companyid'])
            return '<b>'+val+'</b>';
        return val;
    },

    createGrid:function(){
        this.localSearch = new Wtf.KWLLocalSearch({
            emptyText:WtfGlobal.getLocaleText("acc.coa.groupTypeSearchText"),  //'Search by Account Type',
            width: 150,
            searchField: "groupname"
        });
        this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            hidden:this.isSummary,
            tooltip :WtfGlobal.getLocaleText("acc.coa.resetTT"),  //'Allows you to add a new search group by clearing existing search groups.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        });
        var btnArr=[];
        btnArr.push(this.localSearch);
        btnArr.push(this.resetBttn);
        this.resetBttn.on('click',this.handleResetClick,this);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create))
        btnArr.push(new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.coa.addNewAccountType"),  //'Add New Account type',
                id:'maintainAccountType5', //FixMe: remove hardcoded helpmodeid
                tooltip:WtfGlobal.getLocaleText("acc.coa.addNewAccountType"),  //"Add new account type.",
                iconCls:getButtonIconCls(Wtf.etype.add),
                handler:this.editGroup.createDelegate(this,[false])
            })
        );
            this.editType=new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.coa.editAccountType"),  //'Edit Account type',
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.coa.editAccountType"),  //{text:"Select an account type to edit.",dtext:"Select an account type to edit.", etext:"Edit selected account type."},
                iconCls:getButtonIconCls(Wtf.etype.edit),
                disabled:true,
                handler:this.editGroup.createDelegate(this,[true])
            })
        this.deleteType=new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.coa.deleteAccountType"),  //'Delete Account type',
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.coa.deleteAccountType"),  //{text:"Select an account type to delete.",dtext:"Select an account type to delete.", etext:"Delete selected account type."},
                disabled:true,
                iconCls:getButtonIconCls(Wtf.etype.deletebutton),
                handler:this.deleteGroup.createDelegate(this)
            })
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.edit))
            btnArr.push(this.editType);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.remove))
            btnArr.push(this.deleteType);
       btnArr.push("->");
       btnArr.push(getHelpButton(this,5));
       this.grid = new Wtf.grid.HirarchicalGridPanel({
            layout:'fit',
            store: this.store,
            cm: this.gridcm,
            sm : this.selectionModel,
            hirarchyColNumber:1,
            autoScroll:true,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });        
        this.grid.on("render", function(grid) {
            WtfGlobal.autoApplyHeaderQtip(grid);
            this.localSearch.applyGrid(grid);
        },this);
        
//        btnArr.push(new Wtf.exportButton({
//             obj:this,
//             menuItem:{csv:true,pdf:true,rowPdf:false},
//             get:112
//        }));
        this.tbar=btnArr;
        this.selectionModel.on("selectionchange",this.enableDisableButtons.createDelegate(this,[btnArr]),this);
    },

    enableDisableButtons:function(btnArr){
        var rec=this.grid.getSelectionModel().getSelected();
        if(rec&&!rec.data['companyid']){
            WtfGlobal.enableDisableBtnArr(btnArr, this.grid, [], []);            
        }else{
            WtfGlobal.enableDisableBtnArr(btnArr, this.grid, [3,4], []);
        }
    },
        handleResetClick:function(){
        if(this.localSearch.getValue()){
            this.localSearch.reset();
            this.store.load();
        }
    },
    editGroup:function(isEdit){
        this.recArr =[] ;
        this.isEdit=isEdit;
        if(isEdit){
            this.recArr = this.grid.getSelectionModel().getSelections();
            this.grid.getSelectionModel().clearSelections();
            WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,1);
        }
        var  rec=isEdit?this.recArr[0]:null;
        if(rec&&!rec.data['companyid']){
            WtfComMsgBox(31, 2);
            var num= (this.store.indexOf(this.recArr[0]))%2;WtfGlobal.highLightRowColor(this.grid,this.recArr,false,num,true);
            return;
        }
        callGroupWindow(isEdit,rec,"groupWindow");
        Wtf.getCmp("groupWindow").on('update',this.updateGrid,this);
        Wtf.getCmp("groupWindow").on('cancel',function(){ 
            var num= (this.store.indexOf(this.recArr[0]))%2;WtfGlobal.highLightRowColor(this.grid,this.recArr,false,num,true);
        },this);
    },
    updateGrid: function(obj,groupID){
        this.groupID=groupID;
        this.store.reload();
        this.isAdd=true;
        this.store.on('load',this.colorRow,this)
    },
    colorRow: function(store){
       if(this.store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
       }
       if(this.isAdd && (!this.isEdit)){
          this.recArr=[];
          this.recArr.push(store.getAt(store.find('groupid',this.groupID)));
          WtfGlobal.highLightRowColor(this.grid,this.recArr[0],true,0,0);
          this.isAdd=false;
       }
    },
   deleteGroup:function(){
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.show({
        title: WtfGlobal.getLocaleText("acc.common.warning"),  //"Warning",
        msg: WtfGlobal.getLocaleText("acc.rem.12"),  //"Are you sure you want to delete the selected group and all associated sub group(s)?<div><b>Note: This data cannot be retrieved later.</b></div>",
        width: 560,
        buttons: Wtf.MessageBox.OKCANCEL,
        animEl: 'upbtn',
        icon: Wtf.MessageBox.QUESTION,
        scope:this,
        fn:function(btn){
            if(btn!="ok"){
               var num= (this.store.indexOf(this.recArr[0]))%2;
               WtfGlobal.highLightRowColor(this.grid,this.recArr,false,num,2);
                 return;
            }
            Wtf.Ajax.requestEx({
                url:"ACCAccount/deleteGroup.do",
//                url: Wtf.req.account+'CompanyManager.jsp',
                params:{
                    groupid:this.recArr[0].data['groupid'],
                    mode:9
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
            }
        });
    }, 

    genSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.coa.tabTitle"),response.msg],response.success*2+1);
        if(response.success){
            //WtfGlobal.highLightRowColor(this.grid,this.recArr,false,0);
            WtfGlobal.highLightRowColor(this.grid,this.recArr[0],true,0,3);
            (function(){
            this.store.reload();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    natureRenderer:function(val){
        switch(val){
            case Wtf.account.nature.Asset:return "Asset";
            case Wtf.account.nature.Liability:return "Liability";
            case Wtf.account.nature.Expences:return "Expenses";
            case Wtf.account.nature.Income:return "Income";
        }
    }
});
