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
Wtf.account.MasterConfigurator = function (config){
    this.uPermType=Wtf.UPerm.masterconfig;
    this.permType=Wtf.Perm.masterconfig;
    Wtf.account.MasterConfigurator.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.MasterConfigurator,Wtf.Panel,{
    initComponent:function (){
        Wtf.account.MasterConfigurator.superclass.initComponent.call(this);
        this.getMasterGrid();
        this.getMasterItemGrid();
        this.masterStore.on('load',function(){this.MasterItemStore.removeAll();},this);
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
                this.MasterItemGrid,{
                    border:false,
                    region:'west',
                    layout:'border',
                    split:true,
                    width:300,
                    tbar:[this.masterSearch],
                    items:[
                        this.masterGrid,
                        this.masterLinks
                    ]
                }
            ]
        });
        this.masterSm.on("selectionchange",function(){
            if(this.masterSm.getSelected()){
                this.MasterItemAdd.enable();
                this.masterEdit.enable();
                this.MasterItemStore.load({
                    params:{
                        groupid:this.masterGrid.getSelectionModel().getSelected().data["id"]
                    }
                });
                 WtfComMsgBox(29,4,true);
             } else {
                this.MasterItemAdd.disable();
                this.masterEdit.disable();
            }
            this.changeMsg();
        },this);

        this.add(this.mainPanel);
    },
    getMasterGrid:function (){
        this.masterRec = new Wtf.data.Record.create([
            {name:"id"},
            {name:"name"}
        ]);

        this.masterReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.masterRec);

        this.masterStore = new Wtf.data.Store({
//            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCMaster/getMasterGroups.do",
            reader:this.masterReader,
            baseParams:{
                mode:111
            }
        });

        this.masterStore.load();
        this.masterStore.on('load', this.handleLoad1, this);

        this.masterColumn = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.masterConfig.group"),  //"Master Group",
                sortable:true,
                dataIndex:"name"
            },{
                header:'id',
                hidden: true,
                dataIndex: 'id'
            }
        ]);

        var masterbtn=new Array();
        //Temp fix to show help message. This needs to be changed.
