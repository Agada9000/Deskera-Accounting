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
Wtf.account.OSDetailGrid=function(config){
	this.isReceipt=config.isReceipt; 
    this.isMultiDebit=config.isMultiDebit;
    this.isEdit=config.isEdit;
    this.symbol=""; 
    this.businessPerson=(config.isReceipt?'Customer':'Vendor');
    this.currrentAmount=config.amount;
    this.calMultiDebitFunc();
    this.gridRec = Wtf.data.Record.create ([
        {name:'select', type:'bool'},                                    
        {name:'billid'},
        {name:'journalentryid'},
        {name:'personid'},
        {name:'entryno'},
        {name:'billno'},
        {name:'transectionno'},
        {name:'creationdate',type:'date'},
        {name:'date',type:'date'},
        {name:'duedate',type:'date'},
        {name:'currencyid'},
        {name:'oldcurrencyrate'},
        {name:'currencyname'},
        {name:'currencysymbol'},
        {name:'oldcurrencysymbol'},
        {name:'vendorid'},
        {name:'vendorname'},
        {name:'personname'},
        {name: 'externalcurrencyrate'},
        {name:'amountdue', mapping:'amountduenonnegative'},
        {name:'taxpercent'},
        {name:'prpercent'},
        {name:'discount'},
        {name:'amount'},        
        {name:'memo'},
        {name:'payment'},
        {name:'accountid'},
        {name:'accountname'},
        {name:'description'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'dramount'}
    ]);
    var store = new Wtf.data.Store({
        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.gridRec) 
    });
    if(config.isNote){
        store.baseParams={onlyexpenseinv:false,nondeleted:true,deleted:false,ignorezero:true};}
    this.selectionModel =new Wtf.grid.CheckboxSelectionModel({singleSelect:true});

    this.mulDebitCM= new Wtf.grid.ColumnModel([{
        header:WtfGlobal.getLocaleText("acc.rem.30"),  //"Debit Account",
        dataIndex:'accountid',
        width:400,
        editor: this.cmbAccount=new Wtf.form.FnComboBox({
            hiddenName:'accountid',
            store:this.accountStore,
            valueField:'accountid',
            displayField:'accountname',
            forceSelection:true,
            hirarchical:true//,
            //addNewFn:this.openCOAWindow.createDelegate(this)
        }),
        renderer:Wtf.comboBoxRenderer(this.cmbAccount)
    },{
        header:WtfGlobal.getLocaleText("acc.rem.31"),  //"Debit Amount",
        dataIndex:'dramount',
        width:400,
        align:'right',
        renderer:this.drCurrencySymbol,
        summaryType:'sum',
        summaryRenderer:this.drCurrencySymbol.createDelegate(this),
        editor:new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative:false
        })
    },{
        header:WtfGlobal.getLocaleText("acc.invReport.desc"),  //"Description",
        dataIndex:"description",
        width:400,
        editor:this.Description=new Wtf.form.TextArea({
            maxLength:255,
            allowBlank: false,
            xtype:'textarea'
        })
    }]);
    this.checkColumn = new Wtf.grid.CheckColumn({
        header: "",
        dataIndex: 'select',
        width: 20
     });
    
        var columnArr =[];
        if(config.isNote) {
        	columnArr.push(this.selectionModel);				// For Credit Note and Debit Note
        } else{
        	columnArr.push(this.checkColumn);					// For Make Payment and Recieve Payment
        }
        columnArr.push({ 
            header:(config.isReceipt?WtfGlobal.getLocaleText("acc.rem.32"):WtfGlobal.getLocaleText("acc.rem.33")),
            dataIndex:config.readOnly||config.isEdit?'transectionno':'billno',
            width: 450
        },{
            header:WtfGlobal.getLocaleText("acc.rem.34"),  //"Invoice Date",
            dataIndex:config.readOnly||config.isEdit?'creationdate':'date',
            align:'center',
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.mp.amtDue"),  //"Due Date",
            dataIndex:'duedate',
            align:'center',
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.rem.36"),  //"Tax (%)",
            dataIndex:'taxpercent',
            align:'right',
            renderer:function(v){return'<div class="currency">'+v+'%</div>';}
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.discount"),  //"Discount",
            dataIndex:'discount',
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
            header:config.readOnly?(config.isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid")):WtfGlobal.getLocaleText("acc.invoice.gridOriginalAmt"),
            dataIndex:'amount',
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
            header:WtfGlobal.getLocaleText("acc.mp.amtDue"),  //"Amount Due",
            dataIndex:'amountdue',
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            hidelabel:config.readOnly,
            hidden:config.readOnly
        },{
            header:(config.isNote?WtfGlobal.getLocaleText("acc.invoice.discount"):'<b>'+WtfGlobal.getLocaleText("acc.mp.entPay")+'</b>'),
            dataIndex:'payment',
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            hidelabel:(config.isNote ||config.readOnly),
            hidden:(config.isNote ||config.readOnly),
            editor: this.payment=new Wtf.form.NumberField({
                name:'desc'
            })
    });
    this.cm=new Wtf.grid.ColumnModel(columnArr);

    this.summary = new Wtf.ux.grid.GridSummary();
    Wtf.apply(this,{
        store:store,
        stripeRows :true,
        plugins:config.isNote?[this.checkColumn]:[this.summary,this.checkColumn],
        cm:this.cm           
    });
    Wtf.account.OSDetailGrid.superclass.constructor.call(this,config);
    this.on('beforeedit',this.setRowAmount,this);
    this.on('validateedit',this.checkRowAmount,this);
    this.on('afteredit',this.fireAmountChange,this);
    this.on('afteredit',this.addNewRow,this);
    this.on('validateedit',this.checkDuplicateAccount,this);
    this.store.on('load',this.loadPaymentAmount,this);
    this.addEvents({
        'datachanged':true
    });
}

