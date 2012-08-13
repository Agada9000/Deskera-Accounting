/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package com.krawler.common.util;

import com.krawler.common.service.ServiceException;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;
import javax.security.auth.login.AccountException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * @author schemers
 */
public class StringUtil {
    //CRM - Specific Functions
    public static String sizeRenderer(String value) {
        Double size = Double.parseDouble(value);
        String text = "";
        Double val;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (size >= 1 && size < 1024) {
            text = size + " Bytes";
        } else if (size > 1024 && size < 1048576) {
            val = (size / 1024);
            text = decimalFormat.format(val);
            text += " KB";
        } else if (size > 1048576) {
            val = (size / 1048576);
            text = decimalFormat.format(val);
            text += " MB";
        }
        return text;
    }

    public static String getMyAdvanceSearchString(String Searchjson, String appendCase) throws JSONException, JSONException {
        StringBuilder myResult = new StringBuilder();
        int any = 0;
        JSONObject jobj = new JSONObject(Searchjson);
        int count = jobj.getJSONArray("root").length();
        for (int i = 0; i < count; i++) {
            JSONObject jobj1 = jobj.getJSONArray("root").getJSONObject(i);
            any++;
            myResult.append(" ");
            if (i == 0) {
                myResult.append(appendCase);
                myResult.append(" (( ");
            } else {
                myResult.append(" ( ");
            }
            myResult.append(jobj1.getString("column") + " like ? or " + jobj1.getString("column") + " like ?");
            if (i + 1 < count) {
                myResult.append(") ");
            } else {
                myResult.append(")) ");
            }
            if (i + 1 < count) {
                myResult.append(" and ");
            }
        }
        if (any == 0) {
            myResult.append(" ");
        }
        return myResult.toString();
    }

    public static void insertParamAdvanceSearchString(ArrayList al, String Searchjson)
            throws JSONException, JSONException {

        JSONObject jobj = new JSONObject(Searchjson);
        int count = jobj.getJSONArray("root").length();

        for (int i = 0; i < count; i++) {
            JSONObject jobj1 = jobj.getJSONArray("root").getJSONObject(i);
            String trimedStr = jobj1.getString("searchText").trim();
            al.add(trimedStr + "%");
            al.add("% " + trimedStr + "%");
        }

    }

    public static String sNull(String s) {
        String ret = null;
        if (!(StringUtil.isNullOrEmpty(s))) {
            ret = s;
        } else {
            ret = "";
        }
        return ret;
    }

    public static boolean bNull(String s) {
        if (StringUtil.isNullOrEmpty(s)) {
            return false;
        } else {
            return true;
        }
    // return true;
    }

    public static String filterQuery(ArrayList filter_names, String appendCase) {
        StringBuilder filterQuery = new StringBuilder();
//        String filterQuery = "";
        String oper = "";
        String op = "";
        for(int i = 0; i < filter_names.size(); i++) {
            oper = "";
            op = "";
            if(filter_names.get(i).toString().length()>=5)
                op = filter_names.get(i).toString().substring(0, 5);
            if(op.equals("ISNOT")){
                oper = " is not ";
                String opstr = filter_names.get(i).toString();
                filter_names.set(i, opstr.substring(5, opstr.length()));
            }else if(op.equals("NOTIN")){
                oper = " not in("+i+")";
                String opstr = filter_names.get(i).toString();
                filter_names.set(i, opstr.substring(5, opstr.length()));
            }else{
                if(filter_names.get(i).toString().length()>=4)
                    op = filter_names.get(i).toString().substring(0, 4);
                if(op.equals("LIKE")){
                    oper = " like ";
                    String opstr = filter_names.get(i).toString();
                    filter_names.set(i, opstr.substring(4, opstr.length()));
                }else{
                    op = filter_names.get(i).toString().substring(0, 2);
                    if(op.equals("<=")){
                        oper = " <= ";
                        String opstr = filter_names.get(i).toString();
                        filter_names.set(i, opstr.substring(2, opstr.length()));
                    }
                    else if(op.equals(">=")){
                        oper = " >= ";
                        String opstr = filter_names.get(i).toString();
                        filter_names.set(i, opstr.substring(2, opstr.length()));
                    }else if(op.equals("IS")){
                        oper = " is ";
                        String opstr = filter_names.get(i).toString();
                        filter_names.set(i, opstr.substring(2, opstr.length()));
                    }else if(op.equals("IN")){
                        oper = " in("+i+")";
                        String opstr = filter_names.get(i).toString();
                        filter_names.set(i, opstr.substring(2, opstr.length()));
                    }else{

                        op = filter_names.get(i).toString().substring(0, 1);
                        if(op.equals("!")){
                            oper = " != ";
                            String opstr = filter_names.get(i).toString();
                            filter_names.set(i, opstr.substring(1, opstr.length()));
                        }
                        else if(op.equals("<")){
                            oper = " < ";
                            String opstr = filter_names.get(i).toString();
                            filter_names.set(i, opstr.substring(1, opstr.length()));
                        }
                        else if(op.equals(">")){
                            oper = " > ";
                            String opstr = filter_names.get(i).toString();
                            filter_names.set(i, opstr.substring(1, opstr.length()));
                        }
                        else
                            oper = " = ";
                    }
                }
            }

            if(i == 0) {
//                filterQuery += " where "+filter_names.get(i)+" = ? ";
                if(!op.equals("IN")&&!op.equals("NOTIN"))
                    filterQuery.append(" "+ appendCase +" "+ filter_names.get(i) +oper +" ? ");
                else
                    filterQuery.append(" "+ appendCase +" "+ filter_names.get(i) +oper );
            } else {
//                filterQuery += " and "+filter_names.get(i)+" = ? ";
                if(!op.equals("IN")&&!op.equals("NOTIN"))
                    filterQuery.append(" and "+filter_names.get(i)+oper +" ? ");
                else
                    filterQuery.append(" and "+" "+ filter_names.get(i) +oper );
            }
        }
        return filterQuery.toString();
    }

