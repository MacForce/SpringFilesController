package controller;

import java.io.*;
import java.sql.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@Controller
public class FileController {

    @RequestMapping(value = "/files/{file_name}", method = RequestMethod.GET)
    public void handleFileDownload(@PathVariable("file_name") String fileName,
            HttpServletResponse response) {
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(fileName));
            org.apache.commons.io.IOUtils.copy(stream, response.getOutputStream());
            response.flushBuffer();
            response.setStatus(200);
        } catch (IOException e) {
            response.setStatus(400);
        }

    }

    @RequestMapping(value="/upload", method=RequestMethod.POST)
    public void handleFileUpload(@RequestParam("email") String email,
                         @RequestParam("password") String password, @RequestParam("file") MultipartFile file,
                         HttpServletResponse response){
        if (!file.isEmpty() && !email.isEmpty() && !password.isEmpty()) {

            //----check for authorized user------
            if (!checkUser(email, password)) {
                response.setStatus(401);
                return;
            }

            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream = 
                        new BufferedOutputStream(new FileOutputStream(new File(email + "_" + password)));
                stream.write(bytes);
                stream.close();
                response.setStatus(200);
            } catch (Exception e) {
                response.setStatus(500);
            }
        } else {
            response.setStatus(400);
        }
    }

    @RequestMapping(value="/reupload", method=RequestMethod.POST)
    public void handleFileReupload(@RequestParam("email") String email,
                             @RequestParam("password") String password, @RequestParam("file") MultipartFile file,
                             HttpServletResponse response){
        if (!file.isEmpty() && !email.isEmpty() && !password.isEmpty()) {

            //----check for authorized user------
            if (!checkUser(email, password)) {
                response.setStatus(401);
                return;
            }

            //----delete old file------
            File oldFile = new File(email + "_" + password);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(email + "_" + password)));
                stream.write(bytes);
                stream.close();
                response.setStatus(200);
            } catch (Exception e) {
                response.setStatus(500);
            }
        } else {
            response.setStatus(400);
        }
    }

    private boolean checkUser(String email, String password) {
        Connection conn = null;
        try {
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = "jdbc:mysql://127.0.0.1:3306/Game_TP";
            Class.forName(myDriver);
            conn = DriverManager.getConnection(myUrl, "root", "");

            String query = " SELECT * FROM User WHERE email = ? AND password = ? ";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, email);
            preparedStmt.setString (2, password);

            ResultSet rs = preparedStmt.executeQuery();

            if (!rs.next()) {
                return false;
            } else {
                return true;
            }
        }
        catch (Exception e) {
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
