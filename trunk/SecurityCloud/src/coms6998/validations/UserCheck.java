package coms6998.validations;

import java.util.Random;

import coms6998.security.SecuredCloudManager;
import coms6998.security.User;

public class UserCheck {

    private static boolean isAuthenticated = false;
    private static boolean otpAuthenticated = false;

    private static int count = 0;
    private static int otpCount = 0;

    public static boolean check(String username, String password) {
        User user = SecuredCloudManager.getUser(username);
        isAuthenticated = (user != null) && user.getPassword().equals(password);
        if(isAuthenticated) {
            String random = "" + new Random().nextInt(10000);
            user.setOTP(random);
            System.out.println("In sending mail");
            OTPMailSender.sendMail(user);
            System.out.println("Sent mail");
        }
        return isAuthenticated;
    }

    public static boolean isAuthenticated() {
        return isAuthenticated;
    }
    public static boolean checkOTP(String username, String otp) {
        User user = SecuredCloudManager.getUser(username);
        otpAuthenticated = user.getOTP().equals(otp);
        return otpAuthenticated;
    }

    public static boolean otpAuthenticated() {
        return otpAuthenticated;
    }

    public static int getCount() {
        return count++;
    }

    public static int getOTPCount() {
        return otpCount++;
    }
}