    public static String orderQuery(ArrayList field_names, ArrayList field_order) {
        StringBuilder orderQuery = new StringBuilder();
        if(field_names!=null) {
            for(int i = 0; i < field_names.size(); i++) {
                if(i == 0) {
                    orderQuery.append(" order by ");
                    orderQuery.append(" "+ field_names.get(i) +" "+ field_order.get(i));
                } else {
                    orderQuery.append(", "+ field_names.get(i) +" "+ field_order.get(i));
                }
            }
        }
        return orderQuery.toString();
    }

    public static String groupQuery(ArrayList field_names) throws JSONException, JSONException {
        StringBuilder orderQuery = new StringBuilder();
        if(field_names!=null) {
            for(int i = 0; i < field_names.size(); i++) {
                if(i == 0) {
                    orderQuery.append(" group by ");
                    orderQuery.append(" "+ field_names.get(i) );
                } else {
                    orderQuery.append(", "+ field_names.get(i) );
                }
            }
        }
        return orderQuery.toString();
    }

    public static String hNull(String s) {
        String ret = null;
        if (!(StringUtil.isNullOrEmpty(s))) {
            ret = s;
        } else {
            ret = "";
        }
        return ret;
    }

    public static KwlReturnObject buildNExecuteQuery(HibernateTemplate hibernateTemplate, String initialQuery, HashMap<String, Object> requestParams) throws ServiceException {
        return buildNExecuteQuery(hibernateTemplate, initialQuery, requestParams, "");
    }
    public static KwlReturnObject buildNExecuteQuery(HibernateTemplate hibernateTemplate, String initialQuery, HashMap<String, Object> requestParams, String quickSearch) throws ServiceException {
        List list = new ArrayList();

        String filter = "";
        //Get Filter String
        ArrayList filter_names = new ArrayList(),filter_params = new ArrayList();
        if(requestParams.containsKey("filter_names") && requestParams.containsKey("filter_params")){
            filter_names = (ArrayList) requestParams.get("filter_names");
            filter_params = (ArrayList) requestParams.get("filter_params");
            //if(filter_names.size() != filter_params.size()) { //throw "size not same" exception}
            filter = filterQuery(filter_names, "where");
            int ind = filter.indexOf("("); // Insert in/not in params in filter query.
            if(ind>-1){
                int index = Integer.valueOf(filter.substring(ind+1,filter.indexOf(")")));
                filter = filter.replaceAll("("+index+")", filter_params.get(index).toString());
                filter_params.remove(index);
            }
        }

        String query = initialQuery + filter;

        //Add Quick Search Filter
        if(!isNullOrEmpty(quickSearch)) {
            query += (isNullOrEmpty(filter)?" where " : " and " )+quickSearch;
        }

        //Add Order By Clause
        if(requestParams.get("order_by")!=null && requestParams.get("order_type")!=null){
            ArrayList orderby = new ArrayList((List<String>)requestParams.get("order_by"));
            ArrayList ordertype = new ArrayList((List<Object>)requestParams.get("order_type"));
            //if(filter_names.size() != filter_params.size()) { //throw "size not same" exception}
            query += orderQuery(orderby, ordertype);
        }

        list = HibernateUtil.executeQuery(hibernateTemplate, query, filter_params.toArray());
        int count = list.size();

        //Execute Paging Query
        boolean allflag = false;
        if(requestParams.containsKey("allflag") && requestParams.get("allflag")!=null) {
            allflag = Boolean.parseBoolean(requestParams.get("allflag").toString());
        }
        if(!allflag) {
            if (requestParams.containsKey("start") && requestParams.get("start") != null && requestParams.containsKey("limit") && requestParams.get("limit") != null) {
                int start = Integer.parseInt(requestParams.get("start").toString());
                int limit = Integer.parseInt(requestParams.get("limit").toString());
                list = HibernateUtil.executeQueryPaging(hibernateTemplate, query, filter_params.toArray(), new Integer[]{start,limit});
            }
        }

        return new KwlReturnObject(true, null, null, list, count);
    }
        
