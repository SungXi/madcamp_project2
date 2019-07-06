package com.example.serverapp.Retrofit;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface IAppService {
    @POST("register")
    @FormUrlEncoded
    Observable<String> registerUser (@Field("email") String email,
                                     @Field("name") String name,
                                     @Field("password") String password);

    @POST("login")
    @FormUrlEncoded
    Observable<String> loginUser (@Field("email") String email,
                                  @Field("password") String password);

    @POST("fetch_images")
    @FormUrlEncoded
    Observable<String> fetchImages (@Field("email") String email);

    @Multipart
    @POST("upload_image_request")
    Call<ResponseBody> postImage(@Part MultipartBody.Part image,
                                 @Part("upload") RequestBody name,
                                 @Part("owner_email") RequestBody owner_email);

    @POST("upload_image")
    @FormUrlEncoded
    Observable<String> uploadImage (@Field("email") String email,
                                    @Field("path") String path,
                                    @Field("image") String base64);

    @POST("deletePerson")
    @FormUrlEncoded
    Observable<String> deletePerson (@Field("name") String name,
                                       @Field("number") String number,
                                       @Field("email") String email,
                                       @Field("owner_email") String owner_email);

    @POST("getPeople")
    @FormUrlEncoded
    Observable<String> updatePerson (@Field("owner_email") String owner_email);

    @POST("addPerson")
    @FormUrlEncoded
    Observable<String> addPerson (@Field("name") String name,
                                  @Field("number") String number,
                                  @Field("email") String email,
                                  @Field("owner_email") String owner_email);

    @POST("editPerson")
    @FormUrlEncoded
    Observable<String> editPerson (@Field("origin_name") String origin_name,
                                   @Field("origin_number") String origin_number,
                                   @Field("origin_email") String origin_email,
                                   @Field("new_name") String new_name,
                                   @Field("new_number") String new_number,
                                   @Field("new_email") String new_email,
                                   @Field("owner_email") String owner_email);

    @POST("removePeople")
    @FormUrlEncoded
    Observable<String> removeAll (@Field("owner_email") String owner_email);
}
