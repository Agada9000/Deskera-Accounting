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


Wtf.account.PayMethodPanel = function(config) {
    this.isReceipt=config.isReceipt; 
    Wtf.account.PayMethodPanel.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.account.PayMethodPanel, Wtf.form.FormPanel,{
    labelWidth:150,
    onRender: function(config) {
        this.createStore();
        this.createFields();
        this.add(this.cheque);
 //       this.add(this.creditCard,this.cheque);
        this.expDate.on('change',this.checkExpDate,this);
        Wtf.account.PayMethodPanel.superclass.onRender.call(this, config);
    },

    createStore:function(){
        this.bankRec=new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}]);

        this.bankTypeStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.bankRec),
//            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
                groupid:2
            }
        });
        this.bankTypeStore.load();
    },

    createFields:function(){
    this.bank= (!this.isReceipt? new Wtf.form.TextField({
        name:"paymentthrough",
        fieldLabel:WtfGlobal.getLocaleText("acc.nee.47"),  //'Bank From Name*',
        allowBlank:false,
        anchor: '90%',
        maxLength:50
    }):new Wtf.form.FnComboBox({
        fieldLabel:'Bank Name*',
        name:"paymentthrough",
        hiddenName:'paymentthrough',
        store:this.bankTypeStore,
        anchor:'90%',
        listWidth:188,
        allowBlank:false,
        valueField:'id',
        displayField:'name',
        mode: 'local',
        triggerAction:'all',
//        addNewFn:this.addMaster.createDelegate(this,[2,this.bankTypeStore]),
        forceSelection:true
    }));

        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))
            this.bank.addNewFn= this.addMaster.createDelegate(this,[2,this.bankTypeStore])

    this.checkNo = new Wtf.form.NumberField({
        fieldLabel:WtfGlobal.getLocaleText("acc.nee.46"),  //"Cashier's Check/Check/Reference Number*",
        name:"checkNo",
        allowBlank:false,
        maxLength:15,
        allowNegative:false,
        anchor: '80%'
    });

    this.cardNo=new Wtf.form.NumberField({
        fieldLabel:"Card Number",
        name:"CardNo",
        maxLength: 16,
        minLength:16,
        anchor: '90%'
    });

    this.description =new Wtf.form.TextArea({
        name:"description",
        height:40,
        anchor: '90%',
        fieldLabel:WtfGlobal.getLocaleText("acc.product.description"),  //"Description",
        maxLength: 255
    });

    this.expDate=new Wtf.form.DateField({
        name:"expirydate",
        anchor: '90%',
        fieldLabel:"Expiry Date",
        format:WtfGlobal.getOnlyDateFormat()
    });

    this.nameOnCard = new Wtf.form.TextField({
        name:"nameoncard",
        fieldLabel:"Card Holder Name*",
        allowBlank:false,
        anchor: '90%',
        maxLength:50
    });
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :[['0','Master Card'],['1','Visa']]
    });
    this.cardType = new Wtf.form.TextField({
        name:"cardtype",
        maxLength:20,
        fieldLabel:"Card Type:<br><span CLASS=\"x-formsmaller-item\">(eg. Master Card)</font></span>",
        anchor: '90%'
    });

    this.refNo=new Wtf.form.NumberField({
        fieldLabel:'Reference Number*',
        name:"refno",
        maxLength:10,
        allowBlank:false,
        anchor:'90%',
        allowNegative:false
    });

    this.creditCard=new Wtf.form.FieldSet({
        title:'Card Details',
        id:this.id+'card',
        height:92,
        layout:'column',
        items:[{
            layout:'form',
            columnWidth:0.34,
            border:false,
            items:[this.refNo,this.cardNo]
           },{
            layout:'form',
            columnWidth:0.33,
            border:false,
            items:[this.nameOnCard,this.cardType]
           },{
            layout:'form',
            columnWidth:0.32,
            border:false,
            items:[this.expDate]
           }]
        });
//        this.creditCard.setVisible(false);
        this.cheque=new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.nee.43"),  //'Cheque Details',
            id:this.id+'cheque',
            bodyStyle:'padding:10px',
            height:90,
            layout:'column',
            defaults:{border:false},
            items:[{
                layout:'form',
                columnWidth:0.34,
                items:[this.checkNo]
               },{
                layout:'form',
                columnWidth:0.33,
                items:[this.bank]
               },{
                layout:'form',
                columnWidth:0.32,
                items:[this.description]
               }]
        });
//        this.cheque.setVisible(false);
    },
    ShowCheckDetails:function(type, parentObj){
        this.type = type;
        if(type==2){
//            this.cheque.setVisible(true);
//            this.creditCard.setVisible(false);
            this.expDate.setValue(new Date());
            this.checkNo.setValue(" ");
            this.cardType.setValue('0');
            this.refNo.setValue("1");
            this.bank.setValue("");
            this.nameOnCard.setValue("a");
        }else if(type==1){
//            this.cheque.setVisible(false);
//            this.creditCard.setVisible(true);
            this.expDate.setValue(new Date());
            this.refNo.setValue("");
            this.cardType.setValue('');
            this.checkNo.setValue("1");
            this.bank.setValue("a");
            this.nameOnCard.setValue("");
        }else{
//            this.cheque.setVisible(false);
//            this.creditCard.setVisible(false);
            this.refNo.setValue("1");
            this.cardType.setValue('0');
            this.expDate.setValue(new Date());
            this.bank.setValue("a");
            this.checkNo.setValue("1");
            this.nameOnCard.setValue("a");
        }
        this.doLayout();
    },
    GetPaymentFormData:function(){
        var bankname="";
        if(this.type==2){
            var bankindex=this.bankTypeStore.find("id",this.bank.getValue());
            bankname=bankindex<0?"":this.bankTypeStore.getAt(bankindex).data["name"];
        }
        var data="{}";
        switch(this.type){
            case 1:data="{refno:'"+this.refNo.getValue()+"',cardno:'"+this.cardNo.getValue()+"',nameoncard:'"+this.nameOnCard.getValue()+"',cardtype:'"+this.cardType.getValue()+"',expirydate:'"+WtfGlobal.convertToGenericDate(this.expDate.getValue())+"'}";
                break;
            case 2:data="{chequeno:'"+this.checkNo.getValue()+"',bankname:'"+(!this.isReceipt?this.bank.getValue():bankname)+"',bankmasteritemid:'"+(!this.isReceipt?null:this.bank.getValue())+"',description:"+encodeURI("'"+this.description.getValue()+"'")+"}";
                break;
        }
        return data;
    },

    addMaster:function(id,store){
        addMasterItemWindow(id);
        Wtf.getCmp('masterconfiguration').on('update', function(){
            store.reload();
        }, this);
    },
    checkExpDate:function(obj,nval,oval){
        if(nval<(new Date()))
            Wtf.MessageBox.confirm("Confirm","Card is expired. Do you wish to continue??",function(btn){
                if(btn!="yes") {obj.setValue(oval);return};
        },this)
    },
    setBankName:function(val){
        this.bank.setValue(val)
    }
});