	/**
	 * A user-friendly equal that handles one or both nulls easily
	 *
	 * @return
	 */
	public static boolean equal(String s1, String s2) {
		if (s1 == null || s2 == null)
			return s1 == s2;
		return s1.equals(s2);
	}

    public static String padString(String s1, int length, String pad_string, int pad_type) {
		String padStr = "";
        if(length < s1.length()){
            padStr = s1;
        } else {
            int z = (length - s1.length()) / pad_string.length();
            for(int i = 0; i <= z; i++){
                padStr += pad_string;
            }
            if(pad_type == 1){
                padStr += s1;
                padStr = padStr.substring((padStr.length() - length));
            } else {
                padStr = s1 + padStr;
                padStr = padStr.substring(0, length);
            }
        }
        return padStr;
	}
    public static long hexadecimalToDecimal(String hex) throws NumberFormatException {
        long res = 0;
        if (hex.isEmpty()) {
            throw new NumberFormatException("Empty string is not a hexadecimal number");
        }
        for (int i = 0; i < hex.length(); i++) {
            char n = hex.charAt(hex.length() - (i + 1));
            int f = (int) n - 48;
            if (f > 9) {
                f = f - 7;
                if (f > 15) {
                    f = f - 32;
                }
            }
            if (f < 0 || f > 15) {
                throw new NumberFormatException("Not a hexadecimal number");
            } else {
                res += f * Math.round(Math.pow(2.0, (4 * i)));
            }
        }
        return res;
    }
	public static String stripControlCharacters(String raw) {
		if (raw == null)
			return null;
		int i;
		for (i = 0; i < raw.length(); i++) {
			char c = raw.charAt(i);
			// invalid control characters
			if (c < 0x20 && c != 0x09 && c != 0x0A && c != 0x0D)
				break;
			// byte-order markers and high/low surrogates
			if (c == 0xFFFE || c == 0xFFFF || (c > 0xD7FF && c < 0xE000))
				break;
		}
		if (i >= raw.length())
			return raw;
		StringBuilder sb = new StringBuilder(raw.substring(0, i));
		for (; i < raw.length(); i++) {
			char c = raw.charAt(i);
			if (c >= 0x20 || c == 0x09 || c == 0x0A || c == 0x0D)
				if (c != 0xFFFE && c != 0xFFFF && (c <= 0xD7FF || c >= 0xE000))
					sb.append(c);
		}
		return sb.toString();
	}

	public static boolean isAsciiString(String str) {
		if (str == null)
			return false;
		for (int i = 0, len = str.length(); i < len; i++) {
			char c = str.charAt(i);
			if ((c < 0x20 || c >= 0x7F) && c != '\r' && c != '\n' && c != '\t')
				return false;
		}
		return true;
	}

	/**
	 * add the name/value mapping to the map. If an entry doesn't exist, value
	 * remains a String. If an entry already exists as a String, convert to
	 * String[] and add new value. If entry already exists as a String[], grow
	 * array and add new value.
	 *
	 * @param result
	 *            result map
	 * @param name
	 * @param value
	 */
	public static void addToMultiMap(Map<String, Object> result, String name,
			String value) {
		Object currentValue = result.get(name);
		if (currentValue == null) {
			result.put(name, value);
		} else if (currentValue instanceof String) {
			result.put(name, new String[] { (String) currentValue, value });
		} else if (currentValue instanceof String[]) {
			String[] ov = (String[]) currentValue;
			String[] nv = new String[ov.length + 1];
			System.arraycopy(ov, 0, nv, 0, ov.length);
			nv[ov.length] = value;
			result.put(name, nv);
		}
	}

