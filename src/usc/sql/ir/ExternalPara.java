package usc.sql.ir;

public class ExternalPara extends Variable{

	String name;
	String type; // "strings.xml"
	public ExternalPara(String name)
	{
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName()
	{
		return name;
	}

	@Override
	public String getValue() {
		return "@"+name;
	}
	
	@Override
	public String toString()
	{
		return "\""+name+"\"";
	}
}
