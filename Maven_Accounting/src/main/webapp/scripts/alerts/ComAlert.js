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
function WtfComMsgBox(choice, type,iswait,feature) {
    if(iswait==undefined || iswait==null)
        iswait=false;
    var strobj = [];
    var title="";
    switch (choice) {
        case 0:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.0")];
            break;
        case 1:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.1")];
            break;
        case 2:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.2")];
            break;
        case 3:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.3")];
            break;
        case 4:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.4")];
            break;
        case 5:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.5")];
            break;
        case 6:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.6")];
            break;
        case 7:
            strobj = [WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.msgbox.7")];
            break;
        case 8:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.8")];
            break;
        case 9:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.9")];
            break;
        case 10:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.10")];
            break;
        case 11:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.11")];
            break;
        case 12:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.12")];
            break;
        case 13:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.13")];
            break;
        case 14:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.14")];
            break;
        case 15:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.15")];
            break;
        case 16:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.16")];
            break;
        case 17:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.17")];
            break;
        case 18:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.18")];
            break;
        case 19:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.19")];
            break;
        case 20:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.20")];
            break;
        case 21:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.21")];
            break;
        case 22:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.22")];
            break;
        case 23:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.23")];
            break;
        case 24:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.24")];
            break;
        case 25:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.25")];
            break;
        case 26:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.26")];
            break;
        case 27:
            strobj = [WtfGlobal.getLocaleText("acc.common.load")];
            title=WtfGlobal.getLocaleText("acc.common.load1");
            break;
        case 28:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.28")];
            break;
        case 29:
            strobj = [WtfGlobal.getLocaleText("acc.common.load1")];
            title=WtfGlobal.getLocaleText("acc.common.load");
            break;
        case 30:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msgbox.30")];
            break;
        case 31:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.31")];
            break;
        case 32:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.32")];
            break;
        case 33:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msgbox.33")];
            break;
        case 34:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msgbox.34")];
            break;
        case 35:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.35")];
            break;
        case 36:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.36")];
            break;
        case 37:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.37")];
            break;
        case 38:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.38")];
            break;
        case 39:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msgbox.39")];
            break;
        case 40:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msgbox.40")];
            break;
        case 41:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.41")];
            break;
        case 42:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.42")];
            break;
        case 43:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.43")];
            break;
        case 44:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.44")];
            break;
        case 45:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.45")];
            break;        
        case 46:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msgbox.461")+" "+feature +WtfGlobal.getLocaleText("acc.msgbox.462")];
            break;
        case 47:
            strobj = [WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.47")];
            break;
        default:
            strobj = [choice[0], choice[1]];
            break;
    }


	var iconType = Wtf.MessageBox.INFO;

    if(type == 0)
        iconType = Wtf.MessageBox.INFO;
	else if(type == 1)
	    iconType = Wtf.MessageBox.ERROR;
    else if(type == 2)
        iconType = Wtf.MessageBox.WARNING;
    else if(type == 3)
        iconType = Wtf.MessageBox.INFO;

    if(iswait){
        Wtf.MessageBox.show({
           msg: strobj,
           width:320,
           wait:true,
           title:title,
           waitConfig: {interval:100}
        });
    }else{
        Wtf.MessageBox.show({
            title: strobj[0],
            msg: strobj[1],
            width:370,
            buttons: Wtf.MessageBox.OK,
            animEl: 'mb9',
            icon: iconType
        });
    }
}
