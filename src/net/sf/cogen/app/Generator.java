package net.sf.cogen.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eg.java.net.web.jspx.ui.controls.WebControl;
import eg.java.net.web.jspx.ui.controls.html.GenericWebControl;
import eg.java.net.web.jspx.ui.controls.html.StringLiteral;
import eg.java.net.web.jspx.ui.controls.html.elements.Calendar;
import eg.java.net.web.jspx.ui.controls.html.elements.HyperLink;
import eg.java.net.web.jspx.ui.controls.html.elements.Label;
import eg.java.net.web.jspx.ui.controls.html.elements.LinkCommand;
import eg.java.net.web.jspx.ui.controls.html.elements.Panel;
import eg.java.net.web.jspx.ui.controls.html.elements.ajax.AjaxPanel;
import eg.java.net.web.jspx.ui.controls.html.elements.dataitem.DataColumn;
import eg.java.net.web.jspx.ui.controls.html.elements.dataitem.DataColumnCommand;
import eg.java.net.web.jspx.ui.controls.html.elements.dataitem.DataLookup;
import eg.java.net.web.jspx.ui.controls.html.elements.dataitem.DataPK;
import eg.java.net.web.jspx.ui.controls.html.elements.dataitem.DataParam;
import eg.java.net.web.jspx.ui.controls.html.elements.dataitem.DataTable;
import eg.java.net.web.jspx.ui.controls.html.elements.dataitem.ExportToExcel;
import eg.java.net.web.jspx.ui.controls.html.elements.dataitem.ItemTemplate;
import eg.java.net.web.jspx.ui.controls.html.elements.inputs.FileUpload;
import eg.java.net.web.jspx.ui.controls.html.elements.inputs.TextBox;
import eg.java.net.web.jspx.ui.controls.html.elements.select.Option;
import eg.java.net.web.jspx.ui.controls.html.elements.select.Select;
import net.sf.cogen.Main;
import net.sf.cogen.model.DB;
import net.sf.cogen.model.DBColumn;
import net.sf.cogen.model.DBFK;
import net.sf.cogen.model.DBTable;
import net.sf.cogen.model.Pair;

public class Generator
{
	private static final Logger logger = LoggerFactory.getLogger(Generator.class);

	private static TreeMap<String, Pair> bundle = new TreeMap<String, Pair>();
	private static TreeMap<String, Pair> sql = new TreeMap<String, Pair>();
	private static TreeMap<String, Pair> sqlProp = new TreeMap<String, Pair>();

	private static final String VIEW = "View";
	private static final String SEARCH = "Search";
	private static final String EDIT = "Edit";

	private static final int NO_FORMAT = 0;
	private static final int JAVA_FORMAT = 1;
	private static final int XML_FORMAT = 2;

	private String modelPackage;

	public void generateArtifacts(DB db)
	{
		long start = System.currentTimeMillis();
		/**
		 * 1. generate sql file
		 * 2. generate properties files
		 * 3. generate jspx pages.
		 * 	master/content
		 * 4. generate controllers.
		 */
		modelPackage = db.getModelPackage();
		generateMasterPage();
		generateMasterPageController();

		for (DBTable dbTable : db.getDbTables())
		{
			generateContentPage(dbTable, VIEW);
			generateContentPageController(dbTable, VIEW);
			generateContentPage(dbTable, SEARCH);
			generateContentPageController(dbTable, SEARCH);
			generateContentPage(dbTable, EDIT);
			generateContentPageController(dbTable, EDIT);
		}
		generateSqlFile();
		generateBundleFile();
		logger.info("Done in " + (System.currentTimeMillis() - start));
	}

