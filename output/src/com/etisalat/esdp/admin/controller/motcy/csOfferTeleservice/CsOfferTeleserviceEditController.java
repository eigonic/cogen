package com.etisalat.esdp.admin.controller.motcy.csOfferTeleservice;

import ae.etisalat.motcy.model.CsOfferTeleservice;

import eg.java.net.web.jspx.engine.annotation.JspxBean;
import eg.java.net.web.jspx.engine.util.StringUtility;
import eg.java.net.web.jspx.ui.controls.WebControl;
import eg.java.net.web.jspx.ui.pages.ContentPage;

public class CsOfferTeleserviceEditController extends ContentPage
{
	
	boolean isEdit;
	
	@Override
	protected void pagePreLoad()
	{
		isEdit = !StringUtility.isNullOrEmpty(request.getParameter("id"));
	}
	
	protected void pageLoaded()
	{
	}

	public boolean isEdit() {
		return isEdit;
	}
	
	@JspxBean(name="csOfferTeleservice" , dateformat="dd/MM/yyyy HH:mm:ss", scope = JspxBean.CONVERSATION)
CsOfferTeleservice csOfferTeleservice = new CsOfferTeleservice();
public void saveBean(WebControl invoker, String args)
{
}


}
