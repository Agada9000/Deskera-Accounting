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
Wtf.account.BuildAssemblyForm = function(config){
    Wtf.apply(this,config);
    Wtf.account.BuildAssemblyForm.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.BuildAssemblyForm, Wtf.Panel,{
    onRender: function(config){
        Wtf.account.BuildAssemblyForm.superclass.onRender.call(this, config);
        this.createFields();
        this.createAssemblyGrid();
        this.createForm();
        this.add({
            layout:"border",
            border: false,
            items:[{
                region: "center",
                layout: "border",
                border: false,
                items:[this.selectProduct,this.ProductAssemblyGrid,this.BuildForm]
            }],
            bbar:[{
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
                scope:this,
                handler:this.saveForm.createDelegate(this),
                iconCls :'pwnd save'
            },{
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
                scope:this,
                handler:this.closeForm.createDelegate(this),
                iconCls :'pwnd save'
            }]
        });
        this.addEvents({
            'update':true,
            'closed':true
        });
    },

    createFields:function(){
        this.RefNo= new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.build.1")+"*",  //'Build Ref No.*',
            name: 'refno',
            allowBlank:false,
            width: 200
        });
        this.BuildQuantity= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.build.2")+"*",  //'Quantity to build*',
            name: 'quantity',
            allowBlank:false,
            maxLength:15,
            allowDecimals:false,
            allowNegative:false,
            width: 200,
            disabled: true
        });    
        
        this.BuildQuantity.on('blur',this.setCostToBuild,this);
        
        this.memo = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.memo"),  //'Memo',
            name: 'memo',
            height: 50,
            maxLength:255,
            width: 200
        });
        this.costToBuild= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.build.3"),  //'Total Cost To Build',
            name: 'cost',
            value:0,
            cls:"clearStyle",
            readOnly:true
        });
        this.OnHandQuantity= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.build.4"),  //'Available Quantity',
            name: 'qoh',
            value:0,
            cls:"clearStyle",
            readOnly:true
        });
        this.assemblyProductCost= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.build.5"),  //'Unit Product Cost',
            name: 'cost',
            value:0,
            cls:"clearStyle",
            readOnly:true
        });
        this.desc = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.description"),  //'Description',
            name: 'desc',
            height: 70,
            cls:"clearStyle",
            readOnly:true
        });
        this.productRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'productname'},
            {name:'desc'},
            {name:'quantity'},
            {name:'producttype'},
            {name:'type'},
            {name:'purchaseprice'}
        ]);
        this.productStore = new Wtf.data.Store({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url: "ACCProduct/getProductsByType.do",
            baseParams:{mode:28,type:Wtf.producttype.assembly},//Assembly products 
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
        this.productStore.load();
        this.productStore.on("load", this.loadForm,this);
        this.AssemblyProducts=new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.build.6") + "*",  //'Assembly Product*',
            name:'product',
            store:this.productStore,
            allowBlank:false,
            typeAhead: true,
            selectOnFocus:true,
            anchor:"95%",
            valueField:'productid',
            displayField:'productname',
            scope:this,
            forceSelection:true
        });
        this.AssemblyProducts.on("change",function(cmb,newval,oldval){
            if(newval!=oldval){
                this.ProductAssemblyGrid.setProduct(newval);
                var prec = this.productStore.getAt(this.productStore.find("productid",newval));
                this.desc.setValue(prec.data.desc);
                this.OnHandQuantity.setValue(prec.data.quantity);
                this.assemblyProductCost.setValue(prec.data.purchaseprice);
                this.costToBuild.setValue(prec.data.purchaseprice); 
                this.BuildQuantity.enable();
                this.BuildQuantity.setValue(1);
            }
        },this);
        Wtf.productStore.on("load",this.reLoadProductStore,this);
    },
    
    setCostToBuild : function(){
    	if(this.BuildQuantity.getValue() == '0'){
    		Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.build.7"));
    		this.BuildQuantity.setValue(1);
    		this.costToBuild.setValue(this.assemblyProductCost.getValue());
    	}else{
    		this.costToBuild.setValue(this.BuildQuantity.getValue() * this.assemblyProductCost.getValue());
    	}
    },

    reLoadProductStore : function(){
        if(Wtf.getCmp(this.id)){ //Load assembly product store if component exists
            this.productStore.reload();
        } else {//Remove event handler if tab does not exists
            Wtf.productStore.un("load",this.reLoadProductStore,this);
        }
    },

    loadForm: function(){
        if(this.productid!=""){
            this.AssemblyProducts.setValue(this.productid);
            this.ProductAssemblyGrid.setProduct(this.productid);
            var prec = this.productStore.getAt(this.productStore.find("productid", this.productid));
            this.desc.setValue(prec.data.desc);
            this.OnHandQuantity.setValue(prec.data.quantity);
            this.assemblyProductCost.setValue(prec.data.purchaseprice);
            this.costToBuild.setValue(prec.data.purchaseprice);
        }
        this.productStore.un("load", this.loadForm,this);
    },

    createAssemblyGrid:function(){
        this.ProductAssemblyGrid = new Wtf.account.productAssemblyGrid({
            layout:"fit",
             cls:'gridFormat',
             region: 'center',
       //                 border: false,
                 //       baseCls: 'bckgroundcolor',
        //                bodyStyle:"padding:20px",
        //    border:false,
            height:250,
            gridtitle:WtfGlobal.getLocaleText("acc.build.8"), //"Components Needed To Build",
            productid:null,
            rendermode:"buildproduct"
        });
    },

    createForm:function(){
        this.selectProduct=new Wtf.form.FormPanel({
            region: 'north',
            height: 115,
            border:false,
            items:[{
            layout:'form',
            baseCls:'northFormFormat',
            labelWidth:130,
            items:[{
                layout:'column',
                border:false,
                defaults:{border:false},
                items:[{
                        layout:'form',
                        columnWidth:0.32,
                        border:false,
                        defaultType: 'textfield',
                        defaults:{width:200},
                        items:this.AssemblyProducts                     
                    },{
                        layout:"form",
                        border:false,
                        labelWidth:110,
                        columnWidth:0.3,
                        defaultType: 'textfield',
                        defaults:{width:200},
                        items:[this.OnHandQuantity,this.assemblyProductCost]    
                    },{
                        layout:"form",
                        border:false,
                        labelWidth:80,
                        columnWidth:0.35,
                        defaultType: 'textfield',
                        defaults:{width:200},
                        items:[
                            this.desc,
                            {xtype:'hidden',name:'productid'}
                        ]
                    }]
                }]
            }]
       });
       this.BuildForm=new Wtf.form.FormPanel({
            region: 'south',
            height: 150,
            border: false,
            baseCls: 'bckgroundcolor',
            layout: 'form',
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding:0px 10px 10px 10px",
            labelWidth:120,
            items:[{
                layout:'column',
                border: false,
                defaults:{border:false},
                items:[{
                    layout:'form',
                    columnWidth:0.35,
                    items:[this.BuildQuantity,
                           this.RefNo,
                           this.memo]
                	},{
                		labelWidth:110,
                		layout:'form',
                		items:[this.costToBuild]
                	}]
            }]
       });
    },

    closeForm:function(){
        this.fireEvent('closed',this);
    },

    saveForm:function(){
        if(this.BuildQuantity.getValue()==0){
            this.BuildQuantity.markInvalid(WtfGlobal.getLocaleText("acc.build.9"));
            return;
        }
        var selectProdValid = this.selectProduct.getForm().isValid();
        var buildFormValid = this.BuildForm.getForm().isValid();
        if(!selectProdValid || !buildFormValid){
            WtfComMsgBox(2,2);
        }else{
            var warn = WtfGlobal.getLocaleText("acc.build.10");  //"Do you want to save the product(s) assembly?";
            if(this.BuildQuantity.getValue() > this.ProductAssemblyGrid.maxbuilds){
                warn = WtfGlobal.getLocaleText("acc.build.1")+" "+this.BuildQuantity.getValue()+" "+WtfGlobal.getLocaleText("acc.build.2");
            }
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),warn,function(btn){
                if(btn!="yes") { return; }
                WtfComMsgBox(27,4,true);
                var rec=this.BuildForm.getForm().getValues();
                rec.mode=27;
                rec.product=this.AssemblyProducts.getValue();
                rec.assembly=this.ProductAssemblyGrid.getAssemblyJson();
                rec.applydate=WtfGlobal.convertToGenericDate(new Date().clearTime(true));
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                    url: "ACCProduct/buildProductAssembly.do",
                    params: rec
                    },this,this.genSuccessResponse,this.genFailureResponse
                );
            },this);
        }
    },

    genSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.product.gridProduct"),response.msg],response.success*2+1);
        Wtf.dirtyStore.product = true;
        Wtf.productStore.reload();
        Wtf.productStoreSales.reload();
        this.fireEvent('closed',this);
    },
    
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
//        this.fireEvent('closed',this);
    }
});
