package com.example.adoptasv.Conexion;

import com.google.android.gms.tasks.Tasks;
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
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .callTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    static class FirebaseTokenInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Request original = chain.request();

            if (user == null) {
                return chain.proceed(original);
            }

            String token = null;
            try {
                token = Tasks.await(user.getIdToken(false)).getToken();
            } catch (Exception e) {
            }

            Request.Builder builder = original.newBuilder()
                    .addHeader("Accept", "application/json");
            if (token != null) {
                builder.addHeader("Authorization", "Bearer " + token);
            }

            return chain.proceed(builder.build());
        }
    }
}