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
Wtf.account.ReceiptWindow = function(config){
    this.value="1",
    this.isReceipt=config.isReceipt;
    this.isCustBill=config.isCustBill;
    this.isAccPref=config.isAccPref||false;
    this.businessPerson=(config.isReceipt?"Customer":"Vendor");
    this.transectionName=config.isReceipt?"Receipt":"Payment";
    this.butnArr = new Array();
    this.butnArr.push({
        text: this.isAccPref?'Continue':WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: this.saveForm
    });

//    if(!this.isAccPref) {
//        this.butnArr.push({
//            text: 'Cancel',
//            scope: this,
//            handler:this.closeWin
//        });
//    }

    Wtf.apply(this,{
         buttons: this.butnArr
    },config);
    Wtf.account.ReceiptWindow.superclass.constructor.call(this, config);
     this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.ReceiptWindow, Wtf.Window, {

    onRender: function(config){
        Wtf.account.ReceiptWindow.superclass.onRender.call(this, config);
        this.createForm();
       var title=WtfGlobal.getLocaleText("acc.mp.payType");  //this.isAccPref?"Preferences":((this.personwin?"Account ":this.transectionName)+' Type');
       var msg=WtfGlobal.getLocaleText("acc.mp.sel");  //this.isAccPref?"Select Preferences":('Select '+(this.personwin?"Account ":this.transectionName)+' type.');
       var isgrid=(this.isAccPref ?true:false);
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.TypeForm
        });
    },


    createForm:function(){
        this.BillType= new Wtf.form.Checkbox({
            boxLabel:" ",
            width: 50,
            hidden:true,
            hideLabel:true,
            inputValue:'3',
            inputType:'radio',
            name:'rectype',
            fieldLabel:'Receive payment against Bill'
        })
        this.customerType= new Wtf.form.Checkbox({
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'2',
            width: 50,
            fieldLabel:this.isAccPref?"Accounting without inventory":this.isReceipt?(this.personwin?'Add an account':WtfGlobal.getLocaleText("acc.rp.recPayOt")):(this.personwin?'Add an account':WtfGlobal.getLocaleText("acc.mp.msg"))
        })
        this.accountType= new Wtf.form.Checkbox({
            boxLabel:" ",
            width: 50,
            inputType:'radio',
            inputValue:'1',
            name:'rectype',
            fieldLabel:this.isAccPref?"Accounting with inventory":this.isReceipt?(this.personwin?'Add a customer':WtfGlobal.getLocaleText("acc.rp.recPay")):(this.personwin?'Add a vendor':WtfGlobal.getLocaleText("acc.mp.msg1"))
        })
       this.TypeForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            labelWidth:245,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
             items:[this.accountType,this.customerType,this.BillType]
       });
       this.accountType.setValue(true);
   },

   saveForm:function(){        
       this.value=this.accountType.getValue()?(this.isCustBill?"3":"1"):"2";
        this.fireEvent('update',this,this.value);
        this.close();
    },
     closeWin:function(){ this.fireEvent('update',this,this.value);this.close();}
}); 

