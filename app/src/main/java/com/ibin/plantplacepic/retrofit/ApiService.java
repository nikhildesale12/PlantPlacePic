package com.ibin.plantplacepic.retrofit;

import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.bean.UploadResponse;
import com.ibin.plantplacepic.bean.UserDetailResponseBean;
import com.ibin.plantplacepic.utility.Constants;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by NN on 30/01/2017.
 */

public interface ApiService {

    @GET(Constants.LOGIN_SERVICE_URL)
    Call<LoginResponse> loginService(
            @Query("USER") String USER,
            @Query("PASS") String PASS,
            @Query("GOOG_ID") String GOOG_ID
    );

    @GET(Constants.POST_FEEDBACK_DATA)
    Call<LoginResponse> postFeedBackData(
            @Query("USERNAME") String USERNAME,
            @Query("COMMENT") String COMMENT,
            @Query("RATING") String RATING
    );

    @GET(Constants.REGISTER_SERVICE_URL)
    Call<LoginResponse> registerUserService(
            @Query("USER") String USER,
            @Query("PASS") String PASS,
            @Query("GOOG_ID") String GOOG_ID
    );

    @GET(Constants.UPLOAD_DATA_SERVICE_URL)
    Call<LoginResponse> dataUploadService(
            @Query("USERID") String USERID,
            @Query("IMAGE") String IMAGE,
            @Query("SPECIES") String SPECIES,
            @Query("REMARK") String REMARK,
            @Query("TAG") String TAG,
            @Query("STATUS") String STATUS,
            @Query("TITLE") String TITLE,
            @Query("LAT") String LAT,
            @Query("LNG") String LNG,
            @Query("ADDRESS") String ADDRESS,
            @Query("CROP") String CROP,
            @Query("TIME") String TIME,
            @Query("UPLOAD_FROM") String UPLOAD_FROM
    );

    @Multipart
    @POST(Constants.UPLOAD_SERVICE_URL)
    Call<UploadResponse> uploadFile(@Part MultipartBody.Part file, @Part("file") RequestBody name);

    @GET(Constants.DOWNLOAD_DATA_SERVICE_URL)
    Call<InformationResponseBean> downloadDataById(
            @Query("USERID") String USERID
    );

//    @GET(Constants.DOWNLOAD_ALL_DATA_SERVICE_URL)
//    Call<InformationResponseBean> downloadAllData(
//    );

    @GET(Constants.GET_ALL_DATA_SERVICE_URL)
    Call<InformationResponseBean> getAllSpeciesDetail(
    );

    @GET(Constants.GET_DETAIL_BY_SPECIES_NAME)
    Call<InformationResponseBean> getAllSpeciesDetailBySpeciesName(
            @Query("SPECIES") String SPECIES
    );

    @GET(Constants.GET_COUNT_SERVICE_URL)
    Call<LoginResponse> getUplodCount(
            @Query("USERID") String USERID
    );

    @GET(Constants.UPDATE_DATA_SERVICE_URL)
    Call<LoginResponse> dataUpdateService(
            @Query("USERID") String USERID,
            @Query("IMAGE") String IMAGE,
            @Query("SPECIES") String SPECIES,
            @Query("REMARK") String REMARK,
            @Query("TAG") String TAG,
            @Query("TITLE") String TITLE,
            @Query("serverFolderPath") String serverFolderPath,
            @Query("serverFolderPathFrom") String serverFolderPathFrom,
            @Query("ADDRESS") String ADDRESS
    );

    @GET(Constants.DELET_DATA_SERVICE_URL)
    Call<LoginResponse> deleteDataService(
            @Query("USERID") String USERID,
            @Query("IMAGENAME") String IMAGENAME
    );

    @GET(Constants.MOVE_SPECIES_SERVICE_URL)
    Call<LoginResponse> moveUpdateService(
            @Query("USERID") String USERID,
            @Query("IMAGENAME") String IMAGENAME,
            @Query("FROMSPECIES") String FROMSPECIES,
            @Query("TOSPECIES") String TOSPECIES
    );

    @GET(Constants.RENAME_SPECIES_SERVICE_URL)
    Call<LoginResponse> renameSpeciesService(
            @Query("IMAGENAME") String IMAGENAME,
            @Query("FROMSPECIES") String FROMSPECIES,
            @Query("TOSPECIES") String TOSPECIES
    );

    @GET(Constants.UPDATE_USER_DEATAIL_SERVICE_URL)
    Call<UserDetailResponseBean> updateUserDetail(
            @Query("userId") String userId,
            @Query("firstName") String firstName,
            @Query("middleName") String middleName,
            @Query("LastName") String LastName,
            @Query("occupation") String occupation,
            @Query("mobileNum") String mobileNum
    );
    @GET(Constants.USER_PROFILE_SERVICE_URL)
    Call<UserDetailResponseBean> getUserProfile(
            @Query("userId") String USERID
    );
    @GET(Constants.CHECK_VERSION_SERVICE_URL)
    Call<LoginResponse> getAppVersion(
    );
}
