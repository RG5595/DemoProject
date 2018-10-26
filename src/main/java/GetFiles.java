


import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.users.FullAccount;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/GetFiles")
public class GetFiles extends HttpServlet {
    //private static final long serialVersionUID = 1L;
    private String access_token="";
    private String path="";
    public GetFiles() {}

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequests(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        handleRequests(request, response);

    }

    /**
     * A common method to handle both get and post request
     * @param request
     * @param response
     */

    private void handleRequests(HttpServletRequest request, HttpServletResponse response)  throws IOException{
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

        PrintWriter out = response.getWriter();

        if (access_token == null || access_token.isEmpty()) {
            //check if access_token is set, if not indicate failure
            response.setContentType("application/json");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("success", false);
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
                if(path==null)
                    path="";

                // Get files and folder metadata from Dropbox root directory
                ListFolderResult result = client.files().listFolder(path);


                //out.print("<html><body>");
                //out.print("<h3>Welcome, " + account.getName().getDisplayName() + "</h3>");
                //out.println("<br/><h3>" + result.toString() + "</h3>");

                response.setContentType("application/json");
                JSONObject ResultJson = new JSONObject(result.toString());
                ResultJson.put("success", true);
                out.print(ResultJson.toString());


            } catch (DbxException de) {
                // Access Token is invalid or expired
                response.setContentType("application/json");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("success", false);
                out.print(jsonObject.toString());
            }

            out.close();
        }
    }

}
