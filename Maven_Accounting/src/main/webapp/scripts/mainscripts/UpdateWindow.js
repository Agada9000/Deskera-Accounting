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
Wtf.account.GridUpdateWindow = function(config){
    var btnArr=[];
     if(config.mode==32) {
            this.uPermType=Wtf.UPerm.uom;
            this.permType=Wtf.Perm.uom;
        } else if(config.mode==34) {
            this.uPermType=Wtf.UPerm.tax;
            this.permType=Wtf.Perm.tax;
        } else if(config.mode==92) {
            this.uPermType=Wtf.UPerm.creditterm;
            this.permType=Wtf.Perm.creditterm;
        } else if(config.mode==52) {
            this.uPermType=Wtf.UPerm.paymentmethod;
            this.permType=Wtf.Perm.paymentmethod;
//        } else if(config.mode==26) {
        } else if(config.mode==82){
            this.uPermType=3;
            this.permType={edit:2, view:1};
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)) {
            btnArr.push({
                text: WtfGlobal.getLocaleText("acc.common.update"),  //'Update',
                scope: this,
                handler:this.addArr.createDelegate(this)
            });
        }
    btnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.close"),  //'Close',
        scope: this,
        handler: function(){
            this.close();
        }
    });
    Wtf.apply(this,{
        buttons: btnArr
    },config);
    Wtf.account.GridUpdateWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.GridUpdateWindow, Wtf.Window, {
    closable: true,
    addDeleteCol: true,
    rowDeletedIndexArr:null,
    rowIndexArr:null,
    modal: true,
    iconCls :getButtonIconCls(Wtf.etype.deskera),
    width: 550,
    record:null,
    height: 350,
    resizable: false,
    layout: 'border',
    buttonAlign: 'right',
    initComponent: function(config){
        Wtf.account.GridUpdateWindow.superclass.initComponent.call(this, config);
        if(this.addDeleteCol){
            this.cm.push({
                width:50,
                header:WtfGlobal.getLocaleText("acc.masterConfig.costCenter.action"),  //'Action',
                renderer:this.deleteRenderer.createDelegate(this)
            });
        }
    },

    onRender: function(config){
        this.rowDeletedIndexArr=[];
        this.rowIndexArr=[];
        Wtf.account.GridUpdateWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(this.title,WtfGlobal.getLocaleText("acc.rem.27")+" "+this.title,this.headerImage, true)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid

        });
        this.addEvents({
            'update':true
        });
        this.store=this.grid.getStore();
        this.grid.on('rowclick',this.processRow,this);
        this.grid.on('afteredit',this.addGridRec,this);
        this.grid.on('validateedit',this.checkDuplicate,this);
        this.grid.on('beforeedit',this.checkrecord,this);
        this.store.on('load',this.addGridRec,this);
   },

    createDisplayGrid:function(){
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)){
            this.grid = new Wtf.grid.EditorGridPanel({
                plugins:this.gridPlugins,
                layout:'fit',
                clicksToEdit:1,
                store: this.store,
                cm: new Wtf.grid.ColumnModel(this.cm),
                border : false,
                loadMask : true,
                viewConfig: {
                    forceFit:true,
                    emptyText:WtfGlobal.getLocaleText("acc.common.norec")
                }
            });
        }else{
            this.grid = new Wtf.grid.GridPanel({
                plugins:this.gridPlugins,
                layout:'fit',
                clicksToEdit:1,
                store: this.store,
                cm: new Wtf.grid.ColumnModel(this.cm),
                border : false,
                loadMask : true,
                viewConfig: {
                    forceFit:true,
                    emptyText:WtfGlobal.getLocaleText("acc.common.norec")
                }
            });
        }
    },
    getdeletedArr:function(grid,index,rec){
        var store=grid.getStore();
        var fields=store.fields;
            var recarr=[];
            if(rec.data['taxid']!=""){
                for(var j=0;j<fields.length;j++){
                    var value=rec.data[fields.get(j).name];
                    switch(fields.get(j).type){
                        case "auto": value="'"+value+"'"; break;
                        case "date": value="'"+WtfGlobal.convertToGenericDate(value)+"'";break;
                    }
                    recarr.push(fields.get(j).name+":"+value);
                }
                recarr.push("modified:"+rec.dirty);
                this.rowDeletedIndexArr.push("{"+recarr.join(",")+"}");
            }
},
    processRow:function(grid,rowindex,e){        
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
            var store=grid.getStore();
            var rec=store.getAt(rowindex);
            this.getdeletedArr(grid,rowindex,rec);
                store.remove(store.getAt(rowindex));
                this.addGridRec();
            }, this);
        }
    },
    checkrecord:function(obj){
        if(this.istax){
            var idx = this.grid.getStore().find("taxid", obj.record.data["taxid"]);
            if(idx>=0)
                obj.cancel=true;
        }
    },

     checkDuplicate:function(obj){
        if(this.istax &&obj.field=="taxname"){
           var FIND = obj.value;
            FIND =FIND.replace(/\s+/g, '');
            var index=this.grid.getStore().findBy( function(rec){
            var taxname=rec.data['taxname'].trim();
            taxname=taxname.replace(/\s+/g, '');
            if(taxname==FIND)
                return true;
            else
                return false
        })
        if(index>=0){
                obj.cancel=true;
        }
        }
    },
    addArr:function(){
        var inValidRows = new Array();
        var cm = this.grid.getColumnModel();

        var editedarr=[];
         for(var i=0;i<this.store.getCount();i++){
             var   rec=this.store.getAt(i);
            if(rec.dirty){
                editedarr.push(i);

                for(var j=0;j<cm.getColumnCount();j++){
                    var editor = cm.getCellEditor(j,i);
                    var cellData = ""+rec.data[cm.getDataIndex(j)];
                    if(editor != undefined && editor.field.allowBlank !=undefined && !editor.field.allowBlank && cellData.trim().length == 0){
                        inValidRows.push(i+1);
                        break;
                    }
                }
            }
        }

        if(inValidRows.length>0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.tax.msg3")+" "+(inValidRows.join(","))], 2);
            return;
        }

        this.rowIndexArr=editedarr;
        this.update(editedarr);
    },
    addGridRec:function(){ 
        var size=this.store.getCount();
        if(size>0){
            var lastRec=this.store.getAt(size-1);
            var cm=this.grid.getColumnModel();
            var count = cm.getColumnCount();
            for(var i=0;i<count-1;i++){
                if(lastRec.data[cm.getDataIndex(i)].length<=0)
                    return;
            }
        }
        var rec=this.record;
        rec = new rec({});
        rec.beginEdit();
        var fields=this.store.fields;
        for(var x=0;x<fields.length;x++){
            var value="";
            rec.set(fields.get(x).name, value);
        }      
        rec.endEdit();
        rec.commit();
        this.store.add(rec);
    },

    deleteRenderer:function(v,m,rec){
        var flag=true;
        var cm=this.grid.getColumnModel();
        var count = cm.getColumnCount();
        for(var i=0;i<count-1;i++){
            if(rec.data[cm.getDataIndex(i)].length<=0){
                flag=false;
                break;
            }
        }
        if(flag){
              var deletegriclass=getButtonIconCls(Wtf.etype.deletegridrow);
            return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
        }
        return "";
    }, 

    update:function(arr){
        var rec;
        rec={
            data:this.getJSONArray(arr),
            deleteddata:"["+this.rowDeletedIndexArr.join(',')+"]"
        };

        this.ajxUrl = Wtf.req.account+'CompanyManager.jsp';
        if(this.mode==32) {
            this.ajxUrl = "ACCUoM/saveUnitOfMeasure.do";
        } else if(this.mode==34) {
            this.ajxUrl = "ACCTax/saveTax.do";
        } else if(this.mode==92) {
            this.ajxUrl = "ACCTerm/saveTerm.do";
        } else if(this.mode==52) {
            this.ajxUrl = "ACCPaymentMethods/savePaymentMethod.do";
        } else if(this.mode==26) {
            this.ajxUrl = "ACCProduct/saveProductTypes.do";
        } else if(this.mode==82) {
            this.ajxUrl = "CostCenter/saveCostCenter.do";
        }

       if(rec.deleteddata=="[]"&&rec.data=="[]"){
           if(arr!="")
               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.tax.msg2")], 2);
           return;
       }
       else if(this.istax&&rec.deleteddata=="[]"){
             Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.tax.msg1"),function(btn){
                if(btn!="yes") { return; }
                rec.mode=this.mode;
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                    url : this.ajxUrl,
                    params: rec
                },this,this.genSuccessResponse,this.genFailureResponse);
            },this)
        }
        else{
            rec.mode=this.mode;
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url : this.ajxUrl,
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
        }
        //this.close();
    },

    getJSONArray:function(arr){
        return WtfGlobal.getJSONArray(this.grid,false,arr);
    },