Wtf.extend(Wtf.account.OSDetailGrid,Wtf.grid.EditorGridPanel,{ 
    clicksToEdit:1,
    drCurrencySymbol:function(val,m,rec){
        val=WtfGlobal.withoutRateCurrencySymbol(val,m,rec)
        return val
    },
    calMultiDebitFunc:function(){
        this.accRec = Wtf.data.Record.create([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'}
//            {name:'level',type:'int'}
        ]);
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccount/getAccountsForCombo.do",
//            url: Wtf.req.account+'CompanyManager.jsp',
            baseParams:{
                mode:2
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        this.accountStore.load();
    },

    openCOAWindow:function(){
        this.stopEditing(); 
        callCOAWindow(false, null, "coaWin");
        Wtf.getCmp("coaWin").on("update",function(){this.accountStore.reload()},this);
    },

    addNewRow:function(obj){
        for(var i=0;i<this.store.getCount();i++){
            if(this.store.getAt(i).data.accountid.length<=0|| !(this.store.getAt(i).data.dramount))
                return;
        }
        var newrec = new this.gridRec({
            accountid:'',
            currencysymbol:this.symbol, 
            description:'',
            dramount:0,
            prdiscount:0,
            taxpercent:0
        });
        this.store.add(newrec);
    },

    checkDuplicateAccount:function(obj){
        if(this.store.find("accountid",obj.value)>=0&&obj.ckeckProduct==undefined){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.37")], 2);
            obj.cancel=true;
        }
    },

    reconfigureGrid:function(isMultiDebit){
       this.isMultiDebit=isMultiDebit
        if(isMultiDebit){
            this.reconfigure(this.getStore(),this.mulDebitCM);
            this.getView().forceFit=true;
            this.getView().refresh(); 
            this.addNewRow();
        }
    },

    getMultiDebitAmount:function(){
       var amt=0;
       for(var i=0; i<this.store.getCount();i++){
            amt+=this.store.getAt(i).data['dramount'];
        }
        return amt;
    },

    setCurrencyid:function(cuerencyid,symbol){        
        this.currencyid=cuerencyid;
        this.symbol=symbol;
        this.store.each(function(rec){
            rec.set('currencysymbol',symbol)
        },this)
    },
    loadPaymentAmount:function(){
        var i=0;
        if(this.store.getCount()>0 && this.amount>0){
            var rec;
            for(i=0; i<this.store.getCount();i++){
                rec=this.store.getAt(i)
                if(this.currentAmount>=rec.data['amountdue']){
                    rec.set('payment',rec.data['amountdue']);
                    this.currentAmount-=rec.data['amountdue']
                }
                else break;
            }
            if(this.currentAmount>0 && i<this.store.getCount())
                this.store.getAt(i++).set('payment',this.currentAmount);
            for(var j=i; j<this.store.getCount();j++){
                this.store.getAt(j).set('payment',0);
            }
        }
        else{
            for(i=0; i<this.store.getCount();i++){
                this.store.getAt(i).set('payment',0);
            }
        }
       this.fireEvent('datachanged',this);
    }, 
    setRowAmount:function(obj){ 	
    	if(obj.field=='payment'){
    		if(obj.record.data['select']==false){
    			obj.cancel = true;
    		}
    	}  	    		
    	
//    	this.getCurrentAmount();											//  not used in new make payment logic
//        if(obj.field=='payment'){
//            this.currentAmount+=obj.record.data['payment'];
//            if(this.currentAmount>=(obj.record.data['amountdue'])){
//                var val=obj.record.data['amountdue'];
//                obj.record.set('payment',val);
//                this.currentAmount-=val;
//            }
//            else if(this.currentAmount>0){
//                obj.record.set('payment',this.currentAmount);
//                this.currentAmount=0;
//            }
//        }this.fireEvent('datachanged',this);
    },
    checkRowAmount:function(obj){
        if(obj.field=='payment'){
//            var checkamt=this.currentAmount+obj.originalValue;
//            if(checkamt<=0)
//                obj.cancel=true;
            if(obj.value<0)
            	obj.cancel=true;
//            if(obj.value>checkamt)
//                obj.cancel=true;
            if(obj.value>obj.record.data['amountdue']){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.38")],2);
                obj.cancel=true;                
            }    
        }
    },
    
//    fireAmountChange:function(obj){
//        if(obj.field=='payment')
//            if(obj.value=="")
//                obj.record.set('payment',0);                    
//        this.fireEvent('datachanged',this); 
//    },
    
    fireAmountChange:function(obj){						//  Written for new make and recieve payment functionality         Neeraj
        if(obj.field=='payment'){
            if(obj.value==""){
                obj.record.set('payment',0);
    		}
        }else if(obj.field =='select'){ 
             if(obj.originalValue==true){
        		obj.record.set('payment',obj.record.get('amountdue'));
        	} else if(obj.originalValue==false){ 
        		obj.record.set('payment',0);
        	}
         } 
         this.fireEvent('datachanged',this);
     },
    
    
    getAmount:function(isdue){
        var amt=0;
       for(var i=0; i<this.store.getCount();i++){
            amt+=isdue?this.store.getAt(i).data['amountdue']:this.store.getAt(i).data['payment'];
        }
        return amt;
        
    },
    getCurrentAmount:function(){
       var amt=this.getAmount(false);
        this.currentAmount=this.amount-amt;
        return this.currentAmount;
    },
    updateAmount:function(newamount){
        this.currentAmount=newamount;
        this.amount=newamount;
        for(var i=0; i<this.store.getCount();i++){
            this.store.getAt(i).set('payment',0);
        }
        if(this.amount>0)
            this.loadPaymentAmount();
    },
    
    getData:function(){
        var arr=this.getGridArray();
        return WtfGlobal.getJSONArray(this, true,arr);
    },
    getGridArray:function(){
         var arr=[];
         var len=this.store.getCount();
         for(var i=0;i<len;i++){
             if(this.store.getAt(i).data.payment==0||this.isMultiDebit&&(this.store.getAt(i).data.dramount==0||this.store.getAt(i).data.accountid==""))
                continue;
             else
                arr.push(i);
        }
        return arr;
    }
});