	/**
	 * Convert an array of the form:
	 *
	 * a1 v1 a2 v2 a2 v3
	 *
	 * to a map of the form:
	 *
	 * a1 -> v1 a2 -> [v2, v3]
	 */
	public static Map<String, Object> keyValueArrayToMultiMap(String[] args,
			int offset) {
		Map<String, Object> attrs = new HashMap<String, Object>();
		for (int i = offset; i < args.length; i += 2) {
			String n = args[i];
			if (i + 1 >= args.length)
				throw new IllegalArgumentException("not enough arguments");
			String v = args[i + 1];
			addToMultiMap(attrs, n, v);
		}
		return attrs;
	}

	private static final int TERM_WHITESPACE = 1;
	private static final int TERM_SINGLEQUOTE = 2;
	private static final int TERM_DBLQUOTE = 3;

	/**
	 * open the specified file and return the first line in the file, without
	 * the end of line character(s).
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String readSingleLineFromFile(String file) throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			return in.readLine();
		} finally {
			if (is != null)
				is.close();
		}
	}

	/**
	 * read a line from "in", using readLine(). A trailing '\\' on the line will
	 * be treated as continuation and the next line will be read and appended to
	 * the line, without the \\.
	 *
	 * @param in
	 * @return complete line or null on end of file.
	 * @throws IOException
	 */
	public static String readLine(BufferedReader in) throws IOException {
		String line;
		StringBuilder sb = null;

		while ((line = in.readLine()) != null) {
			if (line.length() == 0) {
				break;
			} else if (line.charAt(line.length() - 1) == '\\') {
				if (sb == null)
					sb = new StringBuilder();
				sb.append(line.substring(0, line.length() - 1));
			} else {
				break;
			}
		}

		if (line == null) {
			if (sb == null) {
				return null;
			} else {
				return sb.toString();
			}
		} else {
			if (sb == null) {
				return line;
			} else {
				sb.append(line);
				return sb.toString();
			}
		}
	}

	public static List<String> parseSieveStringList(String value)
			throws ServiceException {
		List<String> result = new ArrayList<String>();
		if (value == null)
			return result;
		value = value.trim();
		if (value.length() == 0)
			return result;
		int i = 0;
		boolean inStr = false;
		boolean inList = false;
		StringBuilder sb = null;
		while (i < value.length()) {
			char ch = value.charAt(i++);
			if (inStr) {
				if (ch == '"') {
					result.add(sb.toString());
					inStr = false;
				} else {
					if (ch == '\\' && i < value.length())
						ch = value.charAt(i++);
					sb.append(ch);
				}
			} else {
				if (ch == '"') {
					inStr = true;
					sb = new StringBuilder();
				} else if (ch == '[' && !inList) {
					inList = true;
				} else if (ch == ']' && inList) {
					inList = false;
				} else if (!Character.isWhitespace(ch)) {
					throw ServiceException.INVALID_REQUEST(
							"unable to parse string list: " + value, null);
				}
			}
		}
		if (inStr || inList) {
			throw ServiceException.INVALID_REQUEST(
					"unable to parse string list2: " + value, null);
		}
		return result;
	}

	/**
	 * split a line into array of Strings, using a shell-style syntax for
	 * tokenizing words.
	 *
	 * @param line
	 * @return
	 */
	public static String[] parseLine(String line) {
		ArrayList<String> result = new ArrayList<String>();

		int i = 0;

		StringBuilder sb = new StringBuilder(32);
		int term = TERM_WHITESPACE;
		boolean inStr = false;

		scan: while (i < line.length()) {
			char ch = line.charAt(i++);
			boolean escapedTerm = false;

			if (ch == '\\' && i < line.length()) {
				ch = line.charAt(i++);
				switch (ch) {
				case '\\':
					break;
				case 'n':
					ch = '\n';
					escapedTerm = true;
					break;
				case 't':
					ch = '\t';
					escapedTerm = true;
					break;
				case 'r':
					ch = '\r';
					escapedTerm = true;
					break;
				case '\'':
					ch = '\'';
					escapedTerm = true;
					break;
				case '"':
					ch = '"';
					escapedTerm = true;
					break;
				default:
					escapedTerm = Character.isWhitespace(ch);
					break;
				}
			}

			if (inStr) {
				if (!escapedTerm
						&& ((term == TERM_WHITESPACE && Character
								.isWhitespace(ch))
								|| (term == TERM_SINGLEQUOTE && ch == '\'') || (term == TERM_DBLQUOTE && ch == '"'))) {
					inStr = false;
					result.add(sb.toString());
					sb = new StringBuilder(32);
					term = TERM_WHITESPACE;
					continue scan;
				}
				sb.append(ch);
			} else {
				if (!escapedTerm) {
					switch (ch) {
					case '\'':
						term = TERM_SINGLEQUOTE;
						inStr = true;
						continue scan;
					case '"':
						term = TERM_DBLQUOTE;
						inStr = true;
						continue scan;
					default:
						if (Character.isWhitespace(ch))
							continue scan;
						inStr = true;
						sb.append(ch);
						break;
					}
				} else {
					// we had an escaped terminator, start a new string
					inStr = true;
					sb.append(ch);
				}
			}
		}

		if (sb.length() > 0)
			result.add(sb.toString());

		return result.toArray(new String[result.size()]);
	}

