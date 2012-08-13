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
Wtf.account.PricelistWindow = function(config){
    this.disablePriceType=(config.carryIn!=null);
    this.record = config.record;
    Wtf.apply(this,{
         buttons: [{
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
                    scope: this,
                    handler: this.saveForm
                  },{
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
                    scope: this,
                    handler: function(){ this.close();}
         }]
    },config);
    Wtf.account.PricelistWindow.superclass.constructor.call(this, config);
     this.addEvents({
        'update':true,
        'loadingcomplete':true
    });
}

Wtf.extend(Wtf.account.PricelistWindow, Wtf.Window, {
    carryIn:true,
    applyDate:null,
    loadRecord:function(){
        this.fireEvent("loadingcomplete",this);
        if(this.record!=null)
            this.PricelistForm.getForm().loadRecord(this.record);
        if(this.applyDate != null || this.applyDate != undefined)
        	this.applydate.setValue(this.applyDate);
    },
    onRender: function(config){
        Wtf.account.PricelistWindow.superclass.onRender.call(this, config);
        this.createCombo();
        this.createFields();
        this.createForm();
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.rem.67"),WtfGlobal.getLocaleText("acc.rem.67")+' '+this.record.data.productname,"../../images/accounting_image/price-list.gif")
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.PricelistForm
        });       
        this.productStore.load();
    },

    createCombo:function(){
         this.productRec = Wtf.data.Record.create ([
        {name:'productid'},
        {name:'productname'},
        {name:'desc'},
        {name:'uomid'},
        {name:'uomname'},
        {name:'parentid'},
        {name:'parentname'},
        {name:'purchaseaccountid'},
        {name:'salesaccountid'},
        {name:'purchaseretaccountid'},
        {name:'salesretaccountid'},
        {name:'reorderquantity'},
        {name:'quantity'},
        {name:'reorderlevel'},
        {name:'leadtime'},
        {name:'purchaseprice'},
        {name:'saleprice'},
        {name: 'leaf'},
        {name: 'level'},
        {name: 'producttype'}
    ]);

    this.productStore = new Wtf.data.Store({
//        url:Wtf.req.account+'CompanyManager.jsp',
        url:"ACCProduct/getProducts.do",
        baseParams:{mode:22},
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.productRec)
    });
    this.productStore.on('load',this.loadRecord,this);
        this.productname= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*',
            hiddenName:'productid',
            name: 'productid',
            store: this.productStore,
            valueField:'productid',
            displayField:'productname',
            mode: 'local',
            readOnly:true,
            hidden:true,
            hideLabel:true
        });
    },
    
    createFields:function(){
        this.price= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.rem.69") + ' '+WtfGlobal.getCurrencySymbolForForm()+'*',
            name: 'price',
            allowBlank:false,
            allowNegative:false,
            xtype:'numberfield',
            maxLength:15
        });
        this.price.on("blur",this.checkZero,this);				// Event fired for checking if price entered is "0"
        
        this.applydate= new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inventoryList.date"),  //'Date',            
            name: 'applydate',
  //          minValue:new Date().format('Y-m-d'),
            format:WtfGlobal.getOnlyDateFormat(),
            value: Wtf.serverDate.clearTime(true),
  //          minValue: new Date().clearTime(true),
            allowBlank:false
        });
        var carryinStore=new Wtf.data.SimpleStore({
            fields:[{name:"id"},{name:"name"}],
            data:[[true,WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice")],[false,WtfGlobal.getLocaleText("acc.productList.gridSalesPrice")]]
        });
        this.cmbCarryIn= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.70") + '*',
            hiddenName:'carryin',
            name: 'carryin',
            store: carryinStore,
            disableKeyFilter:true,
            triggerAction:'all',
            forceSelection:true,
            typeAhead: true,
            value:(this.record.data.producttype == Wtf.producttype.inventoryNonSale)?true:this.carryIn,
            disabled:(this.record.data.producttype == Wtf.producttype.inventoryNonSale)?true:false,		
            readOnly:this.disablePriceType,
            valueField:'id',
            displayField:'name',
            mode: 'local',
            allowBlank:false
        });
    },
    
    checkZero:function(){							// Method for checking if price in pricelist form is "0"
    	if(this.price.getValue() == '0'){
    		Wtf.MessageBox.show({
    			title:WtfGlobal.getLocaleText("acc.common.warning"),
    			msg:WtfGlobal.getLocaleText("acc.rem.71"), //'Cannot set Purchase price or Sales price as \'0\'',
    			buttons:Wtf.Msg.OK
    		});
    		this.price.setValue("");
    	}
    },
    
    createForm:function(){
       this.PricelistForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
            defaults:{
                width:200
            },
             items:[{xtype:'hidden',name:'priceid'}, this.productname,
                   this.cmbCarryIn,this.price,this.applydate]
       });
  
     
   },
   saveForm:function(){
        if(!this.PricelistForm.getForm().isValid()){
            WtfComMsgBox(2,2);
        }
        else{
            var rec=this.PricelistForm.getForm().getValues();
            if(this.record.data.producttype == Wtf.producttype.inventoryNonSale){
            	rec.carryin = true;
            }
            rec.mode=11;
            rec.applydate=WtfGlobal.convertToGenericDate(this.applydate.getValue());
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url:"ACCProduct/setNewPrice.do",
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
        }
    },
    genSuccessResponse:function(response){
        if(response.dateexist)
             Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.currency.update"),(this.cmbCarryIn.getValue()?WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice"):WtfGlobal.getLocaleText("acc.productList.gridSalesPrice"))+ " "+WtfGlobal.getLocaleText("acc.rem.141"),function(btn){
                if(btn!="yes") { return; }
                var rec=this.PricelistForm.getForm().getValues();
                if(this.record.data.producttype == Wtf.producttype.inventoryNonSale){
                	rec.carryin = true;
                }
                rec.mode=11;
                rec.changeprice=true;
                rec.applydate=WtfGlobal.convertToGenericDate(this.applydate.getValue());
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                    url:"ACCProduct/setNewPrice.do",
                    params: rec
                },this,this.genUpdateSuccessResponse,this.genFailureResponse);
             },this)
         else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.rem.72"),response.msg],response.success*2+1);
            if(response.success) {
                this.fireEvent('update',this);
                Wtf.productStore.reload();
                Wtf.productStoreSales.reload();
            }
            this.close();
        }
    },
    genUpdateSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.rem.72"),response.msg],response.success*2+1);
        if(response.success) {
            this.fireEvent('update',this);
            Wtf.productStore.reload();
            Wtf.productStoreSales.reload();
        }
        this.close();
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
});
