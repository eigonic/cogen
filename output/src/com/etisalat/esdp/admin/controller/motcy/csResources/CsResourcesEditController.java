package com.etisalat.esdp.admin.controller.motcy.csResources;

import ae.etisalat.motcy.model.CsResources;

import eg.java.net.web.jspx.engine.annotation.JspxBean;
import eg.java.net.web.jspx.engine.util.StringUtility;
import eg.java.net.web.jspx.ui.controls.WebControl;
import eg.java.net.web.jspx.ui.pages.ContentPage;

public class CsResourcesEditController extends ContentPage
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
	
	@JspxBean(name="csResources" , dateformat="dd/MM/yyyy HH:mm:ss", scope = JspxBean.CONVERSATION)
CsResources csResources = new CsResources();
public void saveBean(WebControl invoker, String args)
{
}


}