	private static void dump(String line) {
		String[] result = parseLine(line);
		System.out.println("line: " + line);
		for (int i = 0; i < result.length; i++)
			System.out.println(i + ": (" + result[i] + ")");
		System.out.println();
	}

	public static void main(String args[]) {
		dump("this is a test");
		dump("this is 'a nother' test");
		dump("this is\\ test");
		dump("first Roland last 'Schemers' full 'Roland Schemers'");
		dump("multi 'Roland\\nSchemers'");
		dump("a");
		dump("");
		dump("\\  \\ ");
		dump("backslash \\\\");
		dump("backslash \\f");
		dump("a           b");
	}

	// A pattern that matches the beginning of a string followed by ${KEY_NAME}
	// followed
	// by the end. There are three groups: the beginning, KEY_NAME and the end.
	// Pattern.DOTALL is required in case one of the values in the map has a
	// newline
	// in it.
	private static Pattern templatePattern = Pattern.compile(
			"(.*)\\$\\{([^\\)]+)\\}(.*)", Pattern.DOTALL);

	/**
	 * Substitutes all occurrences of the specified values into a template. Keys
	 * for the values are specified in the template as <code>${KEY_NAME}</code>.
	 *
	 * @param template
	 *            the template
	 * @param vars
	 *            a <code>Map</code> filled with keys and values. The keys
	 *            must be <code>String</code>s.
	 * @return the template with substituted values
	 */
	public static String fillTemplate(String template, Map vars) {
		if (template == null) {
			return null;
		}

		String line = template;
		Matcher matcher = templatePattern.matcher(line);

		// Substitute multiple variables per line
		while (matcher.matches()) {
			String key = matcher.group(2);
			Object value = vars.get(key);
			if (value == null) {
				KrawlerLog.misc.info("fillTemplate(): could not find key '"
						+ key + "'");
				value = "";
			}
			line = matcher.group(1) + value + matcher.group(3);
			matcher.reset(line);
		}
		return line;
	}

	/**
	 * Joins an array of <code>short</code>s, separated by a delimiter.
	 */
	public static String join(String delimiter, short[] array) {
		if (array == null) {
			return null;
		}

		StringBuilder buf = new StringBuilder();

		for (int i = 0; i < array.length; i++) {
			buf.append(array[i]);
			if (i + 1 < array.length) {
				buf.append(delimiter);
			}
		}
		return buf.toString();
	}

	/**
	 * Joins an array of objects, separated by a delimiter.
	 */
	public static String join(String delimiter, Object[] array) {
		if (array == null) {
			return null;
		}

		StringBuilder buf = new StringBuilder();

		for (int i = 0; i < array.length; i++) {
			buf.append(array[i]);
			if (i + 1 < array.length) {
				buf.append(delimiter);
			}
		}
		return buf.toString();
	}

	public static <E> String join(String delimiter, Collection<E> col) {
		if (col == null) {
			return null;
		}
		Object[] array = new Object[col.size()];
		col.toArray(array);
		return join(delimiter, array);
	}

	/**
	 * Returns the simple class name (the name after the last dot) from a
	 * fully-qualified class name. Behavior is the same as {@link #getExtension}.
	 */
	public static String getSimpleClassName(String className) {
		return getExtension(className);
	}

	/**
	 * Returns the simple class name (the name after the last dot) for the
	 * specified object.
	 */
	public static String getSimpleClassName(Object o) {
		if (o == null) {
			return null;
		}
		return getExtension(o.getClass().getName());
	}

	/**
	 * Returns the extension portion of the given filename.
	 * <ul>
	 * <li>If <code>filename</code> contains one or more dots, returns all
	 * characters after the last dot.</li>
	 * <li>If <code>filename</code> contains no dot, returns
	 * <code>filename</code>.</li>
	 * <li>If <code>filename</code> is <code>null</code>, returns
	 * <code>null</code>.</li>
	 * <li>If <code>filename</code> ends with a dot, returns an empty
	 * <code>String</code>.</li>
	 * </ul>
	 */
	public static String getExtension(String filename) {
		if (filename == null) {
			return null;
		}
		int lastDot = filename.lastIndexOf(".");
		if (lastDot == -1) {
			return filename;
		}
		if (lastDot == filename.length() - 1) {
			return "";
		}
		return filename.substring(lastDot + 1, filename.length());
	}

