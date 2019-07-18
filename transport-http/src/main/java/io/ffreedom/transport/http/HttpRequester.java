package io.ffreedom.transport.http;

import java.io.IOException;

import org.slf4j.Logger;

import io.ffreedom.common.annotations.lang.MayThrowRuntimeException;
import io.ffreedom.common.log.CommonLoggerFactory;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequester {

	protected Logger logger = CommonLoggerFactory.getLogger(getClass());

	private final OkHttpClient client = new OkHttpClient();

	public static final HttpRequester INSTANCE = new HttpRequester();

	public HttpRequester() {
	}

	@MayThrowRuntimeException
	public String httpGet(String url) {
		Request request = new Request.Builder().url(url).build();
		try (Response response = client.newCall(request).execute()) {
			if (response.code() > 307)
				throw new RuntimeException(
						"RuntimeException -> Request Url: [" + url + "] return status code: " + response.code());
			return response.body().string();
		} catch (IOException e) {
			logger.error("IOException -> {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private static final MediaType APPLICATION_JSON = MediaType.get("application/json; charset=utf-8");

	@MayThrowRuntimeException
	public String httpJsonPost(String url, Object obj) throws IOException {
		RequestBody body = RequestBody.create(JsonSerializationUtil.objToJson(obj), APPLICATION_JSON);
		Request request = new Request.Builder().url(url).post(body).build();
		try (Response response = client.newCall(request).execute()) {
			if (response.code() > 307)
				throw new RuntimeException(
						"RuntimeException -> Request Url: [" + url + "] return status code: " + response.code());
			return response.body().string();
		} catch (IOException e) {
			logger.error("IOException -> {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

}
