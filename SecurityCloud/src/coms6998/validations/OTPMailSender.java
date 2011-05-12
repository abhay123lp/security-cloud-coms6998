package coms6998.validations;

import java.io.UnsupportedEncodingException;
import java.util.Properties;  
import javax.mail.Message; 
import javax.mail.MessagingException; 
import javax.mail.Session; 
import javax.mail.Transport; 
import javax.mail.internet.AddressException; 
import javax.mail.internet.InternetAddress; 
import javax.mail.internet.MimeMessage;  

import coms6998.security.User;

public class OTPMailSender {

    private static final String MAIL_ID = "asthamalik8@gmail.com";
    
    public static void sendMail(User user) {
        Properties props = new Properties();        
        Session session1 = Session.getDefaultInstance(props, null);          
        try {             
            Message msg = new MimeMessage(session1);      
            msg.setFrom(new InternetAddress(MAIL_ID, "Administrator"));       
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getUsername(), user.getUsername()));             
            msg.setSubject("OTP Password");
            msg.setText(user.getOTP());
            Transport.send(msg);   
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(MAIL_ID, "Astha"));
            Transport.send(msg);          
        } 
        catch (AddressException e) {             
            System.out.println(e);        
        } 
        catch (MessagingException e) {             
            System.out.println(e) ;        
        } 
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
