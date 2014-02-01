package at.workflow.webdesk.tools;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QueryStringUtils {

	public static String encodeToUTF8(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			// this will never happen :-)
		}
		return str;
	}

	public static String decodeFromUTF8(String str) {
		try {
			return URLDecoder.decode(str, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			// this will never happen :-)
		}
		return str;
	}

	/** build querystring of the passed request parameters map */
	public static String buildQueryString(Map<String, String> requestParams) {
		String queryString = "";
		Iterator<String> itr = requestParams.keySet().iterator();
		boolean first = true;
		while (itr.hasNext()) {
			String paramKey = itr.next();
			String key = encodeToUTF8(paramKey);
			// original paramKey must be used for param retrieval
			String value = encodeToUTF8(requestParams.get(paramKey));

			if (first) {
				first = false;
				queryString += key + "=" + value;
			} else {
				queryString += "&" + key + "=" + value;
			}
		}
		return queryString;
	}

	/** add an URL parameter to the passed url, if the URL parameter is already present, it is replaced! */
	public static String addUrlParameter(String url, String paramname, String paramvalue) {
		if (url != null) {
			if (getRequestParamsFromCompleteUrl(url).containsKey(paramname)) {
				url = removeUrlParameter(url, paramname);
			}
			if (url.indexOf("?") > 0) {
				url += "&" + encodeToUTF8(paramname) + "=" + encodeToUTF8(paramvalue);
			} else {
				url += "?" + encodeToUTF8(paramname) + "=" + encodeToUTF8(paramvalue);
			}
		}
		return url;
	}

	/** extract Request parameter map out of a complete URL, ignores the hash part! */
	public static Map<String, String> getRequestParamsFromCompleteUrl(String url) {
		Map<String, String> ret = new HashMap<String, String>();

		String queryString = "";
		if (url != null && url.indexOf("?") > 0) {

			queryString = url.substring(url.indexOf("?") + 1, url.length());

			// remove part after #
			if (queryString.indexOf("#") > -1) {
				queryString = queryString.substring(0, queryString.indexOf("#"));
			}

			if (queryString.length() > 0) {
				String[] paramTupples = queryString.split("&");
				for (int i = 0; i < paramTupples.length; i++) {

					if (!"".equals(paramTupples[i])) {

						String key = decodeFromUTF8(paramTupples[i].split("=")[0]);

						String value = "";
						if (paramTupples[i].split("=").length == 2) {
							value = decodeFromUTF8(paramTupples[i].split("=")[1]);
						}

						if (!ret.containsKey(key) || ret.get(key) == null || "".equals(ret.get(key)))
							ret.put(key, value);
						else {
							// append value to existing value of the key
							String newStr = ret.get(key) + "," + value;
							ret.put(key, newStr);
						}
					}

				}
			}

		}
		return ret;

	}

	/** remove a url parameter from the passed url and return the new stripped down url */
	public static String removeUrlParameter(String url, String paramname) {
		Map<String, String> params = getRequestParamsFromCompleteUrl(url);
		if (!params.containsKey(paramname)) {
			return url;
		}
		params.remove(paramname);

		return getUrlPartOnly(url) + (params.size() > 0 ? ("?" + buildQueryString(params)) : "");
	}

	public static String getUrlPartOnly(String url) {
		if (url.indexOf("?") > -1) {
			return url.substring(0, url.indexOf("?"));
		}
		if (url.indexOf("#") > -1) {
			return url.substring(0, url.indexOf("#"));
		}
		return url;
	}

	public static String getRequestParamsFromMap(Map<String, String> requestParamMap) {
		StringBuffer params = new StringBuffer();

		boolean first = true;
		for (Object key : requestParamMap.keySet()) {

			String keyS = (String) key;
			if (key != null && (keyS.startsWith("sort_") || keyS.startsWith("search_")
					|| keyS.equals("showPage") || keyS.equals("pageSize"))) {

				if (first)
					first = false;
				else
					params.append("&");

				String value = requestParamMap.get(key);
				if (value != null && (value.indexOf('<') > -1 || value.indexOf('>') > -1 || value.indexOf('=') > -1))
					// transform for use in url
					value = value.replaceAll("<=", "_le_").replaceAll(">=", "_ge_").replaceAll("<", "_lt_").replaceAll(">", "_gt_").replaceAll("=", "_eq_").replaceAll(" ", "");

				params.append(key).append("=").append(value);
			}
		}

		return params.toString();
	}

}