//        this.hiddenBttn = new Wtf.Toolbar.Button({
//            id:"mastergroup"+this.helpmodeid,
//            disabled:true,
//            scope:this
//        });

        this.masterAdd = new Wtf.Toolbar.Button({
            text:"Add Master Group",
            handler:function (){
                this.AddMaster(false);
            },
            iconCls :getButtonIconCls(Wtf.etype.add),
            scope:this
        });

        this.masterEdit = new Wtf.Toolbar.Button({
            text:"Edit Master Group",
            handler:function (){
                this.AddMaster(true);
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.edit),
            disabled:true
        });
        //masterbtn.push(this.masterEdit);
        this.masterSearch = new Wtf.MyQuickSearch({
            field: 'name',
            id:"quickSearch"+this.helpmodeid,
            emptyText:WtfGlobal.getLocaleText("acc.masterConfig.search"),  //'Search by Master Group...',
            width: 150
         });
        this.masterGrid = new Wtf.grid.GridPanel({
            id:"mastergroup"+this.helpmodeid,
            stripeRows :true,
            sm:this.masterSm = new Wtf.grid.RowSelectionModel({singleSelect:true}),
            region:"north",
            height : 200,
            store:this.masterStore,
            sortable:true,
            border: false,
            autoScroll:true,
            split: true,
            cm:this.masterColumn,
            loadMask:true,
            viewConfig:{
                forceFit:true
            }
        });

        var linkData = {links:[
                        {fn:"PaymentMethod()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.msg13")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.payMethod")+"</span>",viewperm:!WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.view)},
                        //{fn:"callTax()",text:"Tax",viewperm:true},
                        {fn:"callCreditTerm()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.msg9")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.payTerm")+"</span>",viewperm:!WtfGlobal.EnableDisable(Wtf.UPerm.creditterm, Wtf.Perm.creditterm.view)},
                        {fn:"callUOM()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.msg10")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.uom")+"</span>",viewperm:!WtfGlobal.EnableDisable(Wtf.UPerm.uom, Wtf.Perm.uom.view)},
                       {fn:"callTax()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.msg11")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.taxes")+"</span>",viewperm:!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.view)},
                       {fn:"callCostCenter()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.msg12")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.costCenter")+"</span>",viewperm:true}
                ]};
        var tpl = new Wtf.XTemplate(
           '<div class ="dashboardcontent linkspanel" style="float:left;width:100%">',
             '<ul id="accMasterSettingPane">',
             '<tpl for="links">',
                '<tpl if="viewperm">',
                    '<li id="wtf-gen215">',
                        '<a onclick="{fn}" href="#" >{text}</a>',
                     '</li>',
                '</tpl>',
             '</tpl>',
             '</ul>',
           '</div>'
       );
        this.masterLinks = new Wtf.Panel({
            region:"center",
            id:"paymnetConfigure"+this.helpmodeid,
            bodyStyle:'background:white;',
            layout:'fit',
//            height:500,
            border: false,
            split: true,
            loadMask:true
        });
        this.masterLinks.on('render', function(){
            tpl.overwrite(this.masterLinks.body, linkData);
        }, this);

    },
    getMasterItemGrid:function (){
        this.MasterItemRec = new Wtf.data.Record.create([
            {name:"id"},
            {name:"name"}
        ]);

        this.MasterItemReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.MasterItemRec);

        this.MasterItemStore = new Wtf.data.Store({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCMaster/getMasterItems.do",
            reader:this.MasterItemReader,
            baseParams:{
                mode:112
            }
        });

        this.MasterItemStore.on('load',this.handleLoad2, this);
        this.MasterItemSm=new Wtf.grid.CheckboxSelectionModel({});
        this.MasterItemColumn = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            this.MasterItemSm,
            {
                header:WtfGlobal.getLocaleText("acc.masterConfig.mi"),  //"Master Items",
                sortable:true,
                dataIndex:"name"
            },{
                header:"id",
                hidden: true,
                dataIndex:'id'
            }
        ]);

        this.MasterItemAdd = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterConfig.add"),  //"Add Master Item",
            id:"masterItem"+this.helpmodeid,
            handler:function (){
                this.AddMasterItem(false,this.masterGrid.getSelectionModel().getSelected().data["id"],false);
            },
            disabled:true,
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.msg14"),  //{text:"Select an entry from master group to add master item.",dtext:"Select an entry from master group to add master item.", etext:"Add new item details to the selected master group."},
            iconCls :getButtonIconCls(Wtf.etype.add),
            scope:this
        });

        this.MasterItemEdit = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterConfig.edit"),  //"Edit Master Item",
            handler:function (){
                this.AddMasterItem(true,this.masterGrid.getSelectionModel().getSelected().data["id"],false);
            },
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.msg15"),  //{text:"Select a master item to edit.",dtext:"Select a master item to edit.", etext:"Edit selected master item details."},
            iconCls :getButtonIconCls(Wtf.etype.edit),
            disabled:true
        });

          this.MasterItemDelete = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterConfig.delete"),  //"Delete Master Item",
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.msg16"),  //{text:"Select a master item to delete.",dtext:"Select a master item to delete.", etext:"Delete selected master item details."},
            handler:function (){
                this.DeleteMasterItem();
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.deletebutton),
            disabled:true
        });

        this.masterItemSearch = new Wtf.MyQuickSearch({
            field: 'name',
            emptyText:WtfGlobal.getLocaleText("acc.masterConfig.search1"),  //'Search by Master Item...',
            width: 150
         });

        var MasterItembtn=new Array();
        MasterItembtn.push(this.masterItemSearch);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create))
            MasterItembtn.push(this.MasterItemAdd);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit))
            MasterItembtn.push(this.MasterItemEdit);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove))
            MasterItembtn.push(this.MasterItemDelete);
        MasterItembtn.push("->");
        MasterItembtn.push(getHelpButton(this,this.helpmodeid));
        this.MasterItemGrid = new Wtf.grid.GridPanel({
            cls:'vline-on',
            sm:this.MasterItemSm,
            store:this.MasterItemStore,
            region:"center",
//            loadMask:true, // Remove double Loading Mask
            border: false,
            split: true,
            cm:this.MasterItemColumn,
            viewConfig:{
                forceFit:true
            },
            tbar:MasterItembtn
        });
        this.MasterItemSm.on("selectionchange",function (){
//                Wtf.uncheckSelAllCheckbox1(this.MasterItemSm);
                WtfGlobal.enableDisableBtnArr(MasterItembtn, this.MasterItemGrid, [2], [3]);
            this.changeMsg();
        },this);
    },
    changeMsg:function(){
        if(this.MasterItemAdd.disabled==false)
            this.MasterItemAdd.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg3"));
            else
                this.MasterItemAdd.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg4"));
            if(this.MasterItemEdit.disabled==false)
                this.MasterItemEdit.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg5"));
            else
                this.MasterItemEdit.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg6"));
            if(this.MasterItemDelete.disabled==false)
                this.MasterItemDelete.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg7"));
            else
                this.MasterItemDelete.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg8"));
    },
    handleLoad1:function(store){
       this.masterSearch.StorageChanged(store);

    },
    handleLoad2:function(store){
//       Wtf.uncheckSelAllCheckbox1(this.MasterItemSm);
       Wtf.MessageBox.hide();
       this.masterItemSearch.StorageChanged(store);


    },
    AddMaster:function (isEdit){
         if(this.masterSm.hasSelection())
             var rec=this.masterSm.getSelected();
         var tempTxt=" the name of the Master Group";
         Wtf.Msg.show({
            title: (isEdit)?'Edit Master Group':'Add Master Group',
            msg: (isEdit)?'Edit'+tempTxt:'Enter'+tempTxt,
            value:(isEdit)?rec.data['name']:'',
            prompt:true,
            buttons:{ok:'Save',cancel:"Cancel"},
            width: 300,
            fn:this.saveMasterGroup.createDelegate(this,[(isEdit?rec.data['id']:"")],true)
         });
    },
    AddMasterItem:function (isEdit,id,outer){
        if(isEdit && this.MasterItemSm.getCount()>1){
            Wtf.MessageBox.alert("Editing Error","Can't edit multiple records simultaneously !");
            return;
        }else{
            var rec=null;
            var groupName=this.masterStore.getAt(this.masterStore.find('id',id)).data['name'];
            if(isEdit && this.MasterItemSm.hasSelection())
                rec=this.MasterItemSm.getSelected();
            Wtf.Msg.show({
                title: groupName,
                width:300,
                msg: ((isEdit?WtfGlobal.getLocaleText("acc.common.edit")+' ':WtfGlobal.getLocaleText("acc.masterConfig.common.enterNew")+' ')+groupName),
                value:(isEdit)?rec.data['name']:'',
                buttons:{ok:WtfGlobal.getLocaleText("acc.common.saveBtn"),cancel:WtfGlobal.getLocaleText("acc.common.cancelBtn")},
                prompt:true,
                fn:this.saveMasterGroupItem.createDelegate(this,[isEdit,(isEdit?rec.data['id']:""),id,outer],true)
            });
        }
    },

    saveMasterGroup: function(btn, txt, id){
        if(btn=="ok"&&txt.replace(/\s+/g, '')!=""){
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url:"ACCMaster/saveMasterGroup.do",
                params: {
                        mode:113,
                        name:txt,
                        id:id
                   }
            },this,this.genSuccessResponse,this.genFailureResponse);
        }
    },

    saveMasterGroupItem: function(btn, txt, isEdit, id, masterid, outer){
       if(btn=="ok"){
           if(txt.replace(/\s+/g, '')!=""){
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                    url:"ACCMaster/saveMasterItem.do",
                    params: {
                        mode:114,
                        name:txt,
                        id:id,
                        groupid:masterid
                    }
                },this,this.genSuccessResponse.createDelegate(this,[outer],true),this.genFailureResponse);
           }else{
               Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),  //'Master Configuration',
                    msg: "Please enter new "+this.masterStore.getAt(this.masterStore.find('id',masterid)).data['name'],
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.INFO,
                    width: 300,
                    scope: this,
                    fn: function(){
                        if(btn=="ok"){
                            this.AddMasterItem(isEdit,masterid,outer);
                        }
                    }
                 });

           }
        } else if(outer){
           this.destroy();
       }
   },

   DeleteMasterItem:function (){
       if(this.MasterItemSm.hasSelection()){
           var arrID=[];
           var rec = this.MasterItemSm.getSelections();
           for(var i=0;i<this.MasterItemSm.getCount();i++){
                arrID.push(rec[i].data['id']);
           }
       }
        Wtf.MessageBox.show({
           title: WtfGlobal.getLocaleText("acc.common.warning"),  //"Warning",
           msg: WtfGlobal.getLocaleText("acc.masterConfig.msg1"),  ///+"<div><b>"+WtfGlobal.getLocaleText("acc.masterConfig.msg1")+"</b></div>",
           width: 380,
           buttons: Wtf.MessageBox.OKCANCEL,
           animEl: 'upbtn',
           icon: Wtf.MessageBox.QUESTION,
           scope:this,
           fn:function(btn){
               if(btn=="ok"){
                    Wtf.Ajax.requestEx({
//                        url:Wtf.req.account+'CompanyManager.jsp',
                        url:"ACCMaster/deleteMasterItem.do",
                        params: {
                                mode:116,
                                ids:arrID
                        }
                    },this,this.genSuccessResponse,this.genFailureResponse);
                }
               // this.close();
            }

        });
    },

    genSuccessResponse:function(response, opt,outer){
        if(response.success){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),response.msg],response.success*2+1);
            this.fireEvent('update');//alert(opt.params.toSource())
            if(opt.params.toString().indexOf("mode=113")>=0)
                this.masterStore.load();
            else{
                if(!outer){
                    var rec=this.masterGrid.getSelectionModel().getSelected();
                    var groupid = rec.data.id;
                    (function(){
                    if(groupid==6){ //Accounting specific code to reload global stores
                        Wtf.TitleStore.reload();
                    } else if(groupid==7){
                        Wtf.CustomerCategoryStore.reload();
                    } else if(groupid==8){
                        Wtf.VendorCategoryStore.reload();
                    } else if(groupid==9){
//                        Wtf.dirtyStore.assetCategory = true;
                        Wtf.AssetCategoryStore.reload();
                    }
                    if(rec)
                        this.MasterItemStore.load({
                            params:{
                                groupid:groupid
                            }
                        });
                    }).defer(WtfGlobal.gridReloadDelay(),this);
                }else{
                    this.destroy();
                }
            }
        }

     },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    addMasterItemOuter:function(isEdit,id,outer){
        if(outer){
            this.masterStore.on("load",function(){
                this.AddMasterItem(isEdit,id,outer);
            },this);
        }
        else
            this.AddMasterItem(isEdit,id,outer);
    }
});

