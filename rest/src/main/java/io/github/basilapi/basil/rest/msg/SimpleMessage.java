/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.basilapi.basil.rest.msg;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class SimpleMessage implements Message {

	private String text = null;
	private String location = null;
	private Exception e = null;

	public SimpleMessage(String message) {
		this.text = message;
	}
	
	public SimpleMessage(String message, String location) {
		this.text = message;
		this.location = location;
	}
	
	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public void setError(Exception e) {
		this.e = e;
	}

	@Override
	public boolean isError() {
		return e != null;
	}

	@Override
	public boolean hasLocation() {
		return location != null;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public String asJSON() {
		return renderJSON(this);
	}
	
	@Override
	public String asTXT() {
		return renderTXT(this);
	}
	
	public static String renderJSON(Message msg) {
		JsonObject m = new JsonObject();
		if (msg.isError()) {
			m.add("error", new JsonPrimitive(msg.getText()));
		} else {
			m.add("message", new JsonPrimitive(msg.getText()));
		}
		if (msg.hasLocation()) {
			m.add("location", new JsonPrimitive(msg.getLocation()));
		}
		return m.toString();
	}

	public static String renderTXT(Message msg) {
		StringBuilder b = new StringBuilder();
		if (msg.isError()) {
			b.append("Error: ");
		} else {
			b.append("Message: ");
		}
		b.append(msg.getText()).append(" \n");
		if (msg.hasLocation()) {
			b.append("Location: ").append(msg.getLocation());
		}
		return b.toString();
	}
}