	protected void generateOneArtifact(String srcFileName, String dstFolder, String dstFileName, String artifactName, String content, String beanName,
			int formatType)
	{
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		FileOutputStream fos = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream(srcFileName)));
			String temp = null;
			while ((temp = br.readLine()) != null)
				sb.append(temp).append("\r\n");

			String artifactString = sb.toString();
			artifactString = artifactString.replace(Constants.ENCODING, Settings.ENCODING);
			artifactString = artifactString.replace(Constants.SQL_FILE, Settings.SQL_NAME);
			// [Aug 15, 2013 6:14:37 PM] [amr.eladawy] [generate the controller in a package with the bean name.]
			artifactString = artifactString.replace(Constants.TOP_LEVEL_PACKAGE, Settings.TOP_LEVEL_PACKAGE
					+ (beanName.length() > 0 ? ("." + beanName.substring(0, 1).toLowerCase() + beanName.substring(1)) : ""));
			artifactString = artifactString.replace(Constants.PAGE_CONTROLLER, artifactName);
			artifactString = artifactString.replace(Constants.RES_FOLDER, Settings.RES_FOLDER);
			artifactString = artifactString.replace(Constants.MASTER_PAGE_NAME,
					"/" + Settings.PAGES_FOLDER.replace(File.separator, "/") + "/" + Settings.MASTER_PAGE_NAME);
			artifactString = artifactString.replace(Constants.RES_FOLDER, Settings.RES_FOLDER);
			artifactString = artifactString.replace(Constants.FORM_ACTION,
					"/" + Settings.PAGES_FOLDER.replace(File.separator, "/") + "/" + beanName + "/" + dstFileName);
			artifactString = artifactString.replace(Constants.FORM_CONTENT, content);
			artifactString = artifactString.replace(Constants.MODEL_PACKAGE, modelPackage);
			artifactString = artifactString.replace(Constants.MODEL_CLASS, beanName);
			artifactString = artifactString.replace(Constants.MODEL_BEAN, StringUtil.createInstanceName(beanName));
			if (formatType == XML_FORMAT)
				artifactString = StringUtil.formateXML(artifactString);

			File artifactFolder = new File(dstFolder);
			if (!artifactFolder.exists())
				artifactFolder.mkdirs();
			File artifactFile = new File(artifactFolder, dstFileName);
			if (!artifactFile.exists())
				artifactFile.createNewFile();
			fos = new FileOutputStream(artifactFile);
			fos.write(artifactString.getBytes());
		}
		catch (Exception e)
		{
			logger.error("generateOneArtifact(srcFileName=" + srcFileName + ", dstFolder=" + dstFolder + ", dstFileName=" + dstFileName
					+ ", artifactName=" + artifactName + ", content=" + content + ", beanName=" + beanName + ")", e);
		}

		try
		{
			if (fos != null)
				fos.close();
		}
		catch (Exception e2)
		{
		}
		try
		{
			if (br != null)
				br.close();
		}
		catch (Exception e2)
		{
		}
	}

	protected void generateMasterPage()
	{
		generateOneArtifact("/res/master.html",
				Settings.TARGET_FOLDER + File.separator + Settings.WEB_ROOT_FOLDER + File.separator + Settings.PAGES_FOLDER + File.separator,
				Settings.MASTER_PAGE_NAME, "", "", "", XML_FORMAT);

	}

	protected void generateMasterPageController()
	{
		generateOneArtifact("/res/Master.txt", Settings.TARGET_FOLDER + File.separator + Settings.SRC_FOLDER + File.separator
				+ Settings.TOP_LEVEL_PACKAGE.replace('.', File.separatorChar) + File.separator, "Master.java", "", "", "", JAVA_FORMAT);
	}

	/**
	 * generates one Jspx page
	 * @param dbTable the DB table that this page is working on
	 * @param type the type of operation to be done against the DB Table (Search, View, Edit)
	 */
	protected void generateContentPage(DBTable dbTable, String type)
	{
		String beanName = dbTable.getPropertyName();
		String pageName = beanName + type + "." + Settings.JSPX_EXT;
		String className = dbTable.getClassName() + type + "Controller";

		String formContent = generateForm(dbTable, pageName, type);

		generateOneArtifact("/res/content.html", Settings.TARGET_FOLDER + File.separator + Settings.WEB_ROOT_FOLDER + File.separator
				+ Settings.PAGES_FOLDER + File.separator + beanName + File.separator, pageName, className, formContent, beanName, XML_FORMAT);
	}

	protected void generateContentPageController(DBTable dbTable, String type)
	{
		String beanName = dbTable.getPropertyName();
		String beanClassName = dbTable.getClassName();
		String formContent = generateController(dbTable, type, beanClassName);

		String className = dbTable.getClassName() + type + "Controller";

		if (type == VIEW)
			generateOneArtifact("/res/ContentView.txt",
					Settings.TARGET_FOLDER + File.separator + Settings.SRC_FOLDER + File.separator
							+ Settings.TOP_LEVEL_PACKAGE.replace('.', File.separatorChar) + File.separator + beanName + File.separator,
					className + ".java", className, formContent, beanClassName, JAVA_FORMAT);
		else
			generateOneArtifact("/res/Content.txt",
					Settings.TARGET_FOLDER + File.separator + Settings.SRC_FOLDER + File.separator
							+ Settings.TOP_LEVEL_PACKAGE.replace('.', File.separatorChar) + File.separator + beanName + File.separator,
					className + ".java", className, formContent, beanClassName, JAVA_FORMAT);
	}

	private String generateController(DBTable dbTable, String type, String beanClassName)
	{
		String beanName = dbTable.getPropertyName();
		StringBuilder sb = new StringBuilder();
		sb.append("@JspxBean(name=\"").append(beanName).append("\" , dateformat=\"dd/MM/yyyy HH:mm:ss\", scope = JspxBean.CONVERSATION)\r\n")
				.append(beanClassName).append(" ").append(beanName).append(" = new ").append(beanClassName).append("();\r\n");
		if (type == VIEW)
			sb.append("public void ").append(Constants.DELETE_ACTION).append("(WebControl invoker, String args)\r\n{\r\n}\r\n");
		if (type == EDIT)
			sb.append("public void ").append(Constants.SAVE_ACTION).append("(WebControl invoker, String args)\r\n{\r\n}\r\n\r\n");
		return sb.toString();
	}

	protected String generateForm(DBTable dbTable, String pageName, String type)
	{
		String beanName = dbTable.getPropertyName();

		AjaxPanel ajaxPanel = new AjaxPanel();
		//		FieldSet fieldSet = new FieldSet();
		Panel rowDiv = new Panel();
		rowDiv.setCssClass(Constants.CSS_ROW);
		for (DBColumn column : dbTable.getColumns())
		{
			// [Jul 18, 2012 1:03:42 PM] [amr.eladawy] [dont add file upload in search]
			if (type == SEARCH && column.getType().toLowerCase().contains("lob"))
				continue;
			String colProperty = column.getPropertyName();
			String colId = StringUtil.getColumnWebControlId(column.getName());
			String colEL = StringUtil.createBeanEL(beanName, colProperty);
			Panel span6 = new Panel();
			span6.setCssClass(Constants.CSS_SPAN4);
			Panel div = new Panel();
			div.setCssClass(Constants.CSS_CONTROL_GROUP);
			div.addControl(createLabel(getBundleEL(column.getName()), colId));

			Panel contDiv = new Panel();
			contDiv.setCssClass(Constants.CSS_CONTROLS);
			// [Jul 17, 2012 7:26:38 PM] [amr.eladawy] [create the input/control]
			if (type == VIEW)
				contDiv.addControl(createStringLiteral(colEL));
			else if (type == EDIT || type == SEARCH)
				contDiv.addControl(createInput(colEL, column, dbTable.getFks().get(column.getName()), colId, type));

			div.addControl(contDiv);
			span6.addControl(div);

			rowDiv.addControl(span6);

		}
		ajaxPanel.addControl(rowDiv);
		Panel action = new Panel();
		action.setCssClass(Constants.CSS_FORM_ACTION);
		if (type == VIEW)
		{
			// [Jul 18, 2012 3:26:39 AM] [amr.eladawy] [add EDIT button]
			HyperLink link = new HyperLink();
			link.setCssClass(Constants.CSS_BTN_PRIM + " " + Constants.CSS_BTN);
			if (dbTable.getPks().size() != 0)
			{
				String pkProp = dbTable.getPks().get(0).getPropertyName();
				String editUrl = new StringBuffer().append("/").append(Settings.PAGES_FOLDER.replace(File.separator, "/")).append("/")
						.append(dbTable.getPropertyName() + "/" + dbTable.getPropertyName() + EDIT + "." + Settings.JSPX_EXT).append("?")
						.append(pkProp).append("=").append(StringUtil.createBeanEL(beanName, pkProp)).toString();
				GenericWebControl i = new GenericWebControl("i");
				i.setCssClass("icon-pencil icon-white");
				link.addControl(i);
				link.addControl(new StringLiteral(StringUtil.createBundleEL(Constants.EDIT_CAPTION)));
				link.setNavigationURL(editUrl);
			}
			//			link.setText(StringUtil.createBundleEL(Constants.EDIT_CAPTION));
			action.addControl(link);

			// [Jul 18, 2012 3:26:46 AM] [amr.eladawy] [add DELETE button]
			LinkCommand linkCommand = new LinkCommand();
			linkCommand.setCssClass(Constants.CSS_BTN_RED + " " + Constants.CSS_BTN);
			linkCommand.setConfirmation(getBundleEL(Constants.CONFIRM_DELETE));
			linkCommand.setOnServerClick(Constants.DELETE_ACTION);
			StringLiteral text = new StringLiteral(" " + StringUtil.createBundleEL(Constants.DELETE_CAPTION));
			GenericWebControl i = new GenericWebControl("i");
			i.setCssClass("icon-trash icon-white");
			linkCommand.addControl(i);
			linkCommand.addControl(text);
			action.addControl(linkCommand);

		}
		else
		{
			LinkCommand linkCommand = new LinkCommand();
			linkCommand.setCssClass(Constants.CSS_BTN_PRIM + " " + Constants.CSS_BTN);
			linkCommand.setAttributeValue("data-loading-text", "...");
			linkCommand.setAttributeValue("autocomplete", "off");
			String title = "";
			if (type == EDIT)
			{
				title = StringUtil.createBundleEL(Constants.SAVE_CAPTION);
				linkCommand.setOnServerClick(Constants.SAVE_ACTION);
			}
			else if (type == SEARCH)
			{
				title = StringUtil.createBundleEL(Constants.SEARCH_CAPTION);
				linkCommand.setOnServerClick(Constants.SEARCH_ACTION);
			}
			linkCommand.addControl(new StringLiteral(title));
			action.addControl(linkCommand);
			if (type == SEARCH)
			{
				action.addControl(new GenericWebControl("hr"));
			}
		}

		ajaxPanel.addControl(action);
		if (type == SEARCH)
			ajaxPanel.addControl(createDataTable(dbTable));

		//		ajaxPanel.addControl(fieldSet);

		return ajaxPanel.jspxString();
	}

	private DataTable createDataTable(DBTable dbTable)
	{
		String tableCodeName = dbTable.getPropertyName();
		DataTable table = new DataTable();
		table.setcaseSenstive(true);
		table.setDataSource(StringUtil.createSqlEL(Constants.DATA_SOURCE));
		table.setTableName(dbTable.getName());

		ArrayList<DataParam> params = new ArrayList<DataParam>();
		ArrayList<DataColumn> dcols = new ArrayList<DataColumn>();
		ArrayList<DataPK> pks = new ArrayList<DataPK>();
		ArrayList<DataLookup> dls = new ArrayList<DataLookup>();
		String tableAlias = new String(new char[]
		{ dbTable.getName().charAt(0) });
		StringBuilder sqlString = new StringBuilder("SELECT ").append(tableAlias).append(".* FROM ").append(dbTable.getName()).append(" ")
				.append(tableAlias).append(" WHERE ");
		for (DBColumn column : dbTable.getColumns())
		{
			if (column.isPrimaryKey())
			{
				DataPK dp = new DataPK();
				dp.setName(column.getName());
				pks.add(dp);
			}

			DataColumn dc = new DataColumn();
			StringBuilder expression = new StringBuilder("#");
			expression.append(tableAlias).append(".");
			expression.append(column.getName());
			if (StringUtil.isNumber(column.getType()) || StringUtil.isDateTime(column.getType()))
			{
				dc.setType("numeric");
				expression.append(" = ").append(" ?").toString();
			}
			else if (StringUtil.isString(column.getType()))
			{
				dc.setType("string");
				expression.append(" LIKE ").append(" '%?%'").toString();
			}
			if (dbTable.getFks().containsKey(column.getName()))
			{
				DBFK fk = dbTable.getFks().get(column.getName());
				dc.setType("lookup");
				DataLookup dl = new DataLookup();
				String dlName = column.getName() + "_FKLP";
				dl.setName(dlName);
				dl.setKey(fk.getForeign());
				String sqlKey = fk.getForeignTable() + "_LOOKUP";
				dl.setSql(StringUtil.createSqlEL(sqlKey));
				dls.add(dl);
				dc.setFK(dlName);
			}
			expression.append("#");

			DataParam dp = new DataParam();
			String key = dbTable.getName() + "_" + column.getName();
			sqlProp.put(key, new Pair(key, expression.toString()).setFormatSql(true));
			dp.setExpression(StringUtil.createSqlEL(key));
			dp.setControl(StringUtil.getColumnWebControlId(column.getName()));
			//			if (StringUtil.isString(column.getType()))
			//				dp.setName(new StringBuilder(column.getName()).append("_VALUE").toString());

			dc.setName(column.getName());
			if (StringUtil.isDateTime(column.getType()))
				dc.setType("date");

			params.add(dp);

			sqlString.append(expression).append(" AND ");

			dc.setFieldName(column.getName());
			dc.setText(getBundleEL(column.getName()));

			dcols.add(dc);
		}
		// [Aug 4, 2012 11:37:07 AM] [Amr.ElAdawy] [details ]

		DataColumn details = new DataColumn();
		ItemTemplate it = new ItemTemplate();
		HyperLink hl = new HyperLink();

		GenericWebControl gwc = new GenericWebControl("i");
		gwc.setCssClass("icon-list-alt icon-black");

		hl.setCssClass(Constants.CSS_BTN + " " + Constants.CSS_BTN_MINI);
		hl.setNavigationURL(new StringBuilder("/").append(Settings.PAGES_FOLDER.replace(File.separator, "/")).append("/")
				.append(dbTable.getPropertyName()).append("/").append(tableCodeName).append(VIEW).append(".").append(Settings.JSPX_EXT)
				.append("?id=${").append(tableCodeName).append("_item.id}").toString());
		hl.addControl(gwc);
		hl.addControl(new StringLiteral(StringUtil.createBundleEL(Constants.DETAILS)));

		it.addControl(hl);
		//it.setCssClass(Constants.CSS_BTN_RED + " " + Constants.CSS_BTN);
		details.addControl(it);

		dcols.add(details);

		sqlString.delete(sqlString.length() - 5, sqlString.length());
		String sqlKey = dbTable.getName() + "_SEARCH";
		sql.put(sqlKey, new Pair(sqlKey, sqlString.toString()).setFormatSql(true));
		table.setSql(StringUtil.createSqlXmlEL(sqlKey));
		table.setNoResults(StringUtil.createBundleEL(Constants.NO_RESULTS));
		table.setAutoBind(true);
		table.setVar(tableCodeName + "_item");

		for (DataParam dataParam : params)
			table.addControl(dataParam);
		for (DataPK dataPK : pks)
			table.addControl(dataPK);
		for (DataColumn dataColumn : dcols)
			table.addControl(dataColumn);
		for (DataLookup dataLookup : dls)
			table.addControl(dataLookup);
		DataColumnCommand dcc = new DataColumnCommand();
		dcc.setType(DataColumnCommand.Edit);
		table.addControl(dcc);

		DataColumnCommand dcc1 = new DataColumnCommand();
		dcc1.setType(DataColumnCommand.Delete);
		dcc1.setConfirm(getBundleEL(Constants.CONFIRM_DELETE));
		table.addControl(dcc1);

		// [Jul 1, 2014 2:44:39 PM] [Amr.ElAdawy] [adding export to excel]
		ExportToExcel ete = new ExportToExcel();
		ete.setFileName(dbTable.getName() + ".xls");
		ete.setRowsToExport("all");
		ete.setCommand(StringUtil.createBundleEL(Constants.EXPORT_TO_EXCEL));

		table.addControl(ete);

		table.setShowRowIndex(true);

		return table;
	}

	private String getBundleEL(String name)
	{
		String el = StringUtil.createBundleEL(name);
		if (!bundle.containsKey(name))
			bundle.put(name, new Pair(name, StringUtil.convertUnderscoreNameToFriendlyName(name)));
		return el;
	}

	private Label createLabel(String text, String forString)
	{
		Label label = new Label();
		label.setCssClass(Constants.CSS_CONTROL_LABEL);
		label.setText(text);
		label.setFor(forString);
		return label;
	}

	private StringLiteral createStringLiteral(String text)
	{
		return new StringLiteral(text);
	}

	private WebControl createInput(String colEL, DBColumn column, DBFK fkTable, String colId, String type)
	{
		WebControl control = null;
		if (fkTable != null)
		{
			control = new Select();
			if (type == EDIT)
				((Select) control).setValue(colEL);
			Option option = new Option();
			option.setText("----");
			control.addControl(option);

			String sqlKey = fkTable.getForeignTable() + "_LOOKUP";
			if (sql.get(sqlKey) == null)
				sql.put(sqlKey, new Pair(sqlKey, "SELECT * FROM " + fkTable.getForeignTable()).setFormatSql(true));

			DataLookup dl = new DataLookup();
			dl.setSql(StringUtil.createSqlXmlEL(sqlKey));

			dl.setDataSource(StringUtil.createSqlEL(Constants.DATA_SOURCE));
			dl.setKey(fkTable.getForeign());
			dl.setValue("NAME");

			control.addControl(dl);
		}
		else
		{
			String colType = column.getType().toLowerCase();
			if (StringUtil.isDateTime(colType))
			{
				control = new Calendar();
				if (type == EDIT)
					((Calendar) control).setValue(colEL);
			}
			else if (StringUtil.isString(colType) || StringUtil.isNumber(colType))
			{
				control = new TextBox();
				if (type == EDIT)
					((TextBox) control).setValue(colEL);
			}
			else if (colType.contains("lob"))
			{
				control = new FileUpload();
				if (type == EDIT)
					((FileUpload) control).setValue(colEL);
			}
			else
				control = createLabel(colEL, "");

		}
		control.setId(colId);
		return control;
	}

	protected void generateSqlFile()
	{
		sqlProp.put(Constants.DATA_SOURCE, new Pair(Constants.DATA_SOURCE, "java:/" + Settings.DATA_SOURCE_NAME));
		writeResourceFile(Settings.SQL_NAME + "Exp.properties", sqlProp, true);
		writeXmlResourceFile(Settings.SQL_NAME + ".xml", sql, true);
	}

	protected void generateBundleFile()
	{
		bundle.put(Constants.EDIT_CAPTION, new Pair(Constants.EDIT_CAPTION, "Edit"));
		bundle.put(Constants.SAVE_CAPTION, new Pair(Constants.SAVE_CAPTION, "Save"));
		bundle.put(Constants.SEARCH_CAPTION, new Pair(Constants.SEARCH_CAPTION, "Search"));
		bundle.put(Constants.DELETE_CAPTION, new Pair(Constants.DELETE_CAPTION, "Delete"));
		bundle.put(Constants.DETAILS, new Pair(Constants.DETAILS, "Details"));
		bundle.put(Constants.CONFIRM_DELETE, new Pair(Constants.CONFIRM_DELETE, "Are you sure you want to delete this item?"));
		bundle.put(Constants.EXPORT_TO_EXCEL, new Pair(Constants.EXPORT_TO_EXCEL, "Export to excel file."));
		bundle.put(Constants.NO_RESULTS, new Pair(Constants.NO_RESULTS, "No results found for search criteria"));
		bundle.put(Constants.APP_NAME, new Pair(Constants.APP_NAME, Settings.APP_NAME));
		writeResourceFile(Settings.BUNDLE_NAME + ".properties", bundle, true);
	}

	protected void writeResourceFile(String resourceFileName, AbstractMap<String, Pair> data, boolean append)
	{
		logger.debug("writeResourceFile(resourceFileName=" + resourceFileName + ", data=, append=" + append + ") - start");
		FileOutputStream fos = null;
		File resFolder = new File(Settings.TARGET_FOLDER + File.separator + Settings.WEB_ROOT_FOLDER + File.separator + Settings.RES_FOLDER);
		if (!resFolder.exists())
			resFolder.mkdirs();
		File resFile = new File(resFolder, resourceFileName);
		try
		{
			if (!resFile.exists())
				resFile.createNewFile();
			fos = new FileOutputStream(resFile, append);
			for (Pair pair : data.values())
			{
				fos.write(pair.toString().getBytes());
				fos.write("\r\n".getBytes());
			}
		}
		catch (Exception e)
		{
			logger.error("writeResourceFile(resourceFileName=" + resourceFileName + ", data=, append=" + append + ")", e);

		}
		finally
		{
			try
			{
				if (fos != null)
					fos.close();
			}
			catch (Exception e2)
			{
				// TODO: handle exception
			}
		}

		logger.debug("writeResourceFile(resourceFileName, data, append) - end");
	}

	protected void writeXmlResourceFile(String resourceFileName, AbstractMap<String, Pair> data, boolean append)
	{
		logger.debug("writeResourceFile(resourceFileName=" + resourceFileName + ", data=, append=" + append + ") - start");
		StringBuilder sb = new StringBuilder();

		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n")
				.append("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\r\n");
		StringBuilder sb2 = new StringBuilder("<properties>");
		for (Pair pair : data.values())
			sb2.append(pair.getXml());

		sb2.append("</properties>");

		String text = StringUtil.formateXML(sb2.toString());

		text = sb.append(text).toString();
		FileOutputStream fos = null;
		File resFolder = new File(Settings.TARGET_FOLDER + File.separator + Settings.WEB_ROOT_FOLDER + File.separator + Settings.RES_FOLDER);
		if (!resFolder.exists())
			resFolder.mkdirs();
		File resFile = new File(resFolder, resourceFileName);
		try
		{
			if (resFile.exists())
				resFile.delete();
			resFile.createNewFile();
			fos = new FileOutputStream(resFile, append);

			fos.write(text.getBytes());
		}
		catch (Exception e)
		{
			logger.error("writeResourceFile(resourceFileName=" + resourceFileName + ", data=, append=" + append + ")", e);

		}
		finally
		{
			try
			{
				if (fos != null)
					fos.close();
			}
			catch (Exception e2)
			{
				// TODO: handle exception
			}
		}

		logger.debug("writeResourceFile(resourceFileName, data, append) - end");
	}
}
