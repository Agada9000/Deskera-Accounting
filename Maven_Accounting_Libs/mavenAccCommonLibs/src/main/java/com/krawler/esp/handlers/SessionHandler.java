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
package com.krawler.esp.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

public class SessionHandler {
	public SessionHandler() {
	}

	public static boolean isValidSession(HttpServletRequest request,
			HttpServletResponse response) {
		boolean bSuccess = false;
		try {
			if (request.getSession().getAttribute("initialized") != null) {
				bSuccess = true;
			}
		} catch (Exception ex) {
		}
		return bSuccess;
	}

	static void updatePreferences(HttpServletRequest request,
			String currencyid, String dateformatid, String timezoneid,
			String tzdiff) {
		if (currencyid != null)
			request.getSession().setAttribute("currencyid", currencyid);
		if (timezoneid != null) {
			request.getSession().setAttribute("timezoneid", timezoneid);
			request.getSession().setAttribute("tzdiff", tzdiff);
		}
		if (dateformatid != null)
			request.getSession().setAttribute("dateformatid", dateformatid);
	}

	public boolean validateSession(HttpServletRequest request,
			HttpServletResponse response) {
		return SessionHandler.isValidSession(request, response);
	}

	public void createUserSession(HttpServletRequest request, JSONObject jObj) {
		if (!request.getSession().isNew()) {
			request.getSession().invalidate();
			request.getSession(true);
		}
		try {
			HttpSession session = request.getSession(true);
			session.setAttribute("username", jObj.getString("username"));
			session.setAttribute("userid", jObj.getString("lid"));
			session.setAttribute("companyid", jObj.getString("companyid"));
			session.setAttribute("company", jObj.getString("company"));
			session.setAttribute("timezoneid", jObj.getString("timezoneid"));
			session.setAttribute("tzdiff", jObj.getString("tzdiff"));
			session
					.setAttribute("dateformatid", jObj
							.getString("dateformatid"));
			session.setAttribute("currencyid", jObj.getString("currencyid"));
			session.setAttribute("initialized", "true");
		} catch (JSONException e) {
		}
	}

	public void destroyUserSession(HttpServletRequest request,
			HttpServletResponse response) {
		request.getSession().invalidate();
	}
}
