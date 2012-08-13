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


Wtf.account.MailWindow = function(config){
     this.rec=(config.rec==undefined?"":config.rec);
     this.userdata=null;
     this.tax1099=(config.tax1099==undefined?false:config.tax1099);
     this.data=this.rec.data;
     this.isinvoice=null;
     this.userrec=null;
     this.mode=null;
     this.label=(config.label==undefined?"":config.label);
     this.isQuotation=config.isQuotation;
     this.sendBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.MailWin.send")  //'Send'
    });
    this.sendBtn.on('click', this.handleSend, this);
     this.closeBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.close")  //'Close'
    });
    this.closeBtn.on('click', this.handleClose, this);

     Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.MailWin.sendMail"),  //"Send Mail",
        buttons: [this.sendBtn,this.closeBtn]
    },config);

    Wtf.account.MailWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.MailWindow, Wtf.Window, {
   getRecord:function(){
        Wtf.Ajax.requestEx({
//            url:Wtf.req.base+"UserManager.jsp",
            url:"ProfileHandler/getAllUserDetails.do",
            params:{
               mode:11,
               lid:Wtf.userid
            }
        },this,this.genSuccessResponse,this.genFailureResponse);

    },
    genSuccessResponse:function(response){
        this.userdata=response.data[0];
        var a='<br><br>Attach '+this.label+ ' PDF ';//this.sendCopy.show();
        if(this.userdata!=null&&this.userdata.emailid!=undefined&&this.userdata.emailid!=""){
            
            a='<span>Send me ({emailid})a copy</span>'+a;
        }else{this.sendCopy.hide();}
        this.tplSummary=new Wtf.XTemplate(a);
        this.userdata=response.data[0];
        if(this.tax1099){
            this.southTpl.hide();
//            this.sendCopy.setValue(false);
//            this.sendPdf.setValue(false);
//            this.sendCopy.hide();
//            this.sendPdf.hide();
            var m = Wtf.DomainPatt.exec(window.location);
            var paramstr="getTaxCode.jsp?personid="+this.data.personid;
            m=window.location.href.replace(m[0],paramstr)
            this.Subject.setValue(WtfGlobal.getLocaleText("acc.MailWin.taxID"));
            this.Message.setValue(WtfGlobal.getLocaleText("acc.MailWin.dear")+" "+this.data.personname+"<br/>"+
                "<br/>"+
                WtfGlobal.getLocaleText("acc.MailWin.taxID1")+" <br/>"+
                "<br/>"+
                "<a href="+m+">"+WtfGlobal.getLocaleText("acc.MailWin.taxID2")+"</a><br/>"+
                "<br/>"+
                WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                "<br/>"+
                WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                "<br/>"+
                companyName+"  "+WtfGlobal.getLocaleText("acc.MailWin.taxID5")+" <br/>");
        }else{
            if(this.isinvoice){
                 this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(this.southTpl.body,{emailid:this.userdata.emailid});
                    this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg2")+" "+this.rec.data.duedate.format("d-m-Y")+".<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+Wtf.account.companyAccountPref.companyEmailId+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg7")+":"+this.data.billno+"");
             } else if(this.isQuotation){
                 this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                 this.tplSummary.overwrite(this.southTpl.body,{emailid:this.userdata.emailid});
                     this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg8")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg9")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+Wtf.account.companyAccountPref.companyEmailId+". <br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg10")+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                     "<br/>"+
                     this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg12")+":"+this.data.billno+"");
              }else {
                    this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                    this.tplSummary.overwrite(this.southTpl.body,{emailid:this.userdata.emailid});
                    this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg13")+" "+this.rec.data.billdate.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+Wtf.account.companyAccountPref.companyEmailId+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg5")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg7")+":"+this.data.billno+"");
             }
        }
    }, 
 
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],1);
    },
    onRender: function(config){
        this.createForm();
        this.getRecord();
        var image="../../images/accounting_image/bank-reconciliation.jpg";


        this.add({
//            region: 'north',
//            height:75,
//            border: false,
//            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
//            html:getTopHtml('Send Mail','Send Mail',image,false)
//        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.Form
        },{
            region: 'south',
            hidden:this.tax1099,
            border: false,
            height:70,//(Wtf.isIE?(this.tax1099?0:53):(this.tax1099?0:70)),
            autoScroll:true,
            baseCls:'bckgroundcolor',
            style: 'padding:0px 0px 0px 10px;',
            layout: 'border',
            items: [{
                region: 'west',
                border: false,
                width:40,
                layout: 'border',
                items:[{
                    region: 'center',
                    border: false,
                    baseCls:'bckgroundcolor',
                    layout: 'fit',
                    items: this.sendCopy= new Wtf.form.Checkbox({
                        name:'emailcopy',
                        checked:!this.tax1099,
                        width: 20
                    })
                },{
                    layout:'fit',
                    height:45,
                    region:'south',
                    baseCls:'bckgroundcolor',          
                    border:false,
                    items:this.sendpdf= new Wtf.form.Checkbox({
                        name:'sendpdf',
                        checked:!this.tax1099,
                        style: 'padding:0px 0px 10px 0px;',
                        width: 20
                    })
                }]
            },
            this.southTpl]
        });
        Wtf.account.ReconciliationWindow.superclass.onRender.call(this, config);                               
    },
    
    createForm:function(){
         this.southTpl=new Wtf.Panel({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            style: 'padding-top:8px;',
            layout: 'fit'
         })
         this.Rec = Wtf.data.Record.create ([
            {name: 'userid'},
            {name: 'username',mapping:'accname'},
            {name: "fullname",mapping:'accname'},
            {name: "emailid",mapping:'email'},
            {name: 'image',mapping:''},
            {name:'accid'},
            {name:'openbalance'},
            {name:'id'},
            {name:'title'},
            {name:'accname'},
            {name:'address'},
            {name:'company'},
            {name:'email'},
            {name:'contactno'},
            {name:'contactno2'},
            {name:'fax'},
            {name:'shippingaddress'},
            {name:'pdm'},
            {name:'pdmname'},
            {name:'parentid'},
            {name:'parentname'},
            {name:'bankaccountno'},
            {name:'termid'},
            {name:'termname'},
            {name:'other'},
            {name: 'leaf'},
            {name: 'currencysymbol'},
            {name: 'currencyname'},
            {name: 'currencyid'},
            {name: 'deleted'},
            {name: 'creationDate' ,type:'date'},
            {name: 'level'}
        ]);
        this.url=this.tax1099?"ACCVendor/getVendors.do":"ACCCustomer/getCustomers.do"
        this.baseParams=this.tax1099?{
                deleted:false,
                nondeleted:true
            }:{
                mode:2,
                group:[10]//:[13])
            }
        this.contactStore = new Wtf.data.Store({
            //this.businessPerson+
    //        url:Wtf.req.account+this.businessPerson+'Manager.jsp',
            url:this.url,
            baseParams:this.baseParams,
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalcount",
                root: "data"
            },this.Rec)
        });
        this.resultTpl = new Wtf.XTemplate(
            '<tpl for="."><div class="search-item">',
                '<img src="{[this.f(values)]}">',
                '<div><h3><span>{fullname} - ({username})</span></h3><br>',
                '<div class="search-item-email">{emailid}</div></div>',
            '</div></tpl>', {
            f: function(val){
                if(val.image == "")
                    val.image = "../../images/user100.png";
                return val.image;
            },
            scope: this
        });

        this.To = new Wtf.form.ComboBox({
            store: this.contactStore,
            name:"to",
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to")+"*",
         //   defaultValue:this.data.personemail,
            emptyText: WtfGlobal.getLocaleText("acc.MAilWin.msg19"),  //'Type user name and select one from the list',
            tabIndex:1,
            cls: 'search-username-combo',
            displayField: 'emailid',
            typeAhead: false,
            loadingText: WtfGlobal.getLocaleText("acc.MAilWin.search"),  //'Searching...',
            pageSize:3,
            anchor:'95%',
            hideTrigger:true,
            tpl: this.resultTpl,
            itemSelector: 'div.search-item',
            minChars: 1,
            onSelect: function(record){
              // override default onSelect to do redirect
                var v = this.getValue().toString();
                if(v.indexOf(record.data['emailid']) == -1){
                    if(v.charAt(v.length) == ';')
                        this.setValue(v+record.data['emailid'] + ';');
                    else{
                        var temp = '';
                        if(v.indexOf(';') !== -1)
                            temp = v.substring(0, v.lastIndexOf(';')+1);
                        else
                            temp = '';
                        this.reset();
                        this.setValue(temp + record.data['emailid'] + ';');
                    }
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.MailWin.msg15")], 1);
                }
                this.focus();
            }
        });
        if(this.data.personemail!=undefined||this.data.personemail!=""||this.data.personemail!=null)
        this.To.setValue(this.data.personemail+";")
        this.To.on('beforequery', function(q){
            var qt = q.query.trim();
            var curr_q = qt.substr(qt.lastIndexOf(';')+1);
            curr_q = WtfGlobal.HTMLStripper(curr_q);
            q.query = curr_q;
        }, this)
        this.Subject=new Wtf.form.TextField({
            name:"subject",
            allowBlank:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.MAilWin.sub"),  //"Subject",
            maxLength:100,
            anchor:'95%'
        });
        this.Message=new Wtf.newHTMLEditor({
            name:"message",
            allowBlank:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.MAilWin.message") + "*",  //"Message*",
            xtype:'htmleditor',
            id:'bio',
            anchor:'95%',

            height: 240,
            border: false,
            enableLists: false,
            enableSourceEdit: false,
            enableAlignments: true,
            hideLabel: true
         });
         this.Form=new Wtf.form.FormPanel({
            region:'north',
            autoScroll:true,
            height:(Wtf.isIE?(this.tax1099?512:455):(this.tax1099?495:425)),
            border:false,
            items:[{
                layout:'form',
                bodyStyle: "background: transparent; padding: 20px;",
                labelWidth:60,
                border:false,
                items:[this.To,{
                            border:false,
                            xtype:'panel',
                            
                            bodyStyle:'padding:0px 0px 10px 65px;',
                            html:'<font color="#555555">'+ WtfGlobal.getLocaleText("acc.MailWin.msg16") +'</font>'
                        },this.Subject,{bodyStyle:'padding:0px 0px 15px 0px;',xtype:'panel',border:false},this.Message] 
            }]
        });
    },
    saveData:function(){ 
        if(!this.Form.getForm().isValid())
                WtfComMsgBox(2,2);
        else{
            var rec=this.Form.getForm().getValues();
            callReconciliationLedger(rec)
            this.close();
        }
    },
     handleClose:function(){
         this.fireEvent('cancel',this)
         this.close();
     },
     handleSend: function(bobj, edfd){
        //FIXME: msg sending problem from saved drafts
        this.sendBtn.disable();
        this.closeBtn.disable();

        if(!(this.To.isValid())||this.To.getValue().trim()==""){          
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.MAilWin.msg17")],2);
                //Wtf.Msg.alert('Alert', 'Please specify atleast one recipient.'2);
                this.sendBtn.enable();
                this.closeBtn.enable();
                return ;
        }

        if(this.Subject.getValue().trim()==""){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.MAilWin.msg18"),
                function(btn){
                  if (btn == "yes") {
                      this.sendMail();
                  }
                  else {
                    this.sendBtn.enable();
                    this.closeBtn.enable();
                    return false;
                  }
                }, this);
        } else this.sendMail();
    },
    sendMail:function(){
        var email="";
        if(this.sendCopy.checked == true){
            email+=this.userdata.emailid+";";
        }

        email+=this.To.getValue().trim();

        var msg = this.Message.getValue();
        msg = msg.replace(/<STRONG>/gi,"<b>");
        msg = msg.replace(/<\/STRONG>/gi,"</b>");
        msg = msg.replace(/<em>/gi,"<i>");
        msg = msg.replace(/<\/em>/gi,"</i>");
        var rec=this.Form.getForm().getValues();
        rec.mode=this.mode;
        rec.billid=this.data.billid;
        rec.mailingDate=WtfGlobal.convertToGenericDate(new Date());
        rec.emailid=email;
        rec.amount=this.data.amount;
        rec.currencyid=this.data.currencyid;
        rec.personid=this.data.personid
        rec.sendpdf=this.tax1099?false:this.sendpdf.getValue();
        Wtf.Ajax.requestEx({
            url:"CommonFunctions/sendMail.do",
//                    url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            params: rec
        },this,this.success,this.failure);
     }, 

    success:function(response){
        if(response.success){
            WtfComMsgBox([this.label,response.msg],3);
             if(this.tax1099){
                var rec=[];
                rec.accid=this.data.personid;
                rec.taxidmailon=WtfGlobal.convertToGenericDate(new Date());;
                Wtf.Ajax.requestEx({
                    url:"ACCVendor/saveVendorMailingDate.do",
//                  url:Wtf.req.account+this.businessPerson+'Manager.jsp',
                    params: rec
                },this,this.mailSuccessResponse,this.mailFailureResponse);
             }
           this.handleClose();
         } else {
        	 WtfComMsgBox([this.label,WtfGlobal.getLocaleText("acc.rem.210")],3);
        	 this.sendBtn.enable();
             this.closeBtn.enable();
         }
    },

    failure:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.handleClose();
    },
    mailSuccessResponse:function(response){
        this.close();
    },
    mailFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
});
