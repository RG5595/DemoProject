import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxPathV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Enumeration;
import java.util.List;

@WebServlet("/UploadFile")
public class UploadFile extends HttpServlet {

    private String access_token="";
    private String path="";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        InputStream localFile = null;

        PrintWriter out = response.getWriter();

        try {
            List items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);

            // Process the uploaded items
            for (Object item1 : items) {
                FileItem item = (FileItem) item1;

                if (item.isFormField()) {

                    String name = item.getFieldName();//text1
                    String value = item.getString();
                    if (name.equalsIgnoreCase("access_token"))
                        access_token = value;
                    else if (name.equalsIgnoreCase("path")) {
                        path = value;
                    }

                    //out.println(name + " : " + value + "<br>");

                } else {
                    localFile = item.getInputStream();
                }
            }
        } catch (FileUploadException Fe){
            response.setContentType("application/json");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("success", false);
            jsonObject.put("error_message", "File Upload exception");
            out.print(jsonObject.toString());
            out.close();
        }


        if(localFile!=null && localFile.read()>0)
        //|| localFile==null || localFile.read()<=0
        {
            if (access_token == null || access_token.isEmpty()) {
                //check if access_token is set, if not indicate failure
                response.setContentType("application/json");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("success", false);
                jsonObject.put("error_message", "access token's empty");
                out.print(jsonObject.toString());
                out.close();

            } else {

                String pathError = DbxPathV2.findError(path);
                if (pathError != null) {
                    //System.err.println("Invalid <dropbox-path>: " + pathError);
                    response.setContentType("application/json");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("success", false);
                    jsonObject.put("error_message", "Invalid path");
                    out.print(jsonObject.toString());
                    return;
                }

                if (localFile == null || localFile.read() <= 0) {
                    //set an error message indication the same?
                    //System.err.println("Invalid <local-path>: not a file.");
                    response.setContentType("application/json");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("success", false);
                    jsonObject.put("error_message", "failed to read file data");
                    out.print(jsonObject.toString());
                    return;
                }

                //Create Dropbox client
                DbxRequestConfig config = DbxRequestConfig.newBuilder("RGsGetFilesDemo").build();
                //new DbxRequestConfig("nlqz8xhj28vsqdd", "en_US");
                DbxClientV2 client = new DbxClientV2(config, access_token);


                if (path == null)
                    path = "";
                JSONObject ResultJson;
                switch (uploadFile(client, localFile, path)) {
                    case 1:
                        response.setContentType("application/json");

                        ResultJson = new JSONObject();
                        ResultJson.put("success", true);
                        out.print(ResultJson.toString());
                        break;
                    case -1:
                        //Error uploading to Dropbox
                        response.setContentType("application/json");
                        ResultJson = new JSONObject();
                        ResultJson.put("success", false);
                        ResultJson.put("error_message", "access token's empty");
                        out.print(ResultJson.toString());
                        break;
                    case -2:
                        //Error reading file
                        response.setContentType("application/json");
                        ResultJson = new JSONObject();
                        ResultJson.put("success", false);
                        ResultJson.put("error_message", "failed to read file");
                        out.print(ResultJson.toString());
                        break;
                    case -3:
                        response.setContentType("application/json");
                        ResultJson = new JSONObject();
                        ResultJson.put("success", false);
                        ResultJson.put("error_message", "Upload exception");
                        out.print(ResultJson.toString());
                        break;
                }


                out.close();
            }
        } else {
            response.setContentType("application/json");
            JSONObject  ResultJson = new JSONObject();
            ResultJson.put("success", false);
            ResultJson.put("error_message", "File not found");
            out.print(ResultJson.toString());
        }
    }


    /**
     * Uploads a file in a single request. This approach is preferred for small files since it
     * eliminates unnecessary round-trips to the servers.
     *
     * @param dbxClient Dropbox user authenticated client
     * @param localFile local file to upload
     * @param dropboxPath Where to upload the file to within Dropbox
     */
    private static int uploadFile(DbxClientV2 dbxClient, InputStream localFile, String dropboxPath) {
        try {


            FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath)
                    .withMode(WriteMode.ADD)
                    .withAutorename(true)
                    .uploadAndFinish(localFile);

            System.out.println(metadata.toStringMultiline());
        } catch (UploadErrorException ex) {
            //Error uploading to Dropbox
            return -3;

        } catch (DbxException ex) {
            //Error uploading to Dropbox
            return -1;
        } catch (IOException ex) {
            //Error reading from file \"" + localFile + "\"
            return -2;
        }

        return 1;
    }
}
