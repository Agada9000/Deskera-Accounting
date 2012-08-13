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
Wtf.account.ProductForm = function(config){
	this.editQuantity=false;						// used as flag to save initial quantity while product editing
	this.isEdit=config.isEdit;
	this.isClone=config.isClone;
    this.productID=config.productID;
    Wtf.apply(this,config);
    Wtf.account.ProductForm.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.ProductForm, Wtf.Panel, {
    loadRecord:function(){
        if(this.record!=null){
            if(this.record.data['parentid']){
                this.subproduct.toggleCollapse();
            }
//            this.ProductForm.getForm().loadRecord(this.record);
                if(!this.isClone){
	            	this.Pname.setValue(this.record.data.productname);
	                this.PID.setValue(this.record.data.pid);
	                this.description.setValue(this.record.data.desc);
                }
                

                if(Wtf.uomStore.getCount()>1){
                    this.uom.setValue(this.record.data.uomid);
                }
                if(Wtf.vendorAccStore.getCount()>1){
                    this.vendor.setValue(this.record.data.vendor);
                }
                if(Wtf.salesAccStore.getCount()>1){
                    this.salesAcc.setValue(this.record.data.salesaccountid);
                    this.salesReturnAcc.setValue(this.record.data.salesretaccountid);
                }
                if(this.globalProductCount>0){
                    this.parentname.setValue(this.record.data.parentuuid);
                }
                this.reorderLevel.setValue(this.record.data.reorderlevel);
                this.rQuantity.setValue(this.record.data.reorderquantity);
                this.leadtime.setValue(this.record.data.leadtime);
                this.cCountInterval.setValue(this.record.data.ccountinterval);
                this.cCountTolerance.setValue(this.record.data.ccounttolerance);
                this.quantity.setValue(this.record.data.initialquantity);
                this.initialprice.setValue(this.record.data.initialprice);
                this.initialsalesprice.setValue(this.record.data.saleprice);
                this.syncable.setValue(this.record.data.syncable);
        }
        if(Wtf.productTypeStore.getCount()>0){
            this.producttype.setValue(this.producttypeval);
            this.changeLayoutWithType();
        }
    },
   onRender: function(config){
       Wtf.account.ProductForm.superclass.onRender.call(this, config);
         this.producttypeval = (this.record!=null?this.record.data.producttype:Wtf.producttype.invpart);
         this.globalProductCount = Wtf.productStore.getCount();
         this.createStore();
         this.createFields();
         this.createToggleFields();
         this.createAssemblyGrid();
         this.createForm();
         this.add({
            layout:"border",
            border:false,
            items:[ this.ProductForm,{
                   region: 'center',
                   border: false,
                    layout:"fit",
                items:this.AssemblyGrid
            }] ,
            bbar:[{
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
                scope:this,
                iconCls :getButtonIconCls(Wtf.etype.save),
                handler: this.saveForm.createDelegate(this)
//            },{
//                 text: 'Cancel',
//                scope: this,
//                iconCls :getButtonIconCls(Wtf.etype.close),
//                handler:this.closeForm.createDelegate(this)
            }]
         });
         this.addEvents({
            'update':true,
            'cancel':true,
            'productClosed':true
        });
        WtfComMsgBox(29,4,true);
    },
    createStore:function(){
       this.purchaseAccRec = Wtf.data.Record.create ([
            {name: 'accid'},
            {name: 'accname'}

       ]);
    this.purchaseAccStore=new Wtf.data.Store({
//           url: Wtf.req.account+'CompanyManager.jsp',
           url:"ACCAccount/getAccountsForCombo.do",
            baseParams:{
                 mode:2,
                 nondeleted: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.purchaseAccRec)
       });
       this.parentRec=new Wtf.data.Record.create([{
                name:'parentid',mapping:'productid'
            },{
                name:'parentname',mapping:'productname'
            },{
                name:'leaf',type:'boolean'
            },{
                name:'level',type:'int'
            },{ 
                name: "productid"
            },{
                name:'pid',mapping:'pid'
            }]);
        this.parentStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.parentRec),
            url:"ACCProduct/getProducts.do",
            baseParams:{
                mode:22,
                productid:(this.record!=null?this.record.data['productid']:null)
            }
        });
        
        if(!this.isEdit || this.isClone){
        	Wtf.Ajax.requestEx({
        		url:"ACCProduct/getProductIDAutoNumber.do",
        		params: {dummyParam:true}
        	},this,this.getIdSuccessResponse);
        }
        
        if(this.isEdit && this.record.data['producttype'] != Wtf.producttype.assembly && !this.isClone){
        	var productid = {productid:this.record.data['productid']};
        	Wtf.Ajax.requestEx({
        		url:"ACCProductCMN/editQuantity.do",
        		params: productid
        	},this,this.editQuantitySuccessResponse,this.editQuantityFailureResponse);
        } 

        if(this.globalProductCount>0){
            this.cloneProductList();
        }
        chkProductTypeload();
        this.loadProductStore();
        chkproductload();
        chkUomload();
        chksalesAccountload();
        chkvenaccload();

        Wtf.productTypeStore.on("load", this.setProductType, this);
        Wtf.uomStore.on("load", this.setUoM, this);
        Wtf.vendorAccStore.on("load",this.setVendor,this);
        Wtf.salesAccStore.on("load", this.setSalesAccount, this);
        Wtf.productStore.on("load", this.cloneProductList, this);