	/**
	 * Returns <code>true</code> if the secified string is <code>null</code>
	 * or its length is <code>0</code>.
	 */
	public static boolean isNullOrEmpty(String s) {
		if (s == null || s.length() == 0) {
			return true;
		}
		return false;
	}

	private static final String[] JS_CHAR_ENCODINGS = { "\\u0000", "\\u0001",
			"\\u0002", "\\u0003", "\\u0004", "\\u0005", "\\u0006", "\\u0007",
			"\\b", "\\t", "\\n", "\\u000B", "\\f", "\\r", "\\u000E", "\\u000F",
			"\\u0010", "\\u0011", "\\u0012", "\\u0013", "\\u0014", "\\u0015",
			"\\u0016", "\\u0017", "\\u0018", "\\u0019", "\\u001A", "\\u001B",
			"\\u001C", "\\u001D", "\\u001E", "\\u001F" };

	public static String jsEncode(Object obj) {
		if (obj == null)
			return "";
		String replacement, str = obj.toString();
		StringBuilder sb = null;
		int i, last, length = str.length();
		for (i = 0, last = -1; i < length; i++) {
			char c = str.charAt(i);
			switch (c) {
			case '\\':
				replacement = "\\\\";
				break;
			case '"':
				replacement = "\\\"";
				break;
			case '\u2028':
				replacement = "\\u2028";
				break;
			case '\u2029':
				replacement = "\\u2029";
				break;
			default:
				if (c >= ' ')
					continue;
				replacement = JS_CHAR_ENCODINGS[c];
				break;
			}
			if (sb == null)
				sb = new StringBuilder(str.substring(0, i));
			else
				sb.append(str.substring(last, i));
			sb.append(replacement);
			last = i + 1;
		}
		return (sb == null ? str : sb.append(str.substring(last, i)).toString());
	}

	public static String jsEncodeKey(String key) {
		return '"' + key + '"';
	}

	//
	// HTML methods
	//
	private static final Pattern PAT_AMP = Pattern.compile("&",
			Pattern.MULTILINE);
	private static final Pattern PAT_LT = Pattern.compile("<",
			Pattern.MULTILINE);
	private static final Pattern PAT_GT = Pattern.compile(">",
			Pattern.MULTILINE);
	private static final Pattern PAT_DBLQT = Pattern.compile("\"",
			Pattern.MULTILINE);

	/**
	 * Escapes special characters with their HTML equivalents.
	 */
	public static String escapeHtml(String text) {
		if (text == null || text.length() == 0)
			return "";
		String s = replaceAll(text, PAT_AMP, "&amp;");
		s = replaceAll(s, PAT_LT, "&lt;");
		s = replaceAll(s, PAT_GT, "&gt;");
		s = replaceAll(s, PAT_DBLQT, "&quot;");
		return s;
	}

	private static String replaceAll(String text, Pattern pattern,
			String replace) {
		Matcher m = pattern.matcher(text);
		StringBuffer sb = null;
		while (m.find()) {
			if (sb == null)
				sb = new StringBuffer();
			m.appendReplacement(sb, replace);
		}
		if (sb != null)
			m.appendTail(sb);
		return sb == null ? text : sb.toString();
	}

	public static boolean stringCompareInLowercase(String strToCompareWith,
			String strTobeCompare) {
		return strToCompareWith.equalsIgnoreCase(strTobeCompare);

	}

        public static String getMySearchString( String searchString, String appendCase, String [] searchParams)
        {
            StringBuilder myResult = new StringBuilder();

            if(!isNullOrEmpty(searchString)) {
                for(int i = 0; i < searchParams.length; i++) {
                    myResult.append(" ");
                    if(i == 0) {
                        myResult.append(appendCase);
                        myResult.append(" (( ");
                    } else {
                        myResult.append(" ( ");
                    }
                    myResult.append(searchParams[i]+" like ? or "+searchParams[i]+" like ?");
                    if(i + 1 < searchParams.length) {
                        myResult.append(") ");
                    } else {
                        myResult.append(")) ");
                    }
                    if(i + 1 < searchParams.length) {
                        myResult.append(" or ");
                    }
                }
            } else {
                myResult.append(" ");
            }
            return myResult.toString();
        }

        public static int insertParamSearchString(int cnt, java.sql.PreparedStatement pstmt, String searchString,int searchParamsLength)
                                    throws SQLException
        {
            int i=0;
            if(!isNullOrEmpty(searchString)) {
                  String trimedStr = searchString.trim();
                  for(i = 0; i < searchParamsLength*2; i++) {
                       if(i%2==0){
                           pstmt.setString(cnt+i,trimedStr+"%");
                        }else {
                           pstmt.setString(cnt+i,"% "+trimedStr+"%");
                        }
                  }
            }
            return (cnt+i);
        }

