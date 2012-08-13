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
Wtf.account.JournalEntryPanel=function(config){
    this.entryDate=new Wtf.form.DateField({
        fieldLabel: WtfGlobal.getLocaleText("acc.je.jeDate"),  //'Journal Date*',
        format:WtfGlobal.getOnlyDateFormat(),
        name: 'entrydate',
        anchor:'90%',
        value:Wtf.serverDate.clearTime(),
        allowBlank:false
    });
    this.jeNo=new Wtf.form.TextField({
        name:'entryno',
        scope:this,
        allowBlank:false ,
        maxLength:45,
        fieldLabel:WtfGlobal.getLocaleText("acc.je.jeNo"),  //'Journal Entry Number*',
        anchor:'85%'
    });

    chkFormCostCenterload();
    this.CostCenter= new Wtf.form.FnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.costCenter"),  //"Cost Center",
        hiddenName:"costcenter",
        store: Wtf.FormCostCenterStore,
        valueField:'id',
        displayField:'name',
        mode: 'local',
        typeAhead: true,
        forceSelection: true,
        selectOnFocus:true,
        anchor:'85%',
        triggerAction:'all',
        addNewFn:this.addCostCenter,
        scope:this
    });

    this.JournalNorthForm=new Wtf.form.FormPanel({
        cls:'northFormFormat',
        labelWidth:140,
        layout:'column',
        border:false,
        defaults:{border:false},
        itemCls:'JEntryform',
        items:[{
            layout:'form',
            columnWidth:0.34,
            items:[this.jeNo,this.CostCenter]
        },{
            layout:'form',
            columnWidth:0.34,
            items:this.entryDate
        },{
            layout:'form',
            columnWidth:0.3,
            items:this.Memo=new Wtf.form.TextArea({
                fieldLabel: WtfGlobal.getLocaleText("acc.je.memo"),  //'Memo',
                name: 'memo',
                anchor:'90%',
                heigth:20,
                maxLength:200,
                xtype:'textarea'
            })
        }]
    });  
    this.createGrid();
    Wtf.apply(this,{
        border:false,
        items:[{
            region : "north",
            height:100,
            border:false,
            items:[this.JournalNorthForm]
        },this.grid],
        bbar: [{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.save),
            handler: this.checkTotal.createDelegate(this)
        }]
    });
    this.grid.on('drtotal',this.updateDrTotal,this);
    this.grid.on('crtotal',this.updateCrTotal,this);
    Wtf.account.JournalEntryPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend(Wtf.account.JournalEntryPanel,Wtf.Panel,{
    onRender:function(config){
        this.setJENumber();
        Wtf.account.JournalEntryPanel.superclass.onRender.call(this,config);
        this.grid.on('render',this.addNewRow.createDelegate(this,[0]),this);
        this.grid.on('afteredit',this.updateRow,this);
        this.grid.on('beforeedit',this.checkEditable,this);

        this.accountStore.load();
        this.accountStore.on('load', function(){this.JournalNorthForm.doLayout();}, this);
    },
    checkTotal:function(){
        var dramount=this.calGridTotal('dramount');
        if(dramount!=this.calGridTotal('cramount')){
            WtfComMsgBox(25,2);
            return;
        }else if(dramount==0){
            WtfComMsgBox(26,2);
            return;
        }else
            this.saveJournal();                  
    },
    addCostCenter:function(){
        callCostCenter('addCostCenterWin');
    },
    saveJournal:function(){
        var lastrec=this.gridStore.getAt(this.gridStore.getCount()-1)
        if(lastrec.data["dramount"]>0 || lastrec.data["cramount"]>0){
            WtfComMsgBox(30,2);
            return;
        }
        if(this.JournalNorthForm.getForm().isValid()){
            var rec=this.JournalNorthForm.getForm().getValues();
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.je.msg1"),function(btn){
                if(btn!="yes") return;
                WtfComMsgBox(27,4,true);
                var rec=this.JournalNorthForm.getForm().getValues();
                rec.memo=this.Memo.getValue();
                rec.currencyid=WtfGlobal.getCurrencyID();
                rec.mode=53;
                rec.entrydate=WtfGlobal.convertToGenericDate(this.entryDate.getValue());
                rec.detail="["+this.getRecords().join(",")+"]";
                this.disable();
                 Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                    url:"ACCJournal/saveJournalEntry.do",
                    params: rec
                },this,this.genSuccessResponse,this.genFailureResponse);
            },this);
        }else{
                 WtfComMsgBox(2,2);
        }        
    },
    genSuccessResponse:function(response){
        this.enable();        
        if(response.success){
            this.fireEvent('update',this);
            this.resetAll();
        }
        this.setJENumber();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.je.tabTitle"),response.msg],response.success*2+1);
    },
    genFailureResponse:function(response){
        this.enable();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    resetAll:function(){
        this.JournalNorthForm.getForm().reset();
        this.grid.store.removeAll();
        this.addNewRow();
        this.Memo.setValue("");
    },
    createGrid:function(){
       this.Save=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            border:false,
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.save),
            handler: this.checkTotal.createDelegate(this)
       });       
       this.gridRec = Wtf.data.Record.create([
            {name:'debit',type:'boolean'},
            {name:'accountid'},
            {name:'description'},
            {name:'dramount',type:'float'},
            {name:'cramount',type:'float'}
        ]);
        this.gridStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.gridRec)
        });
       this.accRec = Wtf.data.Record.create([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'}
//            {name:'level',type:'int'}
        ]);
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccount/getAccountsForCombo.do",
//            url: Wtf.req.account+'CompanyManager.jsp',
            baseParams:{
                mode:2,
                deleted:false,
                nondeleted:true,
                ignoreAssets:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });       

        this.typeStore=new Wtf.data.SimpleStore({
            fields:[{name:"id"},{name:"name"}],
            data:[[true,"Debit"],[false,"Credit"]]
        });
        this.summary = new Wtf.ux.grid.GridSummary();
        this.grid = new Wtf.grid.EditorGridPanel({
            region :"center",
            layout:'fit',
            store:this.gridStore,
            cls:'gridFormat',
            clicksToEdit:1,
            stripeRows :true,
            viewConfig:{forceFit:true},
            plugins:[this.summary],
            columns:[{
                header:WtfGlobal.getLocaleText("acc.je.type"),  //"Type",
                editor: this.cmbType=new Wtf.form.ComboBox({
                    hiddenName:'debit',
                    store:this.typeStore,
                    valueField:'id',
                    displayField:'name',
                    mode: 'local',
                    triggerAction:'all',
                    forceSelection:true
                }),
                renderer:Wtf.comboBoxRenderer(this.cmbType),
                dataIndex:'debit'
            },{
                header:WtfGlobal.getLocaleText("acc.je.acc"),  //"Account",
                dataIndex:'accountid',
                editor: this.cmbAccount=new Wtf.form.FnComboBox({
                    hiddenName:'accountid',
                    store:this.accountStore,
                    valueField:'accountid',
                    displayField:'accountname',
                    forceSelection:true,
                    hirarchical:true
//                    addNewFn:this.openCOAWindow.createDelegate(this)
                }),
                renderer:Wtf.comboBoxRenderer(this.cmbAccount)
            },{
                header:WtfGlobal.getLocaleText("acc.je.debitAmt"),  //"Debit Amount",
                dataIndex:'dramount',
                align:'right',
                renderer:WtfGlobal.currencyRenderer,
                summaryType:'sum',
                summaryRenderer:WtfGlobal.currencySummaryRenderer,
                editor:new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative:false
                })
            },{
                header:WtfGlobal.getLocaleText("acc.je.creditAmt"),  //"Credit Amount",
                dataIndex:'cramount',
                align:'right',
                renderer:WtfGlobal.currencyRenderer,
                summaryType:'sum',
                summaryRenderer:WtfGlobal.currencySummaryRenderer,
                editor:new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative:false
                })           
            },{
                header:WtfGlobal.getLocaleText("acc.je.desc"),  //"Description",
                dataIndex:"description",
                editor:this.Description=new Wtf.form.TextArea({
                    maxLength:200,
                    allowBlank: false,
                    xtype:'textarea'
                })
            }]
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create))
            this.cmbAccount.addNewFn=this.openCOAWindow.createDelegate(this)
    },

    openCOAWindow:function(){
        this.grid.stopEditing();
        callCOAWindow(false, null, "coaWin");
        Wtf.getCmp("coaWin").on("update",function(){this.accountStore.reload()},this);
    },

    addNewRow:function(amount){
        for(var i=0;i<this.gridStore.getCount();i++){
            if(this.gridStore.getAt(i).data['accountid'].length<=0
                 ||!(this.gridStore.getAt(i).data['dramount']
                 ||this.gridStore.getAt(i).data['cramount']))
                return;
        }
        var rec = new this.gridRec({
            debit:amount>=0,
            accountid:'',
             description:'',
            dramount:0,//(amount>=0?amount:null),
            cramount:0//(amount<0?-amount:null)
        });
        this.gridStore.add(rec);
    },

    checkEditable:function(obj){
        if(obj.record.data['debit']){
             if(obj.field=='cramount') obj.cancel=true;
        }else{
             if(obj.field=='dramount') obj.cancel=true;
        }
    },

    calGridTotal:function(column){
        var total=0.0;
        for(var i=0;i<this.gridStore.getCount();i++){
            var temp=parseFloat(this.gridStore.getAt(i).data[column]);
            total=total+(isNaN(temp)?0:temp);
        }
        return total;
    },

    updateRow:function(obj){
        if(obj.field=="debit"){
            if(obj.value&&obj.record.data['cramount']!=null){
                obj.record.set('dramount',obj.record.data['cramount']);
                obj.record.set('cramount',null);
            }else if(!obj.value&&obj.record.data['dramount']!=null){
                obj.record.set('cramount',obj.record.data['dramount']);
                obj.record.set('dramount',null);
            }
        }
        var totaldr=this.calGridTotal('dramount');
        var totalcr=this.calGridTotal('cramount');
        this.addNewRow(totalcr-totaldr);
    },

    getRecords:function(){
        var data=[];
        var amount;
        for(var i=0;i<this.gridStore.getCount();i++){
            var rec=this.gridStore.getAt(i);
            if(rec.data['accountid'].length<=0||(rec.data['dramount']<=0&&rec.data['cramount']<=0))continue;
            if(rec.data['dramount'])
                amount=rec.data['dramount']
            else
                amount=rec.data['cramount']
            var desc = '';
            if(rec.data['description']!=''){
                desc = encodeURI(rec.data['description']);
            }
            data.push('{debit:"'+rec.data['debit']+'",accountid:"'+rec.data['accountid']+'",description:"'+desc+'",amount:'+amount+"}");
        }
        return data;
    },

    setJENumber:function(){
        if(Wtf.account.companyAccountPref.autojournalentry&&Wtf.account.companyAccountPref.autojournalentry.length>0){
            WtfGlobal.fetchAutoNumber(Wtf.autoNum.JournalEntry, function(resp){this.jeNo.setValue(resp.data)}, this);
        }
    }
});