//        this.parentStore.on("load",function(){
//            if(this.record!=null){
//                this.parentname.setValue(this.record.data.parentid);
//            }
//        },this);

        this.purchaseAccStore.on("load",function(){
            if(this.record!=null){							// Neeraj    Check if the id of record is present in the type of Account
                if(this.purchaseAccStore.find('accid',this.record.data.purchaseaccountid) != -1){
                	this.purchaseAcc.setValue(this.record.data.purchaseaccountid);
                }
                if(this.purchaseAccStore.find('accid',this.record.data.purchaseretaccountid) != -1){
                	this.purchaseReturnAcc.setValue(this.record.data.purchaseretaccountid);
                }
            }
            this.loadRecord();
            Wtf.MessageBox.hide();
            this.Pname.focus();
        },this);
        this.purchaseAccStore.on("loadexception",function(){
            this.loadRecord();
            Wtf.MessageBox.hide();
            this.Pname.focus()
        },this);
    },
    
    
    editQuantitySuccessResponse:function(response){
    	this.editQuantity=response.quantityEdit;
		if(!this.editQuantity){
    		this.quantity.disable();
		}else{
			this.quantity.enable();
		}
	},
	
	editQuantityFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
	},
    
	getIdSuccessResponse:function(response){
		if(response.success){
			this.autoID=response.autoNumberID;
			this.PID.setValue(this.autoID);
		}
	},
	
    setUoM: function(){
        if(this.record!=null){
            this.uom.setValue(this.record.data.uomid);
        }
        Wtf.uomStore.un("load", this.setUoM, this);
    },
    setVendor: function(){
        if(this.record!=null){
            this.vendor.setValue(this.record.data.vendor);
        }
        Wtf.vendorAccStore.un("load", this.setVendor, this);
    },
    setProductType: function(){
        this.producttype.setValue(this.producttypeval);//Default Inventory Item
        this.changeLayoutWithType();
        Wtf.productTypeStore.un("load", this.setProductType, this);
    },
    setSalesAccount: function(){
        if(this.record!=null){
            this.salesAcc.setValue(this.record.data.salesaccountid);
            this.salesReturnAcc.setValue(this.record.data.salesretaccountid);
        }
        Wtf.salesAccStore.un("load", this.setSalesAccount, this);
    },
    cloneProductList: function(){
//        this.parentStore.loadData(Wtf.productStore.reader.jsonData);
//        this.parentStore.filter("productid",this.record.data.productid);
        if(Wtf.getCmp(this.id)){
            Wtf.productStore.each(function(rec){
                rec.data.parentid = rec.data.productid;
                rec.data.parentname = rec.data.productname;
                if(this.record!=null){
                    if(rec.data.productid!=this.record.data.productid){
                        this.parentStore.add(rec);
                    }
                } else {
                    this.parentStore.add(rec);
                }
            },this);

            if(this.record!=null && this.globalProductCount<=0){
                this.parentname.setValue(this.record.data.parentuuid);
            }
        }
//        Wtf.productStore.un("load", this.cloneProductList, this);
    },
    createFields:function(){
        this.Pname=new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.productName"),//'Product Name*',
            name: 'productname',
            disabled:(this.isEdit && !this.isClone)?true:false,
            allowBlank:false,
            anchor:'85%',
//            regex:Wtf.specialChar,
            maxLength:50
        });
        this.PID=new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.productID"),//'Product ID*',
            disabled:(this.isEdit && !this.isClone)?true:false,
            name: 'pid',
            anchor:'85%',
            allowBlank:false,
            maxLength:50
        });
        this.syncable= new Wtf.form.Checkbox({
            name:'syncable',
            fieldLabel:WtfGlobal.getLocaleText("acc.product.makeAvailCRM"),//'Make available in CRM',
            checked:false,
            itemCls:"chkboxalign"
        })
        this.producttype= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.productType"),//'Product Type*',
            hiddenName:'producttype',
            store:Wtf.productTypeStore,
            disabled:(this.isEdit && !this.isClone)?true:false,
 			anchor:'85%',
            allowBlank:false,
            valueField:'id',
            displayField:'name',
            forceSelection: true
        });

        this.vendor= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.preferedVendor"),//'Preferred Vendor',
            hiddenName:'vendor',
            store:Wtf.vendorAccStore,
            anchor:'95%',
            allowBlank:true,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true//,
           // addNewFn:this.addPerson.createDelegate(this,[false,null,"vendorwindow",false],true)
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendor, Wtf.Perm.vendor.create))
            this.vendor.addNewFn=this.addPerson.createDelegate(this,[false,null,"vendorwindow",false],true);
        this.cCountInterval=new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.cycleCountInterval"),//"Cycle count interval(in days)* "+WtfGlobal.addLabelHelp("Cycle Count interval defines the time period at which physical counting of Inventory items is to be done. This cross-check helps in identifying discrepancies between Counted quantities,and current records for quantities in the system."),
            name: 'ccountinterval',
            allowDecimals:false,
            allowNegative:false,
            anchor:'85%',
            allowBlank:false,
