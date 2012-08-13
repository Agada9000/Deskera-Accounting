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
Wtf.common.CreateUser=function(config){
    this.record=config.record;
      Wtf.apply(this,{
        title:'Create User',
        id:'createUserWin',
        modal: true,
        iconCls :getButtonIconCls(Wtf.etype.deskera),
        width: 430,
        height:430,
        resizable: false,
        buttonAlign:"right",
        layout: 'border',
        renderTo: document.body,
        buttons: [{
            text:'Save',
            scope:this,
            handler:function(){
                if(!this.userinfo.getForm().isValid())return;
                this.userinfo.getForm().submit({
                    waitMsg:'Updating User Profile',
                    success:function(f,a){this.genSuccessResponse(eval('('+a.response.responseText+')'));this.close();},
                    failure:function(f,a){this.genFailureResponse(eval('('+a.response.responseText+')'));this.close();},
                    scope:this
                });
            }
        },{
            text: 'Cancel',
            scope:this,
            handler:this.cancel
        }]
    },config);
        this.roleRecord=new Wtf.data.Record.create(
        ['roleid','rolename']
    );
    this.roleStore = new Wtf.data.Store({
        url: Wtf.req.base+'UserManager.jsp',
        baseParams:{
            mode:8
        },
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        },this.roleRecord)
    });
    this.roleCmb= new Wtf.form.ComboBox({
        fieldLabel:'Role',
        hiddenName:'roleid',
        store:this.roleStore,
        valueField:'roleid',
        displayField:'rolename',
        allowBlank:false,
        mode: 'local',
        width:'75%',
        triggerAction: 'all',
        editable : false
    });
    Wtf.common.CreateUser.superclass.constructor.call(this, config);
    this.roleStore.on('load',this.loadRecord,this);
    this.roleStore.load();
    this.addEvents({
        'save':true
    });
}

Wtf.extend( Wtf.common.CreateUser, Wtf.Window, {
    loadRecord:function(){
        if(this.record!=null)this.userinfo.getForm().loadRecord(this.record);
    },
    onRender: function(config){
        Wtf.common.CreateUser.superclass.onRender.call(this, config);       
        this.createForm();
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml('Create User','Create User')
        }, {
            region: 'center',
            border: false,
            bodyStyle: 'background:#f1f1f1;font-size:10px;',
            layout: 'fit',  autoScroll:true,
            items:this.userinfo
        });
    },
    createForm:function(){
        this.userinfo= new Wtf.form.FormPanel({
            fileUpload:true,
            baseParams:{mode:12},
            url: Wtf.req.base+'UserManager.jsp',
            region:'center',
         // cls:'x-panel-body x-panel-body-noheader x-panel-body-noborder',
            bodyStyle: "background: transparent;",
            border:false,
            bodyBorder:false,
            style: "background: transparent;padding-left:20px;padding-top: 20px;padding-right: 0px;",
           // width:'100%',
           // height:'100%',
            id:'userinfo',
            defaultType:'textfield',
            items:[{
                name:'userid',
                xtype:'hidden'
            },{
                fieldLabel:'User Name',
                readOnly:(this.record!=null),
                name:'username',
                maxLength:30,
                width:'75%',
                allowBlank:false
            },{
                fieldLabel:'E-Mail',
                name:'emailid',
                allowBlank:false,
                maxLength:50,
                width:'75%',
                vtype:'email'
            },{
                fieldLabel: 'First Name*',
                name: 'fname',
                maxLength:50,
                width:'75%',
                allowBlank:false
            },{
                fieldLabel: 'Last Name*',
                name: 'lname',
                maxLength:50,
                width:'75%',
                allowBlank:false
            },this.roleCmb,{
                fieldLabel: 'User Picture',
                name:'userimage',
                inputType:'file'
            },{
                fieldLabel: 'Contact No*',
                name: 'contactno',
                xtype:"textfield",
                //regex:Wtf.PhoneRegex,
                width:'75%'
            },{
                fieldLabel: 'Address',
                name: 'address',
                width:'75%',
                maxLength:225,
                xtype:'textarea'
            }]
        });
    },
    cancel:function(){
       this.close();
    },

    genSuccessResponse:function(response){
        if(response.success==true){
            this.fireEvent('save');
        }
        WtfComMsgBox(['User Management',response.msg],response.success*2+1);
     },

    genFailureResponse:function(response){
        var msg="Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox(['Alert',msg],2);
    }
});

