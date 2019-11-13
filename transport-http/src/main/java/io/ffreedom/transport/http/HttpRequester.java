package io.ffreedom.transport.http;

import java.io.IOException;

import org.slf4j.Logger;

import io.ffreedom.common.annotations.lang.MayThrowsRuntimeException;
import io.ffreedom.common.log.CommonLoggerFactory;
import io.mercury.persistence.json.JsonWrapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpRequester {

	protected Logger logger = CommonLoggerFactory.getLogger(getClass());

	private final OkHttpClient client = new OkHttpClient();

	public static final HttpRequester INSTANCE = new HttpRequester();

	public HttpRequester() {
	}

	@MayThrowsRuntimeException
	public String httpGet(String url) {
		Request request = new Request.Builder().url(url).build();
		try (Response response = client.newCall(request).execute()) {
			if (response == null)
				throw new RuntimeException("RuntimeException -> Request Url: [" + url + "]");
			if (response.code() > 307)
				throw new RuntimeException(
						"RuntimeException -> Request Url: [" + url + "], return status code: " + response.code());
			ResponseBody body = response.body();
			if (body == null)
				return "";
			return body.string();
		} catch (IOException e) {
			logger.error("IOException -> {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private static final MediaType APPLICATION_JSON = MediaType.get("application/json; charset=utf-8");

	@MayThrowsRuntimeException
	public String httpJsonPost(String url, Object obj) throws IOException {
		RequestBody body = RequestBody.create(JsonWrapper.toJson(obj), APPLICATION_JSON);
		Request request = new Request.Builder().url(url).post(body).build();
		try (Response response = client.newCall(request).execute()) {
			if (response == null)
				throw new RuntimeException("RuntimeException -> Request Url: [" + url + "]");
			if (response.code() > 307)
				throw new RuntimeException(
						"RuntimeException -> Request Url: [" + url + "] return status code: " + response.code());
			ResponseBody rtnBody = response.body();
			if (rtnBody == null)
				return "";
			return rtnBody.string();
		} catch (IOException e) {
			logger.error("IOException -> {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

}
