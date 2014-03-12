package msi.gama.jogl.collada;

import java.util.ArrayList;

public class Triangles 
{
	private int m_count = -1;
	private String m_materials = null;
	private ArrayList<Input> m_inputs = new ArrayList<Input>();
	private P m_p = null;

	
	/**---------------GETTER AND SETTER FUNCTIONS-----------------------------**/
	public int getCount() {
		return m_count;
	}
	
	public void setCount(int m_count) {
		this.m_count = m_count;
	}

	public String getMaterials() {
		return m_materials;
	}

	public void setMaterials(String m_materials) {
		this.m_materials = m_materials;
	}

	public ArrayList<Input> getInputs() {
		return m_inputs;
	}

	public void setInputs(ArrayList<Input> m_inputs) {
		this.m_inputs = m_inputs;
	}

	public P getP() {
		return m_p;
	}

	public void setP(P m_p) {
		this.m_p = m_p;
	}
}