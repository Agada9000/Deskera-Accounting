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
Wtf.account.ClosablePanel=function(config){
    Wtf.account.ClosablePanel.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.ClosablePanel,Wtf.Panel,{
    closeMsg:WtfGlobal.getLocaleText("acc.msgbox.51"),  //"The data you filled is unsaved.Do you still want to close the panel?",

    isClosable:true,
    initComponent:function(config){
        Wtf.account.ClosablePanel.superclass.initComponent.call(this,config);
        this.on('beforeclose', this.askToClose,this);
    },
    
    askToClose:function(){
        if(this.isClosable!==true){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                msg: WtfGlobal.getLocaleText("acc.msgbox.51"),  //this.closeMsg,
               // width:500,
                buttons: Wtf.MessageBox.YESNO,
                animEl: 'mb9',
                fn:function(btn){
                    if(btn!="yes")return;
                    this.ownerCt.remove(this);
                },
                scope:this,
                icon: Wtf.MessageBox.QUESTION
            });
        }
        return this.isClosable;
    }
});
