package uniresolver.did;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uniresolver.did.parser.Displayer;
import uniresolver.did.parser.Parser;
import uniresolver.did.parser.ParserException;
import uniresolver.did.parser.Rule;
import uniresolver.did.parser.Rule_did;
import uniresolver.did.parser.Rule_did_fragment;
import uniresolver.did.parser.Rule_did_path;
import uniresolver.did.parser.Rule_did_query;
import uniresolver.did.parser.Rule_did_reference;
import uniresolver.did.parser.Rule_method;
import uniresolver.did.parser.Rule_service;
import uniresolver.did.parser.Rule_specific_idstring;
import uniresolver.did.parser.Terminal_NumericValue;
import uniresolver.did.parser.Terminal_StringValue;

public class DID {

	public static final String URI_SCHEME = "did";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private String didReference;
	private String did;
	private String method;
	private String specificId;
	private String service;
	private String path;
	private String query;
	private String fragment;

	private DID(String didReference) throws IllegalArgumentException, ParserException {

		this.didReference = didReference;

		this.parse();
	}

	private void parse() throws IllegalArgumentException, ParserException {

		Rule_did_reference rule = (Rule_did_reference) Parser.parse("did-reference", this.didReference);
		rule.accept(new DIDVisitor());
	}

	/*
	 * Factory methods
	 */

	public static DID fromString(String string) throws IllegalArgumentException, ParserException {

		return new DID(string);
	}

	public static DID fromUri(URI uri) throws IllegalArgumentException, ParserException {

		return fromString(uri.toString());
	}

	/*
	 * Serialization
	 */

	public static DID fromJson(String json) throws JsonParseException, JsonMappingException, IOException {

		return objectMapper.readValue(json, DID.class);
	}

	public String toJson() throws JsonProcessingException {

		return objectMapper.writeValueAsString(this);
	}

	/*
	 * Helper classes
	 */

	private class DIDVisitor extends Displayer {

		public Object visit(Rule_did rule) {

			DID.this.did = rule.spelling;
			return visitRules(rule.rules);
		}

		public Object visit(Rule_method rule) {

			DID.this.method = rule.spelling;
			return visitRules(rule.rules);
		}

		public Object visit(Rule_specific_idstring rule) {

			DID.this.specificId = rule.spelling;
			return visitRules(rule.rules);
		}

		public Object visit(Rule_service rule) {

			DID.this.service = rule.spelling;
			return visitRules(rule.rules);
		}

		public Object visit(Rule_did_path rule) {

			DID.this.path = rule.spelling;
			return visitRules(rule.rules);
		}

		public Object visit(Rule_did_query rule) {

			DID.this.query = rule.spelling;
			return visitRules(rule.rules);
		}

		public Object visit(Rule_did_fragment rule) {

			DID.this.fragment = rule.spelling;
			return visitRules(rule.rules);
		}

		@Override
		public Object visit(Terminal_StringValue value) {

			return null;
		}

		@Override
		public Object visit(Terminal_NumericValue value) {
			return null;
		}

		private Object visitRules(ArrayList<Rule> rules) {

			for (Rule rule : rules) rule.accept(this);
			return null;
		}
	}

	/*
	 * Getters
	 */

	public String getDidReference() {

		return this.didReference;
	}

	public String getDid() {

		return this.did;
	}

	public String getMethod() {

		return this.method;
	}

	public String getSpecificId() {

		return this.specificId;
	}

	public String getService() {

		return this.service;
	}

	public String getPath() {

		return this.path;
	}

	public String getQuery() {

		return this.query;
	}

	public String getFragment() {

		return this.fragment;
	}

	/*
	 * Object methods
	 */

	@Override
	public int hashCode() {

		return this.didReference.hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		return this.didReference.equals(obj);
	}

	@Override
	public String toString() {

		return this.didReference.toString();
	}
}