//TODO    gentaxSuccessResponse:function(response){
//       var rec={
//            data:this.getJSONArray(this.rowIndexArr),
//            deleteddata:"["+this.rowDeletedIndexArr.join(',')+"]"
//            };
//        if(response.msg==true&& rec.deleteddata=="[]"){
//             Wtf.MessageBox.confirm("Save Data","Tax details are not editable. Do you wish to continue?",function(btn){
//                if(btn!="yes") { return; }
//            rec.mode=this.mode;
//            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
//                params: rec
//            },this,this.genSuccessResponse,this.genFailureResponse);
//        },this)
//        }
//        else{
//            rec.mode=this.mode;
//            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
//                params: rec
//            },this,this.genSuccessResponse,this.genFailureResponse);
//        }
//    },
//
//    gentaxFailureResponse:function(response){
//        var msg="Failed to make connection with Web Server";
//        if(response.msg)msg=response.msg;
//        WtfComMsgBox(['Alert',msg],2);
//
//    },
//
    genSuccessResponse:function(response){
        WtfComMsgBox([this.title,response.msg],0);
        if(response.success){    
            this.fireEvent('update',this);
            this.store.reload();
            if(this.mode==32) {
                Wtf.uomStore.reload();
            } else if(this.mode==34) {
                Wtf.taxStore.reload();
            } else if(this.mode==92) {
                Wtf.termds.reload();
            } else if(this.mode==52) {
                //PaymentMethods
            } else if(this.mode==26) {
                Wtf.productTypeStore.reolad();
            } else if(this.mode==82) {
                if(Wtf.StoreMgr.containsKey("CostCenter")){Wtf.CostCenterStore.reload();}
                if(Wtf.StoreMgr.containsKey("FormCostCenter")){Wtf.FormCostCenterStore.reload();}
            }
            this.close();
        }
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }

});  

