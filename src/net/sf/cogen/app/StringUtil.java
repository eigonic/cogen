/**
 * 
 */
package net.sf.cogen.app;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import eg.java.net.web.jspx.engine.util.StringUtility;

/**
 * @author Amr.ElAdawy
 *
 */
public class StringUtil
{

	/**
	 * 
	 */
	private StringUtil()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * Convert a column name with underscores to the corresponding property name using "camel case".  A name
	 * like "customer_number" would match a "customerNumber" property name.
	 * @param name the column name to be converted
	 * @return the name using "camel case"
	 */
	public static String convertUnderscoreNameToPropertyName(String name)
	{
		StringBuilder result = new StringBuilder();
		boolean nextIsUpper = false;
		if (name != null && name.length() > 0)
		{
			if (name.length() > 1 && name.substring(1, 2).equals("_"))
			{
				result.append(name.substring(0, 1).toUpperCase());
			}
			else
			{
				result.append(name.substring(0, 1).toLowerCase());
			}
			for (int i = 1; i < name.length(); i++)
			{
				String s = name.substring(i, i + 1);
				if (s.equals("_"))
				{
					nextIsUpper = true;
				}
				else
				{
					if (nextIsUpper)
					{
						result.append(s.toUpperCase());
						nextIsUpper = false;
					}
					else
					{
						result.append(s.toLowerCase());
					}
				}
			}
		}
		return result.toString();
	}

	public static String createInstanceName(String className)
	{
		if (StringUtility.isNullOrEmpty(className))
			return "";
		return new StringBuilder(String.valueOf(className.charAt(0)).toLowerCase()).append(className.substring(1)).toString();
	}

	/**
	 * Convert a column name with underscores to the corresponding property name using "Pascal like case".  A name
	 * like "customer_number" would match a "CustomerNumber" property name.
	 * @param name the column name to be converted
	 * @return the name using "Pascal like case"
	 */
	public static String convertUnderscoreNameToClassName(String name)
	{
		StringBuilder result = new StringBuilder();
		boolean nextIsUpper = false;
		if (name != null && name.length() > 0)
		{
			if (name.length() > 1 && name.substring(1, 2).equals("_"))
			{
				result.append(name.substring(0, 1).toUpperCase());
			}
			else
			{
				result.append(name.substring(0, 1).toUpperCase());
			}
			for (int i = 1; i < name.length(); i++)
			{
				String s = name.substring(i, i + 1);
				if (s.equals("_"))
				{
					nextIsUpper = true;
				}
				else
				{
					if (nextIsUpper)
					{
						result.append(s.toUpperCase());
						nextIsUpper = false;
					}
					else
					{
						result.append(s.toLowerCase());
					}
				}
			}
		}
		return result.toString();
	}

	/**
	 *  Convert a column name with underscores to the corresponding property name using "Initial Cap case".  A name
	 * like "customer_number" would match a "Customer Number" .
	 * 
	 * @param name
	 * @return
	 */
	public static String convertUnderscoreNameToFriendlyName(String name)
	{
		StringBuilder result = new StringBuilder();
		boolean nextIsUpper = false;
		if (name != null && name.length() > 0)
		{
			result.append(name.substring(0, 1).toUpperCase());
			for (int i = 1; i < name.length(); i++)
			{
				String s = name.substring(i, i + 1);
				if (s.equals("_"))
				{
					nextIsUpper = true;
					result.append(" ");
				}
				else
				{
					if (nextIsUpper)
					{
						result.append(s.toUpperCase());
						nextIsUpper = false;
					}
					else
					{
						result.append(s.toLowerCase());
					}
				}
			}
		}
		return result.toString();
	}

	public static String createBeanEL(String bean, String property)
	{
		return new StringBuilder("${").append(bean).append(".").append(property).append("}").toString();
	}

	public static String createBundleEL(String property)
	{
		return createBeanEL(Settings.BUNDLE_NAME, property);
	}

	public static String createSqlXmlEL(String property)
	{
		return createBeanEL(Settings.SQL_NAME, property);
	}

	public static String createSqlEL(String property)
	{
		return createBeanEL(Settings.SQL_NAME + "Exp", property);
	}

	public static String createPageEl(String beanANme, String property)
	{
		return createBeanEL("this", property);
	}

	public static String getColumnWebControlId(String colname)
	{
		return StringUtil.convertUnderscoreNameToPropertyName(colname).concat("WC");
	}

	public static boolean isDateTime(String colType)
	{
		return (colType.toLowerCase().contains("date") || colType.toLowerCase().contains("time"));
	}

	public static boolean isNumber(String colType)
	{
		return colType.toLowerCase().contains("int") || colType.toLowerCase().contains("num") || colType.toLowerCase().contains("decimal");
	}

	public static boolean isString(String colType)
	{
		return colType.toLowerCase().contains("char") || colType.toLowerCase().contains("varchar");
	}

	public static String formateXML(String input)
	{
		try
		{
			Source xmlInput = new StreamSource(new StringReader(input));
			StringWriter stringWriter = new StringWriter();
			StreamResult xmlOutput = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", 4);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(xmlInput, xmlOutput);
			return xmlOutput.getWriter().toString();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e); // simple exception handling, please review it
		}
	}

}
