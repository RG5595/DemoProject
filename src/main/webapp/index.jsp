<html>
<body>
<h2>GetFiles, Click the button below to generate access token!</h2>
<button
        onclick="location.href = 'https://www.dropbox.com/oauth2/authorize?client_id=nlqz8xhj28vsqdd' +
         '&response_type=token' +
          '&redirect_uri=http://localhost:8080';" id="dropbox_con" class="float-left submit-button" >Connect to Dropbox
</button>
<br/>
<br/>
<div>
<h2>Upload file to a specific folder</h2>
<form action='/UploadFile' method='post' enctype='multipart/form-data'>
    <input type='file' name='localFile'/>
    <br/>access_token: <input type='text' name='access_token' value=''/>
    <br/>path (with file name): <input type='text' name='path' value=''/>
    <input type='submit'/>
</form>
</div>


<br/>
<br/>
<div>
    <h2>Download a specific file</h2>
    <form action='/DownloadFile' method='post'>
        <br/>access_token: <input type='text' name='access_token' value=''/>
        <br/>Full path to file: <input type='text' name='path' value=''/>
        <input type='submit'/>
    </form>
</div>
</body>
</html>
