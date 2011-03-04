package net.rubyeye.ur;

/**
 * 表格单元信息
 * 
 * @author dennis
 * 
 */
public class GridInfo {

	private String name;
	private int drawable;

	public GridInfo(String name, int drawable) {
		super();
		this.name = name;
		this.drawable = drawable;
	}

	public String getName() {
		return name;
	}

	public int getDrawable() {
		return drawable;
	}

	public void setDrawable(int drawable) {
		this.drawable = drawable;
	}

	public void setName(String name) {
		this.name = name;
	}

}