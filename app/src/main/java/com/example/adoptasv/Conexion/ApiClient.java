package com.example.adoptasv.Conexion;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://api-adoptasv-production.up.railway.app/api/";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static ApiService getService() {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService.class);
        }
        return apiService;
    }

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(buildOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static OkHttpClient buildOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(new FirebaseTokenInterceptor())
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    // Interceptor que agrega el Bearer token de Firebase en cada request
    static class FirebaseTokenInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                return chain.proceed(chain.request());
            }

            // Obtener token de forma síncrona (estamos en hilo de OkHttp, no en UI)
            final String[] token = {null};
            final Object lock = new Object();

            user.getIdToken(false).addOnCompleteListener(task -> {
                synchronized (lock) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        token[0] = task.getResult().getToken();
                    }
                    lock.notifyAll();
                }
            });

            synchronized (lock) {
                if (token[0] == null) {
                    try { lock.wait(5000); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (token[0] == null) {
                return chain.proceed(chain.request());
            }

            Request request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + token[0])
                    .addHeader("Accept", "application/json")
                    .build();

            return chain.proceed(request);
        }
    }
}