        public static void insertParamSearchString(ArrayList al, String searchString,int searchParamsLength)
                                    throws SQLException
        {
            int i=0;
            if(!isNullOrEmpty(searchString)) {
                String trimedStr = searchString.trim();
                for(i = 0; i < searchParamsLength*2; i++) {
                   if(i%2==0){
                       al.add(trimedStr+"%");
                   }else {
                       al.add("% "+trimedStr+"%");
                   }
              }
            }
        }
        public static String serverHTMLStripper(String stripTags)
                throws IllegalStateException, IndexOutOfBoundsException
        {
            Pattern p = Pattern.compile("<[^>]*>");
            Matcher m = p.matcher(stripTags);
            StringBuffer sb = new StringBuffer();
            if(!isNullOrEmpty(stripTags))
            {
                while(m.find()) {
                    m.appendReplacement(sb, "");
                }
                m.appendTail(sb);
                stripTags = sb.toString();
            }
            return stripTags.trim();
      }
      public static String checkForNull(String rsString){
          return rsString!=null?rsString:"";
      }

    public static boolean serverValidateEmail(String email) {
            boolean result = true;
            String emailCheck = "^[\\w_\\-%\\.]+@[\\w_\\-%\\.]+\\.[a-zA-Z]{2,6}$";
            if(!isNullOrEmpty(email)) {
                result = email.matches(emailCheck);
            }
            return result;
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static int getDaysDiff(String Estartts, String Eendts) {
        double days = 0;
        double diffInMilleseconds = 0;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd 00:00:00");

            if(Estartts.compareTo("") != 0 && Eendts.compareTo("") != 0) {
                java.util.Date sdt = sdf.parse(Estartts);
                java.util.Date edt = sdf.parse(Eendts);

                Estartts = sdf1.format(sdt);
                Eendts = sdf1.format(edt);

                java.util.Date dt1 = sdf1.parse(Estartts);
                java.util.Date dt2 = sdf1.parse(Eendts);

                diffInMilleseconds = dt2.getTime() - dt1.getTime();
                days = Math.round(diffInMilleseconds / (1000 * 60 * 60 * 24));
            }
        } catch (ParseException ex) {
            days = 0;
			throw ServiceException.FAILURE("crmReports.getDayDiff", ex);
        } finally {
            return (int) days;
        }
    }

	public static String getSearchString( String searchString, String appendCase, String [] searchParams)
        {
            StringBuilder myResult = new StringBuilder();

            if(!isNullOrEmpty(searchString)) {
                for(int i = 0; i < searchParams.length; i++) {
                    myResult.append(" ");
                    if(i == 0) {
                        myResult.append(appendCase);
                        myResult.append(" (( ");
                    } else {
                        myResult.append(" ( ");
                    }
                    myResult.append(searchParams[i]+" like ? or "+searchParams[i]+" like ?");
                    if(i + 1 < searchParams.length) {
                        myResult.append(") ");
                    } else {
                        myResult.append(")) ");
                    }
                    if(i + 1 < searchParams.length) {
                        myResult.append(" or ");
                    }
                }
            } else {
                myResult.append(" ");
            }
            return myResult.toString();
        }

    public static double getDouble(String str) {
        double value = 0.0;
        try {
            value = Double.parseDouble(str);
        } catch (NumberFormatException e) {
        //if wrong format will be given, 0.0 will be used as default
        } catch (NullPointerException e) {
        //if no value will be given, 0.0 will be used as default
        } catch (Exception e) {
        //On any general exception, 0.0 will be used as default
        }
        return value;
    }

    public static boolean getBoolean(String str) {
        return "true".equalsIgnoreCase(str);
    }

    public static List getPagedList(List list, int start, int limit) {
        List pagedList = new ArrayList();
        if(list.size() >= start) {
            int end = Math.min(list.size(), start + limit);
            for (int i = start; i < end; i++) {
                pagedList.add(list.get(i));
            }
        } else {
            pagedList = list;
        }
        return pagedList;
    }

