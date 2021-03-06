package com.etisalat.esdp.admin.controller.motcy;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import eg.java.net.web.jspx.ui.controls.WebControl;
import eg.java.net.web.jspx.ui.pages.MasterPage;

public class Master extends MasterPage
{


	public String getUserName()
	{
		if (request.getUserPrincipal() == null)
			return "Anonymous";
		return request.getUserPrincipal().getName();
	}
	
	public String getYear()
	{
		return String.valueOf(new GregorianCalendar().get(Calendar.YEAR));
	}
	public void changeLanguage(WebControl sender, String arguments)
	{
		if (getLocale() == null || getLocale().getLanguage().startsWith("en"))
			setLocale(new Locale("ar"));
		else
			setLocale(new Locale("en"));
		localizeMasterPage();
	}

	public String getLang()
	{
		if (getLocale().getLanguage().startsWith("en"))
			return "عربي";
		return "English";
	}
	
	public String getDir()
	{
		if (getLocale().getLanguage().startsWith("en"))
			return "ltr";
		return "rtl";
	}
}