//            value:0,
            maxLength:11
        });

        this.cCountTolerance=new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.cycleCountTolerance"),//'Cycle count tolerance (%)*'+WtfGlobal.addLabelHelp("It defines the accepted difference between the Physically Counted Quantity and Stock Level present in Deskera Accounting System. This is defined in percentage terms."),
            name: 'ccounttolerance',
            allowDecimals:false,
            allowNegative:false,
            anchor:'85%',
            allowBlank:false,
            maxValue:100,
            minValue:0,
            value:0
        });

        this.uom= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.uom"),//'Unit Of Measure*',
            hiddenName:'uomid',
            store:Wtf.uomStore,
            anchor:'85%',
//            allowBlank:false,
            valueField:'uomid',
            displayField:'uomname',
            forceSelection: true//,
           // addNewFn:this.showUom.createDelegate(this)
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.uom, Wtf.Perm.uom.edit))
            this.uom.addNewFn=this.showUom.createDelegate(this);
        this.salesAcc=new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.salesAcc"),//'Sales Account*',
            store:Wtf.salesAccStore,
            name:'salesaccountid',
            anchor:'85%',
            hiddenName:'salesaccountid',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: false,
          //  addNewFn: this.addAccount.createDelegate(this,[Wtf.salesAccStore,false,false,true],true),
            listeners:{
                select:{
                    fn:function(c){
                        if(!this.salesReturnAcc.getValue())
                            this.salesReturnAcc.setValue(c.getValue());
                    },
                    scope:this
                }
            }
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create))
            this.salesAcc.addNewFn=this.addAccount.createDelegate(this,[Wtf.salesAccStore,false,false,true],true);
        this.salesReturnAcc=new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.salesReturnAcc"),//'Sales Return Account*',
            store:Wtf.salesAccStore,
            name:'salesretaccountid',
            anchor:'85%',
            hiddenName:'salesretaccountid',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: false//,
           // addNewFn: this.addAccount.createDelegate(this,[Wtf.salesAccStore,true,false],true)
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.edit))
            this.salesReturnAcc.addNewFn=this.addAccount.createDelegate(this,[Wtf.salesAccStore,true,false],true);
       this.purchaseAcc=new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'95%',
            name:'purchaseaccountid',
            hiddenName:'purchaseaccountid',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: false,
            //addNewFn:this.isInventory.createDelegate(this),
            listeners:{
                select:{
                    fn:function(c){
                        if(!this.purchaseReturnAcc.getValue())
                            this.purchaseReturnAcc.setValue(c.getValue());
                    },
                    scope:this
                }
            }
       });
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create))
            this.purchaseAcc.addNewFn=this.isInventory.createDelegate(this);//this.uom.addNewFn=this.isInventory.createDelegate(this);
       this.purchaseReturnAcc=new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.purchaseReturnAcc"),//'Purchase Return Account*',
            store:this.purchaseAccStore,
            anchor:'95%',
            name:'purchaseretaccountid',
            hiddenName:'purchaseretaccountid',
            valueField:'accid',
            forceSelection: true,
            displayField:'accname',
            allowBlank: false//,
//            addNewFn: this.addAccount.createDelegate(this,[this.purchaseAccStore,false,true],true)
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create))
            this.purchaseReturnAcc.addNewFn=this.addAccount.createDelegate(this,[this.purchaseAccStore,false,true],true);
        this.parentname= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.parentProduct"),//'Parent Product',
            anchor:'85%',
            hiddenName:'parentid',
            store: this.parentStore,
            valueField:'parentid',
            displayField:'parentname',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            hirarchical:true,
            triggerAction: 'all'
        });
        this.description = new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.description"),// 'Description',
            name: 'desc',
            anchor:'85%',
            height: 50,
            maxLength:200
        });
        this.subproduct=new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.product.isSubProduct"),//'Is a subproduct?',
            checkboxToggle: true,
            autoHeight: true,
