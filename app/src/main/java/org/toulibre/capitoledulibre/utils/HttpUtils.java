package org.toulibre.capitoledulibre.utils;

import android.content.Context;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import org.toulibre.capitoledulibre.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Utility class to perform HTTP requests.
 *
 * @author Christophe Beyls
 */
public class HttpUtils {

	private static final int DEFAULT_TIMEOUT = 10;

	public static class HttpResult {
		// Will be null when the local content is up-to-date
		public InputStream inputStream;
		public String lastModified;
	}

	public static HttpResult get(Context context, String path, String lastModified,
								 String progressAction, String progressExtra) throws IOException {
		HttpResult result = new HttpResult();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        //noinspection ConstantConditions
        loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.HEADERS : HttpLoggingInterceptor.Level.NONE);
		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
				.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
				.addNetworkInterceptor(new ProgressInterceptor(context, progressAction, progressExtra))
                .addNetworkInterceptor(new StethoInterceptor())
                .addInterceptor(loggingInterceptor)
				.build();

        Request.Builder requestBuilder = new Request.Builder().url(path);
        if (lastModified != null) {
            requestBuilder.addHeader("If-Modified-Since", lastModified);
        }

        Response response = client.newCall(requestBuilder.build()).execute();
        if (response.isSuccessful()) {
            result.lastModified = response.header("Last-Modified");
            if (response.code() != HttpURLConnection.HTTP_NOT_MODIFIED) {
                result.inputStream = response.body().byteStream();
            }
            return result;
        } else {
            throw new IOException("Server returned response code: " + response.code());
        }
	}
}
