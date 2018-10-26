import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v1.DbxEntry;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxPathV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetTemporaryLinkResult;
import com.dropbox.core.v2.files.ListFolderResult;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Enumeration;


@WebServlet("/DownloadFile")
public class DownloadFile extends HttpServlet {

    private String access_token="";
    private String path="";
    public DownloadFile() {}

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        Enumeration e = request.getParameterNames();

        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            String fieldName = (String) obj;
            String fieldValue = request.getParameter(fieldName);
            if (fieldName.equalsIgnoreCase("access_token"))
                access_token = fieldValue;
            else if (fieldName.equalsIgnoreCase("path")) {
                path = fieldValue;
            }

            //out.println(fieldName + " : " + fieldValue + "<br>");
        }

        if (access_token == null || access_token.isEmpty() || path==null || path.isEmpty()) {
            //check if access_token is set, if not indicate failure
            response.setContentType("application/json");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("success", false);
            jsonObject.put("error_message", "Access token/Path is missing");
            out.print(jsonObject.toString());
            out.close();

        } else {

            //Create Dropbox client
            DbxRequestConfig config = DbxRequestConfig.newBuilder("RGsGetFilesDemo").build();
            //new DbxRequestConfig("nlqz8xhj28vsqdd", "en_US");
            DbxClientV2 client = new DbxClientV2(config, access_token);


            try {
                // Get current account info
                //FullAccount account = client.users().getCurrentAccount();
                //System.out.println(account.getName().getDisplayName());
                //response.setContentType("text/plain");

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

                GetTemporaryLinkResult downloader = client.files().getTemporaryLink(path);


                    response.setContentType("application/json");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("success", true);
                    jsonObject.put("file_at", downloader.getLink());
                    out.print(jsonObject.toString());



            } catch (DbxException de) {
                // Access Token is invalid or expired
                response.setContentType("application/json");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("success", false);
                System.out.println(de.getMessage());
                jsonObject.put("error_message", "dbxException: Check the file path");
                out.print(jsonObject.toString());
            }

            out.close();
        }
    }

}