//            autoWidth: true,
            width: 380,
            checkboxName: 'subproduct',
            style: 'margin-right:30px',
            collapsed: true,
            items:[this.parentname]
        });


        this.subproduct.on("beforeexpand",this.checkParent,this);
        this.producttype.on("select",this.changeLayoutWithType,this);

        this.CreationDate= new Wtf.form.DateField({
            fieldLabel:'Creation Date*',
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'creationdate',
            anchor:'50%',
    //        listeners:{
    //            'change':{
    //                fn:this.updateDueDate,
    //                scope:this
    //            }
    //        },
            allowBlank:false,
    //        minValue:new Date().format('Y-m-d'),
            minValue: new Date().clearTime(true)
        });



    },
    isInventory:function(){
//        if(this.producttype.getValue()==Wtf.producttype.noninvpart||this.producttype.getValue()==Wtf.producttype.service)
//            this.addAccount(this.purchaseAccStore,false,false)
//        else
            this.addAccount(this.purchaseAccStore,false,true)
    },
    createToggleFields:function(){
        this.rQuantity= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.reorderQty"),//"Reorder Quantity* "+WtfGlobal.addLabelHelp("Reorder Quantity defines minimum quantity of items to be ordered when the stock reaches below Reorder Level."),
            name: 'reorderquantity',
            allowBlank:false,
            anchor:'85%',
            value:0,
            maxLength:15,
            allowDecimals:false,
            allowNegative:false
        });
        this.reorderLevel= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.reorderLevel"),//"Reorder Level* "+WtfGlobal.addLabelHelp("Reorder Level defines a stock level for item at which a new purchase order for items needs to be placed.In simple terms, it denotes the level of stock at which a replenishment order should be placed."),
            name: 'reorderlevel',
            anchor:'85%',
            value:0,
            allowBlank:false,
            maxLength:15,
            allowDecimals:false,
            allowNegative:false
        });
        this.leadtime= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.leadTime"),//'Lead Time(in days)*'+WtfGlobal.addLabelHelp("The amount of time between the placing of an order and the receipt of the goods ordered."),
            name: 'leadtime',
            allowBlank:false,
            anchor:'85%',
            maxLength:3,
            value:0,
            allowDecimals:false,
            allowNegative:false,
            maxValue:365
        });

        this.quantity=new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.initialQty"),//"Initial Quantity",// "+WtfGlobal.addLabelHelp("Transactional data related to initial quantity will not be reflected"),
            name:'initialquantity',
            anchor:'95%',
            maxLength:15,
            disabled :(this.isEdit && !this.isClone)?true:false,
            allowNegative :false,
            allowDecimals:false,
            value:0
        });
        this.initialprice=new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.initialPurchasePrice"),//'Initial Purchase Price',
            name:'initialprice',
            maxLength:15,
            readOnly:(this.isEdit && !this.isClone)?true:false,
            anchor:'95%',
            allowNegative :false
//            allowDecimals:false
//            value:0
        });
        this.initialsalesprice=new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.salesPrice"),//'Sales Price',
            name:'initialsalesprice',
            maxLength:15,
            readOnly:(this.isEdit && !this.isClone)?((this.producttypeval == Wtf.producttype.assembly)?false:true):false,
            anchor:'95%',
            allowNegative :false,