    public static JSONArray getPagedJSON(JSONArray jArr, int start, int limit) throws JSONException {
        JSONArray pagedjArr = new JSONArray();
        if(jArr.length() >= start) {
            int end = Math.min(jArr.length(), start + limit);
            for (int i = start; i < end; i++) {
                pagedjArr.put(jArr.getJSONObject(i));
            }
        } else {
            pagedjArr = jArr;
        }
        return pagedjArr;
    }
      public static String abbreviate(String str, int maxWidth) {
          return abbreviate(str, 0, maxWidth);
      }
      public static String abbreviate(String str, int offset, int maxWidth) {
          if (str == null) {
              return null;
          }
          if (maxWidth < 4) {
              throw new IllegalArgumentException("Minimum abbreviation width is 4");
          }
          if (str.length() <= maxWidth) {
              return str;
          }
          if (offset > str.length()) {
              offset = str.length();
          }
          if ((str.length() - offset) < (maxWidth - 3)) {
              offset = str.length() - (maxWidth - 3);
          }
          if (offset <= 4) {
              return str.substring(0, maxWidth - 3) + "...";
          }
          if (maxWidth < 7) {
              throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
          }
          if ((offset + (maxWidth - 3)) < str.length()) {
              return "..." + abbreviate(str.substring(offset), maxWidth - 3);
          }
          return "..." + str.substring(str.length() - (maxWidth - 3));
      }


      public static String getSearchquery(String ss,String[] searchcol,ArrayList params) {
            boolean success = false;
            String searchQuery = "";
        try {

                if(ss != null && !StringUtil.isNullOrEmpty(ss)){
                        StringUtil.insertParamSearchString(params, ss, searchcol.length);
                        searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                }

        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }finally{
            return searchQuery;
        }
    }

    public static KwlReturnObject getPagingquery(HashMap<String,Object> requestParams,String[] searchcol,HibernateTemplate hibernateTemplate,String hql,ArrayList params) {
            boolean success = false;
            List lst = null;
            int count = 0;
        try {
            String allflag = "true";
            if(requestParams.containsKey("allflag"))
                allflag = requestParams.get("allflag").toString();
            int start = 0;
            int limit = 0;

            if(allflag.equals("false")){
                start = Integer.parseInt(requestParams.get("start").toString());
                limit = Integer.parseInt(requestParams.get("limit").toString());
            }

            lst = HibernateUtil.executeQuery(hibernateTemplate, hql, params.toArray());
            count = lst.size();
            if(allflag.equals("false"))
                lst = HibernateUtil.executeQueryPaging(hibernateTemplate, hql, params.toArray(), new Integer[]{start, limit});
            success = true;
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }finally{
            return new KwlReturnObject(success, "", "-1", lst, count);
        }
    }

     public static boolean checkResultobjList(KwlReturnObject result){
        if(result.getEntityList()!=null && !result.getEntityList().isEmpty()){
                        return true;
        }else{
            return false;
        }
    }

     public static boolean checkpaging(HashMap<String,Object> requestParams, HttpServletRequest request){
        if(request.getParameter("start")!=null && request.getParameter("limit")!=null){
            requestParams.put("start", Integer.valueOf(request.getParameter("start")));
            requestParams.put("limit", Integer.valueOf(request.getParameter("limit")));
            requestParams.put("allflag", false);
            return true;
        }else if(requestParams.containsKey("allflag") && !(Boolean) requestParams.get("allflag")){
            requestParams.put("start", 0);
            requestParams.put("limit", 15);
            return true;
        } else {
            return false;
        }
     }

      public static String makeExternalRequest(String urlstr,String postdata){
        String result = "";
        try {
            URL url = new URL(urlstr);
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                DataOutputStream d = new DataOutputStream(conn.getOutputStream());
                String data = postdata;
                OutputStreamWriter ow = new OutputStreamWriter(d);
                ow.write(data);
                ow.close();
                BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                    StringBuilder stringbufff = new StringBuilder();
                 while ((inputLine = input.readLine()) != null){
                    stringbufff.append(inputLine);
                 }
                    result = stringbufff.toString();
                input.close();
            } catch (IOException ex) {
                System.out.print(ex);
            }

        } catch (MalformedURLException ex) {
            System.out.print(ex);
        }finally{
            return result;
        }
    }

    public static String convertToTwoDecimal(double value) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        return df.format(value);
    }

    public static LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);
        Collections.reverse(mapValues);
        LinkedHashMap sortedMap =
            new LinkedHashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();
                if (comp1.equals(comp2)){
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((String)key, (Double)val);
                    break;
                }
            }
        }
        return sortedMap;
    }
}

class ValueComparator implements Comparator {

  Map base;
  public ValueComparator(Map base) {
      this.base = base;
  }

  public int compare(Object a, Object b) {

    if((Double)base.get(a) < (Double)base.get(b)) {
      return 1;
    } else if((Double)base.get(a) == (Double)base.get(b)) {
      return 0;
    } else {
      return -1;
    }
  }
}
