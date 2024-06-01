package tukano.impl.proxy;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import org.pac4j.scribe.builder.api.DropboxApi20;
import java.util.List;
import tukano.api.java.Result;
import tukano.impl.api.java.ExtendedBlobs;

public class DropboxService implements ExtendedBlobs {

    private static final String apiKey = "2hv460ohvrz8dzn";
    private static final String apiSecret = "4m0r821tp0o32ma";
    private static final String accessTokenStr = "sl.B2EOqAQDB1ZJlgUergBfYzOBD0z5xtSQgifA7vqZtyWnt7KDXJw4kWTk-tWhALc9hmQY0RWINtk4feMoN5oEaoh4KkIe3y0UHeo5fVez4_lMXYinVuY7AVyL2OReE-OyGDnymThhv97T";

    private static final String UPLOAD_URL = "https://content.dropboxapi.com/2/files/upload";
    private static final String DOWNLOAD_URL = "https://content.dropboxapi.com/2/files/download";
    private static final String DELETE_URL = "https://api.dropboxapi.com/2/files/delete_v2";
    private static final String LIST_FOLDER_URL = "https://api.dropboxapi.com/2/files/list_folder";
    private static final String CONTENT_TYPE_HDR = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String AUTHORIZATION_HDR = "Authorization";
    private static final String BEARER = "Bearer ";

    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;
    private final Gson gson;

    public DropboxService() {
        gson = new Gson();
        accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey)
                .apiSecret(apiSecret)
                .build(DropboxApi20.INSTANCE);
    }
    
    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        try {
            uploadFile(blobId, bytes);
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<byte[]> download(String blobId) {
        try {
            return Result.ok(downloadFile(blobId));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> delete(String blobId, String token) {
        try {
            deleteFile(blobId);
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deleteAllBlobs(String userId, String password) {
        try {
            clearState();
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    private void clearState() throws Exception {
        // List all files in the root directory
        var listFolder = new OAuthRequest(Verb.POST, LIST_FOLDER_URL);
        listFolder.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
        listFolder.addHeader(AUTHORIZATION_HDR, BEARER + accessTokenStr);
        listFolder.setPayload("{\"path\": \"\", \"recursive\": true}");
        service.signRequest(accessToken, listFolder);

        Response listResponse = service.execute(listFolder);
        if (listResponse.getCode() != 200) {
            throw new RuntimeException("Failed to list files: " + listResponse.getMessage());
        }

        var listFolderResult = gson.fromJson(listResponse.getBody(), ListFolderResult.class);
        for (var entry : listFolderResult.entries) {
            deleteFile(entry.path_lower);
        }
    }

    private String uploadFile(String path, byte[] fileData) throws Exception {
        var uploadFile = new OAuthRequest(Verb.POST, UPLOAD_URL);
        uploadFile.addHeader("Dropbox-API-Arg", gson.toJson(new UploadFileArgs(path)));
        uploadFile.addHeader(CONTENT_TYPE_HDR, "application/octet-stream");
        uploadFile.addHeader(AUTHORIZATION_HDR, BEARER + accessTokenStr);
        uploadFile.setPayload(fileData);
        service.signRequest(accessToken, uploadFile);

        Response response = service.execute(uploadFile);
        if (response.getCode() != 200) {
            throw new RuntimeException("Failed to upload file: " + response.getMessage());
        }

        return response.getBody();
    }

    private byte[] downloadFile(String path) throws Exception {
        var downloadFile = new OAuthRequest(Verb.POST, DOWNLOAD_URL);
        downloadFile.addHeader("Dropbox-API-Arg", gson.toJson(new DownloadFileArgs(path)));
        downloadFile.addHeader(AUTHORIZATION_HDR, BEARER + accessTokenStr);
        service.signRequest(accessToken, downloadFile);

        Response response = service.execute(downloadFile);
        if (response.getCode() != 200) {
            throw new RuntimeException("Failed to download file: " + response.getMessage());
        }

        return response.getStream().readAllBytes();
    }

    private String deleteFile(String path) throws Exception {
        var deleteFile = new OAuthRequest(Verb.POST, DELETE_URL);
        deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
        deleteFile.addHeader(AUTHORIZATION_HDR, BEARER + accessTokenStr);
        deleteFile.setPayload(gson.toJson(new DeleteFileArgs(path)));
        service.signRequest(accessToken, deleteFile);

        Response response = service.execute(deleteFile);
        if (response.getCode() != 200) {
            throw new RuntimeException("Failed to delete file: " + response.getMessage());
        }

        return response.getBody();
    }

    // Nested classes for Dropbox API arguments
    private record UploadFileArgs(String path) {}
    private record DownloadFileArgs(String path) {}
    private record DeleteFileArgs(String path) {}
    private record ListFolderResult(List<Entry> entries) {}
    private record Entry(String path_lower) {}
}