//            allowDecimals:false,
            value:0
        });
        this.reorderLevel.on('blur',this.setInputValue,this);	// Events fired to set and reset the NumberField value to '0' if no user input is detected
        this.reorderLevel.on('focus',this.resetInputValue,this);
        this.rQuantity.on('blur',this.setInputValue,this);
        this.rQuantity.on('focus',this.resetInputValue,this);
        this.leadtime.on('blur',this.setInputValue,this);
        this.leadtime.on('focus',this.resetInputValue,this);
        this.cCountTolerance.on('blur',this.setInputValue,this);
        this.cCountTolerance.on('focus',this.resetInputValue,this);
        this.quantity.on('blur',this.setInputValue,this);
        this.quantity.on('focus',this.resetInputValue,this);
        this.initialsalesprice.on('blur',this.setInputValue,this);
        this.initialsalesprice.on('focus',this.resetInputValue,this);
        this.initialprice.on('blur',function(){if(this.producttypeval == Wtf.producttype.service){this.setInputValue(this.initialprice);}},this);
        this.initialprice.on('focus',function(){if(this.producttypeval == Wtf.producttype.service){this.resetInputValue(this.initialprice);}},this);
    },

    resetInputValue:function(field){		// Remove value '0' when user put focus on the NumberField
    	if(field.getValue() == 0){
    		field.setValue(""); 
    	}
    },
    
	setInputValue:function(field){			// Set value '0' when user puts no input else keep the user input as it is 
		if(field.getValue() == ""){
			field.setValue(0);
		}
	},
	
    createAssemblyGrid:function(){
        this.AssemblyGrid = new Wtf.account.productAssemblyGrid({
            layout:"fit",
            bodyBorder:true,
            hidden:true,
            border:false,
            bodyStyle:'padding:10px',
            height:200,
            gridtitle:WtfGlobal.getLocaleText("acc.product.gridBillofMaterials"),//"Bill Of Materials",
            productid:(this.record!=null?this.record.data['productid']:null),
            rendermode:"productform"
        });
        this.AssemblyGrid.on("updatedcost",function(grid){
            if( this.producttype.getValue() == Wtf.producttype.assembly){
                this.initialprice.setValue(grid.totalcost);
            }
        },this);

    },
     loadProductStore:function(){
//        if(this.producttypeval==Wtf.producttype.noninvpart||this.producttypeval==Wtf.producttype.service){
//           this.purchaseAccStore.load();
//        }
//        else{
            this.purchaseAccStore.load({params:{group:[6]}});
//        }
    },
    changeLayoutWithType:function(c,rec){
        if(rec && this.producttypeval!=rec.data.id){
            this.producttypeval=rec.data.id;
            this.loadProductStore();
        }

        this.producttypeval = this.producttype.getValue();
        //Reset Form
        this.AssemblyGrid.hide();
        WtfGlobal.showFormElement(this.uom);
        WtfGlobal.showFormElement(this.rQuantity);
        WtfGlobal.showFormElement(this.reorderLevel);
        WtfGlobal.showFormElement(this.leadtime);
        WtfGlobal.showFormElement(this.vendor);
        WtfGlobal.showFormElement(this.quantity);
        WtfGlobal.showFormElement(this.initialprice);
        WtfGlobal.showFormElement(this.initialsalesprice);
        this.cCountInterval.allowBlank = false;
        this.cCountTolerance.allowBlank = false;
        WtfGlobal.showFormElement(this.cCountInterval);
        WtfGlobal.showFormElement(this.cCountTolerance);

        if(this.producttypeval == Wtf.producttype.assembly){ //Inventory Assembly
            this.AssemblyGrid.show();
            WtfGlobal.hideFormElement(this.vendor);
        }else if(this.producttypeval == Wtf.producttype.service){ //Service
            this.AssemblyGrid.hide();
            WtfGlobal.hideFormElement(this.uom);
            WtfGlobal.hideFormElement(this.rQuantity);
            WtfGlobal.hideFormElement(this.reorderLevel);
            WtfGlobal.hideFormElement(this.leadtime);
            WtfGlobal.hideFormElement(this.vendor);
            WtfGlobal.hideFormElement(this.quantity);
//            WtfGlobal.hideFormElement(this.initialprice);
            this.cCountInterval.allowBlank = true;
            this.cCountTolerance.allowBlank = true;
            WtfGlobal.hideFormElement(this.cCountInterval);
            WtfGlobal.hideFormElement(this.cCountTolerance);
        }
        if(this.producttypeval == Wtf.producttype.inventoryNonSale){
        	this.salesReturnAcc.setValue(null);
        	WtfGlobal.hideFormElement(this.salesReturnAcc);
        	this.salesAcc.setValue(null);
        	WtfGlobal.hideFormElement(this.salesAcc);
        	this.salesReturnAcc.allowBlank = true;
            this.salesAcc.allowBlank = true;
            WtfGlobal.hideFormElement(this.initialsalesprice);
        }
        else{
        	WtfGlobal.showFormElement(this.salesReturnAcc);
        	WtfGlobal.showFormElement(this.salesAcc);
        }
        //Reset Form Labels
        WtfGlobal.updateFormLabel(this.PID,WtfGlobal.getLocaleText("acc.product.productID")+":");
        WtfGlobal.updateFormLabel(this.Pname,WtfGlobal.getLocaleText("acc.product.productName")+":");
        WtfGlobal.updateFormLabel(this.initialprice,WtfGlobal.getLocaleText("acc.product.initialPurchasePrice")+":");
        if(this.producttypeval == Wtf.producttype.assembly){ //Inventory Assembly
            WtfGlobal.updateFormLabel(this.initialprice,WtfGlobal.getLocaleText("acc.product.assemblyProductCost"));
            this.initialprice.setValue(0);
            this.initialprice.disable();
        }else if(this.producttypeval == Wtf.producttype.service){ //Service
            WtfGlobal.updateFormLabel(this.PID,WtfGlobal.getLocaleText("acc.product.ServiceID")+":");
            WtfGlobal.updateFormLabel(this.Pname,WtfGlobal.getLocaleText("acc.product.ServiceName")+":");
            WtfGlobal.updateFormLabel(this.initialprice,WtfGlobal.getLocaleText("acc.product.servicePurchasePrice"));
            this.initialprice.enable();
//            this.initialprice.setValue(0);
        }else{
            this.initialprice.enable();
            if(!this.isEdit)
            	this.initialprice.setValue(null);
        }    
        if(this.isEdit && !this.isClone){
            this.initialprice.disable();
            if(this.producttypeval == Wtf.producttype.assembly)
            	this.initialsalesprice.enable();
            else
            	this.initialsalesprice.disable();
        }
        this.doLayout();
    },
    showUom:function(){
       callUOM('uomReportWin');
       Wtf.getCmp('uomReportWin').on('update', function(){
           Wtf.uomStore.reload();
       }, this);
    },

    showProductType:function(){
       callProductType('productTypeWin');
       Wtf.getCmp('productTypeWin').on('update', function(){
           Wtf.productTypeStore.reload();
       }, this);
    },

    addPerson:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        Wtf.getCmp(winid).on('update', function(){
           Wtf.vendorAccStore.reload();
        }, this);
    },
    createForm:function(){
    this.ProductForm=new Wtf.form.FormPanel({
        region: 'north',
        height: 250,
       // bodyStyle:"padding:10px",
        border:false,
        items:[{
            layout:'form',
            defaults:{border:false},
            baseCls:'northFormFormat',
            labelWidth:160,
            items:[{
            layout:'column',
            defaults:{border:false},
            items:[{
                layout:'form',
                columnWidth:0.34,
                items:[ {xtype:'hidden',name:'productid', value:(this.isClone)?"":(this.record==null?"":this.record.data.productid)},
                    this.producttype,
                    this.Pname,
                    this.PID,
                    this.description,
                    this.uom,
                    this.reorderLevel,
                    this.rQuantity]
            },{
                layout:'form',
                columnWidth:0.34,
                items:[this.leadtime,

                    this.cCountInterval,
                    this.cCountTolerance,
                    this.subproduct,
                    this.salesAcc,
                    this.salesReturnAcc]
            },{
                layout:'form',
                columnWidth:0.32,
                items:[
                    this.vendor,
                    this.purchaseAcc,
                    this.purchaseReturnAcc,
                    this.quantity,
                    this.initialprice,
                    this.initialsalesprice,
                    this.syncable]
                }]
            }]
        }]
        });




    },
    checkParent:function(){
        if(this.parentStore.getCount()==0)
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.product.msg9")],1);
    },
    closeForm:function(){
        this.fireEvent('closed',this);
     },
     saveForm:function(){
    	if(this.isClone){
    		this.isEdit = false;
    	} 
        this.parentStore.clearFilter(true)
        this.producttypeval = this.producttype.getValue();
        var asemblyjson = "";
        if(this.producttypeval==Wtf.producttype.assembly){//Assembly Item
            this.initialprice.setValue(this.AssemblyGrid.totalcost);
            asemblyjson = this.AssemblyGrid.getAssemblyJson();
            if(asemblyjson.trim()==""){
               WtfComMsgBox(40,2);
               return;
            }
        }
        if(!this.isEdit){
            var FIND = this.Pname.getValue().trim();
            this.Pname.setValue(FIND);
            FIND =FIND.replace(/\s+/g, '');            
            var FINDPID = this.PID.getValue().trim();
            this.PID.setValue(FINDPID);
            FINDPID = FINDPID.replace(/\s+/g, '');
            
            var index=this.parentStore.findBy( function(rec){
                var parentname=rec.data['parentname'].trim();
                parentname=parentname.replace(/\s+/g, '');
                if(parentname==FIND&&!this.isEdit){
                    return true;
                }else{
                    return false
                }
            },this)
            if(index>=0){
                 WtfComMsgBox(36,2);
                 return;
            }

            if(this.PID.getValue()!=""){ //BUG Fixed #16316
               index=this.parentStore.findBy( function(rec){
                   if(rec.data['pid'] != undefined) {
                        var pid = rec.data['pid'].trim();
                        pid =pid.replace(/\s+/g, '');
                        if(pid==FINDPID&&!this.isEdit){
                            return true;
                        }else{
                            return false
                        }
                   } else {
                       return false
                   }
                },this)
                if(index>=0){
                    WtfComMsgBox(41,2);
                    return;
                }
            }
        }
        var isValid = this.ProductForm.getForm().isValid();

        if(this.producttypeval!=Wtf.producttype.service){ // not Service
            if(this.cCountInterval.getValue()==="") {
                this.cCountInterval.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
                isValid = false;
            }else if(this.cCountInterval.getValue()==0) {
                this.cCountInterval.markInvalid(WtfGlobal.getLocaleText("acc.product.msg2"));
                isValid = false;
            }
        }

        if(!this.isEdit && this.producttypeval != Wtf.producttype.service){
            if(this.initialprice.getValue()==="") {
                this.initialprice.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
                isValid = false;
            }else if(this.initialprice.getValue()==0){ // not Service
                this.initialprice.markInvalid(WtfGlobal.getLocaleText("acc.product.msg3"));
                isValid = false;
            }
            
            if(this.uom.getValue()==''){ // not service
                this.uom.markInvalid(WtfGlobal.getLocaleText("acc.product.msg4"));
                isValid = false;
            }
        }

        if(!isValid){       //if(!this.ProductForm.getForm().isValid()){
               WtfComMsgBox(2,2);
        }else{
//           if(this.cCountInterval.getValue()==0&&this.producttypeval!=Wtf.producttype.service){ // not Service
//                WtfComMsgBox(["Alert","Cycle count interval can not be zero."].2);
//                return;
//            }
            if(this.isEdit){
            	if(this.producttype.getValue() == Wtf.producttype.assembly){
            		Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.rem.4"),WtfGlobal.getLocaleText("acc.product.msg5"),function(btn){
                        if(btn=="yes"){
                        	this.reBuild = true;}
                        else{ return; }
         	
                 var rec=this.ProductForm.getForm().getValues();
                 rec.producttype=this.producttype.getValue();
                 rec.initialprice=this.initialprice.getValue();
                 rec.pid=this.PID.getValue();
                 if(this.producttype.getValue() == Wtf.producttype.assembly){
                 	rec.reBuild = this.reBuild;
                 }	
                    rec.productname=this.Pname.getValue();
                    rec.mode=21;
                    rec.parentname=this.parentname.getRawValue();
                    rec.syncable=(this.syncable.getValue()=="on"?true:this.syncable.getValue());
                    rec.quantity=this.quantity.getValue();
                    rec.editQuantity = this.editQuantity;
                    rec.assembly=asemblyjson;
                    rec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                    if(this.initialsalesprice.getValue() != "" && this.initialprice.getValue() > this.initialsalesprice.getValue()){
                    	Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.rem.4"),WtfGlobal.getLocaleText("acc.product.msg6"),function(btn){
                            if(btn=="yes"){
                            	Wtf.Ajax.requestEx({
                                    url: "ACCProduct/saveProduct.do",
                                    params: rec
                                },this,this.genSuccessResponse,this.genFailureResponse);
                            }
                            else{ return; }
                    	},this);    
                    }else{                    
	                    Wtf.Ajax.requestEx({
	                        url: "ACCProduct/saveProduct.do",
	                        params: rec
	                    },this,this.genSuccessResponse,this.genFailureResponse);
                    }
            	},this);
            	}else{
            	
            	   var rec=this.ProductForm.getForm().getValues();
	               rec.producttype=this.producttype.getValue();
	               rec.initialprice=this.initialprice.getValue();
	               rec.pid=this.PID.getValue();
                	
                   rec.productname=this.Pname.getValue();
                   rec.mode=21;
                   rec.parentname=this.parentname.getRawValue();
                   rec.syncable=(this.syncable.getValue()=="on"?true:this.syncable.getValue());
                   rec.quantity=this.quantity.getValue();
                   rec.editQuantity = this.editQuantity;
                   rec.assembly=asemblyjson;
                   rec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                   Wtf.Ajax.requestEx({
                       url: "ACCProduct/saveProduct.do",
                       params: rec
                   },this,this.genSuccessResponse,this.genFailureResponse);
            	}    
            }else{
//                if(this.initialprice.getValue()==0 && this.producttypeval != Wtf.producttype.service){ // not Service
//                    WtfComMsgBox(["Alert"," Initial purchase price of the product can not be zero."],2);
//                    return;
//                }
//                if(this.uom.getValue()=='' && this.producttypeval!=Wtf.producttype.service){ // not service
//                    WtfComMsgBox(["Alert"," Please select UoM"],2);
//                    return;
//                }
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),(this.producttypeval != Wtf.producttype.service?WtfGlobal.getLocaleText("acc.product.msg7"):"")+" "+WtfGlobal.getLocaleText("acc.product.msg8"),function(btn){
                        if(btn!="yes") { return; }
                        WtfComMsgBox(27,3,true);
                        var rec=this.ProductForm.getForm().getValues();
                        rec.mode=21;
                        rec.parentname=this.parentname.getRawValue();
                  //      rec.creationdate=this.CreationDate.getValue();
                        rec.quantity=this.quantity.getValue();
                        rec.syncable=(this.syncable.getValue()=="on"?true:this.syncable.getValue());
                        rec.assembly=asemblyjson;
                        rec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                        Wtf.Ajax.requestEx({
//                        url: Wtf.req.account+'CompanyManager.jsp',
                        url: "ACCProduct/saveProduct.do",
                        params: rec
                        },this,this.genSuccessResponse,this.genFailureResponse);
                    },this)
            }
        }
    },
    genSuccessResponse:function(response){
        if(response.success){
          WtfComMsgBox([WtfGlobal.getLocaleText("acc.invReport.prod"),response.msg],0);
          if(this.record!=null)
            this.productID=this.record.data.productid;
          if(!this.isEdit){
            var pricerec={};
            this.productID=response.productID;
            //Set initial Purchase price
            if(this.initialprice.getValue()>0){
                pricerec.carryin=true;
                pricerec.productid=response.productID;

                pricerec.price=this.initialprice.getValue();
                pricerec.mode=11;
                pricerec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                    url:"ACCProduct/setNewPrice.do",
                    params: pricerec
                },this,this.genPriceSuccessResponse,this.genPriceFailureResponse);
            }

            //Set initial Sales price
            if(this.initialsalesprice.getValue()>0){
                var pricerec1={};				// Send params as object and not as array bcos general controller accept requests in object form
                pricerec1.carryin=false;
                pricerec1.productid=response.productID;
                pricerec1.price=this.initialsalesprice.getValue();
                pricerec1.mode=11;
                pricerec1.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                    url:"ACCProduct/setNewPrice.do",
                    params: pricerec1
                },this,this.genPriceSuccessResponse,this.genPriceFailureResponse);
            }

