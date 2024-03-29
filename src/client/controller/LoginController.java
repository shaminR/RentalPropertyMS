package client.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;

import client.view.LandlordView;
import client.view.ListingsView;
import client.view.LoginPasswordView;
import client.view.LoginView;
import client.view.ManagerView;
import client.view.*;

public class LoginController {
	
	private ClientCommunicator comms;
	private PrintWriter socketOut;
    private BufferedReader socketIn;
	private MyListener listener;
	private ListingsView listings;
	private LoginView view;
	private LoginPasswordView passwordView;
	private ManagerView managerView; 
	private LandlordView landlordView;
	private SignUp signUpView;
	private LoginEnum loginType;
	
	public LoginController(Client c) {
		
        loginType = LoginEnum.DEFAULT;
        
        comms = c.communicator;
        view = c.loginView;
        listings = c.listings;
        passwordView = c.passwordView;
        managerView = c.managerView;
        landlordView = c.landlordView;
        signUpView = c.signUpView;
        listener = new MyListener();
        addListeners();
	}
	
	private void writeSocket(String s) {
		comms.socketOut.println(s);	
		comms.socketOut.flush();
	}
	
	private String readSocket() {
		try {
			return comms.socketIn.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("error in readSocket");
		}
		return null;
	}
	
	private void addListeners() {
		view.addUserTypeListener(listener);
		passwordView.addLoginListener(listener);
	}

	class MyListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			try {
				
				if(event.getSource() == view.managerButton) {
					loginType = LoginEnum.MANAGER;
					view.setVisible(false);
					passwordView.setVisible(true);
					
				}
				else if(event.getSource() == view.landlordButton) {
					loginType = LoginEnum.LANDLORD;
					view.setVisible(false);
					passwordView.setVisible(true);
				}
				else if(event.getSource() == view.renterButton) {
					listings.setVisible(true);
					listings.setSubscriptionButtonState(false);
					listings.registered = false;
					listings.updateButton.doClick();
					view.setVisible(false);
				}
				else if(event.getSource() == view.regRenterButton) {
					loginType = LoginEnum.REG_RENTER;
					view.setVisible(false);
					passwordView.setVisible(true);
				}
				else if(event.getSource() == view.newUser) {
					view.setVisible(false);
					signUpView.setVisible(true);
				}
				else if(event.getSource() == passwordView.loginButton) {
					String username = passwordView.getUserName();
                    String pass = passwordView.getPassword();
                    
					if(username.equals("") || pass.equals("")){
                        System.out.println("error");
                        passwordView.errorMessage("Please Enter Username and Password");
                        return;
                    }
					
					writeSocket("4");
					String send = username + "é" + pass + "é" + loginType.getCode();
					writeSocket(username + "é" + pass + "é" + loginType.getCode());
					String valid = readSocket();
					if(!valid.equals("yes")) {
						passwordView.errorMessage("username or password incorrect, please try again");
						return;
					}
					
					if(loginType == LoginEnum.MANAGER) {
						managerView.setVisible(true);
						managerView.listPropertiesBtn.doClick();
						passwordView.clearText();
						passwordView.setVisible(false);
					}
					if(loginType == LoginEnum.LANDLORD) {
						landlordView.setVisible(true);
						landlordView.setUsername(username);
						landlordView.showPropertiesBtn.doClick();
						passwordView.clearText();
						passwordView.setVisible(false);
					}
					if(loginType == LoginEnum.REG_RENTER) {
						listings.setVisible(true);
						listings.setSubscriptionButtonState(true);
						listings.registered = true;
						listings.username = username;
						listings.updateButton.doClick();
						passwordView.clearText();
						passwordView.setVisible(false);
					}
				}
				else if(event.getSource() == passwordView.backButton) {
					passwordView.setVisible(false);
					passwordView.clearText();
					view.setVisible(true);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error in LoginController");
			}
		}
	}
}
