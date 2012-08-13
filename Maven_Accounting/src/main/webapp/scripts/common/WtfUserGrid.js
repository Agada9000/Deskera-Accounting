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
Wtf.common.UserGrid=function(config){

    this.usersRec = new Wtf.data.Record.create([
        {name: 'userid'},
        {name: 'username'},
        {name: 'fname'},
        {name: 'lname'},
        {name: 'image'},
        {name: 'emailid'},
        {name: 'lastlogin',type: 'date'},
        {name: 'aboutuser'},
        {name: 'address'},
        {name: 'contactno'},
        {name: 'rolename'},
        {name: 'roleid'}
    ]);

    this.userds = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        },this.usersRec),
//        url: Wtf.req.base+'UserManager.jsp',
        url : "ProfileHandler/getAllUserDetails.do",
        baseParams:{
            mode:11
        }
    });
    this.userds.load({params:{start:0,limit:15}});
    WtfComMsgBox(29,4,true);

    this.userds.on('datachanged', function() {
		if (this.pP.combo) {
			var p = this.pP.combo.value;
			this.quickPanelSearch.setPage(p);
		}
             }, this);
    this.userds.on('load',this.storeloaded,this);

//    this.selectionModel = new Wtf.grid.CheckboxSelectionModel();
    this.selectionModel = new Wtf.grid.RadioSelectionModel();
        
    this.gridcm= new Wtf.grid.ColumnModel([this.selectionModel,{
    	header: WtfGlobal.getLocaleText("acc.userAdmin.image"),  //"Image",
        dataIndex: 'image',
        width : 30,
        renderer : function(value){
            if(!value||value == ""){
                value = Wtf.DEFAULT_USER_URL;
            }
            return String.format("<img src='{0}' style='height:18px;width:18px;vertical-align:text-top;'/>",value);
        }
    },{
        header: WtfGlobal.getLocaleText("acc.userAdmin.name"),  //"Name",
        dataIndex: 'fullname',
        autoWidth : true,
        sortable: true,
        groupable: true,
        renderer : function(value,p,record){
            return (record.data["fname"] + " " + record.data["lname"]);
        }
    },{
        header: WtfGlobal.getLocaleText("acc.userAdmin.userName"),  //"User Name",
        dataIndex: 'username',
        autoWidth : true,
        sortable: true,
        groupable: true
    },{
        header: WtfGlobal.getLocaleText("acc.userAdmin.role"),  //"Role",
        dataIndex: 'rolename',
        autoWidth : true,
        sortable: true,
        groupable: true
    },{
        header :WtfGlobal.getLocaleText("acc.userAdmin.emailAddress"),  //'Email Address',
        dataIndex: 'emailid',
	    autoSize : true,
        sortable: true,
        renderer: WtfGlobal.renderEmailTo,
        groupable: true
    },{
        header :WtfGlobal.getLocaleText("acc.userAdmin.lastLogin"),  //'Last Login',
        dataIndex: 'lastlogin',
        renderer:WtfGlobal.dateRenderer,
        autoSize : true,
        sortable: true,
        groupable: true
    },{
        header :WtfGlobal.getLocaleText("acc.userAdmin.Address"),  //'Address',
        dataIndex: 'address',
        autoSize : true,
        sortable: true,
        groupable: true
    }]);

    this.usergrid = new Wtf.grid.GridPanel({
        stripeRows :true,
        layout:'fit',
        store: this.userds,
        cm: this.gridcm,
        sm : this.selectionModel,
        border : false,
      //  loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }
    });

    this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            tooltip :WtfGlobal.getLocaleText("acc.userAdmin.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    
    this.UsergridPanel  = new Wtf.Panel({
        autoLoad : false,
        paging : false,
        layout : 'fit',
        items:[this.usergrid]
    });
    this.btnArr=[this.quickPanelSearch = new Wtf.KWLTagSearch({
                    emptyText:WtfGlobal.getLocaleText("acc.userAdmin.search"),  //'Search by Name',
                    width: 200,
                    field: 'username'
                }),
                this.resetBttn];
    this.enableBtnArrSingleSelect = new Array();
    this.btnArr.push('-');
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.useradmin, Wtf.Perm.useradmin.assperm)){
    this.assignPermission=new Wtf.Toolbar.Button ({
        text : WtfGlobal.getLocaleText("acc.userAdmin.assignPerm"),  //"Assign Permissions",
        id : "permissions"+this.id,
        allowDomMove:false,
        iconCls :getButtonIconCls(Wtf.etype.permission),
        scope : this,
        tooltip:{text:WtfGlobal.getLocaleText("acc.userAdmin.assignPermTTeText"),dtext:WtfGlobal.getLocaleText("acc.userAdmin.assignPermTTdText")},  //"Select a user and assign permissions for the user with respect to managing vendors, customer and other account settings.", etext:"Select a user and assign permissions for the user with respect to managing vendors, customer and other account settings."},
		disabled: true,
        handler : this.requestPermissions
   })
    this.btnArr.push(this.assignPermission);
    this.enableBtnArrSingleSelect.push(this.btnArr.length-1); // Enable/Disable assignPermission Button as single row select
 //   this.btnArr.push('-');
}
    this.innerpanel = new Wtf.Panel({
        layout : 'fit',
        cls : 'backcolor',
        border : false,
        items:[this.UsergridPanel ],
        // id : 'innerpanel'+this.id,
        tbar:this.btnArr,
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 15,
            //id: "pagingtoolbar" + this.id,
            store: this.userds,
            searchField: this.quickPanelSearch,
            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id})
        })
    });

    Wtf.apply(this,{
        layout : "fit",
        defaults:{border:false,bodyStyle:"background: transparent;"},
      //  loadMask:true,
        autoScroll:true,
        items:[this.innerpanel]
    });
this.usergrid.getSelectionModel().on('selectionchange',this.SelChange,this);
    Wtf.common.UserGrid.superclass.constructor.call(this,config);
},

Wtf.extend(Wtf.common.UserGrid,Wtf.Panel,{
     SelChange:function(){
         WtfGlobal.enableDisableBtnArr(this.btnArr, this.usergrid, this.enableBtnArrSingleSelect, []);
     },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.userds.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
             WtfComMsgBox(29,4,true);
        }
    },
    storeloaded:function(store){
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
        if(this.usergrid.getStore().getCount()==0){
            this.usergrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.usergrid.getView().refresh();
        }
    },
    requestPermissions:function(){
        var permWindow=new Wtf.common.Permissions({
            title:WtfGlobal.getLocaleText("acc.userAdmin.role&perm"),  //"Roles & Permissions",
            resizable: false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            roleid:this.usergrid.getSelectionModel().getSelected().data['roleid'],
            userid:this.usergrid.getSelectionModel().getSelected().data['userid'],
            modal:true
        });
         Wtf.getCmp('AP').on('update',function(){(function(){
            this.usergrid.getStore().reload();
            }).defer(WtfGlobal.gridReloadDelay(),this)},this);
        permWindow.show();
    }
});