//            if(this.initialprice.getValue()<=0 && this.initialsalesprice.getValue()<=0){
//               Wtf.productStore.reload();
                this.fireEvent('update',this,response.productID);
                this.fireEvent('productClosed',this);
//            }
            //if aassembly call function for build assembly


//            if( this.producttype.getValue()==Wtf.producttype.assembly){
////                alert("This is test");
//                Wtf.MessageBox.confirm("Build Assembly"," You want to build assembly?",function(btn){
//                    if(btn == "yes") {
//                        callBuildAssemblyForm(response.productID);
//                        this.fireEvent('update',this,response.productID);
//                        this.fireEvent('closed',this);
//                    }else{
//                        this.fireEvent('update',this,response.productID);
//                        this.fireEvent('closed',this);
//                        return;
//                    }
//                },this)
//            }else{
//                this.fireEvent('update',this,response.productID);
//                this.fireEvent('closed',this);
//            }
          }else{
        	  if(this.isEdit && this.producttype.getValue() == Wtf.producttype.assembly){
                  var pricerec={};
                  this.productID=response.productID;
                  //Set Purchase price for Inventory Assembly 
                  if(this.initialprice.getValue()>0){
                      pricerec.carryin=true;
                      pricerec.productid=response.productID;
                      pricerec.changeprice = true;							// To change existing price			Neeraj
                      pricerec.price=this.initialprice.getValue();
                      pricerec.mode=11;
                      pricerec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                      Wtf.Ajax.requestEx({
                          url:"ACCProduct/setNewPrice.do",
                          params: pricerec
                      },this,this.genPriceSuccessResponse,this.genPriceFailureResponse);
                  }
                  if(this.initialsalesprice.getValue()>0){
                      var pricerec1={};				
                      pricerec1.carryin=false;
                      pricerec1.productid=response.productID;
                      pricerec1.changeprice = true;
                      pricerec1.price=this.initialsalesprice.getValue();
                      pricerec1.mode=11;
                      pricerec1.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                      Wtf.Ajax.requestEx({
                          url:"ACCProduct/setNewPrice.do",
                          params: pricerec1
                      },this,this.genPriceSuccessResponse,this.genPriceFailureResponse);
                  }
        	  }
//                Wtf.productStore.reload();
        	  this.fireEvent('update',this,this.productID);
              this.fireEvent('productClosed',this);
        	    
          }

        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.invReport.prod"),response.msg],1);
        }
        Wtf.productStoreSales.reload();
        Wtf.productStore.reload();
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
//        this.fireEvent('closed',this);
    },
    addAccount: function(store,issales,ispurchase,incomenature){
        callCOAWindow(false,null,"coaWin",issales,ispurchase,false,false,false,incomenature);
        Wtf.getCmp("coaWin").on('update',function(){store.reload();},this);
    },
    genPriceSuccessResponse:function(response){
//          if( this.producttype.getValue()==Wtf.producttype.assembly){
//                Wtf.MessageBox.confirm("Build Assembly"," You want to build assembly?",function(btn){
//                  if(btn!="yes") { this.fireEvent('update',this,this.productID);return; }
//                  else
//                    callBuildAssemblyForm(this.productID);
//                    this.fireEvent('update',this,this.productID);
//                },this)
//          }
//            else
                this.fireEvent('update',this,this.productID);

//        this.fireEvent('productClosed',this);
//        Wtf.productStore.reload();
//        WtfComMsgBox(['Price List',response.msg],response.success*2+1);
//        if(response.success) this.fireEvent('update',this);
//        this.fireEvent('closed',this);
    },
    genPriceFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
//        this.fireEvent('closed',this);
    }
/* Product Type Master
+--------------------------------------+--------------------+
| id                                   | name               |
+--------------------------------------+--------------------+
| e4611696-515c-102d-8de6-001cc0794cfa | Inventory Assembly |
| d8a50d12-515c-102d-8de6-001cc0794cfa | Inventory Part     |
| f071cf84-515c-102d-8de6-001cc0794cfa | Non-Inventory Part |
| 4efb0286-5627-102d-8de6-001cc0794cfa | Service            |
+--------------------------------------+--------------------+
 * */
});
