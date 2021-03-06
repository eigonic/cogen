package net.sf.cogen.model;

import net.sf.cogen.app.StringUtil;

public class DBColumn
{
	String name;
	boolean primaryKey;
	boolean required;

	String propertyName;

	int scale;
	int size;
	String type;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
		this.propertyName = StringUtil.convertUnderscoreNameToPropertyName(this.name);
	}

	public boolean isPrimaryKey()
	{
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey)
	{
		this.primaryKey = primaryKey;
	}

	public boolean isRequired()
	{
		return required;
	}

	public void setRequired(boolean required)
	{
		this.required = required;
	}

	public int getScale()
	{
		return scale;
	}

	public void setScale(int scale)
	{
		this.scale = scale;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getPropertyName()
	{
		return propertyName;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("DBColumn [name=");
		builder.append(name);
		builder.append(", primaryKey=");
		builder.append(primaryKey);
		builder.append(", required=");
		builder.append(required);
		builder.append(", scale=");
		builder.append(scale);
		builder.append(", size=");
		builder.append(size);
		builder.append(", type=");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}

}
