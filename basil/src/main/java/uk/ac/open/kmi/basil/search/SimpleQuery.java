package uk.ac.open.kmi.basil.search;

public class SimpleQuery implements Query {

	private String text = "";
	
	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getText() {
		return this.text;
	}

}